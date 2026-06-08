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
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3SnackbarHostSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Objects;

/// Hosts transient Material Design 3 snackbar messages.
///
/// `M3SnackbarHost` owns the visible snackbar slot and a FIFO queue of pending [M3Snackbar] instances. It
/// coordinates display duration, action dismissal, automatic timeout, and Material entrance and exit motion. Add
/// one host near the root of an application scene and call its show methods from feature code that needs
/// non-modal feedback.
///
/// See [Material Design snackbars](https://m3.material.io/components/snackbar/overview).
@NotNullByDefault
public class M3SnackbarHost extends Control {
    /// The base style class for m3fx snackbar hosts.
    public static final String STYLE_CLASS = "m3-snackbar-host";

    /// The initial vertical offset used by snackbar entrance and exit motion.
    private static final double TRANSITION_OFFSET_Y = 16.0;

    // Backing property for the public read-only current snackbar API.
    private final ReadOnlyObjectWrapper<@Nullable M3Snackbar> snackbar =
            new ReadOnlyObjectWrapper<>(this, "snackbar");

    /// Pending snackbars waiting to be shown.
    private final ObservableList<M3Snackbar> queue = FXCollections.observableArrayList();

    /// Read-only view of pending snackbars.
    private final @UnmodifiableView ObservableList<M3Snackbar> queueView =
            FXCollections.unmodifiableObservableList(queue);

    // Backing property for the public display duration API.
    private final ObjectProperty<@Nullable Duration> displayDuration =
            new SimpleObjectProperty<>(this, "displayDuration") {
                /// Keeps explicit display durations non-negative.
                @Override
                protected void invalidated() {
                    @Nullable Duration duration = get();
                    if (duration != null && duration.lessThan(Duration.ZERO)) {
                        set(Duration.ZERO);
                    }
                }
            };

    // Backing property for the public read-only showing state API.
    private final ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing");

    /// The automatic dismissal timer.
    private final PauseTransition displayTimer = new PauseTransition();

    /// The active show animation.
    private final Timeline showAnimation = new Timeline();

    /// The active hide animation.
    private final Timeline hideAnimation = new Timeline();

    /// Observes runtime motion settings while this host is attached to a scene.
    private final M3MotionSettingsObserver motionSettingsObserver =
            new M3MotionSettingsObserver(this, this::refreshMotionSettings);

    /// Reports hosted snackbar focus changes to accessibility clients.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, this::currentFocusNode);

    /// Creates an empty snackbar host.
    public M3SnackbarHost() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        setPickOnBounds(false);
        showing.addListener((observable, oldValue, newValue) -> {
            notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
            notifyFocusNodeChanged();
        });
        queue.addListener((ListChangeListener<M3Snackbar>) change ->
                notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT));
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        focusNotifier.start();
    }

    /// Returns the currently hosted snackbar.
    ///
    /// @return the currently hosted snackbar, or `null` when the host is idle
    public final @Nullable M3Snackbar getSnackbar() {
        return snackbar.get();
    }

    /// Returns the currently hosted snackbar property.
    ///
    /// @return the read-only currently hosted snackbar property
    public final ReadOnlyObjectProperty<@Nullable M3Snackbar> snackbarProperty() {
        return snackbar.getReadOnlyProperty();
    }

    /// Returns whether the current snackbar is in its visible display phase.
    ///
    /// @return `true` if the current snackbar is in its visible display phase
    public final boolean isShowing() {
        return showing.get();
    }

    /// Returns the read-only showing state property.
    ///
    /// @return the read-only showing state property
    public final ReadOnlyBooleanProperty showingProperty() {
        return showing.getReadOnlyProperty();
    }

    /// Returns the pending snackbars waiting to be shown.
    ///
    /// @return the pending snackbars waiting to be shown
    public final @UnmodifiableView ObservableList<M3Snackbar> getQueue() {
        return queueView;
    }

    /// Returns the display duration before automatic dismissal.
    ///
    /// A null value resolves from the active [org.glavo.m3fx.animation.M3MotionBehavior]. A zero, unknown, or
    /// indefinite duration disables automatic dismissal.
    ///
    /// @return the display duration before automatic dismissal
    public final Duration getDisplayDuration() {
        @Nullable Duration duration = displayDuration.get();
        return duration == null ? M3Animation.motionBehavior(this).snackbarDisplayDuration() : duration;
    }

    /// Sets the display duration before automatic dismissal.
    ///
    /// A zero, unknown, or indefinite duration disables automatic dismissal. Set [displayDurationProperty] to
    /// null to restore motion-behavior defaults.
    ///
    /// @param displayDuration the display duration before automatic dismissal
    public final void setDisplayDuration(Duration displayDuration) {
        this.displayDuration.set(Objects.requireNonNull(displayDuration, "displayDuration"));
    }

    /// Returns the display duration property.
    ///
    /// A null value resolves from the active [org.glavo.m3fx.animation.M3MotionBehavior]. A zero, unknown, or
    /// indefinite duration disables automatic dismissal.
    ///
    /// @return the display duration property
    public final ObjectProperty<@Nullable Duration> displayDurationProperty() {
        return displayDuration;
    }

    /// Shows a snackbar with message text.
    ///
    /// @param text the snackbar message text
    public final void show(String text) {
        M3Snackbar snackbar = new M3Snackbar(text);
        show(snackbar);
    }

    /// Shows a snackbar with message text, action text, and an optional action handler.
    ///
    /// @param text the snackbar message text
    /// @param actionText the action button text
    /// @param actionHandler the action handler, or `null` for none
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
    ///
    /// @param text the snackbar message text
    public final void enqueue(String text) {
        enqueue(new M3Snackbar(text));
    }

    /// Adds a snackbar with message text, action text, and an optional action handler to the display queue.
    ///
    /// @param text the snackbar message text
    /// @param actionText the action button text
    /// @param actionHandler the action handler, or `null` for none
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
    ///
    /// @param snackbar the snackbar to enqueue
    public final void enqueue(M3Snackbar snackbar) {
        Objects.requireNonNull(snackbar, "snackbar");
        if (getSnackbar() == null && !showing.get()) {
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
    ///
    /// @param snackbar the snackbar to show
    public final void show(M3Snackbar snackbar) {
        Objects.requireNonNull(snackbar, "snackbar");

        displayTimer.stop();
        showAnimation.stop();
        hideAnimation.stop();

        @Nullable M3Snackbar previousSnackbar = getSnackbar();
        if (previousSnackbar != snackbar) {
            if (previousSnackbar != null) {
                resetSnackbar(previousSnackbar);
            }
            setSnackbar(snackbar);
            notifyAccessibleAttributeChanged(AccessibleAttribute.CONTENTS);
            notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
            notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
            notifyFocusNodeChanged();
        }

        snackbar.setManaged(true);
        snackbar.setVisible(true);
        showing.set(true);
        refreshAccessibleFocusNode();
        playShowAnimation(snackbar);
    }

    /// Dismisses the currently hosted snackbar.
    public final void dismiss() {
        @Nullable M3Snackbar currentSnackbar = getSnackbar();
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
    ///
    /// @return the snackbar user-agent stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("snackbar.css");
    }

    /// Creates the default Material Design 3 snackbar host skin.
    ///
    /// @return the default Material Design 3 snackbar host skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3SnackbarHostSkin(this);
    }

    /// Returns accessibility attributes for the current snackbar and queue state.
    ///
    /// @param attribute the requested accessibility attribute
    /// @param parameters the optional attribute parameters
    /// @return the attribute value, or `null` when unavailable
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case CONTENTS -> getSnackbar();
            case EXPANDED -> isShowing();
            case FOCUS_NODE -> currentFocusNode();
            case ITEM_COUNT -> snackbarCount();
            case ITEM_AT_INDEX -> snackbarAt(parameters);
            case TEXT -> currentSnackbarText();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions supported by the snackbar host.
    ///
    /// @param action the requested accessibility action
    /// @param parameters the optional action parameters
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case COLLAPSE -> dismiss();
            case REQUEST_FOCUS -> {
                M3Accessible.showItem(currentFocusNode());
                notifyFocusNodeChanged();
            }
            case SHOW_ITEM -> showAccessibleSnackbar(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Handles keyboard dismissal while focus is inside the snackbar host.
    private void handleKeyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case ESCAPE -> {
                if (isShowing()) {
                    dismiss();
                    event.consume();
                }
            }
            default -> {
            }
        }
    }

    /// Plays the snackbar entrance animation.
    private void playShowAnimation(M3Snackbar target) {
        target.setOpacity(0.0);
        target.setTranslateY(TRANSITION_OFFSET_Y);
        M3MotionSpec spec = M3Animation.fastSpatial(this);
        showAnimation.getKeyFrames().setAll(
                new KeyFrame(
                        spec.duration(),
                        new KeyValue(target.opacityProperty(), 1.0, spec.interpolator()),
                        new KeyValue(target.translateYProperty(), 0.0, spec.interpolator())
                )
        );
        showAnimation.setOnFinished(event -> scheduleAutoDismiss(target));
        M3Animation.playFromStart(this, showAnimation);
    }

    /// Plays the snackbar exit animation.
    private void playHideAnimation(M3Snackbar target) {
        hideAnimation.stop();
        M3MotionSpec spec = M3Animation.fastSpatial(this);
        hideAnimation.getKeyFrames().setAll(
                new KeyFrame(
                        spec.duration(),
                        new KeyValue(target.opacityProperty(), 0.0, spec.interpolator()),
                        new KeyValue(target.translateYProperty(), TRANSITION_OFFSET_Y, spec.interpolator())
                )
        );
        hideAnimation.setOnFinished(event -> removeSnackbar(target));
        M3Animation.playFromStart(this, hideAnimation);
    }

    /// Applies changed runtime motion settings to the active snackbar animations.
    private void refreshMotionSettings() {
        M3Animation.finishRunningAnimationsIfDisabled(this, showAnimation, hideAnimation);
    }

    /// Schedules automatic dismissal for the target snackbar.
    private void scheduleAutoDismiss(M3Snackbar target) {
        if (getSnackbar() != target || !showing.get()) {
            return;
        }

        Duration duration = getDisplayDuration();
        if (duration.isUnknown() || duration.isIndefinite() || duration.lessThanOrEqualTo(Duration.ZERO)) {
            return;
        }

        displayTimer.setDuration(duration);
        displayTimer.setOnFinished(event -> {
            if (getSnackbar() == target) {
                dismiss();
            }
        });
        displayTimer.playFromStart();
    }

    /// Removes the snackbar after its exit transition finishes.
    private void removeSnackbar(M3Snackbar target) {
        if (getSnackbar() != target) {
            return;
        }

        resetSnackbar(target);
        setSnackbar(null);
        notifyAccessibleAttributeChanged(AccessibleAttribute.CONTENTS);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
        notifyFocusNodeChanged();
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
        if (getSnackbar() != null || queue.isEmpty()) {
            return;
        }

        M3Snackbar nextSnackbar = queue.remove(0);
        show(nextSnackbar);
    }

    /// Returns the number of currently exposed snackbar nodes.
    private int snackbarCount() {
        return (getSnackbar() == null ? 0 : 1) + queue.size();
    }

    /// Returns the current or queued snackbar at the supplied accessibility index.
    private @Nullable Node snackbarAt(Object... parameters) {
        int index = M3Accessible.indexParameter(parameters);
        if (index < 0) {
            return null;
        }
        @Nullable M3Snackbar currentSnackbar = getSnackbar();
        if (currentSnackbar != null) {
            if (index == 0) {
                return currentSnackbar;
            }
            index--;
        }
        return index < queue.size() ? queue.get(index) : null;
    }

    /// Returns the preferred focus node for the current snackbar.
    private @Nullable Node currentFocusNode() {
        @Nullable M3Snackbar currentSnackbar = getSnackbar();
        if (currentSnackbar == null || !isShowing()) {
            return null;
        }

        @Nullable Object focusNode = currentSnackbar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);
        return focusNode instanceof Node node ? node : null;
    }

    /// Refreshes the current snackbar skin before reporting its focus node to accessibility clients.
    private void refreshAccessibleFocusNode() {
        @Nullable M3Snackbar currentSnackbar = getSnackbar();
        if (currentSnackbar != null) {
            currentSnackbar.applyCss();
        }
        notifyFocusNodeChanged();
    }

    /// Notifies and refreshes cached accessibility focus state.
    private void notifyFocusNodeChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Shows or focuses the snackbar referenced by accessibility action parameters.
    private void showAccessibleSnackbar(Object... parameters) {
        @Nullable M3Snackbar target = accessibleSnackbar(parameters);
        if (target == null) {
            return;
        }

        if (target == getSnackbar()) {
            refreshAccessibleFocusNode();
            M3Accessible.showItem(currentFocusNode());
            refreshAccessibleFocusNode();
            return;
        }
        if (queue.remove(target)) {
            show(target);
            M3Accessible.showItem(currentFocusNode());
            refreshAccessibleFocusNode();
        }
    }

    /// Returns the snackbar referenced by accessibility action parameters.
    private @Nullable M3Snackbar accessibleSnackbar(Object... parameters) {
        if (parameters.length == 0) {
            @Nullable M3Snackbar currentSnackbar = getSnackbar();
            return currentSnackbar != null ? currentSnackbar : (queue.isEmpty() ? null : queue.get(0));
        }

        int index = M3Accessible.indexParameter(parameters);
        if (index >= 0) {
            Node item = snackbarAt(parameters);
            return item instanceof M3Snackbar snackbar ? snackbar : null;
        }

        @Nullable M3Snackbar currentSnackbar = getSnackbar();
        if (currentSnackbar != null && M3Accessible.containsNodeTarget(currentSnackbar, parameters)) {
            return currentSnackbar;
        }
        for (M3Snackbar queuedSnackbar : queue) {
            if (M3Accessible.containsNodeTarget(queuedSnackbar, parameters)) {
                return queuedSnackbar;
            }
        }
        return null;
    }

    /// Returns text for the currently hosted snackbar.
    private String currentSnackbarText() {
        @Nullable M3Snackbar currentSnackbar = getSnackbar();
        if (currentSnackbar == null) {
            return "";
        }
        @Nullable String text = currentSnackbar.getAccessibleText();
        return text == null ? "" : text;
    }

    /// Sets the currently hosted snackbar.
    private void setSnackbar(@Nullable M3Snackbar snackbar) {
        this.snackbar.set(snackbar);
    }
}
