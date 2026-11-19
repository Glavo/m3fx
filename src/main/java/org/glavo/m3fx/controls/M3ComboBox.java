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
import javafx.collections.transformation.FilteredList;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.SimpleStyleableBooleanProperty;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
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
import javafx.scene.control.TextField;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import org.glavo.m3fx.animation.M3Animated;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3DisclosureIcon;
import org.glavo.m3fx.internal.M3FocusGuards;
import org.glavo.m3fx.internal.M3MotionProperties;
import org.glavo.m3fx.internal.M3NodeTransition;
import org.glavo.m3fx.internal.M3PopupContextSynchronizer;
import org.glavo.m3fx.internal.M3PopupPositioning;
import org.glavo.m3fx.internal.M3PopupStyles;
import org.glavo.m3fx.internal.M3PopupWindows;
import org.glavo.m3fx.internal.M3ReachabilityObserver;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.M3VirtualFlowScroll;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.BiPredicate;

/// A field for choosing a value from an exposed dropdown menu or entering a custom value.
///
/// Opening the menu highlights the first item equal to the current value, or the first
/// item if none matches. Arrow keys browse without changing the value. Enter, Space,
/// or clicking an option commits it. A unidirectionally bound value can be displayed
/// but not changed by the user.
///
/// In [M3ComboBoxInputMode#FILTERED_SELECTION] the field contains a text editor. Typing
/// narrows the available choices without changing the committed value. Space inserts
/// text; Enter accepts the highlighted choice, if any. Dismissing the menu or leaving
/// the editor discards uncommitted input. The displayed value does not filter the menu
/// until the user edits it.
///
/// In [M3ComboBoxInputMode#FREE_INPUT], typing filters the suggestions but does not select one.
/// Enter submits the unmodified text through [#parserProperty()]. Clicking a suggestion, or
/// choosing one with the arrow keys and Enter, submits that item without parsing its label.
/// Leaving the editor, including by Tab, submits the draft rather than a highlighted suggestion.
/// If conversion fails, the draft remains available for correction and the value is unchanged.
/// Escape discards the draft. Custom values are not added to [#getItems()].
///
/// See [Material Design menus](https://m3.material.io/components/menus/overview).
///
/// @param <T> the option type
@NotNullByDefault
public final class M3ComboBox<T> extends Control implements M3Animated {
    /// The style class of the field.
    private static final String DEFAULT_STYLE_CLASS = "m3-combo-box";
    /// Marks the field while its menu is open.
    private static final PseudoClass SHOWING = PseudoClass.getPseudoClass("showing");
    /// Resting fraction of the menu size at the collapsed endpoint.
    private static final double COLLAPSED_SCALE = 0.96;
    /// Inheritable CSS switch for optional field and menu motion.
    private static final CssMetaData<M3ComboBox<?>, Boolean> ANIMATIONS_ENABLED =
            M3MotionProperties.cssMetaData(M3ComboBox::animationsEnabledProperty,
                    field -> field.animationsEnabled == null || !field.animationsEnabled.isBound());
    /// CSS properties supported by this control.
    private static final @Unmodifiable List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

    static {
        var metadata = new ArrayList<>(Control.getClassCssMetaData());
        metadata.add(ANIMATIONS_ENABLED);
        STYLEABLES = List.copyOf(metadata);
    }
    /// The popup is created on the first successful attempt to open an attached field.
    private @Nullable ChoicePopup popup;
    /// The native editor is needed only in modes that accept text.
    private @Nullable TextField editor;
    /// A null query distinguishes the committed label from a user edit, even if the strings are equal.
    private @Nullable String query;
    /// Prevents programmatic display updates from starting a search.
    private boolean updatingEditor;
    /// Whether the input method currently owns the uncommitted text.
    private boolean composing;

    /// Creates an empty filled field.
    public M3ComboBox() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        getStyleClass().add(getVariant().styleClass());
        setFocusTraversable(true);
        setAccessibleRole(AccessibleRole.COMBO_BOX);
        addEventFilter(KeyEvent.KEY_PRESSED, this::handleKey);
        addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (isFieldActivation(event)) {
                requestFocus();
                event.consume();
            }
        });
        addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            if (isFieldActivation(event) && contains(event.getX(), event.getY())) {
                if (isShowing()) hide(); else show();
                event.consume();
            }
        });
        items.addListener((ListChangeListener<T>) change -> {
            if (getInputMode() == M3ComboBoxInputMode.READ_ONLY) hide();
        });
        value.addListener(obs -> {
            if (query != null) hide();
            else restoreEditor();
            if (isShowing() && popup != null) popup.highlightValue();
            notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
        });
        variant.addListener((obs, old, next) -> M3ControlStyles.replaceVariant(this,
                next.styleClass(), M3TextInputVariant.FILLED.styleClass(), M3TextInputVariant.OUTLINED.styleClass()));
        converter.addListener(obs -> {
            if (query == null) restoreEditor();
            if (popup != null) popup.list.refresh();
            if (isShowing() && popup != null) popup.updateFilter();
            notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
        });
        promptText.addListener(obs -> notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT));
    }

    /// Lets the text editor keep its native pointer selection and caret behavior.
    private boolean isFieldActivation(MouseEvent event) {
        return !isDisabled() && event.getButton() == MouseButton.PRIMARY
                && !(editor != null && event.getTarget() instanceof Node node && M3FocusGuards.containsNode(editor, node));
    }

    /// The way the user browses and searches for choices.
    private final ObjectProperty<M3ComboBoxInputMode> inputMode =
            new SimpleObjectProperty<>(this, "inputMode", M3ComboBoxInputMode.READ_ONLY) {
                /// Replaces the focus target without carrying a draft across modes.
                @Override
                protected void invalidated() {
                    if (popup != null) popup.dismissImmediately();
                    query = null;
                    boolean editable = get() == M3ComboBoxInputMode.FILTERED_SELECTION
                            || get() == M3ComboBoxInputMode.FREE_INPUT;
                    if (!editable) editor = null;
                    else if (editor == null) editor = createEditor();
                    composing = false;
                    pseudoClassStateChanged(EDITOR_FOCUSED, editor != null && editor.isFocused());
                    setFocusTraversable(!editable);
                    restoreEditor();
                    notifyAccessibleAttributeChanged(AccessibleAttribute.EDITABLE);
                }
            };

    /// Returns the current input mode.
    public M3ComboBoxInputMode getInputMode() {
        return inputMode.get();
    }

    /// Sets the input mode. Switching modes dismisses the menu and preserves the committed value.
    public void setInputMode(M3ComboBoxInputMode mode) {
        inputMode.set(Objects.requireNonNull(mode, "mode"));
    }

    /// The input mode, initially [M3ComboBoxInputMode#READ_ONLY].
    public ObjectProperty<M3ComboBoxInputMode> inputModeProperty() {
        return inputMode;
    }

    /// The optional matcher for non-empty queries.
    private final ObjectProperty<@Nullable BiPredicate<? super T, String>> filter =
            new SimpleObjectProperty<>(this, "filter") {
                /// Re-evaluates an open search when its matching rule changes.
                @Override
                protected void invalidated() {
                    if (isShowing() && popup != null) popup.updateFilter();
                }
            };

    /// Returns the custom item matcher, or null for case-insensitive substring matching of display text.
    public @Nullable BiPredicate<? super T, String> getFilter() {
        return filter.get();
    }

    /// Sets the matcher. It receives an item and the unmodified query. Empty queries always match all items.
    public void setFilter(@Nullable BiPredicate<? super T, String> matcher) {
        filter.set(matcher);
    }

    /// The matcher used by both text input modes. The default is null.
    public ObjectProperty<@Nullable BiPredicate<? super T, String>> filterProperty() {
        return filter;
    }

    /// Styles the field when its native editor has keyboard focus.
    private static final PseudoClass EDITOR_FOCUSED = PseudoClass.getPseudoClass("editor-focused");
    /// Marks a rejected custom value until the draft is edited or discarded.
    private static final PseudoClass ERROR = PseudoClass.getPseudoClass("error");

    /// The optional conversion from custom input to a committed value.
    private final ObjectProperty<@Nullable Function<String, ? extends @Nullable T>> parser =
            new SimpleObjectProperty<>(this, "parser") {
                /// Allows a rejected draft to be retried under the new parser.
                @Override
                protected void invalidated() {
                    pseudoClassStateChanged(ERROR, false);
                }
            };

    /// Returns the free-input parser, or null if only suggestions can be committed.
    public @Nullable Function<String, ? extends @Nullable T> getParser() {
        return parser.get();
    }

    /// Sets the parser used when the user submits a draft in [M3ComboBoxInputMode#FREE_INPUT].
    /// For a string-valued field, use [Function#identity()] to accept the input verbatim.
    public void setParser(@Nullable Function<String, ? extends @Nullable T> parser) {
        this.parser.set(parser);
    }

    /// Converts a submitted draft (including empty text) to a value. The parser is not
    /// called while typing or selecting a suggestion. A missing parser, a null result, or an
    /// [IllegalArgumentException] rejects the submission: the field retains both its previous value
    /// and the editable draft and activates the `:error` pseudo-class. The default parser is null.
    public ObjectProperty<@Nullable Function<String, ? extends @Nullable T>> parserProperty() {
        return parser;
    }

    /// Creates a single-line editor whose uncommitted text is independent of the value.
    private TextField createEditor() {
        var text = new TextField();
        text.getStyleClass().add("m3-combo-editor");
        text.promptTextProperty().bind(promptText);
        text.accessibleTextProperty().bind(accessibleTextProperty());
        text.setMinWidth(0);
        text.addEventFilter(InputMethodEvent.INPUT_METHOD_TEXT_CHANGED,
                event -> composing = !event.getComposed().isEmpty());
        text.textProperty().addListener((obs, old, next) -> {
            if (editor != text || updatingEditor || composing) return;
            query = next;
            pseudoClassStateChanged(ERROR, false);
            if (isShowing() && popup != null) {
                popup.updateFilter();
                // A new draft supersedes keyboard navigation even when the same suggestions still match.
                if (getInputMode() == M3ComboBoxInputMode.FREE_INPUT) popup.list.getSelectionModel().clearSelection();
            }
            else show();
        });
        text.focusedProperty().addListener((obs, old, focused) -> {
            if (editor != text) return;
            pseudoClassStateChanged(EDITOR_FOCUSED, focused);
            if (!focused) {
                if (getInputMode() == M3ComboBoxInputMode.FREE_INPUT && query != null && !composing) {
                    commitText(query);
                    // A rejected draft remains editable, but its suggestions must not follow focus.
                    if (popup != null) popup.hide();
                } else {
                    hide();
                }
                composing = false;
            }
        });
        return text;
    }

    /// Returns the editor to the committed value without filtering the available choices.
    private void restoreEditor() {
        query = null;
        pseudoClassStateChanged(ERROR, false);
        if (editor == null) return;
        String display = format(getValue());
        if (!display.equals(editor.getText())) {
            updatingEditor = true;
            try {
                editor.setText(display);
                editor.positionCaret(display.length());
            } finally {
                updatingEditor = false;
            }
        }
    }

    /// Requests focus on the text editor when present, or on the field otherwise.
    @Override
    public void requestFocus() {
        if (editor != null) editor.requestFocus();
        else super.requestFocus();
    }

    /// The available choices in display order.
    private final ObservableList<T> items = FXCollections.observableArrayList();

    /// Returns the live choices. Changes update filtered results in place and never clear the committed value.
    /// In read-only mode changing this list dismisses an open menu.
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

    /// The optional local motion preference.
    private @Nullable StyleableBooleanProperty animationsEnabled;

    /// Returns whether the field permits optional motion.
    @Override
    public boolean isAnimationsEnabled() {
        return animationsEnabled == null || animationsEnabled.get();
    }

    /// Enables or disables optional motion in the field and its menu.
    @Override
    public void setAnimationsEnabled(boolean enabled) {
        animationsEnabledProperty().set(enabled);
    }

    /// The inherited animation preference, initially true.
    @Override
    public StyleableBooleanProperty animationsEnabledProperty() {
        if (animationsEnabled == null) {
            animationsEnabled = new SimpleStyleableBooleanProperty(ANIMATIONS_ENABLED, this, "animationsEnabled", true);
        }
        return animationsEnabled;
    }

    /// Returns the supported CSS properties, including the inherited animation switch.
    public static @Unmodifiable List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return STYLEABLES;
    }

    /// Returns the supported CSS properties for this field.
    @Override
    public @Unmodifiable List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return STYLEABLES;
    }

    /// Reports whether the dropdown is open for interaction.
    private final ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing");

    /// Returns whether the dropdown is open. It is no longer open once its exit transition begins.
    public boolean isShowing() {
        return showing.get();
    }

    /// The expanded state of the dropdown, initially false.
    public ReadOnlyBooleanProperty showingProperty() {
        return showing.getReadOnlyProperty();
    }

    /// Shows the available choices. Does nothing for an unreachable field, or for an empty
    /// list when the user is not editing. A search with no matches leaves the editor active.
    public void show() {
        if (isShowing() || (items.isEmpty() && query == null) || !M3Accessible.canReach(this) || !M3PopupWindows.canShow(this)) return;
        if (popup == null) popup = new ChoicePopup();
        popup.open();
    }

    /// Dismisses the menu and discards any draft without committing the highlighted choice.
    public void hide() {
        if (popup != null) popup.hide();
        if (query != null) restoreEditor();
    }

    /// Returns the user-facing text of a value.
    private String format(@Nullable T item) {
        if (item == null) return "";
        var formatter = getConverter();
        return formatter == null ? item.toString() : Objects.toString(formatter.apply(item), "");
    }

    /// Commits custom input without inserting it into the suggestion list.
    private void commitText(String text) {
        if (!value.isBound()) {
            var parse = getParser();
            @Nullable T parsed;
            try {
                parsed = parse == null ? null : parse.apply(text);
            } catch (IllegalArgumentException exception) {
                parsed = null;
            }
            if (parsed == null) {
                pseudoClassStateChanged(ERROR, true);
                return;
            }
            setValue(parsed);
        }
        hide();
    }

    /// Handles field activation and keyboard navigation in the detached menu.
    private void handleKey(KeyEvent event) {
        if (isDisabled() || composing || event.isControlDown() || event.isMetaDown()
                || (event.isShiftDown() && event.getCode() != KeyCode.TAB)) {
            return;
        }
        switch (event.getCode()) {
            case DOWN, UP, SPACE, ENTER -> {
                if (event.getCode() == KeyCode.ENTER && getInputMode() == M3ComboBoxInputMode.FREE_INPUT
                        && query != null && (!isShowing() || popup == null || popup.list.getSelectionModel().isEmpty())) {
                    commitText(query);
                    event.consume();
                    return;
                }
                if (editor != null && (event.getCode() == KeyCode.SPACE
                        || event.getCode() == KeyCode.ENTER && !isShowing())) return;
                if (!isShowing()) show();
                else if (popup != null) {
                    switch (event.getCode()) {
                        case DOWN -> popup.highlight(popup.list.getSelectionModel().getSelectedIndex() + 1);
                        case UP -> popup.highlight(popup.list.getSelectionModel().isEmpty()
                                ? popup.list.getItems().size() - 1 : popup.list.getSelectionModel().getSelectedIndex() - 1);
                        default -> popup.commit(popup.list.getSelectionModel().getSelectedIndex());
                    }
                }
            }
            case ESCAPE -> { if (!isShowing() && query == null) return; hide(); }
            case TAB -> {
                if (getInputMode() != M3ComboBoxInputMode.FREE_INPUT) hide();
                return; // Let normal focus traversal finish the edit.
            }
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
            case EDITABLE -> editor != null;
            case ITEM_COUNT -> isShowing() && popup != null ? popup.list.getItems().size() : items.size();
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
        /// Holds the active value presentation and the disclosure arrow.
        private final HBox row = new HBox(12);

        /// Creates the field's presentation without creating its popup.
        FieldSkin() {
            super(M3ComboBox.this);
            var arrow = new M3DisclosureIcon();
            arrow.setVertical(true);
            arrow.animationsEnabledProperty().bind(animationsEnabledProperty());
            arrow.setExpanded(isShowing());
            registerChangeListener(showing, obs -> arrow.setExpanded(isShowing()));
            label.setMaxWidth(Double.MAX_VALUE);
            label.setMinWidth(0);
            HBox.setHgrow(label, Priority.ALWAYS);
            row.setAlignment(Pos.CENTER_LEFT);
            row.getChildren().addAll(label, arrow);
            updateField();
            registerChangeListener(inputMode, obs -> updateField());
            getChildren().add(row);
            registerChangeListener(value, obs -> updateLabel());
            registerChangeListener(promptText, obs -> updateLabel());
            registerChangeListener(converter, obs -> updateLabel());
            updateLabel();
        }

        /// Exchanges the value presentation without replacing the disclosure icon or popup.
        private void updateField() {
            Node previous = row.getChildren().get(0);
            boolean focused = isFocused() || previous.isFocused();
            Node content = editor == null ? label : editor;
            HBox.setHgrow(content, Priority.ALWAYS);
            row.getChildren().set(0, content);
            row.setMouseTransparent(editor == null);
            if (focused) requestFocus();
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
    private final class ChoicePopup extends Popup {
        /// The currently highlighted option is local to this list.
        final ListView<T> list = new ListView<>(items);
        /// A live view of source choices, allocated only when an editable menu is used.
        private @Nullable FilteredList<T> filtered;
        /// Scroll behavior for the option list's currently installed skin.
        private @Nullable M3VirtualFlowScroll scroll;
        /// Synchronizes the detached list with the field's theme.
        final M3PopupContextSynchronizer context = new M3PopupContextSynchronizer(
                M3ComboBox.this, getScene().getRoot(), getUserAgentStylesheet());
        /// Drives only render transforms; popup measurement and menu scrolling remain unchanged.
        final M3NodeTransition motion = new M3NodeTransition(list);
        /// Translation that keeps the collapsed surface against its anchor-facing edge.
        double collapsedOffset;
        /// Closes the popup if the anchor is removed, hidden, or disabled.
        final M3ReachabilityObserver reachability = new M3ReachabilityObserver(M3ComboBox.this, () -> {
            if (!M3Accessible.canReach(M3ComboBox.this)) super.hide();
        });

        /// Initializes the popup once; cells are reused as the menu scrolls.
        ChoicePopup() {
            list.getStyleClass().add("m3-combo-options");
            list.setFixedCellSize(48);
            list.setFocusTraversable(false);
            list.skinProperty().addListener((obs, old, next) -> {
                if (scroll != null) {
                    scroll.dispose();
                    scroll = null;
                }
                if (next != null && isShowing()) installScrolling();
            });
            list.setCellFactory(view -> new OptionCell());
            list.addEventFilter(KeyEvent.KEY_PRESSED, M3ComboBox.this::handleKey);
            setAutoHide(true);
            // Escape belongs to the editor while the input method is composing.
            setHideOnEscape(false);
            getScene().getRoot().getStyleClass().add("m3-combo-popup-root");
            M3PopupStyles.prepareStandalonePopupRoot(getScene().getRoot(), getUserAgentStylesheet());
            getContent().add(new StackPane(list));
            motion.setOnFinished(e -> {
                if (!M3ComboBox.this.isShowing()) super.hide();
            });
            setOnShown(e -> {
                context.start();
                installScrolling();
                reachability.install();
                transitionTo(true);
            });
            setOnHidden(e -> {
                motion.stop();
                if (scroll != null) scroll.stop();
                reachability.uninstall();
                context.stop();
                setVisibleState(false);
                if (!isShowing() && filtered != null) filtered.setPredicate(null);
            });
        }

        /// Presents a virtualized option with the same pointer and selection feedback as a menu item.
        @NotNullByDefault
        private final class OptionCell extends ListCell<T> {
            /// One presentation per cell, not per suggestion.
            private final M3MenuItem row = new M3MenuItem();
            /// Index currently presented, including when several options refer to the same object.
            private int rowIndex = -1;

            /// Leaves editor focus and pending selection under the control of the dropdown.
            OptionCell() {
                row.setFocusTraversable(false);
                row.setMinSize(0, 0);
                row.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                row.animationsEnabledProperty().bind(M3ComboBox.this.animationsEnabledProperty());
                row.setOnAction(event -> {
                    if (!isEmpty()) commit(getIndex());
                    event.consume();
                });
            }

            /// Keeps the row flush with the cell, independently of its label's preferred width.
            @Override protected Skin<?> createDefaultSkin() {
                return new SkinBase<OptionCell>(this) {
                    {
                        if (getGraphic() != null) getChildren().add(row);
                        registerChangeListener(graphicProperty(), obs -> {
                            if (getGraphic() == null) getChildren().clear();
                            else getChildren().setAll(row);
                        });
                    }
                };
            }

            /// Reassignment ends the previous item's transient feedback before presenting its replacement.
            @Override protected void updateItem(@Nullable T item, boolean empty) {
                boolean replaced = getItem() != item || rowIndex != getIndex() || empty != isEmpty();
                super.updateItem(item, empty);
                rowIndex = getIndex();
                if (replaced) setGraphic(null);
                String label = empty ? "" : format(item);
                setAccessibleText(empty ? null : label);
                if (!empty) {
                    row.setHeadlineText(label);
                    if (replaced) row.setSelected(list.getSelectionModel().isSelected(getIndex()));
                    setGraphic(row);
                }
            }

            /// Animates changes to the pending option without changing the committed value.
            @Override public void updateSelected(boolean selected) {
                super.updateSelected(selected);
                row.setSelected(!isEmpty() && isSelected());
            }
        }

        /// Adapts the skin's viewport without adding a second scroll container.
        private void installScrolling() {
            if (scroll == null && list.lookup(".virtual-flow") instanceof VirtualFlow<?> flow) {
                scroll = new M3VirtualFlowScroll(M3ComboBox.this, list, flow,
                        () -> Math.max(0, list.getItems().size() * list.getFixedCellSize() - flow.getHeight()));
            }
        }

        /// Publishes the expanded state to the field and its accessible peers.
        void setVisibleState(boolean visible) {
            if (!visible && query != null && getInputMode() == M3ComboBoxInputMode.FILTERED_SELECTION) restoreEditor();
            pseudoClassStateChanged(SHOWING, visible);
            M3ComboBox.this.showing.set(visible);
            notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
            notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
        }

        /// Fits the menu to the larger available side if it cannot fit below the field.
        void open() {
            // An outside click can both finish a free-input edit and activate a form action.
            setConsumeAutoHidingEvents(getInputMode() != M3ComboBoxInputMode.FREE_INPUT);
            updateFilter();
            @Nullable M3PopupPositioning.Placement placement = position();
            if (placement == null) return;
            if (isShowing()) {
                setAnchorX(placement.x());
                setAnchorY(placement.y());
                transitionTo(true);
                return;
            }
            list.setScaleX(COLLAPSED_SCALE);
            list.setScaleY(COLLAPSED_SCALE);
            list.setTranslateY(collapsedOffset);
            list.setOpacity(0);
            M3PopupWindows.show(this, M3ComboBox.this, placement.x(), placement.y());
        }

        /// Updates the menu size and computes a placement for the current number of matching rows.
        private M3PopupPositioning.@Nullable Placement position() {
            @Nullable Bounds anchor = localToScreen(getLayoutBounds());
            if (anchor == null) return null;
            Rectangle2D screen = M3PopupPositioning.visualBoundsFor(anchor);
            double available = Math.max(screen.getMaxY() - anchor.getMaxY(), anchor.getMinY() - screen.getMinY()) - 12;
            if (available <= 0) return null;
            double width = Math.min(anchor.getWidth(), screen.getWidth() - 16);
            double height = Math.min(16 + list.getFixedCellSize() * Math.max(1, Math.min(8, list.getItems().size())), available);
            list.setPrefSize(width, height);
            var placement = M3PopupPositioning.menuBelowOrAbove(anchor, screen, width, height, 4);
            collapsedOffset = (placement.opensAbove() ? 1 : -1) * height * (1 - COLLAPSED_SCALE) / 2;
            return placement;
        }

        /// Keeps source ordering and object identity, including for equal display strings.
        private void updateFilter() {
            if (editor == null) {
                list.setItems(items);
                filtered = null;
                return;
            }
            if (filtered == null) {
                filtered = new FilteredList<>(items);
                list.setItems(filtered);
                filtered.addListener((ListChangeListener<T>) change -> {
                    if (!M3ComboBox.this.isShowing()) return;
                    highlightValue();
                    var placement = position();
                    if (placement != null) {
                        setAnchorX(placement.x());
                        setAnchorY(placement.y());
                    }
                    notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
                });
            }
            @Nullable String search = query;
            var matcher = getFilter();
            if (search == null || search.isEmpty()) {
                filtered.setPredicate(null);
            } else if (matcher != null) {
                filtered.setPredicate(item -> matcher.test(item, search));
            } else {
                String needle = search.toLowerCase(Locale.ROOT);
                filtered.setPredicate(item -> format(item).toLowerCase(Locale.ROOT).contains(needle));
            }
        }

        /// Ends a popup presentation when the input mode changes.
        private void dismissImmediately() {
            super.hide();
            list.setItems(items);
            filtered = null;
        }

        /// Routes native auto-hide and explicit dismissal through the same exit motion.
        @Override
        public void hide() {
            if (!isShowing() || !M3PopupWindows.canShow(M3ComboBox.this)) {
                super.hide();
            } else if (M3ComboBox.this.isShowing()) {
                transitionTo(false);
            }
        }

        /// Reverses in flight from the current visual state without moving the native window.
        private void transitionTo(boolean expanded) {
            list.setMouseTransparent(!expanded);
            if (expanded) highlightValue();
            else if (scroll != null) scroll.stop();
            setVisibleState(expanded);
            if (M3ComboBox.this.isShowing() != expanded || !isShowing()) return;
            double scale = expanded ? 1 : COLLAPSED_SCALE;
            motion.configure(M3Animation.fastSpatial(M3ComboBox.this), expanded ? 1 : 0,
                    scale, scale, 0, expanded ? 0 : collapsedOffset);
            M3Animation.playFromStart(M3ComboBox.this, motion);
        }

        /// Starts browsing at the committed option, or the first option.
        void highlightValue() {
            if (query != null && getInputMode() == M3ComboBoxInputMode.FREE_INPUT) {
                list.getSelectionModel().clearSelection();
            } else {
                highlight(list.getItems().indexOf(getValue()));
            }
        }

        /// Keeps the pending choice visible while the user browses.
        void highlight(int index) {
            if (list.getItems().isEmpty()) {
                list.getSelectionModel().clearSelection();
                return;
            }
            int target = Math.max(0, Math.min(index, list.getItems().size() - 1));
            list.getSelectionModel().select(target);
            if (scroll != null) scroll.reveal(target);
            else list.scrollTo(target); // Defer the initial reveal until the skin is installed.
        }

        /// Commits one option without attempting to write a bound property.
        void commit(int index) {
            if (!M3ComboBox.this.isShowing()) return;
            var choices = list.getItems();
            if (index < 0 || index >= choices.size()) return;
            if (!isDisabled() && !value.isBound()) setValue(choices.get(index));
            M3ComboBox.this.hide();
        }
    }
}
