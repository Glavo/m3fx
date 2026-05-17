// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3Motion;
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

    /// The duration used when a side sheet enters.
    private static final Duration SHOW_DURATION = M3Motion.MEDIUM2;

    /// The duration used when a side sheet exits.
    private static final Duration HIDE_DURATION = M3Motion.SHORT4;

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

    /// Whether this sheet is shown.
    private final BooleanProperty shown = new SimpleBooleanProperty(this, "shown", true) {
        /// Updates the sheet visibility when the property changes.
        @Override
        protected void invalidated() {
            updateShownState(get());
        }
    };

    /// The sheet show and hide animation.
    private final Timeline visibilityAnimation = new Timeline();

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

    /// Returns whether this sheet is shown.
    public final boolean isShown() {
        return shown.get();
    }

    /// Sets whether this sheet is shown.
    public final void setShown(boolean shown) {
        this.shown.set(shown);
    }

    /// Returns the shown property.
    public final BooleanProperty shownProperty() {
        return shown;
    }

    /// Returns the mutable trailing action node list.
    public final ObservableList<Node> getActions() {
        return actions.getChildren();
    }

    /// Shows this side sheet using the Material visibility motion.
    public final void show() {
        setShown(true);
    }

    /// Hides this side sheet using the Material visibility motion.
    public final void hide() {
        setShown(false);
    }

    /// Returns the user-agent stylesheet for M3FX sheets.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("sheet.css");
    }

    /// Initializes child nodes, style classes, and property listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
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
        addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        updateVariantStyle();
        updateContent(getContent());
    }

    /// Handles keyboard dismissal for modal sheets.
    private void handleKeyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case ESCAPE -> {
                if (isShown() && getVariant() == M3SheetVariant.MODAL) {
                    hide();
                    event.consume();
                }
            }
            default -> {
            }
        }
    }

    /// Updates the sheet content slot.
    private void updateContent(@Nullable Node node) {
        contentSlot.getChildren().clear();
        if (node != null) {
            contentSlot.getChildren().add(node);
        }
    }

    /// Updates shown state with motion when the sheet is attached to a scene.
    private void updateShownState(boolean shown) {
        visibilityAnimation.stop();
        if (shown) {
            setVisible(true);
            setManaged(true);
            if (getScene() == null) {
                applyShownStateImmediately(true);
                return;
            }

            visibilityAnimation.getKeyFrames().setAll(new KeyFrame(
                    SHOW_DURATION,
                    new KeyValue(opacityProperty(), 1.0, M3Motion.EMPHASIZED_DECELERATE),
                    new KeyValue(translateXProperty(), 0.0, M3Motion.EMPHASIZED_DECELERATE)
            ));
            visibilityAnimation.playFromStart();
        } else {
            if (getScene() == null || !isVisible()) {
                applyShownStateImmediately(false);
                return;
            }

            visibilityAnimation.getKeyFrames().setAll(new KeyFrame(
                    HIDE_DURATION,
                    event -> {
                        if (!isShown()) {
                            applyShownStateImmediately(false);
                        }
                    },
                    new KeyValue(opacityProperty(), 0.0, M3Motion.EMPHASIZED_ACCELERATE),
                    new KeyValue(translateXProperty(), hiddenTranslateX(), M3Motion.EMPHASIZED_ACCELERATE)
            ));
            visibilityAnimation.playFromStart();
        }
    }

    /// Applies the shown state without animation.
    private void applyShownStateImmediately(boolean shown) {
        visibilityAnimation.stop();
        setVisible(shown);
        setManaged(shown);
        setOpacity(shown ? 1.0 : 0.0);
        setTranslateX(shown ? 0.0 : hiddenTranslateX());
    }

    /// Returns the off-screen horizontal translation used when the sheet is hidden.
    private double hiddenTranslateX() {
        double width = getWidth();
        if (width <= 0.0) {
            width = prefWidth(-1.0);
        }
        return Math.max(0.0, width);
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
