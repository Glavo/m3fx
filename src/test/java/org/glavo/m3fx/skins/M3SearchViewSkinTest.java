// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.controls.M3SearchView;
import org.glavo.m3fx.controls.M3SearchViewLayout;
import org.glavo.m3fx.controls.M3SearchViewStyle;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/// Tests rounded content clipping in the default search-view skin.
@NotNullByDefault
final class M3SearchViewSkinTest {
    /// Starts the JavaFX toolkit before tests create controls and scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies that contained results are clipped without clipping the control's outer shadow.
    @Test
    void containedResultsUseRetainedRoundedClipAcrossWindowLayouts() {
        FxTestUtils.runOnFxThread(() -> {
            M3SearchView searchView = new M3SearchView("Search");
            searchView.setPrefSize(360.0, 320.0);
            Region result = new Region();
            result.setMinHeight(56.0);
            searchView.getResults().add(result);
            StackPane root = new StackPane(searchView);
            Scene scene = new Scene(root, 420.0, 380.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());

            root.applyCss();
            root.layout();
            searchView.layout();

            VBox content = assertInstanceOf(VBox.class, searchView.lookup(".m3-search-view-content"));
            VBox results = assertInstanceOf(VBox.class, searchView.lookup(".m3-search-view-results"));
            Rectangle clip = assertInstanceOf(Rectangle.class, results.getClip());
            assertEquals(results.getWidth(), clip.getWidth(), 0.0001);
            assertEquals(results.getHeight(), clip.getHeight(), 0.0001);
            assertEquals(24.0, clip.getArcWidth(), 0.0001);
            assertEquals(24.0, clip.getArcHeight(), 0.0001);
            assertNull(content.getClip());
            assertNull(searchView.getClip());

            results.setStyle("-fx-background-radius: 7px;");
            root.applyCss();
            root.layout();
            searchView.layout();
            assertSame(clip, results.getClip());
            assertEquals(14.0, clip.getArcWidth(), 0.0001);
            assertEquals(14.0, clip.getArcHeight(), 0.0001);

            searchView.setViewLayout(M3SearchViewLayout.FULL_SCREEN);
            root.applyCss();
            root.layout();
            searchView.layout();
            assertSame(clip, results.getClip());

            searchView.setViewStyle(M3SearchViewStyle.DIVIDED);
            assertNull(results.getClip());

            searchView.setViewStyle(M3SearchViewStyle.CONTAINED);
            assertSame(clip, results.getClip());
        });
    }
}
