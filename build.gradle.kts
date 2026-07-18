import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kapt) apply false
    alias(libs.plugins.kotlinCompose) apply false
    alias(libs.plugins.androidJunit5) apply false
}

allprojects {
    configurations.all {
        // fix for test running
        resolutionStrategy.force("org.objenesis:objenesis:2.6")
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

allprojects {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.addAll(
                "-Xno-call-assertions",
                "-Xno-receiver-assertions",
                "-Xno-param-assertions",
                "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi"
            )
        }
    }
}

subprojects {
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.fromTarget(Constants.JAVA_VERSION.toString()))
        }
    }

    if (!file("build.gradle.kts").exists() && !file("build.gradle").exists()) {
        tasks.register<Delete>("clean") {
            delete(layout.buildDirectory)
        }
    }
}
