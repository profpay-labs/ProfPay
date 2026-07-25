# ProfPay Wallet

Secure cryptocurrency wallet for TRON network supporting USDT (TRC20) and TRX.

## Language

- [English](README.md)
- [Русский](README.ru.md)

## Disclaimer

This project is currently in active development. While it is open for public testing, many features and components are still being worked on, and updates will be made regularly. We encourage contributions and responsible bug reporting to help improve the project.

## License

This code is proprietary and not intended for public use. It is shared only for auditing and contribution purposes. Please see [LICENSE](./LICENSE) for full terms.

## About

ProfPay is a cryptocurrency wallet designed for secure transactions using USDT (TRC20) and TRX on the TRON blockchain network. The wallet is equipped with a smart contract system for handling USDT TRC20 transactions, providing advanced security features to protect users' funds.

## Features

- Multi-Address System with main address and 6 additional slots for fund protection
- AML Protection where slots act as barriers against dirty currency
- Multi-Currency support for USDT (TRC20) and TRX
- Biometric Authentication with fingerprint and face unlock
- PIN Lock for secure app access
- Push Notifications for real-time transaction alerts
- Dark and Light Theme with system-aware switching
- PDF Export for transaction history

## Tech Stack

- Kotlin 2.3.x
- Android SDK Target 35, Compile 36
- Java 17, Gradle 8.11.1
- Jetpack Compose, Navigation Compose, ViewModel, Room, DataStore, WorkManager
- Hilt 2.59.2, KSP 2.3.6
- Kotlin Coroutines, Flow, StateFlow
- OkHttp, Kotlinx Serialization, Protobuf
- Sentry, Bugfender, SonarQube
- Pushy, Detekt, KtLint

## Architecture

The project follows Clean Architecture with multi-module structure.

## Project Structure

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

### Layers

- Presentation: UI with Compose, ViewModels, Navigation
- Domain: Use Cases, Business Logic, Domain Models
- Data: Repositories, Data Sources, DTOs, Mappers
- Core: Shared utilities, SDK wrappers, Base classes

## Requirements

- Android 10 or higher (API 29+)
- Approximately 50 MB storage
- Internet connection
- Optional: Biometric hardware for fingerprint or face unlock

## Build and Installation

### Prerequisites

- Docker installed and running
- Git

### Quick Build

Clone the repository:

    git clone <repository-url>
    cd ProfPayWallet

Make the build script executable and run it:

    chmod +x docker/build.sh
    ./docker/build.sh

The APK will be available at ./build-output/app-release.apk

### Manual Build Steps

#### Step 1: Generate Keystore

If you do not have a keystore file, generate one:

    keytool -genkeypair -v \
      -keystore keystore/release.jks \
      -keyalg RSA \
      -keysize 2048 \
      -validity 10000 \
      -alias profpay

#### Step 2: Create Configuration File

Create a .env file in the project root:

    KEYSTORE_FILE=keystore/release.jks
    KEYSTORE_PASSWORD=your_keystore_password
    KEY_ALIAS=profpay
    KEY_PASSWORD=your_key_password

#### Step 3: Build via Docker

Build the Docker image:

    docker build -f docker/Dockerfile.release -t profpay-builder .

Extract the APK:

    docker run --rm -v $(pwd)/build-output:/out profpay-builder cp /app/app-release.apk /out/

#### Step 4: Verify

    ls -la build-output/app-release.apk

### Local Development

Requirements:
- Android Studio Ladybug or newer
- JDK 17
- Android SDK API 35+

Debug build:

    ./gradlew assembleDebug

Release build (requires .env):

    ./gradlew assembleRelease

## Configuration

### Environment Variables

Required:
- KEYSTORE_FILE: Path to keystore file
- KEYSTORE_PASSWORD: Keystore password
- KEY_ALIAS: Key alias in keystore
- KEY_PASSWORD: Key password

Optional:
- BUGFENDER_API_KEY: Bugfender logging key

### Build Types

- debug: Debuggable, not minified, debug signing
- staging: Debuggable, not minified, release signing
- release: Not debuggable, minified, release signing

## Testing

Unit tests:

    ./gradlew test

Instrumented tests:

    ./gradlew connectedAndroidTest

Code quality:

    ./gradlew detekt ktlintCheck

Format code:

    ./gradlew ktlintFormat

## Security

### Implemented Measures

- PIN code protection with biometric unlock
- Private keys encrypted via Android Keystore
- Automatic app lock on background
- No sensitive data logging in production
- Secure network communication
- ProGuard/R8 code obfuscation
- Backup disabled (allowBackup=false)

### Notes

- Private keys never leave the device
- Sensitive data cleared when app goes to background
- Encrypted local storage for credentials

## Docker

The project includes Docker configuration for reproducible builds.

### Files

- docker/Dockerfile.release: Multi-stage build for release APK
- docker/build.sh: Automated build script
- docker/.env.example: Environment variables template

### Build Process

The Dockerfile uses a two-stage build:
1. Builder stage with Gradle and Android SDK
2. Runtime stage for artifact extraction

## Troubleshooting

### Build fails with keystore error

Ensure .env file exists and contains correct paths and passwords.

### Docker build runs out of memory

Increase Docker memory limit to at least 8GB.

### Gradle daemon issues

Add --no-daemon flag or restart Docker.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run tests and code quality checks
5. Submit a pull request

### Code Style

- Follow Kotlin coding conventions
- Run ktlintFormat before committing
- Ensure all tests pass
- Add tests for new features
