package com.carrot.Carrot.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

@Service
public class StorageService {

    private final Storage storage;
    private final String bucketName;

    public StorageService(@Value("${spring.cloud.gcp.credentials.location}") String credentialsPath,
                          @Value("${spring.cloud.gcp.storage.bucket}") String bucketName) throws IOException {
        this.bucketName = bucketName;

        // ✅ Carica manualmente le credenziali da /etc/secrets/google-credentials.json
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialsPath))
                .createScoped("https://www.googleapis.com/auth/cloud-platform");
        this.storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    }

    // 📌 Metodo per caricare un file su GCS
    public String uploadFile(MultipartFile file, String ordineId) throws IOException {
        String fileName = "ordini/" + ordineId + "/" + file.getOriginalFilename();

        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();

        storage.create(blobInfo, file.getBytes());

        return fileName; // Salviamo solo il percorso nel database
    }

    // 📌 Metodo per ottenere un URL firmato per il download
    public URL generateSignedUrl(String fileName) {
        BlobId blobId = BlobId.of(bucketName, fileName);
        Blob blob = storage.get(blobId);

        return blob.signUrl(1, TimeUnit.HOURS); // URL valido per 1 ora
    }
}
