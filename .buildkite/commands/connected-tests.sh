#!/bin/bash

set -euo pipefail

echo "--- :closed_lock_with_key: Installing Secrets"
./gradlew applyConfiguration

echo -e "\n--- :gcloud: Logging into Google Cloud"
gcloud auth activate-service-account --key-file .configure-files/firebase.secrets.json

echo -e "\n--- :hammer_and_wrench: Building Tests"
./gradlew --stacktrace app:assembleDebug app:assembleDebugAndroidTest

echo -e "\n--- :firebase: Run Tests"
gcloud firebase test android run \
	--project api-project-108380595987 \
	--type instrumentation \
	--app app/build/outputs/apk/debug/app-debug.apk \
	--test app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk \
	--device model=Nexus5X,version=26,locale=en,orientation=portrait \
	--verbosity info
