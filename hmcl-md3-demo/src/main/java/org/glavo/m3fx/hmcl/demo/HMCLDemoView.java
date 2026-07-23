// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Card;
import org.glavo.m3fx.controls.M3CardVariant;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Provides shared localization, scrolling, spacing, and action dispatch for HMCL demo pages.
@NotNullByDefault
abstract class HMCLDemoView extends BorderPane {
    /// The localization source used by the page.
    protected final HMCLDemoStrings strings;

    /// The shared application state retained by the page.
    protected final HMCLDemoState state;

    /// The application-level command sink used by the page.
    protected final HMCLDemoActions actions;

    /// The scroll container that hosts the rebuilt localized page content.
    private final ScrollPane scrollPane = new ScrollPane();

    /// Creates a page base with its shared services.
    ///
    /// @param strings the localization source
    /// @param state   the shared demo state
    /// @param actions the command sink
    protected HMCLDemoView(HMCLDemoStrings strings, HMCLDemoState state, HMCLDemoActions actions) {
        this.strings = Objects.requireNonNull(strings, "strings");
        this.state = Objects.requireNonNull(state, "state");
        this.actions = Objects.requireNonNull(actions, "actions");

        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setCenter(scrollPane);
    }

    /// Completes page construction and installs automatic localization refresh.
    ///
    /// Subclasses must invoke this method after initializing their own fields.
    protected final void initializeView() {
        refreshView();
        strings.localeProperty().addListener((observable, oldLocale, newLocale) -> refreshView());
    }

    /// Creates the current localized content tree.
    ///
    /// @return the root node displayed inside the page scroller
    protected abstract Node createContent();

    /// Replaces the current page tree with content for the active locale.
    protected final void refreshView() {
        scrollPane.setContent(createContent());
        scrollPane.setVvalue(0.0);
    }

    /// Resolves one localized string.
    ///
    /// @param key  the resource key
    /// @param args optional formatting arguments
    /// @return the localized string
    protected final String text(String key, Object... args) {
        return args.length == 0 ? strings.get(key) : strings.format(key, args);
    }

    /// Creates a standard vertically spaced page body.
    ///
    /// @param children the page children in display order
    /// @return the configured page body
    protected final VBox page(Node... children) {
        VBox page = new VBox(24.0, children);
        page.setPadding(new Insets(28.0, 32.0, 40.0, 32.0));
        page.setFillWidth(true);
        page.setMinWidth(0.0);
        page.setMaxWidth(Double.MAX_VALUE);
        return page;
    }

    /// Creates a page heading with title and supporting text.
    ///
    /// @param title      the localized title
    /// @param supporting the localized supporting text
    /// @return the heading node
    protected final VBox heading(String title, String supporting) {
        M3Text titleText = new M3Text(title, M3TextRole.HEADLINE_LARGE);
        titleText.setWrapText(true);
        M3Text supportingText = new M3Text(supporting, M3TextRole.BODY_LARGE);
        supportingText.setWrapText(true);
        VBox heading = new VBox(6.0, titleText, supportingText);
        heading.setMinWidth(0.0);
        heading.setMaxWidth(Double.MAX_VALUE);
        return heading;
    }

    /// Creates a localized section heading.
    ///
    /// @param title the localized section title
    /// @return the section heading
    protected final M3Text sectionTitle(String title) {
        M3Text text = new M3Text(title, M3TextRole.TITLE_LARGE);
        text.setWrapText(true);
        return text;
    }

    /// Creates a standard vertical card.
    ///
    /// @param variant  the Material card variant
    /// @param children the card children
    /// @return the configured card
    protected final M3Card card(M3CardVariant variant, Node... children) {
        VBox content = new VBox(14.0, children);
        content.setFillWidth(true);
        content.setMinWidth(0.0);
        content.setMaxWidth(Double.MAX_VALUE);
        M3Card card = new M3Card(content, variant);
        card.setMinWidth(0.0);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    /// Creates a wrapping row for cards, chips, or actions.
    ///
    /// @param children the row children
    /// @return the responsive flow container
    protected final FlowPane flow(Node... children) {
        FlowPane flow = new FlowPane(12.0, 12.0);
        flow.setAlignment(Pos.CENTER_LEFT);
        flow.setMinWidth(0.0);
        flow.setMaxWidth(Double.MAX_VALUE);
        flow.getChildren().addAll(children);
        return flow;
    }

    /// Creates a Material button that dispatches one command token.
    ///
    /// @param label   the localized button label
    /// @param variant the Material button variant
    /// @param command the command token
    /// @return the configured button
    protected final M3Button commandButton(String label, M3ButtonVariant variant, String command) {
        M3Button button = new M3Button(label, variant);
        button.setOnAction(event -> actions.dispatch(command));
        return button;
    }

    /// Lets one child consume the available vertical space in a `VBox`.
    ///
    /// @param child the child whose vertical grow priority is set
    protected final void grow(Node child) {
        VBox.setVgrow(child, Priority.ALWAYS);
    }
}
