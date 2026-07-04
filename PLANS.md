# M3FX Project Plan

## Purpose

M3FX is a modular Material Design 3 component library for JavaFX. The project is still in active development, with remaining release work centered on API freeze, final visual review, and release-candidate verification after source or build changes.

## Current Baseline

- Java source and bytecode target: Java 17.
- Default JavaFX dependency for local builds and the demo app: JavaFX 21.
- Public implementation should stay compatible with JavaFX 14 APIs unless newer APIs are guarded by runtime checks or reflection.
- JavaFX is owned by applications and is not published as an API or runtime dependency of the library artifact.
- The JPMS module descriptor keeps transitive JavaFX readability because public APIs expose JavaFX types.
- Material colors are generated through `org.glavo:MonetFX:0.4.0`.
- The demo app is a separate Gradle subproject.
- Baseline Material Design 3 is the primary compatibility target.
- M3 Expressive is represented through profile-aware colors, typography, shape, component metrics, motion schemes, and motion behavior timings. Full exact visual parity for every M3 Expressive component is deferred beyond the 1.0 baseline unless the component already has stable tokens and rendered-state coverage.

## Architecture

- `org.glavo.m3fx.theme` owns theme creation, scene and root installation helpers, stylesheet installation, and token stylesheet generation.
- `org.glavo.m3fx.tokens` owns color, typography, shape, elevation, motion, state-layer, density, profile, and component token groups.
- Public theme and token abstractions use sealed interfaces; implementations live in internal packages.
- `org.glavo.m3fx.animation` owns reusable Material motion durations, easing curves, semantic motion specs, runtime animation settings, motion schemes, and behavior timings.
- `org.glavo.m3fx.internal` owns shared runtime infrastructure such as theme resolution, generated stylesheet caching, popup context propagation, focus guards, scroll reveal, animation helpers, and logical layout helpers.
- `org.glavo.m3fx.controls` owns public controls, foundation primitives, composition containers, and utility APIs.
- `org.glavo.m3fx.skins` owns non-exported custom skins for layout, drawing, interaction, state layers, ripple, animation behavior, and popup content.
- Custom skins inherit JavaFX base skin classes such as `SkinBase`, `LabeledSkinBase`, or project skin bases. Popup skins use a project popup skin base because JavaFX `SkinBase` only accepts `Control` skinnables.
- Foundation primitives such as `M3Icon` and `M3Text` support components but are not standalone Material component pages.
- Public control APIs prefer constructors and explicit mutable properties over static convenience factories.
- Text input controls intentionally retain JavaFX text input base classes to preserve editing, selection, clipboard, IME, undo/redo, and multiline behavior. Other M3FX controls avoid inheriting from concrete JavaFX controls where M3FX owns the behavior surface.
- Controls use per-control user-agent stylesheets where JavaFX supports them. Popup-only styling remains in dedicated control CSS files loaded through the base stylesheet.
- Popup context propagation mirrors owner stylesheets, local theme declarations, profile and brightness classes, node orientation, animation settings, motion schemes, and behavior timings into popup roots.
- `M3ThemeManager` is a convenience installer, not a required runtime dependency for applications.

## Implemented Areas

### Build And Distribution

- Multi-project Gradle build for the library and demo app.
- Java module descriptors for the library and demo app.
- Demo run, shadow jar, and jlink runtime-image tasks.
- Demo shadow jar excludes JavaFX artifacts and verifies executable packaging, demo resources, M3FX classes, and MonetFX runtime classes.
- Host and cross-platform jlink support uses BellSoft LibericaJDK Full target jmods.
- Platform and architecture jlink tasks cover Windows, Linux, and macOS on x64 and AArch64 and have been verified through the aggregate all-platform task.
- `releaseCheck` validates the default release path: `check`, `:demo:test`, demo shadow jar verification, and the default host-platform demo jlink runtime image.
- Publication verification covers Maven metadata, main and sources jars, Maven artifact layout, and consumer resolution without publishing OpenJFX artifacts.
- GitHub Actions runs release validation under Xvfb and uploads the verified demo shadow jar as an unarchived artifact.
- Packaging guidance is documented in `docs/PACKAGING.md`.
- The root README documents status, dependency ownership, JPMS usage, theme installation, component status, demo execution, packaging tasks, validation entry points, and licensing.

### Theme, Tokens, And Motion

- MonetFX-backed Material color mapping.
- Generated root CSS token stylesheets for color, typography, shape, elevation, motion, state layers, density, and component defaults.
- Baseline and M3 Expressive token profiles.
- Profile-aware component tokens for action controls, inputs, selection controls, sliders, navigation, lists, menus, search, pickers, sheets, forms, surfaces, cards, dialogs, snackbars, app bars, toolbars, progress indicators, and loading indicators.
- Runtime motion settings for global and node-local animation enablement, motion schemes, and behavior timings.
- State layers, ripples, elevation transitions, overlay transitions, popup transitions, smooth scrolling, progress loops, and loading indicator loops observe runtime motion settings.
- Reduced-motion behavior keeps indeterminate loading and progress controls visibly active while disabling full morph and transition motion.
- Generated component stylesheets install stable profile-specific metrics and preserve application-owned bound layout properties.

### Components

- Action controls: buttons, icon buttons, floating action buttons, FAB menus, split buttons, button groups, segmented buttons, tabs, chips, and icon toggle groups.
- Selection controls: checkboxes, radio buttons, switches, sliders, progress bars, progress indicators, and loading indicators.
- Text input and forms: text fields, password fields, text areas, text input layouts, validators, form rows, form sections, form panes, and validation summaries.
- Navigation and content: navigation bars, navigation rails, navigation drawers, lists, virtualized list views, list items, carousels, dividers, badges, avatars, surfaces, and cards.
- Feedback and overlays: banners, dialogs, snackbars, tooltips, rich tooltips, scrims, bottom sheets, side sheets, top app bars, bottom app bars, and toolbars.
- Pickers and menus: menus, submenus, menu buttons, search bars, search views, date pickers, date-range pickers, time pickers, and picker fields.

### Demo And Visual Validation

- The demo app is organized as Material component pages with documentation links to `https://m3.material.io/`.
- The demo includes standard, dark, expressive, and right-to-left visual paths.
- Visual smoke tests render registered demo pages, check CSS warnings, inspect layout bounds, sample pixels for important states, and exercise interaction frames.
- Core control tests cover tokens, CSS metadata, fallback stylesheets, accessibility, focus traversal, mixed popup focus routing, RTL behavior, animation enablement, reduced motion, and styleable metrics.

## Release Readiness

- The library is a release candidate for baseline Material Design 3 plus documented M3 Expressive token, profile, motion, and component support.
- Before 1.0, complete final human API sign-off across the module export surface: `org.glavo.m3fx.animation`, `org.glavo.m3fx.controls`, `org.glavo.m3fx.theme`, and `org.glavo.m3fx.tokens`. The exported package and public type inventory is covered by project contract tests.
- Full M3 Expressive visual parity for every component is explicitly deferred beyond the 1.0 baseline; 1.0 documents the supported Expressive token/profile behavior and keeps component parity work incremental.
- Before 1.0, run a final component-by-component visual pass on the demo in standard, expressive, dark, and right-to-left modes.
- Before 1.0, rerun final release validation after the final source, stylesheet, token, demo, or build-logic change.
- Before publishing runtime images, rerun all-platform and all-architecture jlink validation after any jlink, packaging, module, or dependency change.

## Next Goals

- Finish the public API naming and package-surface review.
- Continue improving M3 Expressive parity for components whose official target values are stable and whose visual states can be covered by rendered tests.
- Visual and animation validation is tied to semantic states, stable animation pulses, rendered-pixel changes, and real focus or pointer interactions.
- Audit less common mixed popup stacks for keyboard and accessibility parity, especially combinations of menus, pickers, tooltips, dialogs, sheets, snackbars, and search surfaces.
- Demo page validation targets normal, selected, focused, pressed, disabled, RTL, and reduced-motion states for each implemented component where those states apply.

## Validation Entry Points

- `compileJava` validates main source compilation.
- `compileTestJava` validates test source compilation.
- `test` validates unit, behavior, visual, accessibility, and snapshot tests.
- `check` validates compilation, tests, publication metadata, publication artifact layout, and build-local publication consumption.
- `releaseCheck` validates the library publication path, demo visual and behavior tests, executable demo shadow jar, and default demo jlink runtime image structure.
- `shadowDemoJar` validates executable demo jar packaging without bundled JavaFX.
- `jlinkDemoRuntime` validates the default demo runtime image.
- `jlinkDemoAllPlatformArchitectureRuntimes` validates Windows, Linux, and macOS runtime images on x64 and AArch64.

## Out Of Scope For Now

- Web deployment for the JavaFX demo.
- SASS or another CSS preprocessor layer.
- High-complexity data components such as data tables.
