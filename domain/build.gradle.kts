plugins {
    id("kotlin")
}

java {
    sourceCompatibility = Constants.JAVA_VERSION
    targetCompatibility = Constants.JAVA_VERSION
}

dependencies {
    implementation(project(":shared:domain"))
    implementation(project(":libs:fsync:core"))

    implementation(libs.rxJava)
    implementation(libs.coroutinesCore)
    implementation(libs.coroutinesRx)

    compileOnly(libs.javaxAnnotation)

    testImplementation(libs.bundles.unitTesting)
    testRuntimeOnly(libs.junit5Engine)
    testRuntimeOnly(libs.junitPlatformLauncher)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

