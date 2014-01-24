#!/bin/sh

CLASSPATH=conf:jianghu-sr.jar
for f in lib/*.jar; do
  CLASSPATH=${CLASSPATH}:$f
done
java -server -Xmx2G -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing -XX:CMSIncrementalDutyCycleMin=0 -XX:CMSIncrementalDutyCycle=10 -XX:MaxNewSize=256M -XX:MaxPermSize=256M -cp $CLASSPATH "$@"
