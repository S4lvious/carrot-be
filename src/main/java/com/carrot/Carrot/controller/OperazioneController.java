package com.carrot.Carrot.controller;

import com.carrot.Carrot.model.Operazione;
import com.carrot.Carrot.service.OperazioneService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/operazioni")
public class OperazioneController {
    private final OperazioneService operazioneService;

    public OperazioneController(OperazioneService operazioneService) {
        this.operazioneService = operazioneService;
    }

    @GetMapping
    public ResponseEntity<List<Operazione>> getAllOperazioni() {
        return ResponseEntity.ok(operazioneService.getAllOperazioni());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Operazione> getOperazioneById(@PathVariable Long id) {
        return operazioneService.getOperazioneById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Void> addOperazione(@RequestBody Operazione operazione) {
        operazioneService.addOperazione(operazione);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<Void> updateOperazione(@RequestBody Operazione operazione) {
        operazioneService.updateOperazione(operazione);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOperazione(@PathVariable Long id) {
        operazioneService.deleteOperazione(id);
        return ResponseEntity.ok().build();
    }
}
