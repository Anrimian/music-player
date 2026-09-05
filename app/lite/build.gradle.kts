import buildlogic.registerMappingArchive
import com.android.build.api.artifact.SingleArtifact

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

    namespace = "com.github.anrimian.musicplayer.lite"

    compileSdk = Constants.COMPILE_SDK

    defaultConfig {
        targetSdk = Constants.TARGET_SDK
        applicationId = Constants.LITE_PACKAGE_NAME
        versionName = Constants.LITE_VERSION_NAME
        testInstrumentationRunner = Constants.TEST_INSTRUMENTATION_RUNNER
        testApplicationId = "${Constants.LITE_PACKAGE_NAME}.test"
        base.archivesName.set("${applicationId}-v${versionName}-b[m${Constants.LITE_VERSION_CODE}-l${Constants.LITE_VERSION_CODE_LEGACY}]")
    }

    flavorDimensions.add("feature")
    flavorDimensions.add("apiVersion")

    productFlavors {
        create("modern") {
            dimension = "apiVersion"
            minSdk = Constants.MODERN_SDK_THRESHOLD
            versionCode = Constants.LITE_VERSION_CODE
            isDefault = true
        }
        create("legacy") {
            dimension = "apiVersion"
            minSdk = Constants.MIN_SDK
            versionCode = Constants.LITE_VERSION_CODE_LEGACY
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
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        create("QA") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("test")
            applicationIdSuffix = Constants.QA_PACKAGE_SUFFIX
        }
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = Constants.DEBUG_PACKAGE_SUFFIX
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
}

androidComponents {
    onVariants { appVariant ->
        if (appVariant.buildType != "release") return@onVariants
        val output = appVariant.outputs.single()
        registerMappingArchive(
            app = "lite",
            variant = appVariant.name,
            versionCodeConstant = "LITE_VERSION_CODE",
            appId = appVariant.applicationId,
            version = output.versionName.map { it },
            code = output.versionCode.map { it },
            mapping = appVariant.artifacts.get(SingleArtifact.OBFUSCATION_MAPPING_FILE),
        )
    }
}

dependencies {
    implementation(project(":shared:app"))
    implementation(project(":shared:domain"))
    implementation(project(":shared:app:lite"))

    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":app"))

    implementation(project(":libs:fsync:core"))

    implementation(libs.appCompat)

    implementation(libs.rxAndroid)

    implementation(libs.coroutinesAndroid)

    implementation(libs.dagger)
    kapt(libs.daggerCompiler)

    compileOnly(libs.javaxAnnotation)
}