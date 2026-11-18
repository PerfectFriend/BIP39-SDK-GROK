## U2 progress (2026-11-18)

Vault **create** now writes an **entropy-only** body per `docs/VAULT_FORMAT.md`:

```text
entropy_len:u8 | entropy[len] | word_count:u8 | pp_check[4] | fingerprint[4] | created:u64 BE
```

Unlock still accepts **legacy** bodies that stored UTF-8 mnemonic (first byte = word count 12–24).

Helpers:
- `Bip39Manager.mnemonicToEntropy`
- `Bip39Manager.entropyToMnemonic`

Implemented in local tree:
- `library/src/main/java/com/example/bip39onboarding/vault/VaultV1.kt`
- `library/src/main/java/com/example/bip39onboarding/engine/JvmBip39Core.kt`

Also in progress (U1):
- `:keyboard` module vendored
- `SecureOnboardingShell` / `SecureSeedKeyboard` / `SecurePinKeypad`
- PIN / restore / passphrase use in-app keyboard by default
