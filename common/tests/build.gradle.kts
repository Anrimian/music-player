import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

java {
    sourceCompatibility = Constants.JAVA_VERSION
    targetCompatibility = Constants.JAVA_VERSION
}

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(Constants.JAVA_VERSION.toString()))
}

dependencies {
    implementation(libs.bundles.unitTesting)
    runtimeOnly(libs.junit5Engine)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
