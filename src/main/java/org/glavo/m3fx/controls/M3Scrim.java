// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A Material Design 3 scrim used behind modal content.
///
/// `M3Scrim` is a non-content overlay that dims the scene behind modal sheets, dialogs, and other blocking
/// surfaces. It exposes shown state, visible opacity, action events for outside-click dismissal, keyboard
/// dismissal, and Material fade motion.
///
/// See [Material Design](https://m3.material.io/) for modal surface and overlay behavior.
@NotNullByDefault
public class M3Scrim extends Region {
    /// The base style class for M3FX scrims.
    public static final String STYLE_CLASS = "m3-scrim";

    /// The default visible scrim opacity.
    private static final double DEFAULT_VISIBLE_OPACITY = 0.32;

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
            notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
        }
    };

    /// The opacity used while this scrim is shown.
    private final DoubleProperty visibleOpacity =
            new SimpleDoubleProperty(this, "visibleOpacity", DEFAULT_VISIBLE_OPACITY) {
                /// Applies the updated visible opacity when the scrim is shown.
                @Override
                protected void invalidated() {
                    set(validateOpacity(get()));
                    if (isShown()) {
                        setOpacity(get());
                    }
                }
            };

    /// Whether primary mouse clicks fire this scrim's action event.
    private final BooleanProperty dismissOnClick = new SimpleBooleanProperty(this, "dismissOnClick", true);

    /// The scrim show and hide animation.
    private final Timeline visibilityAnimation = new Timeline();

    /// Creates a scrim.
    public M3Scrim() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.BUTTON);
        setAccessibleText("Dismiss");
        setFocusTraversable(true);
        setOpacity(getVisibleOpacity());
        setPickOnBounds(true);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        setOnMouseClicked(event -> {
            if (isDismissOnClick() && event.getButton() == MouseButton.PRIMARY) {
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

    /// Returns the opacity used while this scrim is shown.
    public final double getVisibleOpacity() {
        return visibleOpacity.get();
    }

    /// Sets the opacity used while this scrim is shown.
    public final void setVisibleOpacity(double visibleOpacity) {
        this.visibleOpacity.set(validateOpacity(visibleOpacity));
    }

    /// Returns the visible opacity property.
    public final DoubleProperty visibleOpacityProperty() {
        return visibleOpacity;
    }

    /// Returns whether primary mouse clicks fire this scrim's action event.
    public final boolean isDismissOnClick() {
        return dismissOnClick.get();
    }

    /// Sets whether primary mouse clicks fire this scrim's action event.
    public final void setDismissOnClick(boolean dismissOnClick) {
        this.dismissOnClick.set(dismissOnClick);
    }

    /// Returns the dismiss-on-click property.
    public final BooleanProperty dismissOnClickProperty() {
        return dismissOnClick;
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

    /// Returns accessibility attributes for scrim visibility state.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case EXPANDED -> isShown();
            case FOCUS_NODE -> isShown() ? this : null;
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes assistive-technology actions supported by this scrim.
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case FIRE -> fire();
            case EXPAND, SHOW_ITEM -> show();
            case COLLAPSE -> hide();
            case REQUEST_FOCUS -> {
                if (isShown()) {
                    requestFocus();
                }
            }
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Handles keyboard activation for focused scrims.
    private void handleKeyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case ENTER, SPACE, ESCAPE -> {
                fire();
                event.consume();
            }
            default -> {
            }
        }
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

            M3MotionSpec spec = M3Animation.defaultEffects(this);
            visibilityAnimation.getKeyFrames().setAll(new KeyFrame(
                    spec.duration(),
                    new KeyValue(opacityProperty(), getVisibleOpacity(), spec.interpolator())
            ));
            M3Animation.playFromStart(this, visibilityAnimation);
        } else {
            if (getScene() == null || !isVisible()) {
                applyShownStateImmediately(false);
                return;
            }

            M3MotionSpec spec = M3Animation.fastEffects(this);
            visibilityAnimation.getKeyFrames().setAll(new KeyFrame(
                    spec.duration(),
                    event -> {
                        if (!isShown()) {
                            applyShownStateImmediately(false);
                        }
                    },
                    new KeyValue(opacityProperty(), 0.0, spec.interpolator())
            ));
            M3Animation.playFromStart(this, visibilityAnimation);
        }
    }

    /// Applies the shown state without animation.
    private void applyShownStateImmediately(boolean shown) {
        visibilityAnimation.stop();
        setVisible(shown);
        setManaged(shown);
        setOpacity(shown ? getVisibleOpacity() : 0.0);
    }

    /// Validates a normalized opacity value.
    private static double validateOpacity(double opacity) {
        if (opacity < 0.0 || opacity > 1.0) {
            throw new IllegalArgumentException("visibleOpacity must be between 0.0 and 1.0");
        }
        return opacity;
    }
}
