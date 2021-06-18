#!/bin/bash

function exit_if_uncommitted_changes {
  path="$1"
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
  snapshot="$2"

  git_add
  git commit -m "New version $version"
  git tag "v$version"

  set_version "$snapshot"
  git_add
  git commit -m "New snapshot version $snapshot"
}

# Check if uncommitted changes
exit_if_uncommitted_changes "."

new_version="$1"
if [ -z "$new_version" ]; then
  echo "Needs to specify new version as first argument (do not prefix with v). Existing versions: "
  git tag -l
  exit 1
fi
next_snapshot="$2"
if [ -z "$next_snapshot" ]; then
  echo "Needs to specify next snapshot version as second argument (do not suffix with -SNAPSHOT)."
  exit 1
fi

set_version "$new_version"
build_jar
release_version "$new_version" "$next_snapshot-SNAPSHOT"

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
