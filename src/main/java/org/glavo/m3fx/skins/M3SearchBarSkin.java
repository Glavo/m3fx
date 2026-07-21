// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Point2D;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.controls.M3SearchBar;
import org.glavo.m3fx.internal.M3Accessible;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default Material Design 3 skin for [M3SearchBar].
///
/// The skin arranges the leading slot, editable text field, and trailing actions in logical order and keeps decorative
/// state-layer and focus geometry aligned with the visible container. Pointer and keyboard activation target the
/// search bar without allowing its decorative layers to intercept input.
@NotNullByDefault
public final class M3SearchBarSkin extends SkinBase<M3SearchBar> {
    /// The spacing between search bar content slots.
    private static final double CONTENT_SPACING = 12.0;

    /// The spacing between trailing action nodes.
    private static final double ACTION_SPACING = 4.0;

    /// The internal horizontal container.
    private final HBox container = new HBox(CONTENT_SPACING);

    /// The slot that hosts the optional leading content.
    private final StackPane leadingSlot = new StackPane();

    /// The trailing action container.
    private final HBox trailingBox = new HBox(ACTION_SPACING);

    /// The bounded interaction state layer and ripple surface.
    private final M3StateLayer stateLayer = new M3StateLayer();

    /// Tracks keyboard-visible focus held by the embedded editor.
    private final M3FocusVisibleTracker editorFocusVisibleTracker;

    /// Mirrors editor focus-visible pseudo-class changes into the outer state layer.
    private final SetChangeListener<PseudoClass> editorPseudoClassListener = change -> {
        @Nullable PseudoClass changed = change.wasAdded() ? change.getElementAdded() : change.getElementRemoved();
        if (changed == M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS) {
            updateEditorFocusVisible();
        }
    };

    /// Handles primary pointer presses anywhere inside the search bar.
    private final EventHandler<MouseEvent> mousePressedHandler = this::handleMousePressed;

    /// Handles primary pointer release after a search-bar press.
    private final EventHandler<MouseEvent> mouseReleasedHandler = this::handleMouseReleased;

    /// Handles keyboard activation before the control consumes the key event.
    private final EventHandler<KeyEvent> keyPressedHandler = this::handleKeyPressed;

    /// Releases keyboard-driven ripple feedback.
    private final EventHandler<KeyEvent> keyReleasedHandler = this::handleKeyReleased;

    /// Clears transient feedback when the control becomes disabled.
    private final ChangeListener<Boolean> disabledListener;

    /// Cancels keyboard ownership when outer search-bar focus moves away before Space is released.
    private final ChangeListener<Boolean> focusedListener = (observable, oldValue, newValue) -> {
        if (!newValue) {
            cancelKeyboardInteraction();
        }
    };

    /// Clears gesture ownership when the search bar leaves its scene before release.
    private final InvalidationListener sceneInvalidation = observable -> {
        if (getSkinnable().getScene() == null) {
            resetInteractionState();
        }
    };

    /// Whether a primary pointer press currently owns ripple feedback.
    private boolean mousePressed;

    /// Whether the space key currently owns ripple feedback.
    private boolean spaceKeyPressed;

    /// Updates the leading slot when the public leading node changes.
    private final ChangeListener<@Nullable Node> leadingListener =
            (observable, oldValue, newValue) -> updateLeading(newValue);

    /// Updates the trailing action container when public actions change.
    private final ListChangeListener<Node> trailingActionsListener = change -> updateTrailingActions();

    /// Creates a search bar skin.
    ///
    /// @param control the search bar controlled by this skin
    /// @throws IllegalStateException if the search bar does not expose its text editor
    public M3SearchBarSkin(M3SearchBar control) {
        super(control);
        container.setManaged(false);
        container.getStyleClass().add(M3SearchBar.CONTENT_STYLE_CLASS);
        container.setAlignment(Pos.CENTER_LEFT);
        container.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        leadingSlot.getStyleClass().add(M3SearchBar.LEADING_STYLE_CLASS);
        trailingBox.getStyleClass().add(M3SearchBar.TRAILING_STYLE_CLASS);
        trailingBox.setAlignment(Pos.CENTER);
        TextField editor = editor(control);
        editorFocusVisibleTracker = new M3FocusVisibleTracker(editor, this::updateEditorFocusVisible);
        disabledListener = (observable, oldValue, newValue) -> {
            if (newValue) {
                mousePressed = false;
                spaceKeyPressed = false;
                stateLayer.cancelRipple();
            }
            updateEditorFocusVisible();
        };
        HBox.setHgrow(editor, Priority.ALWAYS);

        control.leadingProperty().addListener(leadingListener);
        control.getTrailingActions().addListener(trailingActionsListener);
        editor.getPseudoClassStates().addListener(editorPseudoClassListener);
        control.disabledProperty().addListener(disabledListener);
        control.focusedProperty().addListener(focusedListener);
        control.sceneProperty().addListener(sceneInvalidation);
        control.addEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        control.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        control.addEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
        control.addEventFilter(KeyEvent.KEY_RELEASED, keyReleasedHandler);
        stateLayer.installStateTransitions(control);
        editorFocusVisibleTracker.install();

        updateLeading(control.getLeading());
        updateTrailingActions();
        container.getChildren().setAll(leadingSlot, editor, trailingBox);
        getChildren().setAll(container, stateLayer);
    }

    /// Removes listeners and child references before disposal.
    @Override
    public void dispose() {
        M3SearchBar control = getSkinnable();
        control.leadingProperty().removeListener(leadingListener);
        control.getTrailingActions().removeListener(trailingActionsListener);
        editor(control).getPseudoClassStates().removeListener(editorPseudoClassListener);
        control.disabledProperty().removeListener(disabledListener);
        control.focusedProperty().removeListener(focusedListener);
        control.sceneProperty().removeListener(sceneInvalidation);
        control.removeEventHandler(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        control.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        control.removeEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
        control.removeEventFilter(KeyEvent.KEY_RELEASED, keyReleasedHandler);
        editorFocusVisibleTracker.uninstall();
        stateLayer.uninstallStateTransitions();
        container.nodeOrientationProperty().unbind();
        trailingBox.getChildren().clear();
        leadingSlot.getChildren().clear();
        container.getChildren().clear();
        getChildren().removeAll(container, stateLayer);
        super.dispose();
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

    /// Computes the maximum width from the internal container.
    @Override
    protected double computeMaxWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + container.maxWidth(height) + rightInset;
    }

    /// Computes the maximum height from the internal container.
    @Override
    protected double computeMaxHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + container.maxHeight(width) + bottomInset;
    }

    /// Lays out the internal container in the full control content bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        container.resizeRelocate(x, y, width, height);
        layoutStateLayer();
    }

    /// Returns the embedded editor exposed by the skinnable search bar.
    private static TextField editor(M3SearchBar control) {
        Object contents = control.queryAccessibleAttribute(AccessibleAttribute.CONTENTS);
        if (contents instanceof TextField textField) {
            return textField;
        }
        throw new IllegalStateException("Search bar contents must be a text field");
    }

    /// Updates the leading slot content.
    private void updateLeading(@Nullable Node node) {
        leadingSlot.getChildren().clear();
        leadingSlot.setVisible(node != null);
        leadingSlot.setManaged(node != null);
        if (node != null) {
            leadingSlot.getChildren().add(node);
        }
        getSkinnable().requestLayout();
    }

    /// Updates trailing action container content and visibility.
    private void updateTrailingActions() {
        trailingBox.getChildren().setAll(getSkinnable().getTrailingActions());
        boolean visible = !trailingBox.getChildren().isEmpty();
        trailingBox.setVisible(visible);
        trailingBox.setManaged(visible);
        getSkinnable().requestLayout();
    }

    /// Starts a bounded ripple for a primary pointer press.
    private void handleMousePressed(MouseEvent event) {
        M3SearchBar control = getSkinnable();
        if (control.isDisabled() || event.getButton() != MouseButton.PRIMARY) {
            return;
        }

        Node target = event.getPickResult().getIntersectedNode();
        @Nullable Node leading = control.getLeading();
        if (leading != null
                && M3Accessible.structuralFocusTarget(leading) != null
                && M3Accessible.containsNode(leading, target)) {
            return;
        }
        for (Node action : control.getTrailingActions()) {
            if (M3Accessible.structuralFocusTarget(action) != null
                    && M3Accessible.containsNode(action, target)) {
                return;
            }
        }

        mousePressed = true;
        layoutStateLayer();
        Point2D point = control.sceneToLocal(event.getSceneX(), event.getSceneY());
        stateLayer.playRipple(point.getX(), point.getY());
    }

    /// Releases pointer-driven ripple feedback without consuming editor input.
    private void handleMouseReleased(MouseEvent event) {
        if (!mousePressed || event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        mousePressed = false;
        if (!spaceKeyPressed) {
            stateLayer.releaseRipple();
        }
    }

    /// Starts keyboard ripple feedback when the search-bar container owns activation.
    private void handleKeyPressed(KeyEvent event) {
        M3SearchBar control = getSkinnable();
        if (control.isDisabled() || !control.isFocused()) {
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

    /// Releases ripple feedback after keyboard activation.
    private void handleKeyReleased(KeyEvent event) {
        if (event.getCode() != KeyCode.SPACE || !spaceKeyPressed) {
            return;
        }
        spaceKeyPressed = false;
        if (!mousePressed) {
            stateLayer.releaseRipple();
        }
    }

    /// Ends an unfinished Space activation without disturbing an active pointer gesture.
    private void cancelKeyboardInteraction() {
        if (!spaceKeyPressed) {
            return;
        }

        spaceKeyPressed = false;
        if (!mousePressed) {
            stateLayer.releaseRipple();
        }
    }

    /// Clears transient pointer and keyboard feedback.
    private void resetInteractionState() {
        mousePressed = false;
        spaceKeyPressed = false;
        stateLayer.cancelRipple();
    }

    /// Sizes interaction feedback to the complete search-bar container rather than its padded content bounds.
    private void layoutStateLayer() {
        M3SearchBar control = getSkinnable();
        double width = control.getWidth();
        double height = control.getHeight();
        if (width <= 0.0 || height <= 0.0) {
            return;
        }
        stateLayer.layoutLayer(0.0, 0.0, width, height, height / 2.0);
        stateLayer.animateOverlayOpacityFromOwnerState();
    }

    /// Updates delegated focus feedback from the editor's current focus-visible pseudo-class.
    private void updateEditorFocusVisible() {
        TextField editor = editor(getSkinnable());
        stateLayer.setDelegatedFocusVisible(editor.getPseudoClassStates().contains(
                M3FocusVisibleTracker.FOCUS_VISIBLE_PSEUDO_CLASS
        ));
    }
}
