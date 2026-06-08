# ── Stage 1: build ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copia o wrapper Maven e resolve dependências primeiro (cache de camadas)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

# Copia o código e compila
COPY src ./src
RUN ./mvnw clean package -DskipTests -q

# ── Stage 2: runtime ────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
