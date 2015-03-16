Getting Started
===============

Build Tools
-----------

Install Gradle and add to your profile:

    GRADLE_HOME=$HOME/gradle; export GRADLE_HOME
    PATH=$PATH:$GRADLE_HOME/bin; export PATH

Github Fork
-----------

Add remote upstream to fork:

    $ git remote add upstream git@github.com:swirlycloud/twirlyj.git

Sync local fork:

    $ git fetch upstream
    $ git checkout master
    $ git rebase upstream/master

Build and Test
--------------

Generate Eclipse project files:

    $ gradle eclipse

Build and test the application:

    $ gradle build

Or if you do not have access to the Internet:

    $ gradle build --offline

Run a local dev server:

    $ gradle appengineRun

AppEngine Deployment
--------------------

Deploy to appengine:

    $ gradle appengineUpdate

Update dispatch config:

    $ ./appcfg.sh update_dispatch twirly-front/src/main/webapp

Full release:

    $ ./update.sh twirly-dev
    $ ./update.sh twirly-uat
    $ ./update.sh twirly-prd
