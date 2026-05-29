// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.collections.ListChangeListener;
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
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

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
    /// The base style class for m3fx dialog panes.
    public static final String STYLE_CLASS = "m3-dialog-pane";

    /// The style class applied to the dialog action button bar.
    public static final String BUTTON_BAR_STYLE_CLASS = "m3-dialog-button-bar";

    /// The style class applied to dialog action buttons.
    public static final String BUTTON_STYLE_CLASS = "m3-dialog-button";

    /// The default dialog container shape radius.
    private static final double DEFAULT_CONTAINER_SHAPE = 28.0;

    /// The default dialog content padding.
    private static final double DEFAULT_CONTENT_PADDING = 24.0;

    // The styleable dialog container shape token.
    private @Nullable StyleableDoubleProperty containerShape;

    // The styleable dialog content padding token.
    private @Nullable StyleableDoubleProperty contentPadding;

    /// Reports focused dialog content or action changes to accessibility clients.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, this::currentOrFirstFocusableItem);

    /// Creates a dialog pane.
    public M3DialogPane() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.DIALOG);
        headerTextProperty().addListener((observable, oldValue, newValue) -> updateAccessibleText());
        contentTextProperty().addListener((observable, oldValue, newValue) -> updateAccessibleText());
        contentProperty().addListener((observable, oldValue, newValue) -> notifyAccessibleItemsChanged());
        getButtonTypes().addListener((ListChangeListener<ButtonType>) change -> notifyAccessibleItemsChanged());
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
            containerShape = new StyleableDoubleProperty(DEFAULT_CONTAINER_SHAPE) {
                /// Validates updated shape tokens.
                @Override
                protected void invalidated() {
                    set(M3Css.nonNegative(get(), "containerShape"));
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3DialogPane.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "containerShape";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3DialogPane, Number> getCssMetaData() {
                    return StyleableProperties.CONTAINER_SHAPE;
                }
            };
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
            contentPadding = new StyleableDoubleProperty(DEFAULT_CONTENT_PADDING) {
                /// Applies updated metrics when the token changes.
                @Override
                protected void invalidated() {
                    set(M3Css.nonNegative(get(), "contentPadding"));
                    updateMetrics();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3DialogPane.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "contentPadding";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3DialogPane, Number> getCssMetaData() {
                    return StyleableProperties.CONTENT_PADDING;
                }
            };
        }
        return contentPadding;
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
        switch (action) {
            case REQUEST_FOCUS -> {
                M3Accessible.showItem(firstFocusableItem());
                notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
                focusNotifier.refresh();
            }
            case SHOW_ITEM -> {
                M3Accessible.showItem(accessibleActionItem(parameters));
                notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
                focusNotifier.refresh();
            }
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Creates the dialog action button bar.
    @Override
    protected Node createButtonBar() {
        Node buttonBar = super.createButtonBar();
        buttonBar.getStyleClass().add(BUTTON_BAR_STYLE_CLASS);
        if (buttonBar instanceof ButtonBar materialButtonBar) {
            materialButtonBar.setButtonMinWidth(0.0);
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

    /// Returns the user-agent stylesheet for m3fx dialog panes.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("dialog.css");
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double padding = getContentPadding();
        setPadding(new Insets(padding));
    }

    /// Updates the accessibility label from the dialog header and content text.
    private void updateAccessibleText() {
        StringBuilder builder = new StringBuilder();
        appendAccessibleText(builder, getHeaderText());
        appendAccessibleText(builder, getContentText());
        setAccessibleText(builder.length() == 0 ? null : builder.toString());
        notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
    }

    /// Notifies accessibility clients that dialog content or actions changed.
    private void notifyAccessibleItemsChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.CONTENTS);
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
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

        return buttonAt(index);
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
        @Nullable Node contentFocusTarget = M3Accessible.accessibleFocusTarget(content);
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
        @Nullable Node externalContentFocusTarget = activeExternalContentFocusTarget(content);
        if (externalContentFocusTarget != null) {
            return externalContentFocusTarget;
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

    /// Returns an actively focused popup target exposed by dialog content when it lives outside this pane.
    private @Nullable Node activeExternalContentFocusTarget(@Nullable Node content) {
        @Nullable Node contentFocusTarget = M3Accessible.accessibleFocusTarget(content);
        if (contentFocusTarget == null || M3Accessible.containsNode(this, contentFocusTarget)) {
            return null;
        }
        @Nullable Scene focusTargetScene = contentFocusTarget.getScene();
        @Nullable Node focusOwner = focusTargetScene == null ? null : focusTargetScene.getFocusOwner();
        if (focusOwner != null && M3Accessible.containsNode(contentFocusTarget, focusOwner)) {
            return contentFocusTarget;
        }
        return null;
    }

    /// Returns the focus owner when it is inside one dialog item, falling back to the item's focus target.
    private static @Nullable Node containedFocusTarget(@Nullable Node item, Node focusOwner) {
        @Nullable Node itemFocusTarget = M3Accessible.focusTarget(item);
        if (itemFocusTarget == null || !M3Accessible.containsNode(item, focusOwner)) {
            return null;
        }
        return M3Accessible.canReach(focusOwner) ? focusOwner : itemFocusTarget;
    }

    /// Returns the action button at an index in button type order.
    private @Nullable Node buttonAt(int index) {
        return index >= 0 && index < getButtonTypes().size() ? lookupButton(getButtonTypes().get(index)) : null;
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
        if (builder.length() > 0) {
            builder.append(' ');
        }
        builder.append(text);
    }

    /// CSS metadata for m3fx dialog pane component tokens.
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

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(DialogPane.getClassCssMetaData());
            styleables.add(CONTAINER_SHAPE);
            styleables.add(CONTENT_PADDING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
