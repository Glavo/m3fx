// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import org.jetbrains.annotations.NotNullByDefault;

import java.util.Locale;

/// Resolves localized presentation text associated with deterministic model identifiers.
@NotNullByDefault
final class HMCLDemoModelText {
    /// Prevents utility-class instantiation.
    private HMCLDemoModelText() {
    }

    /// Returns the localized provider description for an account.
    ///
    /// @param strings the localization service
    /// @param account the displayed account
    /// @return the localized provider description
    static String accountSubtitle(HMCLDemoStrings strings, HMCLDemoAccount account) {
        return strings.get("accounts.type." + account.type().name().toLowerCase(Locale.ROOT));
    }

    /// Returns the localized description for an instance.
    ///
    /// @param strings the localization service
    /// @param instance the displayed instance
    /// @return the localized instance description
    static String instanceDescription(HMCLDemoStrings strings, HMCLDemoInstance instance) {
        String id = localizationId(instance);
        return strings.get("fixture.instance." + id + ".description");
    }

    /// Returns the localized last-played value for an instance.
    ///
    /// @param strings the localization service
    /// @param instance the displayed instance
    /// @return the localized last-played value
    static String instanceLastPlayed(HMCLDemoStrings strings, HMCLDemoInstance instance) {
        String id = localizationId(instance);
        return strings.get("fixture.instance." + id + ".last_played");
    }

    /// Returns the localized summary for a Discover item.
    ///
    /// @param strings the localization service
    /// @param content the displayed catalog content
    /// @return the localized content summary
    static String contentSummary(HMCLDemoStrings strings, HMCLDemoContent content) {
        return strings.get("fixture.content." + content.id() + ".summary");
    }

    /// Returns the fixture localization identifier inherited by generated copies.
    ///
    /// @param instance the displayed instance
    /// @return the identifier used by the resource bundle
    private static String localizationId(HMCLDemoInstance instance) {
        if (instance.id().startsWith("demo-instance-")) {
            return "new";
        }
        int copyMarker = instance.id().indexOf("-copy-");
        return copyMarker < 0 ? instance.id() : instance.id().substring(0, copyMarker);
    }
}
