import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("kotlin")
}

java {
    sourceCompatibility = Constants.JAVA_VERSION
    targetCompatibility = Constants.JAVA_VERSION
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(Constants.JAVA_VERSION.toString()))
    }
}

dependencies {
    implementation(libs.rxJava)
}