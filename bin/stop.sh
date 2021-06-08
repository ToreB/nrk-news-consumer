#!/bin/bash

install_dir=P:/nrk-news-consumer

pid=$(cat $install_dir/pid.txt)
if [ -z "$pid" ]; then
  exit 0
fi

echo "Stopping nrk-news-consumer with pid $pid"
kill $pid
if [ $? == 1 ]; then
  exit 0
fi

echo "Waiting for application to shutdown"
while true; do
  res=$(ps -ep $pid)
  if [ $? == 1 ]; then
    break
  fi
  sleep 1
done

echo "Application shutdown"
rm -f $install_dir/pid.txt
