// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// A Material Design 3 side sheet container.
@NotNullByDefault
public class M3SideSheet extends BorderPane {
    /// The base style class for M3FX side sheets.
    public static final String STYLE_CLASS = "m3-side-sheet";

    /// The shared sheet header style class.
    public static final String HEADER_STYLE_CLASS = "m3-sheet-header";

    /// The shared sheet title style class.
    public static final String TITLE_STYLE_CLASS = "m3-sheet-title";

    /// The shared sheet action container style class.
    public static final String ACTIONS_STYLE_CLASS = "m3-sheet-actions";

    /// The shared sheet content slot style class.
    public static final String CONTENT_STYLE_CLASS = "m3-sheet-content";

    /// The sheet headline text property.
    private final StringProperty headline = new SimpleStringProperty(this, "headline", "");

    /// The sheet content node property.
    private final ObjectProperty<@Nullable Node> content = new SimpleObjectProperty<>(this, "content");

    /// The sheet variant property.
    private final ObjectProperty<M3SheetVariant> variant =
            new SimpleObjectProperty<>(this, "variant", M3SheetVariant.STANDARD) {
                /// Updates variant style classes when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3SheetVariant.STANDARD);
                        return;
                    }
                    updateVariantStyle();
                }
            };

    /// The header row.
    private final HBox header = new HBox();

    /// The headline label.
    private final Label headlineLabel = new Label();

    /// The flexible header spacer.
    private final Region spacer = new Region();

    /// The trailing action node container.
    private final HBox actions = new HBox();

    /// The content slot.
    private final StackPane contentSlot = new StackPane();

    /// Creates an empty side sheet.
    public M3SideSheet() {
        this("", null);
    }

    /// Creates a side sheet with headline text.
    public M3SideSheet(String headline) {
        this(headline, null);
    }

    /// Creates a side sheet with headline text and content.
    public M3SideSheet(String headline, @Nullable Node content) {
        initialize();
        setHeadline(headline);
        setContent(content);
    }

    /// Creates a side sheet with headline text, content, and trailing actions.
    public M3SideSheet(String headline, @Nullable Node content, Node... actions) {
        this(headline, content);
        Objects.requireNonNull(actions, "actions");
        for (Node action : actions) {
            Objects.requireNonNull(action, "action");
        }
        getActions().addAll(actions);
    }

    /// Returns the sheet headline.
    public final String getHeadline() {
        return headline.get();
    }

    /// Sets the sheet headline.
    public final void setHeadline(String headline) {
        this.headline.set(Objects.requireNonNull(headline, "headline"));
    }

    /// Returns the sheet headline property.
    public final StringProperty headlineProperty() {
        return headline;
    }

    /// Returns the sheet content node.
    public final @Nullable Node getContent() {
        return content.get();
    }

    /// Sets the sheet content node.
    public final void setContent(@Nullable Node content) {
        this.content.set(content);
    }

    /// Returns the sheet content node property.
    public final ObjectProperty<@Nullable Node> contentProperty() {
        return content;
    }

    /// Returns the sheet variant.
    public final M3SheetVariant getVariant() {
        return variant.get();
    }

    /// Sets the sheet variant.
    public final void setVariant(M3SheetVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the sheet variant property.
    public final ObjectProperty<M3SheetVariant> variantProperty() {
        return variant;
    }

    /// Returns the mutable trailing action node list.
    public final ObservableList<Node> getActions() {
        return actions.getChildren();
    }

    /// Returns the user-agent stylesheet for M3FX sheets.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("sheet.css");
    }

    /// Initializes child nodes, style classes, and property listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        header.getStyleClass().add(HEADER_STYLE_CLASS);
        headlineLabel.getStyleClass().add(TITLE_STYLE_CLASS);
        actions.getStyleClass().add(ACTIONS_STYLE_CLASS);
        contentSlot.getStyleClass().add(CONTENT_STYLE_CLASS);

        header.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headlineLabel.textProperty().bind(headline);
        content.addListener((observable, oldValue, newValue) -> updateContent(newValue));
        header.getChildren().addAll(headlineLabel, spacer, actions);
        setTop(header);
        setCenter(contentSlot);
        updateVariantStyle();
        updateContent(getContent());
    }

    /// Updates the sheet content slot.
    private void updateContent(@Nullable Node node) {
        contentSlot.getChildren().clear();
        if (node != null) {
            contentSlot.getChildren().add(node);
        }
    }

    /// Updates the active variant style class.
    private void updateVariantStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getVariant().getStyleClass(),
                M3SheetVariant.STANDARD.getStyleClass(),
                M3SheetVariant.MODAL.getStyleClass()
        );
    }
}
