#!/usr/bin/env bash

set -euo pipefail

# --------------------------------------------------------------------------
# Crash reporting end-to-end test
#
# 1. Build the release APK (R8-minified)
# 2. Install the app and launch CrashActivity (crashes after 3s delay)
# 3. Re-launch the app so the agent exports the buffered crash event
# 4. Verify the crash event appears in Elasticsearch
# --------------------------------------------------------------------------

ES_LOCAL_URL=$1
ES_LOCAL_API_KEY=$2
current_dir=$(pwd)
app_dir="${current_dir%/.github*}/integration-test"
build_dir="$app_dir/build/es/crash-test"
mkdir -p "$build_dir"

es_request() {
  local method="$1"
  local path="$2"
  shift 2
  curl -sS -X "$method" "${ES_LOCAL_URL}${path}" \
    -H "Authorization: ApiKey ${ES_LOCAL_API_KEY}" \
    -H "Content-Type: application/json" \
    "$@"
}

esql_query() {
  local query="$1"
  es_request POST "/_query?format=json" \
    -d "$(jq -n --arg q "$query" '{query: $q}')"
}

assert_not_empty() {
  local value="$1"
  local msg="$2"
  if [ -z "$value" ]; then
    echo "ERROR: $msg"
    exit 1
  fi
}

# ======================== Step 1: Build release APK ========================

echo "=== Building release APK ==="
gradle_args=()
if [ "${WITH_DESUGARING:-false}" = "true" ]; then
  gradle_args+=("-PwithDesugaring=true")
fi

"$app_dir/gradlew" -p "$app_dir" :app:assembleRelease ${gradle_args[@]+"${gradle_args[@]}"}

# ======================== Step 2: Crash the app ============================

echo "=== Installing app and triggering crash ==="
adb install -r "$app_dir/app/build/outputs/apk/release/app-release.apk"
adb logcat -c 2>/dev/null || true

adb shell am start -n co.elastic.otel.android.integration/.CrashActivity 2>/dev/null || true

logcat_file="$build_dir/logcat.txt"
for i in $(seq 1 10); do
  sleep 2
  adb logcat -d > "$logcat_file" 2>&1
  if grep -q "AndroidRuntime.*FATAL EXCEPTION" "$logcat_file"; then
    break
  fi
  echo "Waiting for crash in logcat... ($i/10)"
done

if ! grep -q "AndroidRuntime.*FATAL EXCEPTION" "$logcat_file"; then
  echo "ERROR: No crash found in logcat after 20 seconds"
  echo "--- logcat ---"
  cat "$logcat_file"
  exit 1
fi

echo "Crash detected in logcat"

# ========= Step 3: Re-launch app to export buffered crash data ============
#
# The crash instrumentation buffers the crash event to disk during the crash.
# The actual export to the collector (and then to ES) happens on the next app
# launch. Launch MainActivity (which doesn't crash) and wait for the data to
# be exported.

echo "=== Re-launching app to export crash data ==="
sleep 3
adb shell am force-stop co.elastic.otel.android.integration 2>/dev/null || true
sleep 2
adb shell am start -n co.elastic.otel.android.integration/.MainActivity

echo "Waiting for crash data to be exported..."
crash_event=""
crash_event_response=""
for i in $(seq 1 15); do
  sleep 4
  crash_event_response=$(es_request POST "/_query?format=json" \
    -d '{
      "query": "FROM logs-generic.otel* | WHERE service.name == \"integration-test-app\" AND event_name == \"device.crash\" | LIMIT 1"
    }' 2>/dev/null || true)
  crash_event=$(echo "$crash_event_response" | jq -r '.values[0] // empty' 2>/dev/null || true)
  if [ -n "$crash_event" ]; then
    echo "Crash event found in ES after $((i * 4)) seconds"
    break
  fi
  echo "Waiting for crash event in ES... ($i/15)"
done

echo "$crash_event_response" > "$build_dir/crash_event_esql.json"

crash_doc_response=$(es_request POST "/logs-generic.otel*/_search" \
  -d '{
    "query": {
      "bool": {
        "filter": [
          {"term": {"service.name": {"value": "integration-test-app"}}},
          {"term": {"event_name": {"value": "device.crash"}}}
        ]
      }
    },
    "size": 1
  }' 2>/dev/null || true)
echo "$crash_doc_response" > "$build_dir/crash_event.json"

if [ -z "$crash_event" ]; then
  echo "ERROR: Crash event not found in ES after 60 seconds"
  echo "--- logcat after re-launch ---"
  adb logcat -d 2>&1 | tail -100
  exit 1
fi

# ======================== Step 4: Validate =================================

stacktrace=$(echo "$crash_doc_response" | jq -r '.hits.hits[0]._source.attributes."exception.stacktrace" // empty')
assert_not_empty "$stacktrace" "Crash event has no exception.stacktrace field"

exception_type=$(echo "$crash_doc_response" | jq -r '.hits.hits[0]._source.attributes."exception.type" // empty')
assert_not_empty "$exception_type" "Crash event has no exception.type field"

echo ""
echo "=== Crash event validated ==="
echo "Exception type: $exception_type"
echo "Stacktrace (first 5 lines):"
echo "$stacktrace" | head -5
echo ""
echo "=== Crash reporting test PASSED ==="
