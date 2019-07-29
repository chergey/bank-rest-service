#!/bin/sh

#Copied from https://github.com/appropriate/docker-jetty/blob/master/9.4-jre8/generate-jetty-start.sh


if [ -z "$JETTY_START" ] ; then
	JETTY_START=$JETTY_BASE/jetty.start
fi
rm -f $JETTY_START
/docker-entrypoint.sh --dry-run | sed 's/\\$//' > $JETTY_START