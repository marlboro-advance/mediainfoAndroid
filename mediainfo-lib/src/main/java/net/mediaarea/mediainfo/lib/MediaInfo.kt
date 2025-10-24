/*  Copyright (c) MediaArea.net SARL. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license that can
 *  be found in the License.html file in the root of the source tree.
 */

package net.mediaarea.mediainfo.lib

/**
 * MediaInfo JNI interface for analyzing media files
 * This class provides direct access to the native MediaInfoLib library
 */
class MediaInfo {
    companion object {
        // Load native libraries
        init {
            System.loadLibrary("zen") // load ZenLib manually (fix Android < 4.3)
            System.loadLibrary("mediainfo")
        }
    }

    enum class Stream {
        General,
        Video,
        Audio,
        Text,
        Other,
        Image,
        Menu,
        Max
    }

    enum class Info {
        Name,
        Text,
        Measure,
        Options,
        Name_Text,
        Measure_Text,
        Info,
        HowTo,
        Domain,
        Max
    }

    val mi: Long = Init()

    external fun Init(): Long
    external fun Destroy(): Int
    private external fun OpenFd(fd: Int, name: String): Int
    private external fun Open(name: String): Int

    /**
     * Open a media file for analysis
     * @param fd File descriptor of the media file
     * @param name Name of the file (for reference)
     * @return 1 if successful, 0 otherwise
     */
    fun Open(fd: Int, name: String): Int {
        return OpenFd(fd, name)
    }

    external fun Open_Buffer_Init(fileSize: Long, fileOffset: Long): Int
    external fun Open_Buffer_Continue(buffer: ByteArray, bufferSize: Long): Int
    external fun Open_Buffer_Continue_GoTo_Get(): Long
    external fun Open_Buffer_Finalize(): Long

    /**
     * Close the currently opened file
     */
    external fun Close(): Int

    /**
     * Get the report output based on current inform settings
     * Use Option("Inform", "MIXML") to get XML output
     * @return String containing the formatted report
     */
    external fun Inform(): String

    private external fun GetI(
        streamKind: Int,
        streamNumber: Int,
        parameter: Int,
        infoKind: Int
    ): String

    private external fun GetS(
        streamKind: Int,
        streamNumber: Int,
        parameter: String,
        infoKind: Int,
        searchKind: Int
    ): String

    /**
     * Get information about a specific parameter
     * @param streamKind Type of stream (Video, Audio, etc.)
     * @param streamNumber Stream number (0 for first stream)
     * @param parameter Parameter index
     * @param infoKind Type of information to retrieve
     * @return String value of the requested information
     */
    fun Get(
        streamKind: Stream,
        streamNumber: Int,
        parameter: Int,
        infoKind: Info = Info.Text
    ): String {
        return GetI(streamKind.ordinal, streamNumber, parameter, infoKind.ordinal)
    }

    /**
     * Get information about a specific parameter by name
     * @param streamKind Type of stream (Video, Audio, etc.)
     * @param streamNumber Stream number (0 for first stream)
     * @param parameter Parameter name (e.g., "Format", "Width", "BitRate")
     * @param infoKind Type of information to retrieve
     * @param searchKind Type of search to perform
     * @return String value of the requested information
     */
    fun Get(
        streamKind: Stream,
        streamNumber: Int,
        parameter: String,
        infoKind: Info = Info.Text,
        searchKind: Info = Info.Name
    ): String {
        return GetS(
            streamKind.ordinal,
            streamNumber,
            parameter,
            infoKind.ordinal,
            searchKind.ordinal
        )
    }

    /**
     * Configure MediaInfo options
     * @param option Option name (e.g., "Inform", "Language")
     * @param value Option value
     * @return Result string from the option call
     */
    external fun Option(option: String, value: String = ""): String

    external fun State_Get(): Int

    private external fun Count_Get(streamKind: Int, streamNumber: Int): Int

    /**
     * Get the number of streams of a specific type
     * @param streamKind Type of stream (Video, Audio, etc.)
     * @param streamNumber Stream number (-1 for all)
     * @return Number of streams
     */
    fun Count_Get(streamKind: Stream, streamNumber: Int = -1): Int {
        return Count_Get(streamKind.ordinal, streamNumber)
    }
}
