/*  Copyright (c) MediaArea.net SARL. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license that can
 *  be found in the License.html file in the root of the source tree.
 */

package net.mediaarea.mediainfo.lib

import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * An HTTP/HTTPS implementation of [SeekableSource] that uses HTTP Range requests
 * (`Range: bytes=START-`).
 *
 * This allows analyzing remote files (e.g. on a NAS, Jellyfin/Plex, WebDAV, or HTTP file servers)
 * without downloading the entire media file. MediaInfo only queries the file header and footer,
 * typically transferring less than 1-2 MB even for multi-gigabyte media.
 *
 * @param url The HTTP or HTTPS URL of the media file.
 * @param headers Optional custom HTTP headers (e.g. Authorization, User-Agent, Cookie).
 * @param connectTimeoutMs Connection timeout in milliseconds (default: 15,000 ms).
 * @param readTimeoutMs Read timeout in milliseconds (default: 15,000 ms).
 */
class HttpSeekableSource(
    private val url: String,
    private val headers: Map<String, String> = emptyMap(),
    private val connectTimeoutMs: Int = 15_000,
    private val readTimeoutMs: Int = 15_000
) : SeekableSource {

    private var currentEffectiveUrl: String = url
    private var currentConnection: HttpURLConnection? = null
    private var currentInputStream: InputStream? = null

    override var size: Long = -1L
        private set

    override var position: Long = 0L
        private set

    private var isClosed = false

    init {
        resolveEffectiveUrlAndSize()
    }

    /**
     * Probes the server to follow initial redirects and obtain the total file size
     * via Content-Range or Content-Length.
     */
    private fun resolveEffectiveUrlAndSize() {
        var probeUrl = url
        var redirects = 0
        val maxRedirects = 10

        while (redirects < maxRedirects) {
            val connection = (URL(probeUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "HEAD"
                connectTimeout = connectTimeoutMs
                readTimeout = readTimeoutMs
                instanceFollowRedirects = false
                headers.forEach { (k, v) -> setRequestProperty(k, v) }
            }

            try {
                val responseCode = connection.responseCode
                if (responseCode in 300..399) {
                    val location = connection.getHeaderField("Location")
                    if (!location.isNullOrEmpty()) {
                        probeUrl = if (location.startsWith("http://") || location.startsWith("https://")) {
                            location
                        } else {
                            URL(URL(probeUrl), location).toString()
                        }
                        redirects++
                        continue
                    }
                }

                currentEffectiveUrl = probeUrl
                val contentLength = connection.getHeaderField("Content-Length")?.toLongOrNull() ?: -1L
                if (contentLength > 0) {
                    size = contentLength
                }
                break
            } catch (e: Exception) {
                // If HEAD is rejected or fails, we fall back to a ranged GET on first read
                break
            } finally {
                connection.disconnect()
            }
        }
    }

    @Throws(IOException::class)
    override fun seek(position: Long) {
        checkNotClosed()
        if (position < 0) {
            throw IOException("Negative seek offset: $position")
        }
        if (this.position == position) {
            return
        }

        // Small forward skip optimization: if seek target is <= 32 KB ahead, skip directly
        val forwardDelta = position - this.position
        if (forwardDelta in 1..32768 && currentInputStream != null) {
            var remaining = forwardDelta
            while (remaining > 0) {
                val skipped = currentInputStream?.skip(remaining) ?: 0L
                if (skipped <= 0) break
                remaining -= skipped
            }
            if (remaining == 0L) {
                this.position = position
                return
            }
        }

        // Otherwise close active stream and reconnect at the target offset on next read
        closeCurrentStream()
        this.position = position
    }

    @Throws(IOException::class)
    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        checkNotClosed()
        if (length == 0) return 0

        ensureOpenStream()
        val inputStream = currentInputStream ?: throw IOException("Failed to open HTTP stream")

        val bytesRead = inputStream.read(buffer, offset, length)
        if (bytesRead > 0) {
            position += bytesRead
        }
        return bytesRead
    }

    @Throws(IOException::class)
    private fun ensureOpenStream() {
        if (currentInputStream != null) return

        var targetUrl = currentEffectiveUrl
        var redirects = 0
        val maxRedirects = 10

        while (redirects < maxRedirects) {
            val conn = (URL(targetUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = connectTimeoutMs
                readTimeout = readTimeoutMs
                instanceFollowRedirects = false
                headers.forEach { (k, v) -> setRequestProperty(k, v) }
                if (position > 0) {
                    setRequestProperty("Range", "bytes=$position-")
                }
            }

            val responseCode = conn.responseCode
            if (responseCode in 300..399) {
                val location = conn.getHeaderField("Location")
                conn.disconnect()
                if (!location.isNullOrEmpty()) {
                    targetUrl = if (location.startsWith("http://") || location.startsWith("https://")) {
                        location
                    } else {
                        URL(URL(targetUrl), location).toString()
                    }
                    currentEffectiveUrl = targetUrl
                    redirects++
                    continue
                }
            }

            if (responseCode != HttpURLConnection.HTTP_OK &&
                responseCode != HttpURLConnection.HTTP_PARTIAL
            ) {
                conn.disconnect()
                throw IOException("HTTP request failed with status code $responseCode")
            }

            // Extract total size from Content-Range if not already determined
            if (size <= 0) {
                val contentRange = conn.getHeaderField("Content-Range")
                if (!contentRange.isNullOrEmpty()) {
                    val slashIndex = contentRange.lastIndexOf('/')
                    if (slashIndex != -1 && slashIndex + 1 < contentRange.length) {
                        contentRange.substring(slashIndex + 1).trim().toLongOrNull()?.let {
                            size = it
                        }
                    }
                }
                if (size <= 0 && position == 0L) {
                    val len = conn.getHeaderField("Content-Length")?.toLongOrNull() ?: -1L
                    if (len > 0) size = len
                }
            }

            currentConnection = conn
            currentInputStream = conn.inputStream
            return
        }

        throw IOException("Too many HTTP redirects")
    }

    private fun closeCurrentStream() {
        try {
            currentInputStream?.close()
        } catch (_: Exception) {}
        currentInputStream = null

        try {
            currentConnection?.disconnect()
        } catch (_: Exception) {}
        currentConnection = null
    }

    @Throws(IOException::class)
    private fun checkNotClosed() {
        if (isClosed) {
            throw IOException("HttpSeekableSource is closed")
        }
    }

    override fun close() {
        if (!isClosed) {
            isClosed = true
            closeCurrentStream()
        }
    }
}
