# M3FX Packaging Guide

## Library Artifacts

See the [README](../README.md) for dependency setup. Applications supply their own JavaFX
runtime: M3FX uses compile-only JavaFX dependencies. Its JPMS `requires transitive` declarations provide module
readability, not Maven runtime dependencies.

Build the core library with `./gradlew :assemble`, or install its publication locally with
`./gradlew :publishToMavenLocal`. See [TESTING.md](TESTING.md) for verification.

## Demo Shadow JAR

The demo JAR includes application classes and resources, M3FX, MonetFX, and non-JavaFX runtime dependencies.
It requires an external JavaFX runtime, including platform-native libraries.
The demo uses host-system fonts rather than bundled fonts.

Build it with `./gradlew :demo:shadowJar`. The output is `demo/build/libs/m3fx-demo-1.0-SNAPSHOT-shadow.jar`.

## Demo Jlink Runtime

These tasks download BellSoft LibericaJDK Full archives and extract their target `jmods` to create runtime images
containing JavaFX and the demo modules under `demo/build/jlink/`. Each image is checked for its launcher, modules,
and required runtime files.

```shell
./gradlew :demo:jlinkRuntime
./gradlew :demo:jlinkAllPlatformArchitectureRuntimes
```

The first command targets the host OS and architecture; the second builds Windows, Linux, and macOS images for
x64 and AArch64. For one platform, use `:demo:jlinkWindowsRuntime`, `:demo:jlinkLinuxRuntime`, or
`:demo:jlinkMacosRuntime`. To fix the architecture, insert `X64` or `Aarch64` before `Runtime`, for example
`:demo:jlinkLinuxX64Runtime`.

### Configuration

Override the default target with Gradle properties:

```shell
./gradlew :demo:jlinkRuntime -Pm3fx.jlink.os=windows -Pm3fx.jlink.arch=x86 -Pm3fx.jlink.javaFeature=21
```

| Property prefix: `m3fx.jlink.` | Default or accepted values |
| --- | --- |
| `os` | Host OS; `windows`, `linux`, or `macos` |
| `arch` | Host architecture; `x86` (x64 with 64-bit default) or `aarch64` |
| `bitness` | `64` |
| `javaFeature` | `21` |
| `releaseType` | `lts` |
| `versionModifier` | `latest` |
| `bundleType` | `jdk-full` |
| `downloadUrl` | Optional target archive URL; otherwise resolved through BellSoft |
| `executable` | Optional host-compatible `jlink` path |

Windows and macOS archives use ZIP; Linux uses `tar.gz`. Platform and architecture variants support scoped
properties such as `m3fx.jlink.windows.arch`, `m3fx.jlink.linuxAarch64.javaFeature`, and
`m3fx.jlink.macosX64.downloadUrl`.

Cross-platform or cross-architecture builds use target `jmods` with a host-compatible `jlink` of the same Java
feature version. The build downloads a matching host JDK when needed, or accepts an explicit executable:

```shell
./gradlew :demo:jlinkLinuxX64Runtime -Pm3fx.jlink.executable=/path/to/jdk-21/bin/jlink
```

## Demo Native Image

Install the JavaFX-enabled Full distribution of Liberica Native Image Kit and set `GRAALVM_HOME` before starting
Gradle, or run Gradle with that installation. The build checks for `native-image` and the `javafx.controls` module.
Platform C toolchains are required; Linux also needs JavaFX GTK, graphics, audio, and X11 development packages.
See the [native-image workflow](../.github/workflows/demo-native-image.yml) for the CI toolchain and package list.

```shell
./gradlew :demo:stageNativeExecutable
./gradlew :demo:nativeRun
```

The staging task builds a native executable without a JVM fallback and writes one distributable file:

```text
demo/build/distributions/native/<os>-<arch>/m3fx-demo[.exe]
```

Windows builds use the GUI subsystem and do not allocate a console window. The executable is not an installer
and may still depend on OS libraries. Native Image does not cross-compile desktop executables; build on each
target OS and architecture.

## Nightly Demo Release

The daily or manually dispatched [nightly workflow](../.github/workflows/nightly-release.yml) runs Tier 1 and
builds the Demo Shadow JAR and native distributions for Windows x64, Linux x64, and macOS AArch64. Publication
requires all expected build assets. Successful runs move the `nightly` tag to the tested commit and update the
prerelease notes and stable asset names:

- `m3fx-demo-nightly.jar`
- `m3fx-demo-nightly-windows-x86_64.exe`
- `m3fx-demo-nightly-linux-x86_64.tar.gz`
- `m3fx-demo-nightly-macos-aarch64.tar.gz`
- `m3fx-demo-nightly-build.txt`
- `SHA256SUMS`
