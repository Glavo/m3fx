// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.docs;

import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for Material Design documentation URL metadata.
@NotNullByDefault
final class M3MaterialDocsTest {
    /// Verifies the shared root URLs.
    @Test
    void rootUrlsUseMaterialDesignSite() {
        assertEquals("https://m3.material.io/", M3MaterialDocs.ROOT);
        assertEquals("https://m3.material.io/components", M3MaterialDocs.COMPONENTS);
        assertEquals("https://m3.material.io/styles", M3MaterialDocs.STYLES);
    }

    /// Verifies helper-generated component and style URLs.
    @Test
    void helpersCreateOverviewUrls() {
        assertEquals(
                "https://m3.material.io/components/buttons/overview",
                M3MaterialDocs.componentUrl("buttons")
        );
        assertEquals(
                "https://m3.material.io/styles/typography/overview",
                M3MaterialDocs.styleUrl("typography")
        );
    }

    /// Verifies representative component URL constants used by demo pages and Javadocs.
    @Test
    void componentConstantsPointToOverviewPages() {
        assertEquals("https://m3.material.io/components/top-app-bar/overview", M3MaterialDocs.TOP_APP_BAR);
        assertEquals("https://m3.material.io/components/button-groups/overview", M3MaterialDocs.BUTTON_GROUPS);
        assertEquals("https://m3.material.io/components/extended-fab/overview", M3MaterialDocs.EXTENDED_FAB);
        assertEquals("https://m3.material.io/components/fab-menu/overview", M3MaterialDocs.FAB_MENU);
        assertEquals("https://m3.material.io/components/floating-action-button/overview", M3MaterialDocs.FLOATING_ACTION_BUTTON);
        assertEquals("https://m3.material.io/components/split-button/overview", M3MaterialDocs.SPLIT_BUTTON);
        assertEquals("https://m3.material.io/components/loading-indicator/overview", M3MaterialDocs.LOADING_INDICATOR);
        assertEquals("https://m3.material.io/components/progress-indicators/overview", M3MaterialDocs.PROGRESS_INDICATORS);
        assertEquals("https://m3.material.io/components/navigation-drawer/overview", M3MaterialDocs.NAVIGATION_DRAWER);
        assertEquals("https://m3.material.io/components/text-fields/overview", M3MaterialDocs.TEXT_FIELDS);
        assertEquals("https://m3.material.io/components/toolbars/overview", M3MaterialDocs.TOOLBARS);
    }

    /// Verifies style URL constants used by style-oriented demo pages.
    @Test
    void styleConstantsPointToOverviewPages() {
        assertEquals("https://m3.material.io/styles/typography/overview", M3MaterialDocs.TYPOGRAPHY);
        assertEquals("https://m3.material.io/styles/icons/overview", M3MaterialDocs.ICONS);
    }

    /// Verifies public URLs are normalized and do not contain accidental duplicate separators.
    @Test
    void publicUrlsAreNormalized() {
        String[] urls = {
                M3MaterialDocs.ROOT,
                M3MaterialDocs.COMPONENTS,
                M3MaterialDocs.STYLES,
                M3MaterialDocs.TOP_APP_BAR,
                M3MaterialDocs.BADGES,
                M3MaterialDocs.BUTTON_GROUPS,
                M3MaterialDocs.BUTTONS,
                M3MaterialDocs.EXTENDED_FAB,
                M3MaterialDocs.FAB_MENU,
                M3MaterialDocs.FLOATING_ACTION_BUTTON,
                M3MaterialDocs.ICON_BUTTONS,
                M3MaterialDocs.SEGMENTED_BUTTONS,
                M3MaterialDocs.SPLIT_BUTTON,
                M3MaterialDocs.CARDS,
                M3MaterialDocs.CAROUSEL,
                M3MaterialDocs.CHECKBOX,
                M3MaterialDocs.CHIPS,
                M3MaterialDocs.DATE_PICKERS,
                M3MaterialDocs.TIME_PICKERS,
                M3MaterialDocs.DIALOGS,
                M3MaterialDocs.DIVIDER,
                M3MaterialDocs.LISTS,
                M3MaterialDocs.LOADING_INDICATOR,
                M3MaterialDocs.PROGRESS_INDICATORS,
                M3MaterialDocs.MENUS,
                M3MaterialDocs.NAVIGATION_BAR,
                M3MaterialDocs.NAVIGATION_DRAWER,
                M3MaterialDocs.NAVIGATION_RAIL,
                M3MaterialDocs.RADIO_BUTTON,
                M3MaterialDocs.SEARCH,
                M3MaterialDocs.BOTTOM_SHEETS,
                M3MaterialDocs.SIDE_SHEETS,
                M3MaterialDocs.SLIDERS,
                M3MaterialDocs.SNACKBAR,
                M3MaterialDocs.SWITCH,
                M3MaterialDocs.TABS,
                M3MaterialDocs.TEXT_FIELDS,
                M3MaterialDocs.TOOLBARS,
                M3MaterialDocs.TOOLTIPS,
                M3MaterialDocs.TYPOGRAPHY,
                M3MaterialDocs.ICONS
        };
        for (String url : urls) {
            assertTrue(url.startsWith(M3MaterialDocs.ROOT), url);
            assertFalse(url.substring(M3MaterialDocs.ROOT.length()).contains("//"), url);
            assertFalse(url.endsWith("/overview/"), url);
        }
    }
}
