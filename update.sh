#!/bin/sh
set -e
appid=${1:-'swirly-prd'}
gradle build
patch -p1 <<EOF
--- a/swirly-ear/build/exploded-app/swirly-back-0.1/WEB-INF/appengine-web.xml
+++ b/swirly-ear/build/exploded-app/swirly-back-0.1/WEB-INF/appengine-web.xml
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
./appcfg.sh -A $appid update swirly-ear/build/exploded-app
./appcfg.sh -A $appid update_dispatch swirly-front/src/main/webapp
