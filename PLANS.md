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
- Text input controls retain JavaFX text editing implementations for caret, selection, clipboard, and IME behavior while sharing Material state and token plumbing through `M3TextInput`.
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
- Buttons: filled, tonal, outlined, text, elevated, button groups, split buttons, icon button, icon toggle button, icon toggle groups, floating action buttons, extended floating action buttons, and floating action button menus.
- Inputs: filled and outlined text fields, password fields, text areas, shared variant/error/metric-token support, animated text input layouts with floating labels, reusable validators, multi-validator pipelines, focus-loss validation, leading/trailing adornments, supporting text, error text, character counters, clear buttons, and optional hard character limits.
- Selection controls: checkbox, radio button, switch, slider, chips, chip groups, segmented buttons, and segmented button groups.
- Checkbox, radio button, and switch disabled visuals use part-level state token opacity so selected indicators remain legible.
- Pickers: calendar date picker with selected date, displayed month, first-day-of-week, optional adjacent-month days, and inclusive min/max date range support; date range picker with start/end selection, in-range styling, normalized range endpoint selection, and inclusive min/max date bounds; time picker with 12-hour/24-hour display, minute steps, keyboard adjustment, and inclusive min/max time range support; date, date-range, and time picker fields with editable text, popup picker selection, formatter-based parsing, range validation, and shared popup motion.
- Navigation: tabs, tab bar, navigation item, navigation bar, navigation rail, and navigation drawer.
- App bars: top app bar variants and bottom app bar with configurable floating action alignment.
- Menus and search: menu, menu item, submenu item with hover and keyboard opening, menu section header, menu button with edge-aware popup placement, search bar, and search view with customizable result content and action slots.
- Menu popups support focus-first keyboard navigation, submenu item focus without corrupting selection state, sibling submenu exclusivity, ESC focus return, nested action forwarding, and accessibility focus-node routing across open submenu branches.
- Feedback and progress: banner, snackbar, snackbar host with queued messages, plain tooltip, rich tooltip, linear progress, and circular progress.
- Containment and utility: surface, card, carousel with selected-item snapping, dialog, dialog pane, side sheet, bottom sheet, scrim, divider, list, list item with media slot sizes and trailing supporting text, and tokenized list section header.
- Composition-heavy controls expose mutable child lists and convenience constructors or factories where they simplify common usage without hiding node ownership.
- Virtualized list views expose data selection, keyboard focus navigation, accessible focus routing, and `VirtualFlow`-backed row reuse.

### Demo And Verification

- Demo app with a Material navigation drawer sidebar and one page per major control family.
- Demo pages cover common variants, disabled states, selected states, error states, and animated progress examples.
- Unit tests cover style classes, token CSS metadata, accessibility attributes, interaction events, skin creation, state-layer/ripple presence, and packaging assumptions.
- Snapshot-based visual tests render implemented control families into report images for manual and automated inspection.
- Interactive-state visual snapshots cover representative hover, focus-visible, and pressed feedback across buttons, selection controls, navigation, lists, and cards.
- Animation tests verify ripple release behavior on both the shared state layer and real controls, plus representative text input layout presentation transitions.
- Selection control visual snapshots cover selected, unchecked, indeterminate, disabled, and disabled-selected states.
- Date and time picker visual tests render selected, today, adjacent-month, 12-hour/24-hour, minute-step, and disabled range states into dedicated snapshots.
- Demo packaging tests cover the executable shadow jar and verify that JavaFX classes are not bundled.

## Remaining Work

- Expand Material motion coverage so hover, press, release, selection, shape, indicator, and elevation transitions consistently use tokenized timing and easing.
- Broaden visual checks beyond static contrast and color variety toward geometry, clipping, alignment, and animation-state assertions.
- Continue filling component gaps such as richer validation presentation patterns, picker dialog presets, and higher-level form helpers.
- Tighten accessibility behavior for composite controls, especially indexed children, role choice, and keyboard navigation parity across complex popups.
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
- High-complexity data components such as data tables.
