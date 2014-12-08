Getting Started
===============

Build and test the application:

    $ gradle build

Run a local dev server:

    $ gradle appengineRun

Deploy to appengine:

    $ gradle appengineUpdate

Update dispatch config:

    $ ./appcfg.sh update_dispatch swirly-front/src/main/webapp

Full release:

    $ gradle build
    $ ./appcfg.sh -A swirly-uat update swirly-ear/build/exploded-app
    $ ./appcfg.sh -A swirly-uat update_dispatch swirly-front/src/main/webapp
