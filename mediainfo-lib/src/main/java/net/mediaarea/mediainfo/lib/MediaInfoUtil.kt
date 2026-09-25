/*  Copyright (c) MediaArea.net SARL. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license that can
 *  be found in the License.html file in the root of the source tree.
 */

package net.mediaarea.mediainfo.lib

import java.io.InputStream

/**
 * Utility class providing simplified access to MediaInfo functionality.
 * Supports analyzing local file descriptors, file paths, input streams,
 * and remote network streams (HTTP/HTTPS, NAS, WebDAV, SMB).
 */
class MediaInfoUtil {

    companion object {

        /**
          * Get MediaInfo library version
          * @return Version string (e.g., "24.06")
          */
        @JvmStatic
        fun getVersion(): String {
            return MediaInfo().use { mi ->
                mi.Option("Info_Version").replace("MediaInfoLib - v", "")
            }
        }

        /**
          * Get list of supported output formats
          * @return Comma-separated list of format names
          */
        @JvmStatic
        fun getSupportedFormats(): String {
            return MediaInfo().use { mi ->
                mi.Option("Info_OutputFormats")
            }
        }

        // ====================================================================
        // File Descriptor (Android ContentResolver / ParcelFileDescriptor)
        // ====================================================================

        /**
         * Analyzes a media file from a file descriptor and returns XML output.
         *
         * @param fd File descriptor of the media file (e.g. from `ParcelFileDescriptor.detachFd()`).
         * @param filename Name of the file (for reference only, can be any string).
         * @return Complete media information as XML string.
         */
        @JvmStatic
        fun getMediaInfoXml(fd: Int, filename: String = ""): String {
            return getMediaInfo(fd, filename, "MIXML")
        }

        /**
         * Analyzes a media file from a file descriptor and returns output in the specified format.
         *
         * @param fd File descriptor of the media file.
         * @param filename Name of the file (for reference only).
         * @param format Output format (e.g., "MIXML", "JSON", "Text", "HTML").
         * @return Media information in the specified format.
         */
        @JvmStatic
        fun getMediaInfo(fd: Int, filename: String = "", format: String = "MIXML"): String {
            return MediaInfo().use { mi ->
                mi.Option("Inform", format)
                mi.Open(fd, filename)
                mi.Inform()
            }
        }

        // ====================================================================
        // File Path
        // ====================================================================

        /**
         * Analyzes a local media file by path and returns XML output.
         *
         * @param filePath Absolute path to the media file.
         * @return Complete media information as XML string.
         */
        @JvmStatic
        fun getMediaInfoXml(filePath: String): String {
            return getMediaInfo(filePath, "MIXML")
        }

        /**
         * Analyzes a local media file by path and returns output in the specified format.
         *
         * @param filePath Absolute path to the media file.
         * @param format Output format (e.g., "MIXML", "JSON", "Text", "HTML").
         * @return Media information in the specified format.
         */
        @JvmStatic
        fun getMediaInfo(filePath: String, format: String = "MIXML"): String {
            return MediaInfo().use { mi ->
                mi.Option("Inform", format)
                mi.Open(filePath)
                mi.Inform()
            }
        }

        // ====================================================================
        // SeekableSource (HTTP, NAS, SMB, WebDAV, Custom Streams)
        // ====================================================================

        /**
         * Analyzes a media stream from any [SeekableSource] and returns XML output.
         *
         * @param source The seekable stream source to read from.
         * @param filename Optional filename for reference.
         * @return Complete media information as XML string.
         */
        @JvmStatic
        fun getMediaInfoXml(source: SeekableSource, filename: String = ""): String {
            return getMediaInfo(source, filename, "MIXML")
        }

        /**
         * Analyzes a media stream from any [SeekableSource] and returns output in the specified format.
         *
         * @param source The seekable stream source to read from.
         * @param filename Optional filename for reference.
         * @param format Output format (e.g., "MIXML", "JSON", "Text", "HTML").
         * @return Media information in the specified format.
         */
        @JvmStatic
        fun getMediaInfo(source: SeekableSource, filename: String = "", format: String = "MIXML"): String {
            return MediaInfo().use { mi ->
                mi.Option("Inform", format)
                mi.Open(source, filename)
                mi.Inform()
            }
        }

        // ====================================================================
        // Network Streaming URLs (HTTP / HTTPS / NAS Web Shares)
        // ====================================================================

        /**
         * Analyzes a remote media file over HTTP/HTTPS using HTTP range requests and returns XML output.
         * Only headers and metadata are fetched (~1-2 MB), rather than downloading the entire file.
         *
         * @param url HTTP/HTTPS URL of the remote media file (e.g. NAS HTTP, WebDAV, Jellyfin, Plex).
         * @param headers Optional custom HTTP headers (Authorization, User-Agent, etc.).
         * @param filename Optional filename for reference.
         * @return Complete media information as XML string.
         */
        @JvmStatic
        fun getMediaInfoXml(
            url: String,
            headers: Map<String, String> = emptyMap(),
            filename: String = ""
        ): String {
            return getMediaInfo(url, headers, filename, "MIXML")
        }

        /**
         * Analyzes a remote media file over HTTP/HTTPS using HTTP range requests and returns output in the specified format.
         *
         * @param url HTTP/HTTPS URL of the remote media file.
         * @param headers Optional custom HTTP headers (Authorization, User-Agent, etc.).
         * @param filename Optional filename for reference.
         * @param format Output format (e.g., "MIXML", "JSON", "Text", "HTML").
         * @return Media information in the specified format.
         */
        @JvmStatic
        fun getMediaInfo(
            url: String,
            headers: Map<String, String> = emptyMap(),
            filename: String = "",
            format: String = "MIXML"
        ): String {
            val effectiveName = if (filename.isNotEmpty()) filename else url.substringAfterLast('/')
            return HttpSeekableSource(url, headers).use { source ->
                getMediaInfo(source, effectiveName, format)
            }
        }

        // ====================================================================
        // Input Stream (Sequential streams)
        // ====================================================================

        /**
         * Analyzes a media file from a sequential [InputStream] and returns XML output.
         *
         * @param inputStream The input stream to read from.
         * @param totalSize Total stream size in bytes, or -1L if unknown.
         * @param filename Optional filename for reference.
         * @return Complete media information as XML string.
         */
        @JvmStatic
        fun getMediaInfoXml(
            inputStream: InputStream,
            totalSize: Long = -1L,
            filename: String = ""
        ): String {
            return getMediaInfo(inputStream, totalSize, filename, "MIXML")
        }

        /**
         * Analyzes a media file from a sequential [InputStream] and returns output in the specified format.
         *
         * @param inputStream The input stream to read from.
         * @param totalSize Total stream size in bytes, or -1L if unknown.
         * @param filename Optional filename for reference.
         * @param format Output format (e.g., "MIXML", "JSON", "Text", "HTML").
         * @return Media information in the specified format.
         */
        @JvmStatic
        fun getMediaInfo(
            inputStream: InputStream,
            totalSize: Long = -1L,
            filename: String = "",
            format: String = "MIXML"
        ): String {
            return InputStreamSeekableSource(inputStream, totalSize).use { source ->
                getMediaInfo(source, filename, format)
            }
        }
    }
}
