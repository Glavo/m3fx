// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.controls;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.SkinBase;
import org.glavo.m3fx.FxTestUtils;
import org.glavo.m3fx.skins.M3CarouselSkin;
import org.glavo.m3fx.skins.M3ListViewSkin;
import org.jetbrains.annotations.NotNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/// Verifies the semantic indexed-scrolling contract shared by virtualized controls and their skins.
@NotNullByDefault
final class M3ScrollToEventTest {
    /// Starts the JavaFX toolkit before controls and skins are created.
    @BeforeAll
    static void startToolkit() throws InterruptedException {
        FxTestUtils.startToolkit();
    }

    /// Verifies that list scrolling is dispatched to a custom skin without depending on the default skin type.
    @Test
    void listViewDispatchesScrollRequestsToCustomSkin() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListView<String> listView = new M3ListView<>();
            listView.getItems().addAll("Alpha", "Beta", "Gamma");
            RecordingListSkin skin = new RecordingListSkin(listView);
            listView.setSkin(skin);

            listView.scrollTo(2, false);

            assertEquals(2, skin.requestedIndex);
            assertFalse(skin.animated);
            assertThrows(IndexOutOfBoundsException.class, () -> listView.scrollTo(3));
            assertEquals(1, skin.requestCount);
        });
    }

    /// Verifies that carousel scrolling captures the selected index and is observable by a custom skin.
    @Test
    void carouselDispatchesSelectedItemRequestToCustomSkin() {
        FxTestUtils.runOnFxThread(() -> {
            M3Carousel carousel = new M3Carousel();
            carousel.getItems().addAll(new M3Surface(), new M3Surface());
            RecordingCarouselSkin skin = new RecordingCarouselSkin(carousel);
            carousel.setSkin(skin);
            AtomicInteger observedIndex = new AtomicInteger(-1);
            carousel.addEventFilter(
                    M3ScrollToEvent.SCROLL_TO_INDEX,
                    event -> observedIndex.set(event.getIndex())
            );

            carousel.scrollSelectedItemIntoView();
            assertEquals(0, skin.requestCount);

            carousel.selectIndex(1);
            int requestCountBeforeExplicitScroll = skin.requestCount;
            carousel.scrollSelectedItemIntoView(true);

            assertEquals(1, observedIndex.get());
            assertEquals(1, skin.requestedIndex);
            assertTrue(skin.animated);
            assertEquals(requestCountBeforeExplicitScroll + 1, skin.requestCount);
        });
    }

    /// Verifies that default skins consume accepted requests and unregister their handlers when disposed.
    @Test
    void defaultSkinsOwnOnlyTheirInstalledLifetime() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListView<String> listView = new M3ListView<>();
            listView.getItems().add("Alpha");
            M3ListViewSkin<String> listSkin = new M3ListViewSkin<>(listView);
            listView.setSkin(listSkin);
            M3ScrollToEvent listRequest = new M3ScrollToEvent(listView, listView, 0, false);

            Event.fireEvent(listView, listRequest);
            assertTrue(listRequest.isConsumed());

            listView.setSkin(null);
            M3ScrollToEvent detachedListRequest = new M3ScrollToEvent(listView, listView, 0, false);
            Event.fireEvent(listView, detachedListRequest);
            assertFalse(detachedListRequest.isConsumed());

            M3Carousel carousel = new M3Carousel();
            carousel.getItems().add(new M3Surface());
            carousel.selectIndex(0);
            M3CarouselSkin carouselSkin = new M3CarouselSkin(carousel);
            carousel.setSkin(carouselSkin);
            M3ScrollToEvent carouselRequest = new M3ScrollToEvent(carousel, carousel, 0, false);

            Event.fireEvent(carousel, carouselRequest);
            assertTrue(carouselRequest.isConsumed());

            carousel.setSkin(null);
            M3ScrollToEvent detachedCarouselRequest = new M3ScrollToEvent(carousel, carousel, 0, false);
            Event.fireEvent(carousel, detachedCarouselRequest);
            assertFalse(detachedCarouselRequest.isConsumed());
        });
    }

    /// Verifies event payload access, copying, and constructor validation.
    @Test
    void eventRetainsRequestPayloadAcrossCopies() {
        FxTestUtils.runOnFxThread(() -> {
            M3ListView<String> first = new M3ListView<>();
            M3ListView<String> second = new M3ListView<>();
            M3ScrollToEvent event = new M3ScrollToEvent(first, first, 4, true);
            M3ScrollToEvent copy = event.copyFor(second, second);

            assertSame(M3ScrollToEvent.SCROLL_TO_INDEX, event.getEventType());
            assertEquals(4, copy.getIndex());
            assertTrue(copy.isAnimated());
            assertSame(second, copy.getSource());
            assertSame(second, copy.getTarget());
            assertThrows(IllegalArgumentException.class, () -> new M3ScrollToEvent(first, first, -1, false));
        });
    }

    /// A test skin that records list scrolling requests.
    private static final class RecordingListSkin extends SkinBase<M3ListView<String>> {
        /// The last requested index.
        private int requestedIndex = -1;

        /// Whether the last request preferred animation.
        private boolean animated;

        /// The number of accepted requests.
        private int requestCount;

        /// Handles and records each indexed scrolling request.
        private final EventHandler<M3ScrollToEvent> requestHandler = event -> {
            requestedIndex = event.getIndex();
            animated = event.isAnimated();
            requestCount++;
            event.consume();
        };

        /// Creates a recording list skin.
        ///
        /// @param control the skinnable list view
        private RecordingListSkin(M3ListView<String> control) {
            super(control);
            control.addEventHandler(M3ScrollToEvent.SCROLL_TO_INDEX, requestHandler);
        }

        /// Removes the request handler before disposal.
        @Override
        public void dispose() {
            getSkinnable().removeEventHandler(M3ScrollToEvent.SCROLL_TO_INDEX, requestHandler);
            super.dispose();
        }
    }

    /// A test skin that records carousel scrolling requests.
    private static final class RecordingCarouselSkin extends SkinBase<M3Carousel> {
        /// The last requested index.
        private int requestedIndex = -1;

        /// Whether the last request preferred animation.
        private boolean animated;

        /// The number of accepted requests.
        private int requestCount;

        /// Handles and records each indexed scrolling request.
        private final EventHandler<M3ScrollToEvent> requestHandler = event -> {
            requestedIndex = event.getIndex();
            animated = event.isAnimated();
            requestCount++;
            event.consume();
        };

        /// Creates a recording carousel skin.
        ///
        /// @param control the skinnable carousel
        private RecordingCarouselSkin(M3Carousel control) {
            super(control);
            control.addEventHandler(M3ScrollToEvent.SCROLL_TO_INDEX, requestHandler);
        }

        /// Removes the request handler before disposal.
        @Override
        public void dispose() {
            getSkinnable().removeEventHandler(M3ScrollToEvent.SCROLL_TO_INDEX, requestHandler);
            super.dispose();
        }
    }
}
