// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3BadgeSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A small status, count, or attention indicator.
///
/// A badge with empty [text][#textProperty()] is rendered as a dot. Non-empty text creates a larger badge whose
/// displayed value is shortened according to [maxCharacterCount][#maxCharacterCountProperty()]. The raw text is
/// retained even when its displayed representation is shortened. Badges are non-focus-traversable and do not
/// provide action behavior.
///
/// The no-argument constructor creates a dot badge. Use [M3BadgedBox] to overlay a badge on another node. Size,
/// shape, and padding properties are styleable and use logical-pixel units.
///
/// See [Material Design badges](https://m3.material.io/components/badges/overview).
@NotNullByDefault
public final class M3Badge extends Control {
    /// The base style class for M3FX badges.
    public static final String STYLE_CLASS = "m3-badge";

    /// The default small dot badge size.
    private static final double DEFAULT_SMALL_SIZE = 6.0;

    /// The default large badge height.
    private static final double DEFAULT_LARGE_HEIGHT = 16.0;

    /// The default minimum width for text badges.
    private static final double DEFAULT_LARGE_MIN_WIDTH = 16.0;

    /// The default text badge container shape radius.
    private static final double DEFAULT_CONTAINER_SHAPE = 8.0;

    /// The default horizontal content padding.
    private static final double DEFAULT_HORIZONTAL_PADDING = 4.0;

    /// Creates a dot badge with empty text and the default size tokens.
    public M3Badge() {
        this("");
    }

    /// Creates a badge with the specified raw text.
    ///
    /// @param text the badge text, or an empty string for a dot badge
    /// @throws NullPointerException if `text` is `null`
    public M3Badge(String text) {
        M3ControlStyles.initialize(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TEXT);
        setFocusTraversable(false);
        this.text.addListener((observable, oldValue, newValue) -> updateAccessibleText());
        maxCharacterCount.addListener((observable, oldValue, newValue) -> updateAccessibleText());
        setText(text);
        updateAccessibleText();
    }

    /// Creates a badge that displays a non-negative count.
    ///
    /// @param count the non-negative count displayed by the badge
    /// @throws IllegalArgumentException if `count` is negative
    public M3Badge(int count) {
        this(countText(count));
    }

    /// The raw badge text before overflow handling.
    ///
    /// The default value is the empty string, which selects the dot presentation. [setText][#setText(String)]
    /// rejects `null`. If an external binding supplies `null`, [#getText()] and rendering treat it as an empty
    /// string while the property remains bound.
    ///
    /// @defaultValue `""`
    private final StringProperty text = new SimpleStringProperty(this, "text", "");

    /// Returns the badge text.
    ///
    /// @return the raw badge text before overflow handling
    public final String getText() {
        return Objects.requireNonNullElse(text.get(), "");
    }

    /// Sets the badge text.
    ///
    /// @param text the raw badge text before overflow handling
    /// @throws NullPointerException if `text` is `null`
    public final void setText(String text) {
        this.text.set(Objects.requireNonNull(text, "text"));
    }

    /// Returns the observable property that stores the raw badge text.
    ///
    /// The property can be observed and bound. Its default value is the empty string, which selects the dot
    /// presentation. [#setText(String)] rejects `null`; a `null` value supplied directly or by a binding is
    /// exposed and rendered as an empty string. Values are interpreted before overflow handling.
    ///
    /// @return the raw badge text property
    public final StringProperty textProperty() {
        return text;
    }

    /// The maximum number of UTF-16 code units retained before an overflow suffix is appended.
    ///
    /// The default value is `3`. The value is always at least `1`. The setter rejects non-positive values;
    /// a non-positive value assigned directly to the property is normalized to `1`.
    ///
    /// @defaultValue `3`
    private final IntegerProperty maxCharacterCount = new SimpleIntegerProperty(this, "maxCharacterCount", 3) {
        /// Validates assigned maximum character counts.
        @Override
        protected void invalidated() {
            if (get() < 1) {
                set(1);
            }
        }
    };

    /// Returns the maximum display text length before an overflow suffix is used.
    ///
    /// @return the maximum number of UTF-16 code units retained before `+` is appended
    public final int getMaxCharacterCount() {
        return maxCharacterCount.get();
    }

    /// Sets the maximum display text length before an overflow suffix is used.
    ///
    /// @param maxCharacterCount the maximum number of UTF-16 code units retained before `+` is appended
    /// @throws IllegalArgumentException if `maxCharacterCount` is less than `1`
    public final void setMaxCharacterCount(int maxCharacterCount) {
        if (maxCharacterCount < 1) {
            throw new IllegalArgumentException("maxCharacterCount must be positive");
        }
        this.maxCharacterCount.set(maxCharacterCount);
    }

    /// Returns the observable property that limits retained badge text.
    ///
    /// The property can be observed and bound. Its default value is `3`, and values must be at least `1`.
    /// A non-positive value assigned to an unbound property is normalized to `1`.
    ///
    /// @return the maximum character count property
    public final IntegerProperty maxCharacterCountProperty() {
        return maxCharacterCount;
    }

    /// The width and height of a dot badge, in logical pixels.
    ///
    /// The default value is `6.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `6.0`
    private @Nullable StyleableDoubleProperty smallSize;

    /// Returns the small dot badge size token.
    ///
    /// @return the dot badge size in logical pixels
    public final double getSmallSize() {
        return smallSize == null ? DEFAULT_SMALL_SIZE : smallSize.get();
    }

    /// Sets the small dot badge size token.
    ///
    /// @param smallSize the dot badge size in logical pixels
    /// @throws IllegalArgumentException if `smallSize` is negative or not finite
    public final void setSmallSize(double smallSize) {
        smallSizeProperty().set(M3Css.nonNegative(smallSize, "smallSize"));
    }

    /// Returns the styleable property that stores the dot badge size.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-small-size`, and accepts finite,
    /// non-negative values. Its default value is `6.0` logical pixels.
    ///
    /// @return the dot badge size property
    public final StyleableDoubleProperty smallSizeProperty() {
        if (smallSize == null) {
            smallSize = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_SMALL_SIZE,
                    this,
                    "smallSize",
                    StyleableProperties.SMALL_SIZE,
                    this::requestLayout
            );
        }
        return smallSize;
    }

    /// The height of a text badge, in logical pixels.
    ///
    /// The default value is `16.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `16.0`
    private @Nullable StyleableDoubleProperty largeHeight;

    /// Returns the large badge height token.
    ///
    /// @return the text badge height in logical pixels
    public final double getLargeHeight() {
        return largeHeight == null ? DEFAULT_LARGE_HEIGHT : largeHeight.get();
    }

    /// Sets the large badge height token.
    ///
    /// @param largeHeight the text badge height in logical pixels
    /// @throws IllegalArgumentException if `largeHeight` is negative or not finite
    public final void setLargeHeight(double largeHeight) {
        largeHeightProperty().set(M3Css.nonNegative(largeHeight, "largeHeight"));
    }

    /// Returns the styleable property that stores the text badge height.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-large-height`, and accepts finite,
    /// non-negative values. Its default value is `16.0` logical pixels.
    ///
    /// @return the text badge height property
    public final StyleableDoubleProperty largeHeightProperty() {
        if (largeHeight == null) {
            largeHeight = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_LARGE_HEIGHT,
                    this,
                    "largeHeight",
                    StyleableProperties.LARGE_HEIGHT,
                    this::requestLayout
            );
        }
        return largeHeight;
    }

    /// The minimum width of a text badge, in logical pixels.
    ///
    /// The default value is `16.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `16.0`
    private @Nullable StyleableDoubleProperty largeMinWidth;

    /// Returns the large badge minimum width token.
    ///
    /// @return the text badge minimum width in logical pixels
    public final double getLargeMinWidth() {
        return largeMinWidth == null ? DEFAULT_LARGE_MIN_WIDTH : largeMinWidth.get();
    }

    /// Sets the large badge minimum width token.
    ///
    /// @param largeMinWidth the text badge minimum width in logical pixels
    /// @throws IllegalArgumentException if `largeMinWidth` is negative or not finite
    public final void setLargeMinWidth(double largeMinWidth) {
        largeMinWidthProperty().set(M3Css.nonNegative(largeMinWidth, "largeMinWidth"));
    }

    /// Returns the styleable property that stores the minimum text badge width.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-large-min-width`, and accepts finite,
    /// non-negative values. Its default value is `16.0` logical pixels.
    ///
    /// @return the minimum text badge width property
    public final StyleableDoubleProperty largeMinWidthProperty() {
        if (largeMinWidth == null) {
            largeMinWidth = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_LARGE_MIN_WIDTH,
                    this,
                    "largeMinWidth",
                    StyleableProperties.LARGE_MIN_WIDTH,
                    this::requestLayout
            );
        }
        return largeMinWidth;
    }

    /// The corner radius of a text badge, in logical pixels.
    ///
    /// The default value is `8.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `8.0`
    private @Nullable StyleableDoubleProperty containerShape;

    /// Returns the container shape radius token.
    ///
    /// @return the badge container corner radius in logical pixels
    public final double getContainerShape() {
        return containerShape == null ? DEFAULT_CONTAINER_SHAPE : containerShape.get();
    }

    /// Sets the container shape radius token.
    ///
    /// @param containerShape the badge container corner radius in logical pixels
    /// @throws IllegalArgumentException if `containerShape` is negative or not finite
    public final void setContainerShape(double containerShape) {
        containerShapeProperty().set(M3Css.nonNegative(containerShape, "containerShape"));
    }

    /// Returns the styleable property that stores the text badge corner radius.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-container-shape`, and accepts finite,
    /// non-negative values. Its default value is `8.0` logical pixels.
    ///
    /// @return the badge corner radius property
    public final StyleableDoubleProperty containerShapeProperty() {
        if (containerShape == null) {
            containerShape = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTAINER_SHAPE,
                    this,
                    "containerShape",
                    StyleableProperties.CONTAINER_SHAPE,
                    this::requestLayout
            );
        }
        return containerShape;
    }

    /// The horizontal padding on each side of text badge content, in logical pixels.
    ///
    /// The default value is `4.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `4.0`
    private @Nullable StyleableDoubleProperty horizontalPadding;

    /// Returns the horizontal content padding token.
    ///
    /// @return the horizontal padding used by text badges
    public final double getHorizontalPadding() {
        return horizontalPadding == null ? DEFAULT_HORIZONTAL_PADDING : horizontalPadding.get();
    }

    /// Sets the horizontal content padding token.
    ///
    /// @param horizontalPadding the horizontal padding used by text badges
    /// @throws IllegalArgumentException if `horizontalPadding` is negative or not finite
    public final void setHorizontalPadding(double horizontalPadding) {
        horizontalPaddingProperty().set(M3Css.nonNegative(horizontalPadding, "horizontalPadding"));
    }

    /// Returns the styleable property that stores the text badge horizontal padding.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-horizontal-padding`, and accepts finite,
    /// non-negative values. Its default value is `4.0` logical pixels.
    ///
    /// @return the horizontal padding property
    public final StyleableDoubleProperty horizontalPaddingProperty() {
        if (horizontalPadding == null) {
            horizontalPadding = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_HORIZONTAL_PADDING,
                    this,
                    "horizontalPadding",
                    StyleableProperties.HORIZONTAL_PADDING,
                    this::requestLayout
            );
        }
        return horizontalPadding;
    }

    /// Returns the text representation of a non-negative count.
    private static String countText(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count must be non-negative");
        }
        return Integer.toString(count);
    }

    /// Returns the text rendered by this badge after overflow handling.
    ///
    /// If the raw text is longer than [maxCharacterCount][#maxCharacterCountProperty()], the returned value
    /// consists of the retained prefix followed by `+`. This method does not modify [text][#textProperty()].
    ///
    /// @return the display text shown by the badge
    public final String getDisplayText() {
        String value = getText();
        int maximum = getMaxCharacterCount();
        if (value.isEmpty() || value.length() <= maximum) {
            return value;
        }
        return value.substring(0, maximum) + "+";
    }

    /// Updates the text exposed to assistive technologies.
    private void updateAccessibleText() {
        setAccessibleText(getDisplayText());
    }

    /// Creates the default badge skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3BadgeSkin(this);
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the immutable CSS metadata list for this class
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Returns the user-agent stylesheet for M3FX badges.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("badge.css");
    }

    /// CSS metadata for M3FX badge component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the small size token.
        private static final CssMetaData<M3Badge, Number> SMALL_SIZE =
                new CssMetaData<>("-m3-small-size", SizeConverter.getInstance(), DEFAULT_SMALL_SIZE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Badge control) {
                        return M3Css.isSettable(control.smallSizeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Badge control) {
                        return control.smallSizeProperty();
                    }
                };

        /// CSS metadata for the large height token.
        private static final CssMetaData<M3Badge, Number> LARGE_HEIGHT =
                new CssMetaData<>("-m3-large-height", SizeConverter.getInstance(), DEFAULT_LARGE_HEIGHT) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Badge control) {
                        return M3Css.isSettable(control.largeHeightProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Badge control) {
                        return control.largeHeightProperty();
                    }
                };

        /// CSS metadata for the large minimum width token.
        private static final CssMetaData<M3Badge, Number> LARGE_MIN_WIDTH =
                new CssMetaData<>("-m3-large-min-width", SizeConverter.getInstance(), DEFAULT_LARGE_MIN_WIDTH) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Badge control) {
                        return M3Css.isSettable(control.largeMinWidthProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Badge control) {
                        return control.largeMinWidthProperty();
                    }
                };

        /// CSS metadata for the container shape token.
        private static final CssMetaData<M3Badge, Number> CONTAINER_SHAPE =
                new CssMetaData<>("-m3-container-shape", SizeConverter.getInstance(), DEFAULT_CONTAINER_SHAPE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Badge control) {
                        return M3Css.isSettable(control.containerShapeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Badge control) {
                        return control.containerShapeProperty();
                    }
                };

        /// CSS metadata for the horizontal padding token.
        private static final CssMetaData<M3Badge, Number> HORIZONTAL_PADDING =
                new CssMetaData<>("-m3-horizontal-padding", SizeConverter.getInstance(), DEFAULT_HORIZONTAL_PADDING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Badge control) {
                        return M3Css.isSettable(control.horizontalPaddingProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Badge control) {
                        return control.horizontalPaddingProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(SMALL_SIZE);
            styleables.add(LARGE_HEIGHT);
            styleables.add(LARGE_MIN_WIDTH);
            styleables.add(CONTAINER_SHAPE);
            styleables.add(HORIZONTAL_PADDING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
