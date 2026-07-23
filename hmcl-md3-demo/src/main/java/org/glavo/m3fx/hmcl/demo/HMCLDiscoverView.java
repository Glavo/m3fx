// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3AssistChip;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Card;
import org.glavo.m3fx.controls.M3CardVariant;
import org.glavo.m3fx.controls.M3FilterChip;
import org.glavo.m3fx.controls.M3ProgressBar;
import org.glavo.m3fx.controls.M3Text;
import org.glavo.m3fx.controls.M3TextField;
import org.glavo.m3fx.controls.M3TextRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/// Displays a searchable, filterable, deterministic catalog with simulated installation progress.
@NotNullByDefault
public final class HMCLDiscoverView extends HMCLDemoView {
    /// Creates the Discover page.
    ///
    /// @param strings the localization source
    /// @param state   the shared demo state
    /// @param actions the application command sink
    public HMCLDiscoverView(HMCLDemoStrings strings, HMCLDemoState state, HMCLDemoActions actions) {
        super(strings, state, actions);
        initializeView();
    }

    /// Creates the localized Discover content.
    ///
    /// @return the Discover page tree
    @Override
    protected Node createContent() {
        M3TextField search = new M3TextField(state.getSearchQuery());
        search.setPromptText(text("discover.search"));
        search.setPrefWidth(380.0);
        search.setMaxWidth(560.0);

        M3FilterChip all = new M3FilterChip(text("discover.filter.all"));
        M3FilterChip mods = new M3FilterChip(text("discover.filter.mods"));
        M3FilterChip modpacks = new M3FilterChip(text("discover.filter.modpacks"));
        M3FilterChip resources = new M3FilterChip(text("discover.filter.resources"));
        all.setSelected(true);

        FlowPane results = flow();
        Runnable populateAll = () -> populateResults(results, null);
        all.setOnAction(event -> {
            selectOnly(all, mods, modpacks, resources);
            populateResults(results, null);
        });
        mods.setOnAction(event -> {
            selectOnly(mods, all, modpacks, resources);
            populateResults(results, HMCLDemoContent.ContentKind.MOD);
        });
        modpacks.setOnAction(event -> {
            selectOnly(modpacks, all, mods, resources);
            populateResults(results, HMCLDemoContent.ContentKind.MODPACK);
        });
        resources.setOnAction(event -> {
            selectOnly(resources, all, mods, modpacks);
            populateResults(results, HMCLDemoContent.ContentKind.RESOURCE_PACK);
        });
        search.textProperty().addListener((observable, oldText, newText) -> {
            state.setSearchQuery(newText);
            if (mods.isSelected()) {
                populateResults(results, HMCLDemoContent.ContentKind.MOD);
            } else if (modpacks.isSelected()) {
                populateResults(results, HMCLDemoContent.ContentKind.MODPACK);
            } else if (resources.isSelected()) {
                populateResults(results, HMCLDemoContent.ContentKind.RESOURCE_PACK);
            } else {
                populateAll.run();
            }
        });
        populateAll.run();

        return page(
                heading(text("discover.title"), text("discover.subtitle")),
                search,
                flow(all, mods, modpacks, resources),
                results
        );
    }

    /// Selects one filter chip and clears the other supplied chips.
    ///
    /// @param selected the chip to select
    /// @param others   the chips to clear
    private static void selectOnly(M3FilterChip selected, M3FilterChip... others) {
        selected.setSelected(true);
        for (M3FilterChip chip : others) {
            chip.setSelected(false);
        }
    }

    /// Populates the current filtered result set.
    ///
    /// @param target the result flow pane
    /// @param kind   the optional content-kind filter
    private void populateResults(FlowPane target, @Nullable HMCLDemoContent.ContentKind kind) {
        target.getChildren().clear();
        for (HMCLDemoContent content : state.getFilteredContents()) {
            if (kind == null || content.kind() == kind) {
                target.getChildren().add(createContentCard(content));
            }
        }
        if (target.getChildren().isEmpty()) {
            M3Text empty = new M3Text(text("discover.empty"), M3TextRole.BODY_LARGE);
            empty.setWrapText(true);
            target.getChildren().add(empty);
        }
    }

    /// Creates one catalog result card.
    ///
    /// @param content the represented catalog entry
    /// @return the content card
    private M3Card createContentCard(HMCLDemoContent content) {
        M3AssistChip kind = new M3AssistChip(text(
                "discover.kind." + content.kind().name().toLowerCase(Locale.ROOT)
        ));
        kind.setDisable(true);
        M3Text title = new M3Text(content.title(), M3TextRole.TITLE_LARGE);
        title.setWrapText(true);
        M3Text author = new M3Text(text("discover.by_author", content.author()), M3TextRole.LABEL_LARGE);
        M3Text summary = new M3Text(
                HMCLDemoModelText.contentSummary(strings, content),
                M3TextRole.BODY_MEDIUM
        );
        summary.setWrapText(true);
        M3Text metrics = new M3Text(
                text("discover.metrics", compactDownloads(content.downloadCount()), content.gameVersions().get(0)),
                M3TextRole.BODY_SMALL
        );

        M3Button details = new M3Button(text("discover.details"), M3ButtonVariant.TEXT);
        details.setOnAction(event -> {
            state.selectContent(content.id());
            actions.dispatch("content-detail", content.id());
        });

        HMCLDemoState.InstallState installState = state.installStateFor(content);
        M3Button install = new M3Button(installLabel(installState), M3ButtonVariant.TONAL);
        install.setDisable(installState == HMCLDemoState.InstallState.INSTALLED);
        install.setOnAction(event -> advanceInstallation(content));

        VBox body = new VBox(12.0, kind, title, author, summary, metrics);
        if (content.equals(state.getInstallingContent())) {
            M3ProgressBar progress = new M3ProgressBar(state.getInstallProgress());
            body.getChildren().addAll(
                    new M3Text(
                            text("discover.install.progress", Math.round(state.getInstallProgress() * 100.0)),
                            M3TextRole.LABEL_MEDIUM
                    ),
                    progress
            );
        }
        HBox actionRow = new HBox(8.0, details, install);
        actionRow.setAlignment(Pos.CENTER_LEFT);
        body.getChildren().add(actionRow);

        M3Card card = new M3Card(body, content.featured() ? M3CardVariant.ELEVATED : M3CardVariant.OUTLINED);
        card.setPrefWidth(330.0);
        card.setMinWidth(280.0);
        return card;
    }

    /// Advances the simulated installation lifecycle for one item.
    ///
    /// @param content the target catalog item
    private void advanceInstallation(HMCLDemoContent content) {
        HMCLDemoState.InstallState installState = state.installStateFor(content);
        switch (installState) {
            case AVAILABLE, FAILED -> {
                state.startInstallation(content);
                state.setInstallProgress(0.36);
            }
            case INSTALLING -> state.setInstallProgress(Math.min(1.0, state.getInstallProgress() + 0.34));
            case INSTALLED -> {
                return;
            }
        }
        actions.dispatch(HMCLDemoActions.ACTION_INSTALL, content.id());
        refreshView();
    }

    /// Returns the localized action label for an installation state.
    ///
    /// @param installState the installation state
    /// @return the localized action label
    private String installLabel(HMCLDemoState.InstallState installState) {
        return text("discover.install." + installState.name().toLowerCase(Locale.ROOT));
    }

    /// Formats a deterministic download count into a compact neutral value.
    ///
    /// @param downloads the non-negative download count
    /// @return the compact count
    private static String compactDownloads(long downloads) {
        if (downloads >= 1_000_000L) {
            return String.format(Locale.ROOT, "%.1fM", downloads / 1_000_000.0);
        }
        if (downloads >= 1_000L) {
            return String.format(Locale.ROOT, "%.1fK", downloads / 1_000.0);
        }
        return Long.toString(downloads);
    }
}
