// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.layout.VBox;
import javafx.scene.Node;

import org.glavo.m3fx.controls.M3BottomAppBar;
import org.glavo.m3fx.controls.M3BottomAppBarFloatingActionAlignment;
import org.glavo.m3fx.controls.M3FloatingActionButtonSize;
import org.glavo.m3fx.controls.M3FloatingActionButtonVariant;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the BottomAppBars component showcase page.
@NotNullByDefault
final class BottomAppBarsDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    BottomAppBarsDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the bottom app bar component page.
    Node createContent() {
        M3BottomAppBar end = createBottomAppBar();
        M3BottomAppBar center = createBottomAppBar();
        center.setFloatingActionAlignment(M3BottomAppBarFloatingActionAlignment.CENTER);
        M3BottomAppBar start = createBottomAppBar();
        start.setFloatingActionAlignment(M3BottomAppBarFloatingActionAlignment.START);

        return createGallery(
                createFullWidthShowcaseGroup(
                        "Floating Action",
                        createBottomAppBarPreview(end),
                        createBottomAppBarPreview(center),
                        createBottomAppBarPreview(start)
                )
        );
    }

    /// Creates a preview surface for a bottom app bar.
    private static VBox createBottomAppBarPreview(M3BottomAppBar bottomAppBar) {
        VBox preview = createAppBarPreview();
        preview.getChildren().add(bottomAppBar);
        return preview;
    }

    /// Creates a bottom app bar sample.
    private static M3BottomAppBar createBottomAppBar() {
        M3BottomAppBar bottomAppBar = new M3BottomAppBar();
        bottomAppBar.setFloatingActionAlignment(M3BottomAppBarFloatingActionAlignment.END);
        bottomAppBar.setFloatingAction(
                createFab("add", M3FloatingActionButtonVariant.PRIMARY, M3FloatingActionButtonSize.REGULAR)
        );
        bottomAppBar.getActions().addAll(
                createTrailingAppBarIconButton("search"),
                createTrailingAppBarIconButton("favorite")
        );
        bottomAppBar.setMaxWidth(Double.MAX_VALUE);
        return bottomAppBar;
    }
}
