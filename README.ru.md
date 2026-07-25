# ProfPay Wallet

Безопасный криптовалютный кошелёк для сети TRON с поддержкой USDT (TRC20) и TRX.

## Язык

- [English](README.md)
- [Русский](README.ru.md)

## Дисклеймер

Проект находится в активной разработке. Хотя он открыт для публичного тестирования, многие функции ещё дорабатываются. Мы приветствуем вклад в проект и ответственные сообщения об ошибках.

## Лицензия

Код является проприетарным и не предназначен для публичного использования. Он предоставляется только для аудита и участия в разработке. Подробности в файле [LICENSE](./LICENSE).

## О проекте

ProfPay — криптовалютный кошелёк для безопасных транзакций с использованием USDT (TRC20) и TRX в сети TRON. Кошелёк оснащён системой смарт-контрактов для обработки транзакций USDT TRC20 с продвинутыми функциями безопасности.

## Возможности

- Мульти-адресная система с основным адресом и 6 дополнительными слотами для защиты средств
- AML-защита — слоты служат барьером против грязной валюты
- Поддержка USDT (TRC20) и TRX
- Биометрическая аутентификация (отпечаток пальца, Face ID)
- PIN-код для защиты доступа
- Push-уведомления о транзакциях в реальном времени
- Тёмная и светлая тема с автоопределением системной темы
- Экспорт истории транзакций в PDF

## Технологии

- Kotlin 2.3.x
- Android SDK Target 35, Compile 36
- Java 17, Gradle 8.11.1
- Jetpack Compose, Navigation Compose, ViewModel, Room, DataStore, WorkManager
- Hilt 2.59.2, KSP 2.3.6
- Kotlin Coroutines, Flow, StateFlow
- OkHttp, Kotlinx Serialization, Protobuf
- Sentry, Bugfender, SonarQube
- Pushy, Detekt, KtLint

## Архитектура

Проект построен на Clean Architecture с многомодульной структурой.

## Структура проекта
```
ProfPayWallet/
├── app/
│   ├── ui/
│   ├── navigation/
│   └── di/
├── core/
│   ├── common/
│   ├── crypto/
│   ├── database/
│   ├── network/
│   ├── security/
│   ├── tron/
│   └── ui/
├── data/
│   ├── aml/
│   ├── config/
│   ├── contract/
│   ├── market/
│   ├── transfer/
│   ├── user/
│   └── wallet/
├── domain/
│   ├── aml/
│   ├── config/
│   ├── contract/
│   ├── market/
│   ├── security/
│   ├── transfer/
│   ├── user/
│   └── wallet/
├── feature/
│   └── home/
├── docker/
├── keystore/
├── walletcore/
├── build.gradle.kts
├── settings.gradle.kts
└── LICENSE
```

## Требования

- Android 10+ (API 29+)
- ~50 МБ памяти
- Интернет-соединение

## Сборка и установка

### Предварительные требования

- Установленный Docker
- Git
---
### Быстрая сборка

Клонируй и запусти:
```bash
git clone <repository-url>
cd ProfPayWallet
chmod +x docker/build.sh
./docker/build.sh
```
---
APK будет в `./build-output/app-release.apk`

### Генерация Keystore
```bash
keytool -genkeypair -v -keystore keystore/release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias profpay
```
---
### Создание .env файла
```
KEYSTORE_FILE=keystore/release.jks
KEYSTORE_PASSWORD=ваш_пароль
KEY_ALIAS=profpay
KEY_PASSWORD=ваш_пароль
```
---
### Сборка через Docker

```bash
docker build -f docker/Dockerfile.release -t profpay-builder .
docker run --rm -v $(pwd)/build-output:/out profpay-builder cp /app/app-release.apk /out/
```

---

### Локальная разработка
```bash
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew test
./gradlew detekt ktlintCheck
```

---
## Безопасность

- PIN-код с биометрической разблокировкой
- Приватные ключи шифруются через Android Keystore
- Автоблокировка при сворачивании приложения
- Отсутствие логирования чувствительных данных
- Обфускация кода через ProGuard/R8
- Резервное копирование отключено

## Участие в разработке

1. Сделай форк репозитория
2. Создай feature-ветку
3. Запусти тесты и ktlintFormat
4. Создай pull request
