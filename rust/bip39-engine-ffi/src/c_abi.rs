use bip39_engine::{
    fingerprint_seed_prefix, generate_mnemonic, mnemonic_to_seed, validate_mnemonic, Strength,
};
use std::ffi::{CStr, CString};
use std::os::raw::{c_char, c_int};
use std::ptr;

pub const BIP39_OK: c_int = 0;
pub const BIP39_ERR_NULL: c_int = -1;
pub const BIP39_ERR_UTF8: c_int = -2;
pub const BIP39_ERR_STRENGTH: c_int = -3;
pub const BIP39_ERR_MNEMONIC: c_int = -4;
pub const BIP39_ERR_INTERNAL: c_int = -5;

fn strength_from(words: c_int) -> Option<Strength> {
    match words {
        12 => Some(Strength::Words12),
        24 => Some(Strength::Words24),
        _ => None,
    }
}

unsafe fn cstr_to_str<'a>(p: *const c_char) -> Result<&'a str, c_int> {
    if p.is_null() {
        return Err(BIP39_ERR_NULL);
    }
    CStr::from_ptr(p).to_str().map_err(|_| BIP39_ERR_UTF8)
}

#[no_mangle]
pub unsafe extern "C" fn bip39_generate(word_count: c_int, out: *mut *mut c_char) -> c_int {
    if out.is_null() {
        return BIP39_ERR_NULL;
    }
    *out = ptr::null_mut();
    let Some(strength) = strength_from(word_count) else {
        return BIP39_ERR_STRENGTH;
    };
    match generate_mnemonic(strength) {
        Ok(s) => match CString::new(s) {
            Ok(cs) => {
                *out = cs.into_raw();
                BIP39_OK
            }
            Err(_) => BIP39_ERR_INTERNAL,
        },
        Err(_) => BIP39_ERR_INTERNAL,
    }
}

#[no_mangle]
pub unsafe extern "C" fn bip39_validate(phrase: *const c_char) -> c_int {
    match cstr_to_str(phrase) {
        Ok(s) => {
            if validate_mnemonic(s) {
                1
            } else {
                0
            }
        }
        Err(e) => e,
    }
}

#[no_mangle]
pub unsafe extern "C" fn bip39_to_seed(
    phrase: *const c_char,
    passphrase: *const c_char,
    out: *mut *mut u8,
    out_len: *mut usize,
) -> c_int {
    if out.is_null() || out_len.is_null() {
        return BIP39_ERR_NULL;
    }
    *out = ptr::null_mut();
    *out_len = 0;
    let phrase = match cstr_to_str(phrase) {
        Ok(s) => s,
        Err(e) => return e,
    };
    let pass = if passphrase.is_null() {
        ""
    } else {
        match cstr_to_str(passphrase) {
            Ok(s) => s,
            Err(e) => return e,
        }
    };
    match mnemonic_to_seed(phrase, pass) {
        Ok(seed) => {
            let len = seed.len();
            let mut boxed = seed.into_boxed_slice();
            let ptr = boxed.as_mut_ptr();
            std::mem::forget(boxed);
            *out = ptr;
            *out_len = len;
            BIP39_OK
        }
        Err(_) => BIP39_ERR_MNEMONIC,
    }
}

#[no_mangle]
pub unsafe extern "C" fn bip39_fingerprint(phrase: *const c_char, out: *mut *mut c_char) -> c_int {
    if out.is_null() {
        return BIP39_ERR_NULL;
    }
    *out = ptr::null_mut();
    let phrase = match cstr_to_str(phrase) {
        Ok(s) => s,
        Err(e) => return e,
    };
    match fingerprint_seed_prefix(phrase) {
        Ok(s) => match CString::new(s) {
            Ok(cs) => {
                *out = cs.into_raw();
                BIP39_OK
            }
            Err(_) => BIP39_ERR_INTERNAL,
        },
        Err(_) => BIP39_ERR_MNEMONIC,
    }
}

#[no_mangle]
pub unsafe extern "C" fn bip39_string_free(s: *mut c_char) {
    if !s.is_null() {
        drop(CString::from_raw(s));
    }
}

#[no_mangle]
pub unsafe extern "C" fn bip39_bytes_free(p: *mut u8, len: usize) {
    if !p.is_null() && len > 0 {
        drop(Vec::from_raw_parts(p, len, len));
    }
}
