// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.animation.Animation;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import org.glavo.m3fx.controls.M3ScrollPane;
import org.glavo.m3fx.internal.animation.M3DoubleTransition;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.function.DoubleSupplier;

/// Applies Material scroll bars and retargetable vertical scrolling to a virtualized list.
@NotNullByDefault
public final class M3VirtualFlowScroll {
    /// Smallest meaningful normalized change in scroll position.
    private static final double EPSILON = 0.000001;
    /// The node whose theme and motion preferences govern the scroll.
    private final Node owner;
    /// Receives wheel input before the virtual flow's native wheel handler.
    private final Node input;
    /// The underlying cell viewport.
    private final VirtualFlow<?> flow;
    /// Returns the range of scrollable content in logical pixels.
    private final DoubleSupplier range;
    /// Independent animation coordinate; VirtualFlow remains authoritative about actual cell positions.
    private final DoubleProperty position = new SimpleDoubleProperty();
    /// Drives wheel and indexed scroll requests through the same motion channel.
    private final M3DoubleTransition motion = new M3DoubleTransition(position, EPSILON, 0, 1);
    /// Last animated position applied to the flow in pixels.
    private double applied;
    /// Accumulated wheel destination.
    private double target;
    /// Optional final indexed alignment.
    private @Nullable Runnable completion;
    /// Handles indirect vertical wheel input.
    private final EventHandler<ScrollEvent> wheel = this::scroll;
    /// Releases the viewport to direct manipulation, including thumb drags.
    private final EventHandler<MouseEvent> press = event -> stop();

    /// Installs scroll handling on the list and styles its flow's own scroll bars.
    public M3VirtualFlowScroll(Node owner, Node input, VirtualFlow<?> flow, DoubleSupplier range) {
        this.owner = owner;
        this.input = input;
        this.flow = flow;
        this.range = range;
        for (Node child : flow.getChildrenUnmodifiable()) {
            if (child instanceof ScrollBar bar) M3ScrollPane.style(bar);
        }
        position.addListener(obs -> {
            double next = position.get();
            double pixels = (next - applied) * range.getAsDouble();
            applied = next;
            if (Math.abs(pixels) > EPSILON && flow.getScene() != null) flow.scrollPixels(pixels);
        });
        motion.setOnFinished(event -> {
            @Nullable Runnable callback = completion;
            completion = null;
            flow.setPosition(target);
            flow.requestLayout();
            if (callback != null) callback.run();
        });
        input.addEventFilter(ScrollEvent.SCROLL, wheel);
        input.addEventFilter(MouseEvent.MOUSE_PRESSED, press);
    }

    /// Leaves nested scrolling controls, horizontal input and native direct or inertial gestures alone.
    private void scroll(ScrollEvent event) {
        if (event.isConsumed()) return;
        if (event.getTarget() instanceof Node node && node != input) {
            while (node != flow) {
                if (node instanceof ScrollPane || node instanceof VirtualFlow<?> || node.getParent() == null) return;
                node = node.getParent();
            }
        }
        if (event.isDirect() || event.isInertia() || event.isShiftDown()) {
            stop();
            return;
        }
        double pixels = switch (event.getTextDeltaYUnits()) {
            case LINES -> event.getTextDeltaY() * 40.0;
            case PAGES -> event.getTextDeltaY() * flow.getHeight();
            case NONE -> event.getDeltaY();
        };
        double distance = range.getAsDouble();
        if (Math.abs(pixels) <= EPSILON || distance <= EPSILON) return;
        boolean running = motion.getStatus() == Animation.Status.RUNNING;
        double from = running ? target : flow.getPosition();
        double to = Math.max(0, Math.min(1, from - pixels / distance));
        if (Math.abs(to - from) > EPSILON) {
            if (M3Animation.areAnimationsEnabled(owner)) {
                animateTo(to, null);
            } else {
                stop();
                flow.setPosition(to);
                flow.requestLayout();
            }
            event.consume();
        } else if (running) {
            // The last wheel notch already targets the edge; do not let native scrolling race it.
            event.consume();
        }
    }

    /// Scrolls to a normalized position, optionally aligning an exact cell after settling.
    public void animateTo(double destination, @Nullable Runnable onFinished) {
        if (motion.getStatus() != Animation.Status.RUNNING) {
            applied = flow.getPosition();
            position.set(applied);
        }
        target = destination;
        completion = onFinished;
        motion.configure(M3Animation.defaultSpatial(owner), destination);
        M3Animation.playFromStart(owner, motion);
    }

    /// Cancels a pending scroll without jumping to its old destination.
    public void stop() {
        completion = null;
        motion.stop();
    }

    /// Releases motion observation and input filters.
    public void dispose() {
        stop();
        input.removeEventFilter(ScrollEvent.SCROLL, wheel);
        input.removeEventFilter(MouseEvent.MOUSE_PRESSED, press);
    }
}
