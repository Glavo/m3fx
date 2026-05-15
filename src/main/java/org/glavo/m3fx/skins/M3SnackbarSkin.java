package org.glavo.m3fx.skins;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Snackbar;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for {@link M3Snackbar}.
@NotNullByDefault
public class M3SnackbarSkin extends SkinBase<M3Snackbar> {
    /// The snackbar layout container.
    private final HBox container = new HBox();

    /// The snackbar message label.
    private final Label textLabel = new Label();

    /// The snackbar action button.
    private final org.glavo.m3fx.controls.M3Button actionButton = new org.glavo.m3fx.controls.M3Button();

    /// Creates a snackbar skin.
    public M3SnackbarSkin(M3Snackbar control) {
        super(control);

        container.getStyleClass().add("m3-snackbar-container");
        textLabel.getStyleClass().add("m3-snackbar-text");
        actionButton.getStyleClass().add("m3-snackbar-action");
        actionButton.setVariant(M3ButtonVariant.TEXT);

        textLabel.textProperty().bind(control.textProperty());
        actionButton.textProperty().bind(control.actionTextProperty());
        actionButton.setOnAction(this::fireAction);
        control.actionTextProperty().addListener((observable, oldValue, newValue) -> updateActionVisibility(newValue));
        control.containerShapeProperty().addListener((observable, oldValue, newValue) -> updateTokenStyles());
        control.contentPaddingProperty().addListener((observable, oldValue, newValue) -> updateTokenStyles());

        container.getChildren().addAll(textLabel, actionButton);
        getChildren().add(container);
        updateActionVisibility(control.getActionText());
        updateTokenStyles();
    }

    /// Fires the snackbar action handler if one is present.
    private void fireAction(ActionEvent event) {
        @Nullable EventHandler<ActionEvent> handler = getSkinnable().getOnAction();
        if (handler != null) {
            handler.handle(event);
        }
    }

    /// Updates the action button visibility from its text.
    private void updateActionVisibility(@Nullable String actionText) {
        boolean visible = actionText != null && !actionText.isBlank();
        actionButton.setVisible(visible);
        actionButton.setManaged(visible);
    }

    /// Applies styleable component tokens to the snackbar container.
    private void updateTokenStyles() {
        M3Snackbar snackbar = getSkinnable();
        double padding = snackbar.getContentPadding();
        container.setPadding(new Insets(padding / 2.0, padding / 2.0, padding / 2.0, padding));
        String shape = formatPixels(snackbar.getContainerShape());
        container.setStyle("-fx-background-radius: " + shape + ";");
    }

    /// Formats a CSS pixel value.
    private static String formatPixels(double value) {
        if (Math.rint(value) == value) {
            return Long.toString((long) value) + "px";
        }
        return Double.toString(value) + "px";
    }
}
