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
import org.glavo.m3fx.skins.M3BottomAppBarSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 bar for screen-level actions at the bottom edge of a view.
///
/// The bar lays out an ordered, live list of regular [actions][#getActions()] and an optional
/// [floating action][#floatingActionProperty()]. The floating action has a dedicated slot and is not part of the
/// regular action list. Its alignment can be changed independently with
/// [floatingActionAlignment][#floatingActionAlignmentProperty()].
///
/// The bar itself is not focus traversable. Focus remains with focusable action children, and horizontal
/// traversal follows their visual order and the effective node orientation. The no-argument constructor creates
/// an empty bar with no floating action and end alignment.
///
/// See [Material Design bottom app bars](https://m3.material.io/components/bottom-app-bar/overview).
@NotNullByDefault
public final class M3BottomAppBar extends Control {
    /// The default bottom app bar container height in logical pixels.
    private static final double DEFAULT_CONTAINER_HEIGHT = 80.0;

    /// The default horizontal content padding in logical pixels.
    private static final double DEFAULT_HORIZONTAL_PADDING = 16.0;

    /// The default spacing between regular actions, flexible slots, and floating action content in logical pixels.
    private static final double DEFAULT_CONTENT_SPACING = 16.0;

    /// The default spacing between generated regular action slots in logical pixels.
    private static final double DEFAULT_ACTION_SPACING = 0.0;

    /// The base style class for M3FX bottom app bars.
    public static final String STYLE_CLASS = "m3-bottom-app-bar";

    /// The actions container style class.
    public static final String ACTIONS_STYLE_CLASS = "m3-bottom-app-bar-actions";

    /// The generated regular action slot style class.
    public static final String ACTION_SLOT_STYLE_CLASS = "m3-bottom-app-bar-action-slot";

    /// The floating action slot style class.
    public static final String FLOATING_ACTION_STYLE_CLASS = "m3-bottom-app-bar-floating-action";

    /// Creates an empty bottom app bar with no floating action and end alignment.
    public M3BottomAppBar() {
        initialize();
    }

    /// The node displayed in the dedicated floating-action slot.
    ///
    /// The default value is `null`. The node cannot simultaneously be a child of another parent.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Node> floatingAction = new SimpleObjectProperty<>(this, "floatingAction");

    /// Returns the optional floating action node.
    ///
    /// @return the floating action node, or `null` when no floating action is set
    public final @Nullable Node getFloatingAction() {
        return floatingAction.get();
    }

    /// Sets the optional floating action node.
    ///
    /// @param floatingAction the floating action node, or `null` to clear it
    public final void setFloatingAction(@Nullable Node floatingAction) {
        this.floatingAction.set(floatingAction);
    }

    /// Returns the observable property that stores the optional floating action node.
    ///
    /// The property can be observed and bound. Its default value is `null`.
    ///
    /// @return the floating action property
    public final ObjectProperty<@Nullable Node> floatingActionProperty() {
        return floatingAction;
    }

    /// The alignment of the floating-action slot within the bar.
    ///
    /// The default value is [M3BottomAppBarFloatingActionAlignment#END]. A direct `null` assignment restores the
    /// default; bound values must be non-null.
    ///
    /// @defaultValue [M3BottomAppBarFloatingActionAlignment#END]
    private final ObjectProperty<M3BottomAppBarFloatingActionAlignment> floatingActionAlignment =
            new SimpleObjectProperty<>(this, "floatingActionAlignment", M3BottomAppBarFloatingActionAlignment.END) {
                /// Updates alignment style classes when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3BottomAppBarFloatingActionAlignment.END);
                        return;
                    }
                    updateFloatingActionAlignmentStyle();
                    requestLayout();
                }
            };

    /// Returns the floating action node alignment.
    ///
    /// @return the floating action alignment
    public final M3BottomAppBarFloatingActionAlignment getFloatingActionAlignment() {
        return floatingActionAlignment.get();
    }

    /// Sets the floating action node alignment.
    ///
    /// @param floatingActionAlignment the floating action alignment
    /// @throws NullPointerException if `floatingActionAlignment` is `null`
    public final void setFloatingActionAlignment(M3BottomAppBarFloatingActionAlignment floatingActionAlignment) {
        this.floatingActionAlignment.set(Objects.requireNonNull(floatingActionAlignment, "floatingActionAlignment"));
    }

    /// Returns the observable property that stores the floating action alignment.
    ///
    /// The property can be observed and bound. Its default value is
    /// [M3BottomAppBarFloatingActionAlignment#END], and a direct `null` assignment restores that default.
    ///
    /// @return the floating action alignment property
    public final ObjectProperty<M3BottomAppBarFloatingActionAlignment> floatingActionAlignmentProperty() {
        return floatingActionAlignment;
    }

    /// The preferred bar height, in logical pixels.
    ///
    /// The default value is `80.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `80.0`
    private @Nullable StyleableDoubleProperty containerHeight;

    /// Returns the bottom app bar container height token.
    ///
    /// @return the bottom app bar height in logical pixels
    public final double getContainerHeight() {
        return containerHeight == null ? DEFAULT_CONTAINER_HEIGHT : containerHeight.get();
    }

    /// Sets the bottom app bar container height token.
    ///
    /// @param containerHeight the bottom app bar height in logical pixels
    /// @throws IllegalArgumentException if `containerHeight` is negative or not finite
    public final void setContainerHeight(double containerHeight) {
        containerHeightProperty().set(M3Css.nonNegative(containerHeight, "containerHeight"));
    }

    /// Returns the styleable property that stores the bottom app bar height.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-container-height`, and accepts finite,
    /// non-negative values. Its default value is `80.0` logical pixels.
    ///
    /// @return the bottom app bar height property
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

    /// The padding at the logical start and end of the bar, in logical pixels.
    ///
    /// The default value is `16.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `16.0`
    private @Nullable StyleableDoubleProperty horizontalPadding;

    /// Returns the horizontal content padding token.
    ///
    /// @return the horizontal content padding in logical pixels
    public final double getHorizontalPadding() {
        return horizontalPadding == null ? DEFAULT_HORIZONTAL_PADDING : horizontalPadding.get();
    }

    /// Sets the horizontal content padding token.
    ///
    /// @param horizontalPadding the horizontal content padding in logical pixels
    /// @throws IllegalArgumentException if `horizontalPadding` is negative or not finite
    public final void setHorizontalPadding(double horizontalPadding) {
        horizontalPaddingProperty().set(M3Css.nonNegative(horizontalPadding, "horizontalPadding"));
    }

    /// Returns the styleable property that stores the horizontal content padding.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-horizontal-padding`, and accepts finite,
    /// non-negative values. Its default value is `16.0` logical pixels.
    ///
    /// @return the horizontal content padding property
    public final StyleableDoubleProperty horizontalPaddingProperty() {
        if (horizontalPadding == null) {
            horizontalPadding = createStyleableDoubleProperty(
                    DEFAULT_HORIZONTAL_PADDING,
                    "horizontalPadding",
                    StyleableProperties.HORIZONTAL_PADDING
            );
        }
        return horizontalPadding;
    }

    /// The spacing between regular actions, flexible space, and the floating-action slot, in logical pixels.
    ///
    /// The default value is `16.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `16.0`
    private @Nullable StyleableDoubleProperty contentSpacing;

    /// Returns the spacing token between regular actions, flexible slots, and floating action content.
    ///
    /// @return the content slot spacing in logical pixels
    public final double getContentSpacing() {
        return contentSpacing == null ? DEFAULT_CONTENT_SPACING : contentSpacing.get();
    }

    /// Sets the spacing token between regular actions, flexible slots, and floating action content.
    ///
    /// @param contentSpacing the content slot spacing in logical pixels
    /// @throws IllegalArgumentException if `contentSpacing` is negative or not finite
    public final void setContentSpacing(double contentSpacing) {
        contentSpacingProperty().set(M3Css.nonNegative(contentSpacing, "contentSpacing"));
    }

    /// Returns the styleable property that stores the spacing between content slots.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-content-spacing`, and accepts finite,
    /// non-negative values. Its default value is `16.0` logical pixels.
    ///
    /// @return the content spacing property
    public final StyleableDoubleProperty contentSpacingProperty() {
        if (contentSpacing == null) {
            contentSpacing = createStyleableDoubleProperty(
                    DEFAULT_CONTENT_SPACING,
                    "contentSpacing",
                    StyleableProperties.CONTENT_SPACING
            );
        }
        return contentSpacing;
    }

    /// The spacing between adjacent regular action slots, in logical pixels.
    ///
    /// The default value is `0.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `0.0`
    private @Nullable StyleableDoubleProperty actionSpacing;

    /// Returns the spacing token between generated regular action slots.
    ///
    /// @return the regular action slot spacing in logical pixels
    public final double getActionSpacing() {
        return actionSpacing == null ? DEFAULT_ACTION_SPACING : actionSpacing.get();
    }

    /// Sets the spacing token between generated regular action slots.
    ///
    /// @param actionSpacing the regular action slot spacing in logical pixels
    /// @throws IllegalArgumentException if `actionSpacing` is negative or not finite
    public final void setActionSpacing(double actionSpacing) {
        actionSpacingProperty().set(M3Css.nonNegative(actionSpacing, "actionSpacing"));
    }

    /// Returns the styleable property that stores the spacing between regular action slots.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-action-spacing`, and accepts finite,
    /// non-negative values. Its default value is `0.0` logical pixels.
    ///
    /// @return the regular action spacing property
    public final StyleableDoubleProperty actionSpacingProperty() {
        if (actionSpacing == null) {
            actionSpacing = createStyleableDoubleProperty(
                    DEFAULT_ACTION_SPACING,
                    "actionSpacing",
                    StyleableProperties.ACTION_SPACING
            );
        }
        return actionSpacing;
    }

    /// The live, mutable list of regular action nodes.
    ///
    /// The list preserves insertion order, rejects `null`, and is observed for subsequent changes. Nodes in the
    /// list cannot simultaneously be children of another parent.
    private final ObservableList<Node> actions = M3ObservableLists.nonNullElementList("action");

    /// Notifies accessibility clients when focus moves between action children.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, () -> M3Accessible.currentOrFirstFocusTarget(
                    this,
                    getActions(),
                    getFloatingAction()
            ));

    /// Returns the live list of regular action nodes.
    ///
    /// Changes to the returned list are reflected immediately by this bar. The list preserves insertion order and
    /// rejects `null` elements.
    ///
    /// @return the live, mutable regular action list
    public final ObservableList<Node> getActions() {
        return actions;
    }

    /// Returns the user-agent stylesheet for M3FX bottom app bars.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("bottom-app-bar.css");
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

    /// Initializes style classes, accessibility metadata, and property listeners.
    private void initialize() {
        M3ControlStyles.initialize(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TOOL_BAR);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleItem, this::showAccessibleItem);
        addEventFilter(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        floatingAction.addListener((observable, oldValue, newValue) -> notifyAccessibleItemsChanged());
        actions.addListener((ListChangeListener<Node>) change -> notifyAccessibleItemsChanged());
        focusNotifier.start();
        updateFloatingActionAlignmentStyle();
        updateMetrics();
    }

    /// Returns accessibility attributes for the action and floating action collection.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        return switch (attribute) {
            case ITEM_COUNT -> M3Accessible.itemCount(getActions(), getFloatingAction());
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getActions(), getFloatingAction(), parameters);
            case FOCUS_NODE -> M3Accessible.currentOrFirstFocusTarget(this, getActions(), getFloatingAction());
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions for indexed action and floating action children.
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
        if (M3Accessible.showCurrentOrItem(this, getActions(), getFloatingAction())) {
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
        if (M3Accessible.showCurrentOrItem(this, getActions(), getFloatingAction(), parameters)) {
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

    /// Creates the default Material Design 3 bottom app bar skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3BottomAppBarSkin(this);
    }

    /// Handles keyboard traversal between focusable regular and floating action items.
    private void handleNavigationKeyPressed(KeyEvent event) {
        M3FocusTraversal.handleHorizontalKeyFocus(
                this,
                event,
                M3FocusTraversal.focusTargets(getActions(), getFloatingAction())
        );
    }

    /// Notifies accessibility clients that the indexed bottom app bar item collection changed.
    private void notifyAccessibleItemsChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Updates the active floating action alignment style class.
    private void updateFloatingActionAlignmentStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getFloatingActionAlignment().styleClass(),
                M3BottomAppBarFloatingActionAlignment.START.styleClass(),
                M3BottomAppBarFloatingActionAlignment.CENTER.styleClass(),
                M3BottomAppBarFloatingActionAlignment.END.styleClass()
        );
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double height = getContainerHeight();
        double horizontalPadding = getHorizontalPadding();
        M3Css.setMinHeightIfUnbound(this, height);
        M3Css.setPrefHeightIfUnbound(this, height);
        M3Css.setPaddingIfUnbound(this, new Insets(0.0, horizontalPadding, 0.0, horizontalPadding));
        requestLayout();
    }

    /// Creates a non-negative CSS-backed size token property.
    private StyleableDoubleProperty createStyleableDoubleProperty(
            double initialValue,
            String name,
            CssMetaData<M3BottomAppBar, Number> cssMetaData
    ) {
        return M3Css.nonNegativeStyleableDoubleProperty(
                initialValue,
                this,
                name,
                cssMetaData,
                this::updateMetrics
        );
    }

    /// CSS metadata for M3FX bottom app bar component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the container height token.
        private static final CssMetaData<M3BottomAppBar, Number> CONTAINER_HEIGHT =
                createSizeCssMetaData("-m3-container-height", DEFAULT_CONTAINER_HEIGHT,
                        M3BottomAppBar::containerHeightProperty);

        /// CSS metadata for the horizontal padding token.
        private static final CssMetaData<M3BottomAppBar, Number> HORIZONTAL_PADDING =
                createSizeCssMetaData("-m3-horizontal-padding", DEFAULT_HORIZONTAL_PADDING,
                        M3BottomAppBar::horizontalPaddingProperty);

        /// CSS metadata for the content slot spacing token.
        private static final CssMetaData<M3BottomAppBar, Number> CONTENT_SPACING =
                createSizeCssMetaData("-m3-content-spacing", DEFAULT_CONTENT_SPACING,
                        M3BottomAppBar::contentSpacingProperty);

        /// CSS metadata for the regular action slot spacing token.
        private static final CssMetaData<M3BottomAppBar, Number> ACTION_SPACING =
                createSizeCssMetaData("-m3-action-spacing", DEFAULT_ACTION_SPACING,
                        M3BottomAppBar::actionSpacingProperty);

        /// The complete immutable CSS metadata list.
        private static final @Unmodifiable List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(CONTAINER_HEIGHT);
            styleables.add(HORIZONTAL_PADDING);
            styleables.add(CONTENT_SPACING);
            styleables.add(ACTION_SPACING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }

        /// Prevents CSS metadata holder instantiation.
        private StyleableProperties() {
        }

        /// Creates CSS metadata for a non-negative size token.
        private static CssMetaData<M3BottomAppBar, Number> createSizeCssMetaData(
                String property,
                double initialValue,
                StyleablePropertyAccessor accessor
        ) {
            return new CssMetaData<>(property, SizeConverter.getInstance(), initialValue) {
                /// Returns whether this property can be set by CSS.
                @Override
                public boolean isSettable(M3BottomAppBar control) {
                    return M3Css.isSettable(accessor.property(control));
                }

                /// Returns the styleable property for a control.
                @Override
                public StyleableProperty<Number> getStyleableProperty(M3BottomAppBar control) {
                    return accessor.property(control);
                }
            };
        }

        /// Provides access to a styleable bottom app bar size token property.
        @NotNullByDefault
        @FunctionalInterface
        private interface StyleablePropertyAccessor {
            /// Returns the property for the supplied bottom app bar.
            StyleableDoubleProperty property(M3BottomAppBar control);
        }
    }
}
