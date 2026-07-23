plugins {
    alias(libs.plugins.android.library)
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization") version "2.3.21"
}

android {
    namespace = "com.profpay.data.market"
    compileSdk = 36

    defaultConfig {
        minSdk = 29
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Domain
    implementation(project(":domain:market"))

    implementation(project(":core:database"))
    // Core Network (предоставляет RetrofitFactory, safeApiCall и т.д.)
    implementation(project(":core:network"))

    // Network (нужен для Response, @GET, @Query и др. аннотаций)
    implementation(libs.retrofit)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
}
