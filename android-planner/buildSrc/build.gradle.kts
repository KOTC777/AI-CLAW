plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
}

// Access version catalog from buildSrc
val libs = versionCatalogs.named("libs")
