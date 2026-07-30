// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3Accessible;
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
/// `M3Switch` is built on JavaFX [ButtonBase] and exposes a selected property rather than extending a concrete JavaFX
/// toggle control. Activation toggles [#selectedProperty()] before firing an [ActionEvent]. Direct property changes
/// update state without firing an action event. Pointer dragging previews the handle position and commits the state
/// nearest the handle when released.
///
/// A new switch is unselected and has no handle icons. Selected and unselected icons are optional child nodes owned
/// by the control. Geometry properties use JavaFX logical pixels and are styleable through the control's CSS
/// metadata.
///
/// Use a switch for a setting whose change takes effect immediately. For selection from multiple choices, use
/// radio buttons or segmented buttons. See [Material Design switches](https://m3.material.io/components/switch/overview).
@NotNullByDefault
public final class M3Switch extends ButtonBase {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-switch";

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

    /// The default switch handle size when an icon is shown.
    private static final double DEFAULT_WITH_ICON_HANDLE_SIZE = 24.0;

    /// The default selected switch handle size.
    private static final double DEFAULT_SELECTED_HANDLE_SIZE = 24.0;

    /// The default pressed switch handle size.
    private static final double DEFAULT_PRESSED_HANDLE_SIZE = 28.0;

    /// The default selected or unselected handle icon size.
    private static final double DEFAULT_ICON_SIZE = 16.0;

    /// Creates an unselected switch with empty text and no handle icons.
    public M3Switch() {
        initialize();
    }

    /// Creates a switch with text.
    ///
    /// @param text the text displayed next to the switch
    public M3Switch(String text) {
        super(text);
        initialize();
    }

    /// Whether the switch is selected.
    ///
    /// Direct changes do not fire an action event.
    ///
    /// @defaultValue `false`
    private @Nullable BooleanProperty selected;

    /// Returns whether this switch is selected.
    ///
    /// @return `true` when this switch is selected
    public final boolean isSelected() {
        return selected != null && selected.get();
    }

    /// Sets whether this switch is selected.
    ///
    /// @param selected whether this switch is selected
    public final void setSelected(boolean selected) {
        selectedProperty().set(selected);
    }

    /// Returns the `selected` property.
    ///
    /// The returned property is observable and bindable. Its default value is `false`.
    ///
    /// @return the `selected` property
    public final BooleanProperty selectedProperty() {
        if (selected == null) {
            selected = new BooleanPropertyBase(false) {
                /// Updates selected visual and accessibility state.
                @Override
                protected void invalidated() {
                    pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, get());
                    notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED);
                    // JavaFX 17 has no aggregate TOGGLE_STATE attribute; the helper is a no-op there.
                    M3Accessible.notifyToggleStateChanged(M3Switch.this);
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

    /// The optional icon shown inside the selected handle.
    ///
    /// A non-null node is owned by this switch and must be available for it to parent.
    ///
    /// @defaultValue `null`
    private @Nullable ObjectProperty<@Nullable Node> selectedIcon;

    /// Returns the icon shown inside the handle while this switch is selected.
    ///
    /// @return the selected handle icon, or `null` when the selected handle has no icon
    public final @Nullable Node getSelectedIcon() {
        return selectedIcon == null ? null : selectedIcon.get();
    }

    /// Sets the icon shown inside the handle while this switch is selected.
    ///
    /// The icon should fit [#iconSizeProperty()]. A non-null node becomes a child of this control and must satisfy
    /// normal JavaFX parent ownership rules.
    ///
    /// @param selectedIcon the selected handle icon, or `null` for no icon
    public final void setSelectedIcon(@Nullable Node selectedIcon) {
        selectedIconProperty().set(selectedIcon);
    }

    /// Returns the `selectedIcon` property.
    ///
    /// The returned property is observable and bindable. Its default value is `null`.
    ///
    /// @return the `selectedIcon` property
    public final ObjectProperty<@Nullable Node> selectedIconProperty() {
        if (selectedIcon == null) {
            selectedIcon = new SimpleObjectProperty<>(this, "selectedIcon") {
                /// Requests handle content layout when the selected icon changes.
                @Override
                protected void invalidated() {
                    updateIconMetrics();
                    requestLayout();
                }
            };
        }
        return selectedIcon;
    }

    /// The optional icon shown inside the unselected handle.
    ///
    /// A non-null node is owned by this switch and must be available for it to parent.
    ///
    /// @defaultValue `null`
    private @Nullable ObjectProperty<@Nullable Node> unselectedIcon;

    /// Returns the icon shown inside the handle while this switch is unselected.
    ///
    /// @return the unselected handle icon, or `null` when the unselected handle has no icon
    public final @Nullable Node getUnselectedIcon() {
        return unselectedIcon == null ? null : unselectedIcon.get();
    }

    /// Sets the icon shown inside the handle while this switch is unselected.
    ///
    /// The icon should fit [#iconSizeProperty()]. A non-null node becomes a child of this control and must satisfy
    /// normal JavaFX parent ownership rules.
    ///
    /// @param unselectedIcon the unselected handle icon, or `null` for no icon
    public final void setUnselectedIcon(@Nullable Node unselectedIcon) {
        unselectedIconProperty().set(unselectedIcon);
    }

    /// Returns the `unselectedIcon` property.
    ///
    /// The returned property is observable and bindable. Its default value is `null`.
    ///
    /// @return the `unselectedIcon` property
    public final ObjectProperty<@Nullable Node> unselectedIconProperty() {
        if (unselectedIcon == null) {
            unselectedIcon = new SimpleObjectProperty<>(this, "unselectedIcon") {
                /// Requests handle content layout when the unselected icon changes.
                @Override
                protected void invalidated() {
                    updateIconMetrics();
                    requestLayout();
                }
            };
        }
        return unselectedIcon;
    }

    /// The preferred square touch-target size in logical pixels.
    ///
    /// @defaultValue `48.0`
    private @Nullable StyleableDoubleProperty touchTargetSize;

    /// Returns the preferred touch target size token.
    ///
    /// @return the preferred square target size in pixels
    public final double getTouchTargetSize() {
        return touchTargetSize == null ? DEFAULT_TOUCH_TARGET_SIZE : touchTargetSize.get();
    }

    /// Sets the preferred touch target size token.
    ///
    /// @param touchTargetSize the finite, non-negative target size in pixels
    /// @throws IllegalArgumentException if `touchTargetSize` is negative or non-finite
    public final void setTouchTargetSize(double touchTargetSize) {
        touchTargetSizeProperty().set(M3Css.nonNegative(touchTargetSize, "touchTargetSize"));
    }

    /// Returns the `touchTargetSize` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `48.0` logical pixels.
    ///
    /// @return the `touchTargetSize` property
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

    /// The switch track corner radius in logical pixels.
    ///
    /// @defaultValue `999.0`
    private @Nullable StyleableDoubleProperty trackShape;

    /// Returns the switch track shape radius token.
    ///
    /// @return the track corner radius in pixels
    public final double getTrackShape() {
        return trackShape == null ? DEFAULT_TRACK_SHAPE : trackShape.get();
    }

    /// Sets the switch track shape radius token.
    ///
    /// @param trackShape the finite, non-negative corner radius in pixels
    /// @throws IllegalArgumentException if `trackShape` is negative or non-finite
    public final void setTrackShape(double trackShape) {
        trackShapeProperty().set(M3Css.nonNegative(trackShape, "trackShape"));
    }

    /// Returns the `trackShape` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `999.0` logical pixels.
    ///
    /// @return the `trackShape` property
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

    /// The switch track width in logical pixels.
    ///
    /// @defaultValue `52.0`
    private @Nullable StyleableDoubleProperty trackWidth;

    /// Returns the switch track width token.
    ///
    /// @return the track width in pixels
    public final double getTrackWidth() {
        return trackWidth == null ? DEFAULT_TRACK_WIDTH : trackWidth.get();
    }

    /// Sets the switch track width token.
    ///
    /// @param trackWidth the finite, non-negative track width in pixels
    /// @throws IllegalArgumentException if `trackWidth` is negative or non-finite
    public final void setTrackWidth(double trackWidth) {
        trackWidthProperty().set(M3Css.nonNegative(trackWidth, "trackWidth"));
    }

    /// Returns the `trackWidth` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `52.0` logical pixels.
    ///
    /// @return the `trackWidth` property
    public final StyleableDoubleProperty trackWidthProperty() {
        if (trackWidth == null) {
            trackWidth = sizeProperty(DEFAULT_TRACK_WIDTH, "trackWidth", StyleableProperties.TRACK_WIDTH, this::requestLayout);
        }
        return trackWidth;
    }

    /// The switch track height in logical pixels.
    ///
    /// @defaultValue `32.0`
    private @Nullable StyleableDoubleProperty trackHeight;

    /// Returns the switch track height token.
    ///
    /// @return the track height in pixels
    public final double getTrackHeight() {
        return trackHeight == null ? DEFAULT_TRACK_HEIGHT : trackHeight.get();
    }

    /// Sets the switch track height token.
    ///
    /// @param trackHeight the finite, non-negative track height in pixels
    /// @throws IllegalArgumentException if `trackHeight` is negative or non-finite
    public final void setTrackHeight(double trackHeight) {
        trackHeightProperty().set(M3Css.nonNegative(trackHeight, "trackHeight"));
    }

    /// Returns the `trackHeight` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `32.0` logical pixels.
    ///
    /// @return the `trackHeight` property
    public final StyleableDoubleProperty trackHeightProperty() {
        if (trackHeight == null) {
            trackHeight = sizeProperty(DEFAULT_TRACK_HEIGHT, "trackHeight", StyleableProperties.TRACK_HEIGHT, this::updateMetrics);
        }
        return trackHeight;
    }

    /// The circular handle state-layer diameter in logical pixels.
    ///
    /// @defaultValue `40.0`
    private @Nullable StyleableDoubleProperty stateLayerSize;

    /// Returns the switch state layer size token.
    ///
    /// @return the diameter of the circular handle state layer in pixels
    public final double getStateLayerSize() {
        return stateLayerSize == null ? DEFAULT_STATE_LAYER_SIZE : stateLayerSize.get();
    }

    /// Sets the switch state layer size token.
    ///
    /// @param stateLayerSize the finite, non-negative state-layer diameter in pixels
    /// @throws IllegalArgumentException if `stateLayerSize` is negative or non-finite
    public final void setStateLayerSize(double stateLayerSize) {
        stateLayerSizeProperty().set(M3Css.nonNegative(stateLayerSize, "stateLayerSize"));
    }

    /// Returns the `stateLayerSize` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `40.0` logical pixels.
    ///
    /// @return the `stateLayerSize` property
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

    /// The unselected handle diameter in logical pixels when no icon is present.
    ///
    /// @defaultValue `16.0`
    private @Nullable StyleableDoubleProperty unselectedHandleSize;

    /// Returns the unselected switch handle size token.
    ///
    /// @return the unselected handle diameter in pixels
    public final double getUnselectedHandleSize() {
        return unselectedHandleSize == null ? DEFAULT_UNSELECTED_HANDLE_SIZE : unselectedHandleSize.get();
    }

    /// Sets the unselected switch handle size token.
    ///
    /// @param unselectedHandleSize the finite, non-negative handle diameter in pixels
    /// @throws IllegalArgumentException if `unselectedHandleSize` is negative or non-finite
    public final void setUnselectedHandleSize(double unselectedHandleSize) {
        unselectedHandleSizeProperty().set(M3Css.nonNegative(unselectedHandleSize, "unselectedHandleSize"));
    }

    /// Returns the `unselectedHandleSize` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `16.0` logical pixels.
    ///
    /// @return the `unselectedHandleSize` property
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

    /// The handle diameter in logical pixels when the current state has an icon.
    ///
    /// @defaultValue `24.0`
    private @Nullable StyleableDoubleProperty withIconHandleSize;

    /// Returns the switch handle size used when the current state has an icon.
    ///
    /// @return the handle size in pixels
    public final double getWithIconHandleSize() {
        return withIconHandleSize == null ? DEFAULT_WITH_ICON_HANDLE_SIZE : withIconHandleSize.get();
    }

    /// Sets the switch handle size used when the current state has an icon.
    ///
    /// @param withIconHandleSize the handle size in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setWithIconHandleSize(double withIconHandleSize) {
        withIconHandleSizeProperty().set(M3Css.nonNegative(withIconHandleSize, "withIconHandleSize"));
    }

    /// Returns the `withIconHandleSize` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `24.0` logical pixels.
    ///
    /// @return the `withIconHandleSize` property
    public final StyleableDoubleProperty withIconHandleSizeProperty() {
        if (withIconHandleSize == null) {
            withIconHandleSize = sizeProperty(
                    DEFAULT_WITH_ICON_HANDLE_SIZE,
                    "withIconHandleSize",
                    StyleableProperties.WITH_ICON_HANDLE_SIZE,
                    this::requestLayout
            );
        }
        return withIconHandleSize;
    }

    /// The selected handle diameter in logical pixels when no icon-specific size applies.
    ///
    /// @defaultValue `24.0`
    private @Nullable StyleableDoubleProperty selectedHandleSize;

    /// Returns the selected switch handle size token.
    ///
    /// @return the selected handle diameter in pixels
    public final double getSelectedHandleSize() {
        return selectedHandleSize == null ? DEFAULT_SELECTED_HANDLE_SIZE : selectedHandleSize.get();
    }

    /// Sets the selected switch handle size token.
    ///
    /// @param selectedHandleSize the finite, non-negative handle diameter in pixels
    /// @throws IllegalArgumentException if `selectedHandleSize` is negative or non-finite
    public final void setSelectedHandleSize(double selectedHandleSize) {
        selectedHandleSizeProperty().set(M3Css.nonNegative(selectedHandleSize, "selectedHandleSize"));
    }

    /// Returns the `selectedHandleSize` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `24.0` logical pixels.
    ///
    /// @return the `selectedHandleSize` property
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

    /// The pressed handle diameter in logical pixels.
    ///
    /// @defaultValue `28.0`
    private @Nullable StyleableDoubleProperty pressedHandleSize;

    /// Returns the pressed switch handle size token.
    ///
    /// @return the pressed handle diameter in pixels
    public final double getPressedHandleSize() {
        return pressedHandleSize == null ? DEFAULT_PRESSED_HANDLE_SIZE : pressedHandleSize.get();
    }

    /// Sets the pressed switch handle size token.
    ///
    /// @param pressedHandleSize the finite, non-negative handle diameter in pixels
    /// @throws IllegalArgumentException if `pressedHandleSize` is negative or non-finite
    public final void setPressedHandleSize(double pressedHandleSize) {
        pressedHandleSizeProperty().set(M3Css.nonNegative(pressedHandleSize, "pressedHandleSize"));
    }

    /// Returns the `pressedHandleSize` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `28.0` logical pixels.
    ///
    /// @return the `pressedHandleSize` property
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

    /// The selected and unselected handle icon size in logical pixels.
    ///
    /// @defaultValue `16.0`
    private @Nullable StyleableDoubleProperty iconSize;

    /// Returns the selected or unselected handle icon size token.
    ///
    /// @return the icon size in pixels
    public final double getIconSize() {
        return iconSize == null ? DEFAULT_ICON_SIZE : iconSize.get();
    }

    /// Sets the selected or unselected handle icon size token.
    ///
    /// @param iconSize the icon size in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setIconSize(double iconSize) {
        iconSizeProperty().set(M3Css.nonNegative(iconSize, "iconSize"));
    }

    /// Returns the `iconSize` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `16.0` logical pixels.
    ///
    /// @return the `iconSize` property
    public final StyleableDoubleProperty iconSizeProperty() {
        if (iconSize == null) {
            iconSize = sizeProperty(
                    DEFAULT_ICON_SIZE,
                    "iconSize",
                    StyleableProperties.ICON_SIZE,
                    this::updateIconMetrics
            );
        }
        return iconSize;
    }

    /// Applies the switch icon-size token to direct M3FX handle icons and requests layout.
    private void updateIconMetrics() {
        if (getSelectedIcon() instanceof M3IconGraphic icon) {
            icon.setIconSize(getIconSize());
        }
        if (getUnselectedIcon() instanceof M3IconGraphic icon) {
            icon.setIconSize(getIconSize());
        }
        requestLayout();
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the shared, unmodifiable CSS metadata list for [M3Switch]
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Toggles this switch and fires its action event.
    ///
    /// Selection changes before synchronous event delivery. This method is a no-op while the control is disabled.
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

    /// Returns the initial alignment used before CSS or application code supplies another value.
    ///
    /// @return the initial alignment, [Pos#CENTER_LEFT]
    @Override
    protected Pos getInitialAlignment() {
        return Pos.CENTER_LEFT;
    }

    /// Returns accessibility attributes for switch selection state.
    ///
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        // JavaFX 17 has no TOGGLE_STATE enum constant, so test the optional runtime value first.
        if (M3Accessible.isToggleStateAttribute(attribute)) {
            return M3Accessible.toggleState(isSelected());
        }
        if (attribute == AccessibleAttribute.SELECTED) {
            return isSelected();
        }
        return super.queryAccessibleAttribute(attribute, parameters);
    }

    /// Adds base style classes.
    private void initialize() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.CHECK_BOX);
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

        /// CSS metadata for the switch handle size token used when an icon is shown.
        private static final CssMetaData<M3Switch, Number> WITH_ICON_HANDLE_SIZE =
                sizeCssMetaData(
                        "-m3-with-icon-handle-size",
                        DEFAULT_WITH_ICON_HANDLE_SIZE,
                        M3Switch::withIconHandleSizeProperty
                );

        /// CSS metadata for the selected switch handle size token.
        private static final CssMetaData<M3Switch, Number> SELECTED_HANDLE_SIZE =
                sizeCssMetaData("-m3-selected-handle-size", DEFAULT_SELECTED_HANDLE_SIZE, M3Switch::selectedHandleSizeProperty);

        /// CSS metadata for the pressed switch handle size token.
        private static final CssMetaData<M3Switch, Number> PRESSED_HANDLE_SIZE =
                sizeCssMetaData("-m3-pressed-handle-size", DEFAULT_PRESSED_HANDLE_SIZE, M3Switch::pressedHandleSizeProperty);

        /// CSS metadata for the selected or unselected handle icon size token.
        private static final CssMetaData<M3Switch, Number> ICON_SIZE =
                sizeCssMetaData("-m3-icon-size", DEFAULT_ICON_SIZE, M3Switch::iconSizeProperty);

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
            styleables.add(WITH_ICON_HANDLE_SIZE);
            styleables.add(SELECTED_HANDLE_SIZE);
            styleables.add(PRESSED_HANDLE_SIZE);
            styleables.add(ICON_SIZE);
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
