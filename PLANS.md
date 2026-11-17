# M3FX Roadmap

See [README.md](README.md) for current capabilities and API usage.

## Remaining Implementation

- Integrate built-in color-control labels and accessibility descriptions with `M3Localizer`,
  including channel names and value units. Preserve application-provided text and preset labels.

## Pre-Release Verification

These areas remain to be verified; they are not a list of confirmed defects.

- Review specification fidelity and representative component compositions across Standard and Expressive,
  light and dark themes, adaptive breakpoints, RTL, and reduced motion. Check interaction states,
  keyboard navigation, focus, accessibility, and interrupted transitions against the relevant contracts.
- Review existing rendering and motion assertions for coverage gaps. Add focused regression checks where
  concrete issues are found, using the tiers and release runs in [TESTING.md](docs/TESTING.md).
- Profile animation, layout, popup, and virtualization paths for allocation and responsiveness; check
  listener, binding, popup, and transition lifetimes during skin replacement, detachment, and disposal.
- Review public API and documentation consistency, and verify JavaFX 17 compatibility alongside the
  default JavaFX 21 development setup.
- Verify release artifacts and launch behavior on supported desktop platforms using
  [PACKAGING.md](docs/PACKAGING.md): the modular library, all three sample shadow JARs
  (`demo`, `catalog`, and `hmcl-md3-demo`, without bundled JavaFX), Demo jlink runtimes,
  Native Image distributions, and CI artifacts.

## Deferred Scope

- General multi-child entry and removal orchestration beyond the existing visibility, content-replacement,
  and placement transitions.
- Key-frame and repetition specifications in the M3FX motion API, decay animation, and shared-element
  overlay transitions.
