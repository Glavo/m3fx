// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.geometry.Insets;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import org.jetbrains.annotations.NotNullByDefault;

/// A retained checkerboard region used behind transparent color previews.
@NotNullByDefault
final class M3ColorCheckerboard extends Region {
    /// The reusable eight-pixel checker pattern.
    private static final ImagePattern PATTERN = createPattern();

    /// Creates a checkerboard region.
    M3ColorCheckerboard() {
        getStyleClass().add("color-checkerboard");
        setBackground(new Background(new BackgroundFill(PATTERN, CornerRadii.EMPTY, Insets.EMPTY)));
        setMouseTransparent(true);
    }

    /// Creates the shared checkerboard image pattern.
    private static ImagePattern createPattern() {
        int size = 8;
        int half = size / 2;
        WritableImage image = new WritableImage(size, size);
        PixelWriter writer = image.getPixelWriter();
        Color light = Color.rgb(255, 255, 255);
        Color dark = Color.rgb(218, 218, 218);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                boolean alternate = (x < half) == (y < half);
                writer.setColor(x, y, alternate ? light : dark);
            }
        }
        return new ImagePattern(image, 0.0, 0.0, size, size, false);
    }
}
