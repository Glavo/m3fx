// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.SkinBase;
import org.glavo.m3fx.controls.M3Snackbar;
import org.glavo.m3fx.internal.M3SnackbarHostImpl;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// The default skin for [M3SnackbarHostImpl].
@NotNullByDefault
public final class M3SnackbarHostSkin extends SkinBase<M3SnackbarHostImpl> {
    /// Updates hosted content when the current snackbar changes.
    private final ChangeListener<@Nullable M3Snackbar> snackbarListener =
            (observable, oldValue, newValue) -> updateSnackbar(newValue);

    /// Creates a snackbar host skin.
    ///
    /// @param control the snackbar host controlled by this skin
    public M3SnackbarHostSkin(M3SnackbarHostImpl control) {
        super(control);
        control.snackbarProperty().addListener(snackbarListener);
        updateSnackbar(control.getSnackbar());
    }

    /// Removes listeners and hosted content before disposal.
    @Override
    public void dispose() {
        M3SnackbarHostImpl control = getSkinnable();
        control.snackbarProperty().removeListener(snackbarListener);
        if (control.getSkin() == null || control.getSkin() == this) {
            getChildren().clear();
        }
        super.dispose();
    }

    /// Lays out the current snackbar at its preferred size instead of stretching it to the overlay bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        @Nullable M3Snackbar currentSnackbar = getSkinnable().getSnackbar();
        if (currentSnackbar == null || !currentSnackbar.isManaged() || !getChildren().contains(currentSnackbar)) {
            return;
        }

        double snackbarWidth = Math.min(width, snapSizeX(currentSnackbar.prefWidth(-1.0)));
        double snackbarHeight = Math.min(height, snapSizeY(currentSnackbar.prefHeight(snackbarWidth)));
        double snackbarX = x + (width - snackbarWidth) / 2.0;
        double snackbarY = y + height - snackbarHeight;

        currentSnackbar.resizeRelocate(
                snapPositionX(snackbarX),
                snapPositionY(snackbarY),
                snackbarWidth,
                snackbarHeight
        );
    }

    /// Mirrors the current snackbar into the skin children.
    private void updateSnackbar(@Nullable M3Snackbar snackbar) {
        if (snackbar == null) {
            getChildren().clear();
        } else {
            getChildren().setAll(snackbar);
        }
        getSkinnable().requestLayout();
    }
}
