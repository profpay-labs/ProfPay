# Security module consumer rules
-keep class com.profpay.core.security.** { *; }

# Android Keystore related
-keep class * extends java.security.KeyStore { *; }
-keepclassmembers class * {
    @javax.crypto.* <methods>;
}

