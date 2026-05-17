plugins {
    `java-library`
}

group = "org.glavo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val javafxVersion = providers.gradleProperty("m3fx.javafx.version").orElse("21").get()
val javafxCompatibilityVersion = providers.gradleProperty("m3fx.javafx.compatibilityVersion").orElse("14").get()
val javafxPlatform = when {
    System.getProperty("os.name").lowercase().contains("win") -> "win"
    System.getProperty("os.name").lowercase().contains("mac") -> "mac"
    else -> "linux"
}

val javafxModules = listOf("base", "graphics", "controls")

fun DependencyHandler.addJavafxDependencies(configurationName: String, version: String) {
    for (module in javafxModules) {
        add(configurationName, "org.openjfx:javafx-$module:$version:$javafxPlatform")
    }
}

val javaFx14Compatibility by sourceSets.creating {
    java.setSrcDirs(listOf("src/main/java"))
    resources.setSrcDirs(emptyList<String>())
}

dependencies {
    addJavafxDependencies("api", javafxVersion)
    api("org.glavo:MonetFX:0.4.0")
    compileOnlyApi("org.jetbrains:annotations:26.1.0")

    addJavafxDependencies(javaFx14Compatibility.implementationConfigurationName, javafxCompatibilityVersion)
    add(javaFx14Compatibility.implementationConfigurationName, "org.glavo:MonetFX:0.4.0")
    add(javaFx14Compatibility.compileOnlyConfigurationName, "org.jetbrains:annotations:26.1.0")

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

tasks.register("javaFx14Compatibility") {
    group = "verification"
    description = "Compiles the main sources against JavaFX $javafxCompatibilityVersion to guard API compatibility."
    dependsOn(javaFx14Compatibility.classesTaskName)
}

tasks.check {
    dependsOn("javaFx14Compatibility")
}

tasks.register("runDemo") {
    group = "application"
    description = "Runs the M3FX JavaFX demo application."
    dependsOn(":demo:run")
}
