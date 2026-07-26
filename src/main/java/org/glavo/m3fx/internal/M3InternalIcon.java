// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.List;
import java.util.Objects;

/// An internal token-colored SVG icon used for built-in component affordances.
///
/// Built-in arrows, clear actions, picker buttons, and toggle affordances use this fixed-viewport vector node so
/// their geometry does not depend on an installed icon font. The icon is mouse-transparent and owns its [SVGPath]
/// child for its complete lifetime.
@NotNullByDefault
public final class M3InternalIcon extends StackPane {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-internal-icon";

    /// The style class applied to the SVG path inside the viewport.
    public static final String PATH_STYLE_CLASS = "m3-internal-icon-path";

    /// The style class applied when the icon uses the primary color role.
    public static final String PRIMARY_STYLE_CLASS = "m3-internal-icon-primary";

    /// The style class applied when the icon uses the on-surface color role.
    public static final String ON_SURFACE_STYLE_CLASS = "m3-internal-icon-on-surface";

    /// The style class applied when the icon uses the inverse-on-surface color role.
    public static final String INVERSE_ON_SURFACE_STYLE_CLASS = "m3-internal-icon-inverse-on-surface";

    /// The style class applied when the icon uses the on-surface-variant color role.
    public static final String ON_SURFACE_VARIANT_STYLE_CLASS = "m3-internal-icon-on-surface-variant";

    /// The style class applied when the icon uses the on-primary-container color role.
    public static final String ON_PRIMARY_CONTAINER_STYLE_CLASS = "m3-internal-icon-on-primary-container";

    /// The style class applied when the icon uses the on-primary color role.
    public static final String ON_PRIMARY_STYLE_CLASS = "m3-internal-icon-on-primary";

    /// The style class applied when the icon uses the on-secondary color role.
    public static final String ON_SECONDARY_STYLE_CLASS = "m3-internal-icon-on-secondary";

    /// The style class applied when the icon uses the on-tertiary color role.
    public static final String ON_TERTIARY_STYLE_CLASS = "m3-internal-icon-on-tertiary";

    /// The default Material icon viewport size in JavaFX logical pixels.
    private static final double DEFAULT_SIZE = 24.0;

    /// The SVG path rendered by this icon.
    private final SVGPath path = new SVGPath();

    /// The glyph currently rendered by the SVG path.
    private Glyph glyph;

    /// The color role currently applied to the SVG path.
    private ColorRole colorRole;

    /// Creates a 24 dp internal icon.
    ///
    /// @param glyph     the SVG glyph to render
    /// @param colorRole the Material color role used for the SVG fill
    /// @throws NullPointerException if `glyph` or `colorRole` is `null`
    public M3InternalIcon(Glyph glyph, ColorRole colorRole) {
        this(glyph, colorRole, DEFAULT_SIZE);
    }

    /// Creates an internal icon with an explicit square viewport size.
    ///
    /// @param glyph     the SVG glyph to render
    /// @param colorRole the Material color role used for the SVG fill
    /// The supplied size is assigned to the minimum, preferred, and maximum dimensions of this region.
    /// @param size      the square viewport size in JavaFX logical pixels
    /// @throws NullPointerException if `glyph` or `colorRole` is `null`
    public M3InternalIcon(Glyph glyph, ColorRole colorRole, double size) {
        this.glyph = Objects.requireNonNull(glyph, "glyph");
        this.colorRole = Objects.requireNonNull(colorRole, "colorRole");
        initialize(size);
        updateGlyph();
        updateColor();
    }

    /// Returns the glyph currently rendered by this icon.
    ///
    /// @return the rendered glyph
    public Glyph getGlyph() {
        return glyph;
    }

    /// Sets the glyph rendered by this icon.
    ///
    /// @param glyph the glyph to render
    /// @throws NullPointerException if `glyph` is `null`
    public void setGlyph(Glyph glyph) {
        this.glyph = Objects.requireNonNull(glyph, "glyph");
        updateGlyph();
    }

    /// Returns the Material color role used for the SVG fill.
    ///
    /// @return the Material color role used for the SVG fill
    public ColorRole getColorRole() {
        return colorRole;
    }

    /// Sets the Material color role used for the SVG fill.
    ///
    /// @param colorRole the Material color role used for the SVG fill
    /// @throws NullPointerException if `colorRole` is `null`
    public void setColorRole(ColorRole colorRole) {
        this.colorRole = Objects.requireNonNull(colorRole, "colorRole");
        updateColor();
    }

    /// Returns the SVG path node rendered inside this viewport.
    ///
    /// The returned node is the live child owned by this icon. Mutating its geometry or style directly affects the
    /// rendered icon; it must not be reparented.
    ///
    /// @return the SVG path node rendered inside this viewport
    public SVGPath getPath() {
        return path;
    }

    /// Configures the fixed viewport and child SVG path.
    private void initialize(double size) {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        getChildren().add(path);
        setAlignment(Pos.CENTER);
        setMinSize(size, size);
        setPrefSize(size, size);
        setMaxSize(size, size);
        setMouseTransparent(true);

        path.getStyleClass().add(PATH_STYLE_CLASS);
        path.setFillRule(FillRule.EVEN_ODD);
        path.setMouseTransparent(true);
    }

    /// Applies the current glyph path to the SVG node.
    private void updateGlyph() {
        path.setContent(glyph.path);
    }

    /// Applies the current color role as a token lookup fill.
    private void updateColor() {
        List<String> styleClasses = getStyleClass();
        styleClasses.remove(PRIMARY_STYLE_CLASS);
        styleClasses.remove(ON_SURFACE_STYLE_CLASS);
        styleClasses.remove(INVERSE_ON_SURFACE_STYLE_CLASS);
        styleClasses.remove(ON_SURFACE_VARIANT_STYLE_CLASS);
        styleClasses.remove(ON_PRIMARY_CONTAINER_STYLE_CLASS);
        styleClasses.remove(ON_PRIMARY_STYLE_CLASS);
        styleClasses.remove(ON_SECONDARY_STYLE_CLASS);
        styleClasses.remove(ON_TERTIARY_STYLE_CLASS);
        styleClasses.add(colorRole.styleClass);
    }

    /// The built-in SVG glyphs needed by M3FX controls.
    @NotNullByDefault
    public enum Glyph {
        /// A plus icon used by add and default FAB-menu affordances.
        ADD("M11 5h2v6h6v2h-6v6h-2v-6H5v-2h6z"),

        /// A close icon used by clear actions.
        CLOSE("M18.3 5.7 12 12l6.3 6.3-1.4 1.4-6.3-6.3-6.3 6.3-1.4-1.4L9.2 12 2.9 5.7l1.4-1.4 6.3 6.3 6.3-6.3z"),

        /// A back arrow used to dismiss an active search view.
        ARROW_BACK("M20 11H7.8l5.6-5.6L12 4l-8 8 8 8 1.4-1.4L7.8 13H20z"),

        /// A downward chevron used by closed popup fields.
        EXPAND_MORE("M7.4 8.6 12 13.2l4.6-4.6L18 10l-6 6-6-6z"),

        /// A left chevron used by previous navigation.
        CHEVRON_LEFT("M15.4 7.4 14 6l-6 6 6 6 1.4-1.4L10.8 12z"),

        /// A right chevron used by next navigation and submenu affordances.
        CHEVRON_RIGHT("M8.6 16.6 13.2 12 8.6 7.4 10 6l6 6-6 6z"),

        /// A calendar icon used by date picker fields.
        CALENDAR("M19 4h-1V2h-2v2H8V2H6v2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zm0 16H5V9h14z"),

        /// A clock icon used by time picker fields.
        SCHEDULE("M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20zm1 11h5v-2h-4V6h-2v7z"),

        /// A keyboard icon used to switch time pickers into numeric input mode.
        KEYBOARD("M20 5H4c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 12H4V7h16v10zM6 10h2V8H6v2zm0 3h2v-2H6v2zm3-3h2V8H9v2zm0 3h2v-2H9v2zm3-3h2V8h-2v2zm0 3h2v-2h-2v2zm3-3h2V8h-2v2zm0 3h2v-2h-2v2zM6 16h12v-2H6v2z"),

        /// A search icon used by search bars and search views.
        SEARCH("M9.5 3a6.5 6.5 0 0 1 5.1 10.5l4.4 4.4-1.4 1.4-4.4-4.4A6.5 6.5 0 1 1 9.5 3zm0 2a4.5 4.5 0 1 0 0 9 4.5 4.5 0 0 0 0-9z");

        /// The SVG path data for the glyph.
        private final String path;

        /// Creates a glyph from SVG path data.
        Glyph(String path) {
            this.path = path;
        }

    }

    /// The Material color roles used by built-in affordance icons.
    @NotNullByDefault
    public enum ColorRole {
        /// The primary content color.
        PRIMARY(PRIMARY_STYLE_CLASS),

        /// The on-surface content color.
        ON_SURFACE(ON_SURFACE_STYLE_CLASS),

        /// The inverse-on-surface content color.
        INVERSE_ON_SURFACE(INVERSE_ON_SURFACE_STYLE_CLASS),

        /// The on-surface-variant content color.
        ON_SURFACE_VARIANT(ON_SURFACE_VARIANT_STYLE_CLASS),

        /// The content color for primary-colored containers.
        ON_PRIMARY(ON_PRIMARY_STYLE_CLASS),

        /// The content color for secondary-colored containers.
        ON_SECONDARY(ON_SECONDARY_STYLE_CLASS),

        /// The content color for tertiary-colored containers.
        ON_TERTIARY(ON_TERTIARY_STYLE_CLASS),

        /// The content color for primary FAB containers.
        ON_PRIMARY_CONTAINER(ON_PRIMARY_CONTAINER_STYLE_CLASS);

        /// The style class that selects this color role.
        private final String styleClass;

        /// Creates a color role from a style class.
        ColorRole(String styleClass) {
            this.styleClass = styleClass;
        }

    }
}
