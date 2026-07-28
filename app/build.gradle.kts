import dev.detekt.gradle.Detekt
import io.sentry.android.gradle.instrumentation.logcat.LogcatLevel
import java.util.Properties

plugins {
    id("com.android.application")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    kotlin("plugin.serialization") version "2.3.21"
    id("io.sentry.android.gradle") version "6.16.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10"
    id("dev.detekt") version "2.0.0-alpha.0"
    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
//    id("jacoco")
}

ktlint {
    version.set("1.3.1")
    debug.set(false)
    android.set(false)
    outputToConsole.set(true)
    ignoreFailures.set(true)

    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
}

tasks.withType<Detekt>().configureEach {
    reports {
        xml.required.set(true)
        html.required.set(true)
        sarif.required.set(false)
        md.required.set(false)
    }
}

detekt {
    toolVersion = "2.0.0-alpha.0"

    source.setFrom("src/main/java", "src/main/kotlin")

    buildUponDefaultConfig = true
    ignoreFailures = true
    allRules = false
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "17"
    autoCorrect = true
}

// jacoco {
//    toolVersion = "0.8.10"
// }

sentry {
    tracingInstrumentation {
        enabled.set(true)

        logcat {
            enabled.set(true)
            minLevel.set(LogcatLevel.ERROR)
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

val envFile = rootProject.file(".env")
if (envFile.exists()) {
    envFile.readLines().forEach { line ->
        if (line.isNotBlank() && !line.startsWith("#")) {
            val parts = line.split("=", limit = 2)
            if (parts.size == 2) {
                val key = parts[0].trim()
                val value = parts[1].trim()
                System.setProperty(key, value)
            }
        }
    }
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}

androidComponents {
    onVariants { variant ->
        val tag = "beta"
        val versionName = variant.outputs.firstOrNull()
            ?.versionName
            ?.get()
            ?: "unknown"

        variant.outputs.forEach { output ->
            output.outputFileName.set("profpay-$versionName-$tag.apk")
        }
    }
}

android {
    namespace = "com.profpay.wallet"
    compileSdk = 36

    signingConfigs {
        create("release") {
            storeFile = file(System.getProperty("KEYSTORE_FILE")
                ?: throw RuntimeException("❌ KEYSTORE_FILE is not set. Check your .env file."))
            storePassword = System.getProperty("KEYSTORE_PASSWORD")
                ?: throw RuntimeException("❌ KEYSTORE_PASSWORD is not set. Check your .env file.")
            keyAlias = System.getProperty("KEY_ALIAS")
                ?: throw RuntimeException("❌ KEY_ALIAS is not set. Check your .env file.")
            keyPassword = System.getProperty("KEY_PASSWORD")
                ?: throw RuntimeException("❌ KEY_PASSWORD is not set. Check your .env file.")
        }
    }

    defaultConfig {
        applicationId = "com.profpay.wallet"
        minSdk = 29
        targetSdk = 35
        versionCode = 20

//        MAJOR: Внесение изменений, ломающих обратную совместимость.
//        MINOR: Добавление новых функций без нарушения совместимости.
//        PATCH: Исправление ошибок и незначительные улучшения без изменения функциональности.
        versionName = "1.0.0" // MAJOR.MINOR.PATCH

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }

        buildConfigField("String", "BUGFENDER_API_KEY", "\"${project.findProperty("BUGFENDER_API_KEY") ?: ""}\"")
    }

    buildTypes {
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs.getByName("release")
            buildConfigField("Boolean", "IS_STAGING", "false")
            buildConfigField("String", "BUGFENDER_API_KEY", "\"${project.findProperty("BUGFENDER_API_KEY") ?: ""}\"")
        }
        debug {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"

            signingConfig = signingConfigs.getByName("debug")
            buildConfigField("Boolean", "IS_STAGING", "false")
            buildConfigField("String", "BUGFENDER_API_KEY", "\"${project.findProperty("BUGFENDER_API_KEY") ?: ""}\"")
        }
        create("staging") {
            initWith(getByName("release"))
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"

            signingConfig = signingConfigs.getByName("release")
            buildConfigField("Boolean", "IS_STAGING", "true")
            buildConfigField("String", "BUGFENDER_API_KEY", "\"${project.findProperty("BUGFENDER_API_KEY") ?: ""}\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.6"
    }
    packaging {
        resources {
            excludes += "META-INF/**"
        }
    }
    sourceSets {
        getByName("main") {
            java.srcDir("src/main/java")
            resources.srcDir("src/main/resources")
            val protoSrcDir = "src/main/proto"
            java.srcDirs(protoSrcDir)
            resources.srcDirs(protoSrcDir)
        }

        getByName("androidTest") {
            java.srcDir("src/androidTest/java")
            resources.srcDir("src/androidTest/resources")
        }
    }
    testOptions {
        unitTests.all {
            // Kotlin DSL, нужно использовать it.useJUnitPlatform()
            it.useJUnitPlatform()
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

project.configurations.configureEach {
    resolutionStrategy {
        force("androidx.emoji2:emoji2-views-helper:1.3.0")
        force("androidx.emoji2:emoji2:1.3.0")
    }
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.google.services)
        classpath(kotlin("serialization", version = "1.9.21"))
        classpath(libs.detekt.gradle.plugin)
    }
}

dependencies {
    // Core модули
    implementation(project(":core:network"))
    implementation(project(":core:security"))
    implementation(project(":core:crypto"))
    implementation(project(":core:tron"))
    implementation(project(":core:database"))
    implementation(project(":core:ui"))
    implementation(project(":core:common"))

    // Data модули
    implementation(project(":data:user"))
    implementation(project(":data:wallet"))
    implementation(project(":data:aml"))
    implementation(project(":data:contract"))
    implementation(project(":data:transfer"))
    implementation(project(":data:config"))
    implementation(project(":data:market"))

    // Domain модули
    implementation(project(":domain:user"))
    implementation(project(":domain:wallet"))
    implementation(project(":domain:aml"))
    implementation(project(":domain:contract"))
    implementation(project(":domain:transfer"))
    implementation(project(":domain:config"))
    implementation(project(":domain:security"))
    implementation(project(":domain:market"))

    // -------------------------------------------------
    // UI (Compose, Material, Navigation, System UI)
    // -------------------------------------------------
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.graphics.shapes)
    implementation(libs.androidx.work.runtime)
    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.core.ktx)
    implementation(libs.showcase.layout.compose)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.compose.stacked.snackbar)

    // -------------------------------------------------
    // Dependency Injection (Hilt)
    // -------------------------------------------------
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // -------------------------------------------------
    // Lifecycle / State management
    // -------------------------------------------------
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.compose.runtime.livedata)
    implementation(libs.androidx.lifecycle.process)

    // -------------------------------------------------
    // Data layer (DB, Coroutines, Serialization, Network)
    // -------------------------------------------------
    // Room
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)
    implementation(libs.room.paging)

    // Coroutines + Network
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.okhttp)

    // Protobuf для ByteString
    implementation(libs.protobuf.java)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // -------------------------------------------------
    // Security / Storage
    // -------------------------------------------------
    implementation(libs.jbcrypt)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.datastore.preferences)
    implementation(libs.androidx.security.crypto)

    // AndroidX Hilt (для WorkManager)
    implementation(libs.androidx.hilt.common)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // -------------------------------------------------
    // External SDKs / Features (PDF, QR, Notifications)
    // -------------------------------------------------
    implementation(libs.itext.core)
    implementation(libs.pusher.java.client)
    implementation(libs.google.zxing.core)
    implementation(libs.pushy.sdk)

    // -------------------------------------------------
    // Monitoring / Logging
    // -------------------------------------------------
    implementation(libs.sentry.android)
    implementation(libs.sentry.sentry.compose.android)
    implementation(libs.slf4j.simple)
    implementation(libs.javax.annotation.api)

    // -------------------------------------------------
    // Scheduling
    // -------------------------------------------------
    implementation(libs.krontab)

    implementation(libs.bugfender)
    // -------------------------------------------------
    // Testing
    // -------------------------------------------------
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.turbine)
    testImplementation(libs.androidx.room.testing)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlin.test)
    androidTestImplementation(libs.mockito.android)

    // -------------------------------------------------
    // Debug
    // -------------------------------------------------
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // -------------------------------------------------
    // Annotation Processor
    // -------------------------------------------------
    annotationProcessor(libs.room.compiler)
}

