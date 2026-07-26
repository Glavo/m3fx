// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.controls.M3Color;
import org.glavo.m3fx.controls.M3ColorArea;
import org.glavo.m3fx.controls.M3ColorChannel;
import org.glavo.m3fx.controls.M3ColorField;
import org.glavo.m3fx.controls.M3ColorPicker;
import org.glavo.m3fx.controls.M3ColorSlider;
import org.glavo.m3fx.controls.M3ColorSwatch;
import org.glavo.m3fx.controls.M3ColorSwatchPicker;
import org.glavo.m3fx.controls.M3ColorSwatchRounding;
import org.glavo.m3fx.controls.M3ColorWheel;
import org.jetbrains.annotations.NotNullByDefault;

/// The default composed skin for [M3ColorPicker].
///
/// All editable children are bound bidirectionally to the picker value. Visibility properties only affect layout;
/// hidden editors remain synchronized and reappear with the current color.
@NotNullByDefault
public final class M3ColorPickerSkin extends SkinBase<M3ColorPicker> {
    /// The two-dimensional channel editor.
    private final M3ColorArea area = new M3ColorArea();

    /// The optional physical hue wheel.
    private final M3ColorWheel wheel = new M3ColorWheel();

    /// The always-present linear hue editor.
    private final M3ColorSlider hueSlider = new M3ColorSlider();

    /// The optional alpha-channel editor.
    private final M3ColorSlider alphaSlider = new M3ColorSlider();

    /// The passive preview of the current value.
    private final M3ColorSwatch preview = new M3ColorSwatch();

    /// The optional hexadecimal editor.
    private final M3ColorField field = new M3ColorField();

    /// The optional preset palette.
    private final M3ColorSwatchPicker presetPicker = new M3ColorSwatchPicker();

    /// The complete component layout.
    private final VBox content = new VBox(12.0);

    /// Mirrors changes from the public preset list.
    private final ListChangeListener<M3Color> presetsListener = this::applyPresetChanges;

    /// Selects a preset when the current value becomes equivalent to one.
    private final InvalidationListener valueListener = observable -> synchronizePresetSelection();

    /// Prevents value-to-selection synchronization from replacing the current color-space representation.
    private boolean synchronizingPresetSelection;

    /// Copies a newly selected preset into the shared picker value.
    private final ChangeListener<Number> selectedIndexListener = (observable, oldValue, newValue) -> {
        int index = newValue.intValue();
        if (!synchronizingPresetSelection && index >= 0) {
            getSkinnable().setValue(presetPicker.getItems().get(index));
        }
    };

    /// Updates preset layout when its visibility policy changes.
    private final InvalidationListener presetVisibilityListener = observable -> updatePresetVisibility();

    /// Creates a composed color-picker skin.
    ///
    /// @param control the control managed by this skin
    /// @throws NullPointerException if `control` is `null`
    public M3ColorPickerSkin(M3ColorPicker control) {
        super(control);

        area.getStyleClass().add("m3-color-picker-area");
        wheel.getStyleClass().add("m3-color-picker-wheel");
        hueSlider.getStyleClass().add("m3-color-picker-hue-slider");
        alphaSlider.getStyleClass().add("m3-color-picker-alpha-slider");
        preview.getStyleClass().add("m3-color-picker-preview");
        field.getStyleClass().add("m3-color-picker-field");
        presetPicker.getStyleClass().add("m3-color-picker-presets");
        FlowPane mainRow = new FlowPane(Orientation.HORIZONTAL, 16.0, 16.0);
        mainRow.setPrefWrapLength(448.0);
        mainRow.getStyleClass().add("m3-color-picker-main");
        FlowPane valueRow = new FlowPane(Orientation.HORIZONTAL, 12.0, 12.0);
        valueRow.setPrefWrapLength(224.0);
        valueRow.getStyleClass().add("m3-color-picker-value-row");
        content.getStyleClass().add("m3-color-picker-content");

        hueSlider.setChannel(M3ColorChannel.HUE);
        alphaSlider.setChannel(M3ColorChannel.ALPHA);
        presetPicker.setColumnCount(8);
        presetPicker.setRounding(M3ColorSwatchRounding.DEFAULT);

        area.valueProperty().bindBidirectional(control.valueProperty());
        wheel.valueProperty().bindBidirectional(control.valueProperty());
        hueSlider.valueProperty().bindBidirectional(control.valueProperty());
        alphaSlider.valueProperty().bindBidirectional(control.valueProperty());
        field.valueProperty().bindBidirectional(control.valueProperty());
        area.planeProperty().bind(control.planeProperty());
        field.includeAlphaProperty().bind(control.showAlphaProperty());
        preview.colorProperty().bind(control.valueProperty());

        bindVisibility(alphaSlider, control.showAlphaProperty());
        bindVisibility(wheel, control.showColorWheelProperty());
        bindVisibility(field, control.showColorFieldProperty());

        mainRow.setAlignment(Pos.CENTER_LEFT);
        mainRow.getChildren().addAll(area, wheel);
        valueRow.setAlignment(Pos.CENTER_LEFT);
        valueRow.getChildren().addAll(preview, field);
        content.getChildren().addAll(mainRow, hueSlider, alphaSlider, valueRow, presetPicker);
        getChildren().add(content);

        control.getPresets().addListener(presetsListener);
        control.valueProperty().addListener(valueListener);
        control.showPresetsProperty().addListener(presetVisibilityListener);
        presetPicker.selectedIndexProperty().addListener(selectedIndexListener);

        initializePresets();
        updatePresetVisibility();
    }

    /// Releases bindings and listeners installed by this skin.
    @Override
    public void dispose() {
        M3ColorPicker control = getSkinnable();
        control.getPresets().removeListener(presetsListener);
        control.valueProperty().removeListener(valueListener);
        control.showPresetsProperty().removeListener(presetVisibilityListener);
        presetPicker.selectedIndexProperty().removeListener(selectedIndexListener);

        area.valueProperty().unbindBidirectional(control.valueProperty());
        wheel.valueProperty().unbindBidirectional(control.valueProperty());
        hueSlider.valueProperty().unbindBidirectional(control.valueProperty());
        alphaSlider.valueProperty().unbindBidirectional(control.valueProperty());
        field.valueProperty().unbindBidirectional(control.valueProperty());
        area.planeProperty().unbind();
        field.includeAlphaProperty().unbind();
        preview.colorProperty().unbind();

        alphaSlider.visibleProperty().unbind();
        alphaSlider.managedProperty().unbind();
        wheel.visibleProperty().unbind();
        wheel.managedProperty().unbind();
        field.visibleProperty().unbind();
        field.managedProperty().unbind();
        getChildren().remove(content);
        super.dispose();
    }

    /// Returns the minimum width required by the composed content.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + content.minWidth(height) + rightInset;
    }

    /// Returns the minimum height required by the composed content.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + content.minHeight(width) + bottomInset;
    }

    /// Returns the preferred width of the composed content.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + content.prefWidth(height) + rightInset;
    }

    /// Returns the preferred height of the composed content.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + content.prefHeight(width) + bottomInset;
    }

    /// Sizes the composed content to the control's content box.
    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        content.resizeRelocate(contentX, contentY, contentWidth, contentHeight);
    }

    /// Binds a child node's visible and managed state to one policy property.
    private static void bindVisibility(
            Node node,
            ReadOnlyBooleanProperty visibility
    ) {
        node.visibleProperty().bind(visibility);
        node.managedProperty().bind(visibility);
    }

    /// Initializes the internal palette from the current public preset list.
    private void initializePresets() {
        presetPicker.getItems().setAll(getSkinnable().getPresets());
        synchronizePresetSelection();
        updatePresetVisibility();
    }

    /// Applies one public preset-list change incrementally to the retained palette.
    private void applyPresetChanges(ListChangeListener.Change<? extends M3Color> change) {
        synchronizingPresetSelection = true;
        try {
            while (change.next()) {
                if (change.wasPermutated()) {
                    presetPicker.getItems().setAll(getSkinnable().getPresets());
                    break;
                }
                if (change.wasUpdated()) {
                    for (int index = change.getFrom(); index < change.getTo(); index++) {
                        presetPicker.getItems().set(index, getSkinnable().getPresets().get(index));
                    }
                    continue;
                }

                int from = change.getFrom();
                if (change.getRemovedSize() > 0) {
                    presetPicker.getItems()
                            .subList(from, from + change.getRemovedSize())
                            .clear();
                }
                if (change.getAddedSize() > 0) {
                    presetPicker.getItems().addAll(from, change.getAddedSubList());
                }
            }
        } finally {
            synchronizingPresetSelection = false;
        }
        synchronizePresetSelection();
        updatePresetVisibility();
    }

    /// Selects the first preset equivalent to the current picker value.
    private void synchronizePresetSelection() {
        synchronizingPresetSelection = true;
        try {
            presetPicker.selectColor(getSkinnable().getValue());
        } finally {
            synchronizingPresetSelection = false;
        }
    }

    /// Applies preset visibility from the public policy and list contents.
    private void updatePresetVisibility() {
        boolean visible = getSkinnable().isShowPresets() && !presetPicker.getItems().isEmpty();
        presetPicker.setVisible(visible);
        presetPicker.setManaged(visible);
    }
}
