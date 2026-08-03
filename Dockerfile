# ETAPA 1 — BUILD
# Usamos Maven con Java 21 para compilar el código
# "builder" es un alias para referenciar esta etapa desde la siguiente
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Directorio de trabajo dentro del contenedor donde vivirá el código
WORKDIR /app

# Primero copiamos solo el pom.xml
# Docker cachea capas — si el código cambia pero el pom.xml no,
# no vuelve a descargar las dependencias de Maven (ahorra minutos)
COPY pom.xml .

# Descarga todas las dependencias sin compilar el código
# Esta capa se cachea mientras el pom.xml no cambie
RUN mvn dependency:go-offline -B

# Ahora sí copiamos el código fuente completo
COPY src ./src

# Compila y empaqueta — genera el JAR en target/
# -DskipTests para no correr tests en el build de Docker
RUN mvn package -DskipTests

# ETAPA 2 — RUNTIME
# Imagen mucho más pequeña — solo necesita Java para correr el JAR
# No necesita Maven ni el código fuente
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiamos solo el JAR generado en la etapa anterior
# El resto del build queda descartado — imagen final más liviana
COPY --from=builder /app/target/*.jar app.jar

# Puerto que expone el contenedor — debe coincidir con SERVER_PORT
EXPOSE 8082

# Comando que arranca Spring Boot cuando el contenedor inicia
ENTRYPOINT ["java", "-jar", "app.jar"]