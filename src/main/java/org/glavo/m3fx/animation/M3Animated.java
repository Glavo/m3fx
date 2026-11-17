// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.animation;

import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import org.jetbrains.annotations.NotNullByDefault;

/// A component whose optional motion can be configured through JavaFX properties and CSS.
///
/// The `-m3-animations-enabled` CSS property defaults to `true` and inherits from ancestor styles. CSS changes
/// take effect during CSS processing, whereas changes to the JavaFX property take effect synchronously.
/// A component's own style or property binding follows normal JavaFX CSS precedence. Binding the property
/// prevents CSS from assigning it. A Java setter does not establish a binding to descendant components.
///
/// Disabling optional motion settles finite transitions at their target state. Indeterminate activity indicators
/// may retain simple movement to communicate ongoing work. The application-wide reduced-motion request in
/// [M3MotionSettings] takes precedence without changing this property's value.
/// See [Material motion](https://m3.material.io/styles/motion/overview).
@NotNullByDefault
public interface M3Animated extends Styleable {
    /// Returns whether this component permits optional animations, before the global reduced-motion constraint.
    ///
    /// @return the component's animation setting
    boolean isAnimationsEnabled();

    /// Sets whether this component permits optional animations.
    ///
    /// @param enabled whether optional animations are enabled
    /// @throws RuntimeException if the property is unidirectionally bound
    void setAnimationsEnabled(boolean enabled);

    /// Returns the observable, bindable animation setting.
    ///
    /// @return the styleable property corresponding to `-m3-animations-enabled`
    /// @defaultValue `true`
    StyleableBooleanProperty animationsEnabledProperty();
}
