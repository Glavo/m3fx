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

/// A Material Design 3 list item.
///
/// `M3ListItem` represents one row in a Material list, menu-like list, or navigation drawer. It supports
/// overline, headline, supporting, and trailing supporting text, leading and trailing slots, one-line through
/// three-line metrics, selection state, action events, and keyboard activation. Container controls such as
/// [M3ListPane], [M3ListView], and [M3NavigationDrawer] can manage the selected state for groups of items.
///
/// See [Material Design lists](https://m3.material.io/components/lists/overview).
@NotNullByDefault
public class M3ListItem extends Control {
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

    // The overline text property.
    private final StringProperty overlineText = new SimpleStringProperty(this, "overlineText", "");

    // The headline text property.
    private final StringProperty headlineText = new SimpleStringProperty(this, "headlineText", "");

    // The supporting text property.
    private final StringProperty supportingText = new SimpleStringProperty(this, "supportingText", "");

    // The trailing supporting text property.
    private final StringProperty trailingSupportingText =
            new SimpleStringProperty(this, "trailingSupportingText", "");

    // The leading content node property.
    private final ObjectProperty<@Nullable Node> leading = new SimpleObjectProperty<>(this, "leading") {
        /// Updates accessibility slots when leading content changes.
        @Override
        protected void invalidated() {
            notifyAccessibleSlotsChanged();
        }
    };

    // The trailing content node property.
    private final ObjectProperty<@Nullable Node> trailing = new SimpleObjectProperty<>(this, "trailing") {
        /// Updates accessibility slots when trailing content changes.
        @Override
        protected void invalidated() {
            notifyAccessibleSlotsChanged();
        }
    };

    /// Notifies accessibility clients when focus moves between the row and its leading or trailing slots.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, this::accessibleFocusNode);

    // The leading content slot size.
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

    // The trailing content slot size.
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

    // The action handler property.
    private final ObjectProperty<@Nullable EventHandler<ActionEvent>> onAction =
            new SimpleObjectProperty<>(this, "onAction") {
                /// Updates the registered action event handler.
                @Override
                protected void invalidated() {
                    setEventHandler(ActionEvent.ACTION, get());
                }
            };

    // The selected state property.
    private final BooleanProperty selected = new SimpleBooleanProperty(this, "selected") {
        /// Updates selected pseudo-class state.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, get());
            notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED);
        }
    };

    // The derived line count property.
    private final ReadOnlyObjectWrapper<M3ListItemLineCount> lineCount =
            new ReadOnlyObjectWrapper<>(this, "lineCount", M3ListItemLineCount.ONE_LINE);

    // The styleable one-line height token.
    private @Nullable StyleableDoubleProperty oneLineHeight;

    // The styleable two-line height token.
    private @Nullable StyleableDoubleProperty twoLineHeight;

    // The styleable three-line height token.
    private @Nullable StyleableDoubleProperty threeLineHeight;

    // The styleable container shape token.
    private @Nullable StyleableDoubleProperty containerShape;

    // The styleable horizontal padding token.
    private @Nullable StyleableDoubleProperty horizontalPadding;

    // The styleable vertical padding token.
    private @Nullable StyleableDoubleProperty verticalPadding;

    // The styleable content spacing token.
    private @Nullable StyleableDoubleProperty contentSpacing;

    /// Creates an empty list item.
    public M3ListItem() {
        this("");
    }

    /// Creates a one-line list item with headline text.
    ///
    /// @param headlineText the headline text displayed by the list item
    public M3ListItem(String headlineText) {
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

    /// Returns the overline text.
    ///
    /// @return the overline text
    public final String getOverlineText() {
        return overlineText.get();
    }

    /// Sets the overline text.
    ///
    /// @param overlineText the overline text
    public final void setOverlineText(String overlineText) {
        this.overlineText.set(Objects.requireNonNull(overlineText, "overlineText"));
    }

    /// Returns the overline text property.
    ///
    /// @return the overline text property
    public final StringProperty overlineTextProperty() {
        return overlineText;
    }

    /// Returns the headline text.
    ///
    /// @return the headline text
    public final String getHeadlineText() {
        return headlineText.get();
    }

    /// Sets the headline text.
    ///
    /// @param headlineText the headline text
    public final void setHeadlineText(String headlineText) {
        this.headlineText.set(Objects.requireNonNull(headlineText, "headlineText"));
    }

    /// Returns the headline text property.
    ///
    /// @return the headline text property
    public final StringProperty headlineTextProperty() {
        return headlineText;
    }

    /// Returns the supporting text.
    ///
    /// @return the supporting text
    public final String getSupportingText() {
        return supportingText.get();
    }

    /// Sets the supporting text.
    ///
    /// @param supportingText the supporting text
    public final void setSupportingText(String supportingText) {
        this.supportingText.set(Objects.requireNonNull(supportingText, "supportingText"));
    }

    /// Returns the supporting text property.
    ///
    /// @return the supporting text property
    public final StringProperty supportingTextProperty() {
        return supportingText;
    }

    /// Returns the trailing supporting text.
    ///
    /// @return the trailing supporting text
    public final String getTrailingSupportingText() {
        return trailingSupportingText.get();
    }

    /// Sets the trailing supporting text.
    ///
    /// @param trailingSupportingText the trailing supporting text
    public final void setTrailingSupportingText(String trailingSupportingText) {
        this.trailingSupportingText.set(Objects.requireNonNull(trailingSupportingText, "trailingSupportingText"));
    }

    /// Returns the trailing supporting text property.
    ///
    /// @return the trailing supporting text property
    public final StringProperty trailingSupportingTextProperty() {
        return trailingSupportingText;
    }

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

    /// Returns the leading content node property.
    ///
    /// @return the leading content node property
    public final ObjectProperty<@Nullable Node> leadingProperty() {
        return leading;
    }

    /// Sets the leading content node and its list item slot size.
    ///
    /// @param leading the leading content node, or `null`
    /// @param slotSize the leading content slot size
    public final void setLeadingMedia(@Nullable Node leading, M3ListItemSlotSize slotSize) {
        setLeading(leading);
        setLeadingSlotSize(slotSize);
    }

    /// Sets a medium [M3Icon] as leading content and returns it for further customization.
    ///
    /// @param iconText the glyph text rendered by the leading icon
    /// @return the created leading icon
    public final M3Icon setLeadingIcon(String iconText) {
        M3Icon icon = new M3Icon(iconText, M3IconSize.MEDIUM, M3IconVariant.ON_SURFACE_VARIANT);
        setLeadingMedia(icon, M3ListItemSlotSize.ICON);
        return icon;
    }

    /// Sets an [M3Avatar] as leading content and returns it for further customization.
    ///
    /// @param text the avatar text
    /// @return the created leading avatar
    public final M3Avatar setLeadingAvatar(String text) {
        M3Avatar avatar = new M3Avatar(text);
        setLeadingMedia(avatar, M3ListItemSlotSize.AVATAR);
        return avatar;
    }

    /// Sets a square thumbnail as leading content.
    ///
    /// @param thumbnail the square thumbnail node
    public final void setLeadingThumbnail(Node thumbnail) {
        setLeadingMedia(Objects.requireNonNull(thumbnail, "thumbnail"), M3ListItemSlotSize.THUMBNAIL);
    }

    /// Sets a wide thumbnail as leading content.
    ///
    /// @param thumbnail the wide thumbnail node
    public final void setLeadingWideThumbnail(Node thumbnail) {
        setLeadingMedia(Objects.requireNonNull(thumbnail, "thumbnail"), M3ListItemSlotSize.WIDE_THUMBNAIL);
    }

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

    /// Returns the trailing content node property.
    ///
    /// @return the trailing content node property
    public final ObjectProperty<@Nullable Node> trailingProperty() {
        return trailing;
    }

    /// Sets the trailing content node and its list item slot size.
    ///
    /// @param trailing the trailing content node, or `null`
    /// @param slotSize the trailing content slot size
    public final void setTrailingMedia(@Nullable Node trailing, M3ListItemSlotSize slotSize) {
        setTrailing(trailing);
        setTrailingSlotSize(slotSize);
    }

    /// Sets a medium [M3Icon] as trailing content and returns it for further customization.
    ///
    /// @param iconText the glyph text rendered by the trailing icon
    /// @return the created trailing icon
    public final M3Icon setTrailingIcon(String iconText) {
        M3Icon icon = new M3Icon(iconText, M3IconSize.MEDIUM, M3IconVariant.ON_SURFACE_VARIANT);
        setTrailingMedia(icon, M3ListItemSlotSize.ICON);
        return icon;
    }

    /// Returns the leading content slot size.
    ///
    /// @return the leading content slot size
    public final M3ListItemSlotSize getLeadingSlotSize() {
        return Objects.requireNonNull(leadingSlotSize.get(), "leadingSlotSize");
    }

    /// Sets the leading content slot size.
    ///
    /// @param leadingSlotSize the leading content slot size
    public final void setLeadingSlotSize(M3ListItemSlotSize leadingSlotSize) {
        this.leadingSlotSize.set(Objects.requireNonNull(leadingSlotSize, "leadingSlotSize"));
    }

    /// Returns the leading content slot size property.
    ///
    /// @return the leading content slot size property
    public final ObjectProperty<M3ListItemSlotSize> leadingSlotSizeProperty() {
        return leadingSlotSize;
    }

    /// Returns the trailing content slot size.
    ///
    /// @return the trailing content slot size
    public final M3ListItemSlotSize getTrailingSlotSize() {
        return Objects.requireNonNull(trailingSlotSize.get(), "trailingSlotSize");
    }

    /// Sets the trailing content slot size.
    ///
    /// @param trailingSlotSize the trailing content slot size
    public final void setTrailingSlotSize(M3ListItemSlotSize trailingSlotSize) {
        this.trailingSlotSize.set(Objects.requireNonNull(trailingSlotSize, "trailingSlotSize"));
    }

    /// Returns the trailing content slot size property.
    ///
    /// @return the trailing content slot size property
    public final ObjectProperty<M3ListItemSlotSize> trailingSlotSizeProperty() {
        return trailingSlotSize;
    }

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

    /// Returns the action handler property.
    ///
    /// @return the action handler property
    public final ObjectProperty<@Nullable EventHandler<ActionEvent>> onActionProperty() {
        return onAction;
    }

    /// Returns whether this list item is selected.
    ///
    /// @return `true` when this list item is selected
    public final boolean isSelected() {
        return selected.get();
    }

    /// Sets whether this list item is selected.
    ///
    /// @param selected whether this list item is selected
    public final void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    /// Returns the selected state property.
    ///
    /// @return the selected state property
    public final BooleanProperty selectedProperty() {
        return selected;
    }

    /// Returns the derived list item line count.
    ///
    /// @return the derived list item line count
    public final M3ListItemLineCount getLineCount() {
        return lineCount.get();
    }

    /// Returns the derived line count property.
    ///
    /// @return the derived line count property
    public final ReadOnlyObjectProperty<M3ListItemLineCount> lineCountProperty() {
        return lineCount.getReadOnlyProperty();
    }

    /// Fires this list item's action event.
    public final void fire() {
        if (!isDisabled()) {
            Event.fireEvent(this, new ActionEvent(this, this));
        }
    }

    /// Returns accessibility attributes for list item selection and position.
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

    /// Returns the one-line item height token.
    ///
    /// @return the one-line item height token
    public final double getOneLineHeight() {
        return oneLineHeight == null ? DEFAULT_ONE_LINE_HEIGHT : oneLineHeight.get();
    }

    /// Sets the one-line item height token.
    ///
    /// @param oneLineHeight the one-line item height token
    public final void setOneLineHeight(double oneLineHeight) {
        oneLineHeightProperty().set(M3Css.nonNegative(oneLineHeight, "oneLineHeight"));
    }

    /// Returns the one-line item height token property.
    ///
    /// @return the one-line item height token property
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

    /// Returns the two-line item height token.
    ///
    /// @return the two-line item height token
    public final double getTwoLineHeight() {
        return twoLineHeight == null ? DEFAULT_TWO_LINE_HEIGHT : twoLineHeight.get();
    }

    /// Sets the two-line item height token.
    ///
    /// @param twoLineHeight the two-line item height token
    public final void setTwoLineHeight(double twoLineHeight) {
        twoLineHeightProperty().set(M3Css.nonNegative(twoLineHeight, "twoLineHeight"));
    }

    /// Returns the two-line item height token property.
    ///
    /// @return the two-line item height token property
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

    /// Returns the three-line item height token.
    ///
    /// @return the three-line item height token
    public final double getThreeLineHeight() {
        return threeLineHeight == null ? DEFAULT_THREE_LINE_HEIGHT : threeLineHeight.get();
    }

    /// Sets the three-line item height token.
    ///
    /// @param threeLineHeight the three-line item height token
    public final void setThreeLineHeight(double threeLineHeight) {
        threeLineHeightProperty().set(M3Css.nonNegative(threeLineHeight, "threeLineHeight"));
    }

    /// Returns the three-line item height token property.
    ///
    /// @return the three-line item height token property
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

    /// Returns the container shape radius token.
    ///
    /// @return the container shape radius token
    public final double getContainerShape() {
        return containerShape == null ? DEFAULT_CONTAINER_SHAPE : containerShape.get();
    }

    /// Sets the container shape radius token.
    ///
    /// @param containerShape the container shape radius token
    public final void setContainerShape(double containerShape) {
        containerShapeProperty().set(M3Css.nonNegative(containerShape, "containerShape"));
    }

    /// Returns the container shape radius token property.
    ///
    /// @return the container shape radius token property
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

    /// Returns the horizontal content padding token.
    ///
    /// @return the horizontal content padding token
    public final double getHorizontalPadding() {
        return horizontalPadding == null ? DEFAULT_HORIZONTAL_PADDING : horizontalPadding.get();
    }

    /// Sets the horizontal content padding token.
    ///
    /// @param horizontalPadding the horizontal content padding token
    public final void setHorizontalPadding(double horizontalPadding) {
        horizontalPaddingProperty().set(M3Css.nonNegative(horizontalPadding, "horizontalPadding"));
    }

    /// Returns the horizontal content padding token property.
    ///
    /// @return the horizontal content padding token property
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

    /// Returns the vertical content padding token.
    ///
    /// @return the vertical content padding token
    public final double getVerticalPadding() {
        return verticalPadding == null ? DEFAULT_VERTICAL_PADDING : verticalPadding.get();
    }

    /// Sets the vertical content padding token.
    ///
    /// @param verticalPadding the vertical content padding token
    public final void setVerticalPadding(double verticalPadding) {
        verticalPaddingProperty().set(M3Css.nonNegative(verticalPadding, "verticalPadding"));
    }

    /// Returns the vertical content padding token property.
    ///
    /// @return the vertical content padding token property
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

    /// Returns the content spacing token.
    ///
    /// @return the content spacing token
    public final double getContentSpacing() {
        return contentSpacing == null ? DEFAULT_CONTENT_SPACING : contentSpacing.get();
    }

    /// Sets the content spacing token.
    ///
    /// @param contentSpacing the content spacing token
    public final void setContentSpacing(double contentSpacing) {
        contentSpacingProperty().set(M3Css.nonNegative(contentSpacing, "contentSpacing"));
    }

    /// Returns the content spacing token property.
    ///
    /// @return the content spacing token property
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
    private static boolean hasText(String text) {
        return !text.isBlank();
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
    private static void appendAccessibleText(StringBuilder builder, String text) {
        if (text.isBlank()) {
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
            CssMetaData<M3ListItem, Number> cssMetaData
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
        private static final CssMetaData<M3ListItem, Number> ONE_LINE_HEIGHT =
                createSizeCssMetaData("-m3-one-line-height", DEFAULT_ONE_LINE_HEIGHT, M3ListItem::oneLineHeightProperty);

        /// CSS metadata for the two-line height token.
        private static final CssMetaData<M3ListItem, Number> TWO_LINE_HEIGHT =
                createSizeCssMetaData("-m3-two-line-height", DEFAULT_TWO_LINE_HEIGHT, M3ListItem::twoLineHeightProperty);

        /// CSS metadata for the three-line height token.
        private static final CssMetaData<M3ListItem, Number> THREE_LINE_HEIGHT =
                createSizeCssMetaData("-m3-three-line-height", DEFAULT_THREE_LINE_HEIGHT, M3ListItem::threeLineHeightProperty);

        /// CSS metadata for the container shape token.
        private static final CssMetaData<M3ListItem, Number> CONTAINER_SHAPE =
                createSizeCssMetaData("-m3-container-shape", DEFAULT_CONTAINER_SHAPE, M3ListItem::containerShapeProperty);

        /// CSS metadata for the horizontal padding token.
        private static final CssMetaData<M3ListItem, Number> HORIZONTAL_PADDING =
                createSizeCssMetaData("-m3-horizontal-padding", DEFAULT_HORIZONTAL_PADDING, M3ListItem::horizontalPaddingProperty);

        /// CSS metadata for the vertical padding token.
        private static final CssMetaData<M3ListItem, Number> VERTICAL_PADDING =
                createSizeCssMetaData("-m3-vertical-padding", DEFAULT_VERTICAL_PADDING, M3ListItem::verticalPaddingProperty);

        /// CSS metadata for the content spacing token.
        private static final CssMetaData<M3ListItem, Number> CONTENT_SPACING =
                createSizeCssMetaData("-m3-content-spacing", DEFAULT_CONTENT_SPACING, M3ListItem::contentSpacingProperty);

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
        private static CssMetaData<M3ListItem, Number> createSizeCssMetaData(
                String property,
                double initialValue,
                StyleablePropertyProvider provider
        ) {
            return new CssMetaData<>(property, SizeConverter.getInstance(), initialValue) {
                /// Returns whether this property can be set by CSS.
                @Override
                public boolean isSettable(M3ListItem control) {
                    return M3Css.isSettable(provider.property(control));
                }

                /// Returns the styleable property for a control.
                @Override
                public StyleableProperty<Number> getStyleableProperty(M3ListItem control) {
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
        StyleableDoubleProperty property(M3ListItem control);
    }
}
