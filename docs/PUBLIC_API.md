# Public API (Phase B)

**API level:** `PublicApi.API_LEVEL = 2`

This document freezes what host applications may rely on. Anything not listed is **internal** and may change without a major version bump.

## Integration entry points

| Type | Role |
|------|------|
| `OnboardingNavHost` | Full create/restore flow |
| `PinUnlockScreen` | Daily unlock (+ optional passphrase step) |
| `WelcomeAboardScreen` | Standalone splash if host builds custom nav |
| `OnboardingTheme` / `OnboardingThemeConfig` | Colors, logo, reduce motion, step progress |
| `OnboardingHost` | complete / wipe / analytics callbacks |
| `SeedStorage` + `TinkSeedStorage` | Persistence |
| `SessionLock` | In-memory idle / background lock policy |
| `SeedPayload` | One-shot hand-off; call `clear()` |
| `Bip39Manager` | generate / validate / toSeed / fingerprint / clear |
| `MnemonicEngine` / `MnemonicEngines` | **Rust preferred** via `preferRust()` / auto-init; JVM fallback |
| `Bip39Manager` | Façade → `MnemonicEngines.current` (Rust-first) |

## Theme

```kotlin
OnboardingTheme(
  config = OnboardingThemeConfig(
    colors = DefaultOnboardingColors, // or custom OnboardingColors(...)
    reduceMotion = false,
    logo = { /* Image(...) */ },
    showStepProgress = true
  )
) {
  OnboardingNavHost(storage, onComplete = { ... }, host = myHost)
}
```

## Host

```kotlin
class MyHost : OnboardingHost {
  override fun onOnboardingComplete(payload: SeedPayload) { /* derive keys */; payload.clear() }
  override fun onVaultWiped() { /* reset UI */ }
  override fun onAnalyticsEvent(name: String, properties: Map<String, String>) { /* no secrets */ }
}
```

## Explicitly internal

- `OnboardingViewModel`, `OnboardingStep` ordering details  
- `CryptoUtils` parameter constants (use only via storage/unlock)  
- `BiometricHelper` key aliases  
- Package `ui.components` except `SecureScreen` / documented helpers  

## Versioning

- Additive defaults on data classes / new optional params → minor  
- Removing or changing semantics of stable types → major  
