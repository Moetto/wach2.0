# Wach 2.0
A web-application that helps you celebrate the First of May properly (as is done by engineering students in Oulu, Finland). 
Wach (short for Wappu achievements) contains tasks that users can complete during the celebrations.

This is mainly done as a hobby project to learn new things. Perhaps one day it'll be 
production ready. Until then, I'll work on whatever catches my fancy.

## Building

### Requirements
* Java 15 jdk. Set `JAVA_HOME` environment variable to point at a Java 15 jdk.
* Docker. User must be able to run docker without sudo, usually by being in group `docker`
* jq
* [swagger-cli](https://github.com/APIDevTools/swagger-cli)

### Build
Run:\
`./gradlew build`

The command builds a docker image tagged with `wach:dev` and documentation found at [api-doc/build/swagger-ui/index.html](api-doc/build/swagger-ui/index.html)

## Running
Run:\
`./gradlew run`
The application will be accessible on localhost:8080

For running on either local or remote kubernetes cluster, see [deployment instructions](deployment/DEPLOYMENT.md) ([GitHub Link](https://github.com/Moetto/wach2.0/blob/master/deployment/DEPLOYMENT.md))

## Publishing
### Requirements
* Logged in to docker hub. Run `docker login` or any of its [variants](https://docs.docker.com/engine/reference/commandline/login/) if using saved credentials.
* A repository you can access. 

### Publishing
Run:\
`./gradlew publish`

Publishing to a non-default repository (because I didn't give you access to mine)\
`./gradlew publish -P repository=DOCKERHUB_USERNAME/REPOSITORY`

## CI
See actions in [GitHub](https://github.com/Moetto/wach2.0/actions)