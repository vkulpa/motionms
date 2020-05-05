#!/bin/bash -i

source ~/.bashrc

export VAULT_HOME=~/vault

if [ ! -z "$SDKMAN_DIR" ]; then
  export JVERSION=11.0.9.j9-adpt
  sdk u java ${JVERSION}

  if ! [ $? -eq 0 ]; then
    sdk i java ${JVERSION}
    sdk u java ${JVERSION}
    exit 1
  fi

  export MOTION_HOME_PATH=~/Videos/motion-fs
else
  export MOTION_HOME_PATH=/home/motion
fi

./gradlew clean build "$@"

if [ $? -eq 0 ]; then
  docker-compose build
  docker-compose up -d
fi
