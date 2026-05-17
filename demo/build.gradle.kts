import org.gradle.jvm.tasks.Jar
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipFile

plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":"))
}

val jlinkTargetOs = providers.gradleProperty("m3fx.jlink.os").orElse(detectLibericaOs())
val jlinkTargetArch = providers.gradleProperty("m3fx.jlink.arch").orElse(detectLibericaArch())
val jlinkTargetBitness = providers.gradleProperty("m3fx.jlink.bitness").orElse("64")
val jlinkJavaFeature = providers.gradleProperty("m3fx.jlink.javaFeature").orElse("21")
val jlinkReleaseType = providers.gradleProperty("m3fx.jlink.releaseType").orElse("lts")
val jlinkVersionModifier = providers.gradleProperty("m3fx.jlink.versionModifier").orElse("latest")
val jlinkBundleType = providers.gradleProperty("m3fx.jlink.bundleType").orElse("jdk-full")
val jlinkDownloadUrl = providers.gradleProperty("m3fx.jlink.downloadUrl")
val jlinkTargetId = providers.provider {
    "${jlinkTargetOs.get()}-${jlinkTargetArch.get()}-${jlinkTargetBitness.get()}-java${jlinkJavaFeature.get()}"
}
val libericaArchive = layout.buildDirectory.file(jlinkTargetId.map { "liberica/$it/liberica-jdk.zip" })
val libericaExtractDirectory = layout.buildDirectory.dir(jlinkTargetId.map { "liberica/$it/extracted" })
val jlinkImageDirectory = layout.buildDirectory.dir(jlinkTargetId.map { "jlink/m3fx-demo-$it" })

tasks.withType<JavaCompile>().configureEach {
    options.release = 17
    options.encoding = "UTF-8"
}

application {
    mainModule = "org.glavo.m3fx.demo"
    mainClass = "org.glavo.m3fx.demo.M3FXDemoLauncher"
}

val downloadLibericaJdk by tasks.registering {
    group = "distribution"
    description = "Downloads a target-platform BellSoft LibericaJDK Full archive for jlink jmods."
    inputs.property("jlinkTargetOs", jlinkTargetOs)
    inputs.property("jlinkTargetArch", jlinkTargetArch)
    inputs.property("jlinkTargetBitness", jlinkTargetBitness)
    inputs.property("jlinkJavaFeature", jlinkJavaFeature)
    inputs.property("jlinkReleaseType", jlinkReleaseType)
    inputs.property("jlinkVersionModifier", jlinkVersionModifier)
    inputs.property("jlinkBundleType", jlinkBundleType)
    inputs.property("jlinkDownloadUrl", jlinkDownloadUrl.orElse(""))
    outputs.file(libericaArchive)
    outputs.upToDateWhen {
        val archive = libericaArchive.get().asFile
        archive.isFile && isReadableZip(archive)
    }

    doLast {
        val archive = libericaArchive.get().asFile
        if (archive.isFile && isReadableZip(archive)) {
            return@doLast
        }

        if (archive.isFile) {
            archive.delete()
        }

        val partialArchive = archive.resolveSibling("${archive.name}.part")
        if (partialArchive.isFile) {
            partialArchive.delete()
        }

        val downloadUrl = jlinkDownloadUrl.orNull ?: resolveLibericaDownloadUrl()
        archive.parentFile.mkdirs()
        logger.lifecycle("Downloading LibericaJDK Full from $downloadUrl")
        URI(downloadUrl).toURL().openStream().use { input ->
            partialArchive.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        Files.move(
            partialArchive.toPath(),
            archive.toPath(),
            StandardCopyOption.ATOMIC_MOVE,
            StandardCopyOption.REPLACE_EXISTING
        )
    }
}

val extractLibericaJdk by tasks.registering(Sync::class) {
    group = "distribution"
    description = "Extracts the downloaded BellSoft LibericaJDK archive for jlink."
    dependsOn(downloadLibericaJdk)
    from(libericaArchive.map { zipTree(it) })
    into(libericaExtractDirectory)
}

tasks.register("jlinkRuntime") {
    group = "distribution"
    description = "Builds a target-platform runtime image for the M3FX demo with jlink."
    dependsOn(tasks.named("jar"), project(":").tasks.named("jar"), extractLibericaJdk)

    val demoJar = tasks.named<Jar>("jar").flatMap { it.archiveFile }
    val libraryJar = project(":").tasks.named<Jar>("jar").flatMap { it.archiveFile }
    inputs.files(demoJar, libraryJar, configurations.runtimeClasspath)
    inputs.dir(libericaExtractDirectory)
    inputs.property("jlinkTargetOs", jlinkTargetOs)
    inputs.property("jlinkTargetArch", jlinkTargetArch)
    inputs.property("jlinkTargetBitness", jlinkTargetBitness)
    inputs.property("jlinkJavaFeature", jlinkJavaFeature)
    inputs.property("jlinkExecutable", providers.gradleProperty("m3fx.jlink.executable").orElse(""))
    outputs.dir(jlinkImageDirectory)

    doLast {
        val jmodsDirectory = findJmodsDirectory(libericaExtractDirectory.get().asFile)
        val demoJarFile = demoJar.get().asFile
        val libraryJarFile = libraryJar.get().asFile
        val modulePathFiles = linkedSetOf(jmodsDirectory, libraryJarFile, demoJarFile)
        configurations.runtimeClasspath.get()
            .filter { it.isFile && it.extension == "jar" && !it.name.startsWith("javafx-") }
            .forEach(modulePathFiles::add)

        val imageDirectory = jlinkImageDirectory.get().asFile
        delete(imageDirectory)
        val command = listOf(
            jlinkExecutable(jmodsDirectory),
            "--module-path", modulePathFiles.joinToString(File.pathSeparator) { it.absolutePath },
            "--add-modules", "org.glavo.m3fx.demo",
            "--launcher", "m3fx-demo=org.glavo.m3fx.demo/org.glavo.m3fx.demo.M3FXDemoLauncher",
            "--strip-debug",
            "--no-header-files",
            "--no-man-pages",
            "--output", imageDirectory.absolutePath
        )
        logger.lifecycle("Running jlink: ${command.joinToString(" ")}")
        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()
        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()
        if (output.isNotBlank()) {
            logger.lifecycle(output.trimEnd())
        }
        if (exitCode != 0) {
            throw GradleException("jlink exited with code $exitCode")
        }
    }
}

fun resolveLibericaDownloadUrl(): String {
    val query = mapOf(
        "version-feature" to jlinkJavaFeature.get(),
        "version-modifier" to jlinkVersionModifier.get(),
        "bitness" to jlinkTargetBitness.get(),
        "release-type" to jlinkReleaseType.get(),
        "os" to jlinkTargetOs.get(),
        "arch" to jlinkTargetArch.get(),
        "package-type" to "zip",
        "bundle-type" to jlinkBundleType.get(),
        "fields" to "downloadUrl",
        "output" to "text"
    ).entries.joinToString("&") { (name, value) ->
        "${encodeQueryValue(name)}=${encodeQueryValue(value)}"
    }
    val response = URI("https://api.bell-sw.com/v1/liberica/releases?$query").toURL().readText()
    return response.lineSequence()
        .map(String::trim)
        .firstOrNull { it.startsWith("https://") }
        ?.let(::preferBellSoftDownloadUrl)
        ?: throw GradleException("No LibericaJDK download URL was returned for $query")
}

fun preferBellSoftDownloadUrl(downloadUrl: String): String {
    val githubPrefix = "https://github.com/bell-sw/Liberica/releases/download/"
    if (!downloadUrl.startsWith(githubPrefix)) {
        return downloadUrl
    }

    val releaseAndFile = downloadUrl.removePrefix(githubPrefix)
    val separatorIndex = releaseAndFile.indexOf('/')
    if (separatorIndex < 0) {
        return downloadUrl
    }

    val release = releaseAndFile.substring(0, separatorIndex)
    val fileName = releaseAndFile.substring(separatorIndex + 1)
    return "https://download.bell-sw.com/java/$release/$fileName"
}

fun encodeQueryValue(value: String): String =
    URLEncoder.encode(value, StandardCharsets.UTF_8)

fun findJmodsDirectory(root: File): File =
    root.walkTopDown()
        .firstOrNull { it.isDirectory && it.name == "jmods" && it.resolve("java.base.jmod").isFile }
        ?: throw GradleException("No jmods directory was found under ${root.absolutePath}")

fun isReadableZip(file: File): Boolean = try {
    ZipFile(file).use { true }
} catch (_: Exception) {
    false
}

fun jlinkExecutable(jmodsDirectory: File): String {
    providers.gradleProperty("m3fx.jlink.executable").orNull?.let {
        return it
    }

    if (jlinkTargetOs.get() == detectLibericaOs()) {
        val executableName = if (jlinkTargetOs.get() == "windows") "jlink.exe" else "jlink"
        val targetJlink = jmodsDirectory.parentFile.resolve("bin").resolve(executableName)
        if (targetJlink.isFile) {
            return targetJlink.absolutePath
        }
    }

    val hostFeature = Runtime.version().feature().toString()
    if (hostFeature != jlinkJavaFeature.get()) {
        throw GradleException(
            "Cross-platform jlink requires a host jlink with feature version ${jlinkJavaFeature.get()}, "
                    + "but the current Gradle JVM is Java $hostFeature. Run Gradle with a matching JDK "
                    + "or set -Pm3fx.jlink.executable to a matching jlink executable."
        )
    }

    return File(
        File(System.getProperty("java.home"), "bin"),
        if (System.getProperty("os.name").lowercase().contains("win")) "jlink.exe" else "jlink"
    ).absolutePath
}

fun detectLibericaOs(): String = when {
    System.getProperty("os.name").lowercase().contains("win") -> "windows"
    System.getProperty("os.name").lowercase().contains("mac") -> "macos"
    else -> "linux"
}

fun detectLibericaArch(): String = when (val architecture = System.getProperty("os.arch").lowercase()) {
    "aarch64", "arm64" -> "aarch64"
    "amd64", "x86_64" -> "x86"
    else -> architecture
}
