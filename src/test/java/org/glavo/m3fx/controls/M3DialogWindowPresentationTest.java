// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.geometry.NodeOrientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Density;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies the fast lifecycle and host contracts of dialogs presented in dedicated native windows.
///
/// These tests exercise public standalone-window behavior without inspecting animation frames or platform-specific
/// window decoration. Native stages are retained only long enough to drive close and owner-lifecycle events.
@NotNullByDefault
final class M3DialogWindowPresentationTest {
    /// Starts the JavaFX toolkit before any test creates a native dialog window.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies the documented ownerless configuration and default theme of a standalone dialog window.
    @Test
    void ownerlessWindowUsesDocumentedDefaults() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DialogWindow window = new M3DialogWindow();

            assertNull(window.getOwner());
            assertEquals(Modality.NONE, window.getModality());
            assertEquals(StageStyle.TRANSPARENT, window.getStyle());
            assertFalse(window.isResizable());
            assertEquals("", window.getTitle());
            assertNull(window.getTheme());

            M3Dialog dialog = createDialog("Ownerless dialog");
            M3DialogHandle handle = window.showDialog(dialog);
            Stage stage = stageFor(dialog);
            try {
                Parent root = Objects.requireNonNull(stage.getScene(), "dialog scene").getRoot();

                assertTrue(stage.isShowing());
                assertEquals(Color.TRANSPARENT, stage.getScene().getFill());
                StackPane windowRoot = assertInstanceOf(StackPane.class, root);
                assertTrue(
                        windowRoot.getBackground().getFills().stream()
                                .allMatch(fill -> Color.TRANSPARENT.equals(fill.getFill())),
                        () -> "dialog window root must not paint an opaque rectangle: "
                                + windowRoot.getBackground().getFills().stream()
                                .map(BackgroundFill::getFill)
                                .toList()
                );
                assertSame(dialog, handle.getDialog());
                assertTrue(handle.isShowing());
                assertTrue(root.getStyleClass().contains(M3DialogWindow.STYLE_CLASS));
                M3Theme installedTheme = Objects.requireNonNull(
                        M3ThemeManager.getTheme(root),
                        "standalone theme"
                );
                assertEquals(M3Profile.BASELINE_2021, installedTheme.profile());
                assertEquals(Brightness.LIGHT, installedTheme.brightness());
                assertEquals(M3Density.standard(), installedTheme.density());
            } finally {
                handle.requestClose();
                hideIfShowing(stage);
            }
        });
    }

    /// Verifies lifecycle ordering, event handles, observable state, and programmatic dismissal.
    @Test
    void handleCloseRunsCompleteLifecycle() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DialogWindow window = new M3DialogWindow();
            M3Dialog dialog = createDialog("Lifecycle dialog");
            List<String> lifecycle = new ArrayList<>();
            List<M3DialogHandle> eventHandles = new ArrayList<>();
            dialog.setOnShowing(event -> {
                lifecycle.add("showing");
                eventHandles.add(event.getHandle());
            });
            dialog.setOnShown(event -> {
                lifecycle.add("shown");
                eventHandles.add(event.getHandle());
            });
            dialog.setOnCloseRequest(event -> {
                lifecycle.add("close-request");
                eventHandles.add(event.getHandle());
            });
            dialog.setOnHiding(event -> {
                lifecycle.add("hiding");
                eventHandles.add(event.getHandle());
            });
            dialog.setOnHidden(event -> {
                lifecycle.add("hidden");
                eventHandles.add(event.getHandle());
            });

            M3DialogHandle handle = window.showDialog(dialog);
            Stage stage = stageFor(dialog);
            try {
                assertEquals(List.of("showing", "shown"), lifecycle);
                assertTrue(handle.showingProperty().get());
                assertTrue(handle.requestClose());

                assertEquals(
                        List.of("showing", "shown", "close-request", "hiding", "hidden"),
                        lifecycle
                );
                assertTrue(eventHandles.stream().allMatch(eventHandle -> eventHandle == handle));
                assertFalse(handle.isShowing());
                assertFalse(handle.showingProperty().get());
                assertFalse(stage.isShowing());
                assertNull(dialog.getDialogPane().getParent());
                assertFalse(handle.requestClose());
            } finally {
                handle.requestClose();
                hideIfShowing(stage);
            }
        });
    }

    /// Verifies native close requests remain consumed when dialog closure is cancelled and close on acceptance.
    @Test
    void nativeCloseRequestUsesCancellableDialogLifecycle() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DialogWindow window = new M3DialogWindow();
            M3Dialog dialog = createDialog("Native close dialog");
            AtomicBoolean cancelNextRequest = new AtomicBoolean(true);
            AtomicInteger closeRequests = new AtomicInteger();
            dialog.setOnCloseRequest(event -> {
                closeRequests.incrementAndGet();
                if (cancelNextRequest.getAndSet(false)) {
                    event.consume();
                }
            });

            M3DialogHandle handle = window.showDialog(dialog);
            Stage stage = stageFor(dialog);
            try {
                WindowEvent cancelledRequest =
                        new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST);
                stage.fireEvent(cancelledRequest);

                assertTrue(cancelledRequest.isConsumed());
                assertEquals(1, closeRequests.get());
                assertTrue(handle.isShowing());
                assertTrue(stage.isShowing());

                WindowEvent acceptedRequest =
                        new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST);
                stage.fireEvent(acceptedRequest);

                assertTrue(acceptedRequest.isConsumed());
                assertEquals(2, closeRequests.get());
                assertFalse(handle.isShowing());
                assertFalse(stage.isShowing());
                assertNull(dialog.getDialogPane().getParent());
            } finally {
                handle.requestClose();
                hideIfShowing(stage);
            }
        });
    }

    /// Verifies that JavaFX's silent ownerless `WINDOW_MODAL` fallback is rejected by the Material host API.
    @Test
    void windowModalPresentationRequiresOwner() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DialogWindow window = new M3DialogWindow();
            window.initModality(Modality.WINDOW_MODAL);
            M3Dialog dialog = createDialog("Invalid modal dialog");

            assertThrows(IllegalStateException.class, () -> window.showDialog(dialog));
            assertNull(dialog.getDialogPane().getParent());
        });
    }

    /// Verifies an owned window snapshots the owner's theme and effective orientation for each presentation.
    @Test
    void ownedWindowInheritsThemeAndOrientation() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            StackPane ownerRoot = new StackPane();
            ownerRoot.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            Scene ownerScene = new Scene(ownerRoot, 420.0, 260.0);
            M3Theme ownerTheme = M3Theme.fromSeed(Color.web("#386a20"));
            M3Theme explicitTheme = M3Theme.fromSeed(Color.web("#006a6a"));
            M3ThemeManager.install(ownerScene, ownerTheme);
            Stage owner = new Stage();
            owner.setScene(ownerScene);
            owner.show();

            M3DialogWindow window = new M3DialogWindow();
            window.initOwner(owner);
            window.initModality(Modality.WINDOW_MODAL);
            M3Dialog dialog = createDialog("Owned dialog");
            M3DialogHandle handle = window.showDialog(dialog);
            Stage dialogStage = stageFor(dialog);
            try {
                Parent dialogRoot = Objects.requireNonNull(dialogStage.getScene(), "dialog scene").getRoot();

                assertSame(owner, window.getOwner());
                assertEquals(NodeOrientation.RIGHT_TO_LEFT, dialogRoot.getEffectiveNodeOrientation());
                assertEquals(NodeOrientation.RIGHT_TO_LEFT, dialog.getDialogPane().getEffectiveNodeOrientation());
                assertSame(ownerTheme, M3ThemeManager.getTheme(dialogRoot));

                window.setTheme(explicitTheme);

                assertSame(explicitTheme, window.getTheme());
                assertSame(explicitTheme, M3ThemeManager.getTheme(dialogRoot));

                window.setTheme(null);

                assertNull(window.getTheme());
                assertSame(ownerTheme, M3ThemeManager.getTheme(dialogRoot));
            } finally {
                handle.requestClose();
                hideIfShowing(dialogStage);
                owner.close();
                M3ThemeManager.uninstall(ownerScene);
            }
        });
    }

    /// Verifies one host can be reused and an earlier handle cannot affect a later presentation.
    @Test
    void hostReuseRejectsStaleHandle() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DialogWindow window = new M3DialogWindow();
            M3Dialog dialog = createDialog("Reusable dialog");

            M3DialogHandle firstHandle = window.showDialog(dialog);
            Stage firstStage = stageFor(dialog);
            assertTrue(firstHandle.requestClose());
            assertFalse(firstHandle.isShowing());

            M3DialogHandle secondHandle = window.showDialog(dialog);
            Stage secondStage = stageFor(dialog);
            try {
                assertNotSame(firstHandle, secondHandle);
                assertSame(firstStage, secondStage);
                assertTrue(secondHandle.isShowing());
                assertFalse(firstHandle.requestClose());
                assertTrue(secondHandle.isShowing());
                assertTrue(secondHandle.requestClose());
                assertFalse(secondHandle.isShowing());
            } finally {
                secondHandle.requestClose();
                hideIfShowing(secondStage);
            }
        });
    }

    /// Verifies the host reservation rejects recursive presentation before its native Stage becomes visible.
    @Test
    void showingCallbackCannotReenterSameWindowHost() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DialogWindow window = new M3DialogWindow();
            M3Dialog firstDialog = createDialog("First dialog");
            M3Dialog secondDialog = createDialog("Second dialog");
            AtomicBoolean rejected = new AtomicBoolean();
            firstDialog.setOnShowing(event -> {
                assertThrows(IllegalStateException.class, () -> window.showDialog(secondDialog));
                rejected.set(true);
            });

            M3DialogHandle handle = window.showDialog(firstDialog);
            Stage stage = stageFor(firstDialog);
            try {
                assertTrue(rejected.get());
                assertTrue(handle.isShowing());
                assertNull(secondDialog.getDialogPane().getParent());
            } finally {
                handle.requestClose();
                hideIfShowing(stage);
            }
        });
    }

    /// Verifies one dialog cannot be presented concurrently by native-window and in-scene hosts.
    @Test
    void sameDialogCannotSpanOverlayAndWindowHosts() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3OverlayPane overlay = new M3OverlayPane();
            overlay.setContent(new StackPane());
            Stage overlayStage = new Stage();
            overlayStage.setScene(new Scene(overlay, 420.0, 260.0));
            overlayStage.show();
            M3DialogWindow window = new M3DialogWindow();
            M3Dialog dialog = createDialog("Single presentation dialog");

            M3DialogHandle windowHandle = window.showDialog(dialog);
            Stage dialogStage = stageFor(dialog);
            try {
                assertThrows(IllegalStateException.class, () -> overlay.showDialog(dialog));
                assertTrue(windowHandle.requestClose());

                M3DialogHandle overlayHandle = overlay.showDialog(dialog);
                try {
                    assertThrows(IllegalStateException.class, () -> window.showDialog(dialog));
                } finally {
                    overlayHandle.requestClose();
                }
            } finally {
                windowHandle.requestClose();
                hideIfShowing(dialogStage);
                overlayStage.close();
            }
        });
    }

    /// Verifies externally hiding the native dialog window forces cleanup without a cancellable close request.
    @Test
    void hidingDialogWindowForcesPresentationCleanup() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            M3DialogWindow window = new M3DialogWindow();
            M3Dialog dialog = createDialog("Externally hidden dialog");
            AtomicInteger closeRequests = new AtomicInteger();
            AtomicInteger hidingEvents = new AtomicInteger();
            AtomicInteger hiddenEvents = new AtomicInteger();
            dialog.setOnCloseRequest(event -> closeRequests.incrementAndGet());
            dialog.setOnHiding(event -> hidingEvents.incrementAndGet());
            dialog.setOnHidden(event -> hiddenEvents.incrementAndGet());

            M3DialogHandle handle = window.showDialog(dialog);
            Stage stage = stageFor(dialog);
            stage.hide();

            assertEquals(0, closeRequests.get());
            assertEquals(1, hidingEvents.get());
            assertEquals(1, hiddenEvents.get());
            assertFalse(handle.isShowing());
            assertFalse(stage.isShowing());
            assertNull(dialog.getDialogPane().getParent());
            assertFalse(handle.requestClose());
        });
    }

    /// Verifies hiding an owner forces cleanup of its owned dialog window without a cancellable close request.
    @Test
    void hidingOwnerForcesOwnedWindowCleanup() {
        FxTestUtils.runOnFxThreadWithAnimationsDisabled(() -> {
            Stage owner = new Stage();
            owner.setScene(new Scene(new StackPane(), 420.0, 260.0));
            owner.show();
            M3DialogWindow window = new M3DialogWindow();
            window.initOwner(owner);
            M3Dialog dialog = createDialog("Owner lifecycle dialog");
            AtomicInteger closeRequests = new AtomicInteger();
            AtomicInteger hiddenEvents = new AtomicInteger();
            dialog.setOnCloseRequest(event -> closeRequests.incrementAndGet());
            dialog.setOnHidden(event -> hiddenEvents.incrementAndGet());

            M3DialogHandle handle = window.showDialog(dialog);
            Stage dialogStage = stageFor(dialog);
            owner.hide();

            assertEquals(0, closeRequests.get());
            assertEquals(1, hiddenEvents.get());
            assertFalse(handle.isShowing());
            assertFalse(dialogStage.isShowing());
            assertNull(dialog.getDialogPane().getParent());
            assertFalse(handle.requestClose());
            hideIfShowing(dialogStage);
        });
    }

    /// Creates a compact dialog suitable for lifecycle tests.
    ///
    /// @param title the dialog heading
    /// @return the configured dialog
    private static M3Dialog createDialog(String title) {
        M3Dialog dialog = new M3Dialog();
        dialog.getDialogPane().setHeaderText(title);
        dialog.getDialogPane().setContentText("Standalone dialog content");
        return dialog;
    }

    /// Returns the native Stage currently containing a presented dialog pane.
    ///
    /// @param dialog the presented dialog
    /// @return the Stage containing the dialog
    private static Stage stageFor(M3Dialog dialog) {
        Scene scene = Objects.requireNonNull(dialog.getDialogPane().getScene(), "presented dialog scene");
        return assertInstanceOf(Stage.class, scene.getWindow());
    }

    /// Hides a Stage that remains visible after an assertion or setup failure.
    ///
    /// @param stage the Stage to clean up
    private static void hideIfShowing(Stage stage) {
        if (stage.isShowing()) {
            stage.hide();
        }
    }
}
