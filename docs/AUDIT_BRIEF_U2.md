# Audit brief — BIP39-SDK-GROK after U0–U2 (2026-11-18)

## Scope for auditor

1. **Vault body entropy-only** (`VaultV1.create` / `parseBody`)
   - New body: `entropy_len | entropy | word_count | pp_check | fp | created`
   - Legacy UTF-8 mnemonic body still unlocks (first byte ∈ {12,15,18,21,24})
   - Header `flags` bit0 has_passphrase drives passphrase checks

2. **KDF dual path**
   - Argon2id (`kdf_id=1`) when native `.so` present
   - PBKDF2 (`kdf_id=2`) fallback
   - Anti-downgrade minimums on unlock

3. **Private input (U1)**
   - `:keyboard` module (in-app-keyboard 2.0) vendored
   - `Bip39Features.useInAppKeyboard` / `shufflePinKeypad` default true
   - `PinKeypad` → shuffled `NumericKeypad`
   - `MnemonicWordEntry`, `PassphraseScreen`, `RestorePasteScreen` use `SecureOnboardingShell` / `SecureSeedKeyboard`

4. **Stubs (not production risk modules yet)**
   - `AppKeyDeriver` / `DefaultAppKeyDeriver`
   - `AddressRiskProvider` / `NoopAddressRiskProvider` (no KYC)

## Known gaps (honest)

- Full `:keyboard` sources may not all be on GitHub yet (local + zip)
- NDK `.so` packaging into AAR not finished
- Vault unit tests for entropy round-trip not in CI yet
- Package still `com.example.bip39onboarding` (rename pending)
- Keyboard HANDOFF: not verified compiled in this environment

## Artifacts

- Zip: `BIP39-SDK-GROK-U2-audit.zip` (full tree under artifacts)
- Docs: `EVOLUTION_UNIFIED.md`, `STACK.md`, `U2_VAULT_ENTROPY.md`, `VAULT_FORMAT.md`

## Suggested audit questions

1. Does entropy body match `VAULT_FORMAT.md` §3 exactly?
2. Can legacy unlock still decrypt pre-U2 vaults?
3. Is passphrase empty vs wrong distinguished without PIN oracle?
4. Does `secureMode` keyboard prevent IME on seed fields in Compose?
5. Panic PIN path still wipes without unlocking body?
