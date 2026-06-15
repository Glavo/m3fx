// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3Motion;
import org.glavo.m3fx.animation.M3MotionBehavior;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.glavo.m3fx.internal.M3ThemeResolver;
import org.glavo.m3fx.skins.M3TooltipSkin;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3ComponentTokens;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
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

    /// The minimum grace period for moving the pointer from an interactive owner to its popup.
    private static final Duration INTERACTIVE_POINTER_TRANSFER_DELAY = M3Motion.SHORT4;

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

    // The text displayed by the tooltip.
    private final StringProperty text = new SimpleStringProperty(this, "text", "");

    // The graphic displayed by the tooltip.
    private final ObjectProperty<@Nullable Node> graphic =
            new SimpleObjectProperty<>(this, "graphic");

    // The text and graphic placement mode.
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

    // Whether tooltip text wraps inside its preferred width.
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

    // The delay before the tooltip opens after pointer entry.
    private final ObjectProperty<Duration> showDelay =
            new DurationProperty("showDelay", M3MotionBehavior.standard().tooltipShowDelay());

    // The delay before the tooltip closes after pointer exit.
    private final ObjectProperty<Duration> hideDelay =
            new DurationProperty("hideDelay", M3MotionBehavior.standard().tooltipHideDelay());

    // The maximum duration the tooltip remains visible after pointer-triggered opening.
    private final ObjectProperty<Duration> showDuration =
            new DurationProperty("showDuration", M3MotionBehavior.standard().tooltipShowDuration());

    // The explicit theme applied directly to this tooltip.
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

    /// Whether the current theme value was inherited from the target node hierarchy.
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
    ///
    /// @param text the tooltip text
    public M3Tooltip(String text) {
        initialize();
        setText(text);
    }

    /// Installs a Material Design 3 tooltip with the supplied text on a node.
    ///
    /// @param node the node that should own the tooltip
    /// @param text the tooltip text
    /// @return the installed tooltip
    public static M3Tooltip install(Node node, String text) {
        M3Tooltip tooltip = new M3Tooltip(text);
        install(node, tooltip);
        return tooltip;
    }

    /// Installs a Material Design 3 tooltip on a node.
    ///
    /// @param node the node that should own the tooltip
    /// @param tooltip the tooltip to install
    public static void install(Node node, M3Tooltip tooltip) {
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(tooltip, "tooltip");

        installThemeInheritance(node, tooltip);
        installAccessibleHelp(node, tooltip);
        installActivation(node, tooltip);
        tooltip.inheritThemeFrom(node);
    }

    /// Uninstalls a Material Design 3 tooltip from a node.
    ///
    /// @param node the node that owns the tooltip
    /// @param tooltip the tooltip to uninstall
    public static void uninstall(Node node, M3Tooltip tooltip) {
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(tooltip, "tooltip");

        uninstallThemeInheritance(node);
        uninstallAccessibleHelp(node);
        uninstallActivation(node, tooltip);
    }

    /// Returns the displayed text.
    ///
    /// @return the displayed text, or `null`
    public final @Nullable String getText() {
        return text.get();
    }

    /// Sets the displayed text.
    ///
    /// @param text the displayed text, or `null`
    public final void setText(@Nullable String text) {
        this.text.set(text);
    }

    /// Returns the displayed text property.
    ///
    /// @return the writable displayed text property
    public final StringProperty textProperty() {
        return text;
    }

    /// Returns the displayed graphic.
    ///
    /// @return the displayed graphic, or `null`
    public final @Nullable Node getGraphic() {
        return graphic.get();
    }

    /// Sets the displayed graphic.
    ///
    /// @param graphic the displayed graphic, or `null`
    public final void setGraphic(@Nullable Node graphic) {
        this.graphic.set(graphic);
    }

    /// Returns the displayed graphic property.
    ///
    /// @return the writable displayed graphic property
    public final ObjectProperty<@Nullable Node> graphicProperty() {
        return graphic;
    }

    /// Returns the text and graphic placement mode.
    ///
    /// @return the text and graphic placement mode
    public final ContentDisplay getContentDisplay() {
        return contentDisplay.get();
    }

    /// Sets the text and graphic placement mode.
    ///
    /// @param contentDisplay the text and graphic placement mode
    public final void setContentDisplay(ContentDisplay contentDisplay) {
        this.contentDisplay.set(Objects.requireNonNull(contentDisplay, "contentDisplay"));
    }

    /// Returns the content display property.
    ///
    /// @return the writable content display property
    public final ObjectProperty<ContentDisplay> contentDisplayProperty() {
        return contentDisplay;
    }

    /// Returns whether tooltip text wraps inside its preferred width.
    ///
    /// @return `true` when tooltip text wraps inside its preferred width
    public final boolean isWrapText() {
        return wrapText.get();
    }

    /// Sets whether tooltip text wraps inside its preferred width.
    ///
    /// @param wrapText whether tooltip text wraps inside its preferred width
    public final void setWrapText(boolean wrapText) {
        this.wrapText.set(wrapText);
    }

    /// Returns the wrap text property.
    ///
    /// @return the writable wrap text property
    public final BooleanProperty wrapTextProperty() {
        return wrapText;
    }

    /// Returns the delay before the tooltip opens after pointer entry.
    ///
    /// @return the delay before the tooltip opens after pointer entry
    public final Duration getShowDelay() {
        return showDelay.get();
    }

    /// Sets the delay before the tooltip opens after pointer entry.
    ///
    /// @param showDelay the delay before the tooltip opens after pointer entry
    public final void setShowDelay(Duration showDelay) {
        showDelayExplicit = true;
        this.showDelay.set(Objects.requireNonNull(showDelay, "showDelay"));
    }

    /// Returns the show delay property.
    ///
    /// @return the writable show delay property
    public final ObjectProperty<Duration> showDelayProperty() {
        return showDelay;
    }

    /// Returns the delay before the tooltip closes after pointer exit.
    ///
    /// @return the delay before the tooltip closes after pointer exit
    public final Duration getHideDelay() {
        return hideDelay.get();
    }

    /// Sets the delay before the tooltip closes after pointer exit.
    ///
    /// @param hideDelay the delay before the tooltip closes after pointer exit
    public final void setHideDelay(Duration hideDelay) {
        hideDelayExplicit = true;
        this.hideDelay.set(Objects.requireNonNull(hideDelay, "hideDelay"));
    }

    /// Returns the hide delay property.
    ///
    /// @return the writable hide delay property
    public final ObjectProperty<Duration> hideDelayProperty() {
        return hideDelay;
    }

    /// Returns the maximum duration the tooltip remains visible after pointer-triggered opening.
    ///
    /// @return the maximum visible duration after pointer-triggered opening
    public final Duration getShowDuration() {
        return showDuration.get();
    }

    /// Sets the maximum duration the tooltip remains visible after pointer-triggered opening.
    ///
    /// @param showDuration the maximum visible duration after pointer-triggered opening
    public final void setShowDuration(Duration showDuration) {
        showDurationExplicit = true;
        this.showDuration.set(Objects.requireNonNull(showDuration, "showDuration"));
    }

    /// Returns the show duration property.
    ///
    /// @return the writable show duration property
    public final ObjectProperty<Duration> showDurationProperty() {
        return showDuration;
    }

    /// Returns the explicit theme applied directly to this tooltip.
    ///
    /// @return the explicit theme applied directly to this tooltip, or `null`
    public final @Nullable M3Theme getTheme() {
        return theme.get();
    }

    /// Sets the explicit theme applied directly to this tooltip.
    ///
    /// @param theme the explicit theme, or `null` to inherit or use defaults
    public final void setTheme(@Nullable M3Theme theme) {
        applyingInheritedTheme = false;
        this.theme.set(theme);
    }

    /// Returns the explicit theme property.
    ///
    /// @return the writable explicit theme property
    public final ObjectProperty<@Nullable M3Theme> themeProperty() {
        return theme;
    }

    /// Creates the default Material Design 3 tooltip skin.
    ///
    /// @return the default Material Design 3 tooltip skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3TooltipSkin(this);
    }

    /// Shows this tooltip and synchronizes popup content direction with the owner node.
    ///
    /// @param ownerNode the node that owns the popup
    /// @param anchorX the screen x coordinate for the popup anchor
    /// @param anchorY the screen y coordinate for the popup anchor
    @Override
    public void show(Node ownerNode, double anchorX, double anchorY) {
        Objects.requireNonNull(ownerNode, "ownerNode");
        super.show(ownerNode, anchorX, anchorY);
        syncPopupNodeOrientation(ownerNode);
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
    ///
    /// @param showDuration the default visible duration
    protected final void setDefaultShowDuration(Duration showDuration) {
        showDurationExplicit = false;
        this.showDuration.set(Objects.requireNonNull(showDuration, "showDuration"));
    }

    /// Returns the default visible duration for this tooltip kind from a behavior profile.
    ///
    /// @param behavior the motion behavior profile
    /// @return the default visible duration for this tooltip kind
    protected Duration defaultShowDuration(M3MotionBehavior behavior) {
        return behavior.tooltipShowDuration();
    }

    /// Returns whether this tooltip should stay open while the pointer is over its popup content.
    ///
    /// @return `true` when popup hover participates in tooltip lifetime management
    protected boolean isInteractive() {
        return false;
    }

    /// Returns the first focusable node in the interactive popup content.
    ///
    /// @return the first focusable interactive node, or `null` when the popup has no interactive target
    protected @Nullable Node firstInteractiveFocusTarget() {
        return null;
    }

    /// Returns the last focusable node in the interactive popup content.
    ///
    /// @return the last focusable interactive node, or `null` when the popup has no interactive target
    protected @Nullable Node lastInteractiveFocusTarget() {
        return firstInteractiveFocusTarget();
    }

    /// Returns the next focusable node in the interactive popup content.
    ///
    /// @param currentFocus the current popup focus owner
    /// @param backward whether traversal moves backward
    /// @return the next focusable interactive node, or `null` when traversal should leave the popup
    protected @Nullable Node nextInteractiveFocusTarget(Node currentFocus, boolean backward) {
        Objects.requireNonNull(currentFocus, "currentFocus");
        return null;
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

    /// Applies the node hierarchy theme when this tooltip has no explicit theme.
    private void inheritThemeFrom(Node node) {
        if (getTheme() != null && !themeInherited) {
            return;
        }

        @Nullable M3Theme inheritedTheme = M3ThemeResolver.findTheme(node);
        if (inheritedTheme != null) {
            setInheritedTheme(inheritedTheme);
        } else if (themeInherited) {
            setInheritedTheme(null);
        }
    }

    /// Sets a theme value that came from the target node hierarchy.
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
        String themeStyle = theme.toRootStyleDeclarations();
        if (usesPlainContainerStyle()) {
            themeStyle = mergeStyles(themeStyle, plainContainerStyle(theme.tokens().componentTokens().tooltip()));
        }
        setStyle(mergeStyles(baseStyle, themeStyle));
    }

    /// Merges existing tooltip style declarations with generated theme declarations.
    private static String mergeStyles(String baseStyle, String themeStyle) {
        if (baseStyle.isBlank()) {
            return themeStyle;
        }
        return baseStyle.stripTrailing() + " " + themeStyle;
    }

    /// Returns whether the tooltip root should receive plain tooltip container metrics.
    protected boolean usesPlainContainerStyle() {
        return true;
    }

    /// Creates inline CSS for plain tooltip container metrics.
    private static String plainContainerStyle(M3ComponentTokens.TooltipTokens tokens) {
        return "-fx-background-radius: "
                + pixels(tokens.plainContainerShape())
                + "; -fx-padding: "
                + pixels(tokens.plainVerticalPadding())
                + " "
                + pixels(tokens.plainHorizontalPadding())
                + ";";
    }

    /// Formats a CSS pixel value.
    private static String pixels(double value) {
        if (Math.rint(value) == value) {
            return Long.toString((long) value) + "px";
        }
        return Double.toString(value) + "px";
    }

    /// Synchronizes popup content direction from the owner node while the tooltip is showing.
    private void syncPopupNodeOrientation(Node ownerNode) {
        Objects.requireNonNull(ownerNode, "ownerNode");
        @Nullable Scene scene = getScene();
        if (scene == null || scene.getRoot() == null) {
            return;
        }
        scene.getRoot().setNodeOrientation(ownerNode.getEffectiveNodeOrientation());
    }

    /// Installs a listener that keeps inherited tooltip themes in sync with the target node scene.
    private static void installThemeInheritance(Node node, M3Tooltip tooltip) {
        uninstallThemeInheritance(node);

        SceneThemeListener listener = new SceneThemeListener(node, tooltip);
        node.getProperties().put(THEME_INHERITANCE_LISTENER_KEY, listener);
    }

    /// Removes any previously installed theme inheritance listener from a node.
    private static void uninstallThemeInheritance(Node node) {
        Object listener = node.getProperties().remove(THEME_INHERITANCE_LISTENER_KEY);
        if (listener instanceof SceneThemeListener sceneThemeListener) {
            sceneThemeListener.dispose();
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

    /// Returns the focused node inside an installed interactive tooltip popup for a target node.
    ///
    /// This supports composite accessibility containers, such as menus or app bars, whose child node owns a
    /// tooltip in a separate popup scene.
    static @Nullable Node activeInstalledTooltipFocusTarget(Node node) {
        Objects.requireNonNull(node, "node");
        Object installation = node.getProperties().get(INSTALLATION_KEY);
        if (installation instanceof TooltipInstallation tooltipInstallation) {
            return tooltipInstallation.activePopupFocusTarget();
        }
        return null;
    }

    /// Returns whether an installed interactive tooltip currently owns pointer or keyboard focus inside its popup.
    static boolean activeInstalledTooltipPopupOwnsInteraction(Node node) {
        Objects.requireNonNull(node, "node");
        Object installation = node.getProperties().get(INSTALLATION_KEY);
        return installation instanceof TooltipInstallation tooltipInstallation
                && tooltipInstallation.hasActivePopupInteraction();
    }

    /// Returns whether an installed interactive tooltip exposes an action target requested by accessibility
    /// parameters.
    static boolean containsInstalledTooltipActionTarget(Node node, Object... parameters) {
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(parameters, "parameters");
        Object installation = node.getProperties().get(INSTALLATION_KEY);
        return installation instanceof TooltipInstallation tooltipInstallation
                && tooltipInstallation.containsInteractiveFocusTarget(parameters);
    }

    /// Shows an installed interactive tooltip and focuses the requested action target.
    static boolean showInstalledTooltipActionTarget(Node node, Object... parameters) {
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(parameters, "parameters");
        Object installation = node.getProperties().get(INSTALLATION_KEY);
        return installation instanceof TooltipInstallation tooltipInstallation
                && tooltipInstallation.showInteractiveFocusTarget(parameters);
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

    /// Requests focus for the first interactive popup target.
    boolean focusFirstInteractiveTarget() {
        return focusInteractiveTarget(firstInteractiveFocusTarget());
    }

    /// Returns the interactive popup target referenced by accessibility action parameters.
    protected @Nullable Node interactiveFocusTargetFor(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        for (Object parameter : parameters) {
            @Nullable Node target = interactiveFocusTargetFor(parameter);
            if (target != null) {
                return target;
            }
        }
        return null;
    }

    /// Returns the interactive popup target referenced by one accessibility action parameter.
    private @Nullable Node interactiveFocusTargetFor(@Nullable Object parameter) {
        if (parameter instanceof Node node) {
            return interactiveFocusTargetFor(node);
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                @Nullable Node target = interactiveFocusTargetFor(value);
                if (target != null) {
                    return target;
                }
            }
            return null;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                @Nullable Node target = interactiveFocusTargetFor(value);
                if (target != null) {
                    return target;
                }
            }
        }
        return null;
    }

    /// Returns the interactive popup target for a requested node.
    protected @Nullable Node interactiveFocusTargetFor(Node requestedNode) {
        Objects.requireNonNull(requestedNode, "requestedNode");
        return null;
    }

    /// Moves focus inside interactive popup content or back to the owner node.
    boolean traverseInteractiveFocus(
            @Nullable Node currentFocus,
            Node owner,
            boolean backward
    ) {
        Objects.requireNonNull(owner, "owner");
        @Nullable Node nextFocus = currentFocus == null
                ? (backward ? lastInteractiveFocusTarget() : firstInteractiveFocusTarget())
                : nextInteractiveFocusTarget(currentFocus, backward);
        if (nextFocus != null) {
            return focusInteractiveTarget(nextFocus);
        }
        if (M3Accessible.canReach(owner)) {
            owner.requestFocus();
            return true;
        }
        return false;
    }

    /// Requests focus for one interactive popup target.
    private static boolean focusInteractiveTarget(@Nullable Node target) {
        @Nullable Node focusTarget = M3Accessible.focusTarget(target);
        if (focusTarget == null) {
            return false;
        }
        focusTarget.requestFocus();
        return true;
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

        /// Updates tooltip activation timings when runtime motion settings change.
        private final M3MotionSettingsObserver motionSettingsObserver;

        /// The popup root node that currently has tooltip hover handlers installed.
        private @Nullable Node tooltipRoot;

        /// The popup scene that currently has a focus owner listener installed.
        private @Nullable Scene tooltipScene;

        /// The owner scene that currently has keyboard traversal filtering installed.
        private @Nullable Scene ownerScene;

        /// Whether the pointer is currently inside the target node.
        private boolean ownerContainsPointer;

        /// Whether the pointer is currently inside the tooltip popup.
        private boolean tooltipContainsPointer;

        /// Whether keyboard focus is currently inside the tooltip popup.
        private boolean tooltipContainsFocus;

        /// Handles pointer entry.
        private final javafx.event.EventHandler<MouseEvent> enteredHandler = this::handleEntered;

        /// Handles pointer exit.
        private final javafx.event.EventHandler<MouseEvent> exitedHandler = this::handleExited;

        /// Handles pointer presses.
        private final javafx.event.EventHandler<MouseEvent> pressedHandler = this::handlePressed;

        /// Handles pointer entry into an interactive tooltip popup.
        private final javafx.event.EventHandler<MouseEvent> tooltipEnteredHandler = this::handleTooltipEntered;

        /// Handles pointer exit from an interactive tooltip popup.
        private final javafx.event.EventHandler<MouseEvent> tooltipExitedHandler = this::handleTooltipExited;

        /// Handles keyboard dismissal while focus is inside an interactive tooltip popup.
        private final javafx.event.EventHandler<KeyEvent> tooltipKeyPressedHandler = this::handleTooltipKeyPressed;

        /// Handles focus changes inside an interactive tooltip popup.
        private final ChangeListener<@Nullable Node> tooltipFocusOwnerListener =
                this::handleTooltipFocusOwnerChanged;

        /// Handles keyboard dismissal while the target owns focus.
        private final javafx.event.EventHandler<KeyEvent> keyPressedHandler = this::handleKeyPressed;

        /// Handles owner node scene changes.
        private final ChangeListener<@Nullable Scene> ownerSceneListener = this::handleOwnerSceneChanged;

        /// Handles owner node orientation changes while the tooltip popup is showing.
        private final ChangeListener<NodeOrientation> ownerNodeOrientationListener =
                this::handleOwnerNodeOrientationChanged;

        /// Handles focus changes on the target node.
        private final ChangeListener<Boolean> focusListener = this::handleFocusedChanged;

        /// Handles tooltip popup visibility changes.
        private final ChangeListener<Boolean> showingListener = this::handleTooltipShowingChanged;

        /// Creates a tooltip installation.
        private TooltipInstallation(Node node, M3Tooltip tooltip) {
            this.node = node;
            this.tooltip = tooltip;
            showTimer.setOnFinished(event -> showTooltip());
            hideTimer.setOnFinished(event -> hideIfPointerOutside());
            durationTimer.setOnFinished(event -> hideAfterVisibleDuration());
            motionSettingsObserver = new M3MotionSettingsObserver(node, this::refreshMotionSettings);
        }

        /// Adds event handlers to the target node.
        private void install() {
            node.addEventHandler(MouseEvent.MOUSE_ENTERED, enteredHandler);
            node.addEventHandler(MouseEvent.MOUSE_EXITED, exitedHandler);
            node.addEventHandler(MouseEvent.MOUSE_PRESSED, pressedHandler);
            node.addEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
            node.addEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
            node.focusedProperty().addListener(focusListener);
            node.sceneProperty().addListener(ownerSceneListener);
            node.effectiveNodeOrientationProperty().addListener(ownerNodeOrientationListener);
            tooltip.showingProperty().addListener(showingListener);
            installOwnerSceneFilter(node.getScene());
        }

        /// Removes event handlers and stops pending timers.
        private void uninstall() {
            node.removeEventHandler(MouseEvent.MOUSE_ENTERED, enteredHandler);
            node.removeEventHandler(MouseEvent.MOUSE_EXITED, exitedHandler);
            node.removeEventHandler(MouseEvent.MOUSE_PRESSED, pressedHandler);
            node.removeEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
            node.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
            node.focusedProperty().removeListener(focusListener);
            node.sceneProperty().removeListener(ownerSceneListener);
            node.effectiveNodeOrientationProperty().removeListener(ownerNodeOrientationListener);
            tooltip.showingProperty().removeListener(showingListener);
            motionSettingsObserver.dispose();
            uninstallOwnerSceneFilter();
            uninstallTooltipHoverHandlers();
            ownerContainsPointer = false;
            tooltipContainsPointer = false;
            tooltipContainsFocus = false;
            showTimer.stop();
            hideTimer.stop();
            durationTimer.stop();
            if (tooltip.isShowing()) {
                tooltip.hide();
            }
        }

        /// Updates scene-level keyboard handling when the owner node moves between scenes.
        private void handleOwnerSceneChanged(
                ObservableValue<? extends @Nullable Scene> observable,
                @Nullable Scene oldValue,
                @Nullable Scene newValue
        ) {
            uninstallOwnerSceneFilter();
            installOwnerSceneFilter(newValue);
        }

        /// Synchronizes visible tooltip popup direction when the owner direction changes.
        private void handleOwnerNodeOrientationChanged(
                ObservableValue<? extends NodeOrientation> observable,
                NodeOrientation oldValue,
                NodeOrientation newValue
        ) {
            if (tooltip.isShowing()) {
                tooltip.syncPopupNodeOrientation(node);
            }
        }

        /// Schedules tooltip display after pointer entry.
        private void handleEntered(MouseEvent event) {
            ownerContainsPointer = true;
            scheduleShow();
        }

        /// Schedules tooltip hiding after pointer exit.
        private void handleExited(MouseEvent event) {
            ownerContainsPointer = false;
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

        /// Cancels pending hide while the pointer is inside an interactive tooltip popup.
        private void handleTooltipEntered(MouseEvent event) {
            tooltipContainsPointer = true;
            hideTimer.stop();
        }

        /// Schedules hiding once the pointer leaves an interactive tooltip popup.
        private void handleTooltipExited(MouseEvent event) {
            tooltipContainsPointer = false;
            scheduleHide();
        }

        /// Hides the tooltip from the Escape key while focus is inside its popup.
        private void handleTooltipKeyPressed(KeyEvent event) {
            if (event.isConsumed()) {
                return;
            }
            if (event.getCode() == KeyCode.ESCAPE && tooltip.isShowing()) {
                hideImmediately();
                if (M3Accessible.canReach(node)) {
                    node.requestFocus();
                }
                event.consume();
            } else if (event.getCode() == KeyCode.TAB && tooltip.isInteractive()) {
                @Nullable Node focusOwner = tooltipScene == null ? null : tooltipScene.getFocusOwner();
                if (tooltip.traverseInteractiveFocus(focusOwner, node, event.isShiftDown())) {
                    event.consume();
                }
            }
        }

        /// Keeps interactive tooltip popups open while focus is inside popup content.
        private void handleTooltipFocusOwnerChanged(
                ObservableValue<? extends @Nullable Node> observable,
                @Nullable Node oldValue,
                @Nullable Node newValue
        ) {
            tooltipContainsFocus = newValue != null
                    && tooltipRoot != null
                    && M3Accessible.containsNode(tooltipRoot, newValue);
            if (tooltipContainsFocus) {
                hideTimer.stop();
                durationTimer.stop();
            } else if (tooltip.isShowing()) {
                scheduleHide();
            }
            notifyOwnerFocusNodeChanged();
        }

        /// Installs or removes popup hover handlers as the tooltip is shown or hidden.
        private void handleTooltipShowingChanged(
                ObservableValue<? extends Boolean> observable,
                Boolean oldValue,
                Boolean newValue
        ) {
            if (newValue) {
                tooltip.syncPopupNodeOrientation(node);
            }
            if (newValue && tooltip.isInteractive()) {
                installTooltipHoverHandlers();
            } else {
                tooltipContainsPointer = false;
                tooltipContainsFocus = false;
                uninstallTooltipHoverHandlers();
            }
        }

        /// Hides the tooltip from the Escape key.
        private void handleKeyPressed(KeyEvent event) {
            if (event.isConsumed()) {
                return;
            }
            if (!ownerHasKeyboardFocus()) {
                return;
            }
            if (event.getCode() == KeyCode.ESCAPE && tooltip.isShowing()) {
                hideImmediately();
                event.consume();
            } else if ((event.getCode() == KeyCode.TAB || event.getCode() == KeyCode.F6)
                    && tooltip.isInteractive()
                    && tooltip.isShowing()
                    && !event.isShiftDown()
                    && tooltip.focusFirstInteractiveTarget()) {
                event.consume();
            }
        }

        /// Returns whether keyboard focus is currently owned by the target node.
        private boolean ownerHasKeyboardFocus() {
            if (node.isFocused()) {
                return true;
            }
            @Nullable Scene scene = node.getScene();
            return scene != null && scene.getFocusOwner() == node;
        }

        /// Returns the current focus owner inside the tooltip popup scene.
        private @Nullable Node activePopupFocusTarget() {
            if (!tooltip.isInteractive() || !tooltip.isShowing()) {
                return null;
            }

            @Nullable Scene scene = tooltip.getScene();
            if (scene == null) {
                return null;
            }

            @Nullable Node popupRoot = scene.getRoot();
            @Nullable Node focusOwner = scene.getFocusOwner();
            return popupRoot != null
                    && focusOwner != null
                    && M3Accessible.containsNode(popupRoot, focusOwner)
                    ? focusOwner
                    : null;
        }

        /// Returns whether this installation exposes the requested interactive target.
        private boolean containsInteractiveFocusTarget(Object... parameters) {
            return tooltip.isInteractive() && tooltip.interactiveFocusTargetFor(parameters) != null;
        }

        /// Shows the tooltip and focuses the requested interactive target.
        private boolean showInteractiveFocusTarget(Object... parameters) {
            if (!containsInteractiveFocusTarget(parameters)) {
                return false;
            }

            showTimer.stop();
            hideTimer.stop();
            durationTimer.stop();
            if (!tooltip.isShowing()) {
                showTooltip();
            }
            if (!tooltip.isShowing()) {
                return false;
            }

            @Nullable Node target = tooltip.interactiveFocusTargetFor(parameters);
            if (!focusInteractiveTarget(target)) {
                return false;
            }
            hideTimer.stop();
            durationTimer.stop();
            notifyOwnerFocusNodeChanged();
            return true;
        }

        /// Returns whether pointer or keyboard focus is currently inside the tooltip popup.
        private boolean hasActivePopupInteraction() {
            return tooltip.isInteractive()
                    && tooltip.isShowing()
                    && (tooltipContainsPointer || tooltipContainsFocus);
        }

        /// Notifies the target and owning menu chain that the exposed focus node may have changed.
        private void notifyOwnerFocusNodeChanged() {
            M3Accessible.notifyFocusNodeChanged(node);
            @Nullable Parent parent = node.getParent();
            while (parent != null) {
                if (parent instanceof M3Menu menu) {
                    menu.notifyDescendantFocusNodeChanged();
                }
                parent = parent.getParent();
            }
        }

        /// Schedules tooltip display after the configured show delay.
        private void scheduleShow() {
            showTimer.stop();
            hideTimer.stop();
            if (tooltip.isInteractive() && tooltip.isShowing()) {
                return;
            }
            durationTimer.stop();
            showTimer.setDuration(tooltip.effectiveShowDelay(node));
            showTimer.playFromStart();
        }

        /// Schedules tooltip hiding after the configured hide delay.
        private void scheduleHide() {
            showTimer.stop();
            durationTimer.stop();
            if (tooltip.isInteractive() && isTooltipActive()) {
                return;
            }
            if (tooltip.isShowing()) {
                hideTimer.setDuration(effectiveHideDelay());
                hideTimer.playFromStart();
            }
        }

        /// Hides the tooltip immediately and clears pending timers.
        private void hideImmediately() {
            showTimer.stop();
            hideTimer.stop();
            durationTimer.stop();
            ownerContainsPointer = false;
            tooltipContainsPointer = false;
            tooltipContainsFocus = false;
            if (tooltip.isShowing()) {
                tooltip.hide();
            }
        }

        /// Shows the tooltip near the target node.
        private void showTooltip() {
            if (!M3Accessible.canReach(node)) {
                return;
            }

            installOwnerSceneFilter(node.getScene());
            tooltip.inheritThemeFrom(node);
            Bounds screenBounds = node.localToScreen(node.getBoundsInLocal());
            if (screenBounds == null) {
                return;
            }

            tooltip.show(node, screenBounds.getMinX(), screenBounds.getMaxY() + POPUP_VERTICAL_OFFSET);
            @Nullable Scene tooltipScene = tooltip.getScene();
            if (tooltipScene != null) {
                M3Animation.copyResolvedMotionSettings(node, tooltipScene.getRoot());
            }
            installTooltipHoverHandlers();
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

        /// Returns the hide delay, adding pointer-transfer grace for interactive popups.
        private Duration effectiveHideDelay() {
            Duration delay = tooltip.effectiveHideDelay(node);
            if (tooltip.isInteractive()
                    && isFiniteDuration(delay)
                    && delay.lessThan(INTERACTIVE_POINTER_TRANSFER_DELAY)) {
                return INTERACTIVE_POINTER_TRANSFER_DELAY;
            }
            return delay;
        }

        /// Hides after the visible-duration timer unless an interactive tooltip is being hovered.
        private void hideAfterVisibleDuration() {
            if (tooltip.isInteractive()) {
                hideIfPointerOutside();
            } else {
                tooltip.hide();
            }
        }

        /// Hides the tooltip when neither the target nor interactive popup contains the pointer.
        private void hideIfPointerOutside() {
            if (tooltip.isInteractive() && isTooltipActive()) {
                return;
            }
            tooltip.hide();
        }

        /// Returns whether an interactive tooltip still has pointer or keyboard focus ownership.
        private boolean isTooltipActive() {
            return ownerContainsPointer || node.isFocused() || tooltipContainsPointer || tooltipContainsFocus;
        }

        /// Applies changed runtime motion settings to delayed tooltip activation timers.
        private void refreshMotionSettings() {
            M3Animation.updatePauseDuration(
                    showTimer,
                    tooltip.effectiveShowDelay(node),
                    ownerContainsPointer || ownerHasKeyboardFocus()
            );
            M3Animation.updatePauseDuration(
                    hideTimer,
                    effectiveHideDelay(),
                    tooltip.isShowing() && !isTooltipActive()
            );
            refreshAutoHideDuration();
            @Nullable Scene scene = tooltip.getScene();
            if (tooltip.isShowing() && scene != null && scene.getRoot() != null) {
                M3Animation.copyResolvedMotionSettings(node, scene.getRoot());
            }
        }

        /// Applies changed runtime motion settings to the visible-duration timer.
        private void refreshAutoHideDuration() {
            Duration duration = tooltip.effectiveShowDuration(node);
            if (!isFiniteDuration(duration)) {
                durationTimer.stop();
                return;
            }

            boolean shouldRun = tooltip.isShowing() && !hasActivePopupInteraction();
            M3Animation.updatePauseDuration(durationTimer, duration, shouldRun);
            if (shouldRun && durationTimer.getStatus() != Animation.Status.RUNNING) {
                durationTimer.playFromStart();
            }
        }

        /// Installs scene-level owner keyboard handling.
        private void installOwnerSceneFilter(@Nullable Scene scene) {
            if (ownerScene == scene) {
                return;
            }
            uninstallOwnerSceneFilter();
            ownerScene = scene;
            if (ownerScene != null) {
                ownerScene.addEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
                ownerScene.addEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
            }
        }

        /// Removes scene-level owner keyboard handling.
        private void uninstallOwnerSceneFilter() {
            if (ownerScene != null) {
                ownerScene.removeEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
                ownerScene.removeEventHandler(KeyEvent.KEY_PRESSED, keyPressedHandler);
                ownerScene = null;
            }
        }

        /// Adds popup interaction handlers to the current popup root node.
        private void installTooltipHoverHandlers() {
            if (!tooltip.isInteractive() || !tooltip.isShowing() || tooltip.getScene() == null) {
                return;
            }

            Node root = tooltip.getScene().getRoot();
            if (tooltipRoot == root) {
                return;
            }
            uninstallTooltipHoverHandlers();
            tooltipRoot = root;
            root.addEventHandler(MouseEvent.MOUSE_ENTERED, tooltipEnteredHandler);
            root.addEventHandler(MouseEvent.MOUSE_EXITED, tooltipExitedHandler);
            root.addEventFilter(KeyEvent.KEY_PRESSED, tooltipKeyPressedHandler);
            root.addEventHandler(KeyEvent.KEY_PRESSED, tooltipKeyPressedHandler);
            tooltipScene = root.getScene();
            if (tooltipScene != null) {
                tooltipScene.focusOwnerProperty().addListener(tooltipFocusOwnerListener);
                handleTooltipFocusOwnerChanged(
                        tooltipScene.focusOwnerProperty(),
                        null,
                        tooltipScene.getFocusOwner()
                );
            }
        }

        /// Removes popup interaction handlers from the current popup root node.
        private void uninstallTooltipHoverHandlers() {
            Node root = tooltipRoot;
            if (root != null) {
                root.removeEventHandler(MouseEvent.MOUSE_ENTERED, tooltipEnteredHandler);
                root.removeEventHandler(MouseEvent.MOUSE_EXITED, tooltipExitedHandler);
                root.removeEventFilter(KeyEvent.KEY_PRESSED, tooltipKeyPressedHandler);
                root.removeEventHandler(KeyEvent.KEY_PRESSED, tooltipKeyPressedHandler);
                tooltipRoot = null;
            }
            if (tooltipScene != null) {
                tooltipScene.focusOwnerProperty().removeListener(tooltipFocusOwnerListener);
                tooltipScene = null;
            }
            tooltipContainsFocus = false;
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

    /// Listens for target node scene and theme-root changes and reapplies inherited tooltip themes.
    @NotNullByDefault
    private static final class SceneThemeListener implements ChangeListener<@Nullable Scene> {
        /// The node whose hierarchy supplies inherited theme values.
        private final Node node;

        /// The tooltip receiving inherited theme values.
        private final M3Tooltip tooltip;

        /// Handles runtime scene-root theme changes.
        private final MapChangeListener<Object, Object> sceneRootPropertiesListener =
                this::handleThemeRootPropertiesChanged;

        /// Handles replacement of the scene root that can supply scene-level theme values.
        private final ChangeListener<Parent> sceneRootListener = this::handleSceneRootChanged;

        /// Handles runtime local ancestor theme changes.
        private final MapChangeListener<Object, Object> ancestorThemeRootPropertiesListener =
                this::handleThemeRootPropertiesChanged;

        /// Handles direct parent changes on the target node.
        private final ChangeListener<@Nullable Parent> ownerParentListener = this::handleParentChainChanged;

        /// Handles parent-chain changes on observed owner ancestors.
        private final ChangeListener<@Nullable Parent> ancestorParentListener = this::handleParentChainChanged;

        /// The scene currently observed for root replacement.
        private @Nullable Scene observedScene;

        /// The scene root currently observed for scene-level theme changes.
        private @Nullable Parent observedSceneRoot;

        /// The parent-chain nodes currently observed for local theme changes.
        private final List<Parent> observedAncestorThemeRoots = new ArrayList<>();

        /// Creates a scene listener for a tooltip.
        private SceneThemeListener(Node node, M3Tooltip tooltip) {
            this.node = node;
            this.tooltip = tooltip;
            node.sceneProperty().addListener(this);
            node.parentProperty().addListener(ownerParentListener);
            refreshThemeRootSubscriptions();
        }

        /// Reapplies the target node theme when the target node enters or leaves a scene.
        @Override
        public void changed(
                ObservableValue<? extends @Nullable Scene> observable,
                @Nullable Scene oldValue,
                @Nullable Scene newValue
        ) {
            refreshThemeRootSubscriptions();
            tooltip.inheritThemeFrom(node);
        }

        /// Removes listeners installed on the owner node and theme roots.
        private void dispose() {
            node.sceneProperty().removeListener(this);
            node.parentProperty().removeListener(ownerParentListener);
            clearObservedAncestorThemeRoots();
            updateObservedScene(null);
            updateObservedSceneRoot(null);
        }

        /// Reapplies inherited tooltip theme after an observed root theme property changes.
        private void handleThemeRootPropertiesChanged(MapChangeListener.Change<?, ?> change) {
            if (Objects.equals(change.getKey(), M3ThemeManager.THEME_PROPERTY_KEY)) {
                refreshThemeRootSubscriptions();
                tooltip.inheritThemeFrom(node);
            }
        }

        /// Reapplies inherited tooltip theme after the observed scene root is replaced.
        private void handleSceneRootChanged(
                ObservableValue<? extends Parent> observable,
                Parent oldRoot,
                Parent newRoot
        ) {
            refreshThemeRootSubscriptions();
            tooltip.inheritThemeFrom(node);
        }

        /// Reapplies inherited tooltip theme after the target parent chain changes.
        private void handleParentChainChanged(
                ObservableValue<? extends @Nullable Parent> observable,
                @Nullable Parent oldParent,
                @Nullable Parent newParent
        ) {
            refreshThemeRootSubscriptions();
            tooltip.inheritThemeFrom(node);
        }

        /// Updates the observed roots that can change the inherited tooltip theme.
        private void refreshThemeRootSubscriptions() {
            @Nullable Scene scene = node.getScene();
            updateObservedScene(scene);
            updateObservedSceneRoot(scene == null ? null : scene.getRoot());
            updateObservedAncestorThemeRoots();
        }

        /// Replaces the observed scene root-change listener.
        private void updateObservedScene(@Nullable Scene scene) {
            if (observedScene == scene) {
                return;
            }
            if (observedScene != null) {
                observedScene.rootProperty().removeListener(sceneRootListener);
            }
            observedScene = scene;
            if (observedScene != null) {
                observedScene.rootProperty().addListener(sceneRootListener);
            }
        }

        /// Updates observed parent-chain roots that may receive local themes.
        private void updateObservedAncestorThemeRoots() {
            clearObservedAncestorThemeRoots();
            @Nullable Node current = node;
            while (current != null) {
                if (current instanceof Parent parent && parent != observedSceneRoot) {
                    parent.getProperties().addListener(ancestorThemeRootPropertiesListener);
                    parent.parentProperty().addListener(ancestorParentListener);
                    observedAncestorThemeRoots.add(parent);
                }
                current = current.getParent();
            }
        }

        /// Removes property listeners from all currently observed parent-chain roots.
        private void clearObservedAncestorThemeRoots() {
            for (Parent root : observedAncestorThemeRoots) {
                root.getProperties().removeListener(ancestorThemeRootPropertiesListener);
                root.parentProperty().removeListener(ancestorParentListener);
            }
            observedAncestorThemeRoots.clear();
        }

        /// Replaces the observed scene root property listener.
        private void updateObservedSceneRoot(@Nullable Parent root) {
            if (observedSceneRoot == root) {
                return;
            }
            if (observedSceneRoot != null) {
                observedSceneRoot.getProperties().removeListener(sceneRootPropertiesListener);
            }
            observedSceneRoot = root;
            if (observedSceneRoot != null) {
                observedSceneRoot.getProperties().addListener(sceneRootPropertiesListener);
            }
        }

    }
}
