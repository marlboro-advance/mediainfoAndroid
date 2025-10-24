# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

cmake_minimum_required(VERSION 3.4.1)

project(MediaInfo)

# Build Zenlib
set(ZenLib_DIR ${CMAKE_CURRENT_BINARY_DIR}/ZenLib)
set(ZenLib_LIBRARY zen)

option(LARGE_FILES "Enable large files support" OFF)
add_subdirectory(../dependencies/ZenLib/Project/CMake ${ZenLib_DIR})
include_directories(AFTER SYSTEM ../dependencies/ZenLib/Source)
file(MAKE_DIRECTORY ${ZenLib_DIR}/../../../include) # fake system wide installation

# Build MediaInfoLib
set(MediaInfoLib_LIBRARY mediainfo)
set(MediaInfoLib_DIR ${CMAKE_CURRENT_BINARY_DIR}/MediaInfoLib)

option(JNI "Build JNI interface" ON)
add_subdirectory(../dependencies/MediaInfoLib/Project/CMake ${MediaInfoLib_DIR})
include_directories(AFTER SYSTEM ../dependencies/MediaInfoLib/Source)