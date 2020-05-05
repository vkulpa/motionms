# MotionMS software

## Components

- [motion](https://motion-project.github.io/) software
- MQTT
- MotionMS - this software
- ViberBot
- HashiCorp Vault

## Overview & idea

Motion software publishes events to MQTT. MotionMS is listening preconfigured topics in MQTT and sends messages via ViberBot.
All required credentials MotionMS gets from HashiCorp Vault

## Where to start from

First of all, take a look at `tools/build_and_run.sh` and second - in `docker-compose.yaml`


## First run

Create a vault directory, set permissions and configure its by running http://localhost:8200

```bash
$ mkdir ~/vault
$ export VAULT_HOME=~/vault
$ chmod 777 ~/valut
$ docker-compose up vault
```

Once everything is successfully visit vault's UI and configure secrets. Under `cybbyhole` secret engine, create `motionms/viber` path and set `token` and `adminProfileId` values.

![vault-conf-example](https://raw.githubusercontent.com/vkulpa/motionms/master/tools/vault-example.png)