import java.util.zip.ZipFile
import org.gradle.api.tasks.Delete
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage

plugins {
    id("java-library")
    id("jacoco")
    id("maven-publish")
    id("signing")
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("org.glavo.load-maven-publish-properties") version "0.1.0"
    id("org.glavo.gradle-wrapper-neo") version "0.2.0"
}

allprojects {
    group = "org.glavo"
    version = "1.0-SNAPSHOT"
}

description = "M3FX is a Material Design 3 component library for JavaFX applications."

val m3fxGroupId = group.toString()
val m3fxArtifactId = name
val m3fxVersion = version.toString()
val m3fxCoordinates = "$m3fxGroupId:$m3fxArtifactId:$m3fxVersion"
val m3fxModuleId = "$m3fxGroupId:$m3fxArtifactId"
val monetFxVersion = "0.4.0"

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
    api("org.glavo:MonetFX:$monetFxVersion")
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
    maxHeapSize = providers.gradleProperty("m3fx.test.maxHeapSize").orElse("1g").get()

    doLast {
        val resultDirectory = reports.junitXml.outputLocation.get().asFile
        val documentBuilder = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val warnings = buildList {
            fileTree(resultDirectory) {
                include("TEST-*.xml")
            }.forEach { resultFile ->
                val document = documentBuilder.parse(resultFile)
                val errorNodes = document.getElementsByTagName("system-err")
                for (index in 0 until errorNodes.length) {
                    errorNodes.item(index).textContent.lineSequence()
                        .map(String::trim)
                        .filter { line ->
                            line.contains("Could not resolve '-m3-")
                                    || line.contains("ClassCastException")
                                    && line.contains("while converting value")
                                    && line.contains("/org/glavo/m3fx/styles/")
                        }
                        .forEach { line -> add("${resultFile.name}: $line") }
                }
            }
        }
        if (warnings.isNotEmpty()) {
            throw GradleException(
                "M3FX CSS warnings were emitted during tests:\n" + warnings.joinToString("\n")
            )
        }
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}

val monetFxJavadocLinkDirectory =
    layout.buildDirectory.dir("generated/javadoc-links/monetfx-$monetFxVersion")

val generateMonetFxJavadocElementList = tasks.register("generateMonetFxJavadocElementList") {
    group = "documentation"
    description = "Generates the offline Javadoc element list for MonetFX."

    val outputDirectory = monetFxJavadocLinkDirectory
    outputs.dir(outputDirectory)

    doLast {
        val elementList = outputDirectory.get().file("element-list").asFile
        elementList.parentFile.mkdirs()
        elementList.writeText(
            """
            module:org.glavo.monetfx
            org.glavo.monetfx
            org.glavo.monetfx.beans.binding
            org.glavo.monetfx.beans.property
            org.glavo.monetfx.beans.value
            """.trimIndent() + System.lineSeparator(),
            Charsets.UTF_8
        )
    }
}

tasks.withType<Javadoc> {
    val mainSourceDirectory = layout.projectDirectory.dir("src/main/java").asFile
    dependsOn(generateMonetFxJavadocElementList)
    inputs.dir(monetFxJavadocLinkDirectory)
    setSource(fileTree(mainSourceDirectory) {
        include("org/glavo/m3fx/controls/package-info.java")
    })

    (options as StandardJavadocDocletOptions).also {
        it.jFlags!!.addAll(listOf("-Duser.language=en", "-Duser.country=", "-Duser.variant="))

        it.encoding("UTF-8")
        it.addStringOption("sourcepath", mainSourceDirectory.absolutePath)
        it.addStringOption("subpackages", "org.glavo.m3fx")
        it.links(
            "https://docs.oracle.com/en/java/javase/17/docs/api/",
            "https://openjfx.io/javadoc/$javafxVersion/"
        )
        it.linksOffline(
            "https://javadoc.io/doc/org.glavo/MonetFX/$monetFxVersion/",
            monetFxJavadocLinkDirectory.get().asFile.absolutePath
        )
        it.addBooleanOption("html5", true)
        it.addBooleanOption("Werror", true)
        it.addStringOption("-show-packages", "exported")
        it.addStringOption("exclude", "org.glavo.m3fx.internal:org.glavo.m3fx.internal.animation:org.glavo.m3fx.internal.shape:org.glavo.m3fx.internal.theme:org.glavo.m3fx.internal.tokens:org.glavo.m3fx.skins")
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
    groupId = m3fxGroupId
    version = m3fxVersion
    artifactId = m3fxArtifactId

    from(components["java"])

    pom {
        name.set("M3FX")
        description.set(project.description)
        url.set("https://github.com/Glavo/m3fx")

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
            connection.set("scm:git:https://github.com/Glavo/m3fx.git")
            developerConnection.set("scm:git:ssh://git@github.com/Glavo/m3fx.git")
            url.set("https://github.com/Glavo/m3fx")
        }

        withXml {
            fun groovy.util.Node.localName(): String =
                name().toString().substringAfterLast('}')

            fun groovy.util.Node.childText(childName: String): String? =
                children()
                    .filterIsInstance<groovy.util.Node>()
                    .firstOrNull { child -> child.localName() == childName }
                    ?.text()

            val dependenciesNode = asNode().children()
                .filterIsInstance<groovy.util.Node>()
                .firstOrNull { it.localName() == "dependencies" }
                ?: return@withXml

            dependenciesNode.children()
                .filterIsInstance<groovy.util.Node>()
                .filter { dependency -> dependency.localName() == "dependency" }
                .filter { dependency ->
                    dependency.childText("groupId") == "org.jetbrains"
                            && dependency.childText("artifactId") == "annotations"
                }
                .forEach { dependency ->
                    if (dependency.childText("optional") == null) {
                        dependency.appendNode("optional", "true")
                    }
                }
        }
    }
}

val verificationMavenRepositoryDirectory = layout.buildDirectory.dir("verification-maven-repository")

publishing.repositories.maven {
    name = "verification"
    url = verificationMavenRepositoryDirectory.get().asFile.toURI()
}

val cleanVerificationMavenRepository = tasks.register<Delete>("cleanVerificationMavenRepository") {
    group = "verification"
    description = "Deletes the build-local Maven repository used by publication verification."

    delete(verificationMavenRepositoryDirectory)
}

tasks.named("publishMavenPublicationToVerificationRepository") {
    dependsOn(cleanVerificationMavenRepository)
}

val verifyPublicationMetadata = tasks.register("verifyPublicationMetadata") {
    group = "verification"
    description = "Verifies that generated Maven publication metadata matches M3FX release constraints."

    dependsOn(tasks.named("generatePomFileForMavenPublication"))

    val generatedPom = layout.buildDirectory.file("publications/maven/pom-default.xml")
    inputs.file(generatedPom)

    doLast {
        val pom = generatedPom.get().asFile.readText()

        fun requirePomContains(fragment: String, message: String) {
            if (!pom.contains(fragment)) {
                throw GradleException(message)
            }
        }

        requirePomContains(
            "<name>M3FX</name>",
            "The generated Maven POM must use the M3FX display name."
        )
        requirePomContains(
            "<description>M3FX is a Material Design 3 component library for JavaFX applications.</description>",
            "The generated Maven POM must describe M3FX, not a copied project."
        )
        requirePomContains(
            "<url>https://github.com/Glavo/m3fx</url>",
            "The generated Maven POM must point to the M3FX project URL."
        )
        requirePomContains(
            "<connection>scm:git:https://github.com/Glavo/m3fx.git</connection>",
            "The generated Maven POM must contain the M3FX SCM connection."
        )
        requirePomContains(
            "<developerConnection>scm:git:ssh://git@github.com/Glavo/m3fx.git</developerConnection>",
            "The generated Maven POM must contain the M3FX developer SCM connection."
        )

        val staleFragments = listOf("MonetFX is a JavaFX library", "weburl-java")
                .filter(pom::contains)
        if (staleFragments.isNotEmpty()) {
            throw GradleException("The generated Maven POM contains stale metadata: $staleFragments")
        }

        if (pom.contains("<groupId>org.openjfx</groupId>")
                || Regex("<artifactId>javafx-[^<]+</artifactId>").containsMatchIn(pom)) {
            throw GradleException("The library publication must not expose JavaFX dependencies.")
        }

        val monetFxDependency = Regex(
            "<dependency>\\s*"
                    + "<groupId>org\\.glavo</groupId>\\s*"
                    + "<artifactId>MonetFX</artifactId>\\s*"
                    + "<version>${Regex.escape(monetFxVersion)}</version>\\s*"
                    + "<scope>compile</scope>\\s*"
                    + "</dependency>"
        )
        if (!monetFxDependency.containsMatchIn(pom)) {
            throw GradleException("MonetFX must be published as a compile dependency.")
        }

        val annotationsDependency = Regex(
            "<dependency>\\s*"
                    + "<groupId>org\\.jetbrains</groupId>\\s*"
                    + "<artifactId>annotations</artifactId>\\s*"
                    + "<version>[^<]+</version>\\s*"
                    + "<scope>compile</scope>\\s*"
                    + "<optional>true</optional>\\s*"
                    + "</dependency>"
        )
        if (!annotationsDependency.containsMatchIn(pom)) {
            throw GradleException("JetBrains annotations must be published as an optional compile dependency.")
        }
    }
}

val verifyPublicationArtifacts = tasks.register("verifyPublicationArtifacts") {
    group = "verification"
    description = "Verifies that Maven publication JAR artifacts contain the expected M3FX API and resources."

    val mainJar = tasks.named<org.gradle.jvm.tasks.Jar>("jar").flatMap { it.archiveFile }
    val sourcesJar = tasks.named<org.gradle.jvm.tasks.Jar>("sourcesJar").flatMap { it.archiveFile }
    val javadocJar = tasks.named<org.gradle.jvm.tasks.Jar>("javadocJar").flatMap { it.archiveFile }
    val mainSourceDirectory = layout.projectDirectory.dir("src/main/java").asFile
    val mainModuleInfo = mainSourceDirectory.resolve("module-info.java")
    val mainResourceDirectory = layout.projectDirectory.dir("src/main/resources").asFile
    val mainJavaSources = fileTree(mainSourceDirectory) {
        include("**/*.java")
    }
    val mainStylesheetResources = fileTree(mainResourceDirectory) {
        include("org/glavo/m3fx/styles/**/*.css")
    }

    dependsOn(tasks.named("jar"), tasks.named("sourcesJar"), tasks.named("javadocJar"))
    inputs.files(mainJar, sourcesJar, javadocJar, mainModuleInfo, mainJavaSources, mainStylesheetResources)

    doLast {
        fun jarEntries(file: File): Set<String> =
            ZipFile(file).use { zip ->
                zip.entries().asSequence()
                    .map { entry -> entry.name }
                    .toSet()
            }

        fun requireEntry(entries: Set<String>, entryName: String, artifactName: String) {
            if (entryName !in entries) {
                throw GradleException("$artifactName is missing required entry $entryName")
            }
        }

        fun sourceEntry(root: File, file: File): String =
            root.toPath().relativize(file.toPath()).toString().replace(File.separatorChar, '/')

        val exportPattern = Regex("^\\s*exports\\s+([\\w.]+)\\s*;")
        val exportedPackagePaths = mainModuleInfo.readLines()
            .mapNotNull { line -> exportPattern.find(line)?.groupValues?.get(1) }
            .map { packageName -> packageName.replace('.', '/') }
            .sorted()
        if (exportedPackagePaths.isEmpty()) {
            throw GradleException("Main module descriptor should export at least one API package.")
        }

        val mainEntries = jarEntries(mainJar.get().asFile)
        requireEntry(mainEntries, "module-info.class", "Main JAR")
        requireEntry(mainEntries, "org/glavo/m3fx/controls/M3Button.class", "Main JAR")
        requireEntry(mainEntries, "org/glavo/m3fx/theme/M3Theme.class", "Main JAR")
        requireEntry(mainEntries, "org/glavo/m3fx/styles/base.css", "Main JAR")
        requireEntry(mainEntries, "org/glavo/m3fx/styles/fallback.css", "Main JAR")
        requireEntry(mainEntries, "org/glavo/m3fx/styles/controls/button.css", "Main JAR")
        requireEntry(mainEntries, "org/glavo/m3fx/styles/controls/text-field.css", "Main JAR")
        requireEntry(mainEntries, "org/glavo/m3fx/styles/controls/progress.css", "Main JAR")
        requireEntry(mainEntries, "org/glavo/m3fx/styles/controls/loading-indicator.css", "Main JAR")
        for (packagePath in exportedPackagePaths) {
            requireEntry(mainEntries, "$packagePath/package-info.class", "Main JAR")
        }
        val missingStylesheets = mainStylesheetResources.files
            .map { file -> sourceEntry(mainResourceDirectory, file) }
            .filter { entryName -> entryName !in mainEntries }
            .sorted()
        if (missingStylesheets.isNotEmpty()) {
            throw GradleException("Main JAR is missing stylesheet resources: ${missingStylesheets.take(10)}")
        }
        if (mainEntries.any { it.endsWith(".java") }) {
            throw GradleException("Main JAR must not contain Java source files.")
        }
        if (mainEntries.any { it.startsWith("javafx/") || it.startsWith("com/sun/javafx/") }) {
            throw GradleException("Main JAR must not bundle JavaFX classes.")
        }

        val sourceEntries = jarEntries(sourcesJar.get().asFile)
        requireEntry(sourceEntries, "module-info.java", "Sources JAR")
        requireEntry(sourceEntries, "org/glavo/m3fx/controls/M3Button.java", "Sources JAR")
        requireEntry(sourceEntries, "org/glavo/m3fx/controls/package-info.java", "Sources JAR")
        requireEntry(sourceEntries, "org/glavo/m3fx/tokens/M3TokenSet.java", "Sources JAR")
        val missingSources = mainJavaSources.files
            .map { file -> sourceEntry(mainSourceDirectory, file) }
            .filter { entryName -> entryName !in sourceEntries }
            .sorted()
        if (missingSources.isNotEmpty()) {
            throw GradleException("Sources JAR is missing Java source entries: ${missingSources.take(10)}")
        }
        if (sourceEntries.any { it.endsWith(".class") }) {
            throw GradleException("Sources JAR must not contain compiled class files.")
        }

        val javadocEntries = jarEntries(javadocJar.get().asFile)
        requireEntry(javadocEntries, "index.html", "Javadoc JAR")
        if (javadocEntries.any { it.endsWith(".java") || it.endsWith(".class") }) {
            throw GradleException("Javadoc JAR must contain generated documentation only.")
        }
    }
}

val verifyMavenPublicationLayout = tasks.register("verifyMavenPublicationLayout") {
    group = "verification"
    description = "Publishes M3FX to a build-local Maven repository and verifies the publication layout."

    dependsOn(
        verifyPublicationMetadata,
        verifyPublicationArtifacts,
        tasks.named("publishMavenPublicationToVerificationRepository")
    )

    val artifactDirectory = verificationMavenRepositoryDirectory.map {
        it.dir("${m3fxGroupId.replace('.', '/')}/$m3fxArtifactId/$m3fxVersion")
    }
    inputs.dir(artifactDirectory)

    doLast {
        val directory = artifactDirectory.get().asFile
        if (!directory.isDirectory) {
            throw GradleException("Verification Maven repository is missing artifact directory ${directory.absolutePath}")
        }

        val files = directory.listFiles()?.filter(File::isFile).orEmpty()
        val fileNames = files.map { file -> file.name }.toSet()

        val artifactVersionPattern =
            if (m3fxVersion.endsWith("-SNAPSHOT")) {
                Regex.escape(m3fxVersion.removeSuffix("-SNAPSHOT")) + "-\\d{8}\\.\\d{6}-\\d+"
            } else {
                Regex.escape(m3fxVersion)
            }

        fun requirePublishedArtifact(classifier: String?, extension: String) {
            val classifierPattern = classifier?.let { "-${Regex.escape(it)}" }.orEmpty()
            val pattern =
                Regex("^${Regex.escape(m3fxArtifactId)}-$artifactVersionPattern$classifierPattern\\.${Regex.escape(extension)}$")
            val matchingArtifacts = fileNames.filter { fileName -> pattern.matches(fileName) }
            if (matchingArtifacts.size != 1) {
                val artifactName = classifier?.let { "$it $extension" } ?: extension
                throw GradleException(
                    "Verification Maven repository must contain exactly one $artifactName artifact, "
                            + "but found ${matchingArtifacts.size}: $matchingArtifacts"
                )
            }
        }

        requirePublishedArtifact(null, "jar")
        requirePublishedArtifact("sources", "jar")
        requirePublishedArtifact("javadoc", "jar")
        requirePublishedArtifact(null, "pom")

        if (fileNames.any { it.endsWith(".module") }) {
            throw GradleException("Verification Maven repository must not publish Gradle module metadata.")
        }
        if (fileNames.any { it.contains("javafx", ignoreCase = true) }) {
            throw GradleException("Verification Maven repository must not publish JavaFX artifacts.")
        }
    }
}

val publicationConsumerProject = project(":demo")

publicationConsumerProject.repositories.exclusiveContent {
    forRepository {
        publicationConsumerProject.repositories.maven {
            name = "verificationConsumption"
            url = verificationMavenRepositoryDirectory.get().asFile.toURI()
        }
    }
    filter {
        includeModule(m3fxGroupId, m3fxArtifactId)
    }
}

val publishedRuntimeConfiguration = publicationConsumerProject.configurations.create("publishedM3fxRuntime") {
    isCanBeConsumed = false
    isCanBeResolved = true
    description = "Resolves the build-local Maven publication as a downstream runtime dependency."
    resolutionStrategy.useGlobalDependencySubstitutionRules = false
    resolutionStrategy.dependencySubstitution {
        substitute(project(":")).using(module(m3fxCoordinates))
    }
    attributes {
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
    }
}

publicationConsumerProject.dependencies.add(publishedRuntimeConfiguration.name, m3fxCoordinates)

val verifyMavenPublicationConsumption = tasks.register("verifyMavenPublicationConsumption") {
    group = "verification"
    description = "Resolves the build-local Maven publication as a downstream Gradle consumer."

    dependsOn(verifyMavenPublicationLayout)
    inputs.dir(verificationMavenRepositoryDirectory)

    doLast {
        val resolvedComponents = publishedRuntimeConfiguration.incoming.resolutionResult.allComponents
            .mapNotNull { component -> component.moduleVersion }
            .map { module -> "${module.group}:${module.name}" }
            .toSet()

        if (m3fxModuleId !in resolvedComponents) {
            throw GradleException("Published M3FX runtime dependency did not resolve from the verification repository.")
        }
        if ("org.glavo:MonetFX" !in resolvedComponents) {
            throw GradleException("Published M3FX runtime dependency did not expose MonetFX as a consumer dependency.")
        }

        val openjfxComponents = resolvedComponents
            .filter { component -> component.startsWith("org.openjfx:") }
            .sorted()
        if (openjfxComponents.isNotEmpty()) {
            throw GradleException("Published M3FX runtime dependency must not pull OpenJFX artifacts: $openjfxComponents")
        }

        val runtimeFiles = publishedRuntimeConfiguration.resolve()
        if (runtimeFiles.none { file -> file.name.matches(Regex("^${Regex.escape(m3fxArtifactId)}-.*\\.jar$")) }) {
            throw GradleException("Published M3FX runtime dependency did not resolve the main jar artifact.")
        }

    }
}

tasks.named("check") {
    dependsOn(
        verifyPublicationMetadata,
        verifyPublicationArtifacts,
        verifyMavenPublicationLayout,
        verifyMavenPublicationConsumption
    )
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

tasks.register("releaseCheck") {
    group = "verification"
    description = "Runs local release verification for the library publication, demo tests, and demo distribution."
    dependsOn(
        tasks.named("check"),
        project(":demo").tasks.named("test"),
        tasks.named("shadowDemoJar"),
        tasks.named("jlinkDemoRuntime")
    )
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
