#!/usr/bin/env bash

set -euo pipefail

echo "--- :hammer_and_wrench: Building connected tests"
./gradlew --stacktrace :app:assembleDebug :app:assembleDebugAndroidTest
