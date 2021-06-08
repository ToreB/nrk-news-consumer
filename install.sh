#!/bin/bash

first_arg=$1
if [ -z "$first_arg" ]; then
  mvn clean verify | tee
fi

app_name='nrk-news-consumer'
jar_file="$app_name.jar"
install_dir="P:/$app_name/"

mkdir -p $install_dir
mkdir -p $install_dir/logs

echo "Stopping running application"
cd $install_dir
sh bin/stop.sh
cd -

echo "Moving $jar_file to $install_dir"
cp -f target/$jar_file $install_dir

echo "Moving scripts to $install_dir"
cp -rf bin/* $install_dir/bin

cd $install_dir

echo "Start application"
sh bin/start.sh

echo "Finished installing $app_name"
