// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.skins;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.beans.value.ChangeListener;
import javafx.collections.SetChangeListener;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.stage.Window;
import javafx.util.Duration;
import org.glavo.m3fx.animation.M3MotionSpec;
import org.glavo.m3fx.internal.M3Animation;
import org.glavo.m3fx.internal.M3MotionSettingsObserver;
import org.glavo.m3fx.internal.M3ThemeResolver;
import org.glavo.m3fx.theme.M3Theme;
import org.glavo.m3fx.tokens.M3StateLayerTokens;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// A bounded Material Design 3 state layer with ripple animation support.
@NotNullByDefault
final class M3StateLayer extends Pane {
    /// Style class assigned to direct component container-paint regions.
    static final String CONTAINER_PAINT_STYLE_CLASS = "m3-container-paint";

    /// Whether this layer renders hover, pressed, and ripple feedback in addition to keyboard focus.
    private final boolean interactionFeedbackEnabled;

    /// The pseudo-class used by button-like controls while their armed state is active.
    private static final PseudoClass ARMED_PSEUDO_CLASS = PseudoClass.getPseudoClass("armed");

    /// The pseudo-class used by JavaFX while a node is hovered.
    private static final PseudoClass HOVER_PSEUDO_CLASS = PseudoClass.getPseudoClass("hover");

    /// The pseudo-class used by JavaFX while a node has keyboard-visible focus.
    private static final PseudoClass FOCUS_VISIBLE_PSEUDO_CLASS = PseudoClass.getPseudoClass("focus-visible");

    /// The pseudo-class used by JavaFX while a node is pressed.
    private static final PseudoClass PRESSED_PSEUDO_CLASS = PseudoClass.getPseudoClass("pressed");

    /// The pseudo-class used by draggable components while the dragged state is active.
    private static final PseudoClass DRAGGED_PSEUDO_CLASS = PseudoClass.getPseudoClass("dragged");

    /// The fallback state layer tokens used when no theme is installed.
    private static final M3StateLayerTokens FALLBACK_TOKENS = M3StateLayerTokens.baseline();

    /// The class applied to state layer containers.
    static final String STYLE_CLASS = "m3-state-layer-container";

    /// The class applied to persistent state layer overlays.
    static final String OVERLAY_STYLE_CLASS = "m3-state-layer";

    /// The class applied to animated ripple nodes.
    static final String RIPPLE_STYLE_CLASS = "m3-ripple";

    /// The class applied to keyboard focus indicator rings.
    static final String FOCUS_INDICATOR_STYLE_CLASS = "m3-focus-indicator";

    /// The opacity used by the animated ripple at the start of a press.
    private static final double RIPPLE_START_OPACITY = 0.18;

    /// The path element index occupied by the top-right clip corner.
    private static final int CLIP_TOP_RIGHT_CORNER_INDEX = 2;

    /// The path element index occupied by the bottom-right clip corner.
    private static final int CLIP_BOTTOM_RIGHT_CORNER_INDEX = 4;

    /// The path element index occupied by the bottom-left clip corner.
    private static final int CLIP_BOTTOM_LEFT_CORNER_INDEX = 6;

    /// The path element index occupied by the top-left clip corner.
    private static final int CLIP_TOP_LEFT_CORNER_INDEX = 8;

    /// The persistent overlay node controlled by CSS pseudo-class rules.
    private final Region overlay = new Region();

    /// The optional component container paint rendered beneath interaction feedback.
    private final Region containerPaintLayer = new Region();

    /// The animated bounded ripple node.
    private final Region ripple = new Region();

    /// The independent keyboard focus indicator ring.
    private final Region focusIndicator = new Region();

    /// The explicitly resolved content paint used by controls that cannot retain CSS lookups while detached.
    private @Nullable Paint contentPaint;

    /// The clip that bounds overlay and ripple visuals to the component shape.
    private final Path clip = new Path();

    /// The reusable clip path starting point.
    private final MoveTo clipStart = new MoveTo();

    /// The reusable clip top edge.
    private final LineTo clipTopEdge = new LineTo();

    /// The reusable rounded top-right clip corner.
    private final ArcTo clipTopRightArc = new ArcTo();

    /// The reusable square top-right clip corner.
    private final LineTo clipTopRightLine = new LineTo();

    /// The reusable clip right edge.
    private final LineTo clipRightEdge = new LineTo();

    /// The reusable rounded bottom-right clip corner.
    private final ArcTo clipBottomRightArc = new ArcTo();

    /// The reusable square bottom-right clip corner.
    private final LineTo clipBottomRightLine = new LineTo();

    /// The reusable clip bottom edge.
    private final LineTo clipBottomEdge = new LineTo();

    /// The reusable rounded bottom-left clip corner.
    private final ArcTo clipBottomLeftArc = new ArcTo();

    /// The reusable square bottom-left clip corner.
    private final LineTo clipBottomLeftLine = new LineTo();

    /// The reusable clip left edge.
    private final LineTo clipLeftEdge = new LineTo();

    /// The reusable rounded top-left clip corner.
    private final ArcTo clipTopLeftArc = new ArcTo();

    /// The reusable square top-left clip corner.
    private final LineTo clipTopLeftLine = new LineTo();

    /// The reusable ripple expansion and release transition.
    private final RippleTransition rippleAnimation = new RippleTransition(ripple);

    /// The reusable transition for persistent overlay and focus-indicator opacity.
    private final StateOpacityTransition stateOpacityAnimation =
            new StateOpacityTransition(overlay, focusIndicator);

    /// Pauses motion-setting observation after the last active state-layer animation finishes.
    private final EventHandler<ActionEvent> animationFinishedHandler = event -> stopMotionObservationIfIdle();

    /// The control whose interaction states drive this layer.
    private @Nullable Node stateOwner;

    /// The resting overlay opacity contributed by a persistent semantic state.
    private double restingOverlayOpacity;

    /// Whether a delegated child currently owns keyboard-visible focus for this component.
    private boolean delegatedFocusVisible;

    /// Tracks keyboard-visible focus state for the owner.
    private @Nullable M3FocusVisibleTracker focusVisibleTracker;

    /// Handles disabled-state changes that should end transient feedback and update owner-state opacity.
    private final ChangeListener<Boolean> disabledStateListener = (observable, oldValue, newValue) -> {
        if (newValue) {
            cancelRipple();
        }
        animateOverlayOpacityFromOwnerState();
    };

    /// Clears a ripple when its owner leaves the scene before receiving a release event.
    private final ChangeListener<@Nullable Scene> ownerSceneListener = (observable, oldScene, newScene) -> {
        if (newScene == null) {
            cancelRipple();
        }
    };

    /// Handles relevant owner pseudo-class changes that should animate owner-state opacity.
    private final SetChangeListener<PseudoClass> pseudoClassStateListener = change -> {
        @Nullable PseudoClass pseudoClass = change.wasAdded() ? change.getElementAdded() : change.getElementRemoved();
        if (pseudoClass == ARMED_PSEUDO_CLASS
                || pseudoClass == HOVER_PSEUDO_CLASS
                || pseudoClass == FOCUS_VISIBLE_PSEUDO_CLASS
                || pseudoClass == PRESSED_PSEUDO_CLASS) {
            animateOverlayOpacityFromOwnerState();
        }
    };

    /// Handles button armed changes that should expose the CSS armed pseudo-class.
    private final ChangeListener<Boolean> buttonArmedStateListener = (observable, oldValue, newValue) -> {
        Node owner = stateOwner;
        if (owner != null) {
            owner.pseudoClassStateChanged(ARMED_PSEUDO_CLASS, newValue);
        }
    };

    /// Observes runtime motion settings while the owner is attached to a scene.
    private @Nullable M3MotionSettingsObserver motionSettingsObserver;

    /// The resolved animation setting cached for [motionSettingsRevision].
    private boolean cachedAnimationsEnabled;

    /// The motion-settings revision represented by [cachedAnimationsEnabled].
    private long motionSettingsRevision = Long.MIN_VALUE;

    /// The width currently applied to the focus indicator.
    private double focusIndicatorWidth = Double.NaN;

    /// The height currently applied to the focus indicator.
    private double focusIndicatorHeight = Double.NaN;

    /// The top-left radius currently applied to the focus indicator.
    private double focusIndicatorTopLeftRadius = Double.NaN;

    /// The top-right radius currently applied to the focus indicator.
    private double focusIndicatorTopRightRadius = Double.NaN;

    /// The bottom-right radius currently applied to the focus indicator.
    private double focusIndicatorBottomRightRadius = Double.NaN;

    /// The bottom-left radius currently applied to the focus indicator.
    private double focusIndicatorBottomLeftRadius = Double.NaN;

    /// The inner border inset currently applied to the focus indicator.
    private double focusIndicatorInset = Double.NaN;

    /// The border thickness currently applied to the focus indicator.
    private double focusIndicatorThickness = Double.NaN;

    /// The width currently represented by the rounded-rectangle clip.
    private double clipWidth = Double.NaN;

    /// The height currently represented by the rounded-rectangle clip.
    private double clipHeight = Double.NaN;

    /// The top-left radius currently represented by the rounded-rectangle clip.
    private double clipTopLeftRadius = Double.NaN;

    /// The top-right radius currently represented by the rounded-rectangle clip.
    private double clipTopRightRadius = Double.NaN;

    /// The bottom-right radius currently represented by the rounded-rectangle clip.
    private double clipBottomRightRadius = Double.NaN;

    /// The bottom-left radius currently represented by the rounded-rectangle clip.
    private double clipBottomLeftRadius = Double.NaN;

    /// Creates a state layer with interaction feedback and keyboard-focus indication.
    M3StateLayer() {
        this(true);
    }

    /// Creates a state layer with optional interaction feedback.
    ///
    /// Focus-visible tracking and focus-indicator rendering remain active when interaction feedback is disabled.
    /// Components may use this mode when their interaction feedback is represented by direct geometry changes.
    ///
    /// @param interactionFeedbackEnabled whether hover overlays and ripples are rendered
    M3StateLayer(boolean interactionFeedbackEnabled) {
        this.interactionFeedbackEnabled = interactionFeedbackEnabled;
        getStyleClass().add(STYLE_CLASS);
        containerPaintLayer.getStyleClass().add(CONTAINER_PAINT_STYLE_CLASS);
        overlay.getStyleClass().add(OVERLAY_STYLE_CLASS);
        ripple.getStyleClass().add(RIPPLE_STYLE_CLASS);
        focusIndicator.getStyleClass().add(FOCUS_INDICATOR_STYLE_CLASS);
        setMouseTransparent(true);
        setManaged(false);
        containerPaintLayer.setManaged(false);
        overlay.setManaged(false);
        ripple.setManaged(false);
        focusIndicator.setManaged(false);
        containerPaintLayer.setMouseTransparent(true);
        overlay.setMouseTransparent(true);
        ripple.setMouseTransparent(true);
        focusIndicator.setMouseTransparent(true);
        containerPaintLayer.setVisible(false);
        overlay.setOpacity(0.0);
        ripple.setOpacity(0.0);
        focusIndicator.setOpacity(0.0);
        focusIndicator.setVisible(false);
        rippleAnimation.setOnFinished(animationFinishedHandler);
        stateOpacityAnimation.setOnFinished(animationFinishedHandler);
        Group clippedContent = new Group(overlay, ripple);
        clippedContent.setAutoSizeChildren(false);
        clippedContent.setManaged(false);
        clippedContent.setMouseTransparent(true);
        clip.setFill(Color.BLACK);
        clip.getElements().addAll(
                clipStart,
                clipTopEdge,
                clipTopRightArc,
                clipRightEdge,
                clipBottomRightArc,
                clipBottomEdge,
                clipBottomLeftArc,
                clipLeftEdge,
                clipTopLeftArc,
                new ClosePath()
        );
        clippedContent.setClip(clip);
        getChildren().addAll(containerPaintLayer, clippedContent, focusIndicator);
    }

    /// Installs opacity transitions driven by the owner node's interaction states.
    void installStateTransitions(Node owner) {
        if (stateOwner == owner) {
            return;
        }
        uninstallStateTransitions();
        stateOwner = owner;
        focusVisibleTracker = new M3FocusVisibleTracker(owner, this::animateOverlayOpacityFromOwnerState);
        focusVisibleTracker.install();
        owner.disabledProperty().addListener(disabledStateListener);
        owner.sceneProperty().addListener(ownerSceneListener);
        owner.getPseudoClassStates().addListener(pseudoClassStateListener);
        motionSettingsObserver = new M3MotionSettingsObserver(owner, this::refreshMotionSettings, false);
        if (owner instanceof ButtonBase button) {
            owner.pseudoClassStateChanged(ARMED_PSEUDO_CLASS, button.isArmed());
            button.armedProperty().addListener(buttonArmedStateListener);
        }
        synchronizeOwnerStateOpacity(owner);
    }

    /// Synchronizes opacity with owner interaction states without starting an initial transition.
    private void synchronizeOwnerStateOpacity(Node owner) {
        stateOpacityAnimation.stop();
        overlay.setOpacity(resolvedOverlayOpacity(owner));
        setFocusIndicatorOpacity(focusIndicator, resolvedFocusIndicatorOpacity(owner));
    }

    /// Removes opacity transition listeners from the current owner.
    void uninstallStateTransitions() {
        Node owner = stateOwner;
        if (owner == null) {
            return;
        }

        owner.disabledProperty().removeListener(disabledStateListener);
        owner.sceneProperty().removeListener(ownerSceneListener);
        owner.getPseudoClassStates().removeListener(pseudoClassStateListener);
        M3MotionSettingsObserver observer = motionSettingsObserver;
        if (observer != null) {
            observer.dispose();
            motionSettingsObserver = null;
        }
        if (owner instanceof ButtonBase button) {
            button.armedProperty().removeListener(buttonArmedStateListener);
            owner.pseudoClassStateChanged(ARMED_PSEUDO_CLASS, false);
        }
        M3FocusVisibleTracker tracker = focusVisibleTracker;
        if (tracker != null) {
            tracker.uninstall();
            focusVisibleTracker = null;
        }
        stateOwner = null;
        restingOverlayOpacity = 0.0;
        delegatedFocusVisible = false;
        cachedAnimationsEnabled = false;
        motionSettingsRevision = Long.MIN_VALUE;
        stateOpacityAnimation.stop();
        setFocusIndicatorOpacity(focusIndicator, 0.0);
        rippleAnimation.stop();
        clearRipple();
    }

    /// Sets whether a child node delegates keyboard-visible focus feedback to this state layer.
    ///
    /// Text-entry controls use this channel when keyboard focus is held by an embedded editor rather than the
    /// outer Material component. The delegated state is combined with the owner's own focus-visible state.
    void setDelegatedFocusVisible(boolean delegatedFocusVisible) {
        if (this.delegatedFocusVisible == delegatedFocusVisible) {
            return;
        }
        this.delegatedFocusVisible = delegatedFocusVisible;
        animateOverlayOpacityFromOwnerState();
    }

    /// Lays out the state layer within the skinnable component.
    void layoutLayer(double x, double y, double width, double height, double shapeRadius) {
        layoutLayer(x, y, width, height, shapeRadius, shapeRadius, shapeRadius, shapeRadius);
    }

    /// Lays out the state layer with independent corner radii.
    void layoutLayer(
            double x,
            double y,
            double width,
            double height,
            double topLeftRadius,
            double topRightRadius,
            double bottomRightRadius,
            double bottomLeftRadius
    ) {
        double topLeft = resolvedShapeRadius(width, height, topLeftRadius);
        double topRight = resolvedShapeRadius(width, height, topRightRadius);
        double bottomRight = resolvedShapeRadius(width, height, bottomRightRadius);
        double bottomLeft = resolvedShapeRadius(width, height, bottomLeftRadius);
        resizeRelocate(x, y, width, height);
        containerPaintLayer.resizeRelocate(0.0, 0.0, width, height);
        overlay.resizeRelocate(0.0, 0.0, width, height);
        updateFocusIndicatorShape(0.0, 0.0, width, height, topLeft, topRight, bottomRight, bottomLeft);
        updateClip(width, height, topLeft, topRight, bottomRight, bottomLeft);
    }

    /// Lays out the keyboard focus indicator independently from the bounded overlay and ripple geometry.
    ///
    /// The coordinates are relative to this state layer. Components whose focus outline follows a larger visual
    /// container can keep the interaction overlay bounded to its own state-layer token while outlining that
    /// container.
    void layoutFocusIndicator(double x, double y, double width, double height, double shapeRadius) {
        double radius = resolvedShapeRadius(width, height, shapeRadius);
        updateFocusIndicatorShape(x, y, width, height, radius, radius, radius, radius);
    }

    /// Plays a bounded ripple from a point in this state layer's coordinate space.
    void playRipple(double x, double y) {
        if (!interactionFeedbackEnabled) {
            clearRipple();
            return;
        }
        Node owner = animationOwner();
        if (isPresentationUnavailable(owner) || animationsDisabled(owner)) {
            rippleAnimation.stop();
            clearRipple();
            return;
        }

        double width = getWidth();
        double height = getHeight();
        if (width <= 0.0 || height <= 0.0) {
            return;
        }

        double diameter = rippleDiameter(x, y, width, height);
        rippleAnimation.stop();
        ripple.resizeRelocate(x - diameter / 2.0, y - diameter / 2.0, diameter, diameter);
        ripple.setScaleX(0.0);
        ripple.setScaleY(0.0);
        ripple.setOpacity(RIPPLE_START_OPACITY);
        M3MotionSpec rippleSpec = M3Animation.defaultSpatial(owner);
        rippleAnimation.configureExpansion(rippleSpec);
        startMotionObservation();
        rippleAnimation.playFromStart();
    }

    /// Plays a bounded ripple from the layer center.
    void playCenteredRipple() {
        playRipple(getWidth() / 2.0, getHeight() / 2.0);
    }

    /// Releases the active ripple and fades it out.
    void releaseRipple() {
        if (!interactionFeedbackEnabled) {
            clearRipple();
            return;
        }
        Node owner = animationOwner();
        if (isPresentationUnavailable(owner) || animationsDisabled(owner)) {
            rippleAnimation.stop();
            clearRipple();
            return;
        }

        double startOpacity = ripple.getOpacity();
        if (startOpacity <= 0.0) {
            return;
        }

        double startScaleX = ripple.getScaleX();
        double startScaleY = ripple.getScaleY();
        rippleAnimation.stop();
        ripple.setOpacity(startOpacity);
        ripple.setScaleX(startScaleX);
        ripple.setScaleY(startScaleY);

        M3MotionSpec expansionSpec = M3Animation.defaultSpatial(owner);
        M3MotionSpec fadeSpec = M3Animation.defaultEffects(owner);
        double remainingExpansionMillis = remainingRippleExpansionMillis(
                expansionSpec.duration(),
                Math.max(startScaleX, startScaleY)
        );
        rippleAnimation.configureRelease(
                startScaleX,
                startScaleY,
                startOpacity,
                expansionSpec,
                remainingExpansionMillis,
                fadeSpec
        );
        startMotionObservation();
        rippleAnimation.playFromStart();
    }

    /// Stops active ripple work and clears its visual state without changing persistent overlay state.
    void cancelRipple() {
        rippleAnimation.stop();
        clearRipple();
        stopMotionObservationIfIdle();
    }

    /// Mirrors a pseudo-class to the overlay and ripple nodes.
    void setContentPseudoClass(PseudoClass pseudoClass, boolean active) {
        overlay.pseudoClassStateChanged(pseudoClass, active);
        ripple.pseudoClassStateChanged(pseudoClass, active);
    }

    /// Applies an optional concrete paint beneath this layer's interaction feedback.
    ///
    /// The supplied radii and insets mirror the owning control's current background geometry. A `null` value
    /// removes the container layer and leaves the owning control's normal CSS background visible.
    ///
    /// @param paint  the container paint, or `null` to remove the concrete container layer
    /// @param radii  the corner radii of the owning control's current background
    /// @param insets the insets of the owning control's current background
    void setContainerPaint(@Nullable Paint paint, CornerRadii radii, Insets insets) {
        if (paint == null) {
            containerPaintLayer.setBackground(null);
            containerPaintLayer.setVisible(false);
            return;
        }
        Background background = containerPaintLayer.getBackground();
        if (background != null
                && background.getFills().size() == 1
                && paint.equals(background.getFills().get(0).getFill())
                && radii.equals(background.getFills().get(0).getRadii())
                && insets.equals(background.getFills().get(0).getInsets())) {
            containerPaintLayer.setVisible(true);
            return;
        }
        containerPaintLayer.setBackground(new Background(
                new BackgroundFill(paint, radii, insets)
        ));
        containerPaintLayer.setVisible(true);
    }

    /// Applies a concrete content paint to the persistent overlay and ripple.
    ///
    /// This is intended for popup-owned controls whose token lookup ancestry can disappear before JavaFX completes
    /// a CSS pulse. The concrete paint preserves the last resolved theme color without retaining the popup owner.
    void setContentPaint(Paint paint) {
        Paint currentPaint = contentPaint;
        if (paint.equals(currentPaint)
                && hasSingleBackgroundFill(overlay, paint)
                && hasSingleBackgroundFill(ripple, paint)) {
            return;
        }
        contentPaint = paint;
        if (!overlay.getStyle().isEmpty()) {
            overlay.setStyle("");
        }
        if (!ripple.getStyle().isEmpty()) {
            ripple.setStyle("");
        }
        updateContentBackgrounds();
    }

    /// Returns whether a region has exactly one background fill with the requested paint.
    private static boolean hasSingleBackgroundFill(Region region, Paint paint) {
        Background background = region.getBackground();
        return background != null
                && background.getFills().size() == 1
                && paint.equals(background.getFills().get(0).getFill());
    }

    /// Sets the resting overlay opacity contributed by a persistent semantic state.
    ///
    /// Interaction opacity is composited over this value so hover, focus, and press feedback remain visible.
    void setRestingOverlayOpacity(double opacity) {
        double resolvedOpacity = Math.max(0.0, Math.min(1.0, opacity));
        if (Double.compare(restingOverlayOpacity, resolvedOpacity) == 0) {
            return;
        }
        restingOverlayOpacity = resolvedOpacity;
        animateOverlayOpacityFromOwnerState();
    }

    /// Stops ripple animation and clears transient ripple state.
    void reset() {
        stateOpacityAnimation.stop();
        restingOverlayOpacity = 0.0;
        overlay.setOpacity(0.0);
        setFocusIndicatorOpacity(focusIndicator, 0.0);
        cancelRipple();
    }

    /// Clears transient ripple visual state.
    private void clearRipple() {
        ripple.setOpacity(0.0);
        ripple.setScaleX(0.0);
        ripple.setScaleY(0.0);
    }

    /// Applies focus-indicator opacity while excluding fully transparent rings from visual bounds and rendering.
    private static void setFocusIndicatorOpacity(Region indicator, double opacity) {
        indicator.setOpacity(opacity);
        indicator.setVisible(opacity > 0.0);
    }

    /// Returns whether the overlay opacity is currently animating.
    boolean isOverlayOpacityAnimationRunning() {
        return stateOpacityAnimation.isOverlayAnimating();
    }

    /// Returns whether the ripple is currently animating.
    boolean isRippleAnimationRunning() {
        return rippleAnimation.getStatus() == Animation.Status.RUNNING;
    }

    /// Returns whether the focus indicator is currently animating.
    boolean isFocusIndicatorOpacityAnimationRunning() {
        return stateOpacityAnimation.isFocusIndicatorAnimating();
    }

    /// Applies changed animation settings to currently running state-layer animations.
    private void refreshMotionSettings() {
        Node owner = stateOwner;
        if (owner == null) {
            return;
        }

        if (isPresentationUnavailable(owner) || animationsDisabled(owner)) {
            if (stateOpacityAnimation.getStatus() == Animation.Status.RUNNING) {
                stateOpacityAnimation.stop();
                overlay.setOpacity(resolvedOverlayOpacity(owner));
                setFocusIndicatorOpacity(focusIndicator, resolvedFocusIndicatorOpacity(owner));
            }
            if (rippleAnimation.getStatus() == Animation.Status.RUNNING) {
                rippleAnimation.stop();
                clearRipple();
            }
            stopMotionObservationIfIdle();
            return;
        }

        if (stateOpacityAnimation.getStatus() == Animation.Status.RUNNING) {
            animateOverlayOpacityFromOwnerState();
        }
    }

    /// Animates persistent state-layer opacity channels to values resolved from the owner state.
    void animateOverlayOpacityFromOwnerState() {
        Node owner = stateOwner;
        if (owner == null) {
            return;
        }

        double startOverlayOpacity = overlay.getOpacity();
        double targetOverlayOpacity = resolvedOverlayOpacity(owner);
        double startFocusIndicatorOpacity = focusIndicator.getOpacity();
        double targetFocusIndicatorOpacity = resolvedFocusIndicatorOpacity(owner);
        stateOpacityAnimation.stop();

        boolean overlayChanged = Double.compare(startOverlayOpacity, targetOverlayOpacity) != 0;
        boolean focusIndicatorChanged =
                Double.compare(startFocusIndicatorOpacity, targetFocusIndicatorOpacity) != 0;
        if (!overlayChanged && !focusIndicatorChanged) {
            overlay.setOpacity(targetOverlayOpacity);
            setFocusIndicatorOpacity(focusIndicator, targetFocusIndicatorOpacity);
            return;
        }

        if (isPresentationUnavailable(owner) || animationsDisabled(owner)) {
            overlay.setOpacity(targetOverlayOpacity);
            setFocusIndicatorOpacity(focusIndicator, targetFocusIndicatorOpacity);
            return;
        }

        M3MotionSpec opacitySpec = M3Animation.fastEffects(owner);
        stateOpacityAnimation.configure(
                opacitySpec,
                startOverlayOpacity,
                targetOverlayOpacity,
                startFocusIndicatorOpacity,
                targetFocusIndicatorOpacity
        );
        startMotionObservation();
        stateOpacityAnimation.playFromStart();
    }

    /// Starts observing settings while at least one state-layer animation is active.
    private void startMotionObservation() {
        M3MotionSettingsObserver observer = motionSettingsObserver;
        if (observer != null) {
            observer.start();
        }
    }

    /// Pauses settings observation after both reusable transitions become idle.
    private void stopMotionObservationIfIdle() {
        if (rippleAnimation.getStatus() == Animation.Status.RUNNING
                || stateOpacityAnimation.getStatus() == Animation.Status.RUNNING) {
            return;
        }
        M3MotionSettingsObserver observer = motionSettingsObserver;
        if (observer != null) {
            observer.stop();
        }
    }

    /// Returns whether inherited animations are disabled, refreshing the cache after any settings change.
    private boolean animationsDisabled(Node owner) {
        long revision = M3MotionSettingsObserver.reducedMotionRevision();
        if (motionSettingsRevision != revision) {
            cachedAnimationsEnabled = M3Animation.areAnimationsEnabled(owner);
            motionSettingsRevision = revision;
        }
        return !cachedAnimationsEnabled;
    }

    /// Returns whether the owner cannot currently receive rendered pulses.
    private static boolean isPresentationUnavailable(Node owner) {
        @Nullable Scene scene = owner.getScene();
        if (scene == null) {
            return true;
        }
        @Nullable Window window = scene.getWindow();
        return window != null && !window.isShowing();
    }

    /// Returns the target overlay opacity for the owner interaction state.
    private double resolvedOverlayOpacity(Node owner) {
        if (!interactionFeedbackEnabled || owner.isDisabled()) {
            return 0.0;
        }
        M3StateLayerTokens tokens = stateLayerTokens(owner);
        double interactionOpacity;
        if (owner.getPseudoClassStates().contains(DRAGGED_PSEUDO_CLASS)) {
            interactionOpacity = tokens.draggedOpacity();
        } else if (isPressedLike(owner)) {
            interactionOpacity = tokens.pressedOpacity();
        } else if (delegatedFocusVisible
                || owner.getPseudoClassStates().contains(FOCUS_VISIBLE_PSEUDO_CLASS)) {
            interactionOpacity = tokens.focusOpacity();
        } else if (owner.isHover() || owner.getPseudoClassStates().contains(HOVER_PSEUDO_CLASS)) {
            interactionOpacity = tokens.hoverOpacity();
        } else {
            interactionOpacity = 0.0;
        }
        return compositeOpacity(restingOverlayOpacity, interactionOpacity);
    }

    /// Alpha-composites an interaction state layer over a persistent state layer of the same color.
    private static double compositeOpacity(double backgroundOpacity, double foregroundOpacity) {
        return 1.0 - (1.0 - backgroundOpacity) * (1.0 - foregroundOpacity);
    }

    /// Returns the target focus indicator opacity for the owner interaction state.
    private double resolvedFocusIndicatorOpacity(Node owner) {
        if (owner.isDisabled()) {
            return 0.0;
        }
        return delegatedFocusVisible
                || owner.getPseudoClassStates().contains(FOCUS_VISIBLE_PSEUDO_CLASS) ? 1.0 : 0.0;
    }

    /// Returns the state layer tokens for the owner node.
    private static M3StateLayerTokens stateLayerTokens(Node owner) {
        @Nullable M3Theme theme = M3ThemeResolver.findTheme(owner);
        return theme == null ? FALLBACK_TOKENS : theme.tokens().stateLayerTokens();
    }

    /// Returns whether the owner should show pressed-state feedback.
    private static boolean isPressedLike(Node owner) {
        if (owner.isPressed() || owner.getPseudoClassStates().contains(PRESSED_PSEUDO_CLASS)) {
            return true;
        }
        return owner.getPseudoClassStates().contains(ARMED_PSEUDO_CLASS)
                || owner instanceof ButtonBase button && button.isArmed();
    }

    /// Returns the node whose motion setting controls this state layer.
    private Node animationOwner() {
        @Nullable Node owner = stateOwner;
        return owner == null ? this : owner;
    }

    /// A reusable transition for persistent state-layer opacity channels.
    @NotNullByDefault
    private static final class StateOpacityTransition extends Transition {
        /// The persistent interaction overlay.
        private final Region overlay;

        /// The keyboard focus indicator.
        private final Region focusIndicator;

        /// The overlay opacity at the beginning of the current transition.
        private double startOverlayOpacity;

        /// The overlay opacity at the end of the current transition.
        private double targetOverlayOpacity;

        /// The focus-indicator opacity at the beginning of the current transition.
        private double startFocusIndicatorOpacity;

        /// The focus-indicator opacity at the end of the current transition.
        private double targetFocusIndicatorOpacity;

        /// Whether the overlay channel changes during the current transition.
        private boolean overlayAnimating;

        /// Whether the focus-indicator channel changes during the current transition.
        private boolean focusIndicatorAnimating;

        /// Creates an opacity transition for the persistent state-layer regions.
        private StateOpacityTransition(Region overlay, Region focusIndicator) {
            this.overlay = overlay;
            this.focusIndicator = focusIndicator;
        }

        /// Reconfigures both channels without replacing the animation graph.
        private void configure(
                M3MotionSpec spec,
                double startOverlayOpacity,
                double targetOverlayOpacity,
                double startFocusIndicatorOpacity,
                double targetFocusIndicatorOpacity
        ) {
            stop();
            setCycleDuration(spec.duration());
            setInterpolator(spec.interpolator());
            this.startOverlayOpacity = startOverlayOpacity;
            this.targetOverlayOpacity = targetOverlayOpacity;
            this.startFocusIndicatorOpacity = startFocusIndicatorOpacity;
            this.targetFocusIndicatorOpacity = targetFocusIndicatorOpacity;
            overlayAnimating = Double.compare(startOverlayOpacity, targetOverlayOpacity) != 0;
            focusIndicatorAnimating =
                    Double.compare(startFocusIndicatorOpacity, targetFocusIndicatorOpacity) != 0;
            focusIndicator.setVisible(startFocusIndicatorOpacity > 0.0 || targetFocusIndicatorOpacity > 0.0);
        }

        /// Returns whether the overlay channel is participating in a running transition.
        private boolean isOverlayAnimating() {
            return overlayAnimating && getStatus() == Animation.Status.RUNNING;
        }

        /// Returns whether the focus-indicator channel is participating in a running transition.
        private boolean isFocusIndicatorAnimating() {
            return focusIndicatorAnimating && getStatus() == Animation.Status.RUNNING;
        }

        /// Applies the eased opacity values for the current pulse.
        @Override
        protected void interpolate(double fraction) {
            if (overlayAnimating) {
                overlay.setOpacity(linearInterpolate(
                        startOverlayOpacity,
                        targetOverlayOpacity,
                        fraction
                ));
            }
            if (focusIndicatorAnimating) {
                double opacity = linearInterpolate(
                        startFocusIndicatorOpacity,
                        targetFocusIndicatorOpacity,
                        fraction
                );
                focusIndicator.setOpacity(opacity);
                if (fraction >= 1.0 && targetFocusIndicatorOpacity <= 0.0) {
                    focusIndicator.setVisible(false);
                }
            }
        }
    }

    /// A reusable transition for ripple press expansion and release fading.
    @NotNullByDefault
    private static final class RippleTransition extends Transition {
        /// The ripple region animated by this transition.
        private final Region ripple;

        /// The horizontal scale at the beginning of the current transition.
        private double startScaleX;

        /// The vertical scale at the beginning of the current transition.
        private double startScaleY;

        /// The opacity at the beginning of the current transition.
        private double startOpacity;

        /// The opacity at the end of the current transition.
        private double targetOpacity;

        /// The duration over which scale reaches its final value.
        private double expansionDurationMillis;

        /// The duration represented by the complete transition.
        private double totalDurationMillis;

        /// The easing curve used for scale expansion.
        private Interpolator expansionInterpolator = Interpolator.LINEAR;

        /// The easing curve used for opacity fading.
        private Interpolator opacityInterpolator = Interpolator.LINEAR;

        /// Creates a reusable transition for a ripple region.
        private RippleTransition(Region ripple) {
            this.ripple = ripple;
            setInterpolator(Interpolator.LINEAR);
        }

        /// Configures press expansion while retaining a constant visible opacity.
        private void configureExpansion(M3MotionSpec expansionSpec) {
            stop();
            Duration duration = expansionSpec.duration();
            setCycleDuration(duration);
            startScaleX = 0.0;
            startScaleY = 0.0;
            startOpacity = RIPPLE_START_OPACITY;
            targetOpacity = RIPPLE_START_OPACITY;
            expansionDurationMillis = duration.toMillis();
            totalDurationMillis = expansionDurationMillis;
            expansionInterpolator = expansionSpec.interpolator();
            opacityInterpolator = Interpolator.LINEAR;
        }

        /// Configures a release that keeps unfinished expansion and fading on independent easing curves.
        private void configureRelease(
                double startScaleX,
                double startScaleY,
                double startOpacity,
                M3MotionSpec expansionSpec,
                double remainingExpansionMillis,
                M3MotionSpec fadeSpec
        ) {
            stop();
            double fadeDurationMillis = fadeSpec.duration().toMillis();
            double totalDurationMillis = Math.max(remainingExpansionMillis, fadeDurationMillis);
            setCycleDuration(Duration.millis(totalDurationMillis));
            this.startScaleX = startScaleX;
            this.startScaleY = startScaleY;
            this.startOpacity = startOpacity;
            targetOpacity = 0.0;
            this.totalDurationMillis = totalDurationMillis;
            if (remainingExpansionMillis > 0.0) {
                expansionDurationMillis = remainingExpansionMillis;
                expansionInterpolator = expansionSpec.interpolator();
            } else {
                expansionDurationMillis = fadeDurationMillis;
                expansionInterpolator = fadeSpec.interpolator();
            }
            opacityInterpolator = fadeSpec.interpolator();
        }

        /// Applies independently eased scale and opacity values for the current pulse.
        @Override
        protected void interpolate(double fraction) {
            double elapsedMillis = totalDurationMillis * fraction;
            double expansionFraction = elapsedFraction(elapsedMillis, expansionDurationMillis);
            double opacityFraction = elapsedFraction(elapsedMillis, totalDurationMillis);
            ripple.setScaleX(expansionInterpolator.interpolate(startScaleX, 1.0, expansionFraction));
            ripple.setScaleY(expansionInterpolator.interpolate(startScaleY, 1.0, expansionFraction));
            ripple.setOpacity(opacityInterpolator.interpolate(startOpacity, targetOpacity, opacityFraction));
        }
    }

    /// Computes the ripple diameter needed to cover this layer from an origin point.
    private static double rippleDiameter(double x, double y, double width, double height) {
        double right = width - x;
        double bottom = height - y;
        double radius = Math.hypot(Math.max(x, right), Math.max(y, bottom));
        return radius * 2.0;
    }

    /// Returns the remaining expansion time for a ripple released before it reaches full size.
    private static double remainingRippleExpansionMillis(Duration fullDuration, double currentScale) {
        double clampedScale = Math.max(0.0, Math.min(1.0, currentScale));
        return fullDuration.toMillis() * (1.0 - clampedScale);
    }

    /// Returns elapsed progress clamped to the closed unit interval.
    private static double elapsedFraction(double elapsedMillis, double durationMillis) {
        if (durationMillis <= 0.0) {
            return 1.0;
        }
        return Math.max(0.0, Math.min(1.0, elapsedMillis / durationMillis));
    }

    /// Interpolates linearly between two scalar values.
    private static double linearInterpolate(double start, double end, double fraction) {
        return start + (end - start) * fraction;
    }

    /// Resolves a token radius to a radius that can be represented within the current bounds.
    private static double resolvedShapeRadius(double width, double height, double shapeRadius) {
        double maximumRadius = Math.max(0.0, Math.min(width, height) / 2.0);
        return Math.min(Math.max(0.0, shapeRadius), maximumRadius);
    }

    /// Updates concrete overlay and ripple backgrounds after their paint changes.
    private void updateContentBackgrounds() {
        Paint paint = contentPaint;
        if (paint == null) {
            return;
        }
        overlay.setBackground(new Background(
                new BackgroundFill(paint, CornerRadii.EMPTY, Insets.EMPTY)
        ));
        ripple.setBackground(new Background(
                new BackgroundFill(paint, new CornerRadii(999.0), Insets.EMPTY)
        ));
    }

    /// Updates the keyboard focus indicator ring to follow the component shape and focus offset token.
    private void updateFocusIndicatorShape(
            double x,
            double y,
            double width,
            double height,
            double topLeft,
            double topRight,
            double bottomRight,
            double bottomLeft
    ) {
        Node owner = stateOwner;
        @Nullable M3Theme theme = owner == null ? null : M3ThemeResolver.findTheme(owner);
        M3StateLayerTokens tokens = theme == null ? FALLBACK_TOKENS : theme.tokens().stateLayerTokens();
        double offset = owner != null && usesInnerFocusIndicatorOffset(owner)
                ? tokens.focusIndicatorInnerOffset()
                : tokens.focusIndicatorOuterOffset();
        double thickness = tokens.focusIndicatorThickness();
        double outwardExpansion = offset > 0.0 ? offset + thickness : 0.0;
        double inwardOffset = Math.max(0.0, -offset);
        double indicatorWidth = width + outwardExpansion * 2.0;
        double indicatorHeight = height + outwardExpansion * 2.0;
        double adjustedTopLeft = adjustedIndicatorRadius(topLeft, outwardExpansion - inwardOffset);
        double adjustedTopRight = adjustedIndicatorRadius(topRight, outwardExpansion - inwardOffset);
        double adjustedBottomRight = adjustedIndicatorRadius(bottomRight, outwardExpansion - inwardOffset);
        double adjustedBottomLeft = adjustedIndicatorRadius(bottomLeft, outwardExpansion - inwardOffset);
        focusIndicator.resizeRelocate(
                x - outwardExpansion,
                y - outwardExpansion,
                indicatorWidth,
                indicatorHeight
        );
        if (Double.compare(focusIndicatorWidth, indicatorWidth) == 0
                && Double.compare(focusIndicatorHeight, indicatorHeight) == 0
                && Double.compare(focusIndicatorTopLeftRadius, adjustedTopLeft) == 0
                && Double.compare(focusIndicatorTopRightRadius, adjustedTopRight) == 0
                && Double.compare(focusIndicatorBottomRightRadius, adjustedBottomRight) == 0
                && Double.compare(focusIndicatorBottomLeftRadius, adjustedBottomLeft) == 0
                && Double.compare(focusIndicatorInset, inwardOffset) == 0
                && Double.compare(focusIndicatorThickness, thickness) == 0) {
            return;
        }

        focusIndicatorWidth = indicatorWidth;
        focusIndicatorHeight = indicatorHeight;
        focusIndicatorTopLeftRadius = adjustedTopLeft;
        focusIndicatorTopRightRadius = adjustedTopRight;
        focusIndicatorBottomRightRadius = adjustedBottomRight;
        focusIndicatorBottomLeftRadius = adjustedBottomLeft;
        focusIndicatorInset = inwardOffset;
        focusIndicatorThickness = thickness;
        focusIndicator.setStyle("-fx-background-radius: "
                + formatPixels(adjustedTopLeft) + " "
                + formatPixels(adjustedTopRight) + " "
                + formatPixels(adjustedBottomRight) + " "
                + formatPixels(adjustedBottomLeft) + "; "
                + "-fx-border-insets: " + formatPixels(inwardOffset) + "; "
                + "-fx-border-width: " + formatPixels(thickness) + "; "
                + "-fx-border-radius: "
                + formatPixels(adjustedTopLeft) + " "
                + formatPixels(adjustedTopRight) + " "
                + formatPixels(adjustedBottomRight) + " "
                + formatPixels(adjustedBottomLeft) + ";");
    }

    /// Returns whether the owner uses an inner focus indicator offset in Material component tokens.
    private static boolean usesInnerFocusIndicatorOffset(Node owner) {
        return owner.getStyleClass().contains("m3-list-item")
                || owner.getStyleClass().contains("m3-menu-item")
                || owner.getStyleClass().contains("m3-navigation-item")
                || owner.getStyleClass().contains("m3-tab");
    }

    /// Adjusts a rounded corner for an inner or outer focus indicator offset.
    private static double adjustedIndicatorRadius(double radius, double offset) {
        return Math.max(0.0, radius + offset);
    }

    /// Updates the clip path to match the resolved rounded rectangle shape.
    private void updateClip(double width, double height, double topLeft, double topRight, double bottomRight, double bottomLeft) {
        if (Double.compare(clipWidth, width) == 0
                && Double.compare(clipHeight, height) == 0
                && Double.compare(clipTopLeftRadius, topLeft) == 0
                && Double.compare(clipTopRightRadius, topRight) == 0
                && Double.compare(clipBottomRightRadius, bottomRight) == 0
                && Double.compare(clipBottomLeftRadius, bottomLeft) == 0) {
            return;
        }

        clipWidth = width;
        clipHeight = height;
        clipTopLeftRadius = topLeft;
        clipTopRightRadius = topRight;
        clipBottomRightRadius = bottomRight;
        clipBottomLeftRadius = bottomLeft;
        clipStart.setX(topLeft);
        clipStart.setY(0.0);
        clipTopEdge.setX(width - topRight);
        clipTopEdge.setY(0.0);
        updateClipCorner(
                CLIP_TOP_RIGHT_CORNER_INDEX,
                clipTopRightArc,
                clipTopRightLine,
                topRight,
                width,
                topRight
        );
        clipRightEdge.setX(width);
        clipRightEdge.setY(height - bottomRight);
        updateClipCorner(
                CLIP_BOTTOM_RIGHT_CORNER_INDEX,
                clipBottomRightArc,
                clipBottomRightLine,
                bottomRight,
                width - bottomRight,
                height
        );
        clipBottomEdge.setX(bottomLeft);
        clipBottomEdge.setY(height);
        updateClipCorner(
                CLIP_BOTTOM_LEFT_CORNER_INDEX,
                clipBottomLeftArc,
                clipBottomLeftLine,
                bottomLeft,
                0.0,
                height - bottomLeft
        );
        clipLeftEdge.setX(0.0);
        clipLeftEdge.setY(topLeft);
        updateClipCorner(
                CLIP_TOP_LEFT_CORNER_INDEX,
                clipTopLeftArc,
                clipTopLeftLine,
                topLeft,
                topLeft,
                0.0
        );
    }

    /// Updates one reusable clip corner and selects its rounded or square path element.
    private void updateClipCorner(int index, ArcTo arc, LineTo line, double radius, double x, double y) {
        arc.setRadiusX(radius);
        arc.setRadiusY(radius);
        arc.setX(x);
        arc.setY(y);
        arc.setSweepFlag(true);
        line.setX(x);
        line.setY(y);
        PathElement target = radius <= 0.0 ? line : arc;
        if (clip.getElements().get(index) != target) {
            clip.getElements().set(index, target);
        }
    }

    /// Formats a CSS pixel value.
    private static String formatPixels(double value) {
        return value + "px";
    }
}
