# ═══════════════════════════════════════════════════════════════════════════
# Hilt / Dagger
# ═══════════════════════════════════════════════════════════════════════════
-keep,allowobfuscation,allowshrinking class dagger.hilt.android.internal.** { *; }
-keep,allowobfuscation,allowshrinking class dagger.hilt.internal.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }
-keep class javax.inject.** { *; }

# ═══════════════════════════════════════════════════════════════════════════
# Room (core:database)
# ═══════════════════════════════════════════════════════════════════════════
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep class com.profpay.core.database.** { *; }
-keep class com.profpay.core.database.dao.** { *; }
-keep class com.profpay.core.database.entities.** { *; }
-keep class com.profpay.core.database.models.** { *; }
-keep class com.profpay.core.database.converters.** { *; }

# ═══════════════════════════════════════════════════════════════════════════
# Kotlin Serialization
# ═══════════════════════════════════════════════════════════════════════════
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.profpay.**$$serializer { *; }

# ═══════════════════════════════════════════════════════════════════════════
# Compose Navigation (Route classes)
# ═══════════════════════════════════════════════════════════════════════════
-keep class com.profpay.wallet.ui.navigation.Route { *; }
-keep class com.profpay.wallet.ui.navigation.Route$* { *; }

# ═══════════════════════════════════════════════════════════════════════════
# Domain layer (all modules)
# ═══════════════════════════════════════════════════════════════════════════
-keep class com.profpay.domain.** { *; }

# ═══════════════════════════════════════════════════════════════════════════
# Data layer (all modules)
# ═══════════════════════════════════════════════════════════════════════════
-keep class com.profpay.data.**.dto.** { *; }
-keep class com.profpay.data.**.api.** { *; }
-keep class com.profpay.data.**.di.** { *; }

# ═══════════════════════════════════════════════════════════════════════════
# Core modules
# ═══════════════════════════════════════════════════════════════════════════
-keep class com.profpay.core.network.** { *; }
-keep class com.profpay.core.security.** { *; }
-keep class com.profpay.core.crypto.** { *; }
-keep class com.profpay.core.tron.** { *; }
-keep class com.profpay.core.common.** { *; }

# ═══════════════════════════════════════════════════════════════════════════
# ViewModels
# ═══════════════════════════════════════════════════════════════════════════
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.AndroidViewModel { *; }

# ═══════════════════════════════════════════════════════════════════════════
# Pushy SDK
# ═══════════════════════════════════════════════════════════════════════════
-keep class me.pushy.** { *; }
-dontwarn me.pushy.**

# ═══════════════════════════════════════════════════════════════════════════
# Sentry
# ═══════════════════════════════════════════════════════════════════════════
-keep class io.sentry.** { *; }
-dontwarn io.sentry.**

# ═══════════════════════════════════════════════════════════════════════════
# OkHttp / Retrofit
# ═══════════════════════════════════════════════════════════════════════════
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class retrofit2.** { *; }
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# ═══════════════════════════════════════════════════════════════════════════
# Coroutines
# ═══════════════════════════════════════════════════════════════════════════
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ═══════════════════════════════════════════════════════════════════════════
# Keep Enums (TransactionType, TransactionStatusCode, etc.)
# ═══════════════════════════════════════════════════════════════════════════
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ═══════════════════════════════════════════════════════════════════════════
# App module specific
# ═══════════════════════════════════════════════════════════════════════════
-keep class com.profpay.wallet.App { *; }
-keep class com.profpay.wallet.MainActivity { *; }
-keep class com.profpay.wallet.bridge.viewmodel.** { *; }
-keep class com.profpay.wallet.data.** { *; }

# ═══════════════════════════════════════════════════════════════════════════
# Bugfender
# ═══════════════════════════════════════════════════════════════════════════
-keep class com.bugfender.** { *; }
-dontwarn com.bugfender.**

# ═══════════════════════════════════════════════════════════════════════════
# Vert.x (transitive dependency from Pusher)
# ═══════════════════════════════════════════════════════════════════════════
-dontwarn io.vertx.**
-dontwarn reactor.blockhound.**

# ═══════════════════════════════════════════════════════════════════════════
# BlockHound (reactor debugging tool, not needed in production)
# ═══════════════════════════════════════════════════════════════════════════
-dontwarn reactor.blockhound.integration.BlockHoundIntegration

# ═══════════════════════════════════════════════════════════════════════════
# Bouncy Castle (required for TRON/trident crypto operations)
# ═══════════════════════════════════════════════════════════════════════════
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**
-keep class org.spongycastle.** { *; }
-dontwarn org.spongycastle.**

# Keep the provider registration
-keepclassmembers class org.bouncycastle.jce.provider.BouncyCastleProvider {
    public <init>();
}

# ═══════════════════════════════════════════════════════════════════════════
# TRON Trident SDK
# ═══════════════════════════════════════════════════════════════════════════
-keep class org.tron.trident.** { *; }
-keep class org.tron.trident.crypto.** { *; }
-keep class org.tron.trident.core.** { *; }
-dontwarn org.tron.trident.**

# Keep Security Providers
-keep class java.security.** { *; }
-keep class javax.crypto.** { *; }
-keepclassmembers class * extends java.security.Provider {
    <init>(...);
}
