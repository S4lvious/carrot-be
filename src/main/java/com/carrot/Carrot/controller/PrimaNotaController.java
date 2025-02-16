package com.carrot.Carrot.controller;

import com.carrot.Carrot.model.PrimaNota;
import com.carrot.Carrot.service.PrimaNotaService;
import com.carrot.Carrot.enumerator.TipoMovimento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/prima-nota")
public class PrimaNotaController {

    @Autowired
    private PrimaNotaService primaNotaService;

    // ✅ Ottenere tutte le operazioni dell'utente autenticato
    @GetMapping
    public List<PrimaNota> getAllByUser() {
        return primaNotaService.getAllByUser();
    }

    // ✅ Ottenere una singola operazione
    @GetMapping("/{id}")
    public ResponseEntity<PrimaNota> getById(@PathVariable Long id) {
        Optional<PrimaNota> primaNota = primaNotaService.getById(id);
        return primaNota.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ✅ Ottenere solo le entrate o le uscite dell'utente autenticato
    @GetMapping("/tipo/{tipoMovimento}")
    public List<PrimaNota> getByTipo(@PathVariable TipoMovimento tipoMovimento) {
        return primaNotaService.getByTipo(tipoMovimento);
    }

    // ✅ Creare una nuova operazione
    @PostMapping
    public ResponseEntity<PrimaNota> createPrimaNota(@RequestBody PrimaNota primaNota) {
        return ResponseEntity.ok(primaNotaService.createPrimaNota(primaNota));
    }

    // ✅ Modificare un'operazione esistente
    @PutMapping("/{id}")
    public ResponseEntity<PrimaNota> updatePrimaNota(@PathVariable Long id, @RequestBody PrimaNota updatedPrimaNota) {
        return ResponseEntity.ok(primaNotaService.updatePrimaNota(id, updatedPrimaNota));
    }

    // ✅ Eliminare un'operazione
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePrimaNota(@PathVariable Long id) {
        primaNotaService.deletePrimaNota(id);
        return ResponseEntity.noContent().build();
    }
}