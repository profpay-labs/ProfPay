plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":domain:wallet"))

    implementation(libs.kotlinx.coroutines.core)

    // Для @Inject аннотации (легковесная зависимость, без Android)
    implementation(libs.javax.inject)
}
