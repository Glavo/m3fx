// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.scene.Node;
import javafx.scene.control.Label;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3NodeTransition;
import org.glavo.m3fx.internal.M3PresentationActivity;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// A slider value label whose transient feedback does not resize its track.
@NotNullByDefault
final class M3SliderValueIndicator extends Label {
    /// Scale at the transparent endpoint.
    private static final double HIDDEN_SCALE = 0.8;
    /// Reused across interactions; allocated only for sliders that display an animated value.
    private @Nullable M3NodeTransition motion;
    /// Whether the current transition is headed toward the visible state.
    private boolean showing;

    /// Creates a hidden, non-interactive label.
    M3SliderValueIndicator() {
        getStyleClass().addAll("m3-slider-value-indicator", "m3-label-large-text");
        setManaged(false);
        setMouseTransparent(true);
        snap(false);
    }

    /// Retargets from the current frame, leaving the label visible throughout its exit.
    void setShowing(Node owner, boolean show) {
        if (showing == show) return;
        if (!M3PresentationActivity.isRenderActive(owner) || !M3Animation.areAnimationsEnabled(owner)) {
            snap(show);
            return;
        }
        showing = show;
        if (motion == null) {
            motion = new M3NodeTransition(this);
            motion.setOnFinished(event -> setVisible(showing));
        }
        setVisible(true);
        double scale = show ? 1 : HIDDEN_SCALE;
        motion.configure(M3Animation.fastEffects(owner), show ? 1 : 0, scale, scale, 0, 0);
        M3Animation.playFromStart(owner, motion);
    }

    /// Settles immediately when motion or the indicator itself is disabled.
    void snap(boolean show) {
        if (motion != null) motion.stop();
        showing = show;
        setOpacity(show ? 1 : 0);
        setScaleX(show ? 1 : HIDDEN_SCALE);
        setScaleY(show ? 1 : HIDDEN_SCALE);
        setVisible(show);
    }
}
