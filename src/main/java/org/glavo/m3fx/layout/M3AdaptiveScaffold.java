// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.layout;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3AdaptiveScaffoldSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/// Arranges Material bars, navigation regions, rails, and adaptive content panes.
///
/// The scaffold owns stable slots for a top bar, a contextual bottom bar, a bottom navigation bar, a logical
/// leading navigation rail, a logical trailing rail, and up to three content panes. The current [breakpoint]
/// [#breakpointProperty()] is derived from the scaffold's assigned width unless [#breakpointOverrideProperty()] is
/// set. Changing the window width therefore updates pane count, margins, spacing, and adaptive navigation without
/// requiring an application listener.
///
/// Pane and navigation nodes retain their control state while their region is not effective. Changes in pane
/// topology or navigation presentation animate the affected region bounds and opacity using
/// [#getLayoutMotionSpec()] or the resolved default spatial motion role. A region that is leaving remains rendered
/// until its transition settles but stops receiving input as soon as it ceases to be effective. Continuous resizing
/// that does not change the resolved topology updates geometry directly rather than lagging behind the window.
/// Hidden regions are unmanaged and do not receive input; their controls, selection, and scrolling state remain
/// available across breakpoint changes. A non-null node may occupy at most one scaffold slot, must not already have
/// a parent, and must not be this scaffold or one of its ancestors. Assigning an ineligible node directly fails
/// without changing the property. An observable bound to a slot property must supply values that satisfy the same
/// constraints.
///
/// The [#getPaneLayout()] value controls pane adaptation. Its default, [M3PaneLayout#ADAPTIVE], shows one pane at
/// compact and medium widths and two panes at wider breakpoints. [#getActivePane()] selects the content shown by a
/// single-pane layout. Explicit fixed, split, and three-pane layouts remain in force across breakpoints while their
/// required slots are populated; empty slots cause the effective layout to collapse as described by [M3PaneLayout].
///
/// ```java
/// M3AdaptiveScaffold scaffold = new M3AdaptiveScaffold();
/// scaffold.setTopBar(new M3TopAppBar("Inbox"));
/// scaffold.setNavigationBar(compactNavigation);
/// scaffold.setNavigationRail(expandedNavigation);
/// scaffold.setLeadingPane(messageList);
/// scaffold.setMainPane(messageDetail);
/// ```
///
/// See [Material Design scaffold](https://m3.material.io/foundations/layout/scaffold/overview).
@NotNullByDefault
public final class M3AdaptiveScaffold extends Control {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-adaptive-scaffold";

    /// The compact breakpoint pseudo-class.
    private static final PseudoClass COMPACT_PSEUDO_CLASS = PseudoClass.getPseudoClass("compact");

    /// The medium breakpoint pseudo-class.
    private static final PseudoClass MEDIUM_PSEUDO_CLASS = PseudoClass.getPseudoClass("medium");

    /// The expanded breakpoint pseudo-class.
    private static final PseudoClass EXPANDED_PSEUDO_CLASS = PseudoClass.getPseudoClass("expanded");

    /// The large breakpoint pseudo-class.
    private static final PseudoClass LARGE_PSEUDO_CLASS = PseudoClass.getPseudoClass("large");

    /// The extra-large breakpoint pseudo-class.
    private static final PseudoClass EXTRA_LARGE_PSEUDO_CLASS = PseudoClass.getPseudoClass("extra-large");

    /// The single-pane layout pseudo-class.
    private static final PseudoClass SINGLE_PANE_PSEUDO_CLASS = PseudoClass.getPseudoClass("single-pane");

    /// The split-leading layout pseudo-class.
    private static final PseudoClass SPLIT_LEADING_PSEUDO_CLASS = PseudoClass.getPseudoClass("split-leading");

    /// The split-trailing layout pseudo-class.
    private static final PseudoClass SPLIT_TRAILING_PSEUDO_CLASS = PseudoClass.getPseudoClass("split-trailing");

    /// The fixed-leading layout pseudo-class.
    private static final PseudoClass FIXED_LEADING_PSEUDO_CLASS = PseudoClass.getPseudoClass("fixed-leading");

    /// The fixed-trailing layout pseudo-class.
    private static final PseudoClass FIXED_TRAILING_PSEUDO_CLASS = PseudoClass.getPseudoClass("fixed-trailing");

    /// The three-pane layout pseudo-class.
    private static final PseudoClass THREE_PANE_PSEUDO_CLASS = PseudoClass.getPseudoClass("three-pane");

    /// The navigation-bar presentation pseudo-class.
    private static final PseudoClass NAVIGATION_BAR_PSEUDO_CLASS = PseudoClass.getPseudoClass("navigation-bar");

    /// The navigation-rail presentation pseudo-class.
    private static final PseudoClass NAVIGATION_RAIL_PSEUDO_CLASS = PseudoClass.getPseudoClass("navigation-rail");

    /// The top bar spanning the scaffold width.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Node> topBar = createSlotProperty("topBar");

    /// Returns the top bar.
    ///
    /// @return the top bar, or `null` when the slot is empty
    public @Nullable Node getTopBar() {
        return topBar.get();
    }

    /// Sets the top bar.
    ///
    /// @param topBar the top bar, or `null` to clear the slot
    /// @throws IllegalArgumentException if `topBar` already has a parent or occupies another slot
    public void setTopBar(@Nullable Node topBar) {
        this.topBar.set(topBar);
    }

    /// Returns the observable, bindable top-bar property.
    ///
    /// A non-null value must not have a parent or occupy another scaffold slot.
    ///
    /// @return the top-bar property
    public ObjectProperty<@Nullable Node> topBarProperty() {
        return topBar;
    }

    /// The contextual bottom bar shown above bottom navigation.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Node> bottomBar = createSlotProperty("bottomBar");

    /// Returns the contextual bottom bar.
    ///
    /// @return the bottom bar, or `null` when the slot is empty
    public @Nullable Node getBottomBar() {
        return bottomBar.get();
    }

    /// Sets the contextual bottom bar.
    ///
    /// @param bottomBar the bottom bar, or `null` to clear the slot
    /// @throws IllegalArgumentException if `bottomBar` already has a parent or occupies another slot
    public void setBottomBar(@Nullable Node bottomBar) {
        this.bottomBar.set(bottomBar);
    }

    /// Returns the observable, bindable bottom-bar property.
    ///
    /// A non-null value must not have a parent or occupy another scaffold slot.
    ///
    /// @return the bottom-bar property
    public ObjectProperty<@Nullable Node> bottomBarProperty() {
        return bottomBar;
    }

    /// The navigation control placed in the bottom bar region.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Node> navigationBar = createSlotProperty("navigationBar");

    /// Returns the bottom navigation control.
    ///
    /// @return the navigation bar, or `null` when the slot is empty
    public @Nullable Node getNavigationBar() {
        return navigationBar.get();
    }

    /// Sets the bottom navigation control.
    ///
    /// @param navigationBar the navigation bar, or `null` to clear the slot
    /// @throws IllegalArgumentException if `navigationBar` already has a parent or occupies another slot
    public void setNavigationBar(@Nullable Node navigationBar) {
        this.navigationBar.set(navigationBar);
    }

    /// Returns the observable, bindable navigation-bar property.
    ///
    /// A non-null value must not have a parent or occupy another scaffold slot.
    ///
    /// @return the navigation-bar property
    public ObjectProperty<@Nullable Node> navigationBarProperty() {
        return navigationBar;
    }

    /// The navigation control placed in the logical leading rail region.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Node> navigationRail = createSlotProperty("navigationRail");

    /// Returns the logical leading navigation rail.
    ///
    /// @return the navigation rail, or `null` when the slot is empty
    public @Nullable Node getNavigationRail() {
        return navigationRail.get();
    }

    /// Sets the logical leading navigation rail.
    ///
    /// @param navigationRail the navigation rail, or `null` to clear the slot
    /// @throws IllegalArgumentException if `navigationRail` already has a parent or occupies another slot
    public void setNavigationRail(@Nullable Node navigationRail) {
        this.navigationRail.set(navigationRail);
    }

    /// Returns the observable, bindable navigation-rail property.
    ///
    /// A non-null value must not have a parent or occupy another scaffold slot.
    ///
    /// @return the navigation-rail property
    public ObjectProperty<@Nullable Node> navigationRailProperty() {
        return navigationRail;
    }

    /// The optional logical trailing rail containing supporting controls.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Node> trailingRail = createSlotProperty("trailingRail");

    /// Returns the logical trailing rail.
    ///
    /// @return the trailing rail, or `null` when the slot is empty
    public @Nullable Node getTrailingRail() {
        return trailingRail.get();
    }

    /// Sets the logical trailing rail.
    ///
    /// @param trailingRail the trailing rail, or `null` to clear the slot
    /// @throws IllegalArgumentException if `trailingRail` already has a parent or occupies another slot
    public void setTrailingRail(@Nullable Node trailingRail) {
        this.trailingRail.set(trailingRail);
    }

    /// Returns the observable, bindable trailing-rail property.
    ///
    /// A non-null value must not have a parent or occupy another scaffold slot.
    ///
    /// @return the trailing-rail property
    public ObjectProperty<@Nullable Node> trailingRailProperty() {
        return trailingRail;
    }

    /// The logical leading content pane.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Node> leadingPane = createSlotProperty("leadingPane");

    /// Returns the logical leading content pane.
    ///
    /// @return the leading pane, or `null` when the slot is empty
    public @Nullable Node getLeadingPane() {
        return leadingPane.get();
    }

    /// Sets the logical leading content pane.
    ///
    /// @param leadingPane the leading pane, or `null` to clear the slot
    /// @throws IllegalArgumentException if `leadingPane` already has a parent or occupies another slot
    public void setLeadingPane(@Nullable Node leadingPane) {
        this.leadingPane.set(leadingPane);
    }

    /// Returns the observable, bindable leading-pane property.
    ///
    /// A non-null value must not have a parent or occupy another scaffold slot.
    ///
    /// @return the leading-pane property
    public ObjectProperty<@Nullable Node> leadingPaneProperty() {
        return leadingPane;
    }

    /// The principal flexible content pane.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Node> mainPane = createSlotProperty("mainPane");

    /// Returns the principal content pane.
    ///
    /// @return the main pane, or `null` when the slot is empty
    public @Nullable Node getMainPane() {
        return mainPane.get();
    }

    /// Sets the principal content pane.
    ///
    /// @param mainPane the main pane, or `null` to clear the slot
    /// @throws IllegalArgumentException if `mainPane` already has a parent or occupies another slot
    public void setMainPane(@Nullable Node mainPane) {
        this.mainPane.set(mainPane);
    }

    /// Returns the observable, bindable main-pane property.
    ///
    /// A non-null value must not have a parent or occupy another scaffold slot.
    ///
    /// @return the main-pane property
    public ObjectProperty<@Nullable Node> mainPaneProperty() {
        return mainPane;
    }

    /// The logical trailing content pane.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Node> trailingPane = createSlotProperty("trailingPane");

    /// Returns the logical trailing content pane.
    ///
    /// @return the trailing pane, or `null` when the slot is empty
    public @Nullable Node getTrailingPane() {
        return trailingPane.get();
    }

    /// Sets the logical trailing content pane.
    ///
    /// @param trailingPane the trailing pane, or `null` to clear the slot
    /// @throws IllegalArgumentException if `trailingPane` already has a parent or occupies another slot
    public void setTrailingPane(@Nullable Node trailingPane) {
        this.trailingPane.set(trailingPane);
    }

    /// Returns the observable, bindable trailing-pane property.
    ///
    /// A non-null value must not have a parent or occupy another scaffold slot.
    ///
    /// @return the trailing-pane property
    public ObjectProperty<@Nullable Node> trailingPaneProperty() {
        return trailingPane;
    }

    /// The requested content-pane layout.
    ///
    /// @defaultValue [M3PaneLayout#ADAPTIVE]
    private final ObjectProperty<@Nullable M3PaneLayout> paneLayout =
            new SimpleObjectProperty<>(this, "paneLayout", M3PaneLayout.ADAPTIVE) {
                /// Recomputes effective layout when the request changes.
                @Override
                protected void invalidated() {
                    if (get() == null && !isBound()) {
                        set(M3PaneLayout.ADAPTIVE);
                        return;
                    }
                    updateAdaptiveState();
                }
            };

    /// Returns the requested pane layout.
    ///
    /// @return the requested pane layout
    public M3PaneLayout getPaneLayout() {
        @Nullable M3PaneLayout value = paneLayout.get();
        return value == null ? M3PaneLayout.ADAPTIVE : value;
    }

    /// Sets the requested pane layout.
    ///
    /// @param paneLayout the requested pane layout
    /// @throws NullPointerException if `paneLayout` is `null`
    public void setPaneLayout(M3PaneLayout paneLayout) {
        this.paneLayout.set(Objects.requireNonNull(paneLayout, "paneLayout"));
    }

    /// Returns the observable, bindable requested-pane-layout property.
    ///
    /// An unbound direct `null` assignment restores [M3PaneLayout#ADAPTIVE]. While the property is bound, a `null`
    /// value is interpreted as [M3PaneLayout#ADAPTIVE] without modifying the binding.
    ///
    /// @return the requested-pane-layout property
    public ObjectProperty<@Nullable M3PaneLayout> paneLayoutProperty() {
        return paneLayout;
    }

    /// The pane preferred by a single-pane arrangement.
    ///
    /// @defaultValue [M3PaneRole#MAIN]
    private final ObjectProperty<@Nullable M3PaneRole> activePane =
            new SimpleObjectProperty<>(this, "activePane", M3PaneRole.MAIN) {
                /// Recomputes pane visibility when the active role changes.
                @Override
                protected void invalidated() {
                    if (get() == null && !isBound()) {
                        set(M3PaneRole.MAIN);
                        return;
                    }
                    updateAdaptiveState();
                    notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
                }
            };

    /// Returns the pane preferred by a single-pane arrangement.
    ///
    /// @return the preferred active pane role
    public M3PaneRole getActivePane() {
        @Nullable M3PaneRole value = activePane.get();
        return value == null ? M3PaneRole.MAIN : value;
    }

    /// Sets the pane preferred by a single-pane arrangement.
    ///
    /// If the selected slot is empty, the scaffold falls back to the main, leading, and trailing slots in that
    /// order.
    ///
    /// @param activePane the preferred active pane role
    /// @throws NullPointerException if `activePane` is `null`
    public void setActivePane(M3PaneRole activePane) {
        this.activePane.set(Objects.requireNonNull(activePane, "activePane"));
    }

    /// Returns the observable, bindable active-pane property.
    ///
    /// An unbound direct `null` assignment restores [M3PaneRole#MAIN]. While the property is bound, a `null` value is
    /// interpreted as [M3PaneRole#MAIN] without modifying the binding.
    ///
    /// @return the active-pane property
    public ObjectProperty<@Nullable M3PaneRole> activePaneProperty() {
        return activePane;
    }

    /// The requested navigation presentation.
    ///
    /// @defaultValue [M3NavigationLayout#ADAPTIVE]
    private final ObjectProperty<@Nullable M3NavigationLayout> navigationLayout =
            new SimpleObjectProperty<>(this, "navigationLayout", M3NavigationLayout.ADAPTIVE) {
                /// Recomputes effective navigation when the request changes.
                @Override
                protected void invalidated() {
                    if (get() == null && !isBound()) {
                        set(M3NavigationLayout.ADAPTIVE);
                        return;
                    }
                    updateAdaptiveState();
                }
            };

    /// Returns the requested navigation presentation.
    ///
    /// @return the requested navigation presentation
    public M3NavigationLayout getNavigationLayout() {
        @Nullable M3NavigationLayout value = navigationLayout.get();
        return value == null ? M3NavigationLayout.ADAPTIVE : value;
    }

    /// Sets the requested navigation presentation.
    ///
    /// @param navigationLayout the requested navigation presentation
    /// @throws NullPointerException if `navigationLayout` is `null`
    public void setNavigationLayout(M3NavigationLayout navigationLayout) {
        this.navigationLayout.set(Objects.requireNonNull(navigationLayout, "navigationLayout"));
    }

    /// Returns the observable, bindable requested-navigation property.
    ///
    /// An unbound direct `null` assignment restores [M3NavigationLayout#ADAPTIVE]. While the property is bound, a
    /// `null` value is interpreted as [M3NavigationLayout#ADAPTIVE] without modifying the binding.
    ///
    /// @return the requested-navigation property
    public ObjectProperty<@Nullable M3NavigationLayout> navigationLayoutProperty() {
        return navigationLayout;
    }

    /// The motion specification used for adaptive pane and navigation geometry changes.
    ///
    /// A `null` value selects the default spatial role from the motion scheme resolved for this scaffold. The
    /// specification affects subsequent transitions and a transition that is retargeted while running. It does not
    /// override reduced-motion settings inherited through [org.glavo.m3fx.animation.M3MotionSettings].
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable M3MotionSpec> layoutMotionSpec =
            new SimpleObjectProperty<>(this, "layoutMotionSpec");

    /// Returns the explicit adaptive-layout motion specification.
    ///
    /// @return the motion specification, or `null` to use the resolved default spatial role
    public @Nullable M3MotionSpec getLayoutMotionSpec() {
        return layoutMotionSpec.get();
    }

    /// Sets the adaptive-layout motion specification.
    ///
    /// @param motionSpec the motion specification, or `null` to use the resolved default spatial role
    public void setLayoutMotionSpec(@Nullable M3MotionSpec motionSpec) {
        layoutMotionSpec.set(motionSpec);
    }

    /// Returns the observable, bindable adaptive-layout motion-specification property.
    ///
    /// @return the adaptive-layout motion-specification property
    public ObjectProperty<@Nullable M3MotionSpec> layoutMotionSpecProperty() {
        return layoutMotionSpec;
    }

    /// The optional breakpoint used instead of the scaffold width.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable M3Breakpoint> breakpointOverride =
            new SimpleObjectProperty<>(this, "breakpointOverride") {
                /// Recomputes layout when the override changes.
                @Override
                protected void invalidated() {
                    updateAdaptiveState();
                }
            };

    /// Returns the explicit breakpoint override.
    ///
    /// @return the override, or `null` when the assigned width determines the breakpoint
    public @Nullable M3Breakpoint getBreakpointOverride() {
        return breakpointOverride.get();
    }

    /// Sets the explicit breakpoint override.
    ///
    /// This property is intended for previews, tests, and application policies that intentionally differ from the
    /// assigned width. Set it to `null` to restore automatic breakpoint calculation.
    ///
    /// @param breakpointOverride the override, or `null` for automatic calculation
    public void setBreakpointOverride(@Nullable M3Breakpoint breakpointOverride) {
        this.breakpointOverride.set(breakpointOverride);
    }

    /// Returns the observable, bindable breakpoint-override property.
    ///
    /// @return the breakpoint-override property
    public ObjectProperty<@Nullable M3Breakpoint> breakpointOverrideProperty() {
        return breakpointOverride;
    }

    /// Safety insets reserved inside the scaffold bounds.
    ///
    /// @defaultValue [Insets#EMPTY]
    private final ObjectProperty<@Nullable Insets> safetyInsets =
            new SimpleObjectProperty<>(this, "safetyInsets", Insets.EMPTY) {
                /// Requests layout when the reserved edges change.
                @Override
                protected void invalidated() {
                    if (get() == null && !isBound()) {
                        set(Insets.EMPTY);
                        return;
                    }
                    requestLayout();
                }
            };

    /// Returns the safety insets reserved inside the scaffold bounds.
    ///
    /// @return the physical top, right, bottom, and left safety insets
    public Insets getSafetyInsets() {
        @Nullable Insets value = safetyInsets.get();
        return value == null ? Insets.EMPTY : value;
    }

    /// Sets the safety insets reserved inside the scaffold bounds.
    ///
    /// Insets are physical because they describe platform-reserved screen edges rather than logical content order.
    ///
    /// @param safetyInsets the physical safety insets
    /// @throws NullPointerException if `safetyInsets` is `null`
    public void setSafetyInsets(Insets safetyInsets) {
        this.safetyInsets.set(Objects.requireNonNull(safetyInsets, "safetyInsets"));
    }

    /// Returns the observable, bindable safety-insets property.
    ///
    /// An unbound direct `null` assignment restores [Insets#EMPTY]. While the property is bound, a `null` value is
    /// interpreted as [Insets#EMPTY] without modifying the binding.
    ///
    /// @return the safety-insets property
    public ObjectProperty<@Nullable Insets> safetyInsetsProperty() {
        return safetyInsets;
    }

    /// The explicit leading and trailing content margin, or [Region#USE_COMPUTED_SIZE] for the breakpoint value.
    ///
    /// @defaultValue [Region#USE_COMPUTED_SIZE]
    private final DoubleProperty contentMargin = createMetricProperty("contentMargin");

    /// Returns the configured content margin.
    ///
    /// @return the explicit margin in logical pixels, or [Region#USE_COMPUTED_SIZE]
    public double getContentMargin() {
        return contentMargin.get();
    }

    /// Sets the leading and trailing content margin.
    ///
    /// @param contentMargin a finite non-negative margin, or [Region#USE_COMPUTED_SIZE] to use the breakpoint value
    /// @throws IllegalArgumentException if the value is invalid
    public void setContentMargin(double contentMargin) {
        this.contentMargin.set(validateMetric(contentMargin, "contentMargin"));
    }

    /// Returns the observable, bindable content-margin property.
    ///
    /// A binding source must supply [Region#USE_COMPUTED_SIZE] or a finite, non-negative value.
    ///
    /// @return the content-margin property
    public DoubleProperty contentMarginProperty() {
        return contentMargin;
    }

    /// The explicit pane spacer, or [Region#USE_COMPUTED_SIZE] for the breakpoint value.
    ///
    /// @defaultValue [Region#USE_COMPUTED_SIZE]
    private final DoubleProperty paneSpacing = createMetricProperty("paneSpacing");

    /// Returns the configured pane spacer.
    ///
    /// @return the explicit spacer in logical pixels, or [Region#USE_COMPUTED_SIZE]
    public double getPaneSpacing() {
        return paneSpacing.get();
    }

    /// Sets the spacer between adjacent visible panes.
    ///
    /// @param paneSpacing a finite non-negative spacer, or [Region#USE_COMPUTED_SIZE] for the breakpoint value
    /// @throws IllegalArgumentException if the value is invalid
    public void setPaneSpacing(double paneSpacing) {
        this.paneSpacing.set(validateMetric(paneSpacing, "paneSpacing"));
    }

    /// Returns the observable, bindable pane-spacing property.
    ///
    /// A binding source must supply [Region#USE_COMPUTED_SIZE] or a finite, non-negative value.
    ///
    /// @return the pane-spacing property
    public DoubleProperty paneSpacingProperty() {
        return paneSpacing;
    }

    /// The explicit fixed leading-pane width, or [Region#USE_COMPUTED_SIZE] for the breakpoint default.
    ///
    /// @defaultValue [Region#USE_COMPUTED_SIZE]
    private final DoubleProperty fixedLeadingPaneWidth = createMetricProperty("fixedLeadingPaneWidth");

    /// Returns the configured fixed leading-pane width.
    ///
    /// @return the explicit width in logical pixels, or [Region#USE_COMPUTED_SIZE]
    public double getFixedLeadingPaneWidth() {
        return fixedLeadingPaneWidth.get();
    }

    /// Sets the width used by fixed-leading and three-pane layouts.
    ///
    /// @param width a finite non-negative width, or [Region#USE_COMPUTED_SIZE] for the breakpoint default
    /// @throws IllegalArgumentException if the value is invalid
    public void setFixedLeadingPaneWidth(double width) {
        fixedLeadingPaneWidth.set(validateMetric(width, "fixedLeadingPaneWidth"));
    }

    /// Returns the observable, bindable fixed-leading-pane-width property.
    ///
    /// A binding source must supply [Region#USE_COMPUTED_SIZE] or a finite, non-negative value.
    ///
    /// @return the fixed-leading-pane-width property
    public DoubleProperty fixedLeadingPaneWidthProperty() {
        return fixedLeadingPaneWidth;
    }

    /// The explicit fixed trailing-pane width, or [Region#USE_COMPUTED_SIZE] for the breakpoint default.
    ///
    /// @defaultValue [Region#USE_COMPUTED_SIZE]
    private final DoubleProperty fixedTrailingPaneWidth = createMetricProperty("fixedTrailingPaneWidth");

    /// Returns the configured fixed trailing-pane width.
    ///
    /// @return the explicit width in logical pixels, or [Region#USE_COMPUTED_SIZE]
    public double getFixedTrailingPaneWidth() {
        return fixedTrailingPaneWidth.get();
    }

    /// Sets the width used by fixed-trailing and three-pane layouts.
    ///
    /// @param width a finite non-negative width, or [Region#USE_COMPUTED_SIZE] for the breakpoint default
    /// @throws IllegalArgumentException if the value is invalid
    public void setFixedTrailingPaneWidth(double width) {
        fixedTrailingPaneWidth.set(validateMetric(width, "fixedTrailingPaneWidth"));
    }

    /// Returns the observable, bindable fixed-trailing-pane-width property.
    ///
    /// A binding source must supply [Region#USE_COMPUTED_SIZE] or a finite, non-negative value.
    ///
    /// @return the fixed-trailing-pane-width property
    public DoubleProperty fixedTrailingPaneWidthProperty() {
        return fixedTrailingPaneWidth;
    }

    /// The effective width breakpoint.
    ///
    /// @defaultValue [M3Breakpoint#COMPACT]
    private final ReadOnlyObjectWrapper<M3Breakpoint> breakpoint =
            new ReadOnlyObjectWrapper<>(this, "breakpoint", M3Breakpoint.COMPACT);

    /// Returns the effective width breakpoint.
    ///
    /// @return the effective breakpoint
    public M3Breakpoint getBreakpoint() {
        return breakpoint.get();
    }

    /// Returns the observable, read-only effective-breakpoint property.
    ///
    /// @return the effective-breakpoint property
    public ReadOnlyObjectProperty<M3Breakpoint> breakpointProperty() {
        return breakpoint.getReadOnlyProperty();
    }

    /// The concrete pane layout after resolving the requested layout, current breakpoint, and available pane slots.
    ///
    /// @defaultValue [M3PaneLayout#SINGLE]
    private final ReadOnlyObjectWrapper<M3PaneLayout> effectivePaneLayout =
            new ReadOnlyObjectWrapper<>(this, "effectivePaneLayout", M3PaneLayout.SINGLE);

    /// Returns the concrete pane layout after resolving the requested layout, current breakpoint, and available pane
    /// slots.
    ///
    /// @return the effective pane layout
    public M3PaneLayout getEffectivePaneLayout() {
        return effectivePaneLayout.get();
    }

    /// Returns the observable, read-only effective-pane-layout property.
    ///
    /// @return the effective-pane-layout property
    public ReadOnlyObjectProperty<M3PaneLayout> effectivePaneLayoutProperty() {
        return effectivePaneLayout.getReadOnlyProperty();
    }

    /// The concrete navigation presentation after adaptive resolution.
    ///
    /// @defaultValue [M3NavigationLayout#NONE]
    private final ReadOnlyObjectWrapper<M3NavigationLayout> effectiveNavigationLayout =
            new ReadOnlyObjectWrapper<>(this, "effectiveNavigationLayout", M3NavigationLayout.NONE);

    /// Returns the concrete navigation presentation after adaptive resolution.
    ///
    /// @return the effective navigation presentation
    public M3NavigationLayout getEffectiveNavigationLayout() {
        return effectiveNavigationLayout.get();
    }

    /// Returns the observable, read-only effective-navigation-layout property.
    ///
    /// @return the effective-navigation-layout property
    public ReadOnlyObjectProperty<M3NavigationLayout> effectiveNavigationLayoutProperty() {
        return effectiveNavigationLayout.getReadOnlyProperty();
    }

    /// The number of pane slots currently participating in layout.
    ///
    /// @defaultValue `0`
    private final ReadOnlyIntegerWrapper visiblePaneCount = new ReadOnlyIntegerWrapper(this, "visiblePaneCount");

    /// Returns the number of pane slots currently participating in layout.
    ///
    /// @return the visible pane count from zero through three
    public int getVisiblePaneCount() {
        return visiblePaneCount.get();
    }

    /// Returns the observable, read-only visible-pane-count property.
    ///
    /// @return the visible-pane-count property
    public ReadOnlyIntegerProperty visiblePaneCountProperty() {
        return visiblePaneCount.getReadOnlyProperty();
    }

    /// Creates an empty adaptive scaffold.
    public M3AdaptiveScaffold() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        setFocusTraversable(false);
        widthProperty().addListener(observable -> updateAdaptiveState());
        effectiveNodeOrientationProperty().addListener(observable -> requestLayout());
        updateAdaptiveState();
    }

    /// Returns whether the supplied logical pane role currently participates in layout.
    ///
    /// The result accounts for the effective pane layout, empty slots, and active-pane fallback. Observe
    /// [#effectivePaneLayoutProperty()], [#visiblePaneCountProperty()], and the relevant slot property to react to
    /// changes.
    ///
    /// @param role the logical pane role
    /// @return `true` when the role's non-null node participates in layout
    /// @throws NullPointerException if `role` is `null`
    public boolean isPaneVisible(M3PaneRole role) {
        Objects.requireNonNull(role, "role");
        if (paneForRole(role) == null) {
            return false;
        }
        return switch (getEffectivePaneLayout()) {
            case ADAPTIVE -> false;
            case SINGLE -> role == resolveSinglePaneRole();
            case SPLIT_LEADING, FIXED_LEADING -> role == M3PaneRole.LEADING || role == M3PaneRole.MAIN;
            case SPLIT_TRAILING, FIXED_TRAILING -> role == M3PaneRole.MAIN || role == M3PaneRole.TRAILING;
            case THREE_PANE -> true;
        };
    }

    /// Returns the effective leading and trailing content margin.
    ///
    /// @return the explicit margin or the current breakpoint's standard margin
    /// @throws IllegalArgumentException if a binding supplied an invalid configured value
    public double getEffectiveContentMargin() {
        return resolveMetric(getContentMargin(), getBreakpoint().getContentMargin(), "contentMargin");
    }

    /// Returns the effective spacer between adjacent visible panes.
    ///
    /// @return the explicit spacer or the current breakpoint's standard spacer
    /// @throws IllegalArgumentException if a binding supplied an invalid configured value
    public double getEffectivePaneSpacing() {
        return resolveMetric(getPaneSpacing(), getBreakpoint().getPaneSpacing(), "paneSpacing");
    }

    /// Returns the effective fixed leading-pane width.
    ///
    /// The Material default is 360 logical pixels through expanded widths and 412 logical pixels at large and
    /// extra-large widths. The effective width is reduced when the available pane region is smaller.
    ///
    /// @return the effective fixed leading-pane width
    /// @throws IllegalArgumentException if a binding supplied an invalid configured value
    public double getEffectiveFixedLeadingPaneWidth() {
        return resolveMetric(
                getFixedLeadingPaneWidth(),
                defaultFixedPaneWidth(getBreakpoint()),
                "fixedLeadingPaneWidth"
        );
    }

    /// Returns the effective fixed trailing-pane width.
    ///
    /// A three-pane layout uses the Material side-sheet maximum of 400 logical pixels by default. Other fixed
    /// trailing layouts use 360 logical pixels at expanded widths and 412 logical pixels at large and extra-large
    /// widths.
    ///
    /// @return the effective fixed trailing-pane width
    /// @throws IllegalArgumentException if a binding supplied an invalid configured value
    public double getEffectiveFixedTrailingPaneWidth() {
        double defaultWidth = getEffectivePaneLayout() == M3PaneLayout.THREE_PANE
                ? 400.0
                : defaultFixedPaneWidth(getBreakpoint());
        return resolveMetric(getFixedTrailingPaneWidth(), defaultWidth, "fixedTrailingPaneWidth");
    }

    /// Returns the user-agent stylesheet for adaptive scaffolds.
    ///
    /// @return the stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("adaptive-scaffold.css");
    }

    /// Creates the default scaffold skin.
    ///
    /// @return a new adaptive scaffold skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3AdaptiveScaffoldSkin(this);
    }

    /// Returns accessibility information for the effective scaffold regions.
    ///
    /// @param attribute  the requested attribute
    /// @param parameters optional attribute parameters
    /// @return the requested value, or the superclass result when this scaffold does not handle the attribute
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        List<Node> children = accessibleChildren();
        return switch (attribute) {
            case ITEM_COUNT -> children.size();
            case ITEM_AT_INDEX -> accessibleChildAt(children, parameters);
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Creates one slot property that invalidates adaptive and accessibility state.
    private ObjectProperty<@Nullable Node> createSlotProperty(String name) {
        return new SimpleObjectProperty<>(this, name) {
            /// Validates a direct slot assignment before changing the property.
            @Override
            public void set(@Nullable Node value) {
                if (get() != value) {
                    validateSlotAssignment(this, value);
                }
                super.set(value);
            }

            /// Validates binding updates before recomputing effective state.
            @Override
            protected void invalidated() {
                validateSlotAssignment(this, get());
                updateAdaptiveState();
                notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
                notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
            }
        };
    }

    /// Validates that a node may occupy the requested scaffold slot.
    private void validateSlotAssignment(
            ObjectProperty<@Nullable Node> slot,
            @Nullable Node node
    ) {
        if (node == null) {
            return;
        }
        if (node == this || isAncestorOfThisScaffold(node)) {
            throw new IllegalArgumentException("slot node would create a scene-graph cycle");
        }
        if (node.getParent() != null) {
            throw new IllegalArgumentException("slot node already has a parent");
        }
        if (isAssignedToAnotherSlot(slot, node)) {
            throw new IllegalArgumentException("slot node is already assigned to this scaffold");
        }
    }

    /// Returns whether the candidate is an ancestor of this scaffold.
    private boolean isAncestorOfThisScaffold(Node candidate) {
        Node ancestor = getParent();
        while (ancestor != null) {
            if (ancestor == candidate) {
                return true;
            }
            ancestor = ancestor.getParent();
        }
        return false;
    }

    /// Returns whether a node occupies a scaffold slot other than the requested property.
    private boolean isAssignedToAnotherSlot(
            ObjectProperty<@Nullable Node> slot,
            Node node
    ) {
        return topBar != slot && topBar.get() == node
                || bottomBar != slot && bottomBar.get() == node
                || navigationBar != slot && navigationBar.get() == node
                || navigationRail != slot && navigationRail.get() == node
                || trailingRail != slot && trailingRail.get() == node
                || leadingPane != slot && leadingPane.get() == node
                || mainPane != slot && mainPane.get() == node
                || trailingPane != slot && trailingPane.get() == node;
    }

    /// Creates a layout metric accepting automatic or finite non-negative values.
    private DoubleProperty createMetricProperty(String name) {
        return new SimpleDoubleProperty(this, name, Region.USE_COMPUTED_SIZE) {
            /// Requests layout when the metric changes.
            @Override
            protected void invalidated() {
                requestLayout();
            }
        };
    }

    /// Validates a configured metric.
    private static double validateMetric(double value, String name) {
        if (Double.compare(value, Region.USE_COMPUTED_SIZE) != 0
                && (!Double.isFinite(value) || value < 0.0)) {
            throw new IllegalArgumentException(
                    name + " must be USE_COMPUTED_SIZE or a finite non-negative value"
            );
        }
        return value;
    }

    /// Resolves an explicit or automatic metric.
    private static double resolveMetric(double configured, double automatic, String name) {
        return Double.compare(configured, Region.USE_COMPUTED_SIZE) == 0
                ? automatic
                : validateMetric(configured, name);
    }

    /// Returns the standard fixed-pane width for one breakpoint.
    private static double defaultFixedPaneWidth(M3Breakpoint breakpoint) {
        return switch (breakpoint) {
            case COMPACT, MEDIUM, EXPANDED -> 360.0;
            case LARGE, EXTRA_LARGE -> 412.0;
        };
    }

    /// Recomputes the effective breakpoint, pane layout, navigation layout, and pseudo-class state.
    private void updateAdaptiveState() {
        M3PaneLayout previousPaneLayout = getEffectivePaneLayout();
        M3NavigationLayout previousNavigationLayout = getEffectiveNavigationLayout();
        int previousPaneCount = getVisiblePaneCount();

        M3Breakpoint nextBreakpoint = getBreakpointOverride();
        if (nextBreakpoint == null) {
            nextBreakpoint = M3Breakpoint.forWidth(Math.max(0.0, getWidth()));
        }
        breakpoint.set(nextBreakpoint);

        M3PaneLayout requestedPaneLayout = getPaneLayout();
        M3PaneLayout nextPaneLayout = requestedPaneLayout == M3PaneLayout.ADAPTIVE
                ? resolveAdaptivePaneLayout(nextBreakpoint)
                : resolveExplicitPaneLayout(requestedPaneLayout);
        effectivePaneLayout.set(nextPaneLayout);

        int nextVisiblePaneCount = countVisiblePanes();
        visiblePaneCount.set(nextVisiblePaneCount);
        M3NavigationLayout nextNavigationLayout = resolveNavigationLayout(nextBreakpoint, nextVisiblePaneCount);
        effectiveNavigationLayout.set(nextNavigationLayout);

        updatePseudoClasses(nextBreakpoint, nextPaneLayout, nextNavigationLayout);
        if (previousPaneLayout != nextPaneLayout || previousNavigationLayout != nextNavigationLayout) {
            notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        }
        if (previousPaneCount != nextVisiblePaneCount || previousNavigationLayout != nextNavigationLayout) {
            notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        }
        requestLayout();
    }

    /// Resolves the standard pane total from the breakpoint and installed pane slots.
    private M3PaneLayout resolveAdaptivePaneLayout(M3Breakpoint currentBreakpoint) {
        if (currentBreakpoint.getRecommendedPaneCount() <= 1
                || installedPaneCount() <= 1
                || getMainPane() == null) {
            return M3PaneLayout.SINGLE;
        }
        if (getActivePane() == M3PaneRole.TRAILING && getMainPane() != null && getTrailingPane() != null) {
            return M3PaneLayout.SPLIT_TRAILING;
        }
        if (getLeadingPane() != null && getMainPane() != null) {
            return M3PaneLayout.SPLIT_LEADING;
        }
        if (getMainPane() != null && getTrailingPane() != null) {
            return M3PaneLayout.SPLIT_TRAILING;
        }
        return M3PaneLayout.SINGLE;
    }

    /// Resolves an explicit request around empty pane slots without reserving blank regions.
    private M3PaneLayout resolveExplicitPaneLayout(M3PaneLayout requested) {
        boolean leadingInstalled = getLeadingPane() != null;
        boolean mainInstalled = getMainPane() != null;
        boolean trailingInstalled = getTrailingPane() != null;
        return switch (requested) {
            case ADAPTIVE -> throw new IllegalArgumentException("requested must be an explicit pane layout");
            case SINGLE -> M3PaneLayout.SINGLE;
            case SPLIT_LEADING -> leadingInstalled && mainInstalled
                    ? M3PaneLayout.SPLIT_LEADING
                    : M3PaneLayout.SINGLE;
            case SPLIT_TRAILING -> mainInstalled && trailingInstalled
                    ? M3PaneLayout.SPLIT_TRAILING
                    : M3PaneLayout.SINGLE;
            case FIXED_LEADING -> leadingInstalled && mainInstalled
                    ? M3PaneLayout.FIXED_LEADING
                    : M3PaneLayout.SINGLE;
            case FIXED_TRAILING -> mainInstalled && trailingInstalled
                    ? M3PaneLayout.FIXED_TRAILING
                    : M3PaneLayout.SINGLE;
            case THREE_PANE -> {
                if (leadingInstalled && mainInstalled && trailingInstalled) {
                    yield M3PaneLayout.THREE_PANE;
                }
                if (leadingInstalled && mainInstalled) {
                    yield M3PaneLayout.FIXED_LEADING;
                }
                if (mainInstalled && trailingInstalled) {
                    yield M3PaneLayout.FIXED_TRAILING;
                }
                yield M3PaneLayout.SINGLE;
            }
        };
    }

    /// Resolves the requested or breakpoint-selected navigation presentation.
    private M3NavigationLayout resolveNavigationLayout(M3Breakpoint currentBreakpoint, int paneCount) {
        M3NavigationLayout requested = getNavigationLayout();
        if (requested != M3NavigationLayout.ADAPTIVE) {
            return availableNavigationLayout(requested);
        }

        M3NavigationLayout preferred = switch (currentBreakpoint) {
            case COMPACT -> M3NavigationLayout.BAR;
            case MEDIUM -> paneCount > 1 ? M3NavigationLayout.BAR : M3NavigationLayout.RAIL;
            case EXPANDED, LARGE, EXTRA_LARGE -> M3NavigationLayout.RAIL;
        };
        M3NavigationLayout effective = availableNavigationLayout(preferred);
        if (effective != M3NavigationLayout.NONE) {
            return effective;
        }
        return availableNavigationLayout(
                preferred == M3NavigationLayout.BAR ? M3NavigationLayout.RAIL : M3NavigationLayout.BAR
        );
    }

    /// Returns a requested navigation presentation only when its slot is populated.
    private M3NavigationLayout availableNavigationLayout(M3NavigationLayout requested) {
        return switch (requested) {
            case BAR -> getNavigationBar() == null ? M3NavigationLayout.NONE : M3NavigationLayout.BAR;
            case RAIL -> getNavigationRail() == null ? M3NavigationLayout.NONE : M3NavigationLayout.RAIL;
            case ADAPTIVE, NONE -> M3NavigationLayout.NONE;
        };
    }

    /// Returns the installed pane total.
    private int installedPaneCount() {
        int count = getLeadingPane() == null ? 0 : 1;
        count += getMainPane() == null ? 0 : 1;
        count += getTrailingPane() == null ? 0 : 1;
        return count;
    }

    /// Returns the number of installed roles selected by the effective layout.
    private int countVisiblePanes() {
        int count = isPaneVisible(M3PaneRole.LEADING) ? 1 : 0;
        count += isPaneVisible(M3PaneRole.MAIN) ? 1 : 0;
        count += isPaneVisible(M3PaneRole.TRAILING) ? 1 : 0;
        return count;
    }

    /// Returns the installed pane for a logical role.
    private @Nullable Node paneForRole(M3PaneRole role) {
        return switch (role) {
            case LEADING -> getLeadingPane();
            case MAIN -> getMainPane();
            case TRAILING -> getTrailingPane();
        };
    }

    /// Resolves active-pane fallback when the requested slot is empty.
    private @Nullable M3PaneRole resolveSinglePaneRole() {
        M3PaneRole preferred = getActivePane();
        if (paneForRole(preferred) != null) {
            return preferred;
        }
        if (getMainPane() != null) {
            return M3PaneRole.MAIN;
        }
        if (getLeadingPane() != null) {
            return M3PaneRole.LEADING;
        }
        return getTrailingPane() == null ? null : M3PaneRole.TRAILING;
    }

    /// Updates all breakpoint, pane, and navigation pseudo-classes.
    private void updatePseudoClasses(
            M3Breakpoint currentBreakpoint,
            M3PaneLayout currentPaneLayout,
            M3NavigationLayout currentNavigationLayout
    ) {
        pseudoClassStateChanged(COMPACT_PSEUDO_CLASS, currentBreakpoint == M3Breakpoint.COMPACT);
        pseudoClassStateChanged(MEDIUM_PSEUDO_CLASS, currentBreakpoint == M3Breakpoint.MEDIUM);
        pseudoClassStateChanged(EXPANDED_PSEUDO_CLASS, currentBreakpoint == M3Breakpoint.EXPANDED);
        pseudoClassStateChanged(LARGE_PSEUDO_CLASS, currentBreakpoint == M3Breakpoint.LARGE);
        pseudoClassStateChanged(EXTRA_LARGE_PSEUDO_CLASS, currentBreakpoint == M3Breakpoint.EXTRA_LARGE);

        pseudoClassStateChanged(SINGLE_PANE_PSEUDO_CLASS, currentPaneLayout == M3PaneLayout.SINGLE);
        pseudoClassStateChanged(SPLIT_LEADING_PSEUDO_CLASS, currentPaneLayout == M3PaneLayout.SPLIT_LEADING);
        pseudoClassStateChanged(SPLIT_TRAILING_PSEUDO_CLASS, currentPaneLayout == M3PaneLayout.SPLIT_TRAILING);
        pseudoClassStateChanged(FIXED_LEADING_PSEUDO_CLASS, currentPaneLayout == M3PaneLayout.FIXED_LEADING);
        pseudoClassStateChanged(FIXED_TRAILING_PSEUDO_CLASS, currentPaneLayout == M3PaneLayout.FIXED_TRAILING);
        pseudoClassStateChanged(THREE_PANE_PSEUDO_CLASS, currentPaneLayout == M3PaneLayout.THREE_PANE);

        pseudoClassStateChanged(NAVIGATION_BAR_PSEUDO_CLASS, currentNavigationLayout == M3NavigationLayout.BAR);
        pseudoClassStateChanged(NAVIGATION_RAIL_PSEUDO_CLASS, currentNavigationLayout == M3NavigationLayout.RAIL);
    }

    /// Builds the effective accessibility child order.
    private List<Node> accessibleChildren() {
        ArrayList<Node> children = new ArrayList<>(8);
        addIfNonNull(children, getTopBar());
        if (getEffectiveNavigationLayout() == M3NavigationLayout.RAIL) {
            addIfNonNull(children, getNavigationRail());
        }
        if (isPaneVisible(M3PaneRole.LEADING)) {
            addIfNonNull(children, getLeadingPane());
        }
        if (isPaneVisible(M3PaneRole.MAIN)) {
            addIfNonNull(children, getMainPane());
        }
        if (isPaneVisible(M3PaneRole.TRAILING)) {
            addIfNonNull(children, getTrailingPane());
        }
        addIfNonNull(children, getTrailingRail());
        addIfNonNull(children, getBottomBar());
        if (getEffectiveNavigationLayout() == M3NavigationLayout.BAR) {
            addIfNonNull(children, getNavigationBar());
        }
        return children;
    }

    /// Adds a nullable node to an accessibility list when present.
    private static void addIfNonNull(List<Node> children, @Nullable Node node) {
        if (node != null) {
            children.add(node);
        }
    }

    /// Returns the indexed accessibility child requested by JavaFX.
    private static @Nullable Node accessibleChildAt(List<Node> children, Object... parameters) {
        if (parameters.length == 0 || !(parameters[0] instanceof Number indexValue)) {
            return null;
        }
        int index = indexValue.intValue();
        return index >= 0 && index < children.size() ? children.get(index) : null;
    }
}
