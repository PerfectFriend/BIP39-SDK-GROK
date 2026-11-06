# BIP39 Onboarding Integration

> Module path in this repo: **`:library`** (package `com.example.bip39onboarding`).

## 0. Include the module

```kotlin
// settings.gradle.kts
include(":bip39-onboarding")
project(":bip39-onboarding").projectDir = file("../bip39-onboarding-android/library")
```

Preferred storage:

```kotlin
val tink = TinkSeedStorage(context).also { it.migrateFromLegacyIfNeeded() }
val storage: SeedStorage = TinkSeedStorageAdapter(tink)
```


## 1. Зависимости (app/build.gradle.kts)

```kotlin
dependencies {
    // === Обязательные ===
    implementation("cash.z.ecc.android:kotlin-bip39:1.0.9")
    implementation("androidx.security:security-crypto:1.1.0-alpha07")

    // Compose + Navigation
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.navigation:navigation-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // QR
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // CameraX (для QR-сканера)
    implementation("androidx.camera:camera-camera2:1.4.0")
    implementation("androidx.camera:camera-lifecycle:1.4.0")
    implementation("androidx.camera:camera-view:1.4.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}
```

> **Примечание:** `EncryptedSharedPreferences` помечен deprecated, но продолжает работать.  
> Для новых проектов рекомендуется миграция на **DataStore + Google Tink** (см. раздел «Миграция»).

## 2. AndroidManifest.xml

```xml
<!-- Разрешения -->
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" android:required="false" />

<!-- FileProvider для экспорта PDF -->
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

Создайте `res/xml/file_paths.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <external-files-path name="backups" path="." />
    <cache-path name="cache" path="." />
</paths>
```

## 3. ProGuard / R8 (proguard-rules.pro)

```proguard
# BIP39
-keep class cash.z.ecc.android.bip39.** { *; }

# Security crypto
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**

# ZXing
-keep class com.google.zxing.** { *; }
```

## 4. Точка входа в приложение

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val storage = SecureSeedStorage(applicationContext)

        setContent {
            YourAppTheme {
                if (!storage.isSetup()) {
                    OnboardingNavHost(
                        storage = storage,
                        onComplete = {
                            // Переход в основное приложение
                            // Можно сохранить флаг в DataStore
                        },
                        onWipe = {
                            // Полный restart приложения после panic
                            val intent = packageManager.getLaunchIntentForPackage(packageName)
                            intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            Runtime.getRuntime().exit(0)
                        }
                    )
                } else {
                    // Обычный LockScreen / PIN entry
                    PinUnlockScreen(
                        storage = storage,
                        onSuccess = { mnemonic -> /* передать в ViewModel приложения */ },
                        onPanic = { /* wipe уже произошёл */ }
                    )
                }
            }
        }
    }
}
```

## 5. Конфигурация (рекомендуется вынести)

```kotlin
object OnboardingConfig {
    val defaultStrength = Bip39Manager.Strength.WORDS_12
    val allowUserChooseStrength = true
    val pinLength = 6                     // 6 или 8
    val requirePanicPin = true
    val maxVerifyAttempts = 3
    val pbkdf2Iterations = 310_000
    val enablePdfExport = true
    val enableQrExport = true
}
```

## 6. Миграция с EncryptedSharedPreferences (рекомендация 2026+)

1. Используйте **Jetpack DataStore** (Preferences или Proto).
2. Шифруйте значения через **Google Tink** + Android Keystore.
3. Ключ шифрования seed по-прежнему выводится из PIN (PBKDF2/Argon2).
4. Master-key для Tink храните в Android Keystore.

Пример миграции можно запросить у AI-агента промптом из `AI_PROMPTS.md`.

## 7. Тестирование

- Unit-тесты на `Bip39Manager.validate()` и `CryptoUtils.encrypt/decrypt`
- UI-тесты на quiz (Compose Testing)
- Проверка `FLAG_SECURE` вручную (скриншот должен быть чёрным)
- Panic PIN → данные должны исчезнуть полностью
