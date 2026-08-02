// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.PickResult;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.skins.M3DropZoneSkin;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.theme.M3ThemeManager;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies the public state, layout, styling, and JavaFX drag-event contract of [M3DropZone].
@NotNullByDefault
final class M3DropZoneTest {
    /// Starts the JavaFX toolkit before controls are constructed.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies defaults, property ownership, null handling, and pseudo-class state.
    @Test
    void exposesStableStateProperties() {
        FxTestUtils.runOnFxThread(() -> {
            Label content = new Label("Drop content");
            M3DropZone zone = new M3DropZone(content);

            assertSame(content, zone.getContent());
            assertSame(zone, zone.contentProperty().getBean());
            assertSame(zone, zone.filledProperty().getBean());
            assertSame(zone, zone.preferredTransferModeProperty().getBean());
            assertSame(zone, zone.acceptancePredicateProperty().getBean());
            assertSame(zone, zone.dragActiveProperty().getBean());
            assertInstanceOf(ReadOnlyBooleanProperty.class, zone.dragActiveProperty());
            assertEquals(TransferMode.COPY, zone.getPreferredTransferMode());
            assertNull(zone.getAcceptancePredicate());
            assertFalse(zone.isFilled());
            assertFalse(zone.isDragActive());
            assertEquals(240.0, zone.getContainerMinWidth(), 0.0001);
            assertEquals(160.0, zone.getContainerMinHeight(), 0.0001);
            assertEquals(24.0, zone.getContentPadding(), 0.0001);
            assertEquals(AccessibleRole.PARENT, zone.getAccessibleRole());
            assertFalse(zone.isFocusTraversable());

            zone.setFilled(true);
            assertTrue(zone.getPseudoClassStates().contains(PseudoClass.getPseudoClass("filled")));
            zone.setFilled(false);
            assertFalse(zone.getPseudoClassStates().contains(PseudoClass.getPseudoClass("filled")));

            zone.setPreferredTransferMode(TransferMode.MOVE);
            assertEquals(TransferMode.MOVE, zone.getPreferredTransferMode());
            assertThrows(NullPointerException.class, () -> zone.setPreferredTransferMode(null));
            assertThrows(NullPointerException.class, () -> zone.preferredTransferModeProperty().set(null));

            zone.setContainerMinWidth(320.0);
            zone.setContainerMinHeight(180.0);
            zone.setContentPadding(20.0);
            assertEquals(320.0, zone.getContainerMinWidth(), 0.0001);
            assertEquals(180.0, zone.getContainerMinHeight(), 0.0001);
            assertEquals(20.0, zone.getContentPadding(), 0.0001);
            assertThrows(IllegalArgumentException.class, () -> zone.setContainerMinWidth(-1.0));
            assertThrows(IllegalArgumentException.class, () -> zone.setContainerMinHeight(Double.NaN));
            assertThrows(IllegalArgumentException.class, () -> zone.setContentPadding(Double.POSITIVE_INFINITY));
        });
    }

    /// Verifies skin ownership, CSS metrics, filled styling, and content replacement.
    @Test
    void skinPresentsContentAndCssMetrics() {
        FxTestUtils.runOnFxThread(() -> {
            Label first = new Label("First");
            M3DropZone zone = new M3DropZone(first);
            zone.setStyle(
                    "-m3-container-min-width: 300px;"
                            + "-m3-container-min-height: 190px;"
                            + "-m3-content-padding: 18px;"
            );
            StackPane root = new StackPane(zone);
            Scene scene = new Scene(root, 420.0, 280.0);
            M3ThemeManager.install(scene, M3Theme.defaultTheme());
            root.applyCss();
            root.resize(420.0, 280.0);
            root.layout();

            assertInstanceOf(M3DropZoneSkin.class, zone.getSkin());
            assertSame(zone, first.getParent());
            assertEquals(300.0, zone.getContainerMinWidth(), 0.0001);
            assertEquals(190.0, zone.getContainerMinHeight(), 0.0001);
            assertEquals(new Insets(18.0), zone.getPadding());
            assertEquals(16.0,
                    zone.getBorder().getStrokes().get(0).getRadii().getTopLeftHorizontalRadius(), 0.0001);
            assertFalse(zone.getBorder().getStrokes().get(0).getTopStyle().equals(BorderStrokeStyle.SOLID));

            zone.setFilled(true);
            root.applyCss();
            assertEquals(BorderStrokeStyle.SOLID, zone.getBorder().getStrokes().get(0).getTopStyle());
            assertFalse(zone.getBackground().getFills().isEmpty());

            Label second = new Label("Second");
            zone.setContent(second);
            assertNull(first.getParent());
            assertSame(zone, second.getParent());
            assertSame(second, zone.queryAccessibleAttribute(AccessibleAttribute.CONTENTS));
            assertEquals(1, zone.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
            assertSame(second, zone.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, 0));

            FxTestUtils.replaceSkin(zone, M3DropZoneSkin::new);
            assertSame(zone, second.getParent());
            zone.setContent(null);
            assertNull(second.getParent());
            assertEquals(0, zone.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT));
        });
    }

    /// Verifies predicate routing, disabled rejection, exception propagation, and application-owned drop completion.
    @Test
    void preservesNativeDragEventOwnership() {
        FxTestUtils.runOnFxThread(() -> {
            M3DropZone zone = new M3DropZone();
            AtomicInteger predicateCalls = new AtomicInteger();
            AtomicInteger applicationOverCalls = new AtomicInteger();
            zone.setAcceptancePredicate(event -> {
                predicateCalls.incrementAndGet();
                return false;
            });
            zone.setOnDragOver(event -> applicationOverCalls.incrementAndGet());

            DragEvent rejected = dragEvent(zone, DragEvent.DRAG_OVER);
            zone.fireEvent(rejected);
            assertEquals(1, predicateCalls.get());
            assertEquals(1, applicationOverCalls.get());
            assertFalse(rejected.isConsumed());
            assertFalse(rejected.isAccepted());
            assertFalse(zone.isDragActive());

            zone.setDisable(true);
            zone.fireEvent(dragEvent(zone, DragEvent.DRAG_OVER));
            assertEquals(1, predicateCalls.get(), "disabled zones must not evaluate application predicates");
            assertEquals(2, applicationOverCalls.get(), "disabled controls must not consume native drag events");

            zone.setDisable(false);
            IllegalStateException failure = new IllegalStateException("predicate failure");
            zone.setAcceptancePredicate(event -> {
                throw failure;
            });
            IllegalStateException thrown = assertThrows(
                    IllegalStateException.class,
                    () -> zone.fireEvent(dragEvent(zone, DragEvent.DRAG_OVER))
            );
            assertSame(failure, thrown);
            assertFalse(zone.isDragActive());

            AtomicBoolean dropHandled = new AtomicBoolean();
            zone.setOnDragDropped(event -> {
                dropHandled.set(true);
                event.setDropCompleted(true);
            });
            DragEvent dropped = dragEvent(zone, DragEvent.DRAG_DROPPED);
            zone.fireEvent(dropped);
            assertTrue(dropHandled.get());
            assertTrue(dropped.isDropCompleted());
            assertFalse(dropped.isConsumed());
        });
    }

    /// Creates a synthetic drag event that does not expose a dragboard.
    ///
    /// Rejected drag-over and drop-completion paths do not inspect the dragboard, allowing their event-routing
    /// contracts to be tested without starting a native platform drag gesture.
    ///
    /// @param zone the event target
    /// @param eventType the drag event type
    /// @return the synthetic drag event
    private static DragEvent dragEvent(M3DropZone zone, javafx.event.EventType<DragEvent> eventType) {
        return new DragEvent(
                eventType,
                null,
                12.0,
                12.0,
                12.0,
                12.0,
                TransferMode.COPY,
                new Object(),
                zone,
                new PickResult(zone, 12.0, 12.0)
        );
    }
}
