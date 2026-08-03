// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import org.glavo.m3fx.controls.M3IconSize;
import org.glavo.m3fx.controls.M3IconVariant;
import org.glavo.m3fx.controls.M3LoadingIndicatorVariant;
import org.glavo.m3fx.controls.M3MeterSize;
import org.glavo.m3fx.controls.M3MeterVariant;
import org.glavo.m3fx.controls.M3StatusLightSize;
import org.glavo.m3fx.controls.M3StatusLightVariant;
import org.glavo.m3fx.controls.M3TextRole;
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
                        iconExamples()
                ),
                CatalogComponents.component(
                        "Loading indicators",
                        "Loading indicators communicate an indeterminate wait with expressive shape motion.",
                        CatalogIcons.LOADING,
                        "loading-indicator",
                        "M3LoadingIndicator",
                        loadingExamples()
                ),
                CatalogComponents.extensionComponent(
                        "Meters",
                        "Meters represent a user-determined quantity or achievement with optional semantic meaning.",
                        CatalogIcons.METER,
                        "https://opensource.adobe.com/spectrum-web-components/components/meter/",
                        "M3Meter",
                        meterExamples()
                ),
                CatalogComponents.component(
                        "Progress indicators",
                        "Progress indicators communicate determinate or indeterminate operation progress.",
                        CatalogIcons.PROGRESS,
                        "progress-indicators",
                        "M3ProgressBar",
                        progressExamples()
                ),
                CatalogComponents.component(
                        "Snackbars",
                        "Snackbars provide brief feedback and may include one contextual action.",
                        CatalogIcons.SNACKBAR,
                        "snackbar",
                        "M3Snackbar",
                        snackbarExamples()
                ),
                CatalogComponents.extensionComponent(
                        "Status lights",
                        "Status lights pair a semantic or categorical color indicator with descriptive text.",
                        CatalogIcons.STATUS_LIGHT,
                        "https://opensource.adobe.com/spectrum-web-components/components/status-light/",
                        "M3StatusLight",
                        statusLightExamples()
                ),
                CatalogComponents.component(
                        "Tooltips",
                        "Tooltips provide contextual labels or richer supporting information for an anchor.",
                        CatalogIcons.TOOLTIP,
                        "tooltips",
                        "M3Tooltip",
                        tooltipExamples()
                ),
                CatalogComponents.component(
                        "Typography",
                        "The Material type scale provides semantic display, headline, title, body, and label roles.",
                        CatalogIcons.TYPOGRAPHY,
                        "typography",
                        "M3Text",
                        typographyExamples()
                )
        );
    }

    /// Creates semantic size, color, disabled, and local-tint icon examples.
    ///
    /// @return the complete icon example array
    private static CatalogExample[] iconExamples() {
        return new CatalogExample[]{
                CatalogComponents.example(
                        "Icon size scale",
                        "Vector icons across the semantic small through extra-large sizes.",
                        false,
                        CatalogSamples::icons
                ),
                CatalogComponents.example(
                        "Primary icon",
                        "A medium icon using the primary color role.",
                        false,
                        () -> CatalogFeedbackSamples.icon(M3IconSize.MEDIUM, M3IconVariant.PRIMARY, false)
                ),
                CatalogComponents.example(
                        "Secondary icon",
                        "A medium icon using the secondary color role.",
                        false,
                        () -> CatalogFeedbackSamples.icon(M3IconSize.MEDIUM, M3IconVariant.SECONDARY, false)
                ),
                CatalogComponents.example(
                        "Error icon",
                        "A medium icon using the error color role.",
                        false,
                        () -> CatalogFeedbackSamples.icon(M3IconSize.MEDIUM, M3IconVariant.ERROR, false)
                ),
                CatalogComponents.example(
                        "Disabled icon",
                        "A semantic surface icon with disabled opacity.",
                        false,
                        () -> CatalogFeedbackSamples.icon(
                                M3IconSize.MEDIUM,
                                M3IconVariant.ON_SURFACE_VARIANT,
                                true
                        )
                ),
                CatalogComponents.example(
                        "Local icon colors",
                        "Icons whose local tints override semantic theme colors.",
                        false,
                        CatalogFeedbackSamples::locallyColoredIcons
                )
        };
    }

    /// Creates default and contained loading indicators at regular and showcase sizes.
    ///
    /// @return the complete loading-indicator example array
    private static CatalogExample[] loadingExamples() {
        return new CatalogExample[]{
                CatalogComponents.example(
                        "Default loading indicator",
                        "The default morphing indicator without a container.",
                        true,
                        () -> CatalogFeedbackSamples.loadingIndicator(
                                M3LoadingIndicatorVariant.DEFAULT,
                                false
                        )
                ),
                CatalogComponents.example(
                        "Contained loading indicator",
                        "The morphing indicator inside a colored container.",
                        true,
                        () -> CatalogFeedbackSamples.loadingIndicator(
                                M3LoadingIndicatorVariant.CONTAINED,
                                false
                        )
                ),
                CatalogComponents.example(
                        "Large default indicator",
                        "The default variant using enlarged showcase geometry.",
                        true,
                        () -> CatalogFeedbackSamples.loadingIndicator(
                                M3LoadingIndicatorVariant.DEFAULT,
                                true
                        )
                ),
                CatalogComponents.example(
                        "Large contained indicator",
                        "The contained variant using enlarged showcase geometry.",
                        true,
                        () -> CatalogFeedbackSamples.loadingIndicator(
                                M3LoadingIndicatorVariant.CONTAINED,
                                true
                        )
                )
        };
    }

    /// Creates the standard and expressive progress shape and state matrix.
    ///
    /// @return the complete progress example array
    private static CatalogExample[] progressExamples() {
        return new CatalogExample[]{
                progressExample("Determinate linear", "Standard linear progress at 62 percent.", false, false, false),
                progressExample("Indeterminate linear", "Standard linear progress with unknown duration.", false, true, false),
                progressExample("Determinate circular", "Standard circular progress at 62 percent.", true, false, false),
                progressExample("Indeterminate circular", "Standard circular progress with unknown duration.", true, true, false),
                progressExample(
                        "Expressive determinate linear",
                        "Wavy linear progress at 62 percent.",
                        false,
                        false,
                        true
                ),
                progressExample(
                        "Expressive indeterminate linear",
                        "Wavy linear progress with unknown duration.",
                        false,
                        true,
                        true
                ),
                progressExample(
                        "Expressive determinate circular",
                        "Wavy circular progress at 62 percent.",
                        true,
                        false,
                        true
                ),
                progressExample(
                        "Expressive indeterminate circular",
                        "Wavy circular progress with unknown duration.",
                        true,
                        true,
                        true
                )
        };
    }

    /// Creates semantic, size, placement, and wrapping meter examples.
    ///
    /// @return the complete meter example array
    private static CatalogExample[] meterExamples() {
        return new CatalogExample[]{
                CatalogComponents.example(
                        "Semantic variants",
                        "Informative, positive, notice, and negative quantities with explicit value text.",
                        false,
                        CatalogFeedbackSamples::semanticMeters
                ),
                CatalogComponents.example(
                        "Size scale",
                        "Large and small meters for standard and confined layouts.",
                        false,
                        CatalogFeedbackSamples::sizedMeters
                ),
                CatalogComponents.example(
                        "Side labels",
                        "A meter with its descriptive and value labels positioned beside the track.",
                        false,
                        () -> CatalogFeedbackSamples.meter(
                                "Storage space",
                                0.68,
                                "68%",
                                M3MeterVariant.INFORMATIVE,
                                M3MeterSize.LARGE,
                                true
                        )
                ),
                CatalogComponents.example(
                        "Wrapped label",
                        "A constrained meter whose descriptive label wraps while the value remains intact.",
                        false,
                        CatalogFeedbackSamples::wrappedMeter
                )
        };
    }

    /// Creates message, action, dismissible, long-text, and queued snackbar examples.
    ///
    /// @return the complete snackbar example array
    private static CatalogExample[] snackbarExamples() {
        return new CatalogExample[]{
                CatalogComponents.example(
                        "Message snackbar",
                        "A brief message without additional actions.",
                        false,
                        () -> CatalogFeedbackSamples.snackbar(false, false, false)
                ),
                CatalogComponents.example(
                        "Snackbar with action",
                        "A brief message with one contextual Undo action.",
                        false,
                        () -> CatalogFeedbackSamples.snackbar(true, false, false)
                ),
                CatalogComponents.example(
                        "Dismissible snackbar",
                        "A brief message with an explicit close action.",
                        false,
                        () -> CatalogFeedbackSamples.snackbar(false, true, false)
                ),
                CatalogComponents.example(
                        "Long snackbar",
                        "A longer message with action and close controls.",
                        false,
                        () -> CatalogFeedbackSamples.snackbar(true, true, true)
                ),
                CatalogComponents.example(
                        "Snackbar queue",
                        "An interactive local overlay that can enqueue follow-up messages.",
                        false,
                        CatalogSamples::snackbars
                )
        };
    }

    /// Creates semantic, size, custom-color, and disabled status-light examples.
    ///
    /// @return the complete status-light example array
    private static CatalogExample[] statusLightExamples() {
        return new CatalogExample[]{
                CatalogComponents.example(
                        "Semantic variants",
                        "Neutral, positive, negative, notice, and informational states with explicit labels.",
                        false,
                        CatalogFeedbackSamples::semanticStatusLights
                ),
                CatalogComponents.example(
                        "Size scale",
                        "Status lights across the small through extra-large size roles.",
                        false,
                        CatalogFeedbackSamples::sizedStatusLights
                ),
                CatalogComponents.example(
                        "Category color",
                        "A non-semantic category using an explicit indicator color and descriptive label.",
                        false,
                        CatalogFeedbackSamples::categoryStatusLight
                ),
                CatalogComponents.example(
                        "Disabled status",
                        "An unavailable status retained for layout continuity.",
                        false,
                        () -> CatalogFeedbackSamples.statusLight(
                                "Synchronization unavailable",
                                M3StatusLightVariant.NEUTRAL,
                                M3StatusLightSize.MEDIUM,
                                true
                        )
                )
        };
    }

    /// Creates plain, rich, actionable, and persistent tooltip examples.
    ///
    /// @return the complete tooltip example array
    private static CatalogExample[] tooltipExamples() {
        return new CatalogExample[]{
                CatalogComponents.example(
                        "Plain tooltip",
                        "A brief label attached to a text button.",
                        false,
                        () -> CatalogFeedbackSamples.plainTooltip(false, false)
                ),
                CatalogComponents.example(
                        "Long plain tooltip",
                        "A wrapped contextual explanation attached to a button.",
                        false,
                        () -> CatalogFeedbackSamples.plainTooltip(true, false)
                ),
                CatalogComponents.example(
                        "Icon-button tooltip",
                        "A brief accessible label attached to an icon-only action.",
                        false,
                        () -> CatalogFeedbackSamples.plainTooltip(false, true)
                ),
                CatalogComponents.example(
                        "Rich tooltip",
                        "A transient tooltip with title and supporting text.",
                        false,
                        () -> CatalogFeedbackSamples.richTooltip(false, false)
                ),
                CatalogComponents.example(
                        "Rich tooltip with action",
                        "A transient rich tooltip containing a related action.",
                        false,
                        () -> CatalogFeedbackSamples.richTooltip(true, false)
                ),
                CatalogComponents.example(
                        "Persistent rich tooltip",
                        "A rich tooltip that remains available while its action is used.",
                        false,
                        () -> CatalogFeedbackSamples.richTooltip(true, true)
                )
        };
    }

    /// Creates one example for every Material type-scale family.
    ///
    /// @return the complete typography example array
    private static CatalogExample[] typographyExamples() {
        return new CatalogExample[]{
                CatalogComponents.example(
                        "Display roles",
                        "Large, medium, and small display typography.",
                        false,
                        () -> CatalogFeedbackSamples.typography(
                                M3TextRole.DISPLAY_LARGE,
                                M3TextRole.DISPLAY_MEDIUM,
                                M3TextRole.DISPLAY_SMALL
                        )
                ),
                CatalogComponents.example(
                        "Headline roles",
                        "Large, medium, and small headline typography.",
                        false,
                        () -> CatalogFeedbackSamples.typography(
                                M3TextRole.HEADLINE_LARGE,
                                M3TextRole.HEADLINE_MEDIUM,
                                M3TextRole.HEADLINE_SMALL
                        )
                ),
                CatalogComponents.example(
                        "Title roles",
                        "Large, medium, and small title typography.",
                        false,
                        () -> CatalogFeedbackSamples.typography(
                                M3TextRole.TITLE_LARGE,
                                M3TextRole.TITLE_MEDIUM,
                                M3TextRole.TITLE_SMALL
                        )
                ),
                CatalogComponents.example(
                        "Body roles",
                        "Large, medium, and small body typography.",
                        false,
                        () -> CatalogFeedbackSamples.typography(
                                M3TextRole.BODY_LARGE,
                                M3TextRole.BODY_MEDIUM,
                                M3TextRole.BODY_SMALL
                        )
                ),
                CatalogComponents.example(
                        "Label roles",
                        "Large, medium, and small label typography.",
                        false,
                        () -> CatalogFeedbackSamples.typography(
                                M3TextRole.LABEL_LARGE,
                                M3TextRole.LABEL_MEDIUM,
                                M3TextRole.LABEL_SMALL
                        )
                )
        };
    }

    /// Creates one progress example descriptor.
    ///
    /// @param name the example name
    /// @param description the example description
    /// @param circular whether the indicator is circular
    /// @param indeterminate whether progress is indeterminate
    /// @param expressive whether expressive wavy geometry is enabled
    /// @return the example descriptor
    private static CatalogExample progressExample(
            String name,
            String description,
            boolean circular,
            boolean indeterminate,
            boolean expressive
    ) {
        return CatalogComponents.example(
                name,
                description,
                expressive,
                () -> CatalogFeedbackSamples.progress(circular, indeterminate, expressive)
        );
    }
}
