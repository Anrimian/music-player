plugins {
    id("com.android.library")
    alias(libs.plugins.kapt)
}

android {
    namespace = "com.github.anrimian.musicplayer.shared.data"

    compileSdk = Constants.COMPILE_SDK

    defaultConfig {
        minSdk = Constants.MIN_SDK
    }

    compileOptions {
        sourceCompatibility = Constants.JAVA_VERSION
        targetCompatibility = Constants.JAVA_VERSION
    }

    buildTypes {
        create("QA") {
            initWith(getByName("release"))
        }
    }
}

dependencies {
    implementation(project(":shared:domain"))

    implementation(libs.appCompat)
}