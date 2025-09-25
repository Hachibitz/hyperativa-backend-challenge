# =================================================================
# ESTÁGIO 1: Construir e publicar a biblioteca 'card-common' localmente
# =================================================================
FROM gradle:8.5.0-jdk21 AS common-builder

WORKDIR /workspace/app

# Copia apenas o projeto da biblioteca
COPY card-common/ .

# Publica o JAR no repositório .m2 DENTRO deste container de build
RUN ./gradlew publishToMavenLocal --no-daemon

# =================================================================
# ESTÁGIO 2: Construir o serviço 'card-api'
# =================================================================
FROM gradle:8.5.0-jdk21 AS api-builder

WORKDIR /workspace/app

# Copia o repositório .m2 do estágio anterior, que contém o card-common.jar
COPY --from=common-builder /root/.m2 /root/.m2

# Copia o projeto da API
COPY card-api/ .

# Constrói a API. O Gradle irá encontrar o card-common no .m2 copiado.
RUN ./gradlew bootJar --no-daemon

# =================================================================
# ESTÁGIO 3: Construir o serviço 'card-consumer'
# =================================================================
FROM gradle:8.5.0-jdk21 AS consumer-builder

WORKDIR /workspace/app

# Copia o repositório .m2 do estágio 'common-builder'
COPY --from=common-builder /root/.m2 /root/.m2

# Copia o projeto do Consumer
COPY card-consumer/ .

# Constrói o Consumer
RUN ./gradlew bootJar --no-daemon

# =================================================================
# ESTÁGIO 4: Imagem final para o card-api
# =================================================================
FROM eclipse-temurin:21-jre AS api

WORKDIR /app

COPY --from=api-builder /workspace/app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

# =================================================================
# ESTÁGIO 5: Imagem final para o card-consumer
# =================================================================
FROM eclipse-temurin:21-jre AS consumer

WORKDIR /app

COPY --from=consumer-builder /workspace/app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]