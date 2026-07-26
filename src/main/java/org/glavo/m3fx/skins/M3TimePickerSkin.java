// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.skin.LabeledSkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3TimePicker;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3FiniteTransition;
import org.glavo.m3fx.internal.M3FocusRequests;
import org.glavo.m3fx.internal.M3InternalIcon;
import org.glavo.m3fx.internal.M3KeyEvents;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.glavo.m3fx.internal.M3PickerAccessibilityPresentation;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.LocalTime;
import java.util.List;

/// The default Material dial and keyboard-input skin for [M3TimePicker].
///
/// The dial keeps a fixed 24-label node pool and one reusable selector transition. Pointer animation pulses update
/// primitive geometry only, while structural nodes are retained across values, clock formats, and input modes.
@NotNullByDefault
public class M3TimePickerSkin extends SkinBase<M3TimePicker> implements M3PickerAccessibilityPresentation {
    /// The internal layout container style class.
    private static final String CONTAINER_STYLE_CLASS = "m3-time-picker-container";

    /// The selected time display row style class.
    private static final String DISPLAY_STYLE_CLASS = "m3-time-picker-display";

    /// The hour display style class.
    private static final String HOUR_DISPLAY_STYLE_CLASS = "m3-time-picker-hour-display";

    /// The display separator style class.
    private static final String DISPLAY_SEPARATOR_STYLE_CLASS = "m3-time-picker-display-separator";

    /// The minute display style class.
    private static final String MINUTE_DISPLAY_STYLE_CLASS = "m3-time-picker-minute-display";

    /// The main content style class.
    private static final String CONTENT_STYLE_CLASS = "m3-time-picker-content";

    /// The clock dial style class.
    private static final String DIAL_STYLE_CLASS = "m3-time-picker-dial";

    /// The dial background style class.
    private static final String DIAL_BACKGROUND_STYLE_CLASS = "m3-time-picker-dial-background";

    /// The dial track style class.
    private static final String DIAL_TRACK_STYLE_CLASS = "m3-time-picker-dial-track";

    /// The dial handle style class.
    private static final String DIAL_HANDLE_STYLE_CLASS = "m3-time-picker-dial-handle";

    /// The dial center style class.
    private static final String DIAL_CENTER_STYLE_CLASS = "m3-time-picker-dial-center";

    /// The mode button style class.
    private static final String MODE_BUTTON_STYLE_CLASS = "m3-time-picker-mode-button";

    /// The input content style class.
    private static final String INPUT_CONTENT_STYLE_CLASS = "m3-time-picker-input-content";

    /// The input field style class.
    private static final String INPUT_FIELD_STYLE_CLASS = "m3-time-picker-input-field";

    /// The input group style class.
    private static final String INPUT_GROUP_STYLE_CLASS = "m3-time-picker-input-group";

    /// The input label style class.
    private static final String INPUT_LABEL_STYLE_CLASS = "m3-time-picker-input-label";

    /// The selectable time cell style class.
    private static final String CELL_STYLE_CLASS = "m3-time-picker-cell";

    /// The dial label style class.
    private static final String DIAL_LABEL_STYLE_CLASS = "m3-time-picker-dial-label";

    /// The period row style class.
    private static final String PERIOD_ROW_STYLE_CLASS = "m3-time-picker-period-row";

    /// The period cell style class.
    private static final String PERIOD_CELL_STYLE_CLASS = "m3-time-picker-period-cell";

    /// The selected cell style class.
    private static final String SELECTED_CELL_STYLE_CLASS = "m3-time-picker-selected-cell";

    /// One complete rotation in radians.
    private static final double FULL_ROTATION = Math.PI * 2.0;

    /// The official clock dial diameter.
    private static final double DIAL_SIZE = 256.0;

    /// The outer label and selector radius as a fraction of the dial diameter.
    private static final double OUTER_RADIUS_RATIO = 104.0 / DIAL_SIZE;

    /// The inner 24-hour label and selector radius as a fraction of the dial diameter.
    private static final double INNER_RADIUS_RATIO = 66.0 / DIAL_SIZE;

    /// The square layout size reserved for each dial label.
    private static final double DIAL_LABEL_SIZE = 40.0;

    /// The logical content width of the portrait picker.
    private static final double PORTRAIT_CONTENT_WIDTH = 280.0;

    /// The minimum content width that activates the official horizontal dial arrangement.
    private static final double LANDSCAPE_CONTENT_WIDTH = 560.0;

    /// The selected pseudo-class used by selector and dial-label nodes.
    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    /// The pseudo-class applied while hour selection is active.
    private static final PseudoClass HOUR_SELECTION_PSEUDO_CLASS = PseudoClass.getPseudoClass("hour-selection");

    /// The pseudo-class applied while minute selection is active.
    private static final PseudoClass MINUTE_SELECTION_PSEUDO_CLASS = PseudoClass.getPseudoClass("minute-selection");

    /// The pseudo-class applied to 24-hour picker layouts.
    private static final PseudoClass TWENTY_FOUR_HOUR_PSEUDO_CLASS =
            PseudoClass.getPseudoClass("twenty-four-hour");

    /// The pseudo-class applied to 12-hour picker layouts.
    private static final PseudoClass TWELVE_HOUR_PSEUDO_CLASS = PseudoClass.getPseudoClass("twelve-hour");

    /// The pseudo-class applied to horizontal dial layouts.
    private static final PseudoClass LANDSCAPE_PSEUDO_CLASS = PseudoClass.getPseudoClass("landscape");

    /// The pseudo-class applied to vertical dial layouts.
    private static final PseudoClass PORTRAIT_PSEUDO_CLASS = PseudoClass.getPseudoClass("portrait");

    /// The pseudo-class applied when a dialog supplies the mode switch in its action row.
    private static final PseudoClass DIALOG_EMBEDDED_PSEUDO_CLASS =
            PseudoClass.getPseudoClass("dialog-embedded");

    /// The pseudo-class applied to invalid keyboard input fields.
    private static final PseudoClass INVALID_PSEUDO_CLASS = PseudoClass.getPseudoClass("invalid");

    /// The pseudo-class applied to a horizontal period selector.
    private static final PseudoClass HORIZONTAL_PSEUDO_CLASS = PseudoClass.getPseudoClass("horizontal");

    /// The pseudo-class applied to a vertical period selector.
    private static final PseudoClass VERTICAL_PSEUDO_CLASS = PseudoClass.getPseudoClass("vertical");

    /// The pseudo-class applied when the period selector belongs to Input mode.
    private static final PseudoClass INPUT_PSEUDO_CLASS = PseudoClass.getPseudoClass("input");

    /// The pseudo-class applied when the period selector belongs to Dial mode.
    private static final PseudoClass DIAL_PSEUDO_CLASS = PseudoClass.getPseudoClass("dial");

    /// The root Material container.
    private final StackPane container = new StackPane();

    /// The custom pane that keeps all structural nodes under one stable parent.
    private final PickerLayoutPane layoutPane;

    /// The selected-time display used by dial mode.
    private final HBox display = new HBox();

    /// The hour selector displayed above or beside the clock dial.
    private final TimeCellButton hourSelector;

    /// The separator displayed between hour and minute selectors.
    private final Label displaySeparator = new Label(":");

    /// The minute selector displayed above or beside the clock dial.
    private final TimeCellButton minuteSelector;

    /// The reusable AM/PM selector.
    private final PeriodSelectorPane periodSelector;

    /// The fixed-node clock dial.
    private final DialPane dial;

    /// The keyboard-input row.
    private final HBox inputContent = new HBox();

    /// The hour keyboard input.
    private final TextField hourInput = new TextField();

    /// The minute keyboard input.
    private final TextField minuteInput = new TextField();

    /// The hour input and its supporting label.
    private final VBox hourInputGroup;

    /// The minute input and its supporting label.
    private final VBox minuteInputGroup;

    /// The separator displayed between keyboard inputs.
    private final Label inputSeparator = new Label(":");

    /// The icon used by the standalone mode toggle, or null when a dialog owns the action.
    private final @Nullable M3InternalIcon modeIcon;

    /// The standalone Dial/Input toggle, or null when a dialog owns the action.
    private final @Nullable M3IconButton modeButton;

    /// The one reusable transition that moves the dial selector.
    private final DialSelectorTransition selectorTransition;


    /// Cached hour availability for selectable bounds.
    private final boolean[] selectableHours = new boolean[24];

    /// Refreshes selected labels and selector geometry after the value changes.
    private final InvalidationListener valueInvalidation = observable -> refreshValue(true);

    /// Refreshes dial labels and layout after clock format or minute-step changes.
    private final InvalidationListener structureInvalidation = observable -> refreshStructure();

    /// Refreshes selectable states after minimum or maximum time changes.
    private final InvalidationListener boundsInvalidation = observable -> refreshStructure();

    /// Switches retained content between Dial and Input modes.
    private final InvalidationListener inputModeInvalidation = observable -> refreshMode();

    /// Relayouts retained geometry after a styleable algorithm metric changes.
    private final InvalidationListener metricsInvalidation;

    /// Requests logical relayout when effective node orientation changes.
    private final InvalidationListener orientationInvalidation;

    /// Handles picker-level keyboard navigation before the control's generic fallback handler.
    private final EventHandler<KeyEvent> navigationKeyFilter = this::handleNavigationKeyPressed;

    /// Handles pointer events whose target is the dial background.
    private final EventHandler<MouseEvent> dialMouseHandler = this::handleDialMouseEvent;

    /// Handles pointer events whose target is one of the direct dial label children.
    private final EventHandler<MouseEvent> dialLabelMouseHandler = this::handleDialLabelMouseEvent;

    /// Commits or selects the hour input when its focus changes.
    private final ChangeListener<Boolean> hourInputFocusListener =
            (observable, oldValue, focused) -> handleInputFocusChanged(SelectionUnit.HOUR, focused);

    /// Commits or selects the minute input when its focus changes.
    private final ChangeListener<Boolean> minuteInputFocusListener =
            (observable, oldValue, focused) -> handleInputFocusChanged(SelectionUnit.MINUTE, focused);

    /// The selector unit currently controlled by the dial and keyboard arrows.
    private SelectionUnit activeUnit = SelectionUnit.HOUR;

    /// Whether availability and selector geometry have completed their first refresh.
    private boolean initialized;

    /// Guards input text synchronization against action and focus callbacks.
    private boolean refreshingInputs;

    /// Creates a Material Design 3 time picker skin.
    ///
    /// @param control the time picker control
    public M3TimePickerSkin(M3TimePicker control) {
        super(control);

        hourSelector = createTimeCell("", HOUR_DISPLAY_STYLE_CLASS);
        minuteSelector = createTimeCell("", MINUTE_DISPLAY_STYLE_CLASS);
        periodSelector = new PeriodSelectorPane();
        dial = new DialPane();
        hourInputGroup = createInputGroup(hourInput, "Hour");
        minuteInputGroup = createInputGroup(minuteInput, "Minute");
        if (control.getPseudoClassStates().contains(DIALOG_EMBEDDED_PSEUDO_CLASS)) {
            modeIcon = null;
            modeButton = null;
        } else {
            modeIcon = new M3InternalIcon(
                    M3InternalIcon.Glyph.KEYBOARD,
                    M3InternalIcon.ColorRole.ON_SURFACE_VARIANT
            );
            modeButton = new M3IconButton(modeIcon);
        }
        selectorTransition = new DialSelectorTransition();
        layoutPane = new PickerLayoutPane();
        metricsInvalidation = observable -> {
            dial.requestLayout();
            layoutPane.requestLayout();
            getSkinnable().requestLayout();
        };
        orientationInvalidation = observable -> layoutPane.requestLayout();

        initializeNodes();
        installListeners(control);
        getChildren().setAll(container);
        refreshAll();
    }

    /// Returns the number of period and dial nodes in the current picker presentation.
    @Override
    public int accessibleItemCount() {
        int count = 0;
        if (periodSelector.isVisible()) {
            count += isPresentedTimeCell(periodSelector.amButton) ? 1 : 0;
            count += isPresentedTimeCell(periodSelector.pmButton) ? 1 : 0;
        }
        if (dial.isVisible()) {
            for (int index = 0; index < dial.activeLabelCount; index++) {
                count += isPresentedTimeCell(dial.labels[index]) ? 1 : 0;
            }
        }
        return count;
    }

    /// Returns a period or dial node without traversing or copying the scene graph.
    @Override
    public @Nullable Node accessibleItemAt(int index) {
        if (index < 0) {
            return null;
        }
        if (periodSelector.isVisible()) {
            if (isPresentedTimeCell(periodSelector.amButton) && index-- == 0) {
                return periodSelector.amButton;
            }
            if (isPresentedTimeCell(periodSelector.pmButton) && index-- == 0) {
                return periodSelector.pmButton;
            }
        }
        if (dial.isVisible()) {
            for (int labelIndex = 0; labelIndex < dial.activeLabelCount; labelIndex++) {
                DialLabelButton label = dial.labels[labelIndex];
                if (isPresentedTimeCell(label) && index-- == 0) {
                    return label;
                }
            }
        }
        return null;
    }

    /// Returns whether a reusable time cell belongs to the current indexed presentation.
    private static boolean isPresentedTimeCell(Node node) {
        return M3Accessible.isEffectivelyReachable(node)
                && !node.isMouseTransparent()
                && node.getUserData() instanceof LocalTime;
    }

    /// Removes listeners, event filters, and running animation state before disposal.
    @Override
    public void dispose() {
        M3TimePicker control = getSkinnable();
        control.valueProperty().removeListener(valueInvalidation);
        control.use24HourClockProperty().removeListener(structureInvalidation);
        control.minuteStepProperty().removeListener(structureInvalidation);
        control.minTimeProperty().removeListener(boundsInvalidation);
        control.maxTimeProperty().removeListener(boundsInvalidation);
        control.inputModeProperty().removeListener(inputModeInvalidation);
        control.containerSpacingProperty().removeListener(metricsInvalidation);
        control.dialHandleSizeProperty().removeListener(metricsInvalidation);
        control.dialCenterSizeProperty().removeListener(metricsInvalidation);
        control.effectiveNodeOrientationProperty().removeListener(orientationInvalidation);
        control.removeEventFilter(KeyEvent.KEY_PRESSED, navigationKeyFilter);

        uninstallDialPointerHandlers();
        hourInput.focusedProperty().removeListener(hourInputFocusListener);
        minuteInput.focusedProperty().removeListener(minuteInputFocusListener);
        hourInput.setOnAction(null);
        minuteInput.setOnAction(null);
        hourSelector.setOnAction(null);
        minuteSelector.setOnAction(null);
        periodSelector.dispose();
        M3IconButton standaloneModeButton = modeButton;
        if (standaloneModeButton != null) {
            standaloneModeButton.setOnAction(null);
        }

        selectorTransition.stop();
        getChildren().remove(container);
        super.dispose();
    }

    /// Computes the minimum width from the retained Material container.
    @Override
    protected double computeMinWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + container.minWidth(height) + rightInset;
    }

    /// Computes the minimum height from the retained Material container.
    @Override
    protected double computeMinHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + container.minHeight(width) + bottomInset;
    }

    /// Computes the preferred width from the retained Material container.
    @Override
    protected double computePrefWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + container.prefWidth(height) + rightInset;
    }

    /// Computes the preferred height from the retained Material container.
    @Override
    protected double computePrefHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + container.prefHeight(width) + bottomInset;
    }

    /// Lays out the retained Material container inside the skin bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        container.resizeRelocate(x, y, width, height);
    }

    /// Creates node structure and establishes stable style roles.
    private void initializeNodes() {
        M3TimePicker control = getSkinnable();

        container.getStyleClass().add(CONTAINER_STYLE_CLASS);
        display.getStyleClass().add(DISPLAY_STYLE_CLASS);
        display.setAlignment(Pos.CENTER_LEFT);
        display.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        displaySeparator.getStyleClass().add(DISPLAY_SEPARATOR_STYLE_CLASS);
        displaySeparator.setAlignment(Pos.CENTER);

        hourSelector.setAccessibleText("Hour");
        minuteSelector.setAccessibleText("Minute");
        hourSelector.setOnAction(event -> setActiveUnit(SelectionUnit.HOUR));
        minuteSelector.setOnAction(event -> setActiveUnit(SelectionUnit.MINUTE));
        display.getChildren().setAll(hourSelector, displaySeparator, minuteSelector);

        inputContent.getStyleClass().add(INPUT_CONTENT_STYLE_CLASS);
        inputContent.setAlignment(Pos.TOP_LEFT);
        inputContent.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        inputSeparator.getStyleClass().add(DISPLAY_SEPARATOR_STYLE_CLASS);
        inputSeparator.setAlignment(Pos.TOP_CENTER);
        inputContent.getChildren().setAll(hourInputGroup, inputSeparator, minuteInputGroup);

        configureInput(hourInput, "Hour");
        configureInput(minuteInput, "Minute");
        hourInput.focusedProperty().addListener(hourInputFocusListener);
        minuteInput.focusedProperty().addListener(minuteInputFocusListener);
        hourInput.setOnAction(event -> handleInputAction(SelectionUnit.HOUR));
        minuteInput.setOnAction(event -> handleInputAction(SelectionUnit.MINUTE));

        M3IconButton standaloneModeButton = modeButton;
        if (standaloneModeButton != null) {
            standaloneModeButton.getStyleClass().add(MODE_BUTTON_STYLE_CLASS);
            standaloneModeButton.setAccessibleText("Use keyboard input");
            standaloneModeButton.setOnAction(event -> control.setInputMode(!control.isInputMode()));
        }

        container.getChildren().setAll(layoutPane);
        installDialPointerHandlers();
    }

    /// Installs listeners that synchronize control properties with retained skin nodes.
    private void installListeners(M3TimePicker control) {
        control.valueProperty().addListener(valueInvalidation);
        control.use24HourClockProperty().addListener(structureInvalidation);
        control.minuteStepProperty().addListener(structureInvalidation);
        control.minTimeProperty().addListener(boundsInvalidation);
        control.maxTimeProperty().addListener(boundsInvalidation);
        control.inputModeProperty().addListener(inputModeInvalidation);
        control.containerSpacingProperty().addListener(metricsInvalidation);
        control.dialHandleSizeProperty().addListener(metricsInvalidation);
        control.dialCenterSizeProperty().addListener(metricsInvalidation);
        control.effectiveNodeOrientationProperty().addListener(orientationInvalidation);
        control.addEventFilter(KeyEvent.KEY_PRESSED, navigationKeyFilter);
    }

    /// Installs shared pointer handlers on the dial and its fixed label pool.
    private void installDialPointerHandlers() {
        dial.addEventHandler(MouseEvent.MOUSE_PRESSED, dialMouseHandler);
        dial.addEventHandler(MouseEvent.MOUSE_DRAGGED, dialMouseHandler);
        dial.addEventHandler(MouseEvent.MOUSE_RELEASED, dialMouseHandler);
        for (DialLabelButton label : dial.labels) {
            label.addEventFilter(MouseEvent.MOUSE_PRESSED, dialLabelMouseHandler);
            label.addEventFilter(MouseEvent.MOUSE_DRAGGED, dialLabelMouseHandler);
            label.addEventFilter(MouseEvent.MOUSE_RELEASED, dialLabelMouseHandler);
        }
    }

    /// Removes shared pointer handlers from the dial and its fixed label pool.
    private void uninstallDialPointerHandlers() {
        dial.removeEventHandler(MouseEvent.MOUSE_PRESSED, dialMouseHandler);
        dial.removeEventHandler(MouseEvent.MOUSE_DRAGGED, dialMouseHandler);
        dial.removeEventHandler(MouseEvent.MOUSE_RELEASED, dialMouseHandler);
        for (DialLabelButton label : dial.labels) {
            label.removeEventFilter(MouseEvent.MOUSE_PRESSED, dialLabelMouseHandler);
            label.removeEventFilter(MouseEvent.MOUSE_DRAGGED, dialLabelMouseHandler);
            label.removeEventFilter(MouseEvent.MOUSE_RELEASED, dialLabelMouseHandler);
            label.setOnAction(null);
        }
    }

    /// Configures one numeric keyboard input without regular-expression allocation on each edit.
    private static void configureInput(TextField input, String accessibleText) {
        input.getStyleClass().add(INPUT_FIELD_STYLE_CLASS);
        input.setAccessibleText(accessibleText);
        input.setAlignment(Pos.CENTER);
        input.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        input.setPromptText("--");
        input.setTextFormatter(new TextFormatter<>(M3TimePickerSkin::filterNumericInput));
    }

    /// Creates one keyboard input group with the specification's supporting label below the field.
    private static VBox createInputGroup(TextField input, String labelText) {
        Label label = new Label(labelText);
        label.getStyleClass().add(INPUT_LABEL_STYLE_CLASS);
        VBox group = new VBox(input, label);
        group.getStyleClass().add(INPUT_GROUP_STYLE_CLASS);
        group.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        return group;
    }

    /// Accepts at most two ASCII digits in a time input.
    private static @Nullable TextFormatter.Change filterNumericInput(TextFormatter.Change change) {
        String text = change.getControlNewText();
        if (text.length() > 2) {
            return null;
        }
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (character < '0' || character > '9') {
                return null;
            }
        }
        return change;
    }

    /// Refreshes every structural, selectable, and value-dependent node.
    private void refreshAll() {
        refreshAvailability();
        refreshClockFormatPseudoClasses();
        refreshMode();
        refreshValue(false);
        initialized = true;
    }

    /// Refreshes dial structure and availability after a structural property changes.
    private void refreshStructure() {
        refreshAvailability();
        refreshClockFormatPseudoClasses();
        refreshMode();
        refreshValue(false);
        layoutPane.requestLayout();
    }

    /// Switches managed content between Dial and Input variants without replacing nodes.
    private void refreshMode() {
        boolean inputMode = getSkinnable().isInputMode();
        setVisibleAndManaged(display, !inputMode);
        setVisibleAndManaged(dial, !inputMode);
        setVisibleAndManaged(inputContent, inputMode);
        periodSelector.setInputMode(inputMode);

        M3InternalIcon standaloneModeIcon = modeIcon;
        M3IconButton standaloneModeButton = modeButton;
        if (standaloneModeIcon != null && standaloneModeButton != null) {
            standaloneModeIcon.setGlyph(inputMode ? M3InternalIcon.Glyph.SCHEDULE : M3InternalIcon.Glyph.KEYBOARD);
            standaloneModeButton.setAccessibleText(inputMode ? "Use clock dial" : "Use keyboard input");
        }
        refreshInputFields();
        layoutPane.requestLayout();
    }

    /// Updates value text, enabled labels, selected states, and selector geometry.
    private void refreshValue(boolean animateSelector) {
        M3TimePicker control = getSkinnable();
        @Nullable LocalTime selectedTime = control.getValue();
        LocalTime baseTime = selectedTime == null ? fallbackTime() : selectedTime;

        refreshDisplay(selectedTime);
        refreshInputFields();
        periodSelector.refresh(baseTime, selectedTime);
        dial.refresh(baseTime, selectedTime, animateSelector && initialized && !dial.dragging);
        updateActiveUnitPseudoClasses();
    }

    /// Updates the selected-time display without replacing labels.
    private void refreshDisplay(@Nullable LocalTime selectedTime) {
        M3TimePicker control = getSkinnable();
        String hourText = selectedTime == null ? "--" : formatHour(selectedTime, control.isUse24HourClock());
        String minuteText = selectedTime == null ? "--" : formatTwoDigits(selectedTime.getMinute());
        setTextIfChanged(hourSelector, hourText);
        setTextIfChanged(minuteSelector, minuteText);
    }

    /// Synchronizes non-edited input fields from the selected value.
    private void refreshInputFields() {
        if (refreshingInputs) {
            return;
        }

        refreshingInputs = true;
        try {
            M3TimePicker control = getSkinnable();
            @Nullable LocalTime selectedTime = control.getValue();
            String hourText = selectedTime == null ? "" : formatHour(selectedTime, control.isUse24HourClock());
            String minuteText = selectedTime == null ? "" : formatTwoDigits(selectedTime.getMinute());
            if (!hourInput.isFocused()) {
                setTextIfChanged(hourInput, hourText);
                hourInput.pseudoClassStateChanged(INVALID_PSEUDO_CLASS, false);
            }
            if (!minuteInput.isFocused()) {
                setTextIfChanged(minuteInput, minuteText);
                minuteInput.pseudoClassStateChanged(INVALID_PSEUDO_CLASS, false);
            }
        } finally {
            refreshingInputs = false;
        }
    }

    /// Updates 12/24-hour pseudo-classes used by CSS and layout.
    private void refreshClockFormatPseudoClasses() {
        M3TimePicker control = getSkinnable();
        boolean twentyFourHour = control.isUse24HourClock();
        control.pseudoClassStateChanged(TWENTY_FOUR_HOUR_PSEUDO_CLASS, twentyFourHour);
        control.pseudoClassStateChanged(TWELVE_HOUR_PSEUDO_CLASS, !twentyFourHour);
        periodSelector.setClockFormat(twentyFourHour);
    }

    /// Recomputes which hours and periods contain any selectable minute.
    private void refreshAvailability() {
        for (int hour = 0; hour < selectableHours.length; hour++) {
            selectableHours[hour] = hourHasSelectableMinute(hour);
        }
    }

    /// Changes the active selector and animates the handle to the corresponding value.
    private void setActiveUnit(SelectionUnit unit) {
        if (activeUnit == unit) {
            updateActiveUnitPseudoClasses();
            return;
        }
        activeUnit = unit;
        updateActiveUnitPseudoClasses();

        @Nullable LocalTime selectedTime = getSkinnable().getValue();
        LocalTime baseTime = selectedTime == null ? fallbackTime() : selectedTime;
        dial.refresh(baseTime, selectedTime, initialized);
    }

    /// Synchronizes selector pseudo-classes across display and input nodes.
    private void updateActiveUnitPseudoClasses() {
        boolean hourActive = activeUnit == SelectionUnit.HOUR;
        M3TimePicker control = getSkinnable();
        control.pseudoClassStateChanged(HOUR_SELECTION_PSEUDO_CLASS, hourActive);
        control.pseudoClassStateChanged(MINUTE_SELECTION_PSEUDO_CLASS, !hourActive);
        hourSelector.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, hourActive);
        minuteSelector.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, !hourActive);
        hourInput.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, hourActive);
        minuteInput.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, !hourActive);
    }

    /// Selects and validates an input field when keyboard focus changes.
    private void handleInputFocusChanged(SelectionUnit unit, boolean focused) {
        if (refreshingInputs) {
            return;
        }
        if (focused) {
            setActiveUnit(unit);
            inputFor(unit).selectAll();
        } else {
            commitInputFields();
        }
    }

    /// Commits an input field and advances from hour to minute on Enter.
    private void handleInputAction(SelectionUnit unit) {
        if (!commitInputFields()) {
            return;
        }
        if (unit == SelectionUnit.HOUR) {
            minuteInput.requestFocus();
            minuteInput.selectAll();
        }
    }

    /// Parses both keyboard fields and commits a valid, selectable time.
    private boolean commitInputFields() {
        if (refreshingInputs) {
            return false;
        }

        @Nullable Integer hour = parseInput(hourInput.getText());
        @Nullable Integer minute = parseInput(minuteInput.getText());
        M3TimePicker control = getSkinnable();
        boolean validHour = hour != null && isDisplayHourValid(hour, control.isUse24HourClock());
        boolean validMinute = minute != null && minute >= 0 && minute < 60;
        hourInput.pseudoClassStateChanged(INVALID_PSEUDO_CLASS, !validHour);
        minuteInput.pseudoClassStateChanged(INVALID_PSEUDO_CLASS, !validMinute);
        if (!validHour || !validMinute) {
            return false;
        }

        @Nullable LocalTime selectedTime = control.getValue();
        LocalTime baseTime = selectedTime == null ? fallbackTime() : selectedTime;
        int actualHour = control.isUse24HourClock()
                ? hour
                : toActualHour(hour, baseTime.getHour() >= 12);
        LocalTime candidate = LocalTime.of(actualHour, minute);
        if (control.isTimeDisabled(candidate)) {
            hourInput.pseudoClassStateChanged(INVALID_PSEUDO_CLASS, true);
            minuteInput.pseudoClassStateChanged(INVALID_PSEUDO_CLASS, true);
            return false;
        }

        hourInput.pseudoClassStateChanged(INVALID_PSEUDO_CLASS, false);
        minuteInput.pseudoClassStateChanged(INVALID_PSEUDO_CLASS, false);
        control.setValue(candidate);
        return true;
    }

    /// Returns the input associated with one selector unit.
    private TextField inputFor(SelectionUnit unit) {
        return unit == SelectionUnit.HOUR ? hourInput : minuteInput;
    }

    /// Parses a non-empty decimal input without exception-driven validation.
    private static @Nullable Integer parseInput(String text) {
        if (text.isEmpty()) {
            return null;
        }
        int value = 0;
        for (int index = 0; index < text.length(); index++) {
            value = value * 10 + text.charAt(index) - '0';
        }
        return value;
    }

    /// Returns whether a displayed hour belongs to the selected clock format.
    private static boolean isDisplayHourValid(int hour, boolean twentyFourHour) {
        return twentyFourHour ? hour >= 0 && hour < 24 : hour >= 1 && hour <= 12;
    }

    /// Handles arrow, Home, and End navigation while focus is outside a text editor.
    private void handleNavigationKeyPressed(KeyEvent event) {
        if (event.isConsumed() || M3KeyEvents.hasNavigationModifier(event)) {
            return;
        }
        if (event.getTarget() instanceof Node target && M3Accessible.containsNode(inputContent, target)) {
            return;
        }

        int direction = switch (event.getCode()) {
            case RIGHT -> M3NodeLayout.isRightToLeft(getSkinnable()) ? -1 : 1;
            case LEFT -> M3NodeLayout.isRightToLeft(getSkinnable()) ? 1 : -1;
            case UP -> 1;
            case DOWN -> -1;
            default -> 0;
        };
        if (direction != 0) {
            moveActiveSelection(direction);
            event.consume();
            return;
        }

        if (event.getCode() == KeyCode.HOME) {
            selectActiveBoundary(false);
            event.consume();
        } else if (event.getCode() == KeyCode.END) {
            selectActiveBoundary(true);
            event.consume();
        }
    }

    /// Moves the active hour or minute to the next selectable value.
    private void moveActiveSelection(int direction) {
        M3TimePicker control = getSkinnable();
        @Nullable LocalTime selectedTime = control.getValue();
        LocalTime base = selectedTime == null ? fallbackTime() : selectedTime;
        if (activeUnit == SelectionUnit.HOUR) {
            for (int offset = 1; offset <= 24; offset++) {
                LocalTime candidate = base.plusHours((long) direction * offset);
                if (!control.isTimeDisabled(candidate)) {
                    control.setValue(candidate);
                    return;
                }
            }
            return;
        }

        int step = control.getMinuteStep();
        int attempts = 60 / step;
        for (int offset = 1; offset <= attempts; offset++) {
            LocalTime candidate = base.plusMinutes((long) direction * offset * step);
            if (!control.isTimeDisabled(candidate)) {
                control.setValue(candidate);
                return;
            }
        }
    }

    /// Selects the first or last value for the active unit while respecting time bounds.
    private void selectActiveBoundary(boolean end) {
        M3TimePicker control = getSkinnable();
        @Nullable LocalTime selectedTime = control.getValue();
        LocalTime base = selectedTime == null ? fallbackTime() : selectedTime;
        if (activeUnit == SelectionUnit.HOUR) {
            int start = end ? 23 : 0;
            int direction = end ? -1 : 1;
            for (int hour = start; hour >= 0 && hour < 24; hour += direction) {
                LocalTime candidate = base.withHour(hour);
                if (!control.isTimeDisabled(candidate)) {
                    control.setValue(candidate);
                    return;
                }
            }
            return;
        }

        int step = control.getMinuteStep();
        int start = end ? 60 - step : 0;
        int direction = end ? -step : step;
        for (int minute = start; minute >= 0 && minute < 60; minute += direction) {
            LocalTime candidate = base.withMinute(minute);
            if (!control.isTimeDisabled(candidate)) {
                control.setValue(candidate);
                return;
            }
        }
    }

    /// Handles a pointer event targeted directly at the dial pane.
    private void handleDialMouseEvent(MouseEvent event) {
        handleDialPointer(event, event.getX(), event.getY());
    }

    /// Handles a pointer event targeted at a direct dial-label child without coordinate-object allocation.
    private void handleDialLabelMouseEvent(MouseEvent event) {
        if (!(event.getSource() instanceof DialLabelButton label)) {
            return;
        }
        handleDialPointer(event, label.getLayoutX() + event.getX(), label.getLayoutY() + event.getY());
    }

    /// Updates selection for a press, drag, or release inside the clock dial.
    private void handleDialPointer(MouseEvent event, double x, double y) {
        if (getSkinnable().isDisabled()) {
            return;
        }

        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            if (event.getButton() != MouseButton.PRIMARY) {
                return;
            }
            dial.dragging = true;
            M3FocusRequests.requestFocusIfTraversable(dial);
            selectFromDial(x, y);
            event.consume();
        } else if (event.getEventType() == MouseEvent.MOUSE_DRAGGED) {
            if (!dial.dragging) {
                return;
            }
            selectFromDial(x, y);
            event.consume();
        } else if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
            if (!dial.dragging || event.getButton() != MouseButton.PRIMARY) {
                return;
            }
            selectFromDial(x, y);
            dial.dragging = false;
            if (activeUnit == SelectionUnit.HOUR) {
                setActiveUnit(SelectionUnit.MINUTE);
            }
            event.consume();
        }
    }

    /// Converts a dial coordinate into a bounded hour or minute value.
    private void selectFromDial(double x, double y) {
        double centerX = dial.getWidth() / 2.0;
        double centerY = dial.getHeight() / 2.0;
        double deltaX = x - centerX;
        double deltaY = y - centerY;
        double angle = normalizeAngle(Math.atan2(deltaX, -deltaY));
        double distance = Math.hypot(deltaX, deltaY);

        M3TimePicker control = getSkinnable();
        @Nullable LocalTime selectedTime = control.getValue();
        LocalTime base = selectedTime == null ? fallbackTime() : selectedTime;
        @Nullable LocalTime candidate;
        if (activeUnit == SelectionUnit.HOUR) {
            int dialHour = Math.floorMod((int) Math.round(angle / FULL_ROTATION * 12.0), 12);
            if (control.isUse24HourClock()) {
                double outerRadius = Math.min(dial.getWidth(), dial.getHeight()) * OUTER_RADIUS_RATIO;
                double innerRadius = Math.min(dial.getWidth(), dial.getHeight()) * INNER_RADIUS_RATIO;
                boolean innerRing = distance < (outerRadius + innerRadius) / 2.0;
                int hour = dialHour + (innerRing ? 12 : 0);
                candidate = candidateForHour(hour, base);
            } else {
                int displayHour = dialHour == 0 ? 12 : dialHour;
                candidate = candidateForHour(
                        toActualHour(displayHour, base.getHour() >= 12),
                        base
                );
            }
        } else {
            int rawMinute = Math.floorMod((int) Math.round(angle / FULL_ROTATION * 60.0), 60);
            int snappedMinute = snapMinute(rawMinute, control.getMinuteStep());
            LocalTime minuteCandidate = base.withMinute(snappedMinute);
            candidate = control.isTimeDisabled(minuteCandidate) ? null : minuteCandidate;
        }

        if (candidate != null && !control.isTimeDisabled(candidate) && !candidate.equals(control.getValue())) {
            control.setValue(candidate);
        }
    }

    /// Selects the time represented by a keyboard-activated dial label or period button.
    private void handleTimeCellAction(ActionEvent event) {
        if (!(event.getSource() instanceof Node node) || !(node.getUserData() instanceof LocalTime time)) {
            return;
        }

        M3TimePicker control = getSkinnable();
        if (control.isTimeDisabled(time)) {
            return;
        }
        control.setValue(time);
        if (node instanceof DialLabelButton && activeUnit == SelectionUnit.HOUR) {
            setActiveUnit(SelectionUnit.MINUTE);
        }
    }

    /// Returns a selectable candidate for one hour, adjusting the minute only when bounds require it.
    private @Nullable LocalTime candidateForHour(int hour, LocalTime base) {
        M3TimePicker control = getSkinnable();
        LocalTime candidate = base.withHour(hour);
        if (!control.isTimeDisabled(candidate)) {
            return candidate;
        }

        int preferredMinute = base.getMinute();
        for (int distance = 0; distance < 60; distance++) {
            int lower = preferredMinute - distance;
            if (lower >= 0) {
                candidate = LocalTime.of(hour, lower);
                if (!control.isTimeDisabled(candidate)) {
                    return candidate;
                }
            }
            int upper = preferredMinute + distance;
            if (distance != 0 && upper < 60) {
                candidate = LocalTime.of(hour, upper);
                if (!control.isTimeDisabled(candidate)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    /// Returns a selectable candidate in the requested AM or PM period.
    private @Nullable LocalTime candidateForPeriod(boolean afternoon, LocalTime base) {
        int displayHour = toDisplayHour(base.getHour());
        int preferredHour = toActualHour(displayHour, afternoon);
        @Nullable LocalTime preferred = candidateForHour(preferredHour, base);
        if (preferred != null) {
            return preferred;
        }

        int firstHour = afternoon ? 12 : 0;
        int lastHour = afternoon ? 23 : 11;
        for (int hour = firstHour; hour <= lastHour; hour++) {
            @Nullable LocalTime candidate = candidateForHour(hour, base);
            if (candidate != null) {
                return candidate;
            }
        }
        return null;
    }

    /// Returns whether one hour contains at least one selectable minute.
    private boolean hourHasSelectableMinute(int hour) {
        M3TimePicker control = getSkinnable();
        for (int minute = 0; minute < 60; minute++) {
            if (!control.isTimeDisabled(LocalTime.of(hour, minute))) {
                return true;
            }
        }
        return false;
    }

    /// Returns a stable fallback time when no value is selected.
    private LocalTime fallbackTime() {
        M3TimePicker control = getSkinnable();
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);
        LocalTime snapped = now.withMinute(snapMinute(now.getMinute(), control.getMinuteStep()));
        if (!control.isTimeDisabled(snapped)) {
            return snapped;
        }

        @Nullable LocalTime min = control.getMinTime();
        if (min != null) {
            return min;
        }
        @Nullable LocalTime max = control.getMaxTime();
        if (max != null) {
            return max;
        }
        return LocalTime.MIDNIGHT;
    }

    /// Returns whether the AM or PM period contains any selectable hour.
    private boolean periodSelectable(boolean afternoon) {
        int start = afternoon ? 12 : 0;
        int end = afternoon ? 24 : 12;
        for (int hour = start; hour < end; hour++) {
            if (selectableHours[hour]) {
                return true;
            }
        }
        return false;
    }

    /// Creates one animated selector or period button.
    private TimeCellButton createTimeCell(String text, String roleStyleClass) {
        TimeCellButton cell = new TimeCellButton(text);
        cell.getStyleClass().add(roleStyleClass);
        return cell;
    }

    /// Creates one lightweight persistent dial label.
    private DialLabelButton createDialLabel() {
        DialLabelButton label = new DialLabelButton();
        label.setOnAction(this::handleTimeCellAction);
        return label;
    }

    /// Updates a dial label's text, candidate, selected state, and availability.
    private static void updateDialLabel(
            DialLabelButton label,
            String text,
            LocalTime candidate,
            boolean selected,
            boolean disabled
    ) {
        setTextIfChanged(label, text);
        if (!candidate.equals(label.getUserData())) {
            label.setUserData(candidate);
            label.setAccessibleText(candidate.toString());
        }
        label.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, selected);
        setSelectedStyleClass(label, selected);
        label.setDisable(disabled);
        setVisibleAndManaged(label, true);
    }

    /// Hides an unused member of the fixed 24-label pool.
    private static void hideDialLabel(DialLabelButton label) {
        label.setUserData(null);
        label.setDisable(true);
        label.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, false);
        setSelectedStyleClass(label, false);
        setVisibleAndManaged(label, false);
    }

    /// Updates the selected marker style class without creating duplicate entries.
    private static void setSelectedStyleClass(Node node, boolean selected) {
        List<String> styleClasses = node.getStyleClass();
        if (selected) {
            if (!styleClasses.contains(SELECTED_CELL_STYLE_CLASS)) {
                styleClasses.add(SELECTED_CELL_STYLE_CLASS);
            }
        } else {
            styleClasses.remove(SELECTED_CELL_STYLE_CLASS);
        }
    }

    /// Sets a node's visible and managed states together only when they change.
    private static void setVisibleAndManaged(Node node, boolean visible) {
        if (node.isVisible() != visible) {
            node.setVisible(visible);
        }
        if (node.isManaged() != visible) {
            node.setManaged(visible);
        }
    }

    /// Updates one labeled control only when its text changed.
    private static void setTextIfChanged(javafx.scene.control.Labeled labeled, String text) {
        if (!text.equals(labeled.getText())) {
            labeled.setText(text);
        }
    }

    /// Updates one text field only when its text changed.
    private static void setTextIfChanged(TextField field, String text) {
        if (!text.equals(field.getText())) {
            field.setText(text);
        }
    }

    /// Formats one hour according to 12-hour or 24-hour presentation.
    private static String formatHour(LocalTime time, boolean twentyFourHour) {
        return twentyFourHour ? formatTwoDigits(time.getHour()) : formatTwoDigits(toDisplayHour(time.getHour()));
    }

    /// Formats a non-negative value using two decimal digits.
    private static String formatTwoDigits(int value) {
        return value < 10 ? "0" + value : Integer.toString(value);
    }

    /// Converts a 24-hour value to a 12-hour display value.
    private static int toDisplayHour(int actualHour) {
        int displayHour = actualHour % 12;
        return displayHour == 0 ? 12 : displayHour;
    }

    /// Converts a 12-hour display value and period to a 24-hour value.
    private static int toActualHour(int displayHour, boolean afternoon) {
        int normalized = displayHour == 12 ? 0 : displayHour;
        return afternoon ? normalized + 12 : normalized;
    }

    /// Snaps a minute to the nearest configured step with circular wraparound.
    private static int snapMinute(int minute, int step) {
        int snapped = (int) Math.round((double) minute / step) * step;
        return Math.floorMod(snapped, 60);
    }

    /// Normalizes an angle into the half-open zero-to-full-rotation interval.
    private static double normalizeAngle(double angle) {
        double normalized = angle % FULL_ROTATION;
        return normalized < 0.0 ? normalized + FULL_ROTATION : normalized;
    }

    /// Returns the shortest signed angular delta between two normalized angles.
    private static double shortestAngleDelta(double start, double target) {
        double delta = normalizeAngle(target) - normalizeAngle(start);
        if (delta > Math.PI) {
            delta -= FULL_ROTATION;
        } else if (delta < -Math.PI) {
            delta += FULL_ROTATION;
        }
        return delta;
    }

    /// Returns a mirrored logical x position for the current node orientation.
    private double logicalX(double x, double childWidth, double contentWidth) {
        return M3NodeLayout.isRightToLeft(getSkinnable()) ? contentWidth - x - childWidth : x;
    }

    /// The hour or minute channel controlled by the current selector.
    @NotNullByDefault
    private enum SelectionUnit {
        /// The hour channel.
        HOUR,

        /// The minute channel.
        MINUTE
    }

    /// Stable layout pane for portrait, landscape, Dial, Input, and RTL configurations.
    @NotNullByDefault
    private final class PickerLayoutPane extends Pane {
        /// Whether the last completed dial layout was horizontal.
        private boolean landscape;

        /// Creates the stable picker layout parent.
        private PickerLayoutPane() {
            getStyleClass().add(CONTENT_STYLE_CLASS);
            setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            getChildren().setAll(display, periodSelector, dial, inputContent);
            M3IconButton standaloneModeButton = modeButton;
            if (standaloneModeButton != null) {
                getChildren().add(standaloneModeButton);
            }
        }

        /// Computes the minimum width of the portrait content.
        @Override
        protected double computeMinWidth(double height) {
            return PORTRAIT_CONTENT_WIDTH;
        }

        /// Computes the minimum height of the active picker variant.
        @Override
        protected double computeMinHeight(double width) {
            return preferredHeightForWidth(width);
        }

        /// Computes the preferred width of the default portrait content.
        @Override
        protected double computePrefWidth(double height) {
            return PORTRAIT_CONTENT_WIDTH;
        }

        /// Computes the preferred height from active child metrics and layout spacing.
        @Override
        protected double computePrefHeight(double width) {
            return preferredHeightForWidth(width);
        }

        /// Returns the active layout height for the supplied content width.
        private double preferredHeightForWidth(double width) {
            if (getSkinnable().isInputMode()) {
                return inputContentHeight();
            }
            return width >= LANDSCAPE_CONTENT_WIDTH ? landscapeDialHeight() : portraitDialHeight();
        }

        /// Returns the keyboard-input content height.
        private double inputContentHeight() {
            double rowHeight = Math.max(
                    inputContent.prefHeight(-1.0),
                    periodSelector.isManaged() ? periodSelector.prefHeight(-1.0) : 0.0
            );
            return rowHeight + modeButtonRowHeight();
        }

        /// Returns the vertical Dial content height.
        private double portraitDialHeight() {
            double displayWidth = display.prefWidth(-1.0);
            return display.prefHeight(displayWidth)
                    + getSkinnable().getContainerSpacing()
                    + dial.prefHeight(-1.0)
                    + modeButtonRowHeight();
        }

        /// Returns the horizontal Dial content height.
        private double landscapeDialHeight() {
            double displayWidth = display.prefWidth(-1.0);
            double displayColumnHeight = display.prefHeight(displayWidth);
            if (periodSelector.isManaged()) {
                displayColumnHeight += getSkinnable().getContainerSpacing()
                        + periodSelector.prefHeight(-1.0);
            }
            displayColumnHeight += modeButtonRowHeight();
            return Math.max(dial.prefHeight(-1.0), displayColumnHeight);
        }

        /// Returns the optional standalone mode-button row height including its preceding spacing.
        private double modeButtonRowHeight() {
            M3IconButton standaloneModeButton = modeButton;
            return standaloneModeButton == null
                    ? 0.0
                    : getSkinnable().getContainerSpacing() + standaloneModeButton.prefHeight(-1.0);
        }

        /// Positions retained children for the active variant, width class, and node orientation.
        @Override
        protected void layoutChildren() {
            if (getSkinnable().isInputMode()) {
                layoutInput();
            } else if (getWidth() >= LANDSCAPE_CONTENT_WIDTH) {
                layoutLandscapeDial();
            } else {
                layoutPortraitDial();
            }
        }

        /// Lays out the keyboard-input variant.
        private void layoutInput() {
            updateLandscapeState(false);
            double contentWidth = PORTRAIT_CONTENT_WIDTH;
            double contentHeight = inputContentHeight();
            double offsetX = Math.max(0.0, (getWidth() - contentWidth) / 2.0);
            double offsetY = Math.max(0.0, (getHeight() - contentHeight) / 2.0);
            double inputWidth = inputContent.prefWidth(-1.0);
            double periodWidth = periodSelector.prefWidth(-1.0);
            double rowHeight = Math.max(
                    inputContent.prefHeight(-1.0),
                    periodSelector.isManaged() ? periodSelector.prefHeight(-1.0) : 0.0
            );

            inputContent.resizeRelocate(
                    offsetX + logicalX(0.0, inputWidth, contentWidth),
                    offsetY,
                    inputWidth,
                    inputContent.prefHeight(inputWidth)
            );
            if (periodSelector.isManaged()) {
                periodSelector.resizeRelocate(
                        offsetX + logicalX(contentWidth - periodWidth, periodWidth, contentWidth),
                        offsetY,
                        periodWidth,
                        periodSelector.prefHeight(periodWidth)
                );
            }
            layoutModeButton(
                    offsetX,
                    offsetY + rowHeight + getSkinnable().getContainerSpacing(),
                    contentWidth
            );
        }

        /// Lays out the default vertical dial variant.
        private void layoutPortraitDial() {
            updateLandscapeState(false);
            double contentWidth = PORTRAIT_CONTENT_WIDTH;
            double contentHeight = portraitDialHeight();
            double offsetX = Math.max(0.0, (getWidth() - contentWidth) / 2.0);
            double offsetY = Math.max(0.0, (getHeight() - contentHeight) / 2.0);
            double displayWidth = display.prefWidth(-1.0);
            double displayHeight = display.prefHeight(displayWidth);
            double periodWidth = periodSelector.prefWidth(-1.0);
            double dialSize = dial.prefWidth(-1.0);
            double spacing = getSkinnable().getContainerSpacing();

            double displayX = getSkinnable().isUse24HourClock()
                    ? (contentWidth - displayWidth) / 2.0
                    : 0.0;
            display.resizeRelocate(
                    offsetX + logicalX(displayX, displayWidth, contentWidth),
                    offsetY,
                    displayWidth,
                    displayHeight
            );
            if (periodSelector.isManaged()) {
                periodSelector.resizeRelocate(
                        offsetX + logicalX(contentWidth - periodWidth, periodWidth, contentWidth),
                        offsetY,
                        periodWidth,
                        periodSelector.prefHeight(periodWidth)
                );
            }
            dial.resizeRelocate(
                    offsetX + (contentWidth - dialSize) / 2.0,
                    offsetY + displayHeight + spacing,
                    dialSize,
                    dialSize
            );
            layoutModeButton(
                    offsetX,
                    offsetY + displayHeight + spacing + dialSize + spacing,
                    contentWidth
            );
        }

        /// Lays out the official horizontal dial arrangement.
        private void layoutLandscapeDial() {
            updateLandscapeState(true);
            double contentWidth = LANDSCAPE_CONTENT_WIDTH;
            double contentHeight = landscapeDialHeight();
            double offsetX = Math.max(0.0, (getWidth() - contentWidth) / 2.0);
            double offsetY = Math.max(0.0, (getHeight() - contentHeight) / 2.0);
            double displayWidth = display.prefWidth(-1.0);
            double displayHeight = display.prefHeight(displayWidth);
            double dialSize = dial.prefWidth(-1.0);
            double spacing = getSkinnable().getContainerSpacing();
            M3IconButton standaloneModeButton = modeButton;
            double modeButtonHeight = standaloneModeButton == null ? 0.0 : standaloneModeButton.prefHeight(-1.0);

            display.resizeRelocate(
                    offsetX + logicalX(0.0, displayWidth, contentWidth),
                    offsetY,
                    displayWidth,
                    displayHeight
            );
            if (periodSelector.isManaged()) {
                double periodWidth = periodSelector.prefWidth(-1.0);
                periodSelector.resizeRelocate(
                        offsetX + logicalX(0.0, periodWidth, contentWidth),
                        offsetY + displayHeight + spacing,
                        periodWidth,
                        periodSelector.prefHeight(periodWidth)
                );
            }
            dial.resizeRelocate(
                    offsetX + logicalX(contentWidth - dialSize, dialSize, contentWidth),
                    offsetY,
                    dialSize,
                    dialSize
            );
            layoutModeButton(
                    offsetX,
                    offsetY + contentHeight - modeButtonHeight,
                    contentWidth
            );
        }

        /// Positions the mode button at logical start.
        private void layoutModeButton(double offsetX, double y, double contentWidth) {
            M3IconButton standaloneModeButton = modeButton;
            if (standaloneModeButton == null) {
                return;
            }
            double buttonWidth = standaloneModeButton.prefWidth(-1.0);
            double buttonHeight = standaloneModeButton.prefHeight(buttonWidth);
            standaloneModeButton.resizeRelocate(
                    offsetX + logicalX(0.0, buttonWidth, contentWidth),
                    y,
                    buttonWidth,
                    buttonHeight
            );
        }

        /// Updates landscape pseudo-classes and period-selector orientation only when changed.
        private void updateLandscapeState(boolean landscape) {
            if (this.landscape == landscape && initialized) {
                return;
            }
            this.landscape = landscape;
            M3TimePicker control = getSkinnable();
            control.pseudoClassStateChanged(LANDSCAPE_PSEUDO_CLASS, landscape);
            control.pseudoClassStateChanged(PORTRAIT_PSEUDO_CLASS, !landscape);
            periodSelector.setHorizontal(landscape && !control.isInputMode());
        }
    }

    /// Fixed-node clock dial with primitive selector geometry.
    @NotNullByDefault
    private final class DialPane extends Pane {
        /// The dial background circle.
        private final Circle background = new Circle();

        /// The selector track from the dial center to the handle.
        private final Line selectorTrack = new Line();

        /// The selector handle behind the selected label.
        private final Circle selectorHandle = new Circle();

        /// The selector center dot.
        private final Circle selectorCenter = new Circle();

        /// The fixed maximum label pool.
        private final DialLabelButton[] labels = new DialLabelButton[24];

        /// The normalized angle assigned to each active label.
        private final double[] labelAngles = new double[24];

        /// The radius ratio assigned to each active label.
        private final double[] labelRadiusRatios = new double[24];

        /// The number of visible labels.
        private int activeLabelCount;

        /// The currently rendered selector angle.
        private double selectorAngle;

        /// The currently rendered selector radius ratio.
        private double selectorRadiusRatio = OUTER_RADIUS_RATIO;

        /// Whether selector geometry should be rendered.
        private boolean selectorVisible;

        /// Whether a primary pointer drag currently owns selector updates.
        private boolean dragging;

        /// Creates the fixed dial node tree.
        private DialPane() {
            getStyleClass().add(DIAL_STYLE_CLASS);
            setAccessibleRole(AccessibleRole.PARENT);
            setAccessibleText("Clock dial");
            setFocusTraversable(true);
            setPickOnBounds(true);
            setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            focusedProperty().addListener(observable -> {
                if (!isFocused()) {
                    dragging = false;
                }
            });
            sceneProperty().addListener(observable -> {
                if (getScene() == null) {
                    dragging = false;
                }
            });

            background.getStyleClass().add(DIAL_BACKGROUND_STYLE_CLASS);
            selectorTrack.getStyleClass().add(DIAL_TRACK_STYLE_CLASS);
            selectorHandle.getStyleClass().add(DIAL_HANDLE_STYLE_CLASS);
            selectorCenter.getStyleClass().add(DIAL_CENTER_STYLE_CLASS);
            background.setMouseTransparent(true);
            selectorTrack.setMouseTransparent(true);
            selectorHandle.setMouseTransparent(true);
            selectorCenter.setMouseTransparent(true);

            getChildren().addAll(background, selectorTrack, selectorHandle);
            for (int index = 0; index < labels.length; index++) {
                DialLabelButton label = createDialLabel();
                labels[index] = label;
                getChildren().add(label);
            }
            getChildren().add(selectorCenter);
        }

        /// Computes the official minimum dial width.
        @Override
        protected double computeMinWidth(double height) {
            return DIAL_SIZE;
        }

        /// Computes the official minimum dial height.
        @Override
        protected double computeMinHeight(double width) {
            return DIAL_SIZE;
        }

        /// Computes the official preferred dial width.
        @Override
        protected double computePrefWidth(double height) {
            return DIAL_SIZE;
        }

        /// Computes the official preferred dial height.
        @Override
        protected double computePrefHeight(double width) {
            return DIAL_SIZE;
        }

        /// Reconfigures labels and selector for the current unit and value.
        private void refresh(LocalTime baseTime, @Nullable LocalTime selectedTime, boolean animateSelector) {
            if (activeUnit == SelectionUnit.HOUR) {
                refreshHourLabels(baseTime, selectedTime);
            } else {
                refreshMinuteLabels(baseTime, selectedTime);
            }
            refreshSelector(selectedTime, animateSelector);
            requestLayout();
        }

        /// Configures 12-hour or 24-hour dial labels.
        private void refreshHourLabels(LocalTime baseTime, @Nullable LocalTime selectedTime) {
            M3TimePicker control = getSkinnable();
            boolean twentyFourHour = control.isUse24HourClock();
            activeLabelCount = twentyFourHour ? 24 : 12;
            boolean afternoon = baseTime.getHour() >= 12;

            for (int index = 0; index < activeLabelCount; index++) {
                int hour;
                String text;
                double radiusRatio;
                if (twentyFourHour) {
                    hour = index;
                    text = formatTwoDigits(hour);
                    radiusRatio = hour < 12 ? OUTER_RADIUS_RATIO : INNER_RADIUS_RATIO;
                } else {
                    int displayHour = index == 0 ? 12 : index;
                    hour = toActualHour(displayHour, afternoon);
                    text = Integer.toString(displayHour);
                    radiusRatio = OUTER_RADIUS_RATIO;
                }

                LocalTime candidate = baseTime.withHour(hour);
                labelAngles[index] = (hour % 12) / 12.0 * FULL_ROTATION;
                labelRadiusRatios[index] = radiusRatio;
                updateDialLabel(
                        labels[index],
                        text,
                        candidate,
                        selectedTime != null && selectedTime.getHour() == hour,
                        !selectableHours[hour]
                );
            }
            hideUnusedLabels();
        }

        /// Configures the twelve five-minute guide labels.
        private void refreshMinuteLabels(LocalTime baseTime, @Nullable LocalTime selectedTime) {
            M3TimePicker control = getSkinnable();
            activeLabelCount = 12;
            int step = control.getMinuteStep();
            for (int index = 0; index < activeLabelCount; index++) {
                int minute = index * 5;
                LocalTime candidate = baseTime.withMinute(minute);
                labelAngles[index] = index / 12.0 * FULL_ROTATION;
                labelRadiusRatios[index] = OUTER_RADIUS_RATIO;
                updateDialLabel(
                        labels[index],
                        formatTwoDigits(minute),
                        candidate,
                        selectedTime != null && selectedTime.getMinute() == minute,
                        minute % step != 0 || control.isTimeDisabled(candidate)
                );
            }
            hideUnusedLabels();
        }

        /// Hides labels beyond the active label count.
        private void hideUnusedLabels() {
            for (int index = activeLabelCount; index < labels.length; index++) {
                hideDialLabel(labels[index]);
            }
        }

        /// Moves or hides the selector according to the selected time.
        private void refreshSelector(@Nullable LocalTime selectedTime, boolean animateSelector) {
            if (selectedTime == null) {
                selectorTransition.stop();
                selectorVisible = false;
                requestLayout();
                return;
            }

            double targetAngle;
            double targetRadius;
            if (activeUnit == SelectionUnit.HOUR) {
                targetAngle = (selectedTime.getHour() % 12) / 12.0 * FULL_ROTATION;
                targetRadius = getSkinnable().isUse24HourClock() && selectedTime.getHour() >= 12
                        ? INNER_RADIUS_RATIO
                        : OUTER_RADIUS_RATIO;
            } else {
                targetAngle = selectedTime.getMinute() / 60.0 * FULL_ROTATION;
                targetRadius = OUTER_RADIUS_RATIO;
            }

            boolean wasVisible = selectorVisible;
            selectorVisible = true;
            if (!wasVisible || !animateSelector) {
                selectorTransition.stop();
                setSelectorGeometry(targetAngle, targetRadius);
                return;
            }

            selectorTransition.configure(M3Animation.fastSpatial(getSkinnable()), targetAngle, targetRadius);
            M3Animation.playFromStart(getSkinnable(), selectorTransition);
        }

        /// Stores primitive selector geometry and schedules one layout pulse.
        private void setSelectorGeometry(double angle, double radiusRatio) {
            selectorAngle = normalizeAngle(angle);
            selectorRadiusRatio = radiusRatio;
            requestLayout();
        }

        /// Positions dial shapes, fixed labels, and selector geometry.
        @Override
        protected void layoutChildren() {
            double size = Math.min(getWidth(), getHeight());
            double centerX = getWidth() / 2.0;
            double centerY = getHeight() / 2.0;
            double radius = size / 2.0;

            background.setCenterX(centerX);
            background.setCenterY(centerY);
            background.setRadius(radius);

            double handleX = centerX + Math.sin(selectorAngle) * size * selectorRadiusRatio;
            double handleY = centerY - Math.cos(selectorAngle) * size * selectorRadiusRatio;
            selectorTrack.setStartX(centerX);
            selectorTrack.setStartY(centerY);
            selectorTrack.setEndX(handleX);
            selectorTrack.setEndY(handleY);
            selectorHandle.setCenterX(handleX);
            selectorHandle.setCenterY(handleY);
            selectorHandle.setRadius(Math.min(size, getSkinnable().getDialHandleSize()) / 2.0);
            selectorCenter.setCenterX(centerX);
            selectorCenter.setCenterY(centerY);
            selectorCenter.setRadius(Math.min(size, getSkinnable().getDialCenterSize()) / 2.0);

            selectorTrack.setVisible(selectorVisible);
            selectorHandle.setVisible(selectorVisible);
            selectorCenter.setVisible(selectorVisible);

            for (int index = 0; index < activeLabelCount; index++) {
                double angle = labelAngles[index];
                double labelRadius = size * labelRadiusRatios[index];
                double labelX = centerX + Math.sin(angle) * labelRadius - DIAL_LABEL_SIZE / 2.0;
                double labelY = centerY - Math.cos(angle) * labelRadius - DIAL_LABEL_SIZE / 2.0;
                labels[index].resizeRelocate(
                        Math.rint(labelX),
                        Math.rint(labelY),
                        DIAL_LABEL_SIZE,
                        DIAL_LABEL_SIZE
                );
            }
        }
    }

    /// Reusable transition for dial angle and radius interpolation.
    @NotNullByDefault
    private final class DialSelectorTransition extends M3FiniteTransition {
        /// The angle at the beginning of the current transition.
        private double startAngle;

        /// The shortest signed angular change for the current transition.
        private double angleDelta;

        /// The radius ratio at the beginning of the current transition.
        private double startRadiusRatio;

        /// The radius ratio at the end of the current transition.
        private double targetRadiusRatio;

        /// Configures a selector transition from the currently rendered primitive geometry.
        private void configure(M3MotionSpec spec, double targetAngle, double targetRadiusRatio) {
            stop();
            setCycleDuration(spec.duration());
            setInterpolator(spec.interpolator());
            startAngle = dial.selectorAngle;
            angleDelta = shortestAngleDelta(startAngle, targetAngle);
            startRadiusRatio = dial.selectorRadiusRatio;
            this.targetRadiusRatio = targetRadiusRatio;
        }

        /// Applies eased selector geometry without allocating per animation pulse.
        @Override
        protected void interpolate(double fraction) {
            dial.setSelectorGeometry(
                    startAngle + angleDelta * fraction,
                    startRadiusRatio + (targetRadiusRatio - startRadiusRatio) * fraction
            );
        }
    }

    /// Stable two-button AM/PM selector that supports vertical and horizontal geometry.
    @NotNullByDefault
    private final class PeriodSelectorPane extends Pane {
        /// The AM selector button.
        private final TimeCellButton amButton = createTimeCell("AM", PERIOD_CELL_STYLE_CLASS);

        /// The PM selector button.
        private final TimeCellButton pmButton = createTimeCell("PM", PERIOD_CELL_STYLE_CLASS);

        /// Whether the selector uses horizontal landscape geometry.
        private boolean horizontal;

        /// Whether the selector is displayed next to keyboard inputs.
        private boolean inputMode;

        /// Creates the persistent period selector.
        private PeriodSelectorPane() {
            getStyleClass().add(PERIOD_ROW_STYLE_CLASS);
            setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
            amButton.getStyleClass().add("m3-time-picker-period-start");
            pmButton.getStyleClass().add("m3-time-picker-period-end");
            amButton.setAccessibleText("AM");
            pmButton.setAccessibleText("PM");
            amButton.setOnAction(M3TimePickerSkin.this::handleTimeCellAction);
            pmButton.setOnAction(M3TimePickerSkin.this::handleTimeCellAction);
            getChildren().setAll(amButton, pmButton);
            pseudoClassStateChanged(VERTICAL_PSEUDO_CLASS, true);
            pseudoClassStateChanged(DIAL_PSEUDO_CLASS, true);
        }

        /// Releases action handlers retained by the two persistent buttons.
        private void dispose() {
            amButton.setOnAction(null);
            pmButton.setOnAction(null);
        }

        /// Updates selected period, represented times, and availability.
        private void refresh(LocalTime baseTime, @Nullable LocalTime selectedTime) {
            if (getSkinnable().isUse24HourClock()) {
                return;
            }

            boolean afternoon = selectedTime == null
                    ? baseTime.getHour() >= 12
                    : selectedTime.getHour() >= 12;
            @Nullable LocalTime amCandidate = candidateForPeriod(false, baseTime);
            @Nullable LocalTime pmCandidate = candidateForPeriod(true, baseTime);
            updatePeriodButton(amButton, amCandidate, !afternoon, !periodSelectable(false));
            updatePeriodButton(pmButton, pmCandidate, afternoon, !periodSelectable(true));
        }

        /// Updates a period button without replacing the node.
        private void updatePeriodButton(
                TimeCellButton button,
                @Nullable LocalTime candidate,
                boolean selected,
                boolean disabled
        ) {
            button.setUserData(candidate);
            button.pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, selected);
            setSelectedStyleClass(button, selected);
            button.setDisable(disabled || candidate == null);
        }

        /// Updates visibility for 12-hour and 24-hour formats.
        private void setClockFormat(boolean twentyFourHour) {
            setVisibleAndManaged(this, !twentyFourHour);
        }

        /// Updates the geometry role used by Dial and Input variants.
        private void setInputMode(boolean inputMode) {
            if (this.inputMode == inputMode) {
                return;
            }
            this.inputMode = inputMode;
            pseudoClassStateChanged(INPUT_PSEUDO_CLASS, inputMode);
            pseudoClassStateChanged(DIAL_PSEUDO_CLASS, !inputMode);
            requestLayout();
        }

        /// Updates the period selector's landscape geometry.
        private void setHorizontal(boolean horizontal) {
            if (this.horizontal == horizontal) {
                return;
            }
            this.horizontal = horizontal;
            pseudoClassStateChanged(HORIZONTAL_PSEUDO_CLASS, horizontal);
            pseudoClassStateChanged(VERTICAL_PSEUDO_CLASS, !horizontal);
            requestLayout();
        }

        /// Computes the width defined by the active period-selector token.
        @Override
        protected double computePrefWidth(double height) {
            return horizontal ? 216.0 : 52.0;
        }

        /// Computes the height defined by Dial or Input period-selector tokens.
        @Override
        protected double computePrefHeight(double width) {
            if (horizontal) {
                return 38.0;
            }
            return inputMode ? 72.0 : 80.0;
        }

        /// Positions the two period buttons without overlap or doubled outer dimensions.
        @Override
        protected void layoutChildren() {
            if (horizontal) {
                double halfWidth = getWidth() / 2.0;
                amButton.resizeRelocate(0.0, 0.0, halfWidth, getHeight());
                pmButton.resizeRelocate(halfWidth, 0.0, getWidth() - halfWidth, getHeight());
            } else {
                double halfHeight = getHeight() / 2.0;
                amButton.resizeRelocate(0.0, 0.0, getWidth(), halfHeight);
                pmButton.resizeRelocate(0.0, halfHeight, getWidth(), getHeight() - halfHeight);
            }
        }
    }

    /// Internal animated button used by selectors and AM/PM controls.
    @NotNullByDefault
    private static final class TimeCellButton extends ButtonBase {
        /// Creates one animated time selector button.
        private TimeCellButton(String text) {
            super(text);
            getStyleClass().add(CELL_STYLE_CLASS);
            setAccessibleRole(AccessibleRole.BUTTON);
            setAlignment(Pos.CENTER);
            setFocusTraversable(true);
            setMnemonicParsing(false);
            setTextOverrun(OverrunStyle.CLIP);
        }

        /// Fires the configured action while enabled.
        @Override
        public void fire() {
            if (!isDisabled()) {
                fireEvent(new ActionEvent(this, this));
            }
        }

        /// Creates the shared Material state-layer and ripple skin.
        @Override
        protected Skin<?> createDefaultSkin() {
            return new TimeCellSkin(this);
        }

        /// Returns the time picker user-agent stylesheet.
        @Override
        public String getUserAgentStylesheet() {
            return M3Stylesheets.controlStylesheet("time-picker.css");
        }
    }

    /// Animated labeled skin for selector and period buttons.
    @NotNullByDefault
    private static final class TimeCellSkin extends M3LabeledButtonSkinBase<TimeCellButton>
            implements ChangeListener<Paint> {
        /// Creates one selector button skin.
        private TimeCellSkin(TimeCellButton control) {
            super(control);
            control.textFillProperty().addListener(this);
            setStateLayerPaint(control.getTextFill());
        }

        /// Updates state-layer paint after CSS resolves selected and unselected content colors.
        @Override
        public void changed(ObservableValue<? extends Paint> observable, Paint oldPaint, Paint newPaint) {
            setStateLayerPaint(newPaint);
        }

        /// Removes the text-fill listener before disposing the shared button skin.
        @Override
        public void dispose() {
            getSkinnable().textFillProperty().removeListener(this);
            super.dispose();
        }
    }

    /// Lightweight button used by persistent dial labels.
    @NotNullByDefault
    private static final class DialLabelButton extends ButtonBase {
        /// Whether Space currently owns keyboard activation.
        private boolean spacePressed;

        /// Creates a lightweight dial label.
        private DialLabelButton() {
            super("");
            getStyleClass().addAll(CELL_STYLE_CLASS, DIAL_LABEL_STYLE_CLASS);
            setAccessibleRole(AccessibleRole.BUTTON);
            setAlignment(Pos.CENTER);
            setFocusTraversable(true);
            setMnemonicParsing(false);
            setTextOverrun(OverrunStyle.CLIP);
            addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
            addEventHandler(KeyEvent.KEY_RELEASED, this::handleKeyReleased);
            focusedProperty().addListener(observable -> {
                if (!isFocused()) {
                    spacePressed = false;
                }
            });
            disabledProperty().addListener(observable -> {
                if (isDisabled()) {
                    spacePressed = false;
                }
            });
        }

        /// Fires the configured label action while enabled.
        @Override
        public void fire() {
            if (!isDisabled()) {
                fireEvent(new ActionEvent(this, this));
            }
        }

        /// Creates the allocation-light labeled skin.
        @Override
        protected Skin<?> createDefaultSkin() {
            return new DialLabelSkin(this);
        }

        /// Returns the time picker user-agent stylesheet.
        @Override
        public String getUserAgentStylesheet() {
            return M3Stylesheets.controlStylesheet("time-picker.css");
        }

        /// Handles Enter and the start of Space activation.
        private void handleKeyPressed(KeyEvent event) {
            if (isDisabled()) {
                return;
            }
            if (event.getCode() == KeyCode.ENTER) {
                fire();
                event.consume();
            } else if (event.getCode() == KeyCode.SPACE) {
                spacePressed = true;
                event.consume();
            }
        }

        /// Fires a pending Space activation on release.
        private void handleKeyReleased(KeyEvent event) {
            if (event.getCode() != KeyCode.SPACE || !spacePressed) {
                return;
            }
            spacePressed = false;
            fire();
            event.consume();
        }
    }

    /// Basic labeled skin for dial labels, without one state-layer object per label.
    @NotNullByDefault
    private static final class DialLabelSkin extends LabeledSkinBase<DialLabelButton> {
        /// Creates one lightweight dial-label skin.
        private DialLabelSkin(DialLabelButton control) {
            super(control);
        }
    }
}
