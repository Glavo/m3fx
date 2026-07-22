// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.QuadCurveTo;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.scene.transform.Affine;
import org.glavo.m3fx.animation.M3DoubleAnimatable;
import org.glavo.m3fx.controls.M3IconButton;
import org.glavo.m3fx.controls.M3TextArea;
import org.glavo.m3fx.controls.M3TextInput;
import org.glavo.m3fx.controls.M3TextInputLayout;
import org.glavo.m3fx.controls.M3TextInputVariant;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3InternalIcon;
import org.glavo.m3fx.internal.M3NodeLayout;
import org.glavo.m3fx.internal.M3NodeTransition;
import org.glavo.m3fx.internal.M3TextInputSupport;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Objects;

import static org.glavo.m3fx.controls.M3TextInputLayout.CLEAR_BUTTON_STYLE_CLASS;
import static org.glavo.m3fx.controls.M3TextInputLayout.COUNTER_STYLE_CLASS;
import static org.glavo.m3fx.controls.M3TextInputLayout.INPUT_CONTAINER_STYLE_CLASS;
import static org.glavo.m3fx.controls.M3TextInputLayout.INPUT_STYLE_CLASS;
import static org.glavo.m3fx.controls.M3TextInputLayout.LABEL_STYLE_CLASS;
import static org.glavo.m3fx.controls.M3TextInputLayout.LEADING_STYLE_CLASS;
import static org.glavo.m3fx.controls.M3TextInputLayout.OUTLINE_STYLE_CLASS;
import static org.glavo.m3fx.controls.M3TextInputLayout.SUPPORTING_ROW_STYLE_CLASS;
import static org.glavo.m3fx.controls.M3TextInputLayout.SUPPORTING_TEXT_STYLE_CLASS;
import static org.glavo.m3fx.controls.M3TextInputLayout.TRAILING_STYLE_CLASS;

/// Provides the default visual presentation for [M3TextInputLayout].
///
/// The skin attaches the configured input and adornments while installed. Replacing or disposing the skin detaches
/// those nodes and restores the wrapped input's padding, horizontal translation, and style classes to the values
/// observed when the skin installed it. Semantic properties, validation state, and accessibility behavior remain
/// owned by the control.
///
/// Floating-label motion uses one interruptible scalar channel. Spatial spring overshoot is preserved instead of
/// clipping progress to the closed unit interval. The outlined notch follows the current transformed label bounds
/// and expands continuously with the same transition progress.
///
/// See [Material Design text fields](https://m3.material.io/components/text-fields/overview).
@NotNullByDefault
public final class M3TextInputLayoutSkin extends SkinBase<M3TextInputLayout> {
    /// The pseudo-class used while the wrapped input has keyboard focus.
    private static final javafx.css.PseudoClass FOCUSED_PSEUDO_CLASS =
            javafx.css.PseudoClass.getPseudoClass("focused");

    /// The pseudo-class used while an error is visible.
    private static final javafx.css.PseudoClass ERROR_PSEUDO_CLASS =
            javafx.css.PseudoClass.getPseudoClass("error");

    /// The pseudo-class used while the label is in its minimized semantic state.
    private static final javafx.css.PseudoClass FLOATING_PSEUDO_CLASS =
            javafx.css.PseudoClass.getPseudoClass("floating");

    /// The vertical distance between the input container and supporting row.
    private static final double ROW_SPACING = 4.0;

    /// The horizontal input reservation for an occupied adornment slot.
    private static final double ADORNED_HORIZONTAL_PADDING = 48.0;

    /// The fallback horizontal text inset before input CSS has been resolved.
    private static final double TEXT_HORIZONTAL_PADDING = 16.0;

    /// The horizontal clearance maintained between an outlined border and label.
    private static final double FLOATING_LABEL_HORIZONTAL_PADDING = 4.0;

    /// The fallback expanded scale used when computed font metrics are unavailable.
    private static final double DEFAULT_EXPANDED_LABEL_SCALE = 4.0 / 3.0;

    /// The top position of a minimized label in a filled field.
    private static final double FILLED_FLOATING_LABEL_TOP_MARGIN = 4.0;

    /// The top padding used by a labeled single-line filled input.
    private static final double LABELED_SINGLE_LINE_TOP_PADDING = 20.0;

    /// The top padding used by a labeled single-line outlined input.
    private static final double OUTLINED_LABELED_SINGLE_LINE_TOP_PADDING = 11.0;

    /// The top padding used by a labeled multiline filled input.
    private static final double LABELED_MULTILINE_TOP_PADDING = 28.0;

    /// The normalized visibility threshold for floating-label motion.
    private static final double LABEL_PROGRESS_VISIBILITY_THRESHOLD = 0.001;

    /// The initial vertical offset used when a supporting row appears.
    private static final double SUPPORTING_ROW_TRANSITION_OFFSET_Y = -4.0;

    /// The initial scale used when the built-in clear button appears.
    private static final double TRAILING_TRANSITION_START_SCALE = 0.86;

    /// The internal vertical layout container.
    private final VBox container = new VBox(ROW_SPACING);

    /// The stack containing the input, outline, label, and adornment slots.
    private final InputContainer inputContainer = new InputContainer();

    /// The outlined field border.
    private final Path outlinePath = new Path();

    /// The stable path elements forming the outlined border.
    private final PathElement @Unmodifiable [] outlineElements = {
            new MoveTo(),
            new LineTo(),
            new MoveTo(),
            new LineTo(),
            new QuadCurveTo(),
            new LineTo(),
            new QuadCurveTo(),
            new LineTo(),
            new QuadCurveTo(),
            new LineTo(),
            new QuadCurveTo()
    };

    /// The text geometry displayed inside or above the input.
    private final Text label = new Text();

    /// The affine transform carrying every floating-label presentation channel.
    private final Affine labelTransform = new Affine();

    /// The logical leading adornment slot.
    private final StackPane leadingSlot = new StackPane();

    /// The logical trailing adornment slot.
    private final StackPane trailingSlot = new StackPane();

    /// The row containing supporting and counter text.
    private final HBox supportingRow = new HBox();

    /// The supporting or error message label.
    private final Label supportingLabel = new Label();

    /// The flexible space separating supporting and counter text.
    private final Region supportingSpacer = new Region();

    /// The character counter label.
    private final Label counterLabel = new Label();

    /// The built-in clear-text action.
    private final M3IconButton clearButton = new M3IconButton(new M3InternalIcon(
            M3InternalIcon.Glyph.CLOSE,
            M3InternalIcon.ColorRole.ON_SURFACE_VARIANT
    ));

    /// The continuous expanded-to-minimized label progress.
    private final DoubleProperty labelProgress =
            new SimpleDoubleProperty(this, "labelProgress") {
                /// Applies the current progress without causing a layout pass.
                @Override
                protected void invalidated() {
                    updateLabelPresentation();
                }
            };

    /// The interruptible animation controlling [#labelProgress].
    private final M3DoubleAnimatable labelProgressAnimation =
            new M3DoubleAnimatable(getSkinnable(), labelProgress, LABEL_PROGRESS_VISIBILITY_THRESHOLD);

    /// The reusable supporting-row transition.
    private final M3NodeTransition supportingRowAnimation = new M3NodeTransition(supportingRow);

    /// The reusable clear-button transition.
    private final M3NodeTransition trailingAnimation = new M3NodeTransition(clearButton);

    /// Handles changes to the wrapped input.
    private final ChangeListener<@Nullable TextInputControl> inputListener =
            (observable, oldValue, newValue) -> updateInput(newValue);

    /// Handles changes to label text.
    private final InvalidationListener labelTextListener = observable -> updateLabelState();

    /// Handles changes to the semantic floating-label target.
    private final InvalidationListener labelFloatingListener = observable -> updateLabelFloatingState();

    /// Handles changes to the leading adornment.
    private final InvalidationListener leadingListener = observable -> updateLeading();

    /// Handles changes to the custom trailing adornment.
    private final InvalidationListener trailingListener = observable -> updateTrailing();

    /// Handles changes to supporting-row content or visibility.
    private final InvalidationListener supportingContentListener = observable -> updateSupportingRow();

    /// Handles changes to clear-button availability.
    private final InvalidationListener clearButtonListener = observable -> updateTrailing();

    /// Handles effective node-orientation changes.
    private final InvalidationListener nodeOrientationListener = observable -> updateNodeOrientationLayout();

    /// Handles wrapped-input text changes needed by presentation state.
    private final ChangeListener<String> inputTextListener =
            (observable, oldValue, newValue) -> {
                updateTrailing();
                updateSupportingRow();
            };

    /// Handles wrapped-input editability changes that affect the clear action.
    private final ChangeListener<Boolean> inputEditableListener =
            (observable, oldValue, newValue) -> updateTrailing();

    /// Handles wrapped-input focus changes.
    private final ChangeListener<Boolean> inputFocusListener =
            (observable, oldValue, newValue) -> updateFocusState();

    /// Handles wrapped-input variant changes.
    private final ChangeListener<M3TextInputVariant> inputVariantListener =
            (observable, oldValue, newValue) -> updateInputVariant();

    /// Handles wrapped-input error-state changes.
    private final ChangeListener<Boolean> inputErrorListener =
            (observable, oldValue, newValue) -> updateErrorState();

    /// Handles wrapped-input shape changes.
    private final ChangeListener<Number> inputShapeListener =
            (observable, oldValue, newValue) -> inputContainer.requestLayout();

    /// Captures application- or CSS-owned input padding changes.
    private final ChangeListener<Insets> inputPaddingListener =
            (observable, oldValue, newValue) -> handleInputPaddingChanged(newValue);

    /// Captures application-owned horizontal input translation changes.
    private final ChangeListener<Number> inputTranslateXListener =
            (observable, oldValue, newValue) -> handleInputTranslateXChanged(newValue.doubleValue());

    /// Remeasures label endpoints when either participating font or label padding changes.
    private final InvalidationListener labelMetricsListener = observable -> inputContainer.requestLayout();

    /// Rebuilds stable outline geometry when CSS changes stroke width.
    private final InvalidationListener outlineMetricsListener = observable -> inputContainer.requestLayout();

    /// The input currently installed in this skin.
    private @Nullable TextInputControl installedInput;

    /// The input padding observed before this skin applied layout reservations.
    private @Nullable Insets installedInputBasePadding;

    /// The horizontal input translation observed before this skin applied RTL correction.
    private double installedInputBaseTranslateX;

    /// Whether a padding write originates from this skin.
    private boolean applyingInputPadding;

    /// Whether a translation write originates from this skin.
    private boolean applyingInputTranslation;

    /// Whether this skin added the input style class that must be removed on release.
    private boolean inputStyleClassAdded;

    /// The node currently occupying the trailing slot.
    private @Nullable Node installedTrailing;

    /// Whether the trailing slot has completed its initial synchronization.
    private boolean trailingInitialized;

    /// Whether label progress has been synchronized with a visible label.
    private boolean labelProgressInitialized;

    /// Whether the label was visible at the previous synchronization.
    private boolean labelVisible;

    /// Whether the supporting row was visible at the previous synchronization.
    private boolean supportingRowVisible;

    /// Whether supporting-row motion has completed its initial synchronization.
    private boolean supportingRowMotionInitialized;

    /// The last supporting message written to the label.
    private String renderedSupportingMessage = "";

    /// The last counter text written to the label.
    private String renderedCounterText = "";

    /// Whether the supporting message is currently included in layout.
    private boolean supportingMessageVisible;

    /// Whether the character counter is currently included in layout.
    private boolean counterVisible;

    /// Whether the reusable outline elements are attached.
    private boolean outlineElementsAttached;

    /// Whether cached stable outline geometry is valid.
    private boolean outlineGeometryValid;

    /// The cached top-border y coordinate.
    private double outlineTop;

    /// The cached leading end of the drawable top-border segment.
    private double outlineTopStart;

    /// The cached trailing end of the drawable top-border segment.
    private double outlineTopEnd;

    /// The expanded label x endpoint in input-container coordinates.
    private double expandedLabelX;

    /// The expanded label y endpoint in input-container coordinates.
    private double expandedLabelY;

    /// The minimized label x endpoint in input-container coordinates.
    private double minimizedLabelX;

    /// The minimized label y endpoint in input-container coordinates.
    private double minimizedLabelY;

    /// The stable untransformed label width.
    private double laidOutLabelWidth;

    /// The stable untransformed label height.
    private double laidOutLabelHeight;

    /// The expanded label scale derived from the current input and label fonts.
    private double expandedLabelScale = DEFAULT_EXPANDED_LABEL_SCALE;

    /// Whether cached label endpoint geometry is valid.
    private boolean labelGeometryValid;

    /// Creates the default skin for the supplied text input layout.
    ///
    /// @param control the control presented by this skin
    /// @throws NullPointerException if control is null
    public M3TextInputLayoutSkin(M3TextInputLayout control) {
        super(Objects.requireNonNull(control, "control"));
        initializeNodes();
        installControlListeners();
        updateInput(control.getInput());
        updateNodeOrientationLayout();
        getChildren().setAll(container);
    }

    /// Initializes the skin-owned presentation tree.
    private void initializeNodes() {
        M3TextInputLayout control = getSkinnable();

        container.setManaged(false);
        container.setFillWidth(true);
        container.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());

        inputContainer.getStyleClass().add(INPUT_CONTAINER_STYLE_CLASS);
        inputContainer.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());

        outlinePath.getStyleClass().add(OUTLINE_STYLE_CLASS);
        outlinePath.setFill(null);
        outlinePath.setManaged(false);
        outlinePath.setMouseTransparent(true);
        outlinePath.strokeWidthProperty().addListener(outlineMetricsListener);

        label.getStyleClass().add(LABEL_STYLE_CLASS);
        label.setManaged(false);
        label.setVisible(false);
        label.setMouseTransparent(true);
        label.setBoundsType(TextBoundsType.LOGICAL);
        label.setTextOrigin(VPos.TOP);
        label.getTransforms().add(labelTransform);
        label.fontProperty().addListener(labelMetricsListener);

        leadingSlot.getStyleClass().add(LEADING_STYLE_CLASS);
        trailingSlot.getStyleClass().add(TRAILING_STYLE_CLASS);

        supportingRow.getStyleClass().add(SUPPORTING_ROW_STYLE_CLASS);
        supportingRow.nodeOrientationProperty().bind(control.effectiveNodeOrientationProperty());
        supportingRow.setOpacity(0.0);
        supportingRow.setVisible(false);
        supportingRow.setManaged(false);
        supportingLabel.getStyleClass().add(SUPPORTING_TEXT_STYLE_CLASS);
        supportingLabel.setWrapText(true);
        supportingLabel.setVisible(false);
        supportingLabel.setManaged(false);
        supportingSpacer.setVisible(false);
        supportingSpacer.setManaged(false);
        counterLabel.getStyleClass().add(COUNTER_STYLE_CLASS);
        counterLabel.setVisible(false);
        counterLabel.setManaged(false);
        HBox.setHgrow(supportingSpacer, Priority.ALWAYS);
        supportingRow.getChildren().setAll(supportingLabel, supportingSpacer, counterLabel);

        M3ControlStyles.add(clearButton, CLEAR_BUTTON_STYLE_CLASS);
        clearButton.setAccessibleText("Clear text");
        clearButton.setOnAction(event -> control.clearText());

        inputContainer.getChildren().setAll(outlinePath, label, leadingSlot, trailingSlot);
        container.getChildren().setAll(inputContainer, supportingRow);
    }

    /// Installs every listener whose source is owned by the control.
    private void installControlListeners() {
        M3TextInputLayout control = getSkinnable();
        control.inputProperty().addListener(inputListener);
        control.labelTextProperty().addListener(labelTextListener);
        control.labelFloatingProperty().addListener(labelFloatingListener);
        control.leadingProperty().addListener(leadingListener);
        control.trailingProperty().addListener(trailingListener);
        control.supportingTextProperty().addListener(supportingContentListener);
        control.errorTextProperty().addListener(supportingContentListener);
        control.validationErrorTextProperty().addListener(supportingContentListener);
        control.characterCounterVisibleProperty().addListener(supportingContentListener);
        control.characterLimitProperty().addListener(supportingContentListener);
        control.clearButtonEnabledProperty().addListener(clearButtonListener);
        control.effectiveNodeOrientationProperty().addListener(nodeOrientationListener);
    }

    /// Removes every listener whose source is owned by the control.
    private void uninstallControlListeners() {
        M3TextInputLayout control = getSkinnable();
        control.inputProperty().removeListener(inputListener);
        control.labelTextProperty().removeListener(labelTextListener);
        control.labelFloatingProperty().removeListener(labelFloatingListener);
        control.leadingProperty().removeListener(leadingListener);
        control.trailingProperty().removeListener(trailingListener);
        control.supportingTextProperty().removeListener(supportingContentListener);
        control.errorTextProperty().removeListener(supportingContentListener);
        control.validationErrorTextProperty().removeListener(supportingContentListener);
        control.characterCounterVisibleProperty().removeListener(supportingContentListener);
        control.characterLimitProperty().removeListener(supportingContentListener);
        control.clearButtonEnabledProperty().removeListener(clearButtonListener);
        control.effectiveNodeOrientationProperty().removeListener(nodeOrientationListener);
    }

    /// Releases all presentation resources and restores the wrapped input.
    @Override
    public void dispose() {
        uninstallControlListeners();
        labelProgressAnimation.stop();
        supportingRowAnimation.stop();
        trailingAnimation.stop();

        @Nullable TextInputControl input = installedInput;
        if (input != null) {
            uninstallInput(input);
        }

        outlinePath.strokeWidthProperty().removeListener(outlineMetricsListener);
        label.fontProperty().removeListener(labelMetricsListener);
        clearButton.setOnAction(null);
        clearButton.setOpacity(1.0);
        clearButton.setScaleX(1.0);
        clearButton.setScaleY(1.0);

        container.nodeOrientationProperty().unbind();
        inputContainer.nodeOrientationProperty().unbind();
        supportingRow.nodeOrientationProperty().unbind();

        leadingSlot.getChildren().clear();
        trailingSlot.getChildren().clear();
        supportingRow.getChildren().clear();
        inputContainer.getChildren().clear();
        container.getChildren().clear();
        getChildren().clear();
        super.dispose();
    }

    /// Replaces the installed input and synchronizes presentation state.
    ///
    /// @param newInput the input currently exposed by the control
    private void updateInput(@Nullable TextInputControl newInput) {
        @Nullable TextInputControl actualOldInput = installedInput;
        boolean restoreFocus = isFocusInside(actualOldInput);
        if (actualOldInput != null) {
            uninstallInput(actualOldInput);
        }

        installedInput = null;
        installedInputBasePadding = null;
        installedInputBaseTranslateX = 0.0;
        installedTrailing = null;
        trailingInitialized = false;
        labelProgressAnimation.stop();
        labelProgressInitialized = false;
        supportingRowAnimation.stop();
        supportingRowMotionInitialized = false;
        invalidateLabelGeometry();
        clearOutlinePath();

        if (newInput != null) {
            installInput(newInput);
        }

        boolean visible = newInput != null;
        inputContainer.setVisible(visible);
        inputContainer.setManaged(visible);
        updateLabelState();
        updateLeading();
        updateTrailing();
        updateSupportingRow();
        updateFocusState();
        updateErrorState();
        updateInputVariant();
        if (restoreFocus && newInput != null) {
            M3Accessible.showItem(getSkinnable(), newInput);
        }
        getSkinnable().requestLayout();
    }

    /// Installs one wrapped input into the skin-owned container.
    ///
    /// @param input the input to install
    private void installInput(TextInputControl input) {
        inputStyleClassAdded = !input.getStyleClass().contains(INPUT_STYLE_CLASS);
        M3ControlStyles.add(input, INPUT_STYLE_CLASS);
        input.textProperty().addListener(inputTextListener);
        input.editableProperty().addListener(inputEditableListener);
        input.focusedProperty().addListener(inputFocusListener);
        input.fontProperty().addListener(labelMetricsListener);
        input.paddingProperty().addListener(inputPaddingListener);
        input.translateXProperty().addListener(inputTranslateXListener);

        if (input instanceof M3TextInput textInput) {
            textInput.variantProperty().addListener(inputVariantListener);
            textInput.errorProperty().addListener(inputErrorListener);
            textInput.containerShapeProperty().addListener(inputShapeListener);
        }

        bindPresentationDisableState(input);
        installedInput = input;
        installedInputBasePadding = input.getPadding();
        installedInputBaseTranslateX = input.getTranslateX();
        inputContainer.getChildren().add(1, input);
        updateInputPadding();
    }

    /// Removes listeners and skin-owned state from one wrapped input.
    ///
    /// @param input the input to uninstall
    private void uninstallInput(TextInputControl input) {
        input.textProperty().removeListener(inputTextListener);
        input.editableProperty().removeListener(inputEditableListener);
        input.focusedProperty().removeListener(inputFocusListener);
        input.fontProperty().removeListener(labelMetricsListener);
        input.paddingProperty().removeListener(inputPaddingListener);
        input.translateXProperty().removeListener(inputTranslateXListener);

        if (input instanceof M3TextInput textInput) {
            textInput.variantProperty().removeListener(inputVariantListener);
            textInput.errorProperty().removeListener(inputErrorListener);
            textInput.containerShapeProperty().removeListener(inputShapeListener);
        }

        unbindPresentationDisableState();
        restoreInputMetrics(input);
        if (inputStyleClassAdded) {
            input.getStyleClass().remove(INPUT_STYLE_CLASS);
        }
        inputStyleClassAdded = false;
        inputContainer.getChildren().remove(input);
        installedInput = null;
    }

    /// Binds skin-owned presentation nodes to the installed input disabled state.
    ///
    /// @param input the installed input
    private void bindPresentationDisableState(TextInputControl input) {
        supportingRow.disableProperty().bind(input.disabledProperty());
        outlinePath.disableProperty().bind(input.disabledProperty());
        label.disableProperty().bind(input.disabledProperty());
        leadingSlot.disableProperty().bind(input.disabledProperty());
        trailingSlot.disableProperty().bind(input.disabledProperty());
    }

    /// Removes disabled-state bindings from skin-owned nodes.
    private void unbindPresentationDisableState() {
        supportingRow.disableProperty().unbind();
        outlinePath.disableProperty().unbind();
        label.disableProperty().unbind();
        leadingSlot.disableProperty().unbind();
        trailingSlot.disableProperty().unbind();
        supportingRow.setDisable(false);
        outlinePath.setDisable(false);
        label.setDisable(false);
        leadingSlot.setDisable(false);
        trailingSlot.setDisable(false);
    }

    /// Restores metrics captured before this skin installed its layout corrections.
    ///
    /// @param input the input being released
    private void restoreInputMetrics(TextInputControl input) {
        @Nullable Insets basePadding = installedInputBasePadding;
        if (basePadding != null) {
            writeInputPadding(input, basePadding, false);
        }
        setInputTranslateX(input, installedInputBaseTranslateX);
    }

    /// Updates the leading adornment slot.
    private void updateLeading() {
        @Nullable Node previous =
                leadingSlot.getChildren().isEmpty() ? null : leadingSlot.getChildren().get(0);
        @Nullable Node leading = getSkinnable().getLeading();
        boolean restoreFocus = previous != leading && isFocusInside(previous);
        leadingSlot.getChildren().clear();
        if (leading != null) {
            leadingSlot.getChildren().add(leading);
        }
        updateAdornmentSlot(leadingSlot, leading);
        inputContainer.requestLayout();
        updateInputPadding();
        if (restoreFocus) {
            restoreInputFocus();
        }
    }

    /// Updates the effective trailing adornment slot.
    private void updateTrailing() {
        @Nullable Node trailing = effectiveTrailing();
        @Nullable Node previous = installedTrailing;
        if (trailingInitialized && previous == trailing) {
            return;
        }

        boolean restoreFocus = previous != trailing && isFocusInside(previous);
        trailingAnimation.stop();
        if (previous == clearButton) {
            clearButton.setOpacity(1.0);
            clearButton.setScaleX(1.0);
            clearButton.setScaleY(1.0);
        }

        installedTrailing = trailing;
        trailingSlot.getChildren().clear();
        if (trailing != null) {
            trailingSlot.getChildren().add(trailing);
        }
        updateAdornmentSlot(trailingSlot, trailing);
        updateTrailingPseudoClasses();
        updateTrailingMotion(previous, trailing);
        inputContainer.requestLayout();
        updateInputPadding();
        if (restoreFocus) {
            restoreInputFocus();
        }
        trailingInitialized = true;
    }

    /// Returns the node that should occupy the trailing slot.
    ///
    /// @return the custom trailing node, active clear button, or null
    private @Nullable Node effectiveTrailing() {
        @Nullable Node trailing = getSkinnable().getTrailing();
        if (trailing != null) {
            return trailing;
        }
        return isClearButtonActive() ? clearButton : null;
    }

    /// Returns whether the clear button should be shown.
    ///
    /// @return true when clearing is enabled and the input has content
    private boolean isClearButtonActive() {
        return getSkinnable().isClearButtonEnabled()
                && getSkinnable().getTrailing() == null
                && installedInput != null
                && installedInput.isEditable()
                && !installedInput.textProperty().isBound()
                && getSkinnable().getCharacterCount() > 0;
    }

    /// Updates common slot visibility and mouse behavior.
    ///
    /// @param slot    the slot to update
    /// @param content the current slot content, or null
    private static void updateAdornmentSlot(StackPane slot, @Nullable Node content) {
        boolean visible = content != null;
        slot.setVisible(visible);
        slot.setManaged(visible);
        slot.setMouseTransparent(content == null);
    }

    /// Updates label content, pseudo-classes, geometry, and motion target.
    private void updateLabelState() {
        M3TextInputLayout control = getSkinnable();
        String text = control.getLabelText();
        boolean visible = installedInput != null && !text.isBlank();
        boolean visibilityChanged = visible != labelVisible;

        if (!label.getText().equals(text)) {
            label.setText(text);
            invalidateLabelGeometry();
        }
        labelVisible = visible;
        label.setVisible(visible && labelGeometryValid);
        label.pseudoClassStateChanged(FOCUSED_PSEUDO_CLASS, isInputFocused());
        label.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, hasVisualErrorState());

        updateLabelFloatingState();
        if (visibilityChanged) {
            updateInputPadding();
        }
        if (!visible) {
            invalidateLabelGeometry();
        }
        inputContainer.requestLayout();
        updateOutlineNotch();
    }

    /// Synchronizes the semantic floating pseudo-class and animation target.
    private void updateLabelFloatingState() {
        boolean floating = getSkinnable().isLabelFloating();
        label.pseudoClassStateChanged(FLOATING_PSEUDO_CLASS, floating);
        updateLabelMotion(labelVisible, floating);
    }

    /// Retargets floating-label progress without clipping spatial spring overshoot.
    ///
    /// @param visible  whether the label is currently rendered
    /// @param floating whether the semantic target is minimized
    private void updateLabelMotion(boolean visible, boolean floating) {
        if (!visible) {
            labelProgressAnimation.snapTo(0.0);
            labelProgressInitialized = false;
            return;
        }

        double target = floating ? 1.0 : 0.0;
        if (!labelProgressInitialized || getSkinnable().getScene() == null) {
            labelProgressAnimation.snapTo(target);
            labelProgressInitialized = true;
            return;
        }
        if (Double.compare(labelProgressAnimation.getTargetValue(), target) != 0) {
            labelProgressAnimation.animateTo(target, M3Animation.fastSpatial(getSkinnable()));
        }
    }

    /// Updates focus pseudo-classes.
    private void updateFocusState() {
        boolean focused = isInputFocused();
        label.pseudoClassStateChanged(FOCUSED_PSEUDO_CLASS, focused);
        outlinePath.pseudoClassStateChanged(FOCUSED_PSEUDO_CLASS, focused);
        trailingSlot.pseudoClassStateChanged(FOCUSED_PSEUDO_CLASS, focused);
    }

    /// Updates error pseudo-classes.
    private void updateErrorState() {
        boolean error = hasVisualErrorState();
        label.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, error);
        outlinePath.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, error);
        trailingSlot.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, error);
        supportingLabel.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, error);
        counterLabel.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, error);
        supportingRow.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, error);
    }

    /// Updates trailing-slot focus and error pseudo-classes.
    private void updateTrailingPseudoClasses() {
        trailingSlot.pseudoClassStateChanged(FOCUSED_PSEUDO_CLASS, isInputFocused());
        trailingSlot.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, hasVisualErrorState());
    }

    /// Updates presentation after the input variant changes.
    private void updateInputVariant() {
        boolean outlined = isOutlinedInput();
        outlinePath.setVisible(outlined);
        if (!outlined) {
            clearOutlinePath();
        } else {
            inputContainer.requestLayout();
        }
        updateInputPadding();
        inputContainer.requestLayout();
    }

    /// Updates supporting text, counter content, and row motion.
    private void updateSupportingRow() {
        M3TextInputLayout control = getSkinnable();
        String message = displayedSupportingText();
        boolean showMessage = !message.isEmpty();
        boolean showCounter = control.isCharacterCounterVisible() && installedInput != null;
        boolean showRow = showMessage || showCounter;
        boolean visibilityChanged = showRow != supportingRowVisible;
        String counterText = showCounter ? characterCounterText() : "";

        if (!renderedSupportingMessage.equals(message)) {
            supportingLabel.setText(message);
            renderedSupportingMessage = message;
        }
        if (showMessage != supportingMessageVisible) {
            supportingLabel.setVisible(showMessage);
            supportingLabel.setManaged(showMessage);
            supportingMessageVisible = showMessage;
        }
        if (!renderedCounterText.equals(counterText)) {
            counterLabel.setText(counterText);
            renderedCounterText = counterText;
        }
        if (showCounter != counterVisible) {
            supportingSpacer.setVisible(showCounter);
            supportingSpacer.setManaged(showCounter);
            counterLabel.setVisible(showCounter);
            counterLabel.setManaged(showCounter);
            counterVisible = showCounter;
        }
        if (visibilityChanged) {
            supportingRow.setVisible(showRow);
            supportingRow.setManaged(showRow);
        }

        updateErrorState();
        if (visibilityChanged || !supportingRowMotionInitialized) {
            updateSupportingRowMotion(showRow, visibilityChanged);
        }
        supportingRowVisible = showRow;
    }

    /// Returns the supporting message currently selected by precedence.
    ///
    /// @return explicit error, validation error, or supporting text
    private String displayedSupportingText() {
        String error = displayedErrorText();
        return error.isEmpty() ? getSkinnable().getSupportingText() : error;
    }

    /// Returns the visible explicit or validation error.
    ///
    /// @return the visible error text, or an empty string
    private String displayedErrorText() {
        String error = getSkinnable().getErrorText();
        return error.isEmpty() ? getSkinnable().getValidationErrorText() : error;
    }

    /// Returns the formatted character counter.
    ///
    /// @return the current character count and optional limit
    private String characterCounterText() {
        M3TextInputLayout control = getSkinnable();
        int count = control.getCharacterCount();
        int limit = control.getCharacterLimit();
        return limit >= 0 ? count + " / " + limit : Integer.toString(count);
    }

    /// Returns whether the layout-owned error state is visible.
    ///
    /// @return true when error text or character overflow is visible
    private boolean hasErrorState() {
        return !displayedErrorText().isEmpty() || getSkinnable().isCharacterLimitExceeded();
    }

    /// Returns whether either the layout or input supplies an error state.
    ///
    /// @return true when visual error styling is active
    private boolean hasVisualErrorState() {
        @Nullable M3TextInput textInput = getSkinnable().getTextInput();
        return hasErrorState() || textInput != null && textInput.isError();
    }

    /// Returns whether the wrapped input currently has keyboard focus.
    ///
    /// @return true when the installed input is focused
    private boolean isInputFocused() {
        return installedInput != null && installedInput.isFocused();
    }

    /// Returns whether the installed input is outlined.
    ///
    /// @return true for the outlined variant
    private boolean isOutlinedInput() {
        @Nullable M3TextInput textInput = getSkinnable().getTextInput();
        return textInput != null && textInput.getVariant() == M3TextInputVariant.OUTLINED;
    }

    /// Captures external input padding and reapplies skin-owned reservations.
    ///
    /// @param padding the new externally supplied padding
    private void handleInputPaddingChanged(Insets padding) {
        if (applyingInputPadding) {
            return;
        }
        installedInputBasePadding = Objects.requireNonNull(padding, "padding");
        updateInputPadding();
        inputContainer.requestLayout();
    }

    /// Captures external input translation and reapplies RTL correction.
    ///
    /// @param translateX the new externally supplied translation
    private void handleInputTranslateXChanged(double translateX) {
        if (applyingInputTranslation) {
            return;
        }
        installedInputBaseTranslateX = translateX;
        @Nullable TextInputControl input = installedInput;
        if (input != null) {
            updateInputAreaOffset(input);
        }
    }

    /// Applies stable adornment and label reservations to the input padding.
    private void updateInputPadding() {
        @Nullable TextInputControl input = installedInput;
        @Nullable Insets basePadding = installedInputBasePadding;
        if (input == null || basePadding == null) {
            return;
        }

        double leading = resolvedInputLeadingInset(basePadding);
        double trailing = resolvedInputTrailingInset(basePadding);
        double inputLeading = leading;
        double inputTrailing = trailing;
        if (needsRightToLeftTrailingTextReservation(input)) {
            inputLeading = Math.max(inputLeading, ADORNED_HORIZONTAL_PADDING);
            inputTrailing = inputTrailingInset(basePadding);
        } else if (needsRightToLeftFilledLeadingTextReservation(input)) {
            inputTrailing = Math.max(inputTrailing, ADORNED_HORIZONTAL_PADDING);
        }

        double top = labelVisible
                ? Math.max(basePadding.getTop(), labeledTopPadding(input, basePadding.getTop()))
                : basePadding.getTop();
        double left = physicalLeftInset(inputLeading, inputTrailing);
        double right = physicalRightInset(inputLeading, inputTrailing);
        Insets current = input.getPadding();
        if (Double.compare(current.getTop(), top) != 0
                || Double.compare(current.getRight(), right) != 0
                || Double.compare(current.getBottom(), basePadding.getBottom()) != 0
                || Double.compare(current.getLeft(), left) != 0) {
            writeInputPadding(input, new Insets(top, right, basePadding.getBottom(), left), true);
        }
        updateInputAreaOffset(input);
    }

    /// Writes input padding when it is not bound.
    ///
    /// @param input       the input to update
    /// @param padding     the padding to write
    /// @param helperOwned whether M3FX retains metric ownership
    private void writeInputPadding(TextInputControl input, Insets padding, boolean helperOwned) {
        if (input.paddingProperty().isBound()) {
            return;
        }
        applyingInputPadding = true;
        try {
            if (helperOwned) {
                M3Css.setPaddingAsHelperOwned(input, padding);
            } else {
                M3Css.setPaddingWithoutOwnershipIfUnbound(input, padding);
            }
        } finally {
            applyingInputPadding = false;
        }
    }

    /// Applies the physical RTL correction to the input translation.
    ///
    /// @param input the installed input
    private void updateInputAreaOffset(TextInputControl input) {
        @Nullable Insets basePadding = installedInputBasePadding;
        double correction = basePadding != null && needsRightToLeftLeadingOnlyOffset(input)
                ? Math.max(0.0,
                resolvedInputLeadingInset(basePadding) - resolvedInputTrailingInset(basePadding))
                : 0.0;
        setInputTranslateX(input, installedInputBaseTranslateX + correction);
    }

    /// Writes horizontal input translation when it is not bound.
    ///
    /// @param input      the input to update
    /// @param translateX the physical translation
    private void setInputTranslateX(TextInputControl input, double translateX) {
        if (input.translateXProperty().isBound()) {
            return;
        }
        applyingInputTranslation = true;
        try {
            input.setTranslateX(translateX);
        } finally {
            applyingInputTranslation = false;
        }
    }

    /// Returns the resolved logical leading input inset.
    ///
    /// @param basePadding the unmodified input padding
    /// @return the logical leading inset
    private double resolvedInputLeadingInset(Insets basePadding) {
        double base = inputLeadingInset(basePadding);
        return getSkinnable().getLeading() == null ? base : Math.max(base, ADORNED_HORIZONTAL_PADDING);
    }

    /// Returns the resolved logical trailing input inset.
    ///
    /// @param basePadding the unmodified input padding
    /// @return the logical trailing inset
    private double resolvedInputTrailingInset(Insets basePadding) {
        double base = inputTrailingInset(basePadding);
        return effectiveTrailing() == null ? base : Math.max(base, ADORNED_HORIZONTAL_PADDING);
    }

    /// Returns whether an outlined RTL field needs a leading-only translation correction.
    ///
    /// @param input the installed input
    /// @return true when the correction is required
    private boolean needsRightToLeftLeadingOnlyOffset(TextInputControl input) {
        return isOutlinedInput()
                && isRightToLeft()
                && getSkinnable().getLeading() != null
                && effectiveTrailing() == null
                && input instanceof TextField;
    }

    /// Returns whether a filled RTL field needs balanced leading reservation.
    ///
    /// @param input the installed input
    /// @return true when the reservation is required
    private boolean needsRightToLeftFilledLeadingTextReservation(TextInputControl input) {
        return !isOutlinedInput()
                && isRightToLeft()
                && getSkinnable().getLeading() != null
                && effectiveTrailing() == null
                && input instanceof TextField;
    }

    /// Returns whether an outlined RTL field needs trailing-action reservation.
    ///
    /// @param input the installed input
    /// @return true when the reservation is required
    private boolean needsRightToLeftTrailingTextReservation(TextInputControl input) {
        return isOutlinedInput()
                && isRightToLeft()
                && getSkinnable().getLeading() == null
                && effectiveTrailing() != null
                && input instanceof TextField;
    }

    /// Returns the logical leading inset from physical padding.
    ///
    /// @param padding the physical input padding
    /// @return the logical leading inset
    private double inputLeadingInset(Insets padding) {
        return isRightToLeft() ? padding.getRight() : padding.getLeft();
    }

    /// Returns the logical trailing inset from physical padding.
    ///
    /// @param padding the physical input padding
    /// @return the logical trailing inset
    private double inputTrailingInset(Insets padding) {
        return isRightToLeft() ? padding.getLeft() : padding.getRight();
    }

    /// Converts logical edge insets to a physical left inset.
    ///
    /// @param leading  the logical leading inset
    /// @param trailing the logical trailing inset
    /// @return the physical left inset
    private double physicalLeftInset(double leading, double trailing) {
        return isRightToLeft() ? trailing : leading;
    }

    /// Converts logical edge insets to a physical right inset.
    ///
    /// @param leading  the logical leading inset
    /// @param trailing the logical trailing inset
    /// @return the physical right inset
    private double physicalRightInset(double leading, double trailing) {
        return isRightToLeft() ? leading : trailing;
    }

    /// Returns whether the control is laid out right-to-left.
    ///
    /// @return true for effective RTL orientation
    private boolean isRightToLeft() {
        return M3NodeLayout.isRightToLeft(getSkinnable());
    }

    /// Returns the top input padding required by a visible label.
    ///
    /// @param input          the installed input
    /// @param baseTopPadding the unmodified top padding
    /// @return the reserved top padding
    private double labeledTopPadding(TextInputControl input, double baseTopPadding) {
        if (input instanceof M3TextArea) {
            return isOutlinedInput() ? baseTopPadding : LABELED_MULTILINE_TOP_PADDING;
        }
        return isOutlinedInput() ? OUTLINED_LABELED_SINGLE_LINE_TOP_PADDING : LABELED_SINGLE_LINE_TOP_PADDING;
    }

    /// Updates logical edge alignment after node orientation changes.
    private void updateNodeOrientationLayout() {
        StackPane.setAlignment(leadingSlot, Pos.CENTER_LEFT);
        StackPane.setAlignment(trailingSlot, Pos.CENTER_RIGHT);
        updateInputPadding();
        invalidateLabelGeometry();
        inputContainer.requestLayout();
        getSkinnable().requestLayout();
    }

    /// Lays out label endpoint geometry from stable input metrics.
    ///
    /// @param width  the input-container width
    /// @param height the input-container height
    private void layoutLabelGeometry(double width, double height) {
        if (!labelVisible || width <= 0.0 || height <= 0.0) {
            invalidateLabelGeometry();
            return;
        }

        @Nullable Insets basePadding = installedInputBasePadding;
        double leadingInset = basePadding == null
                ? (getSkinnable().getLeading() == null
                ? TEXT_HORIZONTAL_PADDING : ADORNED_HORIZONTAL_PADDING)
                : resolvedInputLeadingInset(basePadding);
        double trailingInset = basePadding == null
                ? (effectiveTrailing() == null
                ? TEXT_HORIZONTAL_PADDING : ADORNED_HORIZONTAL_PADDING)
                : resolvedInputTrailingInset(basePadding);
        double availableWidth = Math.max(0.0, width - leadingInset - trailingInset);
        expandedLabelScale = resolvedExpandedLabelScale();
        double maximumEndpointScale = Math.max(1.0, expandedLabelScale);
        Bounds labelBounds = label.getLayoutBounds();
        double labelWidth = Math.min(labelBounds.getWidth(), availableWidth / maximumEndpointScale);
        double labelHeight = labelBounds.getHeight();
        if (!Double.isFinite(labelWidth)
                || !Double.isFinite(labelHeight)
                || labelWidth <= 0.0
                || labelHeight <= 0.0) {
            invalidateLabelGeometry();
            return;
        }

        // JavaFX mirrors the input container for RTL. Keeping both endpoints in logical coordinates avoids applying
        // a second physical mirror and preserves the same leading edge throughout the scale transition.
        expandedLabelX = leadingInset;
        minimizedLabelX = leadingInset;
        expandedLabelY = expandedLabelY(height, labelHeight * expandedLabelScale);
        minimizedLabelY = isOutlinedInput()
                ? -labelHeight / 2.0
                : FILLED_FLOATING_LABEL_TOP_MARGIN;
        laidOutLabelWidth = labelWidth;
        laidOutLabelHeight = labelHeight;
        label.relocate(0.0, 0.0);
        labelGeometryValid = true;
        label.setVisible(true);
        updateLabelPresentation();
    }

    /// Returns the expanded label y endpoint using the original input padding.
    ///
    /// @param height      the input-container height
    /// @param labelHeight the untransformed label height
    /// @return the expanded y coordinate
    private double expandedLabelY(double height, double labelHeight) {
        @Nullable TextInputControl input = installedInput;
        @Nullable Insets basePadding = installedInputBasePadding;
        if (input == null || basePadding == null) {
            return Math.max(0.0, (height - labelHeight) / 2.0);
        }
        if (input instanceof M3TextArea) {
            return basePadding.getTop();
        }

        double contentHeight = Math.max(0.0,
                height - basePadding.getTop() - basePadding.getBottom());
        return basePadding.getTop() + Math.max(0.0, (contentHeight - labelHeight) / 2.0);
    }

    /// Returns the expanded scale represented by the current computed input and label fonts.
    ///
    /// The label itself uses minimized typography. Scaling it by the returned ratio makes the resting endpoint match
    /// the wrapped input font while leaving the floating endpoint at an unscaled, natively rasterized size.
    ///
    /// @return a finite, positive expanded scale
    private double resolvedExpandedLabelScale() {
        @Nullable TextInputControl input = installedInput;
        double inputFontSize = input == null ? Double.NaN : input.getFont().getSize();
        double labelFontSize = label.getFont().getSize();
        double scale = inputFontSize / labelFontSize;
        return Double.isFinite(scale) && scale > 0.0
                ? scale
                : DEFAULT_EXPANDED_LABEL_SCALE;
    }

    /// Applies the current unbounded progress to label transform channels.
    private void updateLabelPresentation() {
        if (!labelGeometryValid) {
            closeOutlineNotch();
            return;
        }

        double progress = labelProgress.get();
        double scale = interpolate(expandedLabelScale, 1.0, progress);
        double x = interpolate(expandedLabelX, minimizedLabelX, progress);
        double y = interpolate(expandedLabelY, minimizedLabelY, progress);
        labelTransform.setToTransform(scale, 0.0, x, 0.0, scale, y);
        updateOutlineNotch();
    }

    /// Invalidates label geometry and removes stale rendered bounds.
    private void invalidateLabelGeometry() {
        labelGeometryValid = false;
        label.setVisible(false);
        laidOutLabelWidth = 0.0;
        laidOutLabelHeight = 0.0;
        expandedLabelScale = DEFAULT_EXPANDED_LABEL_SCALE;
        label.relocate(0.0, 0.0);
        labelTransform.setToIdentity();
        closeOutlineNotch();
    }

    /// Rebuilds stable outline geometry for the current container bounds.
    ///
    /// @param width  the input-container width
    /// @param height the input-container height
    private void layoutOutlineGeometry(double width, double height) {
        if (!isOutlinedInput() || width <= 0.0 || height <= 0.0) {
            clearOutlinePath();
            return;
        }

        double strokeWidth = Math.max(1.0, outlinePath.getStrokeWidth());
        double inset = strokeWidth / 2.0;
        double left = inset;
        double top = inset;
        double right = width - inset;
        double bottom = height - inset;
        if (right <= left || bottom <= top) {
            clearOutlinePath();
            return;
        }

        double radius = outlineRadius(right - left, bottom - top);
        double topStart = left + radius;
        double topEnd = right - radius;
        if (topEnd < topStart) {
            clearOutlinePath();
            return;
        }

        ensureOutlineElements();
        outlineTop = top;
        outlineTopStart = topStart;
        outlineTopEnd = topEnd;
        outlineGeometryValid = true;

        ((MoveTo) outlineElements[0]).setX(topStart);
        ((MoveTo) outlineElements[0]).setY(top);
        ((LineTo) outlineElements[1]).setY(top);
        ((MoveTo) outlineElements[2]).setY(top);
        ((LineTo) outlineElements[3]).setX(topEnd);
        ((LineTo) outlineElements[3]).setY(top);
        updateOutlineCurve((QuadCurveTo) outlineElements[4], right, top, right, top + radius);
        ((LineTo) outlineElements[5]).setX(right);
        ((LineTo) outlineElements[5]).setY(bottom - radius);
        updateOutlineCurve((QuadCurveTo) outlineElements[6], right, bottom, right - radius, bottom);
        ((LineTo) outlineElements[7]).setX(left + radius);
        ((LineTo) outlineElements[7]).setY(bottom);
        updateOutlineCurve((QuadCurveTo) outlineElements[8], left, bottom, left, bottom - radius);
        ((LineTo) outlineElements[9]).setX(left);
        ((LineTo) outlineElements[9]).setY(top + radius);
        updateOutlineCurve((QuadCurveTo) outlineElements[10], left, top, topStart, top);
        updateOutlineNotch();
    }

    /// Updates only the two animated top-border endpoints.
    ///
    /// The cutout follows the same progress as the label instead of being derived from its distance to the border.
    /// This keeps the opening visible throughout the transition and makes it grow from the logical leading edge.
    private void updateOutlineNotch() {
        if (!outlineGeometryValid || !outlineElementsAttached) {
            return;
        }
        if (!labelGeometryValid || !label.isVisible()) {
            closeOutlineNotch();
            return;
        }

        double progress = labelProgress.get();
        double reveal = clamp(progress, 0.0, 1.0);
        if (reveal <= 0.0) {
            closeOutlineNotch();
            return;
        }

        double scale = interpolate(expandedLabelScale, 1.0, reveal);
        if (!Double.isFinite(scale) || scale <= 0.0) {
            closeOutlineNotch();
            return;
        }

        double currentX = interpolate(expandedLabelX, minimizedLabelX, reveal);
        double currentWidth = laidOutLabelWidth * scale;
        double notchStart = clamp(
                currentX - FLOATING_LABEL_HORIZONTAL_PADDING * reveal,
                outlineTopStart,
                outlineTopEnd
        );
        double notchEnd = clamp(
                currentX + (currentWidth + FLOATING_LABEL_HORIZONTAL_PADDING) * reveal,
                notchStart,
                outlineTopEnd
        );
        setOutlineNotch(notchStart, notchEnd);
    }

    /// Closes the top-border notch without rebuilding stable outline geometry.
    private void closeOutlineNotch() {
        if (outlineGeometryValid && outlineElementsAttached) {
            setOutlineNotch(outlineTopEnd, outlineTopEnd);
        }
    }

    /// Writes the current top-border notch endpoints.
    ///
    /// @param start the end of the first top segment
    /// @param end   the start of the second top segment
    private void setOutlineNotch(double start, double end) {
        ((LineTo) outlineElements[1]).setX(start);
        ((MoveTo) outlineElements[2]).setX(end);
    }

    /// Attaches the reusable outline path elements.
    private void ensureOutlineElements() {
        if (!outlineElementsAttached) {
            outlinePath.getElements().setAll(outlineElements);
            outlineElementsAttached = true;
        }
    }

    /// Clears path elements and all cached outline geometry.
    private void clearOutlinePath() {
        outlineGeometryValid = false;
        outlineTop = 0.0;
        outlineTopStart = 0.0;
        outlineTopEnd = 0.0;
        if (outlineElementsAttached) {
            outlinePath.getElements().clear();
            outlineElementsAttached = false;
        }
    }

    /// Updates one cached outline curve.
    ///
    /// @param curve    the curve to update
    /// @param controlX the control-point x coordinate
    /// @param controlY the control-point y coordinate
    /// @param x        the endpoint x coordinate
    /// @param y        the endpoint y coordinate
    private static void updateOutlineCurve(
            QuadCurveTo curve,
            double controlX,
            double controlY,
            double x,
            double y
    ) {
        curve.setControlX(controlX);
        curve.setControlY(controlY);
        curve.setX(x);
        curve.setY(y);
    }

    /// Returns the outline radius constrained by current bounds.
    ///
    /// @param width  the drawable outline width
    /// @param height the drawable outline height
    /// @return the constrained radius
    private double outlineRadius(double width, double height) {
        @Nullable M3TextInput textInput = getSkinnable().getTextInput();
        double radius = textInput == null
                ? M3TextInputSupport.DEFAULT_CONTAINER_SHAPE
                : textInput.getContainerShape();
        return clamp(radius, 0.0, Math.min(width, height) / 2.0);
    }

    /// Starts the clear-button entry transition when needed.
    ///
    /// @param previous the previous trailing node, or null
    /// @param trailing the new trailing node, or null
    private void updateTrailingMotion(@Nullable Node previous, @Nullable Node trailing) {
        if (previous == trailing || trailing != clearButton) {
            return;
        }
        M3TextInputLayout control = getSkinnable();
        if (control.getScene() == null) {
            clearButton.setOpacity(1.0);
            clearButton.setScaleX(1.0);
            clearButton.setScaleY(1.0);
            return;
        }

        clearButton.setOpacity(0.0);
        clearButton.setScaleX(TRAILING_TRANSITION_START_SCALE);
        clearButton.setScaleY(TRAILING_TRANSITION_START_SCALE);
        trailingAnimation.configure(
                M3Animation.fastEffects(control),
                1.0,
                1.0,
                1.0,
                clearButton.getTranslateX(),
                clearButton.getTranslateY()
        );
        M3Animation.playFromStart(control, trailingAnimation);
    }

    /// Updates the supporting-row appearance transition.
    ///
    /// @param showRow           whether the row is visible
    /// @param visibilityChanged whether row visibility changed
    private void updateSupportingRowMotion(boolean showRow, boolean visibilityChanged) {
        M3TextInputLayout control = getSkinnable();
        if (!supportingRowMotionInitialized || control.getScene() == null) {
            supportingRow.setOpacity(showRow ? 1.0 : 0.0);
            supportingRow.setTranslateY(0.0);
            supportingRowMotionInitialized = true;
            return;
        }
        supportingRowAnimation.stop();
        if (!visibilityChanged || !showRow) {
            supportingRow.setOpacity(showRow ? 1.0 : 0.0);
            supportingRow.setTranslateY(0.0);
            return;
        }

        supportingRow.setOpacity(0.0);
        supportingRow.setTranslateY(SUPPORTING_ROW_TRANSITION_OFFSET_Y);
        supportingRowAnimation.configure(
                M3Animation.fastSpatial(control),
                1.0,
                supportingRow.getScaleX(),
                supportingRow.getScaleY(),
                supportingRow.getTranslateX(),
                0.0
        );
        M3Animation.playFromStart(control, supportingRowAnimation);
    }

    /// Returns whether focus belongs to a node or its descendants.
    ///
    /// @param node the candidate subtree root, or null
    /// @return true when the current focus owner is inside the subtree
    private boolean isFocusInside(@Nullable Node node) {
        if (node == null || getSkinnable().getScene() == null) {
            return false;
        }
        @Nullable Node focusOwner = getSkinnable().getScene().getFocusOwner();
        return focusOwner != null && M3Accessible.containsNode(node, focusOwner);
    }

    /// Restores keyboard focus to the installed input when reachable.
    private void restoreInputFocus() {
        @Nullable TextInputControl input = installedInput;
        if (M3Accessible.canReach(input)) {
            M3Accessible.showItem(getSkinnable(), input);
        }
    }

    /// Clamps a value to an inclusive range.
    ///
    /// @param value   the value to constrain
    /// @param minimum the inclusive lower bound
    /// @param maximum the inclusive upper bound
    /// @return the constrained value
    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    /// Interpolates linearly between two scalar values.
    ///
    /// @param start    the value at zero progress
    /// @param end      the value at unit progress
    /// @param progress the unbounded progress
    /// @return the interpolated value
    private static double interpolate(double start, double end, double progress) {
        return start + (end - start) * progress;
    }

    /// Computes the minimum width from the internal container.
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

    /// Computes the minimum height from the internal container.
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

    /// Computes the preferred width from the internal container.
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

    /// Computes the preferred height from the internal container.
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

    /// Computes the maximum width from the internal container.
    @Override
    protected double computeMaxWidth(
            double height,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return leftInset + container.maxWidth(height) + rightInset;
    }

    /// Computes the maximum height from the internal container.
    @Override
    protected double computeMaxHeight(
            double width,
            double topInset,
            double rightInset,
            double bottomInset,
            double leftInset
    ) {
        return topInset + container.maxHeight(width) + bottomInset;
    }

    /// Lays out the internal container in the control content bounds.
    @Override
    protected void layoutChildren(double x, double y, double width, double height) {
        container.resizeRelocate(x, y, width, height);
    }

    /// Lays out managed input children before updating cached label and outline geometry.
    @NotNullByDefault
    private final class InputContainer extends StackPane {
        /// Lays out stable children and refreshes endpoint geometry.
        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            layoutOutlineGeometry(getWidth(), getHeight());
            layoutLabelGeometry(getWidth(), getHeight());
        }
    }

}
