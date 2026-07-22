// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;
import javafx.scene.layout.Region;
import org.glavo.m3fx.controls.M3BottomSheet;
import org.glavo.m3fx.controls.M3SheetVariant;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the BottomSheets component showcase page.
@NotNullByDefault
final class BottomSheetsDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    BottomSheetsDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the bottom sheet component page.
    Node createContent() {
        M3BottomSheet bottomSheet = new M3BottomSheet("Now playing", createSheetContent());
        bottomSheet.getActions().add(createIconButton("close"));
        configureResponsiveWidth(bottomSheet, 520.0);
        bottomSheet.setOnDragHandleAction(event -> bottomSheet.setPrefHeight(
                bottomSheet.getPrefHeight() > 360.0 ? Region.USE_COMPUTED_SIZE : 420.0
        ));

        M3BottomSheet modalBottomSheet = new M3BottomSheet("Filters", createSheetContent());
        modalBottomSheet.getActions().add(createIconButton("close"));
        modalBottomSheet.setVariant(M3SheetVariant.MODAL);
        modalBottomSheet.setDragHandleVisible(false);
        configureResponsiveWidth(modalBottomSheet, 520.0);

        return createGallery(
                createFullWidthShowcaseGroup("Standard And Modal", bottomSheet, modalBottomSheet)
        );
    }
}
