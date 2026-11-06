//! JNI exports for [com.example.bip39onboarding.engine.RustMnemonicEngine].

use bip39_engine::{
    fingerprint_seed_prefix, generate_mnemonic, mnemonic_to_seed, validate_mnemonic, Strength,
};
use jni::objects::{JClass, JString};
use jni::sys::{jbyteArray, jint, jstring};
use jni::JNIEnv;

fn strength(words: jint) -> Option<Strength> {
    match words {
        12 => Some(Strength::Words12),
        24 => Some(Strength::Words24),
        _ => None,
    }
}

#[no_mangle]
pub extern "system" fn Java_com_example_bip39onboarding_engine_RustMnemonicEngine_nativeGenerate(
    mut env: JNIEnv,
    _class: JClass,
    word_count: jint,
) -> jstring {
    let Some(s) = strength(word_count) else {
        let _ = env.throw_new("java/lang/IllegalArgumentException", "word_count must be 12 or 24");
        return std::ptr::null_mut();
    };
    match generate_mnemonic(s) {
        Ok(phrase) => env
            .new_string(phrase)
            .map(|js| js.into_raw())
            .unwrap_or(std::ptr::null_mut()),
        Err(e) => {
            let _ = env.throw_new("java/lang/RuntimeException", &e.to_string());
            std::ptr::null_mut()
        }
    }
}

#[no_mangle]
pub extern "system" fn Java_com_example_bip39onboarding_engine_RustMnemonicEngine_nativeValidate(
    mut env: JNIEnv,
    _class: JClass,
    phrase: JString,
) -> jint {
    let Ok(java_str) = env.get_string(&phrase) else {
        return 0;
    };
    let s: String = java_str.into();
    if validate_mnemonic(&s) {
        1
    } else {
        0
    }
}

#[no_mangle]
pub extern "system" fn Java_com_example_bip39onboarding_engine_RustMnemonicEngine_nativeToSeed(
    mut env: JNIEnv,
    _class: JClass,
    phrase: JString,
    passphrase: JString,
) -> jbyteArray {
    let Ok(p) = env.get_string(&phrase) else {
        return std::ptr::null_mut();
    };
    let phrase: String = p.into();
    let pass: String = env
        .get_string(&passphrase)
        .map(|s| s.into())
        .unwrap_or_default();
    match mnemonic_to_seed(&phrase, &pass) {
        Ok(seed) => env
            .byte_array_from_slice(&seed)
            .map(|a| a.into_raw())
            .unwrap_or(std::ptr::null_mut()),
        Err(_) => {
            let _ = env.throw_new("java/lang/IllegalArgumentException", "invalid mnemonic");
            std::ptr::null_mut()
        }
    }
}

#[no_mangle]
pub extern "system" fn Java_com_example_bip39onboarding_engine_RustMnemonicEngine_nativeFingerprint(
    mut env: JNIEnv,
    _class: JClass,
    phrase: JString,
) -> jstring {
    let Ok(p) = env.get_string(&phrase) else {
        return std::ptr::null_mut();
    };
    let phrase: String = p.into();
    match fingerprint_seed_prefix(&phrase) {
        Ok(fp) => env
            .new_string(fp)
            .map(|js| js.into_raw())
            .unwrap_or(std::ptr::null_mut()),
        Err(_) => {
            let _ = env.throw_new("java/lang/IllegalArgumentException", "invalid mnemonic");
            std::ptr::null_mut()
        }
    }
}
