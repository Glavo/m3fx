// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.animation.Animation;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import org.glavo.m3fx.internal.M3PresetNavigation;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.internal.M3InternalIcon;
import org.glavo.m3fx.internal.M3NodeTransition;
import org.glavo.m3fx.internal.M3PopupContextSynchronizer;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.M3ReachabilityObserver;
import org.glavo.m3fx.skins.M3DateRangePickerFieldSkin;
import org.glavo.m3fx.internal.M3PopupPositioning;
import org.glavo.m3fx.internal.M3PopupWindows;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 date range picker field with editable start and end date inputs.
///
/// The field maintains nullable start and end values and their independently editable text. A start without an end
/// is an in-progress range; an end without a start is not permitted. [#selectionProperty()] publishes one immutable
/// snapshot after each valid complete field mutation, so observers do not need to combine two endpoint notifications.
/// Raw editor text does not replace the selected endpoints until [commitEditorText()] succeeds. A failed commit
/// preserves the previous range and publishes validator-produced errors independently of application-supplied
/// start and end error text. As an [M3FormInput], the complete range field can be registered directly with
/// [M3FormValidator] and presented by [M3ValidationSummary].
///
/// The owned [picker][#getPicker()] is synchronized with committed endpoints. Completing a range in the calendar
/// updates both editors and closes the popup. The popup can be opened from either trailing button, Down, or F4 and
/// dismissed with Escape. It inherits the field's theme, stylesheets, and node orientation while showing.
///
/// ```java
/// private M3DateRangePickerField createRangeField() {
///     LocalDate today = LocalDate.now();
///     M3DateRangePickerField field = new M3DateRangePickerField();
///     field.setStartLabelText("Check-in");
///     field.setEndLabelText("Check-out");
///     field.getPicker().setMinDate(today);
///     field.getPicker().setMaxDate(today.plusYears(1));
///     field.getPresets().add(M3DateRangePresets.nextDays(today, 7));
///     return field;
/// }
/// ```
///
/// See [Material Design date pickers](https://m3.material.io/components/date-pickers/overview).
@NotNullByDefault
public final class M3DateRangePickerField extends javafx.scene.control.Control implements M3FormInput {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-date-range-picker-field";

    /// The style class applied to date range picker field popup surfaces.
    private static final String POPUP_STYLE_CLASS = "m3-date-range-picker-field-popup";

    /// The style class applied to popup content when preset actions are visible.
    private static final String PRESET_CONTENT_STYLE_CLASS = "m3-date-range-picker-field-preset-content";

    /// The style class applied to the popup preset action column.
    private static final String PRESET_LIST_STYLE_CLASS = "m3-date-range-picker-field-preset-list";

    /// The style class applied to each popup preset action button.
    private static final String PRESET_BUTTON_STYLE_CLASS = "m3-date-range-picker-field-preset-button";

    /// The style class applied to the trailing picker open buttons.
    private static final String OPEN_BUTTON_STYLE_CLASS = M3PickerField.OPEN_BUTTON_STYLE_CLASS;

    /// The vertical gap between the field and popup picker.
    private static final double POPUP_OFFSET_Y = 8.0;

    /// The initial popup picker scale used for enter and exit motion.
    private static final double POPUP_TRANSITION_SCALE = 0.96;

    /// The initial popup picker offset used for enter and exit motion.
    private static final double POPUP_TRANSITION_OFFSET_Y = 6.0;

    /// Creates an empty range field using ISO local-date text, default labels, and an unbounded picker.
    public M3DateRangePickerField() {
        this(null, null);
    }

    /// Creates a range field initialized with the specified endpoints.
    ///
    /// @param startDate the first selected date, or `null` for no selected range
    /// @param endDate   the last selected date, or `null` for an incomplete range
    /// @throws IllegalArgumentException if `endDate` is non-null while `startDate` is null or if `startDate` is
    ///         after `endDate`
    public M3DateRangePickerField(@Nullable LocalDate startDate, @Nullable LocalDate endDate) {
        formatter.set(DateTimeFormatter.ISO_LOCAL_DATE);
        invalidTextErrorText.set("Enter a valid date");
        rangeErrorText.set("Date range is outside the selectable range");
        initialize();
        setRange(startDate, endDate);
    }

    /// The selected start date, or `null` when the range is empty.
    ///
    /// The default value is `null`. Direct assignments are validated against the picker's inclusive bounds and
    /// current end date. Clearing the start also clears a non-null, unbound end and updates both editors. A
    /// unidirectional binding makes both endpoint editors, popup selection, presets, and value-oriented accessibility
    /// actions read-only. A bound range that violates ordering or selectable bounds remains observable from the
    /// endpoint properties while the editors and picker retain their last valid presentation and generated
    /// validation reports the violation.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable LocalDate> startDate =
            new SimpleObjectProperty<>(this, "startDate") {
                /// Installs a unidirectional start-date binding and makes field interactions read-only.
                @Override
                public void bind(ObservableValue<? extends @Nullable LocalDate> observable) {
                    super.bind(Objects.requireNonNull(observable, "observable"));
                    updateBoundState();
                }

                /// Removes a unidirectional start-date binding and restores a valid writable range state.
                @Override
                public void unbind() {
                    boolean wasBound = isBound();
                    super.unbind();
                    if (wasBound) {
                        updateBoundState();
                    }
                }

                /// Validates direct property writes before applying them.
                @Override
                public void set(@Nullable LocalDate newValue) {
                    beginRangeMutation();
                    try {
                        if (!applyingRange) {
                            if (newValue == null && getEndDate() != null && endDate.isBound()) {
                                throw new RuntimeException("cannot clear startDate while endDate is bound and non-null");
                            }
                            validateDate(newValue);
                            if (newValue != null) {
                                validateDateRange(newValue, getEndDate());
                            }
                        }
                        super.set(newValue);
                    } finally {
                        endRangeMutation();
                    }
                }

                /// Synchronizes editors and popup selection after the start date changes.
                @Override
                protected void invalidated() {
                    if (!applyingRange && isBound() && !isFieldRangeValid()) {
                        handleInvalidBoundRange();
                        return;
                    }
                    if (!applyingRange && get() == null && getEndDate() != null) {
                        endDate.set(null);
                    }
                    markRangeChanged();
                }
            };

    /// Returns the selected start date, or `null` when the range is empty.
    ///
    /// @return the selected start date, or `null` when the range is empty
    public @Nullable LocalDate getStartDate() {
        return startDate.get();
    }

    /// Sets the selected start date, or clears the complete range when `null` is supplied.
    ///
    /// @param startDate the selected start date, or `null` to clear the range
    /// @throws IllegalArgumentException if `startDate` is outside the current picker bounds or after the current end
    /// @throws RuntimeException         if the start-date property is bound, or if `startDate` is `null` while a
    ///         non-null end-date property is bound
    public void setStartDate(@Nullable LocalDate startDate) {
        this.startDate.set(startDate);
    }

    /// Returns the observable property that stores the selected range start.
    ///
    /// The property can be observed and bound, and its default value is `null`. Direct assignments are validated
    /// against the picker bounds and current end; valid changes synchronize the editors and popup picker. A bound
    /// source value is valid only when it remains within the picker bounds, does not move after the current end, and
    /// does not become `null` while the end is non-null. Invalid bound values remain observable but activate generated
    /// validation and do not replace the last valid editor or picker presentation. When both endpoints are bound,
    /// update sources in an order that preserves a valid range when intermediate validation errors are undesirable.
    /// Removing the final endpoint binding restores the last valid range if the retained bound value is invalid.
    ///
    /// @return the selected start-date property
    public ObjectProperty<@Nullable LocalDate> startDateProperty() {
        return startDate;
    }

    /// The selected end date, or `null` while the range is empty or incomplete.
    ///
    /// The default value is `null`. A non-null end requires a non-null start, must not precede it, and must be within
    /// the picker's inclusive bounds. Direct assignments update both editors. A unidirectional binding makes the
    /// complete field read-only to user range changes; invalid bound values retain the last valid presentation and
    /// activate generated validation.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable LocalDate> endDate =
            new SimpleObjectProperty<>(this, "endDate") {
                /// Installs a unidirectional end-date binding and makes field interactions read-only.
                @Override
                public void bind(ObservableValue<? extends @Nullable LocalDate> observable) {
                    super.bind(Objects.requireNonNull(observable, "observable"));
                    updateBoundState();
                }

                /// Removes a unidirectional end-date binding and restores a valid writable range state.
                @Override
                public void unbind() {
                    boolean wasBound = isBound();
                    super.unbind();
                    if (wasBound) {
                        updateBoundState();
                    }
                }

                /// Validates direct property writes before applying them.
                @Override
                public void set(@Nullable LocalDate newValue) {
                    beginRangeMutation();
                    try {
                        if (!applyingRange) {
                            if (newValue != null && getStartDate() == null) {
                                throw new IllegalArgumentException("startDate must be selected before endDate");
                            }
                            validateDate(newValue);
                            validateDateRange(getStartDate(), newValue);
                        }
                        super.set(newValue);
                    } finally {
                        endRangeMutation();
                    }
                }

                /// Synchronizes editors and popup selection after the end date changes.
                @Override
                protected void invalidated() {
                    if (!applyingRange && isBound() && !isFieldRangeValid()) {
                        handleInvalidBoundRange();
                        return;
                    }
                    markRangeChanged();
                }
            };

    /// Returns the selected end date, or `null` while only a start date is selected.
    ///
    /// @return the selected end date, or `null` while only a start date is selected
    public @Nullable LocalDate getEndDate() {
        return endDate.get();
    }

    /// Sets the selected end date, or clears the range end when `null` is supplied.
    ///
    /// @param endDate the selected end date, or `null` to clear the range end
    /// @throws IllegalArgumentException if `endDate` is outside the current picker bounds, no start is selected, or
    ///         it precedes the current start
    /// @throws RuntimeException if the end-date property is bound
    public void setEndDate(@Nullable LocalDate endDate) {
        this.endDate.set(endDate);
    }

    /// Returns the observable property that stores the selected range end.
    ///
    /// The property can be observed and bound, and its default value is `null`. Direct non-null assignments require
    /// a selected start, must not precede it, and must satisfy the picker bounds. A bound source value is valid under
    /// the same conditions; violations remain observable but are not presented as selected values. When both
    /// endpoints are bound, update sources in an order that preserves a valid range when intermediate validation
    /// errors are undesirable. Removing the final endpoint binding restores the last valid range if the retained
    /// bound value is invalid.
    ///
    /// @return the selected end-date property
    public ObjectProperty<@Nullable LocalDate> endDateProperty() {
        return endDate;
    }

    /// The formatter used to parse and format both date editors.
    ///
    /// Changing the formatter rewrites both editor texts from the committed endpoints. `null` is not permitted.
    ///
    /// @defaultValue [DateTimeFormatter#ISO_LOCAL_DATE]
    private final ObjectProperty<DateTimeFormatter> formatter =
            new SimpleObjectProperty<>(this, "formatter") {
                /// Keeps formatter values non-null.
                @Override
                public void set(DateTimeFormatter newValue) {
                    super.set(Objects.requireNonNull(newValue, "formatter"));
                }

                /// Rewrites editor text when the formatter changes.
                @Override
                protected void invalidated() {
                    updateEditorsFromRange();
                }
            };

    /// Returns the formatter used for editor text.
    ///
    /// @return the formatter used for editor text
    public DateTimeFormatter getFormatter() {
        return formatter.get();
    }

    /// Sets the formatter used to parse and format both editor texts.
    ///
    /// Existing editor text is rewritten from the committed endpoints.
    ///
    /// @param formatter the formatter used for editor text
    /// @throws NullPointerException if `formatter` is `null`
    public void setFormatter(DateTimeFormatter formatter) {
        this.formatter.set(formatter);
    }

    /// Returns the observable property that stores the editor formatter.
    ///
    /// The property can be observed and bound. Its default value is [DateTimeFormatter#ISO_LOCAL_DATE]. Values must
    /// be non-null, and changing the formatter rewrites both editor texts from the committed range.
    ///
    /// @return the editor formatter property
    public ObjectProperty<DateTimeFormatter> formatterProperty() {
        return formatter;
    }

    /// The error message used when an editor contains non-empty text that cannot be parsed.
    ///
    /// This value is non-null and may be changed while the control is in use.
    ///
    /// @defaultValue `Enter a valid date`
    private final StringProperty invalidTextErrorText =
            new SimpleStringProperty(this, "invalidTextErrorText") {
                /// Keeps parse error text non-null.
                @Override
                public void set(String newValue) {
                    super.set(Objects.requireNonNull(newValue, "invalidTextErrorText"));
                }
            };

    /// Returns the parse error message used when editor text is invalid.
    ///
    /// @return the parse error message used when editor text is invalid
    public String getInvalidTextErrorText() {
        return invalidTextErrorText.get();
    }

    /// Sets the parse error message used when editor text is invalid.
    ///
    /// @param invalidTextErrorText the parse error message used when editor text is invalid
    /// @throws NullPointerException if `invalidTextErrorText` is `null`
    public void setInvalidTextErrorText(String invalidTextErrorText) {
        this.invalidTextErrorText.set(invalidTextErrorText);
    }

    /// Returns the observable property that stores the parse error message.
    ///
    /// The property can be observed and bound. Its default value is `Enter a valid date`, and values must be
    /// non-null. Changing it refreshes [#validationErrorTextProperty()] while validation is active.
    ///
    /// @return the invalid-text error property
    public StringProperty invalidTextErrorTextProperty() {
        return invalidTextErrorText;
    }

    /// The error message used for reversed ranges and dates outside the picker bounds.
    ///
    /// This value is non-null and may be changed while the control is in use.
    ///
    /// @defaultValue `Date range is outside the selectable range`
    private final StringProperty rangeErrorText =
            new SimpleStringProperty(this, "rangeErrorText") {
                /// Keeps range error text non-null.
                @Override
                public void set(String newValue) {
                    super.set(Objects.requireNonNull(newValue, "rangeErrorText"));
                }
            };

    /// Returns the range error message used when editor text is outside the selectable range.
    ///
    /// @return the range error message used when editor text is outside the selectable range
    public String getRangeErrorText() {
        return rangeErrorText.get();
    }

    /// Sets the range error message used when editor text is outside the selectable range.
    ///
    /// @param rangeErrorText the range error message used when editor text is outside the selectable range
    /// @throws NullPointerException if `rangeErrorText` is `null`
    public void setRangeErrorText(String rangeErrorText) {
        this.rangeErrorText.set(rangeErrorText);
    }

    /// Returns the observable property that stores the range error message.
    ///
    /// The property can be observed and bound. Its default value is
    /// `Date range is outside the selectable range`, and values must be non-null. Changing it refreshes
    /// [#validationErrorTextProperty()] while validation is active.
    ///
    /// @return the range error property
    public StringProperty rangeErrorTextProperty() {
        return rangeErrorText;
    }

    /// The raw start-date editor text.
    ///
    /// The default value is the empty string. Text may be temporarily invalid and is not parsed until
    /// [commitEditorText()] or an editor action commits it. `null` is not permitted.
    ///
    /// @defaultValue `""`
    private final StringProperty startText = new SimpleStringProperty(this, "startText", "") {
        /// Keeps start editor text non-null.
        @Override
        public void set(String newValue) {
            super.set(Objects.requireNonNull(newValue, "startText"));
        }
    };

    /// Returns the current raw start-date editor text.
    ///
    /// This text may be temporarily invalid while the user is editing. Call [commitEditorText] to parse it into
    /// [startDateProperty].
    ///
    /// @return the current raw start-date editor text
    public String getStartText() {
        return startText.get();
    }

    /// Sets the raw start-date editor text without committing it.
    ///
    /// The value is not parsed until [commitEditorText] is called or the editor action commits it.
    ///
    /// @param startText the raw start-date editor text
    /// @throws NullPointerException if `startText` is `null`
    public void setStartText(String startText) {
        this.startText.set(startText);
    }

    /// Returns the observable property that stores the raw start-editor text.
    ///
    /// The property can be observed and bound. Its default value is the empty string, values must be non-null, and
    /// changes remain uncommitted until [commitEditorText()] or an editor action commits them.
    ///
    /// @return the raw start-editor text property
    public StringProperty startTextProperty() {
        return startText;
    }

    /// The raw end-date editor text.
    ///
    /// The default value is the empty string. Text may be temporarily invalid and is not parsed until
    /// [commitEditorText()] or an editor action commits it. `null` is not permitted.
    ///
    /// @defaultValue `""`
    private final StringProperty endText = new SimpleStringProperty(this, "endText", "") {
        /// Keeps end editor text non-null.
        @Override
        public void set(String newValue) {
            super.set(Objects.requireNonNull(newValue, "endText"));
        }
    };

    /// Returns the current raw end-date editor text.
    ///
    /// This text may be temporarily invalid while the user is editing. Call [commitEditorText] to parse it into
    /// [endDateProperty].
    ///
    /// @return the current raw end-date editor text
    public String getEndText() {
        return endText.get();
    }

    /// Sets the raw end-date editor text without committing it.
    ///
    /// The value is not parsed until [commitEditorText] is called or the editor action commits it.
    ///
    /// @param endText the raw end-date editor text
    /// @throws NullPointerException if `endText` is `null`
    public void setEndText(String endText) {
        this.endText.set(endText);
    }

    /// Returns the observable property that stores the raw end-editor text.
    ///
    /// The property can be observed and bound. Its default value is the empty string, values must be non-null, and
    /// changes remain uncommitted until [commitEditorText()] or an editor action commits them.
    ///
    /// @return the raw end-editor text property
    public StringProperty endTextProperty() {
        return endText;
    }

    /// The Material text-input variant used by the start-date editor.
    ///
    /// @defaultValue [M3TextInputVariant#FILLED]
    private final ObjectProperty<M3TextInputVariant> startVariant =
            new SimpleObjectProperty<>(this, "startVariant", M3TextInputVariant.FILLED) {
                /// Keeps the start text input variant non-null.
                @Override
                public void set(M3TextInputVariant newValue) {
                    super.set(Objects.requireNonNull(newValue, "startVariant"));
                }
            };

    /// Returns the text input variant used by the start-date editor.
    ///
    /// @return the text input variant used by the start-date editor
    public M3TextInputVariant getStartVariant() {
        return startVariant.get();
    }

    /// Sets the text input variant used by the start-date editor.
    ///
    /// @param variant the text input variant used by the start-date editor
    /// @throws NullPointerException if `variant` is `null`
    public void setStartVariant(M3TextInputVariant variant) {
        startVariant.set(variant);
    }

    /// Returns the observable property that stores the start-editor variant.
    ///
    /// The property can be observed and bound. Its default value is [M3TextInputVariant#FILLED], and values must be
    /// non-null.
    ///
    /// @return the start-editor variant property
    public ObjectProperty<M3TextInputVariant> startVariantProperty() {
        return startVariant;
    }

    /// The Material text-input variant used by the end-date editor.
    ///
    /// @defaultValue [M3TextInputVariant#FILLED]
    private final ObjectProperty<M3TextInputVariant> endVariant =
            new SimpleObjectProperty<>(this, "endVariant", M3TextInputVariant.FILLED) {
                /// Keeps the end text input variant non-null.
                @Override
                public void set(M3TextInputVariant newValue) {
                    super.set(Objects.requireNonNull(newValue, "endVariant"));
                }
            };

    /// Returns the text input variant used by the end-date editor.
    ///
    /// @return the text input variant used by the end-date editor
    public M3TextInputVariant getEndVariant() {
        return endVariant.get();
    }

    /// Sets the text input variant used by the end-date editor.
    ///
    /// @param variant the text input variant used by the end-date editor
    /// @throws NullPointerException if `variant` is `null`
    public void setEndVariant(M3TextInputVariant variant) {
        endVariant.set(variant);
    }

    /// Returns the observable property that stores the end-editor variant.
    ///
    /// The property can be observed and bound. Its default value is [M3TextInputVariant#FILLED], and values must be
    /// non-null.
    ///
    /// @return the end-editor variant property
    public ObjectProperty<M3TextInputVariant> endVariantProperty() {
        return endVariant;
    }

    /// The explicit application error text displayed by the start-date input.
    ///
    /// The default value is empty. Generated validation is stored independently by the start input layout and never
    /// replaces this value. `null` is not permitted.
    ///
    /// @defaultValue `""`
    private final StringProperty startErrorText = new SimpleStringProperty(this, "startErrorText", "") {
        /// Keeps start error text non-null.
        @Override
        public void set(String newValue) {
            super.set(Objects.requireNonNull(newValue, "startErrorText"));
        }
    };

    /// Returns the explicit application error text for the start-date editor.
    ///
    /// @return the explicit start-date error text
    public String getStartErrorText() {
        return startErrorText.get();
    }

    /// Sets the explicit application error text for the start-date editor.
    ///
    /// @param errorText the explicit start-date error text, or an empty string to clear it
    /// @throws NullPointerException if `errorText` is `null`
    public void setStartErrorText(String errorText) {
        startErrorText.set(errorText);
    }

    /// Returns the observable property that stores the explicit start-editor error.
    ///
    /// The property can be observed and bound. Its default value is the empty string and values must be non-null.
    /// Generated validation is exposed separately through [#validationErrorTextProperty()].
    ///
    /// @return the displayed start-editor error property
    public StringProperty startErrorTextProperty() {
        return startErrorText;
    }

    /// The explicit application error text displayed by the end-date input.
    ///
    /// The default value is empty. Generated validation is stored independently by the end input layout and never
    /// replaces this value. `null` is not permitted.
    ///
    /// @defaultValue `""`
    private final StringProperty endErrorText = new SimpleStringProperty(this, "endErrorText", "") {
        /// Keeps end error text non-null.
        @Override
        public void set(String newValue) {
            super.set(Objects.requireNonNull(newValue, "endErrorText"));
        }
    };

    /// Returns the explicit application error text for the end-date editor.
    ///
    /// @return the explicit end-date error text
    public String getEndErrorText() {
        return endErrorText.get();
    }

    /// Sets the explicit application error text for the end-date editor.
    ///
    /// @param errorText the explicit end-date error text, or an empty string to clear it
    /// @throws NullPointerException if `errorText` is `null`
    public void setEndErrorText(String errorText) {
        endErrorText.set(errorText);
    }

    /// Returns the observable property that stores the explicit end-editor error.
    ///
    /// The property can be observed and bound. Its default value is the empty string and values must be non-null.
    /// Generated validation is exposed separately through [#validationErrorTextProperty()].
    ///
    /// @return the displayed end-editor error property
    public StringProperty endErrorTextProperty() {
        return endErrorText;
    }

    /// The label used to identify the complete range in form-validation presentation.
    ///
    /// This label is not rendered by either endpoint input layout. Their visible labels remain controlled by
    /// [#startLabelTextProperty()] and [#endLabelTextProperty()].
    ///
    /// @defaultValue `Date range`
    private final StringProperty labelText = new SimpleStringProperty(this, "labelText", "Date range") {
        /// Keeps the composite validation label non-null.
        @Override
        public void set(String newValue) {
            super.set(Objects.requireNonNull(newValue, "labelText"));
        }
    };

    /// Returns the label used for form-validation presentation.
    ///
    /// @return the non-null composite validation label
    @Override
    public String getLabelText() {
        return labelText.get();
    }

    /// Sets the label used for form-validation presentation.
    ///
    /// @param labelText the non-null composite validation label
    /// @throws NullPointerException if `labelText` is `null`
    public void setLabelText(String labelText) {
        this.labelText.set(labelText);
    }

    /// Returns the observable, bindable composite validation-label property.
    ///
    /// The property is independent of both visible endpoint labels and rejects `null`.
    ///
    /// @return the composite validation-label property
    @Override
    public StringProperty labelTextProperty() {
        return labelText;
    }

    /// The label text displayed by the start-date input.
    ///
    /// @defaultValue `Start date`
    private final StringProperty startLabelText = new SimpleStringProperty(this, "startLabelText", "") {
        /// Keeps start label text non-null.
        @Override
        public void set(String newValue) {
            super.set(Objects.requireNonNull(newValue, "startLabelText"));
        }
    };

    /// Returns the label text displayed by the start date input layout.
    ///
    /// @return the label text displayed by the start date input layout
    public String getStartLabelText() {
        return startLabelText.get();
    }

    /// Sets the label text displayed by the start date input layout.
    ///
    /// @param startLabelText the label text displayed by the start date input layout
    /// @throws NullPointerException if `startLabelText` is `null`
    public void setStartLabelText(String startLabelText) {
        this.startLabelText.set(startLabelText);
    }

    /// Returns the observable property that stores the start-editor label.
    ///
    /// The property can be observed and bound. Its default value is `Start date`, and values must be non-null.
    ///
    /// @return the start-editor label property
    public StringProperty startLabelTextProperty() {
        return startLabelText;
    }

    /// The label text displayed by the end-date input.
    ///
    /// @defaultValue `End date`
    private final StringProperty endLabelText = new SimpleStringProperty(this, "endLabelText", "") {
        /// Keeps end label text non-null.
        @Override
        public void set(String newValue) {
            super.set(Objects.requireNonNull(newValue, "endLabelText"));
        }
    };

    /// Returns the label text displayed by the end date input layout.
    ///
    /// @return the label text displayed by the end date input layout
    public String getEndLabelText() {
        return endLabelText.get();
    }

    /// Sets the label text displayed by the end date input layout.
    ///
    /// @param endLabelText the label text displayed by the end date input layout
    /// @throws NullPointerException if `endLabelText` is `null`
    public void setEndLabelText(String endLabelText) {
        this.endLabelText.set(endLabelText);
    }

    /// Returns the observable property that stores the end-editor label.
    ///
    /// The property can be observed and bound. Its default value is `End date`, and values must be non-null.
    ///
    /// @return the end-editor label property
    public StringProperty endLabelTextProperty() {
        return endLabelText;
    }

    /// The supporting text displayed below the start-date input.
    ///
    /// @defaultValue `""`
    private final StringProperty startSupportingText =
            new SimpleStringProperty(this, "startSupportingText", "") {
                /// Keeps start supporting text non-null.
                @Override
                public void set(String newValue) {
                    super.set(Objects.requireNonNull(newValue, "startSupportingText"));
                }
            };

    /// Returns the supporting text displayed by the start date input layout.
    ///
    /// @return the supporting text displayed by the start date input layout
    public String getStartSupportingText() {
        return startSupportingText.get();
    }

    /// Sets the supporting text displayed by the start date input layout.
    ///
    /// @param supportingText the supporting text displayed by the start date input layout
    /// @throws NullPointerException if `supportingText` is `null`
    public void setStartSupportingText(String supportingText) {
        startSupportingText.set(supportingText);
    }

    /// Returns the observable property that stores the start-editor supporting text.
    ///
    /// The property can be observed and bound. Its default value is the empty string, and values must be non-null.
    ///
    /// @return the start-editor supporting-text property
    public StringProperty startSupportingTextProperty() {
        return startSupportingText;
    }

    /// The supporting text displayed below the end-date input.
    ///
    /// @defaultValue `""`
    private final StringProperty endSupportingText =
            new SimpleStringProperty(this, "endSupportingText", "") {
                /// Keeps end supporting text non-null.
                @Override
                public void set(String newValue) {
                    super.set(Objects.requireNonNull(newValue, "endSupportingText"));
                }
            };

    /// Returns the supporting text displayed by the end date input layout.
    ///
    /// @return the supporting text displayed by the end date input layout
    public String getEndSupportingText() {
        return endSupportingText.get();
    }

    /// Sets the supporting text displayed by the end date input layout.
    ///
    /// @param supportingText the supporting text displayed by the end date input layout
    /// @throws NullPointerException if `supportingText` is `null`
    public void setEndSupportingText(String supportingText) {
        endSupportingText.set(supportingText);
    }

    /// Returns the observable property that stores the end-editor supporting text.
    ///
    /// The property can be observed and bound. Its default value is the empty string, and values must be non-null.
    ///
    /// @return the end-editor supporting-text property
    public StringProperty endSupportingTextProperty() {
        return endSupportingText;
    }

    /// Whether the picker popup is currently visible.
    ///
    /// This read-only property is initially `false`. It becomes `true` after the popup is shown and returns to
    /// `false` when hiding completes or the popup is otherwise dismissed.
    private final ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing");

    /// Returns whether the picker popup is currently showing.
    ///
    /// @return `true` when the picker popup is currently showing
    public boolean isShowing() {
        return showing.get();
    }

    /// Returns the read-only observable property that reports popup visibility.
    ///
    /// The property can be observed and used as a binding source. Its default value is `false`.
    ///
    /// @return the read-only popup-visibility property
    public ReadOnlyBooleanProperty showingProperty() {
        return showing.getReadOnlyProperty();
    }

    /// The editable text field used for the start date.
    private final M3TextField startEditor = new M3TextField();

    /// The editable text field used for the end date.
    private final M3TextField endEditor = new M3TextField();

    /// The Material text input layout wrapping the start editor.
    private final M3TextInputLayout startInputLayout = new M3TextInputLayout(startEditor);

    /// The Material text input layout wrapping the end editor.
    private final M3TextInputLayout endInputLayout = new M3TextInputLayout(endEditor);

    /// The first generated error contributed by either endpoint input layout.
    private final ReadOnlyStringWrapper validationErrorText =
            new ReadOnlyStringWrapper(this, "validationErrorText", "");

    /// Whether generated validation is active for at least one endpoint input layout.
    private final ReadOnlyBooleanWrapper validationActive =
            new ReadOnlyBooleanWrapper(this, "validationActive");

    /// The endpoint editor that should receive focus for the current generated validation failure.
    private final ReadOnlyObjectWrapper<Node> validationFocusTarget =
            new ReadOnlyObjectWrapper<>(this, "validationFocusTarget", startEditor);

    /// Whether at least one endpoint property currently has a unidirectional binding.
    private final ReadOnlyBooleanWrapper rangeBound = new ReadOnlyBooleanWrapper(this, "rangeBound");

    /// The date range picker owned by this field and displayed in its popup.
    private final M3DateRangePicker picker = new M3DateRangePicker();

    /// The live, mutable, ordered list of presets rendered before the popup picker.
    ///
    /// The list initially is empty, rejects `null` elements, permits duplicates, and observes additions, removals,
    /// and reordering. Presets outside the current picker bounds remain visible but disabled.
    private final ObservableList<M3DateRangePreset> presets = M3ObservableLists.nonNullElementList("preset");

    /// The wrapper used when the popup renders preset actions next to the picker.
    private final HBox presetContent = new HBox(16.0);

    /// The vertical preset action container.
    private final VBox presetList = new VBox(6.0);

    /// Rebuilds preset action buttons when the public preset list changes.
    private final ListChangeListener<M3DateRangePreset> presetsListener = change -> updatePresetContent();

    /// The trailing button that opens the popup from the start editor.
    private final M3IconButton startOpenButton = createOpenButton("Open start date range picker");

    /// The trailing button that opens the popup from the end editor.
    private final M3IconButton endOpenButton = createOpenButton("Open end date range picker");

    /// The popup root that inherits scene styles and hosts the picker.
    private final StackPane popupContent = new StackPane();

    /// The popup window used for picker display.
    private final Popup popup = new Popup();

    /// Keeps the detached date-range picker popup synchronized with the owner scene and theme context while visible.
    private final M3PopupContextSynchronizer popupContextSynchronizer =
            new M3PopupContextSynchronizer(this, popupContent, M3Stylesheets.controlStylesheet("picker-field.css"));

    /// The reusable picker popup enter and exit animation.
    private final M3NodeTransition popupAnimation = new M3NodeTransition(popupContent);

    /// Reports popup picker focus changes through this field's accessibility node.
    private final M3AccessibleFocusNotifier popupFocusNotifier =
            new M3AccessibleFocusNotifier(this, popupContent, this::focusNode, this::notifyFocusNodeChanged);

    /// Closes the popup when this field or one of its ancestors becomes unreachable.
    private final M3ReachabilityObserver reachabilityObserver =
            new M3ReachabilityObserver(this, this::hidePopupIfOwnerUnreachable);

    /// The atomic empty, in-progress, or complete field selection snapshot.
    private final ReadOnlyObjectWrapper<M3DateRangeSelection> selection =
            new ReadOnlyObjectWrapper<>(this, "selection", M3DateRangeSelection.EMPTY);

    /// The last selectable field range reflected by the editors and popup picker.
    private M3DateRangeSelection lastDisplayedSelection = M3DateRangeSelection.EMPTY;

    /// The nesting depth of endpoint mutations contributing to one atomic selection notification.
    private int rangeMutationDepth;

    /// Whether an endpoint changed during the current atomic mutation.
    private boolean rangeMutationDirty;

    /// Whether both field endpoints are currently being assigned through
    /// [setRange][#setRange(LocalDate, LocalDate)].
    private boolean applyingRange;

    /// Whether field state is currently being pushed into the popup picker.
    private boolean synchronizingPicker;

    /// Whether popup picker state is currently being copied into the field.
    private boolean synchronizingFromPicker;

    /// Whether editor text is currently being rewritten from selected dates.
    private boolean updatingEditorText;

    /// The editor that opened the current popup session.
    private @Nullable M3TextField popupOwnerEditor;

    /// The editor that should receive focus after the popup hides.
    private @Nullable M3TextField focusEditorOnHidden;

    /// Whether the reusable popup animation is currently closing the picker.
    private boolean hidingPopup;

    /// The vertical offset used by the current popup hide animation.
    private double popupTransitionOffsetY = -POPUP_TRANSITION_OFFSET_Y;

    /// Sets both range endpoints after validating ordering and selectable bounds.
    ///
    /// The start property is assigned before the end property. Individual endpoint listeners may therefore observe
    /// an intermediate pair, while [#selectionProperty()] and all owned editor and picker synchronization publish
    /// only the final pair.
    ///
    /// @param startDate the first selected date, or `null` to clear the range
    /// @param endDate   the last selected date, or `null` for an incomplete range
    /// @throws IllegalArgumentException if an endpoint is outside the current picker bounds, `endDate` is non-null
    ///         while `startDate` is null, or the start is after the end
    /// @throws RuntimeException         if either endpoint property is bound
    public void setRange(@Nullable LocalDate startDate, @Nullable LocalDate endDate) {
        validateDate(startDate);
        validateDate(endDate);
        validateDateRange(startDate, endDate);
        if (this.startDate.isBound() || this.endDate.isBound()) {
            throw new RuntimeException("cannot set a range while an endpoint property is bound");
        }
        beginRangeMutation();
        applyingRange = true;
        try {
            this.startDate.set(startDate);
            this.endDate.set(endDate);
        } finally {
            applyingRange = false;
            endRangeMutation();
        }
    }

    /// Sets both range endpoints from the supplied inclusive range.
    ///
    /// @param range the inclusive date range to select
    /// @throws NullPointerException     if `range` is `null`
    /// @throws IllegalArgumentException if either endpoint is outside the current picker bounds
    /// @throws RuntimeException         if either endpoint property is bound
    public void setRange(M3DateRange range) {
        M3DateRange validatedRange = Objects.requireNonNull(range, "range");
        setRange(validatedRange.startDate(), validatedRange.endDate());
    }

    /// Clears both selected range endpoints and editor texts.
    ///
    /// Calling this method while the range is empty has no effect on the selected state.
    ///
    /// @throws RuntimeException if either endpoint property is bound
    public void clearRange() {
        setRange(null, null);
    }

    /// Returns whether both range endpoints are selected.
    ///
    /// Invalid bound endpoint values do not replace the last valid selection snapshot used by this result.
    ///
    /// @return `true` when both range endpoints are selected
    public boolean isRangeComplete() {
        return getSelection().isComplete();
    }

    /// Returns the selected range, or `null` when the range is incomplete.
    ///
    /// Invalid bound endpoint values do not replace the last valid range returned by this method.
    ///
    /// @return the selected range, or `null` when the range is incomplete
    public @Nullable M3DateRange getRange() {
        return getSelection().toRange();
    }

    /// Returns the atomic empty, in-progress, or complete selection snapshot.
    ///
    /// A bound endpoint change that violates ordering or selectable bounds does not replace this last valid snapshot.
    ///
    /// @return the current immutable selection snapshot
    public M3DateRangeSelection getSelection() {
        return selection.get();
    }

    /// Returns the observable, read-only atomic selection property.
    ///
    /// The property initially contains [M3DateRangeSelection#EMPTY]. A direct endpoint change publishes after its
    /// validation and any dependent endpoint clearing complete. [#setRange(LocalDate, LocalDate)] and a committed
    /// editor pair publish once after both endpoint properties have been assigned and owned views are synchronized.
    /// Invalid bound endpoint changes activate validation without publishing a new snapshot.
    ///
    /// @return the read-only atomic selection property
    public ReadOnlyObjectProperty<M3DateRangeSelection> selectionProperty() {
        return selection.getReadOnlyProperty();
    }

    /// Applies a date range preset, updates the editors, and closes the popup when it is showing.
    ///
    /// @param preset the date range preset to apply
    private void applyPreset(M3DateRangePreset preset) {
        if (isRangeBound()) {
            return;
        }
        M3DateRange range = Objects.requireNonNull(preset, "preset").range();
        setRange(range);
        picker.showMonth(YearMonth.from(range.startDate()));
        if (popup.isShowing()) {
            hidePicker(true);
        }
    }

    /// Returns the editable text field shown for the start date.
    ///
    /// @return the editable text field shown for the start date
    M3TextField getStartEditor() {
        return startEditor;
    }

    /// Returns the editable text field shown for the end date.
    ///
    /// @return the editable text field shown for the end date
    M3TextField getEndEditor() {
        return endEditor;
    }

    /// Returns the Material text input layout used by the start date editor.
    ///
    /// @return the Material text input layout used by the start date editor
    M3TextInputLayout getStartInputLayout() {
        return startInputLayout;
    }

    /// Returns the Material text input layout used by the end date editor.
    ///
    /// @return the Material text input layout used by the end date editor
    M3TextInputLayout getEndInputLayout() {
        return endInputLayout;
    }

    /// {@inheritDoc}
    @Override
    public String getValidationErrorText() {
        return validationErrorText.get();
    }

    /// {@inheritDoc}
    @Override
    public ReadOnlyStringProperty validationErrorTextProperty() {
        return validationErrorText.getReadOnlyProperty();
    }

    /// {@inheritDoc}
    @Override
    public boolean isValidationActive() {
        return validationActive.get();
    }

    /// {@inheritDoc}
    @Override
    public ReadOnlyBooleanProperty validationActiveProperty() {
        return validationActive.getReadOnlyProperty();
    }

    /// Clears generated validation from both endpoint inputs without changing explicit application errors.
    @Override
    public void clearValidation() {
        startInputLayout.clearValidation();
        endInputLayout.clearValidation();
    }

    /// {@inheritDoc}
    @Override
    public boolean isValidationError() {
        return !getValidationErrorText().isEmpty();
    }

    /// {@inheritDoc}
    @Override
    public Node getValidationNode() {
        return this;
    }

    /// Returns the editor associated with the first current generated error, or the start editor when valid.
    ///
    /// @return the preferred endpoint editor
    @Override
    public Node getValidationFocusTarget() {
        return validationFocusTarget.get();
    }

    /// {@inheritDoc}
    @Override
    public ReadOnlyObjectProperty<Node> validationFocusTargetProperty() {
        return validationFocusTarget.getReadOnlyProperty();
    }

    /// Returns the date range picker owned by this field and displayed in its popup.
    ///
    /// @return the popup date range picker control
    public M3DateRangePicker getPicker() {
        return picker;
    }

    /// Returns the live, mutable date range preset list rendered in the popup.
    ///
    /// @return the live, mutable date range preset list rendered in the popup
    public ObservableList<M3DateRangePreset> getPresets() {
        return presets;
    }

    /// Attempts to parse and commit both editor texts as one range.
    ///
    /// Leading and trailing whitespace is ignored and an empty editor commits a `null` endpoint. If parsing,
    /// ordering, or picker-bound validation fails, the previous committed range is preserved and the relevant
    /// generated validation is updated without changing either explicit application error. If an endpoint property
    /// is unidirectionally bound, the last valid editor presentation is restored and this method returns `false`.
    ///
    /// @return `true` if both texts were committed, or `false` if validation failed
    public boolean commitEditorText() {
        if (isRangeBound()) {
            updateEditorsFromRange();
            return false;
        }
        if (!validateEditors()) {
            return false;
        }

        @Nullable LocalDate parsedStart = parseEditorText(startEditor.getText());
        @Nullable LocalDate parsedEnd = parseEditorText(endEditor.getText());

        setRange(parsedStart, parsedEnd);
        updateEditorsFromRange();
        clearValidation();
        return true;
    }

    /// Runs composite range validation and commits valid editor text when both endpoints are writable.
    ///
    /// Bound endpoint values are not written. Their last valid formatted presentation is restored before current
    /// committed state is validated. For an unbound range this method has the same commit semantics as
    /// [#commitEditorText()].
    ///
    /// @return `true` when the current composite range state is valid
    @Override
    public boolean validate() {
        if (isRangeBound()) {
            updateEditorsFromRange();
            return validateEditors();
        }
        return commitEditorText();
    }

    /// Shows the picker popup when this field is reachable from a visible window.
    ///
    /// Calling this method while an endpoint property is unidirectionally bound, while the popup is showing, or
    /// while it cannot be positioned relative to this field has no effect. On success [showingProperty()] becomes
    /// `true` before the enter transition begins.
    public void showPicker() {
        if (isRangeBound() || !M3Accessible.canReach(this) || popup.isShowing() || !M3PopupWindows.canShow(this)) {
            return;
        }

        if (popupOwnerEditor == null) {
            popupOwnerEditor = currentEditor();
        }
        boolean popupShown = false;
        popupContextSynchronizer.start();
        try {
            preparePopupForShow();
            @Nullable M3PopupPositioning.Placement placement =
                    M3PopupPositioning.menuBelowOrAbove(this, popupContent, POPUP_OFFSET_Y);
            if (placement == null) {
                return;
            }

            popupTransitionOffsetY =
                    placement.opensAbove() ? POPUP_TRANSITION_OFFSET_Y : -POPUP_TRANSITION_OFFSET_Y;
            preparePopupForShowAnimation();
            if (!M3PopupWindows.show(popup, this, placement.x(), placement.y())) {
                return;
            }
            popupShown = true;
        } finally {
            if (!popupShown) {
                resetPopupAnimationState();
                popupOwnerEditor = null;
                popupContextSynchronizer.stop();
            }
        }
        popupFocusNotifier.start();
        reachabilityObserver.install();
        showing.set(true);
        notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
        notifyFocusNodeChanged();
        popupFocusNotifier.refresh();
        playShowAnimation();
    }

    /// Begins hiding the picker popup.
    ///
    /// Calling this method while the popup is not showing has no effect. With motion enabled,
    /// [showingProperty()] remains `true` until the exit transition completes.
    public void hidePicker() {
        hidePicker(false);
    }

    /// Hides the picker when it is showing; otherwise attempts to show it.
    public void togglePicker() {
        if (popup.isShowing()) {
            hidePicker();
        } else {
            showPicker();
        }
    }

    /// Returns the user-agent stylesheet for M3FX picker fields.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("picker-field.css");
    }

    /// Returns accessibility attributes for the editable values and picker popup.
    ///
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case EXPANDED -> isShowing();
            case FOCUS_NODE -> focusNode();
            case ITEM_AT_INDEX -> accessibleItem(parameters);
            case SELECTED_ITEMS -> selectedItems();
            case SUBMENU -> picker;
            case TEXT -> accessibleText();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes editor and popup accessibility actions.
    ///
    /// @throws NullPointerException if `action` is `null`
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        if (isDisabled()) {
            super.executeAccessibleAction(action, parameters);
            return;
        }

        switch (action) {
            case REQUEST_FOCUS -> focusAccessibleNode();
            case SHOW_MENU, EXPAND -> showPicker();
            case COLLAPSE -> hidePicker(true);
            case SHOW_ITEM -> showAccessibleItem(parameters);
            case SET_SELECTED_ITEMS, INCREMENT, DECREMENT, BLOCK_INCREMENT, BLOCK_DECREMENT ->
                    forwardPickerAccessibleAction(action, parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Creates the default Material Design 3 date range picker field skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3DateRangePickerFieldSkin(this);
    }

    /// Adds base style classes and installs event handling.
    private void initialize() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        startEditor.textProperty().bindBidirectional(startText);
        endEditor.textProperty().bindBidirectional(endText);
        startEditor.variantProperty().bindBidirectional(startVariant);
        endEditor.variantProperty().bindBidirectional(endVariant);
        startInputLayout.errorTextProperty().bindBidirectional(startErrorText);
        endInputLayout.errorTextProperty().bindBidirectional(endErrorText);
        startInputLayout.setValidator((input, text) -> endpointValidationError(true));
        endInputLayout.setValidator((input, text) -> endpointValidationError(false));
        startInputLayout.setValidateOnFocusLost(false);
        endInputLayout.setValidateOnFocusLost(false);
        startInputLayout.setValidateOnTextChange(false);
        endInputLayout.setValidateOnTextChange(false);
        startInputLayout.labelTextProperty().bindBidirectional(startLabelText);
        endInputLayout.labelTextProperty().bindBidirectional(endLabelText);
        startInputLayout.supportingTextProperty().bindBidirectional(startSupportingText);
        endInputLayout.supportingTextProperty().bindBidirectional(endSupportingText);
        setAccessibleRole(AccessibleRole.PARENT);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleNode, this::showAccessibleItem,
                this::handlesAccessibleShowTarget);
        setStartLabelText("Start date");
        setEndLabelText("End date");
        startInputLayout.setTrailing(startOpenButton);
        endInputLayout.setTrailing(endOpenButton);
        startEditor.editableProperty().bind(rangeBound.not());
        endEditor.editableProperty().bind(rangeBound.not());
        startOpenButton.disableProperty().bind(rangeBound);
        endOpenButton.disableProperty().bind(rangeBound);
        startInputLayout.disableProperty().bind(disabledProperty());
        endInputLayout.disableProperty().bind(disabledProperty());
        startInputLayout.nodeOrientationProperty().bind(effectiveNodeOrientationProperty());
        endInputLayout.nodeOrientationProperty().bind(effectiveNodeOrientationProperty());
        picker.nodeOrientationProperty().bind(effectiveNodeOrientationProperty());
        popupContent.nodeOrientationProperty().bind(effectiveNodeOrientationProperty());

        M3ControlStyles.add(popupContent, M3PickerField.POPUP_STYLE_CLASS);
        M3ControlStyles.add(popupContent, POPUP_STYLE_CLASS);
        popupContent.getChildren().setAll(picker);
        presetContent.getStyleClass().add(PRESET_CONTENT_STYLE_CLASS);
        presetContent.nodeOrientationProperty().bind(effectiveNodeOrientationProperty());
        presetContent.setAlignment(Pos.TOP_LEFT);
        presetList.getStyleClass().add(PRESET_LIST_STYLE_CLASS);
        presetList.nodeOrientationProperty().bind(effectiveNodeOrientationProperty());
        presetList.setAlignment(Pos.TOP_LEFT);
        M3PresetNavigation.installColumn(presetList, this, this::focusPickerContent);
        popup.setAutoHide(true);
        popup.getContent().add(popupContent);
        popupAnimation.setOnFinished(event -> {
            if (hidingPopup) {
                popup.hide();
            }
        });
        popup.setOnHidden(event -> handlePopupHidden());

        startOpenButton.setOnAction(event -> {
            popupOwnerEditor = startEditor;
            togglePicker();
        });
        endOpenButton.setOnAction(event -> {
            popupOwnerEditor = endEditor;
            togglePicker();
        });
        startEditor.addEventHandler(ActionEvent.ACTION, this::handleEditorAction);
        endEditor.addEventHandler(ActionEvent.ACTION, this::handleEditorAction);
        startEditor.addEventHandler(KeyEvent.KEY_PRESSED, this::handleEditorKeyPressed);
        endEditor.addEventHandler(KeyEvent.KEY_PRESSED, this::handleEditorKeyPressed);
        popupContent.addEventHandler(KeyEvent.KEY_PRESSED, this::handlePickerKeyPressed);
        startEditor.focusedProperty().addListener((observable, oldValue, focused) -> handleEditorFocusChanged());
        endEditor.focusedProperty().addListener((observable, oldValue, focused) -> handleEditorFocusChanged());
        startEditor.textProperty().addListener((observable, oldValue, newValue) -> handleEditorTextChanged());
        endEditor.textProperty().addListener((observable, oldValue, newValue) -> handleEditorTextChanged());
        picker.selectionProperty().addListener(
                (observable, oldSelection, newSelection) -> syncRangeFromPicker(newSelection)
        );
        picker.minDateProperty().addListener((observable, oldValue, newValue) -> handleSelectableBoundsChanged());
        picker.maxDateProperty().addListener((observable, oldValue, newValue) -> handleSelectableBoundsChanged());
        presets.addListener(presetsListener);
        invalidTextErrorText.addListener(observable -> refreshActiveValidation());
        rangeErrorText.addListener(observable -> refreshActiveValidation());
        startInputLayout.validationErrorTextProperty().addListener(observable -> updateCompositeValidationState());
        endInputLayout.validationErrorTextProperty().addListener(observable -> updateCompositeValidationState());
        startInputLayout.validationActiveProperty().addListener(observable -> updateCompositeValidationState());
        endInputLayout.validationActiveProperty().addListener(observable -> updateCompositeValidationState());
        updateCompositeValidationState();
        updateBoundState();
    }

    /// Hides the popup if its owner field can no longer be reached from its scene.
    private void hidePopupIfOwnerUnreachable() {
        if (popup.isShowing() && !M3Accessible.canReach(this)) {
            hidePicker(false);
        }
    }

    /// Creates one popup open button.
    private static M3IconButton createOpenButton(String accessibleText) {
        M3IconButton button = new M3IconButton(new M3InternalIcon(
                M3InternalIcon.Glyph.CALENDAR,
                M3InternalIcon.ColorRole.ON_SURFACE_VARIANT
        ));
        M3ControlStyles.add(button, OPEN_BUTTON_STYLE_CLASS);
        button.setAccessibleText(accessibleText);
        return button;
    }

    /// Handles editor action commits.
    private void handleEditorAction(ActionEvent event) {
        if (commitEditorText()) {
            hidePicker();
        }
        event.consume();
    }

    /// Handles editor keyboard opening and dismissal.
    private void handleEditorKeyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case DOWN, F4 -> {
                popupOwnerEditor = event.getSource() == endEditor ? endEditor : startEditor;
                showPicker();
                focusPicker();
                event.consume();
            }
            case ESCAPE -> {
                if (popup.isShowing()) {
                    hidePicker(true);
                    event.consume();
                }
            }
            default -> {
            }
        }
    }

    /// Handles picker keyboard dismissal.
    private void handlePickerKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE && popup.isShowing()) {
            hidePicker(true);
            event.consume();
        }
    }

    /// Rebuilds popup content from the current preset list.
    private void updatePresetContent() {
        popupContent.getChildren().clear();
        presetContent.getChildren().clear();
        presetList.getChildren().clear();

        if (presets.isEmpty()) {
            popupContent.getChildren().setAll(picker);
            return;
        }

        for (M3DateRangePreset preset : presets) {
            presetList.getChildren().add(createPresetButton(preset));
        }
        presetContent.getChildren().setAll(presetList, picker);
        popupContent.getChildren().setAll(presetContent);
    }

    /// Creates one popup preset action button.
    private M3Button createPresetButton(M3DateRangePreset preset) {
        M3Button button = new M3Button(preset.text(), M3ButtonVariant.TEXT);
        button.getStyleClass().add(PRESET_BUTTON_STYLE_CLASS);
        M3Css.setMaxWidthIfUnbound(button, Double.MAX_VALUE);
        button.setDisable(isPresetDisabled(preset));
        button.setOnAction(event -> {
            applyPreset(preset);
            event.consume();
        });
        return button;
    }

    /// Returns whether a preset cannot be selected with the current date bounds.
    private boolean isPresetDisabled(M3DateRangePreset preset) {
        M3DateRange range = preset.range();
        return isRangeBound()
                || picker.isDateDisabled(range.startDate())
                || picker.isDateDisabled(range.endDate());
    }

    /// Commits editor text after focus leaves both range editors.
    private void handleEditorFocusChanged() {
        if (!startEditor.isFocused() && !endEditor.isFocused() && !popup.isShowing()) {
            commitEditorText();
        }
        notifyFocusNodeChanged();
    }

    /// Refreshes active composite validation after user edits.
    private void handleEditorTextChanged() {
        if (!updatingEditorText && isValidationActive()) {
            validateEditors();
        }
    }

    /// Synchronizes field state from one atomic popup picker selection.
    private void syncRangeFromPicker(M3DateRangeSelection pickerSelection) {
        if (synchronizingPicker) {
            return;
        }
        if (isRangeBound()) {
            syncPickerFromPresentation();
            return;
        }
        synchronizingFromPicker = true;
        try {
            setRange(pickerSelection.startDate(), pickerSelection.endDate());
        } finally {
            synchronizingFromPicker = false;
        }

        if (pickerSelection.isComplete() && popup.isShowing()) {
            hidePicker(true);
        }
    }

    /// Begins one possibly nested endpoint mutation.
    private void beginRangeMutation() {
        rangeMutationDepth++;
    }

    /// Completes one endpoint mutation and publishes the final snapshot at the outer boundary.
    private void endRangeMutation() {
        rangeMutationDepth--;
        if (rangeMutationDepth == 0 && rangeMutationDirty) {
            publishRangeSelection();
        }
    }

    /// Records an endpoint change and publishes immediately when no compound mutation owns it.
    private void markRangeChanged() {
        rangeMutationDirty = true;
        if (rangeMutationDepth == 0) {
            publishRangeSelection();
        }
    }

    /// Synchronizes owned views and publishes one immutable selection snapshot.
    private void publishRangeSelection() {
        rangeMutationDirty = false;
        M3DateRangeSelection snapshot = M3DateRangeSelection.of(getStartDate(), getEndDate());
        handleFieldRangeChanged();
        selection.set(snapshot);
    }

    /// Synchronizes dependent state after either field endpoint changes.
    private void handleFieldRangeChanged() {
        updateEditorsFromRange();
        clearValidation();

        if (!synchronizingFromPicker) {
            syncPickerFromPresentation();
        }

        notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
        notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED_ITEMS);
        M3Accessible.notifyFocusNodeChanged(this);
    }

    /// Returns whether the current field endpoints form a selectable inclusive range.
    private boolean isFieldRangeValid() {
        @Nullable LocalDate start = getStartDate();
        @Nullable LocalDate end = getEndDate();
        return !(start == null && end != null)
                && !(start != null && end != null && start.isAfter(end))
                && (start == null || !picker.isDateDisabled(start))
                && (end == null || !picker.isDateDisabled(end));
    }

    /// Returns the current valid range presentation or the last one accepted by the field.
    ///
    /// @return the immutable range selection to display
    private M3DateRangeSelection displayedSelection() {
        if (isFieldRangeValid()) {
            lastDisplayedSelection = M3DateRangeSelection.of(getStartDate(), getEndDate());
        }
        return lastDisplayedSelection;
    }

    /// Updates editor text from the current valid or last accepted range.
    private void updateEditorsFromRange() {
        M3DateRangeSelection displayedSelection = displayedSelection();
        updatingEditorText = true;
        try {
            startEditor.setText(formatNullableDate(displayedSelection.startDate()));
            endEditor.setText(formatNullableDate(displayedSelection.endDate()));
        } finally {
            updatingEditorText = false;
        }
    }

    /// Synchronizes the popup picker with the current selectable presentation.
    private void syncPickerFromPresentation() {
        M3DateRangeSelection displayedSelection = displayedSelection();
        @Nullable LocalDate displayedStart = displayedSelection.startDate();
        @Nullable LocalDate displayedEnd = displayedSelection.endDate();
        if (displayedStart != null && picker.isDateDisabled(displayedStart)
                || displayedEnd != null && picker.isDateDisabled(displayedEnd)) {
            displayedStart = null;
            displayedEnd = null;
        }

        synchronizingPicker = true;
        try {
            picker.setRange(displayedStart, displayedEnd);
        } finally {
            synchronizingPicker = false;
        }
    }

    /// Formats one nullable date value.
    private String formatNullableDate(@Nullable LocalDate date) {
        return date == null ? "" : getFormatter().format(date);
    }

    /// Parses one nullable editor string without changing validation state.
    ///
    /// @param text the raw editor text, or `null` for empty text
    /// @return the parsed date, or `null` for empty text
    private @Nullable LocalDate parseEditorText(@Nullable String text) {
        String trimmedText = text == null ? "" : text.trim();
        return trimmedText.isEmpty() ? null : LocalDate.from(getFormatter().parse(trimmedText));
    }

    /// Returns the generated validation error for one endpoint of the current composite range state.
    ///
    /// @param startEndpoint whether to return the start endpoint's error
    /// @return the generated endpoint error, or `null` when that endpoint is valid
    private @Nullable String endpointValidationError(boolean startEndpoint) {
        @Nullable LocalDate parsedStart;
        @Nullable LocalDate parsedEnd;
        boolean invalidStart = false;
        boolean invalidEnd = false;

        if (isRangeBound()) {
            parsedStart = getStartDate();
            parsedEnd = getEndDate();
        } else {
            try {
                parsedStart = parseEditorText(startEditor.getText());
            } catch (DateTimeException | IllegalArgumentException e) {
                parsedStart = null;
                invalidStart = true;
            }
            try {
                parsedEnd = parseEditorText(endEditor.getText());
            } catch (DateTimeException | IllegalArgumentException e) {
                parsedEnd = null;
                invalidEnd = true;
            }
        }

        if (invalidStart || invalidEnd) {
            return startEndpoint
                    ? invalidStart ? getInvalidTextErrorText() : null
                    : invalidEnd ? getInvalidTextErrorText() : null;
        }
        if (parsedStart == null && parsedEnd != null) {
            return startEndpoint ? getInvalidTextErrorText() : null;
        }

        boolean rangeInvalid = parsedStart != null && parsedEnd != null && parsedStart.isAfter(parsedEnd)
                || parsedStart != null && picker.isDateDisabled(parsedStart)
                || parsedEnd != null && picker.isDateDisabled(parsedEnd);
        @Nullable LocalDate endpoint = startEndpoint ? parsedStart : parsedEnd;
        return rangeInvalid && endpoint != null ? getRangeErrorText() : null;
    }

    /// Activates validation for both endpoint layouts and returns their aggregate validity.
    ///
    /// @return `true` when both endpoint layouts accept the current composite state
    private boolean validateEditors() {
        boolean startValid = startInputLayout.validate();
        boolean endValid = endInputLayout.validate();
        return startValid && endValid;
    }

    /// Re-runs both endpoint validators when composite validation is already active.
    private void refreshActiveValidation() {
        if (isValidationActive()) {
            validateEditors();
        }
    }

    /// Publishes aggregate validation text, activation, and focus routing from both endpoint layouts.
    private void updateCompositeValidationState() {
        String startError = startInputLayout.getValidationErrorText();
        String endError = endInputLayout.getValidationErrorText();
        validationErrorText.set(startError.isEmpty() ? endError : startError);
        validationActive.set(startInputLayout.isValidationActive() || endInputLayout.isValidationActive());
        validationFocusTarget.set(startError.isEmpty() && !endError.isEmpty() ? endEditor : startEditor);
    }

    /// Handles a binding source that violates endpoint ordering or picker-bound requirements.
    private void handleInvalidBoundRange() {
        updateEditorsFromRange();
        validateEditors();
        syncPickerFromPresentation();
    }

    /// Returns whether either endpoint property has a unidirectional binding.
    ///
    /// @return `true` when field interactions must not write range endpoints
    private boolean isRangeBound() {
        return startDate.isBound() || endDate.isBound();
    }

    /// Applies read-only presentation and popup state after endpoint binding changes.
    private void updateBoundState() {
        boolean bound = isRangeBound();
        rangeBound.set(bound);
        if (bound) {
            hidePicker(false);
        }
        updatePresetContent();
        if (!bound && !isFieldRangeValid()) {
            M3DateRangeSelection displayedSelection = lastDisplayedSelection;
            setRange(displayedSelection.startDate(), displayedSelection.endDate());
            return;
        }
        updateEditorsFromRange();
        if (bound && !isFieldRangeValid()) {
            validateEditors();
        } else {
            refreshActiveValidation();
        }
        syncPickerFromPresentation();
    }

    /// Validates that one date is selectable.
    private void validateDate(@Nullable LocalDate date) {
        if (date != null && picker.isDateDisabled(date)) {
            throw new IllegalArgumentException("date is outside the selectable range");
        }
    }

    /// Validates optional inclusive selected endpoints.
    private static void validateDateRange(@Nullable LocalDate startDate, @Nullable LocalDate endDate) {
        if (startDate == null && endDate != null) {
            throw new IllegalArgumentException("startDate must be selected before endDate");
        }
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate must not be after endDate");
        }
    }

    /// Refreshes preset and field validation state after picker bounds change.
    private void handleSelectableBoundsChanged() {
        updatePresetContent();
        if (isRangeBound()) {
            if (!isFieldRangeValid() || isValidationActive()) {
                validateEditors();
            }
            syncPickerFromPresentation();
        } else if (!isFieldRangeValid()) {
            clearRange();
        } else {
            refreshActiveValidation();
        }
    }

    /// Returns the field selection as an immutable accessibility list.
    private List<LocalDate> selectedItems() {
        @Nullable LocalDate start = getStartDate();
        @Nullable LocalDate end = getEndDate();
        if (start == null) {
            return List.of();
        }
        return end == null ? List.of(start) : List.of(start, end);
    }

    /// Returns one indexed editor layout for accessibility clients and skins.
    ///
    /// @param parameters the accessibility index parameters
    /// @return the indexed editor layout, or `null` when the parameters do not address a child
    private @Nullable Node accessibleItem(Object... parameters) {
        if (parameters.length != 1 || !(parameters[0] instanceof Integer index)) {
            return null;
        }
        return switch (index) {
            case 0 -> startInputLayout;
            case 1 -> endInputLayout;
            default -> null;
        };
    }

    /// Returns combined editor text for accessibility clients.
    private String accessibleText() {
        @Nullable LocalDate start = getStartDate();
        @Nullable LocalDate end = getEndDate();
        if (start == null) {
            return "";
        }
        return end == null ? start.toString() : start + "/" + end;
    }

    /// Returns the current keyboard focus node for accessibility clients.
    private Node focusNode() {
        if (popup.isShowing()) {
            @Nullable Node popupFocusOwner = popupFocusOwner();
            if (popupFocusOwner != null) {
                return popupFocusOwner;
            }

            @Nullable Object focusNode = picker.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);
            return focusNode instanceof Node node && M3Accessible.canReach(node) ? node : picker;
        }
        return fieldFocusNode();
    }

    /// Returns the current focus target inside the closed range field.
    private Node fieldFocusNode() {
        @Nullable Scene scene = getScene();
        @Nullable Node focusOwner = scene == null ? null : scene.getFocusOwner();
        if (focusOwner != null) {
            if (M3Accessible.containsNode(startEditor, focusOwner)) {
                return startEditor;
            }
            if (M3Accessible.containsNode(startOpenButton, focusOwner)) {
                return startOpenButton;
            }
            if (M3Accessible.containsNode(endEditor, focusOwner)) {
                return endEditor;
            }
            if (M3Accessible.containsNode(endOpenButton, focusOwner)) {
                return endOpenButton;
            }
        }
        return startEditor;
    }

    /// Returns the current focus owner inside popup content when it belongs to the popup scene.
    private @Nullable Node popupFocusOwner() {
        @Nullable Scene popupScene = popupContent.getScene();
        @Nullable Node focusOwner = popupScene == null ? null : popupScene.getFocusOwner();
        if (focusOwner != null && M3Accessible.containsNode(popupContent, focusOwner)
                && M3Accessible.canReach(focusOwner)) {
            return focusOwner;
        }
        return null;
    }

    /// Returns whether this field can reveal the supplied accessibility date target.
    private boolean handlesAccessibleShowTarget(@Nullable Object parameter) {
        return parameter instanceof LocalDate date && !picker.isDateDisabled(date);
    }

    /// Returns whether a show-item request names a target this field can reasonably forward.
    private boolean canAttemptAccessibleShow(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            return true;
        }
        for (Object parameter : parameters) {
            if (canAttemptAccessibleShowParameter(parameter)) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether one show-item parameter can be routed to the popup picker.
    private boolean canAttemptAccessibleShowParameter(@Nullable Object parameter) {
        if (parameter instanceof Number) {
            return true;
        }
        if (parameter instanceof Node node) {
            return M3Accessible.isEffectivelyReachable(node);
        }
        if (handlesAccessibleShowTarget(parameter)) {
            return true;
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                if (canAttemptAccessibleShowParameter(value)) {
                    return true;
                }
            }
            return false;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                if (canAttemptAccessibleShowParameter(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /// Shows the popup, forwards a show-item request to the picker, and focuses the requested item.
    ///
    /// @param parameters optional accessibility target parameters
    /// @return `true` when the popup stayed visible and a picker target accepted focus
    boolean showAccessibleItem(Object... parameters) {
        if (!M3Accessible.canReach(this) || !canAttemptAccessibleShow(parameters)) {
            return false;
        }
        boolean preservePopupFocus = popup.isShowing() && parameters.length == 0 && popupFocusOwner() != null;
        showPicker();
        if (!popup.isShowing()) {
            return false;
        }
        if (!preservePopupFocus) {
            forwardPickerAccessibleAction(AccessibleAction.SHOW_ITEM, parameters);
        }
        return focusPicker();
    }

    /// Forwards an accessibility action to the popup range picker.
    private void forwardPickerAccessibleAction(AccessibleAction action, Object... parameters) {
        if (!isRangeBound()) {
            picker.executeAccessibleAction(action, parameters);
        }
    }

    /// Focuses the preferred node inside the popup picker.
    private boolean focusPicker() {
        if (!popup.isShowing()) {
            return false;
        }

        if (M3Accessible.showItem(this, focusNode())) {
            notifyFocusNodeChanged();
            popupFocusNotifier.refresh();
            return true;
        }
        return false;
    }

    /// Focuses the picker content directly instead of preserving an already focused preset action.
    private boolean focusPickerContent() {
        if (!popup.isShowing()) {
            return false;
        }

        @Nullable Object focusNode = picker.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);
        boolean focused;
        if (focusNode instanceof Node node && M3Accessible.canReach(node)) {
            focused = M3Accessible.showItem(this, node);
        } else {
            focused = M3Accessible.showItem(this, picker);
        }
        if (focused) {
            notifyFocusNodeChanged();
            popupFocusNotifier.refresh();
            return true;
        }
        return false;
    }

    /// Requests focus for the field's current editor or popup focus target.
    ///
    /// @return `true` when the current target accepted focus
    boolean focusAccessibleNode() {
        if (!M3Accessible.canReach(this)) {
            return false;
        }
        if (M3Accessible.showItem(this, focusNode())) {
            notifyFocusNodeChanged();
            popupFocusNotifier.refresh();
            return true;
        }
        return false;
    }

    /// Returns the editor currently focused by the user, or the start editor by default.
    private M3TextField currentEditor() {
        @Nullable Scene scene = getScene();
        @Nullable Node focusOwner = scene == null ? null : scene.getFocusOwner();
        if (focusOwner != null
                && (M3Accessible.containsNode(endEditor, focusOwner)
                || M3Accessible.containsNode(endOpenButton, focusOwner))) {
            return endEditor;
        }
        return startEditor;
    }

    /// Notifies accessibility clients and owner containers about the exposed focus target.
    private void notifyFocusNodeChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
    }

    /// Synchronizes owner popup context and minimum-width state into the popup-hosted picker.
    private void preparePopupForShow() {
        double fieldWidth = Math.max(0.0, getWidth());
        M3Css.setMinWidthIfUnbound(popupContent, Math.max(fieldWidth, popupContent.minWidth(-1.0)));
        popupContent.applyCss();
    }

    /// Applies initial visual state before the popup is shown.
    private void preparePopupForShowAnimation() {
        popupAnimation.stop();
        hidingPopup = false;
        popupContent.setOpacity(0.0);
        popupContent.setScaleX(POPUP_TRANSITION_SCALE);
        popupContent.setScaleY(POPUP_TRANSITION_SCALE);
        popupContent.setTranslateY(popupTransitionOffsetY);
    }

    /// Plays the popup picker enter animation.
    private void playShowAnimation() {
        popupAnimation.stop();
        hidingPopup = false;
        M3MotionSpec spec = M3Animation.fastSpatial(this);
        popupAnimation.configure(spec, 1.0, 1.0, 1.0, popupContent.getTranslateX(), 0.0);
        M3Animation.playFromStart(this, popupAnimation);
    }

    /// Hides the popup picker and optionally restores editor focus.
    private void hidePicker(boolean focusEditor) {
        if (!popup.isShowing()) {
            return;
        }

        if (focusEditor) {
            focusEditorOnHidden = popupOwnerEditor == null ? currentEditor() : popupOwnerEditor;
        }
        if (hidingPopup && popupAnimation.getStatus() == Animation.Status.RUNNING) {
            return;
        }
        M3MotionSpec spec = M3Animation.fastSpatial(this);
        hidingPopup = true;
        popupAnimation.configure(
                spec,
                0.0,
                POPUP_TRANSITION_SCALE,
                POPUP_TRANSITION_SCALE,
                popupContent.getTranslateX(),
                popupTransitionOffsetY
        );
        M3Animation.playFromStart(this, popupAnimation);
    }

    /// Handles popup hidden cleanup and optional focus return.
    private void handlePopupHidden() {
        popupFocusNotifier.stop();
        reachabilityObserver.uninstall();
        popupContextSynchronizer.stop();
        showing.set(false);
        notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
        resetPopupAnimationState();
        @Nullable M3TextField editor = focusEditorOnHidden;
        focusEditorOnHidden = null;
        popupOwnerEditor = null;
        if (M3Accessible.canReach(editor)) {
            M3Accessible.showItem(this, editor);
        }
        notifyFocusNodeChanged();
        popupFocusNotifier.refresh();
    }

    /// Resets transient popup picker animation transforms.
    private void resetPopupAnimationState() {
        popupAnimation.stop();
        hidingPopup = false;
        popupContent.setOpacity(1.0);
        popupContent.setScaleX(1.0);
        popupContent.setScaleY(1.0);
        popupContent.setTranslateY(0.0);
    }

}
