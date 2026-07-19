/// Defines the M3FX Material Design 3 component library for JavaFX.
///
/// The module exports controls, motion primitives, theme management, and immutable design-token APIs. Applications
/// normally create controls from [org.glavo.m3fx.controls], install an [org.glavo.m3fx.theme.M3Theme] with
/// [org.glavo.m3fx.theme.M3ThemeManager], and use [org.glavo.m3fx.tokens] only when constructing a custom token set.
/// [org.glavo.m3fx.animation] provides the public motion scheme and the per-node reduced-motion policy.
///
/// JavaFX Controls and Graphics are transitive requirements because exported M3FX APIs expose JavaFX nodes,
/// properties, paints, and animation types. MonetFX is transitive because [org.glavo.m3fx.theme.M3Theme] exposes its
/// dynamic color scheme. JetBrains annotations are a compile-time-only requirement used to publish nullability and
/// immutability contracts.
///
/// M3FX controls follow the JavaFX scene-graph threading model: once attached to a showing scene, they must be
/// accessed from the JavaFX Application Thread. See [Material Design](https://m3.material.io/) for the component and
/// design-token specifications implemented by this module.
module org.glavo.m3fx {
    requires transitive javafx.controls;
    requires transitive javafx.graphics;
    requires transitive org.glavo.monetfx;
    requires static org.jetbrains.annotations;

    exports org.glavo.m3fx.animation;
    exports org.glavo.m3fx.controls;
    exports org.glavo.m3fx.theme;
    exports org.glavo.m3fx.tokens;

    provides java.net.spi.URLStreamHandlerProvider
            with org.glavo.m3fx.internal.theme.M3StylesheetUrlStreamHandlerProvider;
}
