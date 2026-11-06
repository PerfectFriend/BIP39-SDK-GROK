<p align="center">
  <img src="assets/banners/repo-banner-embedded.svg" alt="BIP39 SDK" width="100%"/>
</p>

# BIP39-SDK-GROK

**SDK para crear la cuenta del usuario y entregar las claves raíz de criptografía de la app**

Onboarding BIP39 → entrega única de seed/claves al host → el resto de la criptografía de la app.

[English](README.md) · [Русский](README.ru.md) · [Español](README.es.md)

## Inicio rápido

```kotlin
MnemonicEngines.preferRust()
OnboardingTheme(config = OnboardingThemePresets.config("neon_night")) {
  OnboardingNavHost(storage, onComplete = { payload -> payload.clear() })
}
```

## Para agentes de código

[docs/AGENT_GUIDE.md](docs/AGENT_GUIDE.md)

## Seguridad

[docs/SECURITY.md](docs/SECURITY.md) · MIT
