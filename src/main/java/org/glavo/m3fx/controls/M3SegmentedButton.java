// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3SegmentedButtonSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 segmented button used as one segment in a related choice group.
///
/// `M3SegmentedButton` is a selectable [ButtonBase] with token-backed height, outline shape, and horizontal
/// padding. It can be used directly for custom layouts, but [M3SegmentedButtonGroup] should be used when
/// adjacent segments need shared borders, a single- or multiple-selection policy, and keyboard navigation.
///
/// Activating the button toggles [#selectedProperty()] and then fires an [ActionEvent]. A containing
/// `M3SegmentedButtonGroup` may immediately adjust that state to satisfy its selection mode and empty-selection
/// policy. A standalone segmented button is unselected by default and may be toggled independently.
///
/// The inherited [#graphicProperty()] is the optional icon slot described by Material Design. A selected button may
/// display a check indicator in place of its graphic. [#selectionIndicatorEnabledProperty()] controls that indicator
/// when selection is already communicated by custom content.
///
/// See [Material Design segmented buttons](https://m3.material.io/components/segmented-buttons/overview).
@NotNullByDefault
public final class M3SegmentedButton extends ButtonBase {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-segmented-button";

    /// The selected pseudo-class used by segmented buttons.
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    /// The default segmented button container height.
    private static final double DEFAULT_CONTAINER_HEIGHT = 40.0;

    /// The default segmented button container shape radius.
    private static final double DEFAULT_CONTAINER_SHAPE = 999.0;

    /// The default horizontal content padding.
    private static final double DEFAULT_HORIZONTAL_PADDING = 12.0;

    /// Creates an unselected segmented button with an empty label and no graphic.
    public M3SegmentedButton() {
        this("");
    }

    /// Creates an unselected segmented button with the specified label and no graphic.
    ///
    /// @param text the text displayed by the segmented button
    public M3SegmentedButton(String text) {
        super(text);
        initialize();
    }

    /// Creates an unselected segmented button with the specified label and graphic.
    ///
    /// @param text    the text displayed by the segmented button
    /// @param graphic the graphic displayed by the segmented button, or `null` for none
    public M3SegmentedButton(String text, @Nullable Node graphic) {
        super(text, graphic);
        initialize();
    }

    /// Whether this segment is selected.
    ///
    /// Direct property changes are observed by a containing [M3SegmentedButtonGroup], which may update this or
    /// other segments to restore its selection invariant.
    ///
    /// @defaultValue `false`
    private final BooleanProperty selectedValue = new SimpleBooleanProperty(this, "selected") {
        /// Updates selected pseudo-class state.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, get());
            notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED);
            // JavaFX 17 has no aggregate TOGGLE_STATE attribute; the helper is a no-op there.
            M3Accessible.notifyToggleStateChanged(M3SegmentedButton.this);
        }
    };

    /// Returns whether this segmented button is selected.
    ///
    /// @return `true` if this segmented button is selected
    public final boolean isSelected() {
        return selectedValue.get();
    }

    /// Sets whether this segmented button is selected.
    ///
    /// @param selected whether this segmented button is selected
    public final void setSelected(boolean selected) {
        selectedValue.set(selected);
    }

    /// Returns the property containing this segmented button's selected state.
    ///
    /// The returned property is observable and bindable. Its default value is `false`.
    ///
    /// @return the selected-state property
    public final BooleanProperty selectedProperty() {
        return selectedValue;
    }

    /// Whether this button may display its selected-state check indicator.
    ///
    /// The indicator is rendered only while this button is selected. With an application-provided graphic, the
    /// indicator visually replaces the graphic without changing [#graphicProperty()]. Setting this property to
    /// `false` does not affect selection behavior, accessibility state, or the supplied graphic.
    ///
    /// @defaultValue `true`
    private final BooleanProperty selectionIndicatorEnabledValue =
            new SimpleBooleanProperty(this, "selectionIndicatorEnabled", true);

    /// Returns whether the selected-state check indicator is enabled.
    ///
    /// @return `true` if this button may display its selection indicator
    public final boolean isSelectionIndicatorEnabled() {
        return selectionIndicatorEnabledValue.get();
    }

    /// Enables or disables the selected-state check indicator.
    ///
    /// The indicator is only eligible for display while this button is selected. If this button has a graphic, the
    /// indicator replaces it visually for the duration of the selected state.
    ///
    /// @param enabled whether the selection indicator is enabled
    public final void setSelectionIndicatorEnabled(boolean enabled) {
        selectionIndicatorEnabledValue.set(enabled);
    }

    /// Returns the property controlling the selected-state check indicator.
    ///
    /// The returned property is observable and bindable. Its default value is `true`.
    ///
    /// @return the selection-indicator-enabled property
    public final BooleanProperty selectionIndicatorEnabledProperty() {
        return selectionIndicatorEnabledValue;
    }

    /// The preferred container height in logical pixels.
    ///
    /// @defaultValue `40.0`
    private @Nullable StyleableDoubleProperty containerHeight;

    /// Returns the preferred container height token.
    ///
    /// @return the preferred container height token in pixels
    public final double getContainerHeight() {
        return containerHeight == null ? DEFAULT_CONTAINER_HEIGHT : containerHeight.get();
    }

    /// Sets the preferred container height token.
    ///
    /// @param containerHeight the preferred container height token in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setContainerHeight(double containerHeight) {
        containerHeightProperty().set(M3Css.nonNegative(containerHeight, "containerHeight"));
    }

    /// Returns the `containerHeight` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `40.0` logical pixels.
    ///
    /// @return the `containerHeight` property
    public final StyleableDoubleProperty containerHeightProperty() {
        if (containerHeight == null) {
            containerHeight = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTAINER_HEIGHT,
                    this,
                    "containerHeight",
                    StyleableProperties.CONTAINER_HEIGHT,
                    this::updateMetrics
            );
        }
        return containerHeight;
    }

    /// The container corner radius in logical pixels.
    ///
    /// @defaultValue `999.0`
    private @Nullable StyleableDoubleProperty containerShape;

    /// Returns the container shape radius token.
    ///
    /// @return the container shape radius token in pixels
    public final double getContainerShape() {
        return containerShape == null ? DEFAULT_CONTAINER_SHAPE : containerShape.get();
    }

    /// Sets the container shape radius token.
    ///
    /// @param containerShape the container shape radius token in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setContainerShape(double containerShape) {
        containerShapeProperty().set(M3Css.nonNegative(containerShape, "containerShape"));
    }

    /// Returns the `containerShape` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `999.0` logical pixels.
    ///
    /// @return the `containerShape` property
    public final StyleableDoubleProperty containerShapeProperty() {
        if (containerShape == null) {
            containerShape = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTAINER_SHAPE,
                    this,
                    "containerShape",
                    StyleableProperties.CONTAINER_SHAPE,
                    this::requestLayout
            );
        }
        return containerShape;
    }

    /// The padding on each horizontal side of the content, in logical pixels.
    ///
    /// @defaultValue `12.0`
    private @Nullable StyleableDoubleProperty horizontalPadding;

    /// Returns the horizontal content padding token.
    ///
    /// @return the horizontal content padding token in pixels
    public final double getHorizontalPadding() {
        return horizontalPadding == null ? DEFAULT_HORIZONTAL_PADDING : horizontalPadding.get();
    }

    /// Sets the horizontal content padding token.
    ///
    /// @param horizontalPadding the horizontal content padding token in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setHorizontalPadding(double horizontalPadding) {
        horizontalPaddingProperty().set(M3Css.nonNegative(horizontalPadding, "horizontalPadding"));
    }

    /// Returns the `horizontalPadding` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `12.0` logical pixels.
    ///
    /// @return the `horizontalPadding` property
    public final StyleableDoubleProperty horizontalPaddingProperty() {
        if (horizontalPadding == null) {
            horizontalPadding = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_HORIZONTAL_PADDING,
                    this,
                    "horizontalPadding",
                    StyleableProperties.HORIZONTAL_PADDING,
                    this::updateMetrics
            );
        }
        return horizontalPadding;
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the CSS metadata for this control class
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    ///
    /// @return the CSS metadata for this control
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Returns accessibility attributes for the segment selection state.
    ///
    /// @param attribute  the requested accessibility attribute
    /// @param parameters the optional attribute parameters
    /// @return the attribute value, or `null` when unavailable
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        if (getAccessibleRole() != AccessibleRole.BUTTON) {
            // JavaFX 17 has no TOGGLE_STATE enum constant, so test the optional runtime value first.
            if (M3Accessible.isToggleStateAttribute(attribute)) {
                return M3Accessible.toggleState(isSelected());
            }
            if (attribute == AccessibleAttribute.SELECTED) {
                return isSelected();
            }
        }
        return super.queryAccessibleAttribute(attribute, parameters);
    }

    /// Toggles and fires this segmented button.
    @Override
    public void fire() {
        if (!isDisabled()) {
            setSelected(!isSelected());
            fireEvent(new ActionEvent(this, this));
        }
    }

    /// Creates the default Material Design 3 segmented button skin.
    ///
    /// @return the default Material Design 3 segmented button skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3SegmentedButtonSkin(this);
    }

    /// Returns the user-agent stylesheet for M3FX segmented buttons.
    ///
    /// @return the segmented button user-agent stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("segmented-button.css");
    }

    /// Applies the accessibility role corresponding to a containing group's selection mode.
    ///
    /// @param selectionMode the containing group's selection mode
    void applyGroupSelectionMode(M3SelectionMode selectionMode) {
        setAccessibleRole(switch (selectionMode) {
            case SINGLE -> AccessibleRole.RADIO_BUTTON;
            case MULTIPLE -> AccessibleRole.CHECK_BOX;
            case NONE -> AccessibleRole.BUTTON;
        });
    }

    /// Restores the accessibility role used when this button is not owned by a segmented button group.
    void restoreStandaloneAccessibleRole() {
        setAccessibleRole(AccessibleRole.TOGGLE_BUTTON);
    }

    /// Adds base style classes and applies token metrics.
    private void initialize() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        restoreStandaloneAccessibleRole();
        setFocusTraversable(true);
        setPickOnBounds(true);
        updateMetrics();
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double height = getContainerHeight();
        double padding = getHorizontalPadding();
        M3Css.setMinHeightIfUnbound(this, height);
        M3Css.setPrefHeightIfUnbound(this, height);
        M3Css.setPaddingIfUnbound(this, new Insets(0.0, padding, 0.0, padding));
    }

    /// CSS metadata for M3FX segmented button component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the container height token.
        private static final CssMetaData<M3SegmentedButton, Number> CONTAINER_HEIGHT =
                new CssMetaData<>("-m3-container-height", SizeConverter.getInstance(), DEFAULT_CONTAINER_HEIGHT) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3SegmentedButton control) {
                        return M3Css.isSettable(control.containerHeightProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3SegmentedButton control) {
                        return control.containerHeightProperty();
                    }
                };

        /// CSS metadata for the container shape token.
        private static final CssMetaData<M3SegmentedButton, Number> CONTAINER_SHAPE =
                new CssMetaData<>("-m3-container-shape", SizeConverter.getInstance(), DEFAULT_CONTAINER_SHAPE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3SegmentedButton control) {
                        return M3Css.isSettable(control.containerShapeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3SegmentedButton control) {
                        return control.containerShapeProperty();
                    }
                };

        /// CSS metadata for the horizontal padding token.
        private static final CssMetaData<M3SegmentedButton, Number> HORIZONTAL_PADDING =
                new CssMetaData<>("-m3-horizontal-padding", SizeConverter.getInstance(), DEFAULT_HORIZONTAL_PADDING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3SegmentedButton control) {
                        return M3Css.isSettable(control.horizontalPaddingProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3SegmentedButton control) {
                        return control.horizontalPaddingProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(ButtonBase.getClassCssMetaData());
            styleables.add(CONTAINER_HEIGHT);
            styleables.add(CONTAINER_SHAPE);
            styleables.add(HORIZONTAL_PADDING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
