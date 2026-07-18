// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import javafx.scene.Node;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

/// Defines the immutable component and example registry presented by the M3FX Catalog.
///
/// The registry follows the component names used by the AndroidX Material 3 Catalog and deliberately omits entries
/// for which M3FX has no direct control. The list is alphabetical and does not add an application-specific category
/// hierarchy.
@NotNullByDefault
final class CatalogComponents {
    /// The GitHub source root for M3FX controls.
    private static final String SOURCE_ROOT =
            "https://github.com/Glavo/m3fx/blob/main/src/main/java/org/glavo/m3fx/controls/";

    /// The prospective Javadoc root used by component API links.
    private static final String JAVADOC_ROOT =
            "https://javadoc.io/doc/org.glavo/m3fx/latest/org.glavo.m3fx/org/glavo/m3fx/controls/";

    /// The complete immutable component registry.
    private static final @Unmodifiable List<CatalogComponent> COMPONENTS = createComponents();

    /// Prevents utility class instantiation.
    private CatalogComponents() {
    }

    /// Returns the complete immutable component registry.
    ///
    /// @return the components in case-insensitive alphabetical order
    static @Unmodifiable List<CatalogComponent> all() {
        return COMPONENTS;
    }

    /// Creates a component descriptor with links derived from the Material slug and M3FX class.
    ///
    /// @param name         the component name
    /// @param description  the component description
    /// @param iconPath     the component icon path
    /// @param materialSlug the Material Design documentation slug
    /// @param className    the primary M3FX class name
    /// @param examples     the component examples
    /// @return the component descriptor
    static CatalogComponent component(
            String name,
            String description,
            String iconPath,
            String materialSlug,
            String className,
            CatalogExample... examples
    ) {
        return new CatalogComponent(
                name,
                description,
                iconPath,
                "https://m3.material.io/components/" + materialSlug + "/overview",
                JAVADOC_ROOT + className + ".html",
                SOURCE_ROOT + className + ".java",
                List.of(examples)
        );
    }

    /// Creates an example descriptor whose source link points to the Catalog sample factory.
    ///
    /// @param name        the example name
    /// @param description the example description
    /// @param expressive  whether the example demonstrates Expressive behavior
    /// @param factory     the example content factory
    /// @return the example descriptor
    static CatalogExample example(
            String name,
            String description,
            boolean expressive,
            Supplier<Node> factory
    ) {
        return new CatalogExample(
                name,
                description,
                "https://github.com/Glavo/m3fx/blob/main/catalog/src/main/java/org/glavo/m3fx/catalog/"
                        + "CatalogSamples.java",
                expressive,
                factory
        );
    }

    /// Creates, sorts, validates, and freezes the component registry.
    ///
    /// @return the immutable component registry
    private static @Unmodifiable List<CatalogComponent> createComponents() {
        ArrayList<CatalogComponent> components = new ArrayList<>(38);
        components.addAll(CatalogContainerComponents.create());
        components.addAll(CatalogActionComponents.create());
        components.addAll(CatalogInputComponents.create());
        components.addAll(CatalogNavigationComponents.create());
        components.addAll(CatalogFeedbackComponents.create());
        components.sort(Comparator.comparing(CatalogComponent::name, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(CatalogComponent::name));

        long distinctNames = components.stream().map(CatalogComponent::name).distinct().count();
        if (distinctNames != components.size()) {
            throw new IllegalStateException("Catalog component names must be unique");
        }
        return List.copyOf(components);
    }
}
