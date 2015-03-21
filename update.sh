#!/bin/sh
set -e
cd $(dirname $0)
wd=$PWD
appid=${1:-'twirly-prd'}
gradle build
patch -p1 <<EOF
--- a/twirly-ear/build/exploded-app/twirly-back-0.1/WEB-INF/appengine-web.xml
+++ b/twirly-ear/build/exploded-app/twirly-back-0.1/WEB-INF/appengine-web.xml
@@ -5,13 +5,13 @@
   <version>1</version>
   <threadsafe>true</threadsafe>
   <instance-class>B2</instance-class>
-  <!--basic-scaling>
+  <basic-scaling>
     <max-instances>1</max-instances>
     <idle-timeout>10m</idle-timeout>
-  </basic-scaling-->
-  <manual-scaling>
+  </basic-scaling>
+  <!--manual-scaling>
     <instances>1</instances>
-  </manual-scaling>
+  </manual-scaling-->
   <system-properties>
     <property name="java.util.logging.config.file" value="WEB-INF/logging.properties"/>
   </system-properties>
EOF
$wd/appcfg.sh -A $appid update twirly-ear/build/exploded-app
$wd/appcfg.sh -A $appid update_dispatch twirly-front/src/main/webapp
