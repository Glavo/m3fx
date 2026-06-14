// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
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
/// `M3LoadingIndicator` displays one active shape that morphs as loading advances. Indeterminate indicators
/// continuously rotate and morph between shape states, while determinate indicators use the progress value
/// from `0.0` to `1.0` to choose the displayed shape. It is intended for loading affordances where Material
/// Design 3 Expressive uses a dedicated loading indicator instead of a circular progress indicator. See
/// [Material Design loading indicators](https://m3.material.io/components/loading-indicator/overview).
@NotNullByDefault
public class M3LoadingIndicator extends Control {
    /// The base style class for m3fx loading indicators.
    public static final String STYLE_CLASS = "m3-loading-indicator";

    /// The progress value that marks the control as indeterminate.
    public static final double INDETERMINATE_PROGRESS = -1.0;

    /// The pseudo class applied while progress is indeterminate.
    private static final PseudoClass INDETERMINATE_PSEUDO_CLASS =
            PseudoClass.getPseudoClass("indeterminate");

    /// The pseudo class applied while the contained variant is selected.
    private static final PseudoClass CONTAINED_PSEUDO_CLASS =
            PseudoClass.getPseudoClass("contained");

    /// The default loading indicator container size.
    private static final double DEFAULT_CONTAINER_SIZE = 48.0;

    /// The default active indicator shape size.
    private static final double DEFAULT_INDICATOR_SIZE = 36.0;

    /// The minimum accessible progress value.
    private static final double ACCESSIBLE_MIN_VALUE = 0.0;

    /// The maximum accessible progress value.
    private static final double ACCESSIBLE_MAX_VALUE = 1.0;

    /// The optional accessible value-string attribute available on newer JavaFX runtimes.
    private static final @Nullable AccessibleAttribute VALUE_STRING_ATTRIBUTE =
            M3Accessible.attribute("VALUE_STRING");

    /// The current progress value.
    private @Nullable DoubleProperty progress;

    /// The styleable loading indicator container size token.
    private @Nullable StyleableDoubleProperty containerSize;

    /// The styleable active indicator size token.
    private @Nullable StyleableDoubleProperty indicatorSize;

    /// The visual variant used by this loading indicator.
    private @Nullable ObjectProperty<M3LoadingIndicatorVariant> variant;

    /// Creates an indeterminate loading indicator.
    public M3LoadingIndicator() {
        initialize();
    }

    /// Creates a loading indicator with an initial progress value.
    ///
    /// @param progress the initial progress value, from `0.0` to `1.0`, or [INDETERMINATE_PROGRESS]
    public M3LoadingIndicator(double progress) {
        initialize();
        setProgress(progress);
    }

    /// Returns the current progress value.
    ///
    /// @return the current progress value, or [INDETERMINATE_PROGRESS]
    public final double getProgress() {
        return progress == null ? INDETERMINATE_PROGRESS : progress.get();
    }

    /// Sets the current progress value.
    ///
    /// @param progress the progress value, from `0.0` to `1.0`, or [INDETERMINATE_PROGRESS]
    public final void setProgress(double progress) {
        progressProperty().set(progress);
    }

    /// Returns the current progress value property.
    public final DoubleProperty progressProperty() {
        if (progress == null) {
            progress = new DoublePropertyBase(INDETERMINATE_PROGRESS) {
                /// Normalizes progress and updates the indeterminate pseudo class.
                @Override
                protected void invalidated() {
                    double normalizedProgress = normalizeProgress(get());
                    if (Double.compare(normalizedProgress, get()) != 0) {
                        set(normalizedProgress);
                        return;
                    }
                    pseudoClassStateChanged(INDETERMINATE_PSEUDO_CLASS, isIndeterminate());
                    notifyAccessibleAttributeChanged(AccessibleAttribute.VALUE);
                    M3Accessible.notifyAttribute(M3LoadingIndicator.this, VALUE_STRING_ATTRIBUTE);
                    notifyAccessibleAttributeChanged(AccessibleAttribute.INDETERMINATE);
                    requestLayout();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3LoadingIndicator.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "progress";
                }
            };
        }
        return progress;
    }

    /// Returns whether the current progress value is indeterminate.
    ///
    /// @return `true` when the current progress value is indeterminate
    public final boolean isIndeterminate() {
        return getProgress() == INDETERMINATE_PROGRESS;
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

    /// Returns accessibility attributes for the loading progress value.
    ///
    /// @param attribute the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` when no value is available
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        if (attribute == VALUE_STRING_ATTRIBUTE) {
            return accessibleValueString();
        }
        return switch (attribute) {
            case INDETERMINATE -> isIndeterminate();
            case MIN_VALUE -> ACCESSIBLE_MIN_VALUE;
            case MAX_VALUE -> ACCESSIBLE_MAX_VALUE;
            case VALUE -> getProgress();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Returns the user-agent stylesheet for m3fx loading indicators.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("loading-indicator.css");
    }

    /// Adds base style classes and accessibility metadata.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PROGRESS_INDICATOR);
        pseudoClassStateChanged(INDETERMINATE_PSEUDO_CLASS, true);
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

    /// Normalizes progress values to the supported range.
    private static double normalizeProgress(double progress) {
        if (Double.isNaN(progress) || progress < 0.0) {
            return INDETERMINATE_PROGRESS;
        }
        return Math.min(1.0, progress);
    }

    /// Returns the accessible string representation of the current progress.
    private String accessibleValueString() {
        return isIndeterminate() ? "Indeterminate" : Math.round(getProgress() * 100.0) + "%";
    }

    /// CSS metadata for m3fx loading indicator component tokens.
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
