FROM java:openjdk-8-jre
ENV HZ_VERSION 3.8.1
ENV HZ_HOME /opt/hazelcast/
RUN mkdir -p $HZ_HOME
WORKDIR $HZ_HOME
# Download hazelcast jars from maven repo.
ADD https://repo1.maven.org/maven2/com/hazelcast/hazelcast-all/$HZ_VERSION/hazelcast-all-$HZ_VERSION.jar $HZ_HOME
ADD domain-1.0-SNAPSHOT.jar $HZ_HOME/domain-1.0-SNAPSHOT.jar
ADD cache-1.0-SNAPSHOT.jar $HZ_HOME/cache-1.0-SNAPSHOT.jar
ADD server.sh /$HZ_HOME/server.sh
ADD stop.sh /$HZ_HOME/stop.sh
ADD hazelcast.xml /$HZ_HOME/hazelcast.xml
RUN chmod +x /$HZ_HOME/server.sh
RUN chmod +x /$HZ_HOME/stop.sh
#Set the classpath
#ENV CLASSPATH=.:/opt/hazelcast/cache-1.0-SNAPSHOT.jar:/opt/hazelcast/domain-1.0-SNAPSHOT.jar
# Start hazelcast standalone server.
CMD ["./server.sh"]
EXPOSE 5701
