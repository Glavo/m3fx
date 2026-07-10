// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.collections.ListChangeListener;
import javafx.css.CssMetaData;
import javafx.css.StyleOrigin;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Insets;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.glavo.m3fx.internal.M3FocusTraversal;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.M3ModalFocusTrap;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 dialog pane.
///
/// `M3DialogPane` is the content container used by [M3Dialog]. It keeps JavaFX [DialogPane] button management,
/// content, header, and expandable-content behavior while applying Material container shape, content padding,
/// button styling, and accessibility defaults. The pane can also be installed on a standard JavaFX dialog when
/// the application needs to preserve a custom dialog subclass.
///
/// See [Material Design dialogs](https://m3.material.io/components/dialogs/overview).
@NotNullByDefault
public class M3DialogPane extends DialogPane {
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

    /// The style class applied to Material icons used as dialog graphics.
    private static final String GRAPHIC_ICON_STYLE_CLASS = "m3-dialog-graphic-icon";

    /// The property key used to restore caller-provided dialog graphic icon styles.
    private static final String GRAPHIC_ICON_BASE_STYLE_KEY =
            M3DialogPane.class.getName() + ".graphicIconBaseStyle";

    /// The inline style declaration managed by the container shape token.
    private @Nullable String managedContainerShapeStyle;

    /// Whether the current style change is produced by managed metric synchronization.
    private boolean updatingManagedStyle;

    /// Whether the managed container shape style must be synchronized before the next layout pass.
    private boolean containerShapeStyleDirty;

    // The styleable dialog container shape token.
    private @Nullable StyleableDoubleProperty containerShape;

    // The styleable dialog content padding token.
    private @Nullable StyleableDoubleProperty contentPadding;

    // The styleable minimum dialog container width token.
    private @Nullable StyleableDoubleProperty containerMinWidth;

    // The styleable maximum dialog container width token.
    private @Nullable StyleableDoubleProperty containerMaxWidth;

    // The styleable spacing between dialog action buttons.
    private @Nullable StyleableDoubleProperty actionSpacing;

    // The styleable dialog graphic icon size token.
    private @Nullable StyleableDoubleProperty iconSize;

    /// The internal dialog action button bar.
    private @Nullable ButtonBar buttonBar;

    /// Reports focused dialog content or action changes to accessibility clients.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, this::currentOrFirstFocusableItem);

    /// Keeps keyboard traversal inside this dialog pane while it is hosted by a modal window.
    private final M3ModalFocusTrap focusTrap = new M3ModalFocusTrap(
            this,
            this::isFocusTrapActive,
            this::focusTrapTargets,
            null
    );

    /// Creates a dialog pane.
    public M3DialogPane() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.DIALOG);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleNode, this::showAccessibleItem);
        headerTextProperty().addListener((observable, oldValue, newValue) -> updateAccessibleText());
        contentTextProperty().addListener((observable, oldValue, newValue) -> updateAccessibleText());
        contentProperty().addListener((observable, oldValue, newValue) -> notifyAccessibleItemsChanged());
        graphicProperty().addListener((observable, oldValue, newValue) -> updateGraphicMetrics(oldValue, newValue));
        getButtonTypes().addListener((ListChangeListener<ButtonType>) change -> notifyAccessibleItemsChanged());
        styleProperty().addListener((observable, oldValue, newValue) -> {
            if (!updatingManagedStyle && managedContainerShapeStyle != null) {
                requestContainerShapeStyleSync();
            }
        });
        visibleProperty().addListener((observable, oldValue, newValue) -> focusTrap.update());
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleActionNavigationKey);
        focusTrap.install();
        focusNotifier.start();
        updateMetrics();
        updateAccessibleText();
    }

    /// Returns the dialog container shape radius token.
    ///
    /// @return the dialog container shape radius token
    public final double getContainerShape() {
        return containerShape == null ? DEFAULT_CONTAINER_SHAPE : containerShape.get();
    }

    /// Sets the dialog container shape radius token.
    ///
    /// @param containerShape the dialog container shape radius token
    public final void setContainerShape(double containerShape) {
        containerShapeProperty().set(M3Css.nonNegative(containerShape, "containerShape"));
    }

    /// Returns the dialog container shape radius token property.
    ///
    /// @return the dialog container shape radius token property
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

    /// Returns the dialog content padding token.
    ///
    /// @return the dialog content padding token
    public final double getContentPadding() {
        return contentPadding == null ? DEFAULT_CONTENT_PADDING : contentPadding.get();
    }

    /// Sets the dialog content padding token.
    ///
    /// @param contentPadding the dialog content padding token
    public final void setContentPadding(double contentPadding) {
        contentPaddingProperty().set(M3Css.nonNegative(contentPadding, "contentPadding"));
    }

    /// Returns the dialog content padding token property.
    ///
    /// @return the dialog content padding token property
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

    /// Returns the minimum dialog container width token.
    ///
    /// @return the minimum dialog container width token
    public final double getContainerMinWidth() {
        return containerMinWidth == null ? DEFAULT_CONTAINER_MIN_WIDTH : containerMinWidth.get();
    }

    /// Sets the minimum dialog container width token.
    ///
    /// @param containerMinWidth the minimum dialog container width token
    public final void setContainerMinWidth(double containerMinWidth) {
        containerMinWidthProperty().set(M3Css.nonNegative(containerMinWidth, "containerMinWidth"));
    }

    /// Returns the minimum dialog container width token property.
    ///
    /// @return the minimum dialog container width token property
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

    /// Returns the maximum dialog container width token.
    ///
    /// @return the maximum dialog container width token
    public final double getContainerMaxWidth() {
        return containerMaxWidth == null ? DEFAULT_CONTAINER_MAX_WIDTH : containerMaxWidth.get();
    }

    /// Sets the maximum dialog container width token.
    ///
    /// @param containerMaxWidth the maximum dialog container width token
    public final void setContainerMaxWidth(double containerMaxWidth) {
        containerMaxWidthProperty().set(M3Css.nonNegative(containerMaxWidth, "containerMaxWidth"));
    }

    /// Returns the maximum dialog container width token property.
    ///
    /// @return the maximum dialog container width token property
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

    /// Returns the spacing between dialog action buttons.
    ///
    /// @return the spacing between dialog action buttons
    public final double getActionSpacing() {
        return actionSpacing == null ? DEFAULT_ACTION_SPACING : actionSpacing.get();
    }

    /// Sets the spacing between dialog action buttons.
    ///
    /// @param actionSpacing the spacing between dialog action buttons
    public final void setActionSpacing(double actionSpacing) {
        actionSpacingProperty().set(M3Css.nonNegative(actionSpacing, "actionSpacing"));
    }

    /// Returns the spacing property for dialog action buttons.
    ///
    /// @return the spacing property for dialog action buttons
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

    /// Returns the dialog graphic icon size token.
    ///
    /// @return the dialog graphic icon size token
    public final double getIconSize() {
        return iconSize == null ? DEFAULT_ICON_SIZE : iconSize.get();
    }

    /// Sets the dialog graphic icon size token.
    ///
    /// @param iconSize the dialog graphic icon size token
    public final void setIconSize(double iconSize) {
        iconSizeProperty().set(M3Css.nonNegative(iconSize, "iconSize"));
    }

    /// Returns the dialog graphic icon size token property.
    ///
    /// @return the dialog graphic icon size token property
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
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }

    /// Returns accessibility attributes for the dialog text.
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

    /// Creates the dialog action button bar.
    @Override
    protected Node createButtonBar() {
        Node buttonBar = super.createButtonBar();
        buttonBar.getStyleClass().add(BUTTON_BAR_STYLE_CLASS);
        if (buttonBar instanceof ButtonBar materialButtonBar) {
            this.buttonBar = materialButtonBar;
            materialButtonBar.setButtonMinWidth(0.0);
            materialButtonBar.setButtonOrder(ButtonBar.BUTTON_ORDER_NONE);
            synchronizeActionSpacing();
        }
        return buttonBar;
    }

    /// Creates a Material action button for a dialog button type.
    @Override
    protected Node createButton(ButtonType buttonType) {
        Node sourceNode = super.createButton(buttonType);
        if (!(sourceNode instanceof ButtonBase sourceButton)) {
            return sourceNode;
        }

        M3Button button = new M3Button(buttonType.getText());
        button.getStyleClass().add(BUTTON_STYLE_CLASS);
        button.setVariant(M3ButtonVariant.TEXT);
        button.setOnAction(sourceButton.getOnAction());
        ButtonBar.setButtonData(button, ButtonBar.getButtonData(sourceNode));
        ButtonBar.setButtonUniformSize(button, ButtonBar.isButtonUniformSize(sourceNode));
        if (sourceButton instanceof Button sourcePlainButton) {
            button.setDefaultButton(sourcePlainButton.isDefaultButton());
            button.setCancelButton(sourcePlainButton.isCancelButton());
        } else {
            ButtonBar.ButtonData buttonData = buttonType.getButtonData();
            button.setDefaultButton(buttonData != null && buttonData.isDefaultButton());
            button.setCancelButton(buttonData != null && buttonData.isCancelButton());
        }
        return button;
    }

    /// Handles keyboard traversal between dialog action buttons.
    private void handleActionNavigationKey(KeyEvent event) {
        if (M3FocusTraversal.focusOwnerInside(this, getContent())) {
            return;
        }

        M3FocusTraversal.handleHorizontalKeyFocus(
                this,
                event,
                M3FocusTraversal.focusTargets(actionButtons())
        );
    }

    /// Returns whether dialog keyboard focus should currently stay inside this pane.
    private boolean isFocusTrapActive() {
        if (!M3Accessible.canReach(this)) {
            return false;
        }

        @Nullable Scene scene = getScene();
        @Nullable Window window = scene == null ? null : scene.getWindow();
        return window instanceof Stage stage && stage.getModality() != Modality.NONE;
    }

    /// Returns the focus targets contained by this dialog pane in traversal order.
    private List<Node> focusTrapTargets() {
        ArrayList<Node> targets = new ArrayList<>();
        targets.addAll(M3FocusTraversal.focusTargetsInReachableTree(getContent()));
        for (Node button : actionButtons()) {
            targets.addAll(M3FocusTraversal.focusTargetsInReachableTree(button));
        }
        return List.copyOf(targets);
    }

    /// Returns the user-agent stylesheet for M3FX dialog panes.
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
        double padding = getContentPadding();
        double minWidth = getContainerMinWidth();
        M3Css.setPaddingIfUnbound(this, new Insets(padding));
        M3Css.setMinWidthIfUnbound(this, minWidth);
        M3Css.setMaxWidthIfUnbound(this, Math.max(minWidth, getContainerMaxWidth()));
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
        if (oldGraphic instanceof M3Icon oldIcon) {
            restoreGraphicIconStyle(oldIcon);
        }
        if (!(newGraphic instanceof M3Icon icon)) {
            return;
        }

        applyGraphicIconStyle(icon);
        if (M3Css.isSettable(icon.iconSizeProperty())) {
            icon.iconSizeProperty().applyStyle(StyleOrigin.USER_AGENT, getIconSize());
        }
    }

    /// Applies dialog-specific style classes and managed inline color to a graphic icon.
    private static void applyGraphicIconStyle(M3Icon icon) {
        if (!icon.getStyleClass().contains(GRAPHIC_ICON_STYLE_CLASS)) {
            icon.getStyleClass().add(GRAPHIC_ICON_STYLE_CLASS);
        }
        Object baseStyle = icon.getProperties().computeIfAbsent(GRAPHIC_ICON_BASE_STYLE_KEY, key -> icon.getStyle());
        String callerStyle = baseStyle instanceof String style ? style : "";
        icon.setStyle(mergeStyles("-fx-text-fill: -m3-color-secondary;", callerStyle));
    }

    /// Restores the caller-provided style after an icon leaves the dialog graphic slot.
    private static void restoreGraphicIconStyle(M3Icon icon) {
        icon.getStyleClass().remove(GRAPHIC_ICON_STYLE_CLASS);
        Object baseStyle = icon.getProperties().remove(GRAPHIC_ICON_BASE_STYLE_KEY);
        if (baseStyle instanceof String style) {
            icon.setStyle(style);
        }
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
            return Long.toString((long) value) + "px";
        }
        return Double.toString(value) + "px";
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

        if (index < 0 || index >= getButtonTypes().size()) {
            return null;
        }
        return lookupButton(getButtonTypes().get(index));
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
            for (ButtonType buttonType : getButtonTypes()) {
                if (M3Accessible.showAccessibleActionTarget(this, lookupButton(buttonType), parameters)) {
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
            for (ButtonType buttonType : getButtonTypes()) {
                @Nullable Node button = lookupButton(buttonType);
                if (button != null && (M3Accessible.containsNode(button, node)
                        || M3Accessible.containsAccessibleActionTarget(button, node))) {
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

        for (ButtonType buttonType : getButtonTypes()) {
            @Nullable Node button = lookupButton(buttonType);
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
        if (item instanceof M3SnackbarHost) {
            return item;
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
        for (ButtonType buttonType : getButtonTypes()) {
            @Nullable Node externalButtonFocusTarget =
                    M3Accessible.activeExternalFocusTarget(this, lookupButton(buttonType));
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

        for (ButtonType buttonType : getButtonTypes()) {
            @Nullable Node buttonFocusTarget = containedFocusTarget(lookupButton(buttonType), focusOwner);
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

    /// Returns the current dialog action buttons in button type order.
    private @Unmodifiable List<Node> actionButtons() {
        List<Node> buttons = new ArrayList<>();
        for (ButtonType buttonType : getButtonTypes()) {
            @Nullable Node button = lookupButton(buttonType);
            if (button != null) {
                buttons.add(button);
            }
        }
        return List.copyOf(buttons);
    }

    /// Returns the default action button when one exists.
    private @Nullable Node defaultButton() {
        for (ButtonType buttonType : getButtonTypes()) {
            @Nullable Node button = lookupButton(buttonType);
            if (isDefaultButton(button)) {
                return button;
            }
        }
        return null;
    }

    /// Returns whether a node is one of this dialog pane's action buttons.
    private boolean isDialogButton(Node node) {
        for (ButtonType buttonType : getButtonTypes()) {
            if (lookupButton(buttonType) == node) {
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
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(DialogPane.getClassCssMetaData());
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
