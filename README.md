# UI Grammars -- ICSE 2020

## Building 

The `docker` folder contains docker files which can be used to build the tool.

First build the base container from  `./docker/base` with:

```
docker build -t base .
```

Then build the DroidMate-2 container from  `./docker/dm` with:

```
docker build -t dm .
```

Finally build the tool container from `/docker` with:

```
docker build -t uigrammar .
```

## Running with docker

To run the containers the docker user must have root privileges to access the KVM and start an emulator.

For RQ 1 and 2:
```
docker run --privileged -d -v <APP FOLDER>:/test/experiment --name uig.rq12 uigrammar bash -c "./runExperimentRQ1_2.sh"
```

For RQ 3:

```
docker run --privileged -d -v <APP FOLDER>:/test/experiment --name uig.rq3 uigrammar bash -c "./runExperimentRQ3.sh"
```

For RQ 4:

```
docker run --privileged -d -v <APP FOLDER>:/test/experiment --name uig.rq4 uigrammar bash -c "./runExperimentRQ4.sh"
```

Where `<APP FOLDER>` is a folder which contains a subfolder `apks` with a single instrumented APK file and its json mapping from [DroidMate-2](https://github.com/uds-se/droidmate/wiki/Getting-Statement-Coverage) 

To assist with replication you can use the app and json file from the `./example` folder in this repository.  

## Running without docker

Please have a look on the DockerFile in the `./docker/base`, `./docker/dm` and `/docker` which contain all required commands to install and run the tool. 