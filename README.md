# M3FX

M3FX is a Material Design 3 component library for JavaFX applications. It uses
[MonetFX](https://github.com/Glavo/MonetFX) for dynamic color generation and follows
[Material Design](https://m3.material.io/), with baseline and Expressive token profiles.

The project is in preview; the public API is not frozen. See [PLANS.md](PLANS.md) for remaining work.

## Requirements

- The library targets Java 17 and supports JavaFX 17 or later.
- The repository uses JavaFX 21 by default. CI builds use JDK 21; JitPack uses JDK 25.
- Applications supply their own JavaFX modules and platform-specific native libraries.

## Installation

For a local source build, run `./gradlew :publishToMavenLocal`, then add the library to your application:

```kotlin
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.glavo:m3fx:1.0-SNAPSHOT")
    implementation("org.openjfx:javafx-base:21:win")
    implementation("org.openjfx:javafx-graphics:21:win")
    implementation("org.openjfx:javafx-controls:21:win")
}
```

The example uses the Windows classifier. Choose the JavaFX classifier for your operating system and CPU architecture.
M3FX does not publish OpenJFX artifacts as runtime dependencies.

To use a Git tag or commit through [JitPack](https://jitpack.io/#Glavo/m3fx), add its repository and replace
the M3FX dependency:

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io") {
        content { includeGroup("com.github.Glavo") }
    }
}

dependencies {
    implementation("com.github.Glavo:m3fx:<tag-or-commit>")
}
```

Applications using JPMS require the `org.glavo.m3fx` module:

```java
module your.application {
    requires org.glavo.m3fx;
    requires javafx.controls;
}
```

## Basic Usage

Install a theme on the scene and use M3FX controls with ordinary JavaFX layouts:

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
import org.jetbrains.annotations.NotNullByDefault;

/// Demonstrates themed input and queued feedback.
@NotNullByDefault
public final class DemoApp extends Application {
    /// Creates the application scene.
    @Override
    public void start(Stage stage) {
        M3TextInputLayout name = new M3TextInputLayout(new M3TextField());
        name.setLabelText("Project name");
        M3Button create = new M3Button("Create");

        M3OverlayPane root = new M3OverlayPane();
        root.setContent(new VBox(16.0, name, create));
        create.setOnAction(event -> root.showSnackbar(new M3Snackbar("Project created")));

        Scene scene = new Scene(root, 480.0, 320.0);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        stage.setScene(scene);
        stage.setTitle("M3FX Demo");
        stage.show();
    }
}
```

## Component Configuration

Use JavaFX properties for behavior, variants, sizes, shapes, and local paint overrides:

```java
M3Button button = new M3Button("Save", M3ButtonVariant.TONAL);
button.setSize(M3ButtonSize.LARGE);
button.setButtonShape(M3ButtonShape.SQUARE);
button.setContainerColor(Color.web("#006A6A"));
button.setContentColor(Color.WHITE);
```

Styleable properties follow JavaFX CSS precedence; binding a property prevents CSS assignment.
Use CSS for application-wide typography and visual overrides:

```css
.save-action {
    -m3-container-color: #006A6A;
    -m3-content-color: white;
}
```

`M3ThemeManager.install(Parent, theme)` applies tokens to a subtree. `M3Profile` selects defaults, not which
capabilities a control may use. For example, a baseline application can give one button Expressive component tokens:

```java
M3Theme baseline = M3Theme.defaultTheme();
M3ComponentTokens expressive = M3ComponentTokens.builder(
        M3Profile.EXPRESSIVE_2025, M3ShapeTokens.expressive(), baseline.density()
).build();
M3ComponentTokens components = M3ComponentTokens.builder(baseline.tokens().componentTokens())
        .filledButton(expressive.filledButton())
        .buttonSizing(expressive.buttonSizing())
        .build();
M3Theme localTheme = M3Theme.fromTokenSet(
        M3TokenSet.builder(baseline.tokens())
                .motionTokens(M3MotionTokens.expressive())
                .componentTokens(components)
                .build()
);
M3Button action = new M3Button("Continue");
M3ThemeManager.install(action, localTheme);
```

`M3Theme.fromColorScheme(...)` retains a supplied MonetFX color scheme independently of the selected profile.
Cards and surfaces expose a container paint; configure their child controls separately or install a subtree theme.

### Lists And Cards

```java
M3ListPane settings = new M3ListPane();
settings.setListStyle(M3ListStyle.SEGMENTED);
settings.setSelectionMode(M3SelectionMode.NONE);
settings.getItems().add(new M3SwitchSettingItem("Automatic updates"));
```

Setting rows own their switch, checkbox, or radio state, so their containing list should not also select them.
`M3ListPane` arranges retained nodes; `M3ListView` virtualizes data-backed items.

A card becomes interactive when an action handler is assigned:

```java
M3Card card = new M3Card(projectSummary);
card.setOnAction(event -> openProject());
```

Leave `onAction` unset when the card contains independent buttons or links.

### Scrolling

`M3ScrollPane` enables Material scrollbar styling, smooth wheel motion, and stretch overscroll for continuous
gestures. Its logical scroll values remain bounded. Set `overscrollEffect` to `null` to disable the edge effect,
or configure `overscrollInputMode` to select eligible input.

Existing JavaFX scroll panes opt into styling and smooth scrolling independently:

```java
ScrollPane viewport = new ScrollPane(content);
M3ScrollPane.style(viewport);
M3ScrollPane.enableSmoothScrolling(viewport);
```

`M3ScrollPane.disableSmoothScrolling(...)` removes smooth scrolling without removing the style.
M3FX list views and text areas style their internal scrollbars automatically.

## Dialogs And Feedback

`M3OverlayPane` hosts snackbars, dialogs, and custom overlays above its ordinary content. Presentations retain
lifecycle handles and do not replace the scene root.

Snackbar messages expose observable text and action properties. Both `showSnackbar(...)` and
`enqueueSnackbar(...)` append to the FIFO queue. Horizontal swipe-to-dismiss is enabled by default.

```java
M3Snackbar message = new M3Snackbar("Project archived");
message.setActionText("Undo");
message.setAction(() -> root.enqueueSnackbar(new M3Snackbar("Project restored")));
root.showSnackbar(message);
```

A non-blank action label shows a button even if its callback is null. Activating it dismisses the message.

Dialogs expose `headline` and `supportingText` properties. A custom `content` node replaces the supporting text
without changing its value or binding. Dialog actions are ordinary `M3Button` instances:

```java
M3Dialog dialog = new M3Dialog();
dialog.getDialogPane().setHeadline("Save settings?");
dialog.getDialogPane().setSupportingText("Your changes will be applied to this project.");
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
```

Use `handle.requestClose()` for programmatic dismissal. `M3DialogEvent.getAction()` identifies the closing action
or returns null when no action initiated the close. Without an overlay host, `M3DialogWindow.showDialog(dialog)`
presents a dedicated native window with the same handle and event contract; native windows have no Material scrim.

`showOverlay(...)` and `showModalOverlay(...)` present custom surfaces. The latter blocks interaction with lower
layers until its handle is hidden.

## Localization

`M3Localizer` supplies built-in control messages and calendar formatting. Its base implementation uses English
messages; its `Locale` controls regional formatting, not message translation. Override the methods your
application needs:

```java
/// Supplies German date-entry messages.
@NotNullByDefault
final class GermanLocalizer extends M3Localizer {
    /// Selects German regional formatting.
    GermanLocalizer() {
        super(Locale.GERMANY);
    }

    /// Returns the date validation message.
    @Override
    public String getInvalidDateText() {
        return "Bitte ein korrektes Datum eingeben";
    }
}
```

Configure components individually or bind them to a shared application property:

```java
ObjectProperty<M3Localizer> language = new SimpleObjectProperty<>(new GermanLocalizer());
calendar.localizerProperty().bind(language);
dateField.localizerProperty().bind(language);
language.set(new M3Localizer(Locale.US));
```

Replace localizers on the JavaFX Application Thread. Values must be non-null and stable for their lifetime;
mutating an existing translator does not notify controls. Unbinding retains the current value. Set a component's
localizer to `M3Localizer.getDefault()` to restore the shared English default.

Language does not propagate through the scene graph. Composite controls connect their owned components;
applications configure independent controls and dialogs. Localization does not change node orientation.

Explicit text and formatter overrides remain application-owned. Date fields default to ISO dates, time fields to
`HH:mm`; configure their formatter properties separately. For inherited JavaFX action-button text properties,
only observable changes or bindings override the localized default; assigning the same text emits no notification.
Color-control descriptions are not yet integrated with this API.

## Layout And Motion

`M3AdaptiveScaffold` arranges bars, navigation, and one to three content panes according to its allocated width:

```java
M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();
scaffold.setTopBar(topAppBar);
scaffold.setNavigationBar(compactNavigation);
scaffold.setNavigationRail(expandedNavigation);
scaffold.setLeadingPane(messageList);
scaffold.setMainPane(messageDetail);
```

It uses the `COMPACT`, `MEDIUM`, `EXPANDED`, `LARGE`, and `EXTRA_LARGE` breakpoints. Pane and navigation policies
can be overridden; `activePane` selects the visible content in single-pane layouts. Leading and trailing roles
follow node orientation, and slot content is retained across breakpoint changes.

Animation utilities work with JavaFX properties and containers:

| API | Purpose |
| --- | --- |
| `M3DoubleAnimatable` | Retarget a scalar property, preserving spring velocity. |
| `M3StateTransition<S>` | Coordinate typed states across property channels, including gesture seeking. |
| `M3AnimatedVisibility` | Enter and exit one retained content node. |
| `M3AnimatedContent` | Transition between replacement content nodes. |
| `M3LayoutTransition` | Animate direct-child placement in an existing parent. |

```java
M3AnimatedContent content = new M3AnimatedContent(summaryPane);
content.setContentTransform(M3ContentTransform.sharedAxis(M3TransitionAxis.X, true));
content.setContent(detailsPane);
```

Enter and exit effects compose fade, scale, logical-edge slide, and reveal. Stop standalone animations and dispose
layout observers when their owning application lifecycle ends; consult their Javadoc for ownership and
writable-property requirements.

Animated controls expose `animationsEnabledProperty()`. Disable optional motion on a component with
`setAnimationsEnabled(false)`, or use an inherited CSS declaration for a subtree:

```css
.reduced-motion {
    -m3-animations-enabled: false;
}
```

CSS changes take effect during CSS processing. A Java setter changes the control's own property, not a descendant
binding. `M3MotionSettings.setGlobalReducedMotionRequested(true)` constrains optional motion application-wide,
even if a local setting enables it. Finite transitions settle at their target; indeterminate indicators may retain
simple movement to communicate ongoing work.

The inherited boolean CSS property `-m3-ripple-enabled` controls how press feedback appears. By default
(`true`), a ripple expands from the interaction origin. Set it to `false` to fade in the same highlight
across the entire interaction area instead. Both effects preserve hover and focus feedback and fade out
on release. This setting does not disable press feedback or change other animations.

```css
.root {
    -m3-ripple-enabled: false;
}

.m3-button {
    -m3-ripple-enabled: true;
}
```

Declarations apply through ordinary JavaFX CSS ancestry, including plain layout parents, and can be overridden
on individual controls. Changes take effect during CSS processing and clear any existing expansion. The setting
applies to controls using M3FX's shared press feedback; controls with only focus or drag feedback are unaffected.

## Color Editing

`M3ColorPicker` composes area, slider, wheel, field, and swatch controls. The color controls use an interaction model
inspired by [Spectrum 2](https://react-spectrum.adobe.com/ColorArea) with M3FX styling.
`M3Color` preserves RGB, HSL, or HSB editing state; `toFxColor()` converts it to a JavaFX color.

```java
M3ColorPicker picker = new M3ColorPicker(new M3HsbColor(268.0, 0.62, 0.76));
picker.setShowColorWheel(true);
picker.valueProperty().addListener((observable, oldColor, newColor) ->
        System.out.println(newColor.toFxColor()));
```

## Sample Applications

| Application | Purpose | Command |
| --- | --- | --- |
| Demo | Component variants, states, and responsive examples. | `./gradlew :demo:run` |
| Catalog | Focused component and example navigation. | `./gradlew :catalog:run` |
| HMCL MD3 Demo | Launcher-style application built with M3FX. | `./gradlew :hmcl-md3-demo:run` |

The [nightly release](https://github.com/Glavo/m3fx/releases/tag/nightly) provides the Demo Shadow JAR and
platform-specific native executables. See [PACKAGING.md](docs/PACKAGING.md) for building distributions.

Run `./gradlew test` for the default Tier 1 suite. Higher-cost tests are opt-in; see
[TESTING.md](docs/TESTING.md) for test tiers, environment requirements, and reports.

## License

M3FX is licensed under the Apache License, Version 2.0. The HMCL demo includes upstream assets under GPL-3.0;
see its packaged license.
