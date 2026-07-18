plugins {
    id("com.android.library")
}

android {
    namespace = "com.github.anrimian.musicplayer.shared.app.lite"

    compileSdk = Constants.COMPILE_SDK

    defaultConfig {
        minSdk = Constants.MIN_SDK
    }

    compileOptions {
        sourceCompatibility = Constants.JAVA_VERSION
        targetCompatibility = Constants.JAVA_VERSION
    }

    flavorDimensions.add("apiVersion")
    productFlavors {
        create("modern") {
            dimension = "apiVersion"
            minSdk = Constants.MODERN_SDK_THRESHOLD
            isDefault = true
        }
        create("legacy") {
            dimension = "apiVersion"
            minSdk = Constants.MIN_SDK
        }
    }

    buildTypes {
        create("QA") {
            initWith(getByName("release"))
        }
    }
}

dependencies {
    implementation(project(":shared:app"))
    implementation(project(":shared:domain"))
    implementation(project(":shared:data"))
}