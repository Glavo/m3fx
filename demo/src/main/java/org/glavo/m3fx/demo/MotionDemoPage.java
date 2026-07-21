// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.demo;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.glavo.m3fx.animation.M3AnimatedContent;
import org.glavo.m3fx.animation.M3AnimatedVisibility;
import org.glavo.m3fx.animation.M3DoubleAnimatable;
import org.glavo.m3fx.animation.M3LayoutTransition;
import org.glavo.m3fx.animation.M3StateTransition;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3ButtonVariant;
import org.glavo.m3fx.controls.M3Surface;
import org.glavo.m3fx.layout.M3AdaptiveScaffold;
import org.glavo.m3fx.layout.M3NavigationLayout;
import org.glavo.m3fx.layout.M3PaneLayout;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

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
        valueTrack.setPrefWidth(560.0);
        valueTrack.setMaxWidth(560.0);

        M3Button moveButton = new M3Button("Change target", M3ButtonVariant.FILLED);
        moveButton.setOnAction(event -> horizontalPosition.animateTo(
                horizontalPosition.getTargetValue() < 140.0 ? 320.0 : 0.0
        ));
        M3Button snapButton = new M3Button("Snap to start", M3ButtonVariant.OUTLINED);
        snapButton.setOnAction(event -> horizontalPosition.snapTo(0.0));
        HBox valueActions = new HBox(12.0, moveButton, snapButton);
        valueActions.setAlignment(Pos.CENTER_LEFT);
        VBox valueExample = new VBox(12.0, valueTrack, valueActions);

        Label coordinatedHeadline = new Label("One state, four visual channels");
        coordinatedHeadline.getStyleClass().add("demo-group-title");
        Label coordinatedSupporting = new Label("Position, scale, and opacity share one interruptible transition.");
        M3Surface coordinatedSurface = new M3Surface();
        coordinatedSurface.setPrefSize(220.0, 88.0);
        coordinatedSurface.setMaxSize(220.0, 88.0);
        coordinatedSurface.getContent().add(new VBox(6.0, coordinatedHeadline, coordinatedSupporting));

        StackPane coordinatedTrack = new StackPane(coordinatedSurface);
        coordinatedTrack.getStyleClass().add("demo-flow");
        coordinatedTrack.setAlignment(Pos.CENTER_LEFT);
        coordinatedTrack.setMinHeight(128.0);
        coordinatedTrack.setPrefWidth(560.0);
        coordinatedTrack.setMaxWidth(560.0);

        M3StateTransition<Boolean> stateTransition = new M3StateTransition<>(coordinatedSurface, false);
        stateTransition.addDouble(
                coordinatedSurface.translateXProperty(),
                expanded -> expanded ? 300.0 : 0.0,
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
        HBox stateActions = new HBox(12.0, changeState, finishState, stateLabel);
        stateActions.setAlignment(Pos.CENTER_LEFT);
        VBox stateExample = new VBox(12.0, coordinatedTrack, stateActions);

        Label visibilityHeadline = new Label("Content remains mounted until exit completes");
        visibilityHeadline.getStyleClass().add("demo-group-title");
        Label visibilitySupporting = new Label(
                "Visibility state, visual effects, and container size remain interruptible without changing the node."
        );
        visibilitySupporting.setWrapText(true);
        M3Surface visibilitySurface = new M3Surface();
        visibilitySurface.setPrefWidth(520.0);
        visibilitySurface.setMaxWidth(520.0);
        visibilitySurface.getContent().add(new VBox(8.0, visibilityHeadline, visibilitySupporting));

        M3AnimatedVisibility animatedVisibility = new M3AnimatedVisibility(visibilitySurface);
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
        HBox visibilityActions = new HBox(12.0, toggleVisibility, finishVisibility, visibilityState);
        visibilityActions.setAlignment(Pos.CENTER_LEFT);
        VBox visibilityExample = new VBox(12.0, animatedVisibility, visibilityActions);

        M3AnimatedContent animatedContent = new M3AnimatedContent(createMotionContent(false));
        animatedContent.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        M3Button replaceContent = new M3Button("Show expanded content", M3ButtonVariant.FILLED);
        replaceContent.setOnAction(event -> {
            @Nullable Node current = animatedContent.getContent();
            boolean expanded = current == null || current.prefWidth(-1.0) < 400.0;
            animatedContent.setContent(createMotionContent(expanded));
            replaceContent.setText(expanded ? "Show compact content" : "Show expanded content");
        });
        M3Button finishReplacement = new M3Button("Finish transition", M3ButtonVariant.OUTLINED);
        finishReplacement.setOnAction(event -> animatedContent.finish());
        HBox contentActions = new HBox(12.0, replaceContent, finishReplacement);
        contentActions.setAlignment(Pos.CENTER_LEFT);
        VBox contentExample = new VBox(12.0, animatedContent, contentActions);

        M3Button firstItem = new M3Button("Plan", M3ButtonVariant.TONAL);
        M3Button secondItem = new M3Button("Build", M3ButtonVariant.TONAL);
        M3Button thirdItem = new M3Button("Review", M3ButtonVariant.TONAL);
        HBox layoutTrack = new HBox(12.0, firstItem, secondItem, thirdItem);
        layoutTrack.getStyleClass().add("demo-flow");
        layoutTrack.setAlignment(Pos.CENTER_LEFT);
        layoutTrack.setMinHeight(112.0);
        layoutTrack.setPrefWidth(560.0);
        layoutTrack.setMaxWidth(560.0);

        M3LayoutTransition layoutTransition = new M3LayoutTransition(layoutTrack);
        layoutTrack.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null) {
                layoutTransition.stop();
            } else {
                Platform.runLater(() -> {
                    if (layoutTrack.getScene() != null) {
                        layoutTransition.start();
                    }
                });
            }
        });

        M3Button changeLayout = new M3Button("Change alignment", M3ButtonVariant.FILLED);
        changeLayout.setOnAction(event -> layoutTrack.setAlignment(
                layoutTrack.getAlignment() == Pos.CENTER_LEFT ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT
        ));
        VBox layoutExample = new VBox(12.0, layoutTrack, changeLayout);

        M3AdaptiveScaffold adaptiveScaffold = new M3AdaptiveScaffold();
        adaptiveScaffold.getStyleClass().add("demo-motion-scaffold");
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
        adaptiveScaffold.setPaneLayout(M3PaneLayout.FIXED_LEADING);
        adaptiveScaffold.setNavigationLayout(M3NavigationLayout.RAIL);

        M3Button changeAdaptiveLayout = new M3Button("Use compact topology", M3ButtonVariant.FILLED);
        changeAdaptiveLayout.setOnAction(event -> {
            boolean compact = adaptiveScaffold.getPaneLayout() != M3PaneLayout.SINGLE;
            adaptiveScaffold.setPaneLayout(compact ? M3PaneLayout.SINGLE : M3PaneLayout.FIXED_LEADING);
            adaptiveScaffold.setNavigationLayout(compact ? M3NavigationLayout.BAR : M3NavigationLayout.RAIL);
            changeAdaptiveLayout.setText(compact ? "Use expanded topology" : "Use compact topology");
        });
        VBox adaptiveExample = new VBox(12.0, adaptiveScaffold, changeAdaptiveLayout);

        return createGallery(
                createFullWidthShowcaseGroup("Interruptible Value", valueExample),
                createFullWidthShowcaseGroup("Coordinated State Transition", stateExample),
                createFullWidthShowcaseGroup("Animated Visibility", visibilityExample),
                createFullWidthShowcaseGroup("Animated Content And Size", contentExample),
                createFullWidthShowcaseGroup("Existing Layout Container", layoutExample),
                createFullWidthShowcaseGroup("Adaptive Pane Topology", adaptiveExample)
        );
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
        supporting.setPrefWidth(expanded ? 440.0 : 260.0);

        VBox content = new VBox(8.0, headline, supporting);
        M3Surface surface = new M3Surface();
        surface.getContent().add(content);
        surface.setPrefWidth(expanded ? 520.0 : 340.0);
        return surface;
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
