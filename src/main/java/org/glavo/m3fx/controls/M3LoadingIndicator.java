// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3LoadingIndicatorSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 loading indicator.
///
/// `M3LoadingIndicator` displays an indeterminate active shape for operations whose progress cannot be measured.
/// The default variant draws the active shape alone; [M3LoadingIndicatorVariant#CONTAINED] places it in a
/// contrasting container. The control does not accept or infer a progress value. Use [M3ProgressBar] or
/// [M3ProgressIndicator] when progress can be measured.
///
/// The indeterminate animation follows [org.glavo.m3fx.animation.M3MotionSettings]. When reduced motion is
/// requested, the control keeps a simpler rotating affordance so an indeterminate operation still communicates
/// activity without running the full morph sequence. See
/// [Material Design loading indicators](https://m3.material.io/components/loading-indicator/overview).
@NotNullByDefault
public final class M3LoadingIndicator extends Control {
    /// The base style class for M3FX loading indicators.
    public static final String STYLE_CLASS = "m3-loading-indicator";

    /// The pseudo class applied while the contained variant is selected.
    private static final PseudoClass CONTAINED_PSEUDO_CLASS =
            PseudoClass.getPseudoClass("contained");

    /// The default loading indicator container size.
    private static final double DEFAULT_CONTAINER_SIZE = 48.0;

    /// The default active indicator shape size.
    private static final double DEFAULT_INDICATOR_SIZE = 38.0;

    /// Creates a default loading indicator using the standard container and indicator sizes.
    public M3LoadingIndicator() {
        initialize();
    }

    /// The non-null visual variant used by this loading indicator.
    ///
    /// A `null` value written through the property is normalized to [M3LoadingIndicatorVariant#DEFAULT].
    ///
    /// @defaultValue [M3LoadingIndicatorVariant#DEFAULT]
    private @Nullable ObjectProperty<M3LoadingIndicatorVariant> variant;

    /// Returns the visual variant used by this loading indicator.
    ///
    /// @return the visual variant
    public final M3LoadingIndicatorVariant getVariant() {
        return variant == null ? M3LoadingIndicatorVariant.DEFAULT : variant.get();
    }

    /// Sets the visual variant used by this loading indicator.
    ///
    /// @param variant the visual variant
    /// @throws NullPointerException if `variant` is `null`
    public final void setVariant(M3LoadingIndicatorVariant variant) {
        variantProperty().set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the observable, bindable visual-variant property.
    ///
    /// The property defaults to [M3LoadingIndicatorVariant#DEFAULT]. A `null` value assigned directly through the
    /// property is replaced with that default.
    ///
    /// @return the visual-variant property
    public final ObjectProperty<M3LoadingIndicatorVariant> variantProperty() {
        if (variant == null) {
            variant = new SimpleObjectProperty<>(this, "variant", M3LoadingIndicatorVariant.DEFAULT) {
                /// Updates pseudo classes and redraws the skin when the variant changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3LoadingIndicatorVariant.DEFAULT);
                        return;
                    }
                    pseudoClassStateChanged(CONTAINED_PSEUDO_CLASS, get() == M3LoadingIndicatorVariant.CONTAINED);
                    requestLayout();
                }
            };
        }
        return variant;
    }

    /// The square container size, in logical pixels.
    ///
    /// The value must be finite and non-negative. It determines the control's minimum and preferred dimensions and
    /// is styleable through `-m3-container-size`.
    ///
    /// @defaultValue `48.0`
    private @Nullable StyleableDoubleProperty containerSize;

    /// Returns the loading indicator container size token.
    ///
    /// @return the container size in logical pixels
    public final double getContainerSize() {
        return containerSize == null ? DEFAULT_CONTAINER_SIZE : containerSize.get();
    }

    /// Sets the loading indicator container size token.
    ///
    /// @param containerSize the container size in logical pixels
    /// @throws IllegalArgumentException if `containerSize` is negative or not finite
    public final void setContainerSize(double containerSize) {
        containerSizeProperty().set(M3Css.nonNegative(containerSize, "containerSize"));
    }

    /// Returns the observable, bindable, styleable container-size property.
    ///
    /// The property defaults to `48.0` logical pixels and accepts only finite, non-negative values. CSS cannot set
    /// the property while it is bound.
    ///
    /// @return the container-size property
    public final StyleableDoubleProperty containerSizeProperty() {
        if (containerSize == null) {
            containerSize = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTAINER_SIZE,
                    this,
                    "containerSize",
                    StyleableProperties.CONTAINER_SIZE,
                    this::updateMetrics
            );
        }
        return containerSize;
    }

    /// The maximum active-shape size, in logical pixels.
    ///
    /// The value must be finite and non-negative and is styleable through `-m3-indicator-size`. Values larger than
    /// the container are permitted and may draw outside the nominal container bounds.
    ///
    /// @defaultValue `38.0`
    private @Nullable StyleableDoubleProperty indicatorSize;

    /// Returns the active indicator shape size token.
    ///
    /// @return the active indicator shape size in logical pixels
    public final double getIndicatorSize() {
        return indicatorSize == null ? DEFAULT_INDICATOR_SIZE : indicatorSize.get();
    }

    /// Sets the active indicator shape size token.
    ///
    /// @param indicatorSize the active indicator shape size in logical pixels
    /// @throws IllegalArgumentException if `indicatorSize` is negative or not finite
    public final void setIndicatorSize(double indicatorSize) {
        indicatorSizeProperty().set(M3Css.nonNegative(indicatorSize, "indicatorSize"));
    }

    /// Returns the observable, bindable, styleable active-shape size property.
    ///
    /// The property defaults to `38.0` logical pixels and accepts only finite, non-negative values. Values may
    /// exceed the container size, and CSS cannot set the property while it is bound.
    ///
    /// @return the active-shape size property
    public final StyleableDoubleProperty indicatorSizeProperty() {
        if (indicatorSize == null) {
            indicatorSize = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_INDICATOR_SIZE,
                    this,
                    "indicatorSize",
                    StyleableProperties.INDICATOR_SIZE,
                    this::requestLayout
            );
        }
        return indicatorSize;
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the CSS metadata for `M3LoadingIndicator`
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Creates the default loading indicator skin.
    ///
    /// @return the default loading indicator skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3LoadingIndicatorSkin(this);
    }

    /// Returns accessibility attributes for the active loading operation.
    ///
    /// @param attribute  the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` when no value is available
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        if (attribute == AccessibleAttribute.INDETERMINATE) {
            return true;
        }
        return super.queryAccessibleAttribute(attribute, parameters);
    }

    /// Returns the user-agent stylesheet for M3FX loading indicators.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("loading-indicator.css");
    }

    /// Adds base style classes and accessibility metadata.
    private void initialize() {
        M3ControlStyles.initialize(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PROGRESS_INDICATOR);
        setFocusTraversable(false);
        pseudoClassStateChanged(CONTAINED_PSEUDO_CLASS, false);
        updateMetrics();
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double size = getContainerSize();
        setMinSize(size, size);
        setPrefSize(size, size);
        setMaxSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
    }

    /// CSS metadata for M3FX loading indicator component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the loading indicator container size token.
        private static final CssMetaData<M3LoadingIndicator, Number> CONTAINER_SIZE =
                new CssMetaData<>("-m3-container-size", SizeConverter.getInstance(), DEFAULT_CONTAINER_SIZE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3LoadingIndicator control) {
                        return M3Css.isSettable(control.containerSizeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3LoadingIndicator control) {
                        return control.containerSizeProperty();
                    }
                };

        /// CSS metadata for the active indicator shape size token.
        private static final CssMetaData<M3LoadingIndicator, Number> INDICATOR_SIZE =
                new CssMetaData<>("-m3-indicator-size", SizeConverter.getInstance(), DEFAULT_INDICATOR_SIZE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3LoadingIndicator control) {
                        return M3Css.isSettable(control.indicatorSizeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3LoadingIndicator control) {
                        return control.indicatorSizeProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(CONTAINER_SIZE);
            styleables.add(INDICATOR_SIZE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
