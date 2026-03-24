cmake_minimum_required(VERSION 3.15 FATAL_ERROR)
project(qspbuild)

include(FetchContent)

if (BUILD_PRIVATE_CONTENT)
    add_subdirectory(QSLibSDH)
    add_subdirectory(QSLibSNX)
    add_subdirectory(qsp)
else ()
    FetchContent_Declare(qsp-external
            GIT_REPOSITORY https://github.com/QSPFoundation/qsp.git
            GIT_TAG 0445921b0640c68bc7e7ab03393fc15e8373c8e9
            GIT_PROGRESS true
            SUBBUILD_DIR "${CMAKE_CURRENT_BINARY_DIR}/qsp-download"
            SOURCE_DIR "${CMAKE_CURRENT_BINARY_DIR}/qsp-src"
            BINARY_DIR "${CMAKE_CURRENT_BINARY_DIR}/qsp-build"
    )
    FetchContent_MakeAvailable(qsp-external)

    FetchContent_Declare(qslibsnx-external
            GIT_REPOSITORY https://github.com/l3ger0j/QSLibSNX.git
            GIT_TAG ff5d8f17bad22c964eba4faf7b4765f5e3a568b9
            GIT_PROGRESS true
            SUBBUILD_DIR "${CMAKE_CURRENT_BINARY_DIR}/qslibsnx-download"
            SOURCE_DIR "${CMAKE_CURRENT_BINARY_DIR}/qslibsnx-src"
            BINARY_DIR "${CMAKE_CURRENT_BINARY_DIR}/qslibsnx-build"
    )
    FetchContent_MakeAvailable(qslibsnx-external)

    FetchContent_Declare(qslibsdh-external
            GIT_REPOSITORY https://github.com/l3ger0j/QSLibSDH.git
            GIT_TAG 4a1f5f48a32f3a70f6a2b35508db71d7b1b1ad26
            GIT_PROGRESS true
            SUBBUILD_DIR "${CMAKE_CURRENT_BINARY_DIR}/qslibsdh-download"
            SOURCE_DIR "${CMAKE_CURRENT_BINARY_DIR}/qslibsdh-src"
            BINARY_DIR "${CMAKE_CURRENT_BINARY_DIR}/qslibsdh-build"
    )
    FetchContent_MakeAvailable(qslibsdh-external)
endif ()