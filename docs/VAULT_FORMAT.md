# Vault format v1 (BIP39-SDK-GROK)

**Status:** normative for release 1.0  
**Audience:** implementers (Kotlin now, Rust core preferred)  
**Related audit items:** P0-1 … P0-7

This document is the single source of truth for on-disk vault bytes.  
Implement against these vectors; do not invent alternate field layouts.

---

## 1. Goals

1. **PIN is not the encryption key stored next to ciphertext.**  
   Random DEK encrypts secrets; PIN derives a KEK that only *wraps* DEK.
2. **Wrong PIN ⇒ AEAD failure**, not a separate comparable hash of the PIN-as-key.
3. **Device binding:** KEK incorporates a non-exportable Keystore pepper (Android) / Secure Enclave (iOS).
4. **Panic PIN** uses a separate domain-separated verifier (HMAC), constant-time compare.
5. **Entropy in vault**, not mnemonic UTF-8 string (fewer `String` copies at rest).
6. **Single authenticated blob** with versioned header as AAD.

---

## 2. File location

| Platform | Path |
|----------|------|
| Android | `context.noBackupFilesDir` / `bip39_vault_v1.bin` (never cloud backup) |
| iOS | Keychain / app-group file excluded from iCloud backup |

Do **not** store vault material in backed-up `SharedPreferences` / DataStore alone.

---

## 3. Binary layout

```text
┌─────────────────────────────────────────────┐
│ Header (cleartext, also AEAD AAD)           │
│  magic[4] = 'B' '3' '9' 'V'                 │
│  version: u16 BE = 1                        │
│  kdf_id: u8 = 1 (Argon2id)                  │
│  argon_m_kib: u32 BE                        │
│  argon_t: u32 BE                            │
│  argon_p: u32 BE = 1                        │
│  salt[16]                                   │
│  flags: u16 BE                              │
│    bit0 has_passphrase                      │
│    bit1 bio_wrap_present                    │
│    bit2 pepper_strongbox                    │
│  header_len: u16 BE (bytes of this header)  │
├─────────────────────────────────────────────┤
│ wrap_pin: AEAD(KEK, DEK)                    │
│  nonce[12] ‖ ciphertext[32+tag]             │
├─────────────────────────────────────────────┤
│ wrap_bio?: AEAD(BioKey, DEK) optional       │
├─────────────────────────────────────────────┤
│ body: AEAD(DEK, plaintext, aad=header)      │
│  nonce[12] ‖ ciphertext                     │
├─────────────────────────────────────────────┤
│ panic_mac[32]: HMAC-SHA256(panic_key, salt) │
└─────────────────────────────────────────────┘
```

AEAD algorithm for 1.0: **AES-256-GCM** (nonce 12, tag 16).  
Alternative allowed later: ChaCha20-Poly1305 with same sizes (new `kdf_id` / `aead_id`).

### Body plaintext (after DEK decrypt)

```text
entropy_len: u8          // 16 or 32
entropy[entropy_len]
word_count: u8           // 12,15,18,21,24
passphrase_check[4]      // HMAC-SHA256(seed_with_passphrase, "bip39-sdk/pp-check/v1")[0..4]
fingerprint[4]           // HMAC-SHA256(seed_no_passphrase, "bip39-sdk/fingerprint/v1")[0..4]
created_unix: u64 BE
```

Mnemonic is **re-derived** from entropy with BIP39; never store UTF-8 phrase at rest.

---

## 4. Key hierarchy

```text
PIN (user)
  → Argon2id(PIN, salt, m, t, p) → ikm[32]
  → pepper = HMAC-SHA256(KeystoreNonExtractableKey, salt)   // device binding
  → KEK = HKDF-SHA256(ikm ‖ pepper, info="bip39-sdk/kek/v1", len=32)

DEK = CSPRNG(32)   // once at create

body_key = DEK
wrap: AEAD_Encrypt(KEK, DEK)

panic_key = HKDF-SHA256(Argon2id(panicPIN, salt) ‖ pepper, info="bip39-sdk/panic/v1", len=32)
panic_mac = HMAC-SHA256(panic_key, salt ‖ "panic")
```

**Verification:**

1. If `HMAC(panic_attempt) == panic_mac` (constant-time) → wipe vault (same timing as wrong PIN path as far as practical).
2. Else try unwrap DEK with KEK from PIN; AEAD fail → WrongPin.
3. Decrypt body; verify passphrase_check if flag set.

**Do not store** `hashPin(PIN)` derived from the same material as KEK next to ciphertext (legacy bug).

---

## 5. Argon2id parameters

Calibrate on device for **300–600 ms**; persist actual `m,t,p` in header.

| Bound | Value |
|-------|--------|
| Minimum memory | 32 MiB (`m_kib >= 32768`) |
| Minimum iterations | `t >= 2` (prefer ≥ 3) |
| Parallelism | `p = 1` on mobile |
| Reject on read | if `m` or `t` below minimum → Corrupted / refuse (anti-downgrade) |

Until Argon2 is wired in Rust/native, transitional Kotlin may use PBKDF2-HMAC-SHA512 with **≥ 600_000** iterations **only** for migration tooling; new vaults must be Argon2id.

---

## 6. Fingerprint v1 (UI)

```text
fp = HMAC-SHA256(key="bip39-sdk/fingerprint/v1", seed)[0..4]  // 8 hex chars
```

**Not** the first 8 bytes of the raw BIP39 seed (legacy).  
Display may map to 2–3 words or grouped hex; version field in UI metadata = 1.

Legacy golden vector `abandon…about` seed prefix `5eb00bbd…` remains a **seed** test, not a fingerprint test.

---

## 7. Passphrase check

At setup with passphrase `P`:

```text
seed = BIP39_Seed(mnemonic, P)
check = HMAC-SHA256(key="bip39-sdk/pp-check/v1", seed)[0..4]
```

At unlock, recompute; mismatch → dedicated error `WrongPassphrase` (not silent empty wallet).

---

## 8. States

```text
NoVault | Locked | Unlocked(session) | LockedOut(untilElapsed) | Wiped | Corrupted | KeyLost
```

- **KeyLost:** Keystore pepper gone (new device / app restore without migration) → offer seed restore UI; do not crash.
- **Wipe order:** delete Keystore aliases → delete vault file → clear session → `onVaultWiped`.
- Lockout counters use **elapsedRealtime** (or monotonic), never wall clock alone.

---

## 9. Migration from legacy (pre-v1)

Legacy layout (conceptual): ESP/Tink prefs + `EncryptedPayload(ciphertext, salt, iv)` + `pinHash`/`panicHash` beside ciphertext.

On first successful legacy unlock:

1. Decrypt mnemonic with legacy PIN path.
2. Create vault v1 (new DEK, wraps, body).
3. Write v1 file; crypto-erase legacy prefs/keysets.
4. If step 3 fails, leave legacy intact.

Ship fixture-based tests for migration.

---

## 10. Test vectors (normative intent)

Implementers must add machine-readable vectors under `library/src/test/resources/vault_v1/`.

| ID | Description |
|----|-------------|
| `tv_header_parse` | Valid header round-trip |
| `tv_bitflip_body` | Any bit flip in body ⇒ AEAD fail |
| `tv_wrong_pin` | Wrong PIN ⇒ unwrap fail |
| `tv_panic` | Panic PIN ⇒ wipe semantics |
| `tv_pp_check` | Wrong passphrase ⇒ WrongPassphrase |
| `tv_fp_v1` | Known mnemonic → fingerprint v1 hex |
| `tv_legacy_migrate` | Legacy blob → v1 |

**BIP39:** full Trezor vectors including 24-word + passphrase (seed only).

---

## 11. Implementation order

1. This spec + empty test resource stubs.  
2. Kotlin `VaultV1` writer/reader (or Rust `vault` crate) behind interface.  
3. Wire Android Keystore pepper + bio wrap.  
4. Migrate UI to `VaultState` / session; remove stored PIN-hash-as-key.  
5. Rust port of same format; UniFFI.

Until v1 is live, document residual risk of legacy storage in SECURITY.md.

---

## 12. Non-goals for vault v1

- Multi-user profiles in one file (future).  
- Cloud sync of vault.  
- Storing mnemonic UTF-8 at rest.  
- KYC or identity binding.


## 13. Rust implementation status

- **Argon2id + HKDF + panic MAC:** `bip39_engine::kdf` (normative `kdf_id = 1`).
- **C ABI:** `bip39_argon2id_ikm`, `bip39_derive_kek`, `bip39_panic_mac`.
- **Transitional Kotlin:** may still write `kdf_id = 2` (PBKDF2-HMAC-SHA512 ≥ 600k) until native is packaged in the AAR.
