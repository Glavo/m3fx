# M3FX

M3FX is a Material Design 3 component library for JavaFX applications.

The library provides JavaFX controls, skins, themes, generated Material token stylesheets, motion utilities, demo pages, visual tests, and packaging tasks for demo runtime images. It uses [MonetFX](https://github.com/Glavo/MonetFX) for Material dynamic color generation and follows the Material Design guidance at [m3.material.io](https://m3.material.io/).

## Status

M3FX is in active development. The current artifact version is `1.0-SNAPSHOT`.

The baseline Material Design 3 profile is the primary compatibility target. M3 Expressive support is available through profile-aware color, typography, shape, motion, component, progress, loading, navigation, form, picker, menu, and surface tokens, but exact visual parity for every component remains an ongoing target.

## Requirements

- Java 17 or later for compiling and running M3FX code.
- JavaFX modules supplied by the application.
- JavaFX 21 is the default local build and demo version.
- Public implementation should remain compatible with JavaFX 14 APIs unless newer APIs are guarded at runtime.

M3FX does not publish OpenJFX artifacts as Maven runtime dependencies. Applications own the JavaFX version, platform classifier, module path, and runtime-image strategy.

## Gradle Dependency

```kotlin
dependencies {
    implementation("org.glavo:m3fx:1.0-SNAPSHOT")

    implementation("org.openjfx:javafx-base:21:win")
    implementation("org.openjfx:javafx-graphics:21:win")
    implementation("org.openjfx:javafx-controls:21:win")
}
```

Use the JavaFX platform classifier that matches the target runtime: `win`, `linux`, or `mac`.

The JPMS module name is:

```java
module your.application {
    requires org.glavo.m3fx;
    requires javafx.controls;
}
```

## Basic Usage

Install the M3FX stylesheet and theme on a JavaFX scene, then use M3FX controls like ordinary JavaFX controls.

```java
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3TextField;
import org.glavo.m3fx.controls.M3TextInputLayout;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;

public final class DemoApp extends Application {
    @Override
    public void start(Stage stage) {
        M3TextInputLayout name = new M3TextInputLayout(
                new M3TextField(),
                "Project name",
                "Visible in the workspace list"
        );
        M3Button create = new M3Button("Create");

        VBox root = new VBox(16.0, name, create);
        Scene scene = new Scene(root, 480.0, 320.0);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());

        stage.setScene(scene);
        stage.setTitle("M3FX Demo");
        stage.show();
    }
}
```

`M3ThemeManager` is a convenience installer, not a required runtime singleton. Applications can install the base stylesheet and generated theme stylesheet separately when they need more control over theme ownership.

## Component Areas

Implemented component families include:

- App bars, bottom app bars, toolbars, banners, cards, surfaces, sheets, scrims, dialogs, snackbars, tooltips, badges, avatars, dividers, and typography primitives.
- Buttons, icon buttons, floating action buttons, split buttons, button groups, segmented buttons, tabs, chips, and icon toggle groups.
- Text fields, password fields, text areas, text input layouts, form rows, form sections, form panes, form validators, and validation summaries.
- Checkboxes, radio buttons, switches, sliders, progress bars, progress indicators, and loading indicators.
- Lists, virtualized list views, list items, navigation bars, navigation rails, navigation drawers, menus, submenus, menu buttons, search bars, search views, date pickers, date-range pickers, time pickers, picker fields, and carousels.

Controls use custom skins and avoid inheriting from concrete JavaFX controls where M3FX owns the behavior surface. Text input controls intentionally retain JavaFX text-input bases to preserve editing, selection, clipboard, IME, and multiline behavior.

## Demo Application

Run the demo from the Gradle project:

```shell
./gradlew :demo:run
```

Build the demo shadow jar without bundling JavaFX:

```shell
./gradlew shadowDemoJar
```

Build a host-platform jlink runtime image:

```shell
./gradlew jlinkDemoRuntime
```

Build all supported platform and architecture runtime images:

```shell
./gradlew jlinkDemoAllPlatformArchitectureRuntimes
```

See [docs/PACKAGING.md](docs/PACKAGING.md) for shadow jar, jlink, cross-platform runtime-image, and validation details.

## Verification

Common local gates:

```shell
./gradlew check --warning-mode all
./gradlew releaseCheck --warning-mode all
./gradlew jlinkDemoAllPlatformArchitectureRuntimes
```

`check` covers compilation, tests, Javadoc, Maven publication metadata, publication artifact layout, and build-local publication consumption. `releaseCheck` adds demo shadow jar verification and the default host-platform demo jlink runtime image. The all-platform jlink aggregate validates Windows, Linux, and macOS runtime images on x64 and AArch64 targets.

## Packaging Notes

- The library publishes JavaFX as compile-only because applications own JavaFX runtime artifacts.
- The module descriptor uses transitive JavaFX readability because public M3FX APIs expose JavaFX types.
- The demo shadow jar packages demo classes, M3FX, non-JavaFX dependencies, and the demo default font, but rejects bundled JavaFX entries.
- Jlink tasks download BellSoft LibericaJDK Full archives and use target `jmods` to create runtime images.

## License

M3FX is licensed under the Apache License, Version 2.0.