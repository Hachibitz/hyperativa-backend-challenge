# Estágio 1: Build com JDK completo
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace/app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
RUN ./gradlew build

# Copia o código fonte e constrói o JAR
COPY src src
RUN ./gradlew bootJar

# Estágio 2: Imagem final com JRE enxuto
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copia o JAR do estágio de build para a imagem final
COPY --from=builder /workspace/app/build/libs/*.jar app.jar

# Expõe a porta que a aplicação vai usar
EXPOSE 8080

# Comando para rodar a aplicação
ENTRYPOINT ["java","-jar","/app/app.jar"]