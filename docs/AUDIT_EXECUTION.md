# Audit execution log (toward 1.0)

Source: external audit 2026-11-17. Updated continuously.

## Status matrix

| ID | Topic | Status |
|----|--------|--------|
| Spec | VAULT_FORMAT.md | **Done** |
| P0-1 | Envelope DEK | **Done** — v1-only write; legacy migrate-on-unlock |
| P0-2 | Device pepper + lockout + wipe-N | **Done** — pepper, elapsedRealtime, wipe after 10 fails (configurable) |
| P0-3 | Biometrics unlock | **Done (PIN seal)** — not DEK-wrap in vault file; device-bound |
| P0-4 | noBackup / KeyLost | **Partial** — v1 in noBackupFilesDir |
| P0-5 | CharArray secrets | **Partial** — PinKeypad + word entry; passphrase still String briefly |
| P0-6 | Fingerprint HMAC | **Done** |
| P0-7 | Passphrase check | **Done on v1** |
| P1-1 | Panic deadlock | **Done** |
| P1-2 | Restore wordCount | **Done** |
| P1-3 | Main-thread crypto | **Done** — unlock + setup on Default |
| P1-4 | Payload delivery | **Done** |
| P1-5 | FLAG_SECURE | **Done** |
| P1-6 | IME | **Mostly done** — PIN keypad, word entry, passphrase flags |
| P1-7 | Clipboard | **Done** |
| P1-8 | PDF hygiene | **Partial** — subdir, purge, delayed delete |
| P1-9 | CAMERA optional | **Partial** — `Bip39Features.enableQrRestore`; deps still in library |
| P1-10 | Rust FFI safety | **Mostly done** — catch_unwind all C ABI; golden self-test → JVM fallback |
| P1-11 | Single vault blob | **Done for new installs** |
| P1-12 | Step progress + back | **Done** |
| P1-13 | Quiz difficulty | **Done** — type-in + similar distractors |
| P1-14 | Session lock | **Done** — SessionLockEffect |

## Word-list validation

- `WordListValidator` — count, membership, checksum, structured errors
- `MnemonicWordEntry` — per-word + autocomplete
- Restore default = word-by-word; paste optional

## Feature flags (`Bip39Features`)

```kotlin
Bip39Features.enableQrRestore = false  // hide QR, avoid prompting CAMERA
Bip39Features.enablePdfExport = false
Bip39Features.wipeAfterFailedPinAttempts = 10
```

## Still blocking marketing “1.0 secure SDK”

1. ~~Argon2id~~ **Done** — Rust + JNI + VaultV1 writes `kdf_id=1` when `.so` present; unlock reads 1|2
2. Split optional modules (QR/CameraX not on compile classpath by default)
3. External audit of vault byte format
4. True bio DEK wrap inside vault file (optional hardening beyond PIN seal)

## Residual risks (honest)

- Legacy dual-write removed; migrate leaves panic disabled until user re-sets
- JVM heap may retain secrets; CharArray policy is best-effort
- Biometric path seals PIN, not DEK — compromise of sealed blob + bio = PIN

## Argon2id (Rust)

- `rust/bip39-engine/src/kdf.rs` — all 11 unit tests passed
- C ABI + JNI exported (`RustNativeKdf`)

## Argon2id integration (Kotlin)

- `RustNativeKdf` → JNI → `libbip39_engine_ffi.so`
- `VaultCrypto.preferArgon2id()` when native loads
- New vaults: Argon2id (32 MiB / t=3 / p=1); legacy PBKDF2 still unlocks
- Missing `.so` + Argon2id blob → `VaultError.Other` (no silent downgrade)
