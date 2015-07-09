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

Tomcat Development
------------------

Download and extract Apache Tomcat v8.0.

In Eclipse, add the new Server Runtime Environment using the following dialog:

    Preferences => Server => Runtime Environments

Ensure that the Servers tab is visible in the Eclipse workspace. You can add it to your workspace by
navigating the following menu item hierarchy:

    Window => Show View => Other... => Server => Servers

If no servers are configured, then the tab should report a message similar to the following:

    No servers are available. Click this link to create a new server...

Click on this link or, alternatively, use the right-click context menu to create a new server.

You should now be presented with a dialog box containing a tree-view of the supported server
adapters. Select "Tomcat v8.0 Server" from this tree-view and set the server-name to "twirly".

Click Next and add `twirly-front` and `twirly-back` to the list of configured resources.

Click Finish to complete the wizard.

You should now have a Servers project in your Eclipse workspace containing a `twirly-config`
folder. The `path` attributes of the following contexts in `servers.xml` should be as follows:

    <Context docBase="twirly-back" path="/api" ... />
    <Context docBase="twirly-front" path="/" ... />

Add the following element to the `Host` section to enable Single Sign On (SSO):

    <Valve className="org.apache.catalina.authenticator.SingleSignOn" />

Add roles and a user for testing to `tomcat-users.xml`:

    <role rolename="tomcat"/>
    <role rolename="user"/>
    <role rolename="trader"/>
    <role rolename="admin"/>
    <user username="mark.aylett@gmail.com" password="test" roles="tomcat,user,trader,admin"/>

You should now be able to start your Tomcat server in debug mode from the Servers tab.

JDBC Realm
----------

You can configure the `JDBCRealm` instead of the `UserDatabaseRealm` as follows:

    $ cp mysql-connector-java-x.y.z.jar $CATALINA_HOME/lib/

    <Realm className="org.apache.catalina.realm.JDBCRealm"
           driverName="org.gjt.mm.mysql.Driver"
           connectionURL="jdbc:mysql://localhost/twirly?user=root&amp;password="
           userTable="User_t" userNameCol="email" userCredCol="pass"
           userRoleTable="UserGroup_v" roleNameCol="group_" />

OS X Daemons
------------

After installing mysql-server and memcached from Mac Ports, start the daemons using the following
commands:

    $ auso launchctl load -w /Library/LaunchDaemons/org.macports.mysql56-server.plist 
    $ sudo launchctl load -w /Library/LaunchDaemons/org.macports.memcached.plist
