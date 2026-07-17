// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.animation.PauseTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.WindowEvent;
import javafx.stage.Screen;
import javafx.util.Duration;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.animation.M3Motion;
import org.glavo.m3fx.animation.M3MotionBehavior;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.glavo.m3fx.internal.M3PopupContextSynchronizer;
import org.glavo.m3fx.internal.M3PopupWindows;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.M3ThemeResolver;
import org.glavo.m3fx.internal.M3TooltipInstallation;
import org.glavo.m3fx.internal.M3TooltipRegistry;
import org.glavo.m3fx.skins.M3TooltipSkin;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.internal.theme.M3ThemeMetadata;
import org.glavo.m3fx.internal.theme.M3ThemeCssCompiler;
import org.glavo.m3fx.internal.theme.M3ThemeRuntime;
import org.glavo.m3fx.tokens.M3ComponentTokens;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
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

    /// The currently visible installed tooltip, retained weakly so unused tooltips remain collectable.
    private static @Nullable WeakReference<M3Tooltip> activeTooltipReference;

    /// The displayed text property.
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

    /// A detached theme root used when the tooltip has an explicit theme rather than an owner-inherited theme.
    private final Pane explicitThemeRoot = new Pane();

    /// The active popup context synchronizer while this tooltip is showing.
    private @Nullable M3PopupContextSynchronizer popupContextSynchronizer;

    /// The owner node currently supplying popup context.
    private @Nullable Node popupContextOwner;

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


    /// Installs a Material Design 3 tooltip on a node.
    ///
    /// @param node the node that should own the tooltip
    /// @param tooltip the tooltip to install
    /// @throws NullPointerException if any required argument is `null`
    public static void install(Node node, M3Tooltip tooltip) {
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(tooltip, "tooltip");

        installActivation(node, tooltip);
    }

    /// Uninstalls a Material Design 3 tooltip from a node.
    ///
    /// @param node the node that owns the tooltip
    /// @param tooltip the tooltip to uninstall
    /// @throws NullPointerException if any required argument is `null`
    public static void uninstall(Node node, M3Tooltip tooltip) {
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(tooltip, "tooltip");

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
    /// @throws NullPointerException if any required argument is `null`
    public final void setContentDisplay(ContentDisplay contentDisplay) {
        this.contentDisplay.set(Objects.requireNonNull(contentDisplay, "contentDisplay"));
    }

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
    /// @throws NullPointerException if any required argument is `null`
    public final void setShowDelay(Duration showDelay) {
        showDelayExplicit = true;
        this.showDelay.set(Objects.requireNonNull(showDelay, "showDelay"));
    }

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
    /// @throws NullPointerException if any required argument is `null`
    public final void setHideDelay(Duration hideDelay) {
        hideDelayExplicit = true;
        this.hideDelay.set(Objects.requireNonNull(hideDelay, "hideDelay"));
    }

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
    /// @throws NullPointerException if any required argument is `null`
    public final void setShowDuration(Duration showDuration) {
        showDurationExplicit = true;
        this.showDuration.set(Objects.requireNonNull(showDuration, "showDuration"));
    }

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

    /// Shows this tooltip and synchronizes popup context with the owner node.
    ///
    /// @param ownerNode the node that owns the popup
    /// @param anchorX the screen x coordinate for the popup anchor
    /// @param anchorY the screen y coordinate for the popup anchor
    /// @throws NullPointerException if any required argument is `null`
    @Override
    public void show(Node ownerNode, double anchorX, double anchorY) {
        Objects.requireNonNull(ownerNode, "ownerNode");
        if (!M3PopupWindows.canShow(ownerNode)) {
            stopPopupContextSynchronizer();
            return;
        }
        try {
            super.show(ownerNode, anchorX, anchorY);
        } catch (RuntimeException | Error exception) {
            try {
                hide();
            } catch (RuntimeException | Error cleanupFailure) {
                exception.addSuppressed(cleanupFailure);
            }
            stopPopupContextSynchronizer();
            throw exception;
        }
        if (!isShowing()) {
            hide();
            stopPopupContextSynchronizer();
            return;
        }
        syncPopupRootThemeContext(ownerNode);
    }

    /// Adds base style classes and Material timing defaults.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setWrapText(true);
        setAutoFix(true);
        setAutoHide(true);
        setHideOnEscape(true);
        addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> {
            stopPopupContextSynchronizer();
            @Nullable WeakReference<M3Tooltip> reference = activeTooltipReference;
            if (reference != null && reference.get() == this) {
                activeTooltipReference = null;
            }
        });
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

    /// Returns whether installed activation requires an explicit click or keyboard command.
    ///
    /// @return `true` when hover and focus must not open this tooltip
    protected boolean usesPersistentActivation() {
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
        updateExplicitThemeRoot(theme);
        if (theme == null) {
            M3ThemeRuntime.clearThemeStyleClasses(this);
            String currentBaseStyle = baseStyle;
            if (currentBaseStyle != null) {
                setStyle(currentBaseStyle);
                baseStyle = null;
            }
            if (isShowing()) {
                @Nullable Node ownerNode = getOwnerNode();
                if (ownerNode != null) {
                    syncPopupRootThemeContext(ownerNode);
                }
            }
            return;
        }

        String resolvedBaseStyle = baseStyle;
        if (resolvedBaseStyle == null) {
            resolvedBaseStyle = getStyle();
            baseStyle = resolvedBaseStyle;
        }
        M3ThemeRuntime.applyThemeStyleClasses(this, theme);
        String themeStyle = M3ThemeCssCompiler.rootStyleDeclarations(theme);
        if (usesPlainContainerStyle()) {
            themeStyle = mergeStyles(themeStyle, plainContainerStyle(theme.tokens().componentTokens().tooltip()));
        }
        setStyle(mergeStyles(resolvedBaseStyle, themeStyle));
        if (isShowing()) {
            @Nullable Node ownerNode = getOwnerNode();
            if (ownerNode != null) {
                syncPopupRootThemeContext(ownerNode);
            }
        }
    }

    /// Updates the detached theme root used for explicitly themed popup content.
    private void updateExplicitThemeRoot(@Nullable M3Theme theme) {
        if (theme != null && !themeInherited) {
            M3ThemeRuntime.install(explicitThemeRoot, theme);
        } else {
            M3ThemeRuntime.uninstall(explicitThemeRoot);
        }
    }

    /// Merges existing tooltip style declarations with generated theme declarations.
    private static String mergeStyles(String baseStyle, String themeStyle) {
        if (baseStyle.isBlank()) {
            return themeStyle;
        }
        return baseStyle.stripTrailing() + " " + themeStyle;
    }

    /// Returns whether the tooltip root should receive plain tooltip container metrics.
    ///
    /// Subclasses that provide rich content return `false` so rich container tokens remain in effect.
    ///
    /// @return `true` for the compact plain-tooltip container style
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
            return (long) value + "px";
        }
        return value + "px";
    }


    /// Synchronizes the popup scene root style classes with the owner theme context.
    private void syncPopupRootThemeContext(Node ownerNode) {
        Objects.requireNonNull(ownerNode, "ownerNode");
        if (!isShowing()) {
            stopPopupContextSynchronizer();
            return;
        }

        @Nullable M3PopupContextSynchronizer synchronizer = popupContextSynchronizer;
        if (synchronizer == null || popupContextOwner != ownerNode) {
            startPopupContextSynchronizer(ownerNode);
            return;
        }

        synchronizer.sync();
    }

    /// Starts observing and synchronizing popup root context from the supplied owner node.
    private void startPopupContextSynchronizer(Node ownerNode) {
        Objects.requireNonNull(ownerNode, "ownerNode");
        stopPopupContextSynchronizer();
        @Nullable Scene popupScene = getScene();
        if (popupScene == null) {
            return;
        }

        Parent popupRoot = popupScene.getRoot();
        M3PopupContextSynchronizer synchronizer = new M3PopupContextSynchronizer(
                ownerNode,
                popupRoot,
                () -> popupStylesheetSource(ownerNode),
                () -> popupThemeSource(ownerNode),
                M3Stylesheets.controlStylesheet("tooltip.css")
        );
        popupContextOwner = ownerNode;
        popupContextSynchronizer = synchronizer;
        synchronizer.start();
    }

    /// Stops observing popup root context after this tooltip is hidden or retargeted.
    private void stopPopupContextSynchronizer() {
        @Nullable M3PopupContextSynchronizer synchronizer = popupContextSynchronizer;
        if (synchronizer != null) {
            synchronizer.stop();
            popupContextSynchronizer = null;
        }
        popupContextOwner = null;
    }

    /// Returns the stylesheet list that should be mirrored into the tooltip popup root.
    private static @Nullable ObservableList<String> popupStylesheetSource(Node ownerNode) {
        @Nullable Scene ownerScene = ownerNode.getScene();
        return ownerScene == null ? null : ownerScene.getStylesheets();
    }

    /// Returns the theme root that should supply popup token declarations.
    private @Nullable Parent popupThemeSource(Node ownerNode) {
        if (getTheme() != null && !themeInherited) {
            return explicitThemeRoot;
        }
        return M3ThemeResolver.findThemeRoot(ownerNode);
    }

    /// Installs pointer activation handlers on the tooltip target.
    private static void installActivation(Node node, M3Tooltip tooltip) {
        uninstallActivation(node, null);

        TooltipInstallation installation = new TooltipInstallation(node, tooltip);
        try {
            installation.install();
            M3TooltipRegistry.install(node, installation);
        } catch (RuntimeException | Error exception) {
            installation.uninstall();
            throw exception;
        }
    }

    /// Removes pointer activation handlers from the tooltip target.
    private static void uninstallActivation(Node node, @Nullable M3Tooltip tooltip) {
        M3TooltipInstallation installation = M3TooltipRegistry.installation(node);
        if (!(installation instanceof TooltipInstallation tooltipInstallation)) {
            return;
        }
        if (tooltip != null && tooltipInstallation.tooltip != tooltip) {
            return;
        }

        M3TooltipRegistry.remove(node);
        tooltipInstallation.uninstall();
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

    /// Returns whether this tooltip exposes the requested interactive action target.
    ///
    /// @param parameters accessibility target descriptors; nested arrays and iterables are traversed
    /// @return `true` when the parameters identify a reachable interactive popup node
    /// @throws NullPointerException if `parameters` is `null`
    protected boolean containsInteractiveActionTarget(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        return interactiveFocusTargetFor(parameters) != null;
    }

    /// Shows one requested interactive action target inside the tooltip.
    ///
    /// @param parameters accessibility target descriptors; nested arrays and iterables are traversed
    /// @return `true` when a matching target was revealed and focused
    /// @throws NullPointerException if `parameters` is `null`
    protected boolean showInteractiveActionTarget(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        return focusInteractiveTarget(interactiveFocusTargetFor(parameters));
    }

    /// Returns the interactive popup target referenced by accessibility action parameters.
    ///
    /// @param parameters accessibility target descriptors; nested arrays and iterables are traversed
    /// @return the first matching reachable popup node, or `null`
    /// @throws NullPointerException if `parameters` is `null`
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
    ///
    /// Plain tooltips do not expose interactive targets. Rich tooltip subclasses may return a reachable descendant
    /// that they own.
    ///
    /// @param requestedNode the candidate popup node
    /// @return the owned reachable focus target, or `null` when the node is not an interactive target
    /// @throws NullPointerException if `requestedNode` is `null`
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
        return M3Accessible.canReach(owner) && M3Accessible.showDirectItem(owner, owner);
    }

    /// Requests focus for one interactive popup target.
    private static boolean focusInteractiveTarget(@Nullable Node target) {
        @Nullable Node focusTarget = M3Accessible.focusTarget(target);
        return focusTarget != null && M3Accessible.showDirectItem(focusTarget, focusTarget);
    }

    /// Stores pointer handlers installed on a tooltip target node.
    @NotNullByDefault
    private static final class TooltipInstallation implements M3TooltipInstallation {
        /// Indicates that the reusable timer has no pending action.
        private static final int NO_TIMER_ACTION = 0;

        /// Indicates that the reusable timer will show the tooltip.
        private static final int SHOW_TIMER_ACTION = 1;

        /// Indicates that the reusable timer will hide the tooltip after pointer exit.
        private static final int HIDE_TIMER_ACTION = 2;

        /// Indicates that the reusable timer will enforce the visible-duration limit.
        private static final int DURATION_TIMER_ACTION = 3;

        /// The target node that owns the tooltip activation handlers.
        private final Node node;

        /// The tooltip controlled by the installed handlers.
        private final M3Tooltip tooltip;

        /// The reusable timer for delayed show, delayed hide, and visible-duration actions.
        private final PauseTransition timer = new PauseTransition();

        /// The action executed when [timer] finishes.
        private int timerAction;

        /// Updates tooltip activation timings when runtime motion settings change.
        private final M3MotionSettingsObserver motionSettingsObserver;

        /// Mirrors tooltip text into the target node's accessible help value.
        private final ChangeListener<@Nullable String> accessibleHelpListener;

        /// Observes the target hierarchy while this installation owns the node.
        private @Nullable SceneThemeListener sceneThemeListener;

        /// The accessible help value replaced by this installation.
        private @Nullable String previousAccessibleHelp;

        /// Whether this installation currently owns listeners and target-node state.
        private boolean installed;

        /// The popup root node that currently has tooltip hover handlers installed.
        private @Nullable Node tooltipRoot;

        /// The popup scene that currently has a focus owner listener installed.
        private @Nullable Scene tooltipScene;

        /// Whether the pointer is currently inside the target node.
        private boolean ownerContainsPointer;

        /// Whether the pointer is currently inside the tooltip popup.
        private boolean tooltipContainsPointer;

        /// Whether keyboard focus is currently inside the tooltip popup.
        private boolean tooltipContainsFocus;

        /// Whether a persistent tooltip was visible before the current pointer press.
        private boolean persistentShowingBeforePress;

        /// Handles pointer entry.
        private final javafx.event.EventHandler<MouseEvent> enteredHandler = this::handleEntered;

        /// Handles pointer exit.
        private final javafx.event.EventHandler<MouseEvent> exitedHandler = this::handleExited;

        /// Handles pointer presses.
        private final javafx.event.EventHandler<MouseEvent> pressedHandler = this::handlePressed;

        /// Handles explicit persistent-tooltip activation.
        private final javafx.event.EventHandler<MouseEvent> clickedHandler = this::handleClicked;

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

        /// Handles focus changes on the target node.
        private final ChangeListener<Boolean> focusListener = this::handleFocusedChanged;

        /// Handles tooltip popup visibility changes.
        private final ChangeListener<Boolean> showingListener = this::handleTooltipShowingChanged;

        /// Creates a tooltip installation.
        private TooltipInstallation(Node node, M3Tooltip tooltip) {
            this.node = node;
            this.tooltip = tooltip;
            timer.setOnFinished(event -> handleTimerFinished());
            motionSettingsObserver = new M3MotionSettingsObserver(node, this::refreshMotionSettings, false);
            accessibleHelpListener = (observable, oldValue, newValue) ->
                    node.setAccessibleHelp(accessibleHelpText(newValue));
        }

        /// Returns the installed tooltip.
        @Override
        public M3Tooltip tooltip() {
            return tooltip;
        }

        /// Adds event handlers to the target node.
        private void install() {
            if (installed) {
                return;
            }
            installed = true;
            previousAccessibleHelp = node.getAccessibleHelp();
            tooltip.textProperty().addListener(accessibleHelpListener);
            node.setAccessibleHelp(accessibleHelpText(tooltip.getText()));
            sceneThemeListener = new SceneThemeListener(node, tooltip);
            tooltip.inheritThemeFrom(node);
            node.addEventHandler(MouseEvent.MOUSE_ENTERED, enteredHandler);
            node.addEventHandler(MouseEvent.MOUSE_EXITED, exitedHandler);
            node.addEventHandler(MouseEvent.MOUSE_PRESSED, pressedHandler);
            node.addEventFilter(MouseEvent.MOUSE_CLICKED, clickedHandler);
            node.addEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
            node.focusedProperty().addListener(focusListener);
            tooltip.showingProperty().addListener(showingListener);
        }

        /// Removes event handlers and stops pending timers.
        private void uninstall() {
            if (!installed) {
                return;
            }
            installed = false;
            node.removeEventHandler(MouseEvent.MOUSE_ENTERED, enteredHandler);
            node.removeEventHandler(MouseEvent.MOUSE_EXITED, exitedHandler);
            node.removeEventHandler(MouseEvent.MOUSE_PRESSED, pressedHandler);
            node.removeEventFilter(MouseEvent.MOUSE_CLICKED, clickedHandler);
            node.removeEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
            node.focusedProperty().removeListener(focusListener);
            tooltip.showingProperty().removeListener(showingListener);
            stopTimer();
            motionSettingsObserver.dispose();
            uninstallTooltipHoverHandlers();
            @Nullable SceneThemeListener themeListener = sceneThemeListener;
            sceneThemeListener = null;
            if (themeListener != null) {
                themeListener.dispose();
            }
            tooltip.textProperty().removeListener(accessibleHelpListener);
            node.setAccessibleHelp(previousAccessibleHelp);
            previousAccessibleHelp = null;
            ownerContainsPointer = false;
            tooltipContainsPointer = false;
            tooltipContainsFocus = false;
            persistentShowingBeforePress = false;
            if (tooltip.isShowing()) {
                tooltip.hide();
            }
        }

        /// Schedules tooltip display after pointer entry.
        private void handleEntered(MouseEvent event) {
            ownerContainsPointer = true;
            if (!tooltip.usesPersistentActivation()) {
                scheduleShow();
            }
        }

        /// Schedules tooltip hiding after pointer exit.
        private void handleExited(MouseEvent event) {
            ownerContainsPointer = false;
            if (!tooltip.usesPersistentActivation()) {
                scheduleHide();
            }
        }

        /// Records the pre-press state or dismisses a transient tooltip before owner activation.
        private void handlePressed(MouseEvent event) {
            if (tooltip.usesPersistentActivation()) {
                persistentShowingBeforePress = tooltip.isShowing();
                stopTimer();
            } else {
                hideImmediately();
            }
        }

        /// Toggles a persistent tooltip after primary-button activation of its owner.
        private void handleClicked(MouseEvent event) {
            if (!tooltip.usesPersistentActivation() || event.getButton() != MouseButton.PRIMARY) {
                return;
            }
            if (persistentShowingBeforePress) {
                hideImmediately();
            } else {
                showTooltip();
            }
            persistentShowingBeforePress = false;
        }

        /// Shows or hides the tooltip when keyboard focus enters or leaves the target.
        private void handleFocusedChanged(
                ObservableValue<? extends Boolean> observable,
                Boolean oldValue,
                Boolean newValue
        ) {
            if (!tooltip.usesPersistentActivation()) {
                if (newValue) {
                    scheduleShow();
                } else {
                    scheduleHide();
                }
            }
        }

        /// Cancels pending hide while the pointer is inside an interactive tooltip popup.
        private void handleTooltipEntered(MouseEvent event) {
            tooltipContainsPointer = true;
            if (timerAction == HIDE_TIMER_ACTION) {
                stopTimer();
            }
        }

        /// Schedules hiding once the pointer leaves an interactive tooltip popup.
        private void handleTooltipExited(MouseEvent event) {
            tooltipContainsPointer = false;
            if (!tooltip.usesPersistentActivation()) {
                scheduleHide();
            }
        }

        /// Handles dismissal and keyboard traversal while focus is inside the tooltip popup.
        private void handleTooltipKeyPressed(KeyEvent event) {
            if (event.isConsumed()) {
                return;
            }
            if (event.getCode() == KeyCode.ESCAPE && tooltip.isShowing()) {
                hideImmediately();
                if (M3Accessible.canReach(node)) {
                    M3Accessible.showDirectItem(node, node);
                }
                event.consume();
            } else if ((event.getCode() == KeyCode.TAB || event.getCode() == KeyCode.F6)
                    && tooltip.isInteractive()) {
                @Nullable Node focusOwner = tooltipScene == null ? null : tooltipScene.getFocusOwner();
                if (tooltip.traverseInteractiveFocus(focusOwner, node, event.isShiftDown())) {
                    if (ownerHasKeyboardFocus()) {
                        tooltipContainsFocus = false;
                        notifyOwnerFocusNodeChanged();
                    } else {
                        markTooltipFocusActive();
                    }
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
                stopTimer();
            } else if (tooltip.isShowing() && !tooltip.usesPersistentActivation()) {
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
            if (newValue && tooltip.isInteractive()) {
                installTooltipHoverHandlers();
            } else {
                if (!newValue) {
                    stopTimer();
                }
                tooltipContainsPointer = false;
                tooltipContainsFocus = false;
                persistentShowingBeforePress = false;
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
            } else if (tooltip.usesPersistentActivation()
                    && (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE)) {
                if (tooltip.isShowing()) {
                    hideImmediately();
                } else {
                    showTooltip();
                }
                event.consume();
            } else if ((event.getCode() == KeyCode.TAB || event.getCode() == KeyCode.F6)
                    && tooltip.isInteractive()
                    && tooltip.isShowing()
                    && !event.isShiftDown()
                    && tooltip.focusFirstInteractiveTarget()) {
                markTooltipFocusActive();
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
        @Override
        public @Nullable Node activePopupFocusTarget() {
            if (!tooltip.isInteractive() || !tooltip.isShowing()) {
                return null;
            }

            @Nullable Scene scene = tooltip.getScene();
            if (scene == null) {
                return null;
            }

            @Nullable Node popupRoot = scene.getRoot();
            if (popupRoot == null) {
                return null;
            }

            @Nullable Node externalTarget = M3Accessible.activeExternalFocusTarget(popupRoot, popupRoot);
            if (externalTarget != null) {
                return externalTarget;
            }

            if (!tooltipContainsFocus) {
                return null;
            }

            @Nullable Node focusOwner = scene.getFocusOwner();
            return focusOwner != null && focusOwner.isFocused() && M3Accessible.containsNode(popupRoot, focusOwner)
                    ? focusOwner
                    : null;
        }

        /// Returns whether this installation exposes the requested interactive target.
        @Override
        public boolean containsInteractiveFocusTarget(Object... parameters) {
            return tooltip.isInteractive() && tooltip.containsInteractiveActionTarget(parameters);
        }

        /// Shows the tooltip and focuses the requested interactive target.
        @Override
        public boolean showInteractiveFocusTarget(Object... parameters) {
            if (!containsInteractiveFocusTarget(parameters)) {
                return false;
            }

            stopTimer();
            if (!tooltip.isShowing()) {
                showTooltip();
            }
            if (!tooltip.isShowing()) {
                return false;
            }

            if (!tooltip.showInteractiveActionTarget(parameters)) {
                return false;
            }
            markTooltipFocusActive();
            return true;
        }

        /// Returns whether pointer or keyboard focus is currently inside the tooltip popup.
        @Override
        public boolean hasActivePopupInteraction() {
            return tooltip.isInteractive()
                    && tooltip.isShowing()
                    && (tooltipContainsPointer || tooltipContainsFocus);
        }

        /// Marks the interactive tooltip popup as owning keyboard focus after an explicit focus request.
        private void markTooltipFocusActive() {
            tooltipContainsFocus = true;
            stopTimer();
            notifyOwnerFocusNodeChanged();
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
            stopTimer();
            if (tooltip.usesPersistentActivation()) {
                return;
            }
            if (tooltip.isInteractive() && tooltip.isShowing()) {
                return;
            }
            startTimer(SHOW_TIMER_ACTION, tooltip.effectiveShowDelay(node));
        }

        /// Schedules tooltip hiding after the configured hide delay.
        private void scheduleHide() {
            stopTimer();
            if (tooltip.usesPersistentActivation()) {
                return;
            }
            if (tooltip.isInteractive() && isTooltipActive()) {
                return;
            }
            if (tooltip.isShowing()) {
                startTimer(HIDE_TIMER_ACTION, effectiveHideDelay());
            }
        }

        /// Hides the tooltip immediately and clears pending timers.
        private void hideImmediately() {
            stopTimer();
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

            tooltip.inheritThemeFrom(node);
            Bounds screenBounds = node.localToScreen(node.getBoundsInLocal());
            if (screenBounds == null) {
                return;
            }

            @Nullable WeakReference<M3Tooltip> activeReference = activeTooltipReference;
            @Nullable M3Tooltip activeTooltip = activeReference == null ? null : activeReference.get();
            if (activeTooltip != null && activeTooltip != tooltip && activeTooltip.isShowing()) {
                activeTooltip.hide();
            }

            tooltip.show(node, screenBounds.getMinX(), screenBounds.getMaxY() + POPUP_VERTICAL_OFFSET);
            if (!tooltip.isShowing()) {
                return;
            }
            activeTooltipReference = new WeakReference<>(tooltip);
            positionTooltip(screenBounds);
            installTooltipHoverHandlers();
            scheduleAutoHide();
        }

        /// Positions the realized popup according to the plain or rich Material placement rules.
        private void positionTooltip(Bounds ownerBounds) {
            Parent popupRoot = tooltip.getScene().getRoot();
            popupRoot.applyCss();
            popupRoot.layout();

            double popupWidth = Math.max(1.0, tooltip.getWidth());
            double popupHeight = Math.max(1.0, tooltip.getHeight());
            ObservableList<Screen> screens = Screen.getScreensForRectangle(
                    ownerBounds.getMinX(),
                    ownerBounds.getMinY(),
                    Math.max(1.0, ownerBounds.getWidth()),
                    Math.max(1.0, ownerBounds.getHeight())
            );
            Rectangle2D visualBounds = screens.isEmpty()
                    ? Screen.getPrimary().getVisualBounds()
                    : screens.get(0).getVisualBounds();

            boolean rich = tooltip instanceof M3RichTooltip;
            double gap = rich ? POPUP_VERTICAL_OFFSET : 4.0;
            double edgeMargin = 8.0;
            double anchorX;
            double anchorY;
            if (rich) {
                boolean rightToLeft = node.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;
                anchorX = rightToLeft
                        ? ownerBounds.getMinX() - popupWidth - gap
                        : ownerBounds.getMaxX() + gap;
                if (anchorX < visualBounds.getMinX() + edgeMargin
                        || anchorX + popupWidth > visualBounds.getMaxX() - edgeMargin) {
                    anchorX = rightToLeft
                            ? ownerBounds.getMaxX() + gap
                            : ownerBounds.getMinX() - popupWidth - gap;
                }
                anchorY = ownerBounds.getMaxY() + gap;
                if (anchorY + popupHeight > visualBounds.getMaxY() - edgeMargin) {
                    anchorY = ownerBounds.getMinY() - popupHeight - gap;
                }
            } else {
                anchorX = ownerBounds.getMinX() + (ownerBounds.getWidth() - popupWidth) / 2.0;
                anchorY = ownerBounds.getMinY() - popupHeight - gap;
                if (anchorY < visualBounds.getMinY() + edgeMargin) {
                    anchorY = ownerBounds.getMaxY() + gap;
                }
            }

            if (popupWidth >= visualBounds.getWidth() - edgeMargin * 2.0) {
                anchorX = visualBounds.getMinX();
            } else {
                anchorX = Math.max(
                        visualBounds.getMinX() + edgeMargin,
                        Math.min(anchorX, visualBounds.getMaxX() - popupWidth - edgeMargin)
                );
            }
            if (popupHeight >= visualBounds.getHeight() - edgeMargin * 2.0) {
                anchorY = visualBounds.getMinY();
            } else {
                anchorY = Math.max(
                        visualBounds.getMinY() + edgeMargin,
                        Math.min(anchorY, visualBounds.getMaxY() - popupHeight - edgeMargin)
                );
            }
            tooltip.setAnchorX(anchorX);
            tooltip.setAnchorY(anchorY);
        }

        /// Schedules automatic hiding for finite show durations.
        private void scheduleAutoHide() {
            if (tooltip.usesPersistentActivation()) {
                return;
            }
            Duration duration = tooltip.effectiveShowDuration(node);
            if (!isFiniteDuration(duration)) {
                return;
            }
            startTimer(DURATION_TIMER_ACTION, duration);
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
            if (tooltip.usesPersistentActivation()) {
                stopTimer();
                return;
            }
            if (timerAction == SHOW_TIMER_ACTION) {
                refreshRunningTimer(
                        tooltip.effectiveShowDelay(node),
                        ownerContainsPointer || ownerHasKeyboardFocus()
                );
            } else if (timerAction == HIDE_TIMER_ACTION) {
                refreshRunningTimer(effectiveHideDelay(), tooltip.isShowing() && !isTooltipActive());
            } else {
                refreshAutoHideDuration();
            }
        }

        /// Applies changed runtime motion settings to the visible-duration timer.
        private void refreshAutoHideDuration() {
            Duration duration = tooltip.effectiveShowDuration(node);
            if (!isFiniteDuration(duration)) {
                if (timerAction == DURATION_TIMER_ACTION) {
                    stopTimer();
                }
                return;
            }

            boolean shouldRun = tooltip.isShowing() && !hasActivePopupInteraction();
            if (!shouldRun) {
                if (timerAction == DURATION_TIMER_ACTION) {
                    stopTimer();
                }
                return;
            }
            if (timerAction != DURATION_TIMER_ACTION) {
                startTimer(DURATION_TIMER_ACTION, duration);
            } else {
                refreshRunningTimer(duration, true);
            }
        }

        /// Starts the reusable timer with one pending action.
        ///
        /// @param action the action identifier executed after the delay
        /// @param duration the delay before the action
        private void startTimer(int action, Duration duration) {
            timer.stop();
            motionSettingsObserver.stop();
            timerAction = action;
            timer.setDuration(duration);
            motionSettingsObserver.start();
            timer.playFromStart();
        }

        /// Stops the reusable timer and clears its pending action.
        private void stopTimer() {
            timer.stop();
            motionSettingsObserver.stop();
            timerAction = NO_TIMER_ACTION;
        }

        /// Reconfigures a running timer after motion behavior changes.
        ///
        /// @param duration the updated delay
        /// @param restartIfRunning whether the active interaction still requires the timer
        private void refreshRunningTimer(Duration duration, boolean restartIfRunning) {
            M3Animation.updatePauseDuration(timer, duration, restartIfRunning);
            if (!restartIfRunning) {
                motionSettingsObserver.stop();
                timerAction = NO_TIMER_ACTION;
            }
        }

        /// Executes and clears the action owned by the reusable timer.
        private void handleTimerFinished() {
            motionSettingsObserver.stop();
            int action = timerAction;
            timerAction = NO_TIMER_ACTION;
            switch (action) {
                case SHOW_TIMER_ACTION -> showTooltip();
                case HIDE_TIMER_ACTION -> hideIfPointerOutside();
                case DURATION_TIMER_ACTION -> hideAfterVisibleDuration();
                default -> {
                }
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
        @SuppressWarnings("ConstantValue")
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
        private ArrayList<Parent> observedAncestorThemeRoots = new ArrayList<>();

        /// Reusable storage for collecting the current target ancestor theme roots.
        private ArrayList<Parent> ancestorThemeRootsScratch = new ArrayList<>();

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
            if (M3ThemeMetadata.isThemePropertyKey(change.getKey())) {
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
            ancestorThemeRootsScratch.clear();
            @Nullable Node current = node;
            while (current != null) {
                if (current instanceof Parent parent && parent != observedSceneRoot) {
                    ancestorThemeRootsScratch.add(parent);
                }
                current = current.getParent();
            }

            boolean unchanged = observedAncestorThemeRoots.size() == ancestorThemeRootsScratch.size();
            for (int index = 0; unchanged && index < observedAncestorThemeRoots.size(); index++) {
                unchanged = observedAncestorThemeRoots.get(index) == ancestorThemeRootsScratch.get(index);
            }
            if (unchanged) {
                ancestorThemeRootsScratch.clear();
                return;
            }

            for (Parent parent : observedAncestorThemeRoots) {
                parent.getProperties().removeListener(ancestorThemeRootPropertiesListener);
                parent.parentProperty().removeListener(ancestorParentListener);
            }
            for (Parent parent : ancestorThemeRootsScratch) {
                parent.getProperties().addListener(ancestorThemeRootPropertiesListener);
                parent.parentProperty().addListener(ancestorParentListener);
            }

            ArrayList<Parent> previousRoots = observedAncestorThemeRoots;
            observedAncestorThemeRoots = ancestorThemeRootsScratch;
            ancestorThemeRootsScratch = previousRoots;
            ancestorThemeRootsScratch.clear();
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
