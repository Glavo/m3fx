// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Window;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/// Keeps keyboard traversal inside one modal Material surface while it is active in a scene.
@NotNullByDefault
public final class M3ModalFocusTrap {
    /// Active traps in each scene, ordered from oldest to most recently activated.
    private static final Map<Scene, List<M3ModalFocusTrap>> ACTIVE_TRAPS = new WeakHashMap<>();

    /// The modal surface that owns focus traversal.
    private final Node owner;

    /// Supplies whether the owner should currently trap focus.
    private final BooleanSupplier activeSupplier;

    /// Supplies reachable focus target candidates in traversal order.
    private final Supplier<List<Node>> focusTargetsSupplier;

    /// Runs when Escape should dismiss the modal owner.
    private final @Nullable Runnable escapeAction;

    /// Handles scene-level key presses before the normal JavaFX traversal engine runs.
    private final EventHandler<KeyEvent> keyPressedHandler = this::handleKeyPressed;

    /// Tracks owner scene changes so filters move with the owner.
    private final ChangeListener<@Nullable Scene> sceneListener = (observable, oldScene, newScene) -> {
        observeScene(newScene);
        update();
    };

    /// Tracks scene window changes so modal ownership is recomputed after a scene is attached to a stage.
    private final ChangeListener<@Nullable Window> windowListener = (observable, oldWindow, newWindow) -> {
        observeWindow(newWindow);
        update();
    };

    /// Tracks window visibility changes so traps install after a prebuilt scene is shown and detach after hide.
    private final ChangeListener<Boolean> windowShowingListener = (observable, oldValue, newValue) -> update();

    /// The scene currently observed for window changes.
    private @Nullable Scene observedScene;

    /// The window currently observed for showing changes.
    private @Nullable Window observedWindow;

    /// The scene currently receiving this trap's key filter.
    private @Nullable Scene installedScene;

    /// Creates a modal focus trap.
    ///
    /// @param owner the modal owner node
    /// @param activeSupplier supplies whether the trap is active
    /// @param focusTargetsSupplier supplies focus targets in traversal order
    /// @param escapeAction the optional Escape dismissal action
    public M3ModalFocusTrap(
            Node owner,
            BooleanSupplier activeSupplier,
            Supplier<List<Node>> focusTargetsSupplier,
            @Nullable Runnable escapeAction
    ) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.activeSupplier = Objects.requireNonNull(activeSupplier, "activeSupplier");
        this.focusTargetsSupplier = Objects.requireNonNull(focusTargetsSupplier, "focusTargetsSupplier");
        this.escapeAction = escapeAction;
    }

    /// Starts tracking owner scene changes.
    public void install() {
        owner.sceneProperty().addListener(sceneListener);
        observeScene(owner.getScene());
        update();
    }

    /// Stops tracking owner scene changes and removes any installed scene filter.
    public void uninstall() {
        owner.sceneProperty().removeListener(sceneListener);
        observeScene(null);
        installOnScene(null);
    }

    /// Synchronizes the installed scene filter with the current active state.
    public void update() {
        @Nullable Scene scene = activeSupplier.getAsBoolean() ? owner.getScene() : null;
        @Nullable Scene targetScene = scene != null && windowShowing(scene) ? scene : null;
        if (installedScene == targetScene) {
            if (targetScene != null) {
                register(targetScene);
            }
            return;
        }
        installOnScene(targetScene);
    }

    /// Returns whether the scene is attached to a visible window.
    private boolean windowShowing(Scene scene) {
        @Nullable Window window = scene.getWindow();
        return window != null && window.isShowing();
    }

    /// Observes window changes on the current owner scene.
    private void observeScene(@Nullable Scene scene) {
        if (observedScene == scene) {
            return;
        }

        if (observedScene != null) {
            observedScene.windowProperty().removeListener(windowListener);
        }
        observedScene = scene;
        observeWindow(scene == null ? null : scene.getWindow());
        if (scene != null) {
            scene.windowProperty().addListener(windowListener);
        }
    }

    /// Observes showing changes on the current owner window.
    private void observeWindow(@Nullable Window window) {
        if (observedWindow == window) {
            return;
        }

        if (observedWindow != null) {
            observedWindow.showingProperty().removeListener(windowShowingListener);
        }
        observedWindow = window;
        if (window != null) {
            window.showingProperty().addListener(windowShowingListener);
        }
    }

    /// Installs this trap on the requested scene, replacing any previous scene filter.
    private void installOnScene(@Nullable Scene scene) {
        if (installedScene == scene) {
            return;
        }

        if (installedScene != null) {
            installedScene.removeEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
            unregister(installedScene);
        }
        installedScene = scene;
        if (scene != null) {
            register(scene);
            scene.addEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
        }
    }

    /// Registers this trap as the most recently activated trap in a scene.
    private void register(Scene scene) {
        List<M3ModalFocusTrap> traps = ACTIVE_TRAPS.computeIfAbsent(scene, key -> new ArrayList<>());
        traps.remove(this);
        traps.add(this);
    }

    /// Removes this trap from a scene's active trap stack.
    private void unregister(Scene scene) {
        List<M3ModalFocusTrap> traps = ACTIVE_TRAPS.get(scene);
        if (traps == null) {
            return;
        }

        traps.remove(this);
        if (traps.isEmpty()) {
            ACTIVE_TRAPS.remove(scene);
        }
    }

    /// Returns whether this trap is the topmost active trap in its scene.
    private boolean isTopmostActiveTrap() {
        Scene scene = installedScene;
        if (scene == null) {
            return false;
        }

        List<M3ModalFocusTrap> traps = ACTIVE_TRAPS.get(scene);
        if (traps == null) {
            return false;
        }

        for (int index = traps.size() - 1; index >= 0; index--) {
            M3ModalFocusTrap trap = traps.get(index);
            if (trap.installedScene == scene && trap.activeSupplier.getAsBoolean()) {
                return trap == this;
            }
        }
        return false;
    }

    /// Handles keyboard dismissal and cyclic focus traversal for the active modal owner.
    private void handleKeyPressed(KeyEvent event) {
        if (event.isConsumed() || !activeSupplier.getAsBoolean() || !isTopmostActiveTrap()) {
            return;
        }

        switch (event.getCode()) {
            case ESCAPE -> {
                if (escapeAction != null) {
                    escapeAction.run();
                    event.consume();
                }
            }
            case TAB, F6 -> M3FocusTraversal.handleCyclicTabKeyFocus(owner, event, focusTargetsSupplier.get());
            default -> {
            }
        }
    }
}