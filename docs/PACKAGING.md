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

The demo shadow jar is intended for quick inspection of the demo application. It packages the demo classes, M3FX, non-JavaFX runtime dependencies, and the demo default font. It intentionally excludes JavaFX artifacts.

During resource processing, the demo downloads `https://registry.npmmirror.com/@fontpkg/alibaba-puhuiti-3-0/-/alibaba-puhuiti-3-0-0.0.0.tgz`, extracts `AlibabaPuHuiTi-3-65-Medium.ttf`, and packages it under the demo resources so the application can use it as its default font. The download URL can be overridden with `-Pm3fx.demo.fontPackageUrl=...` when building from a different mirror.

Build it with:

```shell
./gradlew shadowDemoJar
```

The output is:

```text
demo/build/libs/m3fx-demo-1.0-SNAPSHOT-shadow.jar
```

Run it with a JavaFX runtime on the module path or class path according to the launcher setup used by the application environment. The shadow jar should not be treated as a self-contained desktop distribution because JavaFX native libraries are not bundled.

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
./gradlew compileJava
./gradlew test
./gradlew shadowDemoJar
./gradlew jlinkDemoRuntime
./gradlew jlinkDemoAllPlatformArchitectureRuntimes
```

`releaseCheck` runs `check`, `:demo:test`, `shadowDemoJar`, and `jlinkDemoRuntime`. It is the local release gate for the library publication, demo visual and behavior tests, and the host-platform demo distribution. It does not run the all-platform jlink aggregate task, so release builds can opt into the cross-platform runtime images they actually need.

The GitHub Actions release workflow runs the same `releaseCheck` entry point under Xvfb on Linux and then uploads the generated demo shadow jar with `archive: false`. It also uploads visual snapshot PNGs plus Gradle HTML and XML test reports with `if: always()` so failed or suspicious visual runs keep reviewable evidence.

`check` runs publication metadata verification. The verification generates the Maven POM and fails if copied project metadata remains or if JavaFX appears in the published dependency metadata.

`check` also verifies the generated main jar and sources jar. The artifact verification fails if required module or API entries are missing, if any bundled M3FX stylesheet resource is absent from the main jar, if any main Java source is absent from the sources jar, or if the main jar bundles JavaFX implementation classes.

`check` cleans and then publishes the Maven publication to `build/verification-maven-repository` and verifies that the Maven repository layout contains exactly one main jar, sources jar, and POM, including timestamped SNAPSHOT artifacts when applicable, without Gradle module metadata or JavaFX artifacts. This verifies the real `maven-publish` wiring without writing to the user's local Maven cache.

`check` also resolves that build-local Maven publication through a Gradle consumer runtime configuration. The consumer verification requires the runtime dependency to resolve M3FX and MonetFX while rejecting transitive OpenJFX artifacts. The sources classifier availability is covered by the publication layout verification above.

`shadowDemoJar` also runs the demo shadow jar verification task. The verification fails if JavaFX classes or JavaFX jar files are bundled into the shadow jar, if the executable manifest is missing, if required demo classes, demo CSS, M3FX classes, or MonetFX classes are absent, or if the packaged `AlibabaPuHuiTi-3-65-Medium.ttf` demo font is absent or empty.

For smaller cross-platform release checks, run the fixed platform and architecture jlink tasks needed by the release instead of the aggregate task.
