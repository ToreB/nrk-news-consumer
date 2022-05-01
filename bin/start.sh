#!/bin/bash

install_dir=P:/nrk-news-consumer

cd $install_dir

run_command=$(cat bin/run_command)
nohup $run_command > /dev/null 2>&1 &