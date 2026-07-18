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
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import org.glavo.m3fx.internal.M3FocusTraversal;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.M3ModalFocusTrap;
import org.glavo.m3fx.internal.M3ObservableLists;
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
/// It is designed as the retained surface of an [M3Dialog] and does not itself create a native window or depend on
/// JavaFX dialog skins. [M3OverlayPane] and [M3DialogWindow] provide in-scene and native-window presentation.
///
/// The [buttonTypes][#getButtonTypes()] list is live, ordered, and rejects `null` elements before mutation. Each
/// entry creates one [M3Button], including repeated entries; [#lookupButton(ButtonType)] returns the first matching
/// action. Unconsumed action events are reported to the containing [M3Dialog], which emits its cancellable close
/// lifecycle with the initiating button type. While the pane is presented modally, keyboard traversal remains within
/// reachable content and action controls. The pane itself is not focus traversable.
///
/// Geometry properties are expressed in logical pixels and are styleable. Java assignments take precedence over
/// user-agent defaults, and CSS cannot replace a bound styleable property.
///
/// See [Material Design dialogs](https://m3.material.io/components/dialogs/overview).
@NotNullByDefault
public class M3DialogPane extends Control {
    /// The base style class for M3FX dialog panes.
    public static final String STYLE_CLASS = "m3-dialog-pane";

    /// The style class applied to the dialog action button bar.
    public static final String BUTTON_BAR_STYLE_CLASS = "m3-dialog-button-bar";

    /// The style class applied to dialog action buttons.
    public static final String BUTTON_STYLE_CLASS = "m3-dialog-button";

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

    /// The dialog headline displayed before the content area.
    ///
    /// Empty and blank values omit the headline section from layout. The property never contains `null`.
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

    /// The fallback body text displayed while [content][#contentProperty()] is `null`.
    ///
    /// Empty and blank values omit the fallback content section from layout. The property never contains `null`.
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

    /// The optional graphic displayed before the dialog headline.
    ///
    /// A non-null node must not already belong to another scene-graph parent when the skin installs it. Material
    /// icon sizing is applied only when the value is an [M3Icon].
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Node> graphic = new SimpleObjectProperty<>(this, "graphic");

    /// The optional node displayed in the dialog content area.
    ///
    /// A non-null node replaces [contentText][#contentTextProperty()] visually and must not already belong to
    /// another scene-graph parent when the skin installs it.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Node> content = new SimpleObjectProperty<>(this, "content");

    /// The live ordered list of dialog action definitions.
    private final ObservableList<ButtonType> buttonTypes = M3ObservableLists.nonNullElementList("buttonType");

    /// Material action nodes stored in exact button-type list order.
    private final ArrayList<Node> actionButtons = new ArrayList<>();

    /// Receives unconsumed action-button activations while this pane belongs to a dialog.
    private @Nullable Consumer<ButtonType> buttonAction;

    /// Whether this pane currently forms the active modal surface of a dialog overlay.
    private boolean modalActive;

    /// The inline style declaration managed by the container shape token.
    private @Nullable String managedContainerShapeStyle;

    /// Whether the current style change is produced by managed metric synchronization.
    private boolean updatingManagedStyle;

    /// Whether the managed container shape style must be synchronized before the next layout pass.
    private boolean containerShapeStyleDirty;

    /// The dialog container corner radius in logical pixels.
    ///
    /// This styleable property maps to `-m3-container-shape`.
    ///
    /// @defaultValue `28.0`
    private @Nullable StyleableDoubleProperty containerShape;

    /// The uniform padding between the visible dialog surface edge and its sections, in logical pixels.
    ///
    /// This styleable property maps to `-m3-content-padding`.
    ///
    /// @defaultValue `24.0`
    private @Nullable StyleableDoubleProperty contentPadding;

    /// The minimum visible dialog surface width in logical pixels.
    ///
    /// Shadow effect margins are added outside this value when the control computes its minimum region width. This
    /// styleable property maps to `-m3-container-min-width`.
    ///
    /// @defaultValue `280.0`
    private @Nullable StyleableDoubleProperty containerMinWidth;

    /// The preferred maximum dialog surface width in logical pixels.
    ///
    /// If this value is less than [containerMinWidth][#containerMinWidthProperty()], effective layout still permits
    /// at least the configured minimum width. This styleable property maps to `-m3-container-max-width`.
    ///
    /// @defaultValue `560.0`
    private @Nullable StyleableDoubleProperty containerMaxWidth;

    /// The spacing between adjacent dialog action buttons in logical pixels.
    ///
    /// This styleable property maps to `-m3-action-spacing`.
    ///
    /// @defaultValue `8.0`
    private @Nullable StyleableDoubleProperty actionSpacing;

    /// The preferred size of an [M3Icon] used as the dialog graphic, in logical pixels.
    ///
    /// Other graphic node types are not resized by this property. A bound icon-size property is not replaced. This
    /// styleable property maps to `-m3-dialog-icon-size`.
    ///
    /// @defaultValue `24.0`
    private @Nullable StyleableDoubleProperty iconSize;

    /// The internal dialog action button bar created for the current skin.
    private @Nullable ButtonBar buttonBar;

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

    /// Creates an empty dialog pane with Material geometry and no button types.
    ///
    /// Header and content text default to empty strings; graphic and content nodes default to `null`.
    public M3DialogPane() {
        M3ControlStyles.initialize(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.DIALOG);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleNode, this::showAccessibleItem);
        contentProperty().addListener((observable, oldValue, newValue) -> notifyAccessibleItemsChanged());
        graphicProperty().addListener((observable, oldValue, newValue) -> updateGraphicMetrics(oldValue, newValue));
        getButtonTypes().addListener((ListChangeListener<ButtonType>) change -> {
            rebuildActionButtons();
            notifyAccessibleItemsChanged();
        });
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

    public final StringProperty headerTextProperty() {
        return headerText;
    }

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

    public final StringProperty contentTextProperty() {
        return contentText;
    }

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

    public final ObjectProperty<@Nullable Node> graphicProperty() {
        return graphic;
    }

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

    public final ObjectProperty<@Nullable Node> contentProperty() {
        return content;
    }

    /// Returns the live ordered list of dialog action definitions.
    ///
    /// The list rejects `null` elements atomically. Repeated button types are permitted and create repeated action
    /// nodes in the same order.
    ///
    /// @return the mutable button-type list
    public final ObservableList<ButtonType> getButtonTypes() {
        return buttonTypes;
    }

    /// Returns the action node created for a button type.
    ///
    /// When the same button type occurs more than once, this method returns the action created for its first
    /// occurrence. The returned node is owned by this pane and is replaced when the button-type list changes.
    ///
    /// @param buttonType the button type to locate
    /// @return the corresponding action node, or `null` when the type is not present
    /// @throws NullPointerException if `buttonType` is `null`
    public final @Nullable Node lookupButton(ButtonType buttonType) {
        int index = buttonTypes.indexOf(Objects.requireNonNull(buttonType, "buttonType"));
        return index < 0 ? null : actionButtons.get(index);
    }

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

    /// Creates the dialog action button bar used by the default skin.
    ///
    /// Subclasses may override this method to configure button ordering before the bar is installed. The returned
    /// bar must be a new instance and must not already belong to another scene-graph parent.
    ///
    /// @return a new button bar for this pane
    protected ButtonBar createButtonBar() {
        ButtonBar materialButtonBar = new ButtonBar();
        materialButtonBar.getStyleClass().add(BUTTON_BAR_STYLE_CLASS);
        materialButtonBar.setButtonMinWidth(0.0);
        materialButtonBar.setButtonOrder(ButtonBar.BUTTON_ORDER_NONE);
        return materialButtonBar;
    }

    /// Creates the Material action node associated with a dialog button type.
    ///
    /// Unconsumed action events from the returned node bubble to this pane and are forwarded to its owning
    /// [M3Dialog].
    ///
    /// @param buttonType the button type for the new action node
    /// @return the action node associated with `buttonType`
    protected Node createButton(ButtonType buttonType) {
        Objects.requireNonNull(buttonType, "buttonType");
        M3Button button = new M3Button(buttonType.getText());
        button.getStyleClass().add(BUTTON_STYLE_CLASS);
        button.setVariant(M3ButtonVariant.TEXT);
        ButtonBar.ButtonData buttonData = buttonType.getButtonData();
        ButtonBar.setButtonData(button, buttonData);
        ButtonBar.setButtonUniformSize(button, false);
        button.setDefaultButton(buttonData != null && buttonData.isDefaultButton());
        button.setCancelButton(buttonData != null && buttonData.isCancelButton());
        return button;
    }

    /// Creates the skin and installs its newly created action bar.
    ///
    /// @return a new default dialog-pane skin
    @Override
    protected Skin<?> createDefaultSkin() {
        ButtonBar materialButtonBar = createButtonBar();
        buttonBar = materialButtonBar;
        synchronizeButtonBarActions();
        synchronizeActionSpacing();
        return new M3DialogPaneSkin(this, materialButtonBar);
    }

    /// Recreates action nodes after the ordered button definitions change.
    private void rebuildActionButtons() {
        actionButtons.clear();
        for (ButtonType buttonType : buttonTypes) {
            actionButtons.add(createButton(buttonType));
        }
        synchronizeButtonBarActions();
    }

    /// Copies current action nodes into the active button bar.
    private void synchronizeButtonBarActions() {
        @Nullable ButtonBar materialButtonBar = buttonBar;
        if (materialButtonBar != null) {
            materialButtonBar.getButtons().setAll(actionButtons);
        }
    }

    /// Forwards an unconsumed action from one of this pane's action nodes to its dialog controller.
    private void handleActionButtonEvent(ActionEvent event) {
        if (event.isConsumed() || !(event.getTarget() instanceof Node target)) {
            return;
        }

        @Nullable Node current = target;
        while (current != null && current != this) {
            for (int index = 0; index < actionButtons.size(); index++) {
                if (actionButtons.get(index) == current) {
                    @Nullable Consumer<ButtonType> action = buttonAction;
                    if (action != null) {
                        action.accept(buttonTypes.get(index));
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
    final void setButtonAction(@Nullable Consumer<ButtonType> buttonAction) {
        this.buttonAction = buttonAction;
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

    /// Fires the first reachable cancel action, if one is configured.
    private void fireCancelButton() {
        for (int index = 0; index < buttonTypes.size(); index++) {
            ButtonType buttonType = buttonTypes.get(index);
            ButtonBar.ButtonData buttonData = buttonType.getButtonData();
            Node button = actionButtons.get(index);
            if (buttonData != null && buttonData.isCancelButton() && button instanceof M3ButtonBase action) {
                action.fire();
                return;
            }
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
                M3FocusTraversal.focusTargets(actionButtons)
        );
    }

    /// Returns whether dialog keyboard focus should currently stay inside this pane.
    private boolean isFocusTrapActive() {
        return modalActive && M3Accessible.canReach(this);
    }

    /// Returns the focus targets contained by this dialog pane in traversal order.
    private List<Node> focusTrapTargets() {
        return M3FocusTraversal.focusTargetsInReachableTrees(getContent(), actionButtons);
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
        synchronizeActionSpacing();
        updateGraphicMetrics(null, getGraphic());
        super.layoutChildren();
        synchronizeActionSpacing();
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

    /// Applies the action spacing token to the internal button bar and requests layout.
    private void updateActionSpacing() {
        synchronizeActionSpacing();
        requestLayout();
    }

    /// Synchronizes the action spacing token with the JavaFX button bar skin row.
    private void synchronizeActionSpacing() {
        @Nullable ButtonBar materialButtonBar = buttonBar;
        if (materialButtonBar != null) {
            synchronizeActionSpacing(materialButtonBar);
        }
    }

    /// Synchronizes action spacing below one skin node.
    private boolean synchronizeActionSpacing(Node node) {
        if (node instanceof HBox row && row.getStyleClass().contains("container")) {
            if (!row.spacingProperty().isBound()) {
                row.setSpacing(getActionSpacing());
            }
            return true;
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                if (synchronizeActionSpacing(child)) {
                    return true;
                }
            }
        }
        return false;
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
        return (getContent() == null ? 0 : 1) + getButtonTypes().size();
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

        if (index >= getButtonTypes().size()) {
            return null;
        }
        return actionButtons.get(index);
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
            for (Node button : actionButtons) {
                if (M3Accessible.showAccessibleActionTarget(this, button, parameters)) {
                    shown = true;
                    break;
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
                        || (parameter instanceof ButtonType buttonType && lookupButton(buttonType) == item)
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
        if (parameter instanceof ButtonType buttonType) {
            return lookupButton(buttonType);
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
            for (Node button : actionButtons) {
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

        @Nullable Node defaultButton = defaultButton();
        @Nullable Node defaultFocusTarget = M3Accessible.focusTarget(defaultButton);
        if (defaultFocusTarget != null) {
            return defaultFocusTarget;
        }

        for (Node button : actionButtons) {
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
        for (Node button : actionButtons) {
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

        for (Node button : actionButtons) {
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

    /// Returns the default action button when one exists.
    private @Nullable Node defaultButton() {
        for (Node button : actionButtons) {
            if (isDefaultButton(button)) {
                return button;
            }
        }
        return null;
    }

    /// Returns whether a node is one of this dialog pane's action buttons.
    private boolean isDialogButton(Node node) {
        for (Node button : actionButtons) {
            if (button == node) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether a dialog action button is the default action.
    private static boolean isDefaultButton(@Nullable Node button) {
        if (button instanceof M3Button materialButton) {
            return materialButton.isDefaultButton();
        }
        return button != null && ButtonBar.getButtonData(button) != null
                && ButtonBar.getButtonData(button).isDefaultButton();
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
