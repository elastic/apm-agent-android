set -e
./gradlew publishToMavenLocal
./gradlew test
./gradlew -p "android-test" test
./gradlew -p "build-tools" test
