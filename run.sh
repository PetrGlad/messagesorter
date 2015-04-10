#!/usr/bin/env bash
set -e
JARS_DIR=${1:-./build/libs}


java -cp $JARS_DIR/messagesorter-0.1.jar:$JARS_DIR/tests.jar petrglad.msgsort.stub.DestinationStub > target.log &
echo $! >> messagesorter.pids
java -cp $JARS_DIR/messagesorter-0.1.jar:$JARS_DIR/tests.jar petrglad.msgsort.stub.SourceStub > source.log &
echo $! >> messagesorter.pids
java -jar $JARS_DIR/messagesorter-0.1.jar --port 9100 9101 --dest 'http://localhost:9200/incoming?timestamp={timestamp}&value={value}' > process.log &
echo $! >> messagesorter.pids

echo "Started jobs. See log files in this directory. ./kill.sh to abort"
