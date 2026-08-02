// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import org.glavo.m3fx.controls.M3StatusLight;
import org.glavo.m3fx.controls.M3StatusLightSize;
import org.glavo.m3fx.controls.M3StatusLightVariant;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the Status Lights extension showcase page.
@NotNullByDefault
final class StatusLightsDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    StatusLightsDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the status-light extension page.
    ///
    /// @return the complete status-light showcase
    Node createContent() {
        M3StatusLight category = statusLight(
                "Design review",
                M3StatusLightVariant.NEUTRAL,
                M3StatusLightSize.MEDIUM,
                false,
                "demo-status-light-category"
        );
        category.setIndicatorColor(Color.web("#76558E"));

        return createGallery(
                createShowcaseGroup(
                        "Semantic Variants",
                        statusLight("Queued", M3StatusLightVariant.NEUTRAL, M3StatusLightSize.MEDIUM, false,
                                "demo-status-light-semantic"),
                        statusLight("Service healthy", M3StatusLightVariant.POSITIVE, M3StatusLightSize.MEDIUM, false,
                                "demo-status-light-semantic"),
                        statusLight("Build failed", M3StatusLightVariant.NEGATIVE, M3StatusLightSize.MEDIUM, false,
                                "demo-status-light-semantic"),
                        statusLight("Review required", M3StatusLightVariant.NOTICE, M3StatusLightSize.MEDIUM, false,
                                "demo-status-light-semantic"),
                        statusLight("Update available", M3StatusLightVariant.INFO, M3StatusLightSize.MEDIUM, false,
                                "demo-status-light-semantic")
                ),
                createShowcaseGroup(
                        "Sizes",
                        statusLight("Small", M3StatusLightVariant.POSITIVE, M3StatusLightSize.SMALL, false,
                                "demo-status-light-size"),
                        statusLight("Medium", M3StatusLightVariant.POSITIVE, M3StatusLightSize.MEDIUM, false,
                                "demo-status-light-size"),
                        statusLight("Large", M3StatusLightVariant.POSITIVE, M3StatusLightSize.LARGE, false,
                                "demo-status-light-size"),
                        statusLight("Extra large", M3StatusLightVariant.POSITIVE, M3StatusLightSize.EXTRA_LARGE, false,
                                "demo-status-light-size")
                ),
                createShowcaseGroup(
                        "Category and Availability",
                        category,
                        statusLight(
                                "Synchronization unavailable",
                                M3StatusLightVariant.NEUTRAL,
                                M3StatusLightSize.MEDIUM,
                                true,
                                "demo-status-light-disabled"
                        )
                )
        );
    }

    /// Creates one configured status-light sample.
    ///
    /// @param text the descriptive status text
    /// @param variant the semantic status variant
    /// @param size the nominal status-light size
    /// @param disabled whether the status is unavailable
    /// @param styleClass the demo style class identifying the sample role
    /// @return the configured status light
    private static M3StatusLight statusLight(
            String text,
            M3StatusLightVariant variant,
            M3StatusLightSize size,
            boolean disabled,
            String styleClass
    ) {
        M3StatusLight statusLight = new M3StatusLight(text, variant);
        statusLight.setSize(size);
        statusLight.setDisable(disabled);
        statusLight.getStyleClass().add(styleClass);
        return statusLight;
    }
}
