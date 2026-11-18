# Sovereign stack map

See also [EVOLUTION_UNIFIED.md](EVOLUTION_UNIFIED.md).

| Module | Path | Role |
|--------|------|------|
| Onboarding + vault | `:library` | BIP39 create/restore, PIN, panic, vault v1 |
| Private input | `:keyboard` | in-app-keyboard 2.0 (`com.narde.keyboard`) |
| Sample host | `:sample` | Reference wiring |
| Rust engine | `rust/` | BIP39 + Argon2id KDF |
| Keyboard docs | `docs/keyboard/` | Privacy audit, theming, HANDOFF |
| Agent | `docs/AGENT_GUIDE.md` | Integration prompts |

## Sensitive input (default)

```kotlin
Bip39Features.useInAppKeyboard = true
Bip39Features.shufflePinKeypad = true

SecureOnboardingShell {
  // seed field + SecureSeedKeyboard(...)
  // or SecurePinKeypad(onDigit, onBackspace)
}
```

## Theme bridge

`KeyboardThemeBridge.currentPresetId = "neon_night"`  
→ keyboard Matrix scheme (see `KeyboardThemeBridge`).

## Risk (optional)

```kotlin
val risk: AddressRiskProvider = NoopAddressRiskProvider
// later: Stigmata implementation
```

## Versions

- Library: 2.1.0-SNAPSHOT (evolution)
- Keyboard: 2.0.0 (vendored)
- Vault KDF: Argon2id when native `.so` present
