package com.carrot.Carrot.controller;

import com.carrot.Carrot.model.PrimaNota;
import com.carrot.Carrot.service.PrimaNotaService;
import com.carrot.Carrot.enumerator.TipoMovimento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/prima-nota")
public class PrimaNotaController {

    @Autowired
    private PrimaNotaService primaNotaService;

    // ✅ Ottenere tutte le operazioni di un utente
    @GetMapping("/user/{userId}")
    public List<PrimaNota> getAllByUser(@PathVariable Long userId) {
        return primaNotaService.getAllByUser(userId);
    }

    // ✅ Ottenere una singola operazione
    @GetMapping("/{id}")
    public ResponseEntity<PrimaNota> getById(@PathVariable Long id) {
        Optional<PrimaNota> primaNota = primaNotaService.getById(id);
        return primaNota.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ✅ Ottenere solo le entrate o le uscite di un utente
    @GetMapping("/user/{userId}/tipo/{tipoMovimento}")
    public List<PrimaNota> getByTipo(@PathVariable Long userId, @PathVariable TipoMovimento tipoMovimento) {
        return primaNotaService.getByTipo(userId, tipoMovimento);
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

        @GetMapping("/dashboard/totali")
    public ResponseEntity<Map<String, BigDecimal>> getTotaleEntrateUscite(
            @RequestParam Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(primaNotaService.getTotaleEntrateUscite(userId, startDate, endDate));
    }

    // 📈 Saldo storico per gli ultimi X mesi
    @GetMapping("/dashboard/saldo")
    public ResponseEntity<List<Map<String, Object>>> getSaldoMensile(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "6") int mesi) {
        return ResponseEntity.ok(primaNotaService.getSaldoMensile(userId, mesi));
    }

    // 📁 Distribuzione delle categorie per tipo (ENTRATA o USCITA)
    @GetMapping("/dashboard/categorie")
    public ResponseEntity<Map<String, BigDecimal>> getDistribuzioneCategorie(
            @RequestParam Long userId,
            @RequestParam TipoMovimento tipoMovimento) {
        return ResponseEntity.ok(primaNotaService.getDistribuzioneCategorie(userId, tipoMovimento));
    }

}
