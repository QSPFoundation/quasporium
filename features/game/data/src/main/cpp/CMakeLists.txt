cmake_minimum_required(VERSION 3.15 FATAL_ERROR)
project(qspbuild)

include(FetchContent)

if (BUILD_PRIVATE_CONTENT)
    add_subdirectory(qsp-legacy)
    add_subdirectory(qsp)
else ()
    FetchContent_Declare(qsp-external
            GIT_REPOSITORY https://github.com/QSPFoundation/qsp.git
            GIT_TAG d4b6fac90aee1977612b588b3ab07223558cd2fb
            GIT_PROGRESS true
            SUBBUILD_DIR "${CMAKE_CURRENT_BINARY_DIR}/qsp-download"
            SOURCE_DIR "${CMAKE_CURRENT_BINARY_DIR}/qsp-src"
            BINARY_DIR "${CMAKE_CURRENT_BINARY_DIR}/qsp-build"
    )
    FetchContent_MakeAvailable(qsp-external)

    FetchContent_Declare(qsplegacy-external
            GIT_REPOSITORY https://github.com/QSPFoundation/qsp-legacy.git
            GIT_TAG 14fab55cfc4b3611fd8cef586f77d665bc41df3e
            GIT_PROGRESS true
            SUBBUILD_DIR "${CMAKE_CURRENT_BINARY_DIR}/qsplegacy-download"
            SOURCE_DIR "${CMAKE_CURRENT_BINARY_DIR}/qsplegacy-src"
            BINARY_DIR "${CMAKE_CURRENT_BINARY_DIR}/qsplegacy-build"
    )
    FetchContent_MakeAvailable(qsplegacy-external)
endif ()