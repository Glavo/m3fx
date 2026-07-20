// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;

import org.glavo.m3fx.controls.M3AssistChip;
import org.glavo.m3fx.controls.M3Chip;
import org.glavo.m3fx.controls.M3ChipGroup;
import org.glavo.m3fx.controls.M3ChipStyle;
import org.glavo.m3fx.controls.M3FilterChip;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3InputChip;
import org.glavo.m3fx.controls.M3SuggestionChip;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the Chips component showcase page.
@NotNullByDefault
final class ChipsDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    ChipsDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the chip component page.
    Node createContent() {
        M3AssistChip assistDirections = new M3AssistChip("Directions");
        assistDirections.setGraphic(createNavigationIcon("navigation"));
        M3AssistChip assistCalendar = new M3AssistChip("Add to calendar");
        assistCalendar.setGraphic(createNavigationIcon("calendar"));
        assistCalendar.setChipStyle(M3ChipStyle.ELEVATED);
        M3AssistChip assistDisabled = new M3AssistChip("Disabled");
        assistDisabled.setDisable(true);

        M3FilterChip filterAll = new M3FilterChip("All");
        filterAll.setSelected(true);
        filterAll.setGraphic(createNavigationIcon("check"));
        M3FilterChip filterNearby = new M3FilterChip("Nearby");
        M3FilterChip filterOpen = new M3FilterChip("Open now");
        filterOpen.setSelected(true);
        filterOpen.setGraphic(createNavigationIcon("check"));
        M3ChipGroup filters = createChipGroup(filterAll, filterNearby, filterOpen);
        filters.setPrefWrapLength(420.0);

        M3InputChip inputPerson = new M3InputChip("Alex Morgan");
        inputPerson.setGraphic(createNavigationIcon("person"));
        M3IconButton removePerson = new M3IconButton(
                createIconViewport(DemoIcons.onSurfaceVariant("close"), 18.0)
        );
        removePerson.setAccessibleText("Remove Alex Morgan");
        removePerson.setContainerWidth(24.0);
        removePerson.setContainerHeight(24.0);
        removePerson.setOnAction(event -> inputPerson.setVisible(false));
        inputPerson.setTrailingGraphic(removePerson);

        M3InputChip inputTeam = new M3InputChip("Design team");
        inputTeam.setSelected(true);
        inputTeam.setGraphic(createNavigationIcon("group"));
        M3IconButton removeTeam = new M3IconButton(
                createIconViewport(DemoIcons.onSurfaceVariant("close"), 18.0)
        );
        removeTeam.setAccessibleText("Remove Design team");
        removeTeam.setContainerWidth(24.0);
        removeTeam.setContainerHeight(24.0);
        removeTeam.setOnAction(event -> inputTeam.setSelected(false));
        inputTeam.setTrailingGraphic(removeTeam);

        M3InputChip inputDisabled = new M3InputChip("Unavailable");
        inputDisabled.setDisable(true);
        M3IconButton removeDisabled = new M3IconButton(
                createIconViewport(DemoIcons.onSurfaceVariant("close"), 18.0)
        );
        removeDisabled.setAccessibleText("Remove unavailable input");
        removeDisabled.setContainerWidth(24.0);
        removeDisabled.setContainerHeight(24.0);
        inputDisabled.setTrailingGraphic(removeDisabled);

        M3SuggestionChip suggestionReply = new M3SuggestionChip("Reply");
        M3SuggestionChip suggestionRemind = new M3SuggestionChip("Remind me");
        suggestionRemind.setGraphic(createNavigationIcon("schedule"));
        M3SuggestionChip suggestionElevated = new M3SuggestionChip("View details");
        suggestionElevated.setChipStyle(M3ChipStyle.ELEVATED);

        return createGallery(
                createShowcaseGroup("Assist Chips", assistDirections, assistCalendar, assistDisabled),
                createShowcaseGroup("Filter Chips", filters),
                createShowcaseGroup("Input Chips", inputPerson, inputTeam, inputDisabled),
                createShowcaseGroup("Suggestion Chips", suggestionReply, suggestionRemind, suggestionElevated)
        );
    }

    /// Creates a chip group sample with initial chips.
    private static M3ChipGroup createChipGroup(M3Chip... chips) {
        M3ChipGroup group = new M3ChipGroup();
        group.getItems().addAll(chips);
        return group;
    }
}
