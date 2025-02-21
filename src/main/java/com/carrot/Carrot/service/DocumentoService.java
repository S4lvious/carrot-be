package com.carrot.Carrot.service;

import com.carrot.Carrot.model.Documento;
import com.carrot.Carrot.model.User;
import com.carrot.Carrot.repository.DocumentoRepository;
import com.carrot.Carrot.security.MyUserDetails;
import com.google.cloud.storage.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class DocumentoService {
    
    private final DocumentoRepository documentoRepository;
    private final Storage storage;
    private final String bucketName;
    @Autowired
    StorageService storageService;

        private User getCurrentUser() {
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder
                                        .getContext()
                                        .getAuthentication()
                                        .getPrincipal();
        return userDetails.getUser();
    }



    public DocumentoService(DocumentoRepository documentoRepository, 
                            @Value("${spring.cloud.gcp.storage.bucket}") String bucketName) {
        this.documentoRepository = documentoRepository;
        this.storage = StorageOptions.getDefaultInstance().getService();
        this.bucketName = bucketName;
    }

    public List<Documento> getDocumentiByOrdine(Long ordineId) {
        return documentoRepository.findByOrdineIdAndUserId(ordineId, getCurrentUser().getId());
    }

    public List<Documento> getDocumentiByCliente(Long clienteId) {
        return documentoRepository.findByClienteIdAndUserId(clienteId, getCurrentUser().getId());
    }

    @Transactional
    public void deleteDocumento(Long documentoId) {
        Optional<Documento> documentoOpt = documentoRepository.findById(documentoId);
        if (documentoOpt.isPresent()) {
            Documento documento = documentoOpt.get();
            this.storageService.deleteFile(documento);            
            documentoRepository.deleteById(documentoId);
        } else {
            throw new RuntimeException("Documento non trovato con ID: " + documentoId);
        }
    }


    public String getSignedUrl(String filePath) {
        return this.storageService.generateSignedUrl(filePath).toString();
    }
}
