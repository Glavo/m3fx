package org.glavo.m3fx.skins;

import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.StackPane;
import org.glavo.m3fx.controls.M3Card;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for {@link M3Card}.
@NotNullByDefault
public class M3CardSkin extends SkinBase<M3Card> {
    /// The container that hosts the card content.
    private final StackPane container = new StackPane();

    /// Creates a card skin.
    public M3CardSkin(M3Card control) {
        super(control);
        container.getStyleClass().add("m3-card-container");
        getChildren().add(container);
        updateContent(control.getContent());
        control.contentProperty().addListener((observable, oldValue, newValue) -> updateContent(newValue));
    }

    /// Updates the content hosted by this skin.
    private void updateContent(@Nullable Node content) {
        if (content == null) {
            container.getChildren().clear();
        } else {
            container.getChildren().setAll(content);
        }
    }
}
