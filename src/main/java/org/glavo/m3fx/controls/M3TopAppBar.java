// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
/// `M3TopAppBar` provides navigation, title, and trailing action slots for the top edge of an application view.
/// The variant property selects the small, medium, large, or centered layout metrics, while the action list
/// allows arbitrary JavaFX nodes such as [M3IconButton] instances. The scroll-under state exposes the
/// `:scrolled-under` pseudo-class used when content moves beneath the app bar.
///
/// See [Material Design app bars](https://m3.material.io/components/app-bars/overview).
@NotNullByDefault
public class M3TopAppBar extends Control {
    /// The pseudo-class applied while content is scrolled beneath the app bar.
    private static final PseudoClass SCROLLED_UNDER_PSEUDO_CLASS = PseudoClass.getPseudoClass("scrolled-under");

    /// The default small top app bar container height in pixels.
    private static final double DEFAULT_CONTAINER_HEIGHT = 64.0;

    /// The default medium top app bar container height in pixels.
    private static final double DEFAULT_MEDIUM_CONTAINER_HEIGHT = 112.0;

    /// The default large top app bar container height in pixels.
    private static final double DEFAULT_LARGE_CONTAINER_HEIGHT = 152.0;

    /// The default horizontal content padding in pixels.
    private static final double DEFAULT_HORIZONTAL_PADDING = 16.0;

    /// The default medium bottom content padding in pixels.
    private static final double DEFAULT_MEDIUM_BOTTOM_PADDING = 20.0;

    /// The default large bottom content padding in pixels.
    private static final double DEFAULT_LARGE_BOTTOM_PADDING = 28.0;

    /// The default spacing between the leading navigation slot and the title in pixels.
    private static final double DEFAULT_CONTENT_SPACING = 8.0;

    /// The default spacing between trailing action slots in pixels.
    private static final double DEFAULT_ACTION_SPACING = 0.0;

    /// The base style class for M3FX top app bars.
    public static final String STYLE_CLASS = "m3-top-app-bar";

    /// The navigation slot style class.
    public static final String NAVIGATION_STYLE_CLASS = "m3-top-app-bar-navigation";

    /// The title label style class.
    public static final String TITLE_STYLE_CLASS = "m3-top-app-bar-title";

    /// The actions container style class.
    public static final String ACTIONS_STYLE_CLASS = "m3-top-app-bar-actions";

    /// The style class applied to each 48 dp trailing action slot.
    public static final String ACTION_SLOT_STYLE_CLASS = "m3-top-app-bar-action-slot";

    // The app bar title text property.
    private final StringProperty title = new SimpleStringProperty(this, "title", "");

    // The top app bar variant property.
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

    // Whether scrollable content currently passes beneath this app bar.
    private final BooleanProperty scrolledUnder = new SimpleBooleanProperty(this, "scrolledUnder") {
        /// Updates the scroll-under pseudo-class when the property changes.
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(SCROLLED_UNDER_PSEUDO_CLASS, get());
        }
    };

    // The optional leading navigation node property.
    private final ObjectProperty<@Nullable Node> navigation = new SimpleObjectProperty<>(this, "navigation");

    /// The mutable trailing action node list.
    private final ObservableList<Node> actions = M3ObservableLists.nonNullElementList("action");

    // The small and centered app bar container height token.
    private @Nullable StyleableDoubleProperty containerHeight;

    // The medium app bar container height token.
    private @Nullable StyleableDoubleProperty mediumContainerHeight;

    // The large app bar container height token.
    private @Nullable StyleableDoubleProperty largeContainerHeight;

    // The horizontal content padding token.
    private @Nullable StyleableDoubleProperty horizontalPadding;

    // The medium app bar bottom content padding token.
    private @Nullable StyleableDoubleProperty mediumBottomPadding;

    // The large app bar bottom content padding token.
    private @Nullable StyleableDoubleProperty largeBottomPadding;

    // The spacing token between leading, title, and trailing content slots.
    private @Nullable StyleableDoubleProperty contentSpacing;

    // The spacing token between trailing action nodes.
    private @Nullable StyleableDoubleProperty actionSpacing;

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
    public M3TopAppBar(String title) {
        initialize();
        setTitle(title);
    }


    /// Returns the app bar title.
    public final String getTitle() {
        return title.get();
    }

    /// Sets the app bar title.
    public final void setTitle(String title) {
        this.title.set(Objects.requireNonNull(title, "title"));
    }

    /// Returns the app bar title property.
    public final StringProperty titleProperty() {
        return title;
    }

    /// Returns the top app bar variant.
    public final M3TopAppBarVariant getVariant() {
        return variant.get();
    }

    /// Sets the top app bar variant.
    public final void setVariant(M3TopAppBarVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the top app bar variant property.
    public final ObjectProperty<M3TopAppBarVariant> variantProperty() {
        return variant;
    }

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

    /// Returns the scroll-under state property.
    public final BooleanProperty scrolledUnderProperty() {
        return scrolledUnder;
    }

    /// Returns the optional leading navigation node.
    public final @Nullable Node getNavigation() {
        return navigation.get();
    }

    /// Sets the optional leading navigation node.
    public final void setNavigation(@Nullable Node navigation) {
        this.navigation.set(navigation);
    }

    /// Returns the optional leading navigation node property.
    public final ObjectProperty<@Nullable Node> navigationProperty() {
        return navigation;
    }

    /// Returns the mutable trailing action node list.
    public final ObservableList<Node> getActions() {
        return actions;
    }

    /// Returns the small and centered top app bar container height token.
    ///
    /// @return the small and centered top app bar container height in pixels
    public final double getContainerHeight() {
        return containerHeight == null ? DEFAULT_CONTAINER_HEIGHT : containerHeight.get();
    }

    /// Sets the small and centered top app bar container height token.
    ///
    /// @param containerHeight the small and centered top app bar container height in pixels
    public final void setContainerHeight(double containerHeight) {
        containerHeightProperty().set(M3Css.nonNegative(containerHeight, "containerHeight"));
    }

    /// Returns the small and centered top app bar container height token property.
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

    /// Returns the medium top app bar container height token.
    ///
    /// @return the medium top app bar container height in pixels
    public final double getMediumContainerHeight() {
        return mediumContainerHeight == null ? DEFAULT_MEDIUM_CONTAINER_HEIGHT : mediumContainerHeight.get();
    }

    /// Sets the medium top app bar container height token.
    ///
    /// @param mediumContainerHeight the medium top app bar container height in pixels
    public final void setMediumContainerHeight(double mediumContainerHeight) {
        mediumContainerHeightProperty().set(M3Css.nonNegative(mediumContainerHeight, "mediumContainerHeight"));
    }

    /// Returns the medium top app bar container height token property.
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

    /// Returns the large top app bar container height token.
    ///
    /// @return the large top app bar container height in pixels
    public final double getLargeContainerHeight() {
        return largeContainerHeight == null ? DEFAULT_LARGE_CONTAINER_HEIGHT : largeContainerHeight.get();
    }

    /// Sets the large top app bar container height token.
    ///
    /// @param largeContainerHeight the large top app bar container height in pixels
    public final void setLargeContainerHeight(double largeContainerHeight) {
        largeContainerHeightProperty().set(M3Css.nonNegative(largeContainerHeight, "largeContainerHeight"));
    }

    /// Returns the large top app bar container height token property.
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

    /// Returns the horizontal content padding token.
    ///
    /// @return the horizontal content padding in pixels
    public final double getHorizontalPadding() {
        return horizontalPadding == null ? DEFAULT_HORIZONTAL_PADDING : horizontalPadding.get();
    }

    /// Sets the horizontal content padding token.
    ///
    /// @param horizontalPadding the horizontal content padding in pixels
    public final void setHorizontalPadding(double horizontalPadding) {
        horizontalPaddingProperty().set(M3Css.nonNegative(horizontalPadding, "horizontalPadding"));
    }

    /// Returns the horizontal content padding token property.
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

    /// Returns the medium top app bar bottom padding token.
    ///
    /// @return the medium top app bar bottom padding in pixels
    public final double getMediumBottomPadding() {
        return mediumBottomPadding == null ? DEFAULT_MEDIUM_BOTTOM_PADDING : mediumBottomPadding.get();
    }

    /// Sets the medium top app bar bottom padding token.
    ///
    /// @param mediumBottomPadding the medium top app bar bottom padding in pixels
    public final void setMediumBottomPadding(double mediumBottomPadding) {
        mediumBottomPaddingProperty().set(M3Css.nonNegative(mediumBottomPadding, "mediumBottomPadding"));
    }

    /// Returns the medium top app bar bottom padding token property.
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

    /// Returns the large top app bar bottom padding token.
    ///
    /// @return the large top app bar bottom padding in pixels
    public final double getLargeBottomPadding() {
        return largeBottomPadding == null ? DEFAULT_LARGE_BOTTOM_PADDING : largeBottomPadding.get();
    }

    /// Sets the large top app bar bottom padding token.
    ///
    /// @param largeBottomPadding the large top app bar bottom padding in pixels
    public final void setLargeBottomPadding(double largeBottomPadding) {
        largeBottomPaddingProperty().set(M3Css.nonNegative(largeBottomPadding, "largeBottomPadding"));
    }

    /// Returns the large top app bar bottom padding token property.
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

    /// Returns the spacing token between leading, title, and trailing content slots.
    ///
    /// @return the content slot spacing in pixels
    public final double getContentSpacing() {
        return contentSpacing == null ? DEFAULT_CONTENT_SPACING : contentSpacing.get();
    }

    /// Sets the spacing token between leading, title, and trailing content slots.
    ///
    /// @param contentSpacing the content slot spacing in pixels
    public final void setContentSpacing(double contentSpacing) {
        contentSpacingProperty().set(M3Css.nonNegative(contentSpacing, "contentSpacing"));
    }

    /// Returns the spacing token property between leading, title, and trailing content slots.
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

    /// Returns the spacing token between trailing action nodes.
    ///
    /// @return the trailing action spacing in pixels
    public final double getActionSpacing() {
        return actionSpacing == null ? DEFAULT_ACTION_SPACING : actionSpacing.get();
    }

    /// Sets the spacing token between trailing action nodes.
    ///
    /// @param actionSpacing the trailing action spacing in pixels
    public final void setActionSpacing(double actionSpacing) {
        actionSpacingProperty().set(M3Css.nonNegative(actionSpacing, "actionSpacing"));
    }

    /// Returns the spacing token property between trailing action nodes.
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

    /// Returns the user-agent stylesheet for M3FX top app bars.
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
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Initializes style classes, accessibility metadata, and property listeners.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.TOOL_BAR);
        setFocusTraversable(false);
        M3Accessible.installAccessibleActionRoute(this, this::focusAccessibleItem, this::showAccessibleItem);
        addEventFilter(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        title.addListener(observable -> updateAccessibleText());
        navigation.addListener((observable, oldValue, newValue) -> notifyAccessibleItemsChanged());
        actions.addListener((ListChangeListener<Node>) change -> notifyAccessibleItemsChanged());
        focusNotifier.start();
        updateAccessibleText();
        updateVariantStyle();
        updateMetrics();
    }

    /// Returns accessibility attributes for the title and action collection.
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        return switch (attribute) {
            case TEXT -> getTitle();
            case ITEM_COUNT -> M3Accessible.itemCount(getNavigation(), getActions());
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getNavigation(), getActions(), parameters);
            case FOCUS_NODE -> M3Accessible.currentOrFirstFocusTarget(this, getNavigation(), getActions());
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions for indexed navigation and action children.
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
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

    /// Creates the default Material Design 3 top app bar skin.
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
        setAccessibleText(getTitle());
        notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
    }

    /// Notifies accessibility clients that the indexed app bar item collection changed.
    private void notifyAccessibleItemsChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Updates the active variant style class.
    private void updateVariantStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getVariant().getStyleClass(),
                M3TopAppBarVariant.SMALL.getStyleClass(),
                M3TopAppBarVariant.CENTER_ALIGNED.getStyleClass(),
                M3TopAppBarVariant.MEDIUM.getStyleClass(),
                M3TopAppBarVariant.LARGE.getStyleClass()
        );
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double height = switch (getVariant()) {
            case MEDIUM -> getMediumContainerHeight();
            case LARGE -> getLargeContainerHeight();
            case SMALL, CENTER_ALIGNED -> getContainerHeight();
        };
        double bottomPadding = switch (getVariant()) {
            case MEDIUM -> getMediumBottomPadding();
            case LARGE -> getLargeBottomPadding();
            case SMALL, CENTER_ALIGNED -> 0.0;
        };
        double horizontalPadding = getHorizontalPadding();
        M3Css.setMinHeightIfUnbound(this, height);
        M3Css.setPrefHeightIfUnbound(this, height);
        M3Css.setPaddingIfUnbound(this, new Insets(0.0, horizontalPadding, bottomPadding, horizontalPadding));
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
            styleables.add(HORIZONTAL_PADDING);
            styleables.add(MEDIUM_BOTTOM_PADDING);
            styleables.add(LARGE_BOTTOM_PADDING);
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
