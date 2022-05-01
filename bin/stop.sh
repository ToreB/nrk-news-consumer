#!/bin/bash

app_url=http://localhost:9090

function get_application_status {
  local status=$(curl --write-out "%{http_code}" --silent --output /dev/null "$app_url/actuator/health")
  echo $status
}

app_status=$(get_application_status)
if [[ "$app_status" != "200" ]]; then
  echo "Application not running."
  exit 0
fi

echo "Stopping nrk-news-consumer."
shutdown_status=$(curl --write-out "%{http_code}" --silent --output /dev/null -X POST "$app_url/actuator/shutdown")
if [[ "$shutdown_status" == "200" ]]; then
  echo "Application shutdown initiated."
else
  echo "Error shutting down application. Received status ${shutdown_status}."
  exit 1
fi

echo "Waiting for application to shutdown."
while true; do
  status=$(get_application_status)
  if [[ "$status" != "200" ]]; then
    break
  fi
  sleep 1
done

echo "Application shutdown."
