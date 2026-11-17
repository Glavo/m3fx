// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.localization;

import javafx.beans.property.ObjectProperty;
import org.jetbrains.annotations.NotNullByDefault;

/// A component with configurable built-in text and regional formatting.
///
/// The localizer belongs to this component; parent nodes and presentation hosts do not supply it.
/// Applications can bind several components to one observable localizer to change their language together.
/// Application-supplied text and formatters retain precedence over localized defaults.
@NotNullByDefault
public interface M3Localized {
    /// Returns the localizer supplying this component's built-in defaults.
    ///
    /// @return the non-null localizer
    M3Localizer getLocalizer();

    /// Sets the localizer and refreshes built-in defaults.
    ///
    /// @param localizer the non-null replacement localizer
    /// @throws NullPointerException if `localizer` is `null`
    /// @throws RuntimeException if the property is bound
    void setLocalizer(M3Localizer localizer);

    /// Returns the observable, bindable localizer property.
    ///
    /// Assignments and binding sources must supply non-null values. Replacing the instance refreshes localized
    /// defaults; mutating translation resources behind an existing instance does not send a notification.
    /// Unbinding retains the current value. Assign [M3Localizer#getDefault()] to restore the default localizer.
    /// Changes to live controls must be made on the JavaFX Application Thread.
    ///
    /// @return this component's localizer property
    /// @defaultValue [M3Localizer#getDefault()]
    ObjectProperty<M3Localizer> localizerProperty();
}
