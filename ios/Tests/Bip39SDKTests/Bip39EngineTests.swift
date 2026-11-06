import XCTest
@testable import Bip39SDK

final class Bip39EngineTests: XCTestCase {
    func testValidateAbandon() throws {
        let phrase = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about"
        // Requires linked native lib
        XCTAssertTrue(Bip39Engine.validate(phrase))
        let fp = try Bip39Engine.fingerprint(phrase: phrase)
        XCTAssertEqual(fp, "5eb00bbddcf06908")
    }
}
