package org.glavo.m3fx.controls;

import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Insets;
import javafx.scene.control.DialogPane;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/// A Material Design 3 dialog pane.
@NotNullByDefault
public class M3DialogPane extends DialogPane {
    /// The base style class for m3fx dialog panes.
    public static final String STYLE_CLASS = "m3-dialog-pane";

    /// The default dialog container shape radius.
    private static final double DEFAULT_CONTAINER_SHAPE = 28.0;

    /// The default dialog content padding.
    private static final double DEFAULT_CONTENT_PADDING = 24.0;

    /// The styleable dialog container shape token.
    private StyleableDoubleProperty containerShape;

    /// The styleable dialog content padding token.
    private StyleableDoubleProperty contentPadding;

    /// Creates a dialog pane.
    public M3DialogPane() {
        M3ControlStyles.add(this, STYLE_CLASS);
        updateMetrics();
    }

    /// Returns the dialog container shape radius token.
    public final double getContainerShape() {
        return containerShape == null ? DEFAULT_CONTAINER_SHAPE : containerShape.get();
    }

    /// Sets the dialog container shape radius token.
    public final void setContainerShape(double containerShape) {
        containerShapeProperty().set(M3Css.nonNegative(containerShape, "containerShape"));
    }

    /// Returns the dialog container shape radius token property.
    public final StyleableDoubleProperty containerShapeProperty() {
        if (containerShape == null) {
            containerShape = new StyleableDoubleProperty(DEFAULT_CONTAINER_SHAPE) {
                /// Validates updated shape tokens.
                @Override
                protected void invalidated() {
                    set(M3Css.nonNegative(get(), "containerShape"));
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3DialogPane.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "containerShape";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3DialogPane, Number> getCssMetaData() {
                    return StyleableProperties.CONTAINER_SHAPE;
                }
            };
        }
        return containerShape;
    }

    /// Returns the dialog content padding token.
    public final double getContentPadding() {
        return contentPadding == null ? DEFAULT_CONTENT_PADDING : contentPadding.get();
    }

    /// Sets the dialog content padding token.
    public final void setContentPadding(double contentPadding) {
        contentPaddingProperty().set(M3Css.nonNegative(contentPadding, "contentPadding"));
    }

    /// Returns the dialog content padding token property.
    public final StyleableDoubleProperty contentPaddingProperty() {
        if (contentPadding == null) {
            contentPadding = new StyleableDoubleProperty(DEFAULT_CONTENT_PADDING) {
                /// Applies updated metrics when the token changes.
                @Override
                protected void invalidated() {
                    set(M3Css.nonNegative(get(), "contentPadding"));
                    updateMetrics();
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3DialogPane.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "contentPadding";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3DialogPane, Number> getCssMetaData() {
                    return StyleableProperties.CONTENT_PADDING;
                }
            };
        }
        return contentPadding;
    }

    /// Returns the CSS metadata for this control class.
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this dialog pane.
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }

    /// Returns the user-agent stylesheet for m3fx dialog panes.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("dialog.css");
    }

    /// Applies size-related component tokens to JavaFX layout properties.
    private void updateMetrics() {
        double padding = getContentPadding();
        setPadding(new Insets(padding));
    }

    /// CSS metadata for m3fx dialog pane component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the container shape token.
        private static final CssMetaData<M3DialogPane, Number> CONTAINER_SHAPE =
                new CssMetaData<>("-m3-container-shape", SizeConverter.getInstance(), DEFAULT_CONTAINER_SHAPE) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3DialogPane control) {
                        return M3Css.isSettable(control.containerShapeProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3DialogPane control) {
                        return control.containerShapeProperty();
                    }
                };

        /// CSS metadata for the content padding token.
        private static final CssMetaData<M3DialogPane, Number> CONTENT_PADDING =
                new CssMetaData<>("-m3-content-padding", SizeConverter.getInstance(), DEFAULT_CONTENT_PADDING) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3DialogPane control) {
                        return M3Css.isSettable(control.contentPaddingProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3DialogPane control) {
                        return control.contentPaddingProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(DialogPane.getClassCssMetaData());
            styleables.add(CONTAINER_SHAPE);
            styleables.add(CONTENT_PADDING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
