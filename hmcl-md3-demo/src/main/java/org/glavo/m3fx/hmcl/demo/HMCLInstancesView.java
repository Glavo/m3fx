// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3AssistChip;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Card;
import org.glavo.m3fx.controls.M3CardVariant;
import org.glavo.m3fx.controls.M3MenuItem;
import org.glavo.m3fx.controls.M3SplitButton;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextField;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Locale;

/// Displays, searches, selects, and manages deterministic game instances.
@NotNullByDefault
public final class HMCLInstancesView extends HMCLDemoView {
    /// Creates the instance-management page.
    ///
    /// @param strings the localization source
    /// @param state   the shared demo state
    /// @param actions the application command sink
    public HMCLInstancesView(HMCLDemoStrings strings, HMCLDemoState state, HMCLDemoActions actions) {
        super(strings, state, actions);
        state.getInstances().addListener((javafx.collections.ListChangeListener<HMCLDemoInstance>) change ->
                refreshView());
        state.selectedInstanceProperty().addListener((observable, oldInstance, newInstance) -> refreshView());
        initializeView();
    }

    /// Creates the localized instances content.
    ///
    /// @return the instances page tree
    @Override
    protected Node createContent() {
        M3TextField search = new M3TextField();
        search.setPromptText(text("instances.search"));
        search.setPrefWidth(360.0);
        search.setMaxWidth(520.0);

        M3Button add = new M3Button(text("instances.add"), M3ButtonVariant.FILLED);
        add.setGraphic(HMCLDemoIcons.create(HMCLDemoIcons.ADD));
        add.setOnAction(event -> actions.dispatch(HMCLDemoActions.ACTION_ADD_INSTANCE));

        HBox toolbar = new HBox(12.0, search, add);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        FlowPane cards = flow();
        populateCards(cards, "");
        search.textProperty().addListener((observable, oldText, newText) -> populateCards(cards, newText));

        return page(
                heading(text("instances.title"), text("instances.subtitle")),
                toolbar,
                cards
        );
    }

    /// Replaces the instance-card collection with entries matching a query.
    ///
    /// @param target the flow pane that owns the cards
    /// @param query  the case-insensitive query
    private void populateCards(FlowPane target, String query) {
        String normalized = query.strip().toLowerCase(Locale.ROOT);
        target.getChildren().clear();
        for (HMCLDemoInstance instance : state.getInstances()) {
            if (matches(instance, normalized)) {
                target.getChildren().add(createInstanceCard(instance));
            }
        }
        if (target.getChildren().isEmpty()) {
            M3Text empty = new M3Text(text("instances.empty"), M3TextRole.BODY_LARGE);
            empty.setWrapText(true);
            target.getChildren().add(empty);
        }
    }

    /// Returns whether one instance matches a normalized query.
    ///
    /// @param instance the instance to inspect
    /// @param query    the normalized query
    /// @return `true` when the instance should be displayed
    private static boolean matches(HMCLDemoInstance instance, String query) {
        return query.isEmpty()
                || instance.name().toLowerCase(Locale.ROOT).contains(query)
                || instance.gameVersion().toLowerCase(Locale.ROOT).contains(query)
                || instance.loader().toLowerCase(Locale.ROOT).contains(query);
    }

    /// Creates one instance summary card.
    ///
    /// @param instance the represented instance
    /// @return the instance card
    private M3Card createInstanceCard(HMCLDemoInstance instance) {
        boolean selected = instance.equals(state.getSelectedInstance());

        M3Text name = new M3Text(instance.name(), M3TextRole.TITLE_LARGE);
        name.setWrapText(true);
        M3Text details = new M3Text(
                text("instances.card.details", instance.gameVersion(), instance.loader()),
                M3TextRole.BODY_MEDIUM
        );
        details.setWrapText(true);
        M3Text description = new M3Text(
                HMCLDemoModelText.instanceDescription(strings, instance),
                M3TextRole.BODY_MEDIUM
        );
        description.setWrapText(true);

        M3AssistChip status = new M3AssistChip(text(
                "instance.status." + instance.status().name().toLowerCase(Locale.ROOT)
        ));
        status.setDisable(true);

        M3Button select = new M3Button(
                selected ? text("instances.selected") : text("instances.select"),
                selected ? M3ButtonVariant.TONAL : M3ButtonVariant.OUTLINED
        );
        select.setDisable(selected);
        select.setOnAction(event -> {
            state.selectInstance(instance.id());
            actions.dispatch(HMCLDemoActions.ACTION_SELECT_INSTANCE, instance.id());
        });

        M3SplitButton manage = new M3SplitButton(text("instances.manage"));
        manage.setVariant(M3ButtonVariant.TONAL);
        manage.setOnAction(event -> {
            state.selectInstance(instance.id());
            actions.navigate(HMCLDemoActions.ROUTE_INSTANCE_DETAIL);
        });
        M3MenuItem copy = new M3MenuItem(text("instances.copy"));
        copy.setOnAction(event -> {
            state.selectInstance(instance.id());
            state.copySelectedInstance();
            actions.dispatch("copy-instance", instance.id());
        });
        M3MenuItem delete = new M3MenuItem(text("instances.delete"));
        delete.setOnAction(event -> {
            state.selectInstance(instance.id());
            state.deleteSelectedInstance();
            actions.dispatch("delete-instance", instance.id());
        });
        manage.getItems().addAll(copy, delete);

        HBox actionsRow = new HBox(10.0, select, manage);
        actionsRow.setAlignment(Pos.CENTER_LEFT);

        M3Card card = card(
                selected ? M3CardVariant.ELEVATED : M3CardVariant.OUTLINED,
                status,
                name,
                details,
                description,
                actionsRow
        );
        card.setPrefWidth(340.0);
        return card;
    }
}
