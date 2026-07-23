// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "9.2.0" apply false
    id("com.google.dagger.hilt.android") version "2.59.2" apply false
    id("com.google.devtools.ksp") version "2.3.6" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.21" apply false
    id("com.google.protobuf") version "0.9.5" apply false
    id("org.jetbrains.kotlin.jvm") version "2.3.21" apply false
    id("org.sonarqube") version "7.2.3.7755" apply true
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.built.in1.kotlin) apply false
}
