// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
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
import org.glavo.m3fx.skins.M3BannerSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A persistent, in-layout message with optional actions.
///
/// `M3Banner` is an M3FX extension styled with Material tokens; Material Design 3 does not define a banner
/// component. It combines a list-like message layout with optional action controls.
///
/// A banner displays [text][#textProperty()], an optional leading [icon][#iconProperty()], and an ordered list of
/// trailing [actions][#getActions()]. Unlike a popup notification, a banner remains part of its parent's layout;
/// the application controls when it is added, removed, shown, or hidden.
///
/// The banner itself is not focus traversable. Keyboard focus and action handling remain with focusable nodes in
/// the icon and action slots. The no-argument constructor creates an empty banner with no icon or actions.
///
/// See [Material Design lists](https://m3.material.io/components/lists/overview) and
/// [Material Design buttons](https://m3.material.io/components/buttons/overview).
@NotNullByDefault
public final class M3Banner extends Control {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-banner";

    /// The default minimum banner container height.
    private static final double DEFAULT_CONTAINER_MIN_HEIGHT = 80.0;

    /// The default vertical banner content padding.
    private static final double DEFAULT_VERTICAL_PADDING = 16.0;

    /// The default horizontal banner content padding.
    private static final double DEFAULT_HORIZONTAL_PADDING = 24.0;

    /// The default spacing between icon, text, and actions.
    private static final double DEFAULT_CONTENT_SPACING = 16.0;

    /// The default spacing between action nodes.
    private static final double DEFAULT_ACTION_SPACING = 8.0;

    /// Creates a banner with empty text, no icon, and no actions.
    public M3Banner() {
        this("");
    }

    /// Creates a banner with the specified message text and no icon or actions.
    ///
    /// @param text the banner message text
    /// @throws NullPointerException if `text` is `null`
    public M3Banner(String text) {
        initialize();
        setText(text);
    }

    /// The message displayed by this banner.
    ///
    /// The default value is the empty string. [setText][#setText(String)] rejects `null`.
    ///
    /// @defaultValue `""`
    private final StringProperty text = new SimpleStringProperty(this, "text", "");

    /// Returns the banner message text.
    ///
    /// @return the banner message text
    public final String getText() {
        return text.get();
    }

    /// Sets the banner message text.
    ///
    /// @param text the banner message text
    /// @throws NullPointerException if `text` is `null`
    public final void setText(String text) {
        this.text.set(Objects.requireNonNull(text, "text"));
    }

    /// Returns the observable property that stores the banner message.
    ///
    /// The property can be observed and bound. Its default value is the empty string.
    ///
    /// @return the banner message property
    public final StringProperty textProperty() {
        return text;
    }

    /// The node displayed before the message according to the effective node orientation.
    ///
    /// The default value is `null`. The node cannot simultaneously be a child of another parent.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Node> icon = new SimpleObjectProperty<>(this, "icon");

    /// Returns the optional leading icon node.
    ///
    /// @return the leading icon node, or `null` when no icon is set
    public final @Nullable Node getIcon() {
        return icon.get();
    }

    /// Sets the optional leading icon node.
    ///
    /// @param icon the leading icon node, or `null` to clear the icon
    public final void setIcon(@Nullable Node icon) {
        this.icon.set(icon);
    }

    /// Returns the observable property that stores the optional leading icon.
    ///
    /// The property can be observed and bound. Its default value is `null`.
    ///
    /// @return the leading icon property
    public final ObjectProperty<@Nullable Node> iconProperty() {
        return icon;
    }

    /// The minimum banner height, in logical pixels.
    ///
    /// The default value is `80.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `80.0`
    private @Nullable StyleableDoubleProperty containerMinHeight;

    /// Returns the minimum banner container height token.
    ///
    /// @return the minimum banner height in logical pixels
    public final double getContainerMinHeight() {
        return containerMinHeight == null ? DEFAULT_CONTAINER_MIN_HEIGHT : containerMinHeight.get();
    }

    /// Sets the minimum banner container height token.
    ///
    /// @param containerMinHeight the minimum banner height in logical pixels
    /// @throws IllegalArgumentException if `containerMinHeight` is negative or not finite
    public final void setContainerMinHeight(double containerMinHeight) {
        containerMinHeightProperty().set(M3Css.nonNegative(containerMinHeight, "containerMinHeight"));
    }

    /// Returns the styleable property that stores the minimum banner height.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-container-min-height`, and accepts finite,
    /// non-negative values. Its default value is `80.0` logical pixels.
    ///
    /// @return the minimum banner height property
    public final StyleableDoubleProperty containerMinHeightProperty() {
        if (containerMinHeight == null) {
            containerMinHeight = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTAINER_MIN_HEIGHT,
                    this,
                    "containerMinHeight",
                    StyleableProperties.CONTAINER_MIN_HEIGHT,
                    this::updateMetrics
            );
        }
        return containerMinHeight;
    }

    /// The padding above and below banner content, in logical pixels.
    ///
    /// The default value is `16.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `16.0`
    private @Nullable StyleableDoubleProperty verticalPadding;

    /// Returns the vertical banner content padding token.
    ///
    /// @return the vertical content padding in logical pixels
    public final double getVerticalPadding() {
        return verticalPadding == null ? DEFAULT_VERTICAL_PADDING : verticalPadding.get();
    }

    /// Sets the vertical banner content padding token.
    ///
    /// @param verticalPadding the vertical content padding in logical pixels
    /// @throws IllegalArgumentException if `verticalPadding` is negative or not finite
    public final void setVerticalPadding(double verticalPadding) {
        verticalPaddingProperty().set(M3Css.nonNegative(verticalPadding, "verticalPadding"));
    }

    /// Returns the styleable property that stores the vertical content padding.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-vertical-padding`, and accepts finite,
    /// non-negative values. Its default value is `16.0` logical pixels.
    ///
    /// @return the vertical content padding property
    public final StyleableDoubleProperty verticalPaddingProperty() {
        if (verticalPadding == null) {
            verticalPadding = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_VERTICAL_PADDING,
                    this,
                    "verticalPadding",
                    StyleableProperties.VERTICAL_PADDING,
                    this::updateMetrics
            );
        }
        return verticalPadding;
    }

    /// The padding at the logical start and end of banner content, in logical pixels.
    ///
    /// The default value is `24.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `24.0`
    private @Nullable StyleableDoubleProperty horizontalPadding;

    /// Returns the horizontal banner content padding token.
    ///
    /// @return the horizontal content padding in logical pixels
    public final double getHorizontalPadding() {
        return horizontalPadding == null ? DEFAULT_HORIZONTAL_PADDING : horizontalPadding.get();
    }

    /// Sets the horizontal banner content padding token.
    ///
    /// @param horizontalPadding the horizontal content padding in logical pixels
    /// @throws IllegalArgumentException if `horizontalPadding` is negative or not finite
    public final void setHorizontalPadding(double horizontalPadding) {
        horizontalPaddingProperty().set(M3Css.nonNegative(horizontalPadding, "horizontalPadding"));
    }

    /// Returns the styleable property that stores the horizontal content padding.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-horizontal-padding`, and accepts finite,
    /// non-negative values. Its default value is `24.0` logical pixels.
    ///
    /// @return the horizontal content padding property
    public final StyleableDoubleProperty horizontalPaddingProperty() {
        if (horizontalPadding == null) {
            horizontalPadding = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_HORIZONTAL_PADDING,
                    this,
                    "horizontalPadding",
                    StyleableProperties.HORIZONTAL_PADDING,
                    this::updateMetrics
            );
        }
        return horizontalPadding;
    }

    /// The spacing between the icon, message, and action area, in logical pixels.
    ///
    /// The default value is `16.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `16.0`
    private @Nullable StyleableDoubleProperty contentSpacing;

    /// Returns the spacing token between icon, text, and actions.
    ///
    /// @return the spacing between icon, text, and actions in logical pixels
    public final double getContentSpacing() {
        return contentSpacing == null ? DEFAULT_CONTENT_SPACING : contentSpacing.get();
    }

    /// Sets the spacing token between icon, text, and actions.
    ///
    /// @param contentSpacing the spacing between icon, text, and actions in logical pixels
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

    /// The spacing between adjacent action nodes, in logical pixels.
    ///
    /// The default value is `8.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `8.0`
    private @Nullable StyleableDoubleProperty actionSpacing;

    /// Returns the spacing token between action nodes.
    ///
    /// @return the spacing between action nodes in logical pixels
    public final double getActionSpacing() {
        return actionSpacing == null ? DEFAULT_ACTION_SPACING : actionSpacing.get();
    }

    /// Sets the spacing token between action nodes.
    ///
    /// @param actionSpacing the spacing between action nodes in logical pixels
    /// @throws IllegalArgumentException if `actionSpacing` is negative or not finite
    public final void setActionSpacing(double actionSpacing) {
        actionSpacingProperty().set(M3Css.nonNegative(actionSpacing, "actionSpacing"));
    }

    /// Returns the styleable property that stores the spacing between action nodes.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-action-spacing`, and accepts finite,
    /// non-negative values. Its default value is `8.0` logical pixels.
    ///
    /// @return the action spacing property
    public final StyleableDoubleProperty actionSpacingProperty() {
        if (actionSpacing == null) {
            actionSpacing = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_ACTION_SPACING,
                    this,
                    "actionSpacing",
                    StyleableProperties.ACTION_SPACING,
                    this::requestLayout
            );
        }
        return actionSpacing;
    }

    /// The live, mutable list of nodes displayed after the message.
    ///
    /// The list preserves insertion order, permits neither `null` elements nor duplicate parent ownership, and
    /// is observed for subsequent changes. Nodes in this list cannot simultaneously be children of another parent.
    private final ObservableList<Node> actions = M3ObservableLists.identityDistinctElementList("action");

    /// Notifies accessibility clients when focus moves between action children.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, this::accessibleFocusNode);

    /// Returns the live list of trailing action nodes.
    ///
    /// Changes to the returned list are reflected immediately by this banner. The list preserves insertion order
    /// and rejects `null` elements or repeated occurrences of the same node instance. Bulk mutations are validated
    /// before the list changes.
    ///
    /// @return the live, mutable action list
    public final ObservableList<Node> getActions() {
        return actions;
    }

    /// Returns the CSS metadata for banner component tokens.
    ///
    /// @return the CSS metadata for banner component tokens
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this banner.
    ///
    /// @return the CSS metadata for this banner
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Returns the user-agent stylesheet for M3FX banners.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("banner.css");
    }

    /// Returns accessibility attributes for the message and action collection.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        return switch (attribute) {
            case TEXT -> getText();
            case ITEM_COUNT -> M3Accessible.itemCount(getIcon(), getActions());
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getIcon(), getActions(), parameters);
            case FOCUS_NODE -> accessibleFocusNode();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions for indexed icon and action children.
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
        if (M3Accessible.showCurrentOrItem(this, getIcon(), getActions())) {
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
        if (M3Accessible.showCurrentOrItem(this, getIcon(), getActions(), parameters)) {
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

    /// Creates the default skin for this control.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3BannerSkin(this);
    }

    /// Initializes style classes, accessibility metadata, and property listeners.
    private void initialize() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleItem, this::showAccessibleItem);
        text.addListener(observable -> updateAccessibleText());
        icon.addListener((observable, oldValue, newValue) -> notifyAccessibleItemsChanged());
        actions.addListener((ListChangeListener<Node>) change -> notifyAccessibleItemsChanged());
        addEventFilter(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        updateMetrics();
        focusNotifier.start();
        updateAccessibleText();
    }

    /// Updates helper-owned Region metrics from banner tokens.
    private void updateMetrics() {
        M3Css.setMinHeightIfUnbound(this, getContainerMinHeight());
        M3Css.setPaddingIfUnbound(
                this,
                new Insets(getVerticalPadding(), getHorizontalPadding(), getVerticalPadding(), getHorizontalPadding())
        );
        requestLayout();
    }

    /// Handles keyboard traversal between focusable icon and action items.
    private void handleNavigationKeyPressed(KeyEvent event) {
        M3FocusTraversal.handleHorizontalKeyFocus(
                this,
                event,
                M3FocusTraversal.focusTargets(getIcon(), getActions())
        );
    }

    /// Returns the current or first reachable accessibility focus node.
    private @Nullable Node accessibleFocusNode() {
        return M3Accessible.currentOrFirstFocusTarget(this, getIcon(), getActions());
    }

    /// Updates the accessible text exposed by the banner.
    private void updateAccessibleText() {
        setAccessibleText(getText());
        notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
    }

    /// Notifies accessibility clients that the indexed banner item collection changed.
    private void notifyAccessibleItemsChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// CSS metadata for banner component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the minimum banner container height token.
        private static final CssMetaData<M3Banner, Number> CONTAINER_MIN_HEIGHT =
                new CssMetaData<>("-m3-container-min-height", SizeConverter.getInstance(), DEFAULT_CONTAINER_MIN_HEIGHT) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Banner control) {
                        return M3Css.isSettable(control.containerMinHeightProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Banner control) {
                        return control.containerMinHeightProperty();
                    }
                };

        /// CSS metadata for the vertical banner padding token.
        private static final CssMetaData<M3Banner, Number> VERTICAL_PADDING =
                new CssMetaData<>("-m3-vertical-padding", SizeConverter.getInstance(), DEFAULT_VERTICAL_PADDING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Banner control) {
                        return M3Css.isSettable(control.verticalPaddingProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Banner control) {
                        return control.verticalPaddingProperty();
                    }
                };

        /// CSS metadata for the horizontal banner padding token.
        private static final CssMetaData<M3Banner, Number> HORIZONTAL_PADDING =
                new CssMetaData<>("-m3-horizontal-padding", SizeConverter.getInstance(), DEFAULT_HORIZONTAL_PADDING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Banner control) {
                        return M3Css.isSettable(control.horizontalPaddingProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Banner control) {
                        return control.horizontalPaddingProperty();
                    }
                };

        /// CSS metadata for the banner content spacing token.
        private static final CssMetaData<M3Banner, Number> CONTENT_SPACING =
                new CssMetaData<>("-m3-content-spacing", SizeConverter.getInstance(), DEFAULT_CONTENT_SPACING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Banner control) {
                        return M3Css.isSettable(control.contentSpacingProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Banner control) {
                        return control.contentSpacingProperty();
                    }
                };

        /// CSS metadata for the banner action spacing token.
        private static final CssMetaData<M3Banner, Number> ACTION_SPACING =
                new CssMetaData<>("-m3-action-spacing", SizeConverter.getInstance(), DEFAULT_ACTION_SPACING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Banner control) {
                        return M3Css.isSettable(control.actionSpacingProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Banner control) {
                        return control.actionSpacingProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(CONTAINER_MIN_HEIGHT);
            styleables.add(VERTICAL_PADDING);
            styleables.add(HORIZONTAL_PADDING);
            styleables.add(CONTENT_SPACING);
            styleables.add(ACTION_SPACING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }

        /// Prevents instantiation.
        private StyleableProperties() {
        }
    }
}
