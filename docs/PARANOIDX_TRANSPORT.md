# BIP39-SDK-GROK × ParanoidX Node — transport integration

**Status:** design note (2026-11-20)  
**Scope:** how mobile onboarding identity connects to ParanoidX transport **without** putting seed on the wire.

## Roles

| Layer | Owner | Responsibility |
|-------|--------|----------------|
| Root identity | BIP39-SDK-GROK (Android vault v1) | Create/restore mnemonic, PIN vault, in-app keyboard, session unlock |
| Transport | ParanoidX Node (`internal/transport`) | App registration (API key), WebSocket hub, SimpleX bridge, Tor, P2P file transfer |
| Routing | ParanoidX (`internal/paranoidx`, Docker VLESS/VMess/Tor) | Multi-layer egress; not part of SDK |

SDK **never** talks to the network. Host (Isle / Royal / custom) unlocks vault, derives transport credentials, then uses Node APIs.

## Unlock policy (shared)

- **PIN-only.** No biometric unlock on mobile or as Node auth factor.
- Panic PIN → local vault wipe; host should drop in-memory transport keys and disconnect WS.

## Recommended key hierarchy (host-side after unlock)

```text
BIP39 seed (from VaultUnlock / UnlockedSession)
  │
  ├─ HKDF(seed, info="paranoidx/transport/ed25519/v1") → device transport keypair
  ├─ HKDF(seed, info="paranoidx/simplex/link/v1")     → optional SimpleX link material
  └─ HKDF(seed, info="paranoidx/api-key/v1")          → optional static API key material
```

Use existing SDK domains via `KeyDerivationKit` / `AppKeyDeriver` where possible; add explicit info strings above so Node and mobile never collide with vault fingerprint/pp-check keys.

ParanoidX already has `internal/crypto/bip39` (mnemonic → Ed25519). Prefer **one** derivation story:

1. Mobile: vault holds entropy; after PIN unlock, host derives Ed25519 (or registers a public key).
2. Node: does **not** store seed; authenticates apps via `X-API-Key` / registered pubkey (see `transport.Hub`).

## Transport surfaces on Node

From `internal/transport`:

1. **Hub** — register app, authenticate API key, WebSocket subscribe/broadcast, audit log.
2. **P2P transfer** (`transfer.go`) — TCP length-prefixed payloads for vault *files* / radio (not BIP39 vault blob). Do not send mnemonic or DEK over P2P.
3. **SimpleX bridge** — chat path over Tor; identity is SimpleX links, not raw BIP39.

Mobile host flow:

```text
Onboarding (SDK) → VaultV1 create
Daily open → VaultUnlock(PIN) → derive transport keys
→ HTTPS/WS to Node (preferably over Tor / ParanoidX tunnel)
→ Hub.Register / Authenticate / Subscribe
→ SessionLock / wipe clears derived keys
```

## What not to do

- Do not send mnemonic, PIN, or vault blob to `/api/*`.
- Do not use biometrics as Node or app unlock.
- Do not reuse vault HMAC info strings (`bip39-sdk/fingerprint/v1`, `pp-check`) for transport keys.
- Do not treat ParanoidX `internal/vault` (disk quota for files) as BIP39 vault — different product surface.

## Minimal host checklist

1. Depend on `bip39-onboarding` + optional `:keyboard`.
2. `Bip39Features.enableBiometricUnlock` left default (forced false).
3. After `UnlockResult.Success`, derive keys with domain separation; put public identity in Node registry.
4. Wire `SessionLock` / panic → disconnect transport + zero keys.
5. Prefer Tor-aware HTTP client (Isle already has `tor_aware_client.dart` patterns) for Node base URL.

## Future (optional)

- Shared test vectors: mnemonic → transport pubkey (mobile Kotlin + Node Go).
- UniFFI core later so the same KDF domains exist in Go/Rust without reimplementation drift.
