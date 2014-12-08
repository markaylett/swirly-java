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

    $ ./update.sh swirly-dev
    $ ./update.sh swirly-uat
    $ ./update.sh swirly-prd
