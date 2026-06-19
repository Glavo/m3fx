// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
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
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3SurfaceSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 surface container for arbitrary content.
///
/// `M3Surface` is a general-purpose themed container that applies Material surface color roles, elevation, shape,
/// and padding to a list of child nodes. It is useful for composing custom controls or demo content that should
/// still align with the active M3FX token set.
///
/// See [Material Design](https://m3.material.io/) and
/// [Material color roles](https://m3.material.io/styles/color/roles).
@NotNullByDefault
public class M3Surface extends Control {
    /// The base style class for M3FX surfaces.
    public static final String STYLE_CLASS = "m3-surface";

    /// The default surface container shape.
    private static final double DEFAULT_CONTAINER_SHAPE = 12.0;

    /// The default surface content padding.
    private static final double DEFAULT_CONTENT_PADDING = 16.0;

    /// The mutable content nodes displayed inside the surface.
    private final ObservableList<Node> content = FXCollections.observableArrayList();

    /// Notifies accessibility clients when focus moves between content children.
    private final M3AccessibleFocusNotifier focusNotifier =
            new M3AccessibleFocusNotifier(this, () -> M3Accessible.currentOrFirstFocusTarget(this, getContent()));

    // Backing property for the public surface color variant API.
    private final ObjectProperty<M3SurfaceVariant> variant =
            new SimpleObjectProperty<>(this, "variant", M3SurfaceVariant.CONTAINER) {
                /// Updates variant style classes when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3SurfaceVariant.CONTAINER);
                        return;
                    }
                    updateVariantStyle();
                }
            };

    // Backing property for the public surface elevation API.
    private final ObjectProperty<M3SurfaceElevation> elevation =
            new SimpleObjectProperty<>(this, "elevation", M3SurfaceElevation.LEVEL0) {
                /// Updates elevation style classes when the property changes.
                @Override
                protected void invalidated() {
                    if (get() == null) {
                        set(M3SurfaceElevation.LEVEL0);
                        return;
                    }
                    updateElevationStyle();
                }
            };

    // Backing property for the public container shape token API.
    private @Nullable StyleableDoubleProperty containerShape;

    // Backing property for the public content padding token API.
    private @Nullable StyleableDoubleProperty contentPadding;

    /// Creates an empty surface.
    public M3Surface() {
        initialize();
    }

    /// Creates a surface with content nodes.
    ///
    /// @param children the initial content nodes
    public M3Surface(Node... children) {
        initialize();
        getContent().addAll(children);
    }

    /// Returns the mutable content nodes displayed inside the surface.
    ///
    /// @return the mutable content nodes displayed inside the surface
    public final ObservableList<Node> getContent() {
        return content;
    }

    /// Returns the surface color variant.
    ///
    /// @return the surface color variant
    public final M3SurfaceVariant getVariant() {
        return variant.get();
    }

    /// Sets the surface color variant.
    ///
    /// @param variant the surface color variant
    public final void setVariant(M3SurfaceVariant variant) {
        this.variant.set(Objects.requireNonNull(variant, "variant"));
    }

    /// Returns the surface color variant property.
    ///
    /// @return the surface color variant property
    public final ObjectProperty<M3SurfaceVariant> variantProperty() {
        return variant;
    }

    /// Returns the surface elevation level.
    ///
    /// @return the surface elevation level
    public final M3SurfaceElevation getElevation() {
        return elevation.get();
    }

    /// Sets the surface elevation level.
    ///
    /// @param elevation the surface elevation level
    public final void setElevation(M3SurfaceElevation elevation) {
        this.elevation.set(Objects.requireNonNull(elevation, "elevation"));
    }

    /// Returns the surface elevation property.
    ///
    /// @return the surface elevation property
    public final ObjectProperty<M3SurfaceElevation> elevationProperty() {
        return elevation;
    }

    /// Returns the surface container shape token.
    ///
    /// @return the surface container shape token in pixels
    public final double getContainerShape() {
        return containerShape == null ? DEFAULT_CONTAINER_SHAPE : containerShape.get();
    }

    /// Sets the surface container shape token.
    ///
    /// @param containerShape the surface container shape token in pixels
    public final void setContainerShape(double containerShape) {
        containerShapeProperty().set(M3Css.nonNegative(containerShape, "containerShape"));
    }

    /// Returns the surface container shape token property.
    ///
    /// @return the surface container shape token property
    public final StyleableDoubleProperty containerShapeProperty() {
        if (containerShape == null) {
            containerShape = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTAINER_SHAPE,
                    this,
                    "containerShape",
                    StyleableProperties.CONTAINER_SHAPE,
                    this::requestLayout
            );
        }
        return containerShape;
    }

    /// Returns the surface content padding token.
    ///
    /// @return the surface content padding token in pixels
    public final double getContentPadding() {
        return contentPadding == null ? DEFAULT_CONTENT_PADDING : contentPadding.get();
    }

    /// Sets the surface content padding token.
    ///
    /// @param contentPadding the surface content padding token in pixels
    public final void setContentPadding(double contentPadding) {
        contentPaddingProperty().set(M3Css.nonNegative(contentPadding, "contentPadding"));
    }

    /// Returns the surface content padding token property.
    ///
    /// @return the surface content padding token property
    public final StyleableDoubleProperty contentPaddingProperty() {
        if (contentPadding == null) {
            contentPadding = M3Css.nonNegativeStyleableDoubleProperty(
                    DEFAULT_CONTENT_PADDING,
                    this,
                    "contentPadding",
                    StyleableProperties.CONTENT_PADDING,
                    this::updatePadding
            );
        }
        return contentPadding;
    }

    /// Returns the user-agent stylesheet for M3FX surfaces.
    ///
    /// @return the surface user-agent stylesheet URL
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("surface.css");
    }

    /// Creates the default Material Design 3 surface skin.
    ///
    /// @return the default Material Design 3 surface skin
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3SurfaceSkin(this);
    }

    /// Returns accessibility attributes for the surface content collection.
    ///
    /// @param attribute the requested accessibility attribute
    /// @param parameters the optional attribute parameters
    /// @return the attribute value, or `null` when unavailable
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        return switch (attribute) {
            case CONTENTS -> accessibleContents();
            case ITEM_COUNT -> getContent().size();
            case ITEM_AT_INDEX -> M3Accessible.itemAt(getContent(), parameters);
            case FOCUS_NODE -> M3Accessible.currentOrFirstFocusTarget(this, getContent());
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Executes accessibility actions for indexed surface content children.
    ///
    /// @param action the requested accessibility action
    /// @param parameters the optional action parameters
    @Override
    public void executeAccessibleAction(AccessibleAction action, Object... parameters) {
        Objects.requireNonNull(action, "action");
        switch (action) {
            case REQUEST_FOCUS -> M3Accessible.showCurrentOrItem(this, getContent());
            case SHOW_ITEM -> M3Accessible.showCurrentOrItem(this, getContent(), parameters);
            default -> super.executeAccessibleAction(action, parameters);
        }
    }

    /// Returns the CSS metadata for this node class.
    ///
    /// @return the CSS metadata for this node class
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this node.
    ///
    /// @return the CSS metadata for this node
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Initializes style classes and default metrics.
    private void initialize() {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.PARENT);
        getContent().addListener((ListChangeListener<Node>) change -> handleContentChanged());
        addEventFilter(KeyEvent.KEY_PRESSED, this::handleNavigationKeyPressed);
        focusNotifier.start();
        updateVariantStyle();
        updateElevationStyle();
        updatePadding();
    }

    /// Handles linear keyboard traversal between surface content targets.
    private void handleNavigationKeyPressed(KeyEvent event) {
        if (M3FocusTraversal.consumeNavigationKeyIfFocusOwnerInsideTextInput(this, event, true, true)) {
            return;
        }

        M3FocusTraversal.handleDirectionalKeyFocus(
                this,
                event,
                M3FocusTraversal.focusTargetsInReachableTrees(getContent()),
                true,
                true,
                -1,
                false
        );
    }

    /// Returns the single content node when the surface has one logical child.
    private @Nullable Node accessibleContents() {
        return getContent().size() == 1 ? getContent().get(0) : null;
    }

    /// Notifies accessibility clients that the surface content collection changed.
    private void notifyAccessibleItemsChanged() {
        notifyAccessibleAttributeChanged(AccessibleAttribute.CONTENTS);
        notifyAccessibleAttributeChanged(AccessibleAttribute.CHILDREN);
        notifyAccessibleAttributeChanged(AccessibleAttribute.ITEM_COUNT);
        M3Accessible.notifyFocusNodeChanged(this);
        focusNotifier.refresh();
    }

    /// Requests layout and notifies accessibility clients after content changes.
    private void handleContentChanged() {
        requestLayout();
        notifyAccessibleItemsChanged();
    }

    /// Applies the current color variant style class.
    private void updateVariantStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getVariant().getStyleClass(),
                M3SurfaceVariant.SURFACE.getStyleClass(),
                M3SurfaceVariant.CONTAINER_LOWEST.getStyleClass(),
                M3SurfaceVariant.CONTAINER_LOW.getStyleClass(),
                M3SurfaceVariant.CONTAINER.getStyleClass(),
                M3SurfaceVariant.CONTAINER_HIGH.getStyleClass(),
                M3SurfaceVariant.CONTAINER_HIGHEST.getStyleClass(),
                M3SurfaceVariant.PRIMARY_CONTAINER.getStyleClass(),
                M3SurfaceVariant.SECONDARY_CONTAINER.getStyleClass(),
                M3SurfaceVariant.TERTIARY_CONTAINER.getStyleClass()
        );
    }

    /// Applies the current elevation style class.
    private void updateElevationStyle() {
        M3ControlStyles.replaceVariant(
                this,
                getElevation().getStyleClass(),
                M3SurfaceElevation.LEVEL0.getStyleClass(),
                M3SurfaceElevation.LEVEL1.getStyleClass(),
                M3SurfaceElevation.LEVEL2.getStyleClass(),
                M3SurfaceElevation.LEVEL3.getStyleClass(),
                M3SurfaceElevation.LEVEL4.getStyleClass(),
                M3SurfaceElevation.LEVEL5.getStyleClass()
        );
    }

    /// Applies content padding from tokens.
    private void updatePadding() {
        double padding = getContentPadding();
        setPadding(new Insets(padding));
    }

    /// CSS metadata for M3FX surface tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the container shape token.
        private static final CssMetaData<M3Surface, Number> CONTAINER_SHAPE =
                new CssMetaData<>("-m3-container-shape", SizeConverter.getInstance(), DEFAULT_CONTAINER_SHAPE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Surface control) {
                        return M3Css.isSettable(control.containerShapeProperty());
                    }

                    /// Returns the styleable property for a node.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Surface control) {
                        return control.containerShapeProperty();
                    }
                };

        /// CSS metadata for the content padding token.
        private static final CssMetaData<M3Surface, Number> CONTENT_PADDING =
                new CssMetaData<>("-m3-content-padding", SizeConverter.getInstance(), DEFAULT_CONTENT_PADDING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Surface control) {
                        return M3Css.isSettable(control.contentPaddingProperty());
                    }

                    /// Returns the styleable property for a node.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Surface control) {
                        return control.contentPaddingProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(CONTAINER_SHAPE);
            styleables.add(CONTENT_PADDING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
