//! Portable BIP39 core — Phase C of bip39-onboarding-android.

use bip39::{Language, Mnemonic};
use rand::{rngs::OsRng, RngCore};
use sha2::{Digest, Sha256};
use thiserror::Error;

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum Strength {
    Words12,
    Words24,
}

impl Strength {
    pub fn entropy_bits(self) -> usize {
        match self {
            Strength::Words12 => 128,
            Strength::Words24 => 256,
        }
    }

    pub fn word_count(self) -> usize {
        match self {
            Strength::Words12 => 12,
            Strength::Words24 => 24,
        }
    }
}

#[derive(Debug, Error)]
pub enum EngineError {
    #[error("invalid mnemonic")]
    InvalidMnemonic,
    #[error("bip39: {0}")]
    Bip39(String),
}

fn zeroize_bytes(buf: &mut [u8]) {
    for b in buf.iter_mut() {
        *b = 0;
    }
}

/// Generate a new English BIP39 mnemonic.
pub fn generate_mnemonic(strength: Strength) -> Result<String, EngineError> {
    let mut entropy = vec![0u8; strength.entropy_bits() / 8];
    OsRng.fill_bytes(&mut entropy);
    let mnemonic = Mnemonic::from_entropy_in(Language::English, &entropy)
        .map_err(|e| EngineError::Bip39(e.to_string()))?;
    zeroize_bytes(&mut entropy);
    Ok(mnemonic.to_string())
}

pub fn validate_mnemonic(phrase: &str) -> bool {
    Mnemonic::parse_in_normalized(Language::English, phrase).is_ok()
}

pub fn mnemonic_to_seed(phrase: &str, passphrase: &str) -> Result<Vec<u8>, EngineError> {
    let mnemonic = Mnemonic::parse_in_normalized(Language::English, phrase)
        .map_err(|_| EngineError::InvalidMnemonic)?;
    Ok(mnemonic.to_seed(passphrase).to_vec())
}

/// First 8 bytes of BIP39 seed as hex — matches Kotlin [Bip39Manager.fingerprint].
pub fn fingerprint_seed_prefix(phrase: &str) -> Result<String, EngineError> {
    let mut seed = mnemonic_to_seed(phrase, "")?;
    let out = seed[..8]
        .iter()
        .map(|b| format!("{:02x}", b))
        .collect::<String>();
    zeroize_bytes(&mut seed);
    Ok(out)
}

/// SHA-256(seed)[0..8] hex — alternate stable id for cross-checks.
pub fn fingerprint_sha256(phrase: &str) -> Result<String, EngineError> {
    let mut seed = mnemonic_to_seed(phrase, "")?;
    let hash = Sha256::digest(&seed);
    zeroize_bytes(&mut seed);
    Ok(hash[..8].iter().map(|b| format!("{:02x}", b)).collect())
}

#[cfg(test)]
mod tests {
    use super::*;

    const ABANDON12: &str =
        "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about";

    #[test]
    fn generate_12_valid() {
        let m = generate_mnemonic(Strength::Words12).unwrap();
        assert_eq!(m.split_whitespace().count(), 12);
        assert!(validate_mnemonic(&m));
    }

    #[test]
    fn generate_24_valid() {
        let m = generate_mnemonic(Strength::Words24).unwrap();
        assert_eq!(m.split_whitespace().count(), 24);
        assert!(validate_mnemonic(&m));
    }

    #[test]
    fn abandon_vector_seed_prefix() {
        assert!(validate_mnemonic(ABANDON12));
        let fp = fingerprint_seed_prefix(ABANDON12).unwrap();
        assert_eq!(fp, "5eb00bbddcf06908");
    }

    #[test]
    fn passphrase_changes_seed() {
        let a = mnemonic_to_seed(ABANDON12, "").unwrap();
        let b = mnemonic_to_seed(ABANDON12, "TREZOR").unwrap();
        assert_ne!(a, b);
    }

    #[test]
    fn invalid_phrase_rejected() {
        assert!(!validate_mnemonic("not a real phrase at all"));
    }
}
