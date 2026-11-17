// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import org.glavo.m3fx.localization.M3Localizer;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// A component's non-null localizer property, independent of scene-graph ancestry.
@NotNullByDefault
public final class M3LocalizerProperty extends SimpleObjectProperty<M3Localizer> {
    /// Creates a property initialized to the shared default localizer.
    ///
    /// @param bean the property owner
    /// @throws NullPointerException if `bean` is `null`
    public M3LocalizerProperty(Object bean) {
        super(Objects.requireNonNull(bean, "bean"), "localizer", M3Localizer.getDefault());
    }

    /// Returns the localizer, rejecting null values supplied by an invalid binding.
    @Override
    public M3Localizer get() {
        return Objects.requireNonNull(super.get(), "localizer");
    }

    /// Assigns a non-null localizer without changing the value when validation fails.
    @Override
    public void set(M3Localizer value) {
        super.set(Objects.requireNonNull(value, "localizer"));
    }

    /// Binds to a source whose current and subsequent values must be non-null.
    @Override
    public void bind(ObservableValue<? extends M3Localizer> observable) {
        Objects.requireNonNull(observable, "observable");
        Objects.requireNonNull(observable.getValue(), "localizer");
        super.bind(observable);
    }
}
