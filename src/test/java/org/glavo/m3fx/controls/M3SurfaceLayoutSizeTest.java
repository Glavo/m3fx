// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.Region;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.skins.M3DropZoneSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.function.DoubleSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Verifies token-based surface constraints alongside application sizing and content measurement.
@NotNullByDefault
final class M3SurfaceLayoutSizeTest {
    /// Starts JavaFX before constructing controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Retains explicit constraints even when they equal the initial computed value.
    @ParameterizedTest
    @ValueSource(strings = {"banner", "drop-width", "drop-height", "dialog-min", "dialog-max"})
    void tokenChangesPreserveExplicitConstraints(String kind) {
        FxTestUtils.runOnFxThread(() -> {
            ConstraintCase fixture = createCase(kind);
            double initialSize = fixture.size().getAsDouble();
            fixture.constraint().set(initialSize);
            fixture.token().set(fixture.token().get() + 40.0);
            assertEquals(initialSize, fixture.size().getAsDouble());
        });
    }

    /// Retains the final bound value until a reset restores token-based computation.
    @ParameterizedTest
    @ValueSource(strings = {"banner", "drop-width", "drop-height", "dialog-min", "dialog-max"})
    void unbindingAndResetPreserveConstraintOwnership(String kind) {
        FxTestUtils.runOnFxThread(() -> {
            ConstraintCase fixture = createCase(kind);
            SimpleDoubleProperty boundSize = new SimpleDoubleProperty(900.0);
            fixture.constraint().bind(boundSize);
            fixture.token().set(640.0);
            fixture.constraint().unbind();
            fixture.token().set(720.0);
            assertEquals(900.0, fixture.size().getAsDouble());
            fixture.constraint().set(Region.USE_COMPUTED_SIZE);
            assertEquals(720.0 + fixture.margin(), fixture.size().getAsDouble());
            fixture.token().set(800.0);
            assertEquals(800.0 + fixture.margin(), fixture.size().getAsDouble());
        });
    }

    /// Computes the dialog width limits after reset, including the shadow margin and crossed tokens.
    @Test
    void dialogMaximumIncludesMinimumTokenAndShadowMargin() {
        FxTestUtils.runOnFxThread(() -> {
            M3DialogPane pane = new M3DialogPane();
            pane.setMinWidth(Region.USE_COMPUTED_SIZE);
            pane.setMaxWidth(Region.USE_COMPUTED_SIZE);
            assertEquals(304.0, pane.minWidth(-1));
            assertEquals(584.0, pane.maxWidth(-1));
            pane.setContainerMaxWidth(200.0);
            assertEquals(304.0, pane.maxWidth(-1));
            pane.setContainerMinWidth(360.0);
            assertEquals(384.0, pane.minWidth(-1));
            assertEquals(384.0, pane.maxWidth(-1));
            pane.setContentPadding(40.0);
            assertEquals(384.0, pane.minWidth(-1));
            assertEquals(384.0, pane.maxWidth(-1));
        });
    }

    /// Keeps preferred size responsive to content while minimum size follows its tokens.
    @Test
    void dropZonePreferredSizeStillMeasuresContent() {
        FxTestUtils.runOnFxThread(() -> {
            Region content = new Region();
            content.setPrefSize(500.0, 400.0);
            M3DropZone zone = new M3DropZone(content);
            zone.setSkin(new M3DropZoneSkin(zone));
            try {
                zone.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
                assertEquals(240.0, zone.minWidth(-1));
                assertEquals(160.0, zone.minHeight(-1));
                assertEquals(548.0, zone.prefWidth(-1));
                assertEquals(448.0, zone.prefHeight(-1));
                content.setPrefSize(600.0, 500.0);
                assertEquals(648.0, zone.prefWidth(-1));
                assertEquals(548.0, zone.prefHeight(-1));
            } finally {
                zone.setSkin(null);
            }
        });
    }

    /// Creates a surface with one representative token-backed size constraint.
    private static ConstraintCase createCase(String kind) {
        return switch (kind) {
            case "banner" -> {
                M3Banner control = new M3Banner();
                yield new ConstraintCase(control.minHeightProperty(), control.containerMinHeightProperty(),
                        () -> control.minHeight(-1), 0.0);
            }
            case "drop-width" -> {
                M3DropZone control = new M3DropZone();
                yield new ConstraintCase(control.minWidthProperty(), control.containerMinWidthProperty(),
                        () -> control.minWidth(-1), 0.0);
            }
            case "drop-height" -> {
                M3DropZone control = new M3DropZone();
                yield new ConstraintCase(control.minHeightProperty(), control.containerMinHeightProperty(),
                        () -> control.minHeight(-1), 0.0);
            }
            case "dialog-min" -> {
                M3DialogPane control = new M3DialogPane();
                yield new ConstraintCase(control.minWidthProperty(), control.containerMinWidthProperty(),
                        () -> control.minWidth(-1), 24.0);
            }
            case "dialog-max" -> {
                M3DialogPane control = new M3DialogPane();
                yield new ConstraintCase(control.maxWidthProperty(), control.containerMaxWidthProperty(),
                        () -> control.maxWidth(-1), 24.0);
            }
            default -> throw new IllegalArgumentException("Unknown surface constraint: " + kind);
        };
    }

    /// Supplies a constraint, its default token, and the effective size query.
    ///
    /// @param constraint the application layout constraint
    /// @param token the component token that determines its computed default
    /// @param size the effective layout size query
    /// @param margin the additional shadow margin in logical pixels
    @NotNullByDefault
    private record ConstraintCase(DoubleProperty constraint, DoubleProperty token, DoubleSupplier size, double margin) {
    }
}
