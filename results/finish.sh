#!/bin/sh
set -eu

if (( $# == 1 )); then

    if [[ $1 == old ]]; then        
        adb bugreport > ./bugreport.txt        
    else 
        if [[ $1 == new ]]; then
            adb bugreport ./bugreport.zip
        else
            echo "Param should be 'new' or 'old'"
            exit 1
        fi
    fi

    adb shell dumpsys batterystats > ./batterystats.txt
    adb pull /sdcard/Android/data/org.mlayer.batterydrain/cache/primary.log ./primary.log
else
  echo "Usage: ./finish.sh [old|new]"  
fi