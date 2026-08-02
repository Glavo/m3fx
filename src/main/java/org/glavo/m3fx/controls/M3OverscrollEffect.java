// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.geometry.Orientation;
import javafx.scene.input.ScrollEvent;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.DoubleUnaryOperator;

/// Decorates bounded scroll operations with an independently rendered overscroll effect.
///
/// [#applyToScroll(Orientation, double, ScrollEvent, DoubleUnaryOperator)] receives the complete available pixel
/// delta and a callback that performs the bounded scroll. An implementation may consume part of the delta before
/// invoking the callback, for example to relax an existing effect, and may consume any remainder afterward to
/// produce its visual response. The callback must be invoked exactly once. Logical scroll values remain owned by
/// the callback and are not permitted to leave their configured ranges.
///
/// An effect instance is stateful and may be attached to only one [M3ScrollPane] at a time. The owning pane's
/// [M3ScrollPane#overscrollInputModeProperty()] determines which inputs reach the effect. Setting
/// [M3ScrollPane#overscrollEffectProperty()] to `null` disables overscroll. Implementations must release rendering
/// resources and remove changes made to the scene graph when detached.
///
/// This class follows the JavaFX scene-graph threading model. Once its scroll pane is attached to a showing scene,
/// methods that mutate the effect must be called on the JavaFX Application Thread.
///
/// See [Compose OverscrollEffect](https://developer.android.com/reference/kotlin/androidx/compose/foundation/OverscrollEffect).
@NotNullByDefault
public abstract class M3OverscrollEffect {
    /// The scroll pane currently presenting this effect, or `null` while detached.
    private @Nullable M3ScrollPane scrollPane;

    /// Creates a detached overscroll effect.
    protected M3OverscrollEffect() {
    }

    /// Applies this effect around one bounded scroll operation.
    ///
    /// The `performScroll` callback accepts the pixel delta made available to the logical scroll state and returns
    /// the amount it consumed. Both callback values and this method's return value use the same sign as `delta` and
    /// must not have a greater magnitude. The callback must be invoked exactly once, including when `delta` is zero.
    /// The returned value is the sum consumed by the callback and by this effect.
    ///
    /// @param orientation   the axis to which the delta applies
    /// @param delta         the finite pixel delta available on that axis
    /// @param event         the JavaFX event that produced the delta
    /// @param performScroll the bounded logical scroll operation
    /// @return the finite pixel delta consumed by the complete decorated operation
    /// @throws NullPointerException     if any reference argument is `null`
    /// @throws IllegalStateException    if this effect is not attached to a scroll pane
    /// @throws IllegalArgumentException if `delta` is not finite
    public final double applyToScroll(
            Orientation orientation,
            double delta,
            ScrollEvent event,
            DoubleUnaryOperator performScroll
    ) {
        Objects.requireNonNull(orientation, "orientation");
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(performScroll, "performScroll");
        if (!Double.isFinite(delta)) {
            throw new IllegalArgumentException("delta must be finite");
        }
        getScrollPane();
        return onApplyToScroll(orientation, delta, event, performScroll);
    }

    /// Implements the pre-scroll, bounded-scroll, and post-scroll phases for one axis.
    ///
    /// @implSpec An implementation must invoke `performScroll` exactly once. Its argument, callback result, and this
    /// method's result must be finite, retain the sign of `delta`, and not exceed its magnitude. The result must
    /// include all delta consumed before, during, and after the callback.
    ///
    /// @param orientation   the axis to which the delta applies
    /// @param delta         the finite pixel delta available on that axis
    /// @param event         the JavaFX event that produced the delta
    /// @param performScroll the bounded logical scroll operation
    /// @return the finite pixel delta consumed by the complete decorated operation
    protected abstract double onApplyToScroll(
            Orientation orientation,
            double delta,
            ScrollEvent event,
            DoubleUnaryOperator performScroll
    );

    /// Releases any active pull and animates the visual effect back to its resting state.
    ///
    /// Repeated calls are permitted. Reduced-motion settings and an unavailable presentation context may cause the
    /// effect to settle synchronously.
    ///
    /// @throws IllegalStateException if this effect is not attached to a scroll pane
    public final void release() {
        getScrollPane();
        onRelease();
    }

    /// Implements release of any active pull or animation.
    ///
    /// @implSpec An implementation must permit repeated calls and must eventually return to an inactive resting
    /// state. It must settle synchronously when its resolved reduced-motion policy requires that behavior.
    protected abstract void onRelease();

    /// Returns whether this effect is pulled or is animating toward its resting state.
    ///
    /// @return `true` while an overscroll response is in progress
    public abstract boolean isInProgress();

    /// Returns the scroll pane currently presenting this effect.
    ///
    /// @return the attached scroll pane
    /// @throws IllegalStateException if this effect is detached
    protected final M3ScrollPane getScrollPane() {
        @Nullable M3ScrollPane owner = scrollPane;
        if (owner == null) {
            throw new IllegalStateException("The overscroll effect is not attached to an M3ScrollPane");
        }
        return owner;
    }

    /// Responds after this effect has been attached to a scroll pane.
    ///
    /// The default implementation does nothing. An override may install passive observation but should defer
    /// rendering resources until they are needed when practical.
    ///
    /// @param scrollPane the newly attached scroll pane
    protected void onAttached(M3ScrollPane scrollPane) {
    }

    /// Responds before this effect is detached from a scroll pane.
    ///
    /// The default implementation does nothing. An override must synchronously stop its animations, remove its
    /// listeners, and undo scene-graph changes associated with the supplied pane.
    ///
    /// @param scrollPane the scroll pane being detached
    protected void onDetached(M3ScrollPane scrollPane) {
    }

    /// Attaches this effect to one scroll pane.
    ///
    /// @param scrollPane the scroll pane that will present this effect
    private void attach(M3ScrollPane scrollPane) {
        M3ScrollPane checkedPane = Objects.requireNonNull(scrollPane, "scrollPane");
        checkAttachable(checkedPane);
        if (this.scrollPane == checkedPane) {
            return;
        }

        this.scrollPane = checkedPane;
        try {
            onAttached(checkedPane);
        } catch (RuntimeException | Error exception) {
            this.scrollPane = null;
            throw exception;
        }
    }

    /// Verifies that this effect is detached or already owned by the supplied scroll pane.
    ///
    /// @param scrollPane the prospective owner
    private void checkAttachable(M3ScrollPane scrollPane) {
        if (this.scrollPane != null && this.scrollPane != scrollPane) {
            throw new IllegalStateException("An M3OverscrollEffect cannot be shared by multiple scroll panes");
        }
    }

    /// Detaches this effect from its current scroll pane.
    ///
    /// @param scrollPane the scroll pane that currently presents this effect
    private void detach(M3ScrollPane scrollPane) {
        if (this.scrollPane != scrollPane) {
            return;
        }
        try {
            onDetached(scrollPane);
        } finally {
            this.scrollPane = null;
        }
    }

    /// Connects an effect to an M3 scroll pane.
    ///
    /// @param effect     the effect to attach
    /// @param scrollPane the owning scroll pane
    static void attach(M3OverscrollEffect effect, M3ScrollPane scrollPane) {
        Objects.requireNonNull(effect, "effect").attach(scrollPane);
    }

    /// Verifies that an effect may be connected to an M3 scroll pane.
    ///
    /// @param effect     the prospective effect, or `null`
    /// @param scrollPane the prospective owner
    static void checkAttachable(@Nullable M3OverscrollEffect effect, M3ScrollPane scrollPane) {
        if (effect != null) {
            effect.checkAttachable(Objects.requireNonNull(scrollPane, "scrollPane"));
        }
    }

    /// Disconnects an effect from an M3 scroll pane.
    ///
    /// @param effect     the effect to detach
    /// @param scrollPane the owning scroll pane
    static void detach(M3OverscrollEffect effect, M3ScrollPane scrollPane) {
        Objects.requireNonNull(effect, "effect").detach(scrollPane);
    }
}
