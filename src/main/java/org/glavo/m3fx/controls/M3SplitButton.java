// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3SelectionNavigation;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3DisclosureIcon;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3SplitButtonSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 split button with a primary action and an attached menu action.
///
/// `M3SplitButton` combines an [M3Button] for the primary command with an [M3MenuButton] that reveals related
/// secondary commands. The control keeps both parts visually joined, forwards the configured button variant, and
/// exposes the menu items through the embedded menu button. Activating the primary part fires this control's
/// [ActionEvent] without opening the menu; activating the trailing part opens the attached non-modal menu without
/// firing the primary action.
///
/// The default is an empty, small tonal split button with a two-pixel gap. Its attached menu and item list are stable
/// for the control lifetime. Use [#showMenu()] and [#hideMenu()] for non-blocking popup control.
///
/// See [Material Design split buttons](https://m3.material.io/components/split-button/overview).
@NotNullByDefault
public final class M3SplitButton extends Control {
    /// The base style class for M3FX split buttons.
    public static final String STYLE_CLASS = "m3-split-button";

    /// The style class applied to the primary action button.
    public static final String ACTION_BUTTON_STYLE_CLASS = "m3-split-button-action";

    /// The style class applied to the menu button.
    public static final String MENU_BUTTON_STYLE_CLASS = "m3-split-button-menu";

    /// The pseudo-class applied to the local left edge before JavaFX node-orientation mirroring.
    private static final PseudoClass LEFT_EDGE_PSEUDO_CLASS = PseudoClass.getPseudoClass("left-edge");

    /// The pseudo-class applied to the local right edge before JavaFX node-orientation mirroring.
    private static final PseudoClass RIGHT_EDGE_PSEUDO_CLASS = PseudoClass.getPseudoClass("right-edge");

    /// The default Material Expressive split button size.
    private static final M3ButtonSize DEFAULT_SIZE = M3ButtonSize.SMALL;

    /// The default spacing between the action and menu parts.
    private static final double DEFAULT_SPACING = 2.0;

    /// The default outer-corner radius for the small split button size.
    private static final double DEFAULT_OUTER_CORNER = 20.0;

    /// The default resting inner-corner radius for the small split button size.
    private static final double DEFAULT_INNER_CORNER = 4.0;

    /// The default hovered inner-corner radius for the small split button size.
    private static final double DEFAULT_HOVERED_INNER_CORNER = 12.0;

    /// The default pressed inner-corner radius for the small split button size.
    private static final double DEFAULT_PRESSED_INNER_CORNER = 12.0;

    /// The default selected trailing-button inner-corner radius for the small split button size.
    private static final double DEFAULT_SELECTED_INNER_CORNER = 20.0;

    /// The primary action button.
    private final M3Button actionButton = new M3Button();

    /// The attached menu button.
    private final M3MenuButton menuButton = new M3MenuButton();

    /// The disclosure icon displayed by the menu button side.
    private final M3DisclosureIcon menuIndicator = new M3DisclosureIcon();

    /// The focusable button parts exposed to accessibility and keyboard navigation.
    private final ObservableList<Node> buttonParts = M3ObservableLists.nonNullElementList("buttonPart");

    /// Notifies accessibility clients when focus moves between split button parts.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, () -> M3Accessible.currentOrFirstFocusTarget(this, buttonParts));

    /// Notifies accessibility clients when focus moves inside the popup-hosted menu.
    private final M3AccessibleFocusNotifier popupFocusNotifier =
            new M3AccessibleFocusNotifier(this, menuButton.getMenu(), this::focusNode);

    /// Creates an empty split button.
    public M3SplitButton() {
        this("");
    }

    /// Creates a split button with primary action text.
    ///
    /// @param text the primary action text
    /// @throws NullPointerException if `text` is `null`
    public M3SplitButton(String text) {
        initialize();
        setText(text);
    }

    /// The primary action text.
    ///
    /// Assigning `null` through the property is normalized to an empty string.
    ///
    /// @defaultValue `""`
    private final StringProperty text = new SimpleStringProperty(this, "text", "") {
        /// Keeps text non-null and synchronizes the primary button.
        @Override
        protected void invalidated() {
            if (get() == null) {
                set("");
                return;
            }
            actionButton.setText(get());
        }
    };

    /// Returns the primary action text.
    ///
    /// @return the primary action text
    public final String getText() {
        return text.get();
    }

    /// Sets the primary action text.
    ///
    /// @param text the primary action text
    /// @throws NullPointerException if `text` is `null`
    public final void setText(String text) {
        this.text.set(Objects.requireNonNull(text, "text"));
    }

    /// Returns the `text` property.
    ///
    /// The returned property is observable and bindable. Its default value is `""`.
    ///
    /// @return the `text` property
    public final StringProperty textProperty() {
        return text;
    }

    /// The primary action graphic.
    ///
    /// A non-null node is owned by the primary button and must be available for it to parent.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Node> graphic =
            new SimpleObjectProperty<>(this, "graphic") {
                /// Synchronizes the primary button graphic.
                @Override
                protected void invalidated() {
                    actionButton.setGraphic(get());
                }
            };

    /// Returns the primary action graphic.
    ///
    /// @return the primary action graphic, or `null` if none is set
    public final @Nullable Node getGraphic() {
        return graphic.get();
    }

    /// Sets the primary action graphic.
    ///
    /// A non-null graphic becomes a child of the primary button and must satisfy normal JavaFX parent ownership
    /// rules.
    ///
    /// @param graphic the primary action graphic, or `null` to clear it
    public final void setGraphic(@Nullable Node graphic) {
        this.graphic.set(graphic);
    }

    /// Returns the `graphic` property.
    ///
    /// The returned property is observable and bindable. Its default value is `null`.
    ///
    /// @return the `graphic` property
    public final ObjectProperty<@Nullable Node> graphicProperty() {
        return graphic;
    }

    /// The primary action handler.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable EventHandler<ActionEvent>> onAction =
            new SimpleObjectProperty<>(this, "onAction") {
                /// Updates the registered primary action event handler.
                @Override
                protected void invalidated() {
                    setEventHandler(ActionEvent.ACTION, get());
                }
            };

    /// Returns the primary action handler.
    ///
    /// @return the primary action handler, or `null` if none is set
    public final @Nullable EventHandler<ActionEvent> getOnAction() {
        return onAction.get();
    }

    /// Sets the primary action handler.
    ///
    /// @param onAction the primary action handler, or `null` to clear it
    public final void setOnAction(@Nullable EventHandler<ActionEvent> onAction) {
        this.onAction.set(onAction);
    }

    /// Returns the `onAction` property.
    ///
    /// The returned property is observable and bindable. Its default value is `null`.
    ///
    /// @return the `onAction` property
    public final ObjectProperty<@Nullable EventHandler<ActionEvent>> onActionProperty() {
        return onAction;
    }

    /// The button variant shared by both parts.
    ///
    /// Assigning `null` through the property restores [M3ButtonVariant#TONAL].
    ///
    /// @defaultValue `TONAL`
    private final ObjectProperty<M3ButtonVariant> variant =
            new SimpleObjectProperty<>(this, "variant", M3ButtonVariant.TONAL) {
                /// Updates both child buttons when the variant changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3ButtonVariant.TONAL);
                        return;
                    }
                    updateVariant();
                }
            };

    /// Returns the button variant shared by both split button parts.
    ///
    /// @return the button variant shared by both split button parts
    public final M3ButtonVariant getVariant() {
        return variant.get();
    }

    /// Sets the button variant shared by both split button parts.
    ///
    /// @param variant the button variant shared by both split button parts
    /// @throws NullPointerException if `variant` is `null`
    public final void setVariant(M3ButtonVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the `variant` property.
    ///
    /// The returned property is observable and bindable. Its default value is `TONAL`.
    ///
    /// @return the `variant` property
    public final ObjectProperty<M3ButtonVariant> variantProperty() {
        return variant;
    }

    /// The Material Expressive split button size.
    ///
    /// Assigning `null` through the property restores [M3ButtonSize#SMALL].
    ///
    /// @defaultValue `SMALL`
    private final ObjectProperty<M3ButtonSize> size =
            new SimpleObjectProperty<>(this, "size", DEFAULT_SIZE) {
                /// Updates size style classes when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(DEFAULT_SIZE);
                        return;
                    }
                    updateSizeStyle();
                    requestLayout();
                }
            };

    /// Returns the Material Expressive split button size.
    ///
    /// @return the Material Expressive split button size
    public final M3ButtonSize getSize() {
        return size.get();
    }

    /// Sets the Material Expressive split button size.
    ///
    /// @param size the Material Expressive split button size
    /// @throws NullPointerException if `size` is `null`
    public final void setSize(M3ButtonSize size) {
        this.size.set(Objects.requireNonNull(size, "size"));
    }

    /// Returns the `size` property.
    ///
    /// The returned property is observable and bindable. Its default value is `SMALL`.
    ///
    /// @return the `size` property
    public final ObjectProperty<M3ButtonSize> sizeProperty() {
        return size;
    }

    /// The spacing between the action and menu parts in logical pixels.
    ///
    /// Negative values are permitted; non-finite values are rejected.
    ///
    /// @defaultValue `2.0`
    private @Nullable StyleableDoubleProperty spacing;

    /// Returns the spacing between the primary action and menu button parts.
    ///
    /// @return the part spacing in pixels
    public final double getSpacing() {
        return spacing == null ? DEFAULT_SPACING : spacing.get();
    }

    /// Sets the spacing between the primary action and menu button parts.
    ///
    /// @param spacing the part spacing in pixels
    /// @throws IllegalArgumentException if `spacing` is not finite
    public final void setSpacing(double spacing) {
        spacingProperty().set(M3Css.finite(spacing, "spacing"));
    }

    /// Returns the `spacing` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite values, including negative
    /// values, and has a default value of `2.0` logical pixels.
    ///
    /// @return the `spacing` property
    public final StyleableDoubleProperty spacingProperty() {
        if (spacing == null) {
            spacing = M3Css.finiteStyleableDoubleProperty(
                    DEFAULT_SPACING,
                    this,
                    "spacing",
                    StyleableProperties.SPACING,
                    this::requestLayout
            );
        }
        return spacing;
    }

    /// The outer-corner radius shared by both button parts in logical pixels.
    ///
    /// @defaultValue `20.0`
    private @Nullable StyleableDoubleProperty outerCorner;

    /// Returns the outer-corner radius shared by both button parts.
    ///
    /// @return the outer-corner radius in pixels
    public final double getOuterCorner() {
        return outerCorner == null ? DEFAULT_OUTER_CORNER : outerCorner.get();
    }

    /// Sets the outer-corner radius shared by both button parts.
    ///
    /// @param outerCorner the non-negative outer-corner radius in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setOuterCorner(double outerCorner) {
        outerCornerProperty().set(M3Css.nonNegative(outerCorner, "outerCorner"));
    }

    /// Returns the `outerCorner` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `20.0` logical pixels.
    ///
    /// @return the `outerCorner` property
    public final StyleableDoubleProperty outerCornerProperty() {
        if (outerCorner == null) {
            outerCorner = shapeProperty(
                    DEFAULT_OUTER_CORNER,
                    "outerCorner",
                    StyleableProperties.OUTER_CORNER
            );
        }
        return outerCorner;
    }

    /// The resting inner-corner radius shared by both button parts in logical pixels.
    ///
    /// @defaultValue `4.0`
    private @Nullable StyleableDoubleProperty innerCorner;

    /// Returns the resting inner-corner radius shared by both button parts.
    ///
    /// @return the resting inner-corner radius in pixels
    public final double getInnerCorner() {
        return innerCorner == null ? DEFAULT_INNER_CORNER : innerCorner.get();
    }

    /// Sets the resting inner-corner radius shared by both button parts.
    ///
    /// @param innerCorner the non-negative resting inner-corner radius in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setInnerCorner(double innerCorner) {
        innerCornerProperty().set(M3Css.nonNegative(innerCorner, "innerCorner"));
    }

    /// Returns the `innerCorner` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `4.0` logical pixels.
    ///
    /// @return the `innerCorner` property
    public final StyleableDoubleProperty innerCornerProperty() {
        if (innerCorner == null) {
            innerCorner = shapeProperty(
                    DEFAULT_INNER_CORNER,
                    "innerCorner",
                    StyleableProperties.INNER_CORNER
            );
        }
        return innerCorner;
    }

    /// The hovered inner-corner radius shared by both button parts in logical pixels.
    ///
    /// @defaultValue `12.0`
    private @Nullable StyleableDoubleProperty hoveredInnerCorner;

    /// Returns the hovered inner-corner radius shared by both button parts.
    ///
    /// @return the hovered inner-corner radius in pixels
    public final double getHoveredInnerCorner() {
        return hoveredInnerCorner == null ? DEFAULT_HOVERED_INNER_CORNER : hoveredInnerCorner.get();
    }

    /// Sets the hovered inner-corner radius shared by both button parts.
    ///
    /// @param hoveredInnerCorner the non-negative hovered inner-corner radius in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setHoveredInnerCorner(double hoveredInnerCorner) {
        hoveredInnerCornerProperty().set(M3Css.nonNegative(hoveredInnerCorner, "hoveredInnerCorner"));
    }

    /// Returns the `hoveredInnerCorner` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `12.0` logical pixels.
    ///
    /// @return the `hoveredInnerCorner` property
    public final StyleableDoubleProperty hoveredInnerCornerProperty() {
        if (hoveredInnerCorner == null) {
            hoveredInnerCorner = shapeProperty(
                    DEFAULT_HOVERED_INNER_CORNER,
                    "hoveredInnerCorner",
                    StyleableProperties.HOVERED_INNER_CORNER
            );
        }
        return hoveredInnerCorner;
    }

    /// The pressed inner-corner radius shared by both button parts in logical pixels.
    ///
    /// @defaultValue `12.0`
    private @Nullable StyleableDoubleProperty pressedInnerCorner;

    /// Returns the pressed inner-corner radius shared by both button parts.
    ///
    /// @return the pressed inner-corner radius in pixels
    public final double getPressedInnerCorner() {
        return pressedInnerCorner == null ? DEFAULT_PRESSED_INNER_CORNER : pressedInnerCorner.get();
    }

    /// Sets the pressed inner-corner radius shared by both button parts.
    ///
    /// @param pressedInnerCorner the non-negative pressed inner-corner radius in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setPressedInnerCorner(double pressedInnerCorner) {
        pressedInnerCornerProperty().set(M3Css.nonNegative(pressedInnerCorner, "pressedInnerCorner"));
    }

    /// Returns the `pressedInnerCorner` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `12.0` logical pixels.
    ///
    /// @return the `pressedInnerCorner` property
    public final StyleableDoubleProperty pressedInnerCornerProperty() {
        if (pressedInnerCorner == null) {
            pressedInnerCorner = shapeProperty(
                    DEFAULT_PRESSED_INNER_CORNER,
                    "pressedInnerCorner",
                    StyleableProperties.PRESSED_INNER_CORNER
            );
        }
        return pressedInnerCorner;
    }

    /// The selected trailing-button inner-corner radius in logical pixels.
    ///
    /// @defaultValue `20.0`
    private @Nullable StyleableDoubleProperty selectedInnerCorner;

    /// Returns the selected trailing-button inner-corner radius.
    ///
    /// @return the selected inner-corner radius in pixels
    public final double getSelectedInnerCorner() {
        return selectedInnerCorner == null ? DEFAULT_SELECTED_INNER_CORNER : selectedInnerCorner.get();
    }

    /// Sets the selected trailing-button inner-corner radius.
    ///
    /// @param selectedInnerCorner the non-negative selected inner-corner radius in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setSelectedInnerCorner(double selectedInnerCorner) {
        selectedInnerCornerProperty().set(M3Css.nonNegative(selectedInnerCorner, "selectedInnerCorner"));
    }

    /// Returns the `selectedInnerCorner` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `20.0` logical pixels.
    ///
    /// @return the `selectedInnerCorner` property
    public final StyleableDoubleProperty selectedInnerCornerProperty() {
        if (selectedInnerCorner == null) {
            selectedInnerCorner = shapeProperty(
                    DEFAULT_SELECTED_INNER_CORNER,
                    "selectedInnerCorner",
                    StyleableProperties.SELECTED_INNER_CORNER
            );
        }
        return selectedInnerCorner;
    }

    /// The read-only showing state property.
    private final ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing");

    /// Returns whether the attached menu is currently showing.
    ///
    /// @return `true` if the attached menu is currently showing
    public final boolean isShowing() {
        return showing.get();
    }

    /// Returns the `showing` property.
    ///
    /// The returned property is observable and read-only. Its default value is `false`.
    ///
    /// @return the `showing` property
    public final ReadOnlyBooleanProperty showingProperty() {
        return showing.getReadOnlyProperty();
    }

    /// Fires the primary action.
    ///
    /// The event is delivered synchronously when this control is enabled. This method does not open or close the
    /// attached menu and is a no-op while disabled.
    public final void fire() {
        if (!isDisabled()) {
            fireEvent(new ActionEvent(this, this));
        }
    }

    /// Creates one non-negative styleable split-button shape token property.
    private StyleableDoubleProperty shapeProperty(
            double initialValue,
            String name,
            CssMetaData<M3SplitButton, Number> cssMetaData
    ) {
        return M3Css.nonNegativeStyleableDoubleProperty(
                initialValue,
                this,
                name,
                cssMetaData,
                this::requestLayout
        );
    }

    /// Returns the menu displayed by the menu side.
    ///
    /// @return the menu displayed by the menu side
    public final M3Menu getMenu() {
        return menuButton.getMenu();
    }

    /// Returns the mutable item list shown by the menu side.
    ///
    /// The returned list is the attached menu's live, mutable, ordered content list. It rejects `null` elements.
    /// Nodes become children of the menu and must satisfy normal JavaFX parent ownership rules.
    ///
    /// @return the live mutable attached-menu content list
    public final ObservableList<Node> getItems() {
        return menuButton.getItems();
    }

    /// Shows the attached menu.
    ///
    /// This method is non-blocking and has no effect until the control can own a popup in a showing window.
    public final void showMenu() {
        ensureButtonPartsInitialized();
        menuButton.showMenu();
    }

    /// Hides the attached menu.
    ///
    /// This method is non-blocking and is a no-op when the menu is not showing.
    public final void hideMenu() {
        menuButton.hideMenu();
    }

    /// Returns the user-agent stylesheet for M3FX split buttons.
    ///
    /// @return the split button user-agent stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("split-button.css");
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

    /// Returns accessibility attributes for the split button and its attached menu.
    ///
    /// @param attribute  the requested accessibility attribute
    /// @param parameters the optional attribute parameters
    /// @return the attribute value, or `null` when unavailable
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case EXPANDED -> isShowing();
            case ITEM_COUNT -> buttonParts.size();
            case ITEM_AT_INDEX -> M3Accessible.itemAt(buttonParts, parameters);
            case FOCUS_NODE -> focusNode();
            case MULTIPLE_SELECTION -> menuButton.queryAccessibleAttribute(attribute, parameters);
            case SELECTED_ITEMS -> getMenu().getSelectedItems();
            case SUBMENU -> getMenu();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions for the primary action and attached menu.
    ///
    /// @param action     the requested accessibility action
    /// @param parameters the optional action parameters
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
            case SHOW_MENU, EXPAND -> showMenu();
            case COLLAPSE -> menuButton.executeAccessibleAction(action, parameters);
            case REQUEST_FOCUS -> focusAccessibleNode();
            case SET_SELECTED_ITEMS -> menuButton.executeAccessibleAction(action, parameters);
            case SHOW_ITEM -> showAccessibleItem(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Adds base style classes, child buttons, and popup state forwarding.
    private void initialize() {
        M3ControlStyles.initialize(this, STYLE_CLASS);
        M3ControlStyles.add(actionButton, ACTION_BUTTON_STYLE_CLASS);
        M3ControlStyles.add(menuButton, MENU_BUTTON_STYLE_CLASS);
        menuIndicator.setMouseTransparent(true);
        menuIndicator.setVertical(true);
        menuIndicator.expandedProperty().bind(menuButton.showingProperty());
        menuButton.setGraphic(menuIndicator);
        setAccessibleRole(AccessibleRole.TOOL_BAR);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(
                this,
                this::focusAccessibleNode,
                this::showAccessibleItem,
                this::canShowAccessibleItem
        );
        buttonParts.setAll(actionButton, menuButton);
        menuButton.setHorizontalPadding(0.0);
        actionButton.setOnAction(event -> {
            event.consume();
            hideMenu();
            fire();
        });
        menuButton.showingProperty().addListener((observable, oldValue, newValue) -> {
            showing.set(newValue);
            if (newValue) {
                popupFocusNotifier.start();
            } else {
                popupFocusNotifier.stop();
            }
            notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
            notifyFocusNodeChanged();
        });
        menuButton.setPopupFocusNodeListener(this::notifyFocusNodeChanged);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        focusNotifier.start();
        updateVariant();
        updateSizeStyle();
        updatePartEdgeStyles();
    }

    /// Returns the current split button focus node, including popup menu focus while the menu is showing.
    private @Nullable Node focusNode() {
        if (isShowing()) {
            @Nullable Object focusNode = menuButton.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);
            if (focusNode instanceof Node node && M3Accessible.canReach(node)) {
                return node;
            }
            return menuButton;
        }
        return M3Accessible.currentOrFirstFocusTarget(this, buttonParts);
    }

    /// Requests focus for the currently active split button focus branch.
    ///
    /// @return `true` when the target accepted focus
    final boolean focusAccessibleNode() {
        if (!M3Accessible.canReach(this)) {
            return false;
        }
        boolean focused = isShowing()
                ? menuButton.requestAccessibleFocus()
                : M3Accessible.showCurrentOrItem(this, buttonParts);
        if (focused) {
            notifyFocusNodeChanged();
        }
        return focused;
    }

    /// Returns whether this split button can reveal the supplied target without changing popup state.
    private boolean canShowAccessibleItem(@Nullable Object parameter) {
        return !isDisabled()
                && (M3Accessible.actionItem(buttonParts, parameter) != null
                || getMenu().canShowAccessibleItem(parameter));
    }

    /// Ensures the internal buttons are attached before popup-owner actions use them.
    private void ensureButtonPartsInitialized() {
        applyCss();
        menuButton.applyCss();
    }

    /// Focuses a requested split button part or delegates menu-item targets to the popup menu.
    ///
    /// @param parameters optional accessibility target parameters
    /// @return `true` when focus moved to the default or requested split-button target
    final boolean showAccessibleItem(Object... parameters) {
        if (!M3Accessible.canReach(this)) {
            return false;
        }
        if (parameters.length == 0) {
            boolean focused = isShowing()
                    ? menuButton.showAccessibleMenuItem()
                    : M3Accessible.showCurrentOrItem(this, buttonParts);
            if (focused) {
                notifyFocusNodeChanged();
            }
            return focused;
        }

        @Nullable Node buttonPart = M3Accessible.actionItem(buttonParts, parameters);
        if (buttonPart != null) {
            if (M3Accessible.showItem(this, buttonPart)) {
                notifyFocusNodeChanged();
                return true;
            }
            return false;
        }

        if (parameters.length > 0 && getMenu().canShowAccessibleItem(parameters)) {
            ensureButtonPartsInitialized();
            if (menuButton.showAccessibleMenuItem(parameters)) {
                notifyFocusNodeChanged();
                return true;
            }
        }
        return false;
    }

    /// Notifies and refreshes cached accessibility focus state.
    private void notifyFocusNodeChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
        popupFocusNotifier.refresh();
    }

    /// Applies keyboard focus navigation across the two split button parts.
    private void handleNavigationKeyPressed(KeyEvent event) {
        M3SelectionNavigation.handleKeyFocus(
                event,
                this,
                buttonParts,
                M3SelectionNavigation.focused(buttonParts, M3ButtonBase.class),
                M3ButtonBase.class,
                true,
                false,
                M3NodeLayout.isRightToLeft(this)
        );
    }

    /// Applies the configured variant to both child buttons.
    private void updateVariant() {
        M3ButtonVariant currentVariant = getVariant();
        actionButton.setVariant(currentVariant);
        menuButton.setVariant(currentVariant);
    }

    /// Applies the size style class to this split button.
    private void updateSizeStyle() {
        M3ControlStyles.replaceVariant(
                this,
                sizeStyleClass(getSize()),
                sizeStyleClass(M3ButtonSize.EXTRA_SMALL),
                sizeStyleClass(M3ButtonSize.SMALL),
                sizeStyleClass(M3ButtonSize.MEDIUM),
                sizeStyleClass(M3ButtonSize.LARGE),
                sizeStyleClass(M3ButtonSize.EXTRA_LARGE)
        );
        actionButton.setSize(getSize());
        menuButton.setSize(getSize());
    }

    /// Returns the split-button style class for one Material button size.
    ///
    /// @param size the Material button size
    /// @return the split-button size style class
    private static String sizeStyleClass(M3ButtonSize size) {
        return "m3-split-button-" + size.cssSuffix();
    }

    /// Applies local edge roles that JavaFX mirrors with the effective node orientation.
    private void updatePartEdgeStyles() {
        applyEdgeState(actionButton, true);
        applyEdgeState(menuButton, false);
        actionButton.requestLayout();
        menuButton.requestLayout();
        requestLayout();
    }

    /// Applies local edge pseudo-classes to a child button before orientation mirroring.
    private static void applyEdgeState(M3ButtonBase button, boolean leftEdge) {
        button.pseudoClassStateChanged(LEFT_EDGE_PSEUDO_CLASS, leftEdge);
        button.pseudoClassStateChanged(RIGHT_EDGE_PSEUDO_CLASS, !leftEdge);
    }

    /// Creates the default Material Design 3 split button skin.
    ///
    /// @return the default Material Design 3 split button skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3SplitButtonSkin(this);
    }

    /// CSS metadata for split button layout tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for spacing between the action and menu parts.
        private static final CssMetaData<M3SplitButton, Number> SPACING =
                new CssMetaData<>("-m3-split-button-spacing", SizeConverter.getInstance(), DEFAULT_SPACING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3SplitButton control) {
                        return M3Css.isSettable(control.spacingProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3SplitButton control) {
                        return control.spacingProperty();
                    }
                };

        /// CSS metadata for the outer-corner radius.
        private static final CssMetaData<M3SplitButton, Number> OUTER_CORNER = shapeCssMetaData(
                "-m3-split-button-outer-corner",
                DEFAULT_OUTER_CORNER,
                M3SplitButton::outerCornerProperty
        );

        /// CSS metadata for the resting inner-corner radius.
        private static final CssMetaData<M3SplitButton, Number> INNER_CORNER = shapeCssMetaData(
                "-m3-split-button-inner-corner",
                DEFAULT_INNER_CORNER,
                M3SplitButton::innerCornerProperty
        );

        /// CSS metadata for the hovered inner-corner radius.
        private static final CssMetaData<M3SplitButton, Number> HOVERED_INNER_CORNER = shapeCssMetaData(
                "-m3-split-button-hovered-inner-corner",
                DEFAULT_HOVERED_INNER_CORNER,
                M3SplitButton::hoveredInnerCornerProperty
        );

        /// CSS metadata for the pressed inner-corner radius.
        private static final CssMetaData<M3SplitButton, Number> PRESSED_INNER_CORNER = shapeCssMetaData(
                "-m3-split-button-pressed-inner-corner",
                DEFAULT_PRESSED_INNER_CORNER,
                M3SplitButton::pressedInnerCornerProperty
        );

        /// CSS metadata for the selected trailing-button inner-corner radius.
        private static final CssMetaData<M3SplitButton, Number> SELECTED_INNER_CORNER = shapeCssMetaData(
                "-m3-split-button-selected-inner-corner",
                DEFAULT_SELECTED_INNER_CORNER,
                M3SplitButton::selectedInnerCornerProperty
        );

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(SPACING);
            styleables.add(OUTER_CORNER);
            styleables.add(INNER_CORNER);
            styleables.add(HOVERED_INNER_CORNER);
            styleables.add(PRESSED_INNER_CORNER);
            styleables.add(SELECTED_INNER_CORNER);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }

        /// Creates CSS metadata for one non-negative split-button shape token.
        private static CssMetaData<M3SplitButton, Number> shapeCssMetaData(
                String property,
                double initialValue,
                java.util.function.Function<M3SplitButton, StyleableDoubleProperty> accessor
        ) {
            return new CssMetaData<>(property, SizeConverter.getInstance(), initialValue, true) {
                /// Returns whether this property can be set by CSS.
                @Override
                public boolean isSettable(M3SplitButton control) {
                    return M3Css.isSettable(accessor.apply(control));
                }

                /// Returns the styleable property for a control.
                @Override
                public StyleableProperty<Number> getStyleableProperty(M3SplitButton control) {
                    return accessor.apply(control);
                }
            };
        }
    }
}
