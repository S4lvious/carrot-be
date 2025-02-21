package com.carrot.Carrot.service;
import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

@Service
public class StorageService {

    private final Storage storage;
    private final String bucketName;

    public StorageService(@Value("${spring.cloud.gcp.storage.bucket}") String bucketName) {
        this.storage = StorageOptions.getDefaultInstance().getService();
        this.bucketName = bucketName;
    }

    // 📌 Metodo per caricare un file
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
