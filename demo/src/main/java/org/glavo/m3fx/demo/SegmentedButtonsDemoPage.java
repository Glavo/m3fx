// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.scene.Node;

import org.glavo.m3fx.controls.M3IconSize;
import org.glavo.m3fx.controls.M3IconVariant;
import org.glavo.m3fx.controls.M3SegmentedButton;
import org.glavo.m3fx.controls.M3SegmentedButtonGroup;
import org.glavo.m3fx.controls.M3SelectionMode;
import org.jetbrains.annotations.NotNullByDefault;

/// Builds the SegmentedButtons component showcase page.
@NotNullByDefault
final class SegmentedButtonsDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    SegmentedButtonsDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the segmented button component page.
    Node createContent() {
        M3SegmentedButtonGroup dateRange = createDayWeekMonthSegmentedGroup();

        M3SegmentedButton lowPriority = createSegmentedButton("Low", "task");
        M3SegmentedButton mediumPriority = createSegmentedButton("Medium", "schedule");
        mediumPriority.setSelected(true);
        M3SegmentedButton highPriority = createSegmentedButton("High", "warning");
        M3SegmentedButtonGroup priority =
                createSegmentedButtonGroup(lowPriority, mediumPriority, highPriority);
        priority.getItems().get(2).setDisable(true);

        M3SegmentedButtonGroup channels = createSegmentedButtonGroup(
                createSegmentedButton("Email", "email"),
                createSegmentedButton("Chat", "group"),
                createSegmentedButton("Push", "notifications")
        );
        channels.clearSelection();
        channels.setSelectionMode(M3SelectionMode.MULTIPLE);
        channels.selectIndex(0);
        channels.selectIndex(2);

        return createGallery(
                createShowcaseGroup("Text With Selection Indicator", dateRange),
                createShowcaseGroup("Icon And Label", priority),
                createShowcaseGroup("Icon Multi Select", channels)
        );
    }

    /// Creates a segmented button sample with an 18dp leading SVG icon.
    private static M3SegmentedButton createSegmentedButton(String text, String iconName) {
        return new M3SegmentedButton(
                text,
                createDemoIcon(iconName, M3IconSize.SMALL, M3IconVariant.ON_SURFACE)
        );
    }
}
