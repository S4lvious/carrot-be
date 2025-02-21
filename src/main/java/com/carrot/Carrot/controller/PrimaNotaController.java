package com.carrot.Carrot.controller;

import com.carrot.Carrot.model.Ordine;
import com.carrot.Carrot.model.PrimaNota;
import com.carrot.Carrot.service.PrimaNotaService;
import com.carrot.Carrot.enumerator.TipoMovimento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/prima-nota")
public class PrimaNotaController {

    @Autowired
    private PrimaNotaService primaNotaService;

    // ✅ Ottenere tutte le operazioni dell'utente autenticato
    @GetMapping
    public List<Map<String, Object>> getAllByUser() {
        List<PrimaNota> primaNote = primaNotaService.getAllByUser();

        List<Map<String, Object>> result = new ArrayList<>();

        for (PrimaNota nota : primaNote) {
            Map<String, Object> notaData = new HashMap<>();
            notaData.put("primaNota", nota);
            
            // Recuperiamo l'oggetto `Incarico` solo se incaricoId non è null
            Ordine incarico = primaNotaService.getIncarico(nota.getIncaricoId());
            notaData.put("incarico", incarico);

            result.add(notaData);
        }

        return result;
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

    // ✅ Ottenere il totale di entrate e uscite per i grafici
        @GetMapping("/dashboard/totali")
        public ResponseEntity<Map<String, BigDecimal>> getTotaleEntrateUscite(
                @RequestParam(value = "dataInizio", required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInizio,
                @RequestParam(value = "dataFine", required = false) 
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFine,
                @RequestParam(value = "rollingDays", required = false) Integer rollingDays) {

            Map<String, BigDecimal> result;

            if (rollingDays != null) {
                result = primaNotaService.getTotaleEntrateUsciteRolling(rollingDays);
            } else if (dataInizio != null && dataFine != null) {
                result = primaNotaService.getTotaleEntrateUscite(dataInizio, dataFine);
            } else {
                LocalDate dataFineDefault = LocalDate.now();
                LocalDate dataInizioDefault = dataFineDefault.minusMonths(6);
                result = primaNotaService.getTotaleEntrateUscite(dataInizioDefault, dataFineDefault);
            }
            
            return ResponseEntity.ok(result);
        }
    // ✅ Ottenere il saldo mensile per i grafici
    @GetMapping("/dashboard/saldo")
    public ResponseEntity<List<Map<String, Object>>> getSaldoMensile(@RequestParam(defaultValue = "6") int mesi) {
        return ResponseEntity.ok(primaNotaService.getSaldoMensile(mesi));
    }

    // ✅ Ottenere la distribuzione delle categorie per tipo (ENTRATA o USCITA)
    @GetMapping("/dashboard/categorie")
    public ResponseEntity<Map<String, BigDecimal>> getDistribuzioneCategorie(@RequestParam TipoMovimento tipoMovimento) {
        return ResponseEntity.ok(primaNotaService.getDistribuzioneCategorie(tipoMovimento));
    }

    @GetMapping("/dashboard/prodotti")
    public ResponseEntity<Map<String, BigDecimal>> getProdottiPiuCostosiInUscite() {
        return ResponseEntity.ok(primaNotaService.getProdottiPiuCostosiInUscite());
    }    

    @GetMapping("/dashboard/prodotti-rapporto-entrate-uscite")
    public ResponseEntity<Map<String, BigDecimal>> getProdottiConRapportoEntrateUscite() {
    return ResponseEntity.ok(primaNotaService.getProdottiConRapportoEntrateUscite());
    }


}
