// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Insets;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import org.glavo.m3fx.internal.M3FocusTraversal;
import org.glavo.m3fx.internal.M3AccessibleFocusNotifier;
import org.glavo.m3fx.internal.M3Accessible;
import org.glavo.m3fx.internal.M3ControlStyles;
import org.glavo.m3fx.internal.M3Css;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.internal.M3ObservableLists;
import org.glavo.m3fx.skins.M3TopAppBarSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 top app bar.
///
/// `M3TopAppBar` provides navigation, title, optional subtitle, and trailing action slots for the top edge of an
/// application view. The variant property selects a small, centered, baseline tall, or flexible layout. Flexible
/// variants can transform into the small arrangement from either [#scrolledUnderProperty()] or a directly bound
/// [#collapseProgressProperty()]. The action list accepts arbitrary JavaFX nodes such as [M3IconButton] instances.
/// Navigation and action nodes participate in logical-order keyboard traversal.
///
/// A new app bar uses the small variant with empty title and subtitle, no custom title content or navigation node,
/// and no trailing actions. Flexible collapse progress is zero and scroll-under state is false.
///
/// See [Material Design app bars](https://m3.material.io/components/app-bars/overview).
@NotNullByDefault
public final class M3TopAppBar extends Control {
    /// The pseudo-class applied while content is scrolled beneath the app bar.
    private static final PseudoClass SCROLLED_UNDER_PSEUDO_CLASS = PseudoClass.getPseudoClass("scrolled-under");

    /// The pseudo-class applied while subtitle text is present.
    private static final PseudoClass HAS_SUBTITLE_PSEUDO_CLASS = PseudoClass.getPseudoClass("has-subtitle");

    /// The pseudo-class applied when a flexible app bar has completed its small-layout transformation.
    private static final PseudoClass COLLAPSED_PSEUDO_CLASS = PseudoClass.getPseudoClass("collapsed");

    /// The pseudo-class applied to the leading navigation control for app bar color roles.
    private static final PseudoClass LEADING_ACTION_PSEUDO_CLASS = PseudoClass.getPseudoClass("top-app-bar-leading");

    /// The pseudo-class applied to trailing action controls for app bar color roles.
    private static final PseudoClass TRAILING_ACTION_PSEUDO_CLASS = PseudoClass.getPseudoClass("top-app-bar-trailing");

    /// The default small top app bar container height in pixels.
    private static final double DEFAULT_CONTAINER_HEIGHT = 64.0;

    /// The default medium top app bar container height in pixels.
    private static final double DEFAULT_MEDIUM_CONTAINER_HEIGHT = 112.0;

    /// The default large top app bar container height in pixels.
    private static final double DEFAULT_LARGE_CONTAINER_HEIGHT = 152.0;

    /// The default medium flexible container height without a subtitle in pixels.
    private static final double DEFAULT_MEDIUM_FLEXIBLE_CONTAINER_HEIGHT = 112.0;

    /// The default medium flexible container height with a subtitle in pixels.
    private static final double DEFAULT_MEDIUM_FLEXIBLE_SUBTITLE_CONTAINER_HEIGHT = 136.0;

    /// The default large flexible container height without a subtitle in pixels.
    private static final double DEFAULT_LARGE_FLEXIBLE_CONTAINER_HEIGHT = 120.0;

    /// The default large flexible container height with a subtitle in pixels.
    private static final double DEFAULT_LARGE_FLEXIBLE_SUBTITLE_CONTAINER_HEIGHT = 152.0;

    /// The default outer space before leading and after trailing action slots in pixels.
    private static final double DEFAULT_EDGE_PADDING = 4.0;

    /// The default horizontal content padding in pixels.
    private static final double DEFAULT_HORIZONTAL_PADDING = 16.0;

    /// The default medium bottom content padding in pixels.
    private static final double DEFAULT_MEDIUM_BOTTOM_PADDING = 20.0;

    /// The default large bottom content padding in pixels.
    private static final double DEFAULT_LARGE_BOTTOM_PADDING = 28.0;

    /// The default bottom space below flexible title content in pixels.
    private static final double DEFAULT_FLEXIBLE_BOTTOM_PADDING = 12.0;

    /// The default spacing between the leading navigation slot and the title in pixels.
    private static final double DEFAULT_CONTENT_SPACING = 0.0;

    /// The default spacing between trailing action slots in pixels.
    private static final double DEFAULT_ACTION_SPACING = 0.0;

    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-top-app-bar";

    /// The mutable trailing action node list.
    private final ObservableList<Node> actions = M3ObservableLists.identityDistinctElementList("action");

    /// Notifies accessibility clients when focus moves between navigation and action children.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, () -> M3Accessible.currentOrFirstFocusTarget(
                    this,
                    getNavigation(),
                    getActions()
            ));

    /// Creates an empty top app bar.
    public M3TopAppBar() {
        this("");
    }

    /// Creates a top app bar with title text.
    ///
    /// @param title the title text; an empty string creates an app bar without visible title text
    /// @throws NullPointerException if `title` is `null`
    public M3TopAppBar(String title) {
        initialize();
        setTitle(title);
    }

    /// The app bar title property.
    ///
    /// The property never exposes a `null` value. Assigning `null` through the property API normalizes the value to
    /// an empty string; [#setTitle(String)] rejects `null` so ordinary setter misuse is reported immediately.
    ///
    /// @defaultValue `""`
    private final StringProperty title = new SimpleStringProperty(this, "title", "") {
        /// Normalizes direct property assignments to the non-null title contract.
        @Override
        protected void invalidated() {
            if (get() == null) {
                set("");
            }
        }
    };

    /// Returns the app bar title.
    ///
    /// @return the title text, never `null`
    public final String getTitle() {
        return title.get();
    }

    /// Sets the app bar title.
    ///
    /// @param title the title text; an empty string removes visible title text
    /// @throws NullPointerException if `title` is `null`
    public final void setTitle(String title) {
        this.title.set(Objects.requireNonNull(title, "title"));
    }

    /// Returns the `title` property.
    ///
    /// The returned property is observable and bindable. Its default value is `""`.
    ///
    /// @return the `title` property
    public final StringProperty titleProperty() {
        return title;
    }

    /// The app bar subtitle.
    ///
    /// Assigning `null` through the property normalizes the value to an empty string. An empty subtitle selects the
    /// shorter flexible-container metrics.
    ///
    /// @defaultValue `""`
    private final StringProperty subtitle = new SimpleStringProperty(this, "subtitle", "") {
        /// Updates subtitle state and height metrics when the text changes.
        @Override
        protected void invalidated() {
            if (get() == null) {
                set("");
                return;
            }
            pseudoClassStateChanged(HAS_SUBTITLE_PSEUDO_CLASS, !get().isEmpty());
            updateAccessibleText();
            updateMetrics();
        }
    };

    /// Returns the app bar subtitle.
    ///
    /// An empty string suppresses the subtitle and uses the shorter flexible container height. Small app bars use
    /// the small subtitle typography within their single row; baseline medium and large variants do not render a
    /// subtitle because the Material specification assigns subtitle support to the flexible replacements.
    ///
    /// @return the subtitle text, never `null`
    public final String getSubtitle() {
        return subtitle.get();
    }

    /// Sets the app bar subtitle.
    ///
    /// @param subtitle the subtitle text, or an empty string to remove it
    /// @throws NullPointerException if `subtitle` is `null`
    public final void setSubtitle(String subtitle) {
        this.subtitle.set(Objects.requireNonNull(subtitle, "subtitle"));
    }

    /// Returns the `subtitle` property.
    ///
    /// The returned property is observable and bindable. Its default value is `""`.
    ///
    /// @return the `subtitle` property
    public final StringProperty subtitleProperty() {
        return subtitle;
    }

    /// The optional custom title-content node.
    ///
    /// A non-null node replaces the expanded title label and is owned by this app bar.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Node> titleContent = new SimpleObjectProperty<>(this, "titleContent");

    /// Returns the optional custom title content.
    ///
    /// When present, this node replaces the expanded title label. The [#titleProperty()] remains the accessible name
    /// and supplies the compact title used during a flexible app bar collapse. A title-content node must not already
    /// belong to another parent when it is assigned to the app bar.
    ///
    /// @return the custom title content, or `null` when the title string is rendered
    public final @Nullable Node getTitleContent() {
        return titleContent.get();
    }

    /// Sets the optional custom title content.
    ///
    /// @param titleContent the custom title node, or `null` to render the title string
    public final void setTitleContent(@Nullable Node titleContent) {
        this.titleContent.set(titleContent);
    }

    /// Returns the `titleContent` property.
    ///
    /// The returned property is observable and bindable. Its default value is `null`.
    ///
    /// @return the `titleContent` property
    public final ObjectProperty<@Nullable Node> titleContentProperty() {
        return titleContent;
    }

    /// The top app bar layout variant.
    ///
    /// Assigning `null` through the property restores [M3TopAppBarVariant#SMALL].
    ///
    /// @defaultValue `SMALL`
    private final ObjectProperty<M3TopAppBarVariant> variant =
            new SimpleObjectProperty<>(this, "variant", M3TopAppBarVariant.SMALL) {
                /// Updates variant style classes and layout metrics when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3TopAppBarVariant.SMALL);
                        return;
                    }
                    updateVariantStyle();
                    updateMetrics();
                }
            };

    /// Returns the top app bar variant.
    ///
    /// @return the layout variant, never `null`
    public final M3TopAppBarVariant getVariant() {
        return variant.get();
    }

    /// Sets the top app bar variant.
    ///
    /// @param variant the layout variant
    /// @throws NullPointerException if `variant` is `null`
    public final void setVariant(M3TopAppBarVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the `variant` property.
    ///
    /// The returned property is observable and bindable. Its default value is `SMALL`.
    ///
    /// @return the `variant` property
    public final ObjectProperty<M3TopAppBarVariant> variantProperty() {
        return variant;
    }

    /// Whether content is currently scrolled beneath the app bar.
    ///
    /// For an unbound flexible app bar, changing this state drives its collapse transition.
    ///
    /// @defaultValue `false`
    private final BooleanProperty scrolledUnder = new SimpleBooleanProperty(this, "scrolledUnder") {
        /// Updates the scroll-under pseudo-class when the property changes.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(SCROLLED_UNDER_PSEUDO_CLASS, get());
        }
    };

    /// Returns whether scrollable content is currently passing beneath this app bar.
    ///
    /// When this property is `true`, the control enters the `:scrolled-under` pseudo-class so CSS can use the
    /// elevated Material container treatment used while content scrolls under the top app bar.
    ///
    /// @return `true` when content is scrolled under the app bar
    public final boolean isScrolledUnder() {
        return scrolledUnder.get();
    }

    /// Sets whether scrollable content is currently passing beneath this app bar.
    ///
    /// @param scrolledUnder `true` to apply the `:scrolled-under` visual state
    public final void setScrolledUnder(boolean scrolledUnder) {
        this.scrolledUnder.set(scrolledUnder);
    }

    /// Returns the `scrolledUnder` property.
    ///
    /// The returned property is observable and bindable. Its default value is `false`.
    ///
    /// @return the `scrolledUnder` property
    public final BooleanProperty scrolledUnderProperty() {
        return scrolledUnder;
    }

    /// The flexible app bar collapse progress.
    ///
    /// The effective value is in the closed interval `0.0..1.0`. [#setCollapseProgress(double)] rejects values
    /// outside that interval. Direct property writes are accepted and clamped by [#getCollapseProgress()]; a
    /// `NaN` property value is exposed as `0.0`.
    ///
    /// @defaultValue `0.0`
    private final DoubleProperty collapseProgress = new SimpleDoubleProperty(this, "collapseProgress", 0.0) {
        /// Updates height and layout as direct scrolling or the built-in transition changes progress.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(COLLAPSED_PSEUDO_CLASS, getCollapseProgress() >= 1.0);
            updateMetrics();
        }
    };

    /// Returns the current flexible app bar collapse progress.
    ///
    /// Zero represents the fully expanded flexible arrangement and one represents the small arrangement. Values
    /// between those endpoints are used during Material spatial motion. Applications may bind this property to a
    /// continuous scroll offset. While the property is unbound, changes to [#scrolledUnderProperty()] transition it
    /// between the two endpoints. Baseline variants ignore this value.
    ///
    /// @return the collapse progress in the closed interval from zero to one
    public final double getCollapseProgress() {
        double value = collapseProgress.get();
        return Double.isNaN(value) ? 0.0 : Math.max(0.0, Math.min(1.0, value));
    }

    /// Sets the flexible app bar collapse progress.
    ///
    /// @param collapseProgress the collapse progress in the closed interval from zero to one
    /// @throws IllegalArgumentException if the value is not finite or lies outside the supported interval
    public final void setCollapseProgress(double collapseProgress) {
        if (!Double.isFinite(collapseProgress) || collapseProgress < 0.0 || collapseProgress > 1.0) {
            throw new IllegalArgumentException("collapseProgress must be finite and between 0 and 1");
        }
        this.collapseProgress.set(collapseProgress);
    }

    /// Returns the `collapseProgress` property.
    ///
    /// The returned property is observable and bindable. Its default value is `0.0`.
    ///
    /// @return the `collapseProgress` property
    public final DoubleProperty collapseProgressProperty() {
        return collapseProgress;
    }

    /// The optional leading navigation node.
    ///
    /// A non-null node is owned by this app bar and must be available for it to parent.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Node> navigation = new SimpleObjectProperty<>(this, "navigation");

    /// Returns the optional leading navigation node.
    ///
    /// @return the leading navigation node, or `null` when the slot is empty
    public final @Nullable Node getNavigation() {
        return navigation.get();
    }

    /// Sets the optional leading navigation node.
    ///
    /// A non-null value is presented in the leading navigation slot. To be displayed, the node must not
    /// simultaneously belong to another parent.
    ///
    /// @param navigation the leading navigation node, or `null` to clear the slot
    public final void setNavigation(@Nullable Node navigation) {
        this.navigation.set(navigation);
    }

    /// Returns the `navigation` property.
    ///
    /// The returned property is observable and bindable. Its default value is `null`.
    ///
    /// @return the `navigation` property
    public final ObjectProperty<@Nullable Node> navigationProperty() {
        return navigation;
    }

    /// The small and center-aligned container height in logical pixels.
    ///
    /// @defaultValue `64.0`
    private @Nullable StyleableDoubleProperty containerHeight;

    /// Returns the small and centered top app bar container height token.
    ///
    /// @return the small and centered top app bar container height in pixels
    public final double getContainerHeight() {
        return containerHeight == null ? DEFAULT_CONTAINER_HEIGHT : containerHeight.get();
    }

    /// Sets the small and centered top app bar container height token.
    ///
    /// @param containerHeight the small and centered top app bar container height in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setContainerHeight(double containerHeight) {
        containerHeightProperty().set(M3Css.nonNegative(containerHeight, "containerHeight"));
    }

    /// Returns the `containerHeight` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `64.0` logical pixels.
    ///
    /// @return the `containerHeight` property
    public final StyleableDoubleProperty containerHeightProperty() {
        if (containerHeight == null) {
            containerHeight = createStyleableDoubleProperty(
                    DEFAULT_CONTAINER_HEIGHT,
                    "containerHeight",
                    StyleableProperties.CONTAINER_HEIGHT
            );
        }
        return containerHeight;
    }

    /// The baseline medium container height in logical pixels.
    ///
    /// @defaultValue `112.0`
    private @Nullable StyleableDoubleProperty mediumContainerHeight;

    /// Returns the medium top app bar container height token.
    ///
    /// @return the medium top app bar container height in pixels
    public final double getMediumContainerHeight() {
        return mediumContainerHeight == null ? DEFAULT_MEDIUM_CONTAINER_HEIGHT : mediumContainerHeight.get();
    }

    /// Sets the medium top app bar container height token.
    ///
    /// @param mediumContainerHeight the medium top app bar container height in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setMediumContainerHeight(double mediumContainerHeight) {
        mediumContainerHeightProperty().set(M3Css.nonNegative(mediumContainerHeight, "mediumContainerHeight"));
    }

    /// Returns the `mediumContainerHeight` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `112.0` logical pixels.
    ///
    /// @return the `mediumContainerHeight` property
    public final StyleableDoubleProperty mediumContainerHeightProperty() {
        if (mediumContainerHeight == null) {
            mediumContainerHeight = createStyleableDoubleProperty(
                    DEFAULT_MEDIUM_CONTAINER_HEIGHT,
                    "mediumContainerHeight",
                    StyleableProperties.MEDIUM_CONTAINER_HEIGHT
            );
        }
        return mediumContainerHeight;
    }

    /// The baseline large container height in logical pixels.
    ///
    /// @defaultValue `152.0`
    private @Nullable StyleableDoubleProperty largeContainerHeight;

    /// Returns the large top app bar container height token.
    ///
    /// @return the large top app bar container height in pixels
    public final double getLargeContainerHeight() {
        return largeContainerHeight == null ? DEFAULT_LARGE_CONTAINER_HEIGHT : largeContainerHeight.get();
    }

    /// Sets the large top app bar container height token.
    ///
    /// @param largeContainerHeight the large top app bar container height in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setLargeContainerHeight(double largeContainerHeight) {
        largeContainerHeightProperty().set(M3Css.nonNegative(largeContainerHeight, "largeContainerHeight"));
    }

    /// Returns the `largeContainerHeight` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `152.0` logical pixels.
    ///
    /// @return the `largeContainerHeight` property
    public final StyleableDoubleProperty largeContainerHeightProperty() {
        if (largeContainerHeight == null) {
            largeContainerHeight = createStyleableDoubleProperty(
                    DEFAULT_LARGE_CONTAINER_HEIGHT,
                    "largeContainerHeight",
                    StyleableProperties.LARGE_CONTAINER_HEIGHT
            );
        }
        return largeContainerHeight;
    }

    /// The medium flexible container height without a subtitle in logical pixels.
    ///
    /// @defaultValue `112.0`
    private @Nullable StyleableDoubleProperty mediumFlexibleContainerHeight;

    /// Returns the medium flexible container height without a subtitle.
    ///
    /// @return the medium flexible container height in pixels
    public final double getMediumFlexibleContainerHeight() {
        return mediumFlexibleContainerHeight == null
                ? DEFAULT_MEDIUM_FLEXIBLE_CONTAINER_HEIGHT
                : mediumFlexibleContainerHeight.get();
    }

    /// Sets the medium flexible container height without a subtitle.
    ///
    /// @param height the medium flexible container height in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setMediumFlexibleContainerHeight(double height) {
        mediumFlexibleContainerHeightProperty().set(M3Css.nonNegative(height, "mediumFlexibleContainerHeight"));
    }

    /// Returns the `mediumFlexibleContainerHeight` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `112.0` logical pixels.
    ///
    /// @return the `mediumFlexibleContainerHeight` property
    public final StyleableDoubleProperty mediumFlexibleContainerHeightProperty() {
        if (mediumFlexibleContainerHeight == null) {
            mediumFlexibleContainerHeight = createStyleableDoubleProperty(
                    DEFAULT_MEDIUM_FLEXIBLE_CONTAINER_HEIGHT,
                    "mediumFlexibleContainerHeight",
                    StyleableProperties.MEDIUM_FLEXIBLE_CONTAINER_HEIGHT
            );
        }
        return mediumFlexibleContainerHeight;
    }

    /// The medium flexible container height with a subtitle in logical pixels.
    ///
    /// @defaultValue `136.0`
    private @Nullable StyleableDoubleProperty mediumFlexibleSubtitleContainerHeight;

    /// Returns the medium flexible container height used while a subtitle is present.
    ///
    /// @return the medium flexible subtitle container height in pixels
    public final double getMediumFlexibleSubtitleContainerHeight() {
        return mediumFlexibleSubtitleContainerHeight == null
                ? DEFAULT_MEDIUM_FLEXIBLE_SUBTITLE_CONTAINER_HEIGHT
                : mediumFlexibleSubtitleContainerHeight.get();
    }

    /// Sets the medium flexible container height used while a subtitle is present.
    ///
    /// @param height the medium flexible subtitle container height in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setMediumFlexibleSubtitleContainerHeight(double height) {
        mediumFlexibleSubtitleContainerHeightProperty().set(
                M3Css.nonNegative(height, "mediumFlexibleSubtitleContainerHeight")
        );
    }

    /// Returns the `mediumFlexibleSubtitleContainerHeight` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `136.0` logical pixels.
    ///
    /// @return the `mediumFlexibleSubtitleContainerHeight` property
    public final StyleableDoubleProperty mediumFlexibleSubtitleContainerHeightProperty() {
        if (mediumFlexibleSubtitleContainerHeight == null) {
            mediumFlexibleSubtitleContainerHeight = createStyleableDoubleProperty(
                    DEFAULT_MEDIUM_FLEXIBLE_SUBTITLE_CONTAINER_HEIGHT,
                    "mediumFlexibleSubtitleContainerHeight",
                    StyleableProperties.MEDIUM_FLEXIBLE_SUBTITLE_CONTAINER_HEIGHT
            );
        }
        return mediumFlexibleSubtitleContainerHeight;
    }

    /// The large flexible container height without a subtitle in logical pixels.
    ///
    /// @defaultValue `120.0`
    private @Nullable StyleableDoubleProperty largeFlexibleContainerHeight;

    /// Returns the large flexible container height without a subtitle.
    ///
    /// @return the large flexible container height in pixels
    public final double getLargeFlexibleContainerHeight() {
        return largeFlexibleContainerHeight == null
                ? DEFAULT_LARGE_FLEXIBLE_CONTAINER_HEIGHT
                : largeFlexibleContainerHeight.get();
    }

    /// Sets the large flexible container height without a subtitle.
    ///
    /// @param height the large flexible container height in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setLargeFlexibleContainerHeight(double height) {
        largeFlexibleContainerHeightProperty().set(M3Css.nonNegative(height, "largeFlexibleContainerHeight"));
    }

    /// Returns the `largeFlexibleContainerHeight` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `120.0` logical pixels.
    ///
    /// @return the `largeFlexibleContainerHeight` property
    public final StyleableDoubleProperty largeFlexibleContainerHeightProperty() {
        if (largeFlexibleContainerHeight == null) {
            largeFlexibleContainerHeight = createStyleableDoubleProperty(
                    DEFAULT_LARGE_FLEXIBLE_CONTAINER_HEIGHT,
                    "largeFlexibleContainerHeight",
                    StyleableProperties.LARGE_FLEXIBLE_CONTAINER_HEIGHT
            );
        }
        return largeFlexibleContainerHeight;
    }

    /// The large flexible container height with a subtitle in logical pixels.
    ///
    /// @defaultValue `152.0`
    private @Nullable StyleableDoubleProperty largeFlexibleSubtitleContainerHeight;

    /// Returns the large flexible container height used while a subtitle is present.
    ///
    /// @return the large flexible subtitle container height in pixels
    public final double getLargeFlexibleSubtitleContainerHeight() {
        return largeFlexibleSubtitleContainerHeight == null
                ? DEFAULT_LARGE_FLEXIBLE_SUBTITLE_CONTAINER_HEIGHT
                : largeFlexibleSubtitleContainerHeight.get();
    }

    /// Sets the large flexible container height used while a subtitle is present.
    ///
    /// @param height the large flexible subtitle container height in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setLargeFlexibleSubtitleContainerHeight(double height) {
        largeFlexibleSubtitleContainerHeightProperty().set(
                M3Css.nonNegative(height, "largeFlexibleSubtitleContainerHeight")
        );
    }

    /// Returns the `largeFlexibleSubtitleContainerHeight` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `152.0` logical pixels.
    ///
    /// @return the `largeFlexibleSubtitleContainerHeight` property
    public final StyleableDoubleProperty largeFlexibleSubtitleContainerHeightProperty() {
        if (largeFlexibleSubtitleContainerHeight == null) {
            largeFlexibleSubtitleContainerHeight = createStyleableDoubleProperty(
                    DEFAULT_LARGE_FLEXIBLE_SUBTITLE_CONTAINER_HEIGHT,
                    "largeFlexibleSubtitleContainerHeight",
                    StyleableProperties.LARGE_FLEXIBLE_SUBTITLE_CONTAINER_HEIGHT
            );
        }
        return largeFlexibleSubtitleContainerHeight;
    }

    /// The outer space before the leading and after the trailing action slots in logical pixels.
    ///
    /// @defaultValue `4.0`
    private @Nullable StyleableDoubleProperty edgePadding;

    /// Returns the outer space before leading and after trailing action slots.
    ///
    /// @return the action-slot edge padding in pixels
    public final double getEdgePadding() {
        return edgePadding == null ? DEFAULT_EDGE_PADDING : edgePadding.get();
    }

    /// Sets the outer space before leading and after trailing action slots.
    ///
    /// @param edgePadding the action-slot edge padding in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setEdgePadding(double edgePadding) {
        edgePaddingProperty().set(M3Css.nonNegative(edgePadding, "edgePadding"));
    }

    /// Returns the `edgePadding` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `4.0` logical pixels.
    ///
    /// @return the `edgePadding` property
    public final StyleableDoubleProperty edgePaddingProperty() {
        if (edgePadding == null) {
            edgePadding = createStyleableDoubleProperty(
                    DEFAULT_EDGE_PADDING,
                    "edgePadding",
                    StyleableProperties.EDGE_PADDING
            );
        }
        return edgePadding;
    }

    /// The horizontal content padding in logical pixels.
    ///
    /// @defaultValue `16.0`
    private @Nullable StyleableDoubleProperty horizontalPadding;

    /// Returns the horizontal content padding token.
    ///
    /// @return the horizontal content padding in pixels
    public final double getHorizontalPadding() {
        return horizontalPadding == null ? DEFAULT_HORIZONTAL_PADDING : horizontalPadding.get();
    }

    /// Sets the horizontal content padding token.
    ///
    /// @param horizontalPadding the horizontal content padding in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setHorizontalPadding(double horizontalPadding) {
        horizontalPaddingProperty().set(M3Css.nonNegative(horizontalPadding, "horizontalPadding"));
    }

    /// Returns the `horizontalPadding` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `16.0` logical pixels.
    ///
    /// @return the `horizontalPadding` property
    public final StyleableDoubleProperty horizontalPaddingProperty() {
        if (horizontalPadding == null) {
            horizontalPadding = createStyleableDoubleProperty(
                    DEFAULT_HORIZONTAL_PADDING,
                    "horizontalPadding",
                    StyleableProperties.HORIZONTAL_PADDING
            );
        }
        return horizontalPadding;
    }

    /// The baseline medium bottom content padding in logical pixels.
    ///
    /// @defaultValue `20.0`
    private @Nullable StyleableDoubleProperty mediumBottomPadding;

    /// Returns the medium top app bar bottom padding token.
    ///
    /// @return the medium top app bar bottom padding in pixels
    public final double getMediumBottomPadding() {
        return mediumBottomPadding == null ? DEFAULT_MEDIUM_BOTTOM_PADDING : mediumBottomPadding.get();
    }

    /// Sets the medium top app bar bottom padding token.
    ///
    /// @param mediumBottomPadding the medium top app bar bottom padding in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setMediumBottomPadding(double mediumBottomPadding) {
        mediumBottomPaddingProperty().set(M3Css.nonNegative(mediumBottomPadding, "mediumBottomPadding"));
    }

    /// Returns the `mediumBottomPadding` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `20.0` logical pixels.
    ///
    /// @return the `mediumBottomPadding` property
    public final StyleableDoubleProperty mediumBottomPaddingProperty() {
        if (mediumBottomPadding == null) {
            mediumBottomPadding = createStyleableDoubleProperty(
                    DEFAULT_MEDIUM_BOTTOM_PADDING,
                    "mediumBottomPadding",
                    StyleableProperties.MEDIUM_BOTTOM_PADDING
            );
        }
        return mediumBottomPadding;
    }

    /// The baseline large bottom content padding in logical pixels.
    ///
    /// @defaultValue `28.0`
    private @Nullable StyleableDoubleProperty largeBottomPadding;

    /// Returns the large top app bar bottom padding token.
    ///
    /// @return the large top app bar bottom padding in pixels
    public final double getLargeBottomPadding() {
        return largeBottomPadding == null ? DEFAULT_LARGE_BOTTOM_PADDING : largeBottomPadding.get();
    }

    /// Sets the large top app bar bottom padding token.
    ///
    /// @param largeBottomPadding the large top app bar bottom padding in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setLargeBottomPadding(double largeBottomPadding) {
        largeBottomPaddingProperty().set(M3Css.nonNegative(largeBottomPadding, "largeBottomPadding"));
    }

    /// Returns the `largeBottomPadding` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `28.0` logical pixels.
    ///
    /// @return the `largeBottomPadding` property
    public final StyleableDoubleProperty largeBottomPaddingProperty() {
        if (largeBottomPadding == null) {
            largeBottomPadding = createStyleableDoubleProperty(
                    DEFAULT_LARGE_BOTTOM_PADDING,
                    "largeBottomPadding",
                    StyleableProperties.LARGE_BOTTOM_PADDING
            );
        }
        return largeBottomPadding;
    }

    /// The flexible title bottom padding in logical pixels.
    ///
    /// @defaultValue `12.0`
    private @Nullable StyleableDoubleProperty flexibleBottomPadding;

    /// Returns the bottom space below expanded flexible title content.
    ///
    /// @return the flexible title bottom padding in pixels
    public final double getFlexibleBottomPadding() {
        return flexibleBottomPadding == null ? DEFAULT_FLEXIBLE_BOTTOM_PADDING : flexibleBottomPadding.get();
    }

    /// Sets the bottom space below expanded flexible title content.
    ///
    /// @param flexibleBottomPadding the flexible title bottom padding in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setFlexibleBottomPadding(double flexibleBottomPadding) {
        flexibleBottomPaddingProperty().set(M3Css.nonNegative(flexibleBottomPadding, "flexibleBottomPadding"));
    }

    /// Returns the `flexibleBottomPadding` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `12.0` logical pixels.
    ///
    /// @return the `flexibleBottomPadding` property
    public final StyleableDoubleProperty flexibleBottomPaddingProperty() {
        if (flexibleBottomPadding == null) {
            flexibleBottomPadding = createStyleableDoubleProperty(
                    DEFAULT_FLEXIBLE_BOTTOM_PADDING,
                    "flexibleBottomPadding",
                    StyleableProperties.FLEXIBLE_BOTTOM_PADDING
            );
        }
        return flexibleBottomPadding;
    }

    /// The spacing between leading, title, and trailing content slots in logical pixels.
    ///
    /// @defaultValue `0.0`
    private @Nullable StyleableDoubleProperty contentSpacing;

    /// Returns the spacing token between leading, title, and trailing content slots.
    ///
    /// @return the content slot spacing in pixels
    public final double getContentSpacing() {
        return contentSpacing == null ? DEFAULT_CONTENT_SPACING : contentSpacing.get();
    }

    /// Sets the spacing token between leading, title, and trailing content slots.
    ///
    /// @param contentSpacing the content slot spacing in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setContentSpacing(double contentSpacing) {
        contentSpacingProperty().set(M3Css.nonNegative(contentSpacing, "contentSpacing"));
    }

    /// Returns the `contentSpacing` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `0.0` logical pixels.
    ///
    /// @return the `contentSpacing` property
    public final StyleableDoubleProperty contentSpacingProperty() {
        if (contentSpacing == null) {
            contentSpacing = createStyleableDoubleProperty(
                    DEFAULT_CONTENT_SPACING,
                    "contentSpacing",
                    StyleableProperties.CONTENT_SPACING
            );
        }
        return contentSpacing;
    }

    /// The spacing between trailing action nodes in logical pixels.
    ///
    /// @defaultValue `0.0`
    private @Nullable StyleableDoubleProperty actionSpacing;

    /// Returns the spacing token between trailing action nodes.
    ///
    /// @return the trailing action spacing in pixels
    public final double getActionSpacing() {
        return actionSpacing == null ? DEFAULT_ACTION_SPACING : actionSpacing.get();
    }

    /// Sets the spacing token between trailing action nodes.
    ///
    /// @param actionSpacing the trailing action spacing in pixels
    /// @throws IllegalArgumentException if the supplied value is negative or not finite
    public final void setActionSpacing(double actionSpacing) {
        actionSpacingProperty().set(M3Css.nonNegative(actionSpacing, "actionSpacing"));
    }

    /// Returns the `actionSpacing` property.
    ///
    /// The returned property is observable, bindable, and styleable. It accepts finite, non-negative values and has a
    /// default value of `0.0` logical pixels.
    ///
    /// @return the `actionSpacing` property
    public final StyleableDoubleProperty actionSpacingProperty() {
        if (actionSpacing == null) {
            actionSpacing = createStyleableDoubleProperty(
                    DEFAULT_ACTION_SPACING,
                    "actionSpacing",
                    StyleableProperties.ACTION_SPACING
            );
        }
        return actionSpacing;
    }

    /// Returns the mutable trailing action node list.
    ///
    /// Changes to the returned list are observed immediately. The list rejects `null` elements and repeated
    /// occurrences of the same node instance, and validates bulk mutations before changing. Each node must satisfy
    /// the JavaFX single-parent rule while displayed.
    ///
    /// @return the live, mutable list of trailing action nodes
    public final ObservableList<Node> getActions() {
        return actions;
    }

    /// Returns the user-agent stylesheet for M3FX top app bars.
    ///
    /// @return the top app bar user-agent stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("top-app-bar.css");
    }

    /// Returns the CSS metadata for this control class.
    ///
    /// @return the immutable CSS metadata list for this class
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    ///
    /// @return the immutable CSS metadata list for this control
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Initializes style classes, accessibility metadata, and property listeners.
    private void initialize() {
        M3ControlStyles.initialize(this, DEFAULT_STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TOOL_BAR);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleItem, this::showAccessibleItem);
        addEventFilter(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        title.addListener(observable -> updateAccessibleText());
        titleContent.addListener((observable, oldValue, newValue) -> requestLayout());
        navigation.addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.pseudoClassStateChanged(LEADING_ACTION_PSEUDO_CLASS, false);
            }
            if (newValue != null) {
                newValue.pseudoClassStateChanged(LEADING_ACTION_PSEUDO_CLASS, true);
            }
            notifyAccessibleItemsChanged();
        });
        actions.addListener(this::handleActionsChanged);
        focusNotifier.start();
        updateAccessibleText();
        updateVariantStyle();
        updateMetrics();
    }

    /// Returns accessibility attributes for the title, subtitle, and action collection.
    ///
    /// @param attribute  the requested accessibility attribute
    /// @param parameters the optional attribute parameters
    /// @return the attribute value, or `null` when unavailable
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        return switch (attribute) {
            case TEXT -> getSubtitle().isEmpty() ? getTitle() : getTitle() + ", " + getSubtitle();
            case ITEM_COUNT -> M3Accessible.itemCount(getNavigation(), getActions());
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getNavigation(), getActions(), parameters);
            case FOCUS_NODE -> M3Accessible.currentOrFirstFocusTarget(this, getNavigation(), getActions());
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions for indexed navigation and action children.
    ///
    /// @param action     the requested accessibility action
    /// @param parameters the optional action parameters
    /// @throws NullPointerException if `action` is `null`
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        if (isDisabled()) {
            super.executeAccessibleAction(action, parameters);
            return;
        }

        switch (action) {
            case REQUEST_FOCUS -> focusAccessibleItem();
            case SHOW_ITEM -> showAccessibleItem(parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Requests focus on the current or first accessibility item.
    ///
    /// @return `true` when the target accepted focus
    final boolean focusAccessibleItem() {
        if (M3Accessible.showCurrentOrItem(this, getNavigation(), getActions())) {
            notifyAccessibleFocusChanged();
            return true;
        }
        return false;
    }

    /// Shows an item requested by an accessibility client.
    ///
    /// @param parameters optional accessibility target parameters
    /// @return `true` when focus moved to the default or requested item
    final boolean showAccessibleItem(Object... parameters) {
        if (M3Accessible.showCurrentOrItem(this, getNavigation(), getActions(), parameters)) {
            notifyAccessibleFocusChanged();
            return true;
        }
        return false;
    }

    /// Notifies accessibility clients that the container focus target changed.
    private void notifyAccessibleFocusChanged() {
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Returns the default visual representation of this control.
    ///
    /// @return the default visual representation
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3TopAppBarSkin(this);
    }

    /// Handles keyboard traversal between focusable navigation and action items.
    private void handleNavigationKeyPressed(KeyEvent event) {
        M3FocusTraversal.handleHorizontalKeyFocus(
                this,
                event,
                M3FocusTraversal.focusTargets(getNavigation(), getActions())
        );
    }

    /// Updates the accessible text exposed by the app bar.
    private void updateAccessibleText() {
        setAccessibleText(getSubtitle().isEmpty() ? getTitle() : getTitle() + ", " + getSubtitle());
        notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
    }

    /// Notifies accessibility clients that the indexed app bar item collection changed.
    private void notifyAccessibleItemsChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Updates trailing action color-role state and accessibility after list changes.
    private void handleActionsChanged(ListChangeListener.Change<? extends Node> change) {
        while (change.next()) {
            for (Node removed : change.getRemoved()) {
                removed.pseudoClassStateChanged(TRAILING_ACTION_PSEUDO_CLASS, false);
            }
            if (change.wasAdded()) {
                for (Node added : change.getAddedSubList()) {
                    added.pseudoClassStateChanged(TRAILING_ACTION_PSEUDO_CLASS, true);
                }
            }
        }
        notifyAccessibleItemsChanged();
    }

    /// Updates the active variant style class.
    private void updateVariantStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getVariant().styleClass(),
                M3TopAppBarVariant.SMALL.styleClass(),
                M3TopAppBarVariant.CENTER_ALIGNED.styleClass(),
                M3TopAppBarVariant.MEDIUM.styleClass(),
                M3TopAppBarVariant.LARGE.styleClass(),
                M3TopAppBarVariant.MEDIUM_FLEXIBLE.styleClass(),
                M3TopAppBarVariant.LARGE_FLEXIBLE.styleClass()
        );
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double expandedHeight = switch (getVariant()) {
            case MEDIUM -> getMediumContainerHeight();
            case LARGE -> getLargeContainerHeight();
            case MEDIUM_FLEXIBLE -> getSubtitle().isEmpty()
                    ? getMediumFlexibleContainerHeight()
                    : getMediumFlexibleSubtitleContainerHeight();
            case LARGE_FLEXIBLE -> getSubtitle().isEmpty()
                    ? getLargeFlexibleContainerHeight()
                    : getLargeFlexibleSubtitleContainerHeight();
            case SMALL, CENTER_ALIGNED -> getContainerHeight();
        };
        double height = switch (getVariant()) {
            case MEDIUM_FLEXIBLE, LARGE_FLEXIBLE -> expandedHeight
                    + (getContainerHeight() - expandedHeight) * getCollapseProgress();
            case SMALL, CENTER_ALIGNED, MEDIUM, LARGE -> expandedHeight;
        };
        M3Css.setMinHeightIfUnbound(this, height);
        M3Css.setPrefHeightIfUnbound(this, height);
        M3Css.setPaddingIfUnbound(this, Insets.EMPTY);
        requestLayout();
    }

    /// Creates a non-negative CSS-backed size token property.
    private StyleableDoubleProperty createStyleableDoubleProperty(
            double initialValue,
            String name,
            CssMetaData<M3TopAppBar, Number> cssMetaData
    ) {
        return M3Css.nonNegativeStyleableDoubleProperty(
                initialValue,
                this,
                name,
                cssMetaData,
                this::updateMetrics
        );
    }

    /// CSS metadata for M3FX top app bar component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the small and centered container height token.
        private static final CssMetaData<M3TopAppBar, Number> CONTAINER_HEIGHT =
                createSizeCssMetaData("-m3-container-height", DEFAULT_CONTAINER_HEIGHT,
                        M3TopAppBar::containerHeightProperty);

        /// CSS metadata for the medium container height token.
        private static final CssMetaData<M3TopAppBar, Number> MEDIUM_CONTAINER_HEIGHT =
                createSizeCssMetaData("-m3-medium-container-height", DEFAULT_MEDIUM_CONTAINER_HEIGHT,
                        M3TopAppBar::mediumContainerHeightProperty);

        /// CSS metadata for the large container height token.
        private static final CssMetaData<M3TopAppBar, Number> LARGE_CONTAINER_HEIGHT =
                createSizeCssMetaData("-m3-large-container-height", DEFAULT_LARGE_CONTAINER_HEIGHT,
                        M3TopAppBar::largeContainerHeightProperty);

        /// CSS metadata for the medium flexible container height token.
        private static final CssMetaData<M3TopAppBar, Number> MEDIUM_FLEXIBLE_CONTAINER_HEIGHT =
                createSizeCssMetaData(
                        "-m3-medium-flexible-container-height",
                        DEFAULT_MEDIUM_FLEXIBLE_CONTAINER_HEIGHT,
                        M3TopAppBar::mediumFlexibleContainerHeightProperty
                );

        /// CSS metadata for the medium flexible subtitle container height token.
        private static final CssMetaData<M3TopAppBar, Number> MEDIUM_FLEXIBLE_SUBTITLE_CONTAINER_HEIGHT =
                createSizeCssMetaData(
                        "-m3-medium-flexible-subtitle-container-height",
                        DEFAULT_MEDIUM_FLEXIBLE_SUBTITLE_CONTAINER_HEIGHT,
                        M3TopAppBar::mediumFlexibleSubtitleContainerHeightProperty
                );

        /// CSS metadata for the large flexible container height token.
        private static final CssMetaData<M3TopAppBar, Number> LARGE_FLEXIBLE_CONTAINER_HEIGHT =
                createSizeCssMetaData(
                        "-m3-large-flexible-container-height",
                        DEFAULT_LARGE_FLEXIBLE_CONTAINER_HEIGHT,
                        M3TopAppBar::largeFlexibleContainerHeightProperty
                );

        /// CSS metadata for the large flexible subtitle container height token.
        private static final CssMetaData<M3TopAppBar, Number> LARGE_FLEXIBLE_SUBTITLE_CONTAINER_HEIGHT =
                createSizeCssMetaData(
                        "-m3-large-flexible-subtitle-container-height",
                        DEFAULT_LARGE_FLEXIBLE_SUBTITLE_CONTAINER_HEIGHT,
                        M3TopAppBar::largeFlexibleSubtitleContainerHeightProperty
                );

        /// CSS metadata for the leading and trailing edge-padding token.
        private static final CssMetaData<M3TopAppBar, Number> EDGE_PADDING =
                createSizeCssMetaData("-m3-edge-padding", DEFAULT_EDGE_PADDING, M3TopAppBar::edgePaddingProperty);

        /// CSS metadata for the horizontal padding token.
        private static final CssMetaData<M3TopAppBar, Number> HORIZONTAL_PADDING =
                createSizeCssMetaData("-m3-horizontal-padding", DEFAULT_HORIZONTAL_PADDING,
                        M3TopAppBar::horizontalPaddingProperty);

        /// CSS metadata for the medium bottom padding token.
        private static final CssMetaData<M3TopAppBar, Number> MEDIUM_BOTTOM_PADDING =
                createSizeCssMetaData("-m3-medium-bottom-padding", DEFAULT_MEDIUM_BOTTOM_PADDING,
                        M3TopAppBar::mediumBottomPaddingProperty);

        /// CSS metadata for the large bottom padding token.
        private static final CssMetaData<M3TopAppBar, Number> LARGE_BOTTOM_PADDING =
                createSizeCssMetaData("-m3-large-bottom-padding", DEFAULT_LARGE_BOTTOM_PADDING,
                        M3TopAppBar::largeBottomPaddingProperty);

        /// CSS metadata for the flexible title bottom-padding token.
        private static final CssMetaData<M3TopAppBar, Number> FLEXIBLE_BOTTOM_PADDING =
                createSizeCssMetaData(
                        "-m3-flexible-bottom-padding",
                        DEFAULT_FLEXIBLE_BOTTOM_PADDING,
                        M3TopAppBar::flexibleBottomPaddingProperty
                );

        /// CSS metadata for the content slot spacing token.
        private static final CssMetaData<M3TopAppBar, Number> CONTENT_SPACING =
                createSizeCssMetaData("-m3-content-spacing", DEFAULT_CONTENT_SPACING,
                        M3TopAppBar::contentSpacingProperty);

        /// CSS metadata for the trailing action spacing token.
        private static final CssMetaData<M3TopAppBar, Number> ACTION_SPACING =
                createSizeCssMetaData("-m3-action-spacing", DEFAULT_ACTION_SPACING,
                        M3TopAppBar::actionSpacingProperty);

        /// The complete immutable CSS metadata list.
        private static final @Unmodifiable List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(CONTAINER_HEIGHT);
            styleables.add(MEDIUM_CONTAINER_HEIGHT);
            styleables.add(LARGE_CONTAINER_HEIGHT);
            styleables.add(MEDIUM_FLEXIBLE_CONTAINER_HEIGHT);
            styleables.add(MEDIUM_FLEXIBLE_SUBTITLE_CONTAINER_HEIGHT);
            styleables.add(LARGE_FLEXIBLE_CONTAINER_HEIGHT);
            styleables.add(LARGE_FLEXIBLE_SUBTITLE_CONTAINER_HEIGHT);
            styleables.add(EDGE_PADDING);
            styleables.add(HORIZONTAL_PADDING);
            styleables.add(MEDIUM_BOTTOM_PADDING);
            styleables.add(LARGE_BOTTOM_PADDING);
            styleables.add(FLEXIBLE_BOTTOM_PADDING);
            styleables.add(CONTENT_SPACING);
            styleables.add(ACTION_SPACING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }

        /// Prevents CSS metadata holder instantiation.
        private StyleableProperties() {
        }

        /// Creates CSS metadata for a non-negative size token.
        private static CssMetaData<M3TopAppBar, Number> createSizeCssMetaData(
                String property,
                double initialValue,
                StyleablePropertyAccessor accessor
        ) {
            return new CssMetaData<>(property, SizeConverter.getInstance(), initialValue) {
                /// Returns whether this property can be set by CSS.
                @Override
                public boolean isSettable(M3TopAppBar control) {
                    return M3Css.isSettable(accessor.property(control));
                }

                /// Returns the styleable property for a control.
                @Override
                public StyleableProperty<Number> getStyleableProperty(M3TopAppBar control) {
                    return accessor.property(control);
                }
            };
        }

        /// Provides access to a styleable top app bar size token property.
        @NotNullByDefault
        @FunctionalInterface
        private interface StyleablePropertyAccessor {
            /// Returns the property for the supplied top app bar.
            StyleableDoubleProperty property(M3TopAppBar control);
        }
    }
}
