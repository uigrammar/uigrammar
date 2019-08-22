#!/usr/bin/env bash
cd /test

git clone https://github.com/uigrammar/uigrammar.git

mv uigrammar droidgram
cd droidgram

./gradlew clean build

cd /test
