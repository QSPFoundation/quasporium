cmake_minimum_required(VERSION 3.15 FATAL_ERROR)
project(qspbuild)

include(FetchContent)

if (BUILD_PRIVATE_CONTENT)
    add_subdirectory(QSLibSDH)
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

    FetchContent_Declare(qslibsdh-external
            GIT_REPOSITORY https://github.com/l3ger0j/QSLibSDH.git
            GIT_TAG 78c32c1e81da05c3c623a54ba04b5a0d48f6ec8b
            GIT_PROGRESS true
            SUBBUILD_DIR "${CMAKE_CURRENT_BINARY_DIR}/qslibsdh-download"
            SOURCE_DIR "${CMAKE_CURRENT_BINARY_DIR}/qslibsdh-src"
            BINARY_DIR "${CMAKE_CURRENT_BINARY_DIR}/qslibsdh-build"
    )
    FetchContent_MakeAvailable(qslibsdh-external)
endif ()