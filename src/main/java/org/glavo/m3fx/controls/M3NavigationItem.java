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
/// `M3NavigationItem` is a selectable [ButtonBase] with optional graphic content and badge support. Navigation
/// containers manage the selected state across their child items, while the item skin renders the selected
/// indicator, state layer, ripple, label, icon, and badge placement from Material component tokens.
///
/// See [Material Design navigation](https://m3.material.io/components/navigation-bar/overview).
@NotNullByDefault
public class M3NavigationItem extends ButtonBase {
    /// The base style class for M3FX navigation items.
    public static final String STYLE_CLASS = "m3-navigation-item";

    /// The selected pseudo-class used by navigation items.
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

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

    // The styleable container height token.
    private @Nullable StyleableDoubleProperty containerHeight;

    // The styleable item width token.
    private @Nullable StyleableDoubleProperty itemWidth;

    // The styleable selected indicator width token.
    private @Nullable StyleableDoubleProperty indicatorWidth;

    // The styleable selected indicator height token.
    private @Nullable StyleableDoubleProperty indicatorHeight;

    // The styleable selected indicator shape token.
    private @Nullable StyleableDoubleProperty indicatorShape;

    // The styleable content spacing token.
    private @Nullable StyleableDoubleProperty contentSpacing;

    // The selected state property.
    private final BooleanProperty selected = new SimpleBooleanProperty(this, "selected") {
        /// Updates selected pseudo-class state.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, get());
            notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED);
        }
    };

    // The badge displayed over the navigation item graphic.
    private final ObjectProperty<@Nullable M3Badge> badge = new SimpleObjectProperty<>(this, "badge");

    /// Creates an empty navigation item.
    public M3NavigationItem() {
        this("", null);
    }

    /// Creates a navigation item with text.
    ///
    /// @param text the navigation item label
    public M3NavigationItem(String text) {
        this(text, null);
    }

    /// Creates a navigation item with text and graphic content.
    ///
    /// @param text the navigation item label
    /// @param graphic the graphic node, or `null` for no graphic
    public M3NavigationItem(String text, @Nullable Node graphic) {
        this(text, graphic, null);
    }

    /// Creates a navigation item with text, graphic content, and a badge.
    ///
    /// @param text the navigation item label
    /// @param graphic the graphic node, or `null` for no graphic
    /// @param badge the badge shown over the graphic, or `null` for no badge
    public M3NavigationItem(String text, @Nullable Node graphic, @Nullable M3Badge badge) {
        super(Objects.requireNonNull(text, "text"), graphic);
        initialize();
        setBadge(badge);
    }

    /// Creates a navigation item with text and the requested selected state.
    ///
    /// @param text the navigation item label
    /// @param selected the initial selected state
    /// @return a navigation item with the requested selected state
    public static M3NavigationItem withSelected(String text, boolean selected) {
        return withSelected(text, null, null, selected);
    }

    /// Creates a navigation item with text, graphic content, and the requested selected state.
    ///
    /// @param text the navigation item label
    /// @param graphic the graphic node, or `null` for no graphic
    /// @param selected the initial selected state
    /// @return a navigation item with the requested selected state
    public static M3NavigationItem withSelected(String text, @Nullable Node graphic, boolean selected) {
        return withSelected(text, graphic, null, selected);
    }

    /// Creates a navigation item with text, graphic content, badge content, and the requested selected state.
    ///
    /// @param text the navigation item label
    /// @param graphic the graphic node, or `null` for no graphic
    /// @param badge the badge shown over the graphic, or `null` for no badge
    /// @param selected the initial selected state
    /// @return a navigation item with the requested selected state
    public static M3NavigationItem withSelected(
            String text,
            @Nullable Node graphic,
            @Nullable M3Badge badge,
            boolean selected
    ) {
        M3NavigationItem item = new M3NavigationItem(text, graphic, badge);
        item.setSelected(selected);
        return item;
    }

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

    /// Returns the selected state property.
    ///
    /// @return the writable selected state property
    public final BooleanProperty selectedProperty() {
        return selected;
    }

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

    /// Returns the badge property.
    ///
    /// @return the writable badge property
    public final ObjectProperty<@Nullable M3Badge> badgeProperty() {
        return badge;
    }

    /// Returns the navigation item container height token.
    ///
    /// @return the navigation item container height in pixels
    public final double getContainerHeight() {
        return containerHeight == null ? DEFAULT_CONTAINER_HEIGHT : containerHeight.get();
    }

    /// Sets the navigation item container height token.
    ///
    /// @param containerHeight the navigation item container height in pixels
    public final void setContainerHeight(double containerHeight) {
        containerHeightProperty().set(M3Css.nonNegative(containerHeight, "containerHeight"));
    }

    /// Returns the navigation item container height token property.
    ///
    /// @return the styleable navigation item container height property
    public final StyleableDoubleProperty containerHeightProperty() {
        if (containerHeight == null) {
            containerHeight = new StyleableDoubleProperty(DEFAULT_CONTAINER_HEIGHT) {
                /// Applies updated metrics when the token changes.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "containerHeight");
                    updateMetrics();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3NavigationItem.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "containerHeight";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3NavigationItem, Number> getCssMetaData() {
                    return StyleableProperties.CONTAINER_HEIGHT;
                }
            };
        }
        return containerHeight;
    }

    /// Returns the navigation item width token.
    ///
    /// @return the navigation item width in pixels
    public final double getItemWidth() {
        return itemWidth == null ? DEFAULT_ITEM_WIDTH : itemWidth.get();
    }

    /// Sets the navigation item width token.
    ///
    /// @param itemWidth the navigation item width in pixels
    public final void setItemWidth(double itemWidth) {
        itemWidthProperty().set(M3Css.nonNegative(itemWidth, "itemWidth"));
    }

    /// Returns the navigation item width token property.
    ///
    /// @return the styleable navigation item width property
    public final StyleableDoubleProperty itemWidthProperty() {
        if (itemWidth == null) {
            itemWidth = new StyleableDoubleProperty(DEFAULT_ITEM_WIDTH) {
                /// Applies updated metrics when the token changes.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "itemWidth");
                    updateMetrics();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3NavigationItem.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "itemWidth";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3NavigationItem, Number> getCssMetaData() {
                    return StyleableProperties.ITEM_WIDTH;
                }
            };
        }
        return itemWidth;
    }

    /// Returns the selected indicator width token.
    ///
    /// @return the selected indicator width in pixels
    public final double getIndicatorWidth() {
        return indicatorWidth == null ? DEFAULT_INDICATOR_WIDTH : indicatorWidth.get();
    }

    /// Sets the selected indicator width token.
    ///
    /// @param indicatorWidth the selected indicator width in pixels
    public final void setIndicatorWidth(double indicatorWidth) {
        indicatorWidthProperty().set(M3Css.nonNegative(indicatorWidth, "indicatorWidth"));
    }

    /// Returns the selected indicator width token property.
    ///
    /// @return the styleable selected indicator width property
    public final StyleableDoubleProperty indicatorWidthProperty() {
        if (indicatorWidth == null) {
            indicatorWidth = new StyleableDoubleProperty(DEFAULT_INDICATOR_WIDTH) {
                /// Requests layout when the token changes.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "indicatorWidth");
                    requestLayout();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3NavigationItem.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "indicatorWidth";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3NavigationItem, Number> getCssMetaData() {
                    return StyleableProperties.INDICATOR_WIDTH;
                }
            };
        }
        return indicatorWidth;
    }

    /// Returns the selected indicator height token.
    ///
    /// @return the selected indicator height in pixels
    public final double getIndicatorHeight() {
        return indicatorHeight == null ? DEFAULT_INDICATOR_HEIGHT : indicatorHeight.get();
    }

    /// Sets the selected indicator height token.
    ///
    /// @param indicatorHeight the selected indicator height in pixels
    public final void setIndicatorHeight(double indicatorHeight) {
        indicatorHeightProperty().set(M3Css.nonNegative(indicatorHeight, "indicatorHeight"));
    }

    /// Returns the selected indicator height token property.
    ///
    /// @return the styleable selected indicator height property
    public final StyleableDoubleProperty indicatorHeightProperty() {
        if (indicatorHeight == null) {
            indicatorHeight = new StyleableDoubleProperty(DEFAULT_INDICATOR_HEIGHT) {
                /// Requests layout when the token changes.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "indicatorHeight");
                    requestLayout();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3NavigationItem.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "indicatorHeight";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3NavigationItem, Number> getCssMetaData() {
                    return StyleableProperties.INDICATOR_HEIGHT;
                }
            };
        }
        return indicatorHeight;
    }

    /// Returns the selected indicator shape token.
    ///
    /// @return the selected indicator corner radius in pixels
    public final double getIndicatorShape() {
        return indicatorShape == null ? DEFAULT_INDICATOR_SHAPE : indicatorShape.get();
    }

    /// Sets the selected indicator shape token.
    ///
    /// @param indicatorShape the selected indicator corner radius in pixels
    public final void setIndicatorShape(double indicatorShape) {
        indicatorShapeProperty().set(M3Css.nonNegative(indicatorShape, "indicatorShape"));
    }

    /// Returns the selected indicator shape token property.
    ///
    /// @return the styleable selected indicator shape property
    public final StyleableDoubleProperty indicatorShapeProperty() {
        if (indicatorShape == null) {
            indicatorShape = new StyleableDoubleProperty(DEFAULT_INDICATOR_SHAPE) {
                /// Requests layout when the token changes.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "indicatorShape");
                    requestLayout();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3NavigationItem.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "indicatorShape";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3NavigationItem, Number> getCssMetaData() {
                    return StyleableProperties.INDICATOR_SHAPE;
                }
            };
        }
        return indicatorShape;
    }

    /// Returns the content spacing token.
    ///
    /// @return the spacing between item content nodes in pixels
    public final double getContentSpacing() {
        return contentSpacing == null ? DEFAULT_CONTENT_SPACING : contentSpacing.get();
    }

    /// Sets the content spacing token.
    ///
    /// @param contentSpacing the spacing between item content nodes in pixels
    public final void setContentSpacing(double contentSpacing) {
        contentSpacingProperty().set(M3Css.nonNegative(contentSpacing, "contentSpacing"));
    }

    /// Returns the content spacing token property.
    ///
    /// @return the styleable content spacing property
    public final StyleableDoubleProperty contentSpacingProperty() {
        if (contentSpacing == null) {
            contentSpacing = new StyleableDoubleProperty(DEFAULT_CONTENT_SPACING) {
                /// Requests layout when the token changes.
                @Override
                protected void invalidated() {
                    M3Css.nonNegative(get(), "contentSpacing");
                    requestLayout();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3NavigationItem.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "contentSpacing";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3NavigationItem, Number> getCssMetaData() {
                    return StyleableProperties.CONTENT_SPACING;
                }
            };
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
    /// @param attribute the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` when no value is available
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case SELECTED -> isSelected();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Selects and fires this navigation item.
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
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.BUTTON);
        setFocusTraversable(true);
        updateMetrics();
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
