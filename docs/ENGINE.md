# BIP39 Onboarding as an engine

This repository is meant to be a **reusable onboarding engine** for crypto and secure apps on Android — not a full wallet.

## What it owns

- BIP39 mnemonic lifecycle (create / restore / verify)
- PIN + panic PIN + optional passphrase
- Encrypted storage (Tink preferred)
- Unlock UI (PIN, lockout, optional biometrics)
- Hardened UX defaults (FLAG_SECURE, clipboard clear)

## What the host owns

- Key derivation after `SeedPayload.toSeedBytes()`
- Chain accounts, signing, network, portfolio UI
- Theme / branding
- Server-side Play Integrity (if required)

## Recommended integration pattern

```
Host App
  └── :bip39-onboarding  (this library)
        ├── OnboardingNavHost   → first run
        ├── PinUnlockScreen     → every cold start
        └── SeedStorage         → TinkSeedStorageAdapter
```

After unlock, call `payload.clear()` as soon as keys are derived.

## Versioning

- **1.x** — Android Compose engine (this repo)
- Future: optional iOS twin with the same flow contract

## AI-assisted development

Use `docs/AI_PROMPTS.md` so agent-generated screens stay aligned with the security rules already encoded in the library.
