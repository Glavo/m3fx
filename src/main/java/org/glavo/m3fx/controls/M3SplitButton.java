// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
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
/// exposes the menu items through the embedded menu button.
///
/// See [Material Design split buttons](https://m3.material.io/components/split-button/overview).
@NotNullByDefault
public class M3SplitButton extends Control {
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
    private static final double DEFAULT_SPACING = -1.0;

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

    // Backing property for the public shared button variant API.
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

    // The Material Expressive split button size property.
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

    // The styleable spacing between the action and menu parts.
    private @Nullable StyleableDoubleProperty spacing;

    /// Creates an empty split button.
    public M3SplitButton() {
        this("");
    }

    /// Creates a split button with primary action text.
    ///
    /// @param text the primary action text
    public M3SplitButton(String text) {
        initialize();
        setText(text);
    }

    /// Returns the primary action text.
    ///
    /// @return the primary action text
    public final String getText() {
        return actionButton.getText();
    }

    /// Sets the primary action text.
    ///
    /// @param text the primary action text
    public final void setText(String text) {
        actionButton.setText(Objects.requireNonNull(text, "text"));
    }

    /// Returns the primary action text property.
    ///
    /// @return the primary action text property
    public final StringProperty textProperty() {
        return actionButton.textProperty();
    }

    /// Returns the primary action graphic.
    ///
    /// @return the primary action graphic, or `null` if none is set
    public final @Nullable Node getGraphic() {
        return actionButton.getGraphic();
    }

    /// Sets the primary action graphic.
    ///
    /// @param graphic the primary action graphic, or `null` to clear it
    public final void setGraphic(@Nullable Node graphic) {
        actionButton.setGraphic(graphic);
    }

    /// Returns the primary action graphic property.
    ///
    /// @return the primary action graphic property
    public final ObjectProperty<@Nullable Node> graphicProperty() {
        return actionButton.graphicProperty();
    }

    /// Returns the primary action handler.
    ///
    /// @return the primary action handler, or `null` if none is set
    public final @Nullable EventHandler<ActionEvent> getOnAction() {
        return actionButton.getOnAction();
    }

    /// Sets the primary action handler.
    ///
    /// @param onAction the primary action handler, or `null` to clear it
    public final void setOnAction(@Nullable EventHandler<ActionEvent> onAction) {
        actionButton.setOnAction(onAction);
    }

    /// Returns the primary action handler property.
    ///
    /// @return the primary action handler property
    public final ObjectProperty<@Nullable EventHandler<ActionEvent>> onActionProperty() {
        return actionButton.onActionProperty();
    }

    /// Fires the primary action.
    public final void fire() {
        actionButton.fire();
    }

    /// Returns the button variant shared by both split button parts.
    ///
    /// @return the button variant shared by both split button parts
    public final M3ButtonVariant getVariant() {
        return variant.get();
    }

    /// Sets the button variant shared by both split button parts.
    ///
    /// @param variant the button variant shared by both split button parts
    public final void setVariant(M3ButtonVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the shared button variant property.
    ///
    /// @return the shared button variant property
    public final ObjectProperty<M3ButtonVariant> variantProperty() {
        return variant;
    }

    /// Returns the Material Expressive split button size.
    ///
    /// @return the Material Expressive split button size
    public final M3ButtonSize getSize() {
        return size.get();
    }

    /// Sets the Material Expressive split button size.
    ///
    /// @param size the Material Expressive split button size
    public final void setSize(M3ButtonSize size) {
        this.size.set(Objects.requireNonNull(size, "size"));
    }

    /// Returns the Material Expressive split button size property.
    ///
    /// @return the Material Expressive split button size property
    public final ObjectProperty<M3ButtonSize> sizeProperty() {
        return size;
    }

    /// Returns the spacing between the primary action and menu button parts.
    ///
    /// @return the part spacing in pixels
    public final double getSpacing() {
        return spacing == null ? DEFAULT_SPACING : spacing.get();
    }

    /// Sets the spacing between the primary action and menu button parts.
    ///
    /// @param spacing the part spacing in pixels
    public final void setSpacing(double spacing) {
        spacingProperty().set(M3Css.finite(spacing, "spacing"));
    }

    /// Returns the styleable part spacing property.
    ///
    /// @return the styleable part spacing property
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

    /// Returns the menu displayed by the menu side.
    ///
    /// @return the menu displayed by the menu side
    public final M3Menu getMenu() {
        return menuButton.getMenu();
    }

    /// Returns the mutable item list shown by the menu side.
    ///
    /// @return the mutable item list shown by the menu side
    public final ObservableList<Node> getItems() {
        return menuButton.getItems();
    }

    /// Shows the attached menu.
    public final void showMenu() {
        ensureButtonPartsInitialized();
        menuButton.showMenu();
    }

    /// Hides the attached menu.
    public final void hideMenu() {
        menuButton.hideMenu();
    }

    /// Returns whether the attached menu is currently showing.
    ///
    /// @return `true` if the attached menu is currently showing
    public final boolean isShowing() {
        return menuButton.isShowing();
    }

    /// Returns the read-only showing state property.
    ///
    /// @return the read-only showing state property
    public final ReadOnlyBooleanProperty showingProperty() {
        return menuButton.showingProperty();
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
    /// @param attribute the requested accessibility attribute
    /// @param parameters the optional attribute parameters
    /// @return the attribute value, or `null` when unavailable
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
    /// @param action the requested accessibility action
    /// @param parameters the optional action parameters
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
        M3ControlStyles.add(this, STYLE_CLASS);
        M3ControlStyles.add(actionButton, ACTION_BUTTON_STYLE_CLASS);
        M3ControlStyles.add(menuButton, MENU_BUTTON_STYLE_CLASS);
        menuIndicator.setMouseTransparent(true);
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
        actionButton.addEventHandler(ActionEvent.ACTION, event -> hideMenu());
        menuButton.showingProperty().addListener((observable, oldValue, newValue) -> {
            notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
            notifyFocusNodeChanged();
        });
        menuButton.addPopupFocusNodeListener(this::notifyFocusNodeChanged);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        focusNotifier.start();
        popupFocusNotifier.start();
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
                M3SelectionNavigation.focused(buttonParts, M3Button.class),
                M3Button.class,
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
    private static void applyEdgeState(M3Button button, boolean leftEdge) {
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

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(SPACING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
