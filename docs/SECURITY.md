# Чеклист безопасности BIP39 Onboarding

## Обязательные практики (уже реализованы)

- [x] Генерация только через `SecureRandom`
- [x] Использование `CharArray` для mnemonic и PIN
- [x] Валидация checksum BIP39
- [x] Disclaimer + обязательный чекбокс
- [x] `FLAG_SECURE` на экранах с seed
- [x] Quiz из 3 случайных слов (5 вариантов)
- [x] AES-256-GCM + высокий cost PBKDF2
- [x] Отдельный Panic PIN → полный wipe
- [x] Очистка чувствительных массивов после использования
- [x] PDF содержит предупреждения и fingerprint
- [x] Никаких логов seed / PIN

## Рекомендации при интеграции

### 1. Хранение
- Никогда не сохраняйте mnemonic в открытом виде
- Не используйте обычный SharedPreferences
- После unlock — держите seed только в памяти (ViewModel)
- При `onCleared()` / `onDestroy` очищайте

### 2. Сеть
- Seed **никогда** не должен уходить на сервер
- Даже в аналитику / crash-reporting

### 3. Бэкапы Android
- Исключите файл `secure_seed_prefs` из auto-backup:
  ```xml
  <full-backup-content>
      <exclude domain="sharedpref" path="secure_seed_prefs.xml"/>
  </full-backup-content>
  ```

### 4. Root / Magisk
- На rooted-устройствах предупреждайте пользователя
- Можно детектить через SafetyNet / Play Integrity (опционально)

### 5. Screenshot / Screen Recording
- `FLAG_SECURE` блокирует большинство случаев
- На эмуляторе и через ADB иногда можно обойти — это нормально
- Дополнительно можно слушать `UIScreen.capturedDidChange`-аналог (ограничено)

### 6. PDF / QR
- Генерируйте только по явному действию
- После экспорта покажите toast: «Храните только offline. Не отправляйте в мессенджеры»
- QR содержит полный mnemonic — это риск, если пользователь покажет экран

### 7. PIN
- Минимальная длина 6
- Запретите простые последовательности (000000, 123456, 111111…)
- После N неудачных попыток — временная блокировка (опционально)

### 8. Panic PIN
- Должен отличаться от обычного PIN
- При вводе — мгновенный wipe + restart
- Не показывайте подсказку «это panic»

## Аудит

Перед релизом проверьте:

1. `adb shell screencap` на экране seed → чёрный экран
2. Логи (`logcat`) не содержат seed
3. После wipe файл prefs отсутствует
4. Неправильный checksum → ошибка
5. Panic PIN действительно стирает данные


## Threat model (STRIDE-lite)

| Category | Threat | Mitigation in engine | Host still responsible |
|----------|--------|----------------------|------------------------|
| **S**poofing | Fake biometric callback | CryptoObject + Keystore gate | Use `FragmentActivity`; no custom biometric UI that skips CryptoObject |
| **T**ampering | Altered ciphertext / prefs | AEAD (Tink + AES-GCM); PIN KDF | Backup exclusions; no root debugging of process memory |
| **R**epudiation | User denies action | Optional `OnboardingHost.onAnalyticsEvent` (no secrets) | Persist audit logs off-device if legally required |
| **I**nformation disclosure | Screenshots, clipboard, logs | FLAG_SECURE, clipboard clear, CharArray, no seed logs | Never send seed to crash/analytics; train users offline |
| **D**enial of service | PIN brute force | `PinLockout` exponential delay | Rate-limit any network auth separately |
| **E**levation | Panic vs normal PIN | Distinct hashes; panic wipes storage | Clear in-memory keys on wipe; restart UI |

### Trust boundaries

1. **Entropy** — `SecureRandom` on device; host must not inject weak entropy.  
2. **PIN** — never leaves device; derived key unwraps seed.  
3. **Passphrase** — not stored; requested at unlock when flagged.  
4. **Export** — PDF/QR are user-driven advanced paths; treat as high risk.

### Out of scope

- Play Integrity / server attestation (host)  
- Physical coercion beyond panic PIN / decoy (future)  
- Supply-chain of host app signing  
