#!/bin/bash

set -euo pipefail

./gradlew \
    :aztec:prepareToPublishToS3 $(prepare_to_publish_to_s3_params) \
    :aztec:publish

# Add meta-data for the published version so we can use it in subsequent steps
cat ./aztec/build/published-version.txt | buildkite-agent meta-data set "PUBLISHED_AZTEC_VERSION"
