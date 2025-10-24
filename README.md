# MediaInfo Android Library

[![](https://jitpack.io/v/marlboro-advance/mediainfoAndroid.svg)](https://jitpack.io/#marlboro-advance/mediainfoAndroid)
[![Android Build CI](https://github.com/marlboro-advance/mediainfoAndroid/actions/workflows/build.yml/badge.svg)](https://github.com/marlboro-advance/mediainfoAndroid/actions/workflows/build.yml)

An Android library wrapper for [MediaInfoLib](https://mediaarea.net/MediaInfo), providing
comprehensive media file analysis capabilities. Extract detailed technical and tag information from
video, audio, and image files directly in your Android app.

## Features

- **Complete Media Analysis** - Get detailed information about video, audio, text, and other streams
- **Multiple Output Formats** - XML, JSON, Text, HTML, PBCore, EBUCore
- **Easy to Use** - Simple API with both high-level utilities and low-level access
- **Native Performance** - Built on top of the robust MediaInfoLib C++ library
- **Android Optimized** - Supports all major architectures (arm64-v8a, armeabi-v7a, x86, x86_64)
- **Modern Android** - Supports Android 5.0 (API 21) and above

## Installation

### Step 1: Add JitPack repository

Add the JitPack repository to your project's `settings.gradle` or `settings.gradle.kts`:

**Groovy (`settings.gradle`):**

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

**Kotlin (`settings.gradle.kts`):**

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### Step 2: Add the dependency

Add the MediaInfo library to your app's `build.gradle` or `build.gradle.kts`:

**Groovy (`build.gradle`):**

```groovy
dependencies {
    implementation 'com.github.marlboro-advance:mediainfoAndroid:v1.0.0'
}
```

**Kotlin (`build.gradle.kts`):**

```kotlin
dependencies {
    implementation("com.github.marlboro-advance:mediainfoAndroid:v1.0.0")
}
```

## Quick Start

### Using the Simple Utility API

The easiest way to get media information is using the `MediaInfoUtil` class:

```kotlin
import android.net.Uri
import android.os.ParcelFileDescriptor
import net.mediaarea.mediainfo.lib.MediaInfoUtil

fun getMediaInfo(uri: Uri) {
    val pfd: ParcelFileDescriptor? = contentResolver.openFileDescriptor(uri, "r")
    val fd = pfd?.detachFd() ?: return
    
    try {
        // Get XML output (recommended format)
        val xmlOutput = MediaInfoUtil.getMediaInfoXml(fd, "video.mp4")
        println(xmlOutput)
    } finally {
        pfd?.close()
    }
}
```

### Get Different Output Formats

```kotlin
import net.mediaarea.mediainfo.lib.MediaInfoUtil

// XML format (most detailed)
val xmlOutput = MediaInfoUtil.getMediaInfo(fd, "video.mp4", "MIXML")

// JSON format
val jsonOutput = MediaInfoUtil.getMediaInfo(fd, "video.mp4", "JSON")

// Human-readable text (like MediaInfo desktop app)
val textOutput = MediaInfoUtil.getMediaInfo(fd, "video.mp4", "Text")

// HTML format
val htmlOutput = MediaInfoUtil.getMediaInfo(fd, "video.mp4", "HTML")
```

### Check Library Version

```kotlin
import net.mediaarea.mediainfo.lib.MediaInfoUtil

val version = MediaInfoUtil.getVersion()
println("MediaInfo version: $version")
```

## Advanced Usage

### Using the Low-Level API

For more control and to query specific parameters:

```kotlin
import net.mediaarea.mediainfo.lib.MediaInfo

val mi = MediaInfo()

// Open file
mi.Open(fd, "video.mp4")

// Get general information
val format = mi.Get(MediaInfo.Stream.General, 0, "Format")
val duration = mi.Get(MediaInfo.Stream.General, 0, "Duration")
val fileSize = mi.Get(MediaInfo.Stream.General, 0, "FileSize")

// Get video stream information
val videoCount = mi.Count_Get(MediaInfo.Stream.Video)
if (videoCount > 0) {
    val videoFormat = mi.Get(MediaInfo.Stream.Video, 0, "Format")
    val width = mi.Get(MediaInfo.Stream.Video, 0, "Width")
    val height = mi.Get(MediaInfo.Stream.Video, 0, "Height")
    val frameRate = mi.Get(MediaInfo.Stream.Video, 0, "FrameRate")
    val bitRate = mi.Get(MediaInfo.Stream.Video, 0, "BitRate")
    
    println("Video: $videoFormat ${width}x${height} @ $frameRate fps, $bitRate bps")
}

// Get audio stream information
val audioCount = mi.Count_Get(MediaInfo.Stream.Audio)
for (i in 0 until audioCount) {
    val audioFormat = mi.Get(MediaInfo.Stream.Audio, i, "Format")
    val channels = mi.Get(MediaInfo.Stream.Audio, i, "Channels")
    val sampleRate = mi.Get(MediaInfo.Stream.Audio, i, "SamplingRate")
    
    println("Audio $i: $audioFormat, $channels channels, $sampleRate Hz")
}

// Get human-readable text output
mi.Option("Inform", "Text")
val textReport = mi.Inform()
println(textReport)

// Close
mi.Close()
```

### Output Format Options

The library supports multiple output formats through the `Inform` option:

| Format    | Description          | Use Case                         |
|-----------|----------------------|----------------------------------|
| `Text`    | Human-readable text  | Display to users, logs           |
| `MIXML`   | MediaInfo XML format | Structured parsing (recommended) |
| `JSON`    | JSON format          | API integration, web apps        |
| `HTML`    | HTML format          | Web pages, reports               |
| `XML`     | Alternative XML      | Legacy compatibility             |
| `PBCore`  | PBCore 2.0 XML       | Broadcasting metadata            |
| `EBUCore` | EBUCore 1.8 XML      | European broadcast standard      |

Example with custom format:

```kotlin
import net.mediaarea.mediainfo.lib.MediaInfo

val mi = MediaInfo()
mi.Open(fd, "video.mp4")

// Get text format output
mi.Option("Inform", "Text")
val textOutput = mi.Inform()

// Get JSON format output
mi.Option("Inform", "JSON")
val jsonOutput = mi.Inform()

mi.Close()
```

## Stream Types

The library supports the following stream types:

- `MediaInfo.Stream.General` - General container information
- `MediaInfo.Stream.Video` - Video stream data
- `MediaInfo.Stream.Audio` - Audio stream data
- `MediaInfo.Stream.Text` - Subtitle/caption data
- `MediaInfo.Stream.Image` - Image data
- `MediaInfo.Stream.Menu` - Menu/chapter data
- `MediaInfo.Stream.Other` - Other streams

## Complete Example

Here's a complete example in an Android Activity:

```kotlin
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.mediaarea.mediainfo.lib.MediaInfo

class MainActivity : AppCompatActivity() {
    
    private val pickMediaLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { analyzeMedia(it) }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        findViewById<Button>(R.id.btnPickFile).setOnClickListener {
            pickMediaLauncher.launch(arrayOf("video/*", "audio/*"))
        }
    }
    
    private fun analyzeMedia(uri: Uri) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val pfd: ParcelFileDescriptor? = contentResolver.openFileDescriptor(uri, "r")
                val fd = pfd?.detachFd() ?: return@launch
                
                val mi = MediaInfo()
                mi.Open(fd, uri.lastPathSegment ?: "unknown")
                
                // Build detailed report
                val report = buildString {
                    appendLine("=== GENERAL INFO ===")
                    appendLine("Format: ${mi.Get(MediaInfo.Stream.General, 0, "Format")}")
                    appendLine("Duration: ${mi.Get(MediaInfo.Stream.General, 0, "Duration/String")}")
                    appendLine("File Size: ${mi.Get(MediaInfo.Stream.General, 0, "FileSize/String")}")
                    appendLine()
                    
                    val videoCount = mi.Count_Get(MediaInfo.Stream.Video)
                    for (i in 0 until videoCount) {
                        appendLine("=== VIDEO STREAM $i ===")
                        appendLine("Format: ${mi.Get(MediaInfo.Stream.Video, i, "Format")}")
                        appendLine("Resolution: ${mi.Get(MediaInfo.Stream.Video, i, "Width")}x${mi.Get(MediaInfo.Stream.Video, i, "Height")}")
                        appendLine("Frame Rate: ${mi.Get(MediaInfo.Stream.Video, i, "FrameRate")} fps")
                        appendLine("Bit Rate: ${mi.Get(MediaInfo.Stream.Video, i, "BitRate/String")}")
                        appendLine()
                    }
                    
                    val audioCount = mi.Count_Get(MediaInfo.Stream.Audio)
                    for (i in 0 until audioCount) {
                        appendLine("=== AUDIO STREAM $i ===")
                        appendLine("Format: ${mi.Get(MediaInfo.Stream.Audio, i, "Format")}")
                        appendLine("Channels: ${mi.Get(MediaInfo.Stream.Audio, i, "Channels")}")
                        appendLine("Sample Rate: ${mi.Get(MediaInfo.Stream.Audio, i, "SamplingRate")} Hz")
                        appendLine("Bit Rate: ${mi.Get(MediaInfo.Stream.Audio, i, "BitRate/String")}")
                        appendLine()
                    }
                }
                
                mi.Close()
                pfd?.close()
                
                withContext(Dispatchers.Main) {
                    findViewById<TextView>(R.id.tvOutput).text = report
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
```

## Permissions

Add the necessary permissions to your `AndroidManifest.xml`:

```xml
<!-- For Android 12 and below -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />

<!-- For Android 13 and above -->
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
<uses-permission android:name="android.permission.READ_MEDIA_AUDIO" />
```

For Android 11+ (API 30+), it's recommended to use the Storage Access Framework (as shown in the
example above) to avoid requiring `MANAGE_EXTERNAL_STORAGE` permission.

## Supported Formats

MediaInfo supports a wide range of media formats including:

**Video Containers:** MP4, MKV, AVI, MOV, FLV, WebM, WMV, MPEG, TS, M2TS, VOB, and many more

**Video Codecs:** H.264/AVC, H.265/HEVC, VP8, VP9, AV1, MPEG-2, MPEG-4, DivX, Xvid, and many more

**Audio Containers:** MP3, AAC, FLAC, WAV, OGG, WMA, M4A, OPUS, and many more

**Audio Codecs:** MP3, AAC, FLAC, Vorbis, Opus, AC3, DTS, PCM, and many more

**Images:** JPEG, PNG, BMP, GIF, TIFF, WebP, and more

## Requirements

- **Minimum SDK:** API 21 (Android 5.0 Lollipop)
- **Target SDK:** API 36 (Android 15)
- **JDK:** Java 21 or higher
- **Supported ABIs:** armeabi-v7a, arm64-v8a, x86, x86_64

## Building from Source

```bash
# Clone the repository
git clone https://github.com/marlboro-advance/mediainfoAndroid.git
cd mediainfoAndroid

# Initialize submodules
git submodule update --init --recursive

# Build the library
./gradlew build

# Or build release AAR
./gradlew :mediainfo-lib:assembleRelease
```

The AAR will be generated in `mediainfo-lib/build/outputs/aar/`.

## ProGuard/R8

If you're using ProGuard or R8, the library should work out of the box. The consumer ProGuard rules
are included in the AAR to preserve the native JNI methods.

## License

This library is released under the BSD-2-Clause License. See the [LICENSE](LICENSE) file for
details.

MediaInfoLib is Copyright (c) MediaArea.net SARL. All Rights Reserved.

## Credits

- Built on [MediaInfoLib](https://github.com/MediaArea/MediaInfoLib) by MediaArea.net
- Uses [ZenLib](https://github.com/MediaArea/ZenLib) for core utilities