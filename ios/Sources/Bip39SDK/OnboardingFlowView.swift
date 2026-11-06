import SwiftUI

/// Minimal create/restore shell. Host embeds Keychain storage and PIN UI as needed.
public struct OnboardingFlowView: View {
    @State private var step: Step = .welcome
    @State private var mnemonic: String = ""
    @State private var error: String?

    public var onComplete: (String) -> Void

    public init(onComplete: @escaping (String) -> Void) {
        self.onComplete = onComplete
    }

    enum Step {
        case welcome, strength, showSeed, restore, done
    }

    public var body: some View {
        Group {
            switch step {
            case .welcome:
                WelcomeAboardView(
                    onCreate: { step = .strength },
                    onRestore: { step = .restore }
                )
            case .strength:
                VStack(spacing: 16) {
                    Text("Mnemonic strength").font(.title2)
                    Button("12 words") { generate(12) }
                    Button("24 words") { generate(24) }
                }
            case .showSeed:
                ScrollView {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Write this down offline").font(.headline)
                        Text(mnemonic).font(.body.monospaced())
                        if let fp = try? Bip39Engine.fingerprint(phrase: mnemonic) {
                            Text("Fingerprint: \(fp)").font(.caption).foregroundStyle(.secondary)
                        }
                        Button("I wrote it down") {
                            onComplete(mnemonic)
                            step = .done
                        }
                        .buttonStyle(.borderedProminent)
                    }
                    .padding()
                }
            case .restore:
                VStack(spacing: 12) {
                    Text("Paste seed phrase").font(.title2)
                    TextEditor(text: $mnemonic)
                        .frame(minHeight: 120)
                        .border(.secondary)
                    if let error { Text(error).foregroundStyle(.red) }
                    Button("Continue") {
                        if Bip39Engine.validate(mnemonic) {
                            onComplete(mnemonic)
                            step = .done
                        } else {
                            error = "Invalid mnemonic"
                        }
                    }
                }
                .padding()
            case .done:
                Text("Hand-off complete").font(.title2)
            }
        }
    }

    private func generate(_ n: Int32) {
        let s: Bip39Strength = n == 24 ? .words24 : .words12
        do {
            mnemonic = try Bip39Engine.generate(strength: s)
            step = .showSeed
        } catch {
            self.error = "\(error)"
        }
    }
}
