// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Popup;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3DisclosureIcon;
import org.glavo.m3fx.internal.M3PopupContextSynchronizer;
import org.glavo.m3fx.internal.M3PopupPositioning;
import org.glavo.m3fx.internal.M3PopupStyles;
import org.glavo.m3fx.internal.M3PopupWindows;
import org.glavo.m3fx.internal.M3ReachabilityObserver;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

/// A non-editable field for choosing a value from an exposed dropdown menu.
///
/// Opening the menu highlights the first item equal to the current value, or the first
/// item if none matches. Arrow keys browse without changing the value. Enter, Space,
/// or clicking an option commits it; Escape or dismissing the popup leaves the value
/// unchanged. A unidirectionally bound value can be displayed but not changed by the user.
///
/// See [Material Design menus](https://m3.material.io/components/menus/overview).
///
/// @param <T> the option type
@NotNullByDefault
public final class M3ComboBox<T> extends Control {
    /// The style class of the field.
    private static final String DEFAULT_STYLE_CLASS = "m3-combo-box";
    /// Marks the field while its menu is open.
    private static final PseudoClass SHOWING = PseudoClass.getPseudoClass("showing");
    /// The popup is created on the first successful attempt to open an attached field.
    private @Nullable ChoicePopup popup;

    /// Creates an empty filled field.
    public M3ComboBox() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        getStyleClass().add(getVariant().styleClass());
        setFocusTraversable(true);
        setAccessibleRole(AccessibleRole.COMBO_BOX);
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleKey);
        addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            if (!isDisabled() && event.getButton() == MouseButton.PRIMARY) {
                requestFocus();
                event.consume();
            }
        });
        addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            if (!isDisabled() && event.getButton() == MouseButton.PRIMARY && contains(event.getX(), event.getY())) {
                if (isShowing()) hide(); else show();
                event.consume();
            }
        });
        items.addListener((ListChangeListener<T>) change -> hide());
        value.addListener(obs -> {
            if (isShowing() && popup != null) popup.highlightValue();
            notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
        });
        variant.addListener((obs, old, next) -> M3ControlStyles.replaceVariant(this,
                next.styleClass(), M3TextInputVariant.FILLED.styleClass(), M3TextInputVariant.OUTLINED.styleClass()));
        converter.addListener(obs -> {
            if (popup != null) popup.list.refresh();
            notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
        });
        promptText.addListener(obs -> notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT));
    }

    /// The available choices in display order.
    private final ObservableList<T> items = FXCollections.observableArrayList();

    /// Returns the live choices. Changing the list dismisses an open menu but does not clear the value.
    public ObservableList<T> getItems() {
        return items;
    }

    /// The committed value; it need not occur in the list.
    private final ObjectProperty<@Nullable T> value = new SimpleObjectProperty<>(this, "value");

    /// Returns the committed value, or null for an empty field.
    public @Nullable T getValue() {
        return value.get();
    }

    /// Sets the value without adding it to the available choices.
    public void setValue(@Nullable T value) {
        this.value.set(value);
    }

    /// The value displayed in the field, initially null.
    public ObjectProperty<@Nullable T> valueProperty() {
        return value;
    }

    /// The placeholder for an empty field.
    private final StringProperty promptText = new SimpleStringProperty(this, "promptText", "");

    /// Returns the text shown when no value is set.
    public String getPromptText() {
        return Objects.toString(promptText.get(), "");
    }

    /// Sets the placeholder; an empty string suppresses it.
    public void setPromptText(String text) {
        promptText.set(text);
    }

    /// The placeholder, initially empty.
    public StringProperty promptTextProperty() {
        return promptText;
    }

    /// The field container treatment.
    private final ObjectProperty<M3TextInputVariant> variant =
            new SimpleObjectProperty<>(this, "variant", M3TextInputVariant.FILLED);

    /// Returns the field container treatment.
    public M3TextInputVariant getVariant() {
        return variant.get();
    }

    /// Sets the field container treatment.
    public void setVariant(M3TextInputVariant variant) {
        this.variant.set(Objects.requireNonNull(variant));
    }

    /// The field treatment, initially [M3TextInputVariant#FILLED].
    public ObjectProperty<M3TextInputVariant> variantProperty() {
        return variant;
    }

    /// The optional formatter shared by the field and its options.
    private final ObjectProperty<@Nullable Function<? super T, @Nullable String>> converter =
            new SimpleObjectProperty<>(this, "converter");

    /// Returns the formatter, or null to use the value's string representation.
    public @Nullable Function<? super T, @Nullable String> getConverter() {
        return converter.get();
    }

    /// Sets the display formatter. A null result displays an empty string.
    public void setConverter(@Nullable Function<? super T, @Nullable String> converter) {
        this.converter.set(converter);
    }

    /// The display formatter, initially null. It is never called with a null value.
    public ObjectProperty<@Nullable Function<? super T, @Nullable String>> converterProperty() {
        return converter;
    }

    /// Reports actual popup visibility.
    private final ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing");

    /// Returns whether the dropdown is visible.
    public boolean isShowing() {
        return showing.get();
    }

    /// The visibility of the dropdown, initially false.
    public ReadOnlyBooleanProperty showingProperty() {
        return showing.getReadOnlyProperty();
    }

    /// Shows the available choices. Does nothing for an empty or unreachable field.
    public void show() {
        if (isShowing() || items.isEmpty() || !M3Accessible.canReach(this) || !M3PopupWindows.canShow(this)) return;
        if (popup == null) popup = new ChoicePopup();
        popup.open();
    }

    /// Dismisses the menu without committing the highlighted choice.
    public void hide() {
        if (popup != null) popup.window.hide();
    }

    /// Returns the user-facing text of a value.
    private String format(@Nullable T item) {
        if (item == null) return "";
        var formatter = getConverter();
        return formatter == null ? item.toString() : Objects.toString(formatter.apply(item), "");
    }

    /// Handles field activation and keyboard navigation in the detached menu.
    private void handleKey(KeyEvent event) {
        if (isDisabled() || event.isControlDown() || event.isMetaDown()
                || (event.isShiftDown() && event.getCode() != KeyCode.TAB)) {
            return;
        }
        switch (event.getCode()) {
            case DOWN, UP, SPACE, ENTER -> {
                if (!isShowing()) show();
                else if (popup != null) {
                    switch (event.getCode()) {
                        case DOWN -> popup.highlight(popup.list.getSelectionModel().getSelectedIndex() + 1);
                        case UP -> popup.highlight(popup.list.getSelectionModel().getSelectedIndex() - 1);
                        default -> popup.commit(popup.list.getSelectionModel().getSelectedIndex());
                    }
                }
            }
            case ESCAPE -> { if (!isShowing()) return; hide(); }
            case TAB -> { hide(); return; }
            default -> { return; }
        }
        event.consume();
    }

    /// Reports the displayed text and popup state to assistive technology.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        return switch (attribute) {
            case TEXT -> getAccessibleText() != null ? getAccessibleText()
                    : getValue() == null ? getPromptText() : format(getValue());
            case EXPANDED -> isShowing();
            case EDITABLE -> false;
            case ITEM_COUNT -> items.size();
            case FOCUS_NODE -> isShowing() && popup != null ? popup.list : this;
            case ITEM_AT_INDEX -> isShowing() && popup != null
                    ? popup.list.queryAccessibleAttribute(attribute, parameters) : null;
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Opens or dismisses the menu on request from assistive technology.
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        switch (action) {
            case EXPAND, SHOW_MENU -> show();
            case COLLAPSE -> hide();
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Creates the default field presentation.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new FieldSkin();
    }

    /// Returns the field and option stylesheet.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("combo-box.css");
    }

    /// Presents the value and disclosure as a single, non-editable focus target.
    @NotNullByDefault
    private final class FieldSkin extends SkinBase<M3ComboBox<T>> {
        /// The current value or empty-state prompt.
        private final Label label = new Label();

        /// Creates the field's presentation without creating its popup.
        FieldSkin() {
            super(M3ComboBox.this);
            var arrow = new M3DisclosureIcon();
            arrow.setVertical(true);
            arrow.setExpanded(isShowing());
            registerChangeListener(showing, obs -> arrow.setExpanded(isShowing()));
            label.setMaxWidth(Double.MAX_VALUE);
            label.setMinWidth(0);
            HBox.setHgrow(label, Priority.ALWAYS);
            var row = new HBox(12, label, arrow);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setMouseTransparent(true);
            getChildren().add(row);
            registerChangeListener(value, obs -> updateLabel());
            registerChangeListener(promptText, obs -> updateLabel());
            registerChangeListener(converter, obs -> updateLabel());
            updateLabel();
        }

        /// Displays only the committed value, never the popup's pending selection.
        private void updateLabel() {
            @Nullable T item = getValue();
            label.setText(item == null ? getPromptText() : format(item));
            label.pseudoClassStateChanged(PseudoClass.getPseudoClass("prompt"), item == null);
        }
    }

    /// A virtualized list whose selection represents the pending, not the committed, value.
    @NotNullByDefault
    private final class ChoicePopup {
        /// The detached option window.
        final Popup window = new Popup();
        /// The currently highlighted option is local to this list.
        final ListView<T> list = new ListView<>(items);
        /// Synchronizes the detached list with the field's theme.
        final M3PopupContextSynchronizer context = new M3PopupContextSynchronizer(
                M3ComboBox.this, window.getScene().getRoot(), getUserAgentStylesheet());
        /// Closes the popup if the anchor is removed, hidden, or disabled.
        final M3ReachabilityObserver reachability = new M3ReachabilityObserver(M3ComboBox.this, () -> {
            if (!M3Accessible.canReach(M3ComboBox.this)) hide();
        });

        /// Initializes the popup once; cells are reused as the menu scrolls.
        ChoicePopup() {
            list.getStyleClass().add("m3-combo-options");
            list.setFixedCellSize(48);
            list.setFocusTraversable(false);
            list.setCellFactory(view -> new ListCell<>() {
                /// Updates the display label when the cell is reused.
                @Override protected void updateItem(@Nullable T item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : format(item));
                }
            });
            list.addEventFilter(KeyEvent.KEY_PRESSED, M3ComboBox.this::handleKey);
            list.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                if (event.getButton() != MouseButton.PRIMARY) return;
                for (@Nullable Node n = event.getPickResult().getIntersectedNode(); n != null && n != list; n = n.getParent()) {
                    if (n instanceof ListCell<?> cell && !cell.isEmpty()) {
                        commit(cell.getIndex());
                        event.consume();
                        return;
                    }
                }
            });
            window.setAutoHide(true);
            M3PopupStyles.prepareStandalonePopupRoot(window.getScene().getRoot(), getUserAgentStylesheet());
            window.getContent().add(list);
            window.setOnShown(e -> {
                context.start();
                highlightValue();
                reachability.install();
                setVisibleState(true);
            });
            window.setOnHidden(e -> {
                reachability.uninstall();
                context.stop();
                setVisibleState(false);
            });
        }

        /// Publishes visibility only after the window changes state.
        void setVisibleState(boolean visible) {
            pseudoClassStateChanged(SHOWING, visible);
            showing.set(visible);
            notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
            notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
        }

        /// Fits the menu to the larger available side if it cannot fit below the field.
        void open() {
            @Nullable Bounds anchor = localToScreen(getLayoutBounds());
            if (anchor == null) return;
            Rectangle2D screen = M3PopupPositioning.visualBoundsFor(anchor);
            double available = Math.max(screen.getMaxY() - anchor.getMaxY(), anchor.getMinY() - screen.getMinY()) - 12;
            if (available <= 0) return;
            double width = Math.min(anchor.getWidth(), screen.getWidth() - 16);
            double height = Math.min(16 + list.getFixedCellSize() * Math.min(8, items.size()), available);
            list.setPrefSize(width, height);
            var placement = M3PopupPositioning.menuBelowOrAbove(anchor, screen, width, height, 4);
            M3PopupWindows.show(window, M3ComboBox.this, placement.x(), placement.y());
        }

        /// Starts browsing at the committed option, or the first option.
        void highlightValue() {
            highlight(items.indexOf(getValue()));
        }

        /// Keeps the pending choice visible while the user browses.
        void highlight(int index) {
            int target = Math.max(0, Math.min(index, items.size() - 1));
            list.getSelectionModel().select(target);
            list.scrollTo(target);
        }

        /// Commits one option without attempting to write a bound property.
        void commit(int index) {
            if (!isDisabled() && !value.isBound() && index >= 0 && index < items.size()) setValue(items.get(index));
            hide();
        }
    }
}
