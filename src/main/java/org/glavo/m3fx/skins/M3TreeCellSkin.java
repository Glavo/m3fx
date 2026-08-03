// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.skin.TreeCellSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.glavo.m3fx.controls.M3TreeCell;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Renders a Material tree row with a bounded interaction state layer.
///
/// The inherited tree-cell skin retains JavaFX disclosure, indentation, text, graphic, and virtualization behavior.
/// This skin adds the same hover, focus-visible, pressed, and ripple feedback surface used by Material list items.
/// Pointer and keyboard events remain unconsumed so the owning tree view keeps its stock selection, navigation, and
/// expansion behavior.
///
/// @param <T> the tree-item value type
@NotNullByDefault
public final class M3TreeCellSkin<T> extends TreeCellSkin<T> {
    /// The bounded state and ripple layer rendered above the row container and below its content.
    private final M3StateLayer stateLayer = new M3StateLayer();

    /// Dispatches pointer interaction events without replacing JavaFX tree behavior.
    private final EventHandler<MouseEvent> mouseEventHandler = this::handleMouseEvent;

    /// Dispatches keyboard feedback events when a tree cell directly owns focus.
    private final EventHandler<KeyEvent> keyEventHandler = this::handleKeyEvent;

    /// Clears transient feedback when a reusable row becomes disabled.
    private final ChangeListener<Boolean> disabledListener = (observable, oldValue, newValue) -> {
        if (newValue) {
            resetInteractionState();
        }
    };

    /// Clears transient feedback when virtualization removes a row from its scene.
    private final ChangeListener<@Nullable Scene> sceneListener = (observable, oldScene, newScene) -> {
        if (newScene == null) {
            resetInteractionState();
        }
    };

    /// Whether a primary pointer press currently owns the active ripple.
    private boolean mousePressed;

    /// Whether the Space key currently owns the active ripple.
    private boolean spaceKeyPressed;

    /// Creates a Material skin for a reusable tree cell.
    ///
    /// @param control the tree cell controlled by this skin
    public M3TreeCellSkin(M3TreeCell<T> control) {
        super(control);
        getChildren().add(0, stateLayer);
        stateLayer.installStateTransitions(control);
        control.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEventHandler);
        control.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEventHandler);
        control.addEventHandler(MouseEvent.MOUSE_EXITED, mouseEventHandler);
        control.addEventHandler(KeyEvent.KEY_PRESSED, keyEventHandler);
        control.addEventHandler(KeyEvent.KEY_RELEASED, keyEventHandler);
        control.disabledProperty().addListener(disabledListener);
        control.sceneProperty().addListener(sceneListener);
    }

    /// Removes interaction listeners and releases transient state-layer resources.
    @Override
    public void dispose() {
        M3TreeCell<T> control = materialCell();
        control.removeEventHandler(MouseEvent.MOUSE_PRESSED, mouseEventHandler);
        control.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseEventHandler);
        control.removeEventHandler(MouseEvent.MOUSE_EXITED, mouseEventHandler);
        control.removeEventHandler(KeyEvent.KEY_PRESSED, keyEventHandler);
        control.removeEventHandler(KeyEvent.KEY_RELEASED, keyEventHandler);
        control.disabledProperty().removeListener(disabledListener);
        control.sceneProperty().removeListener(sceneListener);
        resetInteractionState();
        stateLayer.uninstallStateTransitions();
        getChildren().remove(stateLayer);
        super.dispose();
    }

    /// Keeps the state layer behind text, graphics, and the disclosure indicator after inherited content updates.
    @Override
    protected void updateChildren() {
        super.updateChildren();
        M3StateLayer layer = stateLayer;
        if (layer == null) {
            return;
        }
        getChildren().remove(layer);
        getChildren().add(0, layer);
    }

    /// Lays out inherited row content and the full-row bounded interaction layer.
    ///
    /// @param x      the content area's x coordinate
    /// @param y      the content area's y coordinate
    /// @param width  the content area's width
    /// @param height the content area's height
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        super.layoutChildren(x, y, width, height);
        layoutStateLayer();
    }

    /// Dispatches one pointer event to its ripple lifecycle operation.
    ///
    /// @param event the pointer event
    private void handleMouseEvent(MouseEvent event) {
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            handleMousePressed(event);
        } else if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
            handleMouseReleased(event);
        } else if (event.getEventType() == MouseEvent.MOUSE_EXITED) {
            handleMouseExited(event);
        }
    }

    /// Starts a bounded ripple for a primary pointer press without consuming tree selection behavior.
    ///
    /// @param event the pointer-pressed event
    private void handleMousePressed(MouseEvent event) {
        if (materialCell().isDisabled() || event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        mousePressed = true;
        layoutStateLayer();
        stateLayer.playRipple(event.getX(), event.getY());
    }

    /// Releases pointer-owned ripple feedback.
    ///
    /// @param event the pointer-released event
    private void handleMouseReleased(MouseEvent event) {
        if (!mousePressed || event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        mousePressed = false;
        if (!spaceKeyPressed) {
            stateLayer.releaseRipple();
        }
    }

    /// Releases a pointer-owned ripple after the pointer leaves without a release delivered to the row.
    ///
    /// @param event the pointer-exited event
    private void handleMouseExited(MouseEvent event) {
        if (!mousePressed || event.isPrimaryButtonDown()) {
            return;
        }
        mousePressed = false;
        if (!spaceKeyPressed) {
            stateLayer.releaseRipple();
        }
    }

    /// Dispatches keyboard feedback while preserving the owning tree's navigation behavior.
    ///
    /// @param event the key event
    private void handleKeyEvent(KeyEvent event) {
        if (event.getEventType() == KeyEvent.KEY_PRESSED) {
            handleKeyPressed(event);
        } else if (event.getEventType() == KeyEvent.KEY_RELEASED) {
            handleKeyReleased(event);
        }
    }

    /// Starts centered Space or Enter feedback without consuming the activation key.
    ///
    /// @param event the key-pressed event
    private void handleKeyPressed(KeyEvent event) {
        if (materialCell().isDisabled()) {
            return;
        }
        if (event.getCode() == KeyCode.SPACE && !spaceKeyPressed) {
            spaceKeyPressed = true;
            layoutStateLayer();
            stateLayer.playCenteredRipple();
        } else if (event.getCode() == KeyCode.ENTER) {
            layoutStateLayer();
            stateLayer.playCenteredRipple();
            stateLayer.releaseRipple();
        }
    }

    /// Releases centered Space feedback.
    ///
    /// @param event the key-released event
    private void handleKeyReleased(KeyEvent event) {
        if (event.getCode() != KeyCode.SPACE || !spaceKeyPressed) {
            return;
        }
        spaceKeyPressed = false;
        if (!mousePressed) {
            stateLayer.releaseRipple();
        }
    }

    /// Fits the state layer to the complete fixed-height row.
    private void layoutStateLayer() {
        M3TreeCell<T> cell = materialCell();
        stateLayer.layoutLayer(0.0, 0.0, cell.getWidth(), cell.getHeight(), 0.0);
    }

    /// Cancels all unfinished pointer and keyboard feedback.
    private void resetInteractionState() {
        mousePressed = false;
        spaceKeyPressed = false;
        stateLayer.cancelRipple();
    }

    /// Returns the Material subtype supplied to this skin's constructor.
    ///
    /// @return the skinned Material tree cell
    @SuppressWarnings("unchecked")
    private M3TreeCell<T> materialCell() {
        return (M3TreeCell<T>) getSkinnable();
    }
}
