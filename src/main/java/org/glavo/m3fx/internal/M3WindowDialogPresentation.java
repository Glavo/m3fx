// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.event.EventHandler;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.glavo.m3fx.controls.M3DialogPane;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Presents one Material dialog inside a dedicated native JavaFX Stage.
///
/// The presentation uses native window modality and intentionally has no cross-window scrim. It owns Stage
/// visibility only for the duration of one dialog presentation and translates native close requests into the
/// dialog's cancellable close lifecycle.
@NotNullByDefault
public final class M3WindowDialogPresentation implements M3DialogPresentation {
    /// The native stage owned by the public window host.
    private final Stage stage;

    /// The stable scene expected to remain installed on the Stage.
    private final Scene scene;

    /// The stable scene root that receives the dialog pane.
    private final Pane root;

    /// The retained Material dialog pane.
    private final M3DialogPane pane;

    /// Synchronizes theme and direction before the pane is attached.
    private final Runnable prepareContext;

    /// Requests closure through the owning dialog lifecycle.
    private final Runnable closeRequest;

    /// Releases the public host's exact-presentation reservation.
    private final Runnable releaseHost;

    /// Consumes native window close requests and routes them through the dialog.
    private final EventHandler<WindowEvent> windowCloseRequestHandler = this::handleWindowCloseRequest;

    /// Whether this presentation currently owns Stage visibility and root content.
    private boolean installed;

    /// Whether this presentation has permanently released its host reservation.
    private boolean disposed;

    /// Creates a detached native-window presentation.
    ///
    /// @param stage          the native stage to show
    /// @param scene          the stable scene retained by the host
    /// @param root           the stable scene root
    /// @param pane           the dialog pane to install
    /// @param prepareContext the action that synchronizes theme and direction
    /// @param closeRequest   the action that requests dialog closure
    /// @param releaseHost    the action that releases the host reservation exactly once
    /// @throws NullPointerException if any argument is `null`
    public M3WindowDialogPresentation(
            Stage stage,
            Scene scene,
            Pane root,
            M3DialogPane pane,
            Runnable prepareContext,
            Runnable closeRequest,
            Runnable releaseHost
    ) {
        this.stage = Objects.requireNonNull(stage, "stage");
        this.scene = Objects.requireNonNull(scene, "scene");
        this.root = Objects.requireNonNull(root, "root");
        this.pane = Objects.requireNonNull(pane, "pane");
        this.prepareContext = Objects.requireNonNull(prepareContext, "prepareContext");
        this.closeRequest = Objects.requireNonNull(closeRequest, "closeRequest");
        this.releaseHost = Objects.requireNonNull(releaseHost, "releaseHost");
    }

    /// Returns the dedicated scene root supplying theme and motion context.
    ///
    /// @return the non-null stable root
    @Override
    public Parent getContextRoot() {
        return root;
    }

    /// Prepares theme context and verifies native Stage invariants.
    @Override
    public void prepare() {
        if (disposed || installed || stage.isShowing() || !root.getChildren().isEmpty()) {
            throw new IllegalStateException("dialog window is already presenting a dialog");
        }
        if (stage.getScene() != scene || scene.getRoot() != root) {
            throw new IllegalStateException("dialog window scene ownership was modified");
        }
        if (stage.getModality() == Modality.WINDOW_MODAL && stage.getOwner() == null) {
            throw new IllegalStateException("WINDOW_MODAL dialog windows require an owner");
        }
        prepareContext.run();
    }

    /// Attaches the dialog pane and shows the native Stage.
    @Override
    public void install() {
        if (disposed || installed) {
            throw new IllegalStateException("dialog window presentation is already installed");
        }

        boolean completed = false;
        root.getChildren().setAll(pane);
        stage.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, windowCloseRequestHandler);
        try {
            boolean stageHasPreviousGeometry = stage.getWidth() > 0.0 && stage.getHeight() > 0.0;
            if (!stageHasPreviousGeometry) {
                root.applyCss();
                root.layout();
                stage.sizeToScene();
            }
            stage.show();
            root.applyCss();
            root.layout();
            stage.sizeToScene();
            installed = true;
            completed = true;
        } finally {
            if (!completed) {
                stage.removeEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, windowCloseRequestHandler);
                root.getChildren().clear();
                if (stage.isShowing()) {
                    stage.hide();
                }
            }
        }
    }

    /// Ignores scrim-dismiss configuration because native windows have no Material scrim.
    ///
    /// @param dismissOnClick ignored
    @Override
    public void setDismissOnScrimClick(boolean dismissOnClick) {
    }

    /// Completes immediately because a native window has no separately animated background.
    ///
    /// @param onFinished the action invoked synchronously
    /// @throws NullPointerException if `onFinished` is `null`
    @Override
    public void startBackgroundExit(Runnable onFinished) {
        Objects.requireNonNull(onFinished, "onFinished").run();
    }

    /// Hides the native Stage and detaches the dialog pane.
    @Override
    public void dispose() {
        if (disposed) {
            return;
        }
        disposed = true;
        stage.removeEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, windowCloseRequestHandler);
        boolean wasInstalled = installed;
        installed = false;
        if (wasInstalled && stage.isShowing()) {
            stage.hide();
        }
        root.getChildren().remove(pane);
        releaseHost.run();
    }

    /// Routes one native close request through the dialog before JavaFX can hide the Stage directly.
    private void handleWindowCloseRequest(WindowEvent event) {
        event.consume();
        closeRequest.run();
    }
}
