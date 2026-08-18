#!/usr/bin/env bash

set -euo pipefail

# --------------------------------------------------------------------------
# Crash reporting and deobfuscation end-to-end test
#
# 1. Build the release APK (R8-minified) to produce mapping.txt
# 2. Upload the R8 mapping to Elasticsearch via the Gradle plugin task
#    (the task creates a lookup index and bulk-uploads documents)
# 3. Install the app and launch CrashActivity
# 4. Re-launch the app to export the buffered crash event
# 5. Retrace the exported stacktrace with Kibana and Google's R8 retrace
# 6. Compare both retraced stacktraces
# --------------------------------------------------------------------------

ES_LOCAL_URL=$1
ES_LOCAL_API_KEY=$2
KIBANA_LOCAL_URL=$3
KIBANA_LOCAL_PASSWORD=$4
current_dir=$(pwd)
app_dir="${current_dir%/.github*}/integration-test"
build_dir="$app_dir/build/es/crash-and-deobfuscation"
mkdir -p "$build_dir"

mapping_file="$app_dir/app/build/outputs/mapping/release/mapping.txt"
retrace_cmd="${ANDROID_HOME:-/usr/local/lib/android/sdk}/cmdline-tools/latest/bin/retrace"

if [ ! -f "$retrace_cmd" ]; then
  echo "ERROR: retrace tool not found at: $retrace_cmd"
  echo "Install Android SDK Command-line Tools or set ANDROID_HOME"
  exit 1
fi

es_request() {
  local method="$1"
  local path="$2"
  shift 2
  curl -sS -X "$method" "${ES_LOCAL_URL}${path}" \
    -H "Authorization: ApiKey ${ES_LOCAL_API_KEY}" \
    -H "Content-Type: application/json" \
    "$@"
}

kibana_request() {
  local method="$1"
  local path="$2"
  shift 2
  curl --fail-with-body -sS -X "$method" "${KIBANA_LOCAL_URL}${path}" \
    --user "elastic:${KIBANA_LOCAL_PASSWORD}" \
    -H "Content-Type: application/json" \
    -H "kbn-xsrf: true" \
    -H "x-elastic-internal-origin: true" \
    "$@"
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
gradle_args=(-PesEndpoint="$ES_LOCAL_URL" -PesApiKey="$ES_LOCAL_API_KEY")
if [ "${WITH_DESUGARING:-false}" = "true" ]; then
  gradle_args+=("-PwithDesugaring=true")
fi

"$app_dir/gradlew" -p "$app_dir" :app:assembleRelease "${gradle_args[@]}"

if [ ! -f "$mapping_file" ]; then
  echo "ERROR: Mapping file not found at: $mapping_file"
  exit 1
fi

# ================= Step 2: Upload mapping to Elasticsearch ====================

echo "=== Uploading mapping to Elasticsearch ==="
es_request DELETE "/.android-r8-mappings-*" > /dev/null 2>&1 || true
"$app_dir/gradlew" -p "$app_dir" :app:releaseUploadMappingToElasticsearch "${gradle_args[@]}"

map_index=$(es_request GET "/_cat/indices/.android-r8-mappings-*?h=index" | tr -d '[:space:]')
assert_not_empty "$map_index" "No .android-r8-mappings-* index found in Elasticsearch"
echo "Mapping index: $map_index"

es_request POST "/${map_index}/_refresh" > /dev/null
count_response=$(es_request GET "/${map_index}/_count")
echo "$count_response" > "$build_dir/mapping_index_count.json"
doc_count=$(echo "$count_response" | jq '.count')
echo "Mapping index contains $doc_count documents"
if [ "$doc_count" -lt 1 ]; then
  echo "ERROR: Mapping index has no documents"
  exit 1
fi

map_sample_response=$(es_request GET "/${map_index}/_search?size=1")
echo "$map_sample_response" > "$build_dir/mapping_index_sample.json"
sample_doc=$(echo "$map_sample_response" | jq -r '.hits.hits[0]._source')
echo "Sample document: $sample_doc"

obf_class=$(echo "$sample_doc" | jq -r '.obfuscated_class')
assert_not_empty "$obf_class" "Sample document missing obfuscated_class field"
echo "Sample obfuscated_class: $obf_class"

has_build_id=$(echo "$sample_doc" | jq 'has("build_id")')
if [ "$has_build_id" = "true" ]; then
  echo "ERROR: Documents should not contain build_id field (it is encoded in the index name)"
  exit 1
fi
echo "Verified: documents do not contain build_id field"

# =================== Step 3: Crash the app ================================

echo "=== Installing app and triggering crash ==="
es_request POST "/logs-generic.otel*/_delete_by_query?conflicts=proceed&refresh=true&allow_no_indices=true" \
  -d '{
    "query": {
      "bool": {
        "filter": [
          {"term": {"service.name": {"value": "integration-test-app"}}},
          {"term": {"event_name": {"value": "app.crash"}}}
        ]
      }
    }
  }' > /dev/null 2>&1 || true

# Clear buffered telemetry from earlier local runs before installing this build.
adb uninstall co.elastic.otel.android.integration > /dev/null 2>&1 || true
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

# ========= Step 3b: Re-launch app to export buffered crash data ===========
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
crash_doc_response=""
crash_query='{
  "query": {
    "bool": {
      "filter": [
        {"term": {"service.name": {"value": "integration-test-app"}}},
        {"term": {"event_name": {"value": "app.crash"}}}
      ]
    }
  },
  "sort": [{"@timestamp": {"order": "desc"}}],
  "size": 1
}'
for i in $(seq 1 15); do
  sleep 4
  crash_doc_response=$(es_request POST "/logs-generic.otel*/_search" \
    -d "$crash_query" 2>/dev/null || true)
  crash_event=$(echo "$crash_doc_response" | jq -r '.hits.hits[0] // empty' 2>/dev/null || true)
  if [ -n "$crash_event" ]; then
    echo "Crash event found in ES after $((i * 4)) seconds"
    break
  fi
  echo "Waiting for crash event in ES... ($i/15)"
done

echo "$crash_doc_response" > "$build_dir/crash_event.json"

if [ -z "$crash_event" ]; then
  echo "ERROR: Crash event not found in ES after 60 seconds"
  echo "--- logcat after re-launch ---"
  adb logcat -d 2>&1 | tail -100
  exit 1
fi

# ================ Step 4: Extract crash telemetry =========================

stacktrace_file="$build_dir/crash_stacktrace.txt"
jq -j '.hits.hits[0]._source.attributes."exception.stacktrace" // empty' \
  "$build_dir/crash_event.json" > "$stacktrace_file"

echo "=== Obfuscated stacktrace ==="
cat "$stacktrace_file"
echo ""

if [ ! -s "$stacktrace_file" ]; then
  echo "ERROR: Crash event has no exception.stacktrace"
  exit 1
fi

exception_type=$(jq -r '.hits.hits[0]._source.attributes."exception.type" // empty' \
  "$build_dir/crash_event.json")
assert_not_empty "$exception_type" "Crash event has no exception.type field"

build_id=$(jq -r '.hits.hits[0]._source.resource.attributes."app.build_id" // empty' \
  "$build_dir/crash_event.json")
assert_not_empty "$build_id" "Crash event has no app.build_id"

if [ "$map_index" != ".android-r8-mappings-${build_id}" ]; then
  echo "ERROR: Mapping index '$map_index' does not match crash build ID '$build_id'"
  exit 1
fi

# =================== Step 5: Google R8 retrace =============================

google_retrace_file="$build_dir/google_retrace_output.txt"
"$retrace_cmd" "$mapping_file" "$stacktrace_file" > "$google_retrace_file"

echo "=== Google R8 retrace output ==="
cat "$google_retrace_file"
echo ""

obfuscated_stacktrace=$(cat "$stacktrace_file")
google_retraced=$(cat "$google_retrace_file")
if [ "$obfuscated_stacktrace" = "$google_retraced" ]; then
  echo "ERROR: Google R8 retrace output is identical to the obfuscated input"
  exit 1
fi

# ======================= Step 6: Kibana retrace ============================

kibana_response_file="$build_dir/kibana_retrace_response.json"
kibana_retrace_file="$build_dir/kibana_retrace_output.txt"
kibana_payload=$(jq -n \
  --rawfile stacktrace "$stacktrace_file" \
  --arg build_id "$build_id" \
  '{stacktrace: $stacktrace, build_id: $build_id}')

kibana_request POST "/internal/client_apps/android/retrace" \
  -d "$kibana_payload" > "$kibana_response_file"

jq -j '.retraced // empty' "$kibana_response_file" > "$kibana_retrace_file"
if [ ! -s "$kibana_retrace_file" ]; then
  echo "ERROR: Kibana retrace response has no retraced stacktrace"
  cat "$kibana_response_file"
  exit 1
fi

echo "=== Kibana retrace output ==="
cat "$kibana_retrace_file"
echo ""

# ==================== Step 7: Compare outputs ==============================

# Command substitution removes trailing newlines. Kibana and Google's tool
# differ only in how many terminal newlines they preserve.
kibana_retraced=$(cat "$kibana_retrace_file")

if [ "$google_retraced" != "$kibana_retraced" ]; then
  normalized_google="$build_dir/google_retrace_normalized.txt"
  normalized_kibana="$build_dir/kibana_retrace_normalized.txt"
  printf '%s\n' "$google_retraced" > "$normalized_google"
  printf '%s\n' "$kibana_retraced" > "$normalized_kibana"

  echo "ERROR: Kibana retrace output does not match Google R8 retrace"
  diff -u "$normalized_google" "$normalized_kibana" || true
  exit 1
fi

echo ""
echo "=== Kibana output matches Google R8 retrace ==="
echo "=== Crash reporting and deobfuscation test PASSED ==="
