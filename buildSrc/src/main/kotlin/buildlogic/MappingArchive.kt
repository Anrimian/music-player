package buildlogic

import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider

private const val ARCHIVE_DIR_PROPERTY = "mappingArchiveDir"
private const val FORCE_PROPERTY = "forceMappingArchive"
private const val VERSION_CONSTANTS_PATH = "buildSrc/src/main/kotlin/Constants.kt"

/**
 * Registers a task that stores the R8 mapping of the [variant] release build in the external mapping
 * archive repository, and makes it run after the variant is assembled or bundled.
 *
 * [versionCodeConstant] is the `Constants.kt` constant holding this app's version code; the task only
 * archives when that constant differs from its committed revision, i.e. when the build is made from a
 * not-yet-committed version bump.
 */
fun Project.registerMappingArchive(
    app: String,
    variant: String,
    versionCodeConstant: String,
    appId: Provider<String>,
    version: Provider<String>,
    code: Provider<Int>,
    mapping: Provider<RegularFile>,
): TaskProvider<ArchiveMappingTask> {
    val repoRoot = rootProject.layout.projectDirectory
    val archiveDir = providers.gradleProperty(ARCHIVE_DIR_PROPERTY)
        .orElse("../${repoRoot.asFile.name}-mappings")
        .map(repoRoot::dir)
    val forced = providers.gradleProperty(FORCE_PROPERTY).map { true }.orElse(false)

    val capitalizedVariant = variant.replaceFirstChar(Char::uppercase)
    val task = tasks.register("archive${capitalizedVariant}Mapping", ArchiveMappingTask::class.java) {
        group = "reporting"
        description = "Archives the R8 mapping of $variant into the mapping repository"

        appName.set(app)
        variantName.set(variant)
        versionConstantName.set(versionCodeConstant)
        applicationId.set(appId)
        versionName.set(version)
        versionCode.set(code)
        mappingFile.set(mapping)
        versionConstantsFile.set(repoRoot.file(VERSION_CONSTANTS_PATH))
        sourceRepoDir.set(repoRoot)
        archiveRepoDir.set(archiveDir)
        force.set(forced)
    }

    // AGP creates these in afterEvaluate, i.e. after onVariants has run, so they cannot be looked up by name here.
    val producers = setOf("assemble$capitalizedVariant", "bundle$capitalizedVariant")
    tasks.configureEach {
        if (name in producers) finalizedBy(task)
    }

    return task
}
