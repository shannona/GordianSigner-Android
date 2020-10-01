#! /usr/bin/env bash

if [ "$(uname)" == "Darwin" ]; then
  echo "It's not supported MacOS for now"
#  echo "Build libwally-core MacOS"
#  ./scripts/build-libwally-core-mac.sh
else
  echo "Build libwally-core Linux"
  ./scripts/build-libwally-core-linux.sh
fi
