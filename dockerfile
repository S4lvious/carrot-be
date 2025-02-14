FROM openjdk:17
WORKDIR /app

# Copia il file JAR corretto dalla cartella target
COPY target/Carrot-0.0.1-SNAPSHOT.jar app.jar

# Definiamo le variabili d'ambiente per Docker
ENV DB_URL=jdbc:mysql://db:3306/carrotpmi
ENV DB_USER=root
ENV DB_PASSWORD=password

# Esponi la porta 8080
EXPOSE 8080

# Avvia l'applicazione
ENTRYPOINT ["java", "-jar", "app.jar"]
