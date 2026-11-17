// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.css.SimpleStyleableBooleanProperty;
import javafx.css.StyleableBooleanProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.scene.AccessibleRole;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.glavo.m3fx.animation.M3Animated;
import org.glavo.m3fx.internal.M3MotionProperties;
import org.glavo.m3fx.internal.M3TextInputSupport;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 single-line password field.
///
/// The field follows the editing, selection, clipboard, undo, and action-event contracts of JavaFX's
/// [PasswordField] while exposing Material state and token APIs through [M3TextInput]. Its text is masked for
/// display but remains available through the inherited text property. Use it directly for compact password entry,
/// or wrap it in [M3TextInputLayout] to add a label, supporting text, validation messages, or a trailing visibility
/// action.
///
/// Computed minimum and preferred total heights, including insets, equal the container-height token before and
/// after skin installation. Explicit heights and bindings remain in effect through geometry changes; set a
/// height to [#USE_COMPUTED_SIZE] to restore computed sizing.
///
/// See [Material Design text fields](https://m3.material.io/components/text-fields/overview).
@NotNullByDefault
public final class M3PasswordField extends PasswordField implements M3Animated, M3TextInput {

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

    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-password-field";

    /// The immutable defaults and CSS metadata shared by instances of this class.
    private static final M3TextInputSupport.Metadata<M3PasswordField> METADATA = new M3TextInputSupport.Metadata<>(
            PasswordField.getClassCssMetaData(),
            M3TextInputSupport.DEFAULT_FIELD_CONTAINER_HEIGHT,
            M3TextInputSupport.DEFAULT_FIELD_VERTICAL_PADDING
    );

    /// Shared Material text input state and token plumbing.
    private final M3TextInputSupport<M3PasswordField> support = new M3TextInputSupport<>(this, METADATA);

    /// Creates an empty password field using the filled variant.
    public M3PasswordField() {
        initialize();
    }

    /// Creates a password field with the supplied initial text and the filled variant.
    ///
    /// @param text the initial field text
    /// @throws NullPointerException if `text` is `null`
    public M3PasswordField(String text) {
        setText(Objects.requireNonNull(text, "text"));
        initialize();
    }

    /// Returns the Material text-input variant.
    ///
    /// @return the current variant
    @Override
    public final M3TextInputVariant getVariant() {
        return support.getVariant();
    }

    /// Sets the Material text-input variant.
    ///
    /// @param variant the variant to use
    /// @throws NullPointerException if `variant` is `null`
    @Override
    public final void setVariant(M3TextInputVariant variant) {
        support.setVariant(variant);
    }

    /// Returns the observable, bindable text-input variant property.
    ///
    /// The property is [M3TextInputVariant#FILLED] by default. A direct `null` assignment restores that default;
    /// changes update the control's variant style classes.
    ///
    /// @return the text-input variant property
    @Override
    public final ObjectProperty<M3TextInputVariant> variantProperty() {
        return support.variantProperty();
    }

    /// Returns whether this field renders its error state.
    ///
    /// @return `true` when the error state is rendered
    @Override
    public final boolean isError() {
        return support.isError();
    }

    /// Sets whether this field renders its error state.
    ///
    /// @param error whether to render the error state
    @Override
    public final void setError(boolean error) {
        support.setError(error);
    }

    /// Returns the observable, bindable error-state property.
    ///
    /// The property is `false` by default. Changes update the error pseudo-class.
    ///
    /// @return the error-state property
    @Override
    public final BooleanProperty errorProperty() {
        return support.errorProperty();
    }

    /// Returns the preferred container height.
    ///
    /// @return the preferred height in logical pixels
    @Override
    public final double getContainerHeight() {
        return support.getContainerHeight();
    }

    /// Sets the preferred container height.
    ///
    /// @param containerHeight the finite, non-negative height in logical pixels
    /// @throws IllegalArgumentException if `containerHeight` is negative or not finite
    @Override
    public final void setContainerHeight(double containerHeight) {
        support.setContainerHeight(containerHeight);
    }

    /// Returns the observable, bindable, CSS-styleable preferred-height property.
    ///
    /// The property is `56.0` logical pixels by default, accepts only finite non-negative values, and is styleable
    /// through `-m3-container-height`. Changes update unbound minimum and preferred heights.
    ///
    /// @return the preferred-height property
    @Override
    public final StyleableDoubleProperty containerHeightProperty() {
        return support.containerHeightProperty();
    }

    /// Returns the container corner radius.
    ///
    /// @return the corner radius in logical pixels
    @Override
    public final double getContainerShape() {
        return support.getContainerShape();
    }

    /// Sets the container corner radius.
    ///
    /// @param containerShape the finite, non-negative corner radius in logical pixels
    /// @throws IllegalArgumentException if `containerShape` is negative or not finite
    @Override
    public final void setContainerShape(double containerShape) {
        support.setContainerShape(containerShape);
    }

    /// Returns the observable, bindable, CSS-styleable container-shape property.
    ///
    /// The property is `4.0` logical pixels by default, accepts only finite non-negative values, and is styleable
    /// through `-m3-container-shape`.
    ///
    /// @return the container-shape property
    @Override
    public final StyleableDoubleProperty containerShapeProperty() {
        return support.containerShapeProperty();
    }

    /// Returns the horizontal content padding.
    ///
    /// @return the horizontal padding in logical pixels
    @Override
    public final double getHorizontalPadding() {
        return support.getHorizontalPadding();
    }

    /// Sets the horizontal content padding.
    ///
    /// @param horizontalPadding the finite, non-negative padding in logical pixels
    /// @throws IllegalArgumentException if `horizontalPadding` is negative or not finite
    @Override
    public final void setHorizontalPadding(double horizontalPadding) {
        support.setHorizontalPadding(horizontalPadding);
    }

    /// Returns the observable, bindable, CSS-styleable horizontal-padding property.
    ///
    /// The property is `16.0` logical pixels by default, accepts only finite non-negative values, and is styleable
    /// through `-m3-horizontal-padding`. Changes update unbound control padding.
    ///
    /// @return the horizontal-padding property
    @Override
    public final StyleableDoubleProperty horizontalPaddingProperty() {
        return support.horizontalPaddingProperty();
    }

    /// Returns the vertical content padding.
    ///
    /// @return the vertical padding in logical pixels
    @Override
    public final double getVerticalPadding() {
        return support.getVerticalPadding();
    }

    /// Sets the vertical content padding.
    ///
    /// @param verticalPadding the finite, non-negative padding in logical pixels
    /// @throws IllegalArgumentException if `verticalPadding` is negative or not finite
    @Override
    public final void setVerticalPadding(double verticalPadding) {
        support.setVerticalPadding(verticalPadding);
    }

    /// Returns the observable, bindable, CSS-styleable vertical-padding property.
    ///
    /// The property is `8.0` logical pixels by default, accepts only finite non-negative values, and is styleable
    /// through `-m3-vertical-padding`. Changes update unbound control padding.
    ///
    /// @return the vertical-padding property
    @Override
    public final StyleableDoubleProperty verticalPaddingProperty() {
        return support.verticalPaddingProperty();
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the CSS metadata for `M3PasswordField`
    public static @Unmodifiable List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    ///
    /// @return the immutable CSS metadata list for this control
    @Override
    public @Unmodifiable List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Returns the user-agent stylesheet for M3FX text input controls.
    ///
    /// @return the user-agent stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("text-field.css");
    }

    /// Computes minimum total height from the container-height token.
    @Override
    protected double computeMinHeight(double width) {
        return getContainerHeight();
    }

    /// Computes preferred total height from the container-height token.
    @Override
    protected double computePrefHeight(double width) {
        return getContainerHeight();
    }

    /// Adds base style classes and applies the default variant.
    private void initialize() {
        support.initialize(DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PASSWORD_FIELD);
    }

    /// CSS metadata for this component.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the inheritable animation setting.
        private static final CssMetaData<M3PasswordField, Boolean> ANIMATIONS_ENABLED =
                M3MotionProperties.cssMetaData(
                        c -> c.animationsEnabledProperty(),
                        c -> c.animationsEnabled == null || !c.animationsEnabled.isBound());

        /// The complete immutable CSS metadata list.
        private static final @Unmodifiable List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<>(METADATA.styleables());
            styleables.add(ANIMATIONS_ENABLED);
            STYLEABLES = List.copyOf(styleables);
        }
    }
}
