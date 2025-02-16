package com.carrot.Carrot.controller;

import com.carrot.Carrot.model.MetodoPagamento;
import com.carrot.Carrot.service.MetodoPagamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/metodo-pagamento")
public class MetodoPagamentoController {

    @Autowired
    private MetodoPagamentoService metodoPagamentoService;

    // Ottenere tutti i metodi di pagamento dell'utente autenticato
    @GetMapping
    public List<MetodoPagamento> getAllMetodiPagamento() {
        return metodoPagamentoService.getAllMetodiPagamento();
    }

    // Ottenere un metodo di pagamento specifico
    @GetMapping("/{id}")
    public ResponseEntity<MetodoPagamento> getMetodoPagamentoById(@PathVariable Long id) {
        Optional<MetodoPagamento> metodoPagamento = metodoPagamentoService.getMetodoPagamentoById(id);
        return metodoPagamento.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Creare un nuovo metodo di pagamento
    @PostMapping
    public ResponseEntity<MetodoPagamento> createMetodoPagamento(@RequestBody MetodoPagamento metodoPagamento) {
        return ResponseEntity.ok(metodoPagamentoService.createMetodoPagamento(metodoPagamento));
    }

    // Modificare un metodo di pagamento esistente
    @PutMapping("/{id}")
    public ResponseEntity<MetodoPagamento> updateMetodoPagamento(@PathVariable Long id, @RequestBody MetodoPagamento updatedMetodoPagamento) {
        return ResponseEntity.ok(metodoPagamentoService.updateMetodoPagamento(id, updatedMetodoPagamento));
    }

    // Eliminare un metodo di pagamento
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMetodoPagamento(@PathVariable Long id) {
        metodoPagamentoService.deleteMetodoPagamento(id);
        return ResponseEntity.noContent().build();
    }
}