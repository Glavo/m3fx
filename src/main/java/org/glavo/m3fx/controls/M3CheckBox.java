// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3CheckBoxSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/// A Material Design 3 checkbox for an independent binary or tri-state option.
///
/// The [selected][#selectedProperty()] and [indeterminate][#indeterminateProperty()] properties represent its
/// state. They are independently writable, although an indeterminate state takes precedence in accessibility
/// reporting. With [allowIndeterminate][#allowIndeterminateProperty()] disabled, activation toggles selected and
/// clears indeterminate. With it enabled, activation cycles unchecked, indeterminate, checked, and unchecked,
/// emitting one [ActionEvent] after each state transition.
///
/// The error property changes presentation only; it does not perform validation or change selection. New checkboxes
/// are unchecked, determinate, allow two-state user interaction, and are focus traversable with mnemonic parsing.
/// Use checkboxes for independent choices or bulk-selection affordances. See
/// [Material Design checkboxes](https://m3.material.io/components/checkbox/overview).
@NotNullByDefault
public final class M3CheckBox extends ButtonBase {
    /// The base style class for M3FX checkboxes.
    public static final String STYLE_CLASS = "m3-checkbox";

    /// The selected pseudo-class used by checkboxes.
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    /// The determinate pseudo-class used by checkboxes.
    private static final PseudoClass DETERMINATE_PSEUDO_CLASS = PseudoClass.getPseudoClass("determinate");

    /// The indeterminate pseudo-class used by checkboxes.
    private static final PseudoClass INDETERMINATE_PSEUDO_CLASS = PseudoClass.getPseudoClass("indeterminate");

    /// The error pseudo-class used by checkboxes.
    private static final PseudoClass ERROR_PSEUDO_CLASS = PseudoClass.getPseudoClass("error");

    /// The default checkbox touch target size.
    private static final double DEFAULT_TOUCH_TARGET_SIZE = 48.0;

    /// The default checkbox state layer size.
    private static final double DEFAULT_STATE_LAYER_SIZE = 40.0;

    /// The default checkbox container size.
    private static final double DEFAULT_CONTAINER_SIZE = 18.0;

    /// The default selected mark width.
    private static final double DEFAULT_SELECTED_MARK_WIDTH = 12.0;

    /// The default selected mark height.
    private static final double DEFAULT_SELECTED_MARK_HEIGHT = 10.0;

    /// The default indeterminate mark width.
    private static final double DEFAULT_INDETERMINATE_MARK_WIDTH = 12.0;

    /// The default indeterminate mark height.
    private static final double DEFAULT_INDETERMINATE_MARK_HEIGHT = 2.0;

    /// Creates an unchecked, determinate checkbox with empty text.
    public M3CheckBox() {
        initialize();
    }

    /// Creates an unchecked, determinate checkbox with the specified text.
    ///
    /// @param text the text displayed by the checkbox
    public M3CheckBox(String text) {
        super(text);
        initialize();
    }

    /// The preferred square pointer target size, in logical pixels.
    ///
    /// The default value is `48.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `48.0`
    private @Nullable StyleableDoubleProperty touchTargetSize;

    /// Returns the preferred touch target size token.
    ///
    /// @return the preferred touch target size in logical pixels
    public final double getTouchTargetSize() {
        return touchTargetSize == null ? DEFAULT_TOUCH_TARGET_SIZE : touchTargetSize.get();
    }

    /// Sets the preferred touch target size token.
    ///
    /// @param touchTargetSize the preferred touch target size in logical pixels
    /// @throws IllegalArgumentException if `touchTargetSize` is negative or not finite
    public final void setTouchTargetSize(double touchTargetSize) {
        touchTargetSizeProperty().set(M3Css.nonNegative(touchTargetSize, "touchTargetSize"));
    }

    /// Returns the styleable property that stores the preferred touch target size.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-touch-target-size`, and accepts finite,
    /// non-negative values. Its default value is `48.0` logical pixels.
    ///
    /// @return the touch target size property
    public final StyleableDoubleProperty touchTargetSizeProperty() {
        if (touchTargetSize == null) {
            touchTargetSize = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_TOUCH_TARGET_SIZE,
                    this,
                    "touchTargetSize",
                    StyleableProperties.TOUCH_TARGET_SIZE,
                    this::updateMetrics
            );
        }
        return touchTargetSize;
    }

    /// The square interaction-state layer size, in logical pixels.
    ///
    /// The default value is `40.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `40.0`
    private @Nullable StyleableDoubleProperty stateLayerSize;

    /// Returns the bounded indicator state layer size token.
    ///
    /// @return the state layer size in logical pixels
    public final double getStateLayerSize() {
        return stateLayerSize == null ? DEFAULT_STATE_LAYER_SIZE : stateLayerSize.get();
    }

    /// Sets the bounded indicator state layer size token.
    ///
    /// @param stateLayerSize the state layer size in logical pixels
    /// @throws IllegalArgumentException if `stateLayerSize` is negative or not finite
    public final void setStateLayerSize(double stateLayerSize) {
        stateLayerSizeProperty().set(M3Css.nonNegative(stateLayerSize, "stateLayerSize"));
    }

    /// Returns the styleable property that stores the interaction-state layer size.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-state-layer-size`, and accepts finite,
    /// non-negative values. Its default value is `40.0` logical pixels.
    ///
    /// @return the state layer size property
    public final StyleableDoubleProperty stateLayerSizeProperty() {
        if (stateLayerSize == null) {
            stateLayerSize = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_STATE_LAYER_SIZE,
                    this,
                    "stateLayerSize",
                    StyleableProperties.STATE_LAYER_SIZE,
                    this::updateMetrics
            );
        }
        return stateLayerSize;
    }

    /// The width and height of the checkbox indicator, in logical pixels.
    ///
    /// The default value is `18.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `18.0`
    private @Nullable StyleableDoubleProperty containerSize;

    /// Returns the checkbox container size token.
    ///
    /// @return the checkbox container size in logical pixels
    public final double getContainerSize() {
        return containerSize == null ? DEFAULT_CONTAINER_SIZE : containerSize.get();
    }

    /// Sets the checkbox container size token.
    ///
    /// @param containerSize the checkbox container size in logical pixels
    /// @throws IllegalArgumentException if `containerSize` is negative or not finite
    public final void setContainerSize(double containerSize) {
        containerSizeProperty().set(M3Css.nonNegative(containerSize, "containerSize"));
    }

    /// Returns the styleable property that stores the checkbox indicator size.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-container-size`, and accepts finite,
    /// non-negative values. Its default value is `18.0` logical pixels.
    ///
    /// @return the indicator size property
    public final StyleableDoubleProperty containerSizeProperty() {
        if (containerSize == null) {
            containerSize = createSizeProperty(
                    DEFAULT_CONTAINER_SIZE,
                    "containerSize",
                    StyleableProperties.CONTAINER_SIZE
            );
        }
        return containerSize;
    }

    /// The width of the selected check mark, in logical pixels.
    ///
    /// The default value is `12.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `12.0`
    private @Nullable StyleableDoubleProperty selectedMarkWidth;

    /// Returns the selected check mark width token.
    ///
    /// @return the selected check mark width in logical pixels
    public final double getSelectedMarkWidth() {
        return selectedMarkWidth == null ? DEFAULT_SELECTED_MARK_WIDTH : selectedMarkWidth.get();
    }

    /// Sets the selected check mark width token.
    ///
    /// @param selectedMarkWidth the selected check mark width in logical pixels
    /// @throws IllegalArgumentException if `selectedMarkWidth` is negative or not finite
    public final void setSelectedMarkWidth(double selectedMarkWidth) {
        selectedMarkWidthProperty().set(M3Css.nonNegative(selectedMarkWidth, "selectedMarkWidth"));
    }

    /// Returns the styleable property that stores the selected mark width.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-selected-mark-width`, and accepts finite,
    /// non-negative values. Its default value is `12.0` logical pixels.
    ///
    /// @return the selected mark width property
    public final StyleableDoubleProperty selectedMarkWidthProperty() {
        if (selectedMarkWidth == null) {
            selectedMarkWidth = createSizeProperty(
                    DEFAULT_SELECTED_MARK_WIDTH,
                    "selectedMarkWidth",
                    StyleableProperties.SELECTED_MARK_WIDTH
            );
        }
        return selectedMarkWidth;
    }

    /// The height of the selected check mark, in logical pixels.
    ///
    /// The default value is `10.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `10.0`
    private @Nullable StyleableDoubleProperty selectedMarkHeight;

    /// Returns the selected check mark height token.
    ///
    /// @return the selected check mark height in logical pixels
    public final double getSelectedMarkHeight() {
        return selectedMarkHeight == null ? DEFAULT_SELECTED_MARK_HEIGHT : selectedMarkHeight.get();
    }

    /// Sets the selected check mark height token.
    ///
    /// @param selectedMarkHeight the selected check mark height in logical pixels
    /// @throws IllegalArgumentException if `selectedMarkHeight` is negative or not finite
    public final void setSelectedMarkHeight(double selectedMarkHeight) {
        selectedMarkHeightProperty().set(M3Css.nonNegative(selectedMarkHeight, "selectedMarkHeight"));
    }

    /// Returns the styleable property that stores the selected mark height.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-selected-mark-height`, and accepts finite,
    /// non-negative values. Its default value is `10.0` logical pixels.
    ///
    /// @return the selected mark height property
    public final StyleableDoubleProperty selectedMarkHeightProperty() {
        if (selectedMarkHeight == null) {
            selectedMarkHeight = createSizeProperty(
                    DEFAULT_SELECTED_MARK_HEIGHT,
                    "selectedMarkHeight",
                    StyleableProperties.SELECTED_MARK_HEIGHT
            );
        }
        return selectedMarkHeight;
    }

    /// The width of the indeterminate mark, in logical pixels.
    ///
    /// The default value is `12.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `12.0`
    private @Nullable StyleableDoubleProperty indeterminateMarkWidth;

    /// Returns the indeterminate dash mark width token.
    ///
    /// @return the indeterminate mark width in logical pixels
    public final double getIndeterminateMarkWidth() {
        return indeterminateMarkWidth == null ? DEFAULT_INDETERMINATE_MARK_WIDTH : indeterminateMarkWidth.get();
    }

    /// Sets the indeterminate dash mark width token.
    ///
    /// @param indeterminateMarkWidth the indeterminate mark width in logical pixels
    /// @throws IllegalArgumentException if `indeterminateMarkWidth` is negative or not finite
    public final void setIndeterminateMarkWidth(double indeterminateMarkWidth) {
        indeterminateMarkWidthProperty().set(M3Css.nonNegative(indeterminateMarkWidth, "indeterminateMarkWidth"));
    }

    /// Returns the styleable property that stores the indeterminate mark width.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-indeterminate-mark-width`, and accepts
    /// finite, non-negative values. Its default value is `12.0` logical pixels.
    ///
    /// @return the indeterminate mark width property
    public final StyleableDoubleProperty indeterminateMarkWidthProperty() {
        if (indeterminateMarkWidth == null) {
            indeterminateMarkWidth = createSizeProperty(
                    DEFAULT_INDETERMINATE_MARK_WIDTH,
                    "indeterminateMarkWidth",
                    StyleableProperties.INDETERMINATE_MARK_WIDTH
            );
        }
        return indeterminateMarkWidth;
    }

    /// The height of the indeterminate mark, in logical pixels.
    ///
    /// The default value is `2.0`. Values must be finite and non-negative.
    ///
    /// @defaultValue `2.0`
    private @Nullable StyleableDoubleProperty indeterminateMarkHeight;

    /// Returns the indeterminate dash mark height token.
    ///
    /// @return the indeterminate mark height in logical pixels
    public final double getIndeterminateMarkHeight() {
        return indeterminateMarkHeight == null ? DEFAULT_INDETERMINATE_MARK_HEIGHT : indeterminateMarkHeight.get();
    }

    /// Sets the indeterminate dash mark height token.
    ///
    /// @param indeterminateMarkHeight the indeterminate mark height in logical pixels
    /// @throws IllegalArgumentException if `indeterminateMarkHeight` is negative or not finite
    public final void setIndeterminateMarkHeight(double indeterminateMarkHeight) {
        indeterminateMarkHeightProperty().set(M3Css.nonNegative(indeterminateMarkHeight, "indeterminateMarkHeight"));
    }

    /// Returns the styleable property that stores the indeterminate mark height.
    ///
    /// The property can be observed and bound, is exposed to CSS as `-m3-indeterminate-mark-height`, and accepts
    /// finite, non-negative values. Its default value is `2.0` logical pixels.
    ///
    /// @return the indeterminate mark height property
    public final StyleableDoubleProperty indeterminateMarkHeightProperty() {
        if (indeterminateMarkHeight == null) {
            indeterminateMarkHeight = createSizeProperty(
                    DEFAULT_INDETERMINATE_MARK_HEIGHT,
                    "indeterminateMarkHeight",
                    StyleableProperties.INDETERMINATE_MARK_HEIGHT
            );
        }
        return indeterminateMarkHeight;
    }

    /// Whether this checkbox is selected.
    ///
    /// The default value is `false`. This property may be set independently of [indeterminate][#indeterminateProperty()].
    ///
    /// @defaultValue `false`
    private @Nullable BooleanProperty selected;

    /// Returns whether this checkbox is selected.
    ///
    /// @return `true` when this checkbox is selected
    public final boolean isSelected() {
        return selected != null && selected.get();
    }

    /// Sets whether this checkbox is selected.
    ///
    /// @param selected whether this checkbox should be selected
    public final void setSelected(boolean selected) {
        selectedProperty().set(selected);
    }

    /// Returns the observable property that stores the selected state.
    ///
    /// The property can be observed and bound. Its default value is `false`, and it is independent of
    /// [indeterminate][#indeterminateProperty()].
    ///
    /// @return the selected property
    public final BooleanProperty selectedProperty() {
        if (selected == null) {
            selected = new BooleanPropertyBase(false) {
                /// Updates selected visual and accessibility state.
                @Override
                protected void invalidated() {
                    pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, get());
                    notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED);
                    notifyAccessibleAttributeChanged(AccessibleAttribute.TOGGLE_STATE);
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3CheckBox.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "selected";
                }
            };
        }
        return selected;
    }

    /// Whether this checkbox is in the indeterminate state.
    ///
    /// The default value is `false`. Programmatic changes are allowed regardless of
    /// [allowIndeterminate][#allowIndeterminateProperty()].
    ///
    /// @defaultValue `false`
    private @Nullable BooleanProperty indeterminate;

    /// Returns whether this checkbox is in its indeterminate state.
    ///
    /// @return `true` when this checkbox is in the indeterminate state
    public final boolean isIndeterminate() {
        return indeterminate != null && indeterminate.get();
    }

    /// Sets whether this checkbox is in its indeterminate state.
    ///
    /// @param indeterminate whether this checkbox should be in the indeterminate state
    public final void setIndeterminate(boolean indeterminate) {
        indeterminateProperty().set(indeterminate);
    }

    /// Returns the observable property that stores the indeterminate state.
    ///
    /// The property can be observed and bound. Its default value is `false`; programmatic changes are permitted
    /// regardless of [allowIndeterminate][#allowIndeterminateProperty()].
    ///
    /// @return the indeterminate property
    public final BooleanProperty indeterminateProperty() {
        if (indeterminate == null) {
            indeterminate = new BooleanPropertyBase(false) {
                /// Updates indeterminate visual and accessibility state.
                @Override
                protected void invalidated() {
                    boolean active = get();
                    pseudoClassStateChanged(DETERMINATE_PSEUDO_CLASS, !active);
                    pseudoClassStateChanged(INDETERMINATE_PSEUDO_CLASS, active);
                    notifyAccessibleAttributeChanged(AccessibleAttribute.INDETERMINATE);
                    notifyAccessibleAttributeChanged(AccessibleAttribute.TOGGLE_STATE);
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3CheckBox.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "indeterminate";
                }
            };
        }
        return indeterminate;
    }

    /// Whether user activation includes the indeterminate state in its cycle.
    ///
    /// The default value is `false`. This property affects activation only and does not clear an existing
    /// indeterminate state when changed.
    ///
    /// @defaultValue `false`
    private @Nullable BooleanProperty allowIndeterminate;

    /// Returns whether user activation cycles through the indeterminate state.
    ///
    /// @return `true` when user activation includes the indeterminate state
    public final boolean isAllowIndeterminate() {
        return allowIndeterminate != null && allowIndeterminate.get();
    }

    /// Sets whether user activation cycles through the indeterminate state.
    ///
    /// @param allowIndeterminate whether user activation cycles through the indeterminate state
    public final void setAllowIndeterminate(boolean allowIndeterminate) {
        allowIndeterminateProperty().set(allowIndeterminate);
    }

    /// Returns the observable property that controls whether user activation cycles through indeterminate state.
    ///
    /// The property can be observed and bound. Its default value is `false`, and changing it does not clear an
    /// existing indeterminate state.
    ///
    /// @return the allow-indeterminate property
    public final BooleanProperty allowIndeterminateProperty() {
        if (allowIndeterminate == null) {
            allowIndeterminate = new SimpleBooleanProperty(this, "allowIndeterminate", false);
        }
        return allowIndeterminate;
    }

    /// Whether this checkbox uses its error presentation.
    ///
    /// The default value is `false`. This property does not validate input or change selection.
    ///
    /// @defaultValue `false`
    private @Nullable BooleanProperty error;

    /// Returns whether this checkbox renders its error state.
    ///
    /// @return `true` when this checkbox renders its error state
    public final boolean isError() {
        return error != null && error.get();
    }

    /// Sets whether this checkbox renders its error state.
    ///
    /// The error state changes the indicator and interaction colors but does not alter selection, validation, or
    /// action-event behavior. Disabled presentation takes precedence over the error presentation.
    ///
    /// @param error whether this checkbox should render its error state
    public final void setError(boolean error) {
        if (this.error != null || error) {
            errorProperty().set(error);
        }
    }

    /// Returns the observable property that controls the error presentation.
    ///
    /// The property can be observed and bound. Its default value is `false`; it changes visual state without
    /// performing validation or changing selection.
    ///
    /// @return the error property
    public final BooleanProperty errorProperty() {
        if (error == null) {
            error = new BooleanPropertyBase(false) {
                /// Updates the Material error pseudo-class.
                @Override
                protected void invalidated() {
                    pseudoClassStateChanged(ERROR_PSEUDO_CLASS, get());
                }

                /// Returns the owning checkbox.
                @Override
                public Object getBean() {
                    return M3CheckBox.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "error";
                }
            };
        }
        return error;
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the immutable CSS metadata list for this class
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Advances the selection state and fires an action event unless this checkbox is disabled.
    ///
    /// With indeterminate interaction disabled, this method toggles selected and clears indeterminate. With it
    /// enabled, the transition order is unchecked to indeterminate, indeterminate to checked, and checked to
    /// unchecked. The event is dispatched after the state properties have changed.
    @Override
    public void fire() {
        if (isDisabled()) {
            return;
        }

        if (isAllowIndeterminate()) {
            if (!isSelected() && !isIndeterminate()) {
                setIndeterminate(true);
            } else if (isSelected() && !isIndeterminate()) {
                setSelected(false);
            } else if (isIndeterminate()) {
                setSelected(true);
                setIndeterminate(false);
            }
        } else {
            setSelected(!isSelected());
            setIndeterminate(false);
        }
        fireEvent(new ActionEvent(this, this));
    }

    /// Creates the default Material Design 3 checkbox skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3CheckBoxSkin(this);
    }

    /// Returns the user-agent stylesheet for M3FX selection controls.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("selection.css");
    }

    /// Returns accessibility attributes for checkbox selection state.
    ///
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case SELECTED -> isSelected();
            case INDETERMINATE -> isIndeterminate();
            case TOGGLE_STATE -> {
                if (isIndeterminate()) {
                    yield AccessibleAttribute.ToggleState.INDETERMINATE;
                }
                yield isSelected()
                        ? AccessibleAttribute.ToggleState.CHECKED
                        : AccessibleAttribute.ToggleState.UNCHECKED;
            }
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Adds base style classes.
    private void initialize() {
        M3ControlStyles.initialize(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.CHECK_BOX);
        setAlignment(Pos.CENTER_LEFT);
        setFocusTraversable(true);
        setMnemonicParsing(true);
        setPickOnBounds(true);
        pseudoClassStateChanged(DETERMINATE_PSEUDO_CLASS, true);
        updateMetrics();
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double size = Math.max(getTouchTargetSize(), getStateLayerSize());
        M3Css.setMinHeightIfUnbound(this, size);
        M3Css.setPrefHeightIfUnbound(this, size);
    }

    /// Creates a non-negative styleable size token property.
    private StyleableDoubleProperty createSizeProperty(
            double initialValue,
            String name,
            CssMetaData<M3CheckBox, Number> cssMetaData
    ) {
        return M3Css.nonNegativeStyleableDoubleProperty(initialValue, this, name, cssMetaData, this::requestLayout);
    }

    /// CSS metadata for M3FX checkbox component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the touch target size token.
        private static final CssMetaData<M3CheckBox, Number> TOUCH_TARGET_SIZE = sizeCssMetaData(
                "-m3-touch-target-size",
                DEFAULT_TOUCH_TARGET_SIZE,
                M3CheckBox::touchTargetSizeProperty
        );

        /// CSS metadata for the state layer size token.
        private static final CssMetaData<M3CheckBox, Number> STATE_LAYER_SIZE = sizeCssMetaData(
                "-m3-state-layer-size",
                DEFAULT_STATE_LAYER_SIZE,
                M3CheckBox::stateLayerSizeProperty
        );

        /// CSS metadata for the checkbox container size token.
        private static final CssMetaData<M3CheckBox, Number> CONTAINER_SIZE = sizeCssMetaData(
                "-m3-container-size",
                DEFAULT_CONTAINER_SIZE,
                M3CheckBox::containerSizeProperty
        );

        /// CSS metadata for the selected mark width token.
        private static final CssMetaData<M3CheckBox, Number> SELECTED_MARK_WIDTH = sizeCssMetaData(
                "-m3-selected-mark-width",
                DEFAULT_SELECTED_MARK_WIDTH,
                M3CheckBox::selectedMarkWidthProperty
        );

        /// CSS metadata for the selected mark height token.
        private static final CssMetaData<M3CheckBox, Number> SELECTED_MARK_HEIGHT = sizeCssMetaData(
                "-m3-selected-mark-height",
                DEFAULT_SELECTED_MARK_HEIGHT,
                M3CheckBox::selectedMarkHeightProperty
        );

        /// CSS metadata for the indeterminate mark width token.
        private static final CssMetaData<M3CheckBox, Number> INDETERMINATE_MARK_WIDTH = sizeCssMetaData(
                "-m3-indeterminate-mark-width",
                DEFAULT_INDETERMINATE_MARK_WIDTH,
                M3CheckBox::indeterminateMarkWidthProperty
        );

        /// CSS metadata for the indeterminate mark height token.
        private static final CssMetaData<M3CheckBox, Number> INDETERMINATE_MARK_HEIGHT = sizeCssMetaData(
                "-m3-indeterminate-mark-height",
                DEFAULT_INDETERMINATE_MARK_HEIGHT,
                M3CheckBox::indeterminateMarkHeightProperty
        );

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(ButtonBase.getClassCssMetaData());
            styleables.add(TOUCH_TARGET_SIZE);
            styleables.add(STATE_LAYER_SIZE);
            styleables.add(CONTAINER_SIZE);
            styleables.add(SELECTED_MARK_WIDTH);
            styleables.add(SELECTED_MARK_HEIGHT);
            styleables.add(INDETERMINATE_MARK_WIDTH);
            styleables.add(INDETERMINATE_MARK_HEIGHT);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }

        /// Creates CSS metadata for a non-negative size token.
        private static CssMetaData<M3CheckBox, Number> sizeCssMetaData(
                String property,
                double initialValue,
                Function<M3CheckBox, StyleableDoubleProperty> accessor
        ) {
            return new CssMetaData<>(property, SizeConverter.getInstance(), initialValue) {
                /// Returns whether this property can be set by CSS.
                @Override
                public boolean isSettable(M3CheckBox control) {
                    return M3Css.isSettable(accessor.apply(control));
                }

                /// Returns the styleable property for a control.
                @Override
                public StyleableProperty<Number> getStyleableProperty(M3CheckBox control) {
                    return accessor.apply(control);
                }
            };
        }
    }
}
