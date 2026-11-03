#!/usr/bin/env bash

set -euo pipefail

echo "--- :robot_face: Starting Android emulator"
start-android-emulator \
	--system-image "system-images;android-34;google_apis;arm64-v8a" \
	--device "pixel_5" \
	--logs-dir "build/emulator-logs"

echo "--- :android: Running connected tests"
./gradlew --stacktrace :app:connectedDebugAndroidTest
