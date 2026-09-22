cmake_minimum_required(VERSION 3.22.1)
project(sesyazi_jni C CXX)

set(CMAKE_C_STANDARD 11)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

include(FetchContent)

# whisper.cpp — sabitlenmiş sürüm. Yerel kopya için:
#   -DFETCHCONTENT_SOURCE_DIR_WHISPER=/yol/whisper.cpp
set(WHISPER_BUILD_TESTS    OFF CACHE BOOL "" FORCE)
set(WHISPER_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
set(WHISPER_BUILD_SERVER   OFF CACHE BOOL "" FORCE)
set(WHISPER_ALL_WARNINGS   OFF CACHE BOOL "" FORCE)
set(BUILD_SHARED_LIBS      OFF CACHE BOOL "" FORCE)
set(GGML_OPENMP            OFF CACHE BOOL "" FORCE)
set(GGML_NATIVE            OFF CACHE BOOL "" FORCE)
set(GGML_CCACHE            OFF CACHE BOOL "" FORCE)
set(GGML_ALL_WARNINGS      OFF CACHE BOOL "" FORCE)

FetchContent_Declare(
    whisper
    GIT_REPOSITORY https://github.com/ggml-org/whisper.cpp.git
    GIT_TAG        v1.9.4
    GIT_SHALLOW    TRUE
)
FetchContent_MakeAvailable(whisper)

add_library(sesyazi SHARED sesyazi_jni.c)
target_link_libraries(sesyazi PRIVATE whisper)

if (ANDROID)
    find_library(LOG_LIB log)
    target_link_libraries(sesyazi PRIVATE ${LOG_LIB})
endif ()

if (NOT CMAKE_BUILD_TYPE STREQUAL "Debug")
    target_compile_options(sesyazi PRIVATE -O3)
endif ()
