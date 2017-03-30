#FROM maven:3.2.5-jdk-8u40-onbuild
#FROM frolvlad/alpine-oraclejdk8:slim
FROM maven:3-jdk-8

RUN mkdir -p /usr/src/app

WORKDIR /usr/src/app

ONBUILD ADD . /usr/src/app

ONBUILD RUN mvn install

# Install maven
#RUN apt-get update
#RUN apt-get install -y maven


#WORKDIR /usr/src/app


# Prepare by downloading dependencies
#ADD pom.xml /usr/src/app
#RUN mvn verify clean
#RUN ["mvn", "verify"]
#RUN ["mvn", "dependency:resolve"]
#RUN ["mvn", "verify"]

# Adding source, compile and package into a fat jar
#ADD src /usr/src/app/src
#RUN  mvn package
#ADD src /usr/src/app/src
#RUN ["mvn", "package"]


EXPOSE 8090
CMD ["java", "-jar", "target/tradeInjectorUI-jar-with-dependencies.jar"]


