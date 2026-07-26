// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.PaintConverter;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Rectangle2D;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.FillRule;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3IconPaints;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3SVGIconSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A token-aware, single-path SVG icon for Material Design 3 controls.
///
/// `M3SVGIcon` renders the path-data syntax accepted by [javafx.scene.shape.SVGPath]. Unlike APIs that assume a
/// 24-by-24 coordinate system, this control accepts an arbitrary [#viewBoxProperty()] including view boxes with
/// negative origins. This allows an icon to use source coordinates directly, such as Material Symbols paths whose
/// published view box is commonly `0 -960 960 960`, without rewriting or prefixing the path data.
///
/// When [#viewBoxProperty()] is `null`, the path's rendered bounds are used as an automatic view box. Automatic bounds
/// are convenient for compact path data but cannot preserve transparent padding authored into an SVG document. Set
/// an explicit view box whenever alignment with the source SVG viewport matters. The path is uniformly scaled with
/// meet semantics, centered in the control, and never stretched to a different aspect ratio.
///
/// This control intentionally models one monochrome path. Its fill follows [#variantProperty()] and the surrounding
/// component's icon-color token. A non-null [tint][#tintProperty()] takes precedence over both sources. Compose
/// several JavaFX nodes when an icon requires multiple independently colored paths, strokes, masks, or animation.
/// The icon is non-interactive and not focus traversable by default; place it in an action-owning control such as
/// [M3IconButton], [M3Chip], or [M3MenuItem] when it represents an action.
///
/// See [Material Design icons](https://m3.material.io/styles/icons/overview) and
/// [Material Design](https://m3.material.io/).
@NotNullByDefault
public final class M3SVGIcon extends Control implements M3IconGraphic {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-svg-icon";

    /// The style class applied when this icon participates in a component graphic slot.
    private static final String GRAPHIC_STYLE_CLASS = "m3-icon-graphic";

    /// The default icon size token.
    private static final double DEFAULT_ICON_SIZE = M3IconSize.MEDIUM.defaultSize();

    /// Creates an empty icon with automatic path bounds as its viewport.
    public M3SVGIcon() {
        this("");
    }

    /// Creates an icon whose viewport is derived from the path's rendered bounds.
    ///
    /// @param content the SVG path data
    /// @throws NullPointerException if `content` is `null`
    public M3SVGIcon(String content) {
        initialize();
        setContent(content);
    }

    /// Creates an icon with an explicit source-coordinate viewport.
    ///
    /// @param content the SVG path data
    /// @param viewBox the source-coordinate viewport; its width and height must be positive and finite
    /// @throws NullPointerException     if `content` or `viewBox` is `null`
    /// @throws IllegalArgumentException if a view-box coordinate is not finite or either dimension is not positive
    public M3SVGIcon(String content, Rectangle2D viewBox) {
        initialize();
        setContent(content);
        setViewBox(Objects.requireNonNull(viewBox, "viewBox"));
    }

    /// The SVG path data rendered by this icon.
    ///
    /// The property is expected to contain a non-null string. Use an empty string for an icon with no visible path.
    ///
    /// @defaultValue `""`
    private final StringProperty contentValue = new SimpleStringProperty(this, "content", "") {
        /// Requests layout after the path data changes.
        @Override
        protected void invalidated() {
            requestLayout();
        }
    };

    /// Returns the SVG path data rendered by this icon.
    ///
    /// @return the non-null SVG path data
    public String getContent() {
        return contentValue.get();
    }

    /// Sets the SVG path data rendered by this icon.
    ///
    /// The value uses the path-data syntax accepted by [javafx.scene.shape.SVGPath].
    ///
    /// @param content the SVG path data, or an empty string to render no path
    /// @throws NullPointerException if `content` is `null`
    public void setContent(String content) {
        contentValue.set(Objects.requireNonNull(content, "content"));
    }

    /// Returns the property containing the SVG path data.
    ///
    /// The property must contain a non-null string. Binding it to a source that produces `null` violates this
    /// control's contract.
    ///
    /// The returned property is observable and bindable. Its default value is `""`.
    ///
    /// @return the SVG path-data property
    public StringProperty contentProperty() {
        return contentValue;
    }

    /// The source-coordinate viewport used to scale and align the path, or `null` to derive it from path bounds.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Rectangle2D> viewBoxValue =
            new SimpleObjectProperty<>(this, "viewBox") {
                /// Requests layout after viewport coordinates change.
                @Override
                protected void invalidated() {
                    requestLayout();
                }
            };

    /// Returns the explicit source-coordinate viewport, or `null` when path bounds are used.
    ///
    /// @return the explicit view box, or `null` for automatic bounds
    public @Nullable Rectangle2D getViewBox() {
        return viewBoxValue.get();
    }

    /// Sets the source-coordinate viewport.
    ///
    /// A `null` value selects automatic path bounds. An explicit viewport preserves authored transparent padding and
    /// is recommended when copying path data from an SVG document with a declared `viewBox` attribute.
    ///
    /// @param viewBox the source-coordinate viewport, or `null` to use path bounds
    /// @throws IllegalArgumentException if a non-null view-box coordinate is not finite or either dimension is not
    /// positive
    public void setViewBox(@Nullable Rectangle2D viewBox) {
        if (viewBox != null) {
            validateViewBox(viewBox);
        }
        viewBoxValue.set(viewBox);
    }

    /// Returns the property containing the source-coordinate viewport.
    ///
    /// A non-null value assigned directly through this property must have finite coordinates and positive finite
    /// dimensions. Use [#setViewBox(Rectangle2D)] when validation at the API boundary is required.
    ///
    /// The returned property is observable and bindable. Its default value is `null`.
    ///
    /// @return the nullable view-box property
    public ObjectProperty<@Nullable Rectangle2D> viewBoxProperty() {
        return viewBoxValue;
    }

    /// The fill rule used to determine the inside of the SVG path.
    ///
    /// @defaultValue [FillRule#NON_ZERO]
    private final ObjectProperty<FillRule> fillRuleValue =
            new SimpleObjectProperty<>(this, "fillRule", FillRule.NON_ZERO);

    /// Returns the path fill rule.
    ///
    /// @return the non-null fill rule
    public FillRule getFillRule() {
        return fillRuleValue.get();
    }

    /// Sets the rule used to determine the inside of the path.
    ///
    /// @param fillRule the path fill rule
    /// @throws NullPointerException if `fillRule` is `null`
    public void setFillRule(FillRule fillRule) {
        fillRuleValue.set(Objects.requireNonNull(fillRule, "fillRule"));
    }

    /// Returns the property containing the path fill rule.
    ///
    /// The property must contain a non-null value.
    ///
    /// The returned property is observable and bindable. Its default value is [FillRule#NON_ZERO].
    ///
    /// @return the fill-rule property
    public ObjectProperty<FillRule> fillRuleProperty() {
        return fillRuleValue;
    }

    /// The semantic icon size role.
    ///
    /// @defaultValue [M3IconSize#MEDIUM]
    private final ObjectProperty<M3IconSize> sizeValue =
            new SimpleObjectProperty<>(this, "size", M3IconSize.MEDIUM) {
                /// Applies the new size role and updates intrinsic metrics.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3IconSize.MEDIUM);
                        return;
                    }
                    updateSizeStyle();
                    requestLayout();
                }
            };

    /// Returns the semantic icon size role.
    ///
    /// @return the icon size role
    public M3IconSize getSize() {
        return sizeValue.get();
    }

    /// Sets the semantic icon size role.
    ///
    /// The role determines the effective size until [#iconSizeProperty()] is explicitly initialized or styled.
    ///
    /// @param size the icon size role
    /// @throws NullPointerException if `size` is `null`
    public void setSize(M3IconSize size) {
        sizeValue.set(Objects.requireNonNull(size, "size"));
    }

    /// Returns the property containing the semantic icon size role.
    ///
    /// The returned property is observable and bindable. Its default value is [M3IconSize#MEDIUM]; assigning
    /// `null` directly restores that default.
    ///
    /// @return the icon size-role property
    public ObjectProperty<M3IconSize> sizeProperty() {
        return sizeValue;
    }

    /// The Material color role used to paint the path.
    ///
    /// @defaultValue [M3IconVariant#ON_SURFACE_VARIANT]
    private final ObjectProperty<M3IconVariant> variantValue =
            new SimpleObjectProperty<>(this, "variant", M3IconVariant.ON_SURFACE_VARIANT) {
                /// Applies the style class for the new color role.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3IconVariant.ON_SURFACE_VARIANT);
                        return;
                    }
                    updateVariantStyle();
                }
            };

    /// Returns the icon color variant.
    ///
    /// @return the icon color variant
    public M3IconVariant getVariant() {
        return variantValue.get();
    }

    /// Sets the Material color role used to paint the path.
    ///
    /// A containing M3FX component may override this standalone role while the icon occupies one of its graphic
    /// slots.
    ///
    /// @param variant the icon color variant
    /// @throws NullPointerException if `variant` is `null`
    public void setVariant(M3IconVariant variant) {
        variantValue.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the property containing the icon color variant.
    ///
    /// The returned property is observable and bindable. Its default value is
    /// [M3IconVariant#ON_SURFACE_VARIANT]; assigning `null` directly restores that default.
    ///
    /// @return the icon color-variant property
    public ObjectProperty<M3IconVariant> variantProperty() {
        return variantValue;
    }

    /// The explicit tint used to fill the SVG path, or `null` to use semantic color resolution.
    ///
    /// A non-null tint takes precedence over [#variantProperty()] and over the color supplied by a containing M3FX
    /// component. Clearing it restores those normal rules without changing the semantic variant.
    ///
    /// @defaultValue `null`
    private final StyleableObjectProperty<@Nullable Paint> tintValue =
            M3Css.styleableObjectProperty(
                    null,
                    this,
                    "tint",
                    StyleableProperties.TINT,
                    this::requestLayout
            );

    /// Returns the explicit SVG icon tint.
    ///
    /// @return the tint, or `null` when semantic color resolution is active
    public @Nullable Paint getTint() {
        return tintValue.get();
    }

    /// Sets the explicit SVG icon tint.
    ///
    /// @param tint the tint to apply, or `null` to restore semantic color resolution
    public void setTint(@Nullable Paint tint) {
        tintValue.set(tint);
    }

    /// Returns the observable property that stores the explicit SVG icon tint.
    ///
    /// The property can be observed and bound. Its default value is `null`.
    ///
    /// @return the nullable tint property
    public StyleableObjectProperty<@Nullable Paint> tintProperty() {
        return tintValue;
    }

    /// Whether the path is mirrored horizontally in a right-to-left scene orientation.
    private @Nullable BooleanProperty autoMirroredValue;

    /// Returns whether the icon mirrors itself in right-to-left orientation.
    ///
    /// @return `true` if the path is automatically mirrored in right-to-left orientation
    public boolean isAutoMirrored() {
        return autoMirroredValue != null && autoMirroredValue.get();
    }

    /// Sets whether the icon mirrors itself in right-to-left orientation.
    ///
    /// Enable this for directional icons such as forward, back, undo, and redo. Leave it disabled for symmetric or
    /// semantically fixed artwork such as clocks, media controls, and brand marks.
    ///
    /// @param autoMirrored whether to mirror the path in right-to-left orientation
    public void setAutoMirrored(boolean autoMirrored) {
        autoMirroredProperty().set(autoMirrored);
    }

    /// Returns the property controlling automatic right-to-left mirroring.
    ///
    /// The returned property is observable and bindable. Its default value is `false`.
    ///
    /// @return the auto-mirroring property
    public BooleanProperty autoMirroredProperty() {
        if (autoMirroredValue == null) {
            autoMirroredValue = new SimpleBooleanProperty(this, "autoMirrored", false) {
                /// Requests layout after mirroring behavior changes.
                @Override
                protected void invalidated() {
                    requestLayout();
                }
            };
        }
        return autoMirroredValue;
    }

    /// The styleable rendered width and height of the icon in logical pixels.
    private @Nullable StyleableDoubleProperty iconSizeValue;

    /// Returns the effective rendered icon size.
    ///
    /// @return the rendered width and height in logical pixels
    @Override
    public double getIconSize() {
        return iconSizeValue == null ? getSize().defaultSize() : iconSizeValue.get();
    }

    /// Sets an explicit rendered icon size, overriding [#sizeProperty()].
    ///
    /// @param iconSize the rendered width and height in logical pixels
    /// @throws IllegalArgumentException if `iconSize` is negative or not finite
    @Override
    public void setIconSize(double iconSize) {
        iconSizeProperty().set(M3Css.nonNegative(iconSize, "iconSize"));
    }

    /// Returns the styleable property containing the rendered icon size.
    ///
    /// The returned property is observable, bindable, and styleable, and accepts finite, non-negative logical
    /// pixel values. When first requested, it is initialized from the current [#sizeProperty()] value.
    ///
    /// @return the icon-size property
    @Override
    public StyleableDoubleProperty iconSizeProperty() {
        if (iconSizeValue == null) {
            iconSizeValue = M3Css.nonNegativeStyleableDoubleProperty(
                    getSize().defaultSize(),
                    this,
                    "iconSize",
                    StyleableProperties.ICON_SIZE,
                    this::requestLayout
            );
        }
        return iconSizeValue;
    }

    /// Returns the user-agent stylesheet for SVG icons.
    ///
    /// @return the SVG icon user-agent stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("svg-icon.css");
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the immutable CSS metadata list
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    ///
    /// @return the immutable CSS metadata list
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Creates the default SVG icon skin.
    ///
    /// @return a new SVG icon skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3SVGIconSkin(this);
    }

    /// Initializes style classes, accessibility, and token roles.
    private void initialize() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        M3IconPaints.initializeSemanticPaint(this, StyleableProperties.ICON_COLOR);
        getStyleClass().add(GRAPHIC_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.NODE);
        setFocusTraversable(false);
        updateSizeStyle();
        updateVariantStyle();
    }

    /// Applies the style class for the selected icon size.
    private void updateSizeStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getSize().styleClass(),
                M3IconSize.SMALL.styleClass(),
                M3IconSize.MEDIUM.styleClass(),
                M3IconSize.LARGE.styleClass(),
                M3IconSize.EXTRA_LARGE.styleClass()
        );
    }

    /// Applies the style class for the selected icon color role.
    private void updateVariantStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getVariant().styleClass(),
                M3IconVariant.PRIMARY.styleClass(),
                M3IconVariant.SECONDARY.styleClass(),
                M3IconVariant.TERTIARY.styleClass(),
                M3IconVariant.ERROR.styleClass(),
                M3IconVariant.ON_SURFACE.styleClass(),
                M3IconVariant.ON_SURFACE_VARIANT.styleClass(),
                M3IconVariant.INVERSE_ON_SURFACE.styleClass()
        );
    }

    /// Validates an explicit source-coordinate viewport.
    ///
    /// @param viewBox the viewport to validate
    /// @throws IllegalArgumentException if a coordinate is not finite or either dimension is not positive
    private static void validateViewBox(Rectangle2D viewBox) {
        if (!Double.isFinite(viewBox.getMinX())
                || !Double.isFinite(viewBox.getMinY())
                || !Double.isFinite(viewBox.getWidth())
                || !Double.isFinite(viewBox.getHeight())
                || viewBox.getWidth() <= 0.0
                || viewBox.getHeight() <= 0.0) {
            throw new IllegalArgumentException("viewBox coordinates must be finite and dimensions must be positive");
        }
    }

    /// CSS metadata for the SVG icon size token.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the semantic SVG path paint resolved from the selected variant.
        private static final CssMetaData<M3SVGIcon, @Nullable Paint> ICON_COLOR =
                new CssMetaData<>("-m3-icon-color", PaintConverter.getInstance(), Color.BLACK) {
                    /// Returns whether CSS may assign the semantic path paint.
                    @Override
                    public boolean isSettable(M3SVGIcon control) {
                        return M3Css.isSettable(M3IconPaints.semanticPaintProperty(control));
                    }

                    /// Returns the semantic path paint property.
                    @Override
                    public StyleableProperty<@Nullable Paint> getStyleableProperty(M3SVGIcon control) {
                        return M3IconPaints.semanticPaintProperty(control);
                    }
                };

        /// CSS metadata for an explicit SVG path tint.
        private static final CssMetaData<M3SVGIcon, @Nullable Paint> TINT =
                new CssMetaData<>("-m3-icon-tint", PaintConverter.getInstance(), null) {
                    /// Returns whether CSS may assign the explicit tint.
                    @Override
                    public boolean isSettable(M3SVGIcon control) {
                        return M3Css.isSettable(control.tintProperty());
                    }

                    /// Returns the explicit tint property.
                    @Override
                    public StyleableProperty<@Nullable Paint> getStyleableProperty(M3SVGIcon control) {
                        return control.tintProperty();
                    }
                };

        /// CSS metadata for the icon size token.
        private static final CssMetaData<M3SVGIcon, Number> ICON_SIZE =
                new CssMetaData<>("-m3-icon-size", SizeConverter.getInstance(), DEFAULT_ICON_SIZE) {
                    /// Returns whether CSS may set the icon size.
                    ///
                    /// @param control the target icon
                    /// @return whether the icon-size property is settable
                    @Override
                    public boolean isSettable(M3SVGIcon control) {
                        return M3Css.isSettable(control.iconSizeProperty());
                    }

                    /// Returns the target icon-size property.
                    ///
                    /// @param control the target icon
                    /// @return the icon-size property
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3SVGIcon control) {
                        return control.iconSizeProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(ICON_COLOR);
            styleables.add(TINT);
            styleables.add(ICON_SIZE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }

        /// Prevents utility class instantiation.
        private StyleableProperties() {
        }
    }
}
