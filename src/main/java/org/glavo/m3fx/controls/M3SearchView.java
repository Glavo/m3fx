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
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
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

    /// The style class applied to the internal content column.
    public static final String CONTENT_STYLE_CLASS = "m3-search-view-content";

    /// The style class applied to the result container.
    public static final String RESULTS_STYLE_CLASS = "m3-search-view-results";

    /// The vertical offset used while search results are hidden.
    private static final double HIDDEN_RESULTS_TRANSLATE_Y = -8.0;

    /// The fallback result row height used for page navigation before results have been measured.
    private static final double DEFAULT_RESULT_PAGE_ROW_HEIGHT = 56.0;

    /// The fallback result page step used before the search view has a measured result viewport.
    private static final int DEFAULT_RESULT_PAGE_STEP = 5;

    /// The embedded search bar.
    private final M3SearchBar searchBar = new M3SearchBar();

    /// The search result container.
    private final VBox resultsBox = new VBox();

    /// The search result visibility animation.
    private final Timeline resultsVisibilityAnimation = new Timeline();

    /// Observes runtime motion settings while this search view is attached to a scene.
    private final M3MotionSettingsObserver motionSettingsObserver =
            new M3MotionSettingsObserver(this, this::refreshMotionSettings);

    /// Notifies accessibility clients when focus moves between the search bar and results.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, this::currentFocusNode);

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
            case REQUEST_FOCUS -> focusAccessibleNode();
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
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleNode, this::showAccessibleResult);
        resultsBox.getStyleClass().add(RESULTS_STYLE_CLASS);
        searchBar.activeProperty().addListener((observable, oldValue, newValue) -> {
            boolean restoreSearchBarFocus = !newValue && isFocusInsideResults();
            notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
            if (restoreSearchBarFocus) {
                restoreSearchBarFocus();
            }
            notifyFocusNodeChanged();
            updateResultsVisibility();
        });
        searchBar.textProperty().addListener((observable, oldValue, newValue) ->
                notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT));
        resultsBox.getChildren().addListener((ListChangeListener<Node>) change -> {
            notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
            notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
            notifyFocusNodeChanged();
        });
        addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        setActive(true);
        applyResultsVisibilityImmediately(isActive());
        focusNotifier.start();
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
            case HOME -> {
                if (focusBoundaryResult(false)) {
                    event.consume();
                }
            }
            case END -> {
                if (focusBoundaryResult(true)) {
                    event.consume();
                }
            }
            case PAGE_UP -> {
                if (focusPagedResult(false)) {
                    event.consume();
                }
            }
            case PAGE_DOWN -> {
                if (focusPagedResult(true)) {
                    event.consume();
                }
            }
            case ESCAPE -> {
                if (isActive()) {
                    deactivate();
                    focusSearchBar();
                    event.consume();
                }
            }
            default -> {
            }
        }
    }

    /// Shows and focuses the result referenced by accessibility action parameters.
    ///
    /// @param parameters optional accessibility target parameters
    /// @return `true` when focus moved to the requested result or default target
    final boolean showAccessibleResult(Object... parameters) {
        if (!M3Accessible.canReach(this)) {
            return false;
        }
        @Nullable Node currentFocusNode = currentDefaultShowItemFocusNode();
        activate();
        if (parameters.length == 0) {
            if (currentFocusNode != null && M3Accessible.showItem(this, currentFocusNode)) {
                notifyFocusNodeChanged();
                return true;
            }
            return focusFirstResult() || focusEditor();
        }

        if (M3Accessible.containsAccessibleActionTarget(searchBar, parameters)) {
            if (M3Accessible.showAccessibleActionTarget(this, searchBar, parameters)) {
                notifyFocusNodeChanged();
                return true;
            }
            return false;
        }

        @Nullable Node focusOwnerBefore = getScene() == null ? null : getScene().getFocusOwner();
        boolean shown = M3Accessible.showIndexedItem(this, getResults(), parameters);

        if (getScene() != null
                && getScene().getFocusOwner() == focusOwnerBefore
                && currentFocusNode() == null
                && M3Accessible.canReach(getEditor())) {
            return focusEditor();
        }
        if (shown) {
            notifyFocusNodeChanged();
            return true;
        }
        return false;
    }

    /// Focuses the next result relative to the current focus owner.
    private boolean focusNextResult() {
        if (!M3Accessible.canReach(this)) {
            return false;
        }
        if (!isActive()) {
            activate();
        }

        int currentIndex = focusedResultIndex();
        if (currentIndex < 0) {
            return focusReachableResultFrom(0, 1);
        }
        return focusReachableResultFrom(currentIndex + 1, 1);
    }

    /// Focuses the previous result or returns focus to the editor from the first result.
    private boolean focusPreviousResult() {
        if (!M3Accessible.canReach(this)) {
            return false;
        }
        int currentIndex = focusedResultIndex();
        if (currentIndex < 0) {
            return false;
        }

        int previousIndex = reachableResultIndexFrom(currentIndex - 1, -1);
        if (previousIndex < 0) {
            focusEditor();
            return true;
        }
        return focusResultAt(previousIndex);
    }

    /// Focuses the first reachable result.
    private boolean focusFirstResult() {
        if (!M3Accessible.canReach(this)) {
            return false;
        }
        if (!isActive()) {
            activate();
        }
        return focusReachableResultFrom(0, 1);
    }

    /// Focuses the last reachable result.
    private boolean focusLastResult() {
        if (!M3Accessible.canReach(this)) {
            return false;
        }
        if (!isActive()) {
            activate();
        }
        return focusReachableResultFrom(getResults().size() - 1, -1);
    }

    /// Focuses the first or last result when keyboard focus is already inside the result list.
    private boolean focusBoundaryResult(boolean last) {
        if (focusedResultIndex() < 0) {
            return false;
        }
        return last ? focusLastResult() : focusFirstResult();
    }

    /// Focuses the reachable result one page before or after the current focused result.
    private boolean focusPagedResult(boolean forward) {
        int currentIndex = focusedResultIndex();
        if (currentIndex < 0) {
            return false;
        }

        int targetIndex = pagedResultIndex(currentIndex, forward);
        return targetIndex >= 0 && focusResultAt(targetIndex);
    }

    /// Focuses a result at an index when it can be reached.
    private boolean focusResultAt(int index) {
        if (!M3Accessible.canReach(this)) {
            return false;
        }
        @Nullable Node focusTarget = focusTargetAt(index);
        if (focusTarget == null) {
            return false;
        }
        if (!M3Accessible.showItem(this, focusTarget)) {
            return false;
        }
        notifyFocusNodeChanged();
        return true;
    }

    /// Focuses the first reachable result found from an index in the requested direction.
    private boolean focusReachableResultFrom(int startIndex, int direction) {
        int targetIndex = reachableResultIndexFrom(startIndex, direction);
        return targetIndex >= 0 && focusResultAt(targetIndex);
    }

    /// Returns the first reachable result index found from an index in the requested direction.
    private int reachableResultIndexFrom(int startIndex, int direction) {
        ObservableList<Node> results = getResults();
        int resultCount = results.size();
        if (resultCount == 0) {
            return -1;
        }

        int index = direction > 0
                ? Math.max(0, startIndex)
                : Math.min(resultCount - 1, startIndex);
        while (index >= 0 && index < resultCount) {
            if (focusTargetAt(index) != null) {
                return index;
            }
            index += direction;
        }
        return -1;
    }

    /// Returns the reachable result index for a page navigation step.
    private int pagedResultIndex(int currentIndex, boolean forward) {
        int direction = forward ? 1 : -1;
        int targetIndex = currentIndex;
        int step = resultPageStep();
        for (int offset = 0; offset < step; offset++) {
            int nextIndex = reachableResultIndexFrom(targetIndex + direction, direction);
            if (nextIndex < 0) {
                return targetIndex;
            }
            targetIndex = nextIndex;
        }
        return targetIndex;
    }

    /// Returns the number of reachable results covered by one page navigation key press.
    private int resultPageStep() {
        double viewportHeight = getHeight() - measuredSearchBarHeight();
        if (viewportHeight <= 0.0) {
            viewportHeight = getLayoutBounds().getHeight() - measuredSearchBarHeight();
        }
        if (viewportHeight <= 0.0) {
            viewportHeight = resultsBox.getLayoutBounds().getHeight();
        }

        double rowHeight = estimatedResultRowHeight();
        if (viewportHeight <= 0.0 || rowHeight <= 0.0) {
            return DEFAULT_RESULT_PAGE_STEP;
        }
        return Math.max(1, (int) Math.floor(viewportHeight / rowHeight));
    }

    /// Returns the best available measured or preferred height for the embedded search bar.
    private double measuredSearchBarHeight() {
        double height = searchBar.getHeight();
        if (height <= 0.0) {
            height = searchBar.getLayoutBounds().getHeight();
        }
        if (height <= 0.0 && searchBar.getPrefHeight() > 0.0) {
            height = searchBar.getPrefHeight();
        }
        if (height <= 0.0) {
            height = searchBar.prefHeight(-1.0);
        }
        return height;
    }

    /// Returns the best available measured or preferred height for one result row.
    private double estimatedResultRowHeight() {
        ObservableList<Node> results = getResults();
        for (int index = 0; index < results.size(); index++) {
            if (focusTargetAt(index) == null) {
                continue;
            }

            Node result = results.get(index);
            double height = result instanceof Region region ? region.getHeight() : 0.0;
            if (height <= 0.0) {
                height = result.getLayoutBounds().getHeight();
            }
            if (height <= 0.0 && result instanceof Region region) {
                height = region.prefHeight(-1.0);
            }
            if (height > 0.0) {
                return height;
            }
        }
        return DEFAULT_RESULT_PAGE_ROW_HEIGHT;
    }

    /// Returns the focus target for the indexed result when it can be reached.
    private @Nullable Node focusTargetAt(int index) {
        if (!M3Accessible.canReach(this) || index < 0 || index >= getResults().size()) {
            return null;
        }
        return M3Accessible.focusTarget(getResults().get(index));
    }

    /// Returns the current accessibility focus node.
    ///
    /// @return the focused result or search bar item when focus is inside this view, otherwise the search editor
    private Node accessibleFocusNode() {
        @Nullable Node focusNode = currentFocusNode();
        return focusNode == null ? getEditor() : focusNode;
    }

    /// Focuses the current accessibility focus node, or the embedded editor when focus is outside this search view.
    ///
    /// @return `true` when the target accepted focus
    final boolean focusAccessibleNode() {
        if (!M3Accessible.canReach(this)) {
            return false;
        }
        @Nullable Node focusNode = currentFocusNode();
        activate();
        if (M3Accessible.showItem(this, focusNode == null ? getEditor() : focusNode)) {
            notifyFocusNodeChanged();
            return true;
        }
        return false;
    }

    /// Returns the current focused child target, or `null` when focus is outside this search view.
    private @Nullable Node currentFocusNode() {
        if (!M3Accessible.canReach(this)) {
            return null;
        }
        @Nullable Node resultFocusNode = M3Accessible.currentFocusTarget(this, getResults());
        if (resultFocusNode != null) {
            return resultFocusNode;
        }
        return currentSearchBarFocusNode();
    }

    /// Returns the current target that a parameterless `SHOW_ITEM` should preserve.
    private @Nullable Node currentDefaultShowItemFocusNode() {
        if (!M3Accessible.canReach(this)) {
            return null;
        }
        @Nullable Node resultFocusNode = M3Accessible.currentFocusTarget(this, getResults());
        if (resultFocusNode != null) {
            return resultFocusNode;
        }

        @Nullable Node searchBarFocusNode = currentSearchBarFocusNode();
        if (searchBarFocusNode == null
                || searchBarFocusNode == getEditor()
                || searchBarFocusNode == searchBar) {
            return null;
        }
        return searchBarFocusNode;
    }

    /// Returns the embedded search bar's current focus target when focus is inside it or one of its popups.
    private @Nullable Node currentSearchBarFocusNode() {
        @Nullable Object focusNode = searchBar.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);
        if (focusNode instanceof Node node && M3Accessible.canReach(node) && containsSceneFocus(node)) {
            return node;
        }

        @Nullable Scene scene = getScene();
        @Nullable Node focusOwner = scene == null ? null : scene.getFocusOwner();
        return focusOwner != null && M3Accessible.containsNode(searchBar, focusOwner) ? searchBar : null;
    }

    /// Returns whether the supplied focus target contains the focus owner of its scene.
    private static boolean containsSceneFocus(Node focusTarget) {
        @Nullable Scene scene = focusTarget.getScene();
        @Nullable Node focusOwner = scene == null ? null : scene.getFocusOwner();
        return focusOwner != null && M3Accessible.containsNode(focusTarget, focusOwner);
    }

    /// Returns the index of the result containing current keyboard focus.
    private int focusedResultIndex() {
        if (!M3Accessible.canReach(this)) {
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

    /// Returns whether focus currently belongs to a result that will be hidden after collapse.
    private boolean isFocusInsideResults() {
        return focusedResultIndex() >= 0;
    }

    /// Moves focus back to the search bar when result content is being collapsed.
    private void restoreSearchBarFocus() {
        focusSearchBar();
    }

    /// Moves focus to the embedded search editor and reveals it through this search view.
    ///
    /// @return `true` when the editor accepted focus
    private boolean focusEditor() {
        if (M3Accessible.canReach(getEditor()) && M3Accessible.showItem(this, getEditor())) {
            notifyFocusNodeChanged();
            return true;
        }
        return false;
    }

    /// Moves focus to the search bar container and reveals it through this search view.
    private void focusSearchBar() {
        if (M3Accessible.showDirectItem(this, searchBar)) {
            notifyFocusNodeChanged();
        }
    }

    /// Notifies and refreshes cached accessibility focus state.
    private void notifyFocusNodeChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
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

    /// Applies changed runtime motion settings to the active results visibility animation.
    private void refreshMotionSettings() {
        M3Animation.finishRunningAnimationsIfDisabled(this, resultsVisibilityAnimation);
    }

    /// Validates a result node array.
    private static void validateResults(Node... results) {
        Objects.requireNonNull(results, "results");
        for (Node result : results) {
            Objects.requireNonNull(result, "result");
        }
    }
}
