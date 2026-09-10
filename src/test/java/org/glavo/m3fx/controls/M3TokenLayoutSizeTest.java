// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Control;
import javafx.scene.layout.Region;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Verifies that component size tokens leave application layout constraints intact.
@NotNullByDefault
final class M3TokenLayoutSizeTest {
    /// Starts JavaFX before constructing controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Preserves independently assigned minimum, preferred, and maximum dimensions.
    @ParameterizedTest
    @ValueSource(strings = {"loading", "progress", "navigation"})
    void tokenChangesPreserveExplicitConstraints(String kind) {
        FxTestUtils.runOnFxThread(() -> {
            SizeCase fixture = createCase(kind);
            Control control = fixture.control();
            control.setMinSize(11.0, 12.0);
            control.setPrefSize(61.0, 62.0);
            control.setMaxSize(91.0, 92.0);
            fixture.changeSize().run();
            assertEquals(11.0, control.minWidth(-1));
            assertEquals(12.0, control.minHeight(-1));
            assertEquals(61.0, control.prefWidth(-1));
            assertEquals(62.0, control.prefHeight(-1));
            assertEquals(91.0, control.maxWidth(-1));
            assertEquals(92.0, control.maxHeight(-1));

            control.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            control.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            assertComputedDimensions(fixture);
            if (control instanceof M3NavigationItem) {
                control.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                assertEquals(fixture.width(), control.maxWidth(-1));
                assertEquals(fixture.height(), control.maxHeight(-1));
            }
        });
    }

    /// Updates tokens without writing bound layout properties or breaking their bindings.
    @ParameterizedTest
    @ValueSource(strings = {"loading", "progress", "navigation"})
    void tokenChangesPreserveBoundConstraints(String kind) {
        FxTestUtils.runOnFxThread(() -> {
            SizeCase fixture = createCase(kind);
            Control control = fixture.control();
            SimpleDoubleProperty dimension = new SimpleDoubleProperty(90.0);
            control.minWidthProperty().bind(dimension);
            control.minHeightProperty().bind(dimension);
            control.prefWidthProperty().bind(dimension);
            control.prefHeightProperty().bind(dimension);
            control.maxWidthProperty().bind(dimension);
            control.maxHeightProperty().bind(dimension);
            fixture.changeSize().run();
            dimension.set(100.0);
            assertEquals(100.0, control.minWidth(-1));
            assertEquals(100.0, control.minHeight(-1));
            assertEquals(100.0, control.prefWidth(-1));
            assertEquals(100.0, control.prefHeight(-1));
            assertEquals(100.0, control.maxWidth(-1));
            assertEquals(100.0, control.maxHeight(-1));
        });
    }

    /// Invalidates cached computed dimensions even before a skin is installed.
    @ParameterizedTest
    @ValueSource(strings = {"loading", "progress", "navigation"})
    void tokenChangesUpdateComputedDimensions(String kind) {
        FxTestUtils.runOnFxThread(() -> {
            SizeCase fixture = createCase(kind);
            Control control = fixture.control();
            control.minWidth(-1);
            control.minHeight(-1);
            control.prefWidth(-1);
            control.prefHeight(-1);
            fixture.changeSize().run();
            assertComputedDimensions(fixture);
            if (control instanceof M3ProgressIndicator progress) {
                progress.setWaveAmplitude(0.0);
                assertEquals(54.0, progress.prefWidth(-1));
                assertEquals(54.0, progress.prefHeight(-1));
            }
        });
    }

    /// Creates a control and a representative sequence of layout-affecting token changes.
    private static SizeCase createCase(String kind) {
        return switch (kind) {
            case "loading" -> {
                M3LoadingIndicator control = new M3LoadingIndicator();
                yield new SizeCase(control, () -> control.setContainerSize(72.0), 72.0, 72.0);
            }
            case "progress" -> {
                M3ProgressIndicator control = new M3ProgressIndicator();
                yield new SizeCase(control, () -> {
                    control.setIndicatorSize(54.0);
                    control.setWaveIndicatorSize(76.0);
                    control.setWaveAmplitude(2.0);
                }, 76.0, 76.0);
            }
            case "navigation" -> {
                M3NavigationItem control = new M3NavigationItem();
                yield new SizeCase(control, () -> {
                    control.setItemWidth(96.0);
                    control.setContainerHeight(84.0);
                }, 96.0, 84.0);
            }
            default -> throw new IllegalArgumentException("Unknown control kind: " + kind);
        };
    }

    /// Checks the current minimum and preferred dimensions against the changed tokens.
    private static void assertComputedDimensions(SizeCase fixture) {
        assertEquals(fixture.width(), fixture.control().minWidth(-1));
        assertEquals(fixture.height(), fixture.control().minHeight(-1));
        assertEquals(fixture.width(), fixture.control().prefWidth(-1));
        assertEquals(fixture.height(), fixture.control().prefHeight(-1));
    }

    /// Supplies a size change and the dimensions it should compute.
    ///
    /// @param control the control under test
    /// @param changeSize the token changes to apply
    /// @param width the resulting computed width
    /// @param height the resulting computed height
    @NotNullByDefault
    private record SizeCase(Control control, Runnable changeSize, double width, double height) {
    }
}
