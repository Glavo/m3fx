// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.controls.M3AssistChip;
import org.glavo.m3fx.controls.M3ChipGroup;
import org.glavo.m3fx.controls.M3FormPane;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/// Verifies that container skins measure the same available area they assign during layout.
@NotNullByDefault
final class M3ContainerSizingTest {
    /// Starts JavaFX before constructing controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Removes opposite-axis insets while preserving unconstrained queries and exhausted content space.
    @ParameterizedTest
    @CsvSource({
            "minWidth,100,104", "prefWidth,100,104", "maxWidth,100,104",
            "minHeight,100,96", "prefHeight,100,96", "maxHeight,100,96",
            "minWidth,5,20", "prefWidth,5,20", "maxWidth,5,20",
            "minHeight,5,16", "prefHeight,5,16", "maxHeight,5,16",
            "minWidth,-1,37", "prefWidth,-1,37", "maxWidth,-1,37",
            "minHeight,-1,33", "prefHeight,-1,33", "maxHeight,-1,33"
    })
    void sharedSkinPassesContentConstraints(String metric, double constraint, double expected) {
        FxTestUtils.runOnFxThread(() -> {
            TestControl control = new TestControl();
            double actual = switch (metric) {
                case "minWidth" -> control.minWidth(constraint);
                case "prefWidth" -> control.prefWidth(constraint);
                case "maxWidth" -> control.maxWidth(constraint);
                case "minHeight" -> control.minHeight(constraint);
                case "prefHeight" -> control.prefHeight(constraint);
                case "maxHeight" -> control.maxHeight(constraint);
                default -> throw new AssertionError(metric);
            };
            assertEquals(expected, actual);
            control.setSkin(null);
        });
    }

    /// Includes every wrapped chip row when external padding reduces the available width.
    @Test
    void chipGroupMeasuresWrappedHeightInsidePadding() {
        FxTestUtils.runOnFxThread(() -> {
            M3ChipGroup group = chipGroup();
            group.setPadding(new Insets(10, 20, 10, 20));
            assertEquals(60, group.prefHeight(130));
        });
    }

    /// Lets a parent allocate the height needed by the chip group's actual width.
    @Test
    void parentUsesChipGroupHorizontalBias() {
        FxTestUtils.runOnFxThread(() -> {
            M3ChipGroup group = chipGroup();
            VBox root = new VBox(group);
            new Scene(root, 90, 200);
            root.applyCss();
            root.resize(90, 200);
            root.layout();

            assertEquals(Orientation.HORIZONTAL, group.getContentBias());
            assertEquals(40, group.getHeight());
        });
    }

    /// Reallocates wrapped height after child metrics or bound container gaps change.
    @ParameterizedTest
    @ValueSource(strings = {"chipWidth", "horizontalGap", "verticalGap"})
    void parentRelayoutsChipGroupAfterMetricsChange(String mutation) {
        FxTestUtils.runOnFxThread(() -> {
            M3ChipGroup group = chipGroup();
            double width = mutation.equals("verticalGap") ? 90 : 130;
            VBox root = new VBox(group);
            new Scene(root, width, 200);
            root.applyCss();
            root.resize(width, 200);
            root.layout();
            assertEquals(mutation.equals("verticalGap") ? 40 : 20, group.getHeight());

            switch (mutation) {
                case "chipWidth" -> {
                    group.getItems().get(0).setMinWidth(100);
                    group.getItems().get(0).setPrefWidth(100);
                    group.getItems().get(0).setMaxWidth(100);
                }
                case "horizontalGap" -> group.setHorizontalGap(20);
                case "verticalGap" -> group.setVerticalGap(10);
                default -> throw new AssertionError(mutation);
            }
            root.layout();

            assertEquals(mutation.equals("verticalGap") ? 50 : 40, group.getHeight());
            group.setSkin(null);
        });
    }

    /// Reallocates form height when an existing row changes size or leaves managed layout.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void parentRelayoutsFormAfterRowChange(boolean unmanaged) {
        FxTestUtils.runOnFxThread(() -> {
            Region row = new Region();
            row.setMinHeight(Region.USE_PREF_SIZE);
            row.setPrefHeight(20);
            row.setMaxHeight(Region.USE_PREF_SIZE);
            M3FormPane form = new M3FormPane();
            form.setPadding(Insets.EMPTY);
            form.setContentPadding(0);
            form.getItems().add(row);
            VBox root = new VBox(form);
            new Scene(root, 200, 200);
            root.applyCss();
            root.resize(200, 200);
            root.layout();
            assertEquals(20, form.getHeight());

            if (unmanaged) {
                row.setManaged(false);
            } else {
                row.setPrefHeight(45);
            }
            root.layout();

            assertEquals(unmanaged ? 0 : 45, form.getHeight(), 0.0);

            row.setManaged(true);
            row.setPrefHeight(25);
            root.layout();
            assertEquals(25, form.getHeight());
            form.setSkin(null);
        });
    }

    /// Removes both external and token padding before measuring width-dependent form rows.
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void formMeasuresRowsInsideBothPaddingLayers(boolean minimum) {
        FxTestUtils.runOnFxThread(() -> {
            M3FormPane form = new M3FormPane();
            form.getItems().add(new ResponsiveRow());
            form.setPadding(new Insets(5, 7, 11, 13));
            form.setContentPadding(20);
            form.setSkin(new M3FormPaneSkin(form));

            assertEquals(70, minimum ? form.minHeight(200) : form.prefHeight(200));
            form.setSkin(null);
        });
    }

    /// Creates two chips that fit in one unconstrained row but wrap below 120 pixels.
    private static M3ChipGroup chipGroup() {
        M3ChipGroup group = new M3ChipGroup();
        group.setHorizontalGap(0);
        group.setVerticalGap(0);
        for (int index = 0; index < 2; index++) {
            M3AssistChip chip = new M3AssistChip();
            chip.setMinSize(60, 20);
            chip.setPrefSize(60, 20);
            chip.setMaxSize(60, 20);
            group.getItems().add(chip);
        }
        group.setSkin(new M3ChipGroupSkin(group));
        return group;
    }

    /// Provides a control with known external insets and a constraint-sensitive skin container.
    @NotNullByDefault
    private static final class TestControl extends Control {
        /// Installs the shared container skin with asymmetric padding.
        private TestControl() {
            setPadding(new Insets(5, 7, 11, 13));
            setSkin(new TestSkin(this));
        }
    }

    /// Uses the production item-container measurement and layout implementation.
    @NotNullByDefault
    private static final class TestSkin extends M3ItemContainerSkinBase<TestControl, ConstraintPane, Node> {
        /// Creates a container skin without item membership effects.
        private TestSkin(TestControl control) {
            super(control, FXCollections.observableArrayList(), new ConstraintPane());
        }
    }

    /// Exposes the opposite-axis constraint in every computed size.
    @NotNullByDefault
    private static final class ConstraintPane extends Pane {
        /// Creates a container without authored sizing properties.
        private ConstraintPane() {
        }

        /// Returns a distinct natural size for unconstrained queries.
        private double measuredSize(double constraint) {
            return constraint < 0 ? 17 : constraint;
        }

        /// Measures minimum width from available height.
        @Override
        protected double computeMinWidth(double height) {
            return measuredSize(height);
        }

        /// Measures preferred width from available height.
        @Override
        protected double computePrefWidth(double height) {
            return measuredSize(height);
        }

        /// Measures maximum width from available height.
        @Override
        protected double computeMaxWidth(double height) {
            return measuredSize(height);
        }

        /// Measures minimum height from available width.
        @Override
        protected double computeMinHeight(double width) {
            return measuredSize(width);
        }

        /// Measures preferred height from available width.
        @Override
        protected double computePrefHeight(double width) {
            return measuredSize(width);
        }

        /// Measures maximum height from available width.
        @Override
        protected double computeMaxHeight(double width) {
            return measuredSize(width);
        }
    }

    /// Computes row height from its allocated width, as responsive form rows do.
    @NotNullByDefault
    private static final class ResponsiveRow extends Region {
        /// Creates a row allowed to fill the form's available width.
        private ResponsiveRow() {
            setMinWidth(0);
            setPrefWidth(100);
        }

        /// Requires width to be resolved before height.
        @Override
        public Orientation getContentBias() {
            return Orientation.HORIZONTAL;
        }

        /// Returns the height needed at the supplied width.
        @Override
        protected double computeMinHeight(double width) {
            return (width < 0 ? 100 : width) / 10;
        }

        /// Uses the same width-dependent preferred height.
        @Override
        protected double computePrefHeight(double width) {
            return computeMinHeight(width);
        }
    }
}
