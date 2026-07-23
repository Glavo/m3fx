// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.hmcl.demo;

import org.jetbrains.annotations.NotNullByDefault;

import java.util.Objects;

/// Receives navigation and dummy-operation requests emitted by the HMCL Material 3 demo pages.
///
/// Commands without a target are delivered as their action token. Commands with a target are encoded as
/// `action:target`. Route changes use the same convention with the `navigate` action.
@FunctionalInterface
@NotNullByDefault
public interface HMCLDemoActions {
    /// The route identifier for the home page.
    String ROUTE_HOME = "home";

    /// The route identifier for the instance list.
    String ROUTE_INSTANCES = "instances";

    /// The route identifier for the selected instance detail page.
    String ROUTE_INSTANCE_DETAIL = "instance-detail";

    /// The route identifier for the discover page.
    String ROUTE_DISCOVER = "discover";

    /// The route identifier for the account page.
    String ROUTE_ACCOUNTS = "accounts";

    /// The route identifier for the settings page.
    String ROUTE_SETTINGS = "settings";

    /// The action token for launching the selected instance.
    String ACTION_PLAY = "play";

    /// The action token for creating or importing an instance.
    String ACTION_ADD_INSTANCE = "add-instance";

    /// The action token for selecting an instance.
    String ACTION_SELECT_INSTANCE = "select-instance";

    /// The action token for installing discovered content.
    String ACTION_INSTALL = "install";

    /// The action token for adding an account.
    String ACTION_ADD_ACCOUNT = "add-account";

    /// The action token for selecting an account.
    String ACTION_SELECT_ACCOUNT = "select-account";

    /// The action token for refreshing dummy content.
    String ACTION_REFRESH = "refresh";

    /// Handles an encoded page command.
    ///
    /// @param command the non-empty command token
    void dispatch(String command);

    /// Requests navigation to a demo route.
    ///
    /// @param route the route identifier
    default void navigate(String route) {
        dispatch(command("navigate", route));
    }

    /// Dispatches an action associated with one target identifier.
    ///
    /// @param action the action token
    /// @param target the target identifier
    default void dispatch(String action, String target) {
        dispatch(command(action, target));
    }

    /// Encodes an action and target as one command token.
    ///
    /// @param action the action token
    /// @param target the target identifier
    /// @return the encoded command
    /// @throws NullPointerException     if an argument is `null`
    /// @throws IllegalArgumentException if an argument is empty or contains `:`
    static String command(String action, String target) {
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(target, "target");
        if (action.isEmpty() || target.isEmpty()) {
            throw new IllegalArgumentException("Action and target must not be empty");
        }
        if (action.indexOf(':') >= 0 || target.indexOf(':') >= 0) {
            throw new IllegalArgumentException("Action and target must not contain ':'");
        }
        return action + ':' + target;
    }
}
