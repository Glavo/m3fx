// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.catalog;

import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Identifies one screen in the Catalog's Home, Component, and Example navigation hierarchy.
@NotNullByDefault
sealed interface CatalogRoute permits CatalogRoute.Home, CatalogRoute.Component, CatalogRoute.Example {
    /// The component-grid home route.
    record Home() implements CatalogRoute {
    }

    /// A component detail route.
    ///
    /// @param component the component displayed by the route
    record Component(CatalogComponent component) implements CatalogRoute {
        /// Validates and stores the route.
        public Component {
            Objects.requireNonNull(component, "component");
        }
    }

    /// An interactive example route.
    ///
    /// @param component the component that owns the example
    /// @param example the displayed example
    record Example(CatalogComponent component, CatalogExample example) implements CatalogRoute {
        /// Validates and stores the route.
        public Example {
            Objects.requireNonNull(component, "component");
            Objects.requireNonNull(example, "example");
            if (!component.examples().contains(example)) {
                throw new IllegalArgumentException("example does not belong to component");
            }
        }
    }
}
