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
# CPU varyantları: ggml her ARM nesli için ayrı .so üretir (armv8.0 … armv9.2),
# çalışma anında telefona en uygun olanı yüklenir (dotprod/fp16/i8mm).
set(BUILD_SHARED_LIBS      ON  CACHE BOOL "" FORCE)
set(GGML_BACKEND_DL        ON  CACHE BOOL "" FORCE)
if (ANDROID)
    set(GGML_CPU_ALL_VARIANTS ON CACHE BOOL "" FORCE)
endif ()
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
# AGP'nin paketlediği klasör; whisper.cpp kendi alt dizininde bunu "bin/" ile ezer.
set(SESYAZI_OUT_DIR "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}")

FetchContent_MakeAvailable(whisper)

# Tüm whisper/ggml .so'larını (dlopen ile yüklenen CPU varyantları dahil)
# APK'ya girecek klasöre yönlendir.
function(sesyazi_redirect_outputs dir)
    get_property(targets DIRECTORY "${dir}" PROPERTY BUILDSYSTEM_TARGETS)
    foreach (t IN LISTS targets)
        get_target_property(type ${t} TYPE)
        if (type STREQUAL "SHARED_LIBRARY" OR type STREQUAL "MODULE_LIBRARY")
            set_target_properties(${t} PROPERTIES
                LIBRARY_OUTPUT_DIRECTORY "${SESYAZI_OUT_DIR}"
                RUNTIME_OUTPUT_DIRECTORY "${SESYAZI_OUT_DIR}")
            list(APPEND SESYAZI_ALL_LIBS ${t})
        endif ()
    endforeach ()
    set(SESYAZI_ALL_LIBS ${SESYAZI_ALL_LIBS} PARENT_SCOPE)
    get_property(subdirs DIRECTORY "${dir}" PROPERTY SUBDIRECTORIES)
    foreach (sd IN LISTS subdirs)
        sesyazi_redirect_outputs("${sd}")
        set(SESYAZI_ALL_LIBS ${SESYAZI_ALL_LIBS} PARENT_SCOPE)
    endforeach ()
endfunction()
if (SESYAZI_OUT_DIR)
    sesyazi_redirect_outputs("${whisper_SOURCE_DIR}")
    message(STATUS "SesYazi native libs -> ${SESYAZI_OUT_DIR}: ${SESYAZI_ALL_LIBS}")
endif ()

add_library(sesyazi SHARED sesyazi_jni.c)
target_link_libraries(sesyazi PRIVATE whisper ggml)
# dlopen ile yüklenen CPU varyantları link bağımlılığı değil; yine de derlensinler
if (SESYAZI_ALL_LIBS)
    add_dependencies(sesyazi ${SESYAZI_ALL_LIBS})
endif ()

if (ANDROID)
    find_library(LOG_LIB log)
    target_link_libraries(sesyazi PRIVATE ${LOG_LIB})
endif ()

if (NOT CMAKE_BUILD_TYPE STREQUAL "Debug")
    target_compile_options(sesyazi PRIVATE -O3)
endif ()
