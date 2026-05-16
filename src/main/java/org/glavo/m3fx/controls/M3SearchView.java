// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;

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
    }
}
