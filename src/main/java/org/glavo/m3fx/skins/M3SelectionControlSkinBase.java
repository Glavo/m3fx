// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.internal.M3FocusRequests;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.jetbrains.annotations.NotNullByDefault;

/// A base skin for Material Design 3 selection controls.
///
/// The base arranges an indicator slot and label in logical order and provides shared state-layer, ripple, focus,
/// pointer, and keyboard interaction. Subclasses supply the indicator geometry and may customize Enter-key
/// activation while retaining Space-key arming semantics.
@NotNullByDefault
abstract class M3SelectionControlSkinBase<C extends ButtonBase> extends SkinBase<C> {
    /// The root layout container.
    private final HBox container = new HBox();

    /// Resolves the configured alignment against the effective node orientation.
    private final ObjectBinding<Pos> containerAlignment;

    /// The indicator touch target slot.
    private final StackPane indicatorSlot = new StackPane();

    /// The bounded state layer for indicator feedback.
    private final M3StateLayer stateLayer = new M3StateLayer();

    /// The label that mirrors the skinnable control's labeled content.
    private final Label label = new Label();

    /// Handles primary mouse presses.
    private final EventHandler<MouseEvent> mousePressedHandler = this::handleMousePressed;

    /// Handles primary mouse releases.
    private final EventHandler<MouseEvent> mouseReleasedHandler = this::handleMouseReleased;

    /// Handles primary-button pointer drags.
    private final EventHandler<MouseEvent> mouseDraggedHandler = this::handleMouseDragged;

    /// Handles pointer entry while a mouse press is active.
    private final EventHandler<MouseEvent> mouseEnteredHandler = this::handleMouseEntered;

    /// Handles pointer exit while a mouse press is active.
    private final EventHandler<MouseEvent> mouseExitedHandler = this::handleMouseExited;

    /// Handles keyboard activation presses.
    private final EventHandler<KeyEvent> keyPressedHandler = this::handleKeyPressed;

    /// Handles keyboard activation releases.
    private final EventHandler<KeyEvent> keyReleasedHandler = this::handleKeyReleased;

    /// Clears transient interaction state when the control becomes disabled.
    private final ChangeListener<Boolean> disabledListener = (observable, oldValue, newValue) -> {
        if (newValue) {
            resetInteractionState();
        }
    };

    /// Clears keyboard and pointer interaction state when focus leaves the control.
    private final ChangeListener<Boolean> focusedListener = (observable, oldValue, newValue) -> {
        if (!newValue) {
            resetInteractionState();
        }
    };

    /// Whether the current interaction was started by a primary mouse press.
    private boolean mousePressed;

    /// Whether the space key currently owns the armed state.
    private boolean spaceKeyPressed;

    /// Creates a selection control skin.
    M3SelectionControlSkinBase(C control) {
        super(control);
        containerAlignment = Bindings.createObjectBinding(
                () -> M3NodeLayout.logicalAlignment(control, control.getAlignment()),
                control.alignmentProperty(),
                control.effectiveNodeOrientationProperty()
        );
        container.getStyleClass().add("m3-selection-container");
        indicatorSlot.getStyleClass().add("m3-selection-indicator");
        label.getStyleClass().add("m3-selection-label");
        container.alignmentProperty().bind(containerAlignment);
        container.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        indicatorSlot.setAlignment(Pos.CENTER);
        bindLabel(control);
        stateLayer.installStateTransitions(control);
        indicatorSlot.getChildren().add(stateLayer);
        container.getChildren().addAll(indicatorSlot, label);
        getChildren().setAll(container);
        installInteractionHandlers(control);
        control.disabledProperty().addListener(disabledListener);
        control.focusedProperty().addListener(focusedListener);
    }

    /// Removes behavior handlers before the skin is disposed.
    @Override
    public void dispose() {
        C control = getSkinnable();
        resetInteractionState();
        stateLayer.uninstallStateTransitions();
        control.disabledProperty().removeListener(disabledListener);
        control.focusedProperty().removeListener(focusedListener);
        container.alignmentProperty().unbind();
        containerAlignment.dispose();
        container.nodeOrientationProperty().unbind();
        unbindLabel();
        control.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        control.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        control.removeEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        control.removeEventHandler(MouseEvent.MOUSE_ENTERED, mouseEnteredHandler);
        control.removeEventHandler(MouseEvent.MOUSE_EXITED, mouseExitedHandler);
        control.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        control.removeEventHandler(KeyEvent.KEY_RELEASED, keyReleasedHandler);
        getChildren().remove(container);
        super.dispose();
    }

    /// Clears armed state and transient feedback.
    private void resetInteractionState() {
        mousePressed = false;
        spaceKeyPressed = false;
        stateLayer.reset();
        getSkinnable().disarm();
    }

    /// Computes the minimum width from the internal container.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + container.minWidth(height) + rightInset;
    }

    /// Computes the minimum height from the internal container.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + container.minHeight(width) + bottomInset;
    }

    /// Computes the preferred width from the internal container.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + container.prefWidth(height) + rightInset;
    }

    /// Computes the preferred height from the internal container.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + container.prefHeight(width) + bottomInset;
    }

    /// Lays out the internal container in the full control bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        container.resizeRelocate(x, y, width, height);
    }

    /// Returns the indicator touch target slot.
    ///
    /// @return the indicator touch target slot
    protected final StackPane indicatorSlot() {
        return indicatorSlot;
    }

    /// Returns the mirrored label node.
    ///
    /// @return the mirrored label node
    protected final Label label() {
        return label;
    }

    /// Returns whether the Enter key activates this selection control.
    ///
    /// @return `true` when Enter should activate the control
    protected boolean isEnterActivationEnabled() {
        return true;
    }

    /// Applies a fixed size to a region.
    ///
    /// @param region the region to size
    /// @param width  the fixed width in pixels
    /// @param height the fixed height in pixels
    protected static void setFixedSize(Region region, double width, double height) {
        region.setMinSize(width, height);
        region.setPrefSize(width, height);
        region.setMaxSize(width, height);
    }

    /// Applies a fixed size to the indicator touch target slot.
    ///
    /// @param width  the fixed indicator slot width in pixels
    /// @param height the fixed indicator slot height in pixels
    protected final void setIndicatorSlotSize(double width, double height) {
        setFixedSize(indicatorSlot, width, height);
        stateLayer.layoutLayer(0.0, 0.0, width, height, Math.max(width, height) / 2.0);
    }

    /// Lays out the indicator state layer within the indicator slot.
    ///
    /// @param x           the layer x position
    /// @param y           the layer y position
    /// @param width       the layer width
    /// @param height      the layer height
    /// @param shapeRadius the layer clipping radius
    protected final void layoutIndicatorStateLayer(
            double x,
            double y,
            double width,
            double height,
            double shapeRadius
    ) {
        stateLayer.layoutLayer(x, y, width, height, shapeRadius);
    }

    /// Lays out a focus indicator independently from the bounded state layer.
    ///
    /// Coordinates are relative to the state layer itself, not the indicator slot.
    ///
    /// @param x           the focus indicator container x position
    /// @param y           the focus indicator container y position
    /// @param width       the focus indicator container width
    /// @param height      the focus indicator container height
    /// @param shapeRadius the focus indicator container shape radius
    protected final void layoutIndicatorFocusIndicator(
            double x,
            double y,
            double width,
            double height,
            double shapeRadius
    ) {
        stateLayer.layoutFocusIndicator(x, y, width, height, shapeRadius);
    }

    /// Begins a component-specific primary-pointer interaction after the control is armed.
    ///
    /// @param event the primary mouse press
    protected void beginPrimaryPointerInteraction(MouseEvent event) {
    }

    /// Continues a component-specific primary-pointer interaction.
    ///
    /// @param event the primary-button drag event
    protected void continuePrimaryPointerInteraction(MouseEvent event) {
    }

    /// Completes a component-specific primary-pointer interaction before normal click activation.
    ///
    /// Returning `false` suppresses the base click action because the subclass has committed the interaction.
    ///
    /// @param event          the primary mouse release
    /// @param releasedInside whether the armed pointer was released inside the control
    /// @return whether normal click activation should continue
    protected boolean completePrimaryPointerInteraction(MouseEvent event, boolean releasedInside) {
        return true;
    }

    /// Binds label content and presentation properties to the skinnable control.
    private void bindLabel(C control) {
        label.textProperty().bind(control.textProperty());
        label.graphicProperty().bind(control.graphicProperty());
        label.textFillProperty().bind(control.textFillProperty());
        label.fontProperty().bind(control.fontProperty());
        label.contentDisplayProperty().bind(control.contentDisplayProperty());
        label.graphicTextGapProperty().bind(control.graphicTextGapProperty());
        label.alignmentProperty().bind(control.alignmentProperty());
        label.textAlignmentProperty().bind(control.textAlignmentProperty());
        label.textOverrunProperty().bind(control.textOverrunProperty());
        label.ellipsisStringProperty().bind(control.ellipsisStringProperty());
        label.wrapTextProperty().bind(control.wrapTextProperty());
        label.underlineProperty().bind(control.underlineProperty());
        label.mnemonicParsingProperty().bind(control.mnemonicParsingProperty());
    }

    /// Unbinds mirrored label properties from the skinnable control.
    private void unbindLabel() {
        label.textProperty().unbind();
        label.graphicProperty().unbind();
        label.textFillProperty().unbind();
        label.fontProperty().unbind();
        label.contentDisplayProperty().unbind();
        label.graphicTextGapProperty().unbind();
        label.alignmentProperty().unbind();
        label.textAlignmentProperty().unbind();
        label.textOverrunProperty().unbind();
        label.ellipsisStringProperty().unbind();
        label.wrapTextProperty().unbind();
        label.underlineProperty().unbind();
        label.mnemonicParsingProperty().unbind();
    }

    /// Installs mouse and keyboard behavior handlers.
    private void installInteractionHandlers(C control) {
        control.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        control.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        control.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        control.addEventHandler(MouseEvent.MOUSE_ENTERED, mouseEnteredHandler);
        control.addEventHandler(MouseEvent.MOUSE_EXITED, mouseExitedHandler);
        control.addEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
        control.addEventHandler(KeyEvent.KEY_RELEASED, keyReleasedHandler);
    }

    /// Arms the control on primary mouse press.
    private void handleMousePressed(MouseEvent event) {
        C control = getSkinnable();
        if (control.isDisabled() || event.getButton() != MouseButton.PRIMARY) {
            return;
        }

        mousePressed = true;
        M3FocusRequests.requestFocusIfTraversable(control);
        playRipple(event);
        control.arm();
        beginPrimaryPointerInteraction(event);
        event.consume();
    }

    /// Forwards primary-button drag events to component-specific pointer behavior.
    private void handleMouseDragged(MouseEvent event) {
        if (!mousePressed || !event.isPrimaryButtonDown() || getSkinnable().isDisabled()) {
            return;
        }

        continuePrimaryPointerInteraction(event);
        event.consume();
    }

    /// Fires the control when a primary mouse press is released inside the control.
    private void handleMouseReleased(MouseEvent event) {
        C control = getSkinnable();
        if (!mousePressed || event.getButton() != MouseButton.PRIMARY) {
            return;
        }

        boolean releasedInside = control.isArmed() && control.contains(event.getX(), event.getY());
        boolean shouldFire = completePrimaryPointerInteraction(event, releasedInside) && releasedInside;
        mousePressed = false;
        stateLayer.releaseRipple();
        control.disarm();
        if (shouldFire) {
            control.fire();
        }
        event.consume();
    }

    /// Re-arms the control when a pressed pointer re-enters the control.
    private void handleMouseEntered(MouseEvent event) {
        C control = getSkinnable();
        if (mousePressed && !control.isDisabled()) {
            control.arm();
            event.consume();
        }
    }

    /// Disarms the control when a pressed pointer exits the control.
    private void handleMouseExited(MouseEvent event) {
        C control = getSkinnable();
        if (mousePressed && !control.isDisabled()) {
            control.disarm();
            event.consume();
        }
    }

    /// Handles keyboard activation for enter and space.
    private void handleKeyPressed(KeyEvent event) {
        C control = getSkinnable();
        if (control.isDisabled()) {
            return;
        }

        if (event.getCode() == KeyCode.SPACE) {
            if (!spaceKeyPressed) {
                spaceKeyPressed = true;
                stateLayer.playCenteredRipple();
                control.arm();
            }
            event.consume();
        } else if (event.getCode() == KeyCode.ENTER && isEnterActivationEnabled()) {
            stateLayer.playCenteredRipple();
            stateLayer.releaseRipple();
            control.fire();
            event.consume();
        }
    }

    /// Fires the control when a space key activation is released.
    private void handleKeyReleased(KeyEvent event) {
        C control = getSkinnable();
        if (event.getCode() != KeyCode.SPACE || !spaceKeyPressed) {
            return;
        }

        boolean shouldFire = control.isArmed() && !control.isDisabled();
        spaceKeyPressed = false;
        stateLayer.releaseRipple();
        control.disarm();
        if (shouldFire) {
            control.fire();
        }
        event.consume();
    }

    /// Plays an indicator-bounded ripple from a mouse event.
    private void playRipple(MouseEvent event) {
        Point2D point = stateLayer.sceneToLocal(event.getSceneX(), event.getSceneY());
        if (stateLayer.contains(point)) {
            stateLayer.playRipple(point.getX(), point.getY());
        } else {
            stateLayer.playCenteredRipple();
        }
    }
}
