# Crypto module consumer rules
-keep class com.profpay.core.crypto.** { *; }

# BouncyCastle
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**
