// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3ListItemSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// The common base class for Material Design 3 list-derived rows.
///
/// `M3ListItemBase` supplies the content, selection state, and action contract shared by [M3ListItem],
/// [M3MenuItem], and the setting-row controls such as [M3SwitchSettingItem], [M3SelectSettingItem], and
/// [M3ExpandableSettingItem]. Applications create one of those permitted concrete controls rather than extending
/// this class.
///
/// A row has a required headline and optional overline, supporting text, trailing supporting text, leading node,
/// and trailing node. Its line count starts at one for the headline and increases for populated overline and
/// supporting-text slots; trailing supporting text does not affect it. The resulting count selects the corresponding
/// one-, two-, or three-line height. Leading and trailing nodes are owned by the row while displayed and therefore
/// must not belong to another parent.
///
/// Calling [#fire()] on an enabled row first lets the concrete row update its own semantic value, then delivers an
/// [ActionEvent] to [#onActionProperty()] and other registered handlers when that activation was accepted. Plain
/// list and menu items leave [#selectedProperty()] independent of action dispatch, while setting rows use it as
/// their selected value. Containers such as [M3ListPane] and [M3Menu] apply their configured selection policy when
/// they handle an item action.
///
/// See [Material Design lists](https://m3.material.io/components/lists/overview).
@NotNullByDefault
public abstract sealed class M3ListItemBase extends Control permits M3ListItem, M3MenuItem, M3SettingItemBase {
    /// The base style class for M3FX list items.
    public static final String STYLE_CLASS = "m3-list-item";

    /// The selected pseudo-class used by list items.
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    /// The pseudo-class used by one-line list items.
    private static final PseudoClass ONE_LINE_PSEUDO_CLASS = PseudoClass.getPseudoClass("one-line");

    /// The pseudo-class used by two-line list items.
    private static final PseudoClass TWO_LINE_PSEUDO_CLASS = PseudoClass.getPseudoClass("two-line");

    /// The pseudo-class used by three-line list items.
    private static final PseudoClass THREE_LINE_PSEUDO_CLASS = PseudoClass.getPseudoClass("three-line");

    /// The default one-line item height.
    private static final double DEFAULT_ONE_LINE_HEIGHT = 56.0;

    /// The default two-line item height.
    private static final double DEFAULT_TWO_LINE_HEIGHT = 72.0;

    /// The default three-line item height.
    private static final double DEFAULT_THREE_LINE_HEIGHT = 88.0;

    /// The default list item container shape radius.
    private static final double DEFAULT_CONTAINER_SHAPE = 0.0;

    /// The default horizontal content padding.
    private static final double DEFAULT_HORIZONTAL_PADDING = 16.0;

    /// The default vertical content padding.
    private static final double DEFAULT_VERTICAL_PADDING = 8.0;

    /// The default spacing between list item content regions.
    private static final double DEFAULT_CONTENT_SPACING = 16.0;

    /// Creates an empty list item.
    protected M3ListItemBase() {
        this("");
    }

    /// Creates a one-line list item with headline text.
    ///
    /// @param headlineText the headline text displayed by the list item
    /// @throws NullPointerException if `headlineText` is `null`
    protected M3ListItemBase(String headlineText) {
        installLineCountListeners();
        M3ControlStyles.initialize(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.LIST_ITEM);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleNode, this::showAccessibleItem);
        setFocusTraversable(true);
        setPickOnBounds(true);
        focusNotifier.start();
        setHeadlineText(headlineText);
        updateLineCount();
        updateAccessibleText();
    }

    /// The optional text displayed above the headline.
    ///
    /// Both `null` and an empty string leave the slot visually empty and do not contribute to [#lineCountProperty()].
    ///
    /// @defaultValue `""`
    private final StringProperty overlineText = new SimpleStringProperty(this, "overlineText", "");

    /// Returns the overline text.
    ///
    /// @return the overline text, or `null` when the slot is empty
    public final @Nullable String getOverlineText() {
        return overlineText.get();
    }

    /// Sets the overline text.
    ///
    /// @param overlineText the overline text, or `null` to clear the slot
    public final void setOverlineText(@Nullable String overlineText) {
        this.overlineText.set(overlineText);
    }

    /// Returns the observable, bindable overline-text property.
    ///
    /// The property defaults to an empty string. Both `null` and an empty string represent an unpopulated slot.
    ///
    /// @return the overline-text property
    public final StringProperty overlineTextProperty() {
        return overlineText;
    }

    /// The primary text of this row.
    ///
    /// The property must contain a non-null value.
    ///
    /// @defaultValue `""`
    private final StringProperty headlineText = new SimpleStringProperty(this, "headlineText", "");

    /// Returns the headline text.
    ///
    /// @return the headline text
    public final String getHeadlineText() {
        return headlineText.get();
    }

    /// Sets the headline text.
    ///
    /// @param headlineText the headline text
    /// @throws NullPointerException if `headlineText` is `null`
    public final void setHeadlineText(String headlineText) {
        this.headlineText.set(Objects.requireNonNull(headlineText, "headlineText"));
    }

    /// Returns the observable, bindable headline-text property.
    ///
    /// The property defaults to an empty string and must contain a non-null value.
    ///
    /// @return the headline-text property
    public final StringProperty headlineTextProperty() {
        return headlineText;
    }

    /// The optional supporting text displayed below the headline.
    ///
    /// Both `null` and an empty string leave the slot visually empty and do not contribute to [#lineCountProperty()].
    ///
    /// @defaultValue `""`
    private final StringProperty supportingText = new SimpleStringProperty(this, "supportingText", "");

    /// Returns the supporting text.
    ///
    /// @return the supporting text, or `null` when the slot is empty
    public final @Nullable String getSupportingText() {
        return supportingText.get();
    }

    /// Sets the supporting text.
    ///
    /// @param supportingText the supporting text, or `null` to clear the slot
    public final void setSupportingText(@Nullable String supportingText) {
        this.supportingText.set(supportingText);
    }

    /// Returns the observable, bindable supporting-text property.
    ///
    /// The property defaults to an empty string. Both `null` and an empty string represent an unpopulated slot.
    ///
    /// @return the supporting-text property
    public final StringProperty supportingTextProperty() {
        return supportingText;
    }

    /// The optional supporting text displayed at the logical trailing edge.
    ///
    /// Both `null` and an empty string leave the slot visually empty. This text does not affect
    /// [#lineCountProperty()].
    ///
    /// @defaultValue `""`
    private final StringProperty trailingSupportingText =
            new SimpleStringProperty(this, "trailingSupportingText", "");

    /// Returns the trailing supporting text.
    ///
    /// @return the trailing supporting text, or `null` when the slot is empty
    public final @Nullable String getTrailingSupportingText() {
        return trailingSupportingText.get();
    }

    /// Sets the trailing supporting text.
    ///
    /// @param trailingSupportingText the trailing supporting text, or `null` to clear the slot
    public final void setTrailingSupportingText(@Nullable String trailingSupportingText) {
        this.trailingSupportingText.set(trailingSupportingText);
    }

    /// Returns the observable, bindable trailing supporting-text property.
    ///
    /// The property defaults to an empty string. Both `null` and an empty string represent an unpopulated slot,
    /// and the value does not affect [#lineCountProperty()].
    ///
    /// @return the trailing supporting-text property
    public final StringProperty trailingSupportingTextProperty() {
        return trailingSupportingText;
    }

    /// The optional node displayed at the logical leading edge.
    ///
    /// The node is owned by this row while displayed and may have only one parent.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Node> leading = new SimpleObjectProperty<>(this, "leading") {
        /// Updates accessibility slots when leading content changes.
        @Override
        protected void invalidated() {
            notifyAccessibleSlotsChanged();
        }
    };

    /// Returns the leading content node.
    ///
    /// @return the leading content node, or `null`
    public final @Nullable Node getLeading() {
        return leading.get();
    }

    /// Sets the leading content node.
    ///
    /// @param leading the leading content node, or `null`
    public final void setLeading(@Nullable Node leading) {
        this.leading.set(leading);
    }

    /// Returns the observable, bindable leading content-node property.
    ///
    /// The property defaults to `null`. A non-null node is owned by this row while displayed and may have only one
    /// parent.
    ///
    /// @return the leading content-node property
    public final ObjectProperty<@Nullable Node> leadingProperty() {
        return leading;
    }

    /// The optional node displayed at the logical trailing edge.
    ///
    /// The node is owned by this row while displayed and may have only one parent.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Node> trailing = new SimpleObjectProperty<>(this, "trailing") {
        /// Updates accessibility slots when trailing content changes.
        @Override
        protected void invalidated() {
            notifyAccessibleSlotsChanged();
        }
    };

    /// Returns the trailing content node.
    ///
    /// @return the trailing content node, or `null`
    public final @Nullable Node getTrailing() {
        return trailing.get();
    }

    /// Sets the trailing content node.
    ///
    /// @param trailing the trailing content node, or `null`
    public final void setTrailing(@Nullable Node trailing) {
        this.trailing.set(trailing);
    }

    /// Returns the observable, bindable trailing content-node property.
    ///
    /// The property defaults to `null`. A non-null node is owned by this row while displayed and may have only one
    /// parent.
    ///
    /// @return the trailing content-node property
    public final ObjectProperty<@Nullable Node> trailingProperty() {
        return trailing;
    }

    /// The measurement role for [#leadingProperty()].
    ///
    /// [M3ListItemSlotSize#AUTO] measures arbitrary content without applying a fixed Material media slot. A direct
    /// assignment of `null` is replaced with [M3ListItemSlotSize#AUTO].
    ///
    /// @defaultValue [M3ListItemSlotSize#AUTO]
    private final ObjectProperty<M3ListItemSlotSize> leadingSlotSize =
            new SimpleObjectProperty<>(this, "leadingSlotSize", M3ListItemSlotSize.AUTO) {
                /// Restores the default slot size when a null value is assigned through the property.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3ListItemSlotSize.AUTO);
                    }
                }
            };

    /// Returns the leading content slot size.
    ///
    /// @return the leading content slot size
    public final M3ListItemSlotSize getLeadingSlotSize() {
        return Objects.requireNonNull(leadingSlotSize.get(), "leadingSlotSize");
    }

    /// Sets the leading content slot size.
    ///
    /// @param leadingSlotSize the leading content slot size
    /// @throws NullPointerException if `leadingSlotSize` is `null`
    public final void setLeadingSlotSize(M3ListItemSlotSize leadingSlotSize) {
        this.leadingSlotSize.set(Objects.requireNonNull(leadingSlotSize, "leadingSlotSize"));
    }

    /// Returns the observable, bindable leading slot-size property.
    ///
    /// The property defaults to [M3ListItemSlotSize#AUTO]. A `null` value assigned directly through the property is
    /// replaced with that default.
    ///
    /// @return the leading slot-size property
    public final ObjectProperty<M3ListItemSlotSize> leadingSlotSizeProperty() {
        return leadingSlotSize;
    }

    /// The measurement role for [#trailingProperty()].
    ///
    /// [M3ListItemSlotSize#AUTO] measures arbitrary content without applying a fixed Material media slot. A direct
    /// assignment of `null` is replaced with [M3ListItemSlotSize#AUTO].
    ///
    /// @defaultValue [M3ListItemSlotSize#AUTO]
    private final ObjectProperty<M3ListItemSlotSize> trailingSlotSize =
            new SimpleObjectProperty<>(this, "trailingSlotSize", M3ListItemSlotSize.AUTO) {
                /// Restores the default slot size when a null value is assigned through the property.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3ListItemSlotSize.AUTO);
                    }
                }
            };

    /// Returns the trailing content slot size.
    ///
    /// @return the trailing content slot size
    public final M3ListItemSlotSize getTrailingSlotSize() {
        return Objects.requireNonNull(trailingSlotSize.get(), "trailingSlotSize");
    }

    /// Sets the trailing content slot size.
    ///
    /// @param trailingSlotSize the trailing content slot size
    /// @throws NullPointerException if `trailingSlotSize` is `null`
    public final void setTrailingSlotSize(M3ListItemSlotSize trailingSlotSize) {
        this.trailingSlotSize.set(Objects.requireNonNull(trailingSlotSize, "trailingSlotSize"));
    }

    /// Returns the observable, bindable trailing slot-size property.
    ///
    /// The property defaults to [M3ListItemSlotSize#AUTO]. A `null` value assigned directly through the property is
    /// replaced with that default.
    ///
    /// @return the trailing slot-size property
    public final ObjectProperty<M3ListItemSlotSize> trailingSlotSizeProperty() {
        return trailingSlotSize;
    }

    /// The action handler invoked for [ActionEvent#ACTION].
    ///
    /// Setting a new value replaces the handler previously installed through this property. Additional handlers
    /// registered with [addEventHandler] remain installed.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable EventHandler<ActionEvent>> onAction =
            new SimpleObjectProperty<>(this, "onAction") {
                /// Updates the registered action event handler.
                @Override
                protected void invalidated() {
                    setEventHandler(ActionEvent.ACTION, get());
                }
            };

    /// Returns the action handler.
    ///
    /// @return the action handler, or `null`
    public final @Nullable EventHandler<ActionEvent> getOnAction() {
        return onAction.get();
    }

    /// Sets the action handler.
    ///
    /// @param onAction the action handler, or `null`
    public final void setOnAction(@Nullable EventHandler<ActionEvent> onAction) {
        this.onAction.set(onAction);
    }

    /// Returns the observable, bindable action-handler property.
    ///
    /// The property defaults to `null`. Changing it replaces the handler registered for [ActionEvent#ACTION]
    /// through this property without affecting handlers added through [addEventHandler].
    ///
    /// @return the action-handler property
    public final ObjectProperty<@Nullable EventHandler<ActionEvent>> onActionProperty() {
        return onAction;
    }

    /// Whether this row is selected or, for a setting row, whether its boolean value is selected.
    ///
    /// Changing the property updates visual and accessibility state but does not fire an action event. A containing
    /// selection control may change a plain list or menu item's value again to maintain its selection policy. Setting
    /// rows use the property as their control value and update it during accepted activation.
    ///
    /// @defaultValue `false`
    private final BooleanProperty selected = new SimpleBooleanProperty(this, "selected") {
        /// Updates selected pseudo-class state.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, get());
            notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED);
        }
    };

    /// Returns whether this row is selected.
    ///
    /// For a setting row, the result is its boolean control value.
    ///
    /// @return `true` when this row is selected
    public final boolean isSelected() {
        return selected.get();
    }

    /// Sets whether this row is selected.
    ///
    /// For a setting row, this directly changes its boolean control value without firing an action event.
    ///
    /// @param selected whether this row is selected
    public final void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    /// Returns the observable, bindable selected-state property.
    ///
    /// The property defaults to `false`. Changing it does not fire an action event. Plain list and menu items remain
    /// subject to an owning selection control's policy; setting rows use it as their boolean control value.
    ///
    /// @return the selected-state property
    public final BooleanProperty selectedProperty() {
        return selected;
    }

    /// The line count derived from populated overline and supporting-text slots.
    private final ReadOnlyObjectWrapper<M3ListItemLineCount> lineCount =
            new ReadOnlyObjectWrapper<>(this, "lineCount", M3ListItemLineCount.ONE_LINE);

    /// Returns the derived list item line count.
    ///
    /// @return the derived list item line count
    public final M3ListItemLineCount getLineCount() {
        return lineCount.get();
    }

    /// Returns the observable read-only derived line-count property.
    ///
    /// The property initially contains [M3ListItemLineCount#ONE_LINE]. It can be used as a binding source but cannot
    /// be set or bound as a writable target.
    ///
    /// @return the derived line-count property
    public final ReadOnlyObjectProperty<M3ListItemLineCount> lineCountProperty() {
        return lineCount.getReadOnlyProperty();
    }

    /// The preferred height of a one-line row in logical pixels.
    ///
    /// Values must be finite and non-negative.
    ///
    /// @defaultValue `56.0`
    private @Nullable StyleableDoubleProperty oneLineHeight;

    /// Returns the preferred one-line row height in logical pixels.
    ///
    /// @return the preferred one-line row height
    public final double getOneLineHeight() {
        return oneLineHeight == null ? DEFAULT_ONE_LINE_HEIGHT : oneLineHeight.get();
    }

    /// Sets the preferred one-line row height in logical pixels.
    ///
    /// @param oneLineHeight the preferred one-line row height
    /// @throws IllegalArgumentException if `oneLineHeight` is negative or not finite
    public final void setOneLineHeight(double oneLineHeight) {
        oneLineHeightProperty().set(M3Css.nonNegative(oneLineHeight, "oneLineHeight"));
    }

    /// Returns the observable, bindable, styleable one-line row height property.
    ///
    /// The property defaults to `56.0` logical pixels and accepts only finite, non-negative values. CSS cannot set
    /// the property while it is bound.
    ///
    /// @return the one-line row height property
    public final StyleableDoubleProperty oneLineHeightProperty() {
        if (oneLineHeight == null) {
            oneLineHeight = createStyleableDoubleProperty(
                    DEFAULT_ONE_LINE_HEIGHT,
                    "oneLineHeight",
                    StyleableProperties.ONE_LINE_HEIGHT
            );
        }
        return oneLineHeight;
    }

    /// The preferred height of a two-line row in logical pixels.
    ///
    /// Values must be finite and non-negative.
    ///
    /// @defaultValue `72.0`
    private @Nullable StyleableDoubleProperty twoLineHeight;

    /// Returns the preferred two-line row height in logical pixels.
    ///
    /// @return the preferred two-line row height
    public final double getTwoLineHeight() {
        return twoLineHeight == null ? DEFAULT_TWO_LINE_HEIGHT : twoLineHeight.get();
    }

    /// Sets the preferred two-line row height in logical pixels.
    ///
    /// @param twoLineHeight the preferred two-line row height
    /// @throws IllegalArgumentException if `twoLineHeight` is negative or not finite
    public final void setTwoLineHeight(double twoLineHeight) {
        twoLineHeightProperty().set(M3Css.nonNegative(twoLineHeight, "twoLineHeight"));
    }

    /// Returns the observable, bindable, styleable two-line row height property.
    ///
    /// The property defaults to `72.0` logical pixels and accepts only finite, non-negative values. CSS cannot set
    /// the property while it is bound.
    ///
    /// @return the two-line row height property
    public final StyleableDoubleProperty twoLineHeightProperty() {
        if (twoLineHeight == null) {
            twoLineHeight = createStyleableDoubleProperty(
                    DEFAULT_TWO_LINE_HEIGHT,
                    "twoLineHeight",
                    StyleableProperties.TWO_LINE_HEIGHT
            );
        }
        return twoLineHeight;
    }

    /// The preferred height of a three-line row in logical pixels.
    ///
    /// Values must be finite and non-negative.
    ///
    /// @defaultValue `88.0`
    private @Nullable StyleableDoubleProperty threeLineHeight;

    /// Returns the preferred three-line row height in logical pixels.
    ///
    /// @return the preferred three-line row height
    public final double getThreeLineHeight() {
        return threeLineHeight == null ? DEFAULT_THREE_LINE_HEIGHT : threeLineHeight.get();
    }

    /// Sets the preferred three-line row height in logical pixels.
    ///
    /// @param threeLineHeight the preferred three-line row height
    /// @throws IllegalArgumentException if `threeLineHeight` is negative or not finite
    public final void setThreeLineHeight(double threeLineHeight) {
        threeLineHeightProperty().set(M3Css.nonNegative(threeLineHeight, "threeLineHeight"));
    }

    /// Returns the observable, bindable, styleable three-line row height property.
    ///
    /// The property defaults to `88.0` logical pixels and accepts only finite, non-negative values. CSS cannot set
    /// the property while it is bound.
    ///
    /// @return the three-line row height property
    public final StyleableDoubleProperty threeLineHeightProperty() {
        if (threeLineHeight == null) {
            threeLineHeight = createStyleableDoubleProperty(
                    DEFAULT_THREE_LINE_HEIGHT,
                    "threeLineHeight",
                    StyleableProperties.THREE_LINE_HEIGHT
            );
        }
        return threeLineHeight;
    }

    /// The resting container corner radius in logical pixels.
    ///
    /// Values must be finite and non-negative.
    ///
    /// @defaultValue `0.0`
    private @Nullable StyleableDoubleProperty containerShape;

    /// Returns the resting container corner radius in logical pixels.
    ///
    /// @return the container corner radius
    public final double getContainerShape() {
        return containerShape == null ? DEFAULT_CONTAINER_SHAPE : containerShape.get();
    }

    /// Sets the resting container corner radius in logical pixels.
    ///
    /// @param containerShape the container corner radius
    /// @throws IllegalArgumentException if `containerShape` is negative or not finite
    public final void setContainerShape(double containerShape) {
        containerShapeProperty().set(M3Css.nonNegative(containerShape, "containerShape"));
    }

    /// Returns the observable, bindable, styleable container corner-radius property.
    ///
    /// The property defaults to `0.0` logical pixels and accepts only finite, non-negative values. CSS cannot set
    /// the property while it is bound.
    ///
    /// @return the container corner-radius property
    public final StyleableDoubleProperty containerShapeProperty() {
        if (containerShape == null) {
            containerShape = createStyleableDoubleProperty(
                    DEFAULT_CONTAINER_SHAPE,
                    "containerShape",
                    StyleableProperties.CONTAINER_SHAPE
            );
        }
        return containerShape;
    }

    /// The padding at the logical leading and trailing edges in logical pixels.
    ///
    /// Values must be finite and non-negative.
    ///
    /// @defaultValue `16.0`
    private @Nullable StyleableDoubleProperty horizontalPadding;

    /// Returns the horizontal content padding in logical pixels.
    ///
    /// @return the horizontal content padding
    public final double getHorizontalPadding() {
        return horizontalPadding == null ? DEFAULT_HORIZONTAL_PADDING : horizontalPadding.get();
    }

    /// Sets the horizontal content padding in logical pixels.
    ///
    /// @param horizontalPadding the horizontal content padding
    /// @throws IllegalArgumentException if `horizontalPadding` is negative or not finite
    public final void setHorizontalPadding(double horizontalPadding) {
        horizontalPaddingProperty().set(M3Css.nonNegative(horizontalPadding, "horizontalPadding"));
    }

    /// Returns the observable, bindable, styleable horizontal content-padding property.
    ///
    /// The property defaults to `16.0` logical pixels and accepts only finite, non-negative values. CSS cannot set
    /// the property while it is bound.
    ///
    /// @return the horizontal content-padding property
    public final StyleableDoubleProperty horizontalPaddingProperty() {
        if (horizontalPadding == null) {
            horizontalPadding = createStyleableDoubleProperty(
                    DEFAULT_HORIZONTAL_PADDING,
                    "horizontalPadding",
                    StyleableProperties.HORIZONTAL_PADDING
            );
        }
        return horizontalPadding;
    }

    /// The padding at the top and bottom edges in logical pixels.
    ///
    /// Values must be finite and non-negative.
    ///
    /// @defaultValue `8.0`
    private @Nullable StyleableDoubleProperty verticalPadding;

    /// Returns the vertical content padding in logical pixels.
    ///
    /// @return the vertical content padding
    public final double getVerticalPadding() {
        return verticalPadding == null ? DEFAULT_VERTICAL_PADDING : verticalPadding.get();
    }

    /// Sets the vertical content padding in logical pixels.
    ///
    /// @param verticalPadding the vertical content padding
    /// @throws IllegalArgumentException if `verticalPadding` is negative or not finite
    public final void setVerticalPadding(double verticalPadding) {
        verticalPaddingProperty().set(M3Css.nonNegative(verticalPadding, "verticalPadding"));
    }

    /// Returns the observable, bindable, styleable vertical content-padding property.
    ///
    /// The property defaults to `8.0` logical pixels and accepts only finite, non-negative values. CSS cannot set
    /// the property while it is bound.
    ///
    /// @return the vertical content-padding property
    public final StyleableDoubleProperty verticalPaddingProperty() {
        if (verticalPadding == null) {
            verticalPadding = createStyleableDoubleProperty(
                    DEFAULT_VERTICAL_PADDING,
                    "verticalPadding",
                    StyleableProperties.VERTICAL_PADDING
            );
        }
        return verticalPadding;
    }

    /// The spacing between the leading, text, and trailing content regions in logical pixels.
    ///
    /// Values must be finite and non-negative.
    ///
    /// @defaultValue `16.0`
    private @Nullable StyleableDoubleProperty contentSpacing;

    /// Returns the spacing between content regions in logical pixels.
    ///
    /// @return the content-region spacing
    public final double getContentSpacing() {
        return contentSpacing == null ? DEFAULT_CONTENT_SPACING : contentSpacing.get();
    }

    /// Sets the spacing between content regions in logical pixels.
    ///
    /// @param contentSpacing the content-region spacing
    /// @throws IllegalArgumentException if `contentSpacing` is negative or not finite
    public final void setContentSpacing(double contentSpacing) {
        contentSpacingProperty().set(M3Css.nonNegative(contentSpacing, "contentSpacing"));
    }

    /// Returns the observable, bindable, styleable content-region spacing property.
    ///
    /// The property defaults to `16.0` logical pixels and accepts only finite, non-negative values. CSS cannot set
    /// the property while it is bound.
    ///
    /// @return the content-region spacing property
    public final StyleableDoubleProperty contentSpacingProperty() {
        if (contentSpacing == null) {
            contentSpacing = createStyleableDoubleProperty(
                    DEFAULT_CONTENT_SPACING,
                    "contentSpacing",
                    StyleableProperties.CONTENT_SPACING
            );
        }
        return contentSpacing;
    }

    /// Notifies accessibility clients when focus moves between the row and its leading or trailing slots.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, this::accessibleFocusNode);

    /// Sets the leading content node and its list item slot size.
    ///
    /// @param leading  the leading content node, or `null`
    /// @param slotSize the leading content slot size
    /// @throws NullPointerException if `slotSize` is `null`
    public final void setLeadingMedia(@Nullable Node leading, M3ListItemSlotSize slotSize) {
        setLeading(leading);
        setLeadingSlotSize(slotSize);
    }

    /// Sets a medium [M3Icon] as leading content and returns it for further customization.
    ///
    /// @param iconText the glyph text rendered by the leading icon
    /// @return the created leading icon
    /// @throws NullPointerException if `iconText` is `null`
    public final M3Icon setLeadingIcon(String iconText) {
        M3Icon icon = new M3Icon(iconText, M3IconSize.MEDIUM, M3IconVariant.ON_SURFACE_VARIANT);
        setLeadingMedia(icon, M3ListItemSlotSize.ICON);
        return icon;
    }

    /// Sets an [M3Avatar] as leading content and returns it for further customization.
    ///
    /// @param text the avatar text
    /// @return the created leading avatar
    /// @throws NullPointerException if `text` is `null`
    public final M3Avatar setLeadingAvatar(String text) {
        M3Avatar avatar = new M3Avatar(text);
        setLeadingMedia(avatar, M3ListItemSlotSize.AVATAR);
        return avatar;
    }

    /// Sets a square thumbnail as leading content.
    ///
    /// @param thumbnail the square thumbnail node
    /// @throws NullPointerException if `thumbnail` is `null`
    public final void setLeadingThumbnail(Node thumbnail) {
        setLeadingMedia(Objects.requireNonNull(thumbnail, "thumbnail"), M3ListItemSlotSize.THUMBNAIL);
    }

    /// Sets a wide thumbnail as leading content.
    ///
    /// @param thumbnail the wide thumbnail node
    /// @throws NullPointerException if `thumbnail` is `null`
    public final void setLeadingWideThumbnail(Node thumbnail) {
        setLeadingMedia(Objects.requireNonNull(thumbnail, "thumbnail"), M3ListItemSlotSize.WIDE_THUMBNAIL);
    }

    /// Sets the trailing content node and its list item slot size.
    ///
    /// @param trailing the trailing content node, or `null`
    /// @param slotSize the trailing content slot size
    /// @throws NullPointerException if `slotSize` is `null`
    public final void setTrailingMedia(@Nullable Node trailing, M3ListItemSlotSize slotSize) {
        setTrailing(trailing);
        setTrailingSlotSize(slotSize);
    }

    /// Sets a medium [M3Icon] as trailing content and returns it for further customization.
    ///
    /// @param iconText the glyph text rendered by the trailing icon
    /// @return the created trailing icon
    /// @throws NullPointerException if `iconText` is `null`
    public final M3Icon setTrailingIcon(String iconText) {
        M3Icon icon = new M3Icon(iconText, M3IconSize.MEDIUM, M3IconVariant.ON_SURFACE_VARIANT);
        setTrailingMedia(icon, M3ListItemSlotSize.ICON);
        return icon;
    }

    /// Fires this row's action event if it is enabled and accepts activation.
    ///
    /// The event is delivered through the normal JavaFX event dispatch chain. No event is created or delivered
    /// while the row is disabled. Plain list and menu items do not directly change [#selectedProperty()]; setting
    /// rows may update their value before the event is delivered. A row may reject activation, for example when a
    /// grouped radio setting is already selected; rejected activation does not deliver an event.
    public final void fire() {
        if (!isDisabled() && prepareAction()) {
            dispatchActionEvent();
        }
    }

    /// Delivers an [ActionEvent] for this row without running [#prepareAction()].
    ///
    /// Setting rows use this when a nested interactive value control has already applied the value transition and the
    /// row must only notify action listeners.
    final void dispatchActionEvent() {
        if (!isDisabled()) {
            Event.fireEvent(this, new ActionEvent(this, this));
        }
    }

    /// Prepares an enabled row for action dispatch.
    ///
    /// The default implementation accepts activation without changing state. Setting-row implementations override
    /// this hook to update their value, or to reject an activation that has no observable effect.
    ///
    /// @return `true` when [#fire()] must deliver an action event
    boolean prepareAction() {
        return true;
    }

    /// Returns accessibility attributes for list item selection and position.
    ///
    /// @param attribute  the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` if the attribute is not supported
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case SELECTED -> isSelected();
            case INDEX -> M3Accessible.indexInParent(this);
            case ITEM_COUNT -> M3Accessible.itemCount(getLeading(), getTrailing());
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getLeading(), getTrailing(), parameters);
            case FOCUS_NODE -> accessibleFocusNode();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions supported by list items.
    ///
    /// @param action     the accessibility action to execute
    /// @param parameters optional action-specific parameters
    /// @throws NullPointerException if `action` is `null`
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        if (isDisabled()) {
            super.executeAccessibleAction(action, parameters);
            return;
        }

        switch (action) {
            case FIRE -> fire();
            case REQUEST_FOCUS -> focusAccessibleNode();
            case SHOW_ITEM -> showAccessibleItem(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Returns the current list row or slot accessibility focus node.
    private @Nullable Node accessibleFocusNode() {
        @Nullable Node currentTarget = M3Accessible.currentFocusTarget(this, getLeading(), getTrailing());
        if (currentTarget != null) {
            return currentTarget;
        }
        @Nullable Node itemTarget = M3Accessible.focusTarget(this);
        return itemTarget != null ? itemTarget : M3Accessible.firstFocusTarget(getLeading(), getTrailing());
    }

    /// Focuses the current row or slot target, or an explicitly requested slot target.
    ///
    /// @param parameters optional accessibility target parameters
    /// @return `true` when focus moved to the default or requested target
    final boolean showAccessibleItem(Object... parameters) {
        boolean shown = parameters.length == 0
                ? M3Accessible.showItem(this, accessibleFocusNode())
                : M3Accessible.showCurrentOrItem(this, getLeading(), getTrailing(), parameters);
        if (shown) {
            notifyAccessibleFocusChanged();
        }
        return shown;
    }

    /// Requests focus on the current list item accessibility focus node.
    ///
    /// @return `true` when the target accepted focus
    final boolean focusAccessibleNode() {
        if (M3Accessible.showItem(this, accessibleFocusNode())) {
            notifyAccessibleFocusChanged();
            return true;
        }
        return false;
    }

    /// Notifies accessibility clients that the list item focus target changed.
    private void notifyAccessibleFocusChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Notifies accessibility clients that leading or trailing accessibility slots changed.
    private void notifyAccessibleSlotsChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        notifyAccessibleFocusChanged();
    }

    /// Creates the default list item skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3ListItemSkin(this);
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the CSS metadata for this control class
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Returns the user-agent stylesheet for M3FX list items.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("list-item.css");
    }

    /// Installs listeners that keep the derived line count in sync with text content.
    private void installLineCountListeners() {
        overlineText.addListener((observable, oldValue, newValue) -> {
            updateLineCount();
            updateAccessibleText();
        });
        headlineText.addListener((observable, oldValue, newValue) -> updateAccessibleText());
        supportingText.addListener((observable, oldValue, newValue) -> {
            updateLineCount();
            updateAccessibleText();
        });
        trailingSupportingText.addListener((observable, oldValue, newValue) -> updateAccessibleText());
    }

    /// Updates the derived line count and related pseudo-class state.
    private void updateLineCount() {
        M3ListItemLineCount currentLineCount = computeLineCount();
        lineCount.set(currentLineCount);
        pseudoClassStateChanged(ONE_LINE_PSEUDO_CLASS, currentLineCount == M3ListItemLineCount.ONE_LINE);
        pseudoClassStateChanged(TWO_LINE_PSEUDO_CLASS, currentLineCount == M3ListItemLineCount.TWO_LINE);
        pseudoClassStateChanged(THREE_LINE_PSEUDO_CLASS, currentLineCount == M3ListItemLineCount.THREE_LINE);
    }

    /// Computes the line count implied by the current text content.
    private M3ListItemLineCount computeLineCount() {
        boolean hasOverline = hasText(getOverlineText());
        boolean hasSupporting = hasText(getSupportingText());
        if (hasOverline && hasSupporting) {
            return M3ListItemLineCount.THREE_LINE;
        }
        if (hasOverline || hasSupporting) {
            return M3ListItemLineCount.TWO_LINE;
        }
        return M3ListItemLineCount.ONE_LINE;
    }

    /// Returns whether text contributes visible list item content.
    private static boolean hasText(@Nullable String text) {
        return text != null && !text.isBlank();
    }

    /// Updates the accessibility label from the visible list item text.
    private void updateAccessibleText() {
        StringBuilder builder = new StringBuilder();
        appendAccessibleText(builder, getOverlineText());
        appendAccessibleText(builder, getHeadlineText());
        appendAccessibleText(builder, getSupportingText());
        appendAccessibleText(builder, getTrailingSupportingText());
        setAccessibleText(builder.length() == 0 ? null : builder.toString());
    }

    /// Appends a non-blank text part to an accessibility label.
    private static void appendAccessibleText(StringBuilder builder, @Nullable String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(' ');
        }
        builder.append(text);
    }

    /// Creates a non-negative styleable double property.
    private StyleableDoubleProperty createStyleableDoubleProperty(
            double initialValue,
            String name,
            CssMetaData<M3ListItemBase, Number> cssMetaData
    ) {
        return M3Css.nonNegativeStyleableDoubleProperty(
                initialValue,
                this,
                name,
                cssMetaData,
                this::requestLayout
        );
    }

    /// CSS metadata for M3FX list item component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the one-line height token.
        private static final CssMetaData<M3ListItemBase, Number> ONE_LINE_HEIGHT =
                createSizeCssMetaData("-m3-one-line-height", DEFAULT_ONE_LINE_HEIGHT, M3ListItemBase::oneLineHeightProperty);

        /// CSS metadata for the two-line height token.
        private static final CssMetaData<M3ListItemBase, Number> TWO_LINE_HEIGHT =
                createSizeCssMetaData("-m3-two-line-height", DEFAULT_TWO_LINE_HEIGHT, M3ListItemBase::twoLineHeightProperty);

        /// CSS metadata for the three-line height token.
        private static final CssMetaData<M3ListItemBase, Number> THREE_LINE_HEIGHT =
                createSizeCssMetaData("-m3-three-line-height", DEFAULT_THREE_LINE_HEIGHT, M3ListItemBase::threeLineHeightProperty);

        /// CSS metadata for the container shape token.
        private static final CssMetaData<M3ListItemBase, Number> CONTAINER_SHAPE =
                createSizeCssMetaData("-m3-container-shape", DEFAULT_CONTAINER_SHAPE, M3ListItemBase::containerShapeProperty);

        /// CSS metadata for the horizontal padding token.
        private static final CssMetaData<M3ListItemBase, Number> HORIZONTAL_PADDING =
                createSizeCssMetaData("-m3-horizontal-padding", DEFAULT_HORIZONTAL_PADDING, M3ListItemBase::horizontalPaddingProperty);

        /// CSS metadata for the vertical padding token.
        private static final CssMetaData<M3ListItemBase, Number> VERTICAL_PADDING =
                createSizeCssMetaData("-m3-vertical-padding", DEFAULT_VERTICAL_PADDING, M3ListItemBase::verticalPaddingProperty);

        /// CSS metadata for the content spacing token.
        private static final CssMetaData<M3ListItemBase, Number> CONTENT_SPACING =
                createSizeCssMetaData("-m3-content-spacing", DEFAULT_CONTENT_SPACING, M3ListItemBase::contentSpacingProperty);

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(ONE_LINE_HEIGHT);
            styleables.add(TWO_LINE_HEIGHT);
            styleables.add(THREE_LINE_HEIGHT);
            styleables.add(CONTAINER_SHAPE);
            styleables.add(HORIZONTAL_PADDING);
            styleables.add(VERTICAL_PADDING);
            styleables.add(CONTENT_SPACING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }

        /// Creates CSS metadata for a size token.
        private static CssMetaData<M3ListItemBase, Number> createSizeCssMetaData(
                String property,
                double initialValue,
                StyleablePropertyProvider provider
        ) {
            return new CssMetaData<>(property, SizeConverter.getInstance(), initialValue) {
                /// Returns whether this property can be set by CSS.
                @Override
                public boolean isSettable(M3ListItemBase control) {
                    return M3Css.isSettable(provider.property(control));
                }

                /// Returns the styleable property for a control.
                @Override
                public StyleableProperty<Number> getStyleableProperty(M3ListItemBase control) {
                    return provider.property(control);
                }
            };
        }
    }

    /// Provides a styleable double property for a list item.
    @FunctionalInterface
    @NotNullByDefault
    private interface StyleablePropertyProvider {
        /// Returns the styleable property for a list item.
        StyleableDoubleProperty property(M3ListItemBase control);
    }
}
