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
            GIT_TAG c95fa014efa72a9211a32d7cae5d0283c1c595c8
            GIT_PROGRESS true
            SUBBUILD_DIR "${CMAKE_CURRENT_BINARY_DIR}/qslibsnx-download"
            SOURCE_DIR "${CMAKE_CURRENT_BINARY_DIR}/qslibsnx-src"
            BINARY_DIR "${CMAKE_CURRENT_BINARY_DIR}/qslibsnx-build"
    )
    FetchContent_MakeAvailable(qslibsnx-external)

    FetchContent_Declare(qslibsdh-external
            GIT_REPOSITORY https://github.com/l3ger0j/QSLibSDH.git
            GIT_TAG 905c155deeee350778567758bcbc3ab69cf66bc1
            GIT_PROGRESS true
            SUBBUILD_DIR "${CMAKE_CURRENT_BINARY_DIR}/qslibsdh-download"
            SOURCE_DIR "${CMAKE_CURRENT_BINARY_DIR}/qslibsdh-src"
            BINARY_DIR "${CMAKE_CURRENT_BINARY_DIR}/qslibsdh-build"
    )
    FetchContent_MakeAvailable(qslibsdh-external)
endif ()