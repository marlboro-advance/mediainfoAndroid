/*  Copyright (c) MediaArea.net SARL. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license that can
 *  be found in the License.html file in the root of the source tree.
 */

package net.mediaarea.mediainfo.lib

import java.io.Closeable
import java.io.IOException

/**
 * Interface representing a seekable data source.
 * Allows MediaInfo to analyze network streams, remote NAS files, and custom data streams
 * by reading only the necessary chunks (headers, index, metadata) without downloading the entire file.
 */
interface SeekableSource : Closeable {
    /**
     * Total size of the stream in bytes, or -1L if the size is unknown.
     */
    val size: Long

    /**
     * Current read offset within the stream (0-indexed).
     */
    val position: Long

    /**
     * Repositions the read pointer to [position] in the stream.
     *
     * @param position Target byte offset from the start of the stream.
     * @throws IOException If repositioning fails or if the source does not support backward seeking.
     */
    @Throws(IOException::class)
    fun seek(position: Long)

    /**
     * Reads up to [length] bytes of data from this source into [buffer].
     *
     * @param buffer The destination buffer into which the data is read.
     * @param offset The start offset in [buffer] at which the data is written.
     * @param length The maximum number of bytes to read.
     * @return The total number of bytes read into the buffer, or -1 if the end of the source is reached.
     * @throws IOException If an I/O error occurs.
     */
    @Throws(IOException::class)
    fun read(buffer: ByteArray, offset: Int = 0, length: Int = buffer.size): Int
}
