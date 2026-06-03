plugins {
    id("java-library")
    id("jacoco")
    id("maven-publish")
    id("signing")
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("org.glavo.load-maven-publish-properties") version "0.1.0"
}

allprojects {
    group = "org.glavo"
    version = "1.0-SNAPSHOT"
}

description = "MonetFX is a JavaFX library that provides material design 3 components for JavaFX applications."

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

tasks.withType<JavaCompile> {
    options.release = 17
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<Javadoc> {
    (options as StandardJavadocDocletOptions).also {
        it.jFlags!!.addAll(listOf("-Duser.language=en", "-Duser.country=", "-Duser.variant="))

        it.encoding("UTF-8")
        it.addStringOption("link", "https://docs.oracle.com/en/java/javase/25/docs/api/")
        it.addBooleanOption("html5", true)
        it.addStringOption("Xdoclint:none", "-quiet")

        it.tags!!.addAll(
            listOf(
                "apiNote:a:API Note:",
                "implNote:a:Implementation Note:",
                "implSpec:a:Implementation Specification:",
            )
        )
    }
}

tasks.withType<GenerateModuleMetadata> {
    enabled = false
}

publishing.publications.create<MavenPublication>("maven") {
    groupId = project.group.toString()
    version = project.version.toString()
    artifactId = project.name

    from(components["java"])

    pom {
        name.set(project.name)
        description.set(project.description)
        url.set("https://github.com/Glavo/weburl-java")

        licenses {
            license {
                name.set("Apache-2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0")
            }
        }

        developers {
            developer {
                id.set("Glavo")
                name.set("Glavo")
                email.set("zjx001202@gmail.com")
            }
        }

        scm {
            url.set("https://github.com/Glavo/weburl-java")
        }
    }
}


if (System.getenv("JITPACK").isNullOrBlank() && rootProject.ext.has("signing.key")) {
    signing {
        useInMemoryPgpKeys(
            rootProject.ext["signing.keyId"].toString(),
            rootProject.ext["signing.key"].toString(),
            rootProject.ext["signing.password"].toString(),
        )
        sign(publishing.publications["maven"])
    }
}

// ./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository
nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))

            username.set(rootProject.ext["sonatypeUsername"].toString())
            password.set(rootProject.ext["sonatypePassword"].toString())
        }
    }
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
    dependsOn(":demo:verifyShadowJar")
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
