// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.beans.property.Property;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// Updates an existing text property's default until an observable application write or binding takes ownership.
///
/// This holder does not replace or bind the target. Same-value assignments that emit no invalidation cannot be
/// detected; an observed application change or binding permanently stops default updates, including after unbinding.
@NotNullByDefault
public final class M3LocalizedTextDefault {
    /// The actual text property displayed by the control.
    private final Property<String> target;

    /// Whether an application change or binding has taken ownership.
    private boolean overridden;

    /// The expected internal write, or null when no default is being assigned.
    private @Nullable String updatingValue;

    /// Observes ownership changes without creating a second text property.
    ///
    /// @param target the existing text property
    /// @throws NullPointerException if the target is null
    public M3LocalizedTextDefault(Property<String> target) {
        this.target = Objects.requireNonNull(target, "target");
        overridden = target.isBound();
        target.getValue();
        target.addListener(observable -> {
            // Revalidate even internal writes so the next application change emits an invalidation.
            @Nullable String value = target.getValue();
            if (updatingValue == null || !updatingValue.equals(value) || target.isBound()) {
                overridden = true;
            }
        });
    }

    /// Updates the actual target only while localization still owns its value.
    ///
    /// @param value the localized default text
    /// @throws NullPointerException if the value is null
    public void updateDefault(String value) {
        Objects.requireNonNull(value, "value");
        if (target.isBound()) {
            overridden = true;
        }
        if (!overridden) {
            @Nullable String previousValue = updatingValue;
            updatingValue = value;
            try {
                target.setValue(value);
            } finally {
                updatingValue = previousValue;
            }
        }
    }
}
