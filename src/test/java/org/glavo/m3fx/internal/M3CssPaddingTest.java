// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.controls.M3TextField;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/// Verifies padding ownership, binding suspension, and explicit layout restoration.
@NotNullByDefault
final class M3CssPaddingTest {
    /// Starts JavaFX before constructing controls.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Preserves application padding that differs from the preceding helper value.
    @Test
    void changedApplicationValueRemainsApplicationOwned() {
        FxTestUtils.runOnFxThread(() -> {
            Region region = new Region();
            M3Css.setPaddingIfUnbound(region, new Insets(8.0));
            region.setPadding(new Insets(12.0));
            M3Css.setPaddingIfUnbound(region, new Insets(16.0));
            assertEquals(new Insets(12.0), region.getPadding());
        });
    }

    /// Resumes a suspended helper update only after its application binding is removed.
    @Test
    void suspendedHelperPaddingResumesAfterUnbinding() {
        FxTestUtils.runOnFxThread(() -> {
            Region region = new Region();
            M3Css.setPaddingIfUnbound(region, new Insets(8.0));
            SimpleObjectProperty<Insets> padding = new SimpleObjectProperty<>(new Insets(12.0));
            region.paddingProperty().bind(padding);
            M3Css.setPaddingIfUnbound(region, new Insets(16.0));
            assertEquals(new Insets(12.0), region.getPadding());
            region.paddingProperty().unbind();
            M3Css.setPaddingIfUnbound(region, new Insets(24.0));
            assertEquals(new Insets(24.0), region.getPadding());
        });
    }

    /// Does not acquire ownership from a binding installed before the helper is used.
    @Test
    void applicationBindingDoesNotAcquireHelperOwnership() {
        FxTestUtils.runOnFxThread(() -> {
            Region region = new Region();
            region.paddingProperty().bind(new SimpleObjectProperty<>(new Insets(12.0)));
            M3Css.setPaddingIfUnbound(region, new Insets(16.0));
            region.paddingProperty().unbind();
            M3Css.setPaddingIfUnbound(region, new Insets(24.0));
            assertEquals(new Insets(12.0), region.getPadding());
        });
    }

    /// Keeps restored empty application padding distinct from a helper-owned empty value.
    @Test
    void restoredPaddingDoesNotReacquireOwnership() {
        FxTestUtils.runOnFxThread(() -> {
            Region region = new Region();
            M3Css.setPaddingAsHelperOwned(region, new Insets(8.0));
            M3Css.setPaddingWithoutOwnershipIfUnbound(region, Insets.EMPTY);
            M3Css.setPaddingIfUnbound(region, new Insets(16.0));
            assertEquals(Insets.EMPTY, region.getPadding());
        });
    }

    /// Discards a suspended update when a layout explicitly restores application padding.
    @Test
    void restorationDiscardsSuspendedOwnership() {
        FxTestUtils.runOnFxThread(() -> {
            Region region = new Region();
            M3Css.setPaddingAsHelperOwned(region, new Insets(8.0));
            region.paddingProperty().bind(new SimpleObjectProperty<>(new Insets(12.0)));
            M3Css.setPaddingIfUnbound(region, new Insets(16.0));
            region.paddingProperty().unbind();
            M3Css.setPaddingWithoutOwnershipIfUnbound(region, new Insets(4.0));
            M3Css.setPaddingIfUnbound(region, new Insets(24.0));
            assertEquals(new Insets(4.0), region.getPadding());
        });
    }

    /// Allows explicit layout management to resume token-derived padding updates.
    @Test
    void layoutOwnershipCanBeRestoredExplicitly() {
        FxTestUtils.runOnFxThread(() -> {
            Region region = new Region();
            M3Css.setPaddingWithoutOwnershipIfUnbound(region, new Insets(12.0));
            M3Css.setPaddingAsHelperOwned(region, Insets.EMPTY);
            M3Css.setPaddingIfUnbound(region, new Insets(16.0));
            assertEquals(new Insets(16.0), region.getPadding());
        });
    }

    /// Keeps component tokens effective when the inherited JavaFX stylesheet applies its defaults.
    @Test
    void textFieldPaddingSurvivesUserAgentCss() {
        FxTestUtils.runOnFxThread(() -> {
            M3TextField field = new M3TextField();
            field.setHorizontalPadding(22.0);
            StackPane root = new StackPane(field);
            new Scene(root);
            root.applyCss();
            assertEquals(22.0, field.getPadding().getLeft());
            field.setHorizontalPadding(30.0);
            root.applyCss();
            assertEquals(30.0, field.getPadding().getLeft());
        });
    }
}
