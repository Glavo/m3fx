// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies source-level packaging and distribution configuration invariants.
@NotNullByDefault
final class PackagingConfigurationTest {
    /// The root Gradle build script exposing public distribution entry points.
    private static final Path ROOT_BUILD_SCRIPT = Path.of("build.gradle.kts");

    /// The demo Gradle build script owning shadow jar and jlink packaging tasks.
    private static final Path DEMO_BUILD_SCRIPT = Path.of("demo", "build.gradle.kts");

    /// The demo application source that loads packaged runtime resources.
    private static final Path DEMO_APP_SOURCE =
            Path.of("demo", "src", "main", "java", "org", "glavo", "m3fx", "demo", "M3FXDemoApp.java");

    /// The library Java module descriptor.
    private static final Path MODULE_INFO = Path.of("src", "main", "java", "module-info.java");

    /// The GitHub Actions workflow that publishes the demo shadow jar artifact.
    private static final Path DEMO_SHADOW_WORKFLOW =
            Path.of(".github", "workflows", "demo-shadow-jar.yml");

    /// The default demo font package URL used when no override property is provided.
    private static final String DEMO_FONT_PACKAGE_URL =
            "https://registry.npmmirror.com/@fontpkg/alibaba-puhuiti-3-0/-/alibaba-puhuiti-3-0-0.0.0.tgz";

    /// The exact font file extracted from the default demo font package.
    private static final String DEMO_FONT_FILE_NAME = "AlibabaPuHuiTi-3-65-Medium.ttf";

    /// The resource path used inside demo runtime artifacts for the default font.
    private static final String DEMO_FONT_RESOURCE_PATH =
            "org/glavo/m3fx/demo/fonts/AlibabaPuHuiTi-3-65-Medium.ttf";

    /// Root distribution aliases expected to remain available to users and CI.
    private static final @Unmodifiable List<String> ROOT_DISTRIBUTION_TASKS = List.of(
            "shadowDemoJar",
            "jlinkDemoRuntime",
            "jlinkDemoWindowsRuntime",
            "jlinkDemoWindowsX64Runtime",
            "jlinkDemoWindowsAarch64Runtime",
            "jlinkDemoLinuxRuntime",
            "jlinkDemoLinuxX64Runtime",
            "jlinkDemoLinuxAarch64Runtime",
            "jlinkDemoMacosRuntime",
            "jlinkDemoMacosX64Runtime",
            "jlinkDemoMacosAarch64Runtime",
            "jlinkDemoAllPlatformRuntimes",
            "jlinkDemoAllPlatformArchitectureRuntimes"
    );

    /// Verifies that root packaging aliases remain present.
    @Test
    void rootDistributionAliasesRemainAvailable() throws IOException {
        String buildScript = Files.readString(ROOT_BUILD_SCRIPT);
        List<String> missingTasks = ROOT_DISTRIBUTION_TASKS.stream()
                .filter(taskName -> !buildScript.contains("tasks.register(\"" + taskName + "\")"))
                .toList();

        assertTrue(missingTasks.isEmpty(), () -> "Missing root distribution task aliases: " + missingTasks);
    }

    /// Verifies that local release checks aggregate publication and demo distribution validation.
    @Test
    void localReleaseCheckAggregatesPublicationAndDemoDistributionGates() throws IOException {
        String buildScript = Files.readString(ROOT_BUILD_SCRIPT);

        assertTrue(buildScript.contains("tasks.register(\"releaseCheck\")"),
                "root build must expose a local release verification task");
        assertTrue(buildScript.contains("Runs local release verification for the library publication and demo distribution."),
                "releaseCheck should describe the local release gates it executes");
        assertTrue(buildScript.contains("tasks.named(\"check\")"),
                "releaseCheck must run the ordinary verification lifecycle");
        assertTrue(buildScript.contains("tasks.named(\"shadowDemoJar\")"),
                "releaseCheck must verify the executable demo shadow jar");
        assertTrue(buildScript.contains("tasks.named(\"jlinkDemoRuntime\")"),
                "releaseCheck must verify the default demo jlink runtime image");
    }

    /// Verifies that the library keeps JavaFX off the published Gradle API dependency surface.
    @Test
    void libraryJavafxDependenciesRemainCompileOnly() throws IOException {
        String buildScript = Files.readString(ROOT_BUILD_SCRIPT);

        assertTrue(buildScript.contains("addJavafxDependencies(\"compileOnly\", javafxVersion)"),
                "library JavaFX dependencies must remain compile-only");
        assertTrue(buildScript.contains("addJavafxDependencies(\"testImplementation\", javafxVersion)"),
                "tests should provide JavaFX explicitly instead of relying on published runtime dependencies");
        assertFalse(buildScript.contains("addJavafxDependencies(\"api\""),
                "library JavaFX dependencies must not be api dependencies");
        assertFalse(buildScript.contains("addJavafxDependencies(\"implementation\""),
                "library JavaFX dependencies must not be implementation dependencies");
        assertFalse(buildScript.contains("api(\"org.openjfx"),
                "library publication must not expose direct JavaFX api dependencies");
        assertFalse(buildScript.contains("implementation(\"org.openjfx"),
                "library publication must not expose direct JavaFX implementation dependencies");
    }

    /// Verifies that generated API documentation cannot ship with Javadoc warnings.
    @Test
    void javadocGenerationTreatsWarningsAsErrors() throws IOException {
        String buildScript = Files.readString(ROOT_BUILD_SCRIPT);

        assertTrue(buildScript.contains("it.addBooleanOption(\"Werror\", true)"),
                "Javadoc generation must fail when API documentation emits warnings");
        assertTrue(buildScript.contains("it.addStringOption(\"Xdoclint:none\", \"-quiet\")"),
                "Javadoc generation should keep style-only doclint noise disabled without ignoring real warnings");
        assertTrue(buildScript.contains("it.addStringOption(\"-show-packages\", \"exported\")"),
                "Javadoc generation must document only exported JPMS packages");
    }

    /// Verifies that JPMS JavaFX readability remains separate from published artifact dependencies.
    @Test
    void moduleDescriptorKeepsJavafxReadabilityExplicit() throws IOException {
        String moduleInfo = Files.readString(MODULE_INFO);

        assertTrue(moduleInfo.contains("requires transitive javafx.controls;"),
                "JPMS consumers need transitive readability for public JavaFX control types");
        assertTrue(moduleInfo.contains("requires transitive javafx.graphics;"),
                "JPMS consumers need transitive readability for public JavaFX graphics types");
    }

    /// Verifies that generated Maven publication metadata is checked before `check` succeeds.
    @Test
    void generatedMavenPublicationMetadataIsVerified() throws IOException {
        String buildScript = Files.readString(ROOT_BUILD_SCRIPT);

        assertTrue(buildScript.contains("val verifyPublicationMetadata = tasks.register(\"verifyPublicationMetadata\")"),
                "root build must keep a publication metadata verification task");
        assertTrue(buildScript.contains("dependsOn(tasks.named(\"generatePomFileForMavenPublication\"))"),
                "publication metadata verification must inspect the generated Maven POM");
        assertTrue(buildScript.contains("buildDirectory.file(\"publications/maven/pom-default.xml\")"),
                "publication metadata verification must read the generated Maven POM location");
        assertTrue(buildScript.contains("The library publication must not expose JavaFX dependencies."),
                "publication metadata verification must reject JavaFX dependencies in generated metadata");
        assertTrue(buildScript.contains("tasks.named(\"check\")"),
                "`check` must include publication metadata verification");
        assertTrue(buildScript.contains("verifyPublicationMetadata,"),
                "`check` must depend on publication metadata verification");
    }

    /// Verifies that generated publication JAR artifacts are checked before `check` succeeds.
    @Test
    void generatedPublicationArtifactsAreVerified() throws IOException {
        String buildScript = Files.readString(ROOT_BUILD_SCRIPT);

        assertTrue(buildScript.contains("val verifyPublicationArtifacts = tasks.register(\"verifyPublicationArtifacts\")"),
                "root build must keep a publication artifact verification task");
        assertTrue(buildScript.contains("tasks.named<org.gradle.jvm.tasks.Jar>(\"jar\")"),
                "publication artifact verification must inspect the main jar");
        assertTrue(buildScript.contains("tasks.named<org.gradle.jvm.tasks.Jar>(\"sourcesJar\")"),
                "publication artifact verification must inspect the sources jar");
        assertTrue(buildScript.contains("tasks.named<org.gradle.jvm.tasks.Jar>(\"javadocJar\")"),
                "publication artifact verification must inspect the javadoc jar");
        assertTrue(buildScript.contains("include(\"org/glavo/m3fx/styles/**/*.css\")"),
                "publication artifact verification must derive stylesheet expectations from source resources");
        assertTrue(buildScript.contains("include(\"**/*.java\")"),
                "publication artifact verification must derive source expectations from main Java sources");
        assertTrue(buildScript.contains("fun exportedPackagePathsFromModuleInfo(moduleInfoFile: File): List<String>"),
                "publication artifact verification must derive exported package paths from module-info.java");
        assertTrue(buildScript.contains("exportedPackagePathsFromModuleInfo(mainModuleInfo)"),
                "publication artifact verification must use the module-derived exported package paths");
        assertTrue(buildScript.contains("Main module descriptor should export at least one API package."),
                "publication artifact verification must reject an empty exported package set");
        assertFalse(buildScript.contains("val exportedPackagePaths = listOf("),
                "publication artifact verification must not maintain a hard-coded exported package list");
        assertTrue(buildScript.contains("org/glavo/m3fx/styles/controls/button.css"),
                "publication artifact verification must check bundled control stylesheets");
        assertTrue(buildScript.contains("Main JAR is missing stylesheet resources:"),
                "publication artifact verification must reject missing bundled stylesheet resources");
        assertTrue(buildScript.contains("Sources JAR is missing Java source entries:"),
                "publication artifact verification must reject missing source jar entries");
        assertTrue(buildScript.contains("Main JAR must not bundle JavaFX classes."),
                "publication artifact verification must reject bundled JavaFX implementation classes");
        assertTrue(buildScript.contains("moduleNameFromModuleInfo(mainModuleInfo)"),
                "publication artifact verification must inspect module-scoped Javadoc entries");
        assertTrue(buildScript.contains("Javadoc JAR must only document exported M3FX API packages:"),
                "publication artifact verification must reject non-exported package documentation");
        assertTrue(buildScript.contains("forbiddenJavadocContentFragments"),
                "publication artifact verification must scan generated Javadoc content for implementation leaks");
        assertTrue(buildScript.contains("org.glavo.m3fx.internal"),
                "publication artifact verification must reject internal package references in generated Javadocs");
        assertTrue(buildScript.contains("org.glavo.m3fx.skins"),
                "publication artifact verification must reject skin package references in generated Javadocs");
        assertTrue(buildScript.contains("Javadoc JAR must not mention non-exported implementation packages or helper types:"),
                "publication artifact verification must reject implementation details in generated Javadoc content");
        assertTrue(buildScript.contains("verifyPublicationArtifacts,"),
                "`check` must depend on both publication metadata and artifact verification");
    }

    /// Verifies that the Maven publication is published to a build-local repository before `check` succeeds.
    @Test
    void generatedMavenPublicationLayoutIsVerified() throws IOException {
        String buildScript = Files.readString(ROOT_BUILD_SCRIPT);

        assertTrue(buildScript.contains("publishing.repositories.maven"),
                "root build must configure a build-local verification Maven repository");
        assertTrue(buildScript.contains("name = \"verification\""),
                "verification Maven repository must have a stable Gradle repository name");
        assertTrue(buildScript.contains("verification-maven-repository"),
                "verification Maven repository must stay inside the build directory");
        assertTrue(buildScript.contains("cleanVerificationMavenRepository"),
                "verification Maven repository must be cleaned before publishing");
        assertTrue(buildScript.contains("tasks.register<Delete>(\"cleanVerificationMavenRepository\")"),
                "verification Maven repository cleaning should use a Gradle Delete task");
        assertTrue(buildScript.contains("tasks.named(\"publishMavenPublicationToVerificationRepository\")"),
                "publication layout verification must configure the real maven-publish task");
        assertTrue(buildScript.contains("dependsOn(cleanVerificationMavenRepository)"),
                "the verification Maven publish task must depend on repository cleanup");
        assertTrue(buildScript.contains("val verifyMavenPublicationLayout = tasks.register(\"verifyMavenPublicationLayout\")"),
                "root build must keep a Maven publication layout verification task");
        assertTrue(buildScript.contains("tasks.named(\"publishMavenPublicationToVerificationRepository\")"),
                "publication layout verification must execute the real maven-publish task");
        assertTrue(buildScript.contains("matchingArtifacts.size != 1"),
                "publication layout verification must require exactly one published artifact per required classifier");
        assertTrue(buildScript.contains("m3fxVersion.endsWith(\"-SNAPSHOT\")"),
                "publication layout verification must support timestamped Maven snapshot artifacts");
        assertTrue(buildScript.contains("requirePublishedArtifact(null, \"jar\")"),
                "publication layout verification must require the main jar");
        assertTrue(buildScript.contains("requirePublishedArtifact(\"sources\", \"jar\")"),
                "publication layout verification must require the sources jar");
        assertTrue(buildScript.contains("requirePublishedArtifact(\"javadoc\", \"jar\")"),
                "publication layout verification must require the javadoc jar");
        assertTrue(buildScript.contains("requirePublishedArtifact(null, \"pom\")"),
                "publication layout verification must require the Maven POM");
        assertTrue(buildScript.contains("Verification Maven repository must not publish Gradle module metadata."),
                "publication layout verification must reject disabled Gradle module metadata if it reappears");
        assertTrue(buildScript.contains("Verification Maven repository must not publish JavaFX artifacts."),
                "publication layout verification must reject accidental JavaFX artifacts");
        assertTrue(buildScript.contains("verifyMavenPublicationLayout,"),
                "`check` must depend on metadata, artifact, and publication layout verification");
    }

    /// Verifies that the build-local Maven publication is resolved like a downstream Gradle dependency.
    @Test
    void generatedMavenPublicationConsumptionIsVerified() throws IOException {
        String buildScript = Files.readString(ROOT_BUILD_SCRIPT);

        assertTrue(buildScript.contains("repositories.exclusiveContent"),
                "publication consumption verification must resolve M3FX only from the build-local repository");
        assertTrue(buildScript.contains("name = \"verificationConsumption\""),
                "publication consumption repository must have a stable Gradle repository name");
        assertTrue(buildScript.contains("includeModule(m3fxGroupId, m3fxArtifactId)"),
                "publication consumption repository must be restricted to the M3FX module");
        assertTrue(buildScript.contains("val publicationConsumerProject = project(\":demo\")"),
                "publication consumption verification must resolve from a downstream project context");
        assertTrue(buildScript.contains(
                        "val publishedRuntimeConfiguration = publicationConsumerProject.configurations.create(\"publishedM3fxRuntime\")"),
                "publication consumption verification must resolve the runtime dependency from the consumer project");
        assertTrue(buildScript.contains("resolutionStrategy.useGlobalDependencySubstitutionRules = false"),
                "publication consumption verification must resolve external Maven artifacts instead of the current project");
        assertTrue(buildScript.contains("substitute(project(\":\")).using(module(m3fxCoordinates))"),
                "publication consumption verification must explicitly redirect current-project substitution back to the module");
        assertTrue(buildScript.contains(
                        "publicationConsumerProject.dependencies.add(publishedRuntimeConfiguration.name, m3fxCoordinates)"),
                "publication consumption verification must add the M3FX module dependency from the consumer project");
        assertTrue(buildScript.contains("val verifyMavenPublicationConsumption = tasks.register(\"verifyMavenPublicationConsumption\")"),
                "root build must keep a Maven publication consumption verification task");
        assertTrue(buildScript.contains("dependsOn(verifyMavenPublicationLayout)"),
                "publication consumption verification must depend on the real build-local publication");
        assertTrue(buildScript.contains("\"org.glavo:MonetFX\""),
                "publication consumption verification must require MonetFX as a resolved consumer dependency");
        assertTrue(buildScript.contains("component.startsWith(\"org.openjfx:\")"),
                "publication consumption verification must reject transitive OpenJFX artifacts");
        assertTrue(buildScript.contains("verifyMavenPublicationConsumption"),
                "`check` must depend on publication consumption verification");
    }

    /// Verifies that the published project identity does not contain copied metadata from another project.
    @Test
    void mavenPublicationUsesM3fxProjectIdentity() throws IOException {
        String buildScript = Files.readString(ROOT_BUILD_SCRIPT);

        assertTrue(buildScript.contains(
                        "description = \"M3FX is a Material Design 3 component library for JavaFX applications.\""),
                "project description must identify M3FX");
        assertTrue(buildScript.contains("name.set(\"M3FX\")"),
                "Maven POM name must use the product display name");
        assertTrue(buildScript.contains("url.set(\"https://github.com/Glavo/m3fx\")"),
                "Maven POM URL and SCM URL must point to M3FX");
        assertTrue(buildScript.contains("connection.set(\"scm:git:https://github.com/Glavo/m3fx.git\")"),
                "Maven POM SCM connection must point to M3FX");
        assertTrue(buildScript.contains("developerConnection.set(\"scm:git:ssh://git@github.com/Glavo/m3fx.git\")"),
                "Maven POM developer SCM connection must point to M3FX");
        assertFalse(buildScript.contains("description = \"MonetFX is a JavaFX library"),
                "project description must not contain stale MonetFX metadata");
        assertFalse(buildScript.contains("url.set(\"https://github.com/Glavo/weburl-java\")"),
                "project metadata must not contain stale weburl-java URLs");
    }

    /// Verifies that static annotation dependencies are not published as required runtime dependencies.
    @Test
    void annotationsPublicationDependencyIsOptional() throws IOException {
        String buildScript = Files.readString(ROOT_BUILD_SCRIPT);

        assertTrue(buildScript.contains("compileOnlyApi(\"org.jetbrains:annotations:26.1.0\")"),
                "JetBrains annotations remain visible to source consumers without becoming runtime code");
        assertTrue(buildScript.contains("dependency.childText(\"groupId\") == \"org.jetbrains\""),
                "POM customization must locate the JetBrains annotations dependency");
        assertTrue(buildScript.contains("dependency.childText(\"artifactId\") == \"annotations\""),
                "POM customization must target only the JetBrains annotations artifact");
        assertTrue(buildScript.contains("dependency.appendNode(\"optional\", \"true\")"),
                "POM customization must publish annotations as optional");
        assertTrue(buildScript.contains("JetBrains annotations must be published as an optional compile dependency."),
                "publication metadata verification must reject non-optional annotations metadata");
    }

    /// Verifies that cross-architecture jlink tasks do not execute a target-architecture jlink binary.
    @Test
    void jlinkExecutableSelectionChecksTargetArchitecture() throws IOException {
        String buildScript = Files.readString(DEMO_BUILD_SCRIPT);

        assertTrue(buildScript.contains("targetArch.get()"),
                "jlink runtime tasks must pass the target architecture into executable selection");
        assertTrue(buildScript.contains("hostLibericaExtractDirectory.get().asFile"),
                "jlink runtime tasks must pass the downloaded host jlink directory into executable selection");
        assertTrue(buildScript.contains("targetArch: String"),
                "jlinkExecutable must receive the target architecture");
        assertTrue(buildScript.contains("targetOs == detectLibericaOs() && targetArch == detectLibericaArch()"),
                "downloaded target jlink must only be used when both OS and architecture match the host");
        assertTrue(buildScript.contains("Cross-platform or cross-architecture jlink requires a host jlink"),
                "cross-architecture failures should explain the host jlink requirement");
    }

    /// Verifies that cross-target jlink tasks can obtain a matching host executable without changing Gradle JVMs.
    @Test
    void crossTargetJlinkDownloadsHostExecutableWhenNeeded() throws IOException {
        String buildScript = Files.readString(DEMO_BUILD_SCRIPT);

        assertTrue(buildScript.contains("hostJlinkNeeded"),
                "jlink tasks must detect when the host jlink executable is needed");
        assertTrue(buildScript.contains("targetOs.get() != detectLibericaOs() || targetArch.get() != detectLibericaArch()"),
                "host jlink should be needed for cross-platform or cross-architecture targets");
        assertTrue(buildScript.contains("Runtime.version().feature().toString() != javaFeature.get()"),
                "host jlink download should be limited to Java feature mismatches");
        assertTrue(buildScript.contains("Downloads a host BellSoft LibericaJDK Full archive for the jlink executable."),
                "cross-target tasks must be able to download a host JDK for the jlink executable");
        assertTrue(buildScript.contains("\"host-jlink-$taskName-"),
                "each jlink task needs an isolated host-jlink output to avoid Gradle implicit dependency conflicts");
        assertTrue(buildScript.contains("findJlinkExecutable(hostJlinkDirectory, detectLibericaOs())"),
                "jlinkExecutable must use the downloaded host jlink when the Gradle JVM version does not match");
    }

    /// Verifies that jlink tasks reject incomplete runtime images before they are treated as release artifacts.
    @Test
    void generatedJlinkRuntimeImageStructureIsVerified() throws IOException {
        String buildScript = Files.readString(DEMO_BUILD_SCRIPT);

        assertTrue(buildScript.contains("verifyJlinkRuntimeImage(imageDirectory, targetOs.get())"),
                "jlink tasks must verify the generated runtime image before succeeding");
        assertTrue(buildScript.contains("fun verifyJlinkRuntimeImage(imageDirectory: File, targetOs: String)"),
                "demo build must keep a dedicated jlink runtime image verifier");
        assertTrue(buildScript.contains("requireJlinkRuntimeFile(imageDirectory, \"release\")"),
                "jlink runtime verification must require the release metadata file");
        assertTrue(buildScript.contains("requireJlinkRuntimeFile(imageDirectory, \"lib/modules\")"),
                "jlink runtime verification must require the linked module image");
        assertTrue(buildScript.contains("requireJlinkRuntimeFile(imageDirectory, \"lib/javafx.properties\")"),
                "jlink runtime verification must require JavaFX runtime metadata");
        assertTrue(buildScript.contains("requireJlinkRuntimeFile(imageDirectory, \"legal/javafx.controls/LICENSE\")"),
                "jlink runtime verification must require JavaFX legal metadata");
        assertTrue(buildScript.contains("\"org.glavo.m3fx.demo\""),
                "jlink runtime verification must require the demo module in release metadata");
        assertTrue(buildScript.contains("requireJlinkRuntimeFile(imageDirectory, \"bin/m3fx-demo.bat\")"),
                "Windows jlink runtime verification must require the batch launcher");
    }

    /// Verifies that BellSoft API architecture names stay separate from M3FX task architecture names.
    @Test
    void libericaDownloadQueryMapsAarch64ToBellsoftArm() throws IOException {
        String buildScript = Files.readString(DEMO_BUILD_SCRIPT);

        assertTrue(buildScript.contains("\"arch\" to libericaApiArch(targetArch)"),
                "Liberica release queries must map canonical task architectures to BellSoft API names");
        assertTrue(buildScript.contains("fun libericaApiArch(architecture: String): String"),
                "demo build must keep a dedicated BellSoft API architecture mapping helper");
        assertTrue(buildScript.contains("\"aarch64\" -> \"arm\""),
                "BellSoft API expects arm for AArch64 downloads");
    }

    /// Verifies that Liberica downloads use package types that exist for each target OS.
    @Test
    void libericaDownloadUsesTargetOsPackageType() throws IOException {
        String buildScript = Files.readString(DEMO_BUILD_SCRIPT);

        assertTrue(buildScript.contains("\"package-type\" to libericaPackageType(targetOs)"),
                "Liberica release queries must choose package type from target OS");
        assertTrue(buildScript.contains("fun libericaPackageType(targetOs: String): String"),
                "demo build must keep package-type mapping separate from jlink task naming");
        assertTrue(buildScript.contains("\"linux\" -> \"tar.gz\""),
                "BellSoft Linux Full JDK downloads are tar.gz archives");
        assertTrue(buildScript.contains("else -> \"zip\""),
                "BellSoft Windows and macOS Full JDK downloads should stay on zip archives");
        assertTrue(buildScript.contains("tarTree(resources.gzip(archive))"),
                "Linux Liberica tar.gz archives must be extracted with tarTree and gzip");
    }

    /// Verifies that the demo shadow jar remains executable without bundling JavaFX modules.
    @Test
    void demoShadowJarVerificationRejectsBundledJavafx() throws IOException {
        String buildScript = Files.readString(DEMO_BUILD_SCRIPT);

        assertTrue(buildScript.contains("tasks.register(\"verifyShadowJar\")"),
                "demo build must keep the shadow jar verification task");
        assertTrue(buildScript.contains("!it.name.startsWith(\"javafx-\")"),
                "demo shadow jar runtime classpath must exclude JavaFX artifacts");
        assertTrue(buildScript.contains("The demo shadow JAR must not bundle JavaFX entries"),
                "demo shadow jar verification must reject bundled JavaFX entries");
        assertTrue(buildScript.contains("Main-Class"),
                "demo shadow jar verification must check executable manifest metadata");
        assertTrue(buildScript.contains(DEMO_FONT_FILE_NAME),
                "demo shadow jar verification must check the bundled default demo font");
        assertTrue(buildScript.contains("val fontEntry = zip.getEntry(demoFontResourcePath)"),
                "demo shadow jar verification must read the packaged demo font entry");
        assertTrue(buildScript.contains("fontEntry.size <= 0L"),
                "demo shadow jar verification must reject empty packaged demo font entries");
    }

    /// Verifies that the demo default font is downloaded, packaged, and loaded at runtime.
    @Test
    void demoDefaultFontIsDownloadedPackagedAndLoaded() throws IOException {
        String buildScript = Files.readString(DEMO_BUILD_SCRIPT);
        String demoApp = Files.readString(DEMO_APP_SOURCE);

        assertTrue(buildScript.contains("m3fx.demo.fontPackageUrl"),
                "demo font package URL must remain overridable for release builds and mirrors");
        assertTrue(buildScript.contains(DEMO_FONT_PACKAGE_URL),
                "demo build must keep the requested default Alibaba PuHuiTi npm mirror URL");
        assertTrue(buildScript.contains("val demoFontFileName = \"" + DEMO_FONT_FILE_NAME + "\""),
                "demo build must extract the exact requested font file");
        assertTrue(buildScript.contains("val demoFontResourcePath = \"org/glavo/m3fx/demo/fonts/$demoFontFileName\""),
                "demo build must package the font at the stable demo resource path");
        assertTrue(buildScript.contains("resources.srcDir(generatedDemoFontResources)"),
                "generated demo font resources must be included in main resources");
        assertTrue(buildScript.contains("tasks.processResources"),
                "processResources must depend on generated demo font resources");
        assertTrue(buildScript.contains("dependsOn(extractDemoFont)"),
                "demo resource processing must extract the bundled font first");
        assertTrue(buildScript.contains("from(tarTree(resources.gzip(demoFontArchive)))"),
                "demo font extraction must read the tgz package as a gzipped tar archive");
        assertTrue(buildScript.contains("include(\"**/$demoFontFileName\")"),
                "demo font extraction must select the exact requested TTF file from the package");
        assertTrue(buildScript.contains("path = demoFontResourcePath"),
                "demo font extraction must relocate the font to the runtime resource path");
        assertTrue(demoApp.contains("\"/" + DEMO_FONT_RESOURCE_PATH + "\""),
                "demo application must load the same packaged font resource");
        assertTrue(demoApp.contains("Font.loadFont(fontUrl.toExternalForm(), DEMO_FONT_LOAD_SIZE)"),
                "demo application must register the packaged font with JavaFX");
        assertTrue(demoApp.contains("-fx-font-family: "),
                "demo application must apply the loaded font family to the root node");
    }

    /// Verifies that CI uploads the built demo shadow jar without repackaging it.
    @Test
    void githubWorkflowUploadsUnarchivedDemoShadowJar() throws IOException {
        String workflow = Files.readString(DEMO_SHADOW_WORKFLOW);

        assertTrue(workflow.contains("xvfb-run -a ./gradlew -g .gradle-user-home releaseCheck --no-daemon"),
                "workflow must run the verified root releaseCheck task under Xvfb for JavaFX tests");
        assertTrue(workflow.contains("name: m3fx-visual-snapshots"),
                "workflow must upload visual snapshot artifacts for review");
        assertTrue(workflow.contains("build/reports/**/*.png"),
                "workflow must upload core visual snapshot PNGs");
        assertTrue(workflow.contains("demo/build/reports/**/*.png"),
                "workflow must upload demo visual snapshot PNGs");
        assertTrue(workflow.contains("name: m3fx-test-reports"),
                "workflow must upload Gradle test report artifacts for diagnostics");
        assertTrue(workflow.contains("build/reports/tests/"),
                "workflow must upload core HTML test reports");
        assertTrue(workflow.contains("demo/build/reports/tests/"),
                "workflow must upload demo HTML test reports");
        assertTrue(workflow.contains("build/test-results/test/"),
                "workflow must upload core XML test results");
        assertTrue(workflow.contains("demo/build/test-results/test/"),
                "workflow must upload demo XML test results");
        assertTrue(workflow.contains("uses: actions/upload-artifact@v7"),
                "workflow must use the requested upload-artifact version");
        assertTrue(workflow.contains("path: demo/build/libs/m3fx-demo-*-shadow.jar"),
                "workflow must upload the demo shadow jar artifact");
        assertTrue(workflow.contains("archive: false"),
                "workflow must upload the artifact without creating an additional archive");
    }
}
