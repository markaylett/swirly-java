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

    $ git remote add upstream git@github.com:swirlycloud/swirlyj.git

Sync local fork:

    $ git checkout master
    $ git fetch upstream
    $ git rebase upstream/master
    $ git push origin

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

    $ ./appcfg.sh update_dispatch swirly-front/src/main/webapp

Full release:

    $ ./update.sh swirly-dev
    $ ./update.sh swirly-uat
    $ ./update.sh swirly-prd

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
adapters. Select "Tomcat v8.0 Server" from this tree-view and set the server-name to "swirly".

Click Next and add `swirly-front` and `swirly-back` to the list of configured resources.

Click Finish to complete the wizard.

You should now have a Servers project in your Eclipse workspace containing a `swirly-config`
folder. The `path` attributes of the following contexts in `servers.xml` should be as follows:

    <Context docBase="swirly-back" path="/back" ... />
    <Context docBase="swirly-front" path="/" ... />

Add the following element to the `Host` section to enable Single Sign On (SSO):

    <Valve className="org.apache.catalina.authenticator.SingleSignOn" />

Add roles and a user for testing to `tomcat-users.xml`:

    <role rolename="tomcat"/>
    <role rolename="user"/>
    <role rolename="trader"/>
    <role rolename="admin"/>
    <user username="mark.aylett@gmail.com" password="test" roles="tomcat,user,trader,admin"/>

You should now be able to start your Tomcat server in debug mode from the Servers tab.

OS X Daemons
------------

After installing mysql-server and memcached from Mac Ports, start the daemons using the following
commands:

    $ sudo launchctl load -w /Library/LaunchDaemons/org.macports.mysql56-server.plist
    $ sudo launchctl load -w /Library/LaunchDaemons/org.macports.memcached.plist

JDBC Realm
----------

You can configure the `JDBCRealm` instead of the `UserDatabaseRealm` as follows:

    $ cp mysql-connector-java-x.y.z.jar $CATALINA_HOME/lib/

    <Realm className="org.apache.catalina.realm.JDBCRealm"
           driverName="org.gjt.mm.mysql.Driver"
           connectionURL="jdbc:mysql://localhost/swirly?user=root&amp;password="
           userTable="User_t" userNameCol="email" userCredCol="pass"
           userRoleTable="UserGroup_v" roleNameCol="group_" />

Memcached
---------

    $ cp spymemcached-x.y.z.jar $CATALINA_HOME/lib/
    $ cp javax.json-x.y.z.jar $CATALINA_HOME/lib/

Environments
------------

1. Create a project... Show advanced options... App Engine location
2. Permissions => Add Member
3. Compute => App Engine => Settings
  1. Application settings => Logs retention
  2. Custom domains => Add a custom domain
