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
- Global animation settings and subtree reduced-motion requests, with Standard and Expressive motion schemes and
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

### 1. Complete MD3 Expressive Component Parity

Token-profile support is present, but every component must also match the Expressive specification in rendered
geometry, adaptive layout, state color, shape, and motion. The navigation family now includes flexible vertical and
horizontal navigation bars, regular and narrow collapsed rails, standard and modal expanded rails, top- and
center-aligned destination groups, content-hugging and full-width active indicators, and immersive hiding behavior.
Baseline navigation drawers now preserve the specified 360-pixel container and 336-pixel indicator geometry while
providing independent vertical scrolling, focus reveal, logical corners, modal surfaces, and complete destination
state colors. Material Design 3 Expressive does not define a replacement drawer token set; expanded navigation rails
are used instead. Button groups now distinguish content-hugging standard layout from full-width connected layout,
including even flexible growth and the 48-pixel compact connected-item target minimum. Split-button state shape
changes use spatial motion while their disclosure icon retains the standard motion scheme required by the
specification. FABs now expose the published 40-, 56-, 80-, and 96-pixel size scale, tonal and solid color roles,
size-specific icon and extended-label spacing, and logical RTL padding. FAB menus use 56-pixel labeled actions,
four-pixel action spacing, an eight-pixel close gap, paired tonal and solid color families, logical trailing
alignment, and a dedicated close-button transition. Chip metrics remain on the published baseline token set because
the current Expressive specification does not define a replacement size scale.
The slider family now provides the published five-size Expressive scale, centered and range selection, discrete
stops, value indicators, dual-handle keyboard and accessibility behavior, and logical RTL interaction. Inset track
graphics switch between active and inactive segments according to available space, while handle-bound focus
indication and pressed-handle geometry replace the deprecated slider state-layer and ripple treatment. Track gaps
are measured from the visible handle edge, and end stops retain the specified four-pixel outer spacing across all
sizes and orientations.
Selection controls use the published baseline component metrics and state colors in both profiles. Checkboxes and
radio buttons preserve their complete selected, indeterminate, error, disabled, focus-visible, and RTL behavior;
switches additionally support direct handle dragging with destination-state visual preview, release-time value
commit, pressed-handle growth, logical RTL movement, and a track-shaped focus indicator. The current Expressive
specification does not publish a separate selection-control size scale.
Progress indicators now use the published Flat treatment by default in both profiles, with Expressive Wavy geometry
available through explicit component configuration. Linear and circular indicator sizes, determinate and
indeterminate wavelengths, track gaps, stops, inactive tracks, and reduced-motion activity follow the published
tokens. Loading indicators provide the Expressive Default and Contained variants, retain centered reusable morph
geometry, and fall back to basic continuous rotation when decorative motion is disabled.
Remaining component families will be audited against the local Material reference snapshot rather than inferred
from baseline behavior.

### 2. Finish The Component-State Visual Audit

Review every public component in Standard and Expressive profiles across all applicable interaction states. Visual
checks must validate geometry and representative pixels, not only confirm that a screenshot was produced. Animated
components require stable start, intermediate, release, and settled frames so malformed transitional geometry is
detectable.

### 3. Complete Performance And Lifetime Review

Audit skins, popup owners, animation loops, virtualized cells, and shared observers for retained listeners, stale
scene references, unnecessary per-pulse allocation, and avoidable layout or CSS invalidation. Prefer reusable
animation state and direct layout calculations on hot paths, while keeping public APIs and ownership boundaries
clear.

### 4. Release-Candidate Verification

After the visual, motion, performance, and lifetime audits are complete, run the complete library and demo test
suites, publication checks, demo shadow-JAR verification, and host runtime-image validation. Cross-platform runtime
images must be revalidated when build logic, modules, JavaFX resolution, or packaging changes.

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
