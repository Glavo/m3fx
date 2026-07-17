import org.gradle.api.file.DuplicatesStrategy
import org.gradle.jvm.tasks.Jar
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

application {
    mainModule = "org.glavo.m3fx.catalog"
    mainClass = "org.glavo.m3fx.catalog.M3FXCatalogLauncher"
}

val shadowJar = tasks.register<Jar>("shadowJar") {
    group = "distribution"
    description = "Builds an executable fat JAR for the M3FX catalog without bundling JavaFX."
    archiveBaseName = "m3fx-catalog"
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
    description = "Verifies that the M3FX catalog shadow JAR is executable and does not bundle JavaFX."
    dependsOn(shadowJar)

    val archiveFile = shadowJar.flatMap { it.archiveFile }
    inputs.file(archiveFile)

    doLast {
        val jarFile = archiveFile.get().asFile
        ZipFile(jarFile).use { zip ->
            val entryNames = zip.entries().asSequence()
                .map { entry -> entry.name }
                .toSet()

            val forbiddenEntries = entryNames.asSequence()
                .filter { entryName ->
                    entryName.startsWith("javafx/")
                            || entryName.startsWith("com/sun/javafx/")
                            || (entryName.startsWith("javafx-") && entryName.endsWith(".jar"))
                }
                .toList()
            if (forbiddenEntries.isNotEmpty()) {
                throw GradleException(
                    "The catalog shadow JAR must not bundle JavaFX entries: ${forbiddenEntries.take(10)}"
                )
            }

            val manifestEntry = zip.getEntry("META-INF/MANIFEST.MF")
                ?: throw GradleException("The catalog shadow JAR is missing META-INF/MANIFEST.MF")
            val manifest = zip.getInputStream(manifestEntry).bufferedReader().use { it.readText() }
            val expectedMainClass = "Main-Class: ${application.mainClass.get()}"
            if (!manifest.lineSequence().any { line -> line.trimEnd() == expectedMainClass }) {
                throw GradleException("The catalog shadow JAR manifest is missing '$expectedMainClass'")
            }

            val requiredEntries = listOf(
                "org/glavo/m3fx/catalog/M3FXCatalogLauncher.class",
                "org/glavo/m3fx/catalog/M3FXCatalogApp.class",
                "org/glavo/m3fx/catalog/m3fx-catalog.css",
                "org/glavo/m3fx/controls/M3Button.class",
                "org/glavo/monetfx/Brightness.class"
            )
            for (entryName in requiredEntries) {
                if (entryName !in entryNames) {
                    throw GradleException("The catalog shadow JAR is missing $entryName")
                }
            }
        }
    }
}
