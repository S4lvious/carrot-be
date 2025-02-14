# Stage 1: Build dell'applicazione
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copia il file pom.xml e scarica le dipendenze (in cache)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia il codice sorgente e compila l'applicazione (includendo il filtering delle risorse)
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Immagine finale per l'esecuzione
FROM openjdk:17-jdk-alpine
WORKDIR /app

# Copia il JAR compilato dal builder (assicurati che il nome corrisponda a quello generato)
COPY --from=builder /app/target/Carrot-0.0.1-SNAPSHOT.jar app.jar

# Variabili d'ambiente per la connessione al database (modifica se necessario)
ENV DB_URL=jdbc:mysql://db:3306/carrotpmi
ENV DB_USER=root
ENV DB_PASSWORD=password

# Esponi la porta dell'applicazione
EXPOSE 8080

# Avvia l'applicazione
ENTRYPOINT ["java", "-jar", "app.jar"]
