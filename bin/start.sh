#!/bin/bash

install_dir=P:/nrk-news-consumer

nohup java -Xmx512m -jar $install_dir/nrk-news-consumer.jar > /dev/null 2>&1 &
echo $! > $install_dir/pid.txt