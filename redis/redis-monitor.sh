#!/bin/bash

cd `dirname $0`
cd ../target/redis-*
./src/redis-cli monitor
