/// Defines the exhaustive M3FX component demonstration application.
///
/// The application presents control variants, interaction states, theme profiles, bidirectional layouts, and motion
/// settings using the organization of the [Material Design component catalog](https://m3.material.io/components).
/// It is a standalone sample module and is not required by applications that consume the M3FX library.
module org.glavo.m3fx.demo {
    requires javafx.controls;
    requires org.glavo.m3fx;
    requires org.glavo.monetfx;
    requires static org.jetbrains.annotations;

    exports org.glavo.m3fx.demo;
}
