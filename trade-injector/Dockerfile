FROM maven:3.2.5-jdk-8u40
FROM frolvlad/alpine-oraclejdk8:slim

WORKDIR /usr/src/app

# Prepare by downloading dependencies
ADD pom.xml /usr/src/app
RUN ["mvn", "dependency:resolve"]
RUN ["mvn", "verify"]

# Adding source, compile and package into a fat jar
ADD src /usr/src/app/src
RUN ["mvn", "package"]


EXPOSE 8090
CMD ["/usr/lib/jvm/java-8-openjdk-amd64/bin/java", "-jar", "target/tradeInjectorUI-jar-with-dependencies.jar"]


