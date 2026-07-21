# M3FX

M3FX is a Material Design 3 component library for JavaFX applications.

The library provides JavaFX controls, skins, themes, generated Material token stylesheets, motion utilities, two standalone sample applications, visual tests, and packaging tasks for desktop artifacts. It uses [MonetFX](https://github.com/Glavo/MonetFX) for Material dynamic color generation and follows the Material Design guidance at [m3.material.io](https://m3.material.io/).

## Status

M3FX implements the baseline Material Design 3 profile and an opt-in Material 3 Expressive profile. The token model covers color, typography, shape, motion, component geometry and semantic color roles, progress and loading indicators, navigation, forms, pickers, menus, and surfaces.

The demo and catalog applications exercise both profiles in light, dark, left-to-right, right-to-left, and reduced-motion configurations.

## Requirements

- Java 17 or later for compiling and running M3FX code.
- JavaFX 17 or later, with modules supplied by the application.
- JavaFX 21 is the default local build and demo version.
- Public implementation remains compatible with JavaFX 17 APIs unless newer APIs are guarded at runtime.

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

## Adaptive Layout

`M3AdaptiveScaffold` arranges a stable top bar, contextual bottom bar, compact navigation bar, expanded navigation
rail, optional trailing rail, and one to three logical content panes. Its breakpoint follows the width actually
assigned by JavaFX, so resizing or moving a window does not require an application-level screen classifier:

```java
M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();
scaffold.setTopBar(topAppBar);
scaffold.setNavigationBar(compactNavigation);
scaffold.setNavigationRail(expandedNavigation);
scaffold.setLeadingPane(messageList);
scaffold.setMainPane(messageDetail);
```

The standard breakpoints are `COMPACT`, `MEDIUM`, `EXPANDED`, `LARGE`, and `EXTRA_LARGE`. Automatic pane layout
uses one pane below 840 logical pixels and two panes at wider sizes when the corresponding slots are populated.
Automatic navigation selects an available bar or rail for the current width and pane count. Set
`paneLayoutProperty()` or `navigationLayoutProperty()` when an application needs an explicit policy, and use
`activePaneProperty()` to select the content shown by a single-pane layout.

Each slot owns a stable internal container. Content that becomes ineffective at a breakpoint is hidden and
unmanaged rather than detached, preserving selection, scrolling, bindings, and other scene-graph state. Leading
and trailing pane roles follow `NodeOrientation`; physical safety insets remain physical. `breakpointOverride` is
intended for previews, tests, or an application policy that intentionally differs from assigned width.

Resolved pane-topology and navigation-presentation changes animate stable slot bounds and opacity without
reparenting application content. An active transition may be reversed or retargeted from its rendered geometry;
physical spring velocity is retained across retargets. Continuous resizing within the same topology remains direct
so the scaffold tracks the window instead of chasing it. Set `layoutMotionSpecProperty()` for a local specification,
or leave it `null` to use the theme's default spatial motion role.

## Motion And Layout Transitions

M3FX adds animation behavior to ordinary JavaFX properties and layout containers instead of introducing animated
copies of `VBox`, `HBox`, and every other pane. `M3DoubleAnimatable` retargets a writable property without allocating
key frames and preserves spring velocity when an active target changes:

```java
M3DoubleAnimatable position = new M3DoubleAnimatable(
        card,
        card.translateXProperty(),
        0.5
);
position.animateTo(240.0);
```

`M3StateTransition<S>` coordinates primitive doubles and immutable multi-component JavaFX values from one typed
state while using one shared pulse receiver. The current state changes only after every channel settles; the target
state can be changed or bound while a run is active:

```java
M3MotionScheme motion = M3MotionScheme.expressive();
M3MotionSpec enterSpec = motion.defaultSpatial();
M3MotionSpec exitSpec = motion.fastEffects();
M3StateTransition<Boolean> expansion = new M3StateTransition<>(card, false);
expansion.addDouble(card.translateXProperty(), expanded -> expanded ? 240.0 : 0.0, 0.5);
expansion.addDouble(card.scaleXProperty(), expanded -> expanded ? 1.08 : 1.0, 0.0005);
expansion.addDouble(
        card.scaleYProperty(),
        expanded -> expanded ? 1.08 : 1.0,
        0.0005,
        (from, to) -> to ? enterSpec : exitSpec
);
expansion.addValue(
        indicatorPosition,
        expanded -> expanded ? new Point2D(240.0, 16.0) : Point2D.ZERO,
        M3VectorConverters.POINT_2D
);
expansion.setTargetState(true);
```

Built-in converters cover `Color`, `Point2D`, `Point3D`, `Dimension2D`, `Rectangle2D`, and `Insets`; custom
converters define a fixed component order and spring visibility thresholds. State mappings are evaluated only when
a target changes and must return non-null values with finite components. Registered properties must remain writable
and must not be independently changed while the transition is running or seeking.

The same transition can be driven by a gesture. A seek fraction is normalized play time across the longest channel,
so every channel continues to use its own easing or spring rather than falling back to linear value interpolation:

```java
expansion.seekTo(true, dragProgress);
// Continue from the sought play time when direct manipulation ends.
expansion.animateToTarget();
```

`progressProperty()` and `seekingProperty()` expose this lifecycle to JavaFX bindings. Reduced motion settles
automatic continuation synchronously; explicit seeking remains responsive because the caller directly controls it.

Enter and exit transitions compose independent fade, scale, slide, and reveal channels. Reveal effects clip the
private retained holder rather than resizing or modifying the application-owned content node, so surrounding layout
remains stable while an animation is interrupted or reversed:

```java
animatedVisibility.setEnterTransition(
        M3EnterTransition.fade(0.0)
                .and(M3EnterTransition.scale(0.92))
                .and(M3EnterTransition.expandIn(M3TransitionEdge.START, M3TransitionEdge.TOP))
);
animatedVisibility.setExitTransition(
        M3ExitTransition.fade(0.0)
                .and(M3ExitTransition.scale(0.92))
                .and(M3ExitTransition.shrinkOut(M3TransitionEdge.END, M3TransitionEdge.BOTTOM))
);
```

Logical start and end anchors follow effective node orientation. Horizontal-only and vertical-only variants are
available for content that should retain one full axis during the reveal.

`M3AnimatedVisibility` retains one content node without taking ownership of that node's visual properties. Its
showing target can be reversed while a transition is running, while `stateProperty()` distinguishes `ENTERING`,
`VISIBLE`, `EXITING`, and `HIDDEN`. Exit keeps the node mounted until opacity, scale, and animated container size
finish, then detaches it while retaining the public content reference:

```java
M3AnimatedVisibility details = new M3AnimatedVisibility(detailsPane);
details.setEnterTransition(
        M3EnterTransition.fade(0.0).and(M3EnterTransition.scale(0.96))
);
details.setExitTransition(
        M3ExitTransition.fade(0.0).and(M3ExitTransition.scale(0.96))
);
details.setShowing(expanded);
details.stateProperty().addListener((observable, oldState, newState) -> updateStatus(newState));
```

`M3AnimatedContent` performs retained-node replacement. The outgoing node remains attached until its exit effect
finishes, the target node enters at its configured drawing order, and the container's preferred size follows the
target. Assigning an outgoing node again reverses the transition from its current visual state. Enter and exit
values compose one fade, scale, and logical-edge slide channel each; `START` and `END` automatically follow the
effective node orientation:

```java
M3AnimatedContent content = new M3AnimatedContent(summaryPane);
content.setContentTransform(new M3ContentTransform(
        M3EnterTransition.fade(0.0)
                .withDelay(Duration.millis(60.0))
                .and(M3EnterTransition.slideFrom(M3TransitionEdge.END, 24.0)),
        M3ExitTransition.fade(0.0)
                .and(M3ExitTransition.slideTo(M3TransitionEdge.START, 12.0)),
        new M3SizeTransform(true, null),
        0.0
));
content.setContent(detailsPane);
```

The container reuses two private holders and one shared transition, so rapid target changes cannot accumulate an
unbounded list of stale nodes or pulse receivers. Individual effects may carry independent motion specifications
and delays. Set the content transform's size transform to `null` when replacement must adopt the target size
synchronously without clipping.

`M3LayoutTransition` observes an existing `Parent` and animates direct-child `layoutX` and `layoutY` changes through
private transforms. Start it after assigning the container to its lifecycle, and dispose it when that lifecycle is
permanently released:

```java
M3LayoutTransition placement = new M3LayoutTransition(buttonRow);
placement.start();
buttonRow.setAlignment(Pos.CENTER_RIGHT);
placement.dispose();
```

All animation APIs and adaptive-scaffold transitions honor `M3MotionSettings`. A disabled or reduced-motion subtree
reaches its target synchronously. Layout transitions animate placement only; shared elements and general child
entry, removal, or remeasurement remain separate concerns rather than implicit behavior of every layout pane.

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
Use the type-safe, styleable paint properties for local component overrides. Button container and content paints
accept any JavaFX `Paint`, remain observable and bindable, and continue to apply when the global theme changes:

```java
M3Button action = new M3Button("Save", M3ButtonVariant.FILLED);
action.setContainerColor(Color.web("#006A6A"));
action.setContentColor(Color.WHITE);

M3Icon icon = new M3Icon("favorite");
icon.setTint(Color.web("#9C4146"));
M3SVGIcon svgIcon = new M3SVGIcon(path, viewBox);
svgIcon.setTint(Color.web("#9C4146"));
```

`M3Card` and `M3Surface` expose `containerColorProperty()` because the control owns that rendered surface.
They deliberately do not expose a content-color property: arbitrary descendant nodes do not share one JavaFX
paint contract. Configure a complete subtree with `M3ThemeManager.install(...)`, or style the relevant descendant
controls directly. Disabled colors remain part of the Material state-token cascade rather than a parallel set of
properties.

The same paints can be configured in CSS through `-m3-container-color`, `-m3-content-color`, and
`-m3-icon-tint`. Use CSS for brand-wide colors, typography, outline treatments, and visual details outside the
typed component model:

```css
.save-action {
    -m3-container-color: #006A6A;
    -m3-content-color: white;
}
```

The button family shares the `.m3-button-base` style class. Concrete controls add a distinct identity class:
`.m3-button`, `.m3-icon-button`, or `.m3-menu-button`. Use the base class only for rules that deliberately apply to
every button subtype.

This boundary keeps commonly configured component paints type-safe without duplicating the complete theme color
scheme on every control.

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
- Adaptive breakpoints and scaffolds with stable bars, navigation regions, logical rails, and one- to three-pane layouts.

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

Build the demo as a host-platform native executable with
[Liberica Native Image Kit Full](https://docs.bell-sw.com/liberica-nik/latest/how-to/using-nik-with-desktop-applications/):

```shell
./gradlew nativeBuildDemo
./gradlew nativeRunDemo
```

Native Image builds require a JavaFX-enabled Liberica NIK Full installation through `GRAALVM_HOME`, or as the JDK
running Gradle. The build rejects other Native Image distributions and NIK installations without JavaFX, compiles
with `--no-fallback`, and stages one distributable executable under
`demo/build/distributions/native/<os>-<arch>/`. Native executables are platform-specific and do not replace the
cross-platform jlink tasks.

Build all supported platform and architecture runtime images:

```shell
./gradlew jlinkDemoAllPlatformArchitectureRuntimes
```

See [docs/PACKAGING.md](docs/PACKAGING.md) for shadow jar, jlink, Native Image, cross-platform runtime-image, and
validation details.

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
