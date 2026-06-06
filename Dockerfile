FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY security-common ./security-common
RUN mvn -f security-common/pom.xml -B -DskipTests install

COPY doctor-service ./doctor-service
RUN mvn -f doctor-service/pom.xml -B -DskipTests package && cp /workspace/doctor-service/target/*-SNAPSHOT.jar /workspace/doctor-service/app.jar

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/doctor-service/app.jar app.jar
EXPOSE 8092
ENTRYPOINT ["java", "-jar", "app.jar"]
