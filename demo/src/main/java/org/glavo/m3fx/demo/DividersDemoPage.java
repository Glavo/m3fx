// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import org.glavo.m3fx.controls.M3Divider;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the Dividers component showcase page.
@NotNullByDefault
final class DividersDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    DividersDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the divider component page.
    Node createContent() {
        M3Divider full = new M3Divider();
        full.setPrefWidth(360.0);
        M3Divider inset = new M3Divider();
        inset.setInsetStart(32.0);
        inset.setPrefWidth(360.0);
        M3Divider middle = new M3Divider();
        middle.setInsetStart(32.0);
        middle.setInsetEnd(32.0);
        middle.setPrefWidth(360.0);
        M3Divider vertical = new M3Divider(Orientation.VERTICAL);
        vertical.setPrefHeight(72.0);

        return createGallery(
                createShowcaseGroup("Horizontal", full, inset, middle),
                createShowcaseGroup("Vertical", vertical)
        );
    }
}
