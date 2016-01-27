#!/usr/bin/env bash
XLSCRIPTER=`dirname $0`/xlscripter.jar
if [[ -f "$XLSCRIPTER" ]]; then
    echo Launching xlscripter.jar
    java -jar $XLSCRIPTER $*
else
    echo "Could not locate $XLSCRIPTER"
fi
