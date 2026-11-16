# ---------- Etapa 1: compilacion ----------
# La imagen coincide con el toolchain de build.gradle (Java 21)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon -x test

# ---------- Etapa 2: ejecucion ----------
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
# JVM ajustada a los 512 MB del plan gratuito y zona horaria de Colombia (reglas de hora del alojamiento)
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -Xss512k -Duser.timezone=America/Bogota"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
