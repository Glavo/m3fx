// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import org.jetbrains.annotations.NotNullByDefault;

import java.util.function.IntToDoubleFunction;

/// Converts values to and from a fixed-size vector of scalar animation components.
///
/// A converter defines a stable component count and ordering for one value type. [M3StateTransition] reads the
/// components of start and target values when a run is configured, animates those components through reusable
/// scalar channels, and calls [#createValue(IntToDoubleFunction)] to reconstruct each rendered value.
/// Implementations should make component access inexpensive and should avoid temporary collections or arrays.
///
/// Values returned from [#getComponent(Object, int)] and visibility thresholds must be finite. A converter must
/// return a non-null value from [#createValue(IntToDoubleFunction)]. If the represented type has a restricted domain,
/// such as color components or non-negative dimensions, `createValue` must account for spring overshoot before
/// invoking that type's constructor.
///
/// A converter may be shared by any number of transitions and must not store per-transition mutable state. The
/// component accessor passed to `createValue` is a transient view over reusable animation storage; a converter must
/// read it only during that invocation and must not retain or publish it.
///
/// @param <T> the non-null value type
@NotNullByDefault
public interface M3VectorConverter<T> {
    /// Returns the fixed number of scalar components used by this converter.
    ///
    /// The result must be greater than zero and must remain constant for the lifetime of this converter.
    ///
    /// @return the positive component count
    int getComponentCount();

    /// Returns one scalar component from a value.
    ///
    /// This method is called when a channel is registered or retargeted, not on every animation pulse.
    ///
    /// @param value the non-null value to decompose
    /// @param index the zero-based component index
    /// @return the finite component value
    /// @throws IndexOutOfBoundsException if `index` is outside the range defined by [#getComponentCount()]
    double getComponent(T value, int index);

    /// Reconstructs a value from current scalar components.
    ///
    /// Valid component indices range from zero to [#getComponentCount()] minus one. The accessor is backed by
    /// mutable transition storage and must not be retained after this method returns.
    ///
    /// @param components the transient component accessor
    /// @return the non-null reconstructed value
    T createValue(IntToDoubleFunction components);

    /// Returns the spring visibility threshold for one component.
    ///
    /// The threshold is expressed in the component's units and determines when a physical spring is considered
    /// visually settled. It does not clamp or round the exact final value. The default is `0.001`.
    ///
    /// @param index the zero-based component index
    /// @return the finite threshold greater than zero
    /// @throws IndexOutOfBoundsException if `index` is outside the range defined by [#getComponentCount()]
    default double getVisibilityThreshold(int index) {
        if (index < 0 || index >= getComponentCount()) {
            throw new IndexOutOfBoundsException(index);
        }
        return 0.001;
    }
}
