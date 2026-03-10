set -e

echo "Verifying that apiCheck and spotlessCheck are wired into check for all modules..."
projects_output=$(./gradlew projects)
dry_run_output=$(./gradlew check --dry-run)

modules=$(echo "$projects_output" | grep -oE "^project ':[^']+'" \
  | grep -v ':internal-tools' \
  | sed "s/^project '//;s/'$//")

missing=()
for module in $modules; do
  if ! echo "$dry_run_output" | grep -q "^${module}:check SKIPPED"; then
    continue
  fi
  if ! echo "$dry_run_output" | grep -q "^${module}:apiCheck SKIPPED"; then
    missing+=("${module}:apiCheck")
  fi
  if ! echo "$dry_run_output" | grep -q "^${module}:spotlessCheck SKIPPED"; then
    missing+=("${module}:spotlessCheck")
  fi
done

if [ ${#missing[@]} -ne 0 ]; then
  echo "ERROR: The following tasks are not wired into 'check':"
  for task in "${missing[@]}"; do
    echo "  - $task"
  done
  exit 1
fi
echo "All modules have apiCheck and spotlessCheck wired into check."

./gradlew check