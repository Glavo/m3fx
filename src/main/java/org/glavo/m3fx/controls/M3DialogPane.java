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
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3FocusTraversal;
import org.glavo.m3fx.internal.M3ModalFocusTrap;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3DialogPaneSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/// A Material Design 3 dialog pane.
///
/// The pane owns a headline, optional graphic, text or node content, and an ordered set of Material action buttons.
/// It is the content surface of an [M3Dialog] and does not itself create a native window. [M3OverlayPane] and
/// [M3DialogWindow] provide in-scene and native-window presentation.
///
/// The [actions][#getActions()] list is live, ordered, and rejects `null` elements before mutation. Each action is
/// the actual [M3Button] rendered by the pane, so its text, graphic, disabled state, role, and event handlers remain
/// observable and configurable without a parallel descriptor object. An unconsumed action event is reported to the
/// containing [M3Dialog], which emits its cancellable close lifecycle with that exact button. While the pane is
/// presented modally, keyboard traversal remains within reachable content and action controls. The pane itself is
/// not focus traversable.
///
/// Geometry properties are expressed in logical pixels and are styleable. Java assignments take precedence over
/// user-agent defaults, and CSS cannot replace a bound styleable property.
///
/// See [Material Design dialogs](https://m3.material.io/components/dialogs/overview).
@NotNullByDefault
public class M3DialogPane extends Control {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-dialog-pane";

    /// The default dialog container shape radius.
    private static final double DEFAULT_CONTAINER_SHAPE = 28.0;

    /// The default dialog content padding.
    private static final double DEFAULT_CONTENT_PADDING = 24.0;

    /// The default minimum dialog container width.
    private static final double DEFAULT_CONTAINER_MIN_WIDTH = 280.0;

    /// The default maximum dialog container width.
    private static final double DEFAULT_CONTAINER_MAX_WIDTH = 560.0;

    /// The default spacing between dialog action buttons.
    private static final double DEFAULT_ACTION_SPACING = 8.0;

    /// The default dialog graphic icon size.
    private static final double DEFAULT_ICON_SIZE = 24.0;

    /// The transparent layout margin reserved around the dialog surface for its level 3 shadow.
    private static final double SURFACE_EFFECT_MARGIN = 12.0;

    /// The style class applied to Material icons used as dialog graphics.
    private static final String GRAPHIC_ICON_STYLE_CLASS = "m3-dialog-graphic-icon";

    /// Creates an empty dialog pane with Material geometry and no actions.
    ///
    /// Header and content text default to empty strings; graphic and content nodes default to `null`.
    public M3DialogPane() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        // JavaFX 17 has no DIALOG role; the helper returns PARENT there.
        setAccessibleRole(M3Accessible.dialogRole());
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleNode, this::showAccessibleItem);
        contentProperty().addListener((observable, oldValue, newValue) -> notifyAccessibleItemsChanged());
        graphicProperty().addListener((observable, oldValue, newValue) -> updateGraphicMetrics(oldValue, newValue));
        getActions().addListener((ListChangeListener<M3Button>) change -> notifyAccessibleItemsChanged());
        styleProperty().addListener((observable, oldValue, newValue) -> {
            if (!updatingManagedStyle && managedContainerShapeStyle != null) {
                requestContainerShapeStyleSync();
            }
        });
        visibleProperty().addListener((observable, oldValue, newValue) -> focusTrap.update());
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleActionNavigationKey);
        addEventHandler(ActionEvent.ACTION, this::handleActionButtonEvent);
        focusTrap.install();
        focusNotifier.start();
        updateMetrics();
        updateAccessibleText();
    }

    /// The dialog headline displayed before the content area.
    ///
    /// Empty and blank values omit the headline section from layout. Values must be non-null; direct property writes
    /// validate this constraint when invalidated.
    ///
    /// @defaultValue `""`
    private final StringProperty headerText = new SimpleStringProperty(this, "headerText", "") {
        /// Rejects null headline values and refreshes accessibility state.
        @Override
        protected void invalidated() {
            Objects.requireNonNull(get(), "headerText");
            updateAccessibleText();
        }
    };

    /// Returns the dialog headline.
    ///
    /// @return the non-null dialog headline, or an empty string when no headline is displayed
    public final String getHeaderText() {
        return headerText.get();
    }

    /// Sets the dialog headline.
    ///
    /// @param headerText the dialog headline, or an empty string to hide it
    /// @throws NullPointerException if `headerText` is `null`
    public final void setHeaderText(String headerText) {
        this.headerText.set(Objects.requireNonNull(headerText, "headerText"));
    }

    /// Returns the observable property that stores the dialog headline.
    ///
    /// The property can be observed and bound. Its default value is the empty string, and values must be non-null.
    /// Empty and blank values omit the headline section from layout.
    ///
    /// @return the dialog headline property
    public final StringProperty headerTextProperty() {
        return headerText;
    }

    /// The fallback body text displayed while [content][#contentProperty()] is `null`.
    ///
    /// Empty and blank values omit the fallback content section from layout. Values must be non-null; direct property
    /// writes validate this constraint when invalidated.
    ///
    /// @defaultValue `""`
    private final StringProperty contentText = new SimpleStringProperty(this, "contentText", "") {
        /// Rejects null content text and refreshes accessibility state.
        @Override
        protected void invalidated() {
            Objects.requireNonNull(get(), "contentText");
            updateAccessibleText();
        }
    };

    /// Returns the fallback dialog body text.
    ///
    /// @return the non-null body text, or an empty string when no text is displayed
    public final String getContentText() {
        return contentText.get();
    }

    /// Sets the fallback dialog body text used when [#getContent()] is `null`.
    ///
    /// @param contentText the dialog body text, or an empty string to hide it
    /// @throws NullPointerException if `contentText` is `null`
    public final void setContentText(String contentText) {
        this.contentText.set(Objects.requireNonNull(contentText, "contentText"));
    }

    /// Returns the observable property that stores the fallback body text.
    ///
    /// The property can be observed and bound. Its default value is the empty string, and values must be non-null.
    /// The text is displayed only while [content][#contentProperty()] is `null`.
    ///
    /// @return the fallback body-text property
    public final StringProperty contentTextProperty() {
        return contentText;
    }

    /// The optional graphic displayed before the dialog headline.
    ///
    /// A non-null node must not already belong to another scene-graph parent when this pane is presented. Material
    /// icon sizing is applied only when the value is an [M3Icon].
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Node> graphic = new SimpleObjectProperty<>(this, "graphic");

    /// Returns the optional dialog graphic.
    ///
    /// @return the dialog graphic, or `null` when none is configured
    public final @Nullable Node getGraphic() {
        return graphic.get();
    }

    /// Sets the optional dialog graphic.
    ///
    /// @param graphic the dialog graphic, or `null` to remove it
    public final void setGraphic(@Nullable Node graphic) {
        this.graphic.set(graphic);
    }

    /// Returns the observable property that stores the optional dialog graphic.
    ///
    /// The property can be observed and bound, and its default value is `null`. A non-null node must not already
    /// belong to another scene-graph parent when this pane is presented.
    ///
    /// @return the dialog graphic property
    public final ObjectProperty<@Nullable Node> graphicProperty() {
        return graphic;
    }

    /// The optional node displayed in the dialog content area.
    ///
    /// A non-null node replaces [contentText][#contentTextProperty()] visually and must not already belong to
    /// another scene-graph parent when this pane is presented.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Node> content = new SimpleObjectProperty<>(this, "content");

    /// Returns the optional dialog content node.
    ///
    /// @return the content node, or `null` to display [#getContentText()]
    public final @Nullable Node getContent() {
        return content.get();
    }

    /// Sets the dialog content node.
    ///
    /// @param content the content node, or `null` to display [#getContentText()]
    public final void setContent(@Nullable Node content) {
        this.content.set(content);
    }

    /// Returns the observable property that stores the optional dialog content node.
    ///
    /// The property can be observed and bound, and its default value is `null`. A non-null node replaces the
    /// fallback body text visually and must not already belong to another scene-graph parent when this pane is
    /// presented.
    ///
    /// @return the dialog content-node property
    public final ObjectProperty<@Nullable Node> contentProperty() {
        return content;
    }

    /// The dialog container corner radius in logical pixels.
    ///
    /// This styleable property maps to `-m3-container-shape`.
    ///
    /// @defaultValue `28.0`
    private @Nullable StyleableDoubleProperty containerShape;

    /// Returns the dialog container corner radius in logical pixels.
    ///
    /// @return the dialog container shape radius token
    public final double getContainerShape() {
        return containerShape == null ? DEFAULT_CONTAINER_SHAPE : containerShape.get();
    }

    /// Sets the dialog container corner radius in logical pixels.
    ///
    /// @param containerShape the dialog container shape radius token
    /// @throws IllegalArgumentException if `containerShape` is negative or not finite
    public final void setContainerShape(double containerShape) {
        containerShapeProperty().set(M3Css.nonNegative(containerShape, "containerShape"));
    }

    /// Returns the styleable property that stores the dialog corner radius.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-container-shape`, and accepts finite,
    /// non-negative values. Its default value is `28.0` logical pixels.
    ///
    /// @return the container-shape property, in logical pixels
    public final StyleableDoubleProperty containerShapeProperty() {
        if (containerShape == null) {
            containerShape = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTAINER_SHAPE,
                    this,
                    "containerShape",
                    StyleableProperties.CONTAINER_SHAPE,
                    this::requestContainerShapeStyleSync
            );
        }
        return containerShape;
    }

    /// The uniform padding between the visible dialog surface edge and its sections, in logical pixels.
    ///
    /// This styleable property maps to `-m3-content-padding`.
    ///
    /// @defaultValue `24.0`
    private @Nullable StyleableDoubleProperty contentPadding;

    /// Returns the uniform dialog content padding in logical pixels.
    ///
    /// @return the dialog content padding token
    public final double getContentPadding() {
        return contentPadding == null ? DEFAULT_CONTENT_PADDING : contentPadding.get();
    }

    /// Sets the uniform dialog content padding in logical pixels.
    ///
    /// @param contentPadding the dialog content padding token
    /// @throws IllegalArgumentException if `contentPadding` is negative or not finite
    public final void setContentPadding(double contentPadding) {
        contentPaddingProperty().set(M3Css.nonNegative(contentPadding, "contentPadding"));
    }

    /// Returns the styleable property that stores the uniform content padding.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-content-padding`, and accepts finite,
    /// non-negative values. Its default value is `24.0` logical pixels.
    ///
    /// @return the content-padding property, in logical pixels
    public final StyleableDoubleProperty contentPaddingProperty() {
        if (contentPadding == null) {
            contentPadding = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTENT_PADDING,
                    this,
                    "contentPadding",
                    StyleableProperties.CONTENT_PADDING,
                    this::updateMetrics
            );
        }
        return contentPadding;
    }

    /// The minimum visible dialog surface width in logical pixels.
    ///
    /// Shadow effect margins are added outside this value when the control computes its minimum region width. This
    /// styleable property maps to `-m3-container-min-width`.
    ///
    /// @defaultValue `280.0`
    private @Nullable StyleableDoubleProperty containerMinWidth;

    /// Returns the minimum dialog surface width in logical pixels.
    ///
    /// @return the minimum dialog container width token
    public final double getContainerMinWidth() {
        return containerMinWidth == null ? DEFAULT_CONTAINER_MIN_WIDTH : containerMinWidth.get();
    }

    /// Sets the minimum dialog surface width in logical pixels.
    ///
    /// @param containerMinWidth the minimum dialog container width token
    /// @throws IllegalArgumentException if `containerMinWidth` is negative or not finite
    public final void setContainerMinWidth(double containerMinWidth) {
        containerMinWidthProperty().set(M3Css.nonNegative(containerMinWidth, "containerMinWidth"));
    }

    /// Returns the styleable property that stores the minimum visible surface width.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-container-min-width`, and accepts finite,
    /// non-negative values. Its default value is `280.0` logical pixels, excluding shadow margins.
    ///
    /// @return the minimum container-width property, in logical pixels
    public final StyleableDoubleProperty containerMinWidthProperty() {
        if (containerMinWidth == null) {
            containerMinWidth = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTAINER_MIN_WIDTH,
                    this,
                    "containerMinWidth",
                    StyleableProperties.CONTAINER_MIN_WIDTH,
                    this::updateMetrics
            );
        }
        return containerMinWidth;
    }

    /// The preferred maximum dialog surface width in logical pixels.
    ///
    /// If this value is less than [containerMinWidth][#containerMinWidthProperty()], effective layout still permits
    /// at least the configured minimum width. This styleable property maps to `-m3-container-max-width`.
    ///
    /// @defaultValue `560.0`
    private @Nullable StyleableDoubleProperty containerMaxWidth;

    /// Returns the preferred maximum dialog surface width in logical pixels.
    ///
    /// @return the maximum dialog container width token
    public final double getContainerMaxWidth() {
        return containerMaxWidth == null ? DEFAULT_CONTAINER_MAX_WIDTH : containerMaxWidth.get();
    }

    /// Sets the preferred maximum dialog surface width in logical pixels.
    ///
    /// @param containerMaxWidth the maximum dialog container width token
    /// @throws IllegalArgumentException if `containerMaxWidth` is negative or not finite
    public final void setContainerMaxWidth(double containerMaxWidth) {
        containerMaxWidthProperty().set(M3Css.nonNegative(containerMaxWidth, "containerMaxWidth"));
    }

    /// Returns the styleable property that stores the preferred maximum surface width.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-container-max-width`, and accepts finite,
    /// non-negative values. Its default value is `560.0` logical pixels; the effective width still honors the
    /// configured minimum.
    ///
    /// @return the maximum container-width property, in logical pixels
    public final StyleableDoubleProperty containerMaxWidthProperty() {
        if (containerMaxWidth == null) {
            containerMaxWidth = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTAINER_MAX_WIDTH,
                    this,
                    "containerMaxWidth",
                    StyleableProperties.CONTAINER_MAX_WIDTH,
                    this::updateMetrics
            );
        }
        return containerMaxWidth;
    }

    /// The spacing between adjacent dialog action buttons in logical pixels.
    ///
    /// This styleable property maps to `-m3-action-spacing`.
    ///
    /// @defaultValue `8.0`
    private @Nullable StyleableDoubleProperty actionSpacing;

    /// Returns the spacing between dialog action buttons in logical pixels.
    ///
    /// @return the spacing between dialog action buttons
    public final double getActionSpacing() {
        return actionSpacing == null ? DEFAULT_ACTION_SPACING : actionSpacing.get();
    }

    /// Sets the spacing between dialog action buttons in logical pixels.
    ///
    /// @param actionSpacing the spacing between dialog action buttons
    /// @throws IllegalArgumentException if `actionSpacing` is negative or not finite
    public final void setActionSpacing(double actionSpacing) {
        actionSpacingProperty().set(M3Css.nonNegative(actionSpacing, "actionSpacing"));
    }

    /// Returns the styleable property that stores action-button spacing.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-action-spacing`, and accepts finite,
    /// non-negative values. Its default value is `8.0` logical pixels.
    ///
    /// @return the action-spacing property, in logical pixels
    public final StyleableDoubleProperty actionSpacingProperty() {
        if (actionSpacing == null) {
            actionSpacing = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_ACTION_SPACING,
                    this,
                    "actionSpacing",
                    StyleableProperties.ACTION_SPACING,
                    this::updateActionSpacing
            );
        }
        return actionSpacing;
    }

    /// The preferred size of an [M3Icon] used as the dialog graphic, in logical pixels.
    ///
    /// Other graphic node types are not resized by this property. A bound icon-size property is not replaced. This
    /// styleable property maps to `-m3-dialog-icon-size`.
    ///
    /// @defaultValue `24.0`
    private @Nullable StyleableDoubleProperty iconSize;

    /// Returns the preferred Material dialog graphic icon size in logical pixels.
    ///
    /// @return the dialog graphic icon size token
    public final double getIconSize() {
        return iconSize == null ? DEFAULT_ICON_SIZE : iconSize.get();
    }

    /// Sets the preferred size of an [M3Icon] used as the dialog graphic, in logical pixels.
    ///
    /// @param iconSize the dialog graphic icon size token
    /// @throws IllegalArgumentException if `iconSize` is negative or not finite
    public final void setIconSize(double iconSize) {
        iconSizeProperty().set(M3Css.nonNegative(iconSize, "iconSize"));
    }

    /// Returns the styleable property that stores the preferred dialog icon size.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-dialog-icon-size`, and accepts finite,
    /// non-negative values. Its default value is `24.0` logical pixels and affects only [M3Icon] graphics.
    ///
    /// @return the icon-size property, in logical pixels
    public final StyleableDoubleProperty iconSizeProperty() {
        if (iconSize == null) {
            iconSize = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_ICON_SIZE,
                    this,
                    "iconSize",
                    StyleableProperties.ICON_SIZE,
                    () -> updateGraphicMetrics(null, getGraphic())
            );
        }
        return iconSize;
    }

    /// The live ordered list of Material dialog actions.
    private final ObservableList<M3Button> actions = M3ObservableLists.identityDistinctElementList("action");

    /// An optional action shown at the logical start of the action row.
    ///
    /// This slot supports specification-defined picker affordances without broadening the public dialog action list
    /// beyond text-capable [M3Button] instances.
    private @Nullable Node leadingAction;

    /// Receives unconsumed action-button activations while this pane belongs to a dialog.
    private @Nullable Consumer<M3Button> buttonAction;

    /// Whether this pane currently forms the active modal surface of a dialog overlay.
    private boolean modalActive;

    /// The inline style declaration managed by the container shape token.
    private @Nullable String managedContainerShapeStyle;

    /// Whether the current style change is produced by managed metric synchronization.
    private boolean updatingManagedStyle;

    /// Whether the managed container shape style must be synchronized before the next layout pass.
    private boolean containerShapeStyleDirty;

    /// Reports focused dialog content or action changes to accessibility clients.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, this::currentOrFirstFocusableItem);

    /// Keeps keyboard traversal inside this dialog pane while it is presented as a modal overlay.
    private final M3ModalFocusTrap focusTrap = new M3ModalFocusTrap(
            this,
            this::isFocusTrapActive,
            this::focusTrapTargets,
            this::fireCancelButton
    );

    /// Returns the live ordered list of Material dialog actions.
    ///
    /// The list rejects `null` elements atomically. Each button exposes its ordinary JavaFX properties and event
    /// lifecycle. Buttons normally use
    /// [M3ButtonVariant#TEXT] to conform to the Material dialog specification. A button marked with
    /// [M3ButtonBase#setDefaultButton(boolean)] receives preferred initial focus after dialog content; a button
    /// marked with [M3ButtonBase#setCancelButton(boolean)] is fired by the dialog's cancel-key behavior.
    ///
    /// Mutations are observed immediately and insertion order determines layout, keyboard traversal, and action-role
    /// resolution. The list rejects `null` elements and repeated occurrences of the same button instance. Bulk
    /// mutations are validated before the list changes, and each button must satisfy the JavaFX single-parent rule.
    ///
    /// @return the mutable action-button list
    public final ObservableList<M3Button> getActions() {
        return actions;
    }

    /// Returns the first action marked as this dialog's default action.
    ///
    /// The result is resolved from the current action-list order each time this method is called. Marking more than
    /// one action as default is permitted, but only the first is used for preferred initial focus.
    ///
    /// @return the first default action, or `null` when no action has that role
    public final @Nullable M3Button getDefaultAction() {
        for (M3Button action : actions) {
            if (action.isDefaultButton()) {
                return action;
            }
        }
        return null;
    }

    /// Returns the first action marked as this dialog's cancel action.
    ///
    /// The result is resolved from the current action-list order each time this method is called. The dialog's
    /// cancel-key behavior fires this action when it is reachable.
    ///
    /// @return the first cancel action, or `null` when no action has that role
    public final @Nullable M3Button getCancelAction() {
        for (M3Button action : actions) {
            if (action.isCancelButton()) {
                return action;
            }
        }
        return null;
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the CSS metadata for this control class
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this dialog pane.
    ///
    /// @return the immutable CSS metadata list for this control
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Returns accessibility attributes for dialog content, actions, and focus state.
    ///
    /// @param attribute  the requested accessibility attribute
    /// @param parameters optional attribute parameters supplied by JavaFX
    /// @return the resolved accessibility value, or `null` when unavailable
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case CONTENTS -> getContent();
            case FOCUS_NODE -> currentOrFirstFocusableItem();
            case ITEM_COUNT -> accessibleItemCount();
            case ITEM_AT_INDEX -> accessibleItemAt(parameters);
            case TEXT -> {
                @Nullable String text = getAccessibleText();
                yield text == null ? "" : text;
            }
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions for dialog content and action buttons.
    ///
    /// @param action     the requested accessibility action
    /// @param parameters optional action parameters supplied by JavaFX
    /// @throws NullPointerException if `action` is `null`
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        if (isDisabled()) {
            super.executeAccessibleAction(action, parameters);
            return;
        }

        switch (action) {
            case REQUEST_FOCUS -> focusAccessibleNode();
            case SHOW_ITEM -> showAccessibleItem(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Creates the default visual representation of this dialog pane.
    ///
    /// @return a new default dialog-pane skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3DialogPaneSkin(this, leadingAction);
    }

    /// Forwards an unconsumed action from one of this pane's action nodes to its dialog controller.
    private void handleActionButtonEvent(ActionEvent event) {
        if (event.isConsumed() || !(event.getTarget() instanceof Node target)) {
            return;
        }

        @Nullable Node current = target;
        while (current != null && current != this) {
            for (M3Button actionButton : actions) {
                if (actionButton == current) {
                    @Nullable Consumer<M3Button> action = buttonAction;
                    if (action != null) {
                        action.accept(actionButton);
                    }
                    return;
                }
            }
            current = current.getParent();
        }
    }

    /// Sets the controller callback for unconsumed dialog action events.
    ///
    /// @param buttonAction the callback, or `null` while this pane is not owned by a dialog
    final void setButtonAction(@Nullable Consumer<M3Button> buttonAction) {
        this.buttonAction = buttonAction;
    }

    /// Installs an action at the logical start of the action row.
    ///
    /// The leading action participates in focus traversal and accessibility but does not automatically request dialog
    /// closure. It must be configured before the pane creates its skin.
    ///
    /// @param leadingAction the leading action, or `null` for no leading action
    /// @throws IllegalStateException if the pane already has a skin
    final void setLeadingAction(@Nullable Node leadingAction) {
        if (getSkin() != null) {
            throw new IllegalStateException("leading action must be configured before the dialog pane creates a skin");
        }
        this.leadingAction = leadingAction;
        notifyAccessibleItemsChanged();
    }

    /// Updates whether keyboard traversal is constrained to this modal dialog surface.
    ///
    /// @param modalActive whether this pane is the active modal overlay
    final void setModalActive(boolean modalActive) {
        if (this.modalActive == modalActive) {
            return;
        }
        this.modalActive = modalActive;
        focusTrap.update();
    }

    /// Requests focus on the preferred initial dialog target.
    final void requestInitialFocus() {
        @Nullable Node target = firstFocusableItem();
        if (target != null) {
            target.requestFocus();
        }
    }

    /// Fires the configured cancel action when it is reachable.
    private void fireCancelButton() {
        @Nullable M3Button action = getCancelAction();
        if (action != null && M3Accessible.canReach(action)) {
            action.fire();
        }
    }

    /// Handles keyboard traversal between dialog action buttons.
    private void handleActionNavigationKey(KeyEvent event) {
        if (M3FocusTraversal.focusOwnerInside(this, getContent())) {
            return;
        }

        M3FocusTraversal.handleHorizontalKeyFocus(
                this,
                event,
                M3FocusTraversal.focusTargets(leadingAction, actions)
        );
    }

    /// Returns whether dialog keyboard focus should currently stay inside this pane.
    private boolean isFocusTrapActive() {
        return modalActive && M3Accessible.canReach(this);
    }

    /// Returns the focus targets contained by this dialog pane in traversal order.
    private List<Node> focusTrapTargets() {
        List<Node> actionTargets = M3FocusTraversal.focusTargetsInReachableTrees(leadingAction, actions);
        @Nullable Node content = getContent();
        if (content == null) {
            return actionTargets;
        }
        List<Node> contentTargets = M3FocusTraversal.focusTargetsInReachableTree(content);
        if (contentTargets.isEmpty()) {
            return actionTargets;
        }
        if (actionTargets.isEmpty()) {
            return contentTargets;
        }
        Node[] targets = new Node[contentTargets.size() + actionTargets.size()];
        int index = 0;
        for (Node target : contentTargets) {
            targets[index++] = target;
        }
        for (Node target : actionTargets) {
            targets[index++] = target;
        }
        return List.of(targets);
    }

    /// Returns the user-agent stylesheet for M3FX dialog panes.
    ///
    /// @return the non-null dialog stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("dialog.css");
    }

    /// Lays out the dialog pane after synchronizing managed shape styles.
    @Override
    protected void layoutChildren() {
        synchronizeContainerShapeStyle();
        updateGraphicMetrics(null, getGraphic());
        super.layoutChildren();
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double surfaceInsets = SURFACE_EFFECT_MARGIN * 2.0;
        double minWidth = getContainerMinWidth() + surfaceInsets;
        M3Css.setPaddingIfUnbound(this, new Insets(getContentPadding() + SURFACE_EFFECT_MARGIN));
        M3Css.setMinWidthIfUnbound(this, minWidth);
        M3Css.setMaxWidthIfUnbound(this, Math.max(minWidth, getContainerMaxWidth() + surfaceInsets));
        updateActionSpacing();
        updateGraphicMetrics(null, getGraphic());
    }

    /// Requests layout after the action-spacing token changes.
    private void updateActionSpacing() {
        requestLayout();
    }

    /// Applies dialog graphic tokens to a Material icon graphic.
    private void updateGraphicMetrics(@Nullable Node oldGraphic, @Nullable Node newGraphic) {
        if (oldGraphic instanceof M3IconGraphic) {
            restoreGraphicIconStyle(oldGraphic);
        }
        if (!(newGraphic instanceof M3IconGraphic icon)) {
            return;
        }

        applyGraphicIconStyle(newGraphic);
        double targetSize = getIconSize();
        if (M3Css.isSettable(icon.iconSizeProperty()) && Double.compare(icon.getIconSize(), targetSize) != 0) {
            icon.setIconSize(targetSize);
        }
    }

    /// Applies the dialog-specific style class to a graphic icon.
    private static void applyGraphicIconStyle(Node icon) {
        if (!icon.getStyleClass().contains(GRAPHIC_ICON_STYLE_CLASS)) {
            icon.getStyleClass().add(GRAPHIC_ICON_STYLE_CLASS);
        }
    }

    /// Removes dialog-specific styling after an icon leaves the graphic slot.
    private static void restoreGraphicIconStyle(Node icon) {
        icon.getStyleClass().remove(GRAPHIC_ICON_STYLE_CLASS);
    }

    /// Requests managed container shape synchronization before layout.
    private void requestContainerShapeStyleSync() {
        containerShapeStyleDirty = true;
        requestLayout();
    }

    /// Synchronizes the resolved background radius with the current container shape token.
    private void synchronizeContainerShapeStyle() {
        if (!containerShapeStyleDirty || updatingManagedStyle) {
            return;
        }
        containerShapeStyleDirty = false;
        String baseStyle = removeManagedContainerShapeStyle(getStyle());
        String nextManagedStyle = "-fx-background-radius: " + formatPixels(getContainerShape()) + ";";
        String nextStyle = mergeStyles(baseStyle, nextManagedStyle);
        managedContainerShapeStyle = nextManagedStyle;
        if (nextStyle.equals(getStyle())) {
            return;
        }

        updatingManagedStyle = true;
        try {
            setStyle(nextStyle);
            if (getScene() != null) {
                applyCss();
            }
        } finally {
            updatingManagedStyle = false;
        }
    }

    /// Removes the previous managed background-radius declaration from a style string.
    private String removeManagedContainerShapeStyle(String style) {
        @Nullable String managedStyle = managedContainerShapeStyle;
        if (managedStyle == null || style.isBlank()) {
            return style;
        }

        int index = style.indexOf(managedStyle);
        if (index < 0) {
            return style;
        }

        String before = style.substring(0, index).stripTrailing();
        String after = style.substring(index + managedStyle.length()).stripLeading();
        return mergeStyles(before, after);
    }

    /// Formats a token value as a CSS pixel size.
    private static String formatPixels(double value) {
        if (Math.rint(value) == value) {
            return (long) value + "px";
        }
        return value + "px";
    }

    /// Merges two inline style fragments.
    private static String mergeStyles(String first, String second) {
        if (first.isBlank()) {
            return second;
        }
        if (second.isBlank()) {
            return first;
        }
        return first.stripTrailing() + " " + second.stripLeading();
    }

    /// Updates the accessibility label from the dialog header and content text.
    private void updateAccessibleText() {
        StringBuilder builder = new StringBuilder();
        appendAccessibleText(builder, getHeaderText());
        appendAccessibleText(builder, getContentText());
        setAccessibleText(builder.isEmpty() ? null : builder.toString());
        notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
    }

    /// Notifies accessibility clients that dialog content or actions changed.
    private void notifyAccessibleItemsChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.CONTENTS);
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleFocusChanged();
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
    }

    /// Notifies accessibility clients that the exposed dialog focus target changed.
    private void notifyAccessibleFocusChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Returns the indexed accessibility item count for content and action buttons.
    private int accessibleItemCount() {
        return (getContent() == null ? 0 : 1)
                + (leadingAction == null ? 0 : 1)
                + actions.size();
    }

    /// Returns the dialog content or action button at an accessibility index.
    private @Nullable Node accessibleItemAt(Object... parameters) {
        int index = M3Accessible.indexParameter(parameters);
        if (index < 0) {
            return null;
        }

        @Nullable Node content = getContent();
        if (content != null) {
            if (index == 0) {
                return content;
            }
            index--;
        }

        @Nullable Node leading = leadingAction;
        if (leading != null) {
            if (index == 0) {
                return leading;
            }
            index--;
        }
        return index >= actions.size() ? null : actions.get(index);
    }

    /// Requests focus on the current or first dialog focus target.
    ///
    /// @return `true` when the target accepted focus
    final boolean focusAccessibleNode() {
        if (M3Accessible.showItem(this, currentOrFirstFocusableItem())) {
            notifyAccessibleFocusChanged();
            return true;
        }
        return false;
    }

    /// Focuses a requested dialog item or delegates deep popup targets to the content control.
    ///
    /// @param parameters optional accessibility target parameters
    /// @return `true` when focus moved to the default or requested dialog item
    final boolean showAccessibleItem(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        @Nullable Node item = parameters.length == 0 ? currentOrFirstFocusableItem() : accessibleActionItem(parameters);
        boolean shown = false;
        if (item != null) {
            shown = M3Accessible.showAccessibleActionTarget(this, item, parameters);
            if (!shown && canFallbackToDialogItem(item, parameters)) {
                shown = M3Accessible.showItem(this, item);
            }
        } else if (M3Accessible.showAccessibleActionTarget(this, getContent(), parameters)) {
            shown = true;
        } else {
            @Nullable Node leading = leadingAction;
            if (leading != null && M3Accessible.showAccessibleActionTarget(this, leading, parameters)) {
                shown = true;
            }
            for (M3Button button : actions) {
                if (shown) {
                    break;
                }
                if (M3Accessible.showAccessibleActionTarget(this, button, parameters)) {
                    shown = true;
                }
            }
        }
        if (shown) {
            notifyAccessibleFocusChanged();
        }
        return shown;
    }

    /// Returns whether a failed explicit reveal may fall back to focusing the resolved dialog item.
    private boolean canFallbackToDialogItem(Node item, Object... parameters) {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(parameters, "parameters");
        return parameters.length == 0 || M3Accessible.parametersDirectlyReferenceSingleTarget(
                parameter -> parameter == item
                        || (parameter instanceof Number number && accessibleItemAt(number) == item),
                parameters
        );
    }

    /// Returns the item requested by accessibility action parameters.
    private @Nullable Node accessibleActionItem(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            return firstFocusableItem();
        }
        if (parameters[0] instanceof Number) {
            return accessibleItemAt(parameters);
        }

        for (Object parameter : parameters) {
            @Nullable Node item = accessibleActionItem(parameter);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /// Returns the item requested by one accessibility action parameter.
    private @Nullable Node accessibleActionItem(@Nullable Object parameter) {
        if (parameter instanceof Number number) {
            return accessibleItemAt(number);
        }
        if (parameter instanceof Node node) {
            if (node == getContent() || isDialogButton(node)) {
                return node;
            }
            @Nullable Node content = getContent();
            if (content != null && M3Accessible.containsNode(content, node)) {
                return node;
            }
            if (content != null && M3Accessible.containsAccessibleActionTarget(content, node)) {
                return content;
            }
            @Nullable Node leading = leadingAction;
            if (leading != null
                    && (M3Accessible.containsNode(leading, node)
                    || M3Accessible.containsAccessibleActionTarget(leading, node))) {
                return leading;
            }
            for (M3Button button : actions) {
                if (M3Accessible.containsNode(button, node)
                        || M3Accessible.containsAccessibleActionTarget(button, node)) {
                    return button;
                }
            }
            return null;
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                @Nullable Node item = accessibleActionItem(value);
                if (item != null) {
                    return item;
                }
            }
            return null;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                @Nullable Node item = accessibleActionItem(value);
                if (item != null) {
                    return item;
                }
            }
        }
        return null;
    }

    /// Returns the preferred focus target for the dialog pane.
    private @Nullable Node firstFocusableItem() {
        @Nullable Node content = getContent();
        @Nullable Node contentFocusTarget = firstDialogContentFocusItem(content);
        if (contentFocusTarget != null) {
            return contentFocusTarget;
        }

        @Nullable Node defaultButton = getDefaultAction();
        @Nullable Node defaultFocusTarget = M3Accessible.focusTarget(defaultButton);
        if (defaultFocusTarget != null) {
            return defaultFocusTarget;
        }

        @Nullable Node leadingFocusTarget = M3Accessible.focusTarget(leadingAction);
        if (leadingFocusTarget != null) {
            return leadingFocusTarget;
        }

        for (M3Button button : actions) {
            @Nullable Node buttonFocusTarget = M3Accessible.focusTarget(button);
            if (buttonFocusTarget != null) {
                return buttonFocusTarget;
            }
        }
        return null;
    }

    /// Returns the first dialog content item that can expose or receive focus.
    private static @Nullable Node firstDialogContentFocusItem(@Nullable Node item) {
        if (!M3Accessible.canReach(item)) {
            return null;
        }
        @Nullable Node directFocusTarget = M3Accessible.focusTarget(item);
        if (directFocusTarget == item) {
            return item;
        }
        if (item instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable Node childFocusItem = firstDialogContentFocusItem(child);
                if (childFocusItem != null) {
                    return childFocusItem;
                }
            }
        }
        return directFocusTarget;
    }

    /// Returns the currently focused dialog item, falling back to the preferred dialog focus target.
    private @Nullable Node currentOrFirstFocusableItem() {
        @Nullable Node focusedItem = currentFocusableItem();
        return focusedItem == null ? firstFocusableItem() : focusedItem;
    }

    /// Returns the dialog content or action that currently contains scene focus.
    private @Nullable Node currentFocusableItem() {
        @Nullable Scene scene = getScene();
        @Nullable Node focusOwner = scene == null ? null : scene.getFocusOwner();
        @Nullable Node content = getContent();
        @Nullable Node externalContentFocusTarget = M3Accessible.activeExternalFocusTarget(this, content);
        if (externalContentFocusTarget != null) {
            return externalContentFocusTarget;
        }
        @Nullable Node externalLeadingFocusTarget =
                M3Accessible.activeExternalFocusTarget(this, leadingAction);
        if (externalLeadingFocusTarget != null) {
            return externalLeadingFocusTarget;
        }
        for (M3Button button : actions) {
            @Nullable Node externalButtonFocusTarget =
                    M3Accessible.activeExternalFocusTarget(this, button);
            if (externalButtonFocusTarget != null) {
                return externalButtonFocusTarget;
            }
        }
        if (focusOwner == null) {
            return null;
        }

        @Nullable Node contentFocusTarget = containedFocusTarget(content, focusOwner);
        if (contentFocusTarget != null) {
            return contentFocusTarget;
        }

        @Nullable Node leadingFocusTarget = containedFocusTarget(leadingAction, focusOwner);
        if (leadingFocusTarget != null) {
            return leadingFocusTarget;
        }

        for (M3Button button : actions) {
            @Nullable Node buttonFocusTarget = containedFocusTarget(button, focusOwner);
            if (buttonFocusTarget != null) {
                return buttonFocusTarget;
            }
        }
        return null;
    }

    /// Returns the focus owner when it is inside one dialog item, falling back to the item's focus target.
    private static @Nullable Node containedFocusTarget(@Nullable Node item, Node focusOwner) {
        if (item == null) {
            return null;
        }
        @Nullable Node itemFocusTarget = M3Accessible.focusTarget(item);
        if (itemFocusTarget == null || !M3Accessible.containsNode(item, focusOwner)) {
            return null;
        }
        return M3Accessible.canReach(focusOwner) ? focusOwner : itemFocusTarget;
    }

    /// Returns whether a node is one of this dialog pane's action buttons.
    private boolean isDialogButton(Node node) {
        if (node == leadingAction) {
            return true;
        }
        for (M3Button button : actions) {
            if (button == node) {
                return true;
            }
        }
        return false;
    }

    /// Appends a non-blank text part to an accessibility label.
    private static void appendAccessibleText(StringBuilder builder, @Nullable String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append(' ');
        }
        builder.append(text);
    }

    /// CSS metadata for M3FX dialog pane component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the container shape token.
        private static final CssMetaData<M3DialogPane, Number> CONTAINER_SHAPE =
                new CssMetaData<>("-m3-container-shape", SizeConverter.getInstance(), DEFAULT_CONTAINER_SHAPE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3DialogPane control) {
                        return M3Css.isSettable(control.containerShapeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3DialogPane control) {
                        return control.containerShapeProperty();
                    }
                };

        /// CSS metadata for the content padding token.
        private static final CssMetaData<M3DialogPane, Number> CONTENT_PADDING =
                new CssMetaData<>("-m3-content-padding", SizeConverter.getInstance(), DEFAULT_CONTENT_PADDING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3DialogPane control) {
                        return M3Css.isSettable(control.contentPaddingProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3DialogPane control) {
                        return control.contentPaddingProperty();
                    }
                };

        /// CSS metadata for the minimum container width token.
        private static final CssMetaData<M3DialogPane, Number> CONTAINER_MIN_WIDTH =
                new CssMetaData<>("-m3-container-min-width", SizeConverter.getInstance(), DEFAULT_CONTAINER_MIN_WIDTH) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3DialogPane control) {
                        return M3Css.isSettable(control.containerMinWidthProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3DialogPane control) {
                        return control.containerMinWidthProperty();
                    }
                };

        /// CSS metadata for the maximum container width token.
        private static final CssMetaData<M3DialogPane, Number> CONTAINER_MAX_WIDTH =
                new CssMetaData<>("-m3-container-max-width", SizeConverter.getInstance(), DEFAULT_CONTAINER_MAX_WIDTH) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3DialogPane control) {
                        return M3Css.isSettable(control.containerMaxWidthProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3DialogPane control) {
                        return control.containerMaxWidthProperty();
                    }
                };

        /// CSS metadata for the dialog action spacing token.
        private static final CssMetaData<M3DialogPane, Number> ACTION_SPACING =
                new CssMetaData<>("-m3-action-spacing", SizeConverter.getInstance(), DEFAULT_ACTION_SPACING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3DialogPane control) {
                        return M3Css.isSettable(control.actionSpacingProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3DialogPane control) {
                        return control.actionSpacingProperty();
                    }
                };

        /// CSS metadata for the dialog graphic icon size token.
        private static final CssMetaData<M3DialogPane, Number> ICON_SIZE =
                new CssMetaData<>("-m3-dialog-icon-size", SizeConverter.getInstance(), DEFAULT_ICON_SIZE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3DialogPane control) {
                        return M3Css.isSettable(control.iconSizeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3DialogPane control) {
                        return control.iconSizeProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(CONTAINER_SHAPE);
            styleables.add(CONTENT_PADDING);
            styleables.add(CONTAINER_MIN_WIDTH);
            styleables.add(CONTAINER_MAX_WIDTH);
            styleables.add(ACTION_SPACING);
            styleables.add(ICON_SIZE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
