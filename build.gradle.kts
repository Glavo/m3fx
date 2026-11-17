import java.lang.module.ModuleDescriptor
import java.lang.module.ModuleFinder
import org.glavo.m3fx.build.M3FXStandardDoclet
import org.gradle.api.component.AdhocComponentWithVariants
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test

plugins {
    id("java-library")
    id("java-test-fixtures")
    id("maven-publish")
    id("signing")
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("org.glavo.load-maven-publish-properties") version "0.1.0"
    id("org.glavo.gradle-wrapper-neo") version "0.4.0"
}

allprojects {
    group = "org.glavo"
    version = "1.0-SNAPSHOT"
}

description = "M3FX is a Material Design 3 component library for JavaFX applications."

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

val javafxJavadocElements = configurations.create("javafxJavadocElements") {
    isCanBeConsumed = false
    isCanBeResolved = true
    isTransitive = false
}

dependencies {
    addJavafxDependencies("compileOnly", javafxVersion)
    addJavafxDependencies(javafxJavadocElements.name, javafxVersion)
    api("org.glavo:MonetFX:$monetFxVersion")
    compileOnlyApi("org.jetbrains:annotations:26.1.0")

    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    addJavafxDependencies("testImplementation", javafxVersion)
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testFixturesImplementation(platform("org.junit:junit-bom:6.0.0"))
    addJavafxDependencies("testFixturesImplementation", javafxVersion)
    testFixturesImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.withType<JavaCompile> {
    options.release = 17
    options.encoding = "UTF-8"
}

allprojects {
    pluginManager.withPlugin("java") {
        val sourceSets = extensions.getByType(SourceSetContainer::class.java)
        val testSourceSet = sourceSets.named("test")

        tasks.named<Test>("test") {
            group = "verification"
            description = "Runs fast, deterministic Tier 1 tests."
            useJUnitPlatform {
                excludeTags("tier2", "tier3")
            }
        }

        tasks.register<Test>("testTier2") {
            group = "verification"
            description = "Runs Tier 2 rendering and window-integration tests."
            testClassesDirs = testSourceSet.get().output.classesDirs
            classpath = testSourceSet.get().runtimeClasspath
            useJUnitPlatform {
                includeTags("tier2")
            }
            shouldRunAfter(tasks.named("test"))
        }

        tasks.register<Test>("testTier3") {
            group = "verification"
            description = "Runs Tier 3 real-window, animation-frame, and visual-matrix tests."
            testClassesDirs = testSourceSet.get().output.classesDirs
            classpath = testSourceSet.get().runtimeClasspath
            useJUnitPlatform {
                includeTags("tier3")
            }
            shouldRunAfter(tasks.named("testTier2"))
        }

        tasks.withType<Test>().configureEach {
            maxHeapSize = providers.gradleProperty("m3fx.test.maxHeapSize").orElse("1g").get()
        }
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}

components.named<AdhocComponentWithVariants>("java") {
    withVariantsFromConfiguration(configurations["testFixturesApiElements"]) {
        skip()
    }
    withVariantsFromConfiguration(configurations["testFixturesRuntimeElements"]) {
        skip()
    }
}

val monetFxJavadocLinkDirectory =
    layout.buildDirectory.dir("generated/javadoc-links/monetfx-$monetFxVersion")
val javaSEJavadocLinkDirectory =
    layout.buildDirectory.dir("generated/javadoc-links/java-se-17")
val javafxJavadocLinkDirectory =
    layout.buildDirectory.dir("generated/javadoc-links/javafx-$javafxVersion")

fun writeJavadocElementList(outputDirectory: File, descriptors: Iterable<ModuleDescriptor>) {
    val content = buildString {
        descriptors
            .sortedBy(ModuleDescriptor::name)
            .forEach { descriptor ->
                val exportedPackages = descriptor.exports()
                    .asSequence()
                    .filterNot(ModuleDescriptor.Exports::isQualified)
                    .map(ModuleDescriptor.Exports::source)
                    .sorted()
                    .toList()
                if (exportedPackages.isNotEmpty()) {
                    appendLine("module:${descriptor.name()}")
                    exportedPackages.forEach(::appendLine)
                }
            }
    }
    val elementList = outputDirectory.resolve("element-list")
    elementList.parentFile.mkdirs()
    elementList.writeText(content, Charsets.UTF_8)
}

val generateJavaSEJavadocElementList = tasks.register("generateJavaSEJavadocElementList") {
    group = "documentation"
    description = "Generates the offline Javadoc element list for Java SE."

    val outputDirectory = javaSEJavadocLinkDirectory
    inputs.property("javaRuntimeVersion", System.getProperty("java.runtime.version", "unknown"))
    outputs.dir(outputDirectory)

    doLast {
        val descriptors = ModuleLayer.boot()
            .modules()
            .asSequence()
            .map(Module::getDescriptor)
            .filter { descriptor ->
                descriptor.name().startsWith("java.") || descriptor.name().startsWith("jdk.")
            }
            .toList()
        writeJavadocElementList(outputDirectory.get().asFile, descriptors)
    }
}

val generateJavafxJavadocElementList = tasks.register("generateJavafxJavadocElementList") {
    group = "documentation"
    description = "Generates the offline Javadoc element list for JavaFX."

    val outputDirectory = javafxJavadocLinkDirectory
    inputs.files(javafxJavadocElements)
    outputs.dir(outputDirectory)

    doLast {
        val modulePaths = javafxJavadocElements.files
            .map(File::toPath)
            .toTypedArray()
        val descriptors = ModuleFinder.of(*modulePaths)
            .findAll()
            .map { reference -> reference.descriptor() }
        writeJavadocElementList(outputDirectory.get().asFile, descriptors)
    }
}

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
    dependsOn(
        generateJavaSEJavadocElementList,
        generateJavafxJavadocElementList,
        generateMonetFxJavadocElementList
    )
    inputs.files(
        javaSEJavadocLinkDirectory,
        javafxJavadocLinkDirectory,
        monetFxJavadocLinkDirectory
    )
    setSource(fileTree(mainSourceDirectory) {
        include("org/glavo/m3fx/controls/package-info.java")
    })
    // Javadoc discovers these files through sourcepath rather than the task's explicit source collection.
    inputs.files(fileTree(mainSourceDirectory) {
        include("**/*.java")
    }).withPropertyName("documentationSources")
        .withPathSensitivity(PathSensitivity.RELATIVE)

    (options as StandardJavadocDocletOptions).also {
        it.doclet = M3FXStandardDoclet::class.java.name
        it.docletpath = listOf(file(M3FXStandardDoclet::class.java.protectionDomain.codeSource.location.toURI()))
        it.jFlags!!.addAll(
            listOf(
                "-Duser.language=en",
                "-Duser.country=",
                "-Duser.variant=",
                "--add-exports=jdk.javadoc/jdk.javadoc.internal.tool=ALL-UNNAMED",
            )
        )

        it.encoding("UTF-8")
        it.addStringOption("sourcepath", mainSourceDirectory.absolutePath)
        it.addStringOption("subpackages", "org.glavo.m3fx")
        it.linksOffline(
            "https://docs.oracle.com/en/java/javase/17/docs/api/",
            javaSEJavadocLinkDirectory.get().asFile.absolutePath
        )
        it.linksOffline(
            "https://openjfx.io/javadoc/$javafxVersion/",
            javafxJavadocLinkDirectory.get().asFile.absolutePath
        )
        it.linksOffline(
            "https://javadoc.io/doc/org.glavo/MonetFX/$monetFxVersion/",
            monetFxJavadocLinkDirectory.get().asFile.absolutePath
        )
        it.addBooleanOption("html5", true)
        it.addBooleanOption("Werror", true)
        it.addStringOption("Xmaxwarns", "10000")
        it.addStringOption("-show-packages", "exported")
        it.addStringOption("exclude", "org.glavo.m3fx.internal:org.glavo.m3fx.internal.animation:org.glavo.m3fx.internal.shape:org.glavo.m3fx.internal.theme:org.glavo.m3fx.internal.tokens:org.glavo.m3fx.skins")
        it.addStringOption("Xdoclint:all,-missing", "-quiet")

        it.tags!!.addAll(
            listOf(
                "defaultValue:a:Default Value:",
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

val signingKeyId = rootProject.ext.properties["signing.keyId"]
    ?.toString()
    ?.takeIf { it.isNotBlank() }
val signingKey = rootProject.ext.properties["signing.key"]
    ?.toString()
    ?.takeIf { it.isNotBlank() }
val signingPassword = rootProject.ext.properties["signing.password"]
    ?.toString()
    ?.takeIf { it.isNotBlank() }

if (System.getenv("JITPACK").isNullOrBlank() && signingKey != null) {
    signing {
        useInMemoryPgpKeys(
            signingKeyId,
            signingKey,
            signingPassword,
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
