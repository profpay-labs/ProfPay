plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.android.built.in1.kotlin)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.profpay.data.wallet"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 26
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
    // Modules
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:security"))
    implementation(project(":core:tron"))
    implementation(project(":core:crypto"))

    implementation(project(":domain:wallet"))
    implementation(project(":domain:security"))

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Retrofit
    implementation(libs.retrofit)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // DataStore
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.datastore.preferences)
}
