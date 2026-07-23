import org.gradle.api.file.DuplicatesStrategy
import java.net.URI
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.HexFormat

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
val hmclSourceUrl = providers.gradleProperty("m3fx.hmcl.sourceUrl")
    .orElse("https://github.com/HMCL-dev/HMCL/archive/refs/tags/v3.16.2.zip")
val hmclSourceSha256 = providers.gradleProperty("m3fx.hmcl.sourceSha256")
    .orElse("a103ba35f3203593b12711b04c01594c47e9642e0df560312148967dcb23a3ca")
val hmclSourceArchive = layout.buildDirectory.file("downloaded-sources/hmcl-v3.16.2.zip")
val generatedHmclResources = layout.buildDirectory.dir("generated/resources/hmcl-assets")
val hmclAssetPackagePath = "org/glavo/m3fx/hmcl/demo/assets"

sourceSets.main {
    resources.srcDir(generatedHmclResources)
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
    testImplementation(testFixtures(project(":")))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 17
    options.encoding = "UTF-8"
}

val downloadHmclSource = tasks.register("downloadHmclSource") {
    group = "build setup"
    description = "Downloads the pinned HMCL source archive used by the HMCL MD3 demo."
    inputs.property("hmclSourceUrl", hmclSourceUrl)
    inputs.property("hmclSourceSha256", hmclSourceSha256)
    outputs.file(hmclSourceArchive)

    doLast {
        val archive = hmclSourceArchive.get().asFile
        archive.parentFile.mkdirs()
        val partialArchive = archive.resolveSibling("${archive.name}.part")
        Files.deleteIfExists(partialArchive.toPath())

        val resolvedUrl = hmclSourceUrl.get()
        logger.lifecycle("Downloading HMCL source archive from $resolvedUrl")
        try {
            URI(resolvedUrl).toURL().openStream().use { input ->
                partialArchive.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            val actualSha256 = HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(partialArchive.toPath()))
            )
            val expectedSha256 = hmclSourceSha256.get().lowercase()
            if (actualSha256 != expectedSha256) {
                throw GradleException(
                    "HMCL source archive checksum mismatch: expected $expectedSha256, got $actualSha256"
                )
            }
            Files.move(
                partialArchive.toPath(),
                archive.toPath(),
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING
            )
        } catch (failure: Throwable) {
            Files.deleteIfExists(partialArchive.toPath())
            throw failure
        }
    }
}

val extractHmclAssets = tasks.register<Sync>("extractHmclAssets") {
    group = "build setup"
    description = "Extracts HMCL artwork, default skin assets, and the upstream license."
    dependsOn(downloadHmclSource)
    duplicatesStrategy = DuplicatesStrategy.FAIL

    from({ zipTree(hmclSourceArchive) }) {
        include(
            "*/HMCL/src/main/resources/assets/img/**",
            "*/HMCLCore/src/main/resources/assets/img/skin/**",
            "*/LICENSE"
        )
        eachFile {
            val normalizedPath = path.replace('\\', '/')
            val hmclImageMarker = "/HMCL/src/main/resources/assets/img/"
            val hmclCoreSkinMarker = "/HMCLCore/src/main/resources/assets/img/skin/"
            path = when {
                hmclImageMarker in normalizedPath ->
                    "$hmclAssetPackagePath/img/${normalizedPath.substringAfter(hmclImageMarker)}"
                hmclCoreSkinMarker in normalizedPath ->
                    "$hmclAssetPackagePath/img/skin/${normalizedPath.substringAfter(hmclCoreSkinMarker)}"
                normalizedPath.endsWith("/LICENSE") ->
                    "META-INF/licenses/HMCL-GPL-3.0.txt"
                else -> {
                    exclude()
                    normalizedPath
                }
            }
        }
        includeEmptyDirs = false
    }
    into(generatedHmclResources)
}

tasks.processResources {
    dependsOn(extractHmclAssets)
}

val verifyHmclAssets = tasks.register("verifyHmclAssets") {
    group = "verification"
    description = "Verifies the generated HMCL artwork, skin assets, and upstream license."
    dependsOn(extractHmclAssets)
    inputs.dir(generatedHmclResources)

    doLast {
        val resourceRoot = generatedHmclResources.get().asFile
        val requiredFiles = listOf(
            "$hmclAssetPackagePath/img/icon.png",
            "$hmclAssetPackagePath/img/skin/slim/alex.png",
            "$hmclAssetPackagePath/img/skin/wide/steve.png",
            "META-INF/licenses/HMCL-GPL-3.0.txt"
        )
        val missingFiles = requiredFiles.filter { relativePath ->
            val file = resourceRoot.resolve(relativePath)
            !file.isFile || file.length() == 0L
        }
        if (missingFiles.isNotEmpty()) {
            throw GradleException("Generated HMCL resources are missing or empty: $missingFiles")
        }
    }
}

tasks.named("check") {
    dependsOn(verifyHmclAssets)
}

application {
    mainModule = "org.glavo.m3fx.hmcl.demo"
    mainClass = "org.glavo.m3fx.hmcl.demo.HMCLM3DemoLauncher"
}
