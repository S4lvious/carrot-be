package com.carrot.Carrot.controller;

import com.carrot.Carrot.model.Documento;
import com.carrot.Carrot.service.DocumentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/documenti")
public class DocumentoController {

    private final DocumentoService documentoService;

    public DocumentoController(DocumentoService documentoService) {
        this.documentoService = documentoService;
    }

    // 📌 Recupera i documenti di un ordine
    @GetMapping("/ordine/{ordineId}")
    public ResponseEntity<List<Documento>> getDocumentiByOrdine(@PathVariable Long ordineId) {
        return ResponseEntity.ok(documentoService.getDocumentiByOrdine(ordineId));
    }

    // 📌 Recupera i documenti di un cliente
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Documento>> getDocumentiByCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(documentoService.getDocumentiByCliente(clienteId));
    }

    // 📌 Genera un link firmato per visualizzare/scaricare un documento
    @GetMapping("/visualizza/{filePath}")
    public ResponseEntity<Map<String, String>> getSignedUrl(@PathVariable String filePath) {
        String url = documentoService.getSignedUrl(filePath);
        return ResponseEntity.ok(Map.of("signedUrl", url));
    }
}
