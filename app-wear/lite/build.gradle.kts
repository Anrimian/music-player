plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kapt)
}

android {

    signingConfigs {
        create("test") {
            keyAlias = "123456"
            keyPassword = "123456"
            storeFile = file("${project.rootDir}/test.jks")
            storePassword = "123456"
        }
    }

    namespace = "com.github.anrimian.musicplayer.wear.lite"

    compileSdk = Constants.WEAR_COMPILE_SDK

    defaultConfig {
        minSdk = Constants.WEAR_MIN_SDK
        targetSdk = Constants.WEAR_TARGET_SDK

        applicationId = Constants.LITE_PACKAGE_NAME
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = Constants.TEST_INSTRUMENTATION_RUNNER
        testInstrumentationRunnerArguments["runnerBuilder"] = Constants.TEST_JUNIT5_BUILDER_ANDROID
        testApplicationId = "$applicationId.test"

        base.archivesName.set("$applicationId-v$versionName-b$versionCode")

        compileOptions {
            sourceCompatibility = Constants.JAVA_VERSION
            targetCompatibility = Constants.JAVA_VERSION
        }

        missingDimensionStrategy("apiVersion", "modern")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        create("QA") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("test")
            applicationIdSuffix = Constants.QA_PACKAGE_SUFFIX
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = Constants.DEBUG_PACKAGE_SUFFIX
        }
    }
    buildFeatures {
        viewBinding = true
    }
    packaging {
        jniLibs {
            excludes.add("**/kotlin/**")
        }
        resources {
            excludes.add("/META-INF/*.kotlin_module")
            excludes.add("**/kotlin/**")
            excludes.add("**/*.txt")
            excludes.add("**/*.xml")
            excludes.add("**/*.properties")
        }
    }
}

dependencies {
    implementation(project(":shared:wear"))
    implementation(project(":shared:app"))
    implementation(project(":shared:app:lite"))
    implementation(project(":shared:domain"))
    implementation(project(":shared:data"))

    implementation(project(":app-wear"))

    implementation(libs.rxAndroid)

    implementation(libs.dagger)
    kapt(libs.daggerCompiler)

    compileOnly(libs.javaxAnnotation)
}