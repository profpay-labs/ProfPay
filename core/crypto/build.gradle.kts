plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.bouncycastle)
    implementation(libs.protobuf.java)
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
}
