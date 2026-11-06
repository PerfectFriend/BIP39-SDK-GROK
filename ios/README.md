# Bip39SDK (iOS)

SwiftUI + shared **Rust** BIP39 core (`bip39-engine-ffi` C ABI).

## Build native (macOS)

```bash
./scripts/build-rust-ios.sh
# → ios/Frameworks/Bip39Engine.xcframework
```

## Xcode

1. Open or create an app.
2. Add local Swift package: `ios/` **or** drag `Bip39Engine.xcframework`.
3. Import:

```swift
import Bip39SDK

WelcomeAboardView(onCreate: { }, onRestore: { })
// or
OnboardingFlowView { mnemonic in
  // store in Keychain, derive keys, wipe string
}
```

## Parity with Android

| Feature | Android | iOS |
|---------|---------|-----|
| BIP39 generate/validate/seed | Rust / JVM | Rust (C ABI) |
| Welcome Aboard | Compose | SwiftUI |
| Full PIN / Tink / PDF | Yes | Host Keychain (extend) |
| Theme presets | 20 Compose | Colors on WelcomeAboardView |

Full PIN/panic/Tink parity on iOS is a follow-up; crypto core is shared today.
