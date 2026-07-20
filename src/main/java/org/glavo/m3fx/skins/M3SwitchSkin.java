// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import org.glavo.m3fx.internal.animation.M3DoubleTransition;

import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Transform;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3Switch;
import org.glavo.m3fx.internal.M3Animation;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3Switch].
///
/// The skin combines shared selection-control interaction with a track, movable thumb, and optional thumb icon.
/// Pointer dragging updates the thumb presentation before committing selection on release; keyboard activation and
/// programmatic selection use the same selected-state transition.
@NotNullByDefault
public class M3SwitchSkin extends M3SelectionControlSkinBase<M3Switch> {
    /// The minimum pointer movement before a track press becomes a handle drag.
    private static final double DRAG_THRESHOLD = 4.0;

    /// The pseudo-class applied while a drag previews the unselected state.
    private static final PseudoClass DRAG_UNSELECTED_PSEUDO_CLASS =
            PseudoClass.getPseudoClass("drag-unselected");

    /// The pseudo-class applied while a drag previews the selected state.
    private static final PseudoClass DRAG_SELECTED_PSEUDO_CLASS =
            PseudoClass.getPseudoClass("drag-selected");

    /// The standard selection pseudo-class temporarily synchronized with the dragged handle.
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    /// The style class applied temporarily to the currently displayed handle icon.
    private static final String HANDLE_ICON_STYLE_CLASS = "m3-switch-handle-icon";

    /// The visual switch track.
    private final StackPane box = new StackPane();

    /// The visual switch thumb.
    private final StackPane thumb = new StackPane();

    /// The fixed-size slot that centers the current handle icon.
    private final StackPane iconSlot = new StackPane();

    /// The application-provided icon currently mounted in the handle.
    private @Nullable Node displayedIcon;

    /// Whether this skin added the managed handle icon style class to [displayedIcon].
    private boolean displayedIconStyleClassAdded;

    /// Whether the current primary press began inside the switch indicator.
    private boolean dragCandidate;

    /// Whether the current primary press has crossed the drag threshold.
    private boolean dragging;

    /// The pointer's scene x coordinate at the beginning of a possible drag.
    private double dragStartSceneX;

    /// The pointer offset from the current handle center at the beginning of a possible drag.
    private double dragPointerOffset;

    /// The animated thumb position from off to on.
    private final DoubleProperty thumbPosition = new SimpleDoubleProperty(this, "thumbPosition");

    /// The thumb position animation.
    private final M3DoubleTransition selectionAnimation = new M3DoubleTransition(
            thumbPosition,
            M3DoubleTransition.NORMALIZED_VISIBILITY_THRESHOLD,
            0.0,
            1.0
    );

    /// The animated fraction of the pressed handle size.
    private final DoubleProperty pressedProgress = new SimpleDoubleProperty(this, "pressedProgress");

    /// The pressed handle size animation.
    private final M3DoubleTransition pressedAnimation = new M3DoubleTransition(
            pressedProgress,
            M3DoubleTransition.NORMALIZED_VISIBILITY_THRESHOLD,
            0.0,
            1.0
    );

    /// Applies animated thumb position changes directly to the internal nodes.
    private final InvalidationListener thumbPositionListener = observable -> layoutThumb();

    /// Applies animated pressed-size changes directly to the internal nodes.
    private final InvalidationListener pressedProgressListener = observable -> layoutThumb();

    /// Applies size token changes to the switch layout.
    private final InvalidationListener metricsInvalidation = observable -> updateMetrics();

    /// Applies track shape token changes to the switch track.
    private final InvalidationListener trackShapeInvalidation = observable -> updateTrackStyle();

    /// Cancels an active drag when the switch becomes disabled.
    private final InvalidationListener disabledInvalidation = observable -> {
        if (getSkinnable().isDisabled()) {
            cancelDrag();
        }
    };

    /// Cancels direct manipulation when keyboard focus leaves the switch.
    private final InvalidationListener focusedInvalidation = observable -> {
        if (!getSkinnable().isFocused()) {
            cancelDrag();
        }
    };

    /// Cancels direct manipulation when the switch leaves its scene before pointer release.
    private final InvalidationListener sceneInvalidation = observable -> {
        if (getSkinnable().getScene() == null) {
            cancelDrag();
        }
    };

    /// Starts a pressed handle size transition when armed state changes.
    private final InvalidationListener armedInvalidation = observable -> {
        pressedAnimation.configure(
                M3Animation.fastSpatial(getSkinnable()),
                getSkinnable().isArmed() ? 1.0 : 0.0
        );
        M3Animation.playFromStart(getSkinnable(), pressedAnimation);
    };

    /// Animates the thumb after selection changes.
    private final ChangeListener<Boolean> selectedListener = (observable, oldValue, newValue) -> {
        updateDisplayedIcon();
        animateThumbPosition(newValue);
    };

    /// Creates a switch skin.
    ///
    /// @param control the switch controlled by this skin
    public M3SwitchSkin(M3Switch control) {
        super(control);
        box.getStyleClass().addAll("box", "m3-switch-track");
        thumb.getStyleClass().addAll("thumb", "m3-switch-thumb");
        iconSlot.getStyleClass().add("m3-switch-icon-slot");
        thumb.getChildren().add(iconSlot);
        thumb.setManaged(false);
        indicatorSlot().getChildren().addAll(box, thumb);
        box.toBack();
        thumbPosition.set(control.isSelected() ? 1.0 : 0.0);
        thumbPosition.addListener(thumbPositionListener);
        pressedProgress.set(control.isArmed() ? 1.0 : 0.0);
        pressedProgress.addListener(pressedProgressListener);

        updateDisplayedIcon();
        updateMetrics();
        control.touchTargetSizeProperty().addListener(metricsInvalidation);
        control.trackWidthProperty().addListener(metricsInvalidation);
        control.trackHeightProperty().addListener(metricsInvalidation);
        control.stateLayerSizeProperty().addListener(metricsInvalidation);
        control.unselectedHandleSizeProperty().addListener(metricsInvalidation);
        control.withIconHandleSizeProperty().addListener(metricsInvalidation);
        control.selectedHandleSizeProperty().addListener(metricsInvalidation);
        control.pressedHandleSizeProperty().addListener(metricsInvalidation);
        control.iconSizeProperty().addListener(metricsInvalidation);
        control.trackShapeProperty().addListener(trackShapeInvalidation);
        control.armedProperty().addListener(armedInvalidation);
        control.selectedProperty().addListener(selectedListener);
        control.disabledProperty().addListener(disabledInvalidation);
        control.focusedProperty().addListener(focusedInvalidation);
        control.sceneProperty().addListener(sceneInvalidation);
    }

    /// Stops animations before the skin is disposed.
    @Override
    public void dispose() {
        M3Switch control = getSkinnable();
        selectionAnimation.stop();
        pressedAnimation.stop();
        thumbPosition.removeListener(thumbPositionListener);
        pressedProgress.removeListener(pressedProgressListener);
        control.touchTargetSizeProperty().removeListener(metricsInvalidation);
        control.trackWidthProperty().removeListener(metricsInvalidation);
        control.trackHeightProperty().removeListener(metricsInvalidation);
        control.stateLayerSizeProperty().removeListener(metricsInvalidation);
        control.unselectedHandleSizeProperty().removeListener(metricsInvalidation);
        control.withIconHandleSizeProperty().removeListener(metricsInvalidation);
        control.selectedHandleSizeProperty().removeListener(metricsInvalidation);
        control.pressedHandleSizeProperty().removeListener(metricsInvalidation);
        control.iconSizeProperty().removeListener(metricsInvalidation);
        control.trackShapeProperty().removeListener(trackShapeInvalidation);
        control.armedProperty().removeListener(armedInvalidation);
        control.selectedProperty().removeListener(selectedListener);
        control.disabledProperty().removeListener(disabledInvalidation);
        control.focusedProperty().removeListener(focusedInvalidation);
        control.sceneProperty().removeListener(sceneInvalidation);
        cancelDrag();
        setDisplayedIcon(null);
        thumb.getChildren().clear();
        super.dispose();
    }

    /// Lays out the selection control and switch thumb.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        super.layoutChildren(x, y, width, height);
        layoutThumb();
    }

    /// Starts handle dragging only when the primary press begins inside the switch indicator.
    @Override
    protected void beginPrimaryPointerInteraction(MouseEvent event) {
        dragCandidate = isIndicatorTarget(event.getTarget());
        dragging = false;
        if (!dragCandidate) {
            return;
        }

        dragStartSceneX = event.getSceneX();
        double pointerX = sceneToIndicatorX(event);
        dragPointerOffset = pointerX - thumbCenterX(thumbPosition.get());
    }

    /// Moves the handle continuously after a track press crosses the drag threshold.
    @Override
    protected void continuePrimaryPointerInteraction(MouseEvent event) {
        if (!dragCandidate) {
            return;
        }
        if (!dragging) {
            if (Math.abs(event.getSceneX() - dragStartSceneX) < DRAG_THRESHOLD) {
                return;
            }
            dragging = true;
            selectionAnimation.stop();
        }

        double handleCenterX = sceneToIndicatorX(event) - dragPointerOffset;
        double offCenterX = thumbCenterX(0.0);
        double onCenterX = thumbCenterX(1.0);
        double travel = onCenterX - offCenterX;
        if (travel <= 0.0) {
            return;
        }
        thumbPosition.set(clamp01((handleCenterX - offCenterX) / travel));
        updateDragPreview();
    }

    /// Commits a dragged handle position and suppresses the base click toggle.
    @Override
    protected boolean completePrimaryPointerInteraction(MouseEvent event, boolean releasedInside) {
        boolean wasDragging = dragging;
        dragCandidate = false;
        dragging = false;
        if (!wasDragging) {
            return true;
        }

        M3Switch control = getSkinnable();
        boolean selected = thumbPosition.get() >= 0.5;
        if (selected != control.isSelected()) {
            control.setSelected(selected);
            control.fireEvent(new ActionEvent(control, control));
        } else {
            animateThumbPosition(selected);
        }
        clearDragPreview();
        return false;
    }

    /// Applies size-related control tokens to the skin nodes.
    private void updateMetrics() {
        M3Switch control = getSkinnable();
        double trackWidth = control.getTrackWidth();
        double trackHeight = control.getTrackHeight();
        double touchTargetHeight = Math.max(Math.max(control.getTouchTargetSize(), trackHeight), control.getStateLayerSize());
        setIndicatorSlotSize(trackWidth, touchTargetHeight);
        setFixedSize(box, trackWidth, trackHeight);
        double iconSize = control.getIconSize();
        setFixedSize(iconSlot, iconSize, iconSize);
        updateTrackStyle();
        control.requestLayout();
    }

    /// Applies the switch track shape token to the visual track.
    private void updateTrackStyle() {
        String shape = formatPixels(getSkinnable().getTrackShape());
        box.setStyle("-fx-background-radius: " + shape + "; -fx-border-radius: " + shape + ";");
    }

    /// Animates the thumb to the selected or unselected position.
    private void animateThumbPosition(boolean selected) {
        M3MotionSpec spec = M3Animation.fastSpatial(getSkinnable());
        selectionAnimation.configure(spec, selected ? 1.0 : 0.0);
        M3Animation.playFromStart(getSkinnable(), selectionAnimation);
    }

    /// Lays out the thumb from the animated position value.
    private void layoutThumb() {
        M3Switch control = getSkinnable();
        updateDisplayedIcon();
        double position = thumbPosition.get();
        double trackWidth = control.getTrackWidth();
        double trackHeight = control.getTrackHeight();
        double touchTargetHeight = Math.max(Math.max(control.getTouchTargetSize(), trackHeight), control.getStateLayerSize());
        double withIconThumbSize = control.getWithIconHandleSize();
        double unselectedThumbSize = control.getUnselectedIcon() == null
                ? control.getUnselectedHandleSize()
                : withIconThumbSize;
        double selectedThumbSize = control.getSelectedIcon() == null
                ? control.getSelectedHandleSize()
                : withIconThumbSize;
        double restingThumbSize = unselectedThumbSize + (selectedThumbSize - unselectedThumbSize) * position;
        double thumbSize = restingThumbSize
                + (control.getPressedHandleSize() - restingThumbSize) * pressedProgress.get();
        double offThumbCenterX = trackHeight / 2.0;
        double onThumbCenterX = trackWidth - trackHeight / 2.0;
        double thumbCenterX = offThumbCenterX + (onThumbCenterX - offThumbCenterX) * position;
        double thumbX = thumbCenterX - thumbSize / 2.0;
        double thumbY = (touchTargetHeight - thumbSize) / 2.0;
        double stateLayerSize = control.getStateLayerSize();
        double stateLayerX = thumbCenterX - stateLayerSize / 2.0;
        double stateLayerY = (touchTargetHeight - stateLayerSize) / 2.0;
        double trackY = (touchTargetHeight - trackHeight) / 2.0;
        layoutIndicatorStateLayer(
                stateLayerX,
                stateLayerY,
                stateLayerSize,
                stateLayerSize,
                stateLayerSize / 2.0
        );
        layoutIndicatorFocusIndicator(
                -stateLayerX,
                trackY - stateLayerY,
                trackWidth,
                trackHeight,
                control.getTrackShape()
        );
        thumb.resizeRelocate(thumbX, thumbY, thumbSize, thumbSize);
        updateIconOpacity(position);
    }

    /// Mounts the icon for the current selected state without retaining the inactive icon in the scene graph.
    private void updateDisplayedIcon() {
        M3Switch control = getSkinnable();
        boolean selected = dragging ? thumbPosition.get() >= 0.5 : control.isSelected();
        setDisplayedIcon(selected ? control.getSelectedIcon() : control.getUnselectedIcon());
    }

    /// Replaces the mounted handle icon and restores the previous node's application-owned style classes.
    private void setDisplayedIcon(@Nullable Node icon) {
        if (displayedIcon == icon) {
            return;
        }

        Node oldIcon = displayedIcon;
        if (oldIcon != null && displayedIconStyleClassAdded) {
            oldIcon.getStyleClass().remove(HANDLE_ICON_STYLE_CLASS);
        }
        iconSlot.getChildren().clear();
        displayedIcon = icon;
        displayedIconStyleClassAdded = false;
        if (icon != null) {
            if (!icon.getStyleClass().contains(HANDLE_ICON_STYLE_CLASS)) {
                icon.getStyleClass().add(HANDLE_ICON_STYLE_CLASS);
                displayedIconStyleClassAdded = true;
            }
            iconSlot.getChildren().add(icon);
        }
        iconSlot.setVisible(icon != null);
    }

    /// Fades the destination-state icon in as the handle moves between switch states.
    private void updateIconOpacity(double position) {
        Node icon = displayedIcon;
        if (icon == null) {
            return;
        }

        M3Switch control = getSkinnable();
        if (control.getSelectedIcon() == control.getUnselectedIcon()) {
            iconSlot.setOpacity(1.0);
        } else {
            boolean selected = dragging ? position >= 0.5 : control.isSelected();
            iconSlot.setOpacity(selected ? position : 1.0 - position);
        }
    }

    /// Updates the transient visual selection state while the handle is dragged.
    private void updateDragPreview() {
        boolean selected = thumbPosition.get() >= 0.5;
        M3Switch control = getSkinnable();
        control.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, selected);
        control.pseudoClassStateChanged(DRAG_UNSELECTED_PSEUDO_CLASS, !selected);
        control.pseudoClassStateChanged(DRAG_SELECTED_PSEUDO_CLASS, selected);
        updateDisplayedIcon();
    }

    /// Clears transient drag-selection pseudo-classes and restores the committed icon state.
    private void clearDragPreview() {
        M3Switch control = getSkinnable();
        control.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, control.isSelected());
        control.pseudoClassStateChanged(DRAG_UNSELECTED_PSEUDO_CLASS, false);
        control.pseudoClassStateChanged(DRAG_SELECTED_PSEUDO_CLASS, false);
        updateDisplayedIcon();
    }

    /// Cancels pointer dragging and restores the handle to the selected state without retaining transient input.
    private void cancelDrag() {
        dragCandidate = false;
        dragging = false;
        selectionAnimation.stop();
        thumbPosition.set(getSkinnable().isSelected() ? 1.0 : 0.0);
        clearDragPreview();
    }

    /// Returns the handle center for a normalized switch position.
    private double thumbCenterX(double position) {
        M3Switch control = getSkinnable();
        double trackHeight = control.getTrackHeight();
        double offCenterX = trackHeight / 2.0;
        return offCenterX + (control.getTrackWidth() - trackHeight) * position;
    }

    /// Converts a scene-space pointer coordinate to the indicator slot without allocating a point per drag event.
    private double sceneToIndicatorX(MouseEvent event) {
        Transform transform = indicatorSlot().getLocalToSceneTransform();
        double determinant = transform.getMxx() * transform.getMyy() - transform.getMxy() * transform.getMyx();
        if (Math.abs(determinant) <= 1.0e-12) {
            return thumbCenterX(thumbPosition.get());
        }
        double sceneX = event.getSceneX() - transform.getTx();
        double sceneY = event.getSceneY() - transform.getTy();
        return (sceneX * transform.getMyy() - sceneY * transform.getMxy()) / determinant;
    }

    /// Returns whether an event target belongs to the indicator subtree.
    private boolean isIndicatorTarget(Object eventTarget) {
        if (!(eventTarget instanceof Node node)) {
            return false;
        }
        Node indicator = indicatorSlot();
        for (Node current = node; current != null; current = current.getParent()) {
            if (current == indicator) {
                return true;
            }
        }
        return false;
    }

    /// Constrains a normalized position to the closed unit interval.
    private static double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    /// Formats a CSS pixel value.
    private static String formatPixels(double value) {
        if (Math.rint(value) == value) {
            return (long) value + "px";
        }
        return value + "px";
    }
}
