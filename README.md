# DEMO-JAVA-DOCKER

## Introduction
Docker is the de facto standard for creating self-contained applications. From version 2.3.0, Spring Boot includes several enhancements to help us create efficient Docker Images. Thus, it allows the decomposition of the application into different layers.

In other words, the source code resides in its own layer. Therefore, it can be independently rebuilt, improving efficiency and start-up time. In this tutorial, we'll see how to exploit the new capabilities of Spring Boot to reuse Docker layers.


## Layered jar in docker
Docker containers consist of a base image and additional layers. Once the layers are built, they'll remain cached. Therefore, subsequent generations will be much faster:

![alt text](assets/java-layers.png?raw=true)

Changes in the lower-level layers also rebuild the upper-level ones. Thus, the infrequently changing layers should remain at the bottom, and the frequently changing ones should be placed on top.

In the same way, Spring Boot allows mapping the content of the artifact into layers. Let's see the default mapping of layers:

![alt text](assets/java-layers2.png?raw=true)

As we can see, the application has its own layer. When modifying the source code, only the independent layer is rebuilt. The loader and the dependencies remain cached, reducing Docker image creation and startup time. Let's see how to do it with Spring Boot!

## Creating Efficient Docker Images with Spring Boot

In the traditional way of building Docker images, Spring Boot uses the fat jar approach. As a result, a single artifact embeds all the dependencies and the application source code. So, any change in our source code forces the rebuilding of the entire layer.

### Layers Configuration with Spring Boot

Spring Boot version 2.3.0 introduces two new features to improve the Docker image generation:

Buildpack support provides the Java runtime for the application, so it's now possible to skip the Dockerfile and build the Docker image automatically
Layered jars help us to get the most out of the Docker layer generation
In this tutorial, we'll extend the layered jar approach.

Initially, we'll set up the layered jar in Maven. When packaging the artifact, we'll generate the layers. Let's inspect the jar file:

`jar tf target/spring-boot-docker-0.0.1-SNAPSHOT.jar`

As we can see, new layers.idx file in the BOOT-INF folder inside the fat jar is created. Certainly, it maps dependencies, resources, and application source code to independent layers:

`BOOT-INF/layers.idx`

Likewise, the content of the file breaks down the different layers stored:

```text
- "dependencies":
  - "BOOT-INF/lib/"
- "spring-boot-loader":
  - "org/"
- "snapshot-dependencies":
- "application":
  - "BOOT-INF/classes/"
  - "BOOT-INF/classpath.idx"
  - "BOOT-INF/layers.idx"
  - "META-INF/"
```

### Interacting with Layers

Let's list the layers inside the artifact:

`java -Djarmode=layertools -jar target/docker-spring-boot-0.0.1.jar list

The result provides a simplistic view of the content of the layers.idx file:

```text
dependencies
spring-boot-loader
snapshot-dependencies
application
```

We can also extract the layers into folders:

`java -Djarmode=layertools -jar target/docker-spring-boot-0.0.1.jar extract`

### Dockerfile Configuration

To get the most out of the Docker capabilities, we need to add the layers to our image.

First, let's add the fat jar file to the base image:

```text
FROM adoptopenjdk:11-jre-hotspot as builder
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar
```

Second, let's extract the layers of the artifact:

`RUN java -Djarmode=layertools -jar application.jar extract`

Finally, let's copy the extracted folders to add the corresponding Docker layers:

```text
FROM adoptopenjdk:11-jre-hotspot
COPY --from=builder dependencies/ ./
COPY --from=builder snapshot-dependencies/ ./
COPY --from=builder spring-boot-loader/ ./
COPY --from=builder application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]
```

## Custom Layers

It seems everything is working like a charm. But if we look carefully, the dependency layer is not shared between our builds. That is to say, all of them come to a single layer, even the internal ones. Therefore, if we change the class of an internal library, we'll rebuild again all the dependency layers.

``