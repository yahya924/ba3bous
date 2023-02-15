FROM maven:3.6.3-openjdk-17-slim as builder
COPY src /usr/src/app/src
COPY settings.xml /root/.m2/settings.xml
COPY pom.xml /usr/src/app

RUN mvn -f /usr/src/app/pom.xml clean install -Dmaven.test.skip

FROM openjdk:17-alpine
COPY --from=builder /usr/src/app/target/extranet-igatn-0.0.2-SNAPSHOT.jar  /usr/app/extranet-igatn-0.0.2-SNAPSHOT.jar
COPY --from=builder /usr/src/app/src/main/resources/extranet-mobile-service-account.json /usr/app/config/
ENTRYPOINT ["java", "-jar", "/usr/app/extranet-igatn-0.0.2-SNAPSHOT.jar"]
