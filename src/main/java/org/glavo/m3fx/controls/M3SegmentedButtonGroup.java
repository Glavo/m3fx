package org.glavo.m3fx.controls;

import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// A Material Design 3 segmented button group that lays out adjacent segments.
@NotNullByDefault
public class M3SegmentedButtonGroup extends HBox {
    /// The base style class for m3fx segmented button groups.
    public static final String STYLE_CLASS = "m3-segmented-button-group";

    /// The style class applied when a segmented button is the only segment.
    public static final String SINGLE_SEGMENT_STYLE_CLASS = "m3-segmented-button-single";

    /// The style class applied to the first segmented button in a group.
    public static final String FIRST_SEGMENT_STYLE_CLASS = "m3-segmented-button-first";

    /// The style class applied to middle segmented buttons in a group.
    public static final String MIDDLE_SEGMENT_STYLE_CLASS = "m3-segmented-button-middle";

    /// The style class applied to the last segmented button in a group.
    public static final String LAST_SEGMENT_STYLE_CLASS = "m3-segmented-button-last";

    /// The default spacing that lets adjacent segment borders overlap.
    private static final double DEFAULT_SPACING = -1.0;

    /// Updates segment position style classes when children change.
    private final ListChangeListener<Node> childrenListener = change -> {
        while (change.next()) {
            for (Node child : change.getRemoved()) {
                if (child instanceof M3SegmentedButton button) {
                    clearSegmentStyle(button);
                }
            }
        }
        updateSegmentStyles();
    };

    /// Creates an empty segmented button group.
    public M3SegmentedButtonGroup() {
        initialize();
    }

    /// Creates a segmented button group with the supplied buttons.
    public M3SegmentedButtonGroup(M3SegmentedButton... buttons) {
        initialize();
        Objects.requireNonNull(buttons, "buttons");
        for (M3SegmentedButton button : buttons) {
            Objects.requireNonNull(button, "button");
        }
        getChildren().addAll(buttons);
    }

    /// Adds base style classes and child list listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setSpacing(DEFAULT_SPACING);
        getChildren().addListener(childrenListener);
        updateSegmentStyles();
    }

    /// Applies first, middle, last, or single segment style classes.
    private void updateSegmentStyles() {
        int segmentCount = 0;
        for (Node child : getChildren()) {
            if (child instanceof M3SegmentedButton) {
                segmentCount++;
            }
        }

        int segmentIndex = 0;
        for (Node child : getChildren()) {
            if (child instanceof M3SegmentedButton button) {
                M3ControlStyles.replaceVariant(
                        button,
                        segmentStyleClass(segmentIndex, segmentCount),
                        SINGLE_SEGMENT_STYLE_CLASS,
                        FIRST_SEGMENT_STYLE_CLASS,
                        MIDDLE_SEGMENT_STYLE_CLASS,
                        LAST_SEGMENT_STYLE_CLASS
                );
                segmentIndex++;
            }
        }
    }

    /// Returns the segment position style class for an index.
    private static String segmentStyleClass(int index, int count) {
        if (count == 1) {
            return SINGLE_SEGMENT_STYLE_CLASS;
        }
        if (index == 0) {
            return FIRST_SEGMENT_STYLE_CLASS;
        }
        if (index == count - 1) {
            return LAST_SEGMENT_STYLE_CLASS;
        }
        return MIDDLE_SEGMENT_STYLE_CLASS;
    }

    /// Removes all segment position style classes from a button.
    private static void clearSegmentStyle(M3SegmentedButton button) {
        button.getStyleClass().remove(SINGLE_SEGMENT_STYLE_CLASS);
        button.getStyleClass().remove(FIRST_SEGMENT_STYLE_CLASS);
        button.getStyleClass().remove(MIDDLE_SEGMENT_STYLE_CLASS);
        button.getStyleClass().remove(LAST_SEGMENT_STYLE_CLASS);
    }
}
