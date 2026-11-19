# Sync status (2026-11-19, ongoing)

## Done on GitHub `main` (this session)

### Vault
- `VaultV1.kt` ✅
- `VaultCrypto.kt` ✅
- `VaultRepository.kt` ✅
- `VaultError.kt` ✅
- `DevicePepper.kt` ✅
- `RustNativeKdf.kt` ✅
- `VaultUnlock.kt` ✅
- `BiometricPinCache.kt` ⏳ still missing

### Engine / crypto
- `WordListValidator.kt` ✅
- `KeyDerivationKit.kt` ✅
- `Hkdf.kt` ✅

### Keyboard module (partial — 6/21 .kt)
- `build.gradle.kts`, manifest, consumer-rules ✅
- `Security.kt`, `StdKey.kt`, `NumericKeypad.kt`, `TextFieldHelpers.kt`, `EditTextBridge.kt`, `KeyboardState.kt` ✅
- Still missing: `StandardKeyboard.kt`, `Keys.kt`, `KeyboardConfig.kt`, `KeyboardColors.kt`, `KeyboardLayout.kt`, `TextEditing.kt`, emoji/*, tests

### UI
- `SecureOnboardingInput.kt` ✅
- `PinKeypad.kt` ✅
- `MnemonicWordEntry` / `PassphraseScreen` ⏳

### Docs
- `AUDIT_BRIEF_RECHECK.md` ✅
- `VAULT_FORMAT.md` ⏳
- `EVOLUTION_UNIFIED.md` ⏳

### Rust
- `kdf.rs` ⏳

## Auditor

Do **not** re-audit until `VAULT_FORMAT.md`, full `keyboard/` sources, and `BiometricPinCache` are 200 OK on raw.githubusercontent.com.

Source of truth until then: local tree / `BIP39-SDK-GROK-FULL-SYNC.zip`.
