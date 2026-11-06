<p align="center">
  <img src="assets/banners/hero.svg" alt="BIP39 SDK — Киберпанк × Ван Гог × Гауди" width="100%"/>
</p>

# BIP39-SDK-GROK

**Готовый безопасный BIP39-онбординг для Android**  
Ядро на Rust · Jetpack Compose · Tink · биометрия · 20 визуальных пресетов

[English](README.md) · [Русский](README.ru.md) · [Español](README.es.md)

---

## Что внутри

| Сценарий | Возможности |
|----------|-------------|
| **Создание** | 12/24 слова · дисклеймер · бумажный бэкап · квиз · PIN + panic PIN |
| **Восстановление** | Вставка · QR · PDF |
| **Вход** | PIN · опциональная passphrase · биометрия · session lock |
| **Движок** | Сначала Rust, иначе JVM |
| **Внешний вид** | 20 тем · заставка Welcome Aboard |

## Быстрый старт

```kotlin
MnemonicEngines.preferRust()
OnboardingTheme(config = OnboardingThemePresets.config("neon_night")) {
  OnboardingNavHost(storage, onComplete = { payload -> /* ключи */; payload.clear() })
}
```

## Темы

```kotlin
OnboardingThemePresets.config("gaudi_mosaic") // или starry_amber, cyber_sakura, …
```

## Rust (по желанию)

```bash
./scripts/doctor.sh
./scripts/build-rust-android.sh
```

Без `.so` SDK сам переключится на Kotlin.

## Скрипты

`doctor.sh` · `build-rust-android.sh` · `verify-parity.sh` · `run-sample.sh` · **`push-to-github.sh`**

## Безопасность

[docs/SECURITY.md](docs/SECURITY.md) · MIT
