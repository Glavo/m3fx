// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3Motion;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Objects;

/// Hosts transient Material Design 3 snackbar messages.
@NotNullByDefault
public class M3SnackbarHost extends StackPane {
    /// The base style class for m3fx snackbar hosts.
    public static final String STYLE_CLASS = "m3-snackbar-host";

    /// The default snackbar display duration.
    private static final Duration DEFAULT_DISPLAY_DURATION = Duration.seconds(4.0);

    /// The duration used when a snackbar appears.
    private static final Duration SHOW_DURATION = M3Motion.SHORT3;

    /// The duration used when a snackbar disappears.
    private static final Duration HIDE_DURATION = M3Motion.SHORT2;

    /// The initial vertical offset used by snackbar entrance and exit motion.
    private static final double TRANSITION_OFFSET_Y = 16.0;

    /// The currently hosted snackbar.
    private @Nullable M3Snackbar snackbar;

    /// Pending snackbars waiting to be shown.
    private final ObservableList<M3Snackbar> queue = FXCollections.observableArrayList();

    /// Read-only view of pending snackbars.
    private final @UnmodifiableView ObservableList<M3Snackbar> queueView =
            FXCollections.unmodifiableObservableList(queue);

    /// The display duration before automatic dismissal.
    private final ObjectProperty<Duration> displayDuration =
            new SimpleObjectProperty<>(this, "displayDuration", DEFAULT_DISPLAY_DURATION) {
                /// Restores the default duration when the property is set to null.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(DEFAULT_DISPLAY_DURATION);
                    }
                }
            };

    /// Whether the current snackbar is in its visible display phase.
    private final ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing");

    /// The automatic dismissal timer.
    private final PauseTransition displayTimer = new PauseTransition();

    /// The active show animation.
    private final Timeline showAnimation = new Timeline();

    /// The active hide animation.
    private final Timeline hideAnimation = new Timeline();

    /// Creates an empty snackbar host.
    public M3SnackbarHost() {
        getStyleClass().add(STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        setAlignment(Pos.BOTTOM_CENTER);
        setPickOnBounds(false);
        showing.addListener((observable, oldValue, newValue) ->
                notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED));
        queue.addListener((ListChangeListener<M3Snackbar>) change ->
                notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT));
    }

    /// Returns the currently hosted snackbar.
    public final @Nullable M3Snackbar getSnackbar() {
        return snackbar;
    }

    /// Returns whether the current snackbar is in its visible display phase.
    public final boolean isShowing() {
        return showing.get();
    }

    /// Returns the read-only showing state property.
    public final ReadOnlyBooleanProperty showingProperty() {
        return showing.getReadOnlyProperty();
    }

    /// Returns the pending snackbars waiting to be shown.
    public final @UnmodifiableView ObservableList<M3Snackbar> getQueue() {
        return queueView;
    }

    /// Returns the display duration before automatic dismissal.
    ///
    /// A zero, unknown, or indefinite duration disables automatic dismissal.
    public final Duration getDisplayDuration() {
        return displayDuration.get();
    }

    /// Sets the display duration before automatic dismissal.
    ///
    /// A zero, unknown, or indefinite duration disables automatic dismissal.
    public final void setDisplayDuration(Duration displayDuration) {
        this.displayDuration.set(Objects.requireNonNull(displayDuration, "displayDuration"));
    }

    /// Returns the display duration property.
    ///
    /// A zero, unknown, or indefinite duration disables automatic dismissal.
    public final ObjectProperty<Duration> displayDurationProperty() {
        return displayDuration;
    }

    /// Shows a snackbar with message text.
    public final void show(String text) {
        M3Snackbar snackbar = new M3Snackbar(text);
        show(snackbar);
    }

    /// Shows a snackbar with message text, action text, and an optional action handler.
    public final void show(
            String text,
            String actionText,
            @Nullable EventHandler<ActionEvent> actionHandler
    ) {
        M3Snackbar snackbar = new M3Snackbar(text, actionText, event -> {
            dismiss();
            if (actionHandler != null) {
                actionHandler.handle(event);
            }
        });
        show(snackbar);
    }

    /// Adds a snackbar with message text to the end of the display queue.
    public final void enqueue(String text) {
        enqueue(new M3Snackbar(text));
    }

    /// Adds a snackbar with message text, action text, and an optional action handler to the display queue.
    public final void enqueue(
            String text,
            String actionText,
            @Nullable EventHandler<ActionEvent> actionHandler
    ) {
        M3Snackbar snackbar = new M3Snackbar(text, actionText, event -> {
            dismiss();
            if (actionHandler != null) {
                actionHandler.handle(event);
            }
        });
        enqueue(snackbar);
    }

    /// Adds the supplied snackbar to the end of the display queue.
    public final void enqueue(M3Snackbar snackbar) {
        Objects.requireNonNull(snackbar, "snackbar");
        if (this.snackbar == null && !showing.get()) {
            show(snackbar);
        } else {
            queue.add(snackbar);
        }
    }

    /// Clears pending snackbars without dismissing the currently shown snackbar.
    public final void clearQueue() {
        queue.clear();
    }

    /// Shows the supplied snackbar.
    public final void show(M3Snackbar snackbar) {
        Objects.requireNonNull(snackbar, "snackbar");

        displayTimer.stop();
        showAnimation.stop();
        hideAnimation.stop();

        M3Snackbar previousSnackbar = this.snackbar;
        if (previousSnackbar != snackbar) {
            if (previousSnackbar != null) {
                getChildren().remove(previousSnackbar);
                resetSnackbar(previousSnackbar);
            }
            this.snackbar = snackbar;
            if (!getChildren().contains(snackbar)) {
                getChildren().add(snackbar);
            }
            StackPane.setAlignment(snackbar, Pos.BOTTOM_CENTER);
            notifyAccessibleAttributeChanged(AccessibleAttribute.CONTENTS);
            notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
            notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
        }

        snackbar.setManaged(true);
        snackbar.setVisible(true);
        showing.set(true);
        playShowAnimation(snackbar);
    }

    /// Dismisses the currently hosted snackbar.
    public final void dismiss() {
        M3Snackbar currentSnackbar = snackbar;
        if (currentSnackbar == null || !showing.get()) {
            return;
        }

        displayTimer.stop();
        showAnimation.stop();
        showing.set(false);
        playHideAnimation(currentSnackbar);
    }

    /// Clears pending snackbars and dismisses the currently hosted snackbar when one is visible.
    public final void dismissAll() {
        clearQueue();
        dismiss();
    }

    /// Returns the user-agent stylesheet for m3fx snackbar hosts.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("snackbar.css");
    }

    /// Lays out the current snackbar at its preferred size instead of stretching it to the overlay bounds.
    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        M3Snackbar currentSnackbar = snackbar;
        if (currentSnackbar == null || !currentSnackbar.isManaged() || !getChildren().contains(currentSnackbar)) {
            return;
        }

        double left = snappedLeftInset();
        double right = snappedRightInset();
        double top = snappedTopInset();
        double bottom = snappedBottomInset();
        double contentWidth = Math.max(0.0, getWidth() - left - right);
        double contentHeight = Math.max(0.0, getHeight() - top - bottom);
        double width = Math.min(contentWidth, snapSizeX(currentSnackbar.prefWidth(-1.0)));
        double height = Math.min(contentHeight, snapSizeY(currentSnackbar.prefHeight(width)));
        double x = left + (contentWidth - width) / 2.0;
        double y = top + contentHeight - height;

        currentSnackbar.resizeRelocate(snapPositionX(x), snapPositionY(y), width, height);
    }

    /// Returns accessibility attributes for the current snackbar and queue state.
    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case CONTENTS -> getSnackbar();
            case EXPANDED -> isShowing();
            case ITEM_COUNT -> snackbarCount();
            case ITEM_AT_INDEX -> snackbarAt(parameters);
            case TEXT -> currentSnackbarText();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions supported by the snackbar host.
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case COLLAPSE -> dismiss();
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Plays the snackbar entrance animation.
    private void playShowAnimation(M3Snackbar target) {
        target.setOpacity(0.0);
        target.setTranslateY(TRANSITION_OFFSET_Y);
        showAnimation.getKeyFrames().setAll(
                new KeyFrame(
                        SHOW_DURATION,
                        new KeyValue(target.opacityProperty(), 1.0, M3Motion.EMPHASIZED_DECELERATE),
                        new KeyValue(target.translateYProperty(), 0.0, M3Motion.EMPHASIZED_DECELERATE)
                )
        );
        showAnimation.setOnFinished(event -> scheduleAutoDismiss(target));
        showAnimation.playFromStart();
    }

    /// Plays the snackbar exit animation.
    private void playHideAnimation(M3Snackbar target) {
        hideAnimation.stop();
        hideAnimation.getKeyFrames().setAll(
                new KeyFrame(
                        HIDE_DURATION,
                        new KeyValue(target.opacityProperty(), 0.0, M3Motion.EMPHASIZED_ACCELERATE),
                        new KeyValue(target.translateYProperty(), TRANSITION_OFFSET_Y, M3Motion.EMPHASIZED_ACCELERATE)
                )
        );
        hideAnimation.setOnFinished(event -> removeSnackbar(target));
        hideAnimation.playFromStart();
    }

    /// Schedules automatic dismissal for the target snackbar.
    private void scheduleAutoDismiss(M3Snackbar target) {
        if (snackbar != target || !showing.get()) {
            return;
        }

        Duration duration = getDisplayDuration();
        if (duration.isUnknown() || duration.isIndefinite() || duration.lessThanOrEqualTo(Duration.ZERO)) {
            return;
        }

        displayTimer.setDuration(duration);
        displayTimer.setOnFinished(event -> {
            if (snackbar == target) {
                dismiss();
            }
        });
        displayTimer.playFromStart();
    }

    /// Removes the snackbar after its exit transition finishes.
    private void removeSnackbar(M3Snackbar target) {
        if (snackbar != target) {
            return;
        }

        getChildren().remove(target);
        resetSnackbar(target);
        snackbar = null;
        notifyAccessibleAttributeChanged(AccessibleAttribute.CONTENTS);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
        showNextQueuedSnackbar();
    }

    /// Resets a snackbar after it leaves the host.
    private static void resetSnackbar(M3Snackbar target) {
        target.setVisible(false);
        target.setManaged(false);
        target.setOpacity(1.0);
        target.setTranslateY(0.0);
    }

    /// Shows the next queued snackbar when the host is idle.
    private void showNextQueuedSnackbar() {
        if (snackbar != null || queue.isEmpty()) {
            return;
        }

        M3Snackbar nextSnackbar = queue.remove(0);
        show(nextSnackbar);
    }

    /// Returns the number of currently exposed snackbar nodes.
    private int snackbarCount() {
        return (snackbar == null ? 0 : 1) + queue.size();
    }

    /// Returns the current or queued snackbar at the supplied accessibility index.
    private @Nullable Node snackbarAt(Object... parameters) {
        int index = M3Accessible.indexParameter(parameters);
        if (index < 0) {
            return null;
        }
        if (snackbar != null) {
            if (index == 0) {
                return snackbar;
            }
            index--;
        }
        return index < queue.size() ? queue.get(index) : null;
    }

    /// Returns text for the currently hosted snackbar.
    private String currentSnackbarText() {
        M3Snackbar currentSnackbar = snackbar;
        if (currentSnackbar == null) {
            return "";
        }
        @Nullable String text = currentSnackbar.getAccessibleText();
        return text == null ? "" : text;
    }
}
