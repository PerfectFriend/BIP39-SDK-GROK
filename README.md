<p align="center">
  <img src="assets/banners/hero.svg" alt="BIP39 SDK — Cyberpunk × Van Gogh × Gaudí" width="100%"/>
</p>

# BIP39-SDK-GROK

**Drop-in secure BIP39 onboarding for Android**  
Rust core · Jetpack Compose · Tink · Biometrics · 20 visual presets

[English](README.md) · [Русский](README.ru.md) · [Español](README.es.md)

---

## What you get

| Flow | Features |
|------|----------|
| **Create** | 12/24 words · disclaimer · paper-first backup · quiz · PIN + panic PIN |
| **Restore** | Paste · QR · PDF |
| **Unlock** | PIN · optional passphrase · biometrics (CryptoObject) · session lock |
| **Engine** | Rust-first (`libbip39_engine_ffi`) with JVM fallback |
| **Look** | 20 theme presets · Welcome Aboard splash |

## Quick start

```kotlin
// settings.gradle.kts — JitPack or module
// implementation("com.github.PerfectFriend:BIP39-SDK-GROK:TAG")

class MainActivity : FragmentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val storage = TinkSeedStorageAdapter(TinkSeedStorage(this))
    MnemonicEngines.preferRust()

    setContent {
      OnboardingTheme(config = OnboardingThemePresets.config("neon_night")) {
        if (!storage.isSetup()) {
          OnboardingNavHost(storage, onComplete = { payload -> /* derive keys */; payload.clear() })
        } else {
          PinUnlockScreen(storage, onSuccess = { /* ... */ }, onPanic = { /* wipe UI */ })
        }
      }
    }
  }
}
```

## Theme presets (pick one)

```kotlin
OnboardingThemePresets.ids()
// neon_night, starry_amber, gaudi_mosaic, cyber_sakura, deep_ocean, ...
OnboardingThemePresets.config("gaudi_mosaic")
```

## Native Rust (optional)

```bash
export ANDROID_NDK_HOME=$ANDROID_HOME/ndk/<ver>
./scripts/build-rust-android.sh   # → library/src/main/jniLibs
./scripts/doctor.sh               # environment check
```

Without `.so`, the SDK uses the Kotlin BIP39 engine automatically.

## Scripts

| Script | Purpose |
|--------|---------|
| `scripts/doctor.sh` | JDK / SDK / NDK / cargo / jniLibs check |
| `scripts/build-rust-android.sh` | Build native libs for all ABIs |
| `scripts/verify-parity.sh` | Golden vector check (Rust) |
| `scripts/run-sample.sh` | Install debug sample |
| `RUN_IN_B39.sh` | One-shot host test + optional NDK build |
| `scripts/push-to-github.sh` | Create/push **BIP39-SDK-GROK** via `gh` |

## Security

- FLAG_SECURE on seed screens · CharArray mnemonics · Tink AEAD · panic PIN wipe  
- See [docs/SECURITY.md](docs/SECURITY.md)

## License

MIT
