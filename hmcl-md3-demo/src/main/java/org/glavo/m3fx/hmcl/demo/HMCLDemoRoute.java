// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies one destination in the HMCL Material 3 demo.
///
/// Top-level destinations participate in adaptive navigation. Detail destinations are pushed onto the application
/// back stack and retain only stable dummy-model identifiers.
@NotNullByDefault
sealed interface HMCLDemoRoute {
    /// Returns the localization key used for the route title.
    ///
    /// @return the title localization key
    String titleKey();

    /// Returns whether the route is a top-level navigation destination.
    ///
    /// @return `true` for a top-level destination
    default boolean topLevel() {
        return this instanceof Home
                || this instanceof Instances
                || this instanceof Discover
                || this instanceof Accounts
                || this instanceof Settings;
    }

    /// Shows the launch-focused home destination.
    record Home() implements HMCLDemoRoute {
        /// Creates the home destination.
        public Home {
        }

        /// {@inheritDoc}
        @Override
        public String titleKey() {
            return "nav.home";
        }
    }

    /// Shows the installed-instance collection.
    record Instances() implements HMCLDemoRoute {
        /// Creates the instances destination.
        public Instances {
        }

        /// {@inheritDoc}
        @Override
        public String titleKey() {
            return "nav.instances";
        }
    }

    /// Shows one installed instance.
    ///
    /// @param instanceId the stable dummy instance identifier
    record InstanceDetail(String instanceId) implements HMCLDemoRoute {
        /// Creates an instance-detail destination.
        ///
        /// @param instanceId the stable dummy instance identifier
        public InstanceDetail {
            if (instanceId.isBlank()) {
                throw new IllegalArgumentException("instanceId must not be blank");
            }
        }

        /// {@inheritDoc}
        @Override
        public String titleKey() {
            return "instance.detail.title";
        }
    }

    /// Shows downloadable games, modpacks, and mods.
    record Discover() implements HMCLDemoRoute {
        /// Creates the discover destination.
        public Discover {
        }

        /// {@inheritDoc}
        @Override
        public String titleKey() {
            return "nav.discover";
        }
    }

    /// Shows one downloadable content item.
    ///
    /// @param contentId the stable dummy content identifier
    record ContentDetail(String contentId) implements HMCLDemoRoute {
        /// Creates a content-detail destination.
        ///
        /// @param contentId the stable dummy content identifier
        public ContentDetail {
            if (contentId.isBlank()) {
                throw new IllegalArgumentException("contentId must not be blank");
            }
        }

        /// {@inheritDoc}
        @Override
        public String titleKey() {
            return "discover.detail.title";
        }
    }

    /// Shows launcher accounts.
    record Accounts() implements HMCLDemoRoute {
        /// Creates the accounts destination.
        public Accounts {
        }

        /// {@inheritDoc}
        @Override
        public String titleKey() {
            return "nav.accounts";
        }
    }

    /// Shows launcher and appearance settings.
    record Settings() implements HMCLDemoRoute {
        /// Creates the settings destination.
        public Settings {
        }

        /// {@inheritDoc}
        @Override
        public String titleKey() {
            return "nav.settings";
        }
    }
}
