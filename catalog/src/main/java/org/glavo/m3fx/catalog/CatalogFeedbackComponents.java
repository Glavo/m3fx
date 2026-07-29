// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

/// Supplies progress, feedback, and type-system entries for the Catalog registry.
@NotNullByDefault
final class CatalogFeedbackComponents {
    /// Prevents utility class instantiation.
    private CatalogFeedbackComponents() {
    }

    /// Creates the feedback component descriptors.
    ///
    /// @return the immutable descriptor list
    static @Unmodifiable List<CatalogComponent> create() {
        return List.of(
                CatalogComponents.extensionComponent(
                        "Icons",
                        "Icons use scalable SVG paths with semantic Material size and color roles.",
                        CatalogIcons.ICONS,
                        "https://m3.material.io/styles/icons/overview",
                        "M3SVGIcon",
                        CatalogComponents.example(
                                "Icon sizes and colors",
                                "Vector icons across semantic size and color roles.",
                                false,
                                CatalogSamples::icons
                        )
                ),
                CatalogComponents.component(
                        "Loading indicators",
                        "Loading indicators communicate an indeterminate wait with expressive shape motion.",
                        CatalogIcons.LOADING,
                        "loading-indicator",
                        "M3LoadingIndicator",
                        CatalogComponents.example(
                                "Default and contained",
                                "The two Material loading-indicator presentations.",
                                true,
                                CatalogSamples::loadingIndicators
                        )
                ),
                CatalogComponents.component(
                        "Progress indicators",
                        "Progress indicators communicate determinate or indeterminate operation progress.",
                        CatalogIcons.PROGRESS,
                        "progress-indicators",
                        "M3ProgressBar",
                        CatalogComponents.example(
                                "Linear and circular",
                                "Determinate and indeterminate progress indicators.",
                                true,
                                CatalogSamples::progressIndicators
                        )
                ),
                CatalogComponents.component(
                        "Snackbars",
                        "Snackbars provide brief feedback and may include one contextual action.",
                        CatalogIcons.SNACKBAR,
                        "snackbar",
                        "M3Snackbar",
                        CatalogComponents.example(
                                "Snackbar presentation",
                                "A transient message presented by a local overlay pane.",
                                false,
                                CatalogSamples::snackbars
                        )
                ),
                CatalogComponents.component(
                        "Tooltips",
                        "Tooltips provide contextual labels or richer supporting information for an anchor.",
                        CatalogIcons.TOOLTIP,
                        "tooltips",
                        "M3Tooltip",
                        CatalogComponents.example(
                                "Plain and rich",
                                "Tooltips attached to interactive anchors.",
                                false,
                                CatalogSamples::tooltips
                        )
                ),
                CatalogComponents.component(
                        "Typography",
                        "The Material type scale provides semantic display, headline, title, body, and label roles.",
                        CatalogIcons.TYPOGRAPHY,
                        "typography",
                        "M3Text",
                        CatalogComponents.example(
                                "Type scale",
                                "Representative semantic typography roles.",
                                false,
                                CatalogSamples::typography
                        )
                )
        );
    }
}
