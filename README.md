Getting Started
===============

Build and test the application:

    $ gradle build

Run a local dev server:

    $ gradle appengineRun

Deploy to appengine:

    $ gradle appengineUpdate

Update dispatch config:

    $ ./appcfg.sh update_dispatch twirly-front/src/main/webapp

Full release:

    $ ./update.sh twirly-dev
    $ ./update.sh twirly-uat
    $ ./update.sh twirly-prd
