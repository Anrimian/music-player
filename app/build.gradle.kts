plugins {
    id("com.android.library")
    id("kotlin-parcelize")
    alias(libs.plugins.kapt)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.androidJunit5)
}

android {
    namespace = "com.github.anrimian.musicplayer"

    compileSdk = Constants.COMPILE_SDK

    defaultConfig {
        testInstrumentationRunner = Constants.TEST_INSTRUMENTATION_RUNNER
        testInstrumentationRunnerArguments["runnerBuilder"] = Constants.TEST_JUNIT5_BUILDER_ANDROID

        resConfigs("en", "be", "cs", "de", "el", "es", "fr", "hu", "in", "pt", "ru", "tr", "uk", "ko", "zh", "zh-rTW")
    }

    flavorDimensions.add("feature")
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
        create("nowear") {
            dimension = "feature"
            isDefault = true
        }
        create("wear") {
            dimension = "feature"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            consumerProguardFiles("proguard-rules.pro")
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
        compose = true
    }

    compileOptions {
        sourceCompatibility = Constants.JAVA_VERSION
        targetCompatibility = Constants.JAVA_VERSION
    }

    kotlin {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-opt-in=androidx.compose.foundation.ExperimentalFoundationApi",
                "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
                "-opt-in=com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi"
            )
        }
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

composeCompiler {
    stabilityConfigurationFiles.add(layout.projectDirectory.file("compose_compiler_config.conf"))

    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    metricsDestination = layout.buildDirectory.dir("compose_compiler")
}

dependencies {
    wearImplementation(project(":shared:wear"))
    implementation(project(":shared:app"))
    implementation(project(":shared:domain"))
    implementation(project(":shared:data"))

    implementation(project(":domain"))
    implementation(project(":data"))

    implementation(project(":libs:fsync:core"))

    implementation(libs.kotlinStdLib)

    implementation(libs.appCompat)
    implementation(libs.appCompatResources)
    implementation(libs.material)
    implementation(libs.recyclerView)
    implementation(libs.constraintLayout)
    implementation(libs.supportMedia)

    wearImplementation(libs.gmsWearable)

    modernImplementation(platform(libs.composeBom))
    legacyImplementation(platform(libsLegacy.composeBomLegacy))
    implementation(libs.bundles.compose)
    modernImplementation(libs.activityCompose)
    legacyImplementation(libsLegacy.activityComposeLegacy)
    modernImplementation(libs.viewModelCompose)
    legacyImplementation(libsLegacy.viewModelCompose)
    debugImplementation(libs.composeTooling)

    implementation(libs.rxAndroid)
    implementation(libs.coroutinesCore)
    implementation(libs.coroutinesRx)
    implementation(libs.coroutinesAndroid)

    implementation(libs.bundles.moxy)
    kapt(libs.moxyCompiler)

    implementation(libs.dagger)
    kapt(libs.daggerCompiler)

    modernImplementation(libs.room)
    kaptModern(libs.roomCompiler)
    legacyImplementation(libsLegacy.room)
    kaptLegacy(libsLegacy.roomCompiler)

    modernImplementation(libs.glide)
    kaptModern(libs.glideCompiler)
    modernImplementation(libs.glideCompose)
    legacyImplementation(libsLegacy.glide)
    kaptLegacy(libsLegacy.glideCompiler)
    legacyImplementation(libsLegacy.glideCompose)

    implementation(libs.slidr)

    implementation(libs.chipsLayoutManager)

    implementation(libs.rotateLayout)

    implementation(libs.reorderable)

    compileOnly(libs.javaxAnnotation)

    testImplementation(libs.bundles.unitTesting)
    testRuntimeOnly(libs.junit5Engine)

    androidTestImplementation(libs.androidXTestRunner)
    androidTestImplementation(libs.espresso)
}
