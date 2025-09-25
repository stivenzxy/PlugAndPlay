FROM openjdk:21-jdk-slim

RUN apt-get update && \
    apt-get install -y wget && \
    wget -q https://repo1.maven.org/maven2/com/h2database/h2/2.2.224/h2-2.2.224.jar -O /opt/h2.jar

EXPOSE 9092
EXPOSE 8082

CMD ["java", "-cp", "/opt/h2.jar", "org.h2.tools.Server", "-web", "-webAllowOthers", "-tcp", "-tcpAllowOthers", "-baseDir", "/opt/h2-data", "-ifNotExists"]