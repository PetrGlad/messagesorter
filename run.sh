#!/usr/bin/env bash
set -e
JARS_DIR=${1:-.}


java -cp $JARS_DIR/messagesorter-0.1.jar:$JARS_DIR/tests.jar petrglad.msgsort.stub.DestinationStub > target.log &
java -cp $JARS_DIR/messagesorter-0.1.jar:$JARS_DIR/tests.jar petrglad.msgsort.stub.SourceStub > source.log &
java -jar $JARS_DIR/messagesorter-0.1.jar --port 9100 9101 --dest 'http://localhost:9200/incoming?timestamp={timestamp}&value={value}'



