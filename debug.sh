#!/bin/sh
GRADLE_OPTS='-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y' gradle appengineRun
