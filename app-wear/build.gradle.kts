plugins {
    id("com.android.library")
    alias(libs.plugins.kapt)
    alias(libs.plugins.androidJunit5)
}

android {
    namespace = "com.github.anrimian.musicplayer.wear"

    compileSdk = Constants.WEAR_COMPILE_SDK

    defaultConfig {
        minSdk = Constants.WEAR_MIN_SDK

        testInstrumentationRunner = Constants.TEST_INSTRUMENTATION_RUNNER
        testInstrumentationRunnerArguments["runnerBuilder"] = Constants.TEST_JUNIT5_BUILDER_ANDROID

        missingDimensionStrategy("apiVersion", "modern")
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        create("QA") {
            initWith(getByName("release"))
        }
        getByName("debug") {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = Constants.JAVA_VERSION
        targetCompatibility = Constants.JAVA_VERSION
    }

    packaging {
        jniLibs.excludes.add("**/kotlin/**")
        resources.excludes.addAll(
            listOf(
                "/META-INF/*.kotlin_module",
                "**/kotlin/**",
                "**/*.txt",
                "**/*.xml",
                "/*.properties",
                "DebugProbesKt.bin"
            )
        )
    }

    testOptions {
        targetSdk = Constants.TARGET_SDK
    }

    lint {
        abortOnError = false
        targetSdk = Constants.TARGET_SDK
    }
}

dependencies {
    implementation(project(":shared:wear"))
    implementation(project(":shared:app"))
    implementation(project(":shared:domain"))
    implementation(project(":shared:data"))

    implementation(libs.appCompat)
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.percentlayout:percentlayout:1.0.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.2.1")
    implementation(libs.constraintLayout)

    implementation(libs.gmsWearable)
    implementation("androidx.wear:wear:1.2.0")
    implementation("androidx.wear:wear-remote-interactions:1.0.0")
    implementation("androidx.wear.tiles:tiles:1.3.0-alpha04")
    implementation("androidx.wear:wear-complications-provider:1.0.0-alpha17")
    implementation("androidx.wear:wear-complications-data-source-ktx:1.0.0-alpha22")

    implementation(libs.rxJava)
    implementation(libs.rxAndroid)

    implementation(libs.bundles.moxy)
    kapt(libs.moxyCompiler)

    implementation(libs.dagger)
    kapt(libs.daggerCompiler)

    testImplementation(libs.bundles.unitTesting)
    testRuntimeOnly(libs.junit5Engine)
}
