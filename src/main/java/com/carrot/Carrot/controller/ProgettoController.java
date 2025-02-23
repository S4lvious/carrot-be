package com.carrot.Carrot.controller;

import com.carrot.Carrot.model.Progetto;
import com.carrot.Carrot.model.Ordine;
import com.carrot.Carrot.service.ProgettoService;
import com.carrot.Carrot.service.OrdineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/progetti")
public class ProgettoController {

    private final ProgettoService progettoService;
    private final OrdineService ordineService;

    public ProgettoController(ProgettoService progettoService, OrdineService ordineService) {
        this.progettoService = progettoService;
        this.ordineService = ordineService;
    }

    @PostMapping
    public ResponseEntity<Progetto> createProgetto(@RequestBody Progetto progetto) {
        return ResponseEntity.ok(progettoService.addProgetto(progetto));
    }

    // 📌 Associa un progetto a un ordine
    @PostMapping("/{progettoId}/associa-ordine/{ordineId}")
    public ResponseEntity<Progetto> associaProgettoAdOrdine(
            @PathVariable Long progettoId,
            @PathVariable Long ordineId) {

        Optional<Progetto> progettoOpt = progettoService.getProgettoById(progettoId);
        Optional<Ordine> ordineOpt = ordineService.getOrdineById(ordineId);

        if (progettoOpt.isPresent() && ordineOpt.isPresent()) {
            Progetto progettoAggiornato = progettoService.associateToOrder(ordineOpt.get(), progettoOpt.get());
            return ResponseEntity.ok(progettoAggiornato);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{progettoId}")
    public ResponseEntity<List<Progetto>> deleteProject(@PathVariable Long progettoId){
        progettoService.deleteProgetto(progettoId);
        return ResponseEntity.ok(progettoService.getProgettiByUser());
    }

    // 📌 Recupera tutti i progetti di un utente
    @GetMapping
    public ResponseEntity<List<Progetto>> getProgettiByUser() {
        List<Progetto> progetti = progettoService.getProgettiByUser();
        return progetti.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(progetti);
    }
}
