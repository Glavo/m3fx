// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// A primary text line with an independently operable accessory at its logical end.
@NotNullByDefault
final class M3TitleLine extends HBox {
    /// Creates a line only for a populated accessory slot.
    private M3TitleLine(Label title, Node accessory) {
        super(4, title, accessory);
        setAlignment(Pos.CENTER_LEFT);
        setMinWidth(0);
        addEventHandler(ActionEvent.ACTION, ActionEvent::consume);
        addEventHandler(KeyEvent.KEY_PRESSED, M3TitleLine::isolateActivationKey);
        addEventHandler(KeyEvent.KEY_RELEASED, M3TitleLine::isolateActivationKey);
        addEventHandler(MouseEvent.MOUSE_PRESSED, this::isolateInteractivePress);
    }

    /// Leaves traversal to the scene but does not let an accessory's activation operate the row.
    private static void isolateActivationKey(KeyEvent event) {
        if (event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.ENTER) event.consume();
    }

    /// A press on a passive tag still belongs to the row.
    private void isolateInteractivePress(MouseEvent event) {
        if (event.getTarget() instanceof Node target) {
            for (@Nullable Node n = target; n != null && n != this; n = n.getParent()) {
                if (n.isFocusTraversable()) {
                    event.consume();
                    return;
                }
            }
        }
    }

    /// Gives the title its natural width before distributing surplus space to a shrinkable
    /// accessory, while preserving enough room to operate the accessory at its minimum width.
    @Override protected void layoutChildren() {
        if (getManagedChildren().size() != 2) {
            super.layoutChildren();
            return;
        }
        Node title = getChildren().get(0);
        Node accessory = getChildren().get(1);
        double space = Math.max(0, getWidth() - getSpacing());
        double titleWidth = Math.min(snapSizeX(title.prefWidth(-1)), Math.max(0, space - accessory.minWidth(-1)));
        layoutInArea(title, 0, 0, titleWidth, getHeight(), -1, HPos.LEFT, VPos.CENTER);
        double accessoryWidth = Math.min(space - titleWidth, snapSizeX(accessory.prefWidth(-1)));
        layoutInArea(accessory, titleWidth + getSpacing(), 0, accessoryWidth, getHeight(), -1, HPos.LEFT, VPos.CENTER);
    }

    /// Retains the original label-only layout when no accessory is installed.
    static void install(VBox column, Label label, @Nullable Node accessory) {
        if (label.getParent() instanceof M3TitleLine line) {
            if (accessory == null) {
                int index = column.getChildren().indexOf(line);
                line.getChildren().clear();
                column.getChildren().set(index, label);
            } else if (line.getChildren().get(1) != accessory) {
                line.getChildren().set(1, accessory);
            }
        } else if (accessory != null) {
            int index = column.getChildren().indexOf(label);
            column.getChildren().remove(index);
            column.getChildren().add(index, new M3TitleLine(label, accessory));
        }
    }
}
