package com.carrot.Carrot.controller;

import com.carrot.Carrot.model.Prodotto;
import com.carrot.Carrot.service.ProdottoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/prodotti")
public class ProdottoController {
    private final ProdottoService prodottoService;

    public ProdottoController(ProdottoService prodottoService) {
        this.prodottoService = prodottoService;
    }

    @GetMapping
    public ResponseEntity<List<Prodotto>> getAllProdotti() {
        return ResponseEntity.ok(prodottoService.getAllProdotti());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Prodotto> getProdottoById(@PathVariable Long id) {
        return prodottoService.getProdottoById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Void> addProdotto(@RequestBody Prodotto prodotto) {
        prodottoService.addProdotto(prodotto);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> updateProdotto(@RequestBody Prodotto prodotto) {
        prodottoService.updateProdotto(prodotto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProdotto(@PathVariable Long id) {
        prodottoService.deleteProdotto(id);
        return ResponseEntity.ok().build();
    }
}