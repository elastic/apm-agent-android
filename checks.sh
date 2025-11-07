set -e
./gradlew check
./gradlew -p "integration-test" :app:assembleRelease