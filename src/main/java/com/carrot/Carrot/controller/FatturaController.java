package com.carrot.Carrot.controller;

import com.carrot.Carrot.dto.fatturarequest;
import com.carrot.Carrot.model.Fattura;
import com.carrot.Carrot.service.FatturaService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import javax.xml.bind.JAXBException;

@RestController
@RequestMapping("/api/fatture")
public class FatturaController {
    private final FatturaService fatturaService;

    public FatturaController(FatturaService fatturaService) {
        this.fatturaService = fatturaService;
    }

    @PostMapping("/genera")
    public ResponseEntity<?> generaFattura(@RequestBody fatturarequest fatturarequest) {
        try {
            fatturaService.generaFattura(fatturarequest.getOrdine(), fatturarequest.getInserisciMovimento(),fatturarequest.isApplicareRitenuta(), fatturarequest.getRitenutaAcconto(), fatturarequest.getScadenza(), fatturarequest.getStato());
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
