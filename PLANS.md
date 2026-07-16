# M3FX Project Plan

## Product Scope

M3FX is a modular Material Design 3 component library for JavaFX. The release target includes baseline Material
Design 3 and complete Material Design 3 Expressive support where the published specification defines stable
component tokens, layouts, states, and motion behavior.

The library uses MonetFX for dynamic Material color generation. Applications own the JavaFX runtime; M3FX does
not bundle or publish JavaFX as a runtime dependency. The default development baseline is JavaFX 21 with Java 17
source and bytecode, while public implementation code remains compatible with JavaFX 14 APIs unless a newer API
is guarded at runtime.

## Architecture

- `org.glavo.m3fx.theme` installs themes and generates root token stylesheets.
- `org.glavo.m3fx.tokens` defines color, typography, shape, elevation, motion, state, density, profile, and
  component tokens. Public token abstractions are sealed interfaces with implementations in internal packages.
- `org.glavo.m3fx.animation` provides Material motion schemes, durations, easing curves, behavior timings, and
  runtime animation settings.
- `org.glavo.m3fx.controls` contains the public component APIs and composition primitives.
- `org.glavo.m3fx.skins` owns rendering, layout, interaction, state layers, ripples, and component animation.
- `org.glavo.m3fx.internal` contains shared runtime infrastructure, generated stylesheet caching, popup context
  propagation, focus handling, logical layout, and low-level animation support.
- The demo is an independent Gradle subproject and serves as the component catalog and visual review surface.

M3FX controls use custom skins based on JavaFX skin base classes. Text inputs retain JavaFX text-input base
classes for editing, selection, clipboard, IME, and undo/redo behavior; other components avoid concrete JavaFX
control subclasses where M3FX owns the behavior surface.

## Implemented Foundation

### Theme And Motion

- MonetFX-backed light and dark color schemes.
- Baseline and Expressive profiles for system and component tokens.
- Token-driven typography, shape, elevation, density, state layers, and component metrics.
- Application-wide and subtree-inherited reduced-motion requests, with Standard and Expressive motion schemes and
  behavior timings supplied by the active theme tokens.
- Reduced-motion behavior for transitions, overlays, scrolling, progress indicators, and loading indicators.
- Theme and direction propagation into popup roots.

### Components

- Buttons, icon buttons, FABs, FAB menus, button groups, split buttons, segmented buttons, tabs, and chips.
- Checkboxes, radio buttons, switches, standard, centered, and range sliders, progress indicators, and loading
  indicators.
- Text fields, password fields, text areas, validation, and form composition.
- Navigation bars, navigation rails, navigation drawers, lists, virtualized list views, and carousels.
- Menus, search, date pickers, date-range pickers, time pickers, and picker fields.
- App bars, toolbars, cards, surfaces, badges, avatars, and dividers.
- Dialogs, sheets, banners, snackbars, scrims, and tooltips.

### Build And Distribution

- Modular library and demo builds.
- Demo shadow JAR without bundled JavaFX.
- Host and cross-platform jlink runtime images for Windows, Linux, and macOS on x64 and AArch64.
- Maven publication metadata and artifact verification.
- GitHub Actions release validation and visual-report artifacts.

### Verification

- Unit and behavior coverage for tokens, CSS metadata, accessibility, keyboard traversal, RTL layout, popup focus,
  animation settings, reduced motion, and disposal behavior.
- Rendered control-state checks for normal, selected, focused, hovered, pressed, disabled, RTL, and animated states.
- A demo visual matrix covering every registered component page across Standard and Expressive profiles, light and
  dark themes, RTL, and reduced-motion configurations.
- Packaging checks for publication artifacts, the demo shadow JAR, and jlink runtime images.

## Remaining Release Work

Implementation coverage is broad, but release readiness remains unproven until the following audits and validation
gates are complete.

### 1. Finish Material Component Parity

Compare every public component and supported variant with the locally captured Material Design 3 specification. Verify
token roles, geometry, adaptive layout, interaction semantics, accessibility, RTL behavior, and motion. Expressive
variants must use published Expressive tokens and patterns; components without a published Expressive replacement
must retain their baseline Material behavior rather than receiving invented profile-specific metrics.

Complete integrated regression of the recently revised app bars, toolbars, tabs, sheets, snackbars, and tooltips.
Corrections should preserve existing API ownership boundaries and avoid compatibility layers that are unnecessary
before the first stable release.

### 2. Finish The Component-State Visual Audit

Review every public component in Standard and Expressive profiles across all applicable normal, hovered, pressed,
focused, selected, disabled, error, indeterminate, dragged, expanded, and popup states. Repeat representative checks
in light and dark themes, LTR and RTL orientation, and normal and reduced-motion configurations.

Visual checks must validate geometry and representative pixels, not only confirm that a screenshot was produced.
Animated components require stable start, intermediate, release, and settled frames so malformed transitional
geometry, clipping, jumps, and stale state layers remain detectable.

### 3. Complete Performance And Lifetime Review

Audit skins, popup owners, animation loops, virtualized cells, and shared observers for retained listeners, stale
scene references, unnecessary per-pulse allocation, and avoidable layout or CSS invalidation. Prefer reusable
animation state and direct layout calculations on hot paths. Verify that skin replacement, scene detachment, popup
dismissal, and queue replacement release all component-owned observers and transient nodes.

### 4. Complete Release-Candidate Verification

Run the complete library and demo test suites after the parity, visual, performance, and lifetime audits are closed.
Then validate publication metadata and artifacts, the demo shadow JAR, the host runtime image, and the GitHub Actions
workflow. Revalidate cross-platform runtime images whenever build logic, modules, JavaFX resolution, or packaging
changes.

## Validation Entry Points

- `compileJava` validates main source compilation.
- `compileTestJava` validates test source compilation.
- `test` validates library behavior, accessibility, interaction, and rendered states.
- `:demo:test` validates demo pages and real-window visual behavior.
- `check` validates tests and publication artifacts.
- `releaseCheck` validates the release path, demo packaging, and the default runtime image.
- `shadowDemoJar` validates the executable demo JAR without bundled JavaFX.
- `jlinkDemoRuntime` validates the default demo runtime image.
- `jlinkDemoAllPlatformArchitectureRuntimes` validates all supported operating-system and architecture pairs.

## Out Of Scope

- Web deployment of the JavaFX demo.
- A CSS preprocessor layer.
- High-complexity data components such as data tables.
