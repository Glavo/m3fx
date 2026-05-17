# M3FX Implementation Plan

## Summary

- Build M3FX as a modular Material Design 3 component library for Java 17 and JavaFX 14.
- Use `org.glavo:MonetFX:0.4.0` as the source of Material color system generation.
- Make token design a first-class layer so controls are driven by theme tokens instead of hard-coded Material values.
- Implement the first usable release around core controls: theme system, buttons, text fields, selection controls, progress controls, utility controls, list items, cards, dialogs, and snackbar.
- Reserve a versioned profile model for future M3 Expressive support.

## Key Changes

- Configure Gradle with Java release 17, JavaFX 14, JetBrains annotations, MonetFX, and JUnit.
- Add `module-info.java` with module name `org.glavo.m3fx`.
- Add the public packages:
  - `org.glavo.m3fx.theme`
  - `org.glavo.m3fx.tokens`
  - `org.glavo.m3fx.controls`
  - `org.glavo.m3fx.skins`
- Add the token system:
  - `M3TokenSet` aggregates color, typography, shape, elevation, motion, state layer, and component tokens.
  - `M3ColorTokens` wraps MonetFX `ColorScheme` and maps MonetFX `ColorRole` values to CSS variables.
  - `M3TypographyTokens`, `M3ShapeTokens`, `M3ElevationTokens`, and `M3MotionTokens` provide baseline Material Design 3 defaults.
  - `M3ComponentTokens` stores component-level defaults for implemented controls.
- Add the theme API:
  - `M3Theme` holds the token set, MonetFX `ColorScheme`, selected profile, brightness, and density.
  - `M3ThemeManager` installs base root styles and generated CSS tokens into a `Scene`.
  - Color CSS variables follow MonetFX output. Non-color variables use the `-m3-*` prefix.
- Add versioned profiles:
  - `M3Profile.BASELINE_2021` is the first default profile.
  - `M3Profile.EXPRESSIVE_2025` is reserved for future M3 Expressive token values.
  - Controls must read component tokens and must not hard-code profile-specific values.
- Implement first core controls:
  - Typography: token-driven text labels for the complete Material Design 3 type scale, including line-height tokens.
  - Buttons: filled, tonal, outlined, text, elevated, icon button, toggle icon button, single- or multi-select toggle icon button group, and floating action button.
  - Inputs: filled and outlined text field, plus password field.
  - Selection: checkbox, radio button, switch, slider, chips, chip groups, segmented buttons, and single- or multi-select segmented button groups.
  - Navigation, feedback, utility, and containment: icons, tabs, top app bar, bottom app bar, navigation bar, navigation rail, navigation drawer, navigation item with badges, menus with selectable items, search bar, search view with result content and customizable slots, linear progress, circular progress, divider, badge, badged box, avatar, surface, scrim, list and list item, card, side sheet, bottom sheet, dialog, snackbar, and snackbar host with queued messages.
- Implement controls with JavaFX `Control`, `Skin`, `CssMetaData`, and pseudo-class support.
- Give JavaFX node/control-backed components a control-specific `getUserAgentStylesheet()`. Popup-only utilities that cannot expose this JavaFX hook keep their CSS split into a dedicated file imported by the base stylesheet.

## Public API And Code Style

- Public APIs expose system tokens and component tokens. MonetFX internal packages must not leak into M3FX APIs.
- Every public class, field, and method must use `///` Markdown Javadocs.
- Every Java class must be annotated with `@NotNullByDefault`.
- Nullable values must be explicit with `@Nullable`.
- Java `Optional` must not be introduced.
- Immutable collections, arrays, and views must use the required JetBrains immutability annotations.
- Use Java records where they match the data model.

## Test Plan

- Run `./gradlew -g .gradle-user-home compileJava`.
- Run `./gradlew -g .gradle-user-home test` with a ten-minute timeout for test tasks.
- Cover these scenarios:
  - MonetFX `ColorScheme` maps into M3FX color tokens and CSS variables.
  - Baseline and expressive profiles generate complete token sets.
  - Core controls can be instantiated and keep stable default style classes.
  - Controls read component tokens, so switching token sets does not require replacing control instances.
  - Theme installation is idempotent and does not duplicate stylesheets.
  - Disabled, hover, focused, pressed, and selected states use state layer tokens.

## Assumptions

- MonetFX is consumed as `org.glavo:MonetFX:0.4.0`.
- MonetFX module name is `org.glavo.monetfx`.
- Java and JavaFX baseline is Java 17 / JavaFX 14.
- The first release prioritizes baseline Material Design 3 visuals.
- M3 Expressive support starts with token and profile compatibility, not full visual parity for every component.
- SASS is not introduced in the first implementation pass.
  - Pickers, carousel, data table, and other full MD3 components are out of scope for the first pass.
