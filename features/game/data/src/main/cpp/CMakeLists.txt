cmake_minimum_required(VERSION 3.15 FATAL_ERROR)
project(qspbuild)

include(FetchContent)

if (BUILD_PRIVATE_CONTENT)
    add_subdirectory(qsp-legacy)
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

    FetchContent_Declare(qsplegacy-external
            GIT_REPOSITORY https://github.com/QSPFoundation/qsp-legacy.git
            GIT_TAG 16b85ac9740a5f05ffc8a753928c658d5a9208f5
            GIT_PROGRESS true
            SUBBUILD_DIR "${CMAKE_CURRENT_BINARY_DIR}/qsplegacy-download"
            SOURCE_DIR "${CMAKE_CURRENT_BINARY_DIR}/qsplegacy-src"
            BINARY_DIR "${CMAKE_CURRENT_BINARY_DIR}/qsplegacy-build"
    )
    FetchContent_MakeAvailable(qsplegacy-external)
endif ()