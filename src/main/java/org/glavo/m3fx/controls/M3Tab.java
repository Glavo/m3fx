// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3TabSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 tab.
@NotNullByDefault
public class M3Tab extends ButtonBase {
    /// The base style class for M3FX tabs.
    public static final String STYLE_CLASS = "m3-tab";

    /// The selected pseudo-class used by tabs.
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

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

    /// The styleable container height token.
    private StyleableDoubleProperty containerHeight;

    /// The styleable tab minimum width token.
    private StyleableDoubleProperty tabMinWidth;

    /// The styleable horizontal padding token.
    private StyleableDoubleProperty horizontalPadding;

    /// The styleable active indicator height token.
    private StyleableDoubleProperty activeIndicatorHeight;

    /// The styleable active indicator shape token.
    private StyleableDoubleProperty activeIndicatorShape;

    /// The selected state property.
    private final BooleanProperty selected = new SimpleBooleanProperty(this, "selected") {
        /// Updates selected pseudo-class state.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, get());
        }
    };

    /// Creates an empty tab.
    public M3Tab() {
        this("", null);
    }

    /// Creates a tab with text.
    public M3Tab(String text) {
        this(text, null);
    }

    /// Creates a tab with text and graphic content.
    public M3Tab(String text, @Nullable Node graphic) {
        super(Objects.requireNonNull(text, "text"), graphic);
        initialize();
    }

    /// Creates a tab with text and the requested selected state.
    public static M3Tab withSelected(String text, boolean selected) {
        return withSelected(text, null, selected);
    }

    /// Creates a tab with text, graphic content, and the requested selected state.
    public static M3Tab withSelected(String text, @Nullable Node graphic, boolean selected) {
        M3Tab tab = new M3Tab(text, graphic);
        tab.setSelected(selected);
        return tab;
    }

    /// Returns whether this tab is selected.
    public final boolean isSelected() {
        return selected.get();
    }

    /// Sets whether this tab is selected.
    public final void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    /// Returns the selected state property.
    public final BooleanProperty selectedProperty() {
        return selected;
    }

    /// Returns the tab container height token.
    public final double getContainerHeight() {
        return containerHeight == null ? DEFAULT_CONTAINER_HEIGHT : containerHeight.get();
    }

    /// Sets the tab container height token.
    public final void setContainerHeight(double containerHeight) {
        containerHeightProperty().set(M3Css.nonNegative(containerHeight, "containerHeight"));
    }

    /// Returns the tab container height token property.
    public final StyleableDoubleProperty containerHeightProperty() {
        if (containerHeight == null) {
            containerHeight = createStyleableDoubleProperty(
                    DEFAULT_CONTAINER_HEIGHT,
                    "containerHeight",
                    StyleableProperties.CONTAINER_HEIGHT,
                    true
            );
        }
        return containerHeight;
    }

    /// Returns the tab minimum width token.
    public final double getTabMinWidth() {
        return tabMinWidth == null ? DEFAULT_TAB_MIN_WIDTH : tabMinWidth.get();
    }

    /// Sets the tab minimum width token.
    public final void setTabMinWidth(double tabMinWidth) {
        tabMinWidthProperty().set(M3Css.nonNegative(tabMinWidth, "tabMinWidth"));
    }

    /// Returns the tab minimum width token property.
    public final StyleableDoubleProperty tabMinWidthProperty() {
        if (tabMinWidth == null) {
            tabMinWidth = createStyleableDoubleProperty(
                    DEFAULT_TAB_MIN_WIDTH,
                    "tabMinWidth",
                    StyleableProperties.TAB_MIN_WIDTH,
                    true
            );
        }
        return tabMinWidth;
    }

    /// Returns the horizontal content padding token.
    public final double getHorizontalPadding() {
        return horizontalPadding == null ? DEFAULT_HORIZONTAL_PADDING : horizontalPadding.get();
    }

    /// Sets the horizontal content padding token.
    public final void setHorizontalPadding(double horizontalPadding) {
        horizontalPaddingProperty().set(M3Css.nonNegative(horizontalPadding, "horizontalPadding"));
    }

    /// Returns the horizontal content padding token property.
    public final StyleableDoubleProperty horizontalPaddingProperty() {
        if (horizontalPadding == null) {
            horizontalPadding = createStyleableDoubleProperty(
                    DEFAULT_HORIZONTAL_PADDING,
                    "horizontalPadding",
                    StyleableProperties.HORIZONTAL_PADDING,
                    true
            );
        }
        return horizontalPadding;
    }

    /// Returns the active indicator height token.
    public final double getActiveIndicatorHeight() {
        return activeIndicatorHeight == null ? DEFAULT_ACTIVE_INDICATOR_HEIGHT : activeIndicatorHeight.get();
    }

    /// Sets the active indicator height token.
    public final void setActiveIndicatorHeight(double activeIndicatorHeight) {
        activeIndicatorHeightProperty().set(M3Css.nonNegative(activeIndicatorHeight, "activeIndicatorHeight"));
    }

    /// Returns the active indicator height token property.
    public final StyleableDoubleProperty activeIndicatorHeightProperty() {
        if (activeIndicatorHeight == null) {
            activeIndicatorHeight = createStyleableDoubleProperty(
                    DEFAULT_ACTIVE_INDICATOR_HEIGHT,
                    "activeIndicatorHeight",
                    StyleableProperties.ACTIVE_INDICATOR_HEIGHT,
                    false
            );
        }
        return activeIndicatorHeight;
    }

    /// Returns the active indicator shape token.
    public final double getActiveIndicatorShape() {
        return activeIndicatorShape == null ? DEFAULT_ACTIVE_INDICATOR_SHAPE : activeIndicatorShape.get();
    }

    /// Sets the active indicator shape token.
    public final void setActiveIndicatorShape(double activeIndicatorShape) {
        activeIndicatorShapeProperty().set(M3Css.nonNegative(activeIndicatorShape, "activeIndicatorShape"));
    }

    /// Returns the active indicator shape token property.
    public final StyleableDoubleProperty activeIndicatorShapeProperty() {
        if (activeIndicatorShape == null) {
            activeIndicatorShape = createStyleableDoubleProperty(
                    DEFAULT_ACTIVE_INDICATOR_SHAPE,
                    "activeIndicatorShape",
                    StyleableProperties.ACTIVE_INDICATOR_SHAPE,
                    false
            );
        }
        return activeIndicatorShape;
    }

    /// Returns the CSS metadata for this control class.
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
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
        M3ControlStyles.add(this, STYLE_CLASS);
        setFocusTraversable(true);
        updateMetrics();
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double height = getContainerHeight();
        double padding = getHorizontalPadding();
        setMinWidth(getTabMinWidth());
        setMinHeight(height);
        setPrefHeight(height);
        setPadding(new Insets(0.0, padding, 0.0, padding));
    }

    /// Creates a non-negative styleable double property.
    private StyleableDoubleProperty createStyleableDoubleProperty(
            double initialValue,
            String name,
            CssMetaData<M3Tab, Number> cssMetaData,
            boolean updateMetrics
    ) {
        return new StyleableDoubleProperty(initialValue) {
            /// Validates updated token values.
            @Override
            protected void invalidated() {
                set(M3Css.nonNegative(get(), name));
                if (updateMetrics) {
                    updateMetrics();
                } else {
                    requestLayout();
                }
            }

            /// Returns the owning bean.
            @Override
            public Object getBean() {
                return M3Tab.this;
            }

            /// Returns the property name.
            @Override
            public String getName() {
                return name;
            }

            /// Returns the CSS metadata for this property.
            @Override
            public CssMetaData<M3Tab, Number> getCssMetaData() {
                return cssMetaData;
            }
        };
    }

    /// CSS metadata for M3FX tab component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the container height token.
        private static final CssMetaData<M3Tab, Number> CONTAINER_HEIGHT =
                createSizeCssMetaData("-m3-container-height", DEFAULT_CONTAINER_HEIGHT, M3Tab::containerHeightProperty);

        /// CSS metadata for the tab minimum width token.
        private static final CssMetaData<M3Tab, Number> TAB_MIN_WIDTH =
                createSizeCssMetaData("-m3-tab-min-width", DEFAULT_TAB_MIN_WIDTH, M3Tab::tabMinWidthProperty);

        /// CSS metadata for the horizontal padding token.
        private static final CssMetaData<M3Tab, Number> HORIZONTAL_PADDING =
                createSizeCssMetaData("-m3-horizontal-padding", DEFAULT_HORIZONTAL_PADDING, M3Tab::horizontalPaddingProperty);

        /// CSS metadata for the active indicator height token.
        private static final CssMetaData<M3Tab, Number> ACTIVE_INDICATOR_HEIGHT =
                createSizeCssMetaData("-m3-active-indicator-height", DEFAULT_ACTIVE_INDICATOR_HEIGHT, M3Tab::activeIndicatorHeightProperty);

        /// CSS metadata for the active indicator shape token.
        private static final CssMetaData<M3Tab, Number> ACTIVE_INDICATOR_SHAPE =
                createSizeCssMetaData("-m3-active-indicator-shape", DEFAULT_ACTIVE_INDICATOR_SHAPE, M3Tab::activeIndicatorShapeProperty);

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(ButtonBase.getClassCssMetaData());
            styleables.add(CONTAINER_HEIGHT);
            styleables.add(TAB_MIN_WIDTH);
            styleables.add(HORIZONTAL_PADDING);
            styleables.add(ACTIVE_INDICATOR_HEIGHT);
            styleables.add(ACTIVE_INDICATOR_SHAPE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }

        /// Creates CSS metadata for a size token.
        private static CssMetaData<M3Tab, Number> createSizeCssMetaData(
                String property,
                double initialValue,
                StyleablePropertyProvider provider
        ) {
            return new CssMetaData<>(property, SizeConverter.getInstance(), initialValue) {
                /// Returns whether this property can be set by CSS.
                @Override
                public boolean isSettable(M3Tab control) {
                    return M3Css.isSettable(provider.property(control));
                }

                /// Returns the styleable property for a control.
                @Override
                public StyleableProperty<Number> getStyleableProperty(M3Tab control) {
                    return provider.property(control);
                }
            };
        }
    }

    /// Provides a styleable double property for a tab.
    @FunctionalInterface
    @NotNullByDefault
    private interface StyleablePropertyProvider {
        /// Returns the styleable property for a tab.
        StyleableDoubleProperty property(M3Tab control);
    }
}
