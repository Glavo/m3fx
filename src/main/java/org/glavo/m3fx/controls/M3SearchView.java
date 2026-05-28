// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3SearchViewSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A Material Design 3 search view with a search bar and result content.
///
/// `M3SearchView` combines an embedded [M3SearchBar] with a result container that can animate into and out of
/// view as search becomes active. Applications add arbitrary JavaFX nodes to the results list and can listen to
/// the search bar action to submit or update search queries.
///
/// See [Material Design search](https://m3.material.io/components/search/overview).
@NotNullByDefault
public class M3SearchView extends Control {
    /// The base style class for M3FX search views.
    public static final String STYLE_CLASS = "m3-search-view";

    /// The style class applied to the result container.
    public static final String RESULTS_STYLE_CLASS = "m3-search-view-results";

    /// The vertical offset used while search results are hidden.
    private static final double HIDDEN_RESULTS_TRANSLATE_Y = -8.0;

    /// The embedded search bar.
    private final M3SearchBar searchBar = new M3SearchBar();

    /// The search result container.
    private final VBox resultsBox = new VBox();

    /// The search result visibility animation.
    private final Timeline resultsVisibilityAnimation = new Timeline();

    /// Creates an empty search view.
    public M3SearchView() {
        initialize();
    }

    /// Creates a search view with prompt text.
    ///
    /// @param promptText the prompt text displayed by the embedded search bar
    public M3SearchView(String promptText) {
        initialize();
        setPromptText(promptText);
    }

    /// Creates a search view with prompt text and initial result nodes.
    ///
    /// @param promptText the prompt text displayed by the embedded search bar
    /// @param results the initial result nodes displayed below the search bar
    public M3SearchView(String promptText, Node... results) {
        this(promptText);
        addResults(results);
    }

    /// Returns the embedded search bar.
    ///
    /// @return the embedded search bar
    public final M3SearchBar getSearchBar() {
        return searchBar;
    }

    /// Returns the result container used by the default skin.
    ///
    /// @return the result container used by the default skin
    public final VBox getResultsContainer() {
        return resultsBox;
    }

    /// Returns the mutable result node list.
    ///
    /// @return the mutable result node list displayed below the search bar
    public final ObservableList<Node> getResults() {
        return resultsBox.getChildren();
    }

    /// Adds one result node.
    ///
    /// @param result the result node to add
    public final void addResult(Node result) {
        getResults().add(Objects.requireNonNull(result, "result"));
    }

    /// Adds result nodes.
    ///
    /// @param results the result nodes to add
    public final void addResults(Node... results) {
        validateResults(results);
        getResults().addAll(results);
    }

    /// Replaces all result nodes.
    ///
    /// @param results the replacement result nodes
    public final void setResults(Node... results) {
        validateResults(results);
        getResults().setAll(results);
    }

    /// Removes all result nodes.
    public final void clearResults() {
        getResults().clear();
    }

    /// Returns the editable search input used by the embedded search bar.
    ///
    /// @return the embedded editable search input
    public final TextField getEditor() {
        return searchBar.getEditor();
    }

    /// Returns the leading content node from the embedded search bar.
    ///
    /// @return the leading content node, or `null` if none is set
    public final @Nullable Node getLeading() {
        return searchBar.getLeading();
    }

    /// Sets the leading content node on the embedded search bar.
    ///
    /// @param leading the leading content node, or `null` to clear it
    public final void setLeading(@Nullable Node leading) {
        searchBar.setLeading(leading);
    }

    /// Returns the embedded search bar leading content node property.
    ///
    /// @return the embedded search bar leading content node property
    public final ObjectProperty<@Nullable Node> leadingProperty() {
        return searchBar.leadingProperty();
    }

    /// Returns the mutable trailing action list from the embedded search bar.
    ///
    /// @return the mutable trailing action list from the embedded search bar
    public final ObservableList<Node> getTrailingActions() {
        return searchBar.getTrailingActions();
    }

    /// Adds one trailing action node to the embedded search bar.
    ///
    /// @param action the trailing action node to add
    public final void addTrailingAction(Node action) {
        searchBar.addTrailingAction(action);
    }

    /// Adds trailing action nodes to the embedded search bar.
    ///
    /// @param actions the trailing action nodes to add
    public final void addTrailingActions(Node... actions) {
        searchBar.addTrailingActions(actions);
    }

    /// Replaces all trailing action nodes in the embedded search bar.
    ///
    /// @param actions the replacement trailing action nodes
    public final void setTrailingActions(Node... actions) {
        searchBar.setTrailingActions(actions);
    }

    /// Removes all trailing action nodes from the embedded search bar.
    public final void clearTrailingActions() {
        searchBar.clearTrailingActions();
    }

    /// Returns the text entered in the embedded search bar.
    ///
    /// @return the text entered in the embedded search bar
    public final String getText() {
        return searchBar.getText();
    }

    /// Sets the text entered in the embedded search bar.
    ///
    /// @param text the text entered in the embedded search bar
    public final void setText(String text) {
        searchBar.setText(Objects.requireNonNull(text, "text"));
    }

    /// Returns the embedded search bar text property.
    ///
    /// @return the embedded search bar text property
    public final StringProperty textProperty() {
        return searchBar.textProperty();
    }

    /// Returns the prompt text displayed by the embedded search bar.
    ///
    /// @return the prompt text displayed by the embedded search bar
    public final String getPromptText() {
        return searchBar.getPromptText();
    }

    /// Sets the prompt text displayed by the embedded search bar.
    ///
    /// @param promptText the prompt text displayed by the embedded search bar
    public final void setPromptText(String promptText) {
        searchBar.setPromptText(Objects.requireNonNull(promptText, "promptText"));
    }

    /// Returns the embedded search bar prompt text property.
    ///
    /// @return the embedded search bar prompt text property
    public final StringProperty promptTextProperty() {
        return searchBar.promptTextProperty();
    }

    /// Returns the search submission handler.
    ///
    /// @return the search submission handler, or `null` if none is set
    public final @Nullable EventHandler<ActionEvent> getOnAction() {
        return searchBar.getOnAction();
    }

    /// Sets the search submission handler.
    ///
    /// @param onAction the search submission handler, or `null` to clear it
    public final void setOnAction(@Nullable EventHandler<ActionEvent> onAction) {
        searchBar.setOnAction(onAction);
    }

    /// Returns the search submission handler property.
    ///
    /// @return the search submission handler property
    public final ObjectProperty<@Nullable EventHandler<ActionEvent>> onActionProperty() {
        return searchBar.onActionProperty();
    }

    /// Fires the embedded search bar action event.
    public final void fire() {
        searchBar.fire();
    }

    /// Returns whether this search view is showing active search results.
    ///
    /// @return `true` if this search view is showing active search results
    public final boolean isActive() {
        return searchBar.isActive();
    }

    /// Sets whether this search view is showing active search results.
    ///
    /// @param active whether this search view is showing active search results
    public final void setActive(boolean active) {
        searchBar.setActive(active);
    }

    /// Returns the active search result state property.
    ///
    /// @return the active search result state property
    public final BooleanProperty activeProperty() {
        return searchBar.activeProperty();
    }

    /// Moves the search view into its active result state.
    public final void activate() {
        searchBar.activate();
    }

    /// Moves the search view out of its active result state.
    public final void deactivate() {
        searchBar.deactivate();
    }

    /// Clears the embedded search bar text.
    public final void clear() {
        searchBar.clear();
    }

    /// Clears the embedded search text and moves this search view out of its active result state.
    public final void clearAndDeactivate() {
        searchBar.clearAndDeactivate();
    }

    /// Returns the user-agent stylesheet for M3FX search views.
    ///
    /// @return the search user-agent stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("search.css");
    }

    /// Returns accessibility attributes for search results and active state.
    ///
    /// @param attribute the requested accessibility attribute
    /// @param parameters the optional attribute parameters
    /// @return the attribute value, or `null` when unavailable
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case EXPANDED -> isActive();
            case TEXT -> getText();
            case FOCUS_NODE -> accessibleFocusNode();
            case ITEM_COUNT -> getResults().size();
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getResults(), parameters);
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes search text, focus, and active-state accessibility actions.
    ///
    /// @param action the requested accessibility action
    /// @param parameters the optional action parameters
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case SET_TEXT -> {
                if (parameters.length > 0 && parameters[0] instanceof String text) {
                    setText(text);
                }
            }
            case REQUEST_FOCUS -> {
                activate();
                getEditor().requestFocus();
                notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
            }
            case FIRE -> fire();
            case EXPAND -> activate();
            case SHOW_ITEM -> showAccessibleResult(parameters);
            case COLLAPSE -> deactivate();
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Creates the default Material Design 3 search view skin.
    ///
    /// @return the default Material Design 3 search view skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3SearchViewSkin(this);
    }

    /// Adds base style classes and configures search result behavior.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        resultsBox.getStyleClass().add(RESULTS_STYLE_CLASS);
        searchBar.activeProperty().addListener((observable, oldValue, newValue) -> {
            notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
            notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
            updateResultsVisibility();
        });
        searchBar.textProperty().addListener((observable, oldValue, newValue) ->
                notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT));
        resultsBox.getChildren().addListener((ListChangeListener<Node>) change -> {
            notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
            notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
            notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        });
        addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        setActive(true);
        applyResultsVisibilityImmediately(isActive());
    }

    /// Handles keyboard movement between the search editor and result items.
    private void handleKeyPressed(KeyEvent event) {
        KeyCode code = event.getCode();
        switch (code) {
            case DOWN -> {
                if (focusNextResult()) {
                    event.consume();
                }
            }
            case UP -> {
                if (focusPreviousResult()) {
                    event.consume();
                }
            }
            case ESCAPE -> {
                if (isActive()) {
                    deactivate();
                    searchBar.requestFocus();
                    event.consume();
                }
            }
            default -> {
            }
        }
    }

    /// Shows and focuses the result referenced by accessibility action parameters.
    private void showAccessibleResult(Object... parameters) {
        activate();
        if (parameters.length == 0) {
            if (!focusResultAt(0)) {
                getEditor().requestFocus();
            }
            return;
        }

        @Nullable Node item = accessibleResultActionItem(parameters);
        if (item == null || M3Accessible.focusTarget(item) == null) {
            getEditor().requestFocus();
            return;
        }
        M3Accessible.showItem(item);
    }

    /// Returns the result referenced by accessibility action parameters.
    private @Nullable Node accessibleResultActionItem(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            return null;
        }
        if (parameters[0] instanceof Number) {
            return M3Accessible.itemAt(getResults(), parameters);
        }
        for (Object parameter : parameters) {
            @Nullable Node item = accessibleResultActionItem(parameter);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /// Returns the result referenced by one accessibility action parameter.
    private @Nullable Node accessibleResultActionItem(@Nullable Object parameter) {
        if (parameter instanceof Number number) {
            return M3Accessible.itemAt(getResults(), number);
        }
        if (parameter instanceof Node node && getResults().contains(node)) {
            return node;
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                @Nullable Node item = accessibleResultActionItem(value);
                if (item != null) {
                    return item;
                }
            }
            return null;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                @Nullable Node item = accessibleResultActionItem(value);
                if (item != null) {
                    return item;
                }
            }
        }
        return null;
    }

    /// Focuses the next result relative to the current focus owner.
    private boolean focusNextResult() {
        if (!isActive()) {
            activate();
        }

        int currentIndex = focusedResultIndex();
        if (currentIndex < 0) {
            return focusResultAt(0);
        }
        return focusResultAt(currentIndex + 1);
    }

    /// Focuses the previous result or returns focus to the editor from the first result.
    private boolean focusPreviousResult() {
        int currentIndex = focusedResultIndex();
        if (currentIndex < 0) {
            return false;
        }
        if (currentIndex == 0) {
            getEditor().requestFocus();
            notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
            return true;
        }
        return focusResultAt(currentIndex - 1);
    }

    /// Focuses a result at an index when it can be reached.
    private boolean focusResultAt(int index) {
        if (index < 0 || index >= getResults().size()) {
            return false;
        }

        @Nullable Node result = getResults().get(index);
        if (M3Accessible.focusTarget(result) == null) {
            return false;
        }
        M3Accessible.showItem(result);
        notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
        return true;
    }

    /// Returns the current accessibility focus node.
    ///
    /// @return the focused result item when focus is inside results, otherwise the search editor
    private Node accessibleFocusNode() {
        int resultIndex = focusedResultIndex();
        if (resultIndex >= 0) {
            @Nullable Node result = getResults().get(resultIndex);
            @Nullable Node focusTarget = M3Accessible.focusTarget(result);
            if (focusTarget != null) {
                return focusTarget;
            }
        }
        return getEditor();
    }

    /// Returns the index of the result containing current keyboard focus.
    private int focusedResultIndex() {
        if (getScene() == null) {
            return -1;
        }

        @Nullable Node focusOwner = getScene().getFocusOwner();
        if (focusOwner == null) {
            return -1;
        }

        ObservableList<Node> results = getResults();
        for (int index = 0; index < results.size(); index++) {
            Node result = results.get(index);
            if (M3Accessible.containsNode(result, focusOwner)) {
                return index;
            }
        }
        return -1;
    }

    /// Updates result container visibility from the active state, using motion when attached to a scene.
    private void updateResultsVisibility() {
        boolean active = isActive();
        if (getScene() == null) {
            applyResultsVisibilityImmediately(active);
            return;
        }

        resultsVisibilityAnimation.stop();
        if (active) {
            resultsBox.setVisible(true);
            resultsBox.setManaged(true);
            M3MotionSpec spec = M3Animation.fastSpatial(this);
            resultsVisibilityAnimation.getKeyFrames().setAll(new KeyFrame(
                    spec.duration(),
                    new KeyValue(resultsBox.opacityProperty(), 1.0, spec.interpolator()),
                    new KeyValue(resultsBox.translateYProperty(), 0.0, spec.interpolator())
            ));
        } else if (resultsBox.isVisible()) {
            M3MotionSpec spec = M3Animation.fastSpatial(this);
            resultsVisibilityAnimation.getKeyFrames().setAll(new KeyFrame(
                    spec.duration(),
                    event -> applyResultsVisibilityImmediately(false),
                    new KeyValue(resultsBox.opacityProperty(), 0.0, spec.interpolator()),
                    new KeyValue(resultsBox.translateYProperty(), HIDDEN_RESULTS_TRANSLATE_Y, spec.interpolator())
            ));
        } else {
            applyResultsVisibilityImmediately(false);
            return;
        }
        M3Animation.playFromStart(this, resultsVisibilityAnimation);
    }

    /// Applies result container visibility without animation.
    private void applyResultsVisibilityImmediately(boolean active) {
        resultsVisibilityAnimation.stop();
        resultsBox.setVisible(active);
        resultsBox.setManaged(active);
        resultsBox.setOpacity(active ? 1.0 : 0.0);
        resultsBox.setTranslateY(active ? 0.0 : HIDDEN_RESULTS_TRANSLATE_Y);
    }

    /// Validates a result node array.
    private static void validateResults(Node... results) {
        Objects.requireNonNull(results, "results");
        for (Node result : results) {
            Objects.requireNonNull(result, "result");
        }
    }
}
