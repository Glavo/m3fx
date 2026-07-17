/// Defines the M3FX component catalog application module.
///
/// The module contains a standalone Catalog that follows the AndroidX Material 3 Catalog's Home, Component, and
/// Example hierarchy. It depends on JavaFX Controls for the application UI and on M3FX for controls, themes,
/// motion, and design tokens.
module org.glavo.m3fx.catalog {
    requires javafx.controls;
    requires org.glavo.m3fx;
    requires org.glavo.monetfx;
    requires static org.jetbrains.annotations;

    exports org.glavo.m3fx.catalog;
}
