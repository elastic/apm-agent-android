#!/bin/bash -e

port_to_wait=$1
max_seconds_to_wait=$2
waited_seconds=0
timedout=0
while ! nc -z localhost "$port_to_wait"; do
  if [ $waited_seconds == "$max_seconds_to_wait" ]; then
    timedout=1
    break
  fi
  echo "$waited_seconds/$max_seconds_to_wait seconds waiting for port $port_to_wait to open"
  sleep 1
  waited_seconds=$((waited_seconds+1))
done
if [ $timedout == 1 ]; then
  echo "Timeout waiting for the port $port_to_wait to open"
  exit 1
fi
