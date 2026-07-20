// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3SheetVariant;
import org.glavo.m3fx.controls.M3SideSheet;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the SideSheets component showcase page.
@NotNullByDefault
final class SideSheetsDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    SideSheetsDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the side sheet component page.
    Node createContent() {
        M3SideSheet sideSheet = new M3SideSheet("Details", createSheetContent());
        sideSheet.getHeaderActions().add(createIconButton("close"));

        M3SideSheet modalSideSheet = new M3SideSheet("Filters", createSheetContent());
        modalSideSheet.getHeaderActions().add(createIconButton("close"));
        modalSideSheet.getActions().addAll(
                new M3Button("Cancel", M3ButtonVariant.TEXT),
                new M3Button("Apply", M3ButtonVariant.FILLED)
        );
        modalSideSheet.setVariant(M3SheetVariant.MODAL);

        M3SideSheet detachedSideSheet = new M3SideSheet("Detached", createSheetContent());
        detachedSideSheet.getHeaderActions().add(createIconButton("close"));
        detachedSideSheet.setDetached(true);

        return createGallery(
                createShowcaseGroup("Standard, Modal, And Detached", sideSheet, modalSideSheet, detachedSideSheet)
        );
    }
}
