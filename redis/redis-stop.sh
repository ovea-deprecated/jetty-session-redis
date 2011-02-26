#!/bin/bash

if [ -f /tmp/redis.pid ]
then
    P=`cat /tmp/redis.pid`
    echo "Stopping REDIS process $P"
    rm /tmp/redis.pid
    kill $P
fi
