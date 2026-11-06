# Agent Guide — BIP39-SDK-GROK

**Audience:** AI coding agents and human integrators  
**Goal:** Wire this SDK into an app as the **account/profile bootstrap** and the **root key source** for all app cryptography.

Default language of this doc: **English**.

---

## 1. What this SDK is (and is not)

### Is

- **Onboarding engine** for creating or restoring a user vault from a BIP39 mnemonic (12/24 words).
- **Root key source:** after unlock, the host receives a one-shot `SeedPayload` and derives app keys (wallet, encryption, identity).
- **Security UX kit:** disclaimer, paper-first backup, quiz, PIN + panic PIN, biometrics (Android), session lock, FLAG_SECURE.
- **Multiplatform core:** Rust BIP39 (`bip39-engine`) with Android (Compose + JNI) and iOS (SwiftUI + C ABI) shells.

### Is not

- A full wallet, exchange, or blockchain client.
- A cloud account system (no server-side user DB).
- A place to log or transmit seed phrases.

**Integration mental model**

```text
[First launch]  →  OnboardingNavHost / OnboardingFlowView
                      ↓
                 SeedPayload (mnemonic in memory once)
                      ↓
              Host derives master keys → store only encrypted vault
                      ↓
[Next launches] →  PinUnlockScreen / host unlock → same SeedPayload path
```

---

## 2. Repository map

| Path | Role |
|------|------|
| `library/` | Android library (Compose UI + storage + engine bridge) |
| `sample/` | Reference Android host app |
| `rust/bip39-engine` | Pure Rust BIP39 |
| `rust/bip39-engine-ffi` | C ABI + optional Android JNI |
| `ios/` | Swift package + Welcome Aboard / minimal flow |
| `docs/` | Architecture, security, this agent guide |
| `scripts/` | doctor, native builds, GitHub push |

**Product name:** BIP39-SDK-GROK  
**Maven (historical/JitPack):** `com.github.PerfectFriend:bip39-onboarding` (tag-dependent)

---

## 3. Android integration (copy-ready)

### 3.1 Dependencies

Module `build.gradle.kts`:

```kotlin
dependencies {
  implementation("com.github.PerfectFriend:BIP39-SDK-GROK:<TAG>")
  // or project(":library")
}
```

Root `settings.gradle.kts` must include JitPack if using GitHub packages via JitPack:

```kotlin
maven { url = uri("https://jitpack.io") }
```

`minSdk` ≥ 26. Host activity for biometrics: `FragmentActivity`.

### 3.2 Minimal host

```kotlin
class MainActivity : FragmentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val tink = TinkSeedStorage(applicationContext).also { it.migrateFromLegacyIfNeeded() }
    val storage: SeedStorage = TinkSeedStorageAdapter(tink)
    MnemonicEngines.preferRust() // no-op fallback to JVM if .so missing

    setContent {
      var session by remember { mutableStateOf<SeedPayload?>(null) }
      OnboardingTheme(config = OnboardingThemePresets.config("neon_night")) {
        when {
          !storage.isSetup() -> OnboardingNavHost(
            storage = storage,
            onComplete = { payload ->
              bootstrapAppCrypto(payload)
              payload.clear()
            },
            onWipe = { /* panic: reset nav / kill process */ }
          )
          session == null -> PinUnlockScreen(
            storage = storage,
            onSuccess = { session = it },
            onPanic = { /* wipe UI */ },
            enableBiometric = true
          )
          else -> AppHome(onLock = { session?.clear(); session = null })
        }
      }
    }
  }
}

fun bootstrapAppCrypto(payload: SeedPayload) {
  val seed = payload.seed() // 64-byte BIP39 seed
  // Derive app-specific keys (HKDF/BIP32/etc). Never log seed.
}
```

### 3.3 Host contract

```kotlin
class MyHost : OnboardingHost {
  override fun onOnboardingComplete(payload: SeedPayload) {
    bootstrapAppCrypto(payload)
    payload.clear()
  }
  override fun onVaultWiped() { /* clear in-memory keys, show welcome */ }
  override fun onAnalyticsEvent(name: String, properties: Map<String, String>) {
    // NEVER put seed/PIN/passphrase in properties
  }
}
```

### 3.4 Theme

```kotlin
val config = OnboardingThemePresets.config("gaudi_mosaic")
OnboardingTheme(config) { /* ... */ }
ThemePresetPicker(selectedId = id, onSelect = { id = it; persist(id) })
```

### 3.5 Native Rust (optional)

```bash
export ANDROID_NDK_HOME=...
./scripts/build-rust-android.sh
```

---

## 4. iOS integration

```bash
./scripts/build-rust-ios.sh
```

```swift
import Bip39SDK
let phrase = try Bip39Engine.generate(strength: .words12)
let seed = try Bip39Engine.seed(phrase: phrase)
WelcomeAboardView(onCreate: { }, onRestore: { })
OnboardingFlowView { mnemonic in /* Keychain + derive */ }
```

---

## 5. Security rules for agents (non-negotiable)

1. Never log/analytics/network: mnemonic, PIN, passphrase, raw seed.
2. Always `SeedPayload.clear()` after derivation.
3. Paper-first; PDF/QR are advanced.
4. Exclude secure prefs from Android backup.
5. Panic PIN wipes storage and memory.
6. Biometrics with CryptoObject.
7. Use `Bip39Manager` / `Bip39Engine` only for BIP39.

---

## 6. API cheat sheet

| Symbol | Use |
|--------|-----|
| `OnboardingNavHost` | Full create/restore |
| `PinUnlockScreen` | Returning user |
| `SeedStorage` / `TinkSeedStorage` | Encrypted vault |
| `SeedPayload` | One-shot key hand-off |
| `Bip39Manager` | Façade → Rust/JVM |
| `MnemonicEngines.preferRust()` | Select backend |
| `OnboardingThemePresets` | 20 looks |
| `OnboardingHost` | complete / wipe / analytics |
| `SessionLock` | Idle/background lock |

---

## 7. Agent workflow

1. Add dependency / module.
2. `TinkSeedStorage` + adapter.
3. Gate UI: `!storage.isSetup()` → onboarding; else unlock.
4. Derive keys → `clear()`.
5. Theme preset; optional picker.
6. `MnemonicEngines.preferRust()`.
7. Panic → wipe + restart.
8. `./scripts/doctor.sh`.

### Prompt snippet

```text
Integrate BIP39-SDK-GROK as the only profile/account bootstrap and root key source.
Use OnboardingNavHost + PinUnlockScreen + TinkSeedStorage.
On SeedPayload, derive app keys then payload.clear().
Never log secrets. Theme: OnboardingThemePresets.config("neon_night").
Prefer MnemonicEngines.preferRust(). Follow docs/AGENT_GUIDE.md and docs/SECURITY.md.
```

---

## 8. Related docs

INTEGRATION.md · PUBLIC_API.md · SECURITY.md · RUST.md · MULTIPLATFORM.md · AI_PROMPTS.md

*BIP39-SDK-GROK — account bootstrap & cryptographic root keys for mobile apps.*
