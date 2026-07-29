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

## HMCL MD3 Demo Shadow Jar

The HMCL Material Design 3 demo has an independent shadow jar. It packages the application, generated HMCL artwork
and skin assets, the upstream GPL-3.0 license, M3FX, MonetFX, and non-JavaFX runtime dependencies. JavaFX remains
supplied by the application environment.

Build it with:

```shell
./gradlew shadowHmclMd3DemoJar
```

The output is:

```text
hmcl-md3-demo/build/libs/hmcl-md3-demo-1.0-SNAPSHOT-shadow.jar
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

Each jlink task validates the generated runtime image before it succeeds. The image contains the expected demo
launcher and the JavaFX runtime required by that platform.

## Demo Native Image

The demo uses the
[GraalVM Native Build Tools Gradle plugin](https://graalvm.github.io/native-build-tools/latest/gradle-plugin) to
compile the application ahead of time. Install the JavaFX-enabled Full distribution of
[Liberica Native Image Kit](https://docs.bell-sw.com/liberica-nik/latest/how-to/using-nik-with-desktop-applications/)
and point `GRAALVM_HOME` at it before starting Gradle, or run Gradle with that installation. The build requires a
working `native-image` executable and the JavaFX controls module.

Build and run the executable with:

```shell
./gradlew nativeBuildDemo
./gradlew nativeRunDemo
```

`nativeBuildDemo` builds the demo shadow jar, compiles it with `--no-fallback`, and stages one distributable file
under:

```text
demo/build/distributions/native/<os>-<arch>/m3fx-demo[.exe]
```

On Windows the staged result is a single `m3fx-demo.exe` linked as a Windows GUI application, so launching it does
not allocate a console window. The executable is not an installer and may still depend on operating-system libraries.
The shadow jar continues to exclude OpenJFX artifacts and fonts because JavaFX and text-rendering resources come
from the target environment.

Native Image does not cross-compile desktop executables. Run the task on each target operating system and
architecture. Platform C toolchains are required locally; Linux also requires the JavaFX GTK, graphics, audio, and
X11 development packages.

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

Cross-platform and cross-architecture jlink use target-platform `jmods` from the selected LibericaJDK. The `jlink`
executable must run on the host and match the target Java feature version. An explicit executable may be provided:

```shell
./gradlew jlinkDemoLinuxX64Runtime -Pm3fx.jlink.executable=/path/to/jdk-21/bin/jlink
```

## Validation

Use these tasks before distributing artifacts:

```shell
./gradlew releaseCheck
./gradlew check
./gradlew fullTest
./gradlew compileJava
./gradlew test
./gradlew shadowDemoJar
./gradlew shadowHmclMd3DemoJar
./gradlew shadowCatalogJar
./gradlew jlinkDemoRuntime
./gradlew jlinkDemoAllPlatformArchitectureRuntimes
```

`releaseCheck` runs `check`, `fullTest`, all three sample-application shadow-jar verifications, and
`jlinkDemoRuntime`. It is the local release gate for library publication and the host-platform demo distribution.

## Nightly Demo Release

The Publish Nightly Demo workflow runs daily and can also be started manually. It publishes the verified Demo Shadow
JAR and available native demo distributions.

The workflow maintains one prerelease at the `nightly` tag. Each successful run moves the tag to the tested commit,
updates the release notes, and replaces the stable asset names:

- `m3fx-demo-nightly.jar`
- `m3fx-demo-nightly-windows-x86_64.exe`
- `m3fx-demo-nightly-linux-x86_64.tar.gz`
- `m3fx-demo-nightly-macos-aarch64.tar.gz`
- `m3fx-demo-nightly-build.txt`
- `SHA256SUMS`

The native assets are platform-specific builds. The Shadow JAR remains an integration artifact and does not bundle
JavaFX.
