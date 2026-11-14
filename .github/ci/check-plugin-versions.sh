#!/usr/bin/env bash
set -euo pipefail

catalog_version() {
  sed -n "s/^$1 = \"\(.*\)\"/\1/p" gradle/libs.versions.toml
}

settings_version() {
  sed -n "s/.*id(\"$1\") version \"\([^\"]*\)\".*/\1/p" settings.gradle.kts
}

status=0
check() {
  local plugin="$1" key="$2"
  local expected actual
  expected="$(catalog_version "$key")"
  actual="$(settings_version "$plugin")"
  if [ -z "$expected" ] || [ -z "$actual" ]; then
    echo "::error::Could not read the $plugin version (catalog: '$expected', settings: '$actual')"
    status=1
  elif [ "$expected" != "$actual" ]; then
    echo "::error file=settings.gradle.kts::$plugin is $actual in settings.gradle.kts but $expected in the version catalog ($key)"
    status=1
  fi
}

check com.android.application agp
check com.android.library agp
check org.jetbrains.kotlin.jvm kotlin
check org.jetbrains.kotlin.android kotlin

exit "$status"
