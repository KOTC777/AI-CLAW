plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.toolbox.core.security"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(project(":core:core-common"))
    implementation(project(":core:core-datastore"))

    // BouncyCastle (Argon2id)
    implementation(libs.bouncycastle)

    // Coroutines
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
}
