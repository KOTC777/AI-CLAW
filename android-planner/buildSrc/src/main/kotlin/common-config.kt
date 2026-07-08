import com.android.build.gradle.LibraryExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile

/**
 * Common configuration applied to all Android library modules.
 */
fun LibraryExtension.commonAndroid() {
    compileSdk = 34
    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

/**
 * Common Kotlin compiler options.
 */
fun org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension.commonKotlinOptions() {
    jvmTarget = "17"
}

/**
 * Common test dependencies.
 */
val testDependencies = listOf(
    "junit5",
    "mockk",
    "coroutines-core",
    "turbine"
)

/**
 * Common Android test dependencies.
 */
val androidTestDependencies = listOf(
    "compose-ui-test",
    "robolectric"
)
