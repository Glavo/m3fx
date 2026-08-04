// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.skin.TableRowSkin;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.glavo.m3fx.controls.M3TableRow;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Renders a Material table row with a bounded interaction state layer.
///
/// The inherited table-row skin retains JavaFX cell creation, column layout, editing, selection, and
/// virtualization behavior. This skin places Material hover, focus-visible, pressed, and ripple feedback beneath
/// the row's table cells. Pointer events remain unconsumed so the owning table keeps its inherited behavior.
///
/// @param <T> the row-item type
@NotNullByDefault
public final class M3TableRowSkin<T> extends TableRowSkin<T> {
    /// The bounded state and ripple layer rendered beneath inherited table cells.
    private final M3StateLayer stateLayer = new M3StateLayer();

    /// Dispatches pointer interaction events without replacing JavaFX table behavior.
    private final EventHandler<MouseEvent> mouseEventHandler = this::handleMouseEvent;

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

    /// Creates a Material skin for a reusable table row.
    ///
    /// @param control the table row controlled by this skin
    public M3TableRowSkin(M3TableRow<T> control) {
        super(control);
        getChildren().add(0, stateLayer);
        stateLayer.installStateTransitions(control);
        control.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEventHandler);
        control.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEventHandler);
        control.addEventHandler(MouseEvent.MOUSE_EXITED, mouseEventHandler);
        control.disabledProperty().addListener(disabledListener);
        control.sceneProperty().addListener(sceneListener);
    }

    /// Removes interaction listeners and releases transient state-layer resources.
    @Override
    public void dispose() {
        M3TableRow<T> control = materialRow();
        control.removeEventHandler(MouseEvent.MOUSE_PRESSED, mouseEventHandler);
        control.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseEventHandler);
        control.removeEventHandler(MouseEvent.MOUSE_EXITED, mouseEventHandler);
        control.disabledProperty().removeListener(disabledListener);
        control.sceneProperty().removeListener(sceneListener);
        resetInteractionState();
        stateLayer.setDelegatedFocusVisible(false);
        stateLayer.uninstallStateTransitions();
        getChildren().remove(stateLayer);
        super.dispose();
    }

    /// Lays out inherited cells and the full-row bounded interaction layer.
    ///
    /// @param x      the content area's x coordinate
    /// @param y      the content area's y coordinate
    /// @param width  the content area's width
    /// @param height the content area's height
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        super.layoutChildren(x, y, width, height);
        keepStateLayerBehindCells();
        stateLayer.layoutLayer(0.0, 0.0, materialRow().getWidth(), materialRow().getHeight(), 0.0);
    }

    /// Restores the state layer to the first child position after inherited column changes create cells.
    private void keepStateLayerBehindCells() {
        if (!getChildren().isEmpty() && getChildren().get(0) == stateLayer) {
            return;
        }
        getChildren().remove(stateLayer);
        getChildren().add(0, stateLayer);
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

    /// Starts a bounded ripple for a primary pointer press without consuming table selection behavior.
    ///
    /// @param event the pointer-pressed event
    private void handleMousePressed(MouseEvent event) {
        if (materialRow().isDisabled() || materialRow().isEmpty() || event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        mousePressed = true;
        stateLayer.layoutLayer(0.0, 0.0, materialRow().getWidth(), materialRow().getHeight(), 0.0);
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
        stateLayer.releaseRipple();
    }

    /// Releases a pointer-owned ripple after the pointer leaves without a release delivered to the row.
    ///
    /// @param event the pointer-exited event
    private void handleMouseExited(MouseEvent event) {
        if (!mousePressed || event.isPrimaryButtonDown()) {
            return;
        }
        mousePressed = false;
        stateLayer.releaseRipple();
    }

    /// Cancels unfinished pointer feedback.
    private void resetInteractionState() {
        mousePressed = false;
        stateLayer.cancelRipple();
    }

    /// Updates keyboard-visible focus delegated by the owning virtualized table view.
    ///
    /// @param focusVisible whether this row is the table's current keyboard-focused row
    void setLogicalFocusVisible(boolean focusVisible) {
        stateLayer.setDelegatedFocusVisible(focusVisible);
    }

    /// Returns the Material subtype supplied to this skin's constructor.
    ///
    /// @return the skinned Material table row
    @SuppressWarnings("unchecked")
    private M3TableRow<T> materialRow() {
        return (M3TableRow<T>) getSkinnable();
    }
}
