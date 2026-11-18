# Audit re-check brief — BIP39-SDK-GROK (full tree)

**Date:** 2026-11-19  
**Repo:** https://github.com/PerfectFriend/BIP39-SDK-GROK  
**Branch:** `main`  
**Context:** Previous audit (2026-11-18) correctly found that GitHub lagged local U0–U2 work. This brief assumes sync of the local source-of-truth tree to `main`.

---

## 1. What went wrong last time

| Source | Content |
|--------|---------|
| Local `artifacts/bip39-onboarding-android/` | Vault v1, `:keyboard`, WordListValidator, kdf.rs, secure input |
| GitHub `main` (before sync) | Legacy `CryptoUtils` storage + docs claiming U2 + broken `include(":keyboard")` without module |

Auditor received the **GitHub/old zip** view, not the local tree. Process failure (partial push), not auditor error.

---

## 2. Where to look (priority order)

### 2.1 Build integrity

1. `settings.gradle.kts` — `:library`, `:keyboard`, `:sample`
2. `library/build.gradle.kts` — `api(project(":keyboard"))`
3. `keyboard/` — in-app-keyboard 2.0 (`com.narde.keyboard`)
4. `engine/WordListValidator.kt` — required by `Bip39Manager`
5. `crypto/KeyDerivationKit.kt` + `Hkdf.kt` — required by `AppKeyDeriver`

**Acceptance:** clean clone compiles `:library` and `:keyboard`.

### 2.2 Vault format (SEC-C1 / WP1)

| File | Why |
|------|-----|
| `docs/VAULT_FORMAT.md` | Normative byte layout |
| `vault/VaultV1.kt` | create/unlock, entropy-only body, legacy unlock |
| `vault/VaultCrypto.kt` | AEAD, KDF switch |
| `vault/VaultRepository.kt` | persistence |
| `vault/DevicePepper.kt` | device binding |
| `vault/RustNativeKdf.kt` | JNI facade |
| `rust/.../kdf.rs` | Argon2id |
| `security/CryptoUtils.kt` | **LEGACY** — verify not default write path |

**Questions:**

1. Does body match VAULT_FORMAT §3?
2. Legacy UTF-8 body still unlocks?
3. Is PIN success only AEAD unwrap of DEK (no pin_hash on disk)?
4. Is CryptoUtils still on create/unlock path?
5. Panic: HMAC only; wipe scope?

### 2.3 Private input

- `keyboard/` — SystemKeyboardGate, NumericKeypad shuffle, secureMode
- `ui/input/SecureOnboardingInput.kt`
- `PinKeypad`, `MnemonicWordEntry`, `PassphraseScreen`, `RestoreScreens`
- `Bip39Features.useInAppKeyboard` / `shufflePinKeypad`

### 2.4 Lockout / panic

Grep: `wipeAfterFailedPinAttempts`, `PinLockout`, `PinUnlock`.

### 2.5 Docs honesty

Every “Done” in AUDIT_EXECUTION.md must map to a file on this commit.

---

## 3. Residual risks (not claimed fixed)

- CI not proven green in agent env
- NDK .so may be missing from AAR
- JNI KDF symbols must match bridge
- Package still `com.example.bip39onboarding`
- iOS prototype
- Low-entropy PIN policy incomplete
- Legacy CryptoUtils may remain for migration only

---

## 4. Method

1. Clone this commit SHA and record it.
2. File checklist §2 — all present.
3. Static read vault create→unlock→panic.
4. Static read restore + PIN for IME gate.
5. Grep: `hashPin`, `pin_hash`, `CryptoUtils.encrypt`, `VaultV1.create`, `useInAppKeyboard`.
6. Optional compile.
7. Re-answer original five U2 brief questions with code citations.

---

## 5. Pass criteria for deep vault audit

- [ ] Clean clone builds library+keyboard
- [ ] VaultV1 + VAULT_FORMAT present and consistent
- [ ] No orphan `include(":keyboard")`
- [ ] No missing WordListValidator / KeyDerivationKit imports
- [ ] Auditor can cite DEK/KEK paths (not only CryptoUtils)
