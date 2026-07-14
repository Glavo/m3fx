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

/// A Material Design 3 checkbox for selecting one or more independent options.
///
/// `M3CheckBox` is built on JavaFX [ButtonBase] and exposes selected and indeterminate properties instead of
/// extending the JavaFX concrete `CheckBox` class. It supports two-state and three-state selection depending on
/// [allowIndeterminateProperty], supports the Material error presentation through [errorProperty], updates
/// JavaFX accessibility toggle attributes, and renders Material state layers and ripple feedback around the
/// selection indicator.
///
/// Use checkboxes for independent choices or bulk-selection affordances. See
/// [Material Design checkboxes](https://m3.material.io/components/checkbox/overview).
@NotNullByDefault
public class M3CheckBox extends ButtonBase {
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

    // The styleable touch target size token.
    private @Nullable StyleableDoubleProperty touchTargetSize;

    // The styleable state layer size token.
    private @Nullable StyleableDoubleProperty stateLayerSize;

    // The styleable checkbox container size token.
    private @Nullable StyleableDoubleProperty containerSize;

    // The styleable selected mark width token.
    private @Nullable StyleableDoubleProperty selectedMarkWidth;

    // The styleable selected mark height token.
    private @Nullable StyleableDoubleProperty selectedMarkHeight;

    // The styleable indeterminate mark width token.
    private @Nullable StyleableDoubleProperty indeterminateMarkWidth;

    // The styleable indeterminate mark height token.
    private @Nullable StyleableDoubleProperty indeterminateMarkHeight;

    // The selected state property.
    private @Nullable BooleanProperty selected;

    // The indeterminate state property.
    private @Nullable BooleanProperty indeterminate;

    // Whether user activation cycles through the indeterminate state.
    private @Nullable BooleanProperty allowIndeterminate;

    /// Whether the checkbox renders its error state.
    private @Nullable BooleanProperty error;

    /// Creates an empty checkbox.
    public M3CheckBox() {
        initialize();
    }

    /// Creates a checkbox with text.
    ///
    /// @param text the text displayed by the checkbox
    public M3CheckBox(String text) {
        super(text);
        initialize();
    }

    /// Sets whether this checkbox is selected.
    ///
    /// @param selected whether this checkbox should be selected
    public final void setSelected(boolean selected) {
        selectedProperty().set(selected);
    }

    /// Returns whether this checkbox is selected.
    ///
    /// @return `true` when this checkbox is selected
    public final boolean isSelected() {
        return selected != null && selected.get();
    }

    /// Returns the selected state property.
    ///
    /// @return the selected state property
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

    /// Sets whether this checkbox is in its indeterminate state.
    ///
    /// @param indeterminate whether this checkbox should be in the indeterminate state
    public final void setIndeterminate(boolean indeterminate) {
        indeterminateProperty().set(indeterminate);
    }

    /// Returns whether this checkbox is in its indeterminate state.
    ///
    /// @return `true` when this checkbox is in the indeterminate state
    public final boolean isIndeterminate() {
        return indeterminate != null && indeterminate.get();
    }

    /// Returns the indeterminate state property.
    ///
    /// @return the indeterminate state property
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

    /// Sets whether user activation cycles through the indeterminate state.
    ///
    /// @param allowIndeterminate whether user activation cycles through the indeterminate state
    public final void setAllowIndeterminate(boolean allowIndeterminate) {
        allowIndeterminateProperty().set(allowIndeterminate);
    }

    /// Returns whether user activation cycles through the indeterminate state.
    ///
    /// @return `true` when user activation includes the indeterminate state
    public final boolean isAllowIndeterminate() {
        return allowIndeterminate != null && allowIndeterminate.get();
    }

    /// Returns the allow-indeterminate state property.
    ///
    /// @return the allow-indeterminate state property
    public final BooleanProperty allowIndeterminateProperty() {
        if (allowIndeterminate == null) {
            allowIndeterminate = new SimpleBooleanProperty(this, "allowIndeterminate", false);
        }
        return allowIndeterminate;
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

    /// Returns whether this checkbox renders its error state.
    ///
    /// @return `true` when this checkbox renders its error state
    public final boolean isError() {
        return error != null && error.get();
    }

    /// Returns the error state property.
    ///
    /// Setting this property activates the `error` CSS pseudo-class. It does not perform validation or provide an
    /// error message; applications remain responsible for deciding when the checkbox value is invalid.
    ///
    /// @return the writable error state property
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

    /// Returns the preferred touch target size token.
    ///
    /// @return the preferred touch target size in pixels
    public final double getTouchTargetSize() {
        return touchTargetSize == null ? DEFAULT_TOUCH_TARGET_SIZE : touchTargetSize.get();
    }

    /// Sets the preferred touch target size token.
    ///
    /// @param touchTargetSize the preferred touch target size in pixels
    public final void setTouchTargetSize(double touchTargetSize) {
        touchTargetSizeProperty().set(M3Css.nonNegative(touchTargetSize, "touchTargetSize"));
    }

    /// Returns the preferred touch target size token property.
    ///
    /// @return the preferred touch target size property
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

    /// Returns the bounded indicator state layer size token.
    ///
    /// @return the state layer size in pixels
    public final double getStateLayerSize() {
        return stateLayerSize == null ? DEFAULT_STATE_LAYER_SIZE : stateLayerSize.get();
    }

    /// Sets the bounded indicator state layer size token.
    ///
    /// @param stateLayerSize the state layer size in pixels
    public final void setStateLayerSize(double stateLayerSize) {
        stateLayerSizeProperty().set(M3Css.nonNegative(stateLayerSize, "stateLayerSize"));
    }

    /// Returns the bounded indicator state layer size token property.
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

    /// Returns the checkbox container size token.
    ///
    /// @return the checkbox container size in pixels
    public final double getContainerSize() {
        return containerSize == null ? DEFAULT_CONTAINER_SIZE : containerSize.get();
    }

    /// Sets the checkbox container size token.
    ///
    /// @param containerSize the checkbox container size in pixels
    public final void setContainerSize(double containerSize) {
        containerSizeProperty().set(M3Css.nonNegative(containerSize, "containerSize"));
    }

    /// Returns the checkbox container size token property.
    ///
    /// @return the checkbox container size property
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

    /// Returns the selected check mark width token.
    ///
    /// @return the selected check mark width in pixels
    public final double getSelectedMarkWidth() {
        return selectedMarkWidth == null ? DEFAULT_SELECTED_MARK_WIDTH : selectedMarkWidth.get();
    }

    /// Sets the selected check mark width token.
    ///
    /// @param selectedMarkWidth the selected check mark width in pixels
    public final void setSelectedMarkWidth(double selectedMarkWidth) {
        selectedMarkWidthProperty().set(M3Css.nonNegative(selectedMarkWidth, "selectedMarkWidth"));
    }

    /// Returns the selected check mark width token property.
    ///
    /// @return the selected check mark width property
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

    /// Returns the selected check mark height token.
    ///
    /// @return the selected check mark height in pixels
    public final double getSelectedMarkHeight() {
        return selectedMarkHeight == null ? DEFAULT_SELECTED_MARK_HEIGHT : selectedMarkHeight.get();
    }

    /// Sets the selected check mark height token.
    ///
    /// @param selectedMarkHeight the selected check mark height in pixels
    public final void setSelectedMarkHeight(double selectedMarkHeight) {
        selectedMarkHeightProperty().set(M3Css.nonNegative(selectedMarkHeight, "selectedMarkHeight"));
    }

    /// Returns the selected check mark height token property.
    ///
    /// @return the selected check mark height property
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

    /// Returns the indeterminate dash mark width token.
    ///
    /// @return the indeterminate dash mark width in pixels
    public final double getIndeterminateMarkWidth() {
        return indeterminateMarkWidth == null ? DEFAULT_INDETERMINATE_MARK_WIDTH : indeterminateMarkWidth.get();
    }

    /// Sets the indeterminate dash mark width token.
    ///
    /// @param indeterminateMarkWidth the indeterminate dash mark width in pixels
    public final void setIndeterminateMarkWidth(double indeterminateMarkWidth) {
        indeterminateMarkWidthProperty().set(M3Css.nonNegative(indeterminateMarkWidth, "indeterminateMarkWidth"));
    }

    /// Returns the indeterminate dash mark width token property.
    ///
    /// @return the indeterminate dash mark width property
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

    /// Returns the indeterminate dash mark height token.
    ///
    /// @return the indeterminate dash mark height in pixels
    public final double getIndeterminateMarkHeight() {
        return indeterminateMarkHeight == null ? DEFAULT_INDETERMINATE_MARK_HEIGHT : indeterminateMarkHeight.get();
    }

    /// Sets the indeterminate dash mark height token.
    ///
    /// @param indeterminateMarkHeight the indeterminate dash mark height in pixels
    public final void setIndeterminateMarkHeight(double indeterminateMarkHeight) {
        indeterminateMarkHeightProperty().set(M3Css.nonNegative(indeterminateMarkHeight, "indeterminateMarkHeight"));
    }

    /// Returns the indeterminate dash mark height token property.
    ///
    /// @return the indeterminate dash mark height property
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

    /// Toggles this checkbox and fires its action handler.
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
