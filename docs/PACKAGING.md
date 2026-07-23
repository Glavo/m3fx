# M3FX Packaging Guide

## Library Usage

M3FX is a JavaFX library. Applications own the JavaFX runtime and should provide JavaFX modules themselves.

The library publishes JavaFX as a compile-only dependency so downstream applications can choose their own JavaFX version, platform classifier, module path, and runtime-image strategy.

The JPMS module descriptor declares `requires transitive javafx.controls` and `requires transitive javafx.graphics` because the public M3FX API exposes JavaFX control and graphics types. This module readability declaration does not publish OpenJFX artifacts as Maven dependencies; applications still own the OpenJFX artifact coordinates and native platform classifiers.

Typical application dependencies:

```kotlin
dependencies {
    implementation("org.glavo:m3fx:1.0-SNAPSHOT")
    implementation("org.openjfx:javafx-base:21:win")
    implementation("org.openjfx:javafx-graphics:21:win")
    implementation("org.openjfx:javafx-controls:21:win")
}
```

Use the platform classifier that matches the target runtime: `win`, `linux`, or `mac`.

## Demo Shadow Jar

The demo shadow jar is intended for quick inspection of the demo application. It packages the demo classes, M3FX, and non-JavaFX runtime dependencies. It intentionally excludes JavaFX artifacts and fonts, so text uses fonts provided by the host system.

Build it with:

```shell
./gradlew shadowDemoJar
```

The output is:

```text
demo/build/libs/m3fx-demo-1.0-SNAPSHOT-shadow.jar
```

Run it with a JavaFX runtime on the module path or class path according to the launcher setup used by the application environment. The shadow jar should not be treated as a self-contained desktop distribution because JavaFX native libraries are not bundled.

## Catalog Shadow Jar

The focused AndroidX-style Material Catalog has an independent shadow jar. It packages the Catalog application, its stylesheet,
M3FX, MonetFX, and non-JavaFX runtime dependencies. JavaFX remains supplied by the application environment.

Build it with:

```shell
./gradlew shadowCatalogJar
```

The output is:

```text
catalog/build/libs/m3fx-catalog-1.0-SNAPSHOT-shadow.jar
```

## Demo Jlink Runtime

The jlink tasks create runtime images for the demo application. They download a target BellSoft LibericaJDK Full archive, extract its `jmods`, and build a runtime image containing JavaFX and the demo modules. Windows and macOS targets use BellSoft zip archives; Linux targets use BellSoft `tar.gz` archives.

Build the host-platform runtime image:

```shell
./gradlew jlinkDemoRuntime
```

Build platform-specific images for the configured architecture:

```shell
./gradlew jlinkDemoWindowsRuntime
./gradlew jlinkDemoLinuxRuntime
./gradlew jlinkDemoMacosRuntime
```

Build fixed platform and architecture images:

```shell
./gradlew jlinkDemoWindowsX64Runtime
./gradlew jlinkDemoWindowsAarch64Runtime
./gradlew jlinkDemoLinuxX64Runtime
./gradlew jlinkDemoLinuxAarch64Runtime
./gradlew jlinkDemoMacosX64Runtime
./gradlew jlinkDemoMacosAarch64Runtime
```

Runtime images are written under:

```text
demo/build/jlink/
```

Each jlink task verifies the generated runtime image before it succeeds. The verification requires the `release` metadata file, `lib/modules`, JavaFX runtime metadata, JavaFX legal metadata, the M3FX demo module entry in `release`, and the expected platform launcher. Windows images must contain both `bin/m3fx-demo` and `bin/m3fx-demo.bat`; Linux and macOS images must contain `bin/m3fx-demo`.

## Demo Native Image

The demo uses the
[GraalVM Native Build Tools Gradle plugin](https://graalvm.github.io/native-build-tools/latest/gradle-plugin) to
compile the application ahead of time. Install the JavaFX-enabled Full distribution of
[Liberica Native Image Kit](https://docs.bell-sw.com/liberica-nik/latest/how-to/using-nik-with-desktop-applications/)
and point `GRAALVM_HOME` at it before starting Gradle, or run Gradle with that installation. The build verifies the
`native-image` executable, the JavaFX controls module, and the Liberica NIK runtime identity before compilation.

Build and run the executable with:

```shell
./gradlew nativeBuildDemo
./gradlew nativeRunDemo
```

`nativeBuildDemo` builds the verified demo shadow jar, compiles it with `--no-fallback`, verifies the generated
executable, and stages one distributable file under:

```text
demo/build/distributions/native/<os>-<arch>/m3fx-demo[.exe]
```

Native Image intermediate files remain under `demo/build/native/nativeCompile/` and are not distribution
artifacts. On Windows the staged result is a single `m3fx-demo.exe` linked as a Windows GUI application, so launching
it does not allocate a console window. The build reads the staged PE metadata and fails unless the executable uses the
Windows GUI subsystem. The executable is not an installer and may still depend on operating-system libraries. The
metadata under `META-INF/native-image/org.glavo/m3fx-demo` retains M3FX and demo CSS, the packaged Alibaba PuHuiTi
font, and the post-JavaFX-17 focus-visible method reached by the guarded runtime path. The shadow jar itself continues
to exclude OpenJFX artifacts because JavaFX comes from Liberica NIK Full.

Native Image does not cross-compile desktop executables. Run the task on each target operating system and
architecture. The manually dispatched `Build Demo Native Image` workflow accepts the Liberica NIK Java feature
version, builds and uploads the staged Linux x64, Windows x64, and macOS AArch64 executables, and performs a Windows
startup smoke check before publishing artifacts. Platform C toolchains are still required locally; Linux additionally
needs the JavaFX GTK, graphics, audio, and X11 development packages installed by that workflow.

## Jlink Configuration

The default jlink target is inferred from the host OS and architecture. It can be overridden with Gradle properties:

```shell
./gradlew jlinkDemoRuntime -Pm3fx.jlink.os=windows -Pm3fx.jlink.arch=x86 -Pm3fx.jlink.javaFeature=21
```

Supported Liberica OS values:

- `windows`
- `linux`
- `macos`

Supported architecture values:

- `x86`
- `aarch64`

Useful properties:

- `m3fx.jlink.os`
- `m3fx.jlink.arch`
- `m3fx.jlink.bitness`
- `m3fx.jlink.javaFeature`
- `m3fx.jlink.releaseType`
- `m3fx.jlink.versionModifier`
- `m3fx.jlink.bundleType`
- `m3fx.jlink.downloadUrl`
- `m3fx.jlink.executable`

Per-platform and per-architecture variants can use scoped properties such as:

```text
m3fx.jlink.windows.arch
m3fx.jlink.linuxAarch64.javaFeature
m3fx.jlink.macosX64.downloadUrl
```

## Cross-Platform Notes

Cross-platform and cross-architecture jlink use target-platform `jmods` from the downloaded target LibericaJDK. The `jlink` executable must run on the host and match the target Java feature version.

When the current Gradle JVM already matches the target Java feature version, the task uses the Gradle JVM's `jlink`. If it does not match, the task automatically downloads a host-platform BellSoft LibericaJDK Full archive and uses its `jlink` executable. You can still pass an explicit executable to override this behavior:

```shell
./gradlew jlinkDemoLinuxX64Runtime -Pm3fx.jlink.executable=/path/to/jdk-21/bin/jlink
```

For same-platform and same-architecture builds, the task uses the downloaded target JDK's `jlink` executable directly.

## Validation

Use these tasks before distributing artifacts:

```shell
./gradlew releaseCheck
./gradlew check
./gradlew fullTest
./gradlew compileJava
./gradlew test
./gradlew shadowDemoJar
./gradlew shadowCatalogJar
./gradlew jlinkDemoRuntime
./gradlew jlinkDemoAllPlatformArchitectureRuntimes
```

`releaseCheck` runs `check`, `fullTest`, both sample-application shadow jar verifications, and `jlinkDemoRuntime`. It is the local release gate for the library publication, all test tiers, sample-application behavior tests, and the host-platform demo distribution. It does not run the all-platform jlink aggregate task, so release builds can opt into the cross-platform runtime images they actually need.

The GitHub Actions workflow runs the Tier 1 build gate under Xvfb for pushes and pull requests. A manual workflow dispatch runs the complete `releaseCheck` entry point. Both paths upload the generated demo and catalog shadow jars with `actions/upload-artifact@v7` and `archive: false`, and preserve available visual, HTML, and XML test reports with `if: always()`.

The separate Native Image workflow is manual because AOT compilation is intentionally outside the fast Tier 1
gate. It provisions Liberica NIK Full and the platform compiler, runs `nativeBuildDemo`, and uploads the staged
executable with `archive: false`.

## Nightly Demo Release

The Publish Nightly Demo workflow runs every day at 02:17 UTC and may also be started with
`workflow_dispatch`. It first runs the Tier 1 test suite and verifies the Demo Shadow JAR. After that gate passes,
it builds Windows x64, Linux x64, and macOS AArch64 Demo executables with Liberica Native Image Kit Full.

The workflow maintains one prerelease at the `nightly` tag. Each successful run moves the tag to the tested commit,
updates the release notes, and replaces the stable asset names:

- `m3fx-demo-nightly.jar`
- `m3fx-demo-nightly-windows-x86_64.exe`
- `m3fx-demo-nightly-linux-x86_64.tar.gz`
- `m3fx-demo-nightly-macos-aarch64.tar.gz`
- `m3fx-demo-nightly-build.txt`
- `SHA256SUMS`

The native assets are self-contained platform builds. The Shadow JAR remains an integration artifact and does not
bundle JavaFX. Manual runs may select a Liberica NIK Java feature version; scheduled runs use Java 25. The workflow
uses fixed asset names and `gh release upload --clobber`, so it updates the existing Nightly Release rather than
creating one release per day.

`check` runs publication metadata verification. The verification generates the Maven POM and fails if copied project metadata remains or if JavaFX appears in the published dependency metadata.

`check` also verifies the generated main jar, sources jar, and Javadoc jar. The artifact verification fails if required module or API entries are missing, if any bundled M3FX stylesheet resource is absent from the main jar, if any main Java source is absent from the sources jar, if the Javadoc jar is not a generated documentation artifact, or if the main jar bundles JavaFX implementation classes.

`check` cleans and then publishes the Maven publication to `build/verification-maven-repository` and verifies that the Maven repository layout contains exactly one main jar, sources jar, Javadoc jar, and POM, including timestamped SNAPSHOT artifacts when applicable, without Gradle module metadata or JavaFX artifacts. This verifies the real `maven-publish` wiring without writing to the user's local Maven cache.

`check` also resolves that build-local Maven publication through a Gradle consumer runtime configuration. The consumer verification requires the runtime dependency to resolve M3FX and MonetFX while rejecting transitive OpenJFX artifacts. The sources and Javadoc classifier availability is covered by the publication layout verification above.

`shadowDemoJar` also runs the demo shadow jar verification task. The verification fails if JavaFX content or font resources are bundled into the shadow jar, or if the executable manifest, required demo classes, demo CSS, M3FX classes, or MonetFX classes are absent.

`shadowCatalogJar` runs the corresponding Catalog verification. It rejects bundled JavaFX content and requires the
Catalog launcher, application, stylesheet, M3FX controls, and MonetFX classes.

For smaller cross-platform release checks, run the fixed platform and architecture jlink tasks needed by the release instead of the aggregate task.
