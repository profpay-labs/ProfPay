# ============================================
# TelegramWallet ProGuard Rules
# ============================================

# -------------------- Общие настройки --------------------
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keepattributes InnerClasses,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations

# Для удобства отладки crash-reports (можно убрать для максимальной обфускации)
-renamesourcefileattribute SourceFile

# -------------------- Kotlin --------------------
-dontwarn kotlin.**
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.flow.**inlined**

# -------------------- Kotlinx Serialization --------------------
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keep,includedescriptorclasses class com.profpay.**$$serializer { *; }
-keepclassmembers class com.profpay.** {
    *** Companion;
}
-keepclasseswithmembers class com.profpay.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep `@Serializable` classes
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# @Serializable and @Polymorphic are used at runtime
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

# Serializer classes
-keepclassmembers class * implements kotlinx.serialization.KSerializer {
    <init>(...);
}

# -------------------- Retrofit --------------------
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>

-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Retrofit with kotlinx.serialization converter
-keep class retrofit2.** { *; }
-keep class com.jakewharton.retrofit2.converter.kotlinx.serialization.** { *; }

# -------------------- OkHttp --------------------
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# -------------------- Hilt/Dagger --------------------
-dontwarn dagger.**
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

-keepclassmembers,allowobfuscation class * {
    @javax.inject.Inject <init>(...);
    @javax.inject.Inject <fields>;
    @dagger.* <fields>;
    @dagger.* <methods>;
}

-keep @dagger.hilt.* class * { *; }
-keep @dagger.Module class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep @dagger.hilt.android.* class * { *; }

# -------------------- Room --------------------
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# -------------------- Jetpack Compose --------------------
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep Compose Compiler generated code
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# -------------------- Android Security / Keystore --------------------
# KeystoreCryptoManager and security classes
-keep class com.profpay.wallet.security.** { *; }
-keep class com.profpay.core.security.** { *; }

# Android Keystore
-keep class android.security.** { *; }
-keepclassmembers class * extends java.security.KeyStore {
    *;
}

# -------------------- BouncyCastle (Crypto) --------------------
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# -------------------- ViewModel & SavedStateHandle --------------------
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class * extends androidx.lifecycle.ViewModel { *; }

-keepclassmembers class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
    <init>(android.app.Application, androidx.lifecycle.SavedStateHandle);
}

# -------------------- Navigation Compose --------------------
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# Navigation routes (sealed/data classes)
-keep class com.profpay.wallet.ui.navigation.Route { *; }
-keep class com.profpay.wallet.ui.navigation.Route$* { *; }

# -------------------- DTO и Domain модели --------------------
# Сохраняем все DTO классы (для сериализации)
-keep class com.profpay.data.**.dto.** { *; }
-keep class com.profpay.data.**.model.** { *; }
-keep class com.profpay.domain.**.model.** { *; }

# -------------------- API интерфейсы --------------------
-keep interface com.profpay.data.**.api.** { *; }
-keep interface com.profpay.core.network.** { *; }

# -------------------- Enum классы --------------------
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# -------------------- Native методы --------------------
-keepclasseswithmembernames class * {
    native <methods>;
}

# -------------------- R класс --------------------
-keepclassmembers class **.R$* {
    public static <fields>;
}

# -------------------- Parcelable --------------------
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# -------------------- WebView (если используется) --------------------
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String);
}

# -------------------- Reflection для BuildConfig --------------------
-keep class com.profpay.wallet.BuildConfig { *; }

# -------------------- Logging (удаление в release) --------------------
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# ... existing rules ...

# ══════════════════════════════════════════════════════════════════════
# Hilt + WorkManager
# ══════════════════════════════════════════════════════════════════════

# Keep Hilt-generated Worker factories
-keep class * extends androidx.hilt.work.HiltWorkerFactory { *; }
-keep class * extends androidx.work.WorkerFactory { *; }

# Keep all HiltWorker annotated classes
-keep @androidx.hilt.work.HiltWorker class * { *; }

# Keep Worker classes and their constructors
-keep class * extends androidx.work.CoroutineWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Keep generated Hilt worker assisted factories
-keepclasseswithmembers class * {
    @dagger.assisted.AssistedInject <init>(...);
}
-keep class **_AssistedFactory { *; }
-keep class **_HiltWorkerFactory { *; }

# ══════════════════════════════════════════════════════════════════════
# Dagger/Hilt (если ещё нет)
# ══════════════════════════════════════════════════════════════════════

-keepclassmembers,allowobfuscation class * {
    @javax.inject.Inject <init>(...);
    @dagger.assisted.AssistedInject <init>(...);
}

-keep,allowobfuscation,allowshrinking class dagger.hilt.** { *; }
-keep,allowobfuscation,allowshrinking class javax.inject.** { *; }
-keep,allowobfuscation,allowshrinking class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# -------------------- Оптимизации --------------------
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Убрать вывод предупреждений
-dontwarn java.lang.invoke.**
-dontwarn **$$Lambda$*
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault
