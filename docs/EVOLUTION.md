# Evolution Plan — BIP39 Onboarding Engine

**Status:** roadmap (not implemented yet)  
**Baseline:** Android Kotlin library v1.2.x  
**Horizon:** v2.0 (Rust core) → v2.1 (universal shell) → v3.0 (multiplatform engine)

---

## 1. Naming: splash / welcome

| Variant | When to use |
|--------|-------------|
| **Welcome Aboard** | Preferred English — idiomatic, warm, “you’re in the crew” |
| **Welcome On Board** | Acceptable; slightly more formal / transport metaphor |
| **Welcome Onboard** | Avoid as title (one word is marketing-jargon-y) |
| **Добро пожаловать** / **На борту** | RU locale strings |

**Product string (EN default):**  
`Welcome Aboard`

**Screen / route id:**  
`welcome_aboard` (stable, not tied to marketing copy)

**Subtitle ideas (pick one tone):**  
- *Your keys. Your rules.*  
- *Secure onboarding starts here.*  
- *Entropy · Custody · Freedom*

Typo in the request (“WELOCME ON BOARD”) → always ship **Welcome Aboard**.

---

## 2. Visual language: Cyberpunk × Van Gogh × Gaudí

Not three skins — **one hybrid system**.

| Layer | Source | Manifestation |
|-------|--------|----------------|
| **Structure** | Gaudí | Organic curves, mosaic tiles as progress dots, arch-like cards, “trencadís” noise in backgrounds |
| **Motion / light** | Van Gogh | Swirling star-field gradients, impasto-like brush strokes in shader/mesh, night blues & amber |
| **Tech / UI chrome** | Cyberpunk | Neon cyan/magenta edges, scanlines (subtle), monospace fingerprints, glitch-free terminal accents |

**Palette (seed tokens)**

```text
bg.void        #0a0618
bg.panel       #12102a
stroke.neon    #00f0ff
stroke.magenta #ff2d95
glow.amber     #f4c15d
text.primary   #f2efe9
text.muted     #9a94b8
mosaic.1..n    Gaudí fragment hues (teal, coral, gold, indigo)
```

**Welcome Aboard motion (3–4 s, skippable)**

1. Void → slow Van Gogh swirl (stars drift).  
2. Gaudí arch forms from mosaic particles.  
3. Neon stroke traces the arch (cyberpunk).  
4. Title fades in: **Welcome Aboard** + subtitle.  
5. CTA: *Create* | *Restore* (Material 3, themed).

**Implementation note:** Compose + optional AGSL/RuntimeShader; Lottie only if it stays under size budget. Prefer code-driven motion for theming.

**Banner / marketing** stays aligned with existing `assets/banner.svg` (same trinity).

---

## 3. Strategic goal

Turn the Android-only Kotlin module into a **portable custody onboarding engine**:

- **Rust** = canonical crypto, mnemonic lifecycle, PIN/passphrase KDF, wipe semantics  
- **Kotlin (Android)** = Compose UI, Keystore/StrongBox, BiometricPrompt, CameraX  
- Later: **Swift/Kotlin Multiplatform shell** shares the same Rust core via FFI  

Principle: *UI may differ; security core must not fork.*

---

## 4. Phased roadmap

### Phase A — v1.3 “Hardened Kotlin” (no Rust yet)

Ship security/UX debt while API is still pure Kotlin.

| Item | Outcome |
|------|---------|
| Biometric **CryptoObject** | Keystore key + cipher; PIN fallback |
| StrongBox / TEE detection | Honest integrity + storage path |
| Passphrase on unlock | Prompt when `hasPassphrase`; never persist phrase |
| Paper-first export | PDF/QR behind “advanced”; double confirm |
| Session auto-lock | Timeout + `onStop` → clear in-memory seed |
| Welcome Aboard screen | Themed splash + Create/Restore (replaces plain splash) |
| Screen-capture hygiene | FLAG_SECURE + optional recording warning |

**API:** keep `OnboardingNavHost`, `SeedStorage`, `SeedPayload` stable; additive only.

---

### Phase B — v1.4 “Engine API freeze”

| Item | Outcome |
|------|---------|
| Public API review | Explicit `api` / `internal` packages |
| Host contract | `OnboardingHost` callbacks: complete / wipe / analytics (no PII) |
| Theming API | `OnboardingTheme` (colors, typography, logo slot, motion on/off) |
| Threat-model doc | Update `SECURITY.md` (STRIDE-lite table) |
| Golden tests | BIP39 vectors + KDF regression |

Exit criteria: external apps can theme Welcome Aboard + full flow without forking sources.

---

### Phase C — v2.0 “Rust core” (breaking by design, migration path)

#### C.1 Crate layout

```text
crates/
  bip39-engine/          # pure Rust: mnemonic, validate, seed, fingerprint
  bip39-engine-crypto/   # KDF (Argon2id or PBKDF2-compat), AEAD helpers
  bip39-engine-ffi/      # UniFFI or cbindgen surface for mobile
```

Responsibilities **in Rust**:

- Entropy → BIP39 mnemonic (12/24)  
- Validate checksum  
- Mnemonic + passphrase → seed bytes  
- Fingerprint (display-only)  
- Optional: SLIP-39 encode/decode (feature-gated)  
- Constant-time-ish compare for PIN hash verification helpers  

Responsibilities **remain on platform**:

- Android Keystore / StrongBox / Tink envelope  
- BiometricPrompt + CryptoObject  
- Secure screen flags, clipboard  
- PDF/QR, CameraX  
- Compose navigation & Welcome Aboard  

#### C.2 FFI

Preferred: **[UniFFI](https://mozilla.github.io/uniffi-rs/)** → Kotlin bindings for Android (Swift later).

```text
bip39_engine.udl  →  generate_mnemonic / validate / to_seed / fingerprint
```

Alternative: `cbindgen` + thin JNI if UniFFI cost is too high for first slice.

#### C.3 Android integration

```text
library/          # Android AAR (Compose UI + platform security)
  + jniLibs/      # librust bip39_engine
  + uniffi gen    # Kotlin stubs
```

Build: `cargo-ndk` / `cargo-mobile2` in CI for `arm64-v8a`, `armeabi-v7a`, `x86_64`.

#### C.4 Migration bridge

```kotlin
// v2 compatibility
interface MnemonicEngine {
  fun generate(strength: Strength): CharArray
  fun validate(mnemonic: CharArray): Boolean
  fun toSeed(mnemonic: CharArray, passphrase: CharArray): ByteArray
}

class RustMnemonicEngine : MnemonicEngine { /* FFI */ }
class KotlinMnemonicEngine : MnemonicEngine { /* current Bip39Manager */ }
```

Default in v2.0: **Rust**. Kotlin engine behind `BuildConfig` or dependency flag for one minor.

Wire format for stored ciphertext **unchanged** where possible (same PBKDF params + AEAD) so users don’t re-onboard.

#### C.5 Why Rust here

- One audited core for Android / iOS / desktop later  
- Memory safety for mnemonic buffers (zeroize on drop)  
- Harder to accidentally log/copy `String` secrets in shared logic  
- Industry direction (wallet cores, cross-platform custody)

---

### Phase D — v2.1 “Universal shell”

| Item | Outcome |
|------|---------|
| Welcome Aboard as **shared design system** | Tokens + motion specs documented for iOS |
| iOS path (optional) | SwiftUI shell + same UniFFI crate |
| Feature flags | `slip39`, `decoy_panic`, `play_integrity_hook` |
| Decoy panic (optional) | Panic PIN opens decoy vault; wipe is separate |

---

### Phase E — v3.0 “Custody platform” (aspirational)

- SLIP-39 first-class in Rust  
- Seedless / MPC **adapter interface** (not implementation) for hosts that want it  
- Formal verification targets for KDF/wipe (docs + tests, not full proofs)  
- Published BOM: `bip39-engine` (Rust) + `bip39-onboarding-android` + `bip39-onboarding-ios`

---

## 5. Welcome Aboard — functional spec

**Route:** first destination if `!storage.isSetup()`.

**Content**

1. Full-bleed animated background (cyberpunk × Van Gogh × Gaudí).  
2. Logo slot (`OnboardingTheme.logo`).  
3. Title: string resource `welcome_aboard_title` → default “Welcome Aboard”.  
4. Subtitle: `welcome_aboard_subtitle`.  
5. Primary: Create new vault.  
6. Secondary: Restore existing.  
7. Footer: short responsibility line (not a full legal wall — that stays on disclaimer step).

**Accessibility:** skip animation (prefers-reduced-motion); content descriptions on CTAs; contrast ≥ WCAG AA on text vs void.

**Performance:** first frame &lt; 300 ms on mid devices; animation does not block navigation click.

---

## 6. Rust migration principles (non-negotiable)

1. **No plaintext mnemonic in Rust logs** — ever.  
2. **Zeroize** buffers on drop (`zeroize` crate).  
3. **Feature parity first** — generate / validate / seed / fingerprint before SLIP-39.  
4. **Storage format compatibility** — Android still owns envelope encryption.  
5. **Fuzz** BIP39 parse/validate in CI.  
6. **Same vectors** as Kotlin tests (cross-check both engines in v2.0).

---

## 7. Suggested version timeline

| Version | Focus | Est. effort |
|---------|--------|-------------|
| **1.3** | Hardening + Welcome Aboard UI | 1–2 sprints |
| **1.4** | API freeze + theme + tests | 1 sprint |
| **2.0-alpha** | Rust crate + UniFFI + Android swap for `Bip39Manager` | 2–3 sprints |
| **2.0** | Default Rust, Kotlin engine deprecated | +1 sprint |
| **2.1** | iOS shell / decoy / SLIP-39 flag | parallel tracks |

---

## 8. Success metrics

- Welcome Aboard: measurable drop in drop-off between splash and create (host analytics).  
- Zero critical findings on “mnemonic in memory/logs” in internal review.  
- Rust and Kotlin engines produce identical seeds for fixed entropy vectors.  
- AAR size increase from native libs within agreed budget (e.g. &lt; +2 MB compressed per ABI or document tradeoff).  
- External host integrates with theme + host callbacks only (no fork).

---

## 9. Out of scope (explicit)

- Full MPC / social recovery implementation (only interfaces later).  
- Guaranteeing physical metal backup (UX guidance only).  
- Replacing host key derivation (BIP32/44 etc. stay in host).  
- Server-side custody.

---

## 10. Implementation progress

### Done in 1.3.0-alpha
- [x] Welcome Aboard screen + tokens (`OnboardingColors` / `OnboardingTheme`)
- [x] Mosaic `StepProgress`
- [x] Paper-first mnemonic UI (PDF under Advanced)
- [x] Passphrase field on unlock when `hasPassphrase`
- [x] `SessionLock` / `RememberSessionLockEffect` + sample wiring

### Still open in Phase A
- [x] Biometric **CryptoObject** (Keystore-bound)
- [x] StrongBox / TEE honest detection (`DeviceIntegrity.KeystoreBackend`)
- [x] System reduce-motion (animator duration scale) for Welcome Aboard
- [x] Screen-capture warning on API 34+ (`SecureScreen`)
- [x] Visual tokens on Choose / Strength / Disclaimer / Restore / Passphrase / PIN headers
- [x] Tink master key StrongBox-first via `KeystoreMasterKey`
- [x] Mnemonic grid mosaic chrome + themed unlock / Set PIN buttons

**Phase A status: COMPLETE (2026-11-06)**

**Phase B status: COMPLETE (2026-11-06)** — public API, theme config, host contract, golden tests, STRIDE-lite

**Phase C status: ADVANCED (2026-11-06)** — C.4 Rust-first: Bip39Manager → MnemonicEngines; JVM fallback retained

## 11. Ordered next steps (do not skip)

1. ~~Phase A — Hardened Kotlin + Welcome Aboard + visual system~~ **done**
2. ~~**Phase B — API freeze**~~ **done**
3. **Phase C — Rust core** — crate + bridge done; native packaging next
4. Phase D — Universal shell / iOS path

Phase B starts only when you explicitly continue. No Rust until B is closed.

## 12. Next concrete step

**Implement Phase A slice 1 in parallel:**

1. `WelcomeAboardScreen` + theme tokens (cyberpunk × Van Gogh × Gaudí).  
2. Wire as initial route in `OnboardingNavHost`.  
3. Keep existing create/restore graph unchanged behind CTAs.

Then P0 security (CryptoObject, passphrase unlock) before any Rust work.

---

*Document version: 2026-11-04 — evolution plan for PerfectFriend/bip39-onboarding-android*
