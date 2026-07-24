// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.animation.Animation;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Popup;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3DisclosureIcon;
import org.glavo.m3fx.internal.M3NodeTransition;
import org.glavo.m3fx.internal.M3PopupContextSynchronizer;
import org.glavo.m3fx.internal.M3PopupPositioning;
import org.glavo.m3fx.internal.M3PopupWindows;
import org.glavo.m3fx.internal.M3ReachabilityObserver;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/// A Material Design 3 settings row that selects one value from a popup menu.
///
/// `M3SelectSettingItem` is the M3FX counterpart of a settings-line dropdown (as used by HMCL
/// `LineSelectButton` and MIUIX spinner/dropdown preferences). The row shows an optional trailing label for the
/// current value and a disclosure indicator. Activating the row toggles an [M3Menu] popup; choosing a menu item
/// updates [#valueProperty()] and delivers one [ActionEvent].
///
/// The control does not persist preference values. Applications own storage and may bind [#valueProperty()] or
/// listen for action events. Menu item labels come from [#converterProperty()]; optional supporting text on each
/// menu item comes from [#descriptionConverterProperty()].
///
/// See [Material Design lists](https://m3.material.io/components/lists/overview) and
/// [Material Design menus](https://m3.material.io/components/menus/overview).
@NotNullByDefault
public final class M3SelectSettingItem<T> extends M3SettingItemBase {
    /// The concrete style class assigned to select setting rows.
    private static final String DEFAULT_STYLE_CLASS = "m3-select-setting-item";

    /// The showing pseudo-class used while the owned menu popup is visible.
    private static final PseudoClass SHOWING_PSEUDO_CLASS = PseudoClass.getPseudoClass("showing");

    /// The vertical gap between the row and popup menu.
    private static final double MENU_OFFSET_Y = 4.0;

    /// The initial popup menu scale used for enter and exit motion.
    private static final double MENU_TRANSITION_SCALE = 0.96;

    /// The initial vertical popup menu offset used for enter and exit motion.
    private static final double MENU_TRANSITION_OFFSET_Y = -6.0;

    /// The menu displayed by this setting row.
    private final M3Menu menu = new M3Menu();

    /// The popup window used to host the menu.
    private final Popup popup = new Popup();

    /// Keeps the detached popup menu synchronized with the owner scene and theme context while visible.
    private final M3PopupContextSynchronizer popupContextSynchronizer =
            new M3PopupContextSynchronizer(this, menu, M3Stylesheets.controlStylesheet("menu.css"));

    /// The reusable menu popup enter and exit animation.
    private final M3NodeTransition popupAnimation = new M3NodeTransition(menu);

    /// Reports popup menu focus changes through this row's accessibility node.
    private final M3AccessibleFocusNotifier popupFocusNotifier =
            new M3AccessibleFocusNotifier(this, menu, this::focusNode, this::notifyPopupFocusNodeChanged);

    /// Closes the popup when this row or one of its ancestors becomes unreachable.
    private final M3ReachabilityObserver reachabilityObserver =
            new M3ReachabilityObserver(this, this::hidePopupIfOwnerUnreachable);

    /// The trailing disclosure indicator owned by this row.
    private final M3DisclosureIcon disclosureIcon = new M3DisclosureIcon();

    /// Rebuilds menu content when the choice list or converters change.
    private final InvalidationListener menuContentInvalidation = observable -> rebuildMenuItems();

    /// Whether focus should return to the owner row after the popup hides.
    private boolean focusOwnerOnHidden;

    /// Whether the reusable popup animation is currently closing the menu.
    private boolean hidingPopup;

    /// Whether menu content is currently being rebuilt.
    private boolean rebuildingMenu;

    /// Creates an empty select setting row with no selected value.
    public M3SelectSettingItem() {
        this("");
    }

    /// Creates a select setting row with the specified headline text and no selected value.
    ///
    /// @param headlineText the primary row text
    /// @throws NullPointerException if `headlineText` is `null`
    public M3SelectSettingItem(String headlineText) {
        super(headlineText, AccessibleRole.COMBO_BOX);
        addSettingStyleClass(DEFAULT_STYLE_CLASS);
        initialize();
    }

    /// The currently selected value.
    ///
    /// The property defaults to `null`. Changing it updates the trailing value label when
    /// [#showValueProperty()] is `true`, refreshes menu selection while the popup is open, and does not fire an
    /// action event.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable T> value = new SimpleObjectProperty<>(this, "value") {
        /// Updates presentation after the selected value changes.
        @Override
        protected void invalidated() {
            updateValuePresentation();
            syncMenuSelection();
            notifyAccessibleAttributeChanged(AccessibleAttribute.VALUE);
            notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
        }
    };

    /// Returns the selected value.
    ///
    /// @return the selected value, or `null` when no value is selected
    public @Nullable T getValue() {
        return value.get();
    }

    /// Sets the selected value.
    ///
    /// Direct assignment does not fire an action event. Choosing an item from the popup menu updates this property
    /// and then delivers an action event.
    ///
    /// @param value the selected value, or `null`
    public void setValue(@Nullable T value) {
        this.value.set(value);
    }

    /// Returns the observable, bindable selected-value property.
    ///
    /// The property defaults to `null`. Programmatic changes update presentation only; popup selection also fires
    /// an action event after the value is assigned.
    ///
    /// @return the selected-value property
    public ObjectProperty<@Nullable T> valueProperty() {
        return value;
    }

    /// The ordered selectable values presented by the popup menu.
    ///
    /// Mutate the live list through [#getItems()] (for example `getItems().setAll(...)`). Individual elements may be
    /// `null` when `T` permits them. Mutations rebuild the popup menu content.
    ///
    /// @defaultValue an empty observable list
    private final ListProperty<T> items =
            new SimpleListProperty<>(this, "items", FXCollections.observableArrayList());

    /// Returns the live selectable-value list.
    ///
    /// Callers should mutate this list in place. Replacing the backing list instance is supported through
    /// [#itemsProperty()].
    ///
    /// @return the selectable values
    public ObservableList<T> getItems() {
        return items.get();
    }

    /// Returns the bindable selectable-value list property.
    ///
    /// @return the items property
    public ListProperty<T> itemsProperty() {
        return items;
    }

    /// Converts a selectable value to the text shown on the row and in the menu.
    ///
    /// When the converter is `null`, the control uses [Objects#toString(Object, String)] with an empty fallback.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Function<? super T, String>> converter =
            new SimpleObjectProperty<>(this, "converter");

    /// Returns the value-to-text converter.
    ///
    /// @return the converter, or `null` for the default string conversion
    public @Nullable Function<? super T, String> getConverter() {
        return converter.get();
    }

    /// Sets the value-to-text converter.
    ///
    /// @param converter the converter, or `null` for the default string conversion
    public void setConverter(@Nullable Function<? super T, String> converter) {
        this.converter.set(converter);
    }

    /// Returns the bindable value-to-text converter property.
    ///
    /// @return the converter property
    public ObjectProperty<@Nullable Function<? super T, String>> converterProperty() {
        return converter;
    }

    /// Converts a selectable value to optional supporting text shown under the menu item label.
    ///
    /// When the converter is `null` or returns a blank string, the menu item has no supporting text.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Function<? super T, String>> descriptionConverter =
            new SimpleObjectProperty<>(this, "descriptionConverter");

    /// Returns the value-to-description converter.
    ///
    /// @return the description converter, or `null` when menu items have no supporting text
    public @Nullable Function<? super T, String> getDescriptionConverter() {
        return descriptionConverter.get();
    }

    /// Sets the value-to-description converter.
    ///
    /// @param descriptionConverter the converter, or `null` to omit menu supporting text
    public void setDescriptionConverter(@Nullable Function<? super T, String> descriptionConverter) {
        this.descriptionConverter.set(descriptionConverter);
    }

    /// Returns the bindable value-to-description converter property.
    ///
    /// @return the description-converter property
    public ObjectProperty<@Nullable Function<? super T, String>> descriptionConverterProperty() {
        return descriptionConverter;
    }

    /// Whether the selected value is shown as trailing supporting text.
    ///
    /// @defaultValue `true`
    private final BooleanProperty showValue = new SimpleBooleanProperty(this, "showValue", true) {
        /// Updates trailing value text when the visibility flag changes.
        @Override
        protected void invalidated() {
            updateValuePresentation();
        }
    };

    /// Returns whether the selected value is shown as trailing supporting text.
    ///
    /// @return `true` when the current value is displayed on the row
    public boolean isShowValue() {
        return showValue.get();
    }

    /// Sets whether the selected value is shown as trailing supporting text.
    ///
    /// @param showValue whether the current value is displayed on the row
    public void setShowValue(boolean showValue) {
        this.showValue.set(showValue);
    }

    /// Returns the bindable show-value property.
    ///
    /// @return the show-value property
    public BooleanProperty showValueProperty() {
        return showValue;
    }

    /// Whether this select setting popup is currently showing.
    ///
    /// @defaultValue `false`
    private final ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing") {
        /// Updates the showing pseudo-class used by owner-specific component styling.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(SHOWING_PSEUDO_CLASS, get());
        }
    };

    /// Returns whether the menu popup is currently showing.
    ///
    /// @return `true` when the menu popup is showing
    public boolean isShowing() {
        return showing.get();
    }

    /// Returns the read-only observable property that reports popup visibility.
    ///
    /// @return the read-only showing property
    public ReadOnlyBooleanProperty showingProperty() {
        return showing.getReadOnlyProperty();
    }

    /// Returns the menu owned and displayed by this setting row.
    ///
    /// Applications may adjust color style or other menu presentation, but the content list is rebuilt from
    /// [#getItems()] and must not be mutated as the primary choice source.
    ///
    /// @return the stable owned menu instance
    public M3Menu getMenu() {
        return menu;
    }

    /// Returns a live unmodifiable view of the menu content generated from [#getItems()].
    ///
    /// @return the generated menu content
    public @UnmodifiableView ObservableList<Node> getMenuItems() {
        return FXCollections.unmodifiableObservableList(menu.getItems());
    }

    /// Shows the owned menu in a popup positioned relative to this row.
    ///
    /// This method is non-blocking and idempotent while the popup is visible. It has no effect if the row is not
    /// reachable from a showing window or a popup position cannot be established.
    public void showMenu() {
        if (!M3Accessible.canReach(this) || popup.isShowing() || !M3PopupWindows.canShow(this)) {
            return;
        }

        rebuildMenuItems();
        boolean popupShown = false;
        popupContextSynchronizer.start();
        try {
            M3Css.setMinWidthIfUnbound(menu, Math.max(getWidth(), menu.minWidth(-1.0)));
            @Nullable M3PopupPositioning.Placement placement =
                    M3PopupPositioning.menuBelowOrAbove(this, menu, MENU_OFFSET_Y);
            if (placement == null) {
                return;
            }
            prepareMenuForShowAnimation();
            menu.setAccessibleFocusNodeListener(this::notifyPopupFocusNodeChanged);
            if (!M3PopupWindows.show(popup, this, placement.x(), placement.y())) {
                return;
            }
            popupShown = true;
        } finally {
            if (!popupShown) {
                menu.setAccessibleFocusNodeListener(null);
                resetMenuAnimationState();
                popupContextSynchronizer.stop();
            }
        }
        popupFocusNotifier.start();
        reachabilityObserver.install();
        showing.set(true);
        notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
        notifyPopupFocusNodeChanged();
        playShowAnimation();
    }

    /// Begins hiding the menu popup if it is visible.
    ///
    /// This method is non-blocking and idempotent while the popup is hidden or already closing. It does not request
    /// focus for the owner row.
    public void hideMenu() {
        hideMenu(false);
    }

    /// Opens or closes the popup without delivering an action event for the row itself.
    ///
    /// Choosing a menu item updates [#valueProperty()] and then delivers an action event.
    ///
    /// @return always `false`, because activation only toggles the menu
    @Override
    boolean prepareAction() {
        if (popup.isShowing()) {
            hideMenu(false);
        } else {
            showMenu();
        }
        return false;
    }

    /// Returns accessibility attributes for the select setting and popup.
    ///
    /// @param attribute  the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` when no value is available
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case EXPANDED -> isShowing();
            case SUBMENU -> menu;
            case FOCUS_NODE -> focusNode();
            case ITEM_COUNT -> getItems().size();
            case ITEM_AT_INDEX -> itemAt(parameters);
            case VALUE, TEXT -> formatValue(getValue());
            case MULTIPLE_SELECTION -> Boolean.FALSE;
            case SELECTED_ITEMS -> menu.getSelectedItems();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes select-setting accessibility actions.
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
            case SHOW_MENU, EXPAND -> showMenu();
            case COLLAPSE -> hideMenu(true);
            case REQUEST_FOCUS -> requestAccessibleFocus();
            case SHOW_ITEM -> showAccessibleMenuItem(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Installs trailing presentation, popup behavior, and property listeners.
    private void initialize() {
        disclosureIcon.setVertical(true);
        disclosureIcon.setFocusTraversable(false);
        disclosureIcon.setMouseTransparent(true);
        disclosureIcon.setAccessibleRole(AccessibleRole.NODE);
        disclosureIcon.disableProperty().bind(disabledProperty());
        disclosureIcon.expandedProperty().bind(showingProperty());
        installTrailingIndicator(disclosureIcon);

        menu.setSelectionMode(M3SelectionMode.SINGLE);
        menu.setAllowEmptySelection(true);
        popup.setAutoHide(true);
        popup.getContent().add(menu);
        popupAnimation.setOnFinished(event -> {
            if (hidingPopup) {
                popup.hide();
            }
        });
        popup.setOnHidden(event -> {
            menu.setAccessibleFocusNodeListener(null);
            popupFocusNotifier.stop();
            reachabilityObserver.uninstall();
            popupContextSynchronizer.stop();
            menu.hideSubMenusExcept(null);
            showing.set(false);
            notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
            notifyPopupFocusNodeChanged();
            resetMenuAnimationState();
            boolean restoreFocus = focusOwnerOnHidden;
            focusOwnerOnHidden = false;
            if (restoreFocus && M3Accessible.canReach(this)) {
                M3Accessible.showDirectItem(this, this);
            }
        });

        items.addListener(menuContentInvalidation);
        converter.addListener(menuContentInvalidation);
        descriptionConverter.addListener(menuContentInvalidation);
        converter.addListener((observable, oldValue, newValue) -> updateValuePresentation());

        addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        menu.addEventHandler(KeyEvent.KEY_PRESSED, this::handleMenuKeyPressed);

        M3Accessible.installAccessibleActionRoute(
                this,
                this::requestAccessibleFocus,
                this::showAccessibleMenuItem,
                this::canShowAccessibleMenuItem
        );

        updateValuePresentation();
        rebuildMenuItems();
    }

    /// Commits a chosen value from the popup menu and delivers the row action.
    private void commitSelection(@Nullable T choice) {
        if (rebuildingMenu) {
            return;
        }
        setValue(choice);
        hideMenu(true);
        if (!isDisabled()) {
            Event.fireEvent(this, new ActionEvent(this, this));
        }
    }

    /// Rebuilds the popup menu from the current items and converters.
    private void rebuildMenuItems() {
        rebuildingMenu = true;
        try {
            ObservableList<T> choices = getItems();
            List<Node> menuItems = new ArrayList<>(choices.size());
            for (T choice : choices) {
                M3MenuItem menuItem = new M3MenuItem(formatValue(choice));
                @Nullable Function<? super T, String> description = getDescriptionConverter();
                if (description != null) {
                    @Nullable String supporting = description.apply(choice);
                    if (supporting != null && !supporting.isBlank()) {
                        menuItem.setSupportingText(supporting);
                    }
                }
                menuItem.setOnAction(event -> commitSelection(choice));
                menuItems.add(menuItem);
            }
            menu.getItems().setAll(menuItems);
            syncMenuSelection();
        } finally {
            rebuildingMenu = false;
        }
    }

    /// Synchronizes menu item selection with [#getValue()].
    private void syncMenuSelection() {
        if (rebuildingMenu && menu.getItems().isEmpty()) {
            return;
        }
        ObservableList<T> choices = getItems();
        ObservableList<Node> menuItems = menu.getItems();
        if (choices.size() != menuItems.size()) {
            return;
        }

        @Nullable T selectedValue = getValue();
        @Nullable M3MenuItem selectedItem = null;
        for (int index = 0; index < choices.size(); index++) {
            if (Objects.equals(choices.get(index), selectedValue)
                    && menuItems.get(index) instanceof M3MenuItem menuItem) {
                selectedItem = menuItem;
                break;
            }
        }

        if (selectedItem == null) {
            menu.clearSelection();
        } else if (!selectedItem.isSelected()) {
            menu.select(selectedItem);
        }
    }

    /// Updates the trailing value label from the current value and converter.
    private void updateValuePresentation() {
        if (isShowValue()) {
            setTrailingSupportingText(formatValue(getValue()));
        } else {
            setTrailingSupportingText("");
        }
    }

    /// Formats a choice for display on the row or in the menu.
    @SuppressWarnings("unchecked")
    private String formatValue(@Nullable T choice) {
        @Nullable Function<? super T, String> activeConverter = getConverter();
        if (activeConverter == null) {
            return Objects.toString(choice, "");
        }
        if (choice == null) {
            // Most converters label concrete choices; empty selection uses a blank trailing label.
            try {
                @Nullable String text = ((Function<@Nullable T, String>) activeConverter).apply(null);
                return text == null ? "" : text;
            } catch (RuntimeException ignored) {
                return "";
            }
        }
        @Nullable String text = activeConverter.apply(choice);
        return text == null ? "" : text;
    }

    /// Hides the menu popup and optionally returns focus to the owner row.
    private void hideMenu(boolean focusOwner) {
        menu.hideSubMenusExcept(null);
        if (!popup.isShowing()) {
            return;
        }

        focusOwnerOnHidden |= focusOwner;
        if (hidingPopup && popupAnimation.getStatus() == Animation.Status.RUNNING) {
            return;
        }
        M3MotionSpec spec = M3Animation.fastSpatial(this);
        hidingPopup = true;
        popupAnimation.configure(
                spec,
                0.0,
                MENU_TRANSITION_SCALE,
                MENU_TRANSITION_SCALE,
                menu.getTranslateX(),
                MENU_TRANSITION_OFFSET_Y
        );
        M3Animation.playFromStart(this, popupAnimation);
    }

    /// Hides the popup if its owner row can no longer be reached from its scene.
    private void hidePopupIfOwnerUnreachable() {
        if (popup.isShowing() && !M3Accessible.canReach(this)) {
            hideMenu(false);
        }
    }

    /// Returns the current popup focus node for accessibility clients.
    private Node focusNode() {
        if (!isShowing()) {
            return this;
        }
        @Nullable Object focusNode = menu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);
        return focusNode instanceof Node node ? node : this;
    }

    /// Requests focus for this row or the currently reachable popup menu focus node.
    private boolean requestAccessibleFocus() {
        if (!M3Accessible.canReach(this)) {
            return false;
        }
        if (!isShowing()) {
            if (M3Accessible.showDirectItem(this, this)) {
                notifyPopupFocusNodeChanged();
                return true;
            }
            return false;
        }

        @Nullable Object focusNode = menu.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);
        if (focusNode instanceof Node node && node != this) {
            if (M3Accessible.showItem(this, node)) {
                notifyPopupFocusNodeChanged();
                return true;
            }
            return false;
        }

        if (menu.requestAccessibleFocus()) {
            notifyPopupFocusNodeChanged();
            return true;
        }

        if (M3Accessible.showDirectItem(this, this)) {
            notifyPopupFocusNodeChanged();
            return true;
        }
        return false;
    }

    /// Returns whether this select setting can reveal the supplied menu target without opening the popup.
    private boolean canShowAccessibleMenuItem(@Nullable Object parameter) {
        return parameter != null && !isDisabled() && menu.canShowAccessibleItem(parameter);
    }

    /// Opens the popup menu and focuses the descendant supplied by accessibility parameters.
    private boolean showAccessibleMenuItem(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (!M3Accessible.canReach(this) || !menu.canShowAccessibleItem(parameters)) {
            return false;
        }
        showMenu();
        if (!popup.isShowing()) {
            return false;
        }
        if (menu.showAccessibleItem(parameters)) {
            notifyPopupFocusNodeChanged();
            return true;
        }
        return false;
    }

    /// Returns the choice at an accessibility index parameter.
    private @Nullable Object itemAt(Object... parameters) {
        if (parameters.length == 0 || !(parameters[0] instanceof Integer index)) {
            return null;
        }
        ObservableList<T> choices = getItems();
        if (index < 0 || index >= choices.size()) {
            return null;
        }
        return choices.get(index);
    }

    /// Handles keyboard opening and dismissal for the popup menu.
    private void handleKeyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case DOWN -> {
                if (showMenuAndFocusFirstItem()) {
                    event.consume();
                }
            }
            case UP -> {
                if (showMenuAndFocusLastItem()) {
                    event.consume();
                }
            }
            case ESCAPE -> {
                if (popup.isShowing()) {
                    hideMenu(true);
                    event.consume();
                }
            }
            default -> {
            }
        }
    }

    /// Handles keyboard dismissal while focus is inside the popup menu.
    private void handleMenuKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE && popup.isShowing()) {
            hideMenu(true);
            event.consume();
        }
    }

    /// Shows the popup menu and focuses the first enabled visible menu item.
    private boolean showMenuAndFocusFirstItem() {
        boolean showingBefore = popup.isShowing();
        showMenu();
        if (!popup.isShowing() && !showingBefore) {
            return false;
        }

        boolean focused = menu.focusFirstItem();
        notifyPopupFocusNodeChanged();
        return focused || popup.isShowing();
    }

    /// Shows the popup menu and focuses the last enabled visible menu item.
    private boolean showMenuAndFocusLastItem() {
        boolean showingBefore = popup.isShowing();
        showMenu();
        if (!popup.isShowing() && !showingBefore) {
            return false;
        }

        boolean focused = menu.focusLastItem();
        notifyPopupFocusNodeChanged();
        return focused || popup.isShowing();
    }

    /// Notifies clients that the current popup-accessible focus node changed.
    private void notifyPopupFocusNodeChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
        popupFocusNotifier.refresh();
    }

    /// Applies initial visual state before the popup is shown.
    private void prepareMenuForShowAnimation() {
        popupAnimation.stop();
        hidingPopup = false;
        menu.setOpacity(0.0);
        menu.setScaleX(MENU_TRANSITION_SCALE);
        menu.setScaleY(MENU_TRANSITION_SCALE);
        menu.setTranslateY(MENU_TRANSITION_OFFSET_Y);
    }

    /// Plays the popup menu enter animation.
    private void playShowAnimation() {
        popupAnimation.stop();
        hidingPopup = false;
        M3MotionSpec spec = M3Animation.fastSpatial(this);
        popupAnimation.configure(spec, 1.0, 1.0, 1.0, menu.getTranslateX(), 0.0);
        M3Animation.playFromStart(this, popupAnimation);
    }

    /// Resets transient popup menu animation transforms.
    private void resetMenuAnimationState() {
        popupAnimation.stop();
        hidingPopup = false;
        menu.setOpacity(1.0);
        menu.setScaleX(1.0);
        menu.setScaleY(1.0);
        menu.setTranslateY(0.0);
    }
}
