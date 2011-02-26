#!/bin/bash

cd `dirname $0`

./redis-stop.sh

echo ==============
echo STARTING REDIS
echo ==============

mkdir ../target
rm -f -r ../target/redis-*
tar xzf redis-*.tar.gz -C ../target
cd ../target/redis-*
make
./src/redis-server ../../redis/redis.conf
sleep 2

echo =============
echo REDIS STARTED
echo =============
