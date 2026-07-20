// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;
import java.util.function.Supplier;

/// Describes one lazily created page in the M3FX demo catalog.
///
/// @param title           the page title shown in the content pane
/// @param navigationTitle the destination label shown by demo navigation
/// @param sidebarSection  the navigation section containing the page
/// @param subtitle        the supporting description shown below the page title
/// @param materialUrl     the Material Design documentation URL for the component or related guidance
/// @param contentFactory  the factory invoked each time the page is shown
@NotNullByDefault
record DemoPage(
        String title,
        String navigationTitle,
        String sidebarSection,
        String subtitle,
        String materialUrl,
        Supplier<Node> contentFactory
) {
    /// Creates a page descriptor.
    ///
    /// The content factory is retained and may be invoked repeatedly as presentation settings change. It must return
    /// a node that is not attached to another parent.
    ///
    /// @throws NullPointerException if any argument is `null`
    DemoPage {
        Objects.requireNonNull(title, "title");
        Objects.requireNonNull(navigationTitle, "navigationTitle");
        Objects.requireNonNull(sidebarSection, "sidebarSection");
        Objects.requireNonNull(subtitle, "subtitle");
        Objects.requireNonNull(materialUrl, "materialUrl");
        Objects.requireNonNull(contentFactory, "contentFactory");
    }
}

