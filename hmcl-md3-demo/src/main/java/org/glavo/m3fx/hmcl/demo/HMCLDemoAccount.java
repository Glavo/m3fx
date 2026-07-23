// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import org.jetbrains.annotations.NotNullByDefault;

/// Describes one deterministic account shown by the HMCL-inspired demo.
///
/// @param id the stable account identifier
/// @param displayName the displayed profile name
/// @param type the account provider category
/// @param skinPath the generated skin asset path used for the face avatar
@NotNullByDefault
public record HMCLDemoAccount(
        String id,
        String displayName,
        AccountType type,
        String skinPath
) {
    /// Identifies the authentication presentation used by a dummy account.
    @NotNullByDefault
    public enum AccountType {
        /// Represents a Microsoft-backed account.
        MICROSOFT,

        /// Represents an offline local profile.
        OFFLINE,

        /// Represents a third-party authentication server.
        EXTERNAL
    }
}
