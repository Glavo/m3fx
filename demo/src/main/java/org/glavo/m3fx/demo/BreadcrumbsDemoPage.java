// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;
import org.glavo.m3fx.controls.M3BreadcrumbItem;
import org.glavo.m3fx.controls.M3Breadcrumbs;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the Breadcrumbs extension showcase page.
@NotNullByDefault
final class BreadcrumbsDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    BreadcrumbsDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the breadcrumbs extension page.
    ///
    /// @return the complete breadcrumbs showcase
    Node createContent() {
        M3Breadcrumbs defaults = breadcrumbs(
                "demo-breadcrumbs-default",
                false,
                false,
                "Home",
                "Projects",
                "M3FX"
        );
        M3Breadcrumbs compact = breadcrumbs(
                "demo-breadcrumbs-compact",
                false,
                true,
                "Home",
                "Projects",
                "Libraries",
                "M3FX"
        );
        M3Breadcrumbs overflow = breadcrumbs(
                "demo-breadcrumbs-overflow",
                false,
                false,
                "Home",
                "Projects",
                "Libraries",
                "JavaFX",
                "Controls",
                "M3FX"
        );
        M3Breadcrumbs rootContext = breadcrumbs(
                "demo-breadcrumbs-root",
                true,
                false,
                "On this device",
                "Users",
                "Glavo",
                "Projects",
                "Libraries",
                "M3FX"
        );

        return createGallery(
                createFullWidthShowcaseGroup("Default and Compact", defaults, compact),
                createFullWidthShowcaseGroup("Overflow", overflow),
                createFullWidthShowcaseGroup("Root Context", rootContext)
        );
    }

    /// Creates one interactive breadcrumb hierarchy.
    ///
    /// @param styleClass the demo style class identifying the sample role
    /// @param keepRootVisible whether overflow should retain the root item
    /// @param compact whether compact vertical metrics are used
    /// @param labels the root-to-current hierarchy labels
    /// @return the configured breadcrumbs control
    private M3Breadcrumbs breadcrumbs(
            String styleClass,
            boolean keepRootVisible,
            boolean compact,
            String... labels
    ) {
        M3Breadcrumbs breadcrumbs = new M3Breadcrumbs();
        breadcrumbs.getStyleClass().add(styleClass);
        breadcrumbs.setKeepRootVisible(keepRootVisible);
        breadcrumbs.setCompact(compact);
        for (String label : labels) {
            M3BreadcrumbItem item = new M3BreadcrumbItem(label);
            item.setOnAction(event -> context.showSnackbar("Navigate to " + label));
            breadcrumbs.getItems().add(item);
        }
        configureResponsiveWidth(breadcrumbs, 620.0);
        return breadcrumbs;
    }
}
