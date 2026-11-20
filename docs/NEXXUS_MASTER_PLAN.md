# NeXXuS — Master Development Plan (agent + auditor)

**Status:** living plan · 2026-11-20  
**Owner vision:** universal transport + identity/onboarding SDK + in-app keyboard + app runtime + store + base apps, living in NeXXuS community space. SimpleX = transport layer for applications, not only human chat.  
**Store / marketplace repo:** [PerfectFriend/ParaMarket](https://github.com/PerfectFriend/ParaMarket)  
**Working method:** slow, release-by-release; external auditor **Колян**; agent executes in thin vertical slices.  
**Identity policy:** PIN-only unlock (no biometrics). Seed never on the wire.

---

## 0. Honest baseline

Portfolio is large (~55 repos under PerfectFriend): identity, SimpleX/Rust transport, NeXXuS storage nodes, ParanoidX economy, messengers, games, vertical markets, radio, living-code agents. Much is prototype-grade, duplicated, or parallel-evolved on free models / constrained networks. That is a feature of velocity and a risk for integration.

**Do not** try to ship 55 products. **Do** collapse into one platform spine + a short base-app shelf.

Success metric for this plan: each phase ends with a **tagged, installable, auditable release** — not another parallel repo.

---

## 1. Target architecture (normative)

```
NeXXuS Platform
├── L0 Identity Runtime     BIP39-SDK-GROK + in-app-keyboard
│                           vault v1 · PIN · session · HKDF domains
├── L1 Transport            SimpleX (SMP/XFTP) as app protocol
│                           RustSimplex / NeXXChat agent · Node bridge
├── L2 Storage mesh         NeXXuS Node (shards, RS, Tor) — blobs only
├── L3 Community / economy  ParanoidX / Isle (optional plane)
├── L4 App Runtime          session handle · permissions · envelope
├── L5 App SDK + Store      **ParaMarket** — manifest · signed packages · catalog
└── L6 Base apps            messenger · radio · feed · games · market
```

**Hard rules**

1. Apps never receive mnemonic/PIN; only scoped session + capabilities.
2. Identity SDK does not embed networking.
3. Transport does not implement vault format.
4. Store installs signed manifests; no “random APK from chat”.
5. Duplicates merge; one app id per product surface.

---

## 2. Repository roles (collapse map)

### Platform spine (keep / harden)

| Role | Primary repo | Notes |
|------|--------------|--------|
| Identity + keyboard | `BIP39-SDK-GROK` (+ absorb `in-app-keyboard`, pipeline) | Release train A |
| SimpleX core | `RustSimplex` | Wire-compatible SMP/XFTP |
| Messenger client | `NeXXChat` | Becomes base app `nexxus.messenger` |
| Storage node | `NeXXuS-Node-Hermes-Rust` (or designate single canonical Node) | Pick ONE canonical; others = archive/mirror |
| Economy / bridge | `ParanoidX-Node` | Optional L3 |
| **Store / App SDK** | **`ParaMarket`** | Phase D home |
| Keyboard standalone | `in-app-keyboard` | Submodule or Maven coord of spine |

### Base-app candidates (dedupe then ship)

| Store id | Source repos | Action |
|----------|--------------|--------|
| `nexxus.messenger` | NeXXChat, Paranoid-Chat | Merge UX; one binary |
| `nexxus.radio` | RadioArmageddonFM | Client shell on transport |
| `nexxus.comics` | Nasreddin | Content reader + optional publisher |
| `nexxus.feed.cats` | VGCatstagram-Rust, Van-Gogh-Cats | One image feed |
| `nexxus.game.oracle` | CyberOracle, WEED-GURU-TAROT, ORACLE-*, CyberTarot-Remix | **One** Oracle app, multiple decks/themes |
| `nexxus.game.madgammon` | BG-AG, MG-AG, MadGammon* | **One** backgammon client |
| `nexxus.game.arcade` | Neon-Snake, memory-game, sound-memory | Mini-pack |
| `nexxus.market.nailix` | Onyx-Aura, NailiX-Android | Services marketplace |
| `nexxus.isle` | ParanoidX clients / SMLI | Community + economy UI |

### Explicit non-store (tools / OS / research)

GodModeCoder, living-code*, Torquemada-AGI-OS, SuperGuard*, cableguard — **Builders / system** shelf or internal only until threat model written.

### Archive policy

Repos parallel forks after merge: README → “Superseded by X”, freeze default branch, no new features.

---

## 3. Phased delivery (slow releases)

Each phase: **spec freeze → implement → agent self-check → Колян audit → tag → only then next**.

### Phase A — Identity Runtime 1.0 (current home repo)

**Goal:** production-usable onboarding/vault/keyboard for any NeXXuS host.

- [ ] Vault v1 complete per `VAULT_FORMAT.md` (entropy body, Argon2id path, tests)
- [ ] PIN-only enforced (`Bip39Features`, no-op biometrics) — done baseline
- [ ] In-app keyboard wired for seed/PIN/passphrase
- [ ] Public API: create / restore / unlock / lock / wipe / derive(domain)
- [ ] HKDF domains documented: `nexxus/transport/v1`, `nexxus/app/v1`, vault domains separate
- [ ] JitPack/Maven publishable module(s)
- [ ] Auditor pack: SECURITY, VAULT_FORMAT, threat model, test vectors

**Exit:** tag `identity-1.0.0` · Колян sign-off on crypto/UX boundaries

### Phase B — TransportSession + Envelope 0.1

**Goal:** identity can feed apps without embedding SimpleX inside vault.

- [ ] `TransportSession` API (keys out, no sockets in identity SDK)
- [ ] NeXXuS message envelope v1 (type, app, from, body, ts)
- [ ] Reference: Android host unlock → derive → connect bridge (Tor-aware)
- [ ] Golden vectors: mnemonic → transport pubkey (Kotlin + Rust/Go)

**Exit:** tag `transport-session-0.1.0` · audit: no seed on wire

### Phase C — Canonical nodes

**Goal:** one storage story, one messaging bridge story.

- [ ] Declare canonical NeXXuS Node repo; document install + POR limits
- [ ] ParanoidX bridge role vs NeXXuS storage role (no name collision “vault”)
- [ ] App blob upload/download via node API using session caps, not mnemonic

**Exit:** tag `nexxus-node-rc` + runbook for Колян

### Phase D — App Runtime + Store v0 (**repo: ParaMarket**)

**Goal:** installable catalog of signed first-party apps.

Canonical repository: `PerfectFriend/ParaMarket`.

- [ ] `nexxus-app.json` manifest schema
- [ ] Permission model: transport.send/recv, storage.blob, profile.read, …
- [ ] Host runtime: grant caps after PIN unlock; revoke on lock/wipe
- [ ] Store v0: static catalog + local install (no full economy required)
- [ ] App SDK stub: `onCreate(session)`, send/subscribe, private storage

**Exit:** tag `store-0.1.0` · two apps installable end-to-end

### Phase E — Base apps (first shelf)

Order chosen for dependency and demo value:

1. `nexxus.messenger` (proves transport)
2. `nexxus.game.oracle` (proves app SDK + fun surface)
3. `nexxus.market.nailix` or `nexxus.radio` (proves vertical / media)
4. MadGammon, Arcade, Comics, Catstagram, Isle — as capacity allows

Each app: manifest, session-only secrets, one store listing, smoke test script.

**Exit:** tag `base-apps-1.0` when ≥3 apps pass auditor checklist

### Phase F — Community plane (optional parallel after D)

ParanoidX economy, Isle governance — only after store + messenger stable. Do not block A–E.

---

## 4. Working agreement with Колян (auditor)

**What we send each gate**

1. Tag + commit SHA  
2. Short delta note (what changed, what explicitly out of scope)  
3. Threat-model touchpoints  
4. How to build/test in <30 minutes  
5. Known residuals (honest list)

**What we ask from Колян**

- Crypto/identity boundaries (Phase A/B)
- Transport leak surface (seed, PIN, over-broad caps)
- Store permission model (Phase D / ParaMarket)
- Per-app: can it exfiltrate identity? can it brick vault?

**What we do not ask**

- Product taste for every game skin
- Full formal verification on day one

Agent response to findings: fix or document residual; no silent dismiss.

---

## 5. Agent execution rules (this project)

1. Prefer **one spine repo** work (`BIP39-SDK-GROK`) until Phase A exits.  
2. Cross-repo only with explicit user go-ahead and a written merge target.  
3. No biometrics reintroduction.  
4. No new parallel “NeXXuS-Final-Final” repos.  
5. After each meaningful slice: update this plan checkboxes + `SYNC_STATUS` / release notes.  
6. Push GitHub in small verified batches when user requests publish.  
7. When stuck: write the decision in `docs/` rather than invent a sixth tarot client.

---

## 6. Near-term backlog (next concrete slices)

1. Finish identity 1.0 gaps from latest audit (vault tests, native KDF self-test, API polish).  
2. Add `docs/NEXXUS_STACK.md` short public summary (link from README).  
3. Implement `KeyDerivationKit` domains `nexxus/transport/v1`, `nexxus/app/v1`.  
4. Stub `TransportSession` interface in library (no network).  
5. Bootstrap **ParaMarket** repo: README, `nexxus-app.json` schema, example messenger manifest.  
6. Prepare **Auditor Brief for Колян** (Phase A): build, vectors, PIN-only policy, residual risks.

---

## 7. Risk register (short)

| Risk | Mitigation |
|------|------------|
| Repo sprawl / duplicates | Collapse map §2; archive superseded |
| Prototype quality variance | Phase gates + auditor |
| Scope fantasy (OS + AGI + store + games) | Phases A–E ordered; F optional |
| Seed leakage via “helpful” app APIs | Session caps; auditor focus B/D |
| Burnout / context loss | This document; slow releases |
| Node naming confusion (vault vs vault) | Glossary in STACK / PARANOIDX_TRANSPORT |

---

## 8. Glossary

- **Identity Runtime** — onboarding, vault, keyboard, session, derive  
- **Transport** — SimpleX queues + bridges; app envelopes  
- **NeXXuS Node** — decentralized storage mesh (not BIP39 vault file)  
- **ParaMarket** — catalog + manifest + App SDK + install of apps  
- **Base app** — first-party application on App SDK  

---

*Plan written for continuous agent use and for handoff to auditor Колян. Update in place; do not fork the plan into parallel files without a reason.*
