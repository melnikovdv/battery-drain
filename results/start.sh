#!/bin/sh
set -eu

adb shell pm clear org.mlayer.batterydrain
adb shell dumpsys batterystats --reset 

# docker run -p 9999 gcr.io/android-battery-historian/stable:3.0 --port 9999
