// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;

import org.glavo.m3fx.controls.M3PasswordField;
import org.glavo.m3fx.controls.M3TextArea;
import org.glavo.m3fx.controls.M3TextField;
import org.glavo.m3fx.controls.M3TextInputLayout;
import org.glavo.m3fx.controls.M3TextInputValidators;
import org.glavo.m3fx.controls.M3TextInputVariant;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.regex.Pattern;

/// Builds the TextFields component showcase page.
@NotNullByDefault
final class TextFieldsDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    TextFieldsDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the text field component page.
    Node createContent() {
        M3TextField filled = createTextField("Filled text field", "", M3TextInputVariant.FILLED, false);
        M3TextField filledText = createTextField("Filled with text", "support@example.com", M3TextInputVariant.FILLED, false);
        filledText.setPrefWidth(340.0);
        M3TextField filledDisabled = createTextField("Disabled filled", "Read only", M3TextInputVariant.FILLED, true);
        M3TextField outlined = createTextField("Outlined text field", "", M3TextInputVariant.OUTLINED, false);
        M3TextField outlinedDisabled =
                createTextField("Disabled outlined", "Read only", M3TextInputVariant.OUTLINED, true);
        M3TextField outlinedText = createTextField("Outlined with text", "M3FX", M3TextInputVariant.OUTLINED, false);
        outlinedText.setPrefWidth(320.0);
        M3PasswordField password = new M3PasswordField("");
        password.setVariant(M3TextInputVariant.OUTLINED);
        password.setPromptText("Password");
        password.setPrefWidth(320.0);
        M3TextField filledError = createTextField("Filled error", "Invalid value", M3TextInputVariant.FILLED, false);
        filledError.setError(true);
        M3TextField outlinedError = createTextField("Outlined error", "", M3TextInputVariant.OUTLINED, false);
        outlinedError.setError(true);
        M3PasswordField passwordError = new M3PasswordField("");
        passwordError.setVariant(M3TextInputVariant.OUTLINED);
        passwordError.setPromptText("Password error");
        passwordError.setError(true);
        passwordError.setPrefWidth(280.0);
        M3TextArea filledArea = createTextArea(
                "Filled text area",
                "Write longer notes across multiple lines.",
                M3TextInputVariant.FILLED
        );
        M3TextArea outlinedArea = createTextArea(
                "Outlined text area",
                "Material text areas share field colors but keep multi-line height tokens.",
                M3TextInputVariant.OUTLINED
        );
        M3TextArea areaError = createTextArea(
                "Text area error",
                "This content needs review.",
                M3TextInputVariant.FILLED
        );
        areaError.setError(true);
        M3TextInputLayout filledLayout = createTextInputLayout(filled, "Supporting text");
        M3TextInputLayout filledTextLayout = createTextInputLayout(filledText, "Email address");
        filledTextLayout.setLeading(createSurfaceVariantIcon("email"));
        filledTextLayout.setClearButtonEnabled(true);
        filledTextLayout.setCharacterCounterVisible(true);
        filledTextLayout.setCharacterLimit(32);
        M3TextInputLayout filledDisabledLayout = createTextInputLayout(filledDisabled, "Disabled supporting text");
        filledDisabledLayout.setLeading(createSurfaceVariantIcon("lock"));
        M3TextInputLayout outlinedLayout = createTextInputLayout(outlined, "Outlined supporting text");
        M3TextInputLayout outlinedDisabledLayout =
                createTextInputLayout(outlinedDisabled, "Disabled outlined supporting text");
        outlinedDisabledLayout.setLeading(createSurfaceVariantIcon("lock"));
        M3TextInputLayout outlinedTextLayout = createTextInputLayout(outlinedText, "Project name");
        outlinedTextLayout.setLeading(createSurfaceVariantIcon("text"));
        outlinedTextLayout.setCharacterCounterVisible(true);
        outlinedTextLayout.setCharacterLimit(24);
        outlinedTextLayout.setCharacterLimitEnforced(true);
        M3TextInputLayout passwordLayout = createTextInputLayout(password, "At least 8 characters");
        passwordLayout.setTrailing(createIconButton("visibility"));
        M3TextField validatedEmail = createTextField("Validated email", "support", M3TextInputVariant.OUTLINED, false);
        validatedEmail.setPrefWidth(340.0);
        M3TextInputLayout validatedEmailLayout = createTextInputLayout(validatedEmail, "Validation runs on focus loss");
        validatedEmailLayout.setValidator(M3TextInputValidators.required("Email is required"));
        validatedEmailLayout.getValidators().add(M3TextInputValidators.pattern(
                Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"),
                "Use an email address"
        ));
        validatedEmailLayout.validate();
        M3TextField requiredProject = createTextField("Required project", "", M3TextInputVariant.FILLED, false);
        M3TextInputLayout requiredProjectLayout = createTextInputLayout(requiredProject, "Required field");
        requiredProjectLayout.setValidator(M3TextInputValidators.required("Project name is required"));
        requiredProjectLayout.setValidateOnTextChange(true);
        M3TextInputLayout filledErrorLayout = createTextInputLayout(filledError, "Supporting text");
        filledErrorLayout.setErrorText("Use a valid value");
        M3TextInputLayout outlinedErrorLayout = createTextInputLayout(outlinedError, "Supporting text");
        outlinedErrorLayout.setLeading(createErrorIcon("error"));
        outlinedErrorLayout.setErrorText("This field is required");
        M3TextInputLayout passwordErrorLayout = createTextInputLayout(passwordError, "Supporting text");
        passwordErrorLayout.setErrorText("Password cannot be empty");
        M3TextInputLayout filledAreaLayout = createTextInputLayout(filledArea, "Filled multi-line input");
        M3TextInputLayout outlinedAreaLayout = createTextInputLayout(outlinedArea, "Outlined multi-line input");
        outlinedAreaLayout.setCharacterCounterVisible(true);
        outlinedAreaLayout.setCharacterLimit(96);
        M3TextInputLayout areaErrorLayout = createTextInputLayout(areaError, "Supporting text");
        areaErrorLayout.setErrorText("Review this text before continuing");
        return createGallery(
                createShowcaseGroup("Filled", filledLayout, filledTextLayout),
                createShowcaseGroup("Outlined", outlinedLayout, outlinedTextLayout, passwordLayout),
                createShowcaseGroup("Disabled", filledDisabledLayout, outlinedDisabledLayout),
                createShowcaseGroup("Validation", validatedEmailLayout, requiredProjectLayout),
                createShowcaseGroup("Error", filledErrorLayout, outlinedErrorLayout, passwordErrorLayout, areaErrorLayout),
                createShowcaseGroup("Text Areas", filledAreaLayout, outlinedAreaLayout)
        );
    }

    /// Creates a text area for the page gallery.
    private static M3TextArea createTextArea(
            String prompt,
            String text,
            M3TextInputVariant variant
    ) {
        M3TextArea textArea = new M3TextArea(text);
        textArea.setVariant(variant);
        textArea.setPromptText(prompt);
        textArea.setPrefWidth(360.0);
        return textArea;
    }
}
