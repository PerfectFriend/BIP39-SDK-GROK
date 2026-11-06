# Changelog

## 2.0.0-alpha3 — 2026-11-06

### DX & branding (BIP39-SDK-GROK)
- Hero / social / badge banners (`assets/banners/`)
- README.md (EN default) + README.ru.md + README.es.md
- 20 theme presets + `ThemePresetPicker`
- Scripts: `doctor.sh`, `run-sample.sh`
- `rootProject.name = BIP39-SDK-GROK`


## 2.0.0-alpha2 — 2026-11-06

### Rust-first migration (C.4)
- `Bip39Manager` delegates crypto to `MnemonicEngines` (Rust preferred)
- `JvmBip39Core` — internal JVM implementation (no recursion)
- `MnemonicEngines.preferRust()` / `ensureInitialized()` / `backend`
- Bootstrap from NavHost + PinUnlock; sample logs active backend
- JVM unit tests force Kotlin engine


## 2.0.0-alpha — 2026-11-06

### Phase C — Rust core (in progress)
- `rust/bip39-engine` — generate / validate / seed / fingerprint
- `rust/bip39-engine-ffi` — FFI façade + UniFFI UDL draft
- Kotlin `MnemonicEngine`, `KotlinMnemonicEngine`, `RustMnemonicEngine` (native pending)
- `MnemonicEngines` process-wide selector (default Kotlin)
- `docs/RUST.md`
- **C.2:** C ABI + JNI bridge, `scripts/build-rust-android.sh`, `RustMnemonicEngine` native methods
- **C.2 verified** on host: cargo 5/5, jniLibs arm64/armeabi-v7a/x86_64
- **C.3:** `EngineParityInstrumentedTest`, `MnemonicEngineTest`, `scripts/verify-parity.sh`


## 1.3.0 — 2026-11-06

### Phase B — API freeze
- `OnboardingHost` + `NoOpOnboardingHost` (`api` package)
- `OnboardingThemeConfig` (colors, reduceMotion, logo, showStepProgress)
- `docs/PUBLIC_API.md` + `PublicApi.API_LEVEL = 2`
- STRIDE-lite section in `docs/SECURITY.md`
- Golden tests: BIP39 abandon vector, encrypt/decrypt roundtrip

### Phase A (included)

### Welcome Aboard + visual system
- `WelcomeAboardScreen` (cyberpunk × Van Gogh × Gaudí)
- `OnboardingColors` / `OnboardingTheme` design tokens
- Mosaic `StepProgress` indicator

### Hardening / UX
- Paper-first mnemonic backup; PDF/QR under Advanced
- Passphrase prompt on unlock when `hasPassphrase`
- `SessionLock` + `RememberSessionLockEffect` for host auto-lock
- Biometric **CryptoObject** (Keystore AES-GCM gate)
- `DeviceIntegrity` StrongBox/TEE/software probe
- `SecureScreen` screen-capture callback (API 34+)
- Welcome Aboard respects system animator duration scale (reduce motion)
- Shared `OnboardingScreenScaffold` / `NeonPrimaryButton` / `NeonOutlinedButton`
- Themed Choose, Strength, Disclaimer, Restore choose, Passphrase, Set PIN headers
- `KeystoreMasterKey.ensureTinkMasterKey` — StrongBox-first Tink master key
- Mnemonic word cards use mosaic borders; unlock/PIN use neon CTAs
- **Phase A complete**


## 1.2.1 — 2026-10-22

### Publish
- JitPack-ready `maven-publish` on `:library`
- `jitpack.yml` builds `:library:publishToMavenLocal`
- Install docs in README + `docs/PUBLISH.md`

### Open-source engine polish
- Multi-module layout: `:library` + `:sample`
- Gradle Wrapper (8.9) + root/settings for real CI
- README rewritten for engine positioning (v1.2 features)
- CONTRIBUTING.md
- CI runs `:library:testDebugUnitTest` and `:sample:assembleDebug`


## 1.2.0 — 2026-10-22

### Added
- BIP39 optional passphrase (25th word)
- BiometricHelper + biometric button on unlock screen
- TinkSeedStorage (DataStore + Tink) + ESP migration
- SeedStorage interface (ESP / Tink adapters)
- DeviceIntegrity root/emulator/debug warning

### Notes
- Biometrics do not replace PIN-derived encryption
- Prefer TinkSeedStorage for greenfield projects


## 1.1.0 — 2026-10-22

### Added
- Full restore: paste / QR (CameraX + ZXing) / PDF import
- `PinUnlockScreen` with exponential lockout
- `PinLockout` (5 failures → backoff, max 5 min)
- Clipboard auto-clear after 60s
- Step progress indicator
- EN + RU string resources
- Android library `build.gradle.kts`, manifest, backup exclude rules
- Unit tests for BIP39 + crypto
- Sample host `MainActivity`
- LICENSE (MIT), `.gitignore`, `consumer-rules.pro`, CI workflow
- `PdfSeedExtractor` for offline PDF/text backups

### Security
- `allowBackup=false` + explicit exclude of `secure_seed_prefs`
- Data extraction rules for cloud/device transfer

## 1.0.0 — 2026-10-20

- Initial create flow, security core, docs, English README + banner
