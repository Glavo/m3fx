// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Describes one Material Design 3 snackbar message.
///
/// A snackbar contains required supporting text, at most one optional text action, and an optional close button.
/// It is presentation data rather than a scene-graph node. [M3OverlayPane] queues messages and installs each current
/// message into its single reusable snackbar presenter.
///
/// The text and action label must not be blank. Activating an action runs its handler and then dismisses the current
/// snackbar. A close button dismisses the current snackbar without invoking the action. Messages with an action or
/// close button remain visible until explicitly dismissed; plain messages use the overlay pane's configured display
/// duration.
///
/// Material Design does not define leading graphics or arbitrary trailing content for snackbars. Applications that
/// need richer transient surfaces should present a custom non-modal overlay through
/// [M3OverlayPane#showOverlay(javafx.scene.Node)].
///
/// See [Material Design snackbars](https://m3.material.io/components/snackbar/overview).
///
/// @param text the non-blank supporting text
/// @param action the optional text action
/// @param closeButtonVisible whether the presenter renders the standard close affordance
@NotNullByDefault
public record M3Snackbar(String text, @Nullable Action action, boolean closeButtonVisible) {
    /// Creates a plain snackbar message without an action or close button.
    ///
    /// @param text the non-blank supporting text
    /// @throws IllegalArgumentException if `text` is blank
    /// @throws NullPointerException     if `text` is `null`
    public M3Snackbar(String text) {
        this(text, null, false);
    }

    /// Creates a snackbar message with one text action and no close button.
    ///
    /// @param text   the non-blank supporting text
    /// @param action the action presented after the supporting text
    /// @throws IllegalArgumentException if `text` is blank
    /// @throws NullPointerException     if `text` or `action` is `null`
    public M3Snackbar(String text, Action action) {
        this(text, Objects.requireNonNull(action, "action"), false);
    }

    /// Creates a snackbar message with an optional text action and optional close button.
    ///
    /// @param text               the non-blank supporting text
    /// @param action             the optional text action
    /// @param closeButtonVisible whether the standard close affordance is shown
    /// @throws IllegalArgumentException if `text` is blank
    /// @throws NullPointerException     if `text` is `null`
    public M3Snackbar {
        text = requireNonBlank(text, "text");
    }

    /// Returns the supporting text.
    ///
    /// @return the non-blank supporting text
    @Override
    public String text() {
        return text;
    }

    /// Returns the optional text action.
    ///
    /// @return the action, or `null` when the message is not actionable
    @Override
    public @Nullable Action action() {
        return action;
    }

    /// Returns whether this message has a text action.
    ///
    /// @return `true` when an action is present
    public boolean hasAction() {
        return action != null;
    }

    /// Returns whether the standard close affordance is visible.
    ///
    /// @return `true` when the presenter renders a close button
    @Override
    public boolean closeButtonVisible() {
        return closeButtonVisible;
    }

    /// Validates and returns one required non-blank string.
    ///
    /// @param value the value to validate
    /// @param name the parameter name used in validation failures
    /// @return the validated value
    /// @throws IllegalArgumentException if `value` is blank
    /// @throws NullPointerException     if `value` is `null`
    private static String requireNonBlank(String value, String name) {
        String nonNullValue = Objects.requireNonNull(value, name);
        if (nonNullValue.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return nonNullValue;
    }

    /// Describes the single optional text action of a snackbar message.
    ///
    /// The handler runs on the JavaFX Application Thread after the presenter's action button is activated. The
    /// presenter dismisses the current snackbar after the handler returns.
    ///
    /// @param text the non-blank action label
    /// @param handler the callback invoked by action activation
    @NotNullByDefault
    public record Action(String text, Runnable handler) {
        /// Creates a text action.
        ///
        /// @param text    the non-blank action label
        /// @param handler the callback invoked by action activation
        /// @throws IllegalArgumentException if `text` is blank
        /// @throws NullPointerException     if `text` or `handler` is `null`
        public Action {
            text = requireNonBlank(text, "action text");
            Objects.requireNonNull(handler, "handler");
        }

        /// Returns the action button label.
        ///
        /// @return the non-blank action label
        @Override
        public String text() {
            return text;
        }

        /// Returns the callback invoked by action activation.
        ///
        /// @return the action callback
        @Override
        public Runnable handler() {
            return handler;
        }
    }
}
