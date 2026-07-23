/// Defines the HMCL Material Design 3 demonstration application.
///
/// The module presents a focused launcher shell built with M3FX and selected artwork from HMCL. It is a standalone
/// sample application and is not required by applications that consume the M3FX library.
module org.glavo.m3fx.hmcl.demo {
    requires javafx.controls;
    requires org.glavo.m3fx;
    requires org.glavo.monetfx;
    requires static org.jetbrains.annotations;

    exports org.glavo.m3fx.hmcl.demo;
}
