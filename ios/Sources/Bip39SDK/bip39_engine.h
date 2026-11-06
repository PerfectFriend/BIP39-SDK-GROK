#ifndef BIP39_ENGINE_H
#define BIP39_ENGINE_H

#include <stddef.h>
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

#define BIP39_OK 0
#define BIP39_ERR_NULL (-1)
#define BIP39_ERR_UTF8 (-2)
#define BIP39_ERR_STRENGTH (-3)
#define BIP39_ERR_MNEMONIC (-4)
#define BIP39_ERR_INTERNAL (-5)

/** Generate mnemonic; *out must be freed with bip39_string_free. word_count: 12 or 24. */
int bip39_generate(int word_count, char **out);

/** 1 valid, 0 invalid, negative error */
int bip39_validate(const char *phrase);

/** 64-byte seed; *out freed with bip39_bytes_free */
int bip39_to_seed(const char *phrase, const char *passphrase, uint8_t **out, size_t *out_len);

/** 16-char hex fingerprint; free with bip39_string_free */
int bip39_fingerprint(const char *phrase, char **out);

void bip39_string_free(char *s);
void bip39_bytes_free(uint8_t *p, size_t len);

#ifdef __cplusplus
}
#endif
#endif
