#!/usr/bin/env bash
set -euo pipefail

output="${1:?usage: merge-detekt-sarif.sh <output.sarif>}"
mapfile -t reports < <(find . -path '*/build/reports/detekt/detekt.sarif' -not -path './build-logic/*' | sort)

if [ "${#reports[@]}" -eq 0 ]; then
  echo "No detekt SARIF reports found"
  exit 0
fi

mkdir -p "$(dirname "$output")"
jq -s '. as $all | $all[0] | .runs[0].results = [$all[].runs[].results[]]' "${reports[@]}" > "$output"
echo "Merged ${#reports[@]} reports, $(jq '.runs[0].results | length' "$output") findings"
