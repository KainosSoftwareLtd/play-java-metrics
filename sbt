java -Xms2048M -Xmx2048M -Xss1M -noverify -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=2048M -jar `dirname $0`/sbt-launch.jar "$@"
