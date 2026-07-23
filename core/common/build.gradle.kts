plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.profpay.core.common"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Protobuf для ByteString (используется в ByteStringExt)
    implementation(libs.protobuf.java)

    implementation(libs.sentry.android)
    // Для @Inject аннотации (легковесная зависимость, без Android)
    implementation(libs.javax.inject)
}
