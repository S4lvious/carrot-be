package com.carrot.Carrot.controller;

import com.carrot.Carrot.model.Documento;
import com.carrot.Carrot.model.Ordine;
import com.carrot.Carrot.repository.DocumentoRepository;
import com.carrot.Carrot.service.OrdineService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ordini")
public class OrdineController {

    private final OrdineService ordineService;
    @Autowired
    DocumentoRepository documentoRepository;

    @Autowired
    private ObjectMapper objectMapper;  // ✅ Usa il bean di Spring


    public OrdineController(OrdineService ordineService) {
        this.ordineService = ordineService;
    }

    @GetMapping("/{id}/documenti")
    public ResponseEntity<List<Documento>> getDocumentiByOrdine(@PathVariable Long id) {
        List<Documento> documenti = documentoRepository.findByOrdineId(id);
        return ResponseEntity.ok(documenti);
    }


    @GetMapping
    public ResponseEntity<List<Ordine>> getAllOrdini() {
        return ResponseEntity.ok(ordineService.getAllOrdini());
    }

    // Recupera un ordine per ID
    @GetMapping("/{id}")
    public ResponseEntity<Ordine> getOrdineById(@PathVariable Long id) {
        return ordineService.getOrdineById(id).map(nullOrdine -> ResponseEntity.ok(nullOrdine)).orElse(ResponseEntity.notFound().build());
    }

    // Crea un nuovo ordine
    @PostMapping
    public ResponseEntity<Ordine> addOrdine(
            @RequestPart("ordineData") String ordineJson,  
            @RequestParam(value = "documenti", required = false) List<MultipartFile> documenti) {
    
        // ✅ Log per controllare cosa arriva
        System.out.println("Ricevuto JSON: " + ordineJson);
        if (documenti != null) {
            System.out.println("Numero documenti ricevuti: " + documenti.size());
        } else {
            System.out.println("Nessun documento ricevuto");
        }
        Ordine ordine;
        try {
            ordine = objectMapper.readValue(ordineJson, Ordine.class);
        } catch (JsonProcessingException e) {
            System.err.println("Errore nella deserializzazione dell'ordine: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    
        // ✅ Controllo se l'oggetto Ordine è stato deserializzato correttamente
        System.out.println("Ordine Deserializzato: " + ordine.toString());
    
        Optional<Ordine> savedOrdine = ordineService.addOrdine(ordine, documenti);
        return savedOrdine.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.badRequest().build());
    }
    
    
    @GetMapping("/unfattured")
    public ResponseEntity<List<Ordine>> findNonFatturati() {
        return ResponseEntity.ok(ordineService.getAllOrdiniNotInvoicedList());
    }


    // Aggiorna un ordine esistente
    @PutMapping("/{id}")
    public ResponseEntity<Ordine> updateOrdine(
            @PathVariable Long id,
            @ModelAttribute Ordine ordine,  // ✅ Riceviamo l'ordine come FormData
            @RequestParam(value = "documenti", required = false) List<MultipartFile> documenti) {  // ✅ Riceviamo i nuovi file
    
        return ordineService.updateOrdine(id, ordine, documenti)
                .map(updatedOrdine -> ResponseEntity.ok(updatedOrdine))
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Elimina un ordine
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrdine(@PathVariable Long id) {
        ordineService.deleteOrdine(id);
        return ResponseEntity.noContent().build();
    }
}
