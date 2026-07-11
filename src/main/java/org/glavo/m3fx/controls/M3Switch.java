// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
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
import org.glavo.m3fx.skins.M3SwitchSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/// A Material Design 3 switch for turning a single setting on or off.
///
/// `M3Switch` is built on JavaFX [ButtonBase] and exposes a selected property rather than extending JavaFX's
/// concrete toggle controls. The skin renders the Material track, handle, selected state, hover and focus state
/// layers, ripple feedback, and token-backed touch target.
///
/// Use a switch for a setting whose change takes effect immediately. For selection from multiple choices, use
/// radio buttons or segmented buttons. See [Material Design switches](https://m3.material.io/components/switch/overview).
@NotNullByDefault
public class M3Switch extends ButtonBase {
    /// The base style class for M3FX switches.
    public static final String STYLE_CLASS = "m3-switch";

    /// The selected pseudo-class used by switches.
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    /// The default switch touch target size.
    private static final double DEFAULT_TOUCH_TARGET_SIZE = 48.0;

    /// The default switch track shape radius.
    private static final double DEFAULT_TRACK_SHAPE = 999.0;

    /// The default switch track width.
    private static final double DEFAULT_TRACK_WIDTH = 52.0;

    /// The default switch track height.
    private static final double DEFAULT_TRACK_HEIGHT = 32.0;

    /// The default switch state layer size.
    private static final double DEFAULT_STATE_LAYER_SIZE = 40.0;

    /// The default unselected switch handle size.
    private static final double DEFAULT_UNSELECTED_HANDLE_SIZE = 16.0;

    /// The default selected switch handle size.
    private static final double DEFAULT_SELECTED_HANDLE_SIZE = 24.0;

    /// The default pressed switch handle size.
    private static final double DEFAULT_PRESSED_HANDLE_SIZE = 28.0;

    // The styleable touch target size token.
    private @Nullable StyleableDoubleProperty touchTargetSize;

    // The styleable switch track shape token.
    private @Nullable StyleableDoubleProperty trackShape;

    // The styleable switch track width token.
    private @Nullable StyleableDoubleProperty trackWidth;

    // The styleable switch track height token.
    private @Nullable StyleableDoubleProperty trackHeight;

    // The styleable switch state layer size token.
    private @Nullable StyleableDoubleProperty stateLayerSize;

    // The styleable unselected switch handle size token.
    private @Nullable StyleableDoubleProperty unselectedHandleSize;

    // The styleable selected switch handle size token.
    private @Nullable StyleableDoubleProperty selectedHandleSize;

    // The styleable pressed switch handle size token.
    private @Nullable StyleableDoubleProperty pressedHandleSize;

    // The selected state property.
    private @Nullable BooleanProperty selected;

    /// Creates an empty switch.
    public M3Switch() {
        initialize();
    }

    /// Creates a switch with text.
    public M3Switch(String text) {
        super(text);
        initialize();
    }

    /// Sets whether this switch is selected.
    public final void setSelected(boolean selected) {
        selectedProperty().set(selected);
    }

    /// Returns whether this switch is selected.
    public final boolean isSelected() {
        return selected != null && selected.get();
    }

    /// Returns the selected state property.
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
                    return M3Switch.this;
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

    /// Returns the preferred touch target size token.
    public final double getTouchTargetSize() {
        return touchTargetSize == null ? DEFAULT_TOUCH_TARGET_SIZE : touchTargetSize.get();
    }

    /// Sets the preferred touch target size token.
    public final void setTouchTargetSize(double touchTargetSize) {
        touchTargetSizeProperty().set(M3Css.nonNegative(touchTargetSize, "touchTargetSize"));
    }

    /// Returns the preferred touch target size token property.
    public final StyleableDoubleProperty touchTargetSizeProperty() {
        if (touchTargetSize == null) {
            touchTargetSize = sizeProperty(
                    DEFAULT_TOUCH_TARGET_SIZE,
                    "touchTargetSize",
                    StyleableProperties.TOUCH_TARGET_SIZE,
                    this::updateMetrics
            );
        }
        return touchTargetSize;
    }

    /// Returns the switch track shape radius token.
    public final double getTrackShape() {
        return trackShape == null ? DEFAULT_TRACK_SHAPE : trackShape.get();
    }

    /// Sets the switch track shape radius token.
    public final void setTrackShape(double trackShape) {
        trackShapeProperty().set(M3Css.nonNegative(trackShape, "trackShape"));
    }

    /// Returns the switch track shape radius token property.
    public final StyleableDoubleProperty trackShapeProperty() {
        if (trackShape == null) {
            trackShape = sizeProperty(
                    DEFAULT_TRACK_SHAPE,
                    "trackShape",
                    StyleableProperties.TRACK_SHAPE,
                    this::requestLayout
            );
        }
        return trackShape;
    }

    /// Returns the switch track width token.
    public final double getTrackWidth() {
        return trackWidth == null ? DEFAULT_TRACK_WIDTH : trackWidth.get();
    }

    /// Sets the switch track width token.
    public final void setTrackWidth(double trackWidth) {
        trackWidthProperty().set(M3Css.nonNegative(trackWidth, "trackWidth"));
    }

    /// Returns the switch track width token property.
    public final StyleableDoubleProperty trackWidthProperty() {
        if (trackWidth == null) {
            trackWidth = sizeProperty(DEFAULT_TRACK_WIDTH, "trackWidth", StyleableProperties.TRACK_WIDTH, this::requestLayout);
        }
        return trackWidth;
    }

    /// Returns the switch track height token.
    public final double getTrackHeight() {
        return trackHeight == null ? DEFAULT_TRACK_HEIGHT : trackHeight.get();
    }

    /// Sets the switch track height token.
    public final void setTrackHeight(double trackHeight) {
        trackHeightProperty().set(M3Css.nonNegative(trackHeight, "trackHeight"));
    }

    /// Returns the switch track height token property.
    public final StyleableDoubleProperty trackHeightProperty() {
        if (trackHeight == null) {
            trackHeight = sizeProperty(DEFAULT_TRACK_HEIGHT, "trackHeight", StyleableProperties.TRACK_HEIGHT, this::updateMetrics);
        }
        return trackHeight;
    }

    /// Returns the switch state layer size token.
    public final double getStateLayerSize() {
        return stateLayerSize == null ? DEFAULT_STATE_LAYER_SIZE : stateLayerSize.get();
    }

    /// Sets the switch state layer size token.
    public final void setStateLayerSize(double stateLayerSize) {
        stateLayerSizeProperty().set(M3Css.nonNegative(stateLayerSize, "stateLayerSize"));
    }

    /// Returns the switch state layer size token property.
    public final StyleableDoubleProperty stateLayerSizeProperty() {
        if (stateLayerSize == null) {
            stateLayerSize = sizeProperty(
                    DEFAULT_STATE_LAYER_SIZE,
                    "stateLayerSize",
                    StyleableProperties.STATE_LAYER_SIZE,
                    this::requestLayout
            );
        }
        return stateLayerSize;
    }

    /// Returns the unselected switch handle size token.
    public final double getUnselectedHandleSize() {
        return unselectedHandleSize == null ? DEFAULT_UNSELECTED_HANDLE_SIZE : unselectedHandleSize.get();
    }

    /// Sets the unselected switch handle size token.
    public final void setUnselectedHandleSize(double unselectedHandleSize) {
        unselectedHandleSizeProperty().set(M3Css.nonNegative(unselectedHandleSize, "unselectedHandleSize"));
    }

    /// Returns the unselected switch handle size token property.
    public final StyleableDoubleProperty unselectedHandleSizeProperty() {
        if (unselectedHandleSize == null) {
            unselectedHandleSize = sizeProperty(
                    DEFAULT_UNSELECTED_HANDLE_SIZE,
                    "unselectedHandleSize",
                    StyleableProperties.UNSELECTED_HANDLE_SIZE,
                    this::requestLayout
            );
        }
        return unselectedHandleSize;
    }

    /// Returns the selected switch handle size token.
    public final double getSelectedHandleSize() {
        return selectedHandleSize == null ? DEFAULT_SELECTED_HANDLE_SIZE : selectedHandleSize.get();
    }

    /// Sets the selected switch handle size token.
    public final void setSelectedHandleSize(double selectedHandleSize) {
        selectedHandleSizeProperty().set(M3Css.nonNegative(selectedHandleSize, "selectedHandleSize"));
    }

    /// Returns the selected switch handle size token property.
    public final StyleableDoubleProperty selectedHandleSizeProperty() {
        if (selectedHandleSize == null) {
            selectedHandleSize = sizeProperty(
                    DEFAULT_SELECTED_HANDLE_SIZE,
                    "selectedHandleSize",
                    StyleableProperties.SELECTED_HANDLE_SIZE,
                    this::requestLayout
            );
        }
        return selectedHandleSize;
    }

    /// Returns the pressed switch handle size token.
    public final double getPressedHandleSize() {
        return pressedHandleSize == null ? DEFAULT_PRESSED_HANDLE_SIZE : pressedHandleSize.get();
    }

    /// Sets the pressed switch handle size token.
    public final void setPressedHandleSize(double pressedHandleSize) {
        pressedHandleSizeProperty().set(M3Css.nonNegative(pressedHandleSize, "pressedHandleSize"));
    }

    /// Returns the pressed switch handle size token property.
    public final StyleableDoubleProperty pressedHandleSizeProperty() {
        if (pressedHandleSize == null) {
            pressedHandleSize = sizeProperty(
                    DEFAULT_PRESSED_HANDLE_SIZE,
                    "pressedHandleSize",
                    StyleableProperties.PRESSED_HANDLE_SIZE,
                    this::requestLayout
            );
        }
        return pressedHandleSize;
    }

    /// Returns the CSS metadata for this control class.
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Toggles this switch and fires its action handler.
    @Override
    public void fire() {
        if (!isDisabled()) {
            setSelected(!isSelected());
            fireEvent(new ActionEvent(this, this));
        }
    }

    /// Creates the default Material Design 3 switch skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3SwitchSkin(this);
    }

    /// Returns the user-agent stylesheet for M3FX selection controls.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("selection.css");
    }

    /// Returns accessibility attributes for switch selection state.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case SELECTED -> isSelected();
            case TOGGLE_STATE -> isSelected()
                    ? AccessibleAttribute.ToggleState.CHECKED
                    : AccessibleAttribute.ToggleState.UNCHECKED;
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
        updateMetrics();
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double size = Math.max(Math.max(getTouchTargetSize(), getTrackHeight()), getStateLayerSize());
        M3Css.setMinHeightIfUnbound(this, size);
        M3Css.setPrefHeightIfUnbound(this, size);
    }

    /// Creates a non-negative styleable size property for a switch token.
    private StyleableDoubleProperty sizeProperty(
            double initialValue,
            String name,
            CssMetaData<M3Switch, Number> cssMetaData,
            Runnable invalidation
    ) {
        return M3Css.nonNegativeStyleableDoubleProperty(initialValue, this, name, cssMetaData, invalidation);
    }

    /// CSS metadata for M3FX switch component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the touch target size token.
        private static final CssMetaData<M3Switch, Number> TOUCH_TARGET_SIZE =
                sizeCssMetaData("-m3-touch-target-size", DEFAULT_TOUCH_TARGET_SIZE, M3Switch::touchTargetSizeProperty);

        /// CSS metadata for the switch track shape token.
        private static final CssMetaData<M3Switch, Number> TRACK_SHAPE =
                sizeCssMetaData("-m3-track-shape", DEFAULT_TRACK_SHAPE, M3Switch::trackShapeProperty);

        /// CSS metadata for the switch track width token.
        private static final CssMetaData<M3Switch, Number> TRACK_WIDTH =
                sizeCssMetaData("-m3-track-width", DEFAULT_TRACK_WIDTH, M3Switch::trackWidthProperty);

        /// CSS metadata for the switch track height token.
        private static final CssMetaData<M3Switch, Number> TRACK_HEIGHT =
                sizeCssMetaData("-m3-track-height", DEFAULT_TRACK_HEIGHT, M3Switch::trackHeightProperty);

        /// CSS metadata for the switch state layer size token.
        private static final CssMetaData<M3Switch, Number> STATE_LAYER_SIZE =
                sizeCssMetaData("-m3-state-layer-size", DEFAULT_STATE_LAYER_SIZE, M3Switch::stateLayerSizeProperty);

        /// CSS metadata for the unselected switch handle size token.
        private static final CssMetaData<M3Switch, Number> UNSELECTED_HANDLE_SIZE =
                sizeCssMetaData(
                        "-m3-unselected-handle-size",
                        DEFAULT_UNSELECTED_HANDLE_SIZE,
                        M3Switch::unselectedHandleSizeProperty
                );

        /// CSS metadata for the selected switch handle size token.
        private static final CssMetaData<M3Switch, Number> SELECTED_HANDLE_SIZE =
                sizeCssMetaData("-m3-selected-handle-size", DEFAULT_SELECTED_HANDLE_SIZE, M3Switch::selectedHandleSizeProperty);

        /// CSS metadata for the pressed switch handle size token.
        private static final CssMetaData<M3Switch, Number> PRESSED_HANDLE_SIZE =
                sizeCssMetaData("-m3-pressed-handle-size", DEFAULT_PRESSED_HANDLE_SIZE, M3Switch::pressedHandleSizeProperty);

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(ButtonBase.getClassCssMetaData());
            styleables.add(TOUCH_TARGET_SIZE);
            styleables.add(TRACK_SHAPE);
            styleables.add(TRACK_WIDTH);
            styleables.add(TRACK_HEIGHT);
            styleables.add(STATE_LAYER_SIZE);
            styleables.add(UNSELECTED_HANDLE_SIZE);
            styleables.add(SELECTED_HANDLE_SIZE);
            styleables.add(PRESSED_HANDLE_SIZE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }

        /// Creates CSS metadata for a non-negative switch size token.
        private static CssMetaData<M3Switch, Number> sizeCssMetaData(
                String property,
                double defaultValue,
                Function<M3Switch, StyleableDoubleProperty> propertyAccessor
        ) {
            return new CssMetaData<>(property, SizeConverter.getInstance(), defaultValue) {
                /// Returns whether this property can be set by CSS.
                @Override
                public boolean isSettable(M3Switch control) {
                    return M3Css.isSettable(propertyAccessor.apply(control));
                }

                /// Returns the styleable property for a control.
                @Override
                public StyleableProperty<Number> getStyleableProperty(M3Switch control) {
                    return propertyAccessor.apply(control);
                }
            };
        }
    }
}
