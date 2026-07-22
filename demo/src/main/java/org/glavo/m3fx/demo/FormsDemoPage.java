// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;

import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3CheckBox;
import org.glavo.m3fx.controls.M3DateRangePickerField;
import org.glavo.m3fx.controls.M3DateRangePresets;
import org.glavo.m3fx.controls.M3FormPane;
import org.glavo.m3fx.controls.M3FormRow;
import org.glavo.m3fx.controls.M3FormSection;
import org.glavo.m3fx.controls.M3FormValidator;
import org.glavo.m3fx.controls.M3Switch;
import org.glavo.m3fx.controls.M3TextField;
import org.glavo.m3fx.controls.M3TextInputLayout;
import org.glavo.m3fx.controls.M3TextInputValidators;
import org.glavo.m3fx.controls.M3TextInputVariant;
import org.glavo.m3fx.controls.M3ValidationSummary;
import org.jetbrains.annotations.NotNullByDefault;

import java.time.LocalDate;
import java.util.regex.Pattern;

/// Builds the Forms component showcase page.
@NotNullByDefault
final class FormsDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    FormsDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the form helpers demo page.
    Node createContent() {
        M3TextField displayName = createTextField("Display name", "", M3TextInputVariant.OUTLINED, false);
        M3TextInputLayout displayNameLayout = createTextInputLayout(displayName, "Visible to collaborators");
        displayNameLayout.setValidator(M3TextInputValidators.required("Display name is required"));
        displayNameLayout.setValidateOnFocusLost(true);

        M3TextField email = createTextField("Email", "support@example.com", M3TextInputVariant.OUTLINED, false);
        M3TextInputLayout emailLayout = createTextInputLayout(email, "Used for project notifications");
        emailLayout.setValidator(M3TextInputValidators.pattern(
                Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"),
                "Enter a valid email address"
        ));
        emailLayout.setValidateOnFocusLost(true);

        M3DateRangePickerField availability =
                new M3DateRangePickerField(LocalDate.now().plusDays(2), LocalDate.now().plusDays(6));
        availability.setStartLabelText("Start");
        availability.setEndLabelText("End");
        availability.getPresets().setAll(M3DateRangePresets.common(LocalDate.now(), availability.getPicker().getFirstDayOfWeek()));
        configureResponsiveWidth(availability, 420.0);

        M3Switch notifications = new M3Switch("");
        notifications.setSelected(true);
        M3CheckBox beta = new M3CheckBox();
        beta.setAllowIndeterminate(true);
        beta.setIndeterminate(true);

        M3FormSection account = new M3FormSection(
                "Account",
                "Common fields use the same label column and content alignment."
        );
        account.getContent().addAll(
                new M3FormRow("Display name", "Primary profile label", displayNameLayout),
                new M3FormRow("Email", "Validated on focus loss", emailLayout),
                new M3FormRow("Availability", "Editable start and end dates", availability)
        );

        M3FormSection preferences = new M3FormSection(
                "Preferences",
                "Boolean settings keep labels aligned with selection controls."
        );
        preferences.getContent().addAll(
                new M3FormRow("Notifications", "Receive product and release updates", notifications),
                new M3FormRow("Beta channel", "Tri-state checkbox in a form row", beta)
        );

        M3FormValidator validator = new M3FormValidator(displayNameLayout, emailLayout);
        M3ValidationSummary validationSummary = new M3ValidationSummary(validator);
        validationSummary.setTitleText("Review form fields");
        validationSummary.setEmptyText("All registered fields are valid");
        configureResponsiveWidth(validationSummary, 720.0);

        M3Button validateButton = new M3Button("Validate form", M3ButtonVariant.FILLED);
        validateButton.setOnAction(event -> {
            if (validator.validateAndFocusFirstInvalidInput()) {
                validationSummary.setShowWhenValid(true);
                context.showSnackbar("Form is valid");
            } else {
                validationSummary.setShowWhenValid(false);
                int invalidCount = validator.getInvalidInputCount();
                context.showSnackbar(invalidCount == 1 ? "Fix 1 field" : "Fix " + invalidCount + " fields");
            }
        });

        M3Button clearValidationButton = new M3Button("Clear validation", M3ButtonVariant.OUTLINED);
        clearValidationButton.disableProperty().bind(validator.validationActiveProperty().not());
        clearValidationButton.setOnAction(event -> {
            validator.clearValidation();
            validationSummary.setShowWhenValid(false);
            context.showSnackbar("Validation cleared");
        });

        Node validationActions = createResponsiveActionRow(validateButton, clearValidationButton);

        M3FormSection validation = new M3FormSection(
                "Validation",
                "Group-level validation keeps form feedback and focus movement coordinated."
        );
        validation.getContent().add(new M3FormRow("Actions", "Validate all registered inputs", validationActions));

        validator.validate();

        M3FormPane form = createFormPane(validationSummary, account, preferences, validation);
        form.getStyleClass().add("demo-form");
        form.setContentPadding(18.0);
        configureResponsiveWidth(form, 760.0);

        return createGallery(createFullWidthShowcaseGroup("Structured Form", form));
    }

    /// Creates a form pane sample with initial items.
    private static M3FormPane createFormPane(Node... items) {
        M3FormPane formPane = new M3FormPane();
        formPane.getItems().addAll(items);
        return formPane;
    }
}
