// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import org.glavo.m3fx.FxTestUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/// Verifies writable-property binding contracts shared by M3FX controls.
@NotNullByDefault
final class M3PropertyBindingContractTest {
    /// Starts the JavaFX toolkit before controls are created.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
        Platform.setImplicitExit(false);
    }

    /// Verifies that valid bound numeric values are observed without writing back to their binding sources.
    @Test
    void validBoundNumericValuesRemainWritableFromTheirSources() {
        FxTestUtils.runOnFxThread(() -> {
            SimpleDoubleProperty wrapLength = new SimpleDoubleProperty(320.0);
            M3ChipGroup chipGroup = new M3ChipGroup();
            chipGroup.prefWrapLengthProperty().bind(wrapLength);
            wrapLength.set(480.0);
            assertEquals(480.0, chipGroup.getPrefWrapLength());

            SimpleDoubleProperty fixedCellSize = new SimpleDoubleProperty(48.0);
            M3ListView<String> listView = new M3ListView<>();
            listView.fixedCellSizeProperty().bind(fixedCellSize);
            fixedCellSize.set(56.0);
            assertEquals(56.0, listView.getFixedCellSize());

            SimpleDoubleProperty visibleOpacity = new SimpleDoubleProperty(0.24);
            M3Scrim scrim = new M3Scrim();
            scrim.visibleOpacityProperty().bind(visibleOpacity);
            visibleOpacity.set(0.40);
            assertEquals(0.40, scrim.getVisibleOpacity());
            assertEquals(0.40, scrim.getOpacity());
        });
    }

    /// Verifies that button role properties preserve their values before and after observation or binding.
    @Test
    void buttonRolePropertiesPreserveValuesAcrossPropertyCreation() {
        FxTestUtils.runOnFxThread(() -> {
            M3Button button = new M3Button("Action");
            button.setVariant(M3ButtonVariant.OUTLINED);
            button.setSize(M3ButtonSize.LARGE);
            button.setButtonShape(M3ButtonShape.SQUARE);

            assertEquals(M3ButtonVariant.OUTLINED, button.variantProperty().get());
            assertEquals(M3ButtonSize.LARGE, button.sizeProperty().get());
            assertEquals(M3ButtonShape.SQUARE, button.buttonShapeProperty().get());

            ObjectProperty<M3ButtonVariant> variant = new SimpleObjectProperty<>(M3ButtonVariant.TONAL);
            ObjectProperty<M3ButtonSize> size = new SimpleObjectProperty<>(M3ButtonSize.MEDIUM);
            ObjectProperty<M3ButtonShape> shape = new SimpleObjectProperty<>(M3ButtonShape.ROUND);
            button.variantProperty().bind(variant);
            button.sizeProperty().bind(size);
            button.buttonShapeProperty().bind(shape);

            variant.set(M3ButtonVariant.FILLED);
            size.set(M3ButtonSize.EXTRA_LARGE);
            shape.set(M3ButtonShape.SQUARE);
            assertEquals(M3ButtonVariant.FILLED, button.getVariant());
            assertEquals(M3ButtonSize.EXTRA_LARGE, button.getSize());
            assertEquals(M3ButtonShape.SQUARE, button.getButtonShape());

            button.variantProperty().unbind();
            button.sizeProperty().unbind();
            button.buttonShapeProperty().unbind();
            button.variantProperty().set(null);
            button.sizeProperty().set(null);
            button.buttonShapeProperty().set(null);
            assertEquals(M3ButtonVariant.FILLED, button.getVariant());
            assertEquals(M3ButtonSize.SMALL, button.getSize());
            assertEquals(M3ButtonShape.ROUND, button.getButtonShape());
        });
    }

    /// Verifies valid progress bindings and normalization of direct assignments.
    @Test
    void progressBindingsAndDirectAssignmentsUseTheirDocumentedDomains() {
        FxTestUtils.runOnFxThread(() -> {
            SimpleDoubleProperty barProgress = new SimpleDoubleProperty(M3ProgressBar.INDETERMINATE_PROGRESS);
            M3ProgressBar progressBar = new M3ProgressBar();
            progressBar.progressProperty().bind(barProgress);
            barProgress.set(0.5);
            assertEquals(0.5, progressBar.getProgress());
            progressBar.progressProperty().unbind();
            progressBar.setProgress(1.5);
            assertEquals(1.0, progressBar.getProgress());

            SimpleDoubleProperty indicatorProgress =
                    new SimpleDoubleProperty(M3ProgressIndicator.INDETERMINATE_PROGRESS);
            M3ProgressIndicator progressIndicator = new M3ProgressIndicator();
            progressIndicator.progressProperty().bind(indicatorProgress);
            indicatorProgress.set(0.75);
            assertEquals(0.75, progressIndicator.getProgress());
            progressIndicator.progressProperty().unbind();
            progressIndicator.setProgress(Double.NaN);
            assertEquals(M3ProgressIndicator.INDETERMINATE_PROGRESS, progressIndicator.getProgress());
        });
    }

    /// Verifies snackbar duration validation for bound and directly assigned values.
    @Test
    void snackbarDurationBindingRequiresNonNegativeFiniteDurations() {
        FxTestUtils.runOnFxThread(() -> {
            M3OverlayPane overlayPane = new M3OverlayPane();
            ObjectProperty<@Nullable Duration> duration = new SimpleObjectProperty<>(Duration.seconds(3));
            overlayPane.snackbarDisplayDurationProperty().bind(duration);
            duration.set(Duration.seconds(5));
            assertEquals(Duration.seconds(5), overlayPane.getSnackbarDisplayDuration());

            overlayPane.snackbarDisplayDurationProperty().unbind();
            overlayPane.setSnackbarDisplayDuration(Duration.millis(-1));
            assertEquals(Duration.ZERO, overlayPane.getSnackbarDisplayDuration());
        });
    }

    /// Verifies that nullable style-token bindings retain their documented effective defaults.
    @Test
    void nullableStyleTokenBindingsUseEffectiveDefaults() {
        FxTestUtils.runOnFxThread(() -> {
            M3Icon icon = new M3Icon("home");
            ObjectProperty<@Nullable String> iconFamily = new SimpleObjectProperty<>("Serif");
            ObjectProperty<@Nullable FontWeight> iconWeight = new SimpleObjectProperty<>(FontWeight.BOLD);
            icon.iconFontFamilyProperty().bind(iconFamily);
            icon.iconFontWeightProperty().bind(iconWeight);
            iconFamily.set(null);
            iconWeight.set(null);
            assertNull(icon.iconFontFamilyProperty().get());
            assertNull(icon.iconFontWeightProperty().get());
            assertEquals("System", icon.getIconFontFamily());
            assertEquals(FontWeight.MEDIUM.getWeight(), icon.getIconFontWeight());
            assertThrows(IllegalArgumentException.class, () -> icon.setIconFontWeight(Double.NaN));

            M3Text text = new M3Text("Label");
            ObjectProperty<@Nullable String> textFamily = new SimpleObjectProperty<>("Serif");
            ObjectProperty<@Nullable FontWeight> textWeight = new SimpleObjectProperty<>(FontWeight.BOLD);
            text.typographyFontFamilyProperty().bind(textFamily);
            text.typographyFontWeightProperty().bind(textWeight);
            textFamily.set(null);
            textWeight.set(null);
            assertNull(text.typographyFontFamilyProperty().get());
            assertNull(text.typographyFontWeightProperty().get());
            assertEquals("System", text.getTypographyFontFamily());
            assertEquals(FontWeight.NORMAL.getWeight(), text.getTypographyFontWeight());
        });
    }

    /// Verifies that valid bound picker values synchronize with their popup pickers.
    @Test
    void boundPickerValuesSynchronizeWithPopupPickers() {
        FxTestUtils.runOnFxThread(() -> {
            ObjectProperty<@Nullable LocalTime> dialogValue =
                    new SimpleObjectProperty<>(LocalTime.of(9, 15));
            M3TimePickerDialog dialog = new M3TimePickerDialog();
            dialog.valueProperty().bind(dialogValue);
            assertEquals(LocalTime.of(9, 15), dialog.getPicker().getValue());
            dialogValue.set(LocalTime.of(10, 45));
            assertEquals(LocalTime.of(10, 45), dialog.getPicker().getValue());

            ObjectProperty<@Nullable LocalTime> fieldValue =
                    new SimpleObjectProperty<>(LocalTime.of(12, 30));
            M3TimePickerField field = new M3TimePickerField();
            field.valueProperty().bind(fieldValue);
            assertEquals(LocalTime.of(12, 30), field.getPicker().getValue());
            fieldValue.set(LocalTime.of(13, 45));
            assertEquals(LocalTime.of(13, 45), field.getPicker().getValue());
        });
    }

    /// Verifies direct clearing and update-order requirements for bound date-range endpoints.
    @Test
    void dateRangeBindingsPreserveCompleteRangeInvariants() {
        FxTestUtils.runOnFxThread(() -> {
            LocalDate start = LocalDate.of(2026, 7, 10);
            LocalDate end = LocalDate.of(2026, 7, 15);
            M3DateRangePickerField directField = new M3DateRangePickerField(start, end);
            directField.setStartDate(null);
            assertNull(directField.getStartDate());
            assertNull(directField.getEndDate());

            ObjectProperty<@Nullable LocalDate> startSource = new SimpleObjectProperty<>(start);
            ObjectProperty<@Nullable LocalDate> endSource = new SimpleObjectProperty<>(null);
            M3DateRangePickerField boundField = new M3DateRangePickerField();
            boundField.startDateProperty().bind(startSource);
            boundField.endDateProperty().bind(endSource);
            endSource.set(end);
            assertEquals(start, boundField.getStartDate());
            assertEquals(end, boundField.getEndDate());
            assertThrows(RuntimeException.class, () -> boundField.setRange(start, end));
            endSource.set(null);
            startSource.set(null);
            assertNull(boundField.getStartDate());
            assertNull(boundField.getEndDate());
        });
    }
}
