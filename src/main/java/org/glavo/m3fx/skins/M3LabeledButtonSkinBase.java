// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.skin.LabeledSkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonGroup;
import org.glavo.m3fx.controls.M3Chip;
import org.glavo.m3fx.controls.M3DatePicker;
import org.glavo.m3fx.controls.M3FloatingActionButton;
import org.glavo.m3fx.controls.M3IconToggleButton;
import org.glavo.m3fx.controls.M3SegmentedButton;
import org.glavo.m3fx.controls.M3SegmentedButtonGroup;
import org.glavo.m3fx.controls.M3SplitButton;
import org.glavo.m3fx.internal.M3Animation;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// A base animated skin for m3fx labeled button controls.
@NotNullByDefault
abstract class M3LabeledButtonSkinBase<C extends ButtonBase> extends LabeledSkinBase<C> {
    /// The scale applied by controls that opt into depth-style pressed motion.
    private static final double PRESSED_SCALE = 0.98;

    /// The press animation timeline.
    private final Timeline animation = new Timeline();

    /// The bounded state layer used for hover, focus, pressed, and ripple feedback.
    private final M3StateLayer stateLayer = new M3StateLayer();

    /// Animates CSS-resolved elevation changes.
    private final M3CssEffectTransition effectTransition;

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

    /// Whether the current interaction was started by a primary mouse press.
    private boolean mousePressed;

    /// Whether the space key currently owns the armed state.
    private boolean spaceKeyPressed;

    /// Animates the pressed scale when the armed state changes.
    private final ChangeListener<Boolean> armedListener =
            (observable, oldValue, newValue) -> animatePressedState(newValue);

    /// Resets transient feedback when the control becomes disabled.
    private final ChangeListener<Boolean> disabledListener = (observable, oldValue, newValue) -> {
        if (newValue) {
            resetInteractionState();
        }
    };

    /// Creates an animated labeled button skin.
    M3LabeledButtonSkinBase(C control) {
        super(control);
        effectTransition = new M3CssEffectTransition(control, control);
        getChildren().add(0, stateLayer);
        stateLayer.installStateTransitions(control);
        effectTransition.install();
        control.setScaleX(1.0);
        control.setScaleY(1.0);
        installInteractionHandlers(control);
        control.widthProperty().addListener(stateLayerLayoutInvalidation);
        control.heightProperty().addListener(stateLayerLayoutInvalidation);
        control.layoutBoundsProperty().addListener(stateLayerLayoutInvalidation);
        control.armedProperty().addListener(armedListener);
        control.disabledProperty().addListener(disabledListener);
        layoutStateLayer();
    }

    /// Stops the animation before the skin is disposed.
    @Override
    public void dispose() {
        getSkinnable().widthProperty().removeListener(stateLayerLayoutInvalidation);
        getSkinnable().heightProperty().removeListener(stateLayerLayoutInvalidation);
        getSkinnable().layoutBoundsProperty().removeListener(stateLayerLayoutInvalidation);
        getSkinnable().armedProperty().removeListener(armedListener);
        getSkinnable().disabledProperty().removeListener(disabledListener);
        resetInteractionState();
        stateLayer.uninstallStateTransitions();
        effectTransition.uninstall();
        uninstallInteractionHandlers(getSkinnable());
        super.dispose();
    }

    /// Lays out labeled content and the bounded state layer.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        super.layoutChildren(x, y, width, height);
        centerFixedTargetContent(x, y, width, height);
        layoutStateLayer();
    }

    /// Installs mouse and keyboard behavior handlers.
    private void installInteractionHandlers(C button) {
        button.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        button.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        button.addEventHandler(MouseEvent.MOUSE_ENTERED, mouseEnteredHandler);
        button.addEventHandler(MouseEvent.MOUSE_EXITED, mouseExitedHandler);
        button.addEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        button.addEventHandler(KeyEvent.KEY_RELEASED, keyReleasedHandler);
    }

    /// Removes mouse and keyboard behavior handlers.
    private void uninstallInteractionHandlers(C button) {
        button.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        button.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        button.removeEventHandler(MouseEvent.MOUSE_ENTERED, mouseEnteredHandler);
        button.removeEventHandler(MouseEvent.MOUSE_EXITED, mouseExitedHandler);
        button.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        button.removeEventHandler(KeyEvent.KEY_RELEASED, keyReleasedHandler);
    }

    /// Arms the button on primary mouse press.
    private void handleMousePressed(MouseEvent event) {
        C button = getSkinnable();
        if (button.isDisabled() || event.getButton() != MouseButton.PRIMARY) {
            return;
        }

        mousePressed = true;
        if (button.isFocusTraversable()) {
            button.requestFocus();
        }
        layoutStateLayer();
        stateLayer.playRipple(event.getX(), event.getY());
        button.arm();
        event.consume();
    }

    /// Fires the button when a primary mouse press is released inside the control.
    private void handleMouseReleased(MouseEvent event) {
        C button = getSkinnable();
        if (!mousePressed || event.getButton() != MouseButton.PRIMARY) {
            return;
        }

        boolean shouldFire = button.isArmed() && button.contains(event.getX(), event.getY());
        mousePressed = false;
        stateLayer.releaseRipple();
        button.disarm();
        if (shouldFire) {
            button.fire();
        }
        event.consume();
    }

    /// Re-arms the button when a pressed pointer re-enters the control.
    private void handleMouseEntered(MouseEvent event) {
        C button = getSkinnable();
        if (mousePressed && !button.isDisabled()) {
            button.arm();
            event.consume();
        }
    }

    /// Disarms the button when a pressed pointer exits the control.
    private void handleMouseExited(MouseEvent event) {
        C button = getSkinnable();
        if (mousePressed && !button.isDisabled()) {
            button.disarm();
            event.consume();
        }
    }

    /// Handles keyboard activation for enter and space.
    private void handleKeyPressed(KeyEvent event) {
        C button = getSkinnable();
        if (button.isDisabled()) {
            return;
        }

        if (event.getCode() == KeyCode.SPACE) {
            if (!spaceKeyPressed) {
                spaceKeyPressed = true;
                layoutStateLayer();
                stateLayer.playCenteredRipple();
                button.arm();
            }
            event.consume();
        } else if (event.getCode() == KeyCode.ENTER) {
            layoutStateLayer();
            stateLayer.playCenteredRipple();
            stateLayer.releaseRipple();
            button.fire();
            event.consume();
        }
    }

    /// Fires the button when a space key activation is released.
    private void handleKeyReleased(KeyEvent event) {
        C button = getSkinnable();
        if (event.getCode() != KeyCode.SPACE || !spaceKeyPressed) {
            return;
        }

        boolean shouldFire = button.isArmed() && !button.isDisabled();
        spaceKeyPressed = false;
        stateLayer.releaseRipple();
        button.disarm();
        if (shouldFire) {
            button.fire();
        }
        event.consume();
    }

    /// Animates the skinnable button into or out of the pressed state.
    private void animatePressedState(boolean pressed) {
        C button = getSkinnable();
        if (button.isDisabled()) {
            return;
        }

        double scale = pressedScale(pressed);
        M3MotionSpec spec = pressed ? M3Animation.fastEffects(button) : M3Animation.defaultEffects(button);
        animation.stop();
        animation.getKeyFrames().setAll(new KeyFrame(
                spec.duration(),
                new KeyValue(button.scaleXProperty(), scale, spec.interpolator()),
                new KeyValue(button.scaleYProperty(), scale, spec.interpolator())
        ));
        M3Animation.playFromStart(button, animation);
    }

    /// Returns the skinnable scale used for the requested pressed state.
    ///
    /// @param pressed whether the skinnable is currently pressed
    /// @return the scale applied while transitioning to the requested pressed state
    protected double pressedScale(boolean pressed) {
        return 1.0;
    }

    /// Returns the skinnable scale for controls that opt into depth-style pressed motion.
    ///
    /// @param pressed whether the skinnable is currently pressed
    /// @return the depth-style pressed scale, or `1.0` when released
    protected final double depthPressedScale(boolean pressed) {
        return pressed ? PRESSED_SCALE : 1.0;
    }

    /// Centers content whose touch target is fixed and square.
    private void centerFixedTargetContent(double x, double y, double width, double height) {
        C button = getSkinnable();
        @Nullable Node graphic = button.getGraphic();
        @Nullable String text = button.getText();
        if (graphic != null
                && (button.getContentDisplay() == ContentDisplay.GRAPHIC_ONLY
                || text == null
                || text.isEmpty())) {
            centerNodeInArea(graphic, x, y, width, height);
            return;
        }

        if (graphic == null && button.getStyleClass().contains(M3DatePicker.DAY_CELL_STYLE_CLASS)) {
            @Nullable Node textNode = firstTextNode();
            if (textNode != null) {
                centerNodeInArea(textNode, x, y, width, height);
            }
        }
    }

    /// Centers a child node inside the supplied skin layout area.
    private void centerNodeInArea(Node node, double x, double y, double width, double height) {
        Bounds bounds = node.getLayoutBounds();
        double targetX = x + (width - bounds.getWidth()) / 2.0;
        double targetY = y + (height - bounds.getHeight()) / 2.0;
        node.relocate(snapPositionX(targetX), snapPositionY(targetY));
    }

    /// Returns the first direct or nested text node owned by this skin.
    private @Nullable Node firstTextNode() {
        for (Node child : getChildren()) {
            @Nullable Node textNode = firstTextNode(child);
            if (textNode != null) {
                return textNode;
            }
        }
        return null;
    }

    /// Returns the first text node inside a child hierarchy.
    private static @Nullable Node firstTextNode(Node node) {
        if (node instanceof Text) {
            return node;
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable Node textNode = firstTextNode(child);
                if (textNode != null) {
                    return textNode;
                }
            }
        }
        return null;
    }

    /// Clears armed state, scale animation, and transient feedback.
    private void resetInteractionState() {
        C control = getSkinnable();
        mousePressed = false;
        spaceKeyPressed = false;
        animation.stop();
        stateLayer.reset();
        control.disarm();
        control.setScaleX(1.0);
        control.setScaleY(1.0);
    }

    /// Lays out the state layer to cover the full control surface.
    private void layoutStateLayer() {
        C control = getSkinnable();
        double width = control.getWidth();
        double height = control.getHeight();
        if (width <= 0.0) {
            width = control.getLayoutBounds().getWidth();
        }
        if (height <= 0.0) {
            height = control.getLayoutBounds().getHeight();
        }
        if (control instanceof M3SegmentedButton segmentedButton) {
            layoutSegmentedButtonStateLayer(segmentedButton, width, height);
            return;
        }
        if (control instanceof M3Button button && layoutGroupedButtonStateLayer(button, width, height)) {
            return;
        }
        stateLayer.layoutLayer(0.0, 0.0, width, height, stateLayerShapeRadius());
    }

    /// Lays out segmented button feedback with position-specific corner radii.
    private void layoutSegmentedButtonStateLayer(M3SegmentedButton button, double width, double height) {
        double radius = button.getContainerShape();
        if (button.getStyleClass().contains(M3SegmentedButtonGroup.SINGLE_SEGMENT_STYLE_CLASS)) {
            stateLayer.layoutLayer(0.0, 0.0, width, height, radius);
        } else if (button.getStyleClass().contains(M3SegmentedButtonGroup.FIRST_SEGMENT_STYLE_CLASS)) {
            stateLayer.layoutLayer(0.0, 0.0, width, height, radius, 0.0, 0.0, radius);
        } else if (button.getStyleClass().contains(M3SegmentedButtonGroup.MIDDLE_SEGMENT_STYLE_CLASS)) {
            stateLayer.layoutLayer(0.0, 0.0, width, height, 0.0);
        } else if (button.getStyleClass().contains(M3SegmentedButtonGroup.LAST_SEGMENT_STYLE_CLASS)) {
            stateLayer.layoutLayer(0.0, 0.0, width, height, 0.0, radius, radius, 0.0);
        } else {
            stateLayer.layoutLayer(0.0, 0.0, width, height, radius);
        }
    }

    /// Lays out button feedback with button-group or split-button corner radii.
    private boolean layoutGroupedButtonStateLayer(M3Button button, double width, double height) {
        double radius = button.getContainerShape();
        if (button.getStyleClass().contains(M3ButtonGroup.SINGLE_BUTTON_STYLE_CLASS)) {
            stateLayer.layoutLayer(0.0, 0.0, width, height, radius);
            return true;
        }
        boolean rightToLeft = button.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;
        boolean roundedLeft = false;
        boolean roundedRight = false;
        if (button.getStyleClass().contains(M3ButtonGroup.FIRST_BUTTON_STYLE_CLASS)) {
            roundedLeft = true;
        } else if (button.getStyleClass().contains(M3ButtonGroup.LAST_BUTTON_STYLE_CLASS)) {
            roundedRight = true;
        } else if (button.getStyleClass().contains(M3SplitButton.ACTION_BUTTON_STYLE_CLASS)) {
            roundedLeft = !rightToLeft;
            roundedRight = rightToLeft;
        } else if (button.getStyleClass().contains(M3SplitButton.MENU_BUTTON_STYLE_CLASS)) {
            roundedLeft = rightToLeft;
            roundedRight = !rightToLeft;
        }
        if (roundedLeft) {
            stateLayer.layoutLayer(0.0, 0.0, width, height, radius, 0.0, 0.0, radius);
            return true;
        }
        if (button.getStyleClass().contains(M3ButtonGroup.MIDDLE_BUTTON_STYLE_CLASS)) {
            stateLayer.layoutLayer(0.0, 0.0, width, height, 0.0);
            return true;
        }
        if (roundedRight) {
            stateLayer.layoutLayer(0.0, 0.0, width, height, 0.0, radius, radius, 0.0);
            return true;
        }
        return false;
    }

    /// Returns the shape radius used to clip state layer feedback.
    private double stateLayerShapeRadius() {
        C button = getSkinnable();
        if (button instanceof M3FloatingActionButton floatingActionButton) {
            return floatingActionButton.getContainerShape();
        }
        if (button instanceof M3Button m3Button) {
            return m3Button.getContainerShape();
        }
        if (button instanceof M3IconToggleButton iconToggleButton) {
            return iconToggleButton.getContainerShape();
        }
        if (button instanceof M3Chip chip) {
            return chip.getContainerShape();
        }
        return Math.min(button.getWidth(), button.getHeight()) / 2.0;
    }
}
