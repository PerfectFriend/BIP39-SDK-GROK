<p align="center">
  <img src="assets/banners/repo-banner-embedded.svg" alt="BIP39 SDK" width="100%"/>
</p>

# BIP39-SDK-GROK

**SDK для создания профиля/аккаунта и корневых криптоключей приложения**

Онбординг по BIP39 → одноразовая выдача seed/ключей хосту → дальше вся криптография приложения.

[English](README.md) · [Русский](README.ru.md) · [Español](README.es.md)

## Быстрый старт

```kotlin
MnemonicEngines.preferRust()
OnboardingTheme(config = OnboardingThemePresets.config("neon_night")) {
  OnboardingNavHost(storage, onComplete = { payload -> payload.clear() })
}
```

## Для AI-агентов

[docs/AGENT_GUIDE.md](docs/AGENT_GUIDE.md)

## Безопасность

[docs/SECURITY.md](docs/SECURITY.md) · MIT
