// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A Material Design 3 search view with a search bar and result content.
@NotNullByDefault
public class M3SearchView extends VBox {
    /// The base style class for M3FX search views.
    public static final String STYLE_CLASS = "m3-search-view";

    /// The style class applied to the result container.
    public static final String RESULTS_STYLE_CLASS = "m3-search-view-results";

    /// The embedded search bar.
    private final M3SearchBar searchBar = new M3SearchBar();

    /// The search result container.
    private final VBox resultsBox = new VBox();

    /// Creates an empty search view.
    public M3SearchView() {
        initialize();
    }

    /// Creates a search view with prompt text.
    public M3SearchView(String promptText) {
        initialize();
        setPromptText(promptText);
    }

    /// Returns the embedded search bar.
    public final M3SearchBar getSearchBar() {
        return searchBar;
    }

    /// Returns the mutable result node list.
    public final ObservableList<Node> getResults() {
        return resultsBox.getChildren();
    }

    /// Returns the editable search input used by the embedded search bar.
    public final TextField getEditor() {
        return searchBar.getEditor();
    }

    /// Returns the leading content node from the embedded search bar.
    public final @Nullable Node getLeading() {
        return searchBar.getLeading();
    }

    /// Sets the leading content node on the embedded search bar.
    public final void setLeading(@Nullable Node leading) {
        searchBar.setLeading(leading);
    }

    /// Returns the embedded search bar leading content node property.
    public final ObjectProperty<@Nullable Node> leadingProperty() {
        return searchBar.leadingProperty();
    }

    /// Returns the mutable trailing action list from the embedded search bar.
    public final ObservableList<Node> getTrailingActions() {
        return searchBar.getTrailingActions();
    }

    /// Returns the text entered in the embedded search bar.
    public final String getText() {
        return searchBar.getText();
    }

    /// Sets the text entered in the embedded search bar.
    public final void setText(String text) {
        searchBar.setText(Objects.requireNonNull(text, "text"));
    }

    /// Returns the embedded search bar text property.
    public final StringProperty textProperty() {
        return searchBar.textProperty();
    }

    /// Returns the prompt text displayed by the embedded search bar.
    public final String getPromptText() {
        return searchBar.getPromptText();
    }

    /// Sets the prompt text displayed by the embedded search bar.
    public final void setPromptText(String promptText) {
        searchBar.setPromptText(Objects.requireNonNull(promptText, "promptText"));
    }

    /// Returns the embedded search bar prompt text property.
    public final StringProperty promptTextProperty() {
        return searchBar.promptTextProperty();
    }

    /// Returns the search submission handler.
    public final @Nullable EventHandler<ActionEvent> getOnAction() {
        return searchBar.getOnAction();
    }

    /// Sets the search submission handler.
    public final void setOnAction(@Nullable EventHandler<ActionEvent> onAction) {
        searchBar.setOnAction(onAction);
    }

    /// Returns the search submission handler property.
    public final ObjectProperty<@Nullable EventHandler<ActionEvent>> onActionProperty() {
        return searchBar.onActionProperty();
    }

    /// Fires the embedded search bar action event.
    public final void fire() {
        searchBar.fire();
    }

    /// Returns whether this search view is showing active search results.
    public final boolean isActive() {
        return searchBar.isActive();
    }

    /// Sets whether this search view is showing active search results.
    public final void setActive(boolean active) {
        searchBar.setActive(active);
    }

    /// Returns the active search result state property.
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

    /// Returns the user-agent stylesheet for M3FX search views.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("search.css");
    }

    /// Adds base style classes and child nodes.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        resultsBox.getStyleClass().add(RESULTS_STYLE_CLASS);
        getChildren().addAll(searchBar, resultsBox);
        searchBar.activeProperty().addListener((observable, oldValue, newValue) -> updateResultsVisibility());
        setActive(true);
        updateResultsVisibility();
    }

    /// Updates result container visibility from the active state.
    private void updateResultsVisibility() {
        boolean active = isActive();
        resultsBox.setVisible(active);
        resultsBox.setManaged(active);
    }
}
