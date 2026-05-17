plugins {
    `java-library`
}

group = "org.glavo"
version = "1.0-SNAPSHOT"

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
    addJavafxDependencies("api", javafxVersion)
    api("org.glavo:MonetFX:0.4.0")
    compileOnlyApi("org.jetbrains:annotations:26.1.0")

    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 17
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
}

tasks.register("runDemo") {
    group = "application"
    description = "Runs the M3FX JavaFX demo application."
    dependsOn(":demo:run")
}

tasks.register("jlinkDemoRuntime") {
    group = "distribution"
    description = "Builds a jlink runtime image for the M3FX demo application."
    dependsOn(":demo:jlinkRuntime")
}
