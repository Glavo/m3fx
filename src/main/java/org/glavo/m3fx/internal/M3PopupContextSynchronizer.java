// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.css.StyleOrigin;
import javafx.css.StyleableBooleanProperty;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import org.glavo.m3fx.animation.M3Animated;
import org.glavo.m3fx.internal.theme.M3ThemeMetadata;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/// Synchronizes detached popup context for popup-hosted roots while they remain visible.
///
/// JavaFX popup content lives in a separate scene after it is shown, so ordinary scene inheritance is not enough
/// for Material token lookups. This helper mirrors the owner scene stylesheet list, nearest M3FX theme root,
/// effective node orientation, and local animation preference into a detached popup root, then keeps that copy fresh
/// when owner scene, stylesheet, theme, orientation, or motion settings change during the popup lifetime.
///
/// Construction, [start], [stop], [sync], and local property changes require the JavaFX application thread.
/// Consecutive stylesheet-list mutations in one application-thread turn produce one synchronization pass.
@NotNullByDefault
public final class M3PopupContextSynchronizer {
    /// The control or item that owns the popup content.
    private final Node owner;

    /// The detached popup content root that receives copied context.
    private final Parent popupRoot;

    /// Supplies the stylesheet list that should be mirrored into the popup root.
    private final Supplier<@Nullable ObservableList<String>> stylesheetSourceSupplier;

    /// Supplies the currently active theme root for popup token declarations.
    private final Supplier<@Nullable Parent> themeRootSupplier;

    /// The popup-control stylesheet appended after owner stylesheets, or `null` when none is needed.
    private final @Nullable String controlStylesheet;

    /// Runtime listeners and ancestor caches, created only after this synchronizer is first started.
    private @Nullable Observation observation;

    /// Whether listeners are currently registered.
    private boolean running;

    /// Identifies the observation run so callbacks cannot resume or tear down a superseded run.
    private long observationEpoch;

    /// Whether a synchronization pass is already in progress.
    private boolean syncing;

    /// Whether one stylesheet or theme notification already scheduled a synchronization pass.
    private boolean syncPending;

    /// Invalidates queued synchronization callbacks when observation stops or an immediate pass supersedes them.
    private long syncRequestEpoch;

    /// Whether the current observation run has copied context from an attached owner scene.
    private boolean hasSyncedAttachedOwnerContext;

    /// Bridges local motion context without copying application-wide reduced-motion settings.
    private final MotionContext motionContext;

    /// Creates a synchronizer that mirrors the owner scene stylesheets and nearest theme root.
    ///
    /// @param owner     the node that owns the popup content
    /// @param popupRoot the detached popup root to synchronize
    /// @throws NullPointerException if `owner` or `popupRoot` is `null`
    public M3PopupContextSynchronizer(Node owner, Parent popupRoot) {
        this(
                owner,
                popupRoot,
                () -> {
                    @Nullable Scene scene = owner.getScene();
                    return scene == null ? null : scene.getStylesheets();
                },
                () -> M3ThemeResolver.findThemeRoot(owner)
        );
    }

    /// Creates a synchronizer that mirrors owner context and appends one popup-control stylesheet.
    ///
    /// @param owner             the node that owns the popup content
    /// @param popupRoot         the detached popup root to synchronize
    /// @param controlStylesheet the popup-specific control stylesheet URL
    /// @throws NullPointerException if any argument is `null`
    public M3PopupContextSynchronizer(Node owner, Parent popupRoot, String controlStylesheet) {
        this(
                owner,
                popupRoot,
                () -> {
                    @Nullable Scene scene = owner.getScene();
                    return scene == null ? null : scene.getStylesheets();
                },
                () -> M3ThemeResolver.findThemeRoot(owner),
                Objects.requireNonNull(controlStylesheet, "controlStylesheet")
        );
    }

    /// Creates a synchronizer with custom stylesheet and theme-root suppliers.
    ///
    /// @param owner                    the node that owns the popup content
    /// @param popupRoot                the detached popup root to synchronize
    /// @param stylesheetSourceSupplier supplies the stylesheet list mirrored into the popup root
    /// @param themeRootSupplier        supplies the current theme root copied into the popup root
    /// @throws NullPointerException if any argument is `null`
    public M3PopupContextSynchronizer(
            Node owner,
            Parent popupRoot,
            Supplier<@Nullable ObservableList<String>> stylesheetSourceSupplier,
            Supplier<@Nullable Parent> themeRootSupplier
    ) {
        this(owner, popupRoot, stylesheetSourceSupplier, themeRootSupplier, null);
    }

    /// Creates a synchronizer with custom context suppliers and an optional popup-control stylesheet.
    ///
    /// @param owner                    the node that owns the popup content
    /// @param popupRoot                the detached popup root to synchronize
    /// @param stylesheetSourceSupplier supplies the stylesheet list mirrored into the popup root
    /// @param themeRootSupplier        supplies the current theme root copied into the popup root
    /// @param controlStylesheet        the popup-specific control stylesheet URL, or `null` when none is needed
    /// @throws NullPointerException if `owner`, `popupRoot`, `stylesheetSourceSupplier`, or `themeRootSupplier` is
    ///                               `null`
    public M3PopupContextSynchronizer(
            Node owner,
            Parent popupRoot,
            Supplier<@Nullable ObservableList<String>> stylesheetSourceSupplier,
            Supplier<@Nullable Parent> themeRootSupplier,
            @Nullable String controlStylesheet
    ) {
        this(owner, popupRoot, stylesheetSourceSupplier, themeRootSupplier, controlStylesheet,
                popupRoot instanceof M3Animated animated ? animated : null);
    }

    /// Creates a synchronizer whose popup animation property may belong to a separate popup control.
    public M3PopupContextSynchronizer(
            Node owner,
            Parent popupRoot,
            Supplier<@Nullable ObservableList<String>> stylesheetSourceSupplier,
            Supplier<@Nullable Parent> themeRootSupplier,
            @Nullable String controlStylesheet,
            @Nullable M3Animated popupControl
    ) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.popupRoot = Objects.requireNonNull(popupRoot, "popupRoot");
        this.stylesheetSourceSupplier = Objects.requireNonNull(stylesheetSourceSupplier, "stylesheetSourceSupplier");
        this.themeRootSupplier = Objects.requireNonNull(themeRootSupplier, "themeRootSupplier");
        this.controlStylesheet = controlStylesheet;
        motionContext = new MotionContext(owner, popupRoot, popupControl);
    }

    /// Starts observing owner context and immediately synchronizes the popup root.
    ///
    /// Calling this method while the synchronizer is running requests synchronization through [sync] without adding
    /// duplicate listeners. A callback may stop or restart observation; the superseded startup does not resume it
    /// or remove listeners installed by the replacement run if that startup fails. Failure to start a new run removes
    /// that run's listeners without rolling back popup state already copied.
    public void start() {
        if (running) {
            sync();
            return;
        }

        running = true;
        long epoch = ++observationEpoch;
        hasSyncedAttachedOwnerContext = false;
        @Nullable Observation currentObservation = observation;
        if (currentObservation == null) {
            currentObservation = new Observation();
            observation = currentObservation;
        }
        try {
            currentObservation.start();
            sync();
            if (epoch == observationEpoch && running) {
                motionContext.start();
            }
        } catch (RuntimeException | Error exception) {
            if (epoch == observationEpoch) {
                stop();
            }
            throw exception;
        }
    }

    /// Stops observing owner context and removes the temporary motion fallback.
    ///
    /// Calling this method while the synchronizer is stopped has no effect. A stopped synchronizer may be started
    /// again. If called from a synchronization callback, the current copying step may finish notifying listeners,
    /// but subsequent copying, CSS, and layout steps from that pass are skipped.
    public void stop() {
        if (!running) {
            return;
        }

        running = false;
        observationEpoch++;
        cancelPendingSync();
        hasSyncedAttachedOwnerContext = false;
        @Nullable Observation currentObservation = observation;
        if (currentObservation != null) {
            currentObservation.stop();
        }
        motionContext.stop();
    }

    /// Copies the latest owner stylesheet, theme, orientation, and motion context into the popup root.
    ///
    /// While observation is running, calls and observed context changes made during a synchronization pass are
    /// coalesced into a later application-thread pass. Reentrant calls while stopped have no effect. Suppliers are
    /// evaluated during the synchronization pass and may return `null` as declared by their types.
    public void sync() {
        if (syncing) {
            requestSync();
            return;
        }
        cancelPendingSync();
        if (running && owner.getScene() == null && hasSyncedAttachedOwnerContext) {
            return;
        }

        syncing = true;
        long epoch = observationEpoch;
        try {
            @Nullable ObservableList<String> stylesheetSource = stylesheetSourceSupplier.get();
            if (epoch != observationEpoch) {
                return;
            }
            @Nullable Parent themeRoot = themeRootSupplier.get();
            if (epoch != observationEpoch) {
                return;
            }
            if (running) {
                @Nullable Observation currentObservation = observation;
                if (currentObservation != null) {
                    currentObservation.refreshThemeRoots(stylesheetSource, themeRoot);
                }
            }

            syncNodeOrientation();
            if (epoch != observationEpoch) {
                return;
            }
            M3PopupStyles.preparePopupRoot(
                    popupRoot,
                    stylesheetSource == null ? List.of() : stylesheetSource,
                    themeRoot,
                    controlStylesheet
            );
            if (epoch == observationEpoch) {
                motionContext.sync();
            }
            applyCssAndLayout(epoch);
            if (epoch == observationEpoch && owner.getScene() != null) {
                hasSyncedAttachedOwnerContext = true;
            }
        } finally {
            syncing = false;
        }
    }

    /// Mirrors the owner's effective orientation when the popup root is not already bound by its owner control.
    private void syncNodeOrientation() {
        if (popupRoot.nodeOrientationProperty().isBound()) {
            return;
        }

        NodeOrientation ownerOrientation = owner.getEffectiveNodeOrientation();
        if (popupRoot.getNodeOrientation() != ownerOrientation) {
            popupRoot.setNodeOrientation(ownerOrientation);
        }
    }

    /// Updates orientation-dependent popup CSS and layout without rebuilding unrelated theme context.
    private void syncNodeOrientationAndLayout() {
        if (!running) {
            return;
        }
        if (syncing) {
            requestSync();
            return;
        }

        syncing = true;
        long epoch = observationEpoch;
        try {
            syncNodeOrientation();
            applyCssAndLayout(epoch);
        } finally {
            syncing = false;
        }
    }

    /// Applies CSS and layout unless a callback has stopped or replaced the observation run.
    private void applyCssAndLayout(long epoch) {
        if (epoch != observationEpoch) {
            return;
        }
        popupRoot.applyCss();
        if (epoch == observationEpoch) {
            popupRoot.layout();
        }
    }

    /// Coalesces related stylesheet and theme notifications into one JavaFX application-thread synchronization.
    private void requestSync() {
        if (!running || syncPending) {
            return;
        }

        syncPending = true;
        long requestEpoch = syncRequestEpoch;
        Platform.runLater(() -> {
            if (!syncPending || requestEpoch != syncRequestEpoch) {
                return;
            }
            syncPending = false;
            if (running) {
                sync();
            }
        });
    }

    /// Invalidates one queued synchronization pass without touching copied popup context.
    private void cancelPendingSync() {
        if (syncPending) {
            syncPending = false;
            syncRequestEpoch++;
        }
    }

    /// Supplies an inheritable CSS fallback for detached content without changing application styles or bindings.
    /// Global reduced motion is deliberately resolved only when an animation is played.
    /// Owners without [M3Animated] provide no readable preference, so no fallback is synthesized for them.
    /// A popup's bound or programmatically assigned preference can still supply the fallback. Popup controls also
    /// retain their resolved CSS preference, including native inheritance through their styleable parent.
    @NotNullByDefault
    public static final class MotionContext {
        /// Root-only scope attached to each private popup host.
        private static final String SCOPE = "m3-popup-motion-context";

        /// Shared fallback enabling local animations.
        private static final String ENABLED_STYLESHEET = stylesheet(true);

        /// Shared fallback disabling local animations.
        private static final String DISABLED_STYLESHEET = stylesheet(false);

        /// The node supplying the local animation preference.
        private final Node owner;

        /// The detached content root.
        private final Parent root;

        /// The popup's own animation preference, when available.
        private final @Nullable M3Animated popupControl;

        /// Refreshes local context after a property or parent change.
        private final InvalidationListener listener = observable -> sync();

        /// Waits until StyleableBooleanProperty has assigned the new style origin after notifying listeners.
        private final InvalidationListener popupListener = observable -> requestSync();

        /// Invalidates deferred work when observation stops.
        private long epoch;

        /// Whether one deferred property refresh is queued.
        private boolean syncPending;

        /// The ordinary ancestor carrying the fallback declaration.
        private @Nullable Parent stylesheetRoot;

        /// Whether this context added the host's scope class.
        private boolean scopeAdded;

        /// Whether listeners are registered.
        private boolean observing;

        /// Prevents CSS application from recursively refreshing the same context.
        private boolean syncing;

        /// The property that received this bridge's temporary user-agent fallback.
        private @Nullable StyleableBooleanProperty fallbackProperty;

        /// Creates a motion-only bridge; call [start] and [stop] with the presentation lifecycle.
        public MotionContext(Node owner, Parent root, @Nullable M3Animated popupControl) {
            this.owner = Objects.requireNonNull(owner, "owner");
            this.root = Objects.requireNonNull(root, "root");
            this.popupControl = popupControl;
        }

        /// Observes local properties and installs the current fallback.
        public void start() {
            if (!observing) {
                observing = true;
                epoch++;
                root.parentProperty().addListener(listener);
                if (owner instanceof M3Animated animated) {
                    animated.animationsEnabledProperty().addListener(listener);
                }
                if (popupControl != null && popupControl != owner) {
                    popupControl.animationsEnabledProperty().addListener(popupListener);
                }
                if (popupControl instanceof PopupControl popup) {
                    popup.skinProperty().addListener(listener);
                }
            }
            sync();
        }

        /// Removes listeners and temporary declarations without altering application-owned values.
        public void stop() {
            observing = false;
            long currentEpoch = ++epoch;
            syncPending = false;
            root.parentProperty().removeListener(listener);
            if (owner instanceof M3Animated animated) {
                animated.animationsEnabledProperty().removeListener(listener);
            }
            if (popupControl != null && popupControl != owner) {
                popupControl.animationsEnabledProperty().removeListener(popupListener);
            }
            if (popupControl instanceof PopupControl popup) {
                popup.skinProperty().removeListener(listener);
            }
            clearStylesheet();
            if (currentEpoch == epoch) {
                clearPropertyFallback();
            }
        }

        /// Restores the fallback after stylesheet replacement and applies it through normal CSS inheritance.
        public void sync() {
            if (syncing) {
                return;
            }
            syncing = true;
            long currentEpoch = epoch;
            try {
                @Nullable Boolean enabled = owner instanceof M3Animated animated
                        ? animated.isAnimationsEnabled() : null;
                if (popupControl != null) {
                    StyleableBooleanProperty property = popupControl.animationsEnabledProperty();
                    boolean popupEnabled = property.get();
                    @Nullable StyleOrigin origin = property.getStyleOrigin();
                    if (property.isBound() || origin == StyleOrigin.USER
                            || popupControl instanceof PopupControl
                            && (origin == StyleOrigin.AUTHOR || origin == StyleOrigin.INLINE)) {
                        enabled = popupEnabled;
                    }
                }

                if (enabled == null) {
                    @Nullable Parent previousHost = stylesheetRoot;
                    clearStylesheet();
                    if (currentEpoch == epoch) {
                        clearPropertyFallback();
                        if (previousHost != null && currentEpoch == epoch) {
                            previousHost.applyCss();
                        }
                    }
                    return;
                }

                if (popupControl instanceof PopupControl) {
                    StyleableBooleanProperty property = popupControl.animationsEnabledProperty();
                    @Nullable StyleOrigin origin = property.getStyleOrigin();
                    if (!property.isBound() && property.get() != enabled
                            && (origin == null || origin == StyleOrigin.USER_AGENT)) {
                        fallbackProperty = property;
                        property.applyStyle(StyleOrigin.USER_AGENT, enabled);
                    }
                    if (currentEpoch != epoch) {
                        return;
                    }
                }

                // An animated root inherits from its popup container, preserving its own CSS and USER values.
                @Nullable Parent host = root instanceof M3Animated ? root.getParent() : root;
                if (popupControl instanceof PopupControl popup) {
                    @Nullable Skin<?> skin = popup.getSkin();
                    if (skin != null && skin.getNode() instanceof Parent skinRoot) {
                        // PopupControl's CSS bridge uses its owner as styleable parent, bypassing the scene root.
                        host = skinRoot;
                    }
                }
                if (stylesheetRoot != host) {
                    clearStylesheet();
                    if (currentEpoch != epoch) {
                        return;
                    }
                    stylesheetRoot = host;
                }
                if (host != null) {
                    boolean changed = false;
                    if (!host.getStyleClass().contains(SCOPE)) {
                        scopeAdded = true;
                        host.getStyleClass().add(SCOPE);
                        changed = true;
                    }
                    if (currentEpoch != epoch) {
                        return;
                    }
                    String stylesheet = enabled ? ENABLED_STYLESHEET : DISABLED_STYLESHEET;
                    changed |= host.getStylesheets().remove(enabled ? DISABLED_STYLESHEET : ENABLED_STYLESHEET);
                    if (currentEpoch != epoch) {
                        return;
                    }
                    if (!host.getStylesheets().contains(stylesheet)) {
                        host.getStylesheets().add(0, stylesheet);
                        changed = true;
                    }
                    if (changed && currentEpoch == epoch) {
                        host.applyCss();
                    }
                } else if (root instanceof M3Animated animated) {
                    StyleableBooleanProperty property = animated.animationsEnabledProperty();
                    @Nullable StyleOrigin origin = property.getStyleOrigin();
                    if (!property.isBound() && property.get() != enabled
                            && (origin == null || origin == StyleOrigin.USER_AGENT)) {
                        fallbackProperty = property;
                        property.applyStyle(StyleOrigin.USER_AGENT, enabled);
                    }
                }
            } finally {
                syncing = false;
            }
        }

        /// Coalesces popup-property changes without observing any descendants or global settings.
        private void requestSync() {
            if (!observing || syncPending) {
                return;
            }
            syncPending = true;
            long currentEpoch = epoch;
            Platform.runLater(() -> {
                if (currentEpoch == epoch && observing) {
                    syncPending = false;
                    sync();
                }
            });
        }

        /// Removes only resources installed by this bridge.
        private void clearStylesheet() {
            @Nullable Parent host = stylesheetRoot;
            stylesheetRoot = null;
            boolean removeScope = scopeAdded;
            scopeAdded = false;
            long currentEpoch = epoch;
            if (host != null) {
                host.getStylesheets().removeAll(ENABLED_STYLESHEET, DISABLED_STYLESHEET);
                if (removeScope && currentEpoch == epoch) {
                    host.getStyleClass().remove(SCOPE);
                }
            }
        }

        /// Releases only a user-agent fallback still owned by this bridge.
        private void clearPropertyFallback() {
            @Nullable StyleableBooleanProperty property = fallbackProperty;
            fallbackProperty = null;
            if (property != null && !property.isBound() && property.getStyleOrigin() == StyleOrigin.USER_AGENT) {
                property.applyStyle(null, true);
            }
        }

        /// Encodes a root-only declaration; descendants inherit it without selector enumeration.
        private static String stylesheet(boolean enabled) {
            String css = "." + SCOPE + " { -m3-animations-enabled: " + enabled + "; }";
            return "data:text/css;base64," + Base64.getEncoder().encodeToString(css.getBytes(StandardCharsets.UTF_8));
        }
    }

    /// Owns listeners and mutable lookup caches used only while popup context is observed.
    @NotNullByDefault
    private final class Observation {
        /// Handles owner scene changes.
        private final ChangeListener<@Nullable Scene> ownerSceneListener =
                (observable, oldScene, newScene) -> sync();

        /// Handles owner parent-chain changes.
        private final ChangeListener<@Nullable Parent> ownerParentListener =
                (observable, oldParent, newParent) -> sync();

        /// Handles owner effective orientation changes.
        private final ChangeListener<NodeOrientation> ownerOrientationListener =
                (observable, oldValue, newValue) -> syncNodeOrientationAndLayout();

        /// Handles root changes on the current owner scene.
        private final ChangeListener<Parent> sceneRootListener =
                (observable, oldRoot, newRoot) -> sync();

        /// Handles stylesheet list mutations from the current stylesheet source.
        private final ListChangeListener<String> stylesheetSourceListener = change -> requestSync();

        /// Handles theme metadata changes on the owner scene root.
        private final MapChangeListener<Object, Object> sceneRootPropertiesListener =
                this::handleThemeRootPropertiesChanged;

        /// Handles theme metadata changes on the currently resolved active theme root.
        private final MapChangeListener<Object, Object> activeThemeRootPropertiesListener =
                this::handleThemeRootPropertiesChanged;

        /// Handles theme metadata changes on owner ancestors that may become the active local theme root.
        private final MapChangeListener<Object, Object> ancestorThemeRootPropertiesListener =
                this::handleThemeRootPropertiesChanged;

        /// Handles parent-chain changes on owner ancestors while a popup remains visible.
        private final ChangeListener<@Nullable Parent> ancestorParentListener =
                (observable, oldParent, newParent) -> sync();

        /// The stylesheet list currently observed for mutations.
        private @Nullable ObservableList<String> observedStylesheetSource;

        /// The scene currently observed for root changes.
        private @Nullable Scene observedScene;

        /// The scene root currently observed for theme metadata changes.
        private @Nullable Parent observedSceneRoot;

        /// The active theme root currently observed for direct theme metadata changes.
        private @Nullable Parent observedThemeRoot;

        /// Owner ancestors currently observed for future local theme installation.
        private ArrayList<Parent> observedAncestorThemeRoots = new ArrayList<>();

        /// Reusable storage for collecting the current owner ancestor chain.
        private ArrayList<Parent> ancestorThemeRootsScratch = new ArrayList<>();

        /// Installs owner-context listeners before the first synchronization pass.
        private void start() {
            owner.sceneProperty().addListener(ownerSceneListener);
            owner.parentProperty().addListener(ownerParentListener);
            owner.effectiveNodeOrientationProperty().addListener(ownerOrientationListener);
        }

        /// Removes every external listener and clears observed object references.
        private void stop() {
            owner.sceneProperty().removeListener(ownerSceneListener);
            owner.parentProperty().removeListener(ownerParentListener);
            owner.effectiveNodeOrientationProperty().removeListener(ownerOrientationListener);
            updateObservedStylesheetSource(null);
            updateObservedScene(null);
            updateObservedSceneRoot(null);
            updateObservedThemeRoot(null);
            clearObservedAncestorThemeRoots();
        }

        /// Handles installed-theme metadata changes on observed roots.
        private void handleThemeRootPropertiesChanged(MapChangeListener.Change<?, ?> change) {
            if (M3ThemeMetadata.isThemePropertyKey(change.getKey())) {
                sync();
            }
        }

        /// Refreshes stylesheet and theme-related observers from one resolved context snapshot.
        private void refreshThemeRoots(
                @Nullable ObservableList<String> stylesheetSource,
                @Nullable Parent themeRoot
        ) {
            updateObservedStylesheetSource(stylesheetSource);
            updateObservedScene(owner.getScene());
            updateObservedSceneRoot(observedScene == null ? null : observedScene.getRoot());
            updateObservedThemeRoot(themeRoot);
            updateObservedAncestorThemeRoots();
        }

        /// Updates the observed stylesheet list.
        private void updateObservedStylesheetSource(@Nullable ObservableList<String> stylesheetSource) {
            if (observedStylesheetSource == stylesheetSource) {
                return;
            }
            if (observedStylesheetSource != null) {
                observedStylesheetSource.removeListener(stylesheetSourceListener);
            }
            observedStylesheetSource = stylesheetSource;
            if (observedStylesheetSource != null) {
                observedStylesheetSource.addListener(stylesheetSourceListener);
            }
        }

        /// Updates the observed scene.
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

        /// Updates the observed scene root.
        private void updateObservedSceneRoot(@Nullable Parent sceneRoot) {
            if (observedSceneRoot == sceneRoot) {
                return;
            }
            if (observedSceneRoot != null) {
                observedSceneRoot.getProperties().removeListener(sceneRootPropertiesListener);
            }
            observedSceneRoot = sceneRoot;
            if (observedSceneRoot != null) {
                observedSceneRoot.getProperties().addListener(sceneRootPropertiesListener);
            }
        }

        /// Updates the observed active theme root.
        private void updateObservedThemeRoot(@Nullable Parent themeRoot) {
            @Nullable Parent nextThemeRoot = themeRoot == observedSceneRoot ? null : themeRoot;
            if (observedThemeRoot == nextThemeRoot) {
                return;
            }
            if (observedThemeRoot != null) {
                observedThemeRoot.getProperties().removeListener(activeThemeRootPropertiesListener);
            }
            observedThemeRoot = nextThemeRoot;
            if (observedThemeRoot != null) {
                observedThemeRoot.getProperties().addListener(activeThemeRootPropertiesListener);
            }
        }

        /// Updates owner ancestors that could receive a local theme while the popup remains open.
        private void updateObservedAncestorThemeRoots() {
            ancestorThemeRootsScratch.clear();
            @Nullable Node current = owner;
            while (current != null) {
                if (current instanceof Parent parent
                        && parent != observedSceneRoot
                        && parent != observedThemeRoot) {
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

        /// Removes local-theme listeners from all previously observed owner ancestors.
        private void clearObservedAncestorThemeRoots() {
            for (Parent parent : observedAncestorThemeRoots) {
                parent.getProperties().removeListener(ancestorThemeRootPropertiesListener);
                parent.parentProperty().removeListener(ancestorParentListener);
            }
            observedAncestorThemeRoots.clear();
        }
    }
}
