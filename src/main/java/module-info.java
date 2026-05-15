/// Defines the m3fx Material Design 3 JavaFX component library module.
module org.glavo.m3fx {
    requires transitive javafx.controls;
    requires transitive javafx.graphics;
    requires transitive org.glavo.monetfx;
    requires static org.jetbrains.annotations;

    exports org.glavo.m3fx.controls;
    exports org.glavo.m3fx.skins;
    exports org.glavo.m3fx.theme;
    exports org.glavo.m3fx.tokens;
}
