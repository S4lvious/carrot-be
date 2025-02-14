package com.carrot.Carrot.controller;

import com.carrot.Carrot.model.*;
import com.carrot.Carrot.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clienti")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> getAllClients() {
        return ResponseEntity.ok(clienteService.getAllClients());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> getClientById(@PathVariable Long id) {
        return clienteService.getClientById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Void> addClient(@RequestBody Cliente cliente) {
        clienteService.addClient(cliente);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> updateClient(@RequestBody Cliente cliente) {
        clienteService.updateClient(cliente);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clienteService.deleteClient(id);
        return ResponseEntity.ok().build();
    }
}

