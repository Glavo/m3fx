// Copyright (c) 2026 Glavo
// SPDX-License-Identifier: Apache-2.0

package org.glavo.m3fx.internal.theme;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.spi.URLStreamHandlerProvider;
import java.util.Base64;

/// Supplies the private URL protocol used for generated stylesheets on older JavaFX runtimes.
///
/// The provider is discovered through the standard [URLStreamHandlerProvider] service mechanism. It is deliberately
/// independent of the JVM-global URL handler factory so applications remain free to install their own protocol
/// support.
@NotNullByDefault
public final class M3StylesheetUrlStreamHandlerProvider extends URLStreamHandlerProvider {
    /// The stateless handler shared by every `m3fx-css` URL.
    private static final URLStreamHandler STYLESHEET_HANDLER = new StylesheetUrlStreamHandler();

    /// Creates a stylesheet URL handler provider for service loading.
    public M3StylesheetUrlStreamHandlerProvider() {
    }

    /// Returns the handler for the private M3FX stylesheet protocol.
    ///
    /// @param protocol the protocol name requested by [URL]
    /// @return the M3FX handler, or `null` when this provider does not own `protocol`
    @Override
    public @Nullable URLStreamHandler createURLStreamHandler(String protocol) {
        return M3StylesheetUrls.CUSTOM_PROTOCOL.equalsIgnoreCase(protocol) ? STYLESHEET_HANDLER : null;
    }

    /// Opens in-memory connections for private stylesheet URLs.
    @NotNullByDefault
    private static final class StylesheetUrlStreamHandler extends URLStreamHandler {
        /// Creates the shared stateless handler.
        private StylesheetUrlStreamHandler() {
        }

        /// Opens a connection that decodes the payload embedded in `url`.
        ///
        /// @param url the private stylesheet URL
        /// @return an in-memory connection to the decoded CSS
        /// @throws IOException if the URL payload is not valid URL-safe Base64
        @Override
        protected URLConnection openConnection(URL url) throws IOException {
            return new StylesheetURLConnection(url);
        }
    }

    /// Exposes one decoded stylesheet payload through a URL connection.
    @NotNullByDefault
    private static final class StylesheetURLConnection extends URLConnection {
        /// The CSS media type and character encoding returned to URL consumers.
        private static final String CONTENT_TYPE = "text/css; charset=UTF-8";

        /// The immutable UTF-8 stylesheet bytes decoded from the URL.
        private final byte @Unmodifiable [] content;

        /// Decodes one private stylesheet URL.
        ///
        /// @param url the URL containing an unpadded URL-safe Base64 payload
        /// @throws IOException if the URL payload cannot be decoded
        private StylesheetURLConnection(URL url) throws IOException {
            super(url);
            try {
                content = Base64.getUrlDecoder().decode(url.getPath());
            } catch (IllegalArgumentException e) {
                throw new IOException("Invalid m3fx-css stylesheet URL", e);
            }
        }

        /// Marks this in-memory connection as connected.
        @Override
        public void connect() {
            connected = true;
        }

        /// Opens a new stream over the decoded stylesheet content.
        ///
        /// @return an input stream positioned at the beginning of the stylesheet
        @Override
        public InputStream getInputStream() {
            connect();
            return new ByteArrayInputStream(content);
        }

        /// Returns the decoded stylesheet length in bytes.
        ///
        /// @return the non-negative UTF-8 byte length
        @Override
        public int getContentLength() {
            return content.length;
        }

        /// Returns the decoded stylesheet length in bytes without integer narrowing.
        ///
        /// @return the non-negative UTF-8 byte length
        @Override
        public long getContentLengthLong() {
            return content.length;
        }

        /// Returns the CSS media type and UTF-8 charset.
        ///
        /// @return the CSS media type and UTF-8 charset declaration
        @Override
        public String getContentType() {
            return CONTENT_TYPE;
        }
    }
}
