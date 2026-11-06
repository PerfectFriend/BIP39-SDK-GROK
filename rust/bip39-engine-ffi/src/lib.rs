//! C ABI for Android + iOS. Optional JNI when built with `android-jni`.

mod c_abi;

#[cfg(feature = "android-jni")]
mod jni_bridge;

pub use bip39_engine::{
    fingerprint_seed_prefix, generate_mnemonic, mnemonic_to_seed, validate_mnemonic, EngineError,
    Strength,
};
pub use c_abi::*;
