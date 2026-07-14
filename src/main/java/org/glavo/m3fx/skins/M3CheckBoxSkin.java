// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3CheckBox;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3NodeTransition;
import org.jetbrains.annotations.NotNullByDefault;

/// The default skin for [M3CheckBox].
@NotNullByDefault
public class M3CheckBoxSkin extends M3SelectionControlSkinBase<M3CheckBox> {
    /// The hidden selected mark scale.
    private static final double HIDDEN_MARK_SCALE = 0.72;


    /// The visual checkbox container.
    private final StackPane box = new StackPane();

    /// The visual selected check mark.
    private final Region mark = new Region();

    /// The selected mark appearance animation.
    private final M3NodeTransition selectionAnimation = new M3NodeTransition(mark);

    /// The currently displayed selected mark shape.
    private MarkKind displayedMarkKind = MarkKind.CHECK;

    /// Applies checkbox geometry token changes to skin nodes.
    private final InvalidationListener metricsInvalidation = observable -> updateMetrics();

    /// Animates the mark after selection changes.
    private final ChangeListener<Boolean> selectedListener =
            (observable, oldValue, newValue) -> animateMarkState();

    /// Animates the mark after indeterminate state changes.
    private final ChangeListener<Boolean> indeterminateListener =
            (observable, oldValue, newValue) -> animateMarkState();

    /// Creates a checkbox skin.
    ///
    /// @param control the checkbox controlled by this skin
    public M3CheckBoxSkin(M3CheckBox control) {
        super(control);
        box.getStyleClass().addAll("box", "m3-checkbox-box");
        mark.getStyleClass().addAll("mark", "m3-checkbox-mark");
        indicatorSlot().getChildren().addAll(box, mark);

        displayedMarkKind = currentMarkKind();
        applyMarkState(control.isSelected() || control.isIndeterminate());
        updateMetrics();
        control.touchTargetSizeProperty().addListener(metricsInvalidation);
        control.stateLayerSizeProperty().addListener(metricsInvalidation);
        control.containerSizeProperty().addListener(metricsInvalidation);
        control.selectedMarkWidthProperty().addListener(metricsInvalidation);
        control.selectedMarkHeightProperty().addListener(metricsInvalidation);
        control.indeterminateMarkWidthProperty().addListener(metricsInvalidation);
        control.indeterminateMarkHeightProperty().addListener(metricsInvalidation);
        control.selectedProperty().addListener(selectedListener);
        control.indeterminateProperty().addListener(indeterminateListener);
    }

    /// Removes listeners before the skin is disposed.
    @Override
    public void dispose() {
        selectionAnimation.stop();
        getSkinnable().touchTargetSizeProperty().removeListener(metricsInvalidation);
        getSkinnable().stateLayerSizeProperty().removeListener(metricsInvalidation);
        getSkinnable().containerSizeProperty().removeListener(metricsInvalidation);
        getSkinnable().selectedMarkWidthProperty().removeListener(metricsInvalidation);
        getSkinnable().selectedMarkHeightProperty().removeListener(metricsInvalidation);
        getSkinnable().indeterminateMarkWidthProperty().removeListener(metricsInvalidation);
        getSkinnable().indeterminateMarkHeightProperty().removeListener(metricsInvalidation);
        getSkinnable().selectedProperty().removeListener(selectedListener);
        getSkinnable().indeterminateProperty().removeListener(indeterminateListener);
        super.dispose();
    }

    /// Applies size-related control tokens to the skin nodes.
    private void updateMetrics() {
        M3CheckBox control = getSkinnable();
        double touchTargetSize = control.getTouchTargetSize();
        double stateLayerSize = control.getStateLayerSize();
        double slotSize = Math.max(touchTargetSize, stateLayerSize);
        double layerOffset = (slotSize - stateLayerSize) / 2.0;

        setIndicatorSlotSize(slotSize, slotSize);
        layoutIndicatorStateLayer(layerOffset, layerOffset, stateLayerSize, stateLayerSize, stateLayerSize / 2.0);
        setFixedSize(box, control.getContainerSize(), control.getContainerSize());
        updateMarkMetrics(displayedMarkKind);
    }

    /// Applies the selected or indeterminate mark dimensions.
    private void updateMarkMetrics(MarkKind markKind) {
        M3CheckBox control = getSkinnable();
        if (markKind == MarkKind.DASH) {
            setFixedSize(mark, control.getIndeterminateMarkWidth(), control.getIndeterminateMarkHeight());
        } else {
            setFixedSize(mark, control.getSelectedMarkWidth(), control.getSelectedMarkHeight());
        }
    }

    /// Applies the selected mark state without animation.
    private void applyMarkState(boolean visible) {
        mark.setOpacity(visible ? 1.0 : 0.0);
        mark.setScaleX(visible ? 1.0 : HIDDEN_MARK_SCALE);
        mark.setScaleY(visible ? 1.0 : HIDDEN_MARK_SCALE);
    }

    /// Animates the selected or indeterminate mark state.
    private void animateMarkState() {
        boolean visible = getSkinnable().isSelected() || getSkinnable().isIndeterminate();
        MarkKind targetMarkKind = currentMarkKind();
        boolean markKindChanged = displayedMarkKind != targetMarkKind;
        selectionAnimation.stop();
        displayedMarkKind = targetMarkKind;
        updateMarkMetrics(targetMarkKind);
        if (markKindChanged && visible) {
            applyMarkState(false);
        }
        M3MotionSpec spec = M3Animation.fastEffects(getSkinnable());
        double targetScale = visible ? 1.0 : HIDDEN_MARK_SCALE;
        selectionAnimation.configure(
                spec,
                visible ? 1.0 : 0.0,
                targetScale,
                targetScale,
                mark.getTranslateX(),
                mark.getTranslateY()
        );
        M3Animation.playFromStart(getSkinnable(), selectionAnimation);
    }

    /// Returns the mark shape that should be displayed for the current control state.
    private MarkKind currentMarkKind() {
        return getSkinnable().isIndeterminate() ? MarkKind.DASH : MarkKind.CHECK;
    }

    /// The rendered checkbox mark shape.
    @NotNullByDefault
    private enum MarkKind {
        /// A check mark used by selected determinate checkboxes.
        CHECK,

        /// A horizontal dash used by indeterminate checkboxes.
        DASH
    }
}
