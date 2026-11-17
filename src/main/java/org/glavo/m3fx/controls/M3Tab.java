// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.SimpleStyleableBooleanProperty;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Skin;
import org.glavo.m3fx.animation.M3Animated;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3MotionProperties;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3TabSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 tab used to switch between peer content views.
///
/// `M3Tab` is a selectable [ButtonBase] with token-backed height, minimum width, padding, and active-indicator
/// metrics. Use [M3TabBar] to manage a set of tabs, selected state, keyboard navigation, and accessibility
/// selection attributes.
///
/// Activating a tab selects it and fires an [ActionEvent]. When the tab belongs to an `M3TabBar`, the bar clears
/// the previous selection and applies its empty-selection policy. A standalone tab is unselected by default and can
/// be selected directly through [#selectedProperty()].
///
/// Computed minimum width equals the tab-minimum-width token, and computed minimum and preferred heights equal
/// the container-height token. These are total dimensions, including insets, and are available before skin
/// installation. Explicit dimensions and bindings remain in effect through token changes; set a dimension to
/// [#USE_COMPUTED_SIZE] to restore computed sizing. Preferred width follows the skin's content measurements.
///
/// See [Material Design tabs](https://m3.material.io/components/tabs/overview).
@NotNullByDefault
public final class M3Tab extends ButtonBase implements M3Animated {

    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-tab";

    /// The selected pseudo-class used by tabs.
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    /// The pseudo-class inherited from a secondary tab bar.
    private static final PseudoClass SECONDARY_PSEUDO_CLASS = PseudoClass.getPseudoClass("secondary");

    /// The default tab container height.
    private static final double DEFAULT_CONTAINER_HEIGHT = 48.0;

    /// The default tab minimum width.
    private static final double DEFAULT_TAB_MIN_WIDTH = 90.0;

    /// The default horizontal content padding.
    private static final double DEFAULT_HORIZONTAL_PADDING = 16.0;

    /// The default active indicator height.
    private static final double DEFAULT_ACTIVE_INDICATOR_HEIGHT = 3.0;

    /// The default active indicator shape radius.
    private static final double DEFAULT_ACTIVE_INDICATOR_SHAPE = 3.0;

    /// The default minimum length of a primary active indicator.
    private static final double DEFAULT_ACTIVE_INDICATOR_MIN_WIDTH = 24.0;

    /// The default extension on each side of primary tab content.
    private static final double DEFAULT_ACTIVE_INDICATOR_HORIZONTAL_INSET = 2.0;

    /// Creates an unselected tab with an empty label and no graphic.
    public M3Tab() {
        this("", null);
    }

    /// Creates an unselected tab with the specified label and no graphic.
    ///
    /// @param text the tab label
    /// @throws NullPointerException if `text` is `null`
    public M3Tab(String text) {
        this(text, null);
    }

    /// Creates an unselected tab with the specified label and graphic.
    ///
    /// @param text    the tab label
    /// @param graphic the graphic displayed with the label, or `null` for none
    /// @throws NullPointerException if `text` is `null`
    public M3Tab(String text, @Nullable Node graphic) {
        super(Objects.requireNonNull(text, "text"), graphic);
        initialize();
    }

    /// Whether this tab is selected.
    ///
    /// Direct changes are observed by a containing [M3TabBar], which enforces single selection synchronously.
    ///
    /// @defaultValue `false`
    private final BooleanProperty selected = new SimpleBooleanProperty(this, "selected") {
        /// Updates selected pseudo-class state.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, get());
            notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED);
        }
    };

    /// Returns whether this tab is selected.
    ///
    /// @return whether the active indicator and selected content color are shown
    public final boolean isSelected() {
        return selected.get();
    }

    /// Sets whether this tab is selected.
    ///
    /// When the tab belongs to an [M3TabBar], the bar enforces its selection policy after this value changes.
    ///
    /// @param selected whether this tab is selected
    public final void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    /// Returns the `selected` property.
    ///
    /// The returned property is observable and bindable. Its default value is `false`.
    ///
    /// @return the `selected` property
    public final BooleanProperty selectedProperty() {
        return selected;
    }

    /// The tab container height token property.
    private @Nullable StyleableDoubleProperty containerHeight;

    /// Returns the tab container height token.
    ///
    /// @return the container height in pixels
    public final double getContainerHeight() {
        return containerHeight == null ? DEFAULT_CONTAINER_HEIGHT : containerHeight.get();
    }

    /// Sets the tab container height token.
    ///
    /// @param containerHeight the finite, non-negative height in pixels
    /// @throws IllegalArgumentException if `containerHeight` is negative or non-finite
    public final void setContainerHeight(double containerHeight) {
        containerHeightProperty().set(M3Css.nonNegative(containerHeight, "containerHeight"));
    }

    /// Returns the `containerHeight` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `48.0` logical pixels.
    ///
    /// @return the `containerHeight` property
    public final StyleableDoubleProperty containerHeightProperty() {
        if (containerHeight == null) {
            containerHeight = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTAINER_HEIGHT,
                    this,
                    "containerHeight",
                    StyleableProperties.CONTAINER_HEIGHT,
                    this::requestLayout
            );
        }
        return containerHeight;
    }

    /// The tab minimum width token property.
    private @Nullable StyleableDoubleProperty tabMinWidth;

    /// Returns the tab minimum width token.
    ///
    /// @return the minimum tab width in pixels
    public final double getTabMinWidth() {
        return tabMinWidth == null ? DEFAULT_TAB_MIN_WIDTH : tabMinWidth.get();
    }

    /// Sets the tab minimum width token.
    ///
    /// @param tabMinWidth the finite, non-negative minimum width in pixels
    /// @throws IllegalArgumentException if `tabMinWidth` is negative or non-finite
    public final void setTabMinWidth(double tabMinWidth) {
        tabMinWidthProperty().set(M3Css.nonNegative(tabMinWidth, "tabMinWidth"));
    }

    /// Returns the `tabMinWidth` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `90.0` logical pixels.
    ///
    /// @return the `tabMinWidth` property
    public final StyleableDoubleProperty tabMinWidthProperty() {
        if (tabMinWidth == null) {
            tabMinWidth = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_TAB_MIN_WIDTH,
                    this,
                    "tabMinWidth",
                    StyleableProperties.TAB_MIN_WIDTH,
                    this::requestLayout
            );
        }
        return tabMinWidth;
    }

    /// The horizontal content padding token property.
    private @Nullable StyleableDoubleProperty horizontalPadding;

    /// Returns the horizontal content padding token.
    ///
    /// @return the padding on each horizontal side in pixels
    public final double getHorizontalPadding() {
        return horizontalPadding == null ? DEFAULT_HORIZONTAL_PADDING : horizontalPadding.get();
    }

    /// Sets the horizontal content padding token.
    ///
    /// @param horizontalPadding the finite, non-negative padding in pixels
    /// @throws IllegalArgumentException if `horizontalPadding` is negative or non-finite
    public final void setHorizontalPadding(double horizontalPadding) {
        horizontalPaddingProperty().set(M3Css.nonNegative(horizontalPadding, "horizontalPadding"));
    }

    /// Returns the `horizontalPadding` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `16.0` logical pixels.
    ///
    /// @return the `horizontalPadding` property
    public final StyleableDoubleProperty horizontalPaddingProperty() {
        if (horizontalPadding == null) {
            horizontalPadding = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_HORIZONTAL_PADDING,
                    this,
                    "horizontalPadding",
                    StyleableProperties.HORIZONTAL_PADDING,
                    this::updatePadding
            );
        }
        return horizontalPadding;
    }

    /// The active indicator height token property.
    private @Nullable StyleableDoubleProperty activeIndicatorHeight;

    /// Returns the active indicator height token.
    ///
    /// @return the active indicator height in pixels
    public final double getActiveIndicatorHeight() {
        return activeIndicatorHeight == null ? DEFAULT_ACTIVE_INDICATOR_HEIGHT : activeIndicatorHeight.get();
    }

    /// Sets the active indicator height token.
    ///
    /// @param activeIndicatorHeight the finite, non-negative indicator height in pixels
    /// @throws IllegalArgumentException if `activeIndicatorHeight` is negative or non-finite
    public final void setActiveIndicatorHeight(double activeIndicatorHeight) {
        activeIndicatorHeightProperty().set(M3Css.nonNegative(activeIndicatorHeight, "activeIndicatorHeight"));
    }

    /// Returns the `activeIndicatorHeight` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `3.0` logical pixels.
    ///
    /// @return the `activeIndicatorHeight` property
    public final StyleableDoubleProperty activeIndicatorHeightProperty() {
        if (activeIndicatorHeight == null) {
            activeIndicatorHeight = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_ACTIVE_INDICATOR_HEIGHT,
                    this,
                    "activeIndicatorHeight",
                    StyleableProperties.ACTIVE_INDICATOR_HEIGHT,
                    this::requestLayout
            );
        }
        return activeIndicatorHeight;
    }

    /// The active indicator shape token property.
    private @Nullable StyleableDoubleProperty activeIndicatorShape;

    /// Returns the active indicator shape token.
    ///
    /// @return the active indicator corner radius in pixels
    public final double getActiveIndicatorShape() {
        return activeIndicatorShape == null ? DEFAULT_ACTIVE_INDICATOR_SHAPE : activeIndicatorShape.get();
    }

    /// Sets the active indicator shape token.
    ///
    /// @param activeIndicatorShape the finite, non-negative corner radius in pixels
    /// @throws IllegalArgumentException if `activeIndicatorShape` is negative or non-finite
    public final void setActiveIndicatorShape(double activeIndicatorShape) {
        activeIndicatorShapeProperty().set(M3Css.nonNegative(activeIndicatorShape, "activeIndicatorShape"));
    }

    /// Returns the `activeIndicatorShape` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `3.0` logical pixels.
    ///
    /// @return the `activeIndicatorShape` property
    public final StyleableDoubleProperty activeIndicatorShapeProperty() {
        if (activeIndicatorShape == null) {
            activeIndicatorShape = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_ACTIVE_INDICATOR_SHAPE,
                    this,
                    "activeIndicatorShape",
                    StyleableProperties.ACTIVE_INDICATOR_SHAPE,
                    this::requestLayout
            );
        }
        return activeIndicatorShape;
    }

    /// The minimum primary active-indicator length property.
    private @Nullable StyleableDoubleProperty activeIndicatorMinWidth;

    /// Returns the minimum length of a primary active indicator.
    ///
    /// Secondary indicators fill the tab width and do not use this value.
    ///
    /// @return the minimum primary active-indicator length in pixels
    public final double getActiveIndicatorMinWidth() {
        return activeIndicatorMinWidth == null
                ? DEFAULT_ACTIVE_INDICATOR_MIN_WIDTH
                : activeIndicatorMinWidth.get();
    }

    /// Sets the minimum length of a primary active indicator.
    ///
    /// @param activeIndicatorMinWidth the minimum indicator length in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setActiveIndicatorMinWidth(double activeIndicatorMinWidth) {
        activeIndicatorMinWidthProperty().set(M3Css.nonNegative(
                activeIndicatorMinWidth,
                "activeIndicatorMinWidth"
        ));
    }

    /// Returns the `activeIndicatorMinWidth` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `24.0` logical pixels.
    ///
    /// @return the `activeIndicatorMinWidth` property
    public final StyleableDoubleProperty activeIndicatorMinWidthProperty() {
        if (activeIndicatorMinWidth == null) {
            activeIndicatorMinWidth = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_ACTIVE_INDICATOR_MIN_WIDTH,
                    this,
                    "activeIndicatorMinWidth",
                    StyleableProperties.ACTIVE_INDICATOR_MIN_WIDTH,
                    this::requestLayout
            );
        }
        return activeIndicatorMinWidth;
    }

    /// The primary active-indicator horizontal-inset property.
    private @Nullable StyleableDoubleProperty activeIndicatorHorizontalInset;

    /// Returns the extension on each side of primary tab content used to size its active indicator.
    ///
    /// Secondary indicators fill the tab width and do not use this value.
    ///
    /// @return the primary active-indicator extension on each side in pixels
    public final double getActiveIndicatorHorizontalInset() {
        return activeIndicatorHorizontalInset == null
                ? DEFAULT_ACTIVE_INDICATOR_HORIZONTAL_INSET
                : activeIndicatorHorizontalInset.get();
    }

    /// Sets the extension on each side of primary tab content used to size its active indicator.
    ///
    /// @param activeIndicatorHorizontalInset the extension on each side in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setActiveIndicatorHorizontalInset(double activeIndicatorHorizontalInset) {
        activeIndicatorHorizontalInsetProperty().set(M3Css.nonNegative(
                activeIndicatorHorizontalInset,
                "activeIndicatorHorizontalInset"
        ));
    }

    /// Returns the `activeIndicatorHorizontalInset` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `2.0` logical pixels.
    ///
    /// @return the `activeIndicatorHorizontalInset` property
    public final StyleableDoubleProperty activeIndicatorHorizontalInsetProperty() {
        if (activeIndicatorHorizontalInset == null) {
            activeIndicatorHorizontalInset = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_ACTIVE_INDICATOR_HORIZONTAL_INSET,
                    this,
                    "activeIndicatorHorizontalInset",
                    StyleableProperties.ACTIVE_INDICATOR_HORIZONTAL_INSET,
                    this::requestLayout
            );
        }
        return activeIndicatorHorizontalInset;
    }

    /// The animation setting, or `null` until the property is first requested.
    private @Nullable StyleableBooleanProperty animationsEnabled;

    /// Returns whether this component permits optional animations.
    @Override
    public final boolean isAnimationsEnabled() {
        return animationsEnabled == null || animationsEnabled.get();
    }

    /// Sets whether this component permits optional animations.
    @Override
    public final void setAnimationsEnabled(boolean enabled) {
        animationsEnabledProperty().set(enabled);
    }

    /// Returns the bindable animation setting styled by `-m3-animations-enabled`.
    ///
    /// The default value is `true`; CSS inherits this setting from ancestor styles.
    @Override
    public final StyleableBooleanProperty animationsEnabledProperty() {
        if (animationsEnabled == null) {
            animationsEnabled = new SimpleStyleableBooleanProperty(
                    StyleableProperties.ANIMATIONS_ENABLED, this, "animationsEnabled", true);
        }
        return animationsEnabled;
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the shared, unmodifiable CSS metadata list for [M3Tab]
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Returns accessibility attributes for the tab selection state.
    ///
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case SELECTED -> isSelected();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Selects and fires this tab.
    @Override
    public void fire() {
        if (!isDisabled()) {
            setSelected(true);
            fireEvent(new ActionEvent(this, this));
        }
    }

    /// Creates the default Material Design 3 tab skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3TabSkin(this);
    }

    /// Returns the user-agent stylesheet for M3FX tabs.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("tab.css");
    }

    /// Adds base style classes and applies token metrics.
    private void initialize() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TAB_ITEM);
        setFocusTraversable(true);
        setPickOnBounds(true);
        updatePadding();
    }

    /// Computes minimum total width from the tab-minimum-width token.
    @Override
    protected double computeMinWidth(double height) {
        return getTabMinWidth();
    }

    /// Computes minimum total height from the container-height token.
    @Override
    protected double computeMinHeight(double width) {
        return getContainerHeight();
    }

    /// Computes preferred total height from the container-height token.
    @Override
    protected double computePrefHeight(double width) {
        return getContainerHeight();
    }

    /// Applies horizontal content padding from its component token.
    private void updatePadding() {
        double padding = getHorizontalPadding();
        M3Css.setPaddingIfUnbound(this, new Insets(0.0, padding, 0.0, padding));
    }

    /// Applies the visual role inherited from the containing tab bar.
    ///
    /// @param variant the containing tab bar variant
    final void updateNavigationVariant(M3TabBarVariant variant) {
        pseudoClassStateChanged(SECONDARY_PSEUDO_CLASS, variant == M3TabBarVariant.SECONDARY);
        requestLayout();
    }

    /// CSS metadata for M3FX tab component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the inheritable animation setting.
        private static final CssMetaData<M3Tab, Boolean> ANIMATIONS_ENABLED =
                M3MotionProperties.cssMetaData(
                        c -> c.animationsEnabledProperty(),
                        c -> c.animationsEnabled == null || !c.animationsEnabled.isBound());

        /// CSS metadata for the container height token.
        private static final CssMetaData<M3Tab, Number> CONTAINER_HEIGHT =
                M3Css.sizeMetaData("-m3-container-height", DEFAULT_CONTAINER_HEIGHT, M3Tab::containerHeightProperty);

        /// CSS metadata for the tab minimum width token.
        private static final CssMetaData<M3Tab, Number> TAB_MIN_WIDTH =
                M3Css.sizeMetaData("-m3-tab-min-width", DEFAULT_TAB_MIN_WIDTH, M3Tab::tabMinWidthProperty);

        /// CSS metadata for the horizontal padding token.
        private static final CssMetaData<M3Tab, Number> HORIZONTAL_PADDING =
                M3Css.sizeMetaData("-m3-horizontal-padding", DEFAULT_HORIZONTAL_PADDING, M3Tab::horizontalPaddingProperty);

        /// CSS metadata for the active indicator height token.
        private static final CssMetaData<M3Tab, Number> ACTIVE_INDICATOR_HEIGHT =
                M3Css.sizeMetaData("-m3-active-indicator-height", DEFAULT_ACTIVE_INDICATOR_HEIGHT, M3Tab::activeIndicatorHeightProperty);

        /// CSS metadata for the active indicator shape token.
        private static final CssMetaData<M3Tab, Number> ACTIVE_INDICATOR_SHAPE =
                M3Css.sizeMetaData("-m3-active-indicator-shape", DEFAULT_ACTIVE_INDICATOR_SHAPE, M3Tab::activeIndicatorShapeProperty);

        /// CSS metadata for the minimum primary active-indicator length token.
        private static final CssMetaData<M3Tab, Number> ACTIVE_INDICATOR_MIN_WIDTH =
                M3Css.sizeMetaData(
                        "-m3-active-indicator-min-width",
                        DEFAULT_ACTIVE_INDICATOR_MIN_WIDTH,
                        M3Tab::activeIndicatorMinWidthProperty
                );

        /// CSS metadata for the primary active-indicator horizontal inset token.
        private static final CssMetaData<M3Tab, Number> ACTIVE_INDICATOR_HORIZONTAL_INSET =
                M3Css.sizeMetaData(
                        "-m3-active-indicator-horizontal-inset",
                        DEFAULT_ACTIVE_INDICATOR_HORIZONTAL_INSET,
                        M3Tab::activeIndicatorHorizontalInsetProperty
                );

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(ButtonBase.getClassCssMetaData());
            styleables.add(ANIMATIONS_ENABLED);
            styleables.add(CONTAINER_HEIGHT);
            styleables.add(TAB_MIN_WIDTH);
            styleables.add(HORIZONTAL_PADDING);
            styleables.add(ACTIVE_INDICATOR_HEIGHT);
            styleables.add(ACTIVE_INDICATOR_SHAPE);
            styleables.add(ACTIVE_INDICATOR_MIN_WIDTH);
            styleables.add(ACTIVE_INDICATOR_HORIZONTAL_INSET);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }

    }
}
