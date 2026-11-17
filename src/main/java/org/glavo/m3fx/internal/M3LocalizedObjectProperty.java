// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// A non-null object property whose localized default yields to application assignments and bindings.
///
/// An explicit assignment owns the value even when it equals the current default. Unbinding retains application
/// ownership and the last bound value. Binding sources must supply non-null values; a null bound value fails when
/// read. Owners refresh defaults through [#updateDefault(Object)]; this property does not observe localization.
///
/// @param <T> the non-null value type
@NotNullByDefault
public class M3LocalizedObjectProperty<T> extends SimpleObjectProperty<T> {
    /// Whether an application assignment or binding has taken ownership of the value.
    private boolean overridden;

    /// Creates a property with an initial localized default.
    ///
    /// @param bean the owning object
    /// @param name the property name
    /// @param defaultValue the initial localized value
    /// @throws NullPointerException if any argument is null
    public M3LocalizedObjectProperty(Object bean, String name, T defaultValue) {
        super(Objects.requireNonNull(bean, "bean"), Objects.requireNonNull(name, "name"),
                Objects.requireNonNull(defaultValue, "defaultValue"));
    }

    /// Returns the current value, rejecting null values from a misbehaving binding source.
    @Override
    public final T get() {
        return Objects.requireNonNull(super.get(), getName());
    }

    /// Assigns an application value, preserving ownership even when the value is unchanged.
    ///
    /// @throws NullPointerException if the value is null
    /// @throws RuntimeException if this property is bound; ownership remains unchanged
    @Override
    public final void set(T value) {
        Objects.requireNonNull(value, getName());
        if (isBound()) {
            throw new RuntimeException("A bound value cannot be set.");
        }
        overridden = true;
        super.set(value);
    }

    /// Gives a non-null binding source ownership, including after the binding is removed.
    ///
    /// @throws NullPointerException if the source or its current value is null
    @Override
    public final void bind(ObservableValue<? extends T> observable) {
        Objects.requireNonNull(observable, "observable");
        Objects.requireNonNull(observable.getValue(), getName());
        overridden = true;
        super.bind(observable);
    }

    /// Refreshes the localized value only while application code has not assigned or bound this property.
    ///
    /// @param defaultValue the replacement localized default
    /// @throws NullPointerException if the default is null, even when an override is active
    public final void updateDefault(T defaultValue) {
        Objects.requireNonNull(defaultValue, "defaultValue");
        if (!overridden && !isBound()) {
            super.set(defaultValue);
        }
    }
}
