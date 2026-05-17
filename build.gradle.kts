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
    addJavafxDependencies("compileOnly", javafxVersion)
    api("org.glavo:MonetFX:0.4.0")
    compileOnlyApi("org.jetbrains:annotations:26.1.0")

    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    addJavafxDependencies("testImplementation", javafxVersion)
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

tasks.register("shadowDemoJar") {
    group = "distribution"
    description = "Builds an executable fat JAR for the M3FX demo application."
    dependsOn(":demo:shadowJar")
}

tasks.register("jlinkDemoWindowsRuntime") {
    group = "distribution"
    description = "Builds a Windows jlink runtime image for the M3FX demo application."
    dependsOn(":demo:jlinkWindowsRuntime")
}

tasks.register("jlinkDemoWindowsX64Runtime") {
    group = "distribution"
    description = "Builds a Windows x64 jlink runtime image for the M3FX demo application."
    dependsOn(":demo:jlinkWindowsX64Runtime")
}

tasks.register("jlinkDemoWindowsAarch64Runtime") {
    group = "distribution"
    description = "Builds a Windows AArch64 jlink runtime image for the M3FX demo application."
    dependsOn(":demo:jlinkWindowsAarch64Runtime")
}

tasks.register("jlinkDemoLinuxRuntime") {
    group = "distribution"
    description = "Builds a Linux jlink runtime image for the M3FX demo application."
    dependsOn(":demo:jlinkLinuxRuntime")
}

tasks.register("jlinkDemoLinuxX64Runtime") {
    group = "distribution"
    description = "Builds a Linux x64 jlink runtime image for the M3FX demo application."
    dependsOn(":demo:jlinkLinuxX64Runtime")
}

tasks.register("jlinkDemoLinuxAarch64Runtime") {
    group = "distribution"
    description = "Builds a Linux AArch64 jlink runtime image for the M3FX demo application."
    dependsOn(":demo:jlinkLinuxAarch64Runtime")
}

tasks.register("jlinkDemoMacosRuntime") {
    group = "distribution"
    description = "Builds a macOS jlink runtime image for the M3FX demo application."
    dependsOn(":demo:jlinkMacosRuntime")
}

tasks.register("jlinkDemoMacosX64Runtime") {
    group = "distribution"
    description = "Builds a macOS x64 jlink runtime image for the M3FX demo application."
    dependsOn(":demo:jlinkMacosX64Runtime")
}

tasks.register("jlinkDemoMacosAarch64Runtime") {
    group = "distribution"
    description = "Builds a macOS AArch64 jlink runtime image for the M3FX demo application."
    dependsOn(":demo:jlinkMacosAarch64Runtime")
}

tasks.register("jlinkDemoAllPlatformRuntimes") {
    group = "distribution"
    description = "Builds Windows, Linux, and macOS jlink runtime images for the configured architecture."
    dependsOn(":demo:jlinkAllPlatformRuntimes")
}

tasks.register("jlinkDemoAllPlatformArchitectureRuntimes") {
    group = "distribution"
    description = "Builds Windows, Linux, and macOS x64 and AArch64 jlink runtime images for the M3FX demo application."
    dependsOn(":demo:jlinkAllPlatformArchitectureRuntimes")
}
