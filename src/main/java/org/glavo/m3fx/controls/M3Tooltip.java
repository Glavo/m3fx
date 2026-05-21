// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.animation.PauseTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionBehavior;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.skins.M3TooltipSkin;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A Material Design 3 tooltip.
///
/// `M3Tooltip` is a [PopupControl] that can be installed on any JavaFX node. It manages show and hide delays,
/// pointer and keyboard triggers, owner-window tracking, theme inheritance for popup content, accessible help
/// text, and Material entrance and exit motion.
///
/// Use [M3RichTooltip] when the popup needs a title, supporting text, or action row. See
/// [Material Design tooltips](https://m3.material.io/components/tooltips/overview).
@NotNullByDefault
public class M3Tooltip extends PopupControl {
    /// The base style class for M3FX tooltips.
    public static final String STYLE_CLASS = "m3-tooltip";

    /// The vertical offset between an owner node and the popup.
    private static final double POPUP_VERTICAL_OFFSET = 8.0;

    /// The node property key used to store theme inheritance listeners.
    private static final String THEME_INHERITANCE_LISTENER_KEY =
            M3Tooltip.class.getName() + ".themeInheritanceListener";

    /// The node property key used to store tooltip activation handlers.
    private static final String INSTALLATION_KEY =
            M3Tooltip.class.getName() + ".installation";

    /// The node property key used to store accessible help bindings.
    private static final String ACCESSIBLE_HELP_BINDING_KEY =
            M3Tooltip.class.getName() + ".accessibleHelpBinding";

    /// The node property key used to store the accessible help value replaced during installation.
    private static final String ACCESSIBLE_HELP_PREVIOUS_VALUE_KEY =
            M3Tooltip.class.getName() + ".accessibleHelpPreviousValue";

    /// The sentinel used when a target node had no previous accessible help value.
    private static final Object ACCESSIBLE_HELP_NULL_VALUE = new Object();

    /// The text displayed by the tooltip.
    private final StringProperty text = new SimpleStringProperty(this, "text", "");

    /// The graphic displayed by the tooltip.
    private final ObjectProperty<@Nullable Node> graphic =
            new SimpleObjectProperty<>(this, "graphic");

    /// The text and graphic placement mode.
    private final ObjectProperty<ContentDisplay> contentDisplay =
            new ObjectPropertyBase<>(ContentDisplay.LEFT) {
                /// Keeps the content display mode non-null.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(ContentDisplay.LEFT);
                    }
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Tooltip.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "contentDisplay";
                }
            };

    /// Whether tooltip text wraps inside its preferred width.
    private final BooleanProperty wrapText = new BooleanPropertyBase(false) {
        /// Returns the owning bean.
        @Override
        public Object getBean() {
            return M3Tooltip.this;
        }

        /// Returns the property name.
        @Override
        public String getName() {
            return "wrapText";
        }
    };

    /// The delay before the tooltip opens after pointer entry.
    private final ObjectProperty<Duration> showDelay =
            new DurationProperty("showDelay", M3MotionBehavior.standard().tooltipShowDelay());

    /// The delay before the tooltip closes after pointer exit.
    private final ObjectProperty<Duration> hideDelay =
            new DurationProperty("hideDelay", M3MotionBehavior.standard().tooltipHideDelay());

    /// The maximum duration the tooltip remains visible after pointer-triggered opening.
    private final ObjectProperty<Duration> showDuration =
            new DurationProperty("showDuration", M3MotionBehavior.standard().tooltipShowDuration());

    /// The explicit theme applied directly to this tooltip.
    private final ObjectProperty<@Nullable M3Theme> theme = new SimpleObjectProperty<>(this, "theme") {
        /// Applies theme declarations to the tooltip style.
        @Override
        protected void invalidated() {
            themeInherited = applyingInheritedTheme;
            applyTheme(get());
        }
    };

    /// The tooltip style before theme declarations were added.
    private @Nullable String baseStyle;

    /// Whether the current theme value was inherited from the target node scene.
    private boolean themeInherited;

    /// Whether the current theme mutation is applying an inherited value.
    private boolean applyingInheritedTheme;

    /// Whether the show delay was explicitly assigned by application code.
    private boolean showDelayExplicit;

    /// Whether the hide delay was explicitly assigned by application code.
    private boolean hideDelayExplicit;

    /// Whether the show duration was explicitly assigned by application code.
    private boolean showDurationExplicit;

    /// Creates an empty tooltip.
    public M3Tooltip() {
        initialize();
    }

    /// Creates a tooltip with text.
    public M3Tooltip(String text) {
        initialize();
        setText(text);
    }

    /// Installs a Material Design 3 tooltip with the supplied text on a node.
    public static M3Tooltip install(Node node, String text) {
        M3Tooltip tooltip = new M3Tooltip(text);
        install(node, tooltip);
        return tooltip;
    }

    /// Installs a Material Design 3 tooltip on a node.
    public static void install(Node node, M3Tooltip tooltip) {
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(tooltip, "tooltip");

        installThemeInheritance(node, tooltip);
        installAccessibleHelp(node, tooltip);
        installActivation(node, tooltip);
        tooltip.inheritThemeFrom(node);
    }

    /// Uninstalls a Material Design 3 tooltip from a node.
    public static void uninstall(Node node, M3Tooltip tooltip) {
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(tooltip, "tooltip");

        uninstallThemeInheritance(node);
        uninstallAccessibleHelp(node);
        uninstallActivation(node, tooltip);
    }

    /// Returns the displayed text.
    public final @Nullable String getText() {
        return text.get();
    }

    /// Sets the displayed text.
    public final void setText(@Nullable String text) {
        this.text.set(text);
    }

    /// Returns the displayed text property.
    public final StringProperty textProperty() {
        return text;
    }

    /// Returns the displayed graphic.
    public final @Nullable Node getGraphic() {
        return graphic.get();
    }

    /// Sets the displayed graphic.
    public final void setGraphic(@Nullable Node graphic) {
        this.graphic.set(graphic);
    }

    /// Returns the displayed graphic property.
    public final ObjectProperty<@Nullable Node> graphicProperty() {
        return graphic;
    }

    /// Returns the text and graphic placement mode.
    public final ContentDisplay getContentDisplay() {
        return contentDisplay.get();
    }

    /// Sets the text and graphic placement mode.
    public final void setContentDisplay(ContentDisplay contentDisplay) {
        this.contentDisplay.set(Objects.requireNonNull(contentDisplay, "contentDisplay"));
    }

    /// Returns the content display property.
    public final ObjectProperty<ContentDisplay> contentDisplayProperty() {
        return contentDisplay;
    }

    /// Returns whether tooltip text wraps inside its preferred width.
    public final boolean isWrapText() {
        return wrapText.get();
    }

    /// Sets whether tooltip text wraps inside its preferred width.
    public final void setWrapText(boolean wrapText) {
        this.wrapText.set(wrapText);
    }

    /// Returns the wrap text property.
    public final BooleanProperty wrapTextProperty() {
        return wrapText;
    }

    /// Returns the delay before the tooltip opens after pointer entry.
    public final Duration getShowDelay() {
        return showDelay.get();
    }

    /// Sets the delay before the tooltip opens after pointer entry.
    public final void setShowDelay(Duration showDelay) {
        showDelayExplicit = true;
        this.showDelay.set(Objects.requireNonNull(showDelay, "showDelay"));
    }

    /// Returns the show delay property.
    public final ObjectProperty<Duration> showDelayProperty() {
        return showDelay;
    }

    /// Returns the delay before the tooltip closes after pointer exit.
    public final Duration getHideDelay() {
        return hideDelay.get();
    }

    /// Sets the delay before the tooltip closes after pointer exit.
    public final void setHideDelay(Duration hideDelay) {
        hideDelayExplicit = true;
        this.hideDelay.set(Objects.requireNonNull(hideDelay, "hideDelay"));
    }

    /// Returns the hide delay property.
    public final ObjectProperty<Duration> hideDelayProperty() {
        return hideDelay;
    }

    /// Returns the maximum duration the tooltip remains visible after pointer-triggered opening.
    public final Duration getShowDuration() {
        return showDuration.get();
    }

    /// Sets the maximum duration the tooltip remains visible after pointer-triggered opening.
    public final void setShowDuration(Duration showDuration) {
        showDurationExplicit = true;
        this.showDuration.set(Objects.requireNonNull(showDuration, "showDuration"));
    }

    /// Returns the show duration property.
    public final ObjectProperty<Duration> showDurationProperty() {
        return showDuration;
    }

    /// Returns the explicit theme applied directly to this tooltip.
    public final @Nullable M3Theme getTheme() {
        return theme.get();
    }

    /// Sets the explicit theme applied directly to this tooltip.
    public final void setTheme(@Nullable M3Theme theme) {
        applyingInheritedTheme = false;
        this.theme.set(theme);
    }

    /// Returns the explicit theme property.
    public final ObjectProperty<@Nullable M3Theme> themeProperty() {
        return theme;
    }

    /// Creates the default Material Design 3 tooltip skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3TooltipSkin(this);
    }

    /// Adds base style classes and Material timing defaults.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setWrapText(true);
        setAutoFix(true);
        setAutoHide(true);
        setHideOnEscape(true);
    }

    /// Sets the default visible duration without marking it as an application override.
    protected final void setDefaultShowDuration(Duration showDuration) {
        showDurationExplicit = false;
        this.showDuration.set(Objects.requireNonNull(showDuration, "showDuration"));
    }

    /// Returns the default visible duration for this tooltip kind from a behavior profile.
    protected Duration defaultShowDuration(M3MotionBehavior behavior) {
        return behavior.tooltipShowDuration();
    }

    /// Returns the effective show delay for an installed target node.
    private Duration effectiveShowDelay(Node owner) {
        return showDelayExplicit ? getShowDelay() : M3Animation.motionBehavior(owner).tooltipShowDelay();
    }

    /// Returns the effective hide delay for an installed target node.
    private Duration effectiveHideDelay(Node owner) {
        return hideDelayExplicit ? getHideDelay() : M3Animation.motionBehavior(owner).tooltipHideDelay();
    }

    /// Returns the effective visible duration for an installed target node.
    private Duration effectiveShowDuration(Node owner) {
        if (showDurationExplicit) {
            return getShowDuration();
        }
        return defaultShowDuration(M3Animation.motionBehavior(owner));
    }

    /// Applies the node's scene theme when this tooltip has no explicit theme.
    private void inheritThemeFrom(Node node) {
        inheritThemeFrom(node.getScene());
    }

    /// Applies the scene theme when this tooltip has no explicit theme.
    private void inheritThemeFrom(@Nullable Scene scene) {
        if (getTheme() != null && !themeInherited) {
            return;
        }

        if (scene == null) {
            if (themeInherited) {
                setInheritedTheme(null);
            }
            return;
        }

        @Nullable M3Theme inheritedTheme = M3ThemeManager.getTheme(scene);
        if (inheritedTheme != null) {
            setInheritedTheme(inheritedTheme);
        } else if (themeInherited) {
            setInheritedTheme(null);
        }
    }

    /// Sets a theme value that came from the target node scene.
    private void setInheritedTheme(@Nullable M3Theme theme) {
        applyingInheritedTheme = true;
        try {
            this.theme.set(theme);
        } finally {
            applyingInheritedTheme = false;
        }
    }

    /// Applies or clears inline theme declarations on the tooltip.
    private void applyTheme(@Nullable M3Theme theme) {
        if (theme == null) {
            M3ThemeManager.clearThemeStyleClasses(this);
            String currentBaseStyle = baseStyle;
            if (currentBaseStyle != null) {
                setStyle(currentBaseStyle);
                baseStyle = null;
            }
            return;
        }

        if (baseStyle == null) {
            baseStyle = getStyle();
        }
        M3ThemeManager.applyThemeStyleClasses(this, theme);
        setStyle(mergeStyles(baseStyle, theme.toRootStyleDeclarations()));
    }

    /// Merges existing tooltip style declarations with generated theme declarations.
    private static String mergeStyles(String baseStyle, String themeStyle) {
        if (baseStyle.isBlank()) {
            return themeStyle;
        }
        return baseStyle.stripTrailing() + " " + themeStyle;
    }

    /// Installs a listener that keeps inherited tooltip themes in sync with the target node scene.
    private static void installThemeInheritance(Node node, M3Tooltip tooltip) {
        uninstallThemeInheritance(node);

        SceneThemeListener listener = new SceneThemeListener(tooltip);
        node.sceneProperty().addListener(listener);
        node.getProperties().put(THEME_INHERITANCE_LISTENER_KEY, listener);
    }

    /// Removes any previously installed theme inheritance listener from a node.
    private static void uninstallThemeInheritance(Node node) {
        Object listener = node.getProperties().remove(THEME_INHERITANCE_LISTENER_KEY);
        if (listener instanceof SceneThemeListener sceneThemeListener) {
            node.sceneProperty().removeListener(sceneThemeListener);
        }
    }

    /// Installs pointer activation handlers on the tooltip target.
    private static void installActivation(Node node, M3Tooltip tooltip) {
        uninstallActivation(node, null);

        TooltipInstallation installation = new TooltipInstallation(node, tooltip);
        installation.install();
        node.getProperties().put(INSTALLATION_KEY, installation);
    }

    /// Removes pointer activation handlers from the tooltip target.
    private static void uninstallActivation(Node node, @Nullable M3Tooltip tooltip) {
        Object installation = node.getProperties().get(INSTALLATION_KEY);
        if (!(installation instanceof TooltipInstallation tooltipInstallation)) {
            return;
        }
        if (tooltip != null && tooltipInstallation.tooltip != tooltip) {
            return;
        }

        node.getProperties().remove(INSTALLATION_KEY);
        tooltipInstallation.uninstall();
    }

    /// Installs an accessible help binding on the tooltip target.
    private static void installAccessibleHelp(Node node, M3Tooltip tooltip) {
        uninstallAccessibleHelp(node);

        @Nullable String previousHelp = node.getAccessibleHelp();
        node.getProperties().put(
                ACCESSIBLE_HELP_PREVIOUS_VALUE_KEY,
                previousHelp == null ? ACCESSIBLE_HELP_NULL_VALUE : previousHelp
        );
        ChangeListener<@Nullable String> listener = (observable, oldValue, newValue) ->
                node.setAccessibleHelp(accessibleHelpText(newValue));
        tooltip.textProperty().addListener(listener);
        node.getProperties().put(ACCESSIBLE_HELP_BINDING_KEY, new AccessibleHelpBinding(tooltip, listener));
        node.setAccessibleHelp(accessibleHelpText(tooltip.getText()));
    }

    /// Removes an accessible help binding and restores the previous node help value.
    private static void uninstallAccessibleHelp(Node node) {
        Object binding = node.getProperties().remove(ACCESSIBLE_HELP_BINDING_KEY);
        if (!(binding instanceof AccessibleHelpBinding accessibleHelpBinding)) {
            return;
        }

        accessibleHelpBinding.uninstall();
        Object previousValue = node.getProperties().remove(ACCESSIBLE_HELP_PREVIOUS_VALUE_KEY);
        node.setAccessibleHelp(previousValue == ACCESSIBLE_HELP_NULL_VALUE ? null : (String) previousValue);
    }

    /// Returns text suitable for a node accessible help value.
    private static @Nullable String accessibleHelpText(@Nullable String text) {
        return text == null || text.isBlank() ? null : text;
    }

    /// Returns whether a duration can be used by a finite timer.
    private static boolean isFiniteDuration(Duration duration) {
        return !duration.isUnknown() && !duration.isIndefinite();
    }

    /// Stores pointer handlers installed on a tooltip target node.
    @NotNullByDefault
    private static final class TooltipInstallation {
        /// The target node that owns the tooltip activation handlers.
        private final Node node;

        /// The tooltip controlled by the installed handlers.
        private final M3Tooltip tooltip;

        /// The delayed opening timer.
        private final PauseTransition showTimer = new PauseTransition();

        /// The delayed closing timer.
        private final PauseTransition hideTimer = new PauseTransition();

        /// The timer used to auto-close pointer-triggered tooltips.
        private final PauseTransition durationTimer = new PauseTransition();

        /// Handles pointer entry.
        private final javafx.event.EventHandler<MouseEvent> enteredHandler = this::handleEntered;

        /// Handles pointer exit.
        private final javafx.event.EventHandler<MouseEvent> exitedHandler = this::handleExited;

        /// Handles pointer presses.
        private final javafx.event.EventHandler<MouseEvent> pressedHandler = this::handlePressed;

        /// Handles keyboard dismissal while the target owns focus.
        private final javafx.event.EventHandler<KeyEvent> keyPressedHandler = this::handleKeyPressed;

        /// Handles focus changes on the target node.
        private final ChangeListener<Boolean> focusListener = this::handleFocusedChanged;

        /// Creates a tooltip installation.
        private TooltipInstallation(Node node, M3Tooltip tooltip) {
            this.node = node;
            this.tooltip = tooltip;
            showTimer.setOnFinished(event -> showTooltip());
            hideTimer.setOnFinished(event -> tooltip.hide());
            durationTimer.setOnFinished(event -> tooltip.hide());
        }

        /// Adds event handlers to the target node.
        private void install() {
            node.addEventHandler(MouseEvent.MOUSE_ENTERED, enteredHandler);
            node.addEventHandler(MouseEvent.MOUSE_EXITED, exitedHandler);
            node.addEventHandler(MouseEvent.MOUSE_PRESSED, pressedHandler);
            node.addEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
            node.focusedProperty().addListener(focusListener);
        }

        /// Removes event handlers and stops pending timers.
        private void uninstall() {
            node.removeEventHandler(MouseEvent.MOUSE_ENTERED, enteredHandler);
            node.removeEventHandler(MouseEvent.MOUSE_EXITED, exitedHandler);
            node.removeEventHandler(MouseEvent.MOUSE_PRESSED, pressedHandler);
            node.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
            node.focusedProperty().removeListener(focusListener);
            showTimer.stop();
            hideTimer.stop();
            durationTimer.stop();
            if (tooltip.isShowing()) {
                tooltip.hide();
            }
        }

        /// Schedules tooltip display after pointer entry.
        private void handleEntered(MouseEvent event) {
            scheduleShow();
        }

        /// Schedules tooltip hiding after pointer exit.
        private void handleExited(MouseEvent event) {
            scheduleHide();
        }

        /// Hides the tooltip when the target is pressed.
        private void handlePressed(MouseEvent event) {
            hideImmediately();
        }

        /// Shows or hides the tooltip when keyboard focus enters or leaves the target.
        private void handleFocusedChanged(
                ObservableValue<? extends Boolean> observable,
                Boolean oldValue,
                Boolean newValue
        ) {
            if (newValue) {
                scheduleShow();
            } else {
                scheduleHide();
            }
        }

        /// Hides the tooltip from the Escape key.
        private void handleKeyPressed(KeyEvent event) {
            if (event.getCode() == KeyCode.ESCAPE && tooltip.isShowing()) {
                hideImmediately();
                event.consume();
            }
        }

        /// Schedules tooltip display after the configured show delay.
        private void scheduleShow() {
            showTimer.stop();
            hideTimer.stop();
            durationTimer.stop();
            showTimer.setDuration(tooltip.effectiveShowDelay(node));
            showTimer.playFromStart();
        }

        /// Schedules tooltip hiding after the configured hide delay.
        private void scheduleHide() {
            showTimer.stop();
            durationTimer.stop();
            if (tooltip.isShowing()) {
                hideTimer.setDuration(tooltip.effectiveHideDelay(node));
                hideTimer.playFromStart();
            }
        }

        /// Hides the tooltip immediately and clears pending timers.
        private void hideImmediately() {
            showTimer.stop();
            hideTimer.stop();
            durationTimer.stop();
            if (tooltip.isShowing()) {
                tooltip.hide();
            }
        }

        /// Shows the tooltip near the target node.
        private void showTooltip() {
            if (node.getScene() == null || node.isDisabled()) {
                return;
            }

            tooltip.inheritThemeFrom(node);
            Bounds screenBounds = node.localToScreen(node.getBoundsInLocal());
            if (screenBounds == null) {
                return;
            }

            tooltip.show(node, screenBounds.getMinX(), screenBounds.getMaxY() + POPUP_VERTICAL_OFFSET);
            scheduleAutoHide();
        }

        /// Schedules automatic hiding for finite show durations.
        private void scheduleAutoHide() {
            Duration duration = tooltip.effectiveShowDuration(node);
            if (!isFiniteDuration(duration)) {
                return;
            }
            durationTimer.setDuration(duration);
            durationTimer.playFromStart();
        }
    }

    /// A non-null duration property.
    @NotNullByDefault
    private final class DurationProperty extends ObjectPropertyBase<Duration> {
        /// The property name.
        private final String name;

        /// Creates a duration property.
        private DurationProperty(String name, Duration initialValue) {
            super(initialValue);
            this.name = name;
        }

        /// Falls back to zero duration when a null value is assigned.
        @Override
        protected void invalidated() {
            if (get() == null) {
                set(Duration.ZERO);
            }
        }

        /// Returns the owning bean.
        @Override
        public Object getBean() {
            return M3Tooltip.this;
        }

        /// Returns the property name.
        @Override
        public String getName() {
            return name;
        }
    }

    /// Stores a tooltip text binding installed on a target node.
    @NotNullByDefault
    private static final class AccessibleHelpBinding {
        /// The tooltip whose text is exposed as accessible help.
        private final M3Tooltip tooltip;

        /// The listener installed on the tooltip text property.
        private final ChangeListener<@Nullable String> listener;

        /// Creates an accessible help binding.
        private AccessibleHelpBinding(M3Tooltip tooltip, ChangeListener<@Nullable String> listener) {
            this.tooltip = tooltip;
            this.listener = listener;
        }

        /// Removes this binding from the tooltip.
        private void uninstall() {
            tooltip.textProperty().removeListener(listener);
        }
    }

    /// Listens for target node scene changes and reapplies inherited tooltip themes.
    @NotNullByDefault
    private static final class SceneThemeListener implements ChangeListener<@Nullable Scene> {
        /// The tooltip receiving inherited theme values.
        private final M3Tooltip tooltip;

        /// Creates a scene listener for a tooltip.
        private SceneThemeListener(M3Tooltip tooltip) {
            this.tooltip = tooltip;
        }

        /// Applies the new scene theme when the target node enters a scene.
        @Override
        public void changed(
                ObservableValue<? extends @Nullable Scene> observable,
                @Nullable Scene oldValue,
                @Nullable Scene newValue
        ) {
            tooltip.inheritThemeFrom(newValue);
        }
    }
}
