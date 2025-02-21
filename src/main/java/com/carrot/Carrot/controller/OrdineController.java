package com.carrot.Carrot.controller;

import com.carrot.Carrot.model.Ordine;
import com.carrot.Carrot.service.OrdineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/ordini")
public class OrdineController {

    private final OrdineService ordineService;

    public OrdineController(OrdineService ordineService) {
        this.ordineService = ordineService;
    }

    // Recupera tutti gli ordini
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
    public ResponseEntity<Ordine> addOrdine(@RequestBody Ordine ordine,  @RequestParam(value = "documenti", required = false) List<MultipartFile> documenti) {
       return ordineService.addOrdine(ordine, documenti).map(newOrdine -> ResponseEntity.ok(newOrdine)).orElse(ResponseEntity.badRequest().build());
    }

    @GetMapping("/unfattured")
    public ResponseEntity<List<Ordine>> findNonFatturati() {
        return ResponseEntity.ok(ordineService.getAllOrdiniNotInvoicedList());
    }


    // Aggiorna un ordine esistente
    @PutMapping("/{id}")
    public ResponseEntity<Ordine> updateOrdine(@RequestBody Ordine ordine) {
        return ordineService.updateOrdine(ordine).map(nullOrdine -> ResponseEntity.ok(nullOrdine)).orElse(ResponseEntity.notFound().build());
    }

    // Elimina un ordine
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrdine(@PathVariable Long id) {
        ordineService.deleteOrdine(id);
        return ResponseEntity.noContent().build();
    }
}
