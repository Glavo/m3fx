# M3FX Project Plan

## Current Snapshot

- M3FX is a modular Material Design 3 component library for JavaFX.
- The library targets Java 17 source and bytecode output.
- JavaFX 21 is the default dependency version for local builds and the demo app.
- Public implementation code must stay compatible with JavaFX 14 APIs unless newer APIs are guarded reflectively.
- Material colors are generated through `org.glavo:MonetFX:0.4.0`.
- Theme and token APIs are token-first and profile-aware, with `M3Profile.BASELINE_2021` as the default and `M3Profile.EXPRESSIVE_2025` available for evolving M3 Expressive values.
- Public theme and token abstractions use sealed interfaces with implementation classes in internal packages.
- JavaFX is an application-owned dependency in Gradle metadata; the library does not publish JavaFX artifacts as API dependencies.
- The demo app is a separate Gradle subproject.

## Architecture

- `org.glavo.m3fx.theme` exposes theme creation, scene installation, stylesheet installation, and token stylesheet generation.
- `org.glavo.m3fx.tokens` exposes color, typography, shape, elevation, motion, state-layer, density, profile, and component token groups.
- `org.glavo.m3fx.animation` exposes reusable Material motion curves and duration constants.
- `org.glavo.m3fx.controls` exposes Material-style JavaFX controls and composition containers.
- `org.glavo.m3fx.skins` contains skin implementations for controls that require custom layout, drawing, or interaction behavior.
- Controls use split user-agent stylesheets through each node or control's `getUserAgentStylesheet()` where JavaFX supports it.
- Popup-only styling remains in dedicated control CSS files loaded through the base stylesheet.
- M3ThemeManager is a convenience installer, not the only way to use the library. Applications may install the base stylesheet and generated token stylesheet themselves when they need custom scene management.

## Implemented

### Build And Distribution

- Multi-project Gradle build with a root library project and a separate demo application project.
- Java module descriptor for `org.glavo.m3fx`.
- Root tasks for running the demo, building the demo shadow jar, and creating demo runtime images.
- Demo shadow jar task that packages the demo and non-JavaFX runtime dependencies without bundling JavaFX.
- jlink support for the demo application using BellSoft LibericaJDK Full jmods.
- Independent jlink targets for Windows, Linux, and macOS.
- Fixed architecture jlink targets for x64 and AArch64 on each supported platform.
- GitHub Actions workflow that builds the demo shadow jar and uploads it as an artifact.

### Theme, Tokens, And Motion

- MonetFX-backed color token mapping.
- Complete root CSS token generation for colors, typography, shape, elevation, motion, state layers, and component defaults.
- Generated component token stylesheets with stable content hashing.
- Baseline typography scale and line-height tokens.
- Baseline and expressive shape token profiles.
- Density-aware component tokens.
- Material motion constants and easing curves for reusable JavaFX animations.

### Controls

- Typography and icon primitives: `M3Text`, `M3Icon`, `M3Avatar`, `M3Badge`, and `M3BadgedBox`.
- Buttons: filled, tonal, outlined, text, elevated, icon button, icon toggle button, icon toggle groups, and floating action buttons.
- Inputs: filled and outlined text fields, password fields, text areas, shared error-state support, and text input layouts with floating labels, validators, focus-loss validation, leading/trailing adornments, supporting text, error text, character counters, clear buttons, and optional hard character limits.
- Selection controls: checkbox, radio button, switch, slider, chips, chip groups, segmented buttons, and segmented button groups.
- Navigation: tabs, tab bar, navigation item, navigation bar, navigation rail, and navigation drawer.
- App bars: top app bar variants and bottom app bar with configurable floating action alignment.
- Menus and search: menu, menu item, submenu item with hover and keyboard opening, menu section header, menu button with edge-aware popup placement, search bar, and search view with customizable result content and action slots.
- Feedback and progress: banner, snackbar, snackbar host with queued messages, plain tooltip, rich tooltip, linear progress, and circular progress.
- Containment and utility: surface, card, dialog, dialog pane, side sheet, bottom sheet, scrim, divider, list, list item, and tokenized list section header.
- Composition-heavy controls expose mutable child lists and convenience constructors or factories where they simplify common usage without hiding node ownership.

### Demo And Verification

- Demo app with a Material navigation drawer sidebar and one page per major control family.
- Demo pages cover common variants, disabled states, selected states, error states, and animated progress examples.
- Unit tests cover style classes, token CSS metadata, accessibility attributes, interaction events, skin creation, state-layer/ripple presence, and packaging assumptions.
- Snapshot-based visual tests render implemented control families into report images for manual and automated inspection.
- Demo packaging tests cover the executable shadow jar and verify that JavaFX classes are not bundled.

## Remaining Work

- Expand Material motion coverage so hover, press, release, selection, shape, indicator, and elevation transitions consistently use tokenized timing and easing.
- Broaden visual checks beyond static contrast and color variety toward geometry, clipping, alignment, and animation-state assertions.
- Continue filling component gaps such as richer list item media variants, richer popup nesting policies, and richer validation presentation patterns.
- Tighten accessibility behavior for composite controls, especially indexed children, selection state, role choice, and keyboard navigation.
- Add more focused demo pages for complex components whose behavior is hard to inspect in a single static gallery.
- Document runtime packaging choices for applications that want regular jars, demo shadow jars, or jlink images.
- Keep JavaFX 14 API compatibility checked during implementation while continuing to build by default against JavaFX 21.
- Improve M3 Expressive parity by adding profile-specific component tokens and visual verification once the target token values are finalized.

## Validation Entry Points

- `compileJava` validates main source compilation.
- `test` validates unit, behavior, snapshot, and packaging tests.
- `shadowDemoJar` validates executable demo jar packaging without bundled JavaFX.
- `jlinkDemoRuntime` validates the default demo runtime image.
- `jlinkDemoWindowsRuntime`, `jlinkDemoLinuxRuntime`, and `jlinkDemoMacosRuntime` validate platform runtime-image targets for the configured architecture.
- `jlinkDemoWindowsX64Runtime`, `jlinkDemoWindowsAarch64Runtime`, `jlinkDemoLinuxX64Runtime`, `jlinkDemoLinuxAarch64Runtime`, `jlinkDemoMacosX64Runtime`, and `jlinkDemoMacosAarch64Runtime` validate fixed platform and architecture runtime-image targets.

## Out Of Scope For The Current Pass

- Full M3 Expressive visual parity for every component.
- A web deployment target for the JavaFX demo.
- SASS or another CSS preprocessor layer.
- Large data components such as data tables.
- High-complexity components such as pickers and carousel.
