// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.glavo.m3fx.internal.M3DialogPresentation;
import org.glavo.m3fx.internal.M3ThemeResolver;
import org.glavo.m3fx.internal.M3WindowDialogPresentation;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Presents [M3Dialog] instances in a dedicated native JavaFX window.
///
/// A dialog window is an alternative presentation host for applications that do not have an existing
/// [M3OverlayPane], including applications whose primary interaction is a single dialog. It owns a stable
/// [Stage] and scene internally, while [#showDialog(M3Dialog)] returns the same exact-presentation
/// [M3DialogHandle] used by in-scene dialogs. The dialog remains non-blocking; this class intentionally does not
/// provide a nested-event-loop `showAndWait` operation.
///
/// The native window has no Material scrim. Native modality is controlled with [#initModality(Modality)], and the
/// window manager's close request is translated into the dialog's cancellable [M3DialogEvent#CLOSE_REQUEST]. Once
/// accepted, the dialog exit transition completes before the native window is hidden. Calling [Stage#hide()] is not
/// exposed because it would bypass that cancellable lifecycle.
///
/// Window owner, modality, and style follow JavaFX initialization rules and must be configured before the window is
/// first shown. A `WINDOW_MODAL` window requires a non-null owner. The default configuration is ownerless,
/// non-modal, decorated, and non-resizable. When [#getTheme()] is `null`, each presentation snapshots the owner's
/// scene theme when available, otherwise [M3Theme#defaultTheme()] is used. Setting an explicit theme while the
/// dialog is visible updates the dedicated scene immediately.
///
/// ```java
/// M3Dialog dialog = new M3Dialog();
/// dialog.getDialogPane().setHeaderText("Settings");
///
/// M3DialogWindow window = new M3DialogWindow();
/// window.setTitle("Settings");
/// M3DialogHandle handle = window.showDialog(dialog);
/// ```
///
/// See [Material Design dialogs](https://m3.material.io/components/dialogs/overview).
@NotNullByDefault
public final class M3DialogWindow {
    /// The style class installed on the dedicated scene root.
    public static final String STYLE_CLASS = "m3-dialog-window-root";

    /// Creates an ownerless, non-modal, decorated dialog window.
    ///
    /// This constructor must run on the JavaFX Application Thread after the JavaFX toolkit has started.
    ///
    /// @throws IllegalStateException if called off the JavaFX Application Thread
    public M3DialogWindow() {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("dialog windows must be created on the JavaFX application thread");
        }

        root.getStyleClass().add(STYLE_CLASS);
        scene = new Scene(root);
        stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("");
        stage.setResizable(false);
        themeValue.addListener((observable, oldTheme, newTheme) -> {
            if (stage.isShowing() || activePresentation != null) {
                installEffectiveTheme();
                root.applyCss();
            }
        });
    }

    /// Returns this dialog window's title.
    ///
    /// @return the non-null title, which is empty by default
    public String getTitle() {
        return Objects.requireNonNull(stage.getTitle(), "title");
    }

    /// Sets this dialog window's title.
    ///
    /// @param title the non-null native window title
    /// @throws NullPointerException if `title` is `null`
    /// @throws RuntimeException     if [#titleProperty()] is bound
    public void setTitle(String title) {
        stage.setTitle(Objects.requireNonNull(title, "title"));
    }

    /// Returns the observable property that stores this dialog window's title.
    ///
    /// The property can be observed and bound. Its default value is the empty string. Values assigned directly or
    /// supplied by a binding must be non-null, and changes update the native window title.
    ///
    /// @return the non-null title property
    public StringProperty titleProperty() {
        return stage.titleProperty();
    }

    /// Returns whether this dialog window can be resized by the user.
    ///
    /// @return `true` when native resizing is enabled
    public boolean isResizable() {
        return stage.isResizable();
    }

    /// Sets whether this dialog window can be resized by the user.
    ///
    /// @param resizable whether native resizing is enabled
    /// @throws RuntimeException if [#resizableProperty()] is bound
    public void setResizable(boolean resizable) {
        stage.setResizable(resizable);
    }

    /// Returns the observable property controlling whether this dialog window can be resized by the user.
    ///
    /// The property can be observed and bound. Its default value is `false`, and changes update native resizing.
    ///
    /// @return the non-null resizable property
    public BooleanProperty resizableProperty() {
        return stage.resizableProperty();
    }

    /// The explicit theme, or `null` to resolve an owner theme or the default theme for each presentation.
    private final ObjectProperty<@Nullable M3Theme> themeValue =
            new SimpleObjectProperty<>(this, "theme");

    /// Returns the explicit theme installed for standalone presentations.
    ///
    /// A `null` value selects the owner's current scene theme when one is installed, or the default M3FX theme for
    /// an ownerless or unthemed window.
    ///
    /// @return the explicit theme, or `null` for automatic resolution
    public @Nullable M3Theme getTheme() {
        return themeValue.get();
    }

    /// Sets the explicit theme installed for standalone presentations.
    ///
    /// Changing this value while a dialog is visible immediately updates its dedicated theme root. Setting it to
    /// `null` restores automatic owner-theme or default-theme resolution.
    ///
    /// @param theme the explicit theme, or `null` for automatic resolution
    /// @throws RuntimeException if [#themeProperty()] is bound
    public void setTheme(@Nullable M3Theme theme) {
        themeValue.set(theme);
    }

    /// Returns the observable property that stores the explicit standalone-window theme.
    ///
    /// The property can be observed and bound. Its default value is `null`, which enables owner-theme or default-theme
    /// resolution. Changes made while a dialog is visible update the dedicated scene immediately.
    ///
    /// @return the non-null, nullable-valued theme property
    public ObjectProperty<@Nullable M3Theme> themeProperty() {
        return themeValue;
    }

    /// The native stage owned by this host.
    private final Stage stage;

    /// The stable root that supplies theme and motion context to each presented pane.
    private final StackPane root = new StackPane();

    /// The scene retained for the complete lifetime of this host.
    private final Scene scene;

    /// The presentation currently reserved for this host, including its showing callback phase.
    private @Nullable M3DialogPresentation activePresentation;

    /// Returns the native owner initialized for this dialog window.
    ///
    /// @return the owner window, or `null` for an ownerless dialog window
    public @Nullable Window getOwner() {
        return stage.getOwner();
    }

    /// Initializes the native owner of this dialog window.
    ///
    /// The owner may be `null` for a standalone top-level window. This method delegates the JavaFX one-time
    /// initialization contract and therefore must be called before the window has ever been shown.
    ///
    /// @param owner the owner window, or `null` for no owner
    /// @throws IllegalStateException if called after this window has been shown or while it is showing
    public void initOwner(@Nullable Window owner) {
        stage.initOwner(owner);
    }

    /// Returns the native modality initialized for this dialog window.
    ///
    /// @return the non-null modality
    public Modality getModality() {
        return stage.getModality();
    }

    /// Initializes the native modality of this dialog window.
    ///
    /// `WINDOW_MODAL` requires a non-null owner when [#showDialog(M3Dialog)] is called. This method follows the
    /// JavaFX one-time initialization contract and must be called before the window has ever been shown.
    ///
    /// @param modality the modality to initialize
    /// @throws IllegalStateException if called after this window has been shown or while it is showing
    /// @throws NullPointerException  if `modality` is `null`
    public void initModality(Modality modality) {
        stage.initModality(Objects.requireNonNull(modality, "modality"));
    }

    /// Returns the native stage style initialized for this dialog window.
    ///
    /// @return the non-null stage style
    public StageStyle getStyle() {
        return stage.getStyle();
    }

    /// Initializes the native style of this dialog window.
    ///
    /// This method follows the JavaFX one-time initialization contract and must be called before the window has
    /// ever been shown. Platform support and fallback behavior for conditional styles are defined by JavaFX.
    ///
    /// @param style the stage style to initialize
    /// @throws IllegalStateException if called after this window has been shown or while it is showing
    /// @throws NullPointerException  if `style` is `null`
    public void initStyle(StageStyle style) {
        stage.initStyle(Objects.requireNonNull(style, "style"));
    }

    /// Returns the mutable list of native window icons.
    ///
    /// Changes to the returned list update the Stage icons according to JavaFX platform behavior.
    ///
    /// @return the mutable, non-null icon list
    public ObservableList<Image> getIcons() {
        return stage.getIcons();
    }

    /// Presents a dialog in this dedicated native window.
    ///
    /// This method is non-blocking and returns after the Stage has been shown and the dialog's shown event has been
    /// dispatched. The host can present only one dialog at a time, and the same dialog cannot be presented by any
    /// other host concurrently. Retain the returned handle for programmatic dismissal and state observation.
    ///
    /// @param dialog the dialog to present
    /// @return the unique handle controlling this presentation
    /// @throws IllegalStateException if called off the JavaFX Application Thread, if this window already presents a
    ///                               dialog, if `WINDOW_MODAL` is configured without an owner, if the dialog is
    ///                               already presented, or if its pane already has a scene-graph parent
    /// @throws NullPointerException  if `dialog` is `null`
    public M3DialogHandle showDialog(M3Dialog dialog) {
        return Objects.requireNonNull(dialog, "dialog").present(this);
    }

    /// Creates a detached native-window presentation for one dialog pane.
    ///
    /// @param pane         the retained pane to install
    /// @param closeRequest the action invoked for native window-manager close requests
    /// @return the detached internal presentation
    M3DialogPresentation createPresentation(M3DialogPane pane, Runnable closeRequest) {
        if (activePresentation != null) {
            throw new IllegalStateException("dialog window is already presenting a dialog");
        }
        M3WindowDialogPresentation presentation = new M3WindowDialogPresentation(
                stage,
                scene,
                root,
                Objects.requireNonNull(pane, "pane"),
                this::preparePresentationContext,
                Objects.requireNonNull(closeRequest, "closeRequest"),
                this::releasePresentation
        );
        activePresentation = presentation;
        return presentation;
    }

    /// Releases the host reservation after its exact presentation has disposed.
    private void releasePresentation() {
        activePresentation = null;
    }

    /// Installs the effective theme and owner direction before a dialog enters this scene.
    private void preparePresentationContext() {
        installEffectiveTheme();
        @Nullable Window owner = stage.getOwner();
        if (owner != null && owner.getScene() != null) {
            root.setNodeOrientation(owner.getScene().getRoot().getEffectiveNodeOrientation());
        } else {
            root.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        }
    }

    /// Resolves and installs the theme currently controlling this dedicated window.
    private void installEffectiveTheme() {
        @Nullable M3Theme theme = getTheme();
        if (theme == null) {
            @Nullable Window owner = stage.getOwner();
            @Nullable Scene ownerScene = owner == null ? null : owner.getScene();
            theme = ownerScene == null ? null : M3ThemeResolver.findTheme(ownerScene);
        }
        M3ThemeManager.install(root, theme == null ? M3Theme.defaultTheme() : theme);
    }
}
