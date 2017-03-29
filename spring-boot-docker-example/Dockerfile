FROM maven:3.3.9-jdk-8-onbuild
FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
COPY /root/.m2/repository/org/springframework/dp-spring-boot-docker/0.1.0/dp-spring-boot-docker-0.1.0.jar /tmp
ADD dp-spring-boot-docker-0.1.0.jar dp-app.jar
RUN sh -c 'touch /dp-app.jar'
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /dp-app.jar" ]
