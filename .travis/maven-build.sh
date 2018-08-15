#! /bin/bash

if [ "$TRAVIS_BRANCH" == "master" ]; then
    mvn -Dbuild.version="$PROVIDER_VERSION" -U package
else
    mvn -Dbuild.version="$PROVIDER_VERSION-RC-$TRAVIS_BUILD_ID" -U package
fi