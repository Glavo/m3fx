# M3FX Project Plan

## Purpose

M3FX is a modular Material Design 3 component library for JavaFX. The project is still early enough to favor clean API and implementation shape over compatibility with earlier internal versions.

This file tracks product status and planning only. Repository rules, code style, nullability, documentation, Gradle invocation, and commit-message requirements belong in `AGENTS.md`.

## Current Baseline

- Java source and bytecode target: Java 17.
- Default JavaFX dependency for local builds and the demo app: JavaFX 21.
- Public implementation should stay compatible with JavaFX 14 APIs unless newer APIs are guarded by runtime checks or reflection.
- JavaFX is an application-owned dependency and is not published as an API dependency of the library.
- Material colors are generated through `org.glavo:MonetFX:0.4.0`.
- The demo app is a separate Gradle subproject.
- Theme and token APIs are token-first and profile-aware, with baseline M3 and M3 Expressive profiles represented separately.
- M3 Expressive currently has profile-specific color, typography, shape, component sizing, semantic motion scheme tokens, and motion behavior timings.
- M3 Expressive progress indicators use wavy linear and circular indicator geometry through profile-specific component tokens, and loading indicators use a dedicated animated shape sequence control with profile-specific metrics and default/contained variants.
- M3 Expressive action controls use profile-specific button padding, connected button padding and spacing, split button menu sizing, FAB extended padding and menu spacing, segmented button padding, icon toggle spacing, tab indicator metrics, icon sizes, chip padding, and chip group spacing.
- M3 Expressive inputs, selection controls, and sliders use profile-specific input padding, touch target, track thickness, and thumb sizing.
- M3 Expressive navigation and list components use profile-specific spacing, padding, and selected-container shape tokens.
- M3 Expressive menus, search surfaces, picker surfaces, and sheets use profile-specific padding, item spacing, result spacing, day/time cell sizing, popup sizing, and drag-handle metrics.
- M3 Expressive forms and validation summaries use profile-specific row spacing, section spacing, label width, row height, summary padding, and invalid-item geometry.
- M3 Expressive surfaces and carousels use profile-specific surface padding, container shape, carousel track padding, item spacing, item opacity, and selected-item shadow metrics.
- M3 Expressive cards, dialogs, snackbars, and app bars use profile-specific container shape, padding, height, and action spacing metrics.
- Theme installation marks roots with profile and brightness style classes so applications and demo pages can branch CSS for baseline, expressive, light, and dark modes without replacing the theme API.

## Architecture

- `org.glavo.m3fx.theme` owns theme creation, scene installation helpers, stylesheet installation, and token stylesheet generation.
- `org.glavo.m3fx.tokens` owns color, typography, shape, elevation, motion, state-layer, density, profile, and component token groups.
- Public theme and token abstractions use sealed interfaces; implementations live in internal packages.
- `org.glavo.m3fx.animation` owns reusable Material motion durations and easing curves.
- `org.glavo.m3fx.internal` owns shared runtime infrastructure such as theme resolution and generated stylesheet caching.
- `org.glavo.m3fx.controls` owns public controls, foundation primitives, and composition containers.
- `org.glavo.m3fx.skins` owns custom skins for layout, drawing, interaction, state layers, ripple, and animation behavior.
- Foundation primitives such as `M3Icon` and `M3Text` support components but are not standalone Material component pages.
- `M3Icon` is an icon glyph primitive for Material Symbols or fallback text; standard component mappings belong to icon buttons, navigation items, app bars, menus, lists, and other controls that use icon slots.
- Controls use per-control user-agent stylesheets where JavaFX supports them. Popup-only styling remains in dedicated control CSS files loaded through the base stylesheet.
- `M3ThemeManager` is a convenience installer, not a required runtime dependency for applications.
- The root style classes `m3-profile-baseline`, `m3-profile-expressive`, `m3-light`, and `m3-dark` describe the installed theme mode for application-level CSS.

## Implemented Areas

### Build And Distribution

- Multi-project Gradle build for the library and demo app.
- Java module descriptors for the library and demo app.
- Demo run, shadow jar, and jlink runtime-image tasks.
- Demo shadow jar excludes JavaFX artifacts.
- Demo shadow jar verification checks for an executable manifest and rejects bundled JavaFX entries.
- jlink support uses BellSoft LibericaJDK Full jmods.
- Platform and architecture-specific jlink tasks cover Windows, Linux, and macOS on x64 and AArch64.
- Runtime packaging choices for library usage, demo shadow jars, and jlink images are documented in `docs/PACKAGING.md`.
- GitHub Actions builds the demo shadow jar and uploads it as an artifact.

### Theme, Tokens, And Motion

- MonetFX-backed Material color mapping.
- Root CSS token generation for color, typography, shape, elevation, motion, state layers, density, and component defaults.
- Generated token stylesheets use stable content hashing.
- Standalone controls install a low-priority fallback token stylesheet on their scene so per-control user-agent styles resolve default Material colors before an application theme is installed.
- Baseline and expressive profile hooks exist for shape and component token evolution.
- Reusable Material motion constants, easing curves, semantic specs, standard/expressive motion schemes, and motion behavior timings exist for JavaFX animations and motion-adjacent interaction delays.
- Runtime motion settings expose a settings revision and explicit change listeners so controls with long-running animation loops can refresh immediately when global or node-local animation switches, motion schemes, or behavior timings change.
- State layers, ripples, and CSS-resolved elevation transitions resolve the installed theme motion scheme when available. Running state-layer opacity, ripple, and CSS-resolved effect animations settle immediately when inherited animation settings are disabled at runtime.
- State layer opacity is resolved from installed state-layer tokens plus owner interaction properties and pseudo-classes so hover, focus-visible, pressed, and armed feedback remain consistent across custom controls, themed overrides, and test-driven pseudo-class states.
- Selection controls, indicators, text input details, popup surfaces, sheets, snackbars, search results, FAB menus, carousel scrolling, and determinate progress transitions now resolve semantic motion specs from the active motion scheme.
- Popup-hosted menus and pickers propagate scene-level or local parent theme context, profile and brightness style classes, animation settings, the active motion scheme, and motion behavior timings into their popup content roots.
- Dialogs can inherit scene or owner-node local theme context, so picker dialogs and other dialog flows launched from locally themed subtrees keep the expected color and profile context.
- Tooltip timing, tooltip popup content, snackbar display duration, submenu hover delays, type-ahead search reset delays, indeterminate progress cycle durations, and loading indicator morph/rotation loops resolve profile-specific motion behavior timings where they are controlled by a scene owner.
- Indeterminate linear progress, circular progress, and loading indicator loops stop and restart when inherited runtime animation settings change, including subtree-level animation disabling.
- JavaFX scroll panes styled through `M3ScrollPanes` and virtualized list view flows support smooth wheel, focus, and programmatic scrolling that resolve the active motion scheme and animation settings, including horizontal-only content such as carousel viewports. Running smooth-scroll animations settle immediately when inherited animation settings are disabled at runtime and restart with updated motion specs when motion settings change while animations remain enabled.
- Progress component tokens include wavy linear and circular amplitude, wavelength, track gap, and linear stop indicator metrics; loading indicator tokens include container size and active indicator size. Applications can override them per control through CSS.
- Button, FAB, icon, connected button group, segmented button group, icon toggle group, tab, chip, and chip group component tokens include Expressive-specific glyph sizing, horizontal padding, spacing, and tab active indicator metrics.
- Connected button group and split button component tokens include Expressive-specific grouped action padding and split menu width.
- Text input, text area, selection, and slider component tokens include Expressive-specific padding and interaction geometry.
- Navigation bar, navigation rail, navigation drawer, and list item component tokens include Expressive-specific content spacing, item spacing, and content padding.
- Menu, search, picker field, date picker, time picker, and sheet component tokens include Expressive-specific container padding, item spacing, result padding, day/time cell sizing, popup sizing, sheet padding, and drag-handle metrics.
- Card, dialog, snackbar, banner, tooltip, top app bar, and bottom app bar component tokens include Expressive-specific shape, padding, height, action spacing, popup sizing, and top app bar variant metrics.
- Form and validation summary component tokens include Expressive-specific spacing, row sizing, label width, container shape, and invalid-item padding.
- Surface and carousel component tokens include Expressive-specific surface geometry and carousel track/item emphasis metrics.

### Component Coverage

- Foundation and utility visuals: text, icon, disclosure icon, avatar, badge, badged box, divider, surface, card, scrim, and sheets.
- Buttons and actions: button variants, icon button, icon toggle button, button groups, split buttons, FABs, extended FABs, and FAB menus.
- Inputs and forms: text fields, password fields, text areas, text input layouts, validation helpers, validation summaries, form containers, supporting text, errors, counters, leading and trailing adornments, and clear buttons.
- Selection: checkbox, radio button, switch, slider, chips, chip groups, segmented buttons, and segmented button groups.
- Pickers: date picker, date range picker, time picker, picker fields, preset actions, dialogs, range constraints, keyboard adjustment, and accessibility actions.
- Navigation: tabs, tab bar, navigation item, navigation bar, navigation rail, navigation drawer, collapsible navigation drawer groups, and type-ahead drawer navigation.
- App bars: top app bar variants and bottom app bar with configurable FAB alignment.
- Menus and search: menu, menu item, submenu item, menu sections, menu button, type-ahead menu keyboard navigation, search bar, and search view with editor, result, and action focus routing.
- Feedback and progress: banner, snackbar, snackbar host, plain tooltip, rich tooltip, loading indicator default and contained variants, linear progress, and circular progress.
- Expressive progress indicators render wavy active paths, separated tracks, and linear stop indicators while baseline progress indicators retain flat line and arc geometry.
- Lists: static list/list item support and `VirtualFlow`-backed list views with row reuse, selection, focus navigation, type-ahead navigation, visible-row accessibility focus, and accessibility routing.
- Slot, container, and popup-backed controls route accessibility focus to the currently focused child when focus is inside their indexed or popup-hosted content, actively notify `FOCUS_NODE` when child focus changes, and fall back to the first reachable focus target for default focus requests.
- Right-to-left horizontal keyboard traversal mirrors the rendered visual direction for connected button groups, icon toggle groups, chip groups, segmented button groups, tab bars, navigation bars, split buttons, carousels, date pickers, and time pickers.

## Demo And Verification

- The demo app uses a Material navigation drawer sidebar with collapsible component groups aligned with the Material component organization.
- Demo pages cover major control families, common variants, disabled states, selected states, error states, composite workflows, and animated progress examples.
- Progress demo pages explicitly show standard geometry alongside expressive wavy geometry using per-control CSS token overrides; the loading indicator page shows the dedicated loading control in the official default and contained forms.
- Unit tests cover style classes, token CSS metadata, skin creation, interaction events, state-layer/ripple behavior, accessibility attributes/actions, selection behavior, and packaging assumptions.
- Snapshot-based visual tests render representative control families into report images and include automated checks for contrast, geometry, clipping, borders, and animation-state frames.
- Recent visual regressions covered by tests include icon fallback glyph clipping, outlined text input notch geometry, segmented button selected borders, progress geometry, selection states, snackbar sizing, trailing icon ripple behavior, and picker cell state-layer feedback for date, date range, and time pickers.
- Expressive profile visual coverage checks real rendered component sizes, action-control padding, input padding, selection/slider geometry, tab indicators, profile root classes, fixed-target centering, date-cell alignment, menu/search/sheet metrics, card/snackbar metrics, and search bar height constraints in mixed-height layouts.
- Dark expressive token-driven visual coverage renders representative action, input, selection, feedback, progress, date picker, and navigation controls without overriding generated dark theme colors.
- Dark expressive popup visual coverage opens real menu and tooltip popups, checks inherited profile and brightness mode classes, and writes rendered snapshots for overlay review.
- Expressive progress visual coverage writes dedicated snapshots for determinate and indeterminate wavy linear and circular progress indicators, plus default and contained loading indicator presentation.
- Demo interaction visual coverage checks hover and pressed-state snapshots for buttons, sidebar destinations, and toggle icon buttons, plus focus feedback for text fields and no-motion state changes.
- State-layer and ripple motion tests inspect expansion, release fade, disabled-motion settling, and runtime animation-setting changes by driving animation timelines directly instead of relying on wall-clock waits.
- Keyboard traversal tests cover right-to-left horizontal focus and selection behavior across action groups, selection groups, tabs, navigation bars, split buttons, carousels, date pickers, and time pickers. Search control tests cover editor, result, trailing action focus routing, unreachable result skipping, and result-list Home/End/PageUp/PageDown movement. Slot/container tests cover current-child accessibility focus routing and active `FOCUS_NODE` notifications for app bars, banners, snackbars, surfaces, forms, sheets, split buttons, button groups, and badged boxes. Popup-backed menu, submenu, picker field, dialog pane, and rich tooltip tests cover cross-scene focus routing, Escape dismissal, and focus restoration from real child focus targets. Picker field preset popups and dialog-hosted picker fields now cover nested popup focus routing from real preset action focus targets. Navigation drawer tests cover collapsible group disclosure keys and focus restoration in left-to-right and right-to-left layouts. Static lists, menus, navigation drawers, and virtualized lists cover disabled and hidden row skipping plus PageUp/PageDown focus movement. FAB menu tests cover action-item focus traversal, Escape dismissal, and toggle focus restoration after collapse.

## Next Goals

- Audit remaining non-animation timing assumptions in tests and visual snapshots so they describe semantic behavior instead of hard-coded milliseconds.
- Increase page-level visual coverage for the demo, especially for alignment, clipping, and animated intermediate states.
- Continue filling component gaps and richer composite workflows where existing controls are still shallow.
- Tighten accessibility behavior for remaining overlay flows, especially keyboard parity and focus routing across mixed popup stacks.
- Improve M3 Expressive parity with additional profile-specific component tokens, expressive containment, and visual verification for remaining component families as target values become stable.

## Validation Entry Points

- `compileJava` validates main source compilation.
- `test` validates unit, behavior, snapshot, and packaging tests.
- `shadowDemoJar` validates executable demo jar packaging without bundled JavaFX.
- `jlinkDemoRuntime` validates the default demo runtime image.
- Platform jlink tasks validate runtime images for Windows, Linux, and macOS.
- Architecture jlink tasks validate fixed x64 and AArch64 runtime images for each supported platform.

## Out Of Scope For Now

- Full M3 Expressive visual parity for every component.
- Web deployment for the JavaFX demo.
- SASS or another CSS preprocessor layer.
- High-complexity data components such as data tables.
