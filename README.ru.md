<p align="center">
  <img src="assets/banners/repo-banner.jpg" alt="BIP39 SDK — создание аккаунта и корневые ключи" width="100%"/>
</p>

# BIP39-SDK-GROK

**SDK для создания профиля/аккаунта и корневых криптоключей приложения**

Онбординг по BIP39 → одноразовая выдача seed/ключей хосту → дальше вся криптография приложения.

Rust · Android Compose · iOS SwiftUI · Tink · биометрия · 20 тем

[English](README.md) · [Русский](README.ru.md) · [Español](README.es.md)

---

## Быстрый старт

```kotlin
MnemonicEngines.preferRust()
OnboardingTheme(config = OnboardingThemePresets.config("neon_night")) {
  OnboardingNavHost(storage, onComplete = { payload -> /* ключи */; payload.clear() })
}
```

## Темы

```kotlin
OnboardingThemePresets.config("gaudi_mosaic")
```

## Для AI-агентов

[docs/AGENT_GUIDE.md](docs/AGENT_GUIDE.md)

## Безопасность

[docs/SECURITY.md](docs/SECURITY.md) · MIT
