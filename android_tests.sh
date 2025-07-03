set -e
./gradlew -p "instrumentation-test" connectedCheck
./gradlew -p "sample-app" connectedCheck -Pelastic.testing.automated=true
