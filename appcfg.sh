#!/bin/sh
set -e
APPENGINE_HOME=$HOME/.gradle/appengine-sdk/appengine-java-sdk-1.9.24
PATH=$APPENGINE_HOME/bin:$PATH
export APPENGINE_HOME PATH
chmod +x $APPENGINE_HOME/bin/*.sh 2>/dev/null || true
exec appcfg.sh --oauth2 $*
