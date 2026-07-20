// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;

import org.glavo.m3fx.controls.M3LoadingIndicator;
import org.glavo.m3fx.controls.M3LoadingIndicatorVariant;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the LoadingIndicator component showcase page.
@NotNullByDefault
final class LoadingIndicatorDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    LoadingIndicatorDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the loading indicator component page.
    Node createContent() {
        M3LoadingIndicator defaultIndicator = new M3LoadingIndicator();
        applyLargeLoadingIndicator(defaultIndicator);

        M3LoadingIndicator containedIndicator = new M3LoadingIndicator();
        applyLargeLoadingIndicator(containedIndicator);
        containedIndicator.setVariant(M3LoadingIndicatorVariant.CONTAINED);

        return createGallery(
                createShowcaseGroup("Default", defaultIndicator),
                createShowcaseGroup("Contained", containedIndicator)
        );
    }

    /// Applies the large demo loading indicator geometry.
    private static void applyLargeLoadingIndicator(M3LoadingIndicator loadingIndicator) {
        loadingIndicator.setStyle("-m3-container-size: 112px; -m3-indicator-size: 89px;");
        loadingIndicator.setMinSize(112.0, 112.0);
        loadingIndicator.setPrefSize(112.0, 112.0);
        loadingIndicator.setMaxSize(112.0, 112.0);
    }
}
