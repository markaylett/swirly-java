#!/bin/sh

set -e

BOOTSTRAP_VER=3.3.5
FONT_AWESOME_VER=4.4.0
JQUERY_VER=1.11.3
REACT_VER=0.13.3

clean() {
    rm -f bootstrap.zip
    rm -fR bootstrap-$BOOTSTRAP_VER/
    rm -f font-awesome.zip
    rm -fR font-awesome-$FONT_AWESOME_VER/
}

trap clean 0

cd src/main/webapp

curl https://codeload.github.com/twbs/bootstrap/zip/v$BOOTSTRAP_VER >bootstrap.zip
unzip bootstrap.zip

curl https://fortawesome.github.io/Font-Awesome/assets/font-awesome-$FONT_AWESOME_VER.zip >font-awesome.zip
unzip font-awesome.zip

mkdir -p css fonts js less scss
rm fonts/*
rm less/*
rm scss/*

# CSS.

cp bootstrap-$BOOTSTRAP_VER/dist/css/*.min.css css/
cp font-awesome-$FONT_AWESOME_VER/css/*.min.css css/

# Fonts.

cp bootstrap-$BOOTSTRAP_VER/dist/fonts/* fonts/
cp font-awesome-$FONT_AWESOME_VER/fonts/* fonts/

# JS.

cp bootstrap-$BOOTSTRAP_VER/dist/js/*.min.js js/

pushd js
curl https://code.jquery.com/jquery-$JQUERY_VER.min.js >jquery.min.js
curl https://cdnjs.cloudflare.com/ajax/libs/react/$REACT_VER/react.min.js >react.min.js
curl https://raw.githubusercontent.com/bassjobsen/Bootstrap-3-Typeahead/master/bootstrap3-typeahead.min.js >bootstrap3-typeahead.min.js
popd

# Less.

cp font-awesome-$FONT_AWESOME_VER/less/* less/

# Scss.

cp font-awesome-$FONT_AWESOME_VER/scss/* scss/
