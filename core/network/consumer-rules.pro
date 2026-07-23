# Network module consumer rules
# Эти правила автоматически применяются к app модулю

# Retrofit API interfaces
-keep interface com.profpay.core.network.** { *; }
-keep class com.profpay.core.network.client.** { *; }
-keep class com.profpay.core.network.error.** { *; }
-keep class com.profpay.core.network.di.** { *; }

