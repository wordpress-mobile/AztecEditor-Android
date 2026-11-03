#!/usr/bin/env bash

set -euo pipefail

SYSTEM_IMAGE="system-images;android-34;google_apis;x86_64"
AVD_NAME="aztec-connected-tests"
EMULATOR_LOG="build/emulator.log"

BUILD_SDK="$ANDROID_HOME"
# The agent user cannot write to the image's SDK, and Gradle still needs it.
EMULATOR_SDK="$(mktemp -d)"
ln -s "$BUILD_SDK/licenses" "$EMULATOR_SDK/licenses"
ln -s "$BUILD_SDK/platform-tools" "$EMULATOR_SDK/platform-tools"
ln -s "$BUILD_SDK/platforms" "$EMULATOR_SDK/platforms"
export ANDROID_HOME="$EMULATOR_SDK"
export ANDROID_SDK_ROOT="$EMULATOR_SDK"
export PATH="$EMULATOR_SDK/emulator:$EMULATOR_SDK/platform-tools:$PATH"

stop_emulator() {
	adb emu kill || true
	avdmanager delete avd --name "$AVD_NAME" || true
	rm -rf "$EMULATOR_SDK"
}

echo "--- :package: Installing emulator and system image"
sdkmanager --sdk_root="$EMULATOR_SDK" "emulator" "$SYSTEM_IMAGE" > /dev/null

echo "--- :mag: Checking hardware acceleration"
ls -la /dev/kvm
emulator -accel-check

echo "--- :robot_face: Starting Android emulator"
avdmanager create avd --force --name "$AVD_NAME" --package "$SYSTEM_IMAGE" --device "pixel_5" <<< "no"
trap stop_emulator EXIT

mkdir -p "$(dirname "$EMULATOR_LOG")"
emulator -avd "$AVD_NAME" -no-window -no-audio -no-boot-anim -no-snapshot -gpu swiftshader_indirect > "$EMULATOR_LOG" 2>&1 &

timeout 300 adb wait-for-device
timeout 300 bash -c 'until [[ "$(adb shell getprop sys.boot_completed 2> /dev/null | tr -d "\r")" == "1" ]]; do sleep 5; done'

adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0
adb shell settings put secure long_press_timeout 1800
adb shell input keyevent 82

echo "--- :android: Running connected tests"
ANDROID_HOME="$BUILD_SDK" ANDROID_SDK_ROOT="$BUILD_SDK" ./gradlew --stacktrace :app:connectedDebugAndroidTest
