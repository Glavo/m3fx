// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.CssMetaData;
import javafx.css.SimpleStyleableBooleanProperty;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Skin;
import javafx.scene.control.TableRow;
import org.glavo.m3fx.animation.M3Animated;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3MotionProperties;
import org.glavo.m3fx.skins.M3TableRowSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

/// Renders one reusable Material row for an [M3TableView].
///
/// The row delegates cell creation, column layout, selection, editing, and virtualization to [TableRow]. Its
/// Material skin adds a row-wide state layer and bounded ripple beneath the inherited table cells. A table may
/// reuse one row for unrelated items while scrolling; application subclasses that retain item-dependent state must
/// therefore clear or replace that state whenever the inherited item changes.
///
/// @param <T> the row-item type
@NotNullByDefault
public class M3TableRow<T> extends TableRow<T> implements M3Animated {

    /// The default row style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-table-row";

    /// Creates an empty reusable Material table row.
    public M3TableRow() {
        M3ControlStyles.add(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TABLE_ROW);
    }

    /// The animation setting, or `null` until the property is first requested.
    private @Nullable StyleableBooleanProperty animationsEnabled;

    /// Returns whether this component permits optional animations.
    @Override
    public final boolean isAnimationsEnabled() {
        return animationsEnabled == null || animationsEnabled.get();
    }

    /// Sets whether this component permits optional animations.
    @Override
    public final void setAnimationsEnabled(boolean enabled) {
        animationsEnabledProperty().set(enabled);
    }

    /// Returns the bindable animation setting styled by `-m3-animations-enabled`.
    ///
    /// The default value is `true`; CSS inherits this setting from ancestor styles.
    @Override
    public final StyleableBooleanProperty animationsEnabledProperty() {
        if (animationsEnabled == null) {
            animationsEnabled = new SimpleStyleableBooleanProperty(
                    StyleableProperties.ANIMATIONS_ENABLED, this, "animationsEnabled", true);
        }
        return animationsEnabled;
    }

    /// Creates the Material table-row skin with bounded state-layer and ripple feedback.
    ///
    /// @return a new Material table-row skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3TableRowSkin<>(this);
    }

    /// Returns the immutable CSS metadata for this component class.
    public static @Unmodifiable List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this component.
    @Override
    public @Unmodifiable List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// CSS metadata for this component.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the inheritable animation setting.
        private static final CssMetaData<M3TableRow<?>, Boolean> ANIMATIONS_ENABLED =
                M3MotionProperties.cssMetaData(
                        c -> c.animationsEnabledProperty(),
                        c -> c.animationsEnabled == null || !c.animationsEnabled.isBound());

        /// The complete immutable CSS metadata list.
        private static final @Unmodifiable List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<>(TableRow.getClassCssMetaData());
            styleables.add(ANIMATIONS_ENABLED);
            STYLEABLES = List.copyOf(styleables);
        }
    }
}
