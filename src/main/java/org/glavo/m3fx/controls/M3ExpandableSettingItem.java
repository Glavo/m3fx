// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.input.MouseEvent;
import org.glavo.m3fx.internal.M3DisclosureIcon;
import org.glavo.m3fx.skins.M3ExpandableSettingItemSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/// An expandable M3FX settings row styled with Material tokens.
///
/// Material Design 3 does not define an expandable settings-row component. This extension belongs to the same
/// list-based setting-row family as [M3SwitchSettingItem] and
/// [M3SelectSettingItem]. It uses the same list-item metrics, text slots, focus model, and whole-row activation.
/// The trailing disclosure indicator is a non-interactive presentation of [#expandedProperty()]. Nested content is
/// shown below the header row while expanded; it is not a second preference value and must not be used as a stale
/// summary of child setting rows.
///
/// Activating the header toggles expansion and delivers an action event. Pointer input that begins inside the expanded
/// content does not toggle the header. Applications own child preference state and may place setting rows or other
/// form content in [#contentProperty()].
///
/// See [Material Design lists](https://m3.material.io/components/lists/overview).
@NotNullByDefault
public final class M3ExpandableSettingItem extends M3SettingItemBase {
    /// The default style class.
    private static final String DEFAULT_STYLE_CLASS = "m3-expandable-setting-item";

    /// The expanded pseudo-class applied while nested content is shown.
    private static final PseudoClass EXPANDED_PSEUDO_CLASS = PseudoClass.getPseudoClass("expanded");

    /// The trailing disclosure indicator owned by this row.
    private final M3DisclosureIcon disclosureIcon = new M3DisclosureIcon();

    /// Whether the next header action should be ignored because the pointer started in expanded content.
    private boolean ignoreNextHeaderAction;

    /// Creates a collapsed expandable setting row with an empty headline.
    public M3ExpandableSettingItem() {
        this("");
    }

    /// Creates a collapsed expandable setting row with the specified headline text.
    ///
    /// @param headlineText the primary row text
    /// @throws NullPointerException if `headlineText` is `null`
    public M3ExpandableSettingItem(String headlineText) {
        super(headlineText, AccessibleRole.BUTTON);
        addSettingStyleClass(DEFAULT_STYLE_CLASS);
        disclosureIcon.setVertical(true);
        installTrailingIndicator(disclosureIcon);
        disclosureIcon.expandedProperty().bind(expandedProperty());
        addEventFilter(MouseEvent.MOUSE_PRESSED, this::trackContentPointerPress);
        addEventFilter(MouseEvent.MOUSE_RELEASED, this::clearContentPointerPressOnRelease);
    }

    /// Whether nested content is currently shown below the header row.
    ///
    /// @defaultValue `false`
    private final BooleanProperty expanded = new BooleanPropertyBase(false) {
        /// Updates presentation after the expanded state changes.
        @Override
        protected void invalidated() {
            boolean active = get();
            pseudoClassStateChanged(EXPANDED_PSEUDO_CLASS, active);
            notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
            requestLayout();
        }

        /// Returns the owning setting row.
        @Override
        public Object getBean() {
            return M3ExpandableSettingItem.this;
        }

        /// Returns the JavaFX property name.
        @Override
        public String getName() {
            return "expanded";
        }
    };

    /// Returns whether nested content is shown.
    ///
    /// @return `true` when the content region is shown
    public boolean isExpanded() {
        return expanded.get();
    }

    /// Sets whether nested content is shown.
    ///
    /// Direct assignment does not fire an action event.
    ///
    /// @param expanded whether nested content should be shown
    public void setExpanded(boolean expanded) {
        this.expanded.set(expanded);
    }

    /// Returns the bindable expanded-state property.
    ///
    /// @return the expanded property
    public BooleanProperty expandedProperty() {
        return expanded;
    }

    /// Nested content shown below the header while expanded.
    ///
    /// The node is owned by this control while assigned and must not belong to another parent. Both `null` and an
    /// empty content region leave the expanded body visually empty.
    ///
    /// @defaultValue `null`
    private final ObjectProperty<@Nullable Node> content =
            new SimpleObjectProperty<>(this, "content") {
                /// Requests layout when nested content is replaced.
                @Override
                protected void invalidated() {
                    requestLayout();
                }
            };

    /// Returns the nested content node.
    ///
    /// @return the nested content, or `null` when absent
    public @Nullable Node getContent() {
        return content.get();
    }

    /// Sets the nested content node.
    ///
    /// @param content the nested content, or `null` to clear it
    public void setContent(@Nullable Node content) {
        this.content.set(content);
    }

    /// Returns the bindable nested-content property.
    ///
    /// @return the content property
    public ObjectProperty<@Nullable Node> contentProperty() {
        return content;
    }

    /// Toggles expansion before dispatching the header action.
    ///
    /// @return `false` when the activation began in expanded content; otherwise `true`
    @Override
    boolean prepareAction() {
        if (ignoreNextHeaderAction) {
            ignoreNextHeaderAction = false;
            return false;
        }
        setExpanded(!isExpanded());
        return true;
    }

    /// Returns accessibility attributes for expansion state.
    ///
    /// @param attribute  the requested accessibility attribute
    /// @param parameters optional attribute-specific parameters
    /// @return the requested accessibility value, or `null` when unsupported
    /// @throws NullPointerException if `attribute` is `null`
    @Override
    public @Nullable Object queryAccessibleAttribute(AccessibleAttribute attribute, Object... parameters) {
        Objects.requireNonNull(attribute, "attribute");
        return switch (attribute) {
            case EXPANDED -> isExpanded();
            default -> super.queryAccessibleAttribute(attribute, parameters);
        };
    }

    /// Creates the expandable setting-row skin.
    @Override
    protected Skin<?> createDefaultSkin() {
        return new M3ExpandableSettingItemSkin(this);
    }

    /// Marks header actions to ignore when the pointer press begins inside expanded content.
    private void trackContentPointerPress(MouseEvent event) {
        ignoreNextHeaderAction = isExpanded() && isInContentArea(event.getY());
    }

    /// Clears a stale content-press flag if the gesture ends without header activation.
    private void clearContentPointerPressOnRelease(MouseEvent event) {
        if (event.isConsumed()) {
            ignoreNextHeaderAction = false;
        }
    }

    /// Returns whether a local y coordinate falls in the expanded content region under the current layout.
    private boolean isInContentArea(double localY) {
        if (!isExpanded() || getContent() == null) {
            return false;
        }
        double headerHeight = switch (getLineCount()) {
            case ONE_LINE -> getOneLineHeight();
            case TWO_LINE -> getTwoLineHeight();
            case THREE_LINE -> getThreeLineHeight();
        };
        return localY >= headerHeight;
    }
}
