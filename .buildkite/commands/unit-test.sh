#!/bin/bash

set -euo pipefail

echo "--- Unit Test"
./gradlew --stacktrace aztec:testRelease

