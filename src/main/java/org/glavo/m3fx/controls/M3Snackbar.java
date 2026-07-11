// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3SnackbarSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 snackbar message.
///
/// `M3Snackbar` displays a short message with an optional action. It is normally shown through
/// [M3SnackbarHost], which handles queueing, timing, entrance motion, and dismissal. The snackbar exposes
/// token-backed container shape, padding, width, line-height, and action-height properties for theme and density
/// integration.
///
/// See [Material Design snackbars](https://m3.material.io/components/snackbar/overview).
@NotNullByDefault
public class M3Snackbar extends Control {
    /// The base style class for M3FX snackbars.
    public static final String STYLE_CLASS = "m3-snackbar";

    /// The default snackbar container shape radius.
    private static final double DEFAULT_CONTAINER_SHAPE = 4.0;

    /// The default snackbar content padding.
    private static final double DEFAULT_CONTENT_PADDING = 16.0;

    /// The default minimum snackbar container width.
    private static final double DEFAULT_CONTAINER_MIN_WIDTH = 344.0;

    /// The default maximum snackbar container width.
    private static final double DEFAULT_CONTAINER_MAX_WIDTH = 672.0;

    /// The default single-line snackbar container height.
    private static final double DEFAULT_SINGLE_LINE_CONTAINER_HEIGHT = 48.0;

    /// The default two-line snackbar container height.
    private static final double DEFAULT_TWO_LINE_CONTAINER_HEIGHT = 68.0;

    /// The default snackbar action button container height.
    private static final double DEFAULT_ACTION_CONTAINER_HEIGHT = 32.0;
    // Backing property for the public snackbar message text API.
    private final StringProperty text = new SimpleStringProperty(this, "text", "");

    // Backing property for the public action button text API.
    private final StringProperty actionText = new SimpleStringProperty(this, "actionText", "");

    // Backing property for the public action handler API.
    private final ObjectProperty<@Nullable EventHandler<ActionEvent>> onAction =
            new SimpleObjectProperty<>(this, "onAction");

    // Backing property for the public container shape token API.
    private @Nullable StyleableDoubleProperty containerShape;

    // Backing property for the public content padding token API.
    private @Nullable StyleableDoubleProperty contentPadding;

    // Backing property for the public minimum container width token API.
    private @Nullable StyleableDoubleProperty containerMinWidth;

    // Backing property for the public maximum container width token API.
    private @Nullable StyleableDoubleProperty containerMaxWidth;

    // Backing property for the public single-line container height token API.
    private @Nullable StyleableDoubleProperty singleLineContainerHeight;

    // Backing property for the public two-line container height token API.
    private @Nullable StyleableDoubleProperty twoLineContainerHeight;

    // Backing property for the public action button container height token API.
    private @Nullable StyleableDoubleProperty actionContainerHeight;

    /// Creates an empty snackbar.
    public M3Snackbar() {
        this("");
    }

    /// Creates a snackbar with message text.
    ///
    /// @param text the snackbar message text
    public M3Snackbar(String text) {
        M3ControlStyles.initialize(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TEXT);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleNode, this::showAccessibleItem);
        this.text.addListener((observable, oldValue, newValue) -> {
            updateAccessibleText();
            notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
        });
        actionText.addListener((observable, oldValue, newValue) -> {
            updateAccessibleText();
            notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
            notifyAccessibleItemsChanged();
        });
        setText(text);
        updateAccessibleText();
    }

    /// Creates a snackbar with message text and action button text.
    ///
    /// @param text the snackbar message text
    /// @param actionText the action button text
    public M3Snackbar(String text, String actionText) {
        this(text);
        setActionText(actionText);
    }

    /// Returns the snackbar message text.
    ///
    /// @return the snackbar message text
    public final String getText() {
        return text.get();
    }

    /// Sets the snackbar message text.
    ///
    /// @param text the snackbar message text
    public final void setText(String text) {
        this.text.set(Objects.requireNonNull(text, "text"));
    }

    /// Returns the snackbar message text property.
    ///
    /// @return the snackbar message text property
    public final StringProperty textProperty() {
        return text;
    }

    /// Returns the action button text.
    ///
    /// @return the action button text
    public final String getActionText() {
        return actionText.get();
    }

    /// Sets the action button text.
    ///
    /// @param actionText the action button text
    public final void setActionText(String actionText) {
        this.actionText.set(Objects.requireNonNull(actionText, "actionText"));
    }

    /// Returns the action button text property.
    ///
    /// @return the action button text property
    public final StringProperty actionTextProperty() {
        return actionText;
    }

    /// Returns the action event handler.
    ///
    /// @return the action event handler, or `null` if none is set
    public final @Nullable EventHandler<ActionEvent> getOnAction() {
        return onAction.get();
    }

    /// Sets the action event handler.
    ///
    /// @param onAction the action event handler, or `null` to clear it
    public final void setOnAction(@Nullable EventHandler<ActionEvent> onAction) {
        this.onAction.set(onAction);
    }

    /// Returns the action event handler property.
    ///
    /// @return the action event handler property
    public final ObjectProperty<@Nullable EventHandler<ActionEvent>> onActionProperty() {
        return onAction;
    }

    /// Returns whether this snackbar currently exposes an action button.
    ///
    /// @return `true` if this snackbar currently exposes an action button
    public final boolean hasAction() {
        return !getActionText().isBlank();
    }

    /// Updates the text exposed to assistive technologies.
    private void updateAccessibleText() {
        String message = getText();
        String action = getActionText();
        setAccessibleText(action.isBlank() ? message : message + " " + action);
    }

    /// Fires this snackbar's action event when it has an enabled action.
    public final void fireAction() {
        if (!isDisabled() && hasAction()) {
            ActionEvent event = new ActionEvent(this, this);
            @Nullable EventHandler<ActionEvent> handler = getOnAction();
            if (handler != null) {
                handler.handle(event);
            }
            if (!event.isConsumed()) {
                Event.fireEvent(this, event);
            }
        }
    }

    /// Returns accessibility attributes for snackbar text and action content.
    ///
    /// @param attribute the requested accessibility attribute
    /// @param parameters the optional attribute parameters
    /// @return the attribute value, or `null` when unavailable
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case FOCUS_NODE -> accessibleFocusNode();
            case ITEM_COUNT -> hasAction() ? 1 : 0;
            case ITEM_AT_INDEX -> actionButtonAt(parameters);
            case TEXT -> accessibleText();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions supported by snackbars with action text.
    ///
    /// @param action the requested accessibility action
    /// @param parameters the optional action parameters
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        if (isDisabled()) {
            super.executeAccessibleAction(action, parameters);
            return;
        }

        switch (action) {
            case FIRE -> fireAction();
            case REQUEST_FOCUS -> focusAccessibleNode();
            case SHOW_ITEM -> showAccessibleItem(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Returns the snackbar container shape radius token.
    ///
    /// @return the snackbar container shape radius token in pixels
    public final double getContainerShape() {
        return containerShape == null ? DEFAULT_CONTAINER_SHAPE : containerShape.get();
    }

    /// Sets the snackbar container shape radius token.
    ///
    /// @param containerShape the snackbar container shape radius token in pixels
    public final void setContainerShape(double containerShape) {
        containerShapeProperty().set(M3Css.nonNegative(containerShape, "containerShape"));
    }

    /// Returns the snackbar container shape radius token property.
    ///
    /// @return the snackbar container shape radius token property
    public final StyleableDoubleProperty containerShapeProperty() {
        if (containerShape == null) {
            containerShape = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTAINER_SHAPE,
                    this,
                    "containerShape",
                    StyleableProperties.CONTAINER_SHAPE,
                    this::requestLayout
            );
        }
        return containerShape;
    }

    /// Returns the snackbar content padding token.
    ///
    /// @return the snackbar content padding token in pixels
    public final double getContentPadding() {
        return contentPadding == null ? DEFAULT_CONTENT_PADDING : contentPadding.get();
    }

    /// Sets the snackbar content padding token.
    ///
    /// @param contentPadding the snackbar content padding token in pixels
    public final void setContentPadding(double contentPadding) {
        contentPaddingProperty().set(M3Css.nonNegative(contentPadding, "contentPadding"));
    }

    /// Returns the snackbar content padding token property.
    ///
    /// @return the snackbar content padding token property
    public final StyleableDoubleProperty contentPaddingProperty() {
        if (contentPadding == null) {
            contentPadding = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTENT_PADDING,
                    this,
                    "contentPadding",
                    StyleableProperties.CONTENT_PADDING,
                    this::requestLayout
            );
        }
        return contentPadding;
    }

    /// Returns the minimum snackbar container width token.
    ///
    /// @return the minimum snackbar container width token in pixels
    public final double getContainerMinWidth() {
        return containerMinWidth == null ? DEFAULT_CONTAINER_MIN_WIDTH : containerMinWidth.get();
    }

    /// Sets the minimum snackbar container width token.
    ///
    /// @param containerMinWidth the minimum snackbar container width token in pixels
    public final void setContainerMinWidth(double containerMinWidth) {
        containerMinWidthProperty().set(M3Css.nonNegative(containerMinWidth, "containerMinWidth"));
    }

    /// Returns the minimum snackbar container width token property.
    ///
    /// @return the minimum snackbar container width token property
    public final StyleableDoubleProperty containerMinWidthProperty() {
        if (containerMinWidth == null) {
            containerMinWidth = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTAINER_MIN_WIDTH,
                    this,
                    "containerMinWidth",
                    StyleableProperties.CONTAINER_MIN_WIDTH,
                    this::requestLayout
            );
        }
        return containerMinWidth;
    }

    /// Returns the maximum snackbar container width token.
    ///
    /// @return the maximum snackbar container width token in pixels
    public final double getContainerMaxWidth() {
        return containerMaxWidth == null ? DEFAULT_CONTAINER_MAX_WIDTH : containerMaxWidth.get();
    }

    /// Sets the maximum snackbar container width token.
    ///
    /// @param containerMaxWidth the maximum snackbar container width token in pixels
    public final void setContainerMaxWidth(double containerMaxWidth) {
        containerMaxWidthProperty().set(M3Css.nonNegative(containerMaxWidth, "containerMaxWidth"));
    }

    /// Returns the maximum snackbar container width token property.
    ///
    /// @return the maximum snackbar container width token property
    public final StyleableDoubleProperty containerMaxWidthProperty() {
        if (containerMaxWidth == null) {
            containerMaxWidth = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTAINER_MAX_WIDTH,
                    this,
                    "containerMaxWidth",
                    StyleableProperties.CONTAINER_MAX_WIDTH,
                    this::requestLayout
            );
        }
        return containerMaxWidth;
    }

    /// Returns the single-line snackbar container height token.
    ///
    /// @return the single-line snackbar container height token in pixels
    public final double getSingleLineContainerHeight() {
        return singleLineContainerHeight == null
                ? DEFAULT_SINGLE_LINE_CONTAINER_HEIGHT
                : singleLineContainerHeight.get();
    }

    /// Sets the single-line snackbar container height token.
    ///
    /// @param singleLineContainerHeight the single-line snackbar container height token in pixels
    public final void setSingleLineContainerHeight(double singleLineContainerHeight) {
        singleLineContainerHeightProperty().set(M3Css.nonNegative(
                singleLineContainerHeight,
                "singleLineContainerHeight"
        ));
    }

    /// Returns the single-line snackbar container height token property.
    ///
    /// @return the single-line snackbar container height token property
    public final StyleableDoubleProperty singleLineContainerHeightProperty() {
        if (singleLineContainerHeight == null) {
            singleLineContainerHeight = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_SINGLE_LINE_CONTAINER_HEIGHT,
                    this,
                    "singleLineContainerHeight",
                    StyleableProperties.SINGLE_LINE_CONTAINER_HEIGHT,
                    this::requestLayout
            );
        }
        return singleLineContainerHeight;
    }

    /// Returns the two-line snackbar container height token.
    ///
    /// @return the two-line snackbar container height token in pixels
    public final double getTwoLineContainerHeight() {
        return twoLineContainerHeight == null
                ? DEFAULT_TWO_LINE_CONTAINER_HEIGHT
                : twoLineContainerHeight.get();
    }

    /// Sets the two-line snackbar container height token.
    ///
    /// @param twoLineContainerHeight the two-line snackbar container height token in pixels
    public final void setTwoLineContainerHeight(double twoLineContainerHeight) {
        twoLineContainerHeightProperty().set(M3Css.nonNegative(
                twoLineContainerHeight,
                "twoLineContainerHeight"
        ));
    }

    /// Returns the two-line snackbar container height token property.
    ///
    /// @return the two-line snackbar container height token property
    public final StyleableDoubleProperty twoLineContainerHeightProperty() {
        if (twoLineContainerHeight == null) {
            twoLineContainerHeight = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_TWO_LINE_CONTAINER_HEIGHT,
                    this,
                    "twoLineContainerHeight",
                    StyleableProperties.TWO_LINE_CONTAINER_HEIGHT,
                    this::requestLayout
            );
        }
        return twoLineContainerHeight;
    }

    /// Returns the action button container height token.
    ///
    /// @return the action button container height token in pixels
    public final double getActionContainerHeight() {
        return actionContainerHeight == null ? DEFAULT_ACTION_CONTAINER_HEIGHT : actionContainerHeight.get();
    }

    /// Sets the action button container height token.
    ///
    /// @param actionContainerHeight the action button container height token in pixels
    public final void setActionContainerHeight(double actionContainerHeight) {
        actionContainerHeightProperty().set(M3Css.nonNegative(actionContainerHeight, "actionContainerHeight"));
    }

    /// Returns the action button container height token property.
    ///
    /// @return the action button container height token property
    public final StyleableDoubleProperty actionContainerHeightProperty() {
        if (actionContainerHeight == null) {
            actionContainerHeight = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_ACTION_CONTAINER_HEIGHT,
                    this,
                    "actionContainerHeight",
                    StyleableProperties.ACTION_CONTAINER_HEIGHT,
                    this::requestLayout
            );
        }
        return actionContainerHeight;
    }

    /// Creates the default snackbar skin.
    ///
    /// @return the default snackbar skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3SnackbarSkin(this);
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the CSS metadata for this control class
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

    /// Returns the user-agent stylesheet for M3FX snackbars.
    ///
    /// @return the snackbar user-agent stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("snackbar.css");
    }

    /// Returns the snackbar text exposed through accessibility queries.
    private String accessibleText() {
        @Nullable String accessibleText = getAccessibleText();
        return accessibleText == null ? "" : accessibleText;
    }

    /// Returns the preferred action focus node when one is rendered.
    private @Nullable Node accessibleFocusNode() {
        @Nullable Node actionButton = renderedActionButton();
        if (actionButton == null) {
            return null;
        }

        @Nullable Node externalTarget = M3Accessible.activeExternalFocusTarget(this, actionButton);
        return externalTarget == null ? actionButton : externalTarget;
    }

    /// Returns the rendered action button when one is visible.
    private @Nullable Node renderedActionButton() {
        @Nullable Node actionButton = lookup(".m3-snackbar-action");
        return actionButton != null && actionButton.isManaged() ? actionButton : null;
    }

    /// Focuses the snackbar action button when it exists.
    ///
    /// @return `true` when the action button accepted focus
    final boolean focusAccessibleNode() {
        if (M3Accessible.showItem(this, accessibleFocusNode())) {
            M3Accessible.notifyFocusNodeChanged(this);
            return true;
        }
        return false;
    }

    /// Returns the action button for an accessibility item index.
    private @Nullable Node actionButtonAt(Object... parameters) {
        int index = M3Accessible.indexParameter(parameters);
        return index == 0 ? renderedActionButton() : null;
    }

    /// Returns the action button referenced by accessibility action parameters.
    private @Nullable Node accessibleActionButton(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            return accessibleFocusNode();
        }
        if (parameters[0] instanceof Number) {
            return actionButtonAt(parameters);
        }

        @Nullable Node actionButton = renderedActionButton();
        if (actionButton == null) {
            return null;
        }
        return M3Accessible.actionItem(actionButton, parameters);
    }

    /// Focuses the snackbar action or delegates to nested action-owned popup targets.
    ///
    /// @param parameters optional accessibility target parameters
    /// @return `true` when focus moved to the action or requested nested target
    final boolean showAccessibleItem(Object... parameters) {
        @Nullable Node actionButton = accessibleActionButton(parameters);
        if (actionButton != null) {
            Object[] targetParameters = actionTargetParameters(parameters);
            if (targetParameters.length > 0
                    && M3Accessible.showAccessibleActionTarget(this, actionButton, targetParameters)) {
                M3Accessible.notifyFocusNodeChanged(this);
                return true;
            }
            if (targetParameters.length > 0
                    && !M3Accessible.parametersContainDirectTarget(parameter -> parameter == actionButton, targetParameters)) {
                return false;
            }
            if (M3Accessible.showItem(this, actionButton)) {
                M3Accessible.notifyFocusNodeChanged(this);
                return true;
            }
        }

        @Nullable Node focusNode = accessibleFocusNode();
        if (parameters.length > 0
                && focusNode != null
                && M3Accessible.showAccessibleActionTarget(this, focusNode, parameters)) {
            M3Accessible.notifyFocusNodeChanged(this);
            return true;
        }
        return false;
    }

    /// Returns nested accessibility target parameters after the action button index is resolved.
    private static Object[] actionTargetParameters(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length <= 1 || !(parameters[0] instanceof Number)) {
            return parameters;
        }

        Object[] targetParameters = new Object[parameters.length - 1];
        System.arraycopy(parameters, 1, targetParameters, 0, targetParameters.length);
        return targetParameters;
    }

    /// Notifies accessibility clients that the action item changed.
    private void notifyAccessibleItemsChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        M3Accessible.notifyFocusNodeChanged(this);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
    }

    /// CSS metadata for M3FX snackbar component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the container shape token.
        private static final CssMetaData<M3Snackbar, Number> CONTAINER_SHAPE =
                new CssMetaData<>("-m3-container-shape", SizeConverter.getInstance(), DEFAULT_CONTAINER_SHAPE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Snackbar control) {
                        return M3Css.isSettable(control.containerShapeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Snackbar control) {
                        return control.containerShapeProperty();
                    }
                };

        /// CSS metadata for the content padding token.
        private static final CssMetaData<M3Snackbar, Number> CONTENT_PADDING =
                new CssMetaData<>("-m3-content-padding", SizeConverter.getInstance(), DEFAULT_CONTENT_PADDING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Snackbar control) {
                        return M3Css.isSettable(control.contentPaddingProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Snackbar control) {
                        return control.contentPaddingProperty();
                    }
                };

        /// CSS metadata for the minimum container width token.
        private static final CssMetaData<M3Snackbar, Number> CONTAINER_MIN_WIDTH =
                new CssMetaData<>("-m3-container-min-width", SizeConverter.getInstance(), DEFAULT_CONTAINER_MIN_WIDTH) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Snackbar control) {
                        return M3Css.isSettable(control.containerMinWidthProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Snackbar control) {
                        return control.containerMinWidthProperty();
                    }
                };

        /// CSS metadata for the maximum container width token.
        private static final CssMetaData<M3Snackbar, Number> CONTAINER_MAX_WIDTH =
                new CssMetaData<>("-m3-container-max-width", SizeConverter.getInstance(), DEFAULT_CONTAINER_MAX_WIDTH) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Snackbar control) {
                        return M3Css.isSettable(control.containerMaxWidthProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Snackbar control) {
                        return control.containerMaxWidthProperty();
                    }
                };

        /// CSS metadata for the single-line container height token.
        private static final CssMetaData<M3Snackbar, Number> SINGLE_LINE_CONTAINER_HEIGHT =
                new CssMetaData<>(
                        "-m3-single-line-container-height",
                        SizeConverter.getInstance(),
                        DEFAULT_SINGLE_LINE_CONTAINER_HEIGHT
                ) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Snackbar control) {
                        return M3Css.isSettable(control.singleLineContainerHeightProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Snackbar control) {
                        return control.singleLineContainerHeightProperty();
                    }
                };

        /// CSS metadata for the two-line container height token.
        private static final CssMetaData<M3Snackbar, Number> TWO_LINE_CONTAINER_HEIGHT =
                new CssMetaData<>(
                        "-m3-two-line-container-height",
                        SizeConverter.getInstance(),
                        DEFAULT_TWO_LINE_CONTAINER_HEIGHT
                ) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Snackbar control) {
                        return M3Css.isSettable(control.twoLineContainerHeightProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Snackbar control) {
                        return control.twoLineContainerHeightProperty();
                    }
                };

        /// CSS metadata for the action button container height token.
        private static final CssMetaData<M3Snackbar, Number> ACTION_CONTAINER_HEIGHT =
                new CssMetaData<>(
                        "-m3-action-container-height",
                        SizeConverter.getInstance(),
                        DEFAULT_ACTION_CONTAINER_HEIGHT
                ) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Snackbar control) {
                        return M3Css.isSettable(control.actionContainerHeightProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Snackbar control) {
                        return control.actionContainerHeightProperty();
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
            styleables.add(SINGLE_LINE_CONTAINER_HEIGHT);
            styleables.add(TWO_LINE_CONTAINER_HEIGHT);
            styleables.add(ACTION_CONTAINER_HEIGHT);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
