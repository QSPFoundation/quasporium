#!/usr/bin/env bash
set -euo pipefail

cmake=features/game/data/src/main/cpp/CMakeLists.txt
java=features/game/data/src/main/java

pinned_tag() {
    awk -v name="$1" '
        $0 ~ "FetchContent_Declare\\(" name "$" { found = 1 }
        found && $1 == "GIT_TAG" { print $2; exit }
    ' "$cmake"
}

check() {
    local repo=$1 name=$2 upstream_path=$3 local_path=$4
    local tag
    tag=$(pinned_tag "$name")
    if [ -z "$tag" ]; then
        echo "::error::No GIT_TAG for $name in $cmake"
        return 1
    fi
    local url="https://raw.githubusercontent.com/QSPFoundation/$repo/$tag/$upstream_path"
    if curl -fsSL "$url" | diff -u - "$java/$local_path"; then
        echo "$local_path matches $repo@$tag"
    else
        echo "::error file=$java/$local_path::Differs from $upstream_path at $repo@$tag; copy it from $url"
        return 1
    fi
}

status=0
check qsp qsp-external qsp/bindings/java/QSPLib.java com/libqsp/jni/QSPLib.java || status=1
check qsp-legacy qsplegacy-external src/bindings/jvm/QSPLegacyLib.java \
    com/libqsplegacy/jni/QSPLegacyLib.java || status=1
exit $status
