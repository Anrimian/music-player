plugins {
    id("kotlin")
}

java {
    sourceCompatibility = Constants.JAVA_VERSION
    targetCompatibility = Constants.JAVA_VERSION
}

dependencies {
    implementation(libs.rxJava)
    implementation(libs.coroutinesCore)
    implementation(libs.coroutinesRx)

    compileOnly(libs.javaxAnnotation)
}