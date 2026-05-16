// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// A Material Design 3 scrim used behind modal content.
@NotNullByDefault
public class M3Scrim extends Region {
    /// The base style class for M3FX scrims.
    public static final String STYLE_CLASS = "m3-scrim";

    /// The action handler property.
    private final ObjectProperty<@Nullable EventHandler<ActionEvent>> onAction =
            new SimpleObjectProperty<>(this, "onAction") {
                /// Updates the registered action event handler.
                @Override
                protected void invalidated() {
                    setEventHandler(ActionEvent.ACTION, get());
                }
            };

    /// Creates a scrim.
    public M3Scrim() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setPickOnBounds(true);
        setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                fire();
                event.consume();
            }
        });
    }

    /// Returns the action handler.
    public final @Nullable EventHandler<ActionEvent> getOnAction() {
        return onAction.get();
    }

    /// Sets the action handler.
    public final void setOnAction(@Nullable EventHandler<ActionEvent> onAction) {
        this.onAction.set(onAction);
    }

    /// Returns the action handler property.
    public final ObjectProperty<@Nullable EventHandler<ActionEvent>> onActionProperty() {
        return onAction;
    }

    /// Fires this scrim's action event.
    public final void fire() {
        if (!isDisabled()) {
            Event.fireEvent(this, new ActionEvent(this, this));
        }
    }

    /// Returns the user-agent stylesheet for M3FX scrims.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("scrim.css");
    }
}
