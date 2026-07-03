import org.gradle.api.Task
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
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

val javafxVersion = providers.gradleProperty("m3fx.javafx.version").orElse("21").get()
val detectedJavafxPlatform = when {
    System.getProperty("os.name").lowercase().contains("win") -> "win"
    System.getProperty("os.name").lowercase().contains("mac") -> "mac"
    else -> "linux"
}
val javafxPlatform = providers.gradleProperty("m3fx.javafx.platform").orElse(detectedJavafxPlatform).get()
val javafxModules = listOf("base", "graphics", "controls")
val demoFontPackageUrl = providers.gradleProperty("m3fx.demo.fontPackageUrl")
    .orElse("https://registry.npmmirror.com/@fontpkg/alibaba-puhuiti-3-0/-/alibaba-puhuiti-3-0-0.0.0.tgz")
val demoFontFileName = "AlibabaPuHuiTi-3-65-Medium.ttf"
val demoFontResourcePath = "org/glavo/m3fx/demo/fonts/$demoFontFileName"
val demoFontArchive = layout.buildDirectory.file("downloaded-fonts/alibaba-puhuiti-3-0-0.0.0.tgz")
val generatedDemoFontResources = layout.buildDirectory.dir("generated/resources/demo-font")

sourceSets.main {
    resources.srcDir(generatedDemoFontResources)
}

fun DependencyHandler.addJavafxDependencies(configurationName: String, version: String) {
    for (module in javafxModules) {
        add(configurationName, "org.openjfx:javafx-$module:$version:$javafxPlatform")
    }
}

dependencies {
    implementation(project(":"))
    addJavafxDependencies("implementation", javafxVersion)

    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val jlinkTargetOs = providers.gradleProperty("m3fx.jlink.os").orElse(detectLibericaOs())
val jlinkTargetArch = providers.gradleProperty("m3fx.jlink.arch")
    .map(::normalizeLibericaArch)
    .orElse(detectLibericaArch())
val jlinkTargetBitness = providers.gradleProperty("m3fx.jlink.bitness").orElse("64")
val jlinkJavaFeature = providers.gradleProperty("m3fx.jlink.javaFeature").orElse("21")
val jlinkReleaseType = providers.gradleProperty("m3fx.jlink.releaseType").orElse("lts")
val jlinkVersionModifier = providers.gradleProperty("m3fx.jlink.versionModifier").orElse("latest")
val jlinkBundleType = providers.gradleProperty("m3fx.jlink.bundleType").orElse("jdk-full")
val jlinkDownloadUrl = providers.gradleProperty("m3fx.jlink.downloadUrl")
val jlinkExecutable = providers.gradleProperty("m3fx.jlink.executable")

tasks.withType<JavaCompile>().configureEach {
    options.release = 17
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
}

val downloadDemoFont = tasks.register("downloadDemoFont") {
    group = "build setup"
    description = "Downloads the Alibaba PuHuiTi font package used by the demo."
    inputs.property("demoFontPackageUrl", demoFontPackageUrl)
    outputs.file(demoFontArchive)
    outputs.upToDateWhen {
        val archive = demoFontArchive.get().asFile
        archive.isFile && archive.length() > 0L
    }

    doLast {
        val archive = demoFontArchive.get().asFile
        if (archive.isFile && archive.length() > 0L) {
            return@doLast
        }

        archive.parentFile.mkdirs()
        val partialArchive = archive.resolveSibling("${archive.name}.part")
        if (partialArchive.isFile) {
            partialArchive.delete()
        }

        val resolvedUrl = demoFontPackageUrl.get()
        logger.lifecycle("Downloading demo font package from $resolvedUrl")
        URI(resolvedUrl).toURL().openStream().use { input ->
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

val extractDemoFont = tasks.register("extractDemoFont", Sync::class) {
    group = "build setup"
    description = "Extracts the Alibaba PuHuiTi Medium TTF used as the demo default font."
    dependsOn(downloadDemoFont)
    from(tarTree(resources.gzip(demoFontArchive))) {
        include("**/$demoFontFileName")
        eachFile {
            path = demoFontResourcePath
        }
        includeEmptyDirs = false
    }
    into(generatedDemoFontResources)

    doLast {
        val fontFile = generatedDemoFontResources.get().asFile.resolve(demoFontResourcePath)
        if (!fontFile.isFile) {
            throw GradleException("Font package does not contain $demoFontFileName")
        }
    }
}

tasks.processResources {
    dependsOn(extractDemoFont)
}

application {
    mainModule = "org.glavo.m3fx.demo"
    mainClass = "org.glavo.m3fx.demo.M3FXDemoLauncher"
}

val shadowJar = tasks.register<Jar>("shadowJar") {
    group = "distribution"
    description = "Builds an executable fat JAR for the M3FX demo without bundling JavaFX."
    archiveBaseName = "m3fx-demo"
    archiveClassifier = "shadow"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    dependsOn(configurations.runtimeClasspath)
    from(sourceSets.main.get().output)
    from({
        configurations.runtimeClasspath.get().filterNot { file ->
            file.name.startsWith("javafx-")
        }.map { file ->
            if (file.isDirectory) {
                file
            } else {
                zipTree(file)
            }
        }
    })
    exclude(
        "module-info.class",
        "META-INF/versions/**/module-info.class",
        "META-INF/*.DSA",
        "META-INF/*.RSA",
        "META-INF/*.SF"
    )
    manifest {
        attributes("Main-Class" to application.mainClass.get())
    }
}

tasks.register("verifyShadowJar") {
    group = "verification"
    description = "Verifies that the M3FX demo shadow JAR stays executable and does not bundle JavaFX."
    dependsOn(shadowJar)

    val archiveFile = shadowJar.flatMap { it.archiveFile }
    inputs.file(archiveFile)

    doLast {
        val jarFile = archiveFile.get().asFile
        ZipFile(jarFile).use { zip ->
            val forbiddenEntries = zip.entries().asSequence()
                .map { it.name }
                .filter { entryName ->
                    entryName.startsWith("javafx/")
                            || entryName.startsWith("com/sun/javafx/")
                            || (entryName.startsWith("javafx-") && entryName.endsWith(".jar"))
                }
                .toList()
            if (forbiddenEntries.isNotEmpty()) {
                throw GradleException(
                    "The demo shadow JAR must not bundle JavaFX entries: ${forbiddenEntries.take(10)}"
                )
            }

            val manifestEntry = zip.getEntry("META-INF/MANIFEST.MF")
                ?: throw GradleException("The demo shadow JAR is missing META-INF/MANIFEST.MF")
            val manifest = zip.getInputStream(manifestEntry).bufferedReader().use { it.readText() }
            val expectedMainClass = "Main-Class: ${application.mainClass.get()}"
            if (!manifest.lineSequence().any { it.trimEnd() == expectedMainClass }) {
                throw GradleException("The demo shadow JAR manifest is missing '$expectedMainClass'")
            }

            val fontEntry = zip.getEntry(demoFontResourcePath)
            if (fontEntry == null) {
                throw GradleException("The demo shadow JAR is missing $demoFontResourcePath")
            }
            if (fontEntry.size <= 0L) {
                throw GradleException("The demo shadow JAR bundles an empty $demoFontResourcePath")
            }
        }
    }
}

val jlinkRuntime = registerJlinkRuntime(
    taskName = "jlinkRuntime",
    downloadTaskName = "downloadLibericaJdk",
    extractTaskName = "extractLibericaJdk",
    displayTarget = "target-platform",
    targetOs = jlinkTargetOs,
    targetArch = jlinkTargetArch,
    targetBitness = jlinkTargetBitness,
    javaFeature = jlinkJavaFeature,
    releaseType = jlinkReleaseType,
    versionModifier = jlinkVersionModifier,
    bundleType = jlinkBundleType,
    downloadUrl = jlinkDownloadUrl,
    executable = jlinkExecutable
)

val jlinkWindowsX64Runtime = registerArchitectureJlinkRuntime(
    platformName = "Windows",
    platformKey = "windows",
    architectureName = "X64",
    architectureKey = "windowsX64",
    targetOs = "windows",
    targetArch = "x86"
)

val jlinkWindowsAarch64Runtime = registerArchitectureJlinkRuntime(
    platformName = "Windows",
    platformKey = "windows",
    architectureName = "Aarch64",
    architectureKey = "windowsAarch64",
    targetOs = "windows",
    targetArch = "aarch64"
)

val jlinkLinuxX64Runtime = registerArchitectureJlinkRuntime(
    platformName = "Linux",
    platformKey = "linux",
    architectureName = "X64",
    architectureKey = "linuxX64",
    targetOs = "linux",
    targetArch = "x86"
)

val jlinkLinuxAarch64Runtime = registerArchitectureJlinkRuntime(
    platformName = "Linux",
    platformKey = "linux",
    architectureName = "Aarch64",
    architectureKey = "linuxAarch64",
    targetOs = "linux",
    targetArch = "aarch64"
)

val jlinkMacosX64Runtime = registerArchitectureJlinkRuntime(
    platformName = "Macos",
    platformKey = "macos",
    architectureName = "X64",
    architectureKey = "macosX64",
    targetOs = "macos",
    targetArch = "x86"
)

val jlinkMacosAarch64Runtime = registerArchitectureJlinkRuntime(
    platformName = "Macos",
    platformKey = "macos",
    architectureName = "Aarch64",
    architectureKey = "macosAarch64",
    targetOs = "macos",
    targetArch = "aarch64"
)

val jlinkWindowsRuntime = registerPlatformJlinkRuntime(
    platformName = "Windows",
    platformKey = "windows",
    x64Runtime = jlinkWindowsX64Runtime,
    aarch64Runtime = jlinkWindowsAarch64Runtime
)

val jlinkLinuxRuntime = registerPlatformJlinkRuntime(
    platformName = "Linux",
    platformKey = "linux",
    x64Runtime = jlinkLinuxX64Runtime,
    aarch64Runtime = jlinkLinuxAarch64Runtime
)

val jlinkMacosRuntime = registerPlatformJlinkRuntime(
    platformName = "Macos",
    platformKey = "macos",
    x64Runtime = jlinkMacosX64Runtime,
    aarch64Runtime = jlinkMacosAarch64Runtime
)

tasks.register("jlinkAllPlatformRuntimes") {
    group = "distribution"
    description = "Builds Windows, Linux, and macOS runtime images for the configured architecture with jlink."
    dependsOn(jlinkWindowsRuntime, jlinkLinuxRuntime, jlinkMacosRuntime)
}

tasks.register("jlinkAllPlatformArchitectureRuntimes") {
    group = "distribution"
    description = "Builds Windows, Linux, and macOS x64 and AArch64 runtime images for the M3FX demo with jlink."
    dependsOn(
        jlinkWindowsX64Runtime,
        jlinkWindowsAarch64Runtime,
        jlinkLinuxX64Runtime,
        jlinkLinuxAarch64Runtime,
        jlinkMacosX64Runtime,
        jlinkMacosAarch64Runtime
    )
}

fun registerPlatformJlinkRuntime(
    platformName: String,
    platformKey: String,
    x64Runtime: TaskProvider<Task>,
    aarch64Runtime: TaskProvider<Task>
) = tasks.register("jlink${platformName}Runtime") {
    group = "distribution"
    description = "Builds a $platformName runtime image for the configured architecture with jlink."
    val architecture = platformJlinkArchitectureProperty(platformKey, providers.provider { detectLibericaArch() })
    dependsOn(providers.provider {
        when (architecture.get()) {
            "aarch64" -> aarch64Runtime
            "x86" -> x64Runtime
            else -> throw GradleException(
                "Unsupported $platformName jlink architecture '${architecture.get()}'. Use x86 or aarch64."
            )
        }
    })
}

fun registerArchitectureJlinkRuntime(
    platformName: String,
    platformKey: String,
    architectureName: String,
    architectureKey: String,
    targetOs: String,
    targetArch: String
) = registerJlinkRuntime(
    taskName = "jlink${platformName}${architectureName}Runtime",
    downloadTaskName = "download${platformName}${architectureName}LibericaJdk",
    extractTaskName = "extract${platformName}${architectureName}LibericaJdk",
    displayTarget = "$platformName $architectureName",
    targetOs = providers.provider { targetOs },
    targetArch = providers.provider { targetArch },
    targetBitness = platformArchitectureJlinkProperty(architectureKey, platformKey, "bitness", jlinkTargetBitness),
    javaFeature = platformArchitectureJlinkProperty(architectureKey, platformKey, "javaFeature", jlinkJavaFeature),
    releaseType = platformArchitectureJlinkProperty(architectureKey, platformKey, "releaseType", jlinkReleaseType),
    versionModifier = platformArchitectureJlinkProperty(
        architectureKey,
        platformKey,
        "versionModifier",
        jlinkVersionModifier
    ),
    bundleType = platformArchitectureJlinkProperty(architectureKey, platformKey, "bundleType", jlinkBundleType),
    downloadUrl = platformArchitectureJlinkProperty(
        architectureKey,
        platformKey,
        "downloadUrl",
        providers.provider { "" }
    ),
    executable = platformArchitectureJlinkProperty(architectureKey, platformKey, "executable", jlinkExecutable)
)

fun registerJlinkRuntime(
    taskName: String,
    downloadTaskName: String,
    extractTaskName: String,
    displayTarget: String,
    targetOs: Provider<String>,
    targetArch: Provider<String>,
    targetBitness: Provider<String>,
    javaFeature: Provider<String>,
    releaseType: Provider<String>,
    versionModifier: Provider<String>,
    bundleType: Provider<String>,
    downloadUrl: Provider<String>,
    executable: Provider<String>
): TaskProvider<Task> {
    val targetId = providers.provider {
        "${targetOs.get()}-${targetArch.get()}-${targetBitness.get()}-java${javaFeature.get()}"
    }
    val libericaArchive = layout.buildDirectory.file(providers.provider {
        "liberica/${targetId.get()}/liberica-jdk.${libericaPackageType(targetOs.get())}"
    })
    val libericaExtractDirectory = layout.buildDirectory.dir(targetId.map { "liberica/$it/extracted" })
    val jlinkImageDirectory = layout.buildDirectory.dir(targetId.map { "jlink/m3fx-demo-$it" })
    val hostJlinkNeeded = providers.provider {
        executable.orNull.isNullOrBlank()
                && (targetOs.get() != detectLibericaOs() || targetArch.get() != detectLibericaArch())
                && Runtime.version().feature().toString() != javaFeature.get()
    }
    val hostJlinkId = providers.provider {
        "host-jlink-$taskName-${detectLibericaOs()}-${detectLibericaArch()}-${targetBitness.get()}-java${javaFeature.get()}"
    }
    val hostLibericaArchive = layout.buildDirectory.file(providers.provider {
        "liberica/${hostJlinkId.get()}/liberica-jdk.${libericaPackageType(detectLibericaOs())}"
    })
    val hostLibericaExtractDirectory = layout.buildDirectory.dir(hostJlinkId.map { "liberica/$it/extracted" })

    val downloadTask = tasks.register(downloadTaskName) {
        group = "distribution"
        description = "Downloads a $displayTarget BellSoft LibericaJDK Full archive for jlink jmods."
        inputs.property("jlinkTargetOs", targetOs)
        inputs.property("jlinkTargetArch", targetArch)
        inputs.property("jlinkTargetBitness", targetBitness)
        inputs.property("jlinkJavaFeature", javaFeature)
        inputs.property("jlinkReleaseType", releaseType)
        inputs.property("jlinkVersionModifier", versionModifier)
        inputs.property("jlinkBundleType", bundleType)
        inputs.property("jlinkPackageType", targetOs.map(::libericaPackageType))
        inputs.property("jlinkDownloadUrl", downloadUrl.orElse(""))
        outputs.file(libericaArchive)
        outputs.upToDateWhen {
            val archive = libericaArchive.get().asFile
            archive.isFile && isReadableLibericaArchive(archive, libericaPackageType(targetOs.get()))
        }

        doLast {
            val archive = libericaArchive.get().asFile
            if (archive.isFile && isReadableLibericaArchive(archive, libericaPackageType(targetOs.get()))) {
                return@doLast
            }

            if (archive.isFile) {
                archive.delete()
            }

            val partialArchive = archive.resolveSibling("${archive.name}.part")
            if (partialArchive.isFile) {
                partialArchive.delete()
            }

            val configuredDownloadUrl = downloadUrl.orNull?.takeIf(String::isNotBlank)
            val resolvedDownloadUrl = configuredDownloadUrl ?: resolveLibericaDownloadUrl(
                targetOs = targetOs.get(),
                targetArch = targetArch.get(),
                targetBitness = targetBitness.get(),
                javaFeature = javaFeature.get(),
                releaseType = releaseType.get(),
                versionModifier = versionModifier.get(),
                bundleType = bundleType.get()
            )
            archive.parentFile.mkdirs()
            logger.lifecycle("Downloading LibericaJDK Full from $resolvedDownloadUrl")
            URI(resolvedDownloadUrl).toURL().openStream().use { input ->
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

    val hostJlinkDownloadTask = tasks.register("${taskName}HostJlinkDownload") {
        group = "distribution"
        description = "Downloads a host BellSoft LibericaJDK Full archive for the jlink executable."
        onlyIf { hostJlinkNeeded.get() }
        inputs.property("jlinkHostOs", providers.provider { detectLibericaOs() })
        inputs.property("jlinkHostArch", providers.provider { detectLibericaArch() })
        inputs.property("jlinkTargetBitness", targetBitness)
        inputs.property("jlinkJavaFeature", javaFeature)
        inputs.property("jlinkReleaseType", releaseType)
        inputs.property("jlinkVersionModifier", versionModifier)
        inputs.property("jlinkBundleType", bundleType)
        inputs.property("jlinkPackageType", providers.provider { libericaPackageType(detectLibericaOs()) })
        outputs.file(hostLibericaArchive)
        outputs.upToDateWhen {
            val archive = hostLibericaArchive.get().asFile
            archive.isFile && isReadableLibericaArchive(archive, libericaPackageType(detectLibericaOs()))
        }

        doLast {
            val archive = hostLibericaArchive.get().asFile
            if (archive.isFile && isReadableLibericaArchive(archive, libericaPackageType(detectLibericaOs()))) {
                return@doLast
            }

            if (archive.isFile) {
                archive.delete()
            }

            val partialArchive = archive.resolveSibling("${archive.name}.part")
            if (partialArchive.isFile) {
                partialArchive.delete()
            }

            val resolvedDownloadUrl = resolveLibericaDownloadUrl(
                targetOs = detectLibericaOs(),
                targetArch = detectLibericaArch(),
                targetBitness = targetBitness.get(),
                javaFeature = javaFeature.get(),
                releaseType = releaseType.get(),
                versionModifier = versionModifier.get(),
                bundleType = bundleType.get()
            )
            archive.parentFile.mkdirs()
            logger.lifecycle("Downloading host LibericaJDK Full for jlink from $resolvedDownloadUrl")
            URI(resolvedDownloadUrl).toURL().openStream().use { input ->
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

    val extractTask = tasks.register(extractTaskName, Sync::class) {
        group = "distribution"
        description = "Extracts the downloaded $displayTarget BellSoft LibericaJDK archive for jlink."
        dependsOn(downloadTask)
        from(libericaArchive.map { archive -> libericaArchiveTree(archive, libericaPackageType(targetOs.get())) })
        into(libericaExtractDirectory)
    }

    val hostJlinkExtractTask = tasks.register("${taskName}HostJlinkExtract", Sync::class) {
        group = "distribution"
        description = "Extracts the host BellSoft LibericaJDK archive used for the jlink executable."
        onlyIf { hostJlinkNeeded.get() }
        dependsOn(hostJlinkDownloadTask)
        from(hostLibericaArchive.map { archive ->
            libericaArchiveTree(archive, libericaPackageType(detectLibericaOs()))
        })
        into(hostLibericaExtractDirectory)
    }

    val demoJar = tasks.named<Jar>("jar").flatMap { it.archiveFile }
    val libraryJar = project(":").tasks.named<Jar>("jar").flatMap { it.archiveFile }

    return tasks.register(taskName) {
        group = "distribution"
        description = "Builds a $displayTarget runtime image for the M3FX demo with jlink."
        dependsOn(tasks.named("jar"), project(":").tasks.named("jar"), extractTask, hostJlinkExtractTask)

        inputs.files(demoJar, libraryJar, configurations.runtimeClasspath)
        inputs.dir(libericaExtractDirectory)
        inputs.property("jlinkTargetOs", targetOs)
        inputs.property("jlinkTargetArch", targetArch)
        inputs.property("jlinkTargetBitness", targetBitness)
        inputs.property("jlinkJavaFeature", javaFeature)
        inputs.property("jlinkExecutable", executable.orElse(""))
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
                jlinkExecutable(
                    jmodsDirectory,
                    targetOs.get(),
                    targetArch.get(),
                    javaFeature.get(),
                    executable,
                    hostLibericaExtractDirectory.get().asFile
                ),
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
            verifyJlinkRuntimeImage(imageDirectory, targetOs.get())
        }
    }
}

fun platformJlinkProperty(platformKey: String, propertyName: String, fallback: Provider<String>): Provider<String> =
    providers.gradleProperty("m3fx.jlink.$platformKey.$propertyName").orElse(fallback)

fun platformJlinkArchitectureProperty(platformKey: String, fallback: Provider<String>): Provider<String> =
    providers.gradleProperty("m3fx.jlink.$platformKey.arch")
        .map(::normalizeLibericaArch)
        .orElse(fallback)

fun platformArchitectureJlinkProperty(
    architectureKey: String,
    platformKey: String,
    propertyName: String,
    fallback: Provider<String>
): Provider<String> =
    platformJlinkProperty(architectureKey, propertyName, platformJlinkProperty(platformKey, propertyName, fallback))

fun resolveLibericaDownloadUrl(
    targetOs: String,
    targetArch: String,
    targetBitness: String,
    javaFeature: String,
    releaseType: String,
    versionModifier: String,
    bundleType: String
): String {
    val query = mapOf(
        "version-feature" to javaFeature,
        "version-modifier" to versionModifier,
        "bitness" to targetBitness,
        "release-type" to releaseType,
        "os" to targetOs,
        "arch" to libericaApiArch(targetArch),
        "package-type" to libericaPackageType(targetOs),
        "bundle-type" to bundleType,
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

fun verifyJlinkRuntimeImage(imageDirectory: File, targetOs: String) {
    requireJlinkRuntimeDirectory(imageDirectory, "bin")
    requireJlinkRuntimeDirectory(imageDirectory, "lib")
    requireJlinkRuntimeFile(imageDirectory, "release")
    requireJlinkRuntimeFile(imageDirectory, "lib/modules")
    requireJlinkRuntimeFile(imageDirectory, "lib/javafx.properties")
    requireJlinkRuntimeFile(imageDirectory, "legal/javafx.controls/LICENSE")

    val releaseText = imageDirectory.resolve("release").readText()
    listOf("javafx.controls", "org.glavo.m3fx", "org.glavo.m3fx.demo").forEach { moduleName ->
        if (!releaseText.contains(moduleName)) {
            throw GradleException("The jlink runtime image is missing module $moduleName in ${imageDirectory.absolutePath}/release")
        }
    }

    if (targetOs == "windows") {
        requireJlinkRuntimeFile(imageDirectory, "bin/m3fx-demo")
        requireJlinkRuntimeFile(imageDirectory, "bin/m3fx-demo.bat")
    } else {
        requireJlinkRuntimeFile(imageDirectory, "bin/m3fx-demo")
    }
}

fun requireJlinkRuntimeDirectory(imageDirectory: File, relativePath: String) {
    val directory = imageDirectory.resolveRuntimePath(relativePath)
    if (!directory.isDirectory) {
        throw GradleException("The jlink runtime image is missing required directory ${directory.absolutePath}")
    }
}

fun requireJlinkRuntimeFile(imageDirectory: File, relativePath: String) {
    val file = imageDirectory.resolveRuntimePath(relativePath)
    if (!file.isFile || file.length() <= 0L) {
        throw GradleException("The jlink runtime image is missing required file ${file.absolutePath}")
    }
}

fun File.resolveRuntimePath(relativePath: String): File =
    resolve(relativePath.replace('/', File.separatorChar))

fun isReadableZip(file: File): Boolean = try {
    ZipFile(file).use { true }
} catch (_: Exception) {
    false
}

fun isReadableLibericaArchive(file: File, packageType: String): Boolean =
    if (packageType == "zip") {
        isReadableZip(file)
    } else {
        file.isFile && file.length() > 0L
    }

fun libericaArchiveTree(archive: Any, packageType: String): Any =
    if (packageType == "zip") {
        zipTree(archive)
    } else {
        tarTree(resources.gzip(archive))
    }

fun jlinkExecutable(
    jmodsDirectory: File,
    targetOs: String,
    targetArch: String,
    javaFeature: String,
    executable: Provider<String>,
    hostJlinkDirectory: File
): String {
    executable.orNull?.takeIf(String::isNotBlank)?.let {
        return it
    }

    if (targetOs == detectLibericaOs() && targetArch == detectLibericaArch()) {
        val executableName = if (targetOs == "windows") "jlink.exe" else "jlink"
        val targetJlink = jmodsDirectory.parentFile.resolve("bin").resolve(executableName)
        if (targetJlink.isFile) {
            return targetJlink.absolutePath
        }
    }

    val hostFeature = Runtime.version().feature().toString()
    if (hostFeature != javaFeature) {
        findJlinkExecutable(hostJlinkDirectory, detectLibericaOs())?.let {
            return it.absolutePath
        }

        throw GradleException(
            "Cross-platform or cross-architecture jlink requires a host jlink with feature version $javaFeature. "
                    + "The current Gradle JVM is Java $hostFeature, and no downloaded host jlink was found under "
                    + "${hostJlinkDirectory.absolutePath}. Run Gradle with a matching JDK or set "
                    + "-Pm3fx.jlink.executable to a matching jlink executable."
        )
    }

    return File(
        File(System.getProperty("java.home"), "bin"),
        if (System.getProperty("os.name").lowercase().contains("win")) "jlink.exe" else "jlink"
    ).absolutePath
}

fun findJlinkExecutable(root: File, hostOs: String): File? {
    val executableName = if (hostOs == "windows") "jlink.exe" else "jlink"
    return root.walkTopDown()
        .firstOrNull { it.isFile && it.name == executableName && it.parentFile.name == "bin" }
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

fun normalizeLibericaArch(architecture: String): String = when (architecture.lowercase()) {
    "amd64", "x64", "x86-64", "x86_64" -> "x86"
    "arm64" -> "aarch64"
    else -> architecture
}

fun libericaApiArch(architecture: String): String = when (architecture) {
    "aarch64" -> "arm"
    else -> architecture
}

fun libericaPackageType(targetOs: String): String = when (targetOs) {
    "linux" -> "tar.gz"
    else -> "zip"
}
