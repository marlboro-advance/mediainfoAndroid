/*  Copyright (c) MediaArea.net SARL. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license that can
 *  be found in the License.html file in the root of the source tree.
 */

package net.mediaarea.mediainfo.lib

import java.io.IOException
import java.io.InputStream

/**
 * An [InputStream] wrapper implementing [SeekableSource].
 * Supports forward seeking via stream skipping. Backward seeking is not supported on
 * purely sequential input streams.
 *
 * @param inputStream The underlying [InputStream] to read from.
 * @param totalSize Total stream size in bytes, or -1L if unknown.
 */
class InputStreamSeekableSource(
    private val inputStream: InputStream,
    private val totalSize: Long = -1L
) : SeekableSource {

    override val size: Long
        get() = totalSize

    override var position: Long = 0L
        private set

    private var isClosed = false

    @Throws(IOException::class)
    override fun seek(position: Long) {
        checkNotClosed()
        if (position < this.position) {
            throw IOException(
                "Backward seeking is not supported on sequential InputStream (requested: $position, current: ${this.position})"
            )
        }
        if (position == this.position) {
            return
        }

        var remaining = position - this.position
        while (remaining > 0) {
            val skipped = inputStream.skip(remaining)
            if (skipped <= 0) {
                // Try reading a byte if skip returns 0
                val readByte = inputStream.read()
                if (readByte == -1) break
                this.position += 1
                remaining -= 1
            } else {
                this.position += skipped
                remaining -= skipped
            }
        }
    }

    @Throws(IOException::class)
    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        checkNotClosed()
        val bytesRead = inputStream.read(buffer, offset, length)
        if (bytesRead > 0) {
            position += bytesRead
        }
        return bytesRead
    }

    @Throws(IOException::class)
    private fun checkNotClosed() {
        if (isClosed) {
            throw IOException("InputStreamSeekableSource is closed")
        }
    }

    override fun close() {
        if (!isClosed) {
            isClosed = true
            inputStream.close()
        }
    }
}
