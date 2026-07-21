// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.skin.LabeledSkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import org.glavo.m3fx.internal.M3FocusRequests;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Renders and activates an internal date-picker day cell.
///
/// Calendar grids contain a fixed set of reusable cells. This skin therefore defers allocation of the complete
/// Material state layer until a cell is first hovered, focused, armed, or activated. Text layout and accessible
/// button behavior remain available before the interaction layer is created.
@NotNullByDefault
final class M3DateCellSkin extends LabeledSkinBase<ButtonBase> {
    /// The lazily allocated Material interaction layer.
    private @Nullable M3StateLayer stateLayer;

    /// The text node whose visual bounds are centered in the fixed day target.
    private @Nullable Text textNode;

    /// Dispatches the mouse event types used by date-cell activation.
    private final EventHandler<MouseEvent> mouseEventHandler = this::handleMouseEvent;

    /// Dispatches the key event types used by date-cell activation.
    private final EventHandler<KeyEvent> keyEventHandler = this::handleKeyEvent;

    /// Allocates interaction feedback when a persistent pointer or armed state first becomes active.
    private final ChangeListener<Boolean> activeStateListener = (observable, oldValue, newValue) -> {
        if (newValue) {
            ensureStateLayer();
        }
    };

    /// Allocates focus feedback on focus gain and cancels unfinished keyboard activation on focus loss.
    private final ChangeListener<Boolean> focusedListener = (observable, oldValue, newValue) -> {
        if (newValue) {
            ensureStateLayer();
        } else {
            cancelKeyboardInteraction();
        }
    };

    /// Clears transient interaction when a cell becomes disabled.
    private final ChangeListener<Boolean> disabledListener = (observable, oldValue, newValue) -> {
        if (newValue) {
            resetInteractionState();
        }
    };

    /// Clears transient interaction when a reusable cell leaves its scene.
    private final InvalidationListener sceneInvalidation = observable -> {
        if (getSkinnable().getScene() == null) {
            resetInteractionState();
        }
    };

    /// Whether the current interaction was started by a primary mouse press.
    private boolean mousePressed;

    /// Whether the Space key currently owns the armed state.
    private boolean spaceKeyPressed;

    /// Creates a skin for an internal date-picker day cell.
    ///
    /// @param control the day cell controlled by this skin
    M3DateCellSkin(ButtonBase control) {
        super(control);
        installInteractionHandlers(control);
        control.hoverProperty().addListener(activeStateListener);
        control.armedProperty().addListener(activeStateListener);
        control.focusedProperty().addListener(focusedListener);
        control.disabledProperty().addListener(disabledListener);
        control.sceneProperty().addListener(sceneInvalidation);
    }

    /// Removes listeners and stops transient interaction feedback.
    @Override
    public void dispose() {
        ButtonBase control = getSkinnable();
        control.hoverProperty().removeListener(activeStateListener);
        control.armedProperty().removeListener(activeStateListener);
        control.focusedProperty().removeListener(focusedListener);
        control.disabledProperty().removeListener(disabledListener);
        control.sceneProperty().removeListener(sceneInvalidation);
        uninstallInteractionHandlers(control);
        resetInteractionState();
        M3StateLayer layer = stateLayer;
        if (layer != null) {
            layer.uninstallStateTransitions();
            stateLayer = null;
        }
        textNode = null;
        super.dispose();
    }

    /// Centers numeric text by visual glyph bounds and lays out an allocated interaction layer.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        super.layoutChildren(x, y, width, height);
        centerText(x, y, width, height);
        M3StateLayer layer = stateLayer;
        if (layer != null) {
            layoutStateLayer(layer);
        }
    }

    /// Installs mouse and keyboard activation handlers.
    private void installInteractionHandlers(ButtonBase control) {
        control.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEventHandler);
        control.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEventHandler);
        control.addEventHandler(MouseEvent.MOUSE_ENTERED, mouseEventHandler);
        control.addEventHandler(MouseEvent.MOUSE_EXITED, mouseEventHandler);
        control.addEventHandler(KeyEvent.KEY_PRESSED, keyEventHandler);
        control.addEventHandler(KeyEvent.KEY_RELEASED, keyEventHandler);
    }

    /// Removes mouse and keyboard activation handlers.
    private void uninstallInteractionHandlers(ButtonBase control) {
        control.removeEventHandler(MouseEvent.MOUSE_PRESSED, mouseEventHandler);
        control.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseEventHandler);
        control.removeEventHandler(MouseEvent.MOUSE_ENTERED, mouseEventHandler);
        control.removeEventHandler(MouseEvent.MOUSE_EXITED, mouseEventHandler);
        control.removeEventHandler(KeyEvent.KEY_PRESSED, keyEventHandler);
        control.removeEventHandler(KeyEvent.KEY_RELEASED, keyEventHandler);
    }

    /// Dispatches one registered mouse event to its activation-state handler.
    private void handleMouseEvent(MouseEvent event) {
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            handleMousePressed(event);
        } else if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
            handleMouseReleased(event);
        } else if (event.getEventType() == MouseEvent.MOUSE_ENTERED) {
            handleMouseEntered(event);
        } else if (event.getEventType() == MouseEvent.MOUSE_EXITED) {
            handleMouseExited(event);
        }
    }

    /// Dispatches one registered key event to its activation-state handler.
    private void handleKeyEvent(KeyEvent event) {
        if (event.getEventType() == KeyEvent.KEY_PRESSED) {
            handleKeyPressed(event);
        } else if (event.getEventType() == KeyEvent.KEY_RELEASED) {
            handleKeyReleased(event);
        }
    }

    /// Arms the cell and starts bounded feedback for a primary mouse press.
    private void handleMousePressed(MouseEvent event) {
        ButtonBase control = getSkinnable();
        if (control.isDisabled() || event.getButton() != MouseButton.PRIMARY) {
            return;
        }

        mousePressed = true;
        M3FocusRequests.requestFocusIfTraversable(control);
        M3StateLayer layer = ensureStateLayer();
        layoutStateLayer(layer);
        layer.playRipple(event.getX() - layer.getLayoutX(), event.getY() - layer.getLayoutY());
        control.arm();
        event.consume();
    }

    /// Fires the cell when a primary mouse press is released inside its bounds.
    private void handleMouseReleased(MouseEvent event) {
        ButtonBase control = getSkinnable();
        if (!mousePressed || event.getButton() != MouseButton.PRIMARY) {
            return;
        }

        boolean shouldFire = control.isArmed() && control.contains(event.getX(), event.getY());
        mousePressed = false;
        if (!spaceKeyPressed) {
            M3StateLayer layer = stateLayer;
            if (layer != null) {
                layer.releaseRipple();
            }
            control.disarm();
        }
        if (shouldFire) {
            control.fire();
        }
        event.consume();
    }

    /// Re-arms the cell when an active pointer gesture re-enters its bounds.
    private void handleMouseEntered(MouseEvent event) {
        ButtonBase control = getSkinnable();
        if (mousePressed && !control.isDisabled()) {
            control.arm();
            event.consume();
        }
    }

    /// Disarms the cell when an active pointer gesture exits its bounds.
    private void handleMouseExited(MouseEvent event) {
        ButtonBase control = getSkinnable();
        if (mousePressed && !control.isDisabled()) {
            control.disarm();
            event.consume();
        }
    }

    /// Handles Enter and Space keyboard activation.
    private void handleKeyPressed(KeyEvent event) {
        ButtonBase control = getSkinnable();
        if (control.isDisabled()) {
            return;
        }

        if (event.getCode() == KeyCode.SPACE) {
            if (!spaceKeyPressed) {
                spaceKeyPressed = true;
                M3StateLayer layer = ensureStateLayer();
                layoutStateLayer(layer);
                layer.playCenteredRipple();
                control.arm();
            }
            event.consume();
        } else if (event.getCode() == KeyCode.ENTER) {
            M3StateLayer layer = ensureStateLayer();
            layoutStateLayer(layer);
            layer.playCenteredRipple();
            layer.releaseRipple();
            control.fire();
            event.consume();
        }
    }

    /// Fires a Space activation when the key is released.
    private void handleKeyReleased(KeyEvent event) {
        ButtonBase control = getSkinnable();
        if (event.getCode() != KeyCode.SPACE || !spaceKeyPressed) {
            return;
        }

        boolean shouldFire = control.isArmed() && !control.isDisabled();
        spaceKeyPressed = false;
        if (!mousePressed) {
            M3StateLayer layer = stateLayer;
            if (layer != null) {
                layer.releaseRipple();
            }
            control.disarm();
        }
        if (shouldFire) {
            control.fire();
        }
        event.consume();
    }

    /// Cancels unfinished keyboard activation without disturbing an active pointer gesture.
    private void cancelKeyboardInteraction() {
        if (!spaceKeyPressed) {
            return;
        }

        spaceKeyPressed = false;
        if (!mousePressed) {
            M3StateLayer layer = stateLayer;
            if (layer != null) {
                layer.releaseRipple();
            }
            getSkinnable().disarm();
        }
    }

    /// Clears armed state and transient ripple feedback.
    private void resetInteractionState() {
        mousePressed = false;
        spaceKeyPressed = false;
        M3StateLayer layer = stateLayer;
        if (layer != null) {
            layer.cancelRipple();
        }
        getSkinnable().disarm();
    }

    /// Returns the existing state layer or creates it for the first interactive state.
    private M3StateLayer ensureStateLayer() {
        M3StateLayer layer = stateLayer;
        if (layer != null) {
            return layer;
        }

        layer = new M3StateLayer();
        stateLayer = layer;
        getChildren().add(0, layer);
        layer.installStateTransitions(getSkinnable(), true);
        layoutStateLayer(layer);
        return layer;
    }

    /// Fits the state layer to the circular target represented by the cell's innermost background inset.
    private void layoutStateLayer(M3StateLayer layer) {
        ButtonBase control = getSkinnable();
        double width = control.getWidth();
        double height = control.getHeight();
        if (width <= 0.0) {
            width = control.getLayoutBounds().getWidth();
        }
        if (height <= 0.0) {
            height = control.getLayoutBounds().getHeight();
        }

        Background background = control.getBackground();
        double inset = 0.0;
        if (background != null && !background.getFills().isEmpty()) {
            Insets fillInsets =
                    background.getFills().get(background.getFills().size() - 1).getInsets();
            inset = Math.max(
                    Math.max(fillInsets.getTop(), fillInsets.getRight()),
                    Math.max(fillInsets.getBottom(), fillInsets.getLeft())
            );
        }
        double targetWidth = Math.max(0.0, width - 2.0 * inset);
        double targetHeight = Math.max(0.0, height - 2.0 * inset);
        layer.layoutLayer(
                (width - targetWidth) / 2.0,
                (height - targetHeight) / 2.0,
                targetWidth,
                targetHeight,
                Math.min(targetWidth, targetHeight) / 2.0
        );
        layer.animateOverlayOpacityFromOwnerState();
    }

    /// Centers the rendered day text using visual rather than logical font bounds.
    private void centerText(double x, double y, double width, double height) {
        Text text = textNode;
        if (text == null || text.getParent() == null) {
            text = firstTextNode();
            textNode = text;
        }
        if (text == null) {
            return;
        }

        text.setBoundsType(TextBoundsType.VISUAL);
        Bounds bounds = text.getLayoutBounds();
        text.relocate(
                snapPositionX(x + (width - bounds.getWidth()) / 2.0),
                snapPositionY(y + (height - bounds.getHeight()) / 2.0)
        );
    }

    /// Returns the first text node rendered by the labeled skin.
    private @Nullable Text firstTextNode() {
        for (Node child : getChildren()) {
            @Nullable Text result = firstTextNode(child);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /// Returns the first text node in a node hierarchy.
    private static @Nullable Text firstTextNode(Node node) {
        if (node instanceof Text text) {
            return text;
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable Text result = firstTextNode(child);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }
}
