// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.controls.M3Button;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/// Tests retained preset actions and local disabled state independently of their host presentation.
@NotNullByDefault
final class M3PickerPresetControllerTest {
    /// Starts JavaFX before creating controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Preserves button identity through permutations and removes actions from discarded buttons.
    @Test
    void permutationsRetainButtonsAndActionsFollowCurrentIndices() {
        FxTestUtils.runOnFxThread(() -> {
            ObservableList<String> presets = FXCollections.observableArrayList("B", "A");
            VBox column = new VBox();
            List<String> applied = new ArrayList<>();
            M3PickerPresetController<String> controller = new M3PickerPresetController<>(
                    presets, column, "m3-test-preset", text -> text, text -> false,
                    (preset, event) -> applied.add(preset)
            );
            controller.install();
            controller.install();
            M3Button first = (M3Button) column.getChildren().get(0);
            M3Button second = (M3Button) column.getChildren().get(1);

            FXCollections.sort(presets);
            assertEquals(2, column.getChildren().size());
            assertSame(second, column.getChildren().get(0));
            assertSame(first, column.getChildren().get(1));
            first.fire();
            second.fire();
            assertEquals(List.of("B", "A"), applied);

            presets.remove("B");
            assertNull(first.getOnAction());
            first.fire();
            assertEquals(List.of("B", "A"), applied);
            assertSame(second, column.getChildren().get(0));
            presets.clear();
            assertFalse(column.isManaged());
            assertFalse(column.isVisible());
            presets.add("C");
            assertTrue(column.isManaged());
            assertTrue(column.isVisible());
        });
    }

    /// Updates the local disabled flag even while a disabled ancestor masks that flag.
    @Test
    void inheritedDisableDoesNotMaskPresetBoundsChanges() {
        FxTestUtils.runOnFxThread(() -> {
            ObservableList<Integer> presets = FXCollections.observableArrayList(1, 2);
            VBox column = new VBox();
            List<Integer> disabledPresets = new ArrayList<>();
            M3PickerPresetController<Integer> controller = new M3PickerPresetController<>(
                    presets, column, "m3-test-preset", Object::toString, disabledPresets::contains,
                    (preset, event) -> fail("Disabled preset must not run")
            );
            controller.install();
            M3Button first = (M3Button) column.getChildren().get(0);
            M3Button second = (M3Button) column.getChildren().get(1);
            column.setDisable(true);
            disabledPresets.add(1);
            controller.refreshDisabledStates();
            column.setDisable(false);
            assertTrue(first.isDisabled());
            assertFalse(second.isDisabled());
            first.fire();

            column.setDisable(true);
            disabledPresets.clear();
            controller.refreshDisabledStates();
            column.setDisable(false);
            assertFalse(first.isDisabled());
            assertSame(first, column.getChildren().get(0));
        });
    }
}
