// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.SimpleStyleableBooleanProperty;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.event.ActionEvent;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Skin;
import org.glavo.m3fx.animation.M3Animated;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3MotionProperties;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3BreadcrumbItemSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/// Represents one navigable level in an [M3Breadcrumbs] hierarchy.
///
/// Activating an enabled item by pointer, keyboard, accessibility action, or [#fire()] emits an [ActionEvent]. The
/// containing breadcrumbs control marks its final item as [current][#currentProperty()] and updates that state when
/// the ordered item list changes. Long labels use single-line end ellipsis; the installed tooltip retains the full
/// text for pointer and keyboard users.
@NotNullByDefault
public final class M3BreadcrumbItem extends ButtonBase implements M3Animated {

    /// The default root style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-breadcrumb-item";

    /// The pseudo-class applied while this item represents the current location.
    private static final PseudoClass CURRENT_PSEUDO_CLASS = PseudoClass.getPseudoClass("current");

    /// The tooltip that preserves access to the complete item text.
    private final M3Tooltip fullTextTooltip = new M3Tooltip();

    /// Creates an empty breadcrumb item.
    public M3BreadcrumbItem() {
        this("");
    }

    /// Creates a breadcrumb item with the specified label.
    ///
    /// @param text the hierarchy-level label
    public M3BreadcrumbItem(String text) {
        super(text);
        initialize();
    }

    /// Whether this item represents the final, current location.
    ///
    /// The containing [M3Breadcrumbs] owns this state. An item outside a breadcrumbs control is not current.
    ///
    /// @defaultValue `false`
    private final ReadOnlyBooleanWrapper current = new ReadOnlyBooleanWrapper(this, "current") {
        /// Updates current-location styling and accessibility after the state changes.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(CURRENT_PSEUDO_CLASS, get());
            notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED);
        }
    };

    /// Returns whether this item represents the current location.
    ///
    /// @return `true` when this is the containing breadcrumbs control's final item
    public boolean isCurrent() {
        return current.get();
    }

    /// Returns the read-only current-location property.
    ///
    /// @return the current-location property
    public ReadOnlyBooleanProperty currentProperty() {
        return current.getReadOnlyProperty();
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

    /// Updates the owner-managed current-location state.
    ///
    /// @param current whether this item is the current location
    void setCurrent(boolean current) {
        this.current.set(current);
    }

    /// Fires this item's action event unless it is disabled.
    @Override
    public void fire() {
        if (!isDisabled()) {
            fireEvent(new ActionEvent(this, this));
        }
    }

    /// Returns accessibility attributes for the current-location state.
    ///
    /// @param attribute the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested value, or the inherited value when this item does not define it
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case SELECTED -> isCurrent();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Creates the default breadcrumb-item skin.
    ///
    /// @return the default breadcrumb-item skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3BreadcrumbItemSkin(this);
    }

    /// Returns the user-agent stylesheet for breadcrumbs.
    ///
    /// @return the breadcrumbs stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("breadcrumbs.css");
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

    /// Initializes style, interaction, accessibility, and full-text tooltip behavior.
    private void initialize() {
        fullTextTooltip.animationsEnabledProperty().bind(animationsEnabledProperty());
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.HYPERLINK);
        setFocusTraversable(true);
        setMnemonicParsing(false);
        setPickOnBounds(true);
        setTextOverrun(OverrunStyle.ELLIPSIS);
        setWrapText(false);
        fullTextTooltip.textProperty().bind(textProperty());
        M3Tooltip.install(this, fullTextTooltip);
        pseudoClassStateChanged(CURRENT_PSEUDO_CLASS, false);
    }

    /// CSS metadata for this component.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the inheritable animation setting.
        private static final CssMetaData<M3BreadcrumbItem, Boolean> ANIMATIONS_ENABLED =
                M3MotionProperties.cssMetaData(
                        c -> c.animationsEnabledProperty(),
                        c -> c.animationsEnabled == null || !c.animationsEnabled.isBound());

        /// The complete immutable CSS metadata list.
        private static final @Unmodifiable List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<>(ButtonBase.getClassCssMetaData());
            styleables.add(ANIMATIONS_ENABLED);
            STYLEABLES = List.copyOf(styleables);
        }
    }
}
