plugins {
    alias(libs.plugins.android.library)
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization") version "2.3.21"
}

android {
    namespace = "com.profpay.core.network"
    compileSdk = 36

    defaultConfig {
        minSdk = 29

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

//        buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8085/api/v1/\"")
        buildConfigField("String", "BASE_URL", "\"https://api.profpay-services.com/api/v1/\"")
        buildConfigField("Boolean", "DEBUG_LOGGING", "true")
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("Boolean", "DEBUG_LOGGING", "false")
            buildConfigField("String", "BUGFENDER_API_KEY", "\"${project.findProperty("BUGFENDER_API_KEY") ?: ""}\"")
        }
        debug {
            buildConfigField("Boolean", "DEBUG_LOGGING", "true")
        }
    }
    buildFeatures {
        buildConfig = true
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
    implementation(libs.core.ktx)
    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Annotations
    implementation(libs.javax.annotation.api)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}
