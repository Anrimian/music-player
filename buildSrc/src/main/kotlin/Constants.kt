import org.gradle.api.JavaVersion

object Constants {

    const val SYNC_VERSION_NAME = "1.0.2"
    /**Even numbers only, increase only by 2*/
    const val SYNC_VERSION_CODE = 224
    const val SYNC_VERSION_CODE_LEGACY = SYNC_VERSION_CODE - 1

    const val LITE_VERSION_NAME = SYNC_VERSION_NAME
    /**Even numbers only, increase only by 2*/
    const val LITE_VERSION_CODE = 2
    const val LITE_VERSION_CODE_LEGACY = LITE_VERSION_CODE - 1

    const val SYNC_PACKAGE_NAME = "com.github.anrimian.musicplayer"
    const val LITE_PACKAGE_NAME = "com.github.anrimian.musicplayer.lite"
    const val DEBUG_PACKAGE_SUFFIX = ".debug"
    const val DEBUG_SYNC_PACKAGE_SUFFIX = ".sync.debug"
    const val QA_PACKAGE_SUFFIX = ".QA"
    const val QA_SYNC_PACKAGE_SUFFIX = ".sync.QA"


    val JAVA_VERSION = JavaVersion.VERSION_17
    const val MIN_SDK = 21
    const val MODERN_SDK_THRESHOLD = 24 // forced to be 24 bc of glide. Can be reduced to 23 after glide removal
    const val TARGET_SDK = 36
    const val COMPILE_SDK = 36
    const val WEAR_MIN_SDK = 30
    const val WEAR_TARGET_SDK = 36
    const val WEAR_COMPILE_SDK = 36

    const val TEST_INSTRUMENTATION_RUNNER = "androidx.test.runner.AndroidJUnitRunner"
    const val TEST_JUNIT5_BUILDER_ESPRESSO = "de.mannodermaus.junit5.EspressoRunnerBuilder"
    const val TEST_JUNIT5_BUILDER_ANDROID = "de.mannodermaus.junit5.AndroidJUnit5Builder"
}