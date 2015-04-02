#!/bin/sh
set -e
cd $(dirname $0)
wd=$PWD
appid=${1:-'twirly-prd'}
env=
case "$appid" in
    twirly-dev)
        env=dev
        ;;
    twirly-uat)
        env=uat
        ;;
    twirly-prd)
        env=prd
        ;;
    *)
        echo >&2 "invalid appid: $appid"
        exit 1
        ;;
esac
$GRADLE_HOME/bin/gradle -Penv=$env -q printConfig
$GRADLE_HOME/bin/gradle -Penv=$env build
$wd/appcfg.sh -A $appid update twirly-ear/build/exploded-app
$wd/appcfg.sh -A $appid update_dispatch twirly-ear/build/exploded-app/twirly-front-0.1
