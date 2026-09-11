// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies consistent icon-size ownership across single and paired graphic slots.
@NotNullByDefault
final class M3ManagedIconSizeTest {
    /// Starts JavaFX before constructing controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Covers both rendering models in every slot that supplies an icon-size token.
    private static Stream<Arguments> slots() {
        return Stream.of("button", "toggle", "fab", "chip-leading", "chip-trailing", "segmented",
                "switch-selected", "switch-unselected", "slider-active", "slider-inactive")
                .flatMap(kind -> Stream.of(Arguments.of(kind, false), Arguments.of(kind, true)));
    }

    /// Preserves explicit sizes set before attachment and equal-valued assignments made afterward.
    @ParameterizedTest
    @MethodSource("slots")
    void explicitIconSizeTakesPrecedence(String kind, boolean svg) {
        FxTestUtils.runOnFxThread(() -> {
            Slot slot = createSlot(kind);
            M3IconGraphic icon = createIcon(svg);
            icon.iconSizeProperty().set(64.0);
            slot.graphic().set((Node) icon);
            assertEquals(64.0, icon.iconSizeProperty().get());
            slot.refresh();
            assertEquals(64.0, icon.iconSizeProperty().get());
            M3IconGraphic replacement = createIcon(svg);
            slot.graphic().set((Node) replacement);
            double managedSize = replacement.iconSizeProperty().get();
            replacement.iconSizeProperty().set(managedSize);
            slot.refresh();
            attach(slot.owner());
            assertEquals(managedSize, replacement.iconSizeProperty().get());
        });
    }

    /// Leaves bound icon sizing unchanged and does not report listener exceptions during updates.
    @ParameterizedTest
    @MethodSource("slots")
    void boundIconSizeTakesPrecedence(String kind, boolean svg) {
        FxTestUtils.runOnFxThread(() -> {
            Thread thread = Thread.currentThread();
            @Nullable Thread.UncaughtExceptionHandler previous = thread.getUncaughtExceptionHandler();
            List<Throwable> failures = new ArrayList<>();
            thread.setUncaughtExceptionHandler((ignored, failure) -> failures.add(failure));
            try {
                Slot slot = createSlot(kind);
                M3IconGraphic icon = createIcon(svg);
                SimpleDoubleProperty size = new SimpleDoubleProperty(64.0);
                icon.iconSizeProperty().bind(size);
                slot.graphic().set((Node) icon);
                slot.refresh();
                attach(slot.owner());
                assertEquals(64.0, icon.iconSizeProperty().get());
                size.set(72.0);
                assertEquals(72.0, icon.iconSizeProperty().get());
                slot.graphic().set(null);
                assertTrue(failures.isEmpty(), failures::toString);
            } finally {
                thread.setUncaughtExceptionHandler(previous);
            }
        });
    }

    /// Keeps container sizing effective through CSS, then restores CSS eligibility when the icon is replaced.
    @ParameterizedTest
    @MethodSource("slots")
    void replacementReleasesIconSizing(String kind, boolean svg) {
        FxTestUtils.runOnFxThread(() -> {
            Slot slot = createSlot(kind);
            M3IconGraphic icon = createIcon(svg);
            ((Node) icon).setStyle("-m3-icon-size: 60px;");
            slot.graphic().set((Node) icon);
            StackPane root = attach(slot.owner());
            assertEquals(slot.size(), icon.iconSizeProperty().get());
            slot.refresh();
            root.applyCss();
            assertEquals(slot.size(), icon.iconSizeProperty().get());
            M3IconGraphic replacement = createIcon(svg);
            slot.graphic().set((Node) replacement);
            root.applyCss();
            root.layout();
            assertEquals(slot.size(), replacement.iconSizeProperty().get());
            root.getChildren().setAll((Node) icon);
            root.applyCss();
            assertEquals(60.0, icon.iconSizeProperty().get());
        });
    }

    /// Creates a font or SVG icon with its normal semantic size.
    private static M3IconGraphic createIcon(boolean svg) {
        return svg ? new M3SVGIcon("M0 0 H24 V24 H0 Z") : new M3Icon("info");
    }

    /// Resolves the container and graphic styles in a themed scene.
    private static StackPane attach(Node owner) {
        StackPane root = new StackPane(owner);
        Scene scene = new Scene(root);
        M3ThemeManager.install(scene, M3Theme.defaultTheme());
        FxTestUtils.assertNoCssWarnings(root::applyCss);
        root.layout();
        return root;
    }

    /// Creates the selected control and exposes the graphic slot under test.
    private static Slot createSlot(String kind) {
        return switch (kind) {
            case "button" -> {
                M3Button control = new M3Button();
                yield new Slot(control, control.graphicProperty(), control.iconSizeProperty());
            }
            case "toggle" -> {
                M3IconToggleButton control = new M3IconToggleButton();
                yield new Slot(control, control.graphicProperty(), control.iconSizeProperty());
            }
            case "fab" -> {
                M3FloatingActionButton control = new M3FloatingActionButton();
                yield new Slot(control, control.graphicProperty(), control.iconSizeProperty());
            }
            case "chip-leading", "chip-trailing" -> {
                M3AssistChip control = new M3AssistChip();
                yield new Slot(control, kind.equals("chip-leading") ? control.graphicProperty()
                        : control.trailingGraphicProperty(), control.iconSizeProperty());
            }
            case "segmented" -> {
                M3SegmentedButton control = new M3SegmentedButton();
                yield new Slot(control, control.graphicProperty(), null);
            }
            case "switch-selected", "switch-unselected" -> {
                M3Switch control = new M3Switch();
                yield new Slot(control, kind.equals("switch-selected") ? control.selectedIconProperty()
                        : control.unselectedIconProperty(), control.iconSizeProperty());
            }
            case "slider-active", "slider-inactive" -> {
                M3Slider control = new M3Slider();
                yield new Slot(control, kind.equals("slider-active") ? control.activeTrackGraphicProperty()
                        : control.inactiveTrackGraphicProperty(), control.iconSizeProperty());
            }
            default -> throw new IllegalArgumentException("Unknown slot: " + kind);
        };
    }

    /// Describes one icon slot and its mutable token, if available.
    ///
    /// @param owner the containing control
    /// @param graphic the nullable graphic slot
    /// @param token the container token, or `null` for the fixed 18-pixel segmented-button slot
    @NotNullByDefault
    private record Slot(Node owner, ObjectProperty<@Nullable Node> graphic, @Nullable StyleableDoubleProperty token) {
        /// Returns the container's current target size.
        double size() {
            return token == null ? 18.0 : token.get();
        }

        /// Triggers another container update through its token or by reattaching a fixed-size graphic.
        void refresh() {
            if (token == null) {
                @Nullable Node current = graphic.get();
                graphic.set(null);
                graphic.set(current);
            } else {
                token.set(token.get() + 8.0);
            }
        }
    }
}
