# Data module consumer rules

# DTO classes for kotlinx.serialization
-keep class com.profpay.data.**.dto.** { *; }

# API interfaces
-keep interface com.profpay.data.**.api.** { *; }

# Repository implementations (для Hilt)
-keep class com.profpay.data.**.repository.** { *; }
-keep class com.profpay.data.**.di.** { *; }

