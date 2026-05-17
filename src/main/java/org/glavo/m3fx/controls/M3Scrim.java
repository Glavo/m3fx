// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.AccessibleRole;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3Motion;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// A Material Design 3 scrim used behind modal content.
@NotNullByDefault
public class M3Scrim extends Region {
    /// The base style class for M3FX scrims.
    public static final String STYLE_CLASS = "m3-scrim";

    /// The default visible scrim opacity.
    private static final double DEFAULT_VISIBLE_OPACITY = 0.32;

    /// The duration used when a scrim enters.
    private static final Duration SHOW_DURATION = M3Motion.SHORT4;

    /// The duration used when a scrim exits.
    private static final Duration HIDE_DURATION = M3Motion.SHORT2;

    /// The action handler property.
    private final ObjectProperty<@Nullable EventHandler<ActionEvent>> onAction =
            new SimpleObjectProperty<>(this, "onAction") {
                /// Updates the registered action event handler.
                @Override
                protected void invalidated() {
                    setEventHandler(ActionEvent.ACTION, get());
                }
            };

    /// Whether this scrim is shown.
    private final BooleanProperty shown = new SimpleBooleanProperty(this, "shown", true) {
        /// Updates the scrim visibility when the property changes.
        @Override
        protected void invalidated() {
            updateShownState(get());
        }
    };

    /// The scrim show and hide animation.
    private final Timeline visibilityAnimation = new Timeline();

    /// The opacity restored when the scrim is shown again.
    private double visibleOpacity = DEFAULT_VISIBLE_OPACITY;

    /// Creates a scrim.
    public M3Scrim() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.BUTTON);
        setAccessibleText("Dismiss");
        setOpacity(DEFAULT_VISIBLE_OPACITY);
        setPickOnBounds(true);
        setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                fire();
                event.consume();
            }
        });
    }

    /// Returns whether this scrim is shown.
    public final boolean isShown() {
        return shown.get();
    }

    /// Sets whether this scrim is shown.
    public final void setShown(boolean shown) {
        this.shown.set(shown);
    }

    /// Returns the shown property.
    public final BooleanProperty shownProperty() {
        return shown;
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

    /// Shows this scrim using the Material visibility motion.
    public final void show() {
        setShown(true);
    }

    /// Hides this scrim using the Material visibility motion.
    public final void hide() {
        setShown(false);
    }

    /// Returns the user-agent stylesheet for M3FX scrims.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("scrim.css");
    }

    /// Updates shown state with motion when the scrim is attached to a scene.
    private void updateShownState(boolean shown) {
        visibilityAnimation.stop();
        if (shown) {
            setVisible(true);
            setManaged(true);
            if (getScene() == null) {
                applyShownStateImmediately(true);
                return;
            }

            visibilityAnimation.getKeyFrames().setAll(new KeyFrame(
                    SHOW_DURATION,
                    new KeyValue(opacityProperty(), visibleOpacity, M3Motion.STANDARD_DECELERATE)
            ));
            visibilityAnimation.playFromStart();
        } else {
            if (getOpacity() > 0.0) {
                visibleOpacity = getOpacity();
            }
            if (getScene() == null || !isVisible()) {
                applyShownStateImmediately(false);
                return;
            }

            visibilityAnimation.getKeyFrames().setAll(new KeyFrame(
                    HIDE_DURATION,
                    event -> {
                        if (!isShown()) {
                            applyShownStateImmediately(false);
                        }
                    },
                    new KeyValue(opacityProperty(), 0.0, M3Motion.STANDARD_ACCELERATE)
            ));
            visibilityAnimation.playFromStart();
        }
    }

    /// Applies the shown state without animation.
    private void applyShownStateImmediately(boolean shown) {
        visibilityAnimation.stop();
        setVisible(shown);
        setManaged(shown);
        setOpacity(shown ? visibleOpacity : 0.0);
    }
}
