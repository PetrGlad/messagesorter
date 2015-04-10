#!/usr/bin/env bash

gradle integrationTestJars
zip messagesorter.zip build/libs/*.jar ./run.sh ./kill.sh
