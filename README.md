# Adoptpet - Getting Started

---
The goal of this project is to centralize information on missing pets 
and provide a tool for monitoring of adoption process. 

It was crated during the ***Development and Deployment of Cloud 
Native Applications in Hybrid Environments*** course.

## Project 

---
It is a series of REST endpoints implementing a CRUD system for both Pets and Cities.

![Available endpoints for Cities API](./resources/api-cities.png "OpenAPI Image")

![Available endpoints for Pets API](./resources/api-pets.png "OpenAPI Image")

It is built using Java Spring Boot and MongoDB.

## Deploy

---
### Prerequisites

Depending on how this project is running, different tools need to be available:

- Local
  - A running instance of MongoDB, a user with read/write privileges 
  (a database and two collections will be crated).
  - Version 17 of both JDK and JRE.
  - Apache Maven installation (3.9 was used while images building).
- Within a container
  - A container runtime, Docker was mainly used during development and tests.
- Within a kubernetes cluster
  - Minikube or an OpenShift environment.
  - Knative CLI if a serverless version of application is to be run.

Set up of these tools are beyond this document's scope.

### Packaging code
An environment variable (`APP_DB_URL`) is needed so app can point to mongo instance used.

You need to clone this repository (or download it) into your computer and locate at root folder and run:

```
export APP_DB_URL=mongodb+srv://<user>:<password>@<server>/adoptpetdb
mvn clean package
```
Substitute `user`, `password` and `server` accordingly, protocol might 
need to change to `mongodb` if a server and port are used.

Tests can also be skipped at this point and therefore variable 
is not needed, it will be to run application though.

### Building Images
Code contains two Docker files for images generation.
``` plain text
.
├── ...
├── db
│   ├── ...
│   └── Dockerfile
└── Dockerfile
```
At project's root you need to run: 
```
docker build -t <registry_user>/<app_image_name>:<tag> . 
```
go to `db` directory and run:
```                                                 
docker build -t <registry_user>/<db_image_name>:<tag> .
```
Next steps assume:
```
registry_user=sacnavi
app_image_name=adoptpetsvc
app_image_name=adoptpetdb
```
tag will be different since this is a WIP at this moment, you can check
[docker hub registry](https://hub.docker.com/u/sacnavi) for latest versions.

You can also use images from docker hub and no need to build them.

### Run application and database
This depends, again, on how it is running:

- Local
  - Assuming you have already a local or cloud database you just need to build
    connection string as it is needed for application to run properly (if not yet set).
  - Set APP_DB_URL variable (if not yet).
  - Run `mvn spring-boot:run` and wait it to finish successfully.
- As containers
  - Create adoptnet network, run container for database, image already contains scripts for user creation 
  and pre loaded data, then run container for service.
    ```shell
    docker network create adoptnet
    docker run -d --name adoptpetdb -p 27017:27017 --network adoptnet -e MONGO_INITDB_ROOT_USERNAME_FILE=/run/secrets/user.txt -e MONGO_INITDB_ROOT_PASSWORD_FILE=/run/secrets/pass.txt sacnavi/adoptpetdb:v1
    docker run -d --name adoptpetsvc -p 8079:8079 --network adoptnet -e APP_DB=adoptpetdb -e APP_USR=adoptpetusr -e APP_PASS=adoptpetpass -e APP_DB_URL=mongodb://adoptpetusr:adoptpetpass@adoptpetdb.adoptnet:27017/adoptpetdb?authSource=admin sacnavi/adoptpetsvc:v1  
    ```
- Within a kubernetes cluster
  - Running cluster for Kubernetes (Minikube, OpenShift) and logged in.
  - Images pushed to some registry.
  - Move to manifests directory and apply configurations for all objects:
    - to start database run: 
      ```shell
      oc apply -f adoptpet-secrets.yaml
      oc apply -f adoptpet-db-pvc.yaml
      oc apply -f adoptpet-db-deployment.yaml
      oc apply -f adoptpet-db-service.yaml
      ```
    - there are two options to start application:
      - as a service
        ```shell
        oc apply -f adoptpet-svc-deployment.yaml
        oc apply -f adoptpet-svc-service.yaml
        oc apply -f adoptpet-svc-imagestream.yaml
        ```
      - as a serverless application
        ```shell
        oc apply -f adoptpet-svc-serverless.yaml
        ```
## Testing

---
Unit tests are run during either packaging (might be skipped) or running of the project.

To test application and database in an actual running environment,
some requests must be issued against service:

This command will show list of pre loaded data:
```shell
curl http://localhost:8079/api/ciudades
```

Keep in mind that host is different if it is running on a cluster.

Output should look like this (there are 16 entries, only 2 shown):

```json
[
  {
    "id": "7a2c89f0-ee94-4d8f-bf3b-315e76014d11",
    "code": "09010",
    "name": "Álvaro Obregón",
    "zips": [
      "01000"
    ]
  },
  {
    "id": "371b81d0-f3d3-496b-aca4-f95c8bd4a63f",
    "code": "09013",
    "name": "Xochimilco",
    "zips": [
      "16000"
    ]
  }
]
```

## Contributing

---
Contributions are welcome in order to improve this code or add new functionalities.
### Branch strategy
If you want to contribute to this project, please create a branch from main and 
push your changes to it, once finished, submit a pull request and once approved it
will be merged into main branch.

Please  use this structure while naming branches:
- For new features prefix branch name with `feat/` and then add a short description.
- To solve issues use `fix/` and again, a short description of the issue you are resolving.
- If this project lands at some management tool, considering including issue number in description used. 

Tags should be also used to mark different versions of application.

A webhook and an event listener were set up, this project should be built now.

## Want to learn more?
### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/3.3.4/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.3.4/maven-plugin/build-image.html)
* [Spring Data MongoDB](https://docs.spring.io/spring-boot/docs/3.3.4/reference/htmlsingle/index.html#data.nosql.mongodb)
* [Spring Web](https://docs.spring.io/spring-boot/docs/3.3.4/reference/htmlsingle/index.html#web)
* [Docker](https://www.docker.com/)
* [Kubernetes](https://kubernetes.io/)
* [MiniKube](https://minikube.sigs.k8s.io/docs/)
* [OpenShift](https://www.redhat.com/en/technologies/cloud-computing/openshift)
* [Knative](https://knative.dev/docs/install/quickstart-install/)

### Guides
The following guides illustrate how to use some features concretely:

* [Accessing Data with MongoDB](https://spring.io/guides/gs/accessing-data-mongodb/)
* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.
