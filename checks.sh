set -e
./gradlew check
./gradlew -p "sample-app" :app:assembleRelease