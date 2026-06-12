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
- M3 Expressive cards, dialogs, snackbars, and app bars use profile-specific container shape, padding, height, and content/action slot metrics.
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
- Standalone controls install a low-priority fallback token stylesheet on their scene and ensure scene, dialog, menu, submenu, picker, and tooltip popup roots match fallback token declarations, so per-control user-agent styles resolve default Material colors before an application theme is installed.
- Baseline and expressive profile hooks exist for shape and component token evolution.
- Reusable Material motion constants, easing curves, semantic specs, standard/expressive motion schemes, and motion behavior timings exist for JavaFX animations and motion-adjacent interaction delays.
- Runtime motion settings expose a settings revision and explicit change listeners so controls with long-running animation loops can refresh immediately when global or node-local animation switches, motion schemes, or behavior timings change.
- Scene-aware motion settings observers centralize listener registration for controls, skins, state layers, effect transitions, scroll smoothing, and popup/overlay animation owners, with explicit disposal for skin and helper lifecycles.
- State layers, ripples, and CSS-resolved elevation transitions resolve the installed theme motion scheme when available. Running state-layer opacity, ripple, and CSS-resolved effect animations settle immediately when inherited animation settings are disabled at runtime.
- State layer opacity is resolved from installed state-layer tokens plus owner interaction properties and pseudo-classes so hover, focus-visible, pressed, and armed feedback remain consistent across custom controls, themed overrides, and test-driven pseudo-class states.
- Skin-owned finite state transitions for selection controls, button pressed scale, sliders, tabs, navigation items, list items, disclosure icons, badges, and navigation drawer groups settle active animation frames immediately when inherited animations are disabled at runtime.
- Carousel selected-item scroll transitions observe runtime motion settings while attached to a scene, finish active scroll animations when inherited animations are disabled, and clear completed animation references.
- Control-owned finite overlay and popup transitions for scrims, sheets, snackbar hosts, search results, FAB menus, menu buttons, submenus, and picker fields settle active animation frames immediately when inherited animations are disabled at runtime. FAB menu expand and collapse transitions also clear completed transition references.
- Selection controls, indicators, text input details, popup surfaces, sheets, snackbars, search results, FAB menus, carousel scrolling, and determinate progress transitions now resolve semantic motion specs from the active motion scheme.
- Text input layout label, clear-button, and supporting-row transitions observe runtime animation settings while attached to a scene and settle active finite animations immediately when inherited animations are disabled.
- Popup-hosted menus and pickers propagate scene-level or local parent theme context, profile and brightness style classes, animation settings, the active motion scheme, and motion behavior timings into their popup content roots, including runtime updates while the popup remains open.
- Dialogs can inherit scene or owner-node local theme context, so picker dialogs and other dialog flows launched from locally themed subtrees keep the expected color and profile context.
- Tooltip timing, tooltip popup content, snackbar display duration, submenu hover delays, type-ahead search reset delays, indeterminate progress cycle durations, and loading indicator morph/rotation loops resolve profile-specific motion behavior timings where they are controlled by a scene owner.
- Type-ahead reset timers for static lists, virtualized lists, menus, and navigation drawers, submenu hover open and close timers, installed tooltip show, hide, and visible-duration timers, and snackbar automatic-dismiss timers refresh when inherited motion behavior changes while the owner remains attached to a scene.
- Indeterminate linear progress, circular progress, and loading indicator loops refresh when inherited runtime animation settings change. Disabling full motion settles determinate transitions and morphing effects, but keeps reduced linear activity loops so indeterminate loading and progress states remain visibly alive.
- JavaFX scroll panes styled through `M3ScrollPanes` and virtualized list view flows support smooth wheel, focus, and programmatic scrolling that resolve the active motion scheme and animation settings, including horizontal-only content such as carousel viewports. Running smooth-scroll animations settle immediately when inherited animation settings are disabled at runtime, restart with updated motion specs when motion settings change while animations remain enabled, and clear completed animation and callback references.
- Progress component tokens include wavy linear and circular amplitude, wavelength, track gap, and linear stop indicator metrics; loading indicator tokens include container size and active indicator size. Applications can override them per control through CSS.
- Button, FAB, icon, connected button group, segmented button group, icon toggle group, tab, chip, and chip group component tokens include Expressive-specific glyph sizing, horizontal padding, spacing, and tab active indicator metrics.
- Connected button group and split button component tokens include Expressive-specific grouped action padding and split menu width.
- Text input, text area, selection, and slider component tokens include Expressive-specific padding and interaction geometry.
- Navigation bar, navigation rail, navigation drawer, and list item component tokens include Expressive-specific content spacing, item spacing, and content padding.
- Menu, search, picker field, date picker, time picker, and sheet component tokens include Expressive-specific container padding, item spacing, result padding, day/time cell sizing, popup sizing, sheet padding, and drag-handle metrics.
- Card, dialog, snackbar, banner, tooltip, top app bar, and bottom app bar component tokens include Expressive-specific shape, padding, height, content/action slot spacing, popup sizing, and top app bar variant metrics.
- Form and validation summary component tokens include Expressive-specific spacing, row sizing, label width, container shape, and invalid-item padding.
- Surface and carousel component tokens include Expressive-specific surface geometry and carousel track/item emphasis metrics.

### Component Coverage

- Foundation and utility visuals: text, icon, disclosure icon, avatar, badge, badged box, divider, surface, card, scrim, and sheets.
- Buttons and actions: button variants, icon button, icon toggle button, button groups, split buttons, FABs, extended FABs, and FAB menus.
- Inputs and forms: text fields, password fields, text areas, text input layouts, validation helpers, validation summaries, form containers, supporting text, errors, counters, leading and trailing adornments, and clear buttons.
- Selection: checkbox, radio button, switch, slider, chips, chip groups, segmented buttons, and segmented button groups.
- Pickers: date picker, date range picker, time picker, picker fields, preset actions, dialogs, range constraints, keyboard adjustment, and accessibility actions.
- Navigation: tabs, tab bar, navigation item, navigation bar, navigation rail, navigation drawer, collapsible navigation drawer groups, and type-ahead drawer navigation.
- App bars: top app bar variants, top app bar scrolled-under state, and bottom app bar with configurable FAB alignment.
- Menus and search: menu, menu item, submenu item, menu sections, menu button, type-ahead menu keyboard navigation, search bar, and search view with editor, result, and action focus routing.
- Feedback and progress: banner, snackbar, snackbar host, plain tooltip, rich tooltip, loading indicator default and contained variants, linear progress, and circular progress.
- Rich tooltips support keyboard transfer from the owner node into the action row, action-row traversal that skips disabled actions, boundary return to the owner, Escape dismissal, and focus restoration.
- Expressive progress indicators render wavy active paths, separated tracks, and linear stop indicators while baseline progress indicators retain flat line and arc geometry.
- Lists: static list/list item support and `VirtualFlow`-backed list views with row reuse, selection, focus navigation, type-ahead navigation, visible-row accessibility focus, accessibility routing, and a large two-line demo list that exercises real virtualized rendering.
- Slot, container, picker-grid, split-button, search, text-input-layout, navigation-drawer, validation-summary, FAB-menu, and popup-backed controls route accessibility focus to the currently focused child when focus is inside their indexed, adorned, grid, or popup-hosted content, route active external popup focus exposed by descendant controls through shared accessibility helpers, actively notify `FOCUS_NODE` when child or popup focus changes, preserve that current child for default `REQUEST_FOCUS` and parameterless `SHOW_ITEM` actions, and fall back to the selected or first reachable focus target when focus is outside the container. Shared reveal helpers delegate explicit `SHOW_ITEM` requests into descendant controls that expose nested popup targets through their accessibility item tree, including ordinary content wrappers inside surfaces and dialog panes. Date range picker fields also track which endpoint opened the popup so Escape and accessible collapse return focus to the matching editor.
- Indexed, slot-based, and popup-backed controls support default accessibility reveal behavior: parameterless `SHOW_ITEM` focuses the selected, current, or first reachable indexed child as appropriate for each control, direct child and descendant `Node` parameters are accepted when contained by an indexed item, menus and submenus prefer selected or active menu items when focus is requested, carousels scroll the selected item while restoring control focus, virtualized list views restore the active row focus target, FAB menus expand before revealing actions, and snackbar hosts refresh hosted snackbar skins before routing focus to current or queued snackbar actions.
- Right-to-left horizontal keyboard traversal mirrors the rendered visual direction for connected button groups, icon toggle groups, chip groups, segmented button groups, tab bars, navigation bars, split buttons, carousels, date pickers, and time pickers.

## Demo And Verification

- The demo app uses a Material navigation drawer sidebar with collapsible component groups aligned with the Material component organization.
- Demo pages cover major control families, common variants, disabled states, selected states, error states, composite workflows, and animated progress examples.
- Progress demo pages explicitly show standard geometry alongside expressive wavy geometry using per-control CSS token overrides; the loading indicator page shows the dedicated loading control in the official default and contained forms.
- Unit tests cover style classes, token CSS metadata, skin creation, interaction events, state-layer/ripple behavior, accessibility attributes/actions, selection behavior, and packaging assumptions.
- Snapshot-based visual tests render representative control families into report images and include automated checks for contrast, geometry, clipping, borders, and animation-state frames.
- Recent visual regressions covered by tests include icon fallback glyph clipping, outlined text input notch geometry, focused outlined text input ink alignment with adornments and clear-button transitions, text input runtime no-motion settling, skin-owned and control-owned state-transition settling, segmented button selected borders, progress geometry, selection states, snackbar sizing, trailing icon ripple behavior, and picker cell state-layer feedback for date, date range, and time pickers.
- Expressive profile visual coverage checks real rendered component sizes, action-control padding, input padding, selection/slider geometry, tab indicators, profile root classes, fixed-target centering, date-cell alignment, menu/search/sheet metrics, card/snackbar metrics, and search bar height constraints in mixed-height layouts.
- Dark expressive token-driven visual coverage renders representative action, input, selection, feedback, progress, date picker, and navigation controls without overriding generated dark theme colors.
- Dark expressive popup visual coverage opens real menu and tooltip popups, checks inherited profile and brightness mode classes, and writes rendered snapshots for overlay review.
- Expressive progress visual coverage writes dedicated snapshots for determinate and indeterminate wavy linear and circular progress indicators, plus default and contained loading indicator presentation.
- Demo interaction visual coverage checks hover and pressed-state snapshots for buttons, sidebar destinations, and toggle icon buttons, verifies button, toggle icon button, and sidebar ripple-release intermediate fade frames, captures switch thumb-selection plus navigation bar, navigation rail, sidebar drawer-group, bottom sheet, and side sheet intermediate frames with local visual-test motion schemes, opens split button, picker field, and nested submenu popups for enter, settled, and exit snapshots, and covers focus feedback for text fields plus no-motion state changes.
- Overlay visual coverage now exercises snackbar host enter/settled/exit frames, FAB menu expand/collapse frames, rich tooltip interactive pointer transfer with action-button containment, and real dialog popup surface snapshots.
- Demo page-level visual coverage now shares geometry assertions across full-page, dark expressive, and right-to-left passes, waits for the demo sidebar selected indicator to settle before full-page snapshots, verifies visible text, Material control bounds, compact navigation badge placement, and scroll viewport clipping while allowing expected partial scroll-edge content and carousel viewport clipping covered by dedicated carousel tests.
- App bar visual coverage renders all top app bar variants and scrolled-under states with real SVG action icons, writes dedicated top-variant, scrolled-under, and bottom-toolbar snapshots, uses the current Material app bars documentation link, verifies logical icon identities, rejects rendered text placeholders inside icon graphics, verifies leading and trailing icon color roles, 48 dp top and bottom app bar action slots, icon button centering, action slot spacing, title and icon-row geometry, RTL mirroring, contextual top app bar preview content, default top app bars sharing the content surface until scrolled-under elevation applies, bottom app bar START/CENTER/END floating action geometry, and preview surfaces that avoid the generic rounded showcase container.
- Cards demo visual coverage opens the real Cards page, verifies filled, outlined, elevated, media, action, and disabled states, checks card surface geometry and variant treatment, and writes dedicated top and scrolled media/action snapshots for review.
- Carousel demo visual coverage opens the real Carousel page, verifies multi-browse and compact carousel viewports, selected item state, selected item layout visibility inside the viewport, action-driven selection changes, intentional viewport clipping, and writes dedicated initial and after-next-action snapshots for review.
- Dialogs demo visual coverage opens the real Dialogs page, verifies launcher buttons, inline basic/form/scrollable dialog panes, Material action buttons, custom content bounds, and writes dedicated top and scrolled dialog snapshots for review. Dialog pane actions use a fixed Material order instead of platform-specific JavaFX button ordering.
- Search demo visual coverage opens the real Search page, verifies standalone and embedded search bar geometry, active and inactive search view result visibility, keyboard reachability from the editor into the first result row, and writes a dedicated active-search snapshot for review.
- Text Fields demo visual coverage opens the real Text Fields page, verifies filled, outlined, validation, error, password, counter, clear-button, adornment, and text-area layouts, checks outlined floating labels use open notch geometry instead of background masks, and writes a dedicated layout-geometry snapshot for review.
- Selection demo visual coverage opens the real Checkbox, Radio Button, Switch, and Slider pages, verifies enabled, disabled, selected, unchecked, indeterminate, continuous, discrete, and vertical states, checks checkbox mark centering, radio dot centering, switch thumb placement, and slider track/thumb value geometry, and writes dedicated state snapshots for review.
- Menus demo visual coverage opens the real Menus page after the sidebar selection indicator settles, verifies inline menu selected and multi-selected states, opens the menu-button popup and nested submenu with inherited theme context, waits for owner and nested popup roots to reach stable rendered state, checks compact side-by-side popup geometry, verifies SVG icon slots, and writes dedicated inline, owner-popup, and submenu snapshots for review.
- List visual coverage opens the real Lists demo page after the sidebar selection indicator settles, verifies that the data-driven list is backed by `VirtualFlow`, asserts that only a bounded number of reusable cells are attached for 240 rows, scrolls to a far row synchronously, and writes a dedicated virtualized-list snapshot for visual review.
- State-layer and ripple motion tests inspect expansion, release fade, disabled-motion settling, runtime animation-setting changes, and reduced indeterminate activity loops by driving animation timelines directly instead of relying on wall-clock waits.
- Card behavior tests cover passive versus actionable accessible roles, automatic focus traversal for actionable card surfaces, handler replacement, and restoration of the previous focus-traversal state when a card becomes passive again.
- Core popup, overlay, snackbar, tooltip, sheet, scrim, search, progress, submenu, drawer-group, standalone fallback stylesheet, and detached virtualized-list completion tests prefer state-driven JavaFX condition waits over hard-coded elapsed-time waits where the controls expose observable state. Demo visual tests also support stable-condition waits that require final popup, sheet, snackbar, and FAB menu states to remain true for consecutive JavaFX pulses before settled snapshots are captured.
- Demo animation and visual tests capture progress, loading indicator, split-button popup, picker-field popup, nested submenu, hover/pressed interaction feedback, switch selection, navigation selection, drawer group disclosure, sheet visibility, snackbar, FAB-menu, and carousel action frames after real rendered pixel or semantic state changes are detected, so those snapshots no longer rely on fixed early popup, generic interaction, ordinary page-layout, or broad midpoint transition delays. Ripple release coverage combines rendered release-frame pixel changes with semantic opacity fade checks, rich tooltip lifetime coverage relies on stable popup ownership and hidden-state conditions after pointer transfer, and disabled-motion interaction coverage waits for the demo page selection state to settle before sampling immediate hover feedback. Core and demo visual test utilities no longer expose public fixed-delay FX sampling helpers; remaining time values are component motion configuration, direct timeline frame probes, CSS warning drains, or state-driven pulse waits.
- Keyboard traversal tests cover right-to-left horizontal focus and selection behavior across action groups, selection groups, tabs, navigation bars, split buttons, carousels, date pickers, and time pickers. Search control tests cover editor, result, direct and descendant trailing-action focus routing, current child preservation for default focus/reveal actions, complex result-row descendant focus routing, result-collapse focus restoration, unreachable result skipping, and result-list Home/End/PageUp/PageDown movement. Picker-grid tests cover current visible date, range-date, and time cell preservation for default focus/reveal actions in real window focus scenarios. Picker field and dialog preset tests cover vertical preset action traversal, PageUp/PageDown movement, and left-to-right or right-to-left handoff from preset columns into adjacent pickers. Text input layout tests cover input, leading adornment, trailing adornment, clear-button accessibility focus routing, current adornment preservation for default focus/reveal actions, compound adornment descendant focus routing, and focus restoration when a focused adornment disappears. Slot/container tests cover current-child accessibility focus routing, active `FOCUS_NODE` notifications, and default focus/reveal preservation for app bars, banners, surfaces, forms, sheets, split buttons, button groups, navigation drawers, navigation drawer groups, validation summaries, and badged boxes. Popup-backed menu, submenu, split-button menu, picker field, dialog pane, FAB menu, and rich tooltip tests cover cross-scene focus routing, action-row traversal, Escape dismissal, accessible collapse focus restoration, and focus restoration from real child focus targets. Modal sheet tests cover accessible collapse focus restoration and hidden-sheet focus target clearing. Picker field preset popups, date range picker endpoint restoration, and dialog-hosted picker fields now cover nested popup focus routing from real preset action focus targets. Navigation drawer tests cover collapsible group disclosure keys, group-row descendant `FOCUS_NODE` routing, collapsed-group descendant reveal actions, current child default focus preservation, and focus restoration in left-to-right and right-to-left layouts. Static lists, menus, navigation drawers, and virtualized lists cover disabled and hidden row skipping plus PageUp/PageDown focus movement. Composite and structural container tests cover parameterless, indexed, direct-node, and descendant-node accessible reveal actions across button groups, list panes, app bars, banners, surfaces, form rows, badged boxes, FAB menus, carousels, navigation drawers, and snackbar hosts.
- Custom indexed-control accessibility tests cover parameterless reveal behavior for carousels and virtualized list views, including real scene focus restoration.
- Selection-container accessibility tests cover current-child `FOCUS_NODE` routing and parameterless reveal behavior for chip groups, icon toggle groups, segmented button groups, list panes, tab bars, navigation bars, navigation rails, and navigation drawers, ensuring real focused children are reported first and selected items are preferred over the first child when focus is outside the container.
- Popup menu accessibility tests cover selected-item default focus for standalone menus, menu buttons, and submenus without changing explicit first/last keyboard-open behavior. Mixed popup focus tests cover rich tooltip actions opened from menu items and nested submenu items while the parent menu stack remains active, dialog-pane focus routing through nested menu-button and picker popups, shared external popup focus routing through indexed containers, surface content subtrees, and slot-based app bars, plus explicit reveal of closed nested menu targets from those composite owners. Dialog pane accessibility tests cover current content/action preservation for parameterless focus and reveal actions, including picker dialog preset and picker-content targets plus nested picker popups exposed through ordinary content containers. Picker field accessibility tests cover open-button focus exposure and preservation of focused preset actions for direct `REQUEST_FOCUS` and parameterless `SHOW_ITEM` actions.

## Next Goals

- Continue reducing the remaining intentional timing samples in interaction visual tests, especially direct timeline frame probes, by replacing them with semantic or rendered-pixel conditions where a stable target state exists.
- Continue filling component gaps and richer composite workflows where existing controls are still shallow.
- Audit less common mixed popup stacks for keyboard parity, especially nested overlay combinations that combine menus, pickers, tooltips, and dialogs.
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
