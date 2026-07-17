// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import javafx.scene.Node;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;
import java.util.function.Supplier;

/// Describes one independently navigable component example in the Catalog.
///
/// @param name the concise example name shown in component-page cards and the example top app bar
/// @param description the supporting text shown in the example card
/// @param sourceUrl the absolute URL of the closest M3FX source or demo implementation
/// @param expressive whether the example specifically demonstrates Material 3 Expressive behavior
/// @param contentFactory a factory that creates a fresh example node for each visit
@NotNullByDefault
record CatalogExample(
        String name,
        String description,
        String sourceUrl,
        boolean expressive,
        Supplier<Node> contentFactory
) {
    /// Validates and stores an example descriptor.
    CatalogExample {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(sourceUrl, "sourceUrl");
        Objects.requireNonNull(contentFactory, "contentFactory");
    }

    /// Creates the example content displayed by an example route.
    ///
    /// @return a newly created, non-null example node
    /// @throws NullPointerException if the configured factory returns `null`
    Node createContent() {
        return Objects.requireNonNull(contentFactory.get(), "contentFactory result");
    }
}
