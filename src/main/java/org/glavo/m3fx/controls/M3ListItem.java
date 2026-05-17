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
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3ListItemSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 list item.
@NotNullByDefault
public class M3ListItem extends Control {
    /// The base style class for m3fx list items.
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

    /// The overline text property.
    private final StringProperty overlineText = new SimpleStringProperty(this, "overlineText", "");

    /// The headline text property.
    private final StringProperty headlineText = new SimpleStringProperty(this, "headlineText", "");

    /// The supporting text property.
    private final StringProperty supportingText = new SimpleStringProperty(this, "supportingText", "");

    /// The leading content node property.
    private final ObjectProperty<@Nullable Node> leading = new SimpleObjectProperty<>(this, "leading");

    /// The trailing content node property.
    private final ObjectProperty<@Nullable Node> trailing = new SimpleObjectProperty<>(this, "trailing");

    /// The action handler property.
    private final ObjectProperty<@Nullable EventHandler<ActionEvent>> onAction =
            new SimpleObjectProperty<>(this, "onAction") {
                /// Updates the registered action event handler.
                @Override
                protected void invalidated() {
                    setEventHandler(ActionEvent.ACTION, get());
                }
            };

    /// The selected state property.
    private final BooleanProperty selected = new SimpleBooleanProperty(this, "selected") {
        /// Updates selected pseudo-class state.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, get());
            notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED);
        }
    };

    /// The derived line count property.
    private final ReadOnlyObjectWrapper<M3ListItemLineCount> lineCount =
            new ReadOnlyObjectWrapper<>(this, "lineCount", M3ListItemLineCount.ONE_LINE);

    /// The styleable one-line height token.
    private StyleableDoubleProperty oneLineHeight;

    /// The styleable two-line height token.
    private StyleableDoubleProperty twoLineHeight;

    /// The styleable three-line height token.
    private StyleableDoubleProperty threeLineHeight;

    /// The styleable container shape token.
    private StyleableDoubleProperty containerShape;

    /// The styleable horizontal padding token.
    private StyleableDoubleProperty horizontalPadding;

    /// The styleable vertical padding token.
    private StyleableDoubleProperty verticalPadding;

    /// The styleable content spacing token.
    private StyleableDoubleProperty contentSpacing;

    /// Creates an empty list item.
    public M3ListItem() {
        this("");
    }

    /// Creates a one-line list item with headline text.
    public M3ListItem(String headlineText) {
        installLineCountListeners();
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.LIST_ITEM);
        setFocusTraversable(true);
        setHeadlineText(headlineText);
        updateLineCount();
        updateAccessibleText();
    }

    /// Returns the overline text.
    public final String getOverlineText() {
        return overlineText.get();
    }

    /// Sets the overline text.
    public final void setOverlineText(String overlineText) {
        this.overlineText.set(Objects.requireNonNull(overlineText, "overlineText"));
    }

    /// Returns the overline text property.
    public final StringProperty overlineTextProperty() {
        return overlineText;
    }

    /// Returns the headline text.
    public final String getHeadlineText() {
        return headlineText.get();
    }

    /// Sets the headline text.
    public final void setHeadlineText(String headlineText) {
        this.headlineText.set(Objects.requireNonNull(headlineText, "headlineText"));
    }

    /// Returns the headline text property.
    public final StringProperty headlineTextProperty() {
        return headlineText;
    }

    /// Returns the supporting text.
    public final String getSupportingText() {
        return supportingText.get();
    }

    /// Sets the supporting text.
    public final void setSupportingText(String supportingText) {
        this.supportingText.set(Objects.requireNonNull(supportingText, "supportingText"));
    }

    /// Returns the supporting text property.
    public final StringProperty supportingTextProperty() {
        return supportingText;
    }

    /// Returns the leading content node.
    public final @Nullable Node getLeading() {
        return leading.get();
    }

    /// Sets the leading content node.
    public final void setLeading(@Nullable Node leading) {
        this.leading.set(leading);
    }

    /// Returns the leading content node property.
    public final ObjectProperty<@Nullable Node> leadingProperty() {
        return leading;
    }

    /// Returns the trailing content node.
    public final @Nullable Node getTrailing() {
        return trailing.get();
    }

    /// Sets the trailing content node.
    public final void setTrailing(@Nullable Node trailing) {
        this.trailing.set(trailing);
    }

    /// Returns the trailing content node property.
    public final ObjectProperty<@Nullable Node> trailingProperty() {
        return trailing;
    }

    /// Returns the action handler.
    public final @Nullable EventHandler<ActionEvent> getOnAction() {
        return onAction.get();
    }

    /// Sets the action handler.
    public final void setOnAction(@Nullable EventHandler<ActionEvent> onAction) {
        this.onAction.set(onAction);
    }

    /// Returns the action handler property.
    public final ObjectProperty<@Nullable EventHandler<ActionEvent>> onActionProperty() {
        return onAction;
    }

    /// Returns whether this list item is selected.
    public final boolean isSelected() {
        return selected.get();
    }

    /// Sets whether this list item is selected.
    public final void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    /// Returns the selected state property.
    public final BooleanProperty selectedProperty() {
        return selected;
    }

    /// Returns the derived list item line count.
    public final M3ListItemLineCount getLineCount() {
        return lineCount.get();
    }

    /// Returns the derived line count property.
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
    public Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case SELECTED -> isSelected();
            case INDEX -> M3Accessible.indexInParent(this);
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions supported by list items.
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case FIRE -> fire();
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Returns the one-line item height token.
    public final double getOneLineHeight() {
        return oneLineHeight == null ? DEFAULT_ONE_LINE_HEIGHT : oneLineHeight.get();
    }

    /// Sets the one-line item height token.
    public final void setOneLineHeight(double oneLineHeight) {
        oneLineHeightProperty().set(M3Css.nonNegative(oneLineHeight, "oneLineHeight"));
    }

    /// Returns the one-line item height token property.
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
    public final double getTwoLineHeight() {
        return twoLineHeight == null ? DEFAULT_TWO_LINE_HEIGHT : twoLineHeight.get();
    }

    /// Sets the two-line item height token.
    public final void setTwoLineHeight(double twoLineHeight) {
        twoLineHeightProperty().set(M3Css.nonNegative(twoLineHeight, "twoLineHeight"));
    }

    /// Returns the two-line item height token property.
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
    public final double getThreeLineHeight() {
        return threeLineHeight == null ? DEFAULT_THREE_LINE_HEIGHT : threeLineHeight.get();
    }

    /// Sets the three-line item height token.
    public final void setThreeLineHeight(double threeLineHeight) {
        threeLineHeightProperty().set(M3Css.nonNegative(threeLineHeight, "threeLineHeight"));
    }

    /// Returns the three-line item height token property.
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
    public final double getContainerShape() {
        return containerShape == null ? DEFAULT_CONTAINER_SHAPE : containerShape.get();
    }

    /// Sets the container shape radius token.
    public final void setContainerShape(double containerShape) {
        containerShapeProperty().set(M3Css.nonNegative(containerShape, "containerShape"));
    }

    /// Returns the container shape radius token property.
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
    public final double getHorizontalPadding() {
        return horizontalPadding == null ? DEFAULT_HORIZONTAL_PADDING : horizontalPadding.get();
    }

    /// Sets the horizontal content padding token.
    public final void setHorizontalPadding(double horizontalPadding) {
        horizontalPaddingProperty().set(M3Css.nonNegative(horizontalPadding, "horizontalPadding"));
    }

    /// Returns the horizontal content padding token property.
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
    public final double getVerticalPadding() {
        return verticalPadding == null ? DEFAULT_VERTICAL_PADDING : verticalPadding.get();
    }

    /// Sets the vertical content padding token.
    public final void setVerticalPadding(double verticalPadding) {
        verticalPaddingProperty().set(M3Css.nonNegative(verticalPadding, "verticalPadding"));
    }

    /// Returns the vertical content padding token property.
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
    public final double getContentSpacing() {
        return contentSpacing == null ? DEFAULT_CONTENT_SPACING : contentSpacing.get();
    }

    /// Sets the content spacing token.
    public final void setContentSpacing(double contentSpacing) {
        contentSpacingProperty().set(M3Css.nonNegative(contentSpacing, "contentSpacing"));
    }

    /// Returns the content spacing token property.
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
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Returns the user-agent stylesheet for m3fx list items.
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
        return new StyleableDoubleProperty(initialValue) {
            /// Validates updated token values.
            @Override
            protected void invalidated() {
                set(M3Css.nonNegative(get(), name));
            }

            /// Returns the owning bean.
            @Override
            public Object getBean() {
                return M3ListItem.this;
            }

            /// Returns the property name.
            @Override
            public String getName() {
                return name;
            }

            /// Returns the CSS metadata for this property.
            @Override
            public CssMetaData<M3ListItem, Number> getCssMetaData() {
                return cssMetaData;
            }
        };
    }

    /// CSS metadata for m3fx list item component tokens.
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
