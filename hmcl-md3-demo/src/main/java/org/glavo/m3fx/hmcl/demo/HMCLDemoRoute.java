// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import org.jetbrains.annotations.NotNullByDefault;

/// Identifies one retained page hosted by the HMCL Material 3 demo shell.
@NotNullByDefault
sealed interface HMCLDemoRoute {
    /// The wallpaper home page with the primary launcher navigation.
    record Home() implements HMCLDemoRoute {
    }

    /// The account-list page.
    record Accounts() implements HMCLDemoRoute {
    }

    /// The game-instance list page.
    record Instances() implements HMCLDemoRoute {
    }

    /// The management page for one selected instance.
    ///
    /// @param instanceId the selected instance identifier
    record Instance(String instanceId) implements HMCLDemoRoute {
    }

    /// The download center page.
    record Download() implements HMCLDemoRoute {
    }

    /// The launcher settings page.
    record Settings() implements HMCLDemoRoute {
    }

    /// The multiplayer page.
    record Multiplayer() implements HMCLDemoRoute {
    }
}
