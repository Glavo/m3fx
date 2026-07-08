// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/// Verifies text-input container slot navigation behavior.
@NotNullByDefault
final class M3InputSlotNavigationTest {
    /// Starts JavaFX before constructing controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies search bars consume owner-level slot navigation while the embedded editor owns focus.
    @Test
    void searchBarConsumesOwnerNavigationWhenEditorOwnsFocus() {
        FxTestUtils.runOnFxThread(() -> {
            M3SearchBar searchBar = new M3SearchBar("Search");
            M3Button action = new M3Button("Action");
            searchBar.getTrailingActions().add(action);
            StackPane root = new StackPane(searchBar);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 320.0, 96.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                boolean[] bubbled = {false};
                root.addEventHandler(KeyEvent.KEY_PRESSED, event -> bubbled[0] = true);

                searchBarEditor(searchBar).requestFocus();
                KeyEvent event = targetedKeyEvent(KeyCode.RIGHT, searchBar);
                searchBar.fireEvent(event);

                assertTrue(searchBarEditor(searchBar).isFocused(), () -> "focused=" + scene.getFocusOwner());
                assertFalse(action.isFocused());
                assertFalse(bubbled[0]);
            } finally {
                stage.close();
            }
        });
    }

    /// Verifies text input layouts consume owner-level slot navigation while the wrapped input owns focus.
    @Test
    void textInputLayoutConsumesOwnerNavigationWhenInputOwnsFocus() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField input = new M3TextField("Edit");
            M3Button trailing = new M3Button("Action");
            M3TextInputLayout layout = new M3TextInputLayout(input, "Label");
            layout.setTrailing(trailing);
            StackPane root = new StackPane(layout);
            Stage stage = new Stage();
            try {
                Scene scene = new Scene(root, 320.0, 128.0);
                M3ThemeManager.install(scene, M3Theme.defaultTheme());
                stage.setScene(scene);
                stage.show();
                root.applyCss();
                root.layout();

                boolean[] bubbled = {false};
                root.addEventHandler(KeyEvent.KEY_PRESSED, event -> bubbled[0] = true);

                input.requestFocus();
                KeyEvent event = targetedKeyEvent(KeyCode.RIGHT, layout);
                layout.fireEvent(event);

                assertTrue(input.isFocused(), () -> "focused=" + scene.getFocusOwner());
                assertFalse(trailing.isFocused());
                assertFalse(bubbled[0]);
            } finally {
                stage.close();
            }
        });
    }

    /// Returns the embedded editor exposed for accessibility by a search bar.
    private static TextField searchBarEditor(M3SearchBar searchBar) {
        return assertInstanceOf(TextField.class, searchBar.queryAccessibleAttribute(AccessibleAttribute.CONTENTS));
    }

    /// Creates a pressed key event with an explicit source and target node.
    private static KeyEvent targetedKeyEvent(KeyCode code, Node target) {
        return new KeyEvent(
                target,
                target,
                KeyEvent.KEY_PRESSED,
                code.getName(),
                code.getName(),
                code,
                false,
                false,
                false,
                false
        );
    }
}