// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.event.ActionEvent;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3NavigationItemSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 navigation item used by bars, rails, and related navigation containers.
///
/// `M3NavigationItem` is a selectable action with text, optional graphic content, and an optional [M3Badge].
/// Calling [#fire()] on an enabled item sets [#selectedProperty()] to `true` and then delivers an action event;
/// firing an already selected item does not clear selection. Calling [#setSelected(boolean)] changes state without
/// firing an event.
///
/// [M3NavigationBar], [M3NavigationRail], and related containers coordinate single selection and propagate their
/// preferred [#itemLayoutProperty()] to each item. The default standalone item is unselected, uses vertical layout,
/// has empty text, and has no graphic or badge. Graphic and badge nodes are owned by the item while displayed and
/// must not belong to another parent.
///
/// See [Material Design navigation](https://m3.material.io/components/navigation-bar/overview).
@NotNullByDefault
public final class M3NavigationItem extends ButtonBase {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-navigation-item";

    /// The selected pseudo-class used by navigation items.
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    /// Marks a direct M3FX icon mounted as the navigation item graphic.
    private static final PseudoClass NAVIGATION_GRAPHIC_PSEUDO_CLASS =
            PseudoClass.getPseudoClass("m3-navigation-item-graphic");

    /// The direct M3FX icon currently managed as the navigation item graphic.
    private @Nullable Node managedIconGraphic;

    /// The default navigation item container height.
    private static final double DEFAULT_CONTAINER_HEIGHT = 80.0;

    /// The default navigation item width.
    private static final double DEFAULT_ITEM_WIDTH = 80.0;

    /// The default selected indicator width.
    private static final double DEFAULT_INDICATOR_WIDTH = 64.0;

    /// The default selected indicator height.
    private static final double DEFAULT_INDICATOR_HEIGHT = 32.0;

    /// The default selected indicator shape radius.
    private static final double DEFAULT_INDICATOR_SHAPE = 16.0;

    /// The default content spacing.
    private static final double DEFAULT_CONTENT_SPACING = 4.0;

    /// Creates an unselected navigation item with empty text, vertical layout, and no graphic or badge.
    public M3NavigationItem() {
        this("", null);
    }

    /// Creates a navigation item with text.
    ///
    /// @param text the navigation item label
    /// @throws NullPointerException if `text` is `null`
    public M3NavigationItem(String text) {
        this(text, null);
    }

    /// Creates a navigation item with text and graphic content.
    ///
    /// @param text    the navigation item label
    /// @param graphic the graphic node, or `null` for no graphic
    /// @throws NullPointerException if `text` is `null`
    public M3NavigationItem(String text, @Nullable Node graphic) {
        this(text, graphic, null);
    }

    /// Creates a navigation item with text, graphic content, and a badge.
    ///
    /// @param text    the navigation item label
    /// @param graphic the graphic node, or `null` for no graphic
    /// @param badge   the badge shown over the graphic, or `null` for no badge
    /// @throws NullPointerException if `text` is `null`
    public M3NavigationItem(String text, @Nullable Node graphic, @Nullable M3Badge badge) {
        super(Objects.requireNonNull(text, "text"), graphic);
        initialize();
        setBadge(badge);
    }

    /// Whether this item represents the selected destination.
    ///
    /// Changing the property updates visual and accessibility state but does not fire an action event. A containing
    /// navigation control observes direct and bound changes and restores its single-selection policy.
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

    /// Returns whether this navigation item is selected.
    ///
    /// @return `true` when this navigation item is selected
    public final boolean isSelected() {
        return selected.get();
    }

    /// Sets whether this navigation item is selected.
    ///
    /// @param selected whether this navigation item is selected
    public final void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    /// Returns the observable, bindable selected-state property.
    ///
    /// The property is `false` by default. Changes update visual and accessibility state without firing an action
    /// event; a containing navigation control may reconcile its single-selection policy.
    ///
    /// @return the selected-state property
    public final BooleanProperty selectedProperty() {
        return selected;
    }

    /// The icon and label arrangement used by this item.
    ///
    /// A direct assignment of `null` is replaced with [M3NavigationItemLayout#VERTICAL]. A containing navigation
    /// control may overwrite this value to keep all destinations consistent.
    ///
    /// @defaultValue [M3NavigationItemLayout#VERTICAL]
    private final ObjectProperty<M3NavigationItemLayout> itemLayoutState =
            new SimpleObjectProperty<>(this, "itemLayout", M3NavigationItemLayout.VERTICAL) {
                /// Maintains the matching layout style class and requests a new skin layout.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3NavigationItemLayout.VERTICAL);
                        return;
                    }
                    updateItemLayoutStyleClass();
                    requestLayout();
                }
            };

    /// Returns the icon and label arrangement.
    ///
    /// @return the current navigation item layout
    public final M3NavigationItemLayout getItemLayout() {
        return itemLayoutState.get();
    }

    /// Sets the icon and label arrangement.
    ///
    /// Navigation bars and rails normally set this property for their child items.
    ///
    /// @param itemLayout the navigation item layout
    /// @throws NullPointerException if `itemLayout` is `null`
    public final void setItemLayout(M3NavigationItemLayout itemLayout) {
        this.itemLayoutState.set(Objects.requireNonNull(itemLayout, "itemLayout"));
    }

    /// Returns the observable, bindable icon-and-label layout property.
    ///
    /// The property is [M3NavigationItemLayout#VERTICAL] by default. A direct `null` assignment restores that
    /// default; changes update the layout style class and request layout.
    ///
    /// @return the navigation-item layout property
    public final ObjectProperty<M3NavigationItemLayout> itemLayoutProperty() {
        return itemLayoutState;
    }

    /// The optional badge associated with this destination.
    ///
    /// The badge node is owned by this item while displayed and may have only one parent.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable M3Badge> badge = new SimpleObjectProperty<>(this, "badge");

    /// Returns the badge displayed over this item's graphic.
    ///
    /// @return the badge shown over the graphic, or `null` when no badge is set
    public final @Nullable M3Badge getBadge() {
        return badge.get();
    }

    /// Sets the badge displayed over this item's graphic.
    ///
    /// @param badge the badge shown over the graphic, or `null` for no badge
    public final void setBadge(@Nullable M3Badge badge) {
        this.badge.set(badge);
    }

    /// Returns the observable, bindable badge property.
    ///
    /// The property is `null` by default. A non-null badge is displayed over the graphic and is owned by this item
    /// while attached to its scene graph.
    ///
    /// @return the badge property
    public final ObjectProperty<@Nullable M3Badge> badgeProperty() {
        return badge;
    }

    /// The item container height in logical pixels.
    ///
    /// Values must be finite and non-negative.
    ///
    /// @defaultValue `80.0`
    private @Nullable StyleableDoubleProperty containerHeight;

    /// Returns the navigation item container height.
    ///
    /// @return the navigation item container height in logical pixels
    public final double getContainerHeight() {
        return containerHeight == null ? DEFAULT_CONTAINER_HEIGHT : containerHeight.get();
    }

    /// Sets the navigation item container height.
    ///
    /// @param containerHeight the navigation item container height in logical pixels
    /// @throws IllegalArgumentException if `containerHeight` is negative or not finite
    public final void setContainerHeight(double containerHeight) {
        containerHeightProperty().set(M3Css.nonNegative(containerHeight, "containerHeight"));
    }

    /// Returns the observable, bindable, CSS-styleable container-height property.
    ///
    /// The property is `80.0` logical pixels by default, accepts only finite non-negative values, and is styleable
    /// through `-m3-container-height`.
    ///
    /// @return the container-height property
    public final StyleableDoubleProperty containerHeightProperty() {
        if (containerHeight == null) {
            containerHeight = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTAINER_HEIGHT,
                    this,
                    "containerHeight",
                    StyleableProperties.CONTAINER_HEIGHT,
                    this::updateMetrics
            );
        }
        return containerHeight;
    }

    /// The preferred item width in logical pixels.
    ///
    /// Values must be finite and non-negative.
    ///
    /// @defaultValue `80.0`
    private @Nullable StyleableDoubleProperty itemWidth;

    /// Returns the navigation item width.
    ///
    /// @return the navigation item width in logical pixels
    public final double getItemWidth() {
        return itemWidth == null ? DEFAULT_ITEM_WIDTH : itemWidth.get();
    }

    /// Sets the navigation item width.
    ///
    /// @param itemWidth the navigation item width in logical pixels
    /// @throws IllegalArgumentException if `itemWidth` is negative or not finite
    public final void setItemWidth(double itemWidth) {
        itemWidthProperty().set(M3Css.nonNegative(itemWidth, "itemWidth"));
    }

    /// Returns the observable, bindable, CSS-styleable item-width property.
    ///
    /// The property is `80.0` logical pixels by default, accepts only finite non-negative values, and is styleable
    /// through `-m3-item-width`.
    ///
    /// @return the item-width property
    public final StyleableDoubleProperty itemWidthProperty() {
        if (itemWidth == null) {
            itemWidth = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_ITEM_WIDTH,
                    this,
                    "itemWidth",
                    StyleableProperties.ITEM_WIDTH,
                    this::updateMetrics
            );
        }
        return itemWidth;
    }

    /// The selected indicator width in logical pixels.
    ///
    /// Values must be finite and non-negative.
    ///
    /// @defaultValue `64.0`
    private @Nullable StyleableDoubleProperty indicatorWidth;

    /// Returns the selected indicator width.
    ///
    /// @return the selected indicator width in logical pixels
    public final double getIndicatorWidth() {
        return indicatorWidth == null ? DEFAULT_INDICATOR_WIDTH : indicatorWidth.get();
    }

    /// Sets the selected indicator width.
    ///
    /// @param indicatorWidth the selected indicator width in logical pixels
    /// @throws IllegalArgumentException if `indicatorWidth` is negative or not finite
    public final void setIndicatorWidth(double indicatorWidth) {
        indicatorWidthProperty().set(M3Css.nonNegative(indicatorWidth, "indicatorWidth"));
    }

    /// Returns the observable, bindable, CSS-styleable selected-indicator-width property.
    ///
    /// The property is `64.0` logical pixels by default, accepts only finite non-negative values, and is styleable
    /// through `-m3-indicator-width`.
    ///
    /// @return the selected-indicator-width property
    public final StyleableDoubleProperty indicatorWidthProperty() {
        if (indicatorWidth == null) {
            indicatorWidth = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_INDICATOR_WIDTH,
                    this,
                    "indicatorWidth",
                    StyleableProperties.INDICATOR_WIDTH,
                    this::requestLayout
            );
        }
        return indicatorWidth;
    }

    /// The selected indicator height in logical pixels.
    ///
    /// Values must be finite and non-negative.
    ///
    /// @defaultValue `32.0`
    private @Nullable StyleableDoubleProperty indicatorHeight;

    /// Returns the selected indicator height.
    ///
    /// @return the selected indicator height in logical pixels
    public final double getIndicatorHeight() {
        return indicatorHeight == null ? DEFAULT_INDICATOR_HEIGHT : indicatorHeight.get();
    }

    /// Sets the selected indicator height.
    ///
    /// @param indicatorHeight the selected indicator height in logical pixels
    /// @throws IllegalArgumentException if `indicatorHeight` is negative or not finite
    public final void setIndicatorHeight(double indicatorHeight) {
        indicatorHeightProperty().set(M3Css.nonNegative(indicatorHeight, "indicatorHeight"));
    }

    /// Returns the observable, bindable, CSS-styleable selected-indicator-height property.
    ///
    /// The property is `32.0` logical pixels by default, accepts only finite non-negative values, and is styleable
    /// through `-m3-indicator-height`.
    ///
    /// @return the selected-indicator-height property
    public final StyleableDoubleProperty indicatorHeightProperty() {
        if (indicatorHeight == null) {
            indicatorHeight = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_INDICATOR_HEIGHT,
                    this,
                    "indicatorHeight",
                    StyleableProperties.INDICATOR_HEIGHT,
                    this::requestLayout
            );
        }
        return indicatorHeight;
    }

    /// The selected indicator corner radius in logical pixels.
    ///
    /// Values must be finite and non-negative.
    ///
    /// @defaultValue `16.0`
    private @Nullable StyleableDoubleProperty indicatorShape;

    /// Returns the selected indicator corner radius.
    ///
    /// @return the selected indicator corner radius in logical pixels
    public final double getIndicatorShape() {
        return indicatorShape == null ? DEFAULT_INDICATOR_SHAPE : indicatorShape.get();
    }

    /// Sets the selected indicator corner radius.
    ///
    /// @param indicatorShape the selected indicator corner radius in logical pixels
    /// @throws IllegalArgumentException if `indicatorShape` is negative or not finite
    public final void setIndicatorShape(double indicatorShape) {
        indicatorShapeProperty().set(M3Css.nonNegative(indicatorShape, "indicatorShape"));
    }

    /// Returns the observable, bindable, CSS-styleable selected-indicator-shape property.
    ///
    /// The property is `16.0` logical pixels by default, accepts only finite non-negative values, and is styleable
    /// through `-m3-indicator-shape`.
    ///
    /// @return the selected-indicator-shape property
    public final StyleableDoubleProperty indicatorShapeProperty() {
        if (indicatorShape == null) {
            indicatorShape = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_INDICATOR_SHAPE,
                    this,
                    "indicatorShape",
                    StyleableProperties.INDICATOR_SHAPE,
                    this::requestLayout
            );
        }
        return indicatorShape;
    }

    /// The spacing between the graphic and label in logical pixels.
    ///
    /// Values must be finite and non-negative.
    ///
    /// @defaultValue `4.0`
    private @Nullable StyleableDoubleProperty contentSpacing;

    /// Returns the spacing between the graphic and label.
    ///
    /// @return the spacing between item content nodes in logical pixels
    public final double getContentSpacing() {
        return contentSpacing == null ? DEFAULT_CONTENT_SPACING : contentSpacing.get();
    }

    /// Sets the spacing between the graphic and label.
    ///
    /// @param contentSpacing the spacing between item content nodes in logical pixels
    /// @throws IllegalArgumentException if `contentSpacing` is negative or not finite
    public final void setContentSpacing(double contentSpacing) {
        contentSpacingProperty().set(M3Css.nonNegative(contentSpacing, "contentSpacing"));
    }

    /// Returns the observable, bindable, CSS-styleable content-spacing property.
    ///
    /// The property is `4.0` logical pixels by default, accepts only finite non-negative values, and is styleable
    /// through `-m3-content-spacing`.
    ///
    /// @return the content-spacing property
    public final StyleableDoubleProperty contentSpacingProperty() {
        if (contentSpacing == null) {
            contentSpacing = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTENT_SPACING,
                    this,
                    "contentSpacing",
                    StyleableProperties.CONTENT_SPACING,
                    this::requestLayout
            );
        }
        return contentSpacing;
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the CSS metadata for `M3NavigationItem`
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Returns accessibility attributes for the navigation item selection state.
    ///
    /// @param attribute  the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` when no value is available
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case SELECTED -> isSelected();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Selects this item and fires its action event when enabled.
    ///
    /// A disabled item performs neither operation. This method never clears selection.
    @Override
    public void fire() {
        if (!isDisabled()) {
            setSelected(true);
            fireEvent(new ActionEvent(this, this));
        }
    }

    /// Creates the default Material Design 3 navigation item skin.
    ///
    /// @return the default Material Design 3 navigation item skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3NavigationItemSkin(this);
    }

    /// Returns the user-agent stylesheet for M3FX navigation controls.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("navigation-bar.css");
    }

    /// Adds base style classes and applies size-related defaults.
    private void initialize() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.BUTTON);
        setFocusTraversable(true);
        setPickOnBounds(true);
        updateItemLayoutStyleClass();
        graphicProperty().addListener(observable -> updateGraphicStyle());
        updateGraphicStyle();
        updateMetrics();
    }

    /// Applies navigation-item semantic coloring to a direct M3FX icon and clears stale managed state.
    private void updateGraphicStyle() {
        @Nullable Node graphic = getGraphic();
        @Nullable Node previous = managedIconGraphic;
        if (previous == graphic) {
            return;
        }
        if (previous != null) {
            previous.pseudoClassStateChanged(NAVIGATION_GRAPHIC_PSEUDO_CLASS, false);
        }
        managedIconGraphic = graphic instanceof M3IconGraphic ? graphic : null;
        if (managedIconGraphic != null) {
            managedIconGraphic.pseudoClassStateChanged(NAVIGATION_GRAPHIC_PSEUDO_CLASS, true);
        }
    }

    /// Applies the style class matching the icon and label arrangement.
    private void updateItemLayoutStyleClass() {
        M3ControlStyles.replaceVariant(
                this,
                getItemLayout().styleClass(),
                M3NavigationItemLayout.VERTICAL.styleClass(),
                M3NavigationItemLayout.HORIZONTAL.styleClass()
        );
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double width = getItemWidth();
        double height = getContainerHeight();
        setMinSize(width, height);
        setPrefSize(width, height);
        setMaxSize(width, height);
    }

    /// CSS metadata for M3FX navigation item component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the container height token.
        private static final CssMetaData<M3NavigationItem, Number> CONTAINER_HEIGHT =
                new CssMetaData<>("-m3-container-height", SizeConverter.getInstance(), DEFAULT_CONTAINER_HEIGHT) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3NavigationItem control) {
                        return M3Css.isSettable(control.containerHeightProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3NavigationItem control) {
                        return control.containerHeightProperty();
                    }
                };

        /// CSS metadata for the item width token.
        private static final CssMetaData<M3NavigationItem, Number> ITEM_WIDTH =
                new CssMetaData<>("-m3-item-width", SizeConverter.getInstance(), DEFAULT_ITEM_WIDTH) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3NavigationItem control) {
                        return M3Css.isSettable(control.itemWidthProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3NavigationItem control) {
                        return control.itemWidthProperty();
                    }
                };

        /// CSS metadata for the selected indicator width token.
        private static final CssMetaData<M3NavigationItem, Number> INDICATOR_WIDTH =
                new CssMetaData<>("-m3-indicator-width", SizeConverter.getInstance(), DEFAULT_INDICATOR_WIDTH) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3NavigationItem control) {
                        return M3Css.isSettable(control.indicatorWidthProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3NavigationItem control) {
                        return control.indicatorWidthProperty();
                    }
                };

        /// CSS metadata for the selected indicator height token.
        private static final CssMetaData<M3NavigationItem, Number> INDICATOR_HEIGHT =
                new CssMetaData<>("-m3-indicator-height", SizeConverter.getInstance(), DEFAULT_INDICATOR_HEIGHT) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3NavigationItem control) {
                        return M3Css.isSettable(control.indicatorHeightProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3NavigationItem control) {
                        return control.indicatorHeightProperty();
                    }
                };

        /// CSS metadata for the selected indicator shape token.
        private static final CssMetaData<M3NavigationItem, Number> INDICATOR_SHAPE =
                new CssMetaData<>("-m3-indicator-shape", SizeConverter.getInstance(), DEFAULT_INDICATOR_SHAPE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3NavigationItem control) {
                        return M3Css.isSettable(control.indicatorShapeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3NavigationItem control) {
                        return control.indicatorShapeProperty();
                    }
                };

        /// CSS metadata for the content spacing token.
        private static final CssMetaData<M3NavigationItem, Number> CONTENT_SPACING =
                new CssMetaData<>("-m3-content-spacing", SizeConverter.getInstance(), DEFAULT_CONTENT_SPACING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3NavigationItem control) {
                        return M3Css.isSettable(control.contentSpacingProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3NavigationItem control) {
                        return control.contentSpacingProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(ButtonBase.getClassCssMetaData());
            styleables.add(CONTAINER_HEIGHT);
            styleables.add(ITEM_WIDTH);
            styleables.add(INDICATOR_WIDTH);
            styleables.add(INDICATOR_HEIGHT);
            styleables.add(INDICATOR_SHAPE);
            styleables.add(CONTENT_SPACING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
