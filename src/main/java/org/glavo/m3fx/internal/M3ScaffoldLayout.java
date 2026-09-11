// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.scene.Node;
import javafx.scene.layout.Region;
import org.glavo.m3fx.layout.M3AdaptiveScaffold;
import org.glavo.m3fx.layout.M3Breakpoint;
import org.glavo.m3fx.layout.M3NavigationLayout;
import org.glavo.m3fx.layout.M3PaneLayout;
import org.glavo.m3fx.layout.M3PaneRole;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Resolves scaffold participation independently of published properties and rendered slot state.
///
/// @param breakpoint the breakpoint used to resolve adaptive policies and automatic metrics
/// @param paneLayout the concrete pane arrangement
/// @param navigationLayout the concrete navigation presentation
/// @param leadingPaneVisible whether the leading pane participates in this arrangement
/// @param mainPaneVisible whether the main pane participates in this arrangement
/// @param trailingPaneVisible whether the trailing pane participates in this arrangement
@NotNullByDefault
public record M3ScaffoldLayout(M3Breakpoint breakpoint, M3PaneLayout paneLayout,
                               M3NavigationLayout navigationLayout, boolean leadingPaneVisible,
                               boolean mainPaneVisible, boolean trailingPaneVisible) {
    /// Resolves current requests at a proposed non-negative width without publishing or laying out anything.
    /// The explicit breakpoint override, when present, takes precedence over the supplied width.
    ///
    /// @throws IllegalArgumentException if no override is set and the width is negative or not finite
    public static M3ScaffoldLayout resolve(M3AdaptiveScaffold control, double width) {
        @Nullable M3Breakpoint breakpoint = control.getBreakpointOverride();
        if (breakpoint == null) {
            breakpoint = M3Breakpoint.forWidth(width);
        }
        M3PaneLayout panes = resolvePaneLayout(control, breakpoint);
        boolean leading = isPaneVisible(control, panes, M3PaneRole.LEADING);
        boolean main = isPaneVisible(control, panes, M3PaneRole.MAIN);
        boolean trailing = isPaneVisible(control, panes, M3PaneRole.TRAILING);
        int count = (leading ? 1 : 0) + (main ? 1 : 0) + (trailing ? 1 : 0);
        return new M3ScaffoldLayout(breakpoint, panes, resolveNavigationLayout(control, breakpoint, count),
                leading, main, trailing);
    }

    /// Returns the number of pane slots participating in this resolved arrangement.
    public int visiblePaneCount() {
        return (leadingPaneVisible ? 1 : 0) + (mainPaneVisible ? 1 : 0) + (trailingPaneVisible ? 1 : 0);
    }

    /// Tests current slot membership against a concrete pane policy without changing effective state.
    public static boolean isPaneVisible(M3AdaptiveScaffold control, M3PaneLayout layout, M3PaneRole role) {
        if (paneForRole(control, role) == null) {
            return false;
        }
        return switch (layout) {
            case ADAPTIVE -> false;
            case SINGLE -> role == singlePaneRole(control);
            case SPLIT_LEADING, FIXED_LEADING -> role == M3PaneRole.LEADING || role == M3PaneRole.MAIN;
            case SPLIT_TRAILING, FIXED_TRAILING -> role == M3PaneRole.MAIN || role == M3PaneRole.TRAILING;
            case THREE_PANE -> true;
        };
    }

    /// Resolves active-pane fallback, returning null when no pane is installed.
    public static @Nullable M3PaneRole singlePaneRole(M3AdaptiveScaffold control) {
        M3PaneRole preferred = control.getActivePane();
        if (paneForRole(control, preferred) != null) {
            return preferred;
        }
        if (control.getMainPane() != null) {
            return M3PaneRole.MAIN;
        }
        if (control.getLeadingPane() != null) {
            return M3PaneRole.LEADING;
        }
        return control.getTrailingPane() == null ? null : M3PaneRole.TRAILING;
    }

    /// Resolves the configured content margin or the supplied breakpoint's default.
    ///
    /// @throws IllegalArgumentException if the configured value fails [#validateMetric(double, String)]
    public static double contentMargin(double configured, M3Breakpoint breakpoint) {
        return resolveMetric(configured, breakpoint.getContentMargin(), "contentMargin");
    }

    /// Resolves the configured pane spacing or the supplied breakpoint's default.
    ///
    /// @throws IllegalArgumentException if the configured value fails [#validateMetric(double, String)]
    public static double paneSpacing(double configured, M3Breakpoint breakpoint) {
        return resolveMetric(configured, breakpoint.getPaneSpacing(), "paneSpacing");
    }

    /// Resolves the requested leading-pane width before layout constrains it to available space.
    ///
    /// @throws IllegalArgumentException if the configured value fails [#validateMetric(double, String)]
    public static double fixedLeadingPaneWidth(double configured, M3Breakpoint breakpoint) {
        return resolveMetric(configured, defaultFixedPaneWidth(breakpoint), "fixedLeadingPaneWidth");
    }

    /// Resolves the requested trailing-pane width for the supplied breakpoint and concrete pane arrangement.
    ///
    /// @throws IllegalArgumentException if the configured value fails [#validateMetric(double, String)]
    public static double fixedTrailingPaneWidth(double configured, M3Breakpoint breakpoint, M3PaneLayout layout) {
        return resolveMetric(configured, layout == M3PaneLayout.THREE_PANE ? 400.0 : defaultFixedPaneWidth(breakpoint),
                "fixedTrailingPaneWidth");
    }

    /// Validates an automatic or finite non-negative configured metric, returning the original value.
    ///
    /// @throws IllegalArgumentException if the value is neither USE_COMPUTED_SIZE nor finite and non-negative
    public static double validateMetric(double value, String name) {
        if (Double.compare(value, Region.USE_COMPUTED_SIZE) != 0 && (!Double.isFinite(value) || value < 0.0)) {
            throw new IllegalArgumentException(name + " must be USE_COMPUTED_SIZE or a finite non-negative value");
        }
        return value;
    }

    /// Resolves an explicit or automatic metric.
    private static double resolveMetric(double configured, double automatic, String name) {
        return Double.compare(configured, Region.USE_COMPUTED_SIZE) == 0
                ? automatic : validateMetric(configured, name);
    }

    /// Returns the standard fixed-pane width for a breakpoint.
    private static double defaultFixedPaneWidth(M3Breakpoint breakpoint) {
        return switch (breakpoint) {
            case COMPACT, MEDIUM, EXPANDED -> 360.0;
            case LARGE, EXTRA_LARGE -> 412.0;
        };
    }

    /// Resolves adaptive and explicit pane policies around missing slots.
    private static M3PaneLayout resolvePaneLayout(M3AdaptiveScaffold control, M3Breakpoint breakpoint) {
        boolean leading = control.getLeadingPane() != null;
        boolean main = control.getMainPane() != null;
        boolean trailing = control.getTrailingPane() != null;
        M3PaneLayout requested = control.getPaneLayout();
        return switch (requested) {
            case ADAPTIVE -> {
                if (breakpoint.getRecommendedPaneCount() <= 1 || !main) {
                    yield M3PaneLayout.SINGLE;
                }
                if (control.getActivePane() == M3PaneRole.TRAILING && trailing) {
                    yield M3PaneLayout.SPLIT_TRAILING;
                }
                yield leading ? M3PaneLayout.SPLIT_LEADING
                        : trailing ? M3PaneLayout.SPLIT_TRAILING : M3PaneLayout.SINGLE;
            }
            case SINGLE -> M3PaneLayout.SINGLE;
            case SPLIT_LEADING, FIXED_LEADING -> leading && main ? requested : M3PaneLayout.SINGLE;
            case SPLIT_TRAILING, FIXED_TRAILING -> main && trailing ? requested : M3PaneLayout.SINGLE;
            case THREE_PANE -> {
                if (leading && main && trailing) {
                    yield M3PaneLayout.THREE_PANE;
                }
                if (leading && main) {
                    yield M3PaneLayout.FIXED_LEADING;
                }
                yield main && trailing ? M3PaneLayout.FIXED_TRAILING : M3PaneLayout.SINGLE;
            }
        };
    }

    /// Resolves requested navigation or the available adaptive alternative.
    private static M3NavigationLayout resolveNavigationLayout(M3AdaptiveScaffold control,
                                                              M3Breakpoint breakpoint, int paneCount) {
        M3NavigationLayout requested = control.getNavigationLayout();
        if (requested != M3NavigationLayout.ADAPTIVE) {
            return availableNavigationLayout(control, requested);
        }
        M3NavigationLayout preferred = switch (breakpoint) {
            case COMPACT -> M3NavigationLayout.BAR;
            case MEDIUM -> paneCount > 1 ? M3NavigationLayout.BAR : M3NavigationLayout.RAIL;
            case EXPANDED, LARGE, EXTRA_LARGE -> M3NavigationLayout.RAIL;
        };
        M3NavigationLayout effective = availableNavigationLayout(control, preferred);
        return effective != M3NavigationLayout.NONE ? effective
                : availableNavigationLayout(control, preferred == M3NavigationLayout.BAR
                        ? M3NavigationLayout.RAIL : M3NavigationLayout.BAR);
    }

    /// Returns a concrete navigation policy only when its slot is populated.
    private static M3NavigationLayout availableNavigationLayout(M3AdaptiveScaffold control, M3NavigationLayout layout) {
        return switch (layout) {
            case BAR -> control.getNavigationBar() == null ? M3NavigationLayout.NONE : M3NavigationLayout.BAR;
            case RAIL -> control.getNavigationRail() == null ? M3NavigationLayout.NONE : M3NavigationLayout.RAIL;
            case ADAPTIVE, NONE -> M3NavigationLayout.NONE;
        };
    }

    /// Returns the installed node for a logical pane role.
    private static @Nullable Node paneForRole(M3AdaptiveScaffold control, M3PaneRole role) {
        return switch (role) {
            case LEADING -> control.getLeadingPane();
            case MAIN -> control.getMainPane();
            case TRAILING -> control.getTrailingPane();
        };
    }
}
