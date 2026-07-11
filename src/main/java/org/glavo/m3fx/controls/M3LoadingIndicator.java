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
/// `M3LoadingIndicator` displays one active shape that continuously rotates and morphs between seven Material
/// shape states. It is intended for short operations whose progress cannot be measured. Use [M3ProgressBar] or
/// [M3ProgressIndicator] when determinate progress must be communicated.
///
/// The indeterminate animation follows [org.glavo.m3fx.animation.M3MotionSettings]. When full animations are
/// disabled, the control keeps a simpler rotating affordance so an indeterminate operation still communicates
/// activity without running the full morph sequence. See
/// [Material Design loading indicators](https://m3.material.io/components/loading-indicator/overview).
@NotNullByDefault
public class M3LoadingIndicator extends Control {
    /// The base style class for M3FX loading indicators.
    public static final String STYLE_CLASS = "m3-loading-indicator";

    /// The pseudo class applied while the contained variant is selected.
    private static final PseudoClass CONTAINED_PSEUDO_CLASS =
            PseudoClass.getPseudoClass("contained");

    /// The default loading indicator container size.
    private static final double DEFAULT_CONTAINER_SIZE = 48.0;

    /// The default active indicator shape size.
    private static final double DEFAULT_INDICATOR_SIZE = 38.0;

    // The styleable loading indicator container size token.
    private @Nullable StyleableDoubleProperty containerSize;

    // The styleable active indicator size token.
    private @Nullable StyleableDoubleProperty indicatorSize;

    // The visual variant used by this loading indicator.
    private @Nullable ObjectProperty<M3LoadingIndicatorVariant> variant;

    /// Creates a loading indicator.
    public M3LoadingIndicator() {
        initialize();
    }

    /// Returns the visual variant used by this loading indicator.
    ///
    /// @return the visual variant
    public final M3LoadingIndicatorVariant getVariant() {
        return variant == null ? M3LoadingIndicatorVariant.DEFAULT : variant.get();
    }

    /// Sets the visual variant used by this loading indicator.
    ///
    /// @param variant the visual variant
    public final void setVariant(M3LoadingIndicatorVariant variant) {
        variantProperty().set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the visual variant property.
    ///
    /// @return the writable visual variant property
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

    /// Returns the loading indicator container size token.
    ///
    /// @return the container size in pixels
    public final double getContainerSize() {
        return containerSize == null ? DEFAULT_CONTAINER_SIZE : containerSize.get();
    }

    /// Sets the loading indicator container size token.
    ///
    /// @param containerSize the container size in pixels
    public final void setContainerSize(double containerSize) {
        containerSizeProperty().set(M3Css.nonNegative(containerSize, "containerSize"));
    }

    /// Returns the loading indicator container size token property.
    ///
    /// @return the styleable loading indicator container size property
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

    /// Returns the active indicator shape size token.
    ///
    /// @return the active indicator shape size in pixels
    public final double getIndicatorSize() {
        return indicatorSize == null ? DEFAULT_INDICATOR_SIZE : indicatorSize.get();
    }

    /// Sets the active indicator shape size token.
    ///
    /// @param indicatorSize the active indicator shape size in pixels
    public final void setIndicatorSize(double indicatorSize) {
        indicatorSizeProperty().set(M3Css.nonNegative(indicatorSize, "indicatorSize"));
    }

    /// Returns the active indicator shape size token property.
    ///
    /// @return the styleable active indicator shape size property
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
    /// @param attribute the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` when no value is available
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case INDETERMINATE -> true;
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
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
