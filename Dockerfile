FROM jetty:9.4-jre11
ADD ./app/target/app-1.0-SNAPSHOT.war /var/lib/jetty/webapps/ROOT.war
CMD java -jar --add-exports=java.base/jdk.internal.misc=ALL-UNNAMED \
--add-exports=java.base/sun.nio.ch=ALL-UNNAMED \
"$JETTY_HOME/start.jar"
