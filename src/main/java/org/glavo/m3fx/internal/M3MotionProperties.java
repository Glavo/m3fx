// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.function.Function;
import java.util.function.Predicate;

/// Creates the inheritable animation metadata shared by components with different JavaFX base classes.
@NotNullByDefault
public final class M3MotionProperties {
    /// Prevents instantiation.
    private M3MotionProperties() {
    }

    /// Creates metadata whose settable check does not need to instantiate the lazy property.
    ///
    /// @param property the component's property accessor
    /// @param settable whether CSS may assign the component's property
    /// @param <S> the component type
    /// @return inheritable boolean metadata with an initial value of `true`
    public static <S extends Styleable> CssMetaData<S, Boolean> cssMetaData(
            Function<S, StyleableBooleanProperty> property, Predicate<S> settable) {
        return new CssMetaData<>("-m3-animations-enabled", BooleanConverter.getInstance(), true, true) {
            /// Checks the component's binding without expanding its property.
            @Override
            public boolean isSettable(S styleable) {
                return settable.test(styleable);
            }

            /// Returns the property to which the CSS engine applies the resolved value.
            @Override
            public StyleableProperty<Boolean> getStyleableProperty(S styleable) {
                return property.apply(styleable);
            }
        };
    }
}
