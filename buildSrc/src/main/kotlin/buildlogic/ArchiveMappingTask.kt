package buildlogic

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.MessageDigest
import java.time.Instant
import java.util.zip.GZIPOutputStream
import javax.inject.Inject

/**
 * Copies the R8 mapping file of a release variant into the external mapping archive repository and
 * commits it there, so that crash reports received by e-mail can be de-obfuscated later.
 *
 * The archive lives outside this repository (see the `mappingArchiveDir` Gradle property) because a
 * single mapping is ~7 MB gzipped and would bloat the source history.
 */
@DisableCachingByDefault(because = "Writes into an external repository, produces no output inside the project")
abstract class ArchiveMappingTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val mappingFile: RegularFileProperty

    /** `Constants.kt`, compared against its committed revision to detect an unreleased version bump. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val versionConstantsFile: RegularFileProperty

    @get:Input
    abstract val versionConstantName: Property<String>

    @get:Input
    abstract val appName: Property<String>

    @get:Input
    abstract val variantName: Property<String>

    @get:Input
    abstract val applicationId: Property<String>

    @get:Input
    abstract val versionName: Property<String>

    @get:Input
    abstract val versionCode: Property<Int>

    @get:Input
    abstract val force: Property<Boolean>

    @get:Internal
    abstract val sourceRepoDir: DirectoryProperty

    @get:Internal
    abstract val archiveRepoDir: DirectoryProperty

    @get:Inject
    abstract val execOps: ExecOperations

    @TaskAction
    fun archive() {
        val mapping = mappingFile.get().asFile
        val baseName = "${versionName.get()}-b${versionCode.get()}-${variantName.get()}"
        val appDir = File(archiveRepoDir.get().asFile, appName.get())

        val skipReason = versionBumpSkipReason()
        if (skipReason != null && !force.get()) {
            reportSkip(skipReason, appDir, baseName, mapping)
            return
        }

        if (!appDir.mkdirs() && !appDir.isDirectory) {
            throw GradleException("Cannot create mapping archive directory $appDir")
        }
        val target = File(appDir, "$baseName.map.gz")
        gzip(mapping, target)
        File(appDir, "$baseName.json").writeText(metadataOf(mapping))

        logger.lifecycle("Mapping archived: $target (${target.length() / 1024 / 1024} MB)")
        commit(archiveRepoDir.get().asFile, baseName)
    }

    /** Returns a human-readable reason to skip archiving, or `null` when the mapping must be archived. */
    private fun versionBumpSkipReason(): String? {
        val constantsFile = versionConstantsFile.get().asFile
        val constantName = versionConstantName.get()
        val workingTreeValue = readVersionCode(constantsFile.readText(), constantName)
            ?: return "$constantName not found in ${constantsFile.name}"

        val sourceRepo = sourceRepoDir.get().asFile
        val relativePath = constantsFile.relativeTo(sourceRepo).invariantSeparatorsPath
        val committedText = git(sourceRepo, "show", "HEAD:$relativePath")
            ?: return null // no git history to compare against — archive to be safe
        val committedValue = readVersionCode(committedText, constantName) ?: return null

        return if (workingTreeValue == committedValue) {
            "$constantName is $workingTreeValue both in the working tree and in HEAD, so this is not a version bump build"
        } else {
            null
        }
    }

    /**
     * Skipping is only safe while the archive already holds the mapping of exactly this build.
     * Anything else means a shipped build would be left without a usable mapping, so say it loudly.
     */
    private fun reportSkip(reason: String, appDir: File, baseName: String, mapping: File) {
        logger.lifecycle("Mapping archive skipped: $reason.")
        val archivedHash = File(appDir, "$baseName.json")
            .takeIf(File::isFile)
            ?.readText()
            ?.let { MAPPING_HASH_REGEX.find(it)?.groupValues?.get(1) }

        when (archivedHash) {
            null -> logger.error(
                "No mapping is archived for $baseName. Crashes from this build will not be de-obfuscatable. " +
                    "Re-run with -PforceMappingArchive to archive it."
            )
            sha256(mapping) -> logger.lifecycle("The archived mapping for $baseName already matches this build.")
            else -> logger.error(
                "The mapping archived for $baseName comes from a different build than the one just produced. " +
                    "Re-run with -PforceMappingArchive to replace it."
            )
        }
    }

    private fun metadataOf(mapping: File): String {
        val sourceRepo = sourceRepoDir.get().asFile
        val commit = git(sourceRepo, "rev-parse", "HEAD")?.trim().orEmpty()
        val dirty = git(sourceRepo, "status", "--porcelain")?.isNotBlank() == true
        return """
            {
              "app": "${appName.get()}",
              "applicationId": "${applicationId.get()}",
              "variant": "${variantName.get()}",
              "versionName": "${versionName.get()}",
              "versionCode": ${versionCode.get()},
              "sourceCommit": "$commit",
              "sourceDirty": $dirty,
              "mappingSha256": "${sha256(mapping)}",
              "builtAt": "${Instant.now()}"
            }

        """.trimIndent()
    }

    private fun commit(archiveRoot: File, baseName: String) {
        if (!File(archiveRoot, ".git").isDirectory) {
            if (git(archiveRoot, "init", "-b", "main") == null) {
                logger.error("Mapping archived to disk but 'git init' failed in $archiveRoot")
                return
            }
            File(archiveRoot, "README.md").writeText(ARCHIVE_README)
        }
        git(archiveRoot, "add", "-A")
        val message = "${appName.get()} ${versionName.get()} (${versionCode.get()}) $baseName"
        if (git(archiveRoot, "commit", "-m", message) == null) {
            logger.error(
                "Mapping archived to disk but the commit in $archiveRoot failed. " +
                    "Commit it manually so the mapping is not lost."
            )
        }
    }

    /** Runs git and returns its stdout, or `null` when the command failed. */
    private fun git(workingDir: File, vararg args: String): String? {
        val stdout = ByteArrayOutputStream()
        val result = execOps.exec {
            commandLine(listOf("git", "-C", workingDir.absolutePath) + args)
            standardOutput = stdout
            errorOutput = ByteArrayOutputStream()
            isIgnoreExitValue = true
        }
        return if (result.exitValue == 0) stdout.toString(Charsets.UTF_8) else null
    }

    private fun gzip(source: File, target: File) {
        source.inputStream().buffered().use { input ->
            GZIPOutputStream(target.outputStream().buffered()).use { output -> input.copyTo(output) }
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        file.inputStream().buffered().use { input ->
            var read = input.read(buffer)
            while (read > 0) {
                digest.update(buffer, 0, read)
                read = input.read(buffer)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun readVersionCode(fileContent: String, constantName: String): Int? =
        Regex("""const\s+val\s+$constantName\s*=\s*(\d+)""").find(fileContent)?.groupValues?.get(1)?.toIntOrNull()
}

private val MAPPING_HASH_REGEX = Regex(""""mappingSha256"\s*:\s*"(\w+)"""")

private val ARCHIVE_README = """
    # S Music Player — R8 mapping archive

    Generated by the `archive*Mapping` Gradle tasks of the `music-player` repository. Do not edit by hand.

    Layout: `<app>/<versionName>-b<versionCode>-<variant>.map.gz`, plus a `.json` sidecar holding the
    source commit the build was made from.

    To de-obfuscate a stacktrace, run `scripts/retrace.sh` from the `music-player` repository.

""".trimIndent()
