# M3FX Packaging Guide

## Library Usage

M3FX is a JavaFX library. Applications own the JavaFX runtime and should provide JavaFX modules themselves.

The library publishes JavaFX as a compile-only dependency so downstream applications can choose their own JavaFX version, platform classifier, module path, and runtime-image strategy.

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

The demo shadow jar is intended for quick inspection of the demo application. It packages the demo classes, M3FX, and non-JavaFX runtime dependencies. It intentionally excludes JavaFX artifacts.

Build it with:

```shell
./gradlew -g .gradle-user-home shadowDemoJar
```

The output is:

```text
demo/build/libs/m3fx-demo-1.0-SNAPSHOT-shadow.jar
```

Run it with a JavaFX runtime on the module path or class path according to the launcher setup used by the application environment. The shadow jar should not be treated as a self-contained desktop distribution because JavaFX native libraries are not bundled.

## Demo Jlink Runtime

The jlink tasks create runtime images for the demo application. They download a BellSoft LibericaJDK Full archive, extract its `jmods`, and build a runtime image containing JavaFX and the demo modules.

Build the host-platform runtime image:

```shell
./gradlew -g .gradle-user-home jlinkDemoRuntime
```

Build platform-specific images for the configured architecture:

```shell
./gradlew -g .gradle-user-home jlinkDemoWindowsRuntime
./gradlew -g .gradle-user-home jlinkDemoLinuxRuntime
./gradlew -g .gradle-user-home jlinkDemoMacosRuntime
```

Build fixed platform and architecture images:

```shell
./gradlew -g .gradle-user-home jlinkDemoWindowsX64Runtime
./gradlew -g .gradle-user-home jlinkDemoWindowsAarch64Runtime
./gradlew -g .gradle-user-home jlinkDemoLinuxX64Runtime
./gradlew -g .gradle-user-home jlinkDemoLinuxAarch64Runtime
./gradlew -g .gradle-user-home jlinkDemoMacosX64Runtime
./gradlew -g .gradle-user-home jlinkDemoMacosAarch64Runtime
```

Runtime images are written under:

```text
demo/build/jlink/
```

## Jlink Configuration

The default jlink target is inferred from the host OS and architecture. It can be overridden with Gradle properties:

```shell
./gradlew -g .gradle-user-home jlinkDemoRuntime -Pm3fx.jlink.os=windows -Pm3fx.jlink.arch=x86 -Pm3fx.jlink.javaFeature=21
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

Cross-platform jlink requires a host `jlink` executable with the same Java feature version as the target runtime. If the current Gradle JVM does not match the target Java feature version, pass a matching executable:

```shell
./gradlew -g .gradle-user-home jlinkDemoLinuxX64Runtime -Pm3fx.jlink.executable=/path/to/jdk-21/bin/jlink
```

The downloaded LibericaJDK archive provides target-platform `jmods`; the host `jlink` executable performs the image build.

## Validation

Use these tasks before distributing artifacts:

```shell
./gradlew -g .gradle-user-home compileJava
./gradlew -g .gradle-user-home test
./gradlew -g .gradle-user-home shadowDemoJar
./gradlew -g .gradle-user-home jlinkDemoRuntime
```

For cross-platform release checks, run the fixed platform and architecture jlink tasks needed by the release.
