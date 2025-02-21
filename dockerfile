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

# Variabili d'ambiente per la connessione al database
ENV DB_URL=jdbc:mysql://db:3306/carrotpmi
ENV DB_USER=root
ENV DB_PASSWORD=password

# ➕ Aggiunta delle variabili per Fattura Elettronica API
ENV WEBHOOK_TOKEN="Bearer qwertyuiopASDFGHJKL1234567890abcXYZ"
ENV FATTURA_API_URL=https://fattura-elettronica-api.it/ws2.0/test/fatture
ENV FATTURA_API_AUTH="Basic cy5saWNjYXJkbzAyMkBnbWFpbC5jb206eFUzN21MbHJ4Zw=="


ENV gocardless_bad_secret-id=5106d36f-2fee-4c83-bbba-612043c07f24
ENV gocardless_bad_secret-key=19f3084a9c938508951663046bac296bd84d42c15edb2033d5239f0295e412ee8221c1545d9c51eedd83a580723cdbe3232b5001d2b4d997a8a61ebfc638b44c
ENV gocardless_bad_api-url=https://bankaccountdata.gocardless.com/api/v2

ENV spring_cloud_gcp_credentials_location=classpath:promising-cairn-450815-a4-905bedbc06aa.json
ENV spring_cloud_gcp_storage_bucket=carrot-document-storage

# Esponi la porta dell'applicazione
EXPOSE 8080

# Avvia l'applicazione
ENTRYPOINT ["java", "-jar", "app.jar"]
