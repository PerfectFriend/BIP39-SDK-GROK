//! Argon2id + HKDF key hierarchy for vault v1 (docs/VAULT_FORMAT.md).
//!
//! ```text
//! PIN → Argon2id(m,t,p) → ikm[32]
//! ikm ‖ pepper → HKDF-SHA256(info) → 32-byte key
//! ```

use argon2::{Algorithm, Argon2, Block, Params, Version};
use hkdf::Hkdf;
use hmac::{Hmac, Mac};
use sha2::Sha256;
use subtle::ConstantTimeEq;
use thiserror::Error;

type HmacSha256 = Hmac<Sha256>;

pub const KDF_ID_ARGON2ID: u8 = 1;
pub const MIN_M_KIB: u32 = 32_768;
pub const MIN_T: u32 = 2;
pub const DEFAULT_T: u32 = 3;
pub const DEFAULT_P: u32 = 1;
pub const DEFAULT_M_KIB: u32 = 32_768;
pub const IKM_LEN: usize = 32;
pub const KEY_LEN: usize = 32;

const INFO_KEK: &[u8] = b"bip39-sdk/kek/v1";
const INFO_PANIC: &[u8] = b"bip39-sdk/panic/v1";

#[derive(Debug, Error)]
pub enum KdfError {
    #[error("argon2 params rejected (anti-downgrade)")]
    ParamsTooWeak,
    #[error("argon2 failed: {0}")]
    Argon2(String),
    #[error("hkdf expand failed")]
    Hkdf,
    #[error("invalid pin (empty)")]
    EmptyPin,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub struct Argon2Params {
    pub m_kib: u32,
    pub t: u32,
    pub p: u32,
}

impl Default for Argon2Params {
    fn default() -> Self {
        Self {
            m_kib: DEFAULT_M_KIB,
            t: DEFAULT_T,
            p: DEFAULT_P,
        }
    }
}

impl Argon2Params {
    pub fn validate_for_read(self) -> Result<(), KdfError> {
        if self.m_kib < MIN_M_KIB || self.t < MIN_T || self.p == 0 {
            return Err(KdfError::ParamsTooWeak);
        }
        Ok(())
    }

    fn to_argon2_params(self) -> Result<Params, KdfError> {
        Params::new(self.m_kib, self.t, self.p, Some(IKM_LEN))
            .map_err(|e| KdfError::Argon2(e.to_string()))
    }
}

fn wipe(buf: &mut [u8]) {
    for b in buf.iter_mut() {
        *b = 0;
    }
}

pub fn argon2id_ikm(pin: &[u8], salt: &[u8], params: Argon2Params) -> Result<[u8; IKM_LEN], KdfError> {
    if pin.is_empty() {
        return Err(KdfError::EmptyPin);
    }
    let argon_params = params.to_argon2_params()?;
    let argon = Argon2::new(Algorithm::Argon2id, Version::V0x13, argon_params);
    let blocks_needed = params.m_kib as usize;
    let mut blocks = vec![Block::default(); blocks_needed];
    let mut out = [0u8; IKM_LEN];
    argon
        .hash_password_into_with_memory(pin, salt, &mut out, &mut blocks)
        .map_err(|e| KdfError::Argon2(e.to_string()))?;
    for b in blocks.iter_mut() {
        b.as_mut().fill(0);
    }
    Ok(out)
}

fn hkdf_expand(ikm: &[u8], salt: &[u8], info: &[u8]) -> Result<[u8; KEY_LEN], KdfError> {
    let hk = Hkdf::<Sha256>::new(Some(salt), ikm);
    let mut out = [0u8; KEY_LEN];
    hk.expand(info, &mut out).map_err(|_| KdfError::Hkdf)?;
    Ok(out)
}

pub fn derive_kek(
    pin: &[u8],
    salt: &[u8],
    pepper: &[u8],
    params: Argon2Params,
) -> Result<[u8; KEY_LEN], KdfError> {
    params.validate_for_read()?;
    let mut ikm = argon2id_ikm(pin, salt, params)?;
    let mut combined = Vec::with_capacity(ikm.len() + pepper.len());
    combined.extend_from_slice(&ikm);
    combined.extend_from_slice(pepper);
    wipe(&mut ikm);
    let key = hkdf_expand(&combined, salt, INFO_KEK);
    wipe(&mut combined);
    key
}

pub fn derive_panic_key(
    panic_pin: &[u8],
    salt: &[u8],
    pepper: &[u8],
    params: Argon2Params,
) -> Result<[u8; KEY_LEN], KdfError> {
    params.validate_for_read()?;
    let mut ikm = argon2id_ikm(panic_pin, salt, params)?;
    let mut combined = Vec::with_capacity(ikm.len() + pepper.len());
    combined.extend_from_slice(&ikm);
    combined.extend_from_slice(pepper);
    wipe(&mut ikm);
    let key = hkdf_expand(&combined, salt, INFO_PANIC);
    wipe(&mut combined);
    key
}

pub fn panic_mac(panic_key: &[u8], salt: &[u8]) -> Result<[u8; 32], KdfError> {
    let mut mac = HmacSha256::new_from_slice(panic_key).map_err(|_| KdfError::Hkdf)?;
    mac.update(salt);
    mac.update(b"panic");
    let result = mac.finalize().into_bytes();
    let mut out = [0u8; 32];
    out.copy_from_slice(&result);
    Ok(out)
}

pub fn ct_eq(a: &[u8], b: &[u8]) -> bool {
    if a.len() != b.len() {
        return false;
    }
    a.ct_eq(b).into()
}

pub fn default_params() -> Argon2Params {
    Argon2Params::default()
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn argon2id_deterministic_fast() {
        let params = Argon2Params {
            m_kib: 8 * 1024,
            t: 2,
            p: 1,
        };
        let salt = b"0123456789abcdef";
        let pin = b"123456";
        let a = argon2id_ikm(pin, salt, params).unwrap();
        let b = argon2id_ikm(pin, salt, params).unwrap();
        assert_eq!(a, b);
        assert_ne!(a, [0u8; 32]);
    }

    #[test]
    fn params_anti_downgrade() {
        let weak = Argon2Params {
            m_kib: 1024,
            t: 1,
            p: 1,
        };
        assert!(weak.validate_for_read().is_err());
        assert!(Argon2Params::default().validate_for_read().is_ok());
    }

    #[test]
    fn kek_domain_separated_from_panic() {
        let salt = [9u8; 16];
        let ikm = [3u8; 32];
        let k1 = hkdf_expand(&ikm, &salt, INFO_KEK).unwrap();
        let k2 = hkdf_expand(&ikm, &salt, INFO_PANIC).unwrap();
        assert_ne!(k1, k2);
    }

    #[test]
    fn panic_mac_ct_eq() {
        let key = [1u8; 32];
        let salt = [2u8; 16];
        let m1 = panic_mac(&key, &salt).unwrap();
        let m2 = panic_mac(&key, &salt).unwrap();
        assert!(ct_eq(&m1, &m2));
        let mut m3 = m1;
        m3[0] ^= 1;
        assert!(!ct_eq(&m1, &m3));
    }

    #[test]
    fn empty_pin_rejected() {
        let p = Argon2Params {
            m_kib: 8192,
            t: 2,
            p: 1,
        };
        assert!(matches!(
            argon2id_ikm(b"", b"0123456789abcdef", p),
            Err(KdfError::EmptyPin)
        ));
    }

    #[test]
    fn derive_kek_runs() {
        let params = Argon2Params {
            m_kib: 8192,
            t: 2,
            p: 1,
        };
        let salt = b"0123456789abcdef";
        let pepper = [9u8; 32];
        let mut ikm = argon2id_ikm(b"847291", salt, params).unwrap();
        let mut combined = ikm.to_vec();
        combined.extend_from_slice(&pepper);
        wipe(&mut ikm);
        let kek = hkdf_expand(&combined, salt, INFO_KEK).unwrap();
        assert_ne!(kek, [0u8; 32]);
    }
}
