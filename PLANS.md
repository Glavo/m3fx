# M3FX Roadmap

## Current Position

M3FX is a broadly implemented preview of a Material Design 3 component library for JavaFX. It has a token-driven
theme and motion system, broad Standard and Expressive component coverage, an exhaustive verification demo, an
independent focused Catalog application, rendered state tests, and release packaging for modular applications. The
project is ready for systematic integration testing, but it is not yet a release candidate.

Scene-scoped presentation now uses one stable `M3OverlayPane` root. It owns ordinary content, snackbar presentation,
regular overlays, and a modal stack; dialogs and custom surfaces retain lifecycle handles instead of replacing the
`Scene` root or mutating a shared overlay list. The Demo and Catalog applications use this integration model, and
real-window tests cover input blocking, focus restoration, accessibility isolation, queue ownership, and rendering.

The remaining risk is concentrated in specification fidelity and runtime quality rather than missing infrastructure:

- Some component variants and transitional states still require direct comparison with the official Material Design
  3 specification.
- Visual automation covers the catalog broadly, but important geometry, color, clipping, and animation defects still
  need stronger assertions than screenshot creation alone.
- Hot animation, layout, popup, and virtualization paths require a final allocation and lifetime audit.
- Public API, JavaFX 14 runtime compatibility, packaging, and cross-platform workflows require one final release
  review after component behavior is stable.

The current component set includes the six Material carousel layouts. Full-screen carousels use vertical,
viewport-sized pagination; the remaining layouts retain horizontal Material keyline behavior.

The exported layout foundation now models all five Material width breakpoints and provides an adaptive scaffold
with stable top, bottom, navigation, rail, and one- to three-pane slots. It supports automatic and explicit pane
policies, centered split panes, fixed side panes, physical safety insets, logical RTL placement, navigation
coordination, and focus repair when an active region becomes hidden. The Catalog uses this scaffold as a real
application integration path.

The exported API has completed a pre-freeze structural review. Public theme and token types are immutable data
models with internal implementations, rendering compilers remain internal, configurable token groups use copyable
builders, and composite controls own the properties and events they expose. The API is not frozen until the final
release review confirms that remaining behavior work requires no contract changes.

## Remaining Work

### 1. Close Material Specification Gaps

Audit every public component and supported variant against the official Material Design 3 component pages and
published tokens. Use Standard behavior when no official Expressive replacement exists; do not invent
profile-specific geometry or motion.

The audit must cover:

- Color roles, typography, shape, elevation, density, spacing, and adaptive geometry.
- Hovered, pressed, focused, selected, disabled, error, indeterminate, dragged, expanded, and popup states where
  applicable.
- Keyboard traversal, focus ownership, accessibility semantics, LTR and RTL layout, and pointer or touch interaction.
- Standard, Expressive, and reduced-motion behavior, including interruption and release transitions.
- Composition between related components such as button groups, split buttons, text-field adornments, menus,
  pickers, navigation containers, feedback overlays, loading indicators, and carousels.
- Canonical responsive compositions built on the adaptive scaffold, including list-detail and supporting-pane
  behavior at every breakpoint, without adding device-type assumptions to the layout API.

Close discovered defects by component family so that implementation, demo coverage, and focused regression tests
land together.

### 2. Complete Real-Rendering And Motion Verification

Turn the existing visual catalog matrix into a release gate that detects incorrect output instead of merely proving
that a scene rendered.

- Validate stable geometry and representative pixels for every public component in light and dark themes.
- Exercise normal, hovered, pressed, focused, selected, disabled, error, indeterminate, RTL, and reduced-motion
  states where each state is meaningful.
- Capture animation start, intermediate, release, interrupted, and settled frames for state layers, ripples,
  selection indicators, navigation transitions, progress indicators, and loading indicators.
- Verify real-window behavior for text rasterization, pixel snapping, popups, focus transfer, and platform-specific
  layout differences.
- Keep broad catalog smoke coverage, but use focused assertions for each previously observed regression class.

### 3. Finish Performance And Lifetime Hardening

Profile and inspect rendering hot paths before release.

- Eliminate avoidable per-pulse allocation, repeated CSS invalidation, redundant layout requests, and transient
  collections or shapes in animation code.
- Confirm that reusable transitions stop when controls leave a scene and resume without discontinuity when needed.
- Verify listener, binding, event-filter, popup-owner, queue, and virtualized-cell cleanup during skin replacement,
  scene detachment, popup dismissal, and control disposal.
- Stress large virtualized lists, animated component catalogs, nested popups, rapid theme changes, RTL changes, and
  reduced-motion toggles.
- Retain abstractions only when they remove meaningful duplication or enforce a shared behavioral contract on a
  non-trivial path.

### 4. Complete The Release-Candidate Review

Run this only after the specification, visual, and performance audits are closed.

- Review public constructors, properties, events, CSS metadata, accessibility contracts, and extension points for
  consistency and unnecessary API surface.
- Verify documentation against actual behavior and official Material Design references.
- Recheck JavaFX 14 API compatibility while retaining JavaFX 21 as the default development dependency.
- Validate the modular library, publication metadata, both sample-application shadow JARs without bundled JavaFX,
  host runtime image, cross-platform jlink inputs, and GitHub Actions artifacts.
- Run the complete library, demo, and Catalog suites on supported desktop platforms and resolve all warnings,
  intermittent failures, and rendering regressions before declaring a release candidate.

## Completion Criteria

The first release candidate is ready when:

- Every public component and documented variant has passed the specification audit.
- No known high-visibility geometry, color, state, RTL, focus, popup, or animation defect remains.
- Visual tests assert the regression classes that previously escaped broad screenshot coverage.
- Performance and lifetime checks show no unbounded retention, persistent off-scene animation, or avoidable
  per-frame allocation on common paths.
- The public API and documentation are internally consistent and do not expose unnecessary compatibility layers.
- Library, demo, Catalog, publication, runtime-image, and CI release checks pass from a clean checkout.

## Deferred Scope

- Browser deployment of the JavaFX demo.
- A CSS preprocessor layer.
- High-complexity data components such as data tables.
