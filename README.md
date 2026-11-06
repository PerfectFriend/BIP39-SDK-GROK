<p align="center">
  <img src="assets/banners/repo-banner.jpg" alt="BIP39 SDK — Account bootstrap and root keys for your app" width="100%"/>
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
| **Unlock** | PIN · optional passphrase · biometrics · session lock |
| **Engine** | Rust-first with JVM fallback |
| **Look** | 20 theme presets · Welcome Aboard |

## Quick start

```kotlin
MnemonicEngines.preferRust()
OnboardingTheme(config = OnboardingThemePresets.config("neon_night")) {
  OnboardingNavHost(storage, onComplete = { payload -> /* derive keys */; payload.clear() })
}
```

## Theme presets

```kotlin
OnboardingThemePresets.config("gaudi_mosaic")
```

## Native Rust (optional)

```bash
./scripts/build-rust-android.sh
./scripts/doctor.sh
```

## For coding agents

**[docs/AGENT_GUIDE.md](docs/AGENT_GUIDE.md)** · [docs/AI_PROMPTS.md](docs/AI_PROMPTS.md)

## Security

[docs/SECURITY.md](docs/SECURITY.md) · MIT
