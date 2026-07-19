// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Identifies one internal map entry by object identity while retaining a diagnostic name.
///
/// Two keys remain distinct even when they carry the same name. This class deliberately inherits
/// [Object#equals(Object)] and [Object#hashCode()] so property-map ownership cannot collide through value equality.
/// The name is exposed only through [#toString()] for diagnostics.
@NotNullByDefault
public final class IdentityKey {
    /// The human-readable name reported by [#toString()].
    private final String name;

    /// Creates an identity key with a diagnostic name.
    ///
    /// @param name the non-null name reported for diagnostics
    /// @throws NullPointerException if `name` is `null`
    public IdentityKey(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    /// Returns this key's diagnostic name.
    ///
    /// @return the name supplied at construction
    @Override
    public String toString() {
        return name;
    }
}
