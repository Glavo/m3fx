// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/// Describes a Material component presented by the Catalog.
///
/// @param name the alphabetical display name used on the home grid and component page
/// @param description the component overview shown above the example list
/// @param iconPath the SVG path used by the component card and component-page illustration
/// @param guidelinesUrl the absolute Material Design guidelines URL
/// @param docsUrl the absolute M3FX API documentation URL
/// @param sourceUrl the absolute M3FX source URL
/// @param examples the immutable, non-empty list of examples for this component
@NotNullByDefault
record CatalogComponent(
        String name,
        String description,
        String iconPath,
        String guidelinesUrl,
        String docsUrl,
        String sourceUrl,
        @Unmodifiable List<CatalogExample> examples
) {
    /// Validates and stores a component descriptor.
    ///
    /// @throws IllegalArgumentException if `examples` is empty
    CatalogComponent {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(description, "description");
        Objects.requireNonNull(iconPath, "iconPath");
        Objects.requireNonNull(guidelinesUrl, "guidelinesUrl");
        Objects.requireNonNull(docsUrl, "docsUrl");
        Objects.requireNonNull(sourceUrl, "sourceUrl");
        examples = List.copyOf(Objects.requireNonNull(examples, "examples"));
        if (examples.isEmpty()) {
            throw new IllegalArgumentException("examples must not be empty");
        }
    }

    /// Returns whether at least one example demonstrates Material 3 Expressive behavior.
    ///
    /// @return `true` when the component has an Expressive example
    boolean hasExpressiveExamples() {
        return examples.stream().anyMatch(CatalogExample::expressive);
    }

    /// Returns whether this component or one of its scenarios matches search text.
    ///
    /// Matching is case-insensitive and examines the component name and description plus every example name and
    /// description. Leading and trailing whitespace is ignored, and an empty query matches every component.
    ///
    /// @param searchText the search text
    /// @return `true` when this component matches the search text
    boolean matchesSearch(String searchText) {
        String query = Objects.requireNonNull(searchText, "searchText").strip().toLowerCase(Locale.ROOT);
        if (query.isEmpty()
                || name.toLowerCase(Locale.ROOT).contains(query)
                || description.toLowerCase(Locale.ROOT).contains(query)) {
            return true;
        }
        return examples.stream().anyMatch(example ->
                example.name().toLowerCase(Locale.ROOT).contains(query)
                        || example.description().toLowerCase(Locale.ROOT).contains(query));
    }
}
