# Consumer rules for core:tron module

# ═══════════════════════════════════════════════════════════════════
# Trident SDK (TRON Java SDK)
# ═══════════════════════════════════════════════════════════════════

# Keep all Trident classes - критично для работы с блокчейном
-keep class org.tron.trident.** { *; }
-keepclassmembers class org.tron.trident.** { *; }
-dontwarn org.tron.trident.**

# ABI types для смарт-контрактов (Function, TypeReference, etc.)
-keep class org.tron.trident.abi.** { *; }
-keepclassmembers class org.tron.trident.abi.** { *; }

# TypeReference используется через рефлексию - обязательно сохраняем
-keep class org.tron.trident.abi.TypeReference { *; }
-keep class * extends org.tron.trident.abi.TypeReference { *; }
-keepclassmembers class * extends org.tron.trident.abi.TypeReference {
    <init>(...);
}

# ABI datatypes
-keep class org.tron.trident.abi.datatypes.** { *; }
-keepclassmembers class org.tron.trident.abi.datatypes.** { *; }

# Protobuf messages для TRON
-keep class org.tron.trident.proto.** { *; }
-keepclassmembers class org.tron.trident.proto.** { *; }

# Core API
-keep class org.tron.trident.core.** { *; }
-keepclassmembers class org.tron.trident.core.** { *; }

# ═══════════════════════════════════════════════════════════════════
# gRPC
# ═══════════════════════════════════════════════════════════════════
-keep class io.grpc.** { *; }
-keepclassmembers class io.grpc.** { *; }
-dontwarn io.grpc.**

# gRPC generated stubs
-keep class **.grpc.*Grpc { *; }
-keep class **.grpc.*Grpc$* { *; }

# ═══════════════════════════════════════════════════════════════════
# Protobuf
# ═══════════════════════════════════════════════════════════════════
-keep class com.google.protobuf.** { *; }
-keepclassmembers class com.google.protobuf.** { *; }
-dontwarn com.google.protobuf.**

# Keep protobuf generated classes
-keep class * extends com.google.protobuf.GeneratedMessageV3 { *; }
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }

# ═══════════════════════════════════════════════════════════════════
# BitcoinJ (используется для криптографии)
# ═══════════════════════════════════════════════════════════════════
-keep class org.bitcoinj.** { *; }
-dontwarn org.bitcoinj.**

# ═══════════════════════════════════════════════════════════════════
# Netty (используется gRPC)
# ═══════════════════════════════════════════════════════════════════
-dontwarn io.netty.**
-keep class io.netty.** { *; }

# ═══════════════════════════════════════════════════════════════════
# Проектные классы
# ═══════════════════════════════════════════════════════════════════
-keep interface com.profpay.core.tron.api.** { *; }
-keep class com.profpay.core.tron.model.** { *; }
-keep class com.profpay.core.tron.impl.** { *; }
-keep class com.profpay.core.tron.network.** { *; }

# ═══════════════════════════════════════════════════════════════════
# Reflection workarounds
# ═══════════════════════════════════════════════════════════════════

# Keep generic signatures for TypeReference to work
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# Anonymous classes extending TypeReference
-keepclassmembers class * {
    ** $*;
}
