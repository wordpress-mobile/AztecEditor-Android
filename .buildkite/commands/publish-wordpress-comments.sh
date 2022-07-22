#!/bin/bash

set -euo pipefail

# Retrieve data from previous steps
PUBLISHED_AZTEC_VERSION=$(buildkite-agent meta-data get "PUBLISHED_AZTEC_VERSION")

./gradlew \
    -PaztecVersion="$PUBLISHED_AZTEC_VERSION" \
    :wordpress-comments:prepareToPublishToS3 $(prepare_to_publish_to_s3_params) \
    :wordpress-comments:publish
