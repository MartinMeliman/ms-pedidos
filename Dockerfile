# ════════════════════════════════════════════════════════════════
#  Dockerfile para ms-pedidos
#  Multi-stage build: compila con Maven y ejecuta con JRE liviano
# ════════════════════════════════════════════════════════════════

# ── ETAPA 1: BUILD (compila el proyecto y genera el .jar) ──────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos primero el pom.xml para cachear dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiamos el código fuente y empaquetamos (sin ejecutar tests)
COPY src ./src
RUN mvn clean package -DskipTests

# ── ETAPA 2: RUNTIME (imagen liviana solo con el JRE) ──────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Render inyecta el puerto via variable $PORT; usamos 8085 por defecto
EXPOSE 8085

# Copiamos el JAR construido en la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Arrancamos el microservicio
ENTRYPOINT ["java", "-jar", "app.jar"]
