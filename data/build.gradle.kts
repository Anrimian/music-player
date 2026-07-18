plugins {
    id("com.android.library")
    alias(libs.plugins.kapt)
    alias(libs.plugins.androidJunit5)
}

android {
    namespace = "com.github.anrimian.musicplayer.data"

    compileSdk = Constants.COMPILE_SDK

    defaultConfig {
        testInstrumentationRunner = Constants.TEST_INSTRUMENTATION_RUNNER
        testInstrumentationRunnerArguments["runnerBuilder"] = Constants.TEST_JUNIT5_BUILDER_ANDROID

        compileOptions {
            sourceCompatibility = Constants.JAVA_VERSION
            targetCompatibility = Constants.JAVA_VERSION
        }

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
        sourceSets {
            getByName("androidTest") {
                assets.directories.add("$projectDir/schemas")
            }
        }
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

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
    packaging {
        resources {
            excludes.add("META-INF/*")
        }
    }
}

dependencies {
    implementation(project(":shared:domain"))
    implementation(project(":shared:data"))

    implementation(project(":domain"))

    implementation(libs.kotlinStdLib)

    implementation(libs.appCompat)
    implementation(libs.documentFile)
    implementation(libs.rxJava)
    implementation(libs.coroutinesCore)
    modernImplementation(libs.exoPlayerCore)
    legacyImplementation(libsLegacy.exoPlayerCore)

    compileOnly(libs.javaxAnnotation)

    modernImplementation(libs.bundles.room)
    kaptModern(libs.roomCompiler)
    legacyImplementation(libsLegacy.room)
    legacyImplementation(libsLegacy.roomRx)
    kaptLegacy(libsLegacy.roomCompiler)
    kaptLegacy(libsLegacy.kotlinMetadataJvm)

    implementation(libs.libjAudioTagger)

    testImplementation(libs.bundles.unitTesting)
    testRuntimeOnly(libs.junit5Engine)

    androidTestImplementation(libs.androidXTestRunner)
    androidTestImplementation(libs.junit5)
    androidTestImplementation(libs.espresso)
    androidTestImplementation(libs.androidXTestRules)
    androidTestImplementation(libs.rxJavaExt)
    androidTestImplementation(libs.roomTest)
    androidTestImplementation(libs.dexmakerMockitoInline)
    androidTestImplementation(libs.mockitoKt)
}