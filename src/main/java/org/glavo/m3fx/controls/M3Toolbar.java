// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
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
/// where the actions should be navigable as one toolbar. Keyboard traversal follows item order and adapts to the
/// configured orientation.
///
/// A new toolbar is horizontal, floating, and uses the standard color mapping. Items are owned by the toolbar and
/// may be changed through the live [#getItems()] list.
///
/// See [Material Design toolbars](https://m3.material.io/components/toolbars/overview).
@NotNullByDefault
public final class M3Toolbar extends Control {
    /// The base style class for M3FX toolbars.
    public static final String STYLE_CLASS = "m3-toolbar";

    /// The toolbar item slot style class.
    public static final String ITEM_SLOT_STYLE_CLASS = "m3-toolbar-item-slot";

    /// The pseudo-class used for the Vibrant toolbar color mapping.
    private static final PseudoClass VIBRANT_PSEUDO_CLASS = PseudoClass.getPseudoClass("vibrant");

    /// The pseudo-class applied to controls hosted as toolbar actions.
    private static final PseudoClass TOOLBAR_ACTION_PSEUDO_CLASS = PseudoClass.getPseudoClass("toolbar-action");

    /// The default toolbar orientation.
    private static final Orientation DEFAULT_ORIENTATION = Orientation.HORIZONTAL;

    /// The default toolbar variant.
    private static final M3ToolbarVariant DEFAULT_VARIANT = M3ToolbarVariant.FLOATING;

    /// The default toolbar color style.
    private static final M3ToolbarColorStyle DEFAULT_COLOR_STYLE = M3ToolbarColorStyle.STANDARD;

    /// The default horizontal toolbar container height in pixels.
    private static final double DEFAULT_CONTAINER_HEIGHT = 64.0;

    /// The default vertical toolbar container width in pixels.
    private static final double DEFAULT_CONTAINER_WIDTH = 64.0;

    /// The default item slot size in pixels.
    private static final double DEFAULT_ITEM_SLOT_SIZE = 48.0;

    /// The default toolbar content padding in pixels.
    private static final double DEFAULT_CONTENT_PADDING = 8.0;

    /// The default leading and trailing padding of a docked toolbar in pixels.
    private static final double DEFAULT_DOCKED_CONTENT_PADDING = 16.0;

    /// The default toolbar item spacing in pixels.
    private static final double DEFAULT_ITEM_SPACING = 4.0;

    /// The default preferred spacing between docked toolbar items in pixels.
    private static final double DEFAULT_DOCKED_MAX_ITEM_SPACING = 32.0;

    /// The mutable toolbar item list.
    private final ObservableList<Node> items = M3ObservableLists.nonNullElementList("item");

    /// The toolbar visual variant.
    ///
    /// Assigning `null` through the property restores [M3ToolbarVariant#FLOATING].
    ///
    /// @defaultValue `FLOATING`
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
                    updateMetrics();
                }
            };

    /// The toolbar color mapping.
    ///
    /// Assigning `null` through the property restores [M3ToolbarColorStyle#STANDARD].
    ///
    /// @defaultValue `STANDARD`
    private final ObjectProperty<M3ToolbarColorStyle> colorStyle =
            new SimpleObjectProperty<>(this, "colorStyle", DEFAULT_COLOR_STYLE) {
                /// Updates the color-style pseudo-class when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(DEFAULT_COLOR_STYLE);
                        return;
                    }
                    updateColorStylePseudoClass();
                }
            };

    /// The toolbar layout orientation.
    ///
    /// Assigning `null` through the property restores [Orientation#HORIZONTAL].
    ///
    /// @defaultValue `HORIZONTAL`
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

    /// The horizontal toolbar container height in logical pixels.
    ///
    /// @defaultValue `64.0`
    private @Nullable StyleableDoubleProperty containerHeight;

    /// The vertical toolbar container width in logical pixels.
    ///
    /// @defaultValue `64.0`
    private @Nullable StyleableDoubleProperty containerWidth;

    /// The square toolbar item-slot size in logical pixels.
    ///
    /// @defaultValue `48.0`
    private @Nullable StyleableDoubleProperty itemSlotSize;

    /// The floating toolbar content padding in logical pixels.
    ///
    /// @defaultValue `8.0`
    private @Nullable StyleableDoubleProperty contentPadding;

    /// The docked toolbar leading and trailing padding in logical pixels.
    ///
    /// @defaultValue `16.0`
    private @Nullable StyleableDoubleProperty dockedContentPadding;

    /// The floating toolbar item spacing in logical pixels.
    ///
    /// @defaultValue `4.0`
    private @Nullable StyleableDoubleProperty itemSpacing;

    /// The preferred maximum docked-toolbar item spacing in logical pixels.
    ///
    /// @defaultValue `32.0`
    private @Nullable StyleableDoubleProperty dockedMaxItemSpacing;

    /// Notifies accessibility clients when focus moves between toolbar actions.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, () -> M3Accessible.currentOrFirstFocusTarget(this, getItems()));

    /// Updates accessibility and layout after toolbar item changes.
    private final ListChangeListener<Node> itemsListener = this::handleItemsChanged;

    /// Creates an empty horizontal floating toolbar with the standard color mapping.
    public M3Toolbar() {
        initialize();
    }

    /// Returns the mutable toolbar item list.
    ///
    /// The returned list is live, mutable, ordered, and rejects `null` elements. Mutations update layout, keyboard
    /// traversal, and accessibility immediately. Nodes become children of the toolbar and must satisfy normal
    /// JavaFX parent ownership rules; duplicate node references are not permitted by that ownership model.
    ///
    /// @return the live mutable toolbar item list
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
    /// @throws NullPointerException if `variant` is `null`
    public final void setVariant(M3ToolbarVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    public final ObjectProperty<M3ToolbarVariant> variantProperty() {
        return variant;
    }

    /// Returns the toolbar color style.
    ///
    /// @return the toolbar color style
    public final M3ToolbarColorStyle getColorStyle() {
        return colorStyle.get();
    }

    /// Sets the toolbar color style.
    ///
    /// @param colorStyle the toolbar color style
    /// @throws NullPointerException if `colorStyle` is `null`
    public final void setColorStyle(M3ToolbarColorStyle colorStyle) {
        this.colorStyle.set(Objects.requireNonNull(colorStyle, "colorStyle"));
    }

    public final ObjectProperty<M3ToolbarColorStyle> colorStyleProperty() {
        return colorStyle;
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
    /// @throws NullPointerException if `orientation` is `null`
    public final void setOrientation(Orientation orientation) {
        this.orientation.set(Objects.requireNonNull(orientation, "orientation"));
    }

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
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setContainerHeight(double containerHeight) {
        containerHeightProperty().set(M3Css.nonNegative(containerHeight, "containerHeight"));
    }

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
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setContainerWidth(double containerWidth) {
        containerWidthProperty().set(M3Css.nonNegative(containerWidth, "containerWidth"));
    }

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
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setItemSlotSize(double itemSlotSize) {
        itemSlotSizeProperty().set(M3Css.nonNegative(itemSlotSize, "itemSlotSize"));
    }

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
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setContentPadding(double contentPadding) {
        contentPaddingProperty().set(M3Css.nonNegative(contentPadding, "contentPadding"));
    }

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

    /// Returns the leading and trailing padding token used by docked toolbars.
    ///
    /// @return the docked toolbar leading and trailing padding in pixels
    public final double getDockedContentPadding() {
        return dockedContentPadding == null ? DEFAULT_DOCKED_CONTENT_PADDING : dockedContentPadding.get();
    }

    /// Sets the leading and trailing padding token used by docked toolbars.
    ///
    /// @param dockedContentPadding the docked toolbar leading and trailing padding in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setDockedContentPadding(double dockedContentPadding) {
        dockedContentPaddingProperty().set(M3Css.nonNegative(dockedContentPadding, "dockedContentPadding"));
    }

    public final StyleableDoubleProperty dockedContentPaddingProperty() {
        if (dockedContentPadding == null) {
            dockedContentPadding = createStyleableDoubleProperty(
                    DEFAULT_DOCKED_CONTENT_PADDING,
                    "dockedContentPadding",
                    StyleableProperties.DOCKED_CONTENT_PADDING
            );
        }
        return dockedContentPadding;
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
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setItemSpacing(double itemSpacing) {
        itemSpacingProperty().set(M3Css.nonNegative(itemSpacing, "itemSpacing"));
    }

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

    /// Returns the preferred maximum spacing token used between docked toolbar items.
    ///
    /// The skin reduces this value toward [#getItemSpacing()] when the docked toolbar does not have enough room.
    ///
    /// @return the preferred docked toolbar item spacing in pixels
    public final double getDockedMaxItemSpacing() {
        return dockedMaxItemSpacing == null ? DEFAULT_DOCKED_MAX_ITEM_SPACING : dockedMaxItemSpacing.get();
    }

    /// Sets the preferred maximum spacing token used between docked toolbar items.
    ///
    /// @param dockedMaxItemSpacing the preferred docked toolbar item spacing in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setDockedMaxItemSpacing(double dockedMaxItemSpacing) {
        dockedMaxItemSpacingProperty().set(M3Css.nonNegative(dockedMaxItemSpacing, "dockedMaxItemSpacing"));
    }

    public final StyleableDoubleProperty dockedMaxItemSpacingProperty() {
        if (dockedMaxItemSpacing == null) {
            dockedMaxItemSpacing = createStyleableDoubleProperty(
                    DEFAULT_DOCKED_MAX_ITEM_SPACING,
                    "dockedMaxItemSpacing",
                    StyleableProperties.DOCKED_MAX_ITEM_SPACING
            );
        }
        return dockedMaxItemSpacing;
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
    ///
    /// @throws NullPointerException if `attribute` is `null`
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
    ///
    /// @throws NullPointerException if `action` is `null`
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
        M3ControlStyles.initialize(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TOOL_BAR);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleItem, this::showAccessibleItem);
        addEventFilter(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        getItems().addListener(itemsListener);
        focusNotifier.start();
        updateVariantStyle();
        updateColorStylePseudoClass();
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
    private void handleItemsChanged(ListChangeListener.Change<? extends Node> change) {
        while (change.next()) {
            for (Node removed : change.getRemoved()) {
                removed.pseudoClassStateChanged(TOOLBAR_ACTION_PSEUDO_CLASS, false);
                removed.pseudoClassStateChanged(VIBRANT_PSEUDO_CLASS, false);
            }
            for (Node added : change.getAddedSubList()) {
                updateActionColorStylePseudoClasses(added);
            }
        }
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
                M3ToolbarVariant.FLOATING.styleClass(),
                M3ToolbarVariant.DOCKED.styleClass()
        );
    }

    /// Applies the active toolbar color-style pseudo-class.
    private void updateColorStylePseudoClass() {
        boolean vibrant = getColorStyle() == M3ToolbarColorStyle.VIBRANT;
        pseudoClassStateChanged(VIBRANT_PSEUDO_CLASS, vibrant);
        for (Node item : getItems()) {
            updateActionColorStylePseudoClasses(item);
        }
    }

    /// Applies toolbar ownership and color-style pseudo-classes to an action node.
    private void updateActionColorStylePseudoClasses(Node item) {
        item.pseudoClassStateChanged(TOOLBAR_ACTION_PSEUDO_CLASS, true);
        item.pseudoClassStateChanged(VIBRANT_PSEUDO_CLASS, getColorStyle() == M3ToolbarColorStyle.VIBRANT);
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
        double mainAxisPadding = getVariant() == M3ToolbarVariant.DOCKED
                ? getDockedContentPadding()
                : contentPadding;
        M3Css.setPaddingIfUnbound(this, getOrientation() == Orientation.HORIZONTAL
                ? new Insets(contentPadding, mainAxisPadding, contentPadding, mainAxisPadding)
                : new Insets(mainAxisPadding, contentPadding, mainAxisPadding, contentPadding));
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

        /// CSS metadata for docked toolbar leading and trailing padding.
        private static final CssMetaData<M3Toolbar, Number> DOCKED_CONTENT_PADDING =
                createSizeCssMetaData("-m3-docked-content-padding", DEFAULT_DOCKED_CONTENT_PADDING,
                        M3Toolbar::dockedContentPaddingProperty);

        /// CSS metadata for toolbar item spacing.
        private static final CssMetaData<M3Toolbar, Number> ITEM_SPACING =
                createSizeCssMetaData("-m3-item-spacing", DEFAULT_ITEM_SPACING,
                        M3Toolbar::itemSpacingProperty);

        /// CSS metadata for preferred docked toolbar item spacing.
        private static final CssMetaData<M3Toolbar, Number> DOCKED_MAX_ITEM_SPACING =
                createSizeCssMetaData("-m3-docked-max-item-spacing", DEFAULT_DOCKED_MAX_ITEM_SPACING,
                        M3Toolbar::dockedMaxItemSpacingProperty);

        /// The complete immutable CSS metadata list.
        private static final @Unmodifiable List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(CONTAINER_HEIGHT);
            styleables.add(CONTAINER_WIDTH);
            styleables.add(ITEM_SLOT_SIZE);
            styleables.add(CONTENT_PADDING);
            styleables.add(DOCKED_CONTENT_PADDING);
            styleables.add(ITEM_SPACING);
            styleables.add(DOCKED_MAX_ITEM_SPACING);
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
