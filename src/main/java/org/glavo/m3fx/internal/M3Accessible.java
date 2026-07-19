// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal;

import javafx.collections.ObservableList;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.glavo.m3fx.controls.M3ListItemBase;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/// Provides accessibility query, focus, reveal, and indexed-child helpers for M3FX controls.
///
/// JavaFX passes accessibility action arguments as an untyped parameter array. M3FX containers accept a numeric
/// first parameter as an indexed-child request and accept [Node] instances, nested [Iterable] values, or nested
/// object arrays as explicit action targets. These helpers apply that grammar consistently while avoiding cycles in
/// physical and accessibility child graphs.
///
/// Unless a method explicitly supports detached structural lookup, a reachable node is visible, enabled, attached
/// to a scene, and has only visible and enabled ancestors. Methods that request focus or execute accessibility
/// actions must be called on the JavaFX Application Thread.
@NotNullByDefault
public final class M3Accessible {
    // JavaFX 14 does not define the aggregate TOGGLE_STATE accessibility attribute.
    /// The optional aggregate toggle-state attribute provided by newer JavaFX releases.
    private static final @Nullable AccessibleAttribute TOGGLE_STATE_ATTRIBUTE = attribute("TOGGLE_STATE");

    // JavaFX 14 cannot link the later ToggleState type, so its values remain runtime objects.
    /// The optional checked value returned for the toggle-state attribute.
    private static final @Nullable Object CHECKED_TOGGLE_STATE =
            toggleStateConstant("CHECKED");

    /// The optional unchecked value returned for the toggle-state attribute.
    private static final @Nullable Object UNCHECKED_TOGGLE_STATE =
            toggleStateConstant("UNCHECKED");

    /// The optional indeterminate value returned for the toggle-state attribute.
    private static final @Nullable Object INDETERMINATE_TOGGLE_STATE =
            toggleStateConstant("INDETERMINATE");

    /// The dialog role used by the running JavaFX release.
    private static final AccessibleRole DIALOG_ROLE;

    static {
        // JavaFX 14 does not define AccessibleRole.DIALOG.
        AccessibleRole dialogRole;
        try {
            dialogRole = AccessibleRole.valueOf("DIALOG");
        } catch (IllegalArgumentException e) {
            dialogRole = AccessibleRole.PARENT;
        }
        DIALOG_ROLE = dialogRole;
    }

    /// The node property key used to provide an accessibility index before a skin attaches nodes.
    private static final IdentityKey ACCESSIBLE_INDEX_ITEMS_KEY =
            new IdentityKey(M3Accessible.class.getName() + ".accessibleIndexItems");

    /// The node property key used to provide direct accessibility focus and reveal routes.
    private static final IdentityKey ACCESSIBLE_ACTION_ROUTE_KEY =
            new IdentityKey(M3Accessible.class.getName() + ".accessibleActionRoute");

    /// Prevents utility class instantiation.
    private M3Accessible() {
    }

    /// Handles a parameterized accessibility reveal request.
    @NotNullByDefault
    @FunctionalInterface
    public interface AccessibleActionHandler {
        /// Handles the supplied accessibility action parameters.
        ///
        /// @param parameters the action parameters
        /// @return `true` when the handler accepted the request
        boolean handle(Object... parameters);
    }

    /// Stores direct accessibility action handlers for a node.
    ///
    /// @param focusHandler      the optional focus handler
    /// @param showHandler       the optional reveal handler
    /// @param showTargetMatcher the optional reveal target matcher
    @NotNullByDefault
    private record AccessibleActionRoute(
            @Nullable BooleanSupplier focusHandler,
            @Nullable AccessibleActionHandler showHandler,
            @Nullable Predicate<@Nullable Object> showTargetMatcher
    ) {
    }

    /// Installs direct accessibility action handlers on a node.
    ///
    /// Supplying `null` for both handlers removes any existing route.
    ///
    /// @param node         the node that owns the route
    /// @param focusHandler the optional handler for focus requests
    /// @param showHandler  the optional handler for explicit reveal requests
    /// @throws NullPointerException if `node` is `null`
    public static void installAccessibleActionRoute(
            Node node,
            @Nullable BooleanSupplier focusHandler,
            @Nullable AccessibleActionHandler showHandler
    ) {
        installAccessibleActionRoute(node, focusHandler, showHandler, null);
    }

    /// Installs direct accessibility action handlers and a non-node reveal target matcher on a node.
    ///
    /// Supplying `null` for all optional arguments removes any existing route. Otherwise the supplied route replaces
    /// the previous route atomically.
    ///
    /// @param node              the node that owns the route
    /// @param focusHandler      the optional handler for focus requests
    /// @param showHandler       the optional handler for explicit reveal requests
    /// @param showTargetMatcher the optional predicate that identifies targets owned by this route
    /// @throws NullPointerException if `node` is `null`
    public static void installAccessibleActionRoute(
            Node node,
            @Nullable BooleanSupplier focusHandler,
            @Nullable AccessibleActionHandler showHandler,
            @Nullable Predicate<@Nullable Object> showTargetMatcher
    ) {
        Objects.requireNonNull(node, "node");
        if (focusHandler == null && showHandler == null && showTargetMatcher == null) {
            if (node.hasProperties()) {
                node.getProperties().remove(ACCESSIBLE_ACTION_ROUTE_KEY);
            }
        } else {
            node.getProperties().put(ACCESSIBLE_ACTION_ROUTE_KEY,
                    new AccessibleActionRoute(focusHandler, showHandler, showTargetMatcher));
        }
    }

    /// Returns the direct accessibility action route installed on a node.
    private static @Nullable AccessibleActionRoute accessibleActionRoute(@Nullable Node item) {
        if (item == null || !item.hasProperties()) {
            return null;
        }
        Object value = item.getProperties().get(ACCESSIBLE_ACTION_ROUTE_KEY);
        return value instanceof AccessibleActionRoute route ? route : null;
    }

    /// Returns an accessibility attribute by name when the running JavaFX version provides it.
    ///
    /// @param name the exact [AccessibleAttribute] enum constant name
    /// @return the attribute, or `null` when that name is unavailable
    /// @throws NullPointerException if `name` is `null`
    public static @Nullable AccessibleAttribute attribute(String name) {
        Objects.requireNonNull(name, "name");
        try {
            return AccessibleAttribute.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /// Returns whether an attribute is the optional toggle-state attribute of the running JavaFX release.
    ///
    /// @param attribute the attribute to test
    /// @return `true` when the attribute represents toggle state
    /// @throws NullPointerException if `attribute` is `null`
    public static boolean isToggleStateAttribute(AccessibleAttribute attribute) {
        return Objects.requireNonNull(attribute, "attribute") == TOGGLE_STATE_ATTRIBUTE;
    }

    /// Returns the optional accessibility value for a binary toggle state.
    ///
    /// @param selected whether the control is selected
    /// @return the runtime toggle-state value, or `null` when unsupported
    public static @Nullable Object toggleState(boolean selected) {
        return selected ? CHECKED_TOGGLE_STATE : UNCHECKED_TOGGLE_STATE;
    }

    /// Returns the optional accessibility value for a three-state toggle.
    ///
    /// @param selected      whether the control is selected
    /// @param indeterminate whether the control is indeterminate
    /// @return the runtime toggle-state value, or `null` when unsupported
    public static @Nullable Object toggleState(boolean selected, boolean indeterminate) {
        return indeterminate ? INDETERMINATE_TOGGLE_STATE : toggleState(selected);
    }

    /// Notifies accessibility clients of a toggle-state change when the running JavaFX release supports it.
    ///
    /// @param node the node whose toggle state changed
    /// @throws NullPointerException if `node` is `null`
    public static void notifyToggleStateChanged(Node node) {
        notifyAttribute(node, TOGGLE_STATE_ATTRIBUTE);
    }

    /// Returns the dialog accessibility role supported by the running JavaFX release.
    ///
    /// JavaFX releases without a dedicated dialog role use [AccessibleRole#PARENT].
    ///
    /// @return the dialog-compatible accessibility role
    public static AccessibleRole dialogRole() {
        return DIALOG_ROLE;
    }

    /// Notifies an optional accessibility attribute when the running JavaFX version provides it.
    ///
    /// A `null` attribute is ignored.
    ///
    /// @param node      the node whose accessibility attribute changed
    /// @param attribute the changed attribute, or `null`
    /// @throws NullPointerException if `node` is `null`
    public static void notifyAttribute(Node node, @Nullable AccessibleAttribute attribute) {
        Objects.requireNonNull(node, "node");
        if (attribute != null) {
            node.notifyAccessibleAttributeChanged(attribute);
        }
    }

    /// Resolves one value of the optional aggregate toggle-state attribute.
    private static @Nullable Object toggleStateConstant(String constantName) {
        Objects.requireNonNull(constantName, "constantName");
        // This is the normal compatibility path on JavaFX 14.
        if (TOGGLE_STATE_ATTRIBUTE == null) {
            return null;
        }
        Object @Nullable [] constants = TOGGLE_STATE_ATTRIBUTE.getReturnType().getEnumConstants();
        if (constants == null) {
            return null;
        }
        for (Object constant : constants) {
            if (constant instanceof Enum<?> enumConstant && enumConstant.name().equals(constantName)) {
                return constant;
            }
        }
        return null;
    }

    /// Returns the child requested by an accessibility index parameter.
    ///
    /// @param items      the indexed children
    /// @param parameters the action parameters whose first value may be a number
    /// @return the indexed child, or `null` when no valid in-range index is supplied
    /// @throws NullPointerException if `items` or `parameters` is `null`
    public static @Nullable Node itemAt(ObservableList<? extends Node> items, Object... parameters) {
        Objects.requireNonNull(items, "items");
        int index = indexParameter(parameters);
        return index >= 0 && index < items.size() ? items.get(index) : null;
    }

    /// Returns the number of indexed accessibility items with an optional leading item.
    ///
    /// @param leading the optional child before `items`
    /// @param items   the indexed item list
    /// @return the number of non-null indexed children
    /// @throws NullPointerException if `items` is `null`
    public static int itemCount(@Nullable Node leading, ObservableList<? extends Node> items) {
        Objects.requireNonNull(items, "items");
        return (leading == null ? 0 : 1) + items.size();
    }

    /// Returns the indexed accessibility item from an optional leading item and trailing item list.
    ///
    /// @param leading    the optional child at the start of the index space
    /// @param items      the remaining indexed children
    /// @param parameters the action parameters whose first value may be a number
    /// @return the indexed child, or `null` when no valid in-range index is supplied
    /// @throws NullPointerException if `items` or `parameters` is `null`
    public static @Nullable Node itemAt(@Nullable Node leading, ObservableList<? extends Node> items, Object... parameters) {
        Objects.requireNonNull(items, "items");
        int index = indexParameter(parameters);
        if (index < 0) {
            return null;
        }
        if (leading != null) {
            if (index == 0) {
                return leading;
            }
            index--;
        }
        return index < items.size() ? items.get(index) : null;
    }

    /// Returns the number of indexed accessibility items with an optional trailing item.
    ///
    /// @param items    the indexed item list
    /// @param trailing the optional child after `items`
    /// @return the number of non-null indexed children
    /// @throws NullPointerException if `items` is `null`
    public static int itemCount(ObservableList<? extends Node> items, @Nullable Node trailing) {
        Objects.requireNonNull(items, "items");
        return items.size() + (trailing == null ? 0 : 1);
    }

    /// Returns the indexed accessibility item from a leading item list and optional trailing item.
    ///
    /// @param items      the indexed children before the trailing slot
    /// @param trailing   the optional final child
    /// @param parameters the action parameters whose first value may be a number
    /// @return the indexed child, or `null` when no valid in-range index is supplied
    /// @throws NullPointerException if `items` or `parameters` is `null`
    public static @Nullable Node itemAt(ObservableList<? extends Node> items, @Nullable Node trailing, Object... parameters) {
        Objects.requireNonNull(items, "items");
        int index = indexParameter(parameters);
        if (index < 0) {
            return null;
        }
        if (index < items.size()) {
            return items.get(index);
        }
        return index == items.size() ? trailing : null;
    }

    /// Returns the number of indexed accessibility items with two optional child slots.
    ///
    /// @param first  the optional first child
    /// @param second the optional second child
    /// @return the number of non-null children
    public static int itemCount(@Nullable Node first, @Nullable Node second) {
        return (first == null ? 0 : 1) + (second == null ? 0 : 1);
    }

    /// Returns the indexed accessibility item from two optional child slots.
    ///
    /// @param first      the optional first child
    /// @param second     the optional second child
    /// @param parameters the action parameters whose first value may be a number
    /// @return the indexed non-null child, or `null` when no valid index is supplied
    /// @throws NullPointerException if `parameters` is `null`
    public static @Nullable Node itemAt(@Nullable Node first, @Nullable Node second, Object... parameters) {
        int index = indexParameter(parameters);
        if (index < 0) {
            return null;
        }
        if (first != null) {
            if (index == 0) {
                return first;
            }
            index--;
        }
        return index == 0 ? second : null;
    }

    /// Returns the number of indexed accessibility items with three optional child slots.
    ///
    /// @param first  the optional first child
    /// @param second the optional second child
    /// @param third  the optional third child
    /// @return the number of non-null children
    public static int itemCount(@Nullable Node first, @Nullable Node second, @Nullable Node third) {
        return (first == null ? 0 : 1) + (second == null ? 0 : 1) + (third == null ? 0 : 1);
    }

    /// Returns the indexed accessibility item from three optional child slots.
    ///
    /// @param first      the optional first child
    /// @param second     the optional second child
    /// @param third      the optional third child
    /// @param parameters the action parameters whose first value may be a number
    /// @return the indexed non-null child, or `null` when no valid index is supplied
    /// @throws NullPointerException if `parameters` is `null`
    public static @Nullable Node itemAt(
            @Nullable Node first,
            @Nullable Node second,
            @Nullable Node third,
            Object... parameters
    ) {
        int index = indexParameter(parameters);
        if (index < 0) {
            return null;
        }
        if (first != null) {
            if (index == 0) {
                return first;
            }
            index--;
        }
        if (second != null) {
            if (index == 0) {
                return second;
            }
            index--;
        }
        return index == 0 ? third : null;
    }

    /// Returns the number of indexed accessibility items with two optional leading slots and a trailing list.
    ///
    /// @param first  the optional first child
    /// @param second the optional second child
    /// @param items  the remaining indexed children
    /// @return the number of non-null indexed children
    /// @throws NullPointerException if `items` is `null`
    public static int itemCount(@Nullable Node first, @Nullable Node second, ObservableList<? extends Node> items) {
        Objects.requireNonNull(items, "items");
        return (first == null ? 0 : 1) + (second == null ? 0 : 1) + items.size();
    }

    /// Returns the indexed accessibility item from two optional leading slots and a trailing list.
    ///
    /// @param first      the optional first child
    /// @param second     the optional second child
    /// @param items      the remaining indexed children
    /// @param parameters the action parameters whose first value may be a number
    /// @return the indexed non-null child, or `null` when no valid index is supplied
    /// @throws NullPointerException if `items` or `parameters` is `null`
    public static @Nullable Node itemAt(
            @Nullable Node first,
            @Nullable Node second,
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        Objects.requireNonNull(items, "items");
        int index = indexParameter(parameters);
        if (index < 0) {
            return null;
        }
        if (first != null) {
            if (index == 0) {
                return first;
            }
            index--;
        }
        if (second != null) {
            if (index == 0) {
                return second;
            }
            index--;
        }
        return index < items.size() ? items.get(index) : null;
    }

    /// Returns whether parameters contain a value accepted by the target matcher.
    ///
    /// Nested iterable values and object arrays are searched recursively.
    ///
    /// @param targetMatcher the predicate that recognizes a target value
    /// @param parameters    the parameter values to search
    /// @return `true` when the matcher accepts any direct or nested value
    /// @throws NullPointerException if `targetMatcher` or `parameters` is `null`
    public static boolean parametersContainDirectTarget(
            Predicate<@Nullable Object> targetMatcher,
            Object... parameters
    ) {
        Objects.requireNonNull(targetMatcher, "targetMatcher");
        Objects.requireNonNull(parameters, "parameters");
        for (Object parameter : parameters) {
            if (parameterContainsDirectTarget(targetMatcher, parameter)) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether one parameter contains a direct atomic value accepted by the target matcher.
    private static boolean parameterContainsDirectTarget(
            Predicate<@Nullable Object> targetMatcher,
            @Nullable Object parameter
    ) {
        if (targetMatcher.test(parameter)) {
            return true;
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                if (parameterContainsDirectTarget(targetMatcher, value)) {
                    return true;
                }
            }
            return false;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                if (parameterContainsDirectTarget(targetMatcher, value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /// Returns whether parameters directly express exactly one target accepted by the matcher and nothing else.
    ///
    /// A single chain of one-element iterable values or object arrays is accepted; empty or multi-value containers
    /// are rejected.
    ///
    /// @param targetMatcher the predicate that recognizes the target value
    /// @param parameters    the parameter values to inspect
    /// @return `true` when the parameters contain exactly one accepted target and no other value
    /// @throws NullPointerException if `targetMatcher` or `parameters` is `null`
    public static boolean parametersDirectlyReferenceSingleTarget(
            Predicate<@Nullable Object> targetMatcher,
            Object... parameters
    ) {
        Objects.requireNonNull(targetMatcher, "targetMatcher");
        Objects.requireNonNull(parameters, "parameters");
        return parameters.length == 1 && parameterDirectlyReferencesSingleTarget(targetMatcher, parameters[0]);
    }

    /// Returns whether one parameter directly expresses exactly one target accepted by the matcher and nothing else.
    private static boolean parameterDirectlyReferencesSingleTarget(
            Predicate<@Nullable Object> targetMatcher,
            @Nullable Object parameter
    ) {
        if (targetMatcher.test(parameter)) {
            return true;
        }
        if (parameter instanceof Iterable<?> values) {
            return parameterValuesDirectlyReferenceSingleTarget(targetMatcher, values);
        }
        if (parameter instanceof Object[] values) {
            return parameterValuesDirectlyReferenceSingleTarget(targetMatcher, values);
        }
        return false;
    }

    /// Returns whether a value collection contains exactly one accepted direct target and no other values.
    private static boolean parameterValuesDirectlyReferenceSingleTarget(
            Predicate<@Nullable Object> targetMatcher,
            Iterable<?> values
    ) {
        boolean matched = false;
        for (Object value : values) {
            if (!parameterDirectlyReferencesSingleTarget(targetMatcher, value)) {
                return false;
            }
            if (matched) {
                return false;
            }
            matched = true;
        }
        return matched;
    }

    /// Returns whether a value array contains exactly one accepted direct target and no other values.
    private static boolean parameterValuesDirectlyReferenceSingleTarget(
            Predicate<@Nullable Object> targetMatcher,
            Object[] values
    ) {
        boolean matched = false;
        for (Object value : values) {
            if (!parameterDirectlyReferencesSingleTarget(targetMatcher, value)) {
                return false;
            }
            if (matched) {
                return false;
            }
            matched = true;
        }
        return matched;
    }

    /// Returns whether accessibility action parameters contain the requested reachable selection target or one of its descendants.
    ///
    /// @param target     the selection root to match
    /// @param parameters the direct or nested action parameters to search
    /// @return `true` when a reachable node parameter belongs to `target`
    /// @throws NullPointerException if `target` or `parameters` is `null`
    public static boolean containsSelectionTarget(Node target, Object... parameters) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(parameters, "parameters");
        for (Object parameter : parameters) {
            if (containsSelectionTarget(target, parameter)) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether accessibility action parameters reference the target node or one of its descendants.
    ///
    /// @param target     the root node to match
    /// @param parameters the direct or nested action parameters to search
    /// @return `true` when a node parameter belongs to `target`
    /// @throws NullPointerException if `target` or `parameters` is `null`
    public static boolean containsNodeTarget(Node target, Object... parameters) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(parameters, "parameters");
        for (Object parameter : parameters) {
            if (containsNodeTarget(target, parameter)) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether an item exposes the supplied target through its accessibility item tree.
    ///
    /// Physical descendants, indexed accessibility children, installed tooltip targets, and direct action routes
    /// participate in the search.
    ///
    /// @param item       the possible accessibility owner, or `null`
    /// @param parameters the direct or nested target parameters
    /// @return `true` when the item exposes any supplied target
    /// @throws NullPointerException if `parameters` is `null`
    public static boolean containsAccessibleActionTarget(@Nullable Node item, @Nullable Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (item == null || parameters.length == 0) {
            return false;
        }

        for (@Nullable Object parameter : parameters) {
            if (parameter instanceof Node node && containsDirectAccessibleNode(item, node)) {
                return true;
            }
        }

        Set<Node> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (@Nullable Object parameter : parameters) {
            visited.clear();
            if (containsAccessibleActionTarget(item, parameter, visited, true)) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether explicit action parameters contain a node that cannot become reachable after owner reveal.
    ///
    /// @param items      the possible action-item owners
    /// @param parameters the direct or nested target parameters
    /// @return `true` when a referenced owned node is disabled or hidden below an unrevealable boundary
    /// @throws NullPointerException if `items` or `parameters` is `null`
    public static boolean containsUnrevealableActionNodeTarget(
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(parameters, "parameters");
        return containsUnrevealableActionNodeTarget(null, null, items, parameters);
    }

    /// Returns whether explicit action parameters contain a node that cannot become reachable after owner reveal.
    private static boolean containsUnrevealableActionNodeTarget(
            @Nullable Node leading,
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        return containsUnrevealableActionNodeTarget(leading, null, items, parameters);
    }

    /// Returns whether explicit action parameters contain a node that cannot become reachable after owner reveal.
    private static boolean containsUnrevealableActionNodeTarget(
            @Nullable Node first,
            @Nullable Node second,
            Object... parameters
    ) {
        Objects.requireNonNull(parameters, "parameters");
        for (Object parameter : parameters) {
            if (containsUnrevealableActionNodeTarget(first, second, null, parameter)) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether explicit action parameters contain a node that cannot become reachable after owner reveal.
    private static boolean containsUnrevealableActionNodeTarget(
            @Nullable Node first,
            @Nullable Node second,
            @Nullable Node third,
            Object... parameters
    ) {
        return containsUnrevealableActionNodeTarget(first, second, parameters)
                || containsUnrevealableActionNodeTarget(third, parameters);
    }

    /// Returns whether explicit action parameters contain a node that cannot become reachable after owner reveal.
    private static boolean containsUnrevealableActionNodeTarget(
            @Nullable Node first,
            @Nullable Node second,
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(parameters, "parameters");
        for (Object parameter : parameters) {
            if (containsUnrevealableActionNodeTarget(first, second, items, parameter)) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether explicit action parameters contain a node that cannot become reachable after owner reveal.
    ///
    /// @param item       the possible action-item owner, or `null`
    /// @param parameters the direct or nested target parameters
    /// @return `true` when a referenced owned node is disabled or hidden below an unrevealable boundary
    /// @throws NullPointerException if `parameters` is `null`
    public static boolean containsUnrevealableActionNodeTarget(@Nullable Node item, Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        for (Object parameter : parameters) {
            if (containsUnrevealableActionNodeTarget(item, null, null, parameter)) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether one explicit action parameter cannot become reachable after owner reveal.
    private static boolean containsUnrevealableActionNodeTarget(
            @Nullable Node first,
            @Nullable Node second,
            @Nullable ObservableList<? extends Node> items,
            @Nullable Object parameter
    ) {
        if (parameter instanceof Node node) {
            if (isUnrevealableActionNodeTarget(first, node) || isUnrevealableActionNodeTarget(second, node)) {
                return true;
            }
            if (items != null) {
                for (Node item : items) {
                    if (isUnrevealableActionNodeTarget(item, node)) {
                        return true;
                    }
                }
            }
            return false;
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                if (containsUnrevealableActionNodeTarget(first, second, items, value)) {
                    return true;
                }
            }
            return false;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                if (containsUnrevealableActionNodeTarget(first, second, items, value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /// Returns whether a requested action-owned node is disabled, hidden, or hidden below the action item boundary.
    private static boolean isUnrevealableActionNodeTarget(@Nullable Node item, Node target) {
        if (item == null) {
            return false;
        }
        if (item == target) {
            return !item.isVisible() || item.isDisabled();
        }
        if (!containsNode(item, target)) {
            return false;
        }
        return cannotRevealActionNode(item, target);
    }

    /// Returns whether a node under an action item cannot become reachable when the owner reveals the item.
    private static boolean cannotRevealActionNode(Node item, Node target) {
        if (item.isDisabled() || !target.isVisible() || target.isDisabled()) {
            return true;
        }

        @Nullable Parent parent = target.getParent();
        while (parent != null && parent != item) {
            if (parent.isDisabled()) {
                return true;
            }
            parent = parent.getParent();
        }
        return parent != item && !containsNode(item, target);
    }

    /// Returns whether a leading item or indexed items can handle explicit accessibility item parameters.
    ///
    /// An empty parameter array is always accepted. A numeric first parameter selects the corresponding child;
    /// remaining parameters, when present, must identify a revealable target exposed by that child.
    ///
    /// @param leading    the optional leading action item
    /// @param items      the remaining action items
    /// @param parameters the accessibility action parameters to validate
    /// @return `true` when the request can be handled by this child set
    /// @throws NullPointerException if `items` or `parameters` is `null`
    public static boolean canShowItem(@Nullable Node leading, ObservableList<? extends Node> items, Object... parameters) {
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            return true;
        }
        if (parameters[0] instanceof Number) {
            @Nullable Node indexedItem = itemAt(leading, items, parameters);
            if (indexedItem == null || indexedItem.isDisabled()) {
                return false;
            }
            if (parameters.length == 1) {
                return true;
            }
            Object[] nestedParameters = new Object[parameters.length - 1];
            System.arraycopy(parameters, 1, nestedParameters, 0, nestedParameters.length);
            return !containsUnrevealableActionNodeTarget(indexedItem, nestedParameters)
                    && containsAccessibleActionTarget(indexedItem, nestedParameters);
        }
        if (containsUnrevealableActionNodeTarget(leading, items, parameters)) {
            return false;
        }
        if (actionItem(leading, items, parameters) != null) {
            return true;
        }
        if (containsAccessibleActionTarget(leading, parameters)) {
            return true;
        }
        for (Node item : items) {
            if (containsAccessibleActionTarget(item, parameters)) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether two leading items or indexed trailing items can handle explicit accessibility item parameters.
    ///
    /// @param first      the optional first leading action item
    /// @param second     the optional second leading action item
    /// @param items      the remaining action items
    /// @param parameters the accessibility action parameters to validate
    /// @return `true` when the request can be handled by this child set
    /// @throws NullPointerException if `items` or `parameters` is `null`
    public static boolean canShowItem(
            @Nullable Node first,
            @Nullable Node second,
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            return true;
        }
        if (parameters[0] instanceof Number) {
            @Nullable Node indexedItem = itemAt(first, second, items, parameters);
            if (indexedItem == null || indexedItem.isDisabled()) {
                return false;
            }
            if (parameters.length == 1) {
                return true;
            }
            Object[] nestedParameters = new Object[parameters.length - 1];
            System.arraycopy(parameters, 1, nestedParameters, 0, nestedParameters.length);
            return !containsUnrevealableActionNodeTarget(indexedItem, nestedParameters)
                    && containsAccessibleActionTarget(indexedItem, nestedParameters);
        }
        if (containsUnrevealableActionNodeTarget(first, second, items, parameters)) {
            return false;
        }
        if (actionItem(first, second, items, parameters) != null) {
            return true;
        }
        if (containsAccessibleActionTarget(first, parameters)
                || containsAccessibleActionTarget(second, parameters)) {
            return true;
        }
        for (Node item : items) {
            if (containsAccessibleActionTarget(item, parameters)) {
                return true;
            }
        }
        return false;
    }

    /// Requests focus for the item referenced by accessibility action parameters.
    ///
    /// With no parameters, the first item exposing an accessible focus target is used.
    ///
    /// @param items      the indexed action items
    /// @param parameters the index or explicit target parameters
    /// @return `true` when focus or a delegated reveal request succeeded
    /// @throws NullPointerException if `items` or `parameters` is `null`
    public static boolean showItem(ObservableList<? extends Node> items, Object... parameters) {
        return showItemOrAccessibleActionTarget(actionItem(items, parameters), items, parameters);
    }

    /// Requests focus for the item referenced by accessibility action parameters and reveals it through the owner.
    ///
    /// @param owner      the container through which the focused target should be revealed
    /// @param items      the indexed action items
    /// @param parameters the index or explicit target parameters
    /// @return `true` when focus or a delegated reveal request succeeded
    /// @throws NullPointerException if `owner`, `items`, or `parameters` is `null`
    public static boolean showIndexedItem(Node owner, ObservableList<? extends Node> items, Object... parameters) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(items, "items");
        return showIndexedItemOrAccessibleActionTarget(owner, actionItem(items, parameters), items, parameters);
    }

    /// Requests focus for the leading item or one of the indexed trailing items.
    ///
    /// @param leading    the optional leading action item
    /// @param items      the remaining indexed action items
    /// @param parameters the index or explicit target parameters
    /// @return `true` when focus or a delegated reveal request succeeded
    /// @throws NullPointerException if `items` or `parameters` is `null`
    public static boolean showItem(@Nullable Node leading, ObservableList<? extends Node> items, Object... parameters) {
        Objects.requireNonNull(items, "items");
        return showItemOrAccessibleActionTarget(actionItem(leading, items, parameters), leading, items, parameters);
    }

    /// Requests focus for one of the indexed items or the trailing item.
    ///
    /// @param items      the indexed action items before the trailing slot
    /// @param trailing   the optional trailing action item
    /// @param parameters the index or explicit target parameters
    /// @return `true` when focus or a delegated reveal request succeeded
    /// @throws NullPointerException if `items` or `parameters` is `null`
    public static boolean showItem(ObservableList<? extends Node> items, @Nullable Node trailing, Object... parameters) {
        Objects.requireNonNull(items, "items");
        return showItemOrAccessibleActionTarget(actionItem(items, trailing, parameters), items, trailing, parameters);
    }

    /// Requests focus for one of two optional indexed items.
    ///
    /// @param first      the optional first action item
    /// @param second     the optional second action item
    /// @param parameters the index or explicit target parameters
    /// @return `true` when focus or a delegated reveal request succeeded
    /// @throws NullPointerException if `parameters` is `null`
    public static boolean showItem(@Nullable Node first, @Nullable Node second, Object... parameters) {
        return showItemOrAccessibleActionTarget(actionItem(first, second, parameters), first, second, parameters);
    }

    /// Requests focus for one of three optional indexed items.
    ///
    /// @param first      the optional first action item
    /// @param second     the optional second action item
    /// @param third      the optional third action item
    /// @param parameters the index or explicit target parameters
    /// @return `true` when focus or a delegated reveal request succeeded
    /// @throws NullPointerException if `parameters` is `null`
    public static boolean showItem(
            @Nullable Node first,
            @Nullable Node second,
            @Nullable Node third,
            Object... parameters
    ) {
        return showItemOrAccessibleActionTarget(
                actionItem(first, second, third, parameters),
                first,
                second,
                third,
                parameters
        );
    }

    /// Requests focus for one of two optional leading items or an indexed trailing item.
    ///
    /// @param first      the optional first leading action item
    /// @param second     the optional second leading action item
    /// @param items      the remaining indexed action items
    /// @param parameters the index or explicit target parameters
    /// @return `true` when focus or a delegated reveal request succeeded
    /// @throws NullPointerException if `items` or `parameters` is `null`
    public static boolean showItem(
            @Nullable Node first,
            @Nullable Node second,
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        Objects.requireNonNull(items, "items");
        return showItemOrAccessibleActionTarget(
                actionItem(first, second, items, parameters),
                first,
                second,
                items,
                parameters
        );
    }

    /// Requests focus for the default item or indexed item and reveals it through the owner.
    ///
    /// `defaultItem` is preferred for an empty request when it exposes a focus target; otherwise the first focusable
    /// list item is used.
    ///
    /// @param owner       the container through which the focused target should be revealed
    /// @param defaultItem the preferred item for an empty request, or `null`
    /// @param items       the indexed action items
    /// @param parameters  the index or explicit target parameters
    /// @return `true` when focus or a delegated reveal request succeeded
    /// @throws NullPointerException if `owner`, `items`, or `parameters` is `null`
    public static boolean showItemOrDefault(
            Node owner,
            @Nullable Node defaultItem,
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(parameters, "parameters");
        @Nullable Node item = parameters.length == 0
                ? (focusTarget(defaultItem) == null ? firstFocusableItem(items) : defaultItem)
                : actionItem(items, parameters);
        return showIndexedItemOrAccessibleActionTarget(owner, item, items, parameters);
    }

    /// Requests focus for the current focus target in an indexed container, or for the requested item.
    ///
    /// @param owner      the container that supplies current focus and reveal context
    /// @param items      the indexed action items
    /// @param parameters the index or explicit target parameters; empty to restore current or first focus
    /// @return `true` when focus or a delegated reveal request succeeded
    /// @throws NullPointerException if any argument is `null`
    public static boolean showCurrentOrItem(Node owner, ObservableList<? extends Node> items, Object... parameters) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            return showItem(owner, currentOrFirstFocusTarget(owner, items));
        }
        return showIndexedItem(owner, items, parameters);
    }

    /// Requests focus for the current focus target in a leading/list container, or for the requested item.
    ///
    /// @param owner      the container that supplies current focus and reveal context
    /// @param leading    the optional leading action item
    /// @param items      the remaining action items
    /// @param parameters the index or explicit target parameters; empty to restore current or first focus
    /// @return `true` when focus or a delegated reveal request succeeded
    /// @throws NullPointerException if `owner`, `items`, or `parameters` is `null`
    public static boolean showCurrentOrItem(
            Node owner,
            @Nullable Node leading,
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            return showItem(owner, currentOrFirstFocusTarget(owner, leading, items));
        }
        boolean shown = showItem(leading, items, parameters);
        if (shown) {
            revealCurrentFocusOwner(owner);
        }
        return shown;
    }

    /// Requests focus for the current focus target in a list/trailing container, or for the requested item.
    ///
    /// @param owner      the container that supplies current focus and reveal context
    /// @param items      the action items before the trailing slot
    /// @param trailing   the optional trailing action item
    /// @param parameters the index or explicit target parameters; empty to restore current or first focus
    /// @return `true` when focus or a delegated reveal request succeeded
    /// @throws NullPointerException if `owner`, `items`, or `parameters` is `null`
    public static boolean showCurrentOrItem(
            Node owner,
            ObservableList<? extends Node> items,
            @Nullable Node trailing,
            Object... parameters
    ) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            return showItem(owner, currentOrFirstFocusTarget(owner, items, trailing));
        }
        boolean shown = showItem(items, trailing, parameters);
        if (shown) {
            revealCurrentFocusOwner(owner);
        }
        return shown;
    }

    /// Requests focus for the current focus target among two optional children, or for the requested item.
    ///
    /// @param owner      the container that supplies current focus and reveal context
    /// @param first      the optional first action item
    /// @param second     the optional second action item
    /// @param parameters the index or explicit target parameters; empty to restore current or first focus
    /// @return `true` when focus or a delegated reveal request succeeded
    /// @throws NullPointerException if `owner` or `parameters` is `null`
    public static boolean showCurrentOrItem(Node owner, @Nullable Node first, @Nullable Node second, Object... parameters) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            return showItem(owner, currentOrFirstFocusTarget(owner, first, second));
        }
        boolean shown = showItem(first, second, parameters);
        if (shown) {
            revealCurrentFocusOwner(owner);
        }
        return shown;
    }

    /// Requests focus for the current focus target among three optional children, or for the requested item.
    ///
    /// @param owner      the container that supplies current focus and reveal context
    /// @param first      the optional first action item
    /// @param second     the optional second action item
    /// @param third      the optional third action item
    /// @param parameters the index or explicit target parameters; empty to restore current or first focus
    /// @return `true` when focus or a delegated reveal request succeeded
    /// @throws NullPointerException if `owner` or `parameters` is `null`
    public static boolean showCurrentOrItem(
            Node owner,
            @Nullable Node first,
            @Nullable Node second,
            @Nullable Node third,
            Object... parameters
    ) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            return showItem(owner, currentOrFirstFocusTarget(owner, first, second, third));
        }
        boolean shown = showItem(first, second, third, parameters);
        if (shown) {
            revealCurrentFocusOwner(owner);
        }
        return shown;
    }

    /// Requests focus for the current focus target in two leading slots or a trailing list.
    ///
    /// @param owner      the container that supplies current focus and reveal context
    /// @param first      the optional first leading action item
    /// @param second     the optional second leading action item
    /// @param items      the remaining action items
    /// @param parameters the index or explicit target parameters; empty to restore current or first focus
    /// @return `true` when focus or a delegated reveal request succeeded
    /// @throws NullPointerException if `owner`, `items`, or `parameters` is `null`
    public static boolean showCurrentOrItem(
            Node owner,
            @Nullable Node first,
            @Nullable Node second,
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            return showItem(owner, currentOrFirstFocusTarget(owner, first, second, items));
        }
        boolean shown = showItem(first, second, items, parameters);
        if (shown) {
            revealCurrentFocusOwner(owner);
        }
        return shown;
    }

    /// Requests focus for an accessibility item when it can be reached.
    ///
    /// @param item the item to focus, or `null`
    /// @return `true` when a reachable focus target accepted focus
    public static boolean showItem(@Nullable Node item) {
        return showItemIfPresent(item);
    }

    /// Requests focus for an accessibility item and reveals it through the owner when it can be reached.
    ///
    /// @param owner the container through which the focused target should be revealed
    /// @param item  the item to focus, or `null`
    /// @return `true` when a reachable focus target accepted focus
    /// @throws NullPointerException if `owner` is `null`
    public static boolean showItem(Node owner, @Nullable Node item) {
        Objects.requireNonNull(owner, "owner");
        return showItemIfPresent(owner, item);
    }

    /// Requests focus for the exact item node and reveals it through the owner when the item can be reached.
    ///
    /// Unlike [#showItem(Node, Node)], this method does not substitute a descendant accessibility focus target and
    /// therefore requires `item` itself to be focus traversable.
    ///
    /// @param owner the container through which the item should be revealed
    /// @param item  the exact node to focus, or `null`
    /// @return `true` when the item accepted focus
    /// @throws NullPointerException if `owner` is `null`
    public static boolean showDirectItem(Node owner, @Nullable Node item) {
        Objects.requireNonNull(owner, "owner");
        if (!canReach(item) || !item.isFocusTraversable()) {
            return false;
        }

        if (containsNode(owner, item)) {
            return M3ScrollReveal.requestFocusAndReveal(owner, item);
        }
        return M3FocusRequests.requestFocus(item);
    }

    /// Requests focus through a node's accessibility focus action.
    ///
    /// A directly installed focus route is preferred; otherwise [AccessibleAction#REQUEST_FOCUS] is executed and
    /// the resulting contained or external focus target is verified.
    ///
    /// @param item the item whose accessible focus action should run, or `null`
    /// @return `true` when the action established a reachable focus target
    public static boolean requestAccessibleFocus(@Nullable Node item) {
        if (!canReach(item)) {
            return false;
        }
        @Nullable AccessibleActionRoute route = accessibleActionRoute(item);
        if (route != null && route.focusHandler != null) {
            return route.focusHandler.getAsBoolean();
        }
        item.executeAccessibleAction(AccessibleAction.REQUEST_FOCUS);
        return currentContainedFocusTarget(item) != null || activeExternalFocusTarget(item, item) != null;
    }

    /// Requests focus through a node's accessibility focus action and reveals it through the owner.
    ///
    /// @param owner the container through which the resulting focus target should be revealed
    /// @param item  the item whose accessible focus action should run, or `null`
    /// @return `true` when the action established a reachable focus target
    /// @throws NullPointerException if `owner` is `null`
    public static boolean requestAccessibleFocus(Node owner, @Nullable Node item) {
        Objects.requireNonNull(owner, "owner");
        if (requestAccessibleFocus(item)) {
            revealCurrentFocusOwner(owner);
            return true;
        }
        return false;
    }

    /// Requests focus for an accessibility item when it can be reached.
    private static boolean showItemIfPresent(@Nullable Node item) {
        return focusItemIfPresent(item) != null;
    }

    /// Requests focus for an accessibility item and reveals it through the owner when it can be reached.
    private static boolean showItemIfPresent(Node owner, @Nullable Node item) {
        Objects.requireNonNull(owner, "owner");
        @Nullable Node focusTarget = focusItemIfPresent(item);
        if (focusTarget == null) {
            return false;
        }
        if (containsNode(owner, focusTarget)) {
            M3ScrollReveal.revealTarget(owner, focusTarget, M3Accessible::containsNode);
        }
        return true;
    }

    /// Reveals the current focus owner when it belongs to the supplied owner subtree.
    private static boolean revealCurrentFocusOwner(Node owner) {
        Objects.requireNonNull(owner, "owner");
        @Nullable Node focusTarget = focusOwner(owner);
        if (focusTarget != null && containsNode(owner, focusTarget)) {
            M3ScrollReveal.revealTarget(owner, focusTarget, M3Accessible::containsNode);
            return true;
        }
        return false;
    }

    /// Requests focus for an accessibility item and returns the actual focus target when it can be reached.
    private static @Nullable Node focusItemIfPresent(@Nullable Node item) {
        @Nullable Node focusTarget = currentContainedFocusTarget(item);
        if (focusTarget == null) {
            focusTarget = accessibleFocusTarget(item);
        }
        if (focusTarget != null && M3FocusRequests.requestFocus(focusTarget)) {
            return focusTarget;
        }
        return null;
    }

    /// Focuses a direct action target or delegates explicit reveal to nested accessible popup owners.
    private static boolean showItemOrAccessibleActionTarget(
            @Nullable Node item,
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        if (parameters.length > 0 && containsUnrevealableActionNodeTarget(items, parameters)) {
            return false;
        }
        if (delegateContainingItemReveal(item, items, parameters)) {
            return true;
        }
        if (delegateSelectedItemReveal(item, parameters)) {
            return true;
        }
        if (showItemIfPresent(item)) {
            return true;
        }
        return parameters.length > 0 && showAccessibleActionTarget(items, parameters);
    }

    /// Focuses a direct action target, reveals it through the owner, or delegates explicit reveal to children.
    private static boolean showIndexedItemOrAccessibleActionTarget(
            Node owner,
            @Nullable Node item,
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        Objects.requireNonNull(owner, "owner");
        if (parameters.length > 0 && containsUnrevealableActionNodeTarget(items, parameters)) {
            return false;
        }
        if (delegateContainingItemReveal(item, items, parameters)) {
            return revealCurrentFocusOwner(owner);
        }
        if (delegateSelectedItemReveal(item, parameters)) {
            return revealCurrentFocusOwner(owner);
        }
        if (showItemIfPresent(owner, item)) {
            return true;
        }
        if (parameters.length > 0 && showAccessibleActionTarget(items, parameters)) {
            revealCurrentFocusOwner(owner);
            return true;
        }
        return false;
    }

    /// Focuses a direct action target or delegates explicit reveal to a leading/list child.
    private static boolean showItemOrAccessibleActionTarget(
            @Nullable Node item,
            @Nullable Node leading,
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        if (parameters.length > 0 && containsUnrevealableActionNodeTarget(leading, items, parameters)) {
            return false;
        }
        if (delegateContainingNodeReveal(item, leading, parameters)
                || delegateContainingListReveal(item, items, parameters)) {
            return true;
        }
        if (delegateSelectedItemReveal(item, parameters)) {
            return true;
        }
        if (showItemIfPresent(item)) {
            return true;
        }
        return parameters.length > 0
                && (showAccessibleActionTarget(leading, parameters) || showAccessibleActionTarget(items, parameters));
    }

    /// Focuses a direct action target or delegates explicit reveal to a list/trailing child.
    private static boolean showItemOrAccessibleActionTarget(
            @Nullable Node item,
            ObservableList<? extends Node> items,
            @Nullable Node trailing,
            Object... parameters
    ) {
        if (parameters.length > 0 && containsUnrevealableActionNodeTarget(trailing, items, parameters)) {
            return false;
        }
        if (delegateContainingListReveal(item, items, parameters)
                || delegateContainingNodeReveal(item, trailing, parameters)) {
            return true;
        }
        if (delegateSelectedItemReveal(item, parameters)) {
            return true;
        }
        if (showItemIfPresent(item)) {
            return true;
        }
        return parameters.length > 0
                && (showAccessibleActionTarget(items, parameters) || showAccessibleActionTarget(trailing, parameters));
    }

    /// Focuses a direct action target or delegates explicit reveal to either optional child.
    private static boolean showItemOrAccessibleActionTarget(
            @Nullable Node item,
            @Nullable Node first,
            @Nullable Node second,
            Object... parameters
    ) {
        if (parameters.length > 0 && containsUnrevealableActionNodeTarget(first, second, parameters)) {
            return false;
        }
        if (delegateContainingNodeReveal(item, first, parameters)
                || delegateContainingNodeReveal(item, second, parameters)) {
            return true;
        }
        if (delegateSelectedItemReveal(item, parameters)) {
            return true;
        }
        if (showItemIfPresent(item)) {
            return true;
        }
        return parameters.length > 0
                && (showAccessibleActionTarget(first, parameters) || showAccessibleActionTarget(second, parameters));
    }

    /// Focuses a direct action target or delegates explicit reveal to any of three optional children.
    private static boolean showItemOrAccessibleActionTarget(
            @Nullable Node item,
            @Nullable Node first,
            @Nullable Node second,
            @Nullable Node third,
            Object... parameters
    ) {
        if (parameters.length > 0 && containsUnrevealableActionNodeTarget(first, second, third, parameters)) {
            return false;
        }
        if (delegateContainingNodeReveal(item, first, parameters)
                || delegateContainingNodeReveal(item, second, parameters)
                || delegateContainingNodeReveal(item, third, parameters)) {
            return true;
        }
        if (delegateSelectedItemReveal(item, parameters)) {
            return true;
        }
        if (showItemIfPresent(item)) {
            return true;
        }
        return parameters.length > 0
                && (showAccessibleActionTarget(first, parameters)
                || showAccessibleActionTarget(second, parameters)
                || showAccessibleActionTarget(third, parameters));
    }

    /// Focuses a direct action target or delegates explicit reveal to two leading slots or a trailing list.
    private static boolean showItemOrAccessibleActionTarget(
            @Nullable Node item,
            @Nullable Node first,
            @Nullable Node second,
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        if (parameters.length > 0 && containsUnrevealableActionNodeTarget(first, second, items, parameters)) {
            return false;
        }
        if (delegateContainingNodeReveal(item, first, parameters)
                || delegateContainingNodeReveal(item, second, parameters)
                || delegateContainingListReveal(item, items, parameters)) {
            return true;
        }
        if (delegateSelectedItemReveal(item, parameters)) {
            return true;
        }
        if (showItemIfPresent(item)) {
            return true;
        }
        return parameters.length > 0
                && (showAccessibleActionTarget(first, parameters)
                || showAccessibleActionTarget(second, parameters)
                || showAccessibleActionTarget(items, parameters));
    }

    /// Delegates reveal into a containing node slot before focusing a nested descendant directly.
    private static boolean delegateContainingNodeReveal(
            @Nullable Node selectedItem,
            @Nullable Node containingItem,
            Object... parameters
    ) {
        Objects.requireNonNull(parameters, "parameters");
        return indexParameter(parameters) < 0
                && containingItem != null
                && containingItem != selectedItem
                && containsAccessibleActionTarget(containingItem, parameters)
                && showAccessibleActionTarget(containingItem, parameters);
    }

    /// Delegates reveal into a containing list item for target-based parameters.
    private static boolean delegateContainingListReveal(
            @Nullable Node selectedItem,
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(parameters, "parameters");
        return indexParameter(parameters) < 0 && delegateContainingItemReveal(selectedItem, items, parameters);
    }

    /// Delegates reveal into a selected child before falling back to its current contained focus.
    private static boolean delegateSelectedItemReveal(@Nullable Node item, Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        return item != null
                && parameters.length > 0
                && containsOwnAccessibleActionTarget(item, parameters)
                && showAccessibleActionTarget(item, parameters);
    }

    /// Delegates reveal into the list item that owns the requested target before focusing a nested descendant.
    private static boolean delegateContainingItemReveal(
            @Nullable Node selectedItem,
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(parameters, "parameters");
        @Nullable Node containingItem = containingItem(items, parameters);
        return containingItem != null
                && containingItem != selectedItem
                && delegateSelectedItemReveal(containingItem, parameters);
    }

    /// Delegates an explicit reveal request to a resolved item and reveals the focused target through the owner.
    ///
    /// @param owner      the container through which the resulting focus target should be revealed
    /// @param item       the already resolved action item, or `null`
    /// @param parameters the explicit target parameters
    /// @return `true` when a nested route or accessibility action handled the request
    /// @throws NullPointerException if `owner` or `parameters` is `null`
    public static boolean showResolvedAccessibleActionTarget(Node owner, @Nullable Node item, Object... parameters) {
        Objects.requireNonNull(owner, "owner");
        if (showAccessibleActionTarget(item, parameters)) {
            revealCurrentFocusOwner(owner);
            return true;
        }
        return false;
    }

    /// Delegates an explicit reveal request and reveals the focused target through the owner when one is reached.
    ///
    /// The owner may itself route an indexed request to `item`; direct item routes are attempted before the owner's
    /// fallback route.
    ///
    /// @param owner      the container through which the resulting focus target should be revealed
    /// @param item       the preferred action item, or `null`
    /// @param parameters the explicit target parameters
    /// @return `true` when a nested route or accessibility action handled the request
    /// @throws NullPointerException if `owner` or `parameters` is `null`
    public static boolean showAccessibleActionTarget(Node owner, @Nullable Node item, Object... parameters) {
        Objects.requireNonNull(owner, "owner");
        if (item != null
                && parameters.length > 1
                && parametersReferenceIndexedActionItem(owner, item, parameters)
                && showAccessibleActionTarget(item, parametersAfterOwnerIndex(parameters))) {
            revealCurrentFocusOwner(owner);
            return true;
        }
        if (showAccessibleActionTarget(item, parameters)) {
            revealCurrentFocusOwner(owner);
            return true;
        }
        if (item != null && showRoutedAccessibleActionTarget(owner, prependParameter(item, parameters))) {
            revealCurrentFocusOwner(owner);
            return true;
        }
        return false;
    }

    /// Delegates an explicit reveal request to the first child that exposes the requested accessibility target.
    ///
    /// @param item       the root of the physical and accessibility child search, or `null`
    /// @param parameters the explicit target parameters
    /// @return `true` when a route or accessibility action handled the request
    /// @throws NullPointerException if `parameters` is `null`
    public static boolean showAccessibleActionTarget(@Nullable Node item, Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (item == null || cannotReachOrReveal(item) || parameters.length == 0) {
            return false;
        }
        Set<Node> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        return showAccessibleActionTarget(item, visited, parameters);
    }

    /// Delegates an explicit reveal request while avoiding accessibility child cycles.
    private static boolean showAccessibleActionTarget(
            @Nullable Node item,
            Set<Node> visited,
            Object... parameters
    ) {
        if (item == null || cannotReachOrReveal(item) || parameters.length == 0 || !visited.add(item)) {
            return false;
        }
        if (containsUnrevealableActionNodeTarget(item, parameters)) {
            return false;
        }
        if (routeRejectsAccessibleActionTarget(item, parameters)) {
            return false;
        }
        if (showRoutedAccessibleActionTarget(item, parameters)) {
            return true;
        }

        @Nullable Node directTarget = actionItem(item, parameters);
        if (directTarget != null && parametersReferenceOwnerRevealTarget(item, directTarget, parameters)) {
            item.executeAccessibleAction(AccessibleAction.SHOW_ITEM, parameters);
            return showItemIfPresent(directTarget);
        }
        if (showItemIfPresent(directTarget)) {
            return true;
        }

        if (M3TooltipRegistry.showInstalledTooltipActionTarget(item, parameters)) {
            return true;
        }

        if (item instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                if (showAccessibleActionTarget(child, visited, parameters)) {
                    return true;
                }
            }
        }

        if (containsOwnAccessibleActionTarget(item, parameters)
                && showOwnAccessibleActionTarget(item, parameters)) {
            return true;
        }

        int itemCount = indexedChildCount(item);
        for (int index = 0; index < itemCount; index++) {
            @Nullable Node child = indexedChild(item, index);
            if (child != null && child != item && showAccessibleActionTarget(child, visited, parameters)) {
                return true;
            }
        }
        return false;
    }

    /// Prepends one primary target before nested accessibility action parameters.
    private static Object[] prependParameter(Object first, Object... rest) {
        Objects.requireNonNull(rest, "rest");
        Object[] parameters = new Object[rest.length + 1];
        parameters[0] = first;
        System.arraycopy(rest, 0, parameters, 1, rest.length);
        return parameters;
    }

    /// Returns accessibility action parameters after the current owner index has selected a child.
    private static Object[] parametersAfterOwnerIndex(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        Object[] nestedParameters = new Object[parameters.length - 1];
        System.arraycopy(parameters, 1, nestedParameters, 0, nestedParameters.length);
        return nestedParameters;
    }

    /// Returns whether an installed route matcher rejects the supplied reveal parameters.
    private static boolean routeRejectsAccessibleActionTarget(@Nullable Node item, Object... parameters) {
        @Nullable AccessibleActionRoute route = accessibleActionRoute(item);
        return route != null && route.showTargetMatcher != null && !routeHandlesAnyShowTarget(route, parameters);
    }

    /// Delegates an explicit reveal request through a node's installed accessibility route.
    private static boolean showRoutedAccessibleActionTarget(@Nullable Node item, Object... parameters) {
        @Nullable AccessibleActionRoute route = accessibleActionRoute(item);
        return route != null
                && route.showHandler != null
                && routeHandlesAnyShowTarget(route, parameters)
                && route.showHandler.handle(parameters);
    }

    /// Returns whether a route matcher accepts any explicit reveal parameter.
    private static boolean routeHandlesAnyShowTarget(AccessibleActionRoute route, Object... parameters) {
        Objects.requireNonNull(route, "route");
        Objects.requireNonNull(parameters, "parameters");
        for (Object parameter : parameters) {
            if (routeHandlesShowTarget(route, parameter)) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether a route matcher accepts a possibly nested explicit reveal parameter.
    private static boolean routeHandlesShowTarget(AccessibleActionRoute route, @Nullable Object parameter) {
        if (route.showTargetMatcher != null && route.showTargetMatcher.test(parameter)) {
            return true;
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                if (routeHandlesShowTarget(route, value)) {
                    return true;
                }
            }
            return false;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                if (routeHandlesShowTarget(route, value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /// Delegates an explicit reveal request to a node that owns the requested accessibility target.
    private static boolean showOwnAccessibleActionTarget(@Nullable Node item, Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (item == null) {
            return false;
        }
        @Nullable AccessibleActionRoute route = accessibleActionRoute(item);
        if (route != null && route.showHandler != null) {
            return route.showHandler.handle(parameters);
        }
        item.executeAccessibleAction(AccessibleAction.SHOW_ITEM, parameters);
        return currentContainedFocusTarget(item) != null || activeExternalFocusTarget(item, item) != null;
    }

    /// Returns whether a node can neither receive focus now nor reveal itself from a collapsed self-hidden state.
    private static boolean cannotReachOrReveal(Node item) {
        if (canReach(item)) {
            return false;
        }
        return item.getScene() == null || !canReveal(item);
    }

    /// Delegates an explicit reveal request to an indexed child and reveals it through the owner when reached.
    ///
    /// @param owner      the container through which the resulting focus target should be revealed
    /// @param items      the indexed action items
    /// @param parameters the explicit target parameters
    /// @return `true` when an item handled the request
    /// @throws NullPointerException if `owner`, `items`, or `parameters` is `null`
    public static boolean showAccessibleActionTarget(
            Node owner,
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        Objects.requireNonNull(owner, "owner");
        if (showAccessibleActionTarget(items, parameters)) {
            revealCurrentFocusOwner(owner);
            return true;
        }
        return false;
    }

    /// Delegates an explicit reveal request to the first indexed child that exposes the requested target.
    ///
    /// @param items      the indexed action items
    /// @param parameters the explicit target parameters
    /// @return `true` when an item handled the request
    /// @throws NullPointerException if `items` or `parameters` is `null`
    public static boolean showAccessibleActionTarget(
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        Objects.requireNonNull(items, "items");
        if (containsUnrevealableActionNodeTarget(items, parameters)) {
            return false;
        }
        for (Node item : items) {
            if (showAccessibleActionTarget(item, parameters)) {
                return true;
            }
        }
        return false;
    }

    /// Returns a node's currently exposed accessibility focus target when available.
    ///
    /// The node's `FOCUS_NODE` attribute is preferred when it resolves to a reachable node; otherwise the first
    /// reachable focus-traversable node in the item's accessibility tree is returned.
    ///
    /// @param item the item to inspect, or `null`
    /// @return the reachable accessibility focus target, or `null`
    public static @Nullable Node accessibleFocusTarget(@Nullable Node item) {
        if (!canReach(item)) {
            return null;
        }
        @Nullable Object focusNode = item.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);
        if (focusNode instanceof Node node && canReach(node)) {
            @Nullable Node focusTarget = focusTarget(node);
            if (focusTarget != null) {
                return focusTarget;
            }
        }
        return focusTarget(item);
    }

    /// Returns the focusable item or descendant used for accessibility focus requests.
    ///
    /// @param item the item whose reachable tree should be searched, or `null`
    /// @return the first reachable focus-traversable node, or `null`
    public static @Nullable Node focusTarget(@Nullable Node item) {
        if (!canReach(item)) {
            return null;
        }
        return focusTargetInReachableTree(item, true);
    }

    /// Returns a focusable item or descendant without requiring attachment to a scene.
    ///
    /// This helper is for structural reveal APIs that can decide which node would receive focus before a control is
    /// attached to a live scene. Callers that actually request keyboard focus must still check [canReach].
    ///
    /// @param item the item whose visible and enabled structure should be searched, or `null`
    /// @return the first structurally reachable focus-traversable node, or `null`
    public static @Nullable Node structuralFocusTarget(@Nullable Node item) {
        if (!isEffectivelyReachable(item)) {
            return null;
        }
        return focusTargetInReachableTree(Objects.requireNonNull(item, "item"), false);
    }

    /// Returns a focusable item or descendant in an already reachable tree.
    private static @Nullable Node focusTargetInReachableTree(Node item, boolean requireScene) {
        if (!(requireScene ? canReach(item) : isEffectivelyReachable(item))) {
            return null;
        }
        if (item.isFocusTraversable()) {
            return item;
        }

        Set<Node> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        return focusTargetInReachableTree(item, requireScene, visited);
    }

    /// Returns a focusable item or descendant while avoiding accessibility child cycles.
    private static @Nullable Node focusTargetInReachableTree(
            Node item,
            boolean requireScene,
            Set<Node> visited
    ) {
        if (!visited.add(item) || !(requireScene ? canReach(item) : isEffectivelyReachable(item))) {
            return null;
        }
        if (item.isFocusTraversable()) {
            return item;
        }

        @Nullable Node indexedTarget = firstIndexedFocusTarget(item, requireScene, visited, indexedChildCount(item));
        if (indexedTarget != null) {
            return indexedTarget;
        }

        if (item instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable Node focusTarget = focusTargetInReachableTree(child, requireScene, visited);
                if (focusTarget != null) {
                    return focusTarget;
                }
            }
        }
        return null;
    }

    /// Returns the first focusable target exposed through indexed accessibility children.
    private static @Nullable Node firstIndexedFocusTarget(
            Node item,
            boolean requireScene,
            Set<Node> visited,
            int indexedChildCount
    ) {
        for (int index = 0; index < indexedChildCount; index++) {
            @Nullable Node child = indexedChild(item, index);
            if (child != null && child != item) {
                @Nullable Node focusTarget = focusTargetInReachableTree(child, requireScene, visited);
                if (focusTarget != null) {
                    return focusTarget;
                }
            }
        }
        return null;
    }

    /// Returns the first focusable item or descendant in the supplied item list.
    ///
    /// @param items the items in focus order
    /// @return the first reachable accessibility focus target, or `null`
    /// @throws NullPointerException if `items` is `null`
    public static @Nullable Node firstFocusTarget(ObservableList<? extends Node> items) {
        Objects.requireNonNull(items, "items");
        return firstAccessibleFocusTarget(items);
    }

    /// Returns the leading focus target, or the first focusable item in the supplied item list.
    ///
    /// @param leading the optional leading item
    /// @param items   the remaining items in focus order
    /// @return the first reachable accessibility focus target, or `null`
    /// @throws NullPointerException if `items` is `null`
    public static @Nullable Node firstFocusTarget(@Nullable Node leading, ObservableList<? extends Node> items) {
        Objects.requireNonNull(items, "items");
        @Nullable Node leadingTarget = accessibleFocusTarget(leading);
        return leadingTarget != null ? leadingTarget : firstAccessibleFocusTarget(items);
    }

    /// Returns the first focusable item in the supplied item list, or the trailing focus target.
    ///
    /// @param items    the items before the trailing slot
    /// @param trailing the optional trailing item
    /// @return the first reachable accessibility focus target, or `null`
    /// @throws NullPointerException if `items` is `null`
    public static @Nullable Node firstFocusTarget(ObservableList<? extends Node> items, @Nullable Node trailing) {
        Objects.requireNonNull(items, "items");
        @Nullable Node itemTarget = firstAccessibleFocusTarget(items);
        return itemTarget != null ? itemTarget : accessibleFocusTarget(trailing);
    }

    /// Returns the first focusable target among two optional child nodes.
    ///
    /// @param first  the optional first item
    /// @param second the optional second item
    /// @return the first reachable accessibility focus target, or `null`
    public static @Nullable Node firstFocusTarget(@Nullable Node first, @Nullable Node second) {
        @Nullable Node firstTarget = accessibleFocusTarget(first);
        return firstTarget != null ? firstTarget : accessibleFocusTarget(second);
    }

    /// Returns the first focusable target among three optional child nodes.
    ///
    /// @param first  the optional first item
    /// @param second the optional second item
    /// @param third  the optional third item
    /// @return the first reachable accessibility focus target, or `null`
    public static @Nullable Node firstFocusTarget(@Nullable Node first, @Nullable Node second, @Nullable Node third) {
        @Nullable Node firstTarget = accessibleFocusTarget(first);
        if (firstTarget != null) {
            return firstTarget;
        }
        @Nullable Node secondTarget = accessibleFocusTarget(second);
        return secondTarget != null ? secondTarget : accessibleFocusTarget(third);
    }

    /// Returns the first focusable target among two optional child nodes and a trailing list.
    ///
    /// @param first  the optional first item
    /// @param second the optional second item
    /// @param items  the remaining items in focus order
    /// @return the first reachable accessibility focus target, or `null`
    /// @throws NullPointerException if `items` is `null`
    public static @Nullable Node firstFocusTarget(
            @Nullable Node first,
            @Nullable Node second,
            ObservableList<? extends Node> items
    ) {
        Objects.requireNonNull(items, "items");
        @Nullable Node firstTarget = accessibleFocusTarget(first);
        if (firstTarget != null) {
            return firstTarget;
        }
        @Nullable Node secondTarget = accessibleFocusTarget(second);
        return secondTarget != null ? secondTarget : firstAccessibleFocusTarget(items);
    }

    /// Returns the current focus target inside the supplied item list, or the first focusable item.
    ///
    /// @param owner the node whose scene and external overlays supply current focus
    /// @param items the possible focus owners
    /// @return the current contained or external target, the first reachable target, or `null`
    /// @throws NullPointerException if `owner` or `items` is `null`
    public static @Nullable Node currentOrFirstFocusTarget(Node owner, ObservableList<? extends Node> items) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(items, "items");
        @Nullable Node currentTarget = currentFocusTarget(owner, items);
        return currentTarget != null ? currentTarget : firstFocusTarget(items);
    }

    /// Returns the current focus target inside a selection container, or its selected or first reachable item.
    ///
    /// @param <T>          the selectable item type
    /// @param owner        the node whose scene and external overlays supply current focus
    /// @param items        the selectable children in order
    /// @param selectedItem the current selection, or `null`
    /// @param itemType     the selectable item type token
    /// @return the current focus target, selected-item target, first reachable target, or `null`
    /// @throws NullPointerException if `owner`, `items`, or `itemType` is `null`
    public static <T extends Node> @Nullable Node currentOrSelectionFocusTarget(
            Node owner,
            List<? extends Node> items,
            @Nullable T selectedItem,
            Class<T> itemType
    ) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(itemType, "itemType");

        @Nullable Node currentTarget = currentFocusTarget(owner, items);
        if (currentTarget != null) {
            return currentTarget;
        }
        return focusTarget(M3SelectionNavigation.focusTarget(items, selectedItem, itemType));
    }

    /// Returns the current focus target inside a leading item or item list, or the first focusable item.
    ///
    /// @param owner   the node whose scene and external overlays supply current focus
    /// @param leading the optional leading item
    /// @param items   the remaining possible focus owners
    /// @return the current or first reachable focus target, or `null`
    /// @throws NullPointerException if `owner` or `items` is `null`
    public static @Nullable Node currentOrFirstFocusTarget(
            Node owner,
            @Nullable Node leading,
            ObservableList<? extends Node> items
    ) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(items, "items");
        @Nullable Node currentTarget = currentFocusTarget(owner, leading, items);
        return currentTarget != null ? currentTarget : firstFocusTarget(leading, items);
    }

    /// Returns the current focus target inside an item list or trailing item, or the first focusable item.
    ///
    /// @param owner    the node whose scene and external overlays supply current focus
    /// @param items    the possible focus owners before the trailing slot
    /// @param trailing the optional trailing item
    /// @return the current or first reachable focus target, or `null`
    /// @throws NullPointerException if `owner` or `items` is `null`
    public static @Nullable Node currentOrFirstFocusTarget(
            Node owner,
            ObservableList<? extends Node> items,
            @Nullable Node trailing
    ) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(items, "items");
        @Nullable Node currentTarget = currentFocusTarget(owner, items, trailing);
        return currentTarget != null ? currentTarget : firstFocusTarget(items, trailing);
    }

    /// Returns the current focus target inside either optional child node, or the first focusable item.
    ///
    /// @param owner  the node whose scene and external overlays supply current focus
    /// @param first  the optional first item
    /// @param second the optional second item
    /// @return the current or first reachable focus target, or `null`
    /// @throws NullPointerException if `owner` is `null`
    public static @Nullable Node currentOrFirstFocusTarget(Node owner, @Nullable Node first, @Nullable Node second) {
        Objects.requireNonNull(owner, "owner");
        @Nullable Node currentTarget = currentFocusTarget(owner, first, second);
        return currentTarget != null ? currentTarget : firstFocusTarget(first, second);
    }

    /// Returns the current focus target inside three optional child nodes, or the first focusable item.
    ///
    /// @param owner  the node whose scene and external overlays supply current focus
    /// @param first  the optional first item
    /// @param second the optional second item
    /// @param third  the optional third item
    /// @return the current or first reachable focus target, or `null`
    /// @throws NullPointerException if `owner` is `null`
    public static @Nullable Node currentOrFirstFocusTarget(
            Node owner,
            @Nullable Node first,
            @Nullable Node second,
            @Nullable Node third
    ) {
        Objects.requireNonNull(owner, "owner");
        @Nullable Node currentTarget = currentFocusTarget(owner, first, second, third);
        return currentTarget != null ? currentTarget : firstFocusTarget(first, second, third);
    }

    /// Returns the current focus target inside two optional child nodes and a list, or the first focusable item.
    ///
    /// @param owner  the node whose scene and external overlays supply current focus
    /// @param first  the optional first item
    /// @param second the optional second item
    /// @param items  the remaining possible focus owners
    /// @return the current or first reachable focus target, or `null`
    /// @throws NullPointerException if `owner` or `items` is `null`
    public static @Nullable Node currentOrFirstFocusTarget(
            Node owner,
            @Nullable Node first,
            @Nullable Node second,
            ObservableList<? extends Node> items
    ) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(items, "items");
        @Nullable Node currentTarget = currentFocusTarget(owner, first, second, items);
        return currentTarget != null ? currentTarget : firstFocusTarget(first, second, items);
    }

    /// Returns the current focus owner when it belongs to one item in the supplied list.
    ///
    /// Active popup or overlay focus exposed by an item is considered before focus in the owner's scene.
    ///
    /// @param owner the node whose scene and external overlays supply current focus
    /// @param items the possible focus owners
    /// @return the current contained or external focus target, or `null`
    /// @throws NullPointerException if `owner` or `items` is `null`
    public static @Nullable Node currentFocusTarget(Node owner, List<? extends Node> items) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(items, "items");
        @Nullable Node externalTarget = activeExternalFocusTarget(owner, items);
        if (externalTarget != null) {
            return externalTarget;
        }

        @Nullable Node focusOwner = focusOwner(owner);
        if (focusOwner == null) {
            return null;
        }

        for (Node item : items) {
            @Nullable Node target = containedFocusTarget(item, focusOwner);
            if (target != null) {
                return target;
            }
        }
        return null;
    }

    /// Returns the current focus owner when it belongs to a leading item or one item in the supplied list.
    ///
    /// @param owner   the node whose scene and external overlays supply current focus
    /// @param leading the optional leading focus owner
    /// @param items   the remaining possible focus owners
    /// @return the current contained or external focus target, or `null`
    /// @throws NullPointerException if `owner` or `items` is `null`
    public static @Nullable Node currentFocusTarget(
            Node owner,
            @Nullable Node leading,
            ObservableList<? extends Node> items
    ) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(items, "items");
        @Nullable Node externalLeadingTarget = activeExternalFocusTarget(owner, leading);
        if (externalLeadingTarget != null) {
            return externalLeadingTarget;
        }
        @Nullable Node externalItemTarget = activeExternalFocusTarget(owner, items);
        if (externalItemTarget != null) {
            return externalItemTarget;
        }

        @Nullable Node focusOwner = focusOwner(owner);
        if (focusOwner == null) {
            return null;
        }

        @Nullable Node leadingTarget = containedFocusTarget(leading, focusOwner);
        if (leadingTarget != null) {
            return leadingTarget;
        }

        for (Node item : items) {
            @Nullable Node target = containedFocusTarget(item, focusOwner);
            if (target != null) {
                return target;
            }
        }
        return null;
    }

    /// Returns the current focus owner when it belongs to one item in the supplied list or a trailing item.
    ///
    /// @param owner    the node whose scene and external overlays supply current focus
    /// @param items    the possible focus owners before the trailing slot
    /// @param trailing the optional trailing focus owner
    /// @return the current contained or external focus target, or `null`
    /// @throws NullPointerException if `owner` or `items` is `null`
    public static @Nullable Node currentFocusTarget(
            Node owner,
            ObservableList<? extends Node> items,
            @Nullable Node trailing
    ) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(items, "items");
        @Nullable Node externalItemTarget = activeExternalFocusTarget(owner, items);
        if (externalItemTarget != null) {
            return externalItemTarget;
        }
        @Nullable Node externalTrailingTarget = activeExternalFocusTarget(owner, trailing);
        if (externalTrailingTarget != null) {
            return externalTrailingTarget;
        }

        @Nullable Node focusOwner = focusOwner(owner);
        if (focusOwner == null) {
            return null;
        }

        for (Node item : items) {
            @Nullable Node target = containedFocusTarget(item, focusOwner);
            if (target != null) {
                return target;
            }
        }

        return containedFocusTarget(trailing, focusOwner);
    }

    /// Returns the current focus owner when it belongs to either optional child node.
    ///
    /// @param owner  the node whose scene and external overlays supply current focus
    /// @param first  the optional first focus owner
    /// @param second the optional second focus owner
    /// @return the current contained or external focus target, or `null`
    /// @throws NullPointerException if `owner` is `null`
    public static @Nullable Node currentFocusTarget(Node owner, @Nullable Node first, @Nullable Node second) {
        Objects.requireNonNull(owner, "owner");
        @Nullable Node externalFirstTarget = activeExternalFocusTarget(owner, first);
        if (externalFirstTarget != null) {
            return externalFirstTarget;
        }
        @Nullable Node externalSecondTarget = activeExternalFocusTarget(owner, second);
        if (externalSecondTarget != null) {
            return externalSecondTarget;
        }

        @Nullable Node focusOwner = focusOwner(owner);
        if (focusOwner == null) {
            return null;
        }

        @Nullable Node firstTarget = containedFocusTarget(first, focusOwner);
        return firstTarget != null ? firstTarget : containedFocusTarget(second, focusOwner);
    }

    /// Returns the current focus owner when it belongs to one of three optional child nodes.
    ///
    /// @param owner  the node whose scene and external overlays supply current focus
    /// @param first  the optional first focus owner
    /// @param second the optional second focus owner
    /// @param third  the optional third focus owner
    /// @return the current contained or external focus target, or `null`
    /// @throws NullPointerException if `owner` is `null`
    public static @Nullable Node currentFocusTarget(
            Node owner,
            @Nullable Node first,
            @Nullable Node second,
            @Nullable Node third
    ) {
        Objects.requireNonNull(owner, "owner");
        @Nullable Node externalFirstTarget = activeExternalFocusTarget(owner, first);
        if (externalFirstTarget != null) {
            return externalFirstTarget;
        }
        @Nullable Node externalSecondTarget = activeExternalFocusTarget(owner, second);
        if (externalSecondTarget != null) {
            return externalSecondTarget;
        }
        @Nullable Node externalThirdTarget = activeExternalFocusTarget(owner, third);
        if (externalThirdTarget != null) {
            return externalThirdTarget;
        }

        @Nullable Node focusOwner = focusOwner(owner);
        if (focusOwner == null) {
            return null;
        }

        @Nullable Node firstTarget = containedFocusTarget(first, focusOwner);
        if (firstTarget != null) {
            return firstTarget;
        }
        @Nullable Node secondTarget = containedFocusTarget(second, focusOwner);
        return secondTarget != null ? secondTarget : containedFocusTarget(third, focusOwner);
    }

    /// Returns the current focus owner when it belongs to one of two optional children or a list item.
    ///
    /// @param owner  the node whose scene and external overlays supply current focus
    /// @param first  the optional first focus owner
    /// @param second the optional second focus owner
    /// @param items  the remaining possible focus owners
    /// @return the current contained or external focus target, or `null`
    /// @throws NullPointerException if `owner` or `items` is `null`
    public static @Nullable Node currentFocusTarget(
            Node owner,
            @Nullable Node first,
            @Nullable Node second,
            ObservableList<? extends Node> items
    ) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(items, "items");
        @Nullable Node externalFirstTarget = activeExternalFocusTarget(owner, first);
        if (externalFirstTarget != null) {
            return externalFirstTarget;
        }
        @Nullable Node externalSecondTarget = activeExternalFocusTarget(owner, second);
        if (externalSecondTarget != null) {
            return externalSecondTarget;
        }
        @Nullable Node externalItemTarget = activeExternalFocusTarget(owner, items);
        if (externalItemTarget != null) {
            return externalItemTarget;
        }

        @Nullable Node focusOwner = focusOwner(owner);
        if (focusOwner == null) {
            return null;
        }

        @Nullable Node firstTarget = containedFocusTarget(first, focusOwner);
        if (firstTarget != null) {
            return firstTarget;
        }
        @Nullable Node secondTarget = containedFocusTarget(second, focusOwner);
        if (secondTarget != null) {
            return secondTarget;
        }
        for (Node item : items) {
            @Nullable Node itemTarget = containedFocusTarget(item, focusOwner);
            if (itemTarget != null) {
                return itemTarget;
            }
        }
        return null;
    }

    /// Returns the current scene focus owner for an owner node.
    private static @Nullable Node focusOwner(Node owner) {
        @Nullable Scene scene = owner.getScene();
        return scene == null ? null : scene.getFocusOwner();
    }

    /// Returns an active external focus target exposed by one item outside the owner subtree.
    ///
    /// Installed rich-tooltip content, popup content exposed through `FOCUS_NODE`, and indexed accessibility
    /// descendants participate in the search.
    ///
    /// @param owner the node whose physical subtree is excluded
    /// @param item  the item that may expose external focus, or `null`
    /// @return the active reachable external focus target, or `null`
    /// @throws NullPointerException if `owner` is `null`
    public static @Nullable Node activeExternalFocusTarget(Node owner, @Nullable Node item) {
        Objects.requireNonNull(owner, "owner");
        if (!canReach(item)) {
            return null;
        }

        @Nullable Node directTarget = directActiveExternalFocusTarget(owner, item);
        if (directTarget != null) {
            return directTarget;
        }

        Set<Node> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        visited.add(item);
        return activeExternalDescendantFocusTarget(owner, item, visited);
    }

    /// Returns an active external focus target exposed directly by one reachable item.
    private static @Nullable Node directActiveExternalFocusTarget(Node owner, Node item) {
        @Nullable Node tooltipFocusTarget = M3TooltipRegistry.activeInstalledTooltipFocusTarget(item);
        if (tooltipFocusTarget != null && isActiveExternalFocusTarget(owner, tooltipFocusTarget)) {
            return tooltipFocusTarget;
        }

        @Nullable Object focusNode = item.queryAccessibleAttribute(AccessibleAttribute.FOCUS_NODE);
        return focusNode instanceof Node node && isActiveExternalFocusTarget(owner, node) ? node : null;
    }

    /// Returns an active external focus target while avoiding accessibility child cycles.
    private static @Nullable Node activeExternalFocusTarget(
            Node owner,
            @Nullable Node item,
            Set<Node> visited
    ) {
        if (!canReach(item) || !visited.add(item)) {
            return null;
        }

        @Nullable Node directTarget = directActiveExternalFocusTarget(owner, item);
        if (directTarget != null) {
            return directTarget;
        }

        return activeExternalDescendantFocusTarget(owner, item, visited);
    }

    /// Returns an active external focus target exposed by one item's physical or indexed descendants.
    private static @Nullable Node activeExternalDescendantFocusTarget(
            Node owner,
            Node item,
            Set<Node> visited
    ) {
        if (item instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                @Nullable Node childTarget = activeExternalFocusTarget(owner, child, visited);
                if (childTarget != null) {
                    return childTarget;
                }
            }
        }

        if (indexedChildrenCanExposeActiveFocus(item)) {
            int itemCount = indexedChildCount(item);
            for (int index = 0; index < itemCount; index++) {
                @Nullable Node child = indexedChild(item, index);
                if (child != null && child != item) {
                    @Nullable Node childTarget = activeExternalFocusTarget(owner, child, visited);
                    if (childTarget != null) {
                        return childTarget;
                    }
                }
            }
        }
        return null;
    }

    /// Returns the first active external focus target exposed by one item list.
    private static @Nullable Node activeExternalFocusTarget(Node owner, List<? extends Node> items) {
        Set<Node> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Node item : items) {
            @Nullable Node target = activeExternalFocusTarget(owner, item, visited);
            if (target != null) {
                return target;
            }
        }
        return null;
    }

    /// Returns whether indexed children can currently expose active external focus.
    private static boolean indexedChildrenCanExposeActiveFocus(Node item) {
        @Nullable Object expanded = item.queryAccessibleAttribute(AccessibleAttribute.EXPANDED);
        return !Boolean.FALSE.equals(expanded);
    }

    /// Returns whether a focus target belongs to a live popup or overlay outside the owner subtree.
    private static boolean isActiveExternalFocusTarget(Node owner, Node focusTarget) {
        if (!canReach(focusTarget) || containsNode(owner, focusTarget)) {
            return false;
        }

        @Nullable Scene focusTargetScene = focusTarget.getScene();
        @Nullable Node focusOwner = focusTargetScene == null ? null : focusTargetScene.getFocusOwner();
        return focusOwner != null && focusOwner.isFocused() && containsNode(focusTarget, focusOwner);
    }

    /// Returns a focus owner when it is contained by an item that can expose focus.
    private static @Nullable Node containedFocusTarget(@Nullable Node item, Node focusOwner) {
        if (item == null) {
            return null;
        }
        @Nullable Node itemFocusTarget = accessibleFocusTarget(item);
        if (itemFocusTarget == null || !containsNode(item, focusOwner)) {
            return null;
        }
        return canReach(focusOwner) && focusOwner != item ? focusOwner : itemFocusTarget;
    }

    /// Returns the current scene focus owner when it is already inside the supplied item.
    private static @Nullable Node currentContainedFocusTarget(@Nullable Node item) {
        if (!canReach(item)) {
            return null;
        }
        @Nullable Scene scene = item.getScene();
        @Nullable Node focusOwner = scene == null ? null : scene.getFocusOwner();
        if (!canReach(focusOwner) || !containsNode(item, focusOwner)) {
            return null;
        }
        return focusOwner;
    }

    /// Returns whether a node can receive a direct or descendant focus request.
    ///
    /// @param node the node to inspect, or `null`
    /// @return `true` when the node is attached and effectively reachable
    public static boolean canReach(@Nullable Node node) {
        return node != null && node.getScene() != null && isEffectivelyReachable(node);
    }

    /// Returns whether a node can be revealed from a collapsed or hidden-self state.
    ///
    /// Unlike [canReach], this allows the node itself to be invisible because several Material surfaces use
    /// visibility to represent their collapsed state. The node may be detached for structural tests, but it must be
    /// enabled and every ancestor must be visible and enabled.
    ///
    /// @param node the node to inspect, or `null`
    /// @return `true` when the node can be revealed by its owner
    public static boolean canReveal(@Nullable Node node) {
        if (node == null || node.isDisabled()) {
            return false;
        }

        @Nullable Parent parent = node.getParent();
        while (parent != null) {
            if (!parent.isVisible() || parent.isDisabled()) {
                return false;
            }
            parent = parent.getParent();
        }
        return true;
    }

    /// Returns whether a node and its ancestor chain are visible and enabled.
    ///
    /// Scene attachment is not required.
    ///
    /// @param node the node to inspect, or `null`
    /// @return `true` when the node and every ancestor are visible and enabled
    public static boolean isEffectivelyReachable(@Nullable Node node) {
        @Nullable Node current = node;
        while (current != null) {
            if (!current.isVisible() || current.isDisabled()) {
                return false;
            }
            current = current.getParent();
        }
        return node != null;
    }

    /// Notifies a node and its ancestors that the node's exposed accessibility focus target changed.
    ///
    /// @param node the node whose focus target changed
    /// @throws NullPointerException if `node` is `null`
    public static void notifyFocusNodeChanged(Node node) {
        Objects.requireNonNull(node, "node");
        node.notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
        notifyFocusNodeChangedInAncestors(node);
    }

    /// Notifies ancestor nodes that a descendant's accessible focus target changed.
    ///
    /// The supplied node itself is not notified.
    ///
    /// @param node the descendant whose focus target changed
    /// @throws NullPointerException if `node` is `null`
    public static void notifyFocusNodeChangedInAncestors(Node node) {
        Objects.requireNonNull(node, "node");
        @Nullable Parent parent = node.getParent();
        while (parent != null) {
            parent.notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
            parent = parent.getParent();
        }
    }

    /// Returns whether the possible ancestor contains the requested descendant node.
    ///
    /// In addition to physical parent links, this recognizes the logical leading and trailing content owned by
    /// [M3ListItemBase].
    ///
    /// @param possibleAncestor   the possible physical or logical ancestor
    /// @param possibleDescendant the node to locate
    /// @return `true` when the nodes are identical or the descendant belongs to the ancestor
    /// @throws NullPointerException if either argument is `null`
    public static boolean containsNode(Node possibleAncestor, Node possibleDescendant) {
        Objects.requireNonNull(possibleAncestor, "possibleAncestor");
        Objects.requireNonNull(possibleDescendant, "possibleDescendant");

        @Nullable Node current = possibleDescendant;
        while (current != null) {
            if (current == possibleAncestor) {
                return true;
            }
            current = current.getParent();
        }

        if (!containsLogicalContentOwner(possibleAncestor)) {
            return false;
        }
        Set<Node> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        return containsNode(possibleAncestor, possibleDescendant, visited);
    }

    /// Returns whether a Parent subtree contains a list item that can expose logical content edges.
    private static boolean containsLogicalContentOwner(Node node) {
        if (node instanceof M3ListItemBase) {
            return true;
        }
        if (node instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                if (containsLogicalContentOwner(child)) {
                    return true;
                }
            }
        }
        return false;
    }

    /// Returns whether the possible ancestor contains the requested descendant node without revisiting cycles.
    private static boolean containsNode(Node possibleAncestor, Node possibleDescendant, Set<Node> visited) {
        if (possibleAncestor == possibleDescendant) {
            return true;
        }
        if (!visited.add(possibleAncestor)) {
            return false;
        }
        if (possibleAncestor instanceof M3ListItemBase listItem
                && (containsOptionalNode(possibleAncestor, listItem.getLeading(), possibleDescendant, visited)
                || containsOptionalNode(possibleAncestor, listItem.getTrailing(), possibleDescendant, visited))) {
            return true;
        }
        if (possibleAncestor instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                if (containsNode(child, possibleDescendant, visited)) {
                    return true;
                }
            }
        }
        return false;
    }

    /// Returns whether a nullable logical content node contains the requested descendant.
    private static boolean containsOptionalNode(
            Node owner,
            @Nullable Node possibleAncestor,
            Node possibleDescendant,
            Set<Node> visited
    ) {
        if (possibleAncestor == null) {
            return false;
        }
        if (possibleAncestor == possibleDescendant) {
            return true;
        }
        return possibleAncestor != owner && containsNode(possibleAncestor, possibleDescendant, visited);
    }

    /// Returns the indexed item or the item exposing the target referenced by accessibility parameters.
    ///
    /// @param items      the candidate item owners
    /// @param parameters the index or explicit target parameters
    /// @return the matching item, or `null` when no item owns the request
    /// @throws NullPointerException if `items` or `parameters` is `null`
    public static @Nullable Node containingItem(ObservableList<? extends Node> items, Object... parameters) {
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            return null;
        }
        @Nullable Object firstParameter = parameters[0];
        if (firstParameter instanceof Number) {
            return itemAt(items, parameters);
        }
        for (Object parameter : parameters) {
            @Nullable Node item = containingItem(items, parameter);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /// Returns whether one accessibility action parameter references the requested reachable selection target or one of its descendants.
    private static boolean containsSelectionTarget(Node target, @Nullable Object parameter) {
        if (parameter instanceof Node node) {
            return isEffectivelyReachable(node) && containsNode(target, node);
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                if (containsSelectionTarget(target, value)) {
                    return true;
                }
            }
            return false;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                if (containsSelectionTarget(target, value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /// Returns whether one accessibility action parameter references a target node or its descendant.
    private static boolean containsNodeTarget(Node target, @Nullable Object parameter) {
        if (parameter instanceof Node node) {
            return containsNode(target, node);
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                if (containsNodeTarget(target, value)) {
                    return true;
                }
            }
            return false;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                if (containsNodeTarget(target, value)) {
                    return true;
                }
            }
        }
        return false;
    }

    /// Returns whether an item exposes one accessibility action target parameter.
    private static boolean containsAccessibleActionTarget(
            Node item,
            @Nullable Object parameter,
            Set<Node> visited,
            boolean includeParentChildren
    ) {
        @Nullable AccessibleActionRoute route = accessibleActionRoute(item);
        if (route != null && route.showTargetMatcher != null) {
            return routeHandlesShowTarget(route, parameter);
        }
        if (parameter instanceof Node node) {
            return containsAccessibleNode(item, node, visited, includeParentChildren);
        }
        if (!visited.add(item)) {
            return false;
        }
        if (parameter instanceof Iterable<?> values) {
            Set<Node> branchVisited = Collections.newSetFromMap(new IdentityHashMap<>());
            for (Object value : values) {
                branchVisited.clear();
                if (containsAccessibleActionTarget(item, value, branchVisited, includeParentChildren)) {
                    return true;
                }
            }
            return false;
        }
        if (parameter instanceof Object[] values) {
            Set<Node> branchVisited = Collections.newSetFromMap(new IdentityHashMap<>());
            for (Object value : values) {
                branchVisited.clear();
                if (containsAccessibleActionTarget(item, value, branchVisited, includeParentChildren)) {
                    return true;
                }
            }
        }
        if (includeParentChildren && item instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                if (containsAccessibleActionTarget(child, parameter, visited, true)) {
                    return true;
                }
            }
        }

        @Nullable Object itemCountValue = item.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT);
        if (!(itemCountValue instanceof Number itemCountNumber)) {
            return false;
        }

        int itemCount = Math.max(0, itemCountNumber.intValue());
        for (int index = 0; index < itemCount; index++) {
            @Nullable Object child = item.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, index);
            if (child instanceof Node childNode
                    && containsAccessibleActionTarget(childNode, parameter, visited, includeParentChildren)) {
                return true;
            }
        }
        return false;
    }

    /// Returns whether an item itself exposes the supplied target through its accessibility item tree.
    private static boolean containsOwnAccessibleActionTarget(@Nullable Node item, Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (item == null || parameters.length == 0) {
            return false;
        }

        for (Object parameter : parameters) {
            if (parameter instanceof Node node && containsDirectAccessibleNode(item, node)) {
                return true;
            }
        }

        Set<Node> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Object parameter : parameters) {
            visited.clear();
            if (containsAccessibleActionTarget(item, parameter, visited, false)) {
                return true;
            }
        }
        return false;
    }


    /// Returns whether an owner exposes a node without traversing indexed accessibility children.
    private static boolean containsDirectAccessibleNode(Node owner, Node requestedNode) {
        @Nullable AccessibleActionRoute route = accessibleActionRoute(owner);
        if (route != null && route.showTargetMatcher != null) {
            return routeHandlesShowTarget(route, requestedNode);
        }
        return owner == requestedNode
                || containsNode(owner, requestedNode)
                || M3TooltipRegistry.containsInstalledTooltipActionTarget(owner, requestedNode);
    }

    /// Returns whether an owner node exposes a requested node directly or through indexed accessibility children.
    private static boolean containsAccessibleNode(
            Node owner,
            Node requestedNode,
            Set<Node> visited,
            boolean includeParentChildren
    ) {
        if (!visited.add(owner)) {
            return false;
        }
        if (owner == requestedNode) {
            return true;
        }
        @Nullable AccessibleActionRoute route = accessibleActionRoute(owner);
        if (route != null && route.showTargetMatcher != null) {
            return routeHandlesShowTarget(route, requestedNode);
        }
        if (containsNode(owner, requestedNode)) {
            return true;
        }
        if (M3TooltipRegistry.containsInstalledTooltipActionTarget(owner, requestedNode)) {
            return true;
        }

        if (includeParentChildren && owner instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                if (containsAccessibleNode(child, requestedNode, visited, true)) {
                    return true;
                }
            }
        }

        @Nullable Object itemCountValue = owner.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT);
        if (!(itemCountValue instanceof Number itemCountNumber)) {
            return false;
        }

        int itemCount = Math.max(0, itemCountNumber.intValue());
        for (int index = 0; index < itemCount; index++) {
            @Nullable Object child = owner.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, index);
            if (child instanceof Node childNode
                    && containsAccessibleNode(childNode, requestedNode, visited, includeParentChildren)) {
                return true;
            }
        }
        return false;
    }

    /// Returns the indexed item or the item exposing one referenced target.
    private static @Nullable Node containingItem(ObservableList<? extends Node> items, @Nullable Object parameter) {
        if (parameter instanceof Number number) {
            return itemAt(items, number);
        }
        if (parameter instanceof Node node) {
            for (Node item : items) {
                if (containsNode(item, node) || containsAccessibleActionTarget(item, node)) {
                    return item;
                }
            }
            return null;
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                @Nullable Node item = containingItem(items, value);
                if (item != null) {
                    return item;
                }
            }
            return null;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                @Nullable Node item = containingItem(items, value);
                if (item != null) {
                    return item;
                }
            }
        }
        for (Node item : items) {
            if (containsAccessibleActionTarget(item, parameter)) {
                return item;
            }
        }
        return null;
    }

    /// Returns the child item referenced by accessibility action parameters.
    ///
    /// With no parameters, the first item exposing a reachable focus target is returned.
    ///
    /// @param items      the candidate action items
    /// @param parameters the index or explicit target parameters
    /// @return the matching action item, or `null`
    /// @throws NullPointerException if `items` or `parameters` is `null`
    public static @Nullable Node actionItem(ObservableList<? extends Node> items, Object... parameters) {
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            return firstFocusableItem(items);
        }
        @Nullable Object firstParameter = parameters[0];
        if (firstParameter instanceof Number) {
            return itemAt(items, parameters);
        }
        for (Object parameter : parameters) {
            @Nullable Node item = actionItem(items, parameter);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /// Returns a single node or contained descendant referenced by accessibility action parameters.
    ///
    /// With no parameters, `item` is returned only when it exposes a reachable focus target.
    ///
    /// @param item       the candidate action-item root, or `null`
    /// @param parameters the index or explicit target parameters
    /// @return the matching node or owned descendant, or `null`
    /// @throws NullPointerException if `parameters` is `null`
    public static @Nullable Node actionItem(@Nullable Node item, Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            return accessibleFocusTarget(item) == null ? null : item;
        }
        for (Object parameter : parameters) {
            @Nullable Node target = actionItem(item, parameter);
            if (target != null) {
                return target;
            }
        }
        return null;
    }

    /// Returns the leading or child item referenced by accessibility action parameters.
    private static @Nullable Node actionItem(
            @Nullable Node leading,
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            return accessibleFocusTarget(leading) != null ? leading : firstFocusableItem(items);
        }
        @Nullable Object firstParameter = parameters[0];
        if (firstParameter instanceof Number) {
            return itemAt(leading, items, parameters);
        }
        for (Object parameter : parameters) {
            @Nullable Node item = actionItem(leading, items, parameter);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /// Returns the leading or child item referenced by one accessibility action parameter.
    private static @Nullable Node actionItem(
            @Nullable Node leading,
            ObservableList<? extends Node> items,
            @Nullable Object parameter
    ) {
        if (parameter instanceof Number number) {
            return itemAt(leading, items, number);
        }
        if (parameter instanceof Node node) {
            @Nullable Node leadingTarget = containedActionTarget(leading, node);
            if (leadingTarget != null) {
                return leadingTarget;
            }
            for (Node item : items) {
                @Nullable Node target = containedActionTarget(item, node);
                if (target != null) {
                    return target;
                }
            }
            return null;
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                @Nullable Node item = actionItem(leading, items, value);
                if (item != null) {
                    return item;
                }
            }
            return null;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                @Nullable Node item = actionItem(leading, items, value);
                if (item != null) {
                    return item;
                }
            }
        }
        return null;
    }

    /// Returns the child or trailing item referenced by accessibility action parameters.
    private static @Nullable Node actionItem(
            ObservableList<? extends Node> items,
            @Nullable Node trailing,
            Object... parameters
    ) {
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            @Nullable Node item = firstFocusableItem(items);
            return item != null ? item : (accessibleFocusTarget(trailing) == null ? null : trailing);
        }
        @Nullable Object firstParameter = parameters[0];
        if (firstParameter instanceof Number) {
            return itemAt(items, trailing, parameters);
        }
        for (Object parameter : parameters) {
            @Nullable Node item = actionItem(items, trailing, parameter);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /// Returns the child or trailing item referenced by one accessibility action parameter.
    private static @Nullable Node actionItem(
            ObservableList<? extends Node> items,
            @Nullable Node trailing,
            @Nullable Object parameter
    ) {
        if (parameter instanceof Number number) {
            return itemAt(items, trailing, number);
        }
        if (parameter instanceof Node node) {
            for (Node item : items) {
                @Nullable Node target = containedActionTarget(item, node);
                if (target != null) {
                    return target;
                }
            }
            return containedActionTarget(trailing, node);
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                @Nullable Node item = actionItem(items, trailing, value);
                if (item != null) {
                    return item;
                }
            }
            return null;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                @Nullable Node item = actionItem(items, trailing, value);
                if (item != null) {
                    return item;
                }
            }
        }
        return null;
    }

    /// Returns the first or second optional item referenced by accessibility action parameters.
    private static @Nullable Node actionItem(@Nullable Node first, @Nullable Node second, Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            return accessibleFocusTarget(first) != null
                    ? first
                    : (accessibleFocusTarget(second) == null ? null : second);
        }
        @Nullable Object firstParameter = parameters[0];
        if (firstParameter instanceof Number) {
            return itemAt(first, second, firstParameter);
        }
        for (Object parameter : parameters) {
            @Nullable Node item = actionItem(first, second, parameter);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /// Returns the first or second optional item referenced by one accessibility action parameter.
    private static @Nullable Node actionItem(@Nullable Node first, @Nullable Node second, @Nullable Object parameter) {
        if (parameter instanceof Number number) {
            return itemAt(first, second, number);
        }
        if (parameter instanceof Node node) {
            @Nullable Node firstTarget = containedActionTarget(first, node);
            if (firstTarget != null) {
                return firstTarget;
            }
            return containedActionTarget(second, node);
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                @Nullable Node item = actionItem(first, second, value);
                if (item != null) {
                    return item;
                }
            }
            return null;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                @Nullable Node item = actionItem(first, second, value);
                if (item != null) {
                    return item;
                }
            }
        }
        return null;
    }

    /// Returns one of three optional items referenced by accessibility action parameters.
    private static @Nullable Node actionItem(
            @Nullable Node first,
            @Nullable Node second,
            @Nullable Node third,
            Object... parameters
    ) {
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            if (accessibleFocusTarget(first) != null) {
                return first;
            }
            return accessibleFocusTarget(second) != null
                    ? second
                    : (accessibleFocusTarget(third) == null ? null : third);
        }
        @Nullable Object firstParameter = parameters[0];
        if (firstParameter instanceof Number) {
            return itemAt(first, second, third, firstParameter);
        }
        for (Object parameter : parameters) {
            @Nullable Node item = actionItem(first, second, third, parameter);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /// Returns one of three optional items referenced by one accessibility action parameter.
    private static @Nullable Node actionItem(
            @Nullable Node first,
            @Nullable Node second,
            @Nullable Node third,
            @Nullable Object parameter
    ) {
        if (parameter instanceof Number number) {
            return itemAt(first, second, third, number);
        }
        if (parameter instanceof Node node) {
            @Nullable Node firstTarget = containedActionTarget(first, node);
            if (firstTarget != null) {
                return firstTarget;
            }
            @Nullable Node secondTarget = containedActionTarget(second, node);
            if (secondTarget != null) {
                return secondTarget;
            }
            return containedActionTarget(third, node);
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                @Nullable Node item = actionItem(first, second, third, value);
                if (item != null) {
                    return item;
                }
            }
            return null;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                @Nullable Node item = actionItem(first, second, third, value);
                if (item != null) {
                    return item;
                }
            }
        }
        return null;
    }

    /// Returns one of two optional leading items or a trailing-list item referenced by action parameters.
    private static @Nullable Node actionItem(
            @Nullable Node first,
            @Nullable Node second,
            ObservableList<? extends Node> items,
            Object... parameters
    ) {
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0) {
            if (accessibleFocusTarget(first) != null) {
                return first;
            }
            @Nullable Node secondTarget = accessibleFocusTarget(second);
            return secondTarget != null ? second : firstFocusableItem(items);
        }
        @Nullable Object firstParameter = parameters[0];
        if (firstParameter instanceof Number) {
            return itemAt(first, second, items, firstParameter);
        }
        for (Object parameter : parameters) {
            @Nullable Node item = actionItem(first, second, items, parameter);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /// Returns one of two optional leading items or a trailing-list item referenced by one action parameter.
    private static @Nullable Node actionItem(
            @Nullable Node first,
            @Nullable Node second,
            ObservableList<? extends Node> items,
            @Nullable Object parameter
    ) {
        if (parameter instanceof Number number) {
            return itemAt(first, second, items, number);
        }
        if (parameter instanceof Node node) {
            @Nullable Node firstTarget = containedActionTarget(first, node);
            if (firstTarget != null) {
                return firstTarget;
            }
            @Nullable Node secondTarget = containedActionTarget(second, node);
            if (secondTarget != null) {
                return secondTarget;
            }
            for (Node item : items) {
                @Nullable Node target = containedActionTarget(item, node);
                if (target != null) {
                    return target;
                }
            }
            return null;
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                @Nullable Node item = actionItem(first, second, items, value);
                if (item != null) {
                    return item;
                }
            }
            return null;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                @Nullable Node item = actionItem(first, second, items, value);
                if (item != null) {
                    return item;
                }
            }
        }
        return null;
    }

    /// Returns the first child item with a focusable target from a list.
    private static @Nullable Node firstFocusableItem(ObservableList<? extends Node> items) {
        for (Node item : items) {
            if (accessibleFocusTarget(item) != null) {
                return item;
            }
        }
        return null;
    }

    /// Returns the first exposed accessible focus target from a list.
    private static @Nullable Node firstAccessibleFocusTarget(ObservableList<? extends Node> items) {
        for (Node item : items) {
            @Nullable Node target = accessibleFocusTarget(item);
            if (target != null) {
                return target;
            }
        }
        return null;
    }

    /// Returns the child item referenced by one accessibility action parameter.
    private static @Nullable Node actionItem(ObservableList<? extends Node> items, @Nullable Object parameter) {
        if (parameter instanceof Number number) {
            return itemAt(items, number);
        }
        if (parameter instanceof Node node) {
            for (Node item : items) {
                @Nullable Node target = containedActionTarget(item, node);
                if (target != null) {
                    return target;
                }
            }
            return null;
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                @Nullable Node item = actionItem(items, value);
                if (item != null) {
                    return item;
                }
            }
            return null;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                @Nullable Node item = actionItem(items, value);
                if (item != null) {
                    return item;
                }
            }
        }
        return null;
    }

    /// Returns the action target contained by one item for a requested node.
    private static @Nullable Node containedActionTarget(@Nullable Node item, Node requestedNode) {
        if (item == null || !containsNode(item, requestedNode)) {
            return null;
        }
        if (item == requestedNode) {
            return isEffectivelyReachable(item) ? item : null;
        }
        if (cannotRevealActionNode(item, requestedNode)) {
            return null;
        }
        return accessibleFocusTarget(requestedNode) == null ? item : requestedNode;
    }

    /// Returns the single-node action target referenced by one accessibility action parameter.
    private static @Nullable Node actionItem(@Nullable Node item, @Nullable Object parameter) {
        if (item == null) {
            return null;
        }
        if (parameter instanceof Number number) {
            @Nullable Node indexedItem = indexedActionItem(item, number);
            return indexedItem == null && number.intValue() == 0 ? item : indexedItem;
        }
        if (parameter instanceof Node node) {
            return containedActionTarget(item, node);
        }
        if (parameter instanceof Iterable<?> values) {
            for (Object value : values) {
                @Nullable Node target = actionItem(item, value);
                if (target != null) {
                    return target;
                }
            }
            return null;
        }
        if (parameter instanceof Object[] values) {
            for (Object value : values) {
                @Nullable Node target = actionItem(item, value);
                if (target != null) {
                    return target;
                }
            }
        }
        return null;
    }

    /// Returns whether the parameters should be delegated to the item's own reveal action before direct focus.
    private static boolean parametersReferenceOwnerRevealTarget(Node item, Node target, Object... parameters) {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(target, "target");
        if (parametersReferenceIndexedActionItem(item, target, parameters)) {
            return true;
        }
        @Nullable Object expanded = item.queryAccessibleAttribute(AccessibleAttribute.EXPANDED);
        return Boolean.FALSE.equals(expanded) && containsOwnAccessibleActionTarget(item, parameters);
    }

    /// Returns whether the parameters select the supplied target from the item's own indexed children.
    private static boolean parametersReferenceIndexedActionItem(Node item, Node target, Object... parameters) {
        int index = indexParameter(parameters);
        return index >= 0 && indexedActionItem(item, index) == target;
    }

    /// Returns the child exposed by one item's accessibility index.
    private static @Nullable Node indexedActionItem(Node item, Number number) {
        Objects.requireNonNull(number, "number");
        return indexedActionItem(item, number.intValue());
    }

    /// Returns the non-negative number of indexed accessibility children exposed by one item.
    private static int indexedChildCount(Node item) {
        Objects.requireNonNull(item, "item");
        @Nullable Object itemCountValue = item.queryAccessibleAttribute(AccessibleAttribute.ITEM_COUNT);
        return itemCountValue instanceof Number itemCountNumber ? Math.max(0, itemCountNumber.intValue()) : 0;
    }

    /// Returns the child exposed by one item's accessibility index.
    private static @Nullable Node indexedChild(Node item, int index) {
        Objects.requireNonNull(item, "item");
        if (index < 0) {
            return null;
        }
        @Nullable Object child = item.queryAccessibleAttribute(AccessibleAttribute.ITEM_AT_INDEX, index);
        return child instanceof Node node ? node : null;
    }

    /// Returns the child exposed by one item's accessibility index.
    private static @Nullable Node indexedActionItem(Node item, int index) {
        return indexedChild(item, index);
    }

    /// Returns this node's index in its parent child list, or `-1` when it is detached.
    ///
    /// A detached node may still resolve an index from a list previously registered by
    /// [setIndexOwner][#setIndexOwner(Node, ObservableList)].
    ///
    /// @param node the node whose index should be resolved
    /// @return the identity-based child-list index, or `-1`
    /// @throws NullPointerException if `node` is `null`
    public static int indexInParent(Node node) {
        Objects.requireNonNull(node, "node");
        @Nullable Parent parent = node.getParent();
        if (parent != null) {
            return parent.getChildrenUnmodifiable().indexOf(node);
        }
        if (!node.hasProperties()) {
            return -1;
        }
        @Nullable Object ownerItems = node.getProperties().get(ACCESSIBLE_INDEX_ITEMS_KEY);
        return ownerItems instanceof ObservableList<?> items ? items.indexOf(node) : -1;
    }

    /// Sets the owner item list used for accessibility index lookup before skin attachment.
    ///
    /// The list is retained by reference until [#clearIndexOwner(Node)] is called. Later list mutations are reflected
    /// in [#indexInParent(Node)].
    ///
    /// @param node  the detached or pre-skin child node
    /// @param items the list that defines the node's logical index
    /// @throws NullPointerException if either argument is `null`
    public static void setIndexOwner(Node node, ObservableList<? extends Node> items) {
        Objects.requireNonNull(node, "node");
        Objects.requireNonNull(items, "items");
        node.getProperties().put(ACCESSIBLE_INDEX_ITEMS_KEY, items);
    }

    /// Clears the owner item list used for accessibility index lookup.
    ///
    /// @param node the node whose logical owner should be cleared
    /// @throws NullPointerException if `node` is `null`
    public static void clearIndexOwner(Node node) {
        Objects.requireNonNull(node, "node");
        if (node.hasProperties()) {
            node.getProperties().remove(ACCESSIBLE_INDEX_ITEMS_KEY);
        }
    }

    /// Returns the first integer accessibility parameter, or `-1` when none was supplied.
    ///
    /// Only the first parameter is inspected; any [Number] is converted with [Number#intValue()].
    ///
    /// @param parameters the accessibility action parameters
    /// @return the converted first parameter, or `-1` when it is absent or not numeric
    /// @throws NullPointerException if `parameters` is `null`
    public static int indexParameter(Object... parameters) {
        Objects.requireNonNull(parameters, "parameters");
        if (parameters.length == 0 || !(parameters[0] instanceof Number number)) {
            return -1;
        }
        return number.intValue();
    }
}
