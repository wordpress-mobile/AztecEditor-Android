#!/bin/bash

set -euo pipefail

echo "--- ktlint"
./gradlew --stacktrace ktlint

echo -e "\n--- lintRelease"
./gradlew --stacktrace lintRelease
