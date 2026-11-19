# Unified Evolution Plan — Sovereign Crypto App Stack

**Status:** normative roadmap (2026-11-18)  
**Goal:** one reference pipeline for future crypto apps — onboarding, vault, private input, optional hygiene, shared design language.  
**Not a goal:** merge every repo into one binary; **unify contracts, themes, and agent prompts**.

## Execution log

| Date | Done |
|------|------|
| 2026-11-18 | U0/U1 started: vendored `:keyboard` 2.0, `STACK.md`, bridges, stubs |
| 2026-11-18 | Audit package: `BIP39-SDK-GROK-U2-audit.zip` + `docs/AUDIT_BRIEF_U2.md` on GitHub |
| 2026-11-18 | U2: Vault body **entropy-only** (create); unlock still reads legacy UTF-8 mnemonic body |
| 2026-11-18 | U1 wiring: `PinKeypad`→shuffle NumericKeypad; `MnemonicWordEntry`/`PassphraseScreen`/`RestorePasteScreen` + SecureShell/SeedKeyboard |
| 2026-11-19 | Full keyboard module + VAULT_FORMAT + BiometricPinCache pushed; force-sync ready |

---

## 0. North star

Build a **Lego stack** that any host (wallet, messenger, game, node UI) can assemble:

```text
┌─────────────────────────────────────────────────────────────┐
│  Host app (NeXX / Paranoid-Chat / MadGammon / custom)       │
├──────────────┬──────────────────┬───────────────────────────┤
│  Onboarding  │  Private Input   │  Post-unlock services     │
│  + Vault     │  (in-app kbd)    │  derive · risk · cloud    │
├──────────────┴──────────────────┴───────────────────────────┤
│  Shared design system (Cyberpunk × Van Gogh × Gaudí)        │
│  Shared agent docs (one AGENT_GUIDE + composable prompts)   │
└─────────────────────────────────────────────────────────────┘
```

**Product promise to the user:**  
“Your root key is born and unlocked only on this device; nothing you type on seed/PIN goes through a system keyboard; the app can prove protocol hygiene without KYC.”

---

## 1. Inventory (what we already have)

### 1.1 BIP39-SDK-GROK / bip39-onboarding-android

| Layer | State |
|-------|--------|
| Create / restore / quiz / disclaimer | Mature Compose flow |
| PIN + panic PIN + wipe-N | Done |
| Vault v1 envelope (DEK/KEK) | Done locally; dual KDF Argon2id/PBKDF2 |
| Rust BIP39 + Argon2id KDF | Done locally; partial GitHub sync |
| Word-list validation + word entry UI | Done locally |
| Biometric PIN seal, session lock, FLAG_SECURE | Done |
| iOS | Skeleton only |
| Package name `com.example…` | Must change |

### 1.2 BIP39-Onboarding-pipeline (TS + bip39-auth-rs)

| Layer | Useful for unified stack |
|-------|---------------------------|
| Multi-chain derive (EVM/BTC/SOL) | Yes — host key API |
| EIP-191 challenge-response | Optional auth product, not vault core |
| Cloud E2EE key derivation | Optional module |
| AML registry (`aml.rs`) | Seed for Stigmata / AddressRiskProvider |
| Stealth / ERC-4337 | Out of core; host-specific |
| Web onboarding UI | Reference UX only; mobile is source of truth for security |

### 1.3 in-app-keyboard **2.0.0** (Claude audit zip)

Reference **private input** layer. Verified design, not yet CI-green in the audit environment.

| Capability | Relevance to crypto onboarding |
|------------|--------------------------------|
| `onKey`-only I/O, empty manifest, privacy-check CI | **Hard requirement** for seed/PIN |
| `secureMode` (no preview, no suggestions, no recent emoji) | Seed restore, passphrase |
| `NumericKeypad(shuffle)` | PIN / panic PIN (anti shoulder-surf + touch path) |
| `SystemKeyboardGate` | Block Gboard on Compose fields |
| `SecureWindowEffect` | Align with SDK FLAG_SECURE |
| `secureTouches` (tapjacking) | Optional on PIN |
| 8 schemes + Auto, WCAG AA tests | Map to SDK theme presets |
| RU/EN/UK/DE layouts, BIP39 word entry | Restore flow |
| Honest threat model (IME closed; Accessibility not) | Must document in SECURITY.md |
| HANDOFF: compile → test → sample → release | Gate before depending on 2.0 |

**Do not weaken keyboard privacy rules when integrating.**

### 1.4 Stigmata

Not on GitHub yet. Planned as **bidirectional dirt passport** (outbound counterparty + inbound self-watch), no KYC.

---

## 2. Target architecture (unified pipeline)

### 2.1 Modules (recommended monorepo *or* versioned multi-repo)

Prefer **one umbrella repo** later (`sovereign-stack` / keep `BIP39-SDK-GROK` as umbrella). Until then, strict **semver contracts**:

```text
packages/
  design-tokens/          # colors, type, motion — shared JSON + Compose + Swift
  keyboard/               # in-app-keyboard 2.x (com.narde.keyboard or relocated)
  bip39-core/             # Rust: BIP39, Argon2id, vault code
  bip39-onboarding/       # Android/iOS UI + vault glue
  derive/                 # multi-chain key derivation
  risk/                   # AddressRiskProvider (Stigmata)
```

### 2.2 Hard contracts

1. **Vault format** = `docs/VAULT_FORMAT.md` (entropy body, Argon2id primary).
2. **Keyboard** = `onKey` only; host never sees IME; `secureMode` default for seed/PIN/passphrase.
3. **No KYC** in core. Risk module is optional and local-first.
4. **Agent prompts** live in `docs/AGENT_GUIDE.md` + `docs/AI_PROMPTS.md`.

---

## 3. Phases (U0–U6)

| Phase | Goal | Exit criteria |
|-------|------|---------------|
| **U0** | Inventory + STACK.md | This document + module map |
| **U1** | Wire keyboard 2.0 into onboarding | SecureOnboardingShell, PinKeypad shuffle, SecureSeedKeyboard |
| **U2** | Vault entropy body + dual KDF | VAULT_FORMAT, VaultV1 create/unlock, BiometricPinCache |
| **U3** | Rust vault core parity | kdf.rs + UniFFI/JNI full path |
| **U4** | Theme bridge (20 presets ↔ 8 keyboard schemes) | KeyboardThemeBridge |
| **U5** | Derive + risk stubs | AppKeyDeriver, AddressRiskProvider |
| **U6** | iOS parity + JitPack release | Package rename, publish, sample apps |

---

## 4. Threat model (summary)

- **Closed:** system IME, cloud backup of vault, PIN-as-key next to ciphertext.
- **Open / residual:** Accessibility services, physical access + shoulder, Keystore wipe on device reset, side-channel on Argon2id timing (mitigated by constant params).
- **Non-goals:** malware with root, screen recording by privileged apps, social engineering.

See `docs/SECURITY.md` and keyboard `PRIVACY-AUDIT.md`.

---

## 5. Agent handoff

When an external auditor or agent reviews:

1. Read `docs/AUDIT_BRIEF_RECHECK.md`.
2. Verify GitHub tree matches local `bip39-onboarding-android` for vault + keyboard.
3. Run `scripts/doctor.sh` and keyboard privacy check.
4. Confirm no system IME path remains for seed/PIN.

Source of truth until full sync lands: local tree + `BIP39-SDK-GROK-FULL-SYNC.zip`.
