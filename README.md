<p align="center">
  <img src="assets/banners/hero.svg" alt="BIP39 SDK — Account bootstrap and root keys" width="100%"/>
</p>

# BIP39-SDK-GROK

**SDK to bootstrap user accounts and supply root cryptographic keys for your app**

Create or restore a profile from a BIP39 seed, then hand a one-shot key payload to the host — wallets, E2E encryption, identity, anything that needs a strong local root.

Rust core · Android Compose · iOS SwiftUI · Tink · Biometrics · 20 themes

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

## Theme presets

```kotlin
OnboardingThemePresets.config("gaudi_mosaic")
// neon_night, starry_amber, cyber_sakura, deep_ocean, ...
```

## Native Rust (optional)

```bash
export ANDROID_NDK_HOME=$ANDROID_HOME/ndk/<ver>
./scripts/build-rust-android.sh
./scripts/doctor.sh
```

## Scripts

| Script | Purpose |
|--------|---------|
| `scripts/doctor.sh` | Environment check |
| `scripts/build-rust-android.sh` | Native Android libs |
| `scripts/build-rust-ios.sh` | iOS XCFramework |
| `scripts/verify-parity.sh` | Golden vector (Rust) |
| `scripts/run-sample.sh` | Install sample |
| `scripts/push-to-github.sh` | Create/push via `gh` |

## For coding agents

Full integration playbook: **[docs/AGENT_GUIDE.md](docs/AGENT_GUIDE.md)**  
Prompts: [docs/AI_PROMPTS.md](docs/AI_PROMPTS.md)

## Security

- FLAG_SECURE · CharArray mnemonics · Tink AEAD · panic PIN wipe  
- See [docs/SECURITY.md](docs/SECURITY.md)

## License

MIT
