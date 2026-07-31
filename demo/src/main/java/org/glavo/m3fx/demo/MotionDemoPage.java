// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.animation.M3AnimatedContent;
import org.glavo.m3fx.animation.M3AnimatedVisibility;
import org.glavo.m3fx.animation.M3ContentTransform;
import org.glavo.m3fx.animation.M3DoubleAnimatable;
import org.glavo.m3fx.animation.M3EnterTransition;
import org.glavo.m3fx.animation.M3ExitTransition;
import org.glavo.m3fx.animation.M3LayoutTransition;
import org.glavo.m3fx.animation.M3StateTransition;
import org.glavo.m3fx.animation.M3TransitionAxis;
import org.glavo.m3fx.animation.M3TransitionEdge;
import org.glavo.m3fx.animation.M3VectorConverters;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Slider;
import org.glavo.m3fx.controls.M3Surface;
import org.glavo.m3fx.layout.M3AdaptiveScaffold;
import org.glavo.m3fx.layout.M3NavigationLayout;
import org.glavo.m3fx.layout.M3PaneLayout;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

/// Builds the Motion component showcase page.
@NotNullByDefault
final class MotionDemoPage extends DemoPageSupport {
    /// Creates a page builder backed by the shared demo context.
    ///
    /// @param context the application-level actions available to interactive samples
    MotionDemoPage(DemoPageContext context) {
        super(context);
    }

    /// Creates the reusable Material motion API demo page.
    Node createContent() {
        M3Button movingTarget = new M3Button("Moving target", M3ButtonVariant.TONAL);
        M3DoubleAnimatable horizontalPosition = new M3DoubleAnimatable(
                movingTarget,
                movingTarget.translateXProperty(),
                0.5
        );
        StackPane valueTrack = new StackPane(movingTarget);
        valueTrack.getStyleClass().add("demo-flow");
        valueTrack.setAlignment(Pos.CENTER_LEFT);
        valueTrack.setMinHeight(96.0);
        configureResponsiveWidth(valueTrack, 560.0);

        M3Button moveButton = new M3Button("Change target", M3ButtonVariant.FILLED);
        moveButton.setOnAction(event -> {
            double travel = availableHorizontalTravel(valueTrack, movingTarget);
            horizontalPosition.animateTo(horizontalPosition.getTargetValue() < travel / 2.0 ? travel : 0.0);
        });
        M3Button snapButton = new M3Button("Snap to start", M3ButtonVariant.OUTLINED);
        snapButton.setOnAction(event -> horizontalPosition.snapTo(0.0));
        FlowPane valueActions = createResponsiveActionRow(moveButton, snapButton);
        VBox valueExample = new VBox(12.0, valueTrack, valueActions);
        configureResponsiveWidth(valueExample, 560.0);

        Label coordinatedHeadline = new Label("One state, four visual channels");
        coordinatedHeadline.getStyleClass().add("demo-group-title");
        Label coordinatedSupporting = new Label("Position, scale, and opacity share one interruptible transition.");
        M3Surface coordinatedSurface = new M3Surface();
        coordinatedSurface.setPrefSize(220.0, 88.0);
        coordinatedSurface.setMaxSize(220.0, 88.0);
        coordinatedSurface.setMinWidth(0.0);
        coordinatedSurface.getContent().add(new VBox(6.0, coordinatedHeadline, coordinatedSupporting));

        StackPane coordinatedTrack = new StackPane(coordinatedSurface);
        coordinatedTrack.getStyleClass().add("demo-flow");
        coordinatedTrack.setAlignment(Pos.CENTER_LEFT);
        coordinatedTrack.setMinHeight(128.0);
        configureResponsiveWidth(coordinatedTrack, 560.0);

        M3StateTransition<Boolean> stateTransition = new M3StateTransition<>(coordinatedSurface, false);
        stateTransition.addDouble(
                coordinatedSurface.translateXProperty(),
                expanded -> expanded ? availableHorizontalTravel(coordinatedTrack, coordinatedSurface) : 0.0,
                0.5
        );
        stateTransition.addDouble(
                coordinatedSurface.scaleXProperty(),
                expanded -> expanded ? 1.08 : 1.0,
                5.0e-4
        );
        stateTransition.addDouble(
                coordinatedSurface.scaleYProperty(),
                expanded -> expanded ? 1.08 : 1.0,
                5.0e-4
        );
        stateTransition.addDouble(
                coordinatedSurface.opacityProperty(),
                expanded -> expanded ? 1.0 : 0.72,
                0.01
        );

        Label stateLabel = new Label("Current: compact / target: compact");
        Runnable updateStateLabel = () -> stateLabel.setText(
                "Current: " + (stateTransition.getCurrentState() ? "expanded" : "compact")
                        + " / target: " + (stateTransition.getTargetState() ? "expanded" : "compact")
        );
        stateTransition.currentStateProperty().addListener(
                (observable, oldState, newState) -> updateStateLabel.run()
        );
        stateTransition.targetStateProperty().addListener(
                (observable, oldState, newState) -> updateStateLabel.run()
        );

        M3Button changeState = new M3Button("Change state", M3ButtonVariant.FILLED);
        changeState.setOnAction(event -> stateTransition.setTargetState(!stateTransition.getTargetState()));
        M3Button finishState = new M3Button("Finish transition", M3ButtonVariant.OUTLINED);
        finishState.setOnAction(event -> stateTransition.finish());
        FlowPane stateActions = createResponsiveActionRow(changeState, finishState, stateLabel);
        VBox stateExample = new VBox(12.0, coordinatedTrack, stateActions);
        configureResponsiveWidth(stateExample, 560.0);

        Label seekHeadline = new Label("Drag this transition in either direction");
        seekHeadline.getStyleClass().add("demo-group-title");
        seekHeadline.setWrapText(true);
        Label seekSupporting = new Label("Point2D, scale, and opacity use one normalized play-time position.");
        seekSupporting.setWrapText(true);
        M3Surface seekSurface = new M3Surface();
        seekSurface.setPrefSize(220.0, 88.0);
        seekSurface.setMaxSize(220.0, 88.0);
        seekSurface.setMinWidth(0.0);
        seekSurface.getContent().add(new VBox(6.0, seekHeadline, seekSupporting));

        StackPane seekTrack = new StackPane(seekSurface);
        seekTrack.getStyleClass().add("demo-flow");
        seekTrack.setAlignment(Pos.CENTER_LEFT);
        seekTrack.setMinHeight(128.0);
        configureResponsiveWidth(seekTrack, 560.0);

        SimpleObjectProperty<Point2D> seekPosition = new SimpleObjectProperty<>(Point2D.ZERO);
        seekPosition.addListener((observable, oldPosition, newPosition) -> {
            seekSurface.setTranslateX(newPosition.getX());
            seekSurface.setTranslateY(newPosition.getY());
        });
        M3StateTransition<Boolean> seekTransition = new M3StateTransition<>(seekSurface, false);
        seekTransition.addValue(
                seekPosition,
                expanded -> expanded ? new Point2D(availableHorizontalTravel(seekTrack, seekSurface), 0.0) : Point2D.ZERO,
                M3VectorConverters.POINT_2D
        );
        seekTransition.addDouble(
                seekSurface.scaleXProperty(),
                expanded -> expanded ? 1.08 : 1.0,
                5.0e-4
        );
        seekTransition.addDouble(
                seekSurface.scaleYProperty(),
                expanded -> expanded ? 1.08 : 1.0,
                5.0e-4
        );
        seekTransition.addDouble(
                seekSurface.opacityProperty(),
                expanded -> expanded ? 1.0 : 0.72,
                0.01
        );

        M3Slider seekSlider = new M3Slider(0.0, 1.0, 0.0);
        configureResponsiveWidth(seekSlider, 300.0);
        seekSlider.setBlockIncrement(0.1);
        AtomicBoolean synchronizingSeekSlider = new AtomicBoolean();
        seekSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!synchronizingSeekSlider.get()) {
                seekTransition.seekTo(true, newValue.doubleValue());
            }
        });
        seekTransition.progressProperty().addListener((observable, oldProgress, newProgress) -> {
            if (!seekTransition.isSeeking() && seekTransition.getTargetState()) {
                synchronizingSeekSlider.set(true);
                try {
                    seekSlider.setValue(newProgress.doubleValue());
                } finally {
                    synchronizingSeekSlider.set(false);
                }
            }
        });

        M3Button continueSeek = new M3Button("Continue", M3ButtonVariant.FILLED);
        continueSeek.disableProperty().bind(seekTransition.seekingProperty().not());
        continueSeek.setOnAction(event -> seekTransition.animateToTarget());
        M3Button resetSeek = new M3Button("Reset", M3ButtonVariant.OUTLINED);
        resetSeek.setOnAction(event -> {
            seekTransition.snapTo(false);
            synchronizingSeekSlider.set(true);
            try {
                seekSlider.setValue(0.0);
            } finally {
                synchronizingSeekSlider.set(false);
            }
        });
        Label seekProgress = new Label();
        seekProgress.textProperty().bind(
                seekTransition.progressProperty().multiply(100.0).asString("Progress: %.0f%%")
        );
        FlowPane seekActions = createResponsiveActionRow(seekSlider, continueSeek, resetSeek, seekProgress);
        VBox seekExample = new VBox(12.0, seekTrack, seekActions);
        configureResponsiveWidth(seekExample, 560.0);

        Label visibilityHeadline = new Label("Content remains mounted until exit completes");
        visibilityHeadline.getStyleClass().add("demo-group-title");
        Label visibilitySupporting = new Label(
                "Visibility state, visual effects, and container size remain interruptible without changing the node."
        );
        visibilitySupporting.setWrapText(true);
        M3Surface visibilitySurface = new M3Surface();
        configureResponsiveWidth(visibilitySurface, 520.0);
        visibilitySurface.getContent().add(new VBox(8.0, visibilityHeadline, visibilitySupporting));

        M3AnimatedVisibility animatedVisibility = new M3AnimatedVisibility(visibilitySurface);
        animatedVisibility.setFitToWidth(true);
        configureResponsiveWidth(animatedVisibility, 520.0);
        animatedVisibility.setEnterTransition(
                M3EnterTransition.fade(0.0)
                        .and(M3EnterTransition.scale(0.92))
                        .and(M3EnterTransition.expandIn(M3TransitionEdge.START, M3TransitionEdge.TOP))
        );
        animatedVisibility.setExitTransition(
                M3ExitTransition.fade(0.0)
                        .and(M3ExitTransition.scale(0.92))
                        .and(M3ExitTransition.shrinkOut(M3TransitionEdge.END, M3TransitionEdge.BOTTOM))
        );
        M3Button toggleVisibility = new M3Button("Hide content", M3ButtonVariant.FILLED);
        toggleVisibility.setOnAction(event -> {
            boolean show = !animatedVisibility.isShowing();
            animatedVisibility.setShowing(show);
            toggleVisibility.setText(show ? "Hide content" : "Show content");
        });
        M3Button finishVisibility = new M3Button("Finish transition", M3ButtonVariant.OUTLINED);
        finishVisibility.disableProperty().bind(animatedVisibility.transitioningProperty().not());
        finishVisibility.setOnAction(event -> animatedVisibility.finish());
        Label visibilityState = new Label();
        visibilityState.textProperty().bind(animatedVisibility.stateProperty().asString("State: %s"));
        FlowPane visibilityActions = createResponsiveActionRow(toggleVisibility, finishVisibility, visibilityState);
        VBox visibilityExample = new VBox(12.0, animatedVisibility, visibilityActions);
        configureResponsiveWidth(visibilityExample, 520.0);

        M3AnimatedContent animatedContent = new M3AnimatedContent(createMotionContent(false));
        animatedContent.setFitToWidth(true);
        animatedContent.setMinWidth(0.0);
        animatedContent.setMaxWidth(Double.MAX_VALUE);
        animatedContent.setMaxHeight(Region.USE_PREF_SIZE);
        M3Button replaceContent = new M3Button("Show expanded content", M3ButtonVariant.FILLED);
        replaceContent.setOnAction(event -> {
            @Nullable Node current = animatedContent.getContent();
            boolean expanded = current == null || current.prefWidth(-1.0) < 400.0;
            configureContentTransform(animatedContent, expanded);
            animatedContent.setContent(createMotionContent(expanded));
            replaceContent.setText(expanded ? "Show compact content" : "Show expanded content");
        });
        M3Button finishReplacement = new M3Button("Finish transition", M3ButtonVariant.OUTLINED);
        finishReplacement.setOnAction(event -> animatedContent.finish());
        FlowPane contentActions = createResponsiveActionRow(replaceContent, finishReplacement);
        VBox contentExample = new VBox(12.0, animatedContent, contentActions);
        configureResponsiveWidth(contentExample, 520.0);

        M3Button firstItem = new M3Button("Plan", M3ButtonVariant.TONAL);
        M3Button secondItem = new M3Button("Build", M3ButtonVariant.TONAL);
        M3Button thirdItem = new M3Button("Review", M3ButtonVariant.TONAL);
        HBox layoutTrack = new HBox(12.0, firstItem, secondItem, thirdItem);
        layoutTrack.getStyleClass().add("demo-flow");
        layoutTrack.setAlignment(Pos.CENTER_LEFT);
        layoutTrack.setMinHeight(112.0);
        configureResponsiveWidth(layoutTrack, 560.0);

        M3LayoutTransition layoutTransition = new M3LayoutTransition(layoutTrack);
        layoutTrack.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null) {
                layoutTransition.stop();
            } else {
                layoutTransition.start();
            }
        });

        M3Button changeLayout = new M3Button("Change alignment", M3ButtonVariant.FILLED);
        changeLayout.setOnAction(event -> layoutTrack.setAlignment(
                layoutTrack.getAlignment() == Pos.CENTER_LEFT ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT
        ));
        VBox layoutExample = new VBox(12.0, layoutTrack, changeLayout);
        configureResponsiveWidth(layoutExample, 560.0);

        M3AdaptiveScaffold adaptiveScaffold = new M3AdaptiveScaffold();
        adaptiveScaffold.getStyleClass().add("demo-motion-scaffold");
        adaptiveScaffold.setMinWidth(0.0);
        adaptiveScaffold.setMaxWidth(Double.MAX_VALUE);
        adaptiveScaffold.setMinHeight(300.0);
        adaptiveScaffold.setPrefHeight(300.0);
        adaptiveScaffold.setContentMargin(12.0);
        adaptiveScaffold.setPaneSpacing(12.0);
        adaptiveScaffold.setFixedLeadingPaneWidth(240.0);
        adaptiveScaffold.setLeadingPane(createAdaptiveMotionPane(
                "Supporting pane",
                "Stable content slides out while the main pane reflows."
        ));
        adaptiveScaffold.setMainPane(createAdaptiveMotionPane(
                "Main pane",
                "Resize or reverse the transition without resetting rendered geometry."
        ));
        Region navigationRail = createAdaptiveMotionPane("Rail", "Expanded navigation");
        navigationRail.setMinWidth(88.0);
        navigationRail.setPrefWidth(88.0);
        Region navigationBar = createAdaptiveMotionPane("Bar", "Compact navigation");
        navigationBar.setMinHeight(64.0);
        navigationBar.setPrefHeight(64.0);
        adaptiveScaffold.setNavigationRail(navigationRail);
        adaptiveScaffold.setNavigationBar(navigationBar);
        adaptiveScaffold.setPaneLayout(M3PaneLayout.ADAPTIVE);
        adaptiveScaffold.setNavigationLayout(M3NavigationLayout.ADAPTIVE);

        M3Slider splitPosition = new M3Slider(0.2, 0.8, 0.5);
        adaptiveScaffold.splitPositionProperty().bind(splitPosition.valueProperty());
        M3Button changeAdaptiveLayout = new M3Button("Use compact topology", M3ButtonVariant.FILLED);
        changeAdaptiveLayout.setOnAction(event -> {
            boolean compact = adaptiveScaffold.getPaneLayout() != M3PaneLayout.SINGLE;
            adaptiveScaffold.setPaneLayout(compact ? M3PaneLayout.SINGLE : M3PaneLayout.ADAPTIVE);
            adaptiveScaffold.setNavigationLayout(compact ? M3NavigationLayout.BAR : M3NavigationLayout.ADAPTIVE);
            changeAdaptiveLayout.setText(compact ? "Resume adaptive topology" : "Use compact topology");
        });
        VBox adaptiveExample = new VBox(
                12.0,
                adaptiveScaffold,
                new Label("Flexible pane split"),
                splitPosition,
                changeAdaptiveLayout
        );
        configureResponsiveWidth(adaptiveExample, 560.0);

        return createGallery(
                createFullWidthShowcaseGroup("Interruptible Value", valueExample),
                createFullWidthShowcaseGroup("Coordinated State Transition", stateExample),
                createFullWidthShowcaseGroup("Seekable State Transition", seekExample),
                createFullWidthShowcaseGroup("Animated Visibility", visibilityExample),
                createFullWidthShowcaseGroup("Animated Content And Size", contentExample),
                createFullWidthShowcaseGroup("Existing Layout Container", layoutExample),
                createFullWidthShowcaseGroup("Adaptive Pane Topology", adaptiveExample)
        );
    }

    /// Configures a direction-aware shared-axis transform for one content replacement.
    ///
    /// @param animatedContent the retained-content host
    /// @param forward         whether the replacement advances to expanded content
    private static void configureContentTransform(M3AnimatedContent animatedContent, boolean forward) {
        M3TransitionEdge enterEdge = forward ? M3TransitionEdge.END : M3TransitionEdge.START;
        M3TransitionEdge exitEdge = forward ? M3TransitionEdge.START : M3TransitionEdge.END;
        M3ContentTransform sharedAxis = M3ContentTransform.sharedAxis(M3TransitionAxis.X, forward);
        animatedContent.setContentTransform(new M3ContentTransform(
                sharedAxis.targetContentEnter()
                        .and(M3EnterTransition.expandHorizontally(enterEdge)),
                sharedAxis.initialContentExit()
                        .and(M3ExitTransition.shrinkHorizontally(exitEdge)),
                sharedAxis.sizeTransform(),
                sharedAxis.targetContentZIndex()
        ));
    }

    /// Computes the horizontal distance a sample may travel without leaving its track's content area.
    ///
    /// @param track  the containing track
    /// @param sample the moving sample
    /// @return the non-negative available travel distance in logical pixels
    private static double availableHorizontalTravel(Region track, Region sample) {
        Insets insets = track.getInsets();
        return Math.max(0.0, track.getWidth() - insets.getLeft() - insets.getRight() - sample.prefWidth(-1.0));
    }

    /// Creates one compact or expanded node for the animated-content showcase.
    private static M3Surface createMotionContent(boolean expanded) {
        Label headline = new Label(expanded ? "Expanded workspace" : "Compact summary");
        headline.getStyleClass().add("demo-group-title");
        Label supporting = new Label(expanded
                ? "Incoming and outgoing nodes coexist while the container follows the new preferred size. "
                + "Repeated clicks retarget every active channel from its current visual state."
                : "Replace this summary without resetting an in-progress transition.");
        supporting.setWrapText(true);
        configureResponsiveWidth(supporting, expanded ? 440.0 : 260.0);

        VBox content = new VBox(8.0, headline, supporting);
        content.setMinWidth(0.0);
        content.setMaxWidth(Double.MAX_VALUE);
        M3Surface surface = new M3Surface();
        surface.getContent().add(content);
        return configureResponsiveWidth(surface, expanded ? 520.0 : 340.0);
    }

    /// Creates one labeled surface used by the adaptive scaffold motion example.
    ///
    /// @param headline   the surface headline
    /// @param supporting the supporting description
    /// @return the configured surface
    private static M3Surface createAdaptiveMotionPane(String headline, String supporting) {
        Label headlineLabel = new Label(headline);
        headlineLabel.getStyleClass().add("demo-group-title");
        Label supportingLabel = new Label(supporting);
        supportingLabel.setWrapText(true);

        M3Surface surface = new M3Surface();
        surface.getStyleClass().add("demo-motion-pane");
        surface.getContent().add(new VBox(8.0, headlineLabel, supportingLabel));
        surface.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        return surface;
    }
}
