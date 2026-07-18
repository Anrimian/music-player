plugins {
    id("com.android.library")
    alias(libs.plugins.kapt)
    alias(libs.plugins.androidJunit5)
}

android {
    namespace = "com.github.anrimian.musicplayer.shared.wear"

    compileSdk = Constants.COMPILE_SDK

    defaultConfig {
        minSdk = Constants.MIN_SDK

        compileOptions {
            sourceCompatibility = Constants.JAVA_VERSION
            targetCompatibility = Constants.JAVA_VERSION
        }

        missingDimensionStrategy("apiVersion", "modern")
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


    implementation(libs.gmsWearable)

    implementation(libs.rxJava)
    implementation(libs.rxAndroid)
    implementation(libs.coroutinesCore)
    implementation(libs.coroutinesRx)
    implementation(libs.coroutinesAndroid)


    testImplementation(libs.bundles.unitTesting)
    testRuntimeOnly(libs.junit5Engine)
}