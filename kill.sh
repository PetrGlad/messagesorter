#!/usr/bin/env bash

# Having more time one come up with better idea. Just make it work ritgh now.
cat messagesorter.pids | xargs kill
rm messagesorter.pids
