// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3NavigationDrawerGroup;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Verifies drawer-group size constraints and indentation before any layout pass.
@NotNullByDefault
final class M3NavigationDrawerGroupSizingTest {
    /// Starts JavaFX before constructing controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Passes the header its content-area constraint without changing negative unconstrained queries.
    @ParameterizedTest
    @CsvSource({
            "minWidth,100,104", "prefWidth,100,104", "maxWidth,100,104",
            "minHeight,100,96", "prefHeight,100,96", "maxHeight,100,96",
            "minWidth,5,20", "prefWidth,5,20", "maxWidth,5,20",
            "minHeight,5,16", "prefHeight,5,16", "maxHeight,5,16",
            "minWidth,-1,37", "prefWidth,-1,37", "maxWidth,-1,37",
            "minHeight,-1,33", "prefHeight,-1,33", "maxHeight,-1,33"
    })
    void collapsedHeaderUsesContentConstraints(String metric, double constraint, double expected) {
        FxTestUtils.runOnFxThread(() -> {
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup();
            group.setPadding(new Insets(5, 7, 11, 13));
            M3ListItem header = group.getHeaderItem();
            header.setPadding(Insets.EMPTY);
            header.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            header.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            header.setSkin(new ConstraintSkin(header));
            group.setSkin(new M3NavigationDrawerGroupSkin(group));
            try {
                double actual = switch (metric) {
                    case "minWidth" -> group.minWidth(constraint);
                    case "prefWidth" -> group.prefWidth(constraint);
                    case "maxWidth" -> group.maxWidth(constraint);
                    case "minHeight" -> group.minHeight(constraint);
                    case "prefHeight" -> group.prefHeight(constraint);
                    case "maxHeight" -> group.maxHeight(constraint);
                    default -> throw new AssertionError(metric);
                };
                assertEquals(expected, actual);
            } finally {
                group.setSkin(null);
                header.setSkin(null);
            }
        });
    }

    /// Includes child indentation in the first measurement and retains it after layout.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void expandedWidthIncludesIndentationBeforeLayout(boolean minimum) {
        FxTestUtils.runOnFxThread(() -> {
            M3NavigationDrawerGroup group = new M3NavigationDrawerGroup();
            group.setPadding(new Insets(5, 7, 11, 13));
            M3ListItem header = group.getHeaderItem();
            header.setMinSize(50, 40);
            header.setPrefSize(50, 40);
            header.setMaxSize(50, 40);
            header.setHorizontalPadding(10);
            M3ListItem child = new M3ListItem();
            child.setMinSize(80, 30);
            child.setPrefSize(100, 30);
            child.setMaxSize(100, 30);
            child.setHorizontalPadding(30);
            group.getItems().add(child);
            group.setExpanded(true);
            group.setSkin(new M3NavigationDrawerGroupSkin(group));
            try {
                double expected = minimum ? 120 : 140;
                assertEquals(expected, minimum ? group.minWidth(-1) : group.prefWidth(-1));
                group.resize(200, 100);
                group.layout();
                assertEquals(expected, minimum ? group.minWidth(-1) : group.prefWidth(-1));
            } finally {
                group.setSkin(null);
            }
        });
    }

    /// Propagates mounted-row measurement changes through the clipped viewport to the outer layout.
    @ParameterizedTest
    @ValueSource(strings = {"height", "managed", "width", "indentation"})
    void parentRemeasuresExpandedRows(String mutation) {
        FxTestUtils.runOnFxThread(() -> {
            M3NavigationDrawerGroup group = expandedGroup();
            M3ListItem child = group.getItems().get(0);
            VBox root = new VBox(group);
            new Scene(root, 200, 200);
            root.applyCss();
            root.resize(200, 200);
            root.layout();
            assertEquals(70, group.getHeight());
            assertEquals(120, root.prefWidth(-1));

            switch (mutation) {
                case "height" -> child.setPrefHeight(55);
                case "managed" -> child.setManaged(false);
                case "width" -> child.setPrefWidth(160);
                case "indentation" -> child.setHorizontalPadding(40);
                default -> throw new AssertionError(mutation);
            }
            root.layout();

            switch (mutation) {
                case "height" -> assertEquals(95, group.getHeight());
                case "managed" -> assertEquals(40, group.getHeight());
                case "width" -> assertEquals(180, root.prefWidth(-1));
                case "indentation" -> assertEquals(140, root.prefWidth(-1));
                default -> throw new AssertionError(mutation);
            }
            group.setSkin(null);
        });
    }

    /// Keeps rows fitted to the assigned viewport width when it differs from their preferred width.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void viewportRetainsAssignedWidth(boolean rightToLeft) {
        FxTestUtils.runOnFxThread(() -> {
            M3NavigationDrawerGroup group = expandedGroup();
            group.setNodeOrientation(rightToLeft ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);
            VBox root = new VBox(group);
            new Scene(root, 200, 200);
            root.applyCss();
            root.resize(200, 200);
            root.layout();

            M3ListItem child = group.getItems().get(0);
            assertEquals(200, group.getWidth());
            assertEquals(180, child.getWidth());
            assertEquals(30, child.getHeight());
            group.setSkin(null);
        });
    }

    /// Creates an expanded group with a fixed-height header and one independently measurable row.
    private static M3NavigationDrawerGroup expandedGroup() {
        M3NavigationDrawerGroup group = new M3NavigationDrawerGroup();
        group.setPadding(Insets.EMPTY);
        group.setMaxWidth(Double.MAX_VALUE);
        M3ListItem header = group.getHeaderItem();
        header.setMinSize(100, 40);
        header.setPrefSize(100, 40);
        header.setMaxSize(Double.MAX_VALUE, 40);
        header.setHorizontalPadding(0);
        M3ListItem child = new M3ListItem();
        child.setMinSize(0, Region.USE_PREF_SIZE);
        child.setPrefSize(100, 30);
        child.setMaxSize(Double.MAX_VALUE, Region.USE_PREF_SIZE);
        child.setHorizontalPadding(20);
        group.getItems().add(child);
        group.setExpanded(true);
        group.setSkin(new M3NavigationDrawerGroupSkin(group));
        return group;
    }

    /// Makes the header's preferred dimensions depend on their supplied opposite-axis constraint.
    @NotNullByDefault
    private static final class ConstraintSkin extends SkinBase<M3ListItem> {
        /// Installs a header skin without child geometry or additional insets.
        private ConstraintSkin(M3ListItem control) {
            super(control);
        }

        /// Returns available height or the header's natural unconstrained size.
        @Override
        protected double computePrefWidth(double height, double topInset, double rightInset,
                                          double bottomInset, double leftInset) {
            return height < 0 ? 17 : height;
        }

        /// Returns available width or the header's natural unconstrained size.
        @Override
        protected double computePrefHeight(double width, double topInset, double rightInset,
                                           double bottomInset, double leftInset) {
            return width < 0 ? 17 : width;
        }
    }
}
