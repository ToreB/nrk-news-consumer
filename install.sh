#!/bin/bash

function exit_if_uncommitted_changes {
  path="$1"
  git fetch
  git status
  git diff-index --quiet HEAD -- "$path"
  ret_val=$?
  if [ $ret_val == 1 ]; then
    echo "There are uncommitted git changes in '$path'"
    exit 1
  fi
}

function set_version {
  version="$1"

  npm version "$version" --no-git-tag-version
  ret_val=$?
  if [ $ret_val == 1 ]; then
    echo "Unable to set npm version."
    exit 1
  fi

  set -o pipefail
  mvn versions:set -DnewVersion="$version" -DgenerateBackupPoms=false | tee
  ret_val=$?
  set +o pipefail
  if [ $ret_val == 1 ]; then
    echo "Unable to set mvn version."
    exit 1
  fi
}

function build_jar {
  set -o pipefail
  mvn clean verify | tee
  ret_val=$?
  set +o pipefail

  if [ $ret_val == 1 ]; then
    exit 1;
  fi
}

function git_add {
  git add pom.xml
  git add package.json
  git add package-lock.json
}

function release_version {
  version="$1"

  git tag "v$version"

  set_version "0.0.0-SNAPSHOT"
}

# Check if uncommitted changes
exit_if_uncommitted_changes "."

new_version="$1"
if [ -z "$new_version" ]; then
  echo "Needs to specify new version as first argument (do not prefix with v). Most recent existing versions: "
  git tag --sort=-v:refname | head -n 3
  exit 1
fi

set_version "$new_version"
build_jar
release_version "$new_version"

app_name='nrk-news-consumer'
jar_file="$app_name.jar"
install_dir="P:/$app_name/"

mkdir -p $install_dir
mkdir -p $install_dir/logs

echo "Moving scripts to $install_dir"
cp -rf bin/* $install_dir/bin

echo "Stopping running application"
cd $install_dir
sh bin/stop.sh
cd -

echo "Moving $jar_file to $install_dir"
cp -f target/$jar_file $install_dir

cd $install_dir

echo "Start application"
sh bin/start.sh

echo "Finished installing $app_name"
