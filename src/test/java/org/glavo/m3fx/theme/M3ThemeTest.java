package org.glavo.m3fx.theme;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import org.glavo.m3fx.controls.M3Badge;
import org.glavo.m3fx.controls.M3Button;
import org.glavo.m3fx.controls.M3Card;
import org.glavo.m3fx.controls.M3Chip;
import org.glavo.m3fx.controls.M3DialogPane;
import org.glavo.m3fx.controls.M3Divider;
import org.glavo.m3fx.controls.M3ListItem;
import org.glavo.m3fx.controls.M3SegmentedButton;
import org.glavo.m3fx.controls.M3TextField;
import org.glavo.m3fx.tokens.M3Density;
import org.glavo.m3fx.tokens.M3Profile;
import org.glavo.monetfx.Brightness;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Tests for m3fx theme creation and installation.
@NotNullByDefault
final class M3ThemeTest {
    /// Starts the JavaFX toolkit before tests create scenes.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException ignored) {
            latch.countDown();
        }
        assertTrue(latch.await(10, TimeUnit.SECONDS));
    }

    /// Verifies that the baseline profile creates a complete token set.
    @Test
    void createsBaselineTokenSet() {
        M3Theme theme = M3Theme.fromSeed(Color.web("#6750a4"));

        assertEquals(M3Profile.BASELINE_2021, theme.profile());
        assertSame(theme.colorScheme(), theme.tokens().colorTokens().colorScheme());
        assertTrue(theme.toRootStyleDeclarations().contains("-monet-primary"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-color-primary"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-typescale-label-large-font-size"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-button-filled-container-height"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-segmented-button-container-height"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-slider-track-thickness"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-chip-container-height"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-progress-indicator-size"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-card-content-padding"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-divider-thickness"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-badge-small-size"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-list-item-one-line-height"));
        assertTrue(theme.toControlStyleRules().contains(".m3-filled-button"));
        assertTrue(theme.toControlStyleRules().contains(".m3-segmented-button"));
        assertTrue(theme.toControlStyleRules().contains(".m3-segmented-button-first"));
        assertTrue(theme.toControlStyleRules().contains(".m3-dialog-pane"));
        assertTrue(theme.toControlStyleRules().contains(".m3-badge"));
        assertTrue(theme.toControlStyleRules().contains(".m3-list-item"));
        assertTrue(theme.toControlStyleRules().contains("-fx-opacity: 0.92"));
        assertTrue(theme.toControlStyleRules().contains(".m3-elevated-card .m3-card-container"));
        assertNotNull(theme.tokens().componentTokens().filledButton());
        assertNotNull(theme.tokens().componentTokens().segmentedButton());
        assertNotNull(theme.tokens().componentTokens().slider());
        assertNotNull(theme.tokens().componentTokens().chip());
        assertNotNull(theme.tokens().componentTokens().divider());
        assertNotNull(theme.tokens().componentTokens().badge());
        assertNotNull(theme.tokens().componentTokens().listItem());
    }

    /// Verifies that the expressive profile creates a complete token set.
    @Test
    void createsExpressiveTokenSet() {
        M3Theme theme = M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.DARK,
                M3Density.standard()
        );

        assertEquals(M3Profile.EXPRESSIVE_2025, theme.profile());
        assertSame(theme.colorScheme(), theme.tokens().colorTokens().colorScheme());
        assertTrue(theme.toRootStyleDeclarations().contains("-monet-primary"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-color-primary"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-button-filled-container-height: 48px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-segmented-button-container-height: 48px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-slider-touch-target-size: 48px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-chip-container-height: 36px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-card-container-shape: 16px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-dialog-container-shape: 32px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-badge-small-size: 8px"));
        assertTrue(theme.toRootStyleDeclarations().contains("-m3-list-item-one-line-height: 64px"));
        assertTrue(theme.toControlStyleRules().contains("-m3-container-height: 48px"));
        assertTrue(theme.toControlStyleRules().contains("-fx-background-radius: 999px"));
        assertNotNull(theme.tokens().componentTokens().filledButton());
        assertNotNull(theme.tokens().componentTokens().segmentedButton());
        assertNotNull(theme.tokens().componentTokens().slider());
        assertNotNull(theme.tokens().componentTokens().chip());
        assertNotNull(theme.tokens().componentTokens().divider());
        assertNotNull(theme.tokens().componentTokens().badge());
        assertNotNull(theme.tokens().componentTokens().listItem());
    }

    /// Verifies that installing a theme on a scene is idempotent.
    @Test
    void installsThemeOnSceneOnce() {
        Pane root = new Pane();
        Scene scene = new Scene(root);
        M3Theme theme = M3Theme.defaultTheme();

        M3ThemeManager.install(scene, theme);
        M3ThemeManager.install(scene, theme);

        assertTrue(root.getStyleClass().contains(M3ThemeManager.ROOT_STYLE_CLASS));
        assertSame(theme, root.getProperties().get(M3ThemeManager.THEME_PROPERTY_KEY));
        assertTrue(root.getStyle().contains("-m3-color-primary"));
        assertEquals(2, scene.getStylesheets().size());
        assertEquals(M3ThemeManager.stylesheetUrl(), scene.getStylesheets().get(0));
    }

    /// Verifies that generated component stylesheets apply theme tokens to controls.
    @Test
    void installsGeneratedComponentStylesheet() {
        M3Button button = new M3Button("Button");
        M3TextField textField = new M3TextField();
        M3Chip chip = new M3Chip("Chip");
        M3SegmentedButton segmentedButton = new M3SegmentedButton("Week");
        Pane root = new Pane(button, textField, chip, segmentedButton);
        Scene scene = new Scene(root);

        M3Theme expressiveTheme = M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT,
                M3Density.standard()
        );
        M3ThemeManager.install(scene, expressiveTheme);
        root.applyCss();

        assertEquals(48.0, button.getContainerHeight(), 0.0001);
        assertEquals(64.0, textField.getContainerHeight(), 0.0001);
        assertEquals(36.0, chip.getContainerHeight(), 0.0001);
        assertEquals(48.0, segmentedButton.getContainerHeight(), 0.0001);

        M3Theme baselineTheme = M3Theme.defaultTheme();
        M3ThemeManager.install(scene, baselineTheme);
        root.applyCss();

        assertEquals(40.0, button.getContainerHeight(), 0.0001);
        assertEquals(56.0, textField.getContainerHeight(), 0.0001);
        assertEquals(32.0, chip.getContainerHeight(), 0.0001);
        assertEquals(40.0, segmentedButton.getContainerHeight(), 0.0001);
        assertEquals(2, scene.getStylesheets().size());
    }

    /// Verifies that generated component stylesheets apply utility component tokens.
    @Test
    void generatedComponentStylesheetAppliesUtilityTokens() {
        M3Divider divider = new M3Divider();
        M3Badge badge = new M3Badge("12");
        Pane root = new Pane(divider, badge);
        Scene scene = new Scene(root);

        M3Theme expressiveTheme = M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT,
                M3Density.standard()
        );
        M3ThemeManager.install(scene, expressiveTheme);
        root.applyCss();

        assertEquals(1.0, divider.getThickness(), 0.0001);
        assertEquals(0.0, divider.getInsetStart(), 0.0001);
        assertEquals(0.0, divider.getInsetEnd(), 0.0001);
        assertEquals(8.0, badge.getSmallSize(), 0.0001);
        assertEquals(18.0, badge.getLargeHeight(), 0.0001);
        assertEquals(18.0, badge.getLargeMinWidth(), 0.0001);
        assertEquals(9.0, badge.getContainerShape(), 0.0001);
    }

    /// Verifies that generated component stylesheets apply list item tokens.
    @Test
    void generatedComponentStylesheetAppliesListItemTokens() {
        M3ListItem listItem = new M3ListItem("Headline");
        Pane root = new Pane(listItem);
        Scene scene = new Scene(root);

        M3Theme expressiveTheme = M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT,
                M3Density.standard()
        );
        M3ThemeManager.install(scene, expressiveTheme);
        root.applyCss();

        assertEquals(64.0, listItem.getOneLineHeight(), 0.0001);
        assertEquals(80.0, listItem.getTwoLineHeight(), 0.0001);
        assertEquals(96.0, listItem.getThreeLineHeight(), 0.0001);
        assertEquals(10.0, listItem.getContainerShape(), 0.0001);
        assertEquals(16.0, listItem.getHorizontalPadding(), 0.0001);
        assertEquals(8.0, listItem.getVerticalPadding(), 0.0001);
        assertEquals(16.0, listItem.getContentSpacing(), 0.0001);
    }

    /// Verifies that generated component stylesheets apply container tokens.
    @Test
    void generatedComponentStylesheetAppliesContainerTokens() {
        M3Card card = new M3Card();
        M3DialogPane dialogPane = new M3DialogPane();
        Pane root = new Pane(card, dialogPane);
        Scene scene = new Scene(root);

        M3Theme expressiveTheme = M3Theme.fromSeed(
                Color.web("#006a6a"),
                M3Profile.EXPRESSIVE_2025,
                Brightness.LIGHT,
                M3Density.standard()
        );
        M3ThemeManager.install(scene, expressiveTheme);
        root.applyCss();

        assertEquals(16.0, card.getContainerShape(), 0.0001);
        assertEquals(16.0, card.getContentPadding(), 0.0001);
        assertEquals(1.0, card.getOutlineWidth(), 0.0001);
        assertEquals(32.0, dialogPane.getContainerShape(), 0.0001);
        assertEquals(24.0, dialogPane.getContentPadding(), 0.0001);
        assertEquals(24.0, dialogPane.getPadding().getTop(), 0.0001);
    }
}
