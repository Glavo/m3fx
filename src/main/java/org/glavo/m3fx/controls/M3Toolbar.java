// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3FocusTraversal;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.skins.M3ToolbarSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 toolbar for related actions.
///
/// `M3Toolbar` hosts a row or column of action nodes and applies Material container, padding, spacing, shape, and
/// elevation tokens. It is intended for compact tool palettes, contextual editing actions, and docked tool areas
/// where the actions should be navigable as one toolbar.
///
/// See [Material Design toolbars](https://m3.material.io/components/toolbars/overview).
@NotNullByDefault
public class M3Toolbar extends Control {
    /// The base style class for M3FX toolbars.
    public static final String STYLE_CLASS = "m3-toolbar";

    /// The toolbar item slot style class.
    public static final String ITEM_SLOT_STYLE_CLASS = "m3-toolbar-item-slot";

    /// The default toolbar orientation.
    private static final Orientation DEFAULT_ORIENTATION = Orientation.HORIZONTAL;

    /// The default toolbar variant.
    private static final M3ToolbarVariant DEFAULT_VARIANT = M3ToolbarVariant.STANDARD;

    /// The default horizontal toolbar container height in pixels.
    private static final double DEFAULT_CONTAINER_HEIGHT = 64.0;

    /// The default vertical toolbar container width in pixels.
    private static final double DEFAULT_CONTAINER_WIDTH = 64.0;

    /// The default item slot size in pixels.
    private static final double DEFAULT_ITEM_SLOT_SIZE = 48.0;

    /// The default toolbar content padding in pixels.
    private static final double DEFAULT_CONTENT_PADDING = 8.0;

    /// The default toolbar item spacing in pixels.
    private static final double DEFAULT_ITEM_SPACING = 0.0;

    /// The mutable toolbar item list.
    private final ObservableList<Node> items = M3ObservableLists.nonNullElementList("item");

    // The toolbar visual variant backing property.
    private final ObjectProperty<M3ToolbarVariant> variant =
            new SimpleObjectProperty<>(this, "variant", DEFAULT_VARIANT) {
                /// Updates variant style classes when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(DEFAULT_VARIANT);
                        return;
                    }
                    updateVariantStyle();
                }
            };

    // The toolbar layout orientation backing property.
    private final ObjectProperty<Orientation> orientation =
            new SimpleObjectProperty<>(this, "orientation", DEFAULT_ORIENTATION) {
                /// Updates orientation style classes and layout metrics when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(DEFAULT_ORIENTATION);
                        return;
                    }
                    updateOrientationStyle();
                    updateMetrics();
                }
            };

    // The styleable horizontal toolbar height backing property.
    private @Nullable StyleableDoubleProperty containerHeight;

    // The styleable vertical toolbar width backing property.
    private @Nullable StyleableDoubleProperty containerWidth;

    // The styleable item slot size backing property.
    private @Nullable StyleableDoubleProperty itemSlotSize;

    // The styleable content padding backing property.
    private @Nullable StyleableDoubleProperty contentPadding;

    // The styleable item spacing backing property.
    private @Nullable StyleableDoubleProperty itemSpacing;

    /// Notifies accessibility clients when focus moves between toolbar actions.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, () -> M3Accessible.currentOrFirstFocusTarget(this, getItems()));

    /// Updates accessibility and layout after toolbar item changes.
    private final ListChangeListener<Node> itemsListener = change -> handleItemsChanged();

    /// Creates an empty toolbar.
    public M3Toolbar() {
        initialize();
    }

    /// Returns the mutable toolbar item list.
    ///
    /// @return the mutable toolbar item list
    public final ObservableList<Node> getItems() {
        return items;
    }





    /// Returns the toolbar visual variant.
    ///
    /// @return the toolbar visual variant
    public final M3ToolbarVariant getVariant() {
        return variant.get();
    }

    /// Sets the toolbar visual variant.
    ///
    /// @param variant the toolbar visual variant
    public final void setVariant(M3ToolbarVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the toolbar visual variant property.
    ///
    /// @return the toolbar visual variant property
    public final ObjectProperty<M3ToolbarVariant> variantProperty() {
        return variant;
    }

    /// Returns the toolbar layout orientation.
    ///
    /// @return the toolbar layout orientation
    public final Orientation getOrientation() {
        return orientation.get();
    }

    /// Sets the toolbar layout orientation.
    ///
    /// @param orientation the toolbar layout orientation
    public final void setOrientation(Orientation orientation) {
        this.orientation.set(Objects.requireNonNull(orientation, "orientation"));
    }

    /// Returns the toolbar layout orientation property.
    ///
    /// @return the toolbar layout orientation property
    public final ObjectProperty<Orientation> orientationProperty() {
        return orientation;
    }

    /// Returns the horizontal toolbar container height token.
    ///
    /// @return the horizontal toolbar container height in pixels
    public final double getContainerHeight() {
        return containerHeight == null ? DEFAULT_CONTAINER_HEIGHT : containerHeight.get();
    }

    /// Sets the horizontal toolbar container height token.
    ///
    /// @param containerHeight the horizontal toolbar container height in pixels
    public final void setContainerHeight(double containerHeight) {
        containerHeightProperty().set(M3Css.nonNegative(containerHeight, "containerHeight"));
    }

    /// Returns the horizontal toolbar container height token property.
    ///
    /// @return the horizontal toolbar container height token property
    public final StyleableDoubleProperty containerHeightProperty() {
        if (containerHeight == null) {
            containerHeight = createStyleableDoubleProperty(
                    DEFAULT_CONTAINER_HEIGHT,
                    "containerHeight",
                    StyleableProperties.CONTAINER_HEIGHT
            );
        }
        return containerHeight;
    }

    /// Returns the vertical toolbar container width token.
    ///
    /// @return the vertical toolbar container width in pixels
    public final double getContainerWidth() {
        return containerWidth == null ? DEFAULT_CONTAINER_WIDTH : containerWidth.get();
    }

    /// Sets the vertical toolbar container width token.
    ///
    /// @param containerWidth the vertical toolbar container width in pixels
    public final void setContainerWidth(double containerWidth) {
        containerWidthProperty().set(M3Css.nonNegative(containerWidth, "containerWidth"));
    }

    /// Returns the vertical toolbar container width token property.
    ///
    /// @return the vertical toolbar container width token property
    public final StyleableDoubleProperty containerWidthProperty() {
        if (containerWidth == null) {
            containerWidth = createStyleableDoubleProperty(
                    DEFAULT_CONTAINER_WIDTH,
                    "containerWidth",
                    StyleableProperties.CONTAINER_WIDTH
            );
        }
        return containerWidth;
    }

    /// Returns the toolbar item slot size token.
    ///
    /// @return the toolbar item slot size in pixels
    public final double getItemSlotSize() {
        return itemSlotSize == null ? DEFAULT_ITEM_SLOT_SIZE : itemSlotSize.get();
    }

    /// Sets the toolbar item slot size token.
    ///
    /// @param itemSlotSize the toolbar item slot size in pixels
    public final void setItemSlotSize(double itemSlotSize) {
        itemSlotSizeProperty().set(M3Css.nonNegative(itemSlotSize, "itemSlotSize"));
    }

    /// Returns the toolbar item slot size token property.
    ///
    /// @return the toolbar item slot size token property
    public final StyleableDoubleProperty itemSlotSizeProperty() {
        if (itemSlotSize == null) {
            itemSlotSize = createStyleableDoubleProperty(
                    DEFAULT_ITEM_SLOT_SIZE,
                    "itemSlotSize",
                    StyleableProperties.ITEM_SLOT_SIZE
            );
        }
        return itemSlotSize;
    }

    /// Returns the toolbar content padding token.
    ///
    /// @return the toolbar content padding in pixels
    public final double getContentPadding() {
        return contentPadding == null ? DEFAULT_CONTENT_PADDING : contentPadding.get();
    }

    /// Sets the toolbar content padding token.
    ///
    /// @param contentPadding the toolbar content padding in pixels
    public final void setContentPadding(double contentPadding) {
        contentPaddingProperty().set(M3Css.nonNegative(contentPadding, "contentPadding"));
    }

    /// Returns the toolbar content padding token property.
    ///
    /// @return the toolbar content padding token property
    public final StyleableDoubleProperty contentPaddingProperty() {
        if (contentPadding == null) {
            contentPadding = createStyleableDoubleProperty(
                    DEFAULT_CONTENT_PADDING,
                    "contentPadding",
                    StyleableProperties.CONTENT_PADDING
            );
        }
        return contentPadding;
    }

    /// Returns the toolbar item spacing token.
    ///
    /// @return the toolbar item spacing in pixels
    public final double getItemSpacing() {
        return itemSpacing == null ? DEFAULT_ITEM_SPACING : itemSpacing.get();
    }

    /// Sets the toolbar item spacing token.
    ///
    /// @param itemSpacing the toolbar item spacing in pixels
    public final void setItemSpacing(double itemSpacing) {
        itemSpacingProperty().set(M3Css.nonNegative(itemSpacing, "itemSpacing"));
    }

    /// Returns the toolbar item spacing token property.
    ///
    /// @return the toolbar item spacing token property
    public final StyleableDoubleProperty itemSpacingProperty() {
        if (itemSpacing == null) {
            itemSpacing = createStyleableDoubleProperty(
                    DEFAULT_ITEM_SPACING,
                    "itemSpacing",
                    StyleableProperties.ITEM_SPACING
            );
        }
        return itemSpacing;
    }

    /// Returns the user-agent stylesheet for M3FX toolbars.
    ///
    /// @return the user-agent stylesheet for M3FX toolbars
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("toolbar.css");
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the immutable CSS metadata list for this class
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    ///
    /// @return the CSS metadata for this control
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Returns accessibility attributes for toolbar items.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case ITEM_COUNT -> getItems().size();
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getItems(), parameters);
            case FOCUS_NODE -> M3Accessible.currentOrFirstFocusTarget(this, getItems());
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions for toolbar item children.
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        if (isDisabled()) {
            super.executeAccessibleAction(action, parameters);
            return;
        }

        switch (action) {
            case REQUEST_FOCUS -> focusAccessibleItem();
            case SHOW_ITEM -> showAccessibleItem(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Requests focus on the current or first accessibility item.
    ///
    /// @return `true` when the target accepted focus
    final boolean focusAccessibleItem() {
        if (M3Accessible.showCurrentOrItem(this, getItems())) {
            notifyAccessibleFocusChanged();
            return true;
        }
        return false;
    }

    /// Shows an item requested by an accessibility client.
    ///
    /// @param parameters optional accessibility target parameters
    /// @return `true` when focus moved to the default or requested item
    final boolean showAccessibleItem(Object... parameters) {
        if (M3Accessible.showCurrentOrItem(this, getItems(), parameters)) {
            notifyAccessibleFocusChanged();
            return true;
        }
        return false;
    }

    /// Notifies accessibility clients that the container focus target changed.
    private void notifyAccessibleFocusChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Creates the default Material Design 3 toolbar skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3ToolbarSkin(this);
    }

    /// Initializes style classes, accessibility metadata, and item listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TOOL_BAR);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleItem, this::showAccessibleItem);
        addEventFilter(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        getItems().addListener(itemsListener);
        focusNotifier.start();
        updateVariantStyle();
        updateOrientationStyle();
        updateMetrics();
    }

    /// Handles keyboard traversal between focusable toolbar items.
    private void handleNavigationKeyPressed(KeyEvent event) {
        Objects.requireNonNull(event, "event");
        Orientation orientation = getOrientation();

        M3FocusTraversal.handleDirectionalKeyFocus(
                this,
                event,
                M3FocusTraversal.focusTargets(getItems()),
                orientation == Orientation.HORIZONTAL,
                orientation == Orientation.VERTICAL
        );
    }

    /// Handles item list changes.
    private void handleItemsChanged() {
        requestLayout();
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Applies the active toolbar variant style class.
    private void updateVariantStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getVariant().styleClass(),
                M3ToolbarVariant.STANDARD.styleClass(),
                M3ToolbarVariant.FLOATING.styleClass(),
                M3ToolbarVariant.DOCKED.styleClass()
        );
    }

    /// Applies the active toolbar orientation style class.
    private void updateOrientationStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getOrientation() == Orientation.HORIZONTAL ? "m3-toolbar-horizontal" : "m3-toolbar-vertical",
                "m3-toolbar-horizontal",
                "m3-toolbar-vertical"
        );
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double contentPadding = getContentPadding();
        M3Css.setPaddingIfUnbound(this, new Insets(contentPadding));
        if (getOrientation() == Orientation.HORIZONTAL) {
            M3Css.setMinHeightIfUnbound(this, getContainerHeight());
            M3Css.setPrefHeightIfUnbound(this, getContainerHeight());
            M3Css.setMinWidthIfUnbound(this, USE_COMPUTED_SIZE);
            M3Css.setPrefWidthIfUnbound(this, USE_COMPUTED_SIZE);
        } else {
            M3Css.setMinWidthIfUnbound(this, getContainerWidth());
            M3Css.setPrefWidthIfUnbound(this, getContainerWidth());
            M3Css.setMinHeightIfUnbound(this, USE_COMPUTED_SIZE);
            M3Css.setPrefHeightIfUnbound(this, USE_COMPUTED_SIZE);
        }
        requestLayout();
    }

    /// Creates a non-negative CSS-backed size token property.
    private StyleableDoubleProperty createStyleableDoubleProperty(
            double initialValue,
            String name,
            CssMetaData<M3Toolbar, Number> cssMetaData
    ) {
        return M3Css.nonNegativeStyleableDoubleProperty(
                initialValue,
                this,
                name,
                cssMetaData,
                this::updateMetrics
        );
    }

    /// CSS metadata for M3FX toolbar component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for horizontal toolbar container height.
        private static final CssMetaData<M3Toolbar, Number> CONTAINER_HEIGHT =
                createSizeCssMetaData("-m3-container-height", DEFAULT_CONTAINER_HEIGHT,
                        M3Toolbar::containerHeightProperty);

        /// CSS metadata for vertical toolbar container width.
        private static final CssMetaData<M3Toolbar, Number> CONTAINER_WIDTH =
                createSizeCssMetaData("-m3-container-width", DEFAULT_CONTAINER_WIDTH,
                        M3Toolbar::containerWidthProperty);

        /// CSS metadata for toolbar item slot size.
        private static final CssMetaData<M3Toolbar, Number> ITEM_SLOT_SIZE =
                createSizeCssMetaData("-m3-item-slot-size", DEFAULT_ITEM_SLOT_SIZE,
                        M3Toolbar::itemSlotSizeProperty);

        /// CSS metadata for toolbar content padding.
        private static final CssMetaData<M3Toolbar, Number> CONTENT_PADDING =
                createSizeCssMetaData("-m3-content-padding", DEFAULT_CONTENT_PADDING,
                        M3Toolbar::contentPaddingProperty);

        /// CSS metadata for toolbar item spacing.
        private static final CssMetaData<M3Toolbar, Number> ITEM_SPACING =
                createSizeCssMetaData("-m3-item-spacing", DEFAULT_ITEM_SPACING,
                        M3Toolbar::itemSpacingProperty);

        /// The complete immutable CSS metadata list.
        private static final @Unmodifiable List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(CONTAINER_HEIGHT);
            styleables.add(CONTAINER_WIDTH);
            styleables.add(ITEM_SLOT_SIZE);
            styleables.add(CONTENT_PADDING);
            styleables.add(ITEM_SPACING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }

        /// Prevents CSS metadata holder instantiation.
        private StyleableProperties() {
        }

        /// Creates CSS metadata for a non-negative size token.
        private static CssMetaData<M3Toolbar, Number> createSizeCssMetaData(
                String property,
                double initialValue,
                StyleablePropertyAccessor accessor
        ) {
            return new CssMetaData<>(property, SizeConverter.getInstance(), initialValue) {
                /// Returns whether this property can be set by CSS.
                @Override
                public boolean isSettable(M3Toolbar control) {
                    return M3Css.isSettable(accessor.property(control));
                }

                /// Returns the styleable property for a control.
                @Override
                public StyleableProperty<Number> getStyleableProperty(M3Toolbar control) {
                    return accessor.property(control);
                }
            };
        }

        /// Provides access to a styleable toolbar size token property.
        @NotNullByDefault
        @FunctionalInterface
        private interface StyleablePropertyAccessor {
            /// Returns the property for the supplied toolbar.
            StyleableDoubleProperty property(M3Toolbar control);
        }
    }
}
