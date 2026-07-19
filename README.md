# M3FX

M3FX is a Material Design 3 component library for JavaFX applications.

The library provides JavaFX controls, skins, themes, generated Material token stylesheets, motion utilities, two standalone sample applications, visual tests, and packaging tasks for desktop artifacts. It uses [MonetFX](https://github.com/Glavo/MonetFX) for Material dynamic color generation and follows the Material Design guidance at [m3.material.io](https://m3.material.io/).

## Status

M3FX implements the baseline Material Design 3 profile and an opt-in Material 3 Expressive profile. The token model covers color, typography, shape, motion, component geometry and semantic color roles, progress and loading indicators, navigation, forms, pickers, menus, and surfaces.

The demo and catalog applications exercise both profiles in light, dark, left-to-right, right-to-left, and reduced-motion configurations.

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
the in-scene layers used by `M3Dialog`; neither feature replaces `Scene.root`. Present a dialog directly through the
overlay pane and retain the returned handle when programmatic dismissal or presentation-state observation is needed:

```java
M3Dialog dialog = new M3Dialog();
M3Button cancel = new M3Button("Cancel", M3ButtonVariant.TEXT);
cancel.setCancelButton(true);
M3Button save = new M3Button("Save", M3ButtonVariant.TEXT);
save.setDefaultButton(true);
dialog.getDialogPane().getActions().addAll(cancel, save);
dialog.setOnHidden(event -> {
    if (event.getAction() == save) {
        System.out.println("Settings saved");
    }
});

M3DialogHandle handle = root.showDialog(dialog);
handle.requestClose();
```

Dialog actions are retained `M3Button` instances rather than immutable button descriptors. Their observable text,
graphic, disable state, and action properties can therefore follow runtime localization and application state. Use
object identity with `M3DialogEvent.getAction()` to distinguish the action that closed a dialog; the value is `null`
when no action initiated the close.

When no application scene or overlay host exists, `M3DialogWindow` presents the same dialog in a dedicated native
Stage. The window host owns native owner, modality, style, title, and theme configuration while `M3Dialog` remains a
host-independent description:

```java
M3DialogWindow window = new M3DialogWindow();
window.setTitle("Settings");
window.initModality(Modality.NONE);
window.showDialog(dialog);
```

Standalone windows use native modality and do not draw a cross-window Material scrim. Both presentation modes return
`M3DialogHandle` and use the same cancellable close and lifecycle-event contract.

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

`M3Profile` selects default token families; it is not a capability gate. Explicit component tokens are honored
independently of the profile retained by the token set. For example, one control or subtree can use Expressive
component geometry and semantic color-role mappings while the application keeps its baseline color scheme and
other token groups:

```java
M3Theme baseline = M3Theme.defaultTheme();
M3ComponentTokens expressiveDefaults = M3ComponentTokens.builder(
        M3Profile.EXPRESSIVE_2025,
        M3ShapeTokens.expressive(),
        baseline.density()
).build();
M3ComponentTokens buttonComponents = M3ComponentTokens.builder(baseline.tokens().componentTokens())
        .filledButton(expressiveDefaults.filledButton())
        .buttonSizing(expressiveDefaults.buttonSizing())
        .build();
M3Theme expressiveControlTheme = M3Theme.fromTokenSet(
        M3TokenSet.builder(baseline.tokens())
                .motionTokens(M3MotionTokens.expressive())
                .componentTokens(buttonComponents)
                .build()
);

M3Button emphasizedAction = new M3Button("Continue");
M3ThemeManager.install(emphasizedAction, expressiveControlTheme);
```

Color generation is independent when a prebuilt MonetFX `ColorScheme` is supplied through
`M3Theme.fromColorScheme(...)`; M3FX retains that scheme instead of regenerating it for the selected profile.
Use the type-safe color properties for local component overrides. A `null` base-color slot contributes no local
declaration, so the component variant and active theme continue to resolve that role. Base colors remain in the CSS
cascade for all states; provide a disabled replacement when the disabled state should use a different local color:

```java
M3Button action = new M3Button("Save", M3ButtonVariant.FILLED);
action.setColors(new M3ButtonColors(
        Color.web("#006A6A"),
        Color.WHITE,
        null,
        Color.web("#7A7A7A")
));

M3Icon icon = new M3Icon("favorite");
icon.setTint(Color.web("#9C4146"));
M3SVGIcon svgIcon = new M3SVGIcon(path, viewBox);
svgIcon.setTint(Color.web("#9C4146"));
```

The same local-color pattern is available through `M3Card.setColors(...)` and `M3Surface.setColors(...)`. These
overrides remain attached to the control when the application switches its global theme; roles without a local
declaration continue to follow that new theme. Use a local `M3ThemeManager.install(...)` call when an entire
subtree needs a different token set. Use CSS for brand-wide typography, outline treatments, and visual details
outside the typed component color model:

```css
.save-action {
    -m3-color-primary: #006A6A;
    -m3-color-on-primary: white;
}
```

Colors that must follow a subtree-wide token set should still be expressed through a local theme. This keeps the
public properties focused on common component semantics without duplicating every CSS token on every control.

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
