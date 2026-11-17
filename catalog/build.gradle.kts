import org.gradle.api.file.DuplicatesStrategy
import org.gradle.jvm.tasks.Jar

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

tasks.register<Jar>("shadowJar") {
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
