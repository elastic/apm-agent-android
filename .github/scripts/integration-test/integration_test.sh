#!/usr/bin/env bash

set -euo pipefail

require() {
  local value="$1"
  if [ -z "$value" ]; then
    exit 1
  fi
  echo "$value"
}

es_retrieve_first_item () {
  local index="$1"
  local service_name="$2"
  local response
  response=$(curl "${ES_LOCAL_URL}/$index/_search" -sS \
   -H "Authorization: ApiKey ${ES_LOCAL_API_KEY}" \
   -H "Content-Type: application/json" \
   -d "{\"query\":{\"term\":{\"service.name\":{\"value\":\"$service_name\"}}}}"
  )

  response=$(require "$response")

  local hits
  hits=$(echo "$response" | jq -r '.hits.total.value')

  if [ "$hits" -lt 1 ]; then
    echo ""
  fi

  echo "$response" | jq -r '.hits.hits[0]'
}

launch_app() {
  local app_dir="$1"
  ./gradlew -p "$app_dir" :app:assembleRelease
  adb install -r "$app_dir"/app/build/outputs/apk/release/app-release.apk
  adb shell am start -n co.elastic.otel.android.integration/.MainActivity
}

assert_equals() {
  local expected="$1"
  local actual="$2"
  if [ "$expected" != "$actual" ]; then
    echo "Expected value: '$expected' not matching actual value: '$actual'"
    exit 1
  fi
}

validate_span() {
  local span
  span=$(require "$1")
  local expected_span_name="span name"
  local expected_agent_name="android/java"
  local span_name
  span_name=$(echo "$span" | jq -r '._source.name')
  local agent_name
  agent_name=$(echo "$span" | jq -r '._source.resource.attributes."agent.name"')

  assert_equals "$expected_span_name" "$span_name"
  assert_equals "$expected_agent_name" "$agent_name"
}

validate_log() {
  local log
  log=$(require "$1")
  local expected_body_text="log body"
  local expected_agent_name="android/java"
  local counter_value
  counter_value=$(echo "$log" | jq -r '._source.body.text')
  local agent_name
  agent_name=$(echo "$log" | jq -r '._source.resource.attributes."agent.name"')

  assert_equals "$expected_body_text" "$counter_value"
  assert_equals "$expected_agent_name" "$agent_name"
}

# Main execution
if [ -z "$ES_LOCAL_URL" ] || [ -z "$ES_LOCAL_API_KEY" ]; then
  echo "Must set ES_LOCAL_URL and ES_LOCAL_API_KEY env vars"
  exit 1
fi

app_dir="integration-test"
launch_app "$app_dir"

echo "Awaiting for data to get exported"
sleep 20

span=$(es_retrieve_first_item "traces-*" "integration-test-app")
log=$(es_retrieve_first_item "logs-*" "integration-test-app")

# Storing ES responses
mkdir -p "$app_dir/build/es"
echo "$span" > "$app_dir/build/es/span.json"
echo "$log" > "$app_dir/build/es/log.json"

# Validate data
validate_span "$span"
validate_log "$log"

echo "Integration tests succeeded"