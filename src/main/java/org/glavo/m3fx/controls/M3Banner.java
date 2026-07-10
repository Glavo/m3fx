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

/// A Material Design 3 banner for persistent contextual messages and actions.
///
/// `M3Banner` displays a message, optional leading icon, and trailing action nodes inside the current layout
/// instead of in an overlay. Use it for important contextual information that should remain visible until the
/// user acts or the application state changes.
///
/// See [Material Design](https://m3.material.io/) for the component and interaction principles used by M3FX.
@NotNullByDefault
public class M3Banner extends Control {
    /// The base style class for M3FX banners.
    public static final String STYLE_CLASS = "m3-banner";

    /// The internal content container style class.
    public static final String CONTAINER_STYLE_CLASS = "m3-banner-container";

    /// The leading icon slot style class.
    public static final String ICON_STYLE_CLASS = "m3-banner-icon";

    /// The text label style class.
    public static final String TEXT_STYLE_CLASS = "m3-banner-text";

    /// The actions container style class.
    public static final String ACTIONS_STYLE_CLASS = "m3-banner-actions";

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

    // The banner message text property.
    private final StringProperty text = new SimpleStringProperty(this, "text", "");

    // The optional leading icon property.
    private final ObjectProperty<@Nullable Node> icon = new SimpleObjectProperty<>(this, "icon");

    /// The mutable trailing action node list.
    private final ObservableList<Node> actions = M3ObservableLists.nonNullElementList("action");

    // The minimum banner container height token.
    private @Nullable StyleableDoubleProperty containerMinHeight;

    // The vertical banner content padding token.
    private @Nullable StyleableDoubleProperty verticalPadding;

    // The horizontal banner content padding token.
    private @Nullable StyleableDoubleProperty horizontalPadding;

    // The spacing token between icon, text, and actions.
    private @Nullable StyleableDoubleProperty contentSpacing;

    // The spacing token between action nodes.
    private @Nullable StyleableDoubleProperty actionSpacing;

    /// Notifies accessibility clients when focus moves between action children.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, this::accessibleFocusNode);

    /// Creates an empty banner.
    public M3Banner() {
        this("");
    }

    /// Creates a banner with message text.
    ///
    /// @param text the banner message text
    public M3Banner(String text) {
        initialize();
        setText(text);
    }


    /// Returns the banner message text.
    ///
    /// @return the banner message text
    public final String getText() {
        return text.get();
    }

    /// Sets the banner message text.
    ///
    /// @param text the banner message text
    public final void setText(String text) {
        this.text.set(Objects.requireNonNull(text, "text"));
    }

    /// Returns the banner message text property.
    ///
    /// @return the banner message text property
    public final StringProperty textProperty() {
        return text;
    }

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

    /// Returns the optional leading icon property.
    ///
    /// @return the leading icon property
    public final ObjectProperty<@Nullable Node> iconProperty() {
        return icon;
    }

    /// Returns the mutable trailing action node list.
    ///
    /// @return the mutable trailing action node list
    public final ObservableList<Node> getActions() {
        return actions;
    }

    /// Returns the minimum banner container height token.
    ///
    /// @return the minimum banner container height in pixels
    public final double getContainerMinHeight() {
        return containerMinHeight == null ? DEFAULT_CONTAINER_MIN_HEIGHT : containerMinHeight.get();
    }

    /// Sets the minimum banner container height token.
    ///
    /// @param containerMinHeight the minimum banner container height in pixels
    public final void setContainerMinHeight(double containerMinHeight) {
        containerMinHeightProperty().set(M3Css.nonNegative(containerMinHeight, "containerMinHeight"));
    }

    /// Returns the minimum banner container height token property.
    ///
    /// @return the minimum banner container height token property
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

    /// Returns the vertical banner content padding token.
    ///
    /// @return the vertical banner content padding in pixels
    public final double getVerticalPadding() {
        return verticalPadding == null ? DEFAULT_VERTICAL_PADDING : verticalPadding.get();
    }

    /// Sets the vertical banner content padding token.
    ///
    /// @param verticalPadding the vertical banner content padding in pixels
    public final void setVerticalPadding(double verticalPadding) {
        verticalPaddingProperty().set(M3Css.nonNegative(verticalPadding, "verticalPadding"));
    }

    /// Returns the vertical banner content padding token property.
    ///
    /// @return the vertical banner content padding token property
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

    /// Returns the horizontal banner content padding token.
    ///
    /// @return the horizontal banner content padding in pixels
    public final double getHorizontalPadding() {
        return horizontalPadding == null ? DEFAULT_HORIZONTAL_PADDING : horizontalPadding.get();
    }

    /// Sets the horizontal banner content padding token.
    ///
    /// @param horizontalPadding the horizontal banner content padding in pixels
    public final void setHorizontalPadding(double horizontalPadding) {
        horizontalPaddingProperty().set(M3Css.nonNegative(horizontalPadding, "horizontalPadding"));
    }

    /// Returns the horizontal banner content padding token property.
    ///
    /// @return the horizontal banner content padding token property
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

    /// Returns the spacing token between icon, text, and actions.
    ///
    /// @return the spacing between icon, text, and actions in pixels
    public final double getContentSpacing() {
        return contentSpacing == null ? DEFAULT_CONTENT_SPACING : contentSpacing.get();
    }

    /// Sets the spacing token between icon, text, and actions.
    ///
    /// @param contentSpacing the spacing between icon, text, and actions in pixels
    public final void setContentSpacing(double contentSpacing) {
        contentSpacingProperty().set(M3Css.nonNegative(contentSpacing, "contentSpacing"));
    }

    /// Returns the spacing token between icon, text, and actions property.
    ///
    /// @return the spacing token between icon, text, and actions property
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

    /// Returns the spacing token between action nodes.
    ///
    /// @return the spacing between action nodes in pixels
    public final double getActionSpacing() {
        return actionSpacing == null ? DEFAULT_ACTION_SPACING : actionSpacing.get();
    }

    /// Sets the spacing token between action nodes.
    ///
    /// @param actionSpacing the spacing between action nodes in pixels
    public final void setActionSpacing(double actionSpacing) {
        actionSpacingProperty().set(M3Css.nonNegative(actionSpacing, "actionSpacing"));
    }

    /// Returns the spacing token between action nodes property.
    ///
    /// @return the spacing token between action nodes property
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

    /// Creates the default Material Design 3 banner skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3BannerSkin(this);
    }

    /// Initializes style classes, accessibility metadata, and property listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
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

        /// Prevents metadata holder instantiation.
        private StyleableProperties() {
        }
    }
}
