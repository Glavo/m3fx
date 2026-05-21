// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3Motion;
import org.glavo.m3fx.controls.M3Badge;
import org.glavo.m3fx.controls.M3NavigationItem;
import org.glavo.m3fx.internal.M3Animation;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3NavigationItem].
@NotNullByDefault
public class M3NavigationItemSkin extends SkinBase<M3NavigationItem> {
    /// The duration used by selected indicator transitions.
    private static final Duration INDICATOR_DURATION = M3Motion.SHORT4;

    /// The selected indicator hidden scale.
    private static final double HIDDEN_INDICATOR_SCALE = 0.72;

    /// The bounded state layer used for hover, focus, pressed, and ripple feedback.
    private final M3StateLayer stateLayer = new M3StateLayer();

    /// The visual content stack.
    private final VBox content = new VBox();

    /// The icon and selected indicator slot.
    private final StackPane iconContainer = new StackPane();

    /// The selected indicator background.
    private final Region indicator = new Region();

    /// The graphic content slot.
    private final StackPane graphicContainer = new StackPane();

    /// The optional badge slot.
    private final StackPane badgeContainer = new StackPane();

    /// The item text label.
    private final Label label = new Label();

    /// The selected indicator animation timeline.
    private final Timeline indicatorAnimation = new Timeline();

    /// Handles primary mouse presses.
    private final EventHandler<MouseEvent> mousePressedHandler = this::handleMousePressed;

    /// Handles primary mouse releases.
    private final EventHandler<MouseEvent> mouseReleasedHandler = this::handleMouseReleased;

    /// Handles pointer entry while a mouse press is active.
    private final EventHandler<MouseEvent> mouseEnteredHandler = this::handleMouseEntered;

    /// Handles pointer exit while a mouse press is active.
    private final EventHandler<MouseEvent> mouseExitedHandler = this::handleMouseExited;

    /// Handles keyboard activation presses.
    private final EventHandler<KeyEvent> keyPressedHandler = this::handleKeyPressed;

    /// Handles keyboard activation releases.
    private final EventHandler<KeyEvent> keyReleasedHandler = this::handleKeyReleased;

    /// Keeps the state layer sized when controls are resized outside a layout pass.
    private final InvalidationListener stateLayerLayoutInvalidation = observable -> layoutStateLayer();

    /// Updates the displayed label text.
    private final ChangeListener<@Nullable String> textListener =
            (observable, oldValue, newValue) -> updateText(newValue);

    /// Updates the displayed graphic node.
    private final ChangeListener<@Nullable Node> graphicListener =
            (observable, oldValue, newValue) -> updateGraphic(newValue);

    /// Updates the displayed badge.
    private final ChangeListener<@Nullable M3Badge> badgeListener =
            (observable, oldValue, newValue) -> updateBadge(newValue);

    /// Animates the selected indicator when selection changes.
    private final ChangeListener<Boolean> selectedListener =
            (observable, oldValue, newValue) -> animateSelectedIndicator(newValue);

    /// Resets transient feedback when the control becomes disabled.
    private final ChangeListener<Boolean> disabledListener = (observable, oldValue, newValue) -> {
        if (newValue) {
            resetInteractionState();
        }
    };

    /// Whether the current interaction was started by a primary mouse press.
    private boolean mousePressed;

    /// Whether the space key currently owns the armed state.
    private boolean spaceKeyPressed;

    /// Creates a navigation item skin.
    public M3NavigationItemSkin(M3NavigationItem control) {
        super(control);
        content.getStyleClass().add("m3-navigation-item-content");
        iconContainer.getStyleClass().add("m3-navigation-item-icon-container");
        indicator.getStyleClass().add("m3-navigation-item-indicator");
        graphicContainer.getStyleClass().add("m3-navigation-item-graphic");
        badgeContainer.getStyleClass().add("m3-navigation-item-badge-container");
        label.getStyleClass().add("m3-navigation-item-label");

        content.setManaged(false);
        content.setAlignment(Pos.CENTER);
        iconContainer.setAlignment(Pos.CENTER);
        graphicContainer.setAlignment(Pos.CENTER);
        badgeContainer.setAlignment(Pos.TOP_RIGHT);
        badgeContainer.setMouseTransparent(true);
        indicator.setManaged(false);
        indicator.setMouseTransparent(true);
        label.setMouseTransparent(true);

        iconContainer.getChildren().addAll(indicator, graphicContainer, badgeContainer);
        content.getChildren().addAll(iconContainer, label);
        getChildren().addAll(stateLayer, content);

        stateLayer.installStateTransitions(control);
        updateText(control.getText());
        updateGraphic(control.getGraphic());
        updateBadge(control.getBadge());
        updateIndicatorImmediate(control.isSelected());
        installInteractionHandlers(control);
        control.textProperty().addListener(textListener);
        control.graphicProperty().addListener(graphicListener);
        control.badgeProperty().addListener(badgeListener);
        control.widthProperty().addListener(stateLayerLayoutInvalidation);
        control.heightProperty().addListener(stateLayerLayoutInvalidation);
        control.layoutBoundsProperty().addListener(stateLayerLayoutInvalidation);
        control.selectedProperty().addListener(selectedListener);
        control.disabledProperty().addListener(disabledListener);
        layoutStateLayer();
    }

    /// Stops animations and removes listeners before the skin is disposed.
    @Override
    public void dispose() {
        M3NavigationItem item = getSkinnable();
        indicatorAnimation.stop();
        stateLayer.uninstallStateTransitions();
        item.textProperty().removeListener(textListener);
        item.graphicProperty().removeListener(graphicListener);
        item.badgeProperty().removeListener(badgeListener);
        item.widthProperty().removeListener(stateLayerLayoutInvalidation);
        item.heightProperty().removeListener(stateLayerLayoutInvalidation);
        item.layoutBoundsProperty().removeListener(stateLayerLayoutInvalidation);
        item.selectedProperty().removeListener(selectedListener);
        item.disabledProperty().removeListener(disabledListener);
        uninstallInteractionHandlers(item);
        resetInteractionState();
        super.dispose();
    }

    /// Lays out the item content and state layer.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        M3NavigationItem item = getSkinnable();
        double indicatorWidth = item.getIndicatorWidth();
        double indicatorHeight = item.getIndicatorHeight();
        double indicatorShape = item.getIndicatorShape();

        content.setSpacing(item.getContentSpacing());
        iconContainer.setMinSize(indicatorWidth, indicatorHeight);
        iconContainer.setPrefSize(indicatorWidth, indicatorHeight);
        iconContainer.setMaxSize(indicatorWidth, indicatorHeight);
        indicator.resizeRelocate(0.0, 0.0, indicatorWidth, indicatorHeight);
        indicator.setStyle("-fx-background-radius: " + indicatorShape + "px;");
        content.resizeRelocate(x, y, width, height);
        layoutStateLayer();
    }

    /// Installs mouse and keyboard behavior handlers.
    private void installInteractionHandlers(M3NavigationItem item) {
        item.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        item.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        item.addEventHandler(MouseEvent.MOUSE_ENTERED, mouseEnteredHandler);
        item.addEventHandler(MouseEvent.MOUSE_EXITED, mouseExitedHandler);
        item.addEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        item.addEventHandler(KeyEvent.KEY_RELEASED, keyReleasedHandler);
    }

    /// Removes mouse and keyboard behavior handlers.
    private void uninstallInteractionHandlers(M3NavigationItem item) {
        item.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        item.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        item.removeEventHandler(MouseEvent.MOUSE_ENTERED, mouseEnteredHandler);
        item.removeEventHandler(MouseEvent.MOUSE_EXITED, mouseExitedHandler);
        item.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        item.removeEventHandler(KeyEvent.KEY_RELEASED, keyReleasedHandler);
    }

    /// Arms the item on primary mouse press.
    private void handleMousePressed(MouseEvent event) {
        M3NavigationItem item = getSkinnable();
        if (item.isDisabled() || event.getButton() != MouseButton.PRIMARY) {
            return;
        }

        mousePressed = true;
        if (item.isFocusTraversable()) {
            item.requestFocus();
        }
        layoutStateLayer();
        stateLayer.playRipple(event.getX(), event.getY());
        item.arm();
        event.consume();
    }

    /// Fires the item when a primary mouse press is released inside the control.
    private void handleMouseReleased(MouseEvent event) {
        M3NavigationItem item = getSkinnable();
        if (!mousePressed || event.getButton() != MouseButton.PRIMARY) {
            return;
        }

        boolean shouldFire = item.isArmed() && item.contains(event.getX(), event.getY());
        mousePressed = false;
        stateLayer.releaseRipple();
        item.disarm();
        if (shouldFire) {
            item.fire();
        }
        event.consume();
    }

    /// Re-arms the item when a pressed pointer re-enters the control.
    private void handleMouseEntered(MouseEvent event) {
        M3NavigationItem item = getSkinnable();
        if (mousePressed && !item.isDisabled()) {
            item.arm();
            event.consume();
        }
    }

    /// Disarms the item when a pressed pointer exits the control.
    private void handleMouseExited(MouseEvent event) {
        M3NavigationItem item = getSkinnable();
        if (mousePressed && !item.isDisabled()) {
            item.disarm();
            event.consume();
        }
    }

    /// Handles keyboard activation for enter and space.
    private void handleKeyPressed(KeyEvent event) {
        M3NavigationItem item = getSkinnable();
        if (item.isDisabled()) {
            return;
        }

        if (event.getCode() == KeyCode.SPACE) {
            if (!spaceKeyPressed) {
                spaceKeyPressed = true;
                layoutStateLayer();
                stateLayer.playCenteredRipple();
                item.arm();
            }
            event.consume();
        } else if (event.getCode() == KeyCode.ENTER) {
            layoutStateLayer();
            stateLayer.playCenteredRipple();
            stateLayer.releaseRipple();
            item.fire();
            event.consume();
        }
    }

    /// Fires the item when a space key activation is released.
    private void handleKeyReleased(KeyEvent event) {
        M3NavigationItem item = getSkinnable();
        if (event.getCode() != KeyCode.SPACE || !spaceKeyPressed) {
            return;
        }

        boolean shouldFire = item.isArmed() && !item.isDisabled();
        spaceKeyPressed = false;
        stateLayer.releaseRipple();
        item.disarm();
        if (shouldFire) {
            item.fire();
        }
        event.consume();
    }

    /// Updates the label from the current item text.
    private void updateText(@Nullable String text) {
        boolean hasText = text != null && !text.isEmpty();
        label.setText(hasText ? text : "");
        label.setVisible(hasText);
        label.setManaged(hasText);
    }

    /// Updates the graphic slot from the current item graphic.
    private void updateGraphic(@Nullable Node graphic) {
        graphicContainer.getChildren().clear();
        if (graphic != null) {
            if (!graphic.getStyleClass().contains("m3-navigation-item-graphic-node")) {
                graphic.getStyleClass().add("m3-navigation-item-graphic-node");
            }
            graphicContainer.getChildren().add(graphic);
        }
    }

    /// Updates the badge slot from the current item badge.
    private void updateBadge(@Nullable M3Badge badge) {
        badgeContainer.getChildren().clear();
        if (badge != null) {
            if (!badge.getStyleClass().contains("m3-navigation-item-badge")) {
                badge.getStyleClass().add("m3-navigation-item-badge");
            }
            StackPane.setAlignment(badge, Pos.TOP_RIGHT);
            badgeContainer.getChildren().add(badge);
        }
    }

    /// Animates the selected indicator to the requested state.
    private void animateSelectedIndicator(boolean selected) {
        double targetOpacity = selected ? 1.0 : 0.0;
        double targetScale = selected ? 1.0 : HIDDEN_INDICATOR_SCALE;
        indicatorAnimation.stop();
        indicatorAnimation.getKeyFrames().setAll(
                new KeyFrame(
                        INDICATOR_DURATION,
                        new KeyValue(indicator.opacityProperty(), targetOpacity, M3Motion.STANDARD),
                        new KeyValue(indicator.scaleXProperty(), targetScale, M3Motion.STANDARD)
                )
        );
        M3Animation.playFromStart(getSkinnable(), indicatorAnimation);
    }

    /// Updates the selected indicator without animation.
    private void updateIndicatorImmediate(boolean selected) {
        indicator.setOpacity(selected ? 1.0 : 0.0);
        indicator.setScaleX(selected ? 1.0 : HIDDEN_INDICATOR_SCALE);
    }

    /// Clears armed state and transient feedback.
    private void resetInteractionState() {
        M3NavigationItem item = getSkinnable();
        mousePressed = false;
        spaceKeyPressed = false;
        stateLayer.reset();
        item.disarm();
    }

    /// Lays out the state layer to cover the full item surface.
    private void layoutStateLayer() {
        M3NavigationItem item = getSkinnable();
        double width = item.getWidth();
        double height = item.getHeight();
        if (width <= 0.0) {
            width = item.getLayoutBounds().getWidth();
        }
        if (height <= 0.0) {
            height = item.getLayoutBounds().getHeight();
        }
        stateLayer.layoutLayer(0.0, 0.0, width, height, item.getIndicatorShape());
    }
}
