// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

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
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3NodeTransition;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A Material Design 3 scrim used behind modal content.
///
/// `M3Scrim` is a non-content overlay that dims the scene behind modal sheets, dialogs, and other blocking
/// surfaces. It does not impose modality or manage the overlaid surface; applications place it in the appropriate
/// parent and coordinate its [#shownProperty()] with that surface.
///
/// A primary click fires an [ActionEvent] when [#isDismissOnClick()] is `true`. Enter, Space, and Escape also fire
/// the action while the scrim has focus. Firing the event does not hide the scrim automatically; the action handler
/// decides whether to call [#hide()]. Disabled scrims do not fire action events. The scrim is shown by default.
///
/// See [Material Design](https://m3.material.io/) for modal surface and overlay behavior.
@NotNullByDefault
public final class M3Scrim extends Region {
    /// The base style class for M3FX scrims.
    public static final String STYLE_CLASS = "m3-scrim";

    /// The default visible scrim opacity.
    private static final double DEFAULT_VISIBLE_OPACITY = 0.32;

    /// The handler invoked for this scrim's action events.
    ///
    /// A `null` value removes the handler.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable EventHandler<ActionEvent>> onAction =
            new SimpleObjectProperty<>(this, "onAction") {
                /// Updates the registered action event handler.
                @Override
                protected void invalidated() {
                    setEventHandler(ActionEvent.ACTION, get());
                }
            };

    /// Whether this scrim is logically shown.
    ///
    /// Setting this property to `true` makes the node visible and managed before its entrance transition. Setting it
    /// to `false` starts the exit transition and makes the node invisible and unmanaged after that transition. When
    /// the node is not attached to a scene, the final state is applied immediately.
    ///
    /// @defaultValue `true`
    private final BooleanProperty shown = new SimpleBooleanProperty(this, "shown", true) {
        /// Updates the scrim visibility when the property changes.
        @Override
        protected void invalidated() {
            updateShownState(get());
            notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
        }
    };

    /// The opacity used when the scrim is fully shown.
    ///
    /// Values below `0.0` or above `1.0` are rejected, including values assigned through the property.
    ///
    /// @defaultValue `0.32`
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

    /// Whether a primary mouse click fires the scrim's action event.
    ///
    /// This property does not control keyboard activation and does not hide the scrim by itself.
    ///
    /// @defaultValue `true`
    private final BooleanProperty dismissOnClick = new SimpleBooleanProperty(this, "dismissOnClick", true);

    /// The scrim show and hide animation.
    private final M3NodeTransition visibilityAnimation = new M3NodeTransition(this);

    /// Creates a shown, focus-traversable scrim with `0.32` visible opacity and click activation enabled.
    public M3Scrim() {
        M3ControlStyles.initialize(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.BUTTON);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleNode, parameters -> parameters.length == 0 && showAccessibleItem());
        setAccessibleText("Dismiss");
        setFocusTraversable(true);
        setOpacity(getVisibleOpacity());
        setPickOnBounds(true);
        visibilityAnimation.setOnFinished(event -> {
            if (!isShown()) {
                applyShownStateImmediately(false);
            }
        });
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        setOnMouseClicked(event -> {
            if (isDismissOnClick() && event.getButton() == MouseButton.PRIMARY) {
                fire();
                event.consume();
            }
        });
    }

    /// Returns whether this scrim is shown.
    ///
    /// @return `true` if the scrim is visible and managed
    public final boolean isShown() {
        return shown.get();
    }

    /// Sets whether this scrim is shown.
    ///
    /// @param shown whether the scrim should be visible and managed
    public final void setShown(boolean shown) {
        this.shown.set(shown);
    }

    public final BooleanProperty shownProperty() {
        return shown;
    }

    /// Returns the opacity used while this scrim is shown.
    ///
    /// @return the opacity used while this scrim is shown
    public final double getVisibleOpacity() {
        return visibleOpacity.get();
    }

    /// Sets the opacity used while this scrim is shown.
    ///
    /// @param visibleOpacity the normalized opacity used while this scrim is shown
    /// @throws IllegalArgumentException if the value is outside `0.0..1.0`
    public final void setVisibleOpacity(double visibleOpacity) {
        this.visibleOpacity.set(validateOpacity(visibleOpacity));
    }

    public final DoubleProperty visibleOpacityProperty() {
        return visibleOpacity;
    }

    /// Returns whether primary mouse clicks fire this scrim's action event.
    ///
    /// @return `true` if primary mouse clicks fire this scrim's action event
    public final boolean isDismissOnClick() {
        return dismissOnClick.get();
    }

    /// Sets whether primary mouse clicks fire this scrim's action event.
    ///
    /// @param dismissOnClick whether primary mouse clicks fire this scrim's action event
    public final void setDismissOnClick(boolean dismissOnClick) {
        this.dismissOnClick.set(dismissOnClick);
    }

    public final BooleanProperty dismissOnClickProperty() {
        return dismissOnClick;
    }

    /// Returns the action handler.
    ///
    /// @return the action handler, or `null` if none is set
    public final @Nullable EventHandler<ActionEvent> getOnAction() {
        return onAction.get();
    }

    /// Sets the action handler.
    ///
    /// @param onAction the action handler, or `null` to clear it
    public final void setOnAction(@Nullable EventHandler<ActionEvent> onAction) {
        this.onAction.set(onAction);
    }

    public final ObjectProperty<@Nullable EventHandler<ActionEvent>> onActionProperty() {
        return onAction;
    }

    /// Fires this scrim's action event unless the scrim is disabled.
    ///
    /// This method does not change [#shownProperty()].
    public final void fire() {
        if (!isDisabled()) {
            Event.fireEvent(this, new ActionEvent(this, this));
        }
    }

    /// Shows this scrim.
    ///
    /// Calling this method when the scrim is already shown has no effect.
    public final void show() {
        setShown(true);
    }

    /// Hides this scrim.
    ///
    /// Calling this method when the scrim is already hidden has no effect.
    public final void hide() {
        setShown(false);
    }

    /// Returns the user-agent stylesheet for M3FX scrims.
    ///
    /// @return the scrim user-agent stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("scrim.css");
    }

    /// Returns accessibility attributes for scrim visibility state.
    ///
    /// @param attribute the requested accessibility attribute
    /// @param parameters the optional attribute parameters
    /// @return the attribute value, or `null` when unavailable
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case EXPANDED -> isShown();
            case FOCUS_NODE -> isShown() && M3Accessible.isEffectivelyReachable(this) ? this : null;
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes assistive-technology actions supported by this scrim.
    ///
    /// @param action the requested accessibility action
    /// @param parameters the optional action parameters
    /// @throws NullPointerException if `action` is `null`
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        if (isDisabled()) {
            super.executeAccessibleAction(action, parameters);
            return;
        }

        switch (action) {
            case FIRE -> fire();
            case EXPAND, SHOW_ITEM -> showAccessibleItem();
            case COLLAPSE -> hide();
            case REQUEST_FOCUS -> focusAccessibleNode();
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Shows this scrim through an accessibility reveal request.
    ///
    /// @return `true` when the scrim can be revealed
    final boolean showAccessibleItem() {
        if (!M3Accessible.canReveal(this)) {
            return false;
        }
        show();
        return true;
    }

    /// Requests focus on this scrim when it is visible and reachable.
    ///
    /// @return `true` when this scrim accepted focus
    final boolean focusAccessibleNode() {
        return isShown() && M3Accessible.canReach(this) && M3Accessible.showDirectItem(this, this);
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
            visibilityAnimation.configure(
                    spec,
                    getVisibleOpacity(),
                    getScaleX(),
                    getScaleY(),
                    getTranslateX(),
                    getTranslateY()
            );
            M3Animation.playFromStart(this, visibilityAnimation);
        } else {
            if (getScene() == null || !isVisible()) {
                applyShownStateImmediately(false);
                return;
            }

            M3MotionSpec spec = M3Animation.fastEffects(this);
            visibilityAnimation.configure(
                    spec,
                    0.0,
                    getScaleX(),
                    getScaleY(),
                    getTranslateX(),
                    getTranslateY()
            );
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
