// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3RadioButton;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3KeyEvents;
import org.glavo.m3fx.internal.M3NodeTransition;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3RadioButton].
@NotNullByDefault
public class M3RadioButtonSkin extends M3SelectionControlSkinBase<M3RadioButton> {
    /// The hidden selected dot scale.
    private static final double HIDDEN_DOT_SCALE = 0.64;


    /// The visual radio indicator container.
    private final Pane radio = new Pane();

    /// The selected and unselected radio ring.
    private final Circle ring = new Circle();

    /// The selected radio dot.
    private final Circle dot = new Circle();

    /// The transform layer used to animate the selected dot without overriding its CSS opacity.
    private final Group dotLayer = new Group(dot);

    /// The selected dot appearance animation.
    private final M3NodeTransition selectionAnimation = new M3NodeTransition(dotLayer);

    /// Applies radio geometry token changes to skin nodes.
    private final InvalidationListener metricsInvalidation = observable -> updateMetrics();

    /// Animates the selected dot after selection changes.
    private final ChangeListener<Boolean> selectedListener =
            (observable, oldValue, newValue) -> animateSelectedState(newValue);

    /// Handles cyclic selection and focus movement within a radio-button group.
    private final EventHandler<KeyEvent> groupNavigationHandler = this::handleGroupNavigation;

    /// Creates a radio button skin.
    ///
    /// @param control the skinned radio button
    public M3RadioButtonSkin(M3RadioButton control) {
        super(control);
        radio.getStyleClass().addAll("radio", "m3-radio");
        ring.getStyleClass().addAll("ring", "m3-radio-ring");
        dot.getStyleClass().addAll("dot", "m3-radio-dot");
        configureCircle(ring);
        configureCircle(dot);
        dotLayer.setManaged(false);
        dotLayer.setMouseTransparent(true);
        ring.setStrokeType(StrokeType.INSIDE);
        radio.getChildren().addAll(ring, dotLayer);
        indicatorSlot().getChildren().add(radio);

        applySelectedState(control.isSelected());
        updateMetrics();
        control.touchTargetSizeProperty().addListener(metricsInvalidation);
        control.stateLayerSizeProperty().addListener(metricsInvalidation);
        control.containerSizeProperty().addListener(metricsInvalidation);
        control.selectedDotSizeProperty().addListener(metricsInvalidation);
        control.selectedProperty().addListener(selectedListener);
        control.addEventHandler(KeyEvent.KEY_PRESSED, groupNavigationHandler);
    }

    /// Removes listeners before the skin is disposed.
    @Override
    public void dispose() {
        selectionAnimation.stop();
        getSkinnable().touchTargetSizeProperty().removeListener(metricsInvalidation);
        getSkinnable().stateLayerSizeProperty().removeListener(metricsInvalidation);
        getSkinnable().containerSizeProperty().removeListener(metricsInvalidation);
        getSkinnable().selectedDotSizeProperty().removeListener(metricsInvalidation);
        getSkinnable().selectedProperty().removeListener(selectedListener);
        getSkinnable().removeEventHandler(KeyEvent.KEY_PRESSED, groupNavigationHandler);
        super.dispose();
    }

    /// Returns whether Enter activates a radio button.
    ///
    /// Material radio buttons use Space for activation and reserve arrow keys for group selection.
    @Override
    protected boolean isEnterActivationEnabled() {
        return false;
    }

    /// Applies size-related control tokens to the skin nodes.
    private void updateMetrics() {
        M3RadioButton control = getSkinnable();
        double touchTargetSize = control.getTouchTargetSize();
        double stateLayerSize = control.getStateLayerSize();
        double slotSize = Math.max(touchTargetSize, stateLayerSize);
        double layerOffset = (slotSize - stateLayerSize) / 2.0;
        double containerSize = control.getContainerSize();
        double center = containerSize / 2.0;

        setIndicatorSlotSize(slotSize, slotSize);
        layoutIndicatorStateLayer(layerOffset, layerOffset, stateLayerSize, stateLayerSize, stateLayerSize / 2.0);
        setFixedSize(radio, containerSize, containerSize);
        layoutCircle(ring, center, center, containerSize / 2.0);
        layoutCircle(dot, center, center, control.getSelectedDotSize() / 2.0);
    }

    /// Applies the selected dot state without animation.
    private void applySelectedState(boolean selected) {
        dotLayer.setOpacity(selected ? 1.0 : 0.0);
        dotLayer.setScaleX(selected ? 1.0 : HIDDEN_DOT_SCALE);
        dotLayer.setScaleY(selected ? 1.0 : HIDDEN_DOT_SCALE);
    }

    /// Animates the selected dot state.
    private void animateSelectedState(boolean selected) {
        selectionAnimation.stop();
        M3MotionSpec spec = M3Animation.fastEffects(getSkinnable());
        double targetScale = selected ? 1.0 : HIDDEN_DOT_SCALE;
        selectionAnimation.configure(
                spec,
                selected ? 1.0 : 0.0,
                targetScale,
                targetScale,
                dotLayer.getTranslateX(),
                dotLayer.getTranslateY()
        );
        M3Animation.playFromStart(getSkinnable(), selectionAnimation);
    }

    /// Moves selection and focus to an adjacent enabled toggle in the current group.
    private void handleGroupNavigation(KeyEvent event) {
        if (event.isConsumed() || M3KeyEvents.hasNavigationModifier(event)) {
            return;
        }

        M3RadioButton control = getSkinnable();
        KeyCode code = event.getCode();
        boolean forward;
        if (code == KeyCode.DOWN) {
            forward = true;
        } else if (code == KeyCode.UP) {
            forward = false;
        } else if (code == KeyCode.RIGHT) {
            forward = control.getEffectiveNodeOrientation() != NodeOrientation.RIGHT_TO_LEFT;
        } else if (code == KeyCode.LEFT) {
            forward = control.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;
        } else {
            return;
        }

        ToggleGroup group = control.getToggleGroup();
        if (group == null) {
            return;
        }
        ObservableList<Toggle> toggles = group.getToggles();
        int itemCount = toggles.size();
        int currentIndex = toggles.indexOf(control);
        if (itemCount < 2 || currentIndex < 0) {
            return;
        }

        for (int step = 1; step < itemCount; step++) {
            int targetIndex = Math.floorMod(currentIndex + (forward ? step : -step), itemCount);
            Toggle candidate = toggles.get(targetIndex);
            if (candidate instanceof Node target
                    && !target.isDisabled()
                    && target.isVisible()
                    && target.getScene() != null) {
                group.selectToggle(candidate);
                target.requestFocus();
                event.consume();
                return;
            }
        }
    }

    /// Configures a circle for unmanaged indicator painting.
    private static void configureCircle(Circle circle) {
        circle.setManaged(false);
        circle.setMouseTransparent(true);
        circle.setSmooth(true);
    }

    /// Positions a circle at the requested center.
    private static void layoutCircle(Circle circle, double centerX, double centerY, double radius) {
        circle.setCenterX(centerX);
        circle.setCenterY(centerY);
        circle.setRadius(radius);
    }
}
