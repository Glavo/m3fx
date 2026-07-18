// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import org.glavo.m3fx.internal.M3DialogPresentation;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Controls one presentation of an [M3Dialog].
///
/// A handle is created by [M3OverlayPane#showDialog(M3Dialog)] or [M3DialogWindow#showDialog(M3Dialog)]. It remains
/// associated with the dialog after the presentation ends, but releases its presentation backend when the dialog is
/// fully hidden. Retaining an old handle therefore cannot close a later presentation of the same dialog.
@NotNullByDefault
public final class M3DialogHandle {
    /// The dialog represented by this presentation.
    private final M3Dialog dialog;

    /// Whether this presentation still occupies its host surface.
    private boolean showing;

    /// Lazily created observable view of the showing state.
    private @Nullable ReadOnlyBooleanWrapper showingProperty;

    /// The backend currently hosting this presentation, or `null` after it is hidden.
    private @Nullable M3DialogPresentation presentation;

    /// Creates a detached-state handle allocated for one presentation backend before installation begins.
    ///
    /// @param dialog       the dialog represented by this handle
    /// @param presentation the backend represented by this handle
    /// @throws NullPointerException if `dialog` or `presentation` is `null`
    M3DialogHandle(M3Dialog dialog, M3DialogPresentation presentation) {
        this.dialog = Objects.requireNonNull(dialog, "dialog");
        this.presentation = Objects.requireNonNull(presentation, "presentation");
    }

    /// Returns the dialog represented by this presentation.
    ///
    /// @return the non-null dialog
    public M3Dialog getDialog() {
        return dialog;
    }

    /// Returns whether this presentation still occupies its host surface.
    ///
    /// The value remains `true` while an accepted exit transition is running and becomes `false` immediately before
    /// the dialog's hidden lifecycle event is dispatched.
    ///
    /// @return `true` while this presentation is showing or leaving
    public boolean isShowing() {
        return showing;
    }

    /// Returns the read-only property reporting this presentation's visible lifecycle state.
    ///
    /// @return the read-only showing property
    public ReadOnlyBooleanProperty showingProperty() {
        @Nullable ReadOnlyBooleanWrapper property = showingProperty;
        if (property == null) {
            property = new ReadOnlyBooleanWrapper(this, "showing", showing);
            showingProperty = property;
        }
        return property.getReadOnlyProperty();
    }

    /// Requests that this presentation close without selecting an action button.
    ///
    /// The request emits the dialog's cancellable close-request event. It has no effect after this handle is hidden,
    /// while another close request is being dispatched, or while an accepted exit transition is already running.
    /// This method must run on the JavaFX Application Thread.
    ///
    /// @return `true` if this call started an accepted close transition; `false` otherwise
    /// @throws IllegalStateException if called off the JavaFX Application Thread
    public boolean requestClose() {
        return dialog.requestClose(this);
    }

    /// Returns whether this handle belongs to an exact presentation backend and has not detached.
    ///
    /// @param candidate the backend to compare by identity
    /// @return `true` when `candidate` is this handle's current backend
    boolean belongsTo(M3DialogPresentation candidate) {
        return presentation == candidate;
    }

    /// Marks this handle as occupying its installed host surface.
    void markShowing() {
        setShowing(true);
    }

    /// Detaches this handle from its backend after failed presentation or completed hiding.
    void detach() {
        presentation = null;
        setShowing(false);
    }

    /// Updates the presentation state and its observable view when one has been requested.
    ///
    /// @param value the new showing state
    private void setShowing(boolean value) {
        if (showing == value) {
            return;
        }
        showing = value;
        @Nullable ReadOnlyBooleanWrapper property = showingProperty;
        if (property != null) {
            property.set(value);
        }
    }
}
