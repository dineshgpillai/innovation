FROM frolvlad/alpine-oraclejdk8:slim
export DOCKER_HOST=tcp://138.68.168.237:2376
VOLUME /tmp
ADD spring-boot-docker-example-0.0.1-SNAPSHOT.jar app.jar
RUN sh -c 'touch /app.jar'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app