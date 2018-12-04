#!/bin/bash
function tabname {
  echo -n -e "\033]0;$1\007"
}
tabname "oscars export_timestamps"

# Find the OSCARS export_timestamps.jar file
JARFILE=""
# Artifact location in target directory, for running in-tree
LOCALJAR=`echo target/export_timestamps-*.jar`
if [ -e $LOCALJAR ]; then
    JARFILE=$LOCALJAR
fi
# Make sure we can find it
if [ "x$JARFILE" = "x" ]; then
    echo "Unable to locate OSCARS export_timestamps.jar file"
    exit 1
fi

java -Xmx512m -jar ${JARFILE} $1 $2 $3 $4 $5
