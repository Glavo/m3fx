// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.glavo.m3fx.tokens.M3Density;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies expandable setting rows reserve vertical space for nested content.
@NotNullByDefault
class M3ExpandableSettingItemLayoutTest {
    /// Starts the JavaFX toolkit before layout measurements run.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Expanded rows must grow beyond the header height so sibling content is not overpainted.
    @Test
    void expandedContentIncreasesPreferredAndLaidOutHeight() {
        FxTestUtils.runOnFxThread(() -> {
            M3ExpandableSettingItem item = new M3ExpandableSettingItem("Memory");
            item.setSupportingText("Auto");
            M3SwitchSettingItem first = new M3SwitchSettingItem("Automatic allocation");
            M3SwitchSettingItem second = new M3SwitchSettingItem("Manual allocation");
            VBox body = new VBox(8.0, first, second, new Label("status line"));
            item.setContent(body);

            M3ListPane list = new M3ListPane();
            list.setListStyle(M3ListStyle.STANDARD);
            list.setSelectionMode(M3SelectionMode.NONE);
            list.getItems().setAll(item);

            VBox root = new VBox(16.0, list, new Label("after"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 480, 720));
            stage.show();
            root.applyCss();
            root.layout();

            double collapsedPref = item.prefHeight(400.0);
            item.setExpanded(true);
            root.applyCss();
            root.layout();

            double expandedPref = item.prefHeight(400.0);
            double bodyPref = body.prefHeight(400.0);
            double listPref = list.prefHeight(400.0);

            assertTrue(bodyPref > 40.0, "nested body must report a usable preferred height, was " + bodyPref);
            assertTrue(
                    expandedPref > collapsedPref + bodyPref * 0.5,
                    "expanded pref height must include content: collapsed="
                            + collapsedPref
                            + " expanded="
                            + expandedPref
                            + " body="
                            + bodyPref
            );
            assertTrue(
                    listPref >= expandedPref - 1.0,
                    "list pane must host the expanded row height: list=" + listPref + " item=" + expandedPref
            );
            assertTrue(
                    item.getHeight() >= expandedPref - 2.0,
                    "laid-out height must match preferred height: height="
                            + item.getHeight()
                            + " pref="
                            + expandedPref
            );

            stage.close();
        });
    }

    /// Nested list panes used by HMCL game-settings groups must still reserve expanded height.
    @Test
    void nestedListPaneContentReservesExpandedHeightLikeDemoForm() {
        FxTestUtils.runOnFxThread(() -> {
            M3ExpandableSettingItem memory = new M3ExpandableSettingItem("Memory");
            memory.setSupportingText("Automatic allocation");

            M3RadioButtonSettingItem auto = new M3RadioButtonSettingItem("Automatic");
            M3RadioButtonSettingItem manual = new M3RadioButtonSettingItem("Manual");
            M3SelectSettingItem<Integer> min = new M3SelectSettingItem<>("Minimum");
            min.getItems().setAll(256, 512, 1024);

            M3ListPane nested = new M3ListPane();
            nested.setListStyle(M3ListStyle.STANDARD);
            nested.setSelectionMode(M3SelectionMode.NONE);
            nested.getItems().setAll(auto, manual, min);
            nested.setMinHeight(0.0);
            VBox body = new VBox(8.0, nested);
            body.setMinHeight(0.0);
            memory.setContent(body);
            memory.setExpanded(true);

            M3ListPane host = new M3ListPane();
            host.setListStyle(M3ListStyle.STANDARD);
            host.setSelectionMode(M3SelectionMode.NONE);
            host.getStyleClass().addAll("hmcl-settings-list", "hmcl-settings-group");
            host.getItems().setAll(memory);

            M3ExpandableSettingItem window = new M3ExpandableSettingItem("Window");
            window.setSupportingText("854x480");
            window.setExpanded(true);
            M3ListPane windowNested = new M3ListPane();
            windowNested.setListStyle(M3ListStyle.STANDARD);
            windowNested.setSelectionMode(M3SelectionMode.NONE);
            windowNested.getItems().setAll(
                    new M3RadioButtonSettingItem("Windowed"),
                    new M3RadioButtonSettingItem("Fullscreen")
            );
            window.setContent(windowNested);

            M3ListPane windowHost = new M3ListPane();
            windowHost.setListStyle(M3ListStyle.STANDARD);
            windowHost.setSelectionMode(M3SelectionMode.NONE);
            windowHost.getItems().setAll(window);

            VBox root = new VBox(16.0, host, windowHost, new Label("Launcher section"));
            root.setMinHeight(0.0);
            VBox column = new VBox(20.0, root);
            column.setPadding(new javafx.geometry.Insets(20.0, 24.0, 28.0, 24.0));
            column.setMinSize(0.0, 0.0);
            column.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            ScrollPane scroll = new ScrollPane(column);
            scroll.setFitToWidth(true);
            scroll.setMinSize(0.0, 0.0);
            scroll.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            Stage stage = new Stage();
            Scene scene = new Scene(scroll, 520, 400);
            M3ThemeManager.install(
                    scene,
                    M3Theme.fromSeed(Color.web("#5C6BC0"), M3Profile.BASELINE_2021, Brightness.LIGHT, M3Density.standard())
            );
            stage.setScene(scene);
            stage.show();
            scroll.applyCss();
            scroll.layout();
            column.applyCss();
            column.layout();

            double memoryPref = memory.prefHeight(440.0);
            double nestedPref = body.prefHeight(440.0);
            double hostPref = host.prefHeight(440.0);
            double windowPref = window.prefHeight(440.0);

            assertTrue(nestedPref > 100.0, "nested body pref was " + nestedPref);
            assertTrue(memoryPref > nestedPref, "memory pref=" + memoryPref + " nested=" + nestedPref);
            assertTrue(hostPref >= memoryPref - 1.0, "host pref=" + hostPref + " memory=" + memoryPref);
            assertTrue(memory.getHeight() >= memoryPref - 2.0,
                    "memory laid-out height=" + memory.getHeight() + " pref=" + memoryPref);
            assertTrue(window.getHeight() >= windowPref - 2.0,
                    "window laid-out height=" + window.getHeight() + " pref=" + windowPref);
            assertTrue(
                    windowHost.getLayoutY() >= host.getLayoutY() + host.getHeight() - 1.0,
                    "window host must start below expanded memory host: windowY="
                            + windowHost.getLayoutY()
                            + " memoryBottom="
                            + (host.getLayoutY() + host.getHeight())
            );

            stage.close();
        });
    }
}
