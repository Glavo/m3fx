// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.Pane;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.internal.theme.M3ThemeMetadata;
import org.glavo.m3fx.internal.theme.M3ThemeRuntime;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies that popup theme synchronization owns generated theme state without owning inline styles.
@NotNullByDefault
final class M3PopupStylesTest {
    /// Starts JavaFX before theme synchronization tests.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Clears copied theme metadata and CSS while preserving a subsequently edited inline style.
    @Test
    void clearingCopiedThemePreservesApplicationStyleChanges() {
        FxTestUtils.runOnFxThread(() -> {
            Pane owner = new Pane();
            Pane popup = new Pane();
            M3Theme theme = M3Theme.defaultTheme();
            M3ThemeManager.install(owner, theme);
            popup.setStyle("-fx-padding: 4;");
            M3PopupStyles.preparePopupRoot(popup, List.of(), owner, null);
            assertSame(theme, M3ThemeMetadata.getTheme(popup));
            String themeStylesheet = M3ThemeRuntime.themeStylesheetUrl(theme);
            assertTrue(popup.getStylesheets().contains(themeStylesheet));

            popup.setStyle("-fx-padding: 12;");
            M3PopupStyles.prepareStandalonePopupRoot(popup, null);

            assertEquals("-fx-padding: 12;", popup.getStyle());
            assertNull(M3ThemeMetadata.getTheme(popup));
            assertFalse(popup.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
            assertFalse(popup.getStylesheets().contains(themeStylesheet));
            assertTrue(popup.getStylesheets().contains(M3Stylesheets.fallbackStylesheet()));
        });
    }

    /// Allows inline styles to remain bound and change between standalone popup synchronizations.
    @Test
    void standaloneSynchronizationPreservesBoundInlineStyle() {
        FxTestUtils.runOnFxThread(() -> {
            Pane popup = new Pane();
            StringProperty style = new SimpleStringProperty("-fx-padding: 4;");
            popup.styleProperty().bind(style);
            M3PopupStyles.prepareStandalonePopupRoot(popup, null);
            style.set("-fx-padding: 12;");
            M3PopupStyles.prepareStandalonePopupRoot(popup, null);
            assertTrue(popup.styleProperty().isBound());
            assertEquals(style.get(), popup.getStyle());
            style.set("-fx-padding: 16;");
            assertEquals(style.get(), popup.getStyle());
        });
    }
}
