plugins {
    id("com.android.library")
    id("kotlin-parcelize")
    alias(libs.plugins.kapt)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.androidJunit5)
}

android {
    namespace = "com.github.anrimian.musicplayer.shared.app"

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

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":shared:domain"))
    implementation(project(":shared:data"))

    implementation(libs.androidxAnnotation)
    implementation(libs.appCompatResources)
    implementation(libs.recyclerView)
    implementation(libs.fragmentKtx)

    modernImplementation(platform(libs.composeBom))
    legacyImplementation(platform(libsLegacy.composeBomLegacy))
    modernImplementation(libs.viewModelCompose)
    legacyImplementation(libsLegacy.viewModelCompose)
    implementation(libs.bundles.compose)
    debugImplementation(libs.composeTooling)

    implementation(libs.rxJava)
    implementation(libs.rxAndroid)
    implementation(libs.coroutinesCore)
    implementation(libs.coroutinesRx)
    implementation(libs.coroutinesAndroid)

    implementation(libs.bundles.moxy)
    modernImplementation(libs.viewModel)
    legacyImplementation(libsLegacy.viewModel)


    implementation(libs.dagger)
    kapt(libs.daggerCompiler)

    implementation(libs.reorderable)

    testImplementation(libs.bundles.unitTesting)
    testRuntimeOnly(libs.junit5Engine)
}
