#!/usr/bin/env bash

set -euo pipefail

echo "--- :arrow_down: Downloading APKs"
buildkite-agent artifact download "app/build/outputs/apk/debug/app-debug.apk" . --step "build-connected-tests"
buildkite-agent artifact download "app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk" . --step "build-connected-tests"

echo "--- :robot_face: Starting Android emulator"
start-android-emulator \
	--system-image "system-images;android-34;google_apis;arm64-v8a" \
	--device "pixel_5"

echo "--- :android: Running connected tests"
marathon run -m .buildkite/Marathonfile
