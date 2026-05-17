// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.geometry.Orientation;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.glavo.m3fx.internal.M3Stylesheets;
import org.glavo.m3fx.skins.M3DividerSkin;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/// A Material Design 3 divider.
@NotNullByDefault
public class M3Divider extends Control {
    /// The base style class for m3fx dividers.
    public static final String STYLE_CLASS = "m3-divider";

    /// The default divider thickness.
    private static final double DEFAULT_THICKNESS = 1.0;

    /// The default leading inset.
    private static final double DEFAULT_INSET_START = 0.0;

    /// The default trailing inset.
    private static final double DEFAULT_INSET_END = 0.0;

    /// The divider orientation property.
    private final ObjectProperty<Orientation> orientation = new SimpleObjectProperty<>(this, "orientation", Orientation.HORIZONTAL) {
        /// Restores the default orientation when a null value is assigned.
        @Override
        protected void invalidated() {
            if (get() == null) {
                set(Orientation.HORIZONTAL);
            }
        }
    };

    /// The styleable divider thickness token.
    private StyleableDoubleProperty thickness;

    /// The styleable leading inset token.
    private StyleableDoubleProperty insetStart;

    /// The styleable trailing inset token.
    private StyleableDoubleProperty insetEnd;

    /// Creates a horizontal divider.
    public M3Divider() {
        this(Orientation.HORIZONTAL);
    }

    /// Creates a divider with the requested orientation.
    public M3Divider(Orientation orientation) {
        M3ControlStyles.add(this, STYLE_CLASS);
        setAccessibleRole(AccessibleRole.NODE);
        setOrientation(orientation);
    }

    /// Returns the divider orientation.
    public final Orientation getOrientation() {
        return orientation.get();
    }

    /// Sets the divider orientation.
    public final void setOrientation(Orientation orientation) {
        this.orientation.set(Objects.requireNonNull(orientation, "orientation"));
    }

    /// Returns the divider orientation property.
    public final ObjectProperty<Orientation> orientationProperty() {
        return orientation;
    }

    /// Returns the divider thickness token.
    public final double getThickness() {
        return thickness == null ? DEFAULT_THICKNESS : thickness.get();
    }

    /// Sets the divider thickness token.
    public final void setThickness(double thickness) {
        thicknessProperty().set(M3Css.nonNegative(thickness, "thickness"));
    }

    /// Returns the divider thickness token property.
    public final StyleableDoubleProperty thicknessProperty() {
        if (thickness == null) {
            thickness = new StyleableDoubleProperty(DEFAULT_THICKNESS) {
                /// Validates updated thickness tokens.
                @Override
                protected void invalidated() {
                    set(M3Css.nonNegative(get(), "thickness"));
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Divider.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "thickness";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3Divider, Number> getCssMetaData() {
                    return StyleableProperties.THICKNESS;
                }
            };
        }
        return thickness;
    }

    /// Returns the leading inset token.
    public final double getInsetStart() {
        return insetStart == null ? DEFAULT_INSET_START : insetStart.get();
    }

    /// Sets the leading inset token.
    public final void setInsetStart(double insetStart) {
        insetStartProperty().set(M3Css.nonNegative(insetStart, "insetStart"));
    }

    /// Returns the leading inset token property.
    public final StyleableDoubleProperty insetStartProperty() {
        if (insetStart == null) {
            insetStart = new StyleableDoubleProperty(DEFAULT_INSET_START) {
                /// Validates updated inset tokens.
                @Override
                protected void invalidated() {
                    set(M3Css.nonNegative(get(), "insetStart"));
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Divider.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "insetStart";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3Divider, Number> getCssMetaData() {
                    return StyleableProperties.INSET_START;
                }
            };
        }
        return insetStart;
    }

    /// Returns the trailing inset token.
    public final double getInsetEnd() {
        return insetEnd == null ? DEFAULT_INSET_END : insetEnd.get();
    }

    /// Sets the trailing inset token.
    public final void setInsetEnd(double insetEnd) {
        insetEndProperty().set(M3Css.nonNegative(insetEnd, "insetEnd"));
    }

    /// Returns the trailing inset token property.
    public final StyleableDoubleProperty insetEndProperty() {
        if (insetEnd == null) {
            insetEnd = new StyleableDoubleProperty(DEFAULT_INSET_END) {
                /// Validates updated inset tokens.
                @Override
                protected void invalidated() {
                    set(M3Css.nonNegative(get(), "insetEnd"));
                }

                /// Returns the owning bean.
                @Override
                public Object getBean() {
                    return M3Divider.this;
                }

                /// Returns the property name.
                @Override
                public String getName() {
                    return "insetEnd";
                }

                /// Returns the CSS metadata for this property.
                @Override
                public CssMetaData<M3Divider, Number> getCssMetaData() {
                    return StyleableProperties.INSET_END;
                }
            };
        }
        return insetEnd;
    }

    /// Creates the default divider skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3DividerSkin(this);
    }

    /// Returns the CSS metadata for this control class.
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /// Returns the CSS metadata for this control.
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /// Returns the user-agent stylesheet for m3fx dividers.
    @Override
    public String getUserAgentStylesheet() {
        return M3Stylesheets.controlStylesheet("divider.css");
    }

    /// CSS metadata for m3fx divider component tokens.
    @NotNullByDefault
    private static final class StyleableProperties {
        /// CSS metadata for the thickness token.
        private static final CssMetaData<M3Divider, Number> THICKNESS =
                new CssMetaData<>("-m3-thickness", SizeConverter.getInstance(), DEFAULT_THICKNESS) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Divider control) {
                        return M3Css.isSettable(control.thicknessProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Divider control) {
                        return control.thicknessProperty();
                    }
                };

        /// CSS metadata for the leading inset token.
        private static final CssMetaData<M3Divider, Number> INSET_START =
                new CssMetaData<>("-m3-inset-start", SizeConverter.getInstance(), DEFAULT_INSET_START) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Divider control) {
                        return M3Css.isSettable(control.insetStartProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Divider control) {
                        return control.insetStartProperty();
                    }
                };

        /// CSS metadata for the trailing inset token.
        private static final CssMetaData<M3Divider, Number> INSET_END =
                new CssMetaData<>("-m3-inset-end", SizeConverter.getInstance(), DEFAULT_INSET_END) {
                    /// Returns whether this property can be set by CSS.
                    @Override
                    public boolean isSettable(M3Divider control) {
                        return M3Css.isSettable(control.insetEndProperty());
                    }

                    /// Returns the styleable property for a control.
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(M3Divider control) {
                        return control.insetEndProperty();
                    }
                };

        /// The complete immutable CSS metadata list.
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(THICKNESS);
            styleables.add(INSET_START);
            styleables.add(INSET_END);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }
}
