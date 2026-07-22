// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3IconToggleButton;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3FiniteTransition;
import org.glavo.m3fx.internal.M3IconPaints;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default animated skin for [M3IconToggleButton].
///
/// Selection retargets the resolved container, icon, and interaction paints through the active fast-effects motion
/// specification. Material Expressive shape changes remain owned by [M3LabeledButtonSkinBase], which uses the
/// independent fast-spatial specification. Both transitions render through retained skin nodes and leave the
/// control's layout metrics unchanged.
@NotNullByDefault
public final class M3IconToggleButtonSkin extends M3LabeledButtonSkinBase<M3IconToggleButton> {
    /// Reconciles non-selection paint changes with the retained rendering nodes.
    private final InvalidationListener targetPaintInvalidation = observable -> targetPaintInvalidated();

    /// Starts one coordinated visual transition after the selected pseudo-class has changed.
    private final InvalidationListener selectedInvalidation = observable -> selectedStateInvalidated();

    /// Transfers the animated content paint when the graphic changes.
    private final InvalidationListener graphicInvalidation = observable -> updateManagedGraphic();

    /// The reusable transition that interpolates all selection paint channels.
    private final TogglePaintTransition paintTransition = new TogglePaintTransition();

    /// The container paint currently visible in the retained state-layer node.
    private Paint displayedContainerPaint = Color.TRANSPARENT;

    /// The content paint currently visible in the icon and interaction layers.
    private Paint displayedContentPaint = Color.BLACK;

    /// The direct M3FX icon graphic currently receiving the animated content paint.
    private @Nullable Node managedGraphic;

    /// The cached inherited-paint channel of [#managedGraphic], or `null` for an arbitrary graphic.
    private @Nullable ObjectProperty<@Nullable Paint> managedGraphicPaint;

    /// Whether paint invalidations belong to the CSS pass resolving a selection target.
    private boolean resolvingSelectionCss;

    /// Creates a toggle icon button skin.
    ///
    /// @param control the toggle icon button controlled by this skin
    public M3IconToggleButtonSkin(M3IconToggleButton control) {
        super(control);
        control.containerColorProperty().addListener(targetPaintInvalidation);
        control.contentColorProperty().addListener(targetPaintInvalidation);
        control.selectedProperty().addListener(selectedInvalidation);
        control.graphicProperty().addListener(graphicInvalidation);
        updateManagedGraphic();
        applyPaints(control.getContainerColor(), control.getContentColor());
    }

    /// Removes visual listeners and inherited icon paint before this skin is disposed.
    @Override
    public void dispose() {
        M3IconToggleButton control = getSkinnable();
        control.containerColorProperty().removeListener(targetPaintInvalidation);
        control.contentColorProperty().removeListener(targetPaintInvalidation);
        control.selectedProperty().removeListener(selectedInvalidation);
        control.graphicProperty().removeListener(graphicInvalidation);
        paintTransition.stop();
        clearManagedGraphic();
        super.dispose();
    }

    /// Resolves selected-state CSS and starts one coordinated color transition.
    private void selectedStateInvalidated() {
        M3IconToggleButton control = getSkinnable();
        resolvingSelectionCss = true;
        try {
            control.applyCss();
        } finally {
            resolvingSelectionCss = false;
        }

        Paint targetContainerPaint = control.getContainerColor();
        Paint targetContentPaint = control.getContentColor();
        if (displayedContainerPaint.equals(targetContainerPaint)
                && displayedContentPaint.equals(targetContentPaint)) {
            return;
        }

        paintTransition.configure(
                M3Animation.fastEffects(control),
                targetContainerPaint,
                targetContentPaint
        );
        M3Animation.playFromStart(control, paintTransition);
    }

    /// Applies target paint changes that are not part of selection CSS synchronously.
    private void targetPaintInvalidated() {
        if (resolvingSelectionCss) {
            return;
        }
        paintTransition.stop();
        M3IconToggleButton control = getSkinnable();
        applyPaints(control.getContainerColor(), control.getContentColor());
    }

    /// Transfers ownership of inherited content paint to the current direct M3FX icon graphic.
    private void updateManagedGraphic() {
        @Nullable Node graphic = getSkinnable().getGraphic();
        @Nullable Node nextManagedGraphic = graphic != null && M3IconPaints.supportsInheritedPaint(graphic)
                ? graphic
                : null;
        if (managedGraphic == nextManagedGraphic) {
            return;
        }
        clearManagedGraphic();
        managedGraphic = nextManagedGraphic;
        if (nextManagedGraphic != null) {
            ObjectProperty<@Nullable Paint> paintProperty = M3IconPaints.inheritedPaintProperty(nextManagedGraphic);
            managedGraphicPaint = paintProperty;
            paintProperty.set(displayedContentPaint);
        }
    }

    /// Releases inherited paint from the previously managed icon graphic.
    private void clearManagedGraphic() {
        managedGraphic = null;
        @Nullable ObjectProperty<@Nullable Paint> paintProperty = managedGraphicPaint;
        managedGraphicPaint = null;
        if (paintProperty != null) {
            paintProperty.set(null);
        }
    }

    /// Applies one concrete paint pair to every visible toggle-button channel.
    ///
    /// @param containerPaint the container paint
    /// @param contentPaint   the icon and interaction paint
    private void applyPaints(Paint containerPaint, Paint contentPaint) {
        displayedContainerPaint = containerPaint;
        displayedContentPaint = contentPaint;
        setContainerPaint(containerPaint);
        setStateLayerPaint(contentPaint);

        M3IconToggleButton control = getSkinnable();
        if (!contentPaint.equals(control.getTextFill())) {
            control.setTextFill(contentPaint);
        }
        @Nullable ObjectProperty<@Nullable Paint> paintProperty = managedGraphicPaint;
        if (paintProperty != null && !contentPaint.equals(paintProperty.get())) {
            paintProperty.set(contentPaint);
        }
    }

    /// Returns an interpolated color or the appropriate endpoint for a non-color paint pair.
    ///
    /// Material theme colors are concrete [Color] values. A custom gradient or image paint remains at its current
    /// endpoint until the transition completes because JavaFX provides no general interpolation contract for
    /// arbitrary [Paint] implementations.
    ///
    /// @param start    the visible start paint
    /// @param target   the target paint
    /// @param fraction the eased transition fraction
    /// @return the paint for the requested fraction
    private static Paint interpolatePaint(Paint start, Paint target, double fraction) {
        if (start instanceof Color startColor && target instanceof Color targetColor) {
            return startColor.interpolate(targetColor, fraction);
        }
        return fraction < 1.0 ? start : target;
    }

    /// Interpolates the retained container and content paints without mutating layout properties.
    @NotNullByDefault
    private final class TogglePaintTransition extends M3FiniteTransition {
        /// The visible container paint captured when the current run starts.
        private Paint startContainerPaint = Color.TRANSPARENT;

        /// The target container paint for the current run.
        private Paint targetContainerPaint = Color.TRANSPARENT;

        /// The visible content paint captured when the current run starts.
        private Paint startContentPaint = Color.BLACK;

        /// The target content paint for the current run.
        private Paint targetContentPaint = Color.BLACK;

        /// Configures a run from the currently rendered paints to new CSS-resolved targets.
        ///
        /// @param spec                 the motion specification for selection effects
        /// @param targetContainerPaint the target container paint
        /// @param targetContentPaint   the target icon and interaction paint
        private void configure(
                M3MotionSpec spec,
                Paint targetContainerPaint,
                Paint targetContentPaint
        ) {
            stop();
            setCycleDuration(spec.duration());
            setInterpolator(spec.interpolator());
            startContainerPaint = displayedContainerPaint;
            startContentPaint = displayedContentPaint;
            this.targetContainerPaint = targetContainerPaint;
            this.targetContentPaint = targetContentPaint;
        }

        /// Applies one eased paint frame.
        ///
        /// @param fraction the eased transition fraction
        @Override
        protected void interpolate(double fraction) {
            applyPaints(
                    interpolatePaint(startContainerPaint, targetContainerPaint, fraction),
                    interpolatePaint(startContentPaint, targetContentPaint, fraction)
            );
        }
    }
}
