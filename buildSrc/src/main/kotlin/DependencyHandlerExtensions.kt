import org.gradle.api.artifacts.dsl.DependencyHandler


fun DependencyHandler.wearImplementation(dependencyNotation: Any) {
    add("wearImplementation", dependencyNotation)
}

fun DependencyHandler.modernImplementation(dependencyNotation: Any) {
    add("modernImplementation", dependencyNotation)
}

fun DependencyHandler.kaptModern(dependencyNotation: Any) {
    add("kaptModern", dependencyNotation)
}

fun DependencyHandler.kspModern(dependencyNotation: Any) {
    add("kspModern", dependencyNotation)
}

fun DependencyHandler.legacyImplementation(dependencyNotation: Any) {
    add("legacyImplementation", dependencyNotation)
}

fun DependencyHandler.kaptLegacy(dependencyNotation: Any) {
    add("kaptLegacy", dependencyNotation)
}

fun DependencyHandler.kspLegacy(dependencyNotation: Any) {
    add("kspLegacy", dependencyNotation)
}

fun DependencyHandler.testModernImplementation(dependencyNotation: Any) {
    add("testModernImplementation", dependencyNotation)
}

fun DependencyHandler.testLegacyImplementation(dependencyNotation: Any) {
    add("testLegacyImplementation", dependencyNotation)
}