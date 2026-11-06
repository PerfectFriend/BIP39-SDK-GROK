import Foundation

#if canImport(Bip39EngineC)
import Bip39EngineC
#endif

/// Swift façade over the shared Rust BIP39 core (C ABI).
public enum Bip39EngineError: Error {
    case invalidStrength
    case invalidMnemonic
    case native(Int32)
    case unavailable
}

public enum Bip39Strength: Int32 {
    case words12 = 12
    case words24 = 24
}

public enum Bip39Engine {

    /// Generate a new English mnemonic.
    public static func generate(strength: Bip39Strength = .words12) throws -> String {
        var out: UnsafeMutablePointer<CChar>?
        let code = bip39_generate(strength.rawValue, &out)
        guard code == BIP39_OK, let out else { throw Bip39EngineError.native(code) }
        defer { bip39_string_free(out) }
        return String(cString: out)
    }

    public static func validate(_ phrase: String) -> Bool {
        phrase.withCString { bip39_validate($0) == 1 }
    }

    public static func seed(phrase: String, passphrase: String = "") throws -> Data {
        var out: UnsafeMutablePointer<UInt8>?
        var len: Int = 0
        let code = phrase.withCString { p in
            passphrase.withCString { pp in
                bip39_to_seed(p, pp, &out, &len)
            }
        }
        guard code == BIP39_OK, let out else { throw Bip39EngineError.native(code) }
        defer { bip39_bytes_free(out, len) }
        return Data(bytes: out, count: len)
    }

    public static func fingerprint(phrase: String) throws -> String {
        var out: UnsafeMutablePointer<CChar>?
        let code = phrase.withCString { bip39_fingerprint($0, &out) }
        guard code == BIP39_OK, let out else { throw Bip39EngineError.native(code) }
        defer { bip39_string_free(out) }
        return String(cString: out)
    }
}
