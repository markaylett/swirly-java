#!/bin/sh
set -e
cd $(dirname $0)
wd=$PWD
appid=${1:-'swirly-prd'}
env=
case "$appid" in
    swirly-dev*)
        env=dev
        ;;
    swirly-uat*)
        env=uat
        ;;
    swirly-prd*)
        env=prd
        ;;
    *)
        echo >&2 "invalid appid: $appid"
        exit 1
        ;;
esac
$GRADLE_HOME/bin/gradle -Penv=$env -q printConfig
$GRADLE_HOME/bin/gradle -Penv=$env build
$wd/appcfg.sh -A $appid update swirly-ear/build/exploded-app
$wd/appcfg.sh -A $appid update_dispatch swirly-ear/build/exploded-app/swirly-front-0.1
