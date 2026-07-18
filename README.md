# M3FX

M3FX is a Material Design 3 component library for JavaFX applications.

The library provides JavaFX controls, skins, themes, generated Material token stylesheets, motion utilities, two standalone sample applications, visual tests, and packaging tasks for desktop artifacts. It uses [MonetFX](https://github.com/Glavo/MonetFX) for Material dynamic color generation and follows the Material Design guidance at [m3.material.io](https://m3.material.io/).

## Status

M3FX is a `1.0-SNAPSHOT` release candidate for baseline Material Design 3 support. The public API surface has completed its 1.0 review, and every registered demo page participates in the release visual matrix. Source changes remain gated by release verification rather than broad architecture changes.

The baseline Material Design 3 profile is the primary 1.0 compatibility target. M3 Expressive support is available through profile-aware color, typography, shape, motion, component, progress, loading, navigation, form, picker, menu, and surface tokens. Full exact visual parity for every M3 Expressive component is tracked as post-baseline parity work unless a component already has stable tokens and rendered-state coverage.

The exported package surface, public top-level and nested type inventory, constructor surface, enum constants, public field constants, static utility methods, style class namespace, duplicate wrapper methods, reviewed public batch constructors, and internal type exposure are covered by project contract tests.

The release visual matrix renders all component pages in baseline light, expressive light, baseline dark, expressive dark, baseline RTL, expressive dark RTL, and reduced-motion modes. It captures reviewable screenshots while checking theme context, CSS warnings, sidebar navigation, documentation links, component geometry, and page-specific visual states.

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
import org.glavo.m3fx.controls.M3OverlayPane;
import org.glavo.m3fx.controls.M3Snackbar;
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

        VBox content = new VBox(16.0, name, create);
        M3OverlayPane root = new M3OverlayPane();
        root.setContent(content);
        create.setOnAction(event -> root.showSnackbar(new M3Snackbar("Project created")));

        Scene scene = new Scene(root, 480.0, 320.0);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());

        stage.setScene(scene);
        stage.setTitle("M3FX Demo");
        stage.show();
    }
}
```

Snackbar messages are observable non-node models. Their text, action text, callback, and close-button visibility
are JavaFX properties, so visible or queued messages can follow localization bindings. A non-blank action label
shows the action button even when its callback is `null`; the presenter dismisses the current message after action
activation. Queue any follow-up feedback instead of dismissing the active message from the callback:

```java
M3Snackbar archived = new M3Snackbar("Project archived");
archived.setActionText("Undo");
archived.setAction(() -> root.enqueueSnackbar(new M3Snackbar("Project restored")));
root.showSnackbar(archived);

M3Snackbar connectionLost = new M3Snackbar("Connection lost");
connectionLost.setCloseButtonVisible(true);
root.showSnackbar(connectionLost);
```

Use one `M3OverlayPane` as the stable root of each application scene. It owns transient snackbar presentation and
the in-scene layers used by `M3Dialog`; neither feature replaces `Scene.root`. A dialog owner must be the overlay
pane or one of its descendants.

Custom floating surfaces use a retained lifecycle handle instead of mutating an exposed overlay list:

```java
M3OverlayPane.OverlayHandle handle = root.showOverlay(floatingSurface);
handle.hide();
```

`showModalOverlay(...)` uses the same handle contract while blocking lower-layer input and accessibility and
suspending snackbar interaction until the modal layer is hidden.

`M3ThemeManager` is a stateless installer rather than a required runtime singleton. It can install a theme on a complete `Scene` or on a `Parent` subtree. Theme stylesheet compilation is an internal implementation detail, so applications should use the manager instead of constructing generated stylesheet URLs.

## Per-Control Configuration

Use JavaFX properties for component semantics, behavior, and common geometry:

```java
M3Button button = new M3Button("Save");
button.setVariant(M3ButtonVariant.TONAL);
button.setSize(M3ButtonSize.LARGE);
button.setButtonShape(M3ButtonShape.SQUARE);

M3ListPane list = new M3ListPane();
list.setListStyle(M3ListStyle.SEGMENTED);
list.setItemSpacing(6.0);
```

Install a local theme when one application section needs a different token set without changing the complete scene:

```java
VBox themedSection = new VBox();
M3ThemeManager.install(themedSection, sectionTheme);
```

Use CSS for brand colors, specialized typography, or visual treatments outside the component property model. Color, typography, and elevation remain theme-token concerns rather than being duplicated as properties on every control.

A card becomes an interactive whole only when it has an action handler:

```java
M3Card projectCard = new M3Card(projectSummary);
projectCard.setOnAction(event -> openProject());
```

Leave `onAction` unset when a card contains independent buttons or links. This keeps the card passive and lets its descendants own pointer and keyboard actions.

Style an application-owned JavaFX scroll pane explicitly; this installs only the Material scrollbar visuals. Smooth wheel motion remains an independent opt-in:

```java
ScrollPane viewport = new ScrollPane(content);
M3ScrollPanes.style(viewport);
M3ScrollPanes.enableSmoothScrolling(viewport);
```

M3FX-owned scrolling controls, including `M3ListView` and `M3TextArea`, style their internal scrollbars automatically. Ordinary JavaFX scroll panes remain unchanged until passed to `M3ScrollPanes.style(...)`.

## Component Areas

Implemented component families include:

- App bars, bottom app bars, toolbars, banners, cards, surfaces, sheets, scrims, dialogs, snackbars, tooltips, badges, avatars, dividers, and typography primitives.
- Buttons, icon buttons, floating action buttons, split buttons, button groups, segmented buttons, tabs, chips, and icon toggle groups.
- Text fields, password fields, text areas, text input layouts, form rows, form sections, form panes, form validators, and validation summaries.
- Checkboxes, radio buttons, switches, sliders, progress bars, progress indicators, and loading indicators.
- Lists, virtualized list views, list items, navigation bars, navigation rails, navigation drawers, menus, submenus, menu buttons, search bars, search views, date pickers, date-range pickers, time pickers, picker fields, and carousels.

Controls use custom skins and avoid inheriting from concrete JavaFX controls where M3FX owns the behavior surface. Text input controls intentionally retain JavaFX text-input bases to preserve editing, selection, clipboard, IME, and multiline behavior.

## Sample Applications

The `demo` project is the exhaustive verification gallery. It exposes complete component families, variants, states,
directionality, and motion cases used by the visual test matrix:

```shell
./gradlew :demo:run
```

The independent `catalog` project follows the AndroidX Material 3 Catalog's Home, Component, and Example
hierarchy. Its home route is an alphabetical adaptive card grid; component routes provide descriptions and example
lists, and example routes isolate one working specimen. Theme and display controls live in a modal bottom sheet:

```shell
./gradlew :catalog:run
```

Build either application as a shadow jar without bundling JavaFX:

```shell
./gradlew shadowDemoJar
./gradlew shadowCatalogJar
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
./gradlew fullTest --warning-mode all
./gradlew releaseCheck --warning-mode all
./gradlew jlinkDemoAllPlatformArchitectureRuntimes
```

`check` covers compilation, fast Tier 1 tests, Maven publication metadata, main, sources, and Javadoc artifact structure, and build-local publication consumption. `fullTest` runs every library, demo, and catalog test tier. `releaseCheck` adds both shadow jar verifications and the default host-platform demo jlink runtime image. The all-platform jlink aggregate validates Windows, Linux, and macOS runtime images on x64 and AArch64 targets.

See [docs/TESTING.md](docs/TESTING.md) for the test-tier boundaries and commands.

## Packaging Notes

- The library publishes JavaFX as compile-only because applications own JavaFX runtime artifacts.
- The module descriptor uses transitive JavaFX readability because public M3FX APIs expose JavaFX types.
- The demo shadow jar packages demo classes, demo CSS, M3FX, MonetFX, non-JavaFX dependencies, and the demo default font, and verification rejects bundled JavaFX entries.
- The catalog shadow jar packages the focused AndroidX-style Catalog, its CSS, M3FX, MonetFX, and non-JavaFX dependencies while excluding JavaFX.
- Jlink tasks download BellSoft LibericaJDK Full archives and use target `jmods` to create runtime images.

## License

M3FX is licensed under the Apache License, Version 2.0.
