// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Describes one observable Material Design 3 snackbar message.
///
/// A snackbar contains required supporting text, an optional text action, and an optional close button. It is a
/// presentation model rather than a scene-graph node. [M3OverlayPane] queues messages and renders the current
/// message as a Material snackbar surface.
///
/// Every content field is exposed as a JavaFX property and may be bound to application state such as localized
/// resources. Changes made while this message is current are reflected by the existing snackbar surface without
/// replacing that surface. The supporting [text][#textProperty()] must remain non-blank. The action button is shown
/// exactly when [actionText][#actionTextProperty()] is non-blank; its [action][#actionProperty()] may be `null`, in
/// which case activation performs no callback before dismissing the snackbar.
///
/// A close button dismisses the current snackbar without invoking the text action. Messages with a visible text
/// action or close button remain visible until explicitly dismissed; plain messages use the overlay pane's
/// configured display duration. Changing either affordance while the message is current updates that timeout
/// behavior.
///
/// After a message has been submitted to an [M3OverlayPane], its properties must be changed on the JavaFX
/// Application Thread. Changes to a pending message become visible when that message becomes current. Enqueuing the
/// same instance more than once shares one observable state between those queue entries.
///
/// Material Design does not define leading graphics or arbitrary trailing content for snackbars. Applications that
/// need richer transient surfaces should present a custom non-modal overlay through
/// [M3OverlayPane#showOverlay(javafx.scene.Node)].
///
/// See [Material Design snackbars](https://m3.material.io/components/snackbar/overview).
@NotNullByDefault
public final class M3Snackbar {

    /// Creates a plain snackbar message without an action or close button.
    ///
    /// @param text the non-blank supporting text
    /// @throws IllegalArgumentException if `text` is blank
    /// @throws NullPointerException     if `text` is `null`
    public M3Snackbar(String text) {
        setText(text);
    }

    /// The required supporting text.
    ///
    /// Callers and bound sources must not assign `null` or a blank string.
    ///
    /// @defaultValue the text supplied to the constructor
    private final StringProperty text = new SimpleStringProperty(this, "text", "") {
        /// Rejects values that cannot form a Material snackbar message.
        @Override
        protected void invalidated() {
            requireNonBlankText(get());
        }
    };

    /// Returns the supporting text.
    ///
    /// @return the non-blank supporting text
    public String getText() {
        return text.get();
    }

    /// Sets the supporting text.
    ///
    /// Bound text properties cannot be set directly.
    ///
    /// @param text the non-blank supporting text
    /// @throws IllegalArgumentException if `text` is blank
    /// @throws NullPointerException     if `text` is `null`
    /// @throws RuntimeException         if this property is bound
    public void setText(String text) {
        this.text.set(requireNonBlankText(text));
    }

    /// Returns the property containing the supporting text.
    ///
    /// The property may be bound to an observable localization source. Bound sources must not produce `null` or
    /// blank values.
    ///
    /// The returned property is observable and bindable. Its initial value is the text supplied to the constructor.
    ///
    /// @return the supporting-text property
    public StringProperty textProperty() {
        return text;
    }

    /// The optional text-action label.
    ///
    /// Empty and blank values hide the action button. Callers and bound sources must not assign `null`.
    ///
    /// @defaultValue `""`
    private final StringProperty actionText = new SimpleStringProperty(this, "actionText", "") {
        /// Rejects null action labels while allowing blank labels to omit the action.
        @Override
        protected void invalidated() {
            Objects.requireNonNull(get(), "actionText");
        }
    };

    /// Returns the optional text-action label.
    ///
    /// @return the action label, or an empty or blank string when no action button is shown
    public String getActionText() {
        return actionText.get();
    }

    /// Sets the optional text-action label.
    ///
    /// Bound action-text properties cannot be set directly.
    ///
    /// @param actionText the action label, or an empty or blank string to hide the action button
    /// @throws NullPointerException if `actionText` is `null`
    /// @throws RuntimeException     if this property is bound
    public void setActionText(String actionText) {
        this.actionText.set(Objects.requireNonNull(actionText, "actionText"));
    }

    /// Returns the property containing the optional text-action label.
    ///
    /// The property may be bound to an observable localization source. Bound sources must not produce `null`.
    ///
    /// The returned property is observable and bindable. Its default value is `""`.
    ///
    /// @return the action-text property
    public StringProperty actionTextProperty() {
        return actionText;
    }

    /// The callback invoked when the visible text action is activated.
    ///
    /// A `null` value is treated as a no-op. The snackbar is dismissed after activation regardless of whether a
    /// callback is installed.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Runnable> action =
            new SimpleObjectProperty<>(this, "action");

    /// Returns the callback invoked by text-action activation.
    ///
    /// @return the callback, or `null` when activation performs no callback
    public @Nullable Runnable getAction() {
        return action.get();
    }

    /// Sets the callback invoked by text-action activation.
    ///
    /// The callback does not control action-button visibility. Bound action properties cannot be set directly.
    ///
    /// @param action the callback, or `null` to perform no callback
    /// @throws RuntimeException if this property is bound
    public void setAction(@Nullable Runnable action) {
        this.action.set(action);
    }

    /// Returns the property containing the optional text-action callback.
    ///
    /// The returned property is observable and bindable. Its default value is `null`.
    ///
    /// @return the action-callback property
    public ObjectProperty<@Nullable Runnable> actionProperty() {
        return action;
    }

    /// Whether the standard close affordance is shown.
    ///
    /// @defaultValue `false`
    private final BooleanProperty closeButtonVisible =
            new SimpleBooleanProperty(this, "closeButtonVisible");

    /// Returns whether the standard close affordance is visible.
    ///
    /// @return `true` when a close button is shown
    public boolean isCloseButtonVisible() {
        return closeButtonVisible.get();
    }

    /// Sets whether the standard close affordance is visible.
    ///
    /// Bound visibility properties cannot be set directly.
    ///
    /// @param closeButtonVisible whether a close button is shown
    /// @throws RuntimeException if this property is bound
    public void setCloseButtonVisible(boolean closeButtonVisible) {
        this.closeButtonVisible.set(closeButtonVisible);
    }

    /// Returns the property controlling standard close-affordance visibility.
    ///
    /// The returned property is observable and bindable. Its default value is `false`.
    ///
    /// @return the close-button visibility property
    public BooleanProperty closeButtonVisibleProperty() {
        return closeButtonVisible;
    }

    /// Returns whether this message currently exposes a text action.
    ///
    /// Visibility depends only on [#getActionText()]; the callback may be `null`.
    ///
    /// @return `true` when the action label is non-blank
    public boolean hasAction() {
        return !getActionText().isBlank();
    }

    /// Validates and returns required supporting text.
    ///
    /// @param value the value to validate
    /// @return the validated value
    /// @throws IllegalArgumentException if `value` is blank
    /// @throws NullPointerException     if `value` is `null`
    private static String requireNonBlankText(String value) {
        String nonNullValue = Objects.requireNonNull(value, "text");
        if (nonNullValue.isBlank()) {
            throw new IllegalArgumentException("text must not be blank");
        }
        return nonNullValue;
    }
}
