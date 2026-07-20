// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;

import org.glavo.m3fx.controls.M3Scrim;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the Scrims component showcase page.
@NotNullByDefault
final class ScrimsDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    ScrimsDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the scrim component page.
    Node createContent() {
        StackPane plainScrim = createScrimPreview(false);
        StackPane actionScrim = createScrimPreview(true);

        return createGallery(
                createShowcaseGroup("States", plainScrim, actionScrim)
        );
    }

    /// Creates a sample scrim preview.
    private StackPane createScrimPreview(boolean actionEnabled) {
        Label content = new Label(actionEnabled ? "Click scrim" : "Modal content");
        content.getStyleClass().add("demo-scrim-content");

        M3Scrim scrim = new M3Scrim();
        scrim.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        if (actionEnabled) {
            scrim.setOnAction(event -> context.showSnackbar("Theme-aware snackbar"));
        }

        StackPane preview = new StackPane(content, scrim);
        preview.getStyleClass().add("demo-scrim-preview");
        preview.setMinSize(360.0, 180.0);
        preview.setPrefSize(360.0, 180.0);
        preview.setMaxSize(360.0, 180.0);
        return preview;
    }
}
