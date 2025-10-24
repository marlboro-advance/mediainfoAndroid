/*  Copyright (c) MediaArea.net SARL. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license that can
 *  be found in the License.html file in the root of the source tree.
 */

package net.mediaarea.mediainfo.lib

/**
 * Utility class providing simplified access to MediaInfo functionality
 * This class provides convenience methods for common use cases
 */
class MediaInfoUtil {

    companion object {
        /**
         * Analyzes a media file and returns XML output
         * This is the simplest way to get complete media information in XML format
         *
         * @param fd File descriptor of the media file (obtained from ParcelFileDescriptor.detachFd())
         * @param filename Name of the file (for reference only, can be any string)
         * @return Complete media information as XML string
         *
         * Example usage:
         * ```
         * val pfd = contentResolver.openFileDescriptor(uri, "r")
         * val fd = pfd?.detachFd() ?: return
         * val xmlOutput = MediaInfoUtil.getMediaInfoXml(fd, "video.mp4")
         * pfd?.close()
         * ```
         */
        @JvmStatic
        fun getMediaInfoXml(fd: Int, filename: String): String {
            val mi = MediaInfo()

            // Configure for XML output
            mi.Option("Inform", "MIXML")

            // Open and analyze file
            mi.Open(fd, filename)

            // Get XML output
            val xmlOutput = mi.Inform()

            // Close
            mi.Close()

            return xmlOutput
        }

        /**
         * Get MediaInfo library version
         * @return Version string (e.g., "24.06")
         */
        @JvmStatic
        fun getVersion(): String {
            val mi = MediaInfo()
            return mi.Option("Info_Version").replace("MediaInfoLib - v", "")
        }

        /**
         * Get list of supported output formats
         * @return Comma-separated list of format names
         */
        @JvmStatic
        fun getSupportedFormats(): String {
            val mi = MediaInfo()
            return mi.Option("Info_OutputFormats")
        }

        /**
         * Analyzes a media file and returns output in the specified format
         *
         * @param fd File descriptor of the media file
         * @param filename Name of the file (for reference only)
         * @param format Output format (e.g., "MIXML", "JSON", "Text", "HTML")
         * @return Media information in the specified format
         *
         * Available formats:
         * - MIXML: XML format (default and recommended)
         * - JSON: JSON format
         * - Text: Human-readable text
         * - HTML: HTML format
         * - XML: Alternative XML format
         * - PBCore: PBCore 2.0 XML format
         * - EBUCore: EBUCore 1.8 XML format
         */
        @JvmStatic
        fun getMediaInfo(fd: Int, filename: String, format: String = "MIXML"): String {
            val mi = MediaInfo()

            // Configure output format
            mi.Option("Inform", format)

            // Open and analyze file
            mi.Open(fd, filename)

            // Get output
            val output = mi.Inform()

            // Close
            mi.Close()

            return output
        }
    }
}
