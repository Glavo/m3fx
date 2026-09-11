// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.internal.theme.M3ThemeMetadata;
import org.glavo.m3fx.internal.theme.M3ThemeRuntime;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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

    /// Retains source edits made by popup stylesheet listeners for the next synchronization pass.
    @ParameterizedTest
    @ValueSource(strings = {"append", "replace", "clear"})
    void sourceChangesDuringCopyAreNotCachedAsAlreadyApplied(String mutation) {
        FxTestUtils.runOnFxThread(() -> {
            Pane popup = new Pane();
            String first = M3Stylesheets.controlStylesheet("button.css");
            String second = M3Stylesheets.controlStylesheet("text-field.css");
            ObservableList<String> source = FXCollections.observableArrayList(first);
            AtomicBoolean changed = new AtomicBoolean();
            popup.getStylesheets().addListener((ListChangeListener<String>) change -> {
                if (changed.compareAndSet(false, true)) {
                    switch (mutation) {
                        case "append" -> source.add(second);
                        case "replace" -> source.setAll(second);
                        case "clear" -> source.clear();
                        default -> throw new AssertionError(mutation);
                    }
                }
            });

            M3PopupStyles.preparePopupRoot(popup, source, null, null);
            assertTrue(changed.get());
            M3PopupStyles.preparePopupRoot(popup, source, null, null);

            List<String> expected = new ArrayList<>();
            expected.add(M3Stylesheets.fallbackStylesheet());
            expected.addAll(source);
            assertEquals(expected, popup.getStylesheets());
        });
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
