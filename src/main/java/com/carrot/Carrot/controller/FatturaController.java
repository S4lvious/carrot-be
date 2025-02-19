package com.carrot.Carrot.controller;

import com.carrot.Carrot.dto.FatturaCompletaDTO;
import com.carrot.Carrot.dto.fatturarequest;
import com.carrot.Carrot.model.Fattura;
import com.carrot.Carrot.service.FatturaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

@RestController
@RequestMapping("/api/fatture")
public class FatturaController {


    @Value("${webhook.token}")
    String tokenSalvato;
    private final FatturaService fatturaService;

    public FatturaController(FatturaService fatturaService) {
        this.fatturaService = fatturaService;
    }

    @PostMapping("/genera")
    public ResponseEntity<?> generaFattura(@RequestBody FatturaCompletaDTO fatturarequest) {
        try {
            fatturaService.generaFatturaCompleta(fatturarequest);
            return ResponseEntity.ok(fatturaService.getAllFatture());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Errore nella generazione della fattura: " + e.getMessage());
        }
    }



    @PostMapping("/webhook/notification")
    public ResponseEntity<String> riceviNotificaFattura(
            @RequestBody List<Map<String, Object>> bodyList,
            @RequestHeader("Authorization") String authHeader) {

        // ✅ Controllo autenticazione
        if (!tokenSalvato.equals(authHeader)) {
            System.err.println("🔴 Tentativo di accesso con token non valido: " + authHeader);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token non valido");
        }

        try {
            for (Map<String, Object> body : bodyList) {
                // ✅ Verifica e parsing dei dati
                Long fatturaId = Long.parseLong(body.get("id").toString());
                String sdiStato = body.getOrDefault("sdi_stato", "UNKNOWN").toString();
                String sdiMessaggio = body.getOrDefault("sdi_messaggio", "").toString();
                String partitaIva = body.getOrDefault("partita_iva", "").toString();

                System.out.println("📩 Notifica ricevuta per fattura ID: " + fatturaId);
                System.out.println("🏢 Azienda: " + partitaIva + " - Stato SDI: " + sdiStato);

                // ✅ Aggiorna il database per la giusta azienda
                fatturaService.aggiornaFatturaPerAzienda(fatturaId, partitaIva, sdiStato, sdiMessaggio);
            }

            return ResponseEntity.ok("✅ Notifica ricevuta con successo");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("❌ Errore nel parsing JSON: " + e.getMessage());
        }
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendInCloud(@RequestBody Fattura fatturarequest) {
        try {
            fatturaService.inviaFatturaAFornitoreEsterno(fatturarequest);
            return ResponseEntity.ok(fatturaService.getAllFatture());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Errore nella generazione della fattura: " + e.getMessage());
        }
    }


    @PostMapping("/generaxml")
    public ResponseEntity<String> generaXml(@RequestBody Fattura fattura) {
        try {
            String xml = fatturaService.generaXmlFattura(fattura);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/xml");
            headers.add("Content-Disposition", "attachment; filename=fattura.xml");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(xml);
        } catch (JAXBException e) {
            return ResponseEntity.internalServerError().body("Errore nella generazione dell'XML");
        }
    }


    @PostMapping("/generapdf")
    public ResponseEntity<byte[]> generaPdf(@RequestBody Fattura fattura) {
        try {
            byte[] pdfBytes = fatturaService.generaPdfFattura(fattura);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "inline; filename=fattura.pdf");
            headers.add("Content-Type", "application/pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @GetMapping
    public List<Fattura> getAllFatture() {
        return fatturaService.getAllFatture();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Fattura> getFatturaById(@PathVariable Long id) {
        return fatturaService.getFatturaById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateFattura(@PathVariable Long id, @RequestBody Fattura fattura) {
        fattura.setId(id);
        fatturaService.updateFattura(fattura);
        return ResponseEntity.ok("Fattura aggiornata con successo");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteFattura(@PathVariable Long id) {
        fatturaService.deleteFattura(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/non-pagate")
    public List<Fattura> getFattureNonPagate() {
        return fatturaService.getFattureNonPagate();
    }

    @GetMapping("/fatturato-mese")
    public ResponseEntity<BigDecimal> getFatturatoMese() {
        return ResponseEntity.ok(fatturaService.getFatturatoMese());
    }
}
