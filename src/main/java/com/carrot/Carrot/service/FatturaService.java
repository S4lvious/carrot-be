package com.carrot.Carrot.service;

import com.carrot.Carrot.dto.FatturaCompletaDTO;
import com.carrot.Carrot.enumerator.TipoDocumento;
import com.carrot.Carrot.enumerator.TipoMovimento;
import com.carrot.Carrot.model.*;
import com.carrot.Carrot.repository.CategoriaMovimentoRepository;
import com.carrot.Carrot.repository.FatturaRepository;
import com.carrot.Carrot.repository.MetodoPagamentoRepository;
import com.carrot.Carrot.repository.OrdineRepository;
import com.carrot.Carrot.repository.PrimaNotaRepository;
import com.carrot.Carrot.security.MyUserDetails;
import com.carrot.Carrot.repository.OperazioneRepository;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBException;

@Service
public class FatturaService {

    @Autowired
    private FatturaRepository fatturaRepository;

    @Autowired
    private OrdineRepository ordineRepository;

    @Autowired
    private OperazioneRepository operazioneRepository;

    @Autowired
    private PrimaNotaRepository primaNotaRepository;


    // Metodo di supporto per ottenere l'utente corrente
    private User getCurrentUser() {
        MyUserDetails userDetails = (MyUserDetails) SecurityContextHolder
                                        .getContext()
                                        .getAuthentication()
                                        .getPrincipal();
        return userDetails.getUser();
    }

    

    @Transactional
    public Fattura generaFatturaCompleta(FatturaCompletaDTO dto) {
    
        // 1) CARICO L’ORDINE DAL DB TRAMITE L’ID
        Long ordineId = dto.getOrdine().getId();
        Ordine incarico = ordineRepository.findById(ordineId)
            .orElseThrow(() -> new IllegalStateException("Ordine non trovato"));
    
        // 2) CONTROLLO CHE L’ORDINE SIA DELL’UTENTE CORRENTE + NO FATTURE DOPPIE
        User currentUser = getCurrentUser();
        if (!incarico.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("L'ordine non appartiene all'utente autenticato.");
        }
        if (fatturaRepository.existsByOrdineIdAndUserId(incarico.getId(), currentUser.getId())) {
            throw new IllegalStateException("Esiste già una fattura per questo ordine.");
        }
    
        // 3) RECUPERO E VERIFICO DATI DELL’EMITTENTE (USER)
        List<String> erroriEmittente = new ArrayList<>();
        if (currentUser.getPartitaIva() == null) erroriEmittente.add("Partita IVA");
        if (currentUser.getIndirizzo() == null)  erroriEmittente.add("Indirizzo");
        if (currentUser.getCap() == null)        erroriEmittente.add("CAP");
        if (currentUser.getCitta() == null)      erroriEmittente.add("Città");
        if (currentUser.getProvincia() == null)  erroriEmittente.add("Provincia");
        if (!erroriEmittente.isEmpty()) {
            throw new IllegalStateException(
                "Dati mancanti per l'emittente: " + String.join(", ", erroriEmittente)
            );
        }
    
        // 4) RECUPERO E VERIFICO DATI DEL CLIENTE
        Cliente cliente = incarico.getCliente();
        if (cliente == null) {
            throw new IllegalStateException("Errore: L'ordine non ha un cliente associato.");
        }
        List<String> erroriCliente = new ArrayList<>();
        if (cliente.getPartitaIva() == null && cliente.getCodiceFiscale() == null)
            erroriCliente.add("Partita IVA o Codice Fiscale (uno dei due obbligatorio)");
        if (cliente.getIndirizzo() == null) erroriCliente.add("Indirizzo");
        if (cliente.getCap() == null)       erroriCliente.add("CAP");
        if (cliente.getCitta() == null)     erroriCliente.add("Città");
        if (cliente.getProvincia() == null) erroriCliente.add("Provincia");
        if (!erroriCliente.isEmpty()) {
            throw new IllegalStateException(
                "Dati mancanti per il cliente: " + String.join(", ", erroriCliente)
            );
        }
    
        // 5) CREO LA FATTURA
        Fattura fattura = new Fattura();
        fattura.setUser(currentUser);
        fattura.setDataEmissione(LocalDate.now());
    
        // Numero Fattura progressivo
        int annoCorrente = LocalDate.now().getYear();
        int numeroProgressivo = fatturaRepository.countByUserAndYear(currentUser.getId(), annoCorrente) + 1;
        String numeroFattura = String.format("%d-%03d", annoCorrente, numeroProgressivo);
        fattura.setNumeroFattura(numeroFattura);
    
        // Collego l’Ordine persistito (non il DTO) per evitare problemi
        fattura.setOrdine(incarico);
    
        // 6) CALCOLO I TOTALI (NETTO, IVA, LORDO) DAI DETTAGLI DELL’ORDINE
        BigDecimal totaleNetto = incarico.getDettagliOrdine().stream()
            .map(d -> d.getPrezzoUnitario().multiply(BigDecimal.valueOf(d.getQuantita())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        fattura.setTotaleNetto(totaleNetto);
    
        BigDecimal totaleIVA = incarico.getDettagliOrdine().stream()
            .map(d -> {
                BigDecimal imponibileRiga = d.getPrezzoUnitario()
                        .multiply(BigDecimal.valueOf(d.getQuantita()));
                BigDecimal aliquota = d.getProdotto().getAliquotaIVA();
                return imponibileRiga.multiply(aliquota)
                                     .divide(BigDecimal.valueOf(100), MathContext.DECIMAL128);
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        fattura.setTotaleIVA(totaleIVA);
    
        BigDecimal totaleLordo = totaleNetto.add(totaleIVA);
        fattura.setTotaleLordo(totaleLordo);
    
        // 7) RITENUTA d’ACCONTO
        boolean applicareRitenuta   = dto.isApplicareRitenuta();
        BigDecimal ritenutaAcconto  = dto.getRitenutaAcconto();
        fattura.setApplicareRitenuta(applicareRitenuta);
        fattura.setRitenutaAcconto(BigDecimal.ZERO);
        BigDecimal importoRitenuta = BigDecimal.ZERO;
    
        if (applicareRitenuta && ritenutaAcconto != null && ritenutaAcconto.compareTo(BigDecimal.ZERO) > 0) {
            importoRitenuta = totaleLordo.multiply(ritenutaAcconto)
                                          .divide(BigDecimal.valueOf(100), MathContext.DECIMAL128);
            fattura.setRitenutaAcconto(ritenutaAcconto);
        }
        fattura.setImportoRitenuta(importoRitenuta);
    
        BigDecimal totaleDovuto = totaleLordo.subtract(importoRitenuta);
        fattura.setTotaleDovuto(totaleDovuto);
    
        // 8) STATO, SCADENZA, etc.
        fattura.setInviataAdE(false);
        fattura.setDataScadenza(dto.getScadenza());
        fattura.setStato(dto.getStato());  // es. "Pagata" / "Non pagata"
    
        // 9) DATI EMITTENTE dal currentUser
        String denominazioneEmittente = (currentUser.getRagioneSociale() != null 
            && !currentUser.getRagioneSociale().trim().isEmpty())
            ? currentUser.getRagioneSociale()
            : (currentUser.getNome() + " " + currentUser.getCognome()).trim();
    
        fattura.setNomeEmittente(denominazioneEmittente);
        fattura.setIndirizzoEmittente(currentUser.getIndirizzo());
        fattura.setCapEmittente(currentUser.getCap());
        fattura.setCittaEmittente(currentUser.getCitta());
        fattura.setProvinciaEmittente(currentUser.getProvincia());
        fattura.setPartitaIVAEmittente(currentUser.getPartitaIva());
        fattura.setCodiceFiscaleEmittente(currentUser.getCodiceFiscale());
    
        // 10) DATI CLIENTE dal ‘incarico.getCliente()’
        String denominazioneCliente;
        if (cliente.getRagioneSociale() != null && !cliente.getRagioneSociale().trim().isEmpty()) {
            denominazioneCliente = cliente.getRagioneSociale();
        } else {
            denominazioneCliente = (cliente.getNome() + " " + cliente.getCognome()).trim();
        }
        fattura.setNomeCliente(denominazioneCliente);
        fattura.setIndirizzoCliente(cliente.getIndirizzo());
        fattura.setCapCliente(cliente.getCap());
        fattura.setCittaCliente(cliente.getCitta());
        fattura.setProvinciaCliente(cliente.getProvincia());
        fattura.setPartitaIVACliente(cliente.getPartitaIva());
        fattura.setCodiceFiscaleCliente(cliente.getCodiceFiscale());
    
        // CodiceSDI, PEC, etc. (facoltativi)
        fattura.setCodiceSDIDestinatario(cliente.getCodiceSDI());
        fattura.setPecDestinatario(cliente.getPec());
        fattura.setDenominazioneDestinatario(denominazioneCliente);
        fattura.setNazioneDestinatario(cliente.getNazione() != null ? cliente.getNazione() : "IT");
    
        // 11) NUOVI CAMPI dal DTO
        fattura.setTipoDocumento(dto.getTipoDocumento());   // FATT, NDC
        fattura.setCausale(dto.getCausale());
        fattura.setCausalePagamento(dto.getCausalePagamento());
    
        fattura.setDatiBollo(dto.getDatiBollo());
        fattura.setDatiCassaPrevidenziale(dto.getDatiCassaPrevidenziale());
        fattura.setDatiPagamento(dto.getDatiPagamento());
    
        fattura.setDatiOrdineAcquisto(dto.getDatiOrdineAcquisto());
        fattura.setDatiContratto(dto.getDatiContratto());
        fattura.setDatiConvenzione(dto.getDatiConvenzione());
        fattura.setDatiRicezione(dto.getDatiRicezione());
        fattura.setDatiFattureCollegate(dto.getDatiFattureCollegate());
    
        // 12) SALVA FATTURA
        fatturaRepository.save(fattura);
    
        // 13) SEGNO L’ORDINE COME FATTURATO
        incarico.setFatturato(true);
        ordineRepository.save(incarico);
    
        // 14) LOG operazione
        Operazione operazione = new Operazione();
        operazione.setUser(currentUser);
        operazione.setEntita("Fattura");
        operazione.setTipo("Aggiunta");
        operazione.setDescrizione("Nuova fattura creata: " + fattura.getNumeroFattura()
            + " per l'ordine " + incarico.getId());
        operazione.setDataOperazione(LocalDateTime.now());
        operazioneRepository.save(operazione);
    
        // 15) EVENTUALE MOVIMENTO DI PRIMA NOTA
        if (dto.isInserisciMovimento()) {
            PrimaNota primaNota = new PrimaNota();
            primaNota.setDataOperazione(fattura.getDataEmissione());
            primaNota.setFattura(fattura);
            primaNota.setImporto(totaleDovuto);
            primaNota.setIncaricoId(incarico.getId());
            primaNota.setNome(fattura.getNumeroFattura());
            primaNota.setTipoMovimento(TipoMovimento.ENTRATA);
            primaNota.setUser(currentUser);
            primaNotaRepository.save(primaNota);
        }
    
        return fattura;
    }
        private static final String API_FATTURA_ELETTRONICA_URL = "https://fattura-elettronica-api.it/ws2.0/test/fatture";
        @Autowired
    private RestTemplate restTemplate;  // O un HttpClient a tua scelta


@Transactional
public Fattura inviaFatturaAFornitoreEsterno(Fattura fattura) {
    // 1) Costruisci il body JSON come una mappa o un DTO
    Map<String, Object> jsonBody = costruisciJsonFattura(fattura);

    // 2) Imposta gli headers
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Basic cy5saWNjYXJkbzAyMkBnbWFpbC5jb206eFUzN21MbHJ4Zw==");

    HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(jsonBody, headers);

    // 3) Effettua la chiamata HTTP POST
    ResponseEntity<Map> response = restTemplate.exchange(
        API_FATTURA_ELETTRONICA_URL,
        HttpMethod.POST,
        requestEntity,
        Map.class
    );

    // 4) Gestione della risposta
    if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        Map<String, Object> responseBody = response.getBody();

        Object idApi = responseBody.get("id");
        Object sdiIdentificativo = responseBody.get("sdi_identificativo");
        Object sdiNomeFile = responseBody.get("sdi_nome_file");
        Object sdiFattura = responseBody.get("sdi_fattura");
        Object sdiStato = responseBody.get("sdi_stato");
        Object sdiMessaggio = responseBody.get("sdi_messaggio");

        // 5) Aggiorna i campi della fattura
        fattura.setItalaID((idApi != null) ? idApi.toString() : null);
        fattura.setSdiIdentificativo((sdiIdentificativo != null) ? sdiIdentificativo.toString() : null);
        fattura.setSdiNomeFile((sdiNomeFile != null) ? sdiNomeFile.toString() : null);
        fattura.setSdiFattura((sdiFattura != null) ? sdiFattura.toString() : null);
        fattura.setSdiStato((sdiStato != null) ? sdiStato.toString() : null);
        fattura.setSdiMessaggio((sdiMessaggio != null) ? sdiMessaggio.toString() : null);

        if ("INVI".equals(fattura.getSdiStato())) {
            fattura.setInviataAdE(true);
        }

        // 6) Salva la fattura aggiornata
        fatturaRepository.save(fattura);

    } else {
        throw new IllegalStateException(
            "Errore nell'invio della fattura: " + response.getStatusCode()
        );
    }

    return fattura;
}

    /**
     * Costruisce il body JSON da inviare all'API esterna, mappando i campi della Fattura
     * secondo la specifica fornita.
     */
    private Map<String, Object> costruisciJsonFattura(Fattura fattura) {
        // Esempio di mappatura "manuale" in una struttura Map:
        Map<String, Object> root = new HashMap<>();

        // 1) piva_mittente: la p.IVA dell'emittente
        root.put("piva_mittente", fattura.getPartitaIVAEmittente());

        // 2) Destinatario
        Map<String, Object> destinatario = new HashMap<>();
        destinatario.put("CodiceSDI", (fattura.getCodiceSDIDestinatario() != null)
            ? fattura.getCodiceSDIDestinatario() : "0000000");
        destinatario.put("PEC", fattura.getPecDestinatario());
        destinatario.put("PartitaIVA", (fattura.getPartitaIVACliente() != null)
            ? fattura.getPartitaIVACliente() : "00000000000");
        destinatario.put("CodiceFiscale", (fattura.getCodiceFiscaleCliente() != null)
            ? fattura.getCodiceFiscaleCliente() : "00000000000");
        destinatario.put("Denominazione", fattura.getDenominazioneDestinatario());
        destinatario.put("Indirizzo", fattura.getIndirizzoCliente());
        destinatario.put("CAP", fattura.getCapCliente());
        destinatario.put("Comune", fattura.getCittaCliente());
        destinatario.put("Provincia", fattura.getProvinciaCliente());
        destinatario.put("Nazione", (fattura.getNazioneDestinatario() != null)
            ? fattura.getNazioneDestinatario() : "IT");
        root.put("destinatario", destinatario);

        // 3) documento
        Map<String, Object> documento = new HashMap<>();
        documento.put("tipo", fattura.getTipoDocumento());            // FATT o NDC
        documento.put("Data", fattura.getDataEmissione().toString()); // es. "2023-10-03"
        documento.put("Numero", fattura.getNumeroFattura());
        documento.put("Causale", fattura.getCausale());
        documento.put("ImportoRitenuta", fattura.getImportoRitenuta().toPlainString());
        documento.put("AliquotaRitenuta", (fattura.getRitenutaAcconto() != null)
            ? fattura.getRitenutaAcconto().toPlainString()
            : "0.00");
        documento.put("CausalePagamento", fattura.getCausalePagamento());

        // DatiBollo
        Map<String, Object> datiBollo = new HashMap<>();
        datiBollo.put("BolloVirtuale", (fattura.getDatiBollo() != null && Boolean.TRUE.equals(fattura.getDatiBollo().getBolloVirtuale()))
            ? "SI" : "NO");
        datiBollo.put("ImportoBollo", (fattura.getDatiBollo() != null)
            ? fattura.getDatiBollo().getImportoBollo().toPlainString()
            : "0.00");
        documento.put("DatiBollo", datiBollo);

        // DatiCassaPrevidenziale
        Map<String, Object> datiCassaMap = new HashMap<>();
        if (fattura.getDatiCassaPrevidenziale() != null) {
            datiCassaMap.put("TipoCassa", fattura.getDatiCassaPrevidenziale().getTipoCassa());
            datiCassaMap.put("AlCassa", fattura.getDatiCassaPrevidenziale().getAlCassa());
            datiCassaMap.put("ImportoContributoCassa", (fattura.getDatiCassaPrevidenziale().getImportoContributoCassa() != null)
                ? fattura.getDatiCassaPrevidenziale().getImportoContributoCassa().toPlainString() : "");
            datiCassaMap.put("ImponibileCassa", (fattura.getDatiCassaPrevidenziale().getImponibileCassa() != null)
                ? fattura.getDatiCassaPrevidenziale().getImponibileCassa().toPlainString() : "");
            datiCassaMap.put("AliquotaIVA", (fattura.getDatiCassaPrevidenziale().getAliquotaIVACassa() != null)
                ? fattura.getDatiCassaPrevidenziale().getAliquotaIVACassa().toPlainString() : "");
            datiCassaMap.put("Natura", fattura.getDatiCassaPrevidenziale().getNatura());
            datiCassaMap.put("Ritenuta", (Boolean.TRUE.equals(fattura.getDatiCassaPrevidenziale().getRitenuta()))
                ? "SI" : "NO");
        } else {
            // campi vuoti
            datiCassaMap.put("TipoCassa", "");
            datiCassaMap.put("AlCassa", "");
            datiCassaMap.put("ImportoContributoCassa", "");
            datiCassaMap.put("ImponibileCassa", "");
            datiCassaMap.put("AliquotaIVA", "");
            datiCassaMap.put("Natura", "");
            datiCassaMap.put("Ritenuta", "NO");
        }
        documento.put("DatiCassaPrevidenziale", datiCassaMap);

        // Dati Ordine/Contratto/Convenzione/Ricezione/FattureCollegate
        documento.put("DatiOrdineAcquisto", mappaDocumentoRiferimento(fattura.getDatiOrdineAcquisto()));
        documento.put("DatiContratto", mappaDocumentoRiferimento(fattura.getDatiContratto()));
        documento.put("DatiConvenzione", mappaDocumentoRiferimento(fattura.getDatiConvenzione()));
        documento.put("DatiRicezione", mappaDocumentoRiferimento(fattura.getDatiRicezione()));
        documento.put("DatiFattureCollegate", mappaDocumentoRiferimento(fattura.getDatiFattureCollegate()));

        // Dati Pagamento
        Map<String, Object> datiPagMap = new HashMap<>();
        if (fattura.getDatiPagamento() != null) {
            datiPagMap.put("CondizioniPagamento", fattura.getDatiPagamento().getCondizioniPagamento());
            Map<String, Object> dettPag = new HashMap<>();
            DettaglioPagamento dp = fattura.getDatiPagamento().getDettaglioPagamento();
            if (dp != null) {
                dettPag.put("Beneficiario", dp.getBeneficiario());
                dettPag.put("ModalitaPagamento", dp.getModalitaPagamento());
                dettPag.put("DataScadenzaPagamento", (dp.getDataScadenzaPagamento() != null)
                    ? dp.getDataScadenzaPagamento().toString()
                    : "");
                dettPag.put("IBAN", dp.getIban());
                dettPag.put("ImportoPagamento", (dp.getImportoPagamento() != null)
                    ? dp.getImportoPagamento().toPlainString()
                    : "0.00");
                dettPag.put("IstitutoFinanziario", dp.getIstitutoFinanziario());
            }
            datiPagMap.put("DettaglioPagamento", dettPag);

        } else {
            // default
            datiPagMap.put("CondizioniPagamento", "TP02");
            Map<String, Object> dettPag = new HashMap<>();
            dettPag.put("Beneficiario", "");
            dettPag.put("ModalitaPagamento", "MP01");
            dettPag.put("DataScadenzaPagamento", "");
            dettPag.put("IBAN", "");
            dettPag.put("ImportoPagamento", "0.00");
            dettPag.put("IstitutoFinanziario", "");
            datiPagMap.put("DettaglioPagamento", dettPag);
        }
        documento.put("DatiPagamento", datiPagMap);

        root.put("documento", documento);

        // 4) righe
        List<Map<String, Object>> righeList = new ArrayList<>();
        // Genera righe da ordini o se la fattura avesse un meccanismo di righe interne
        // Es: prendiamo dal "Ordine" con i suoi DettagliOrdine
        if (fattura.getOrdine() != null && fattura.getOrdine().getDettagliOrdine() != null) {
            for (DettaglioOrdine det : fattura.getOrdine().getDettagliOrdine()) {
                Map<String, Object> riga = new HashMap<>();
                Prodotto prod = det.getProdotto();
                // Descrizione => usiamo nome del prodotto
                riga.put("Descrizione", (prod.getDescrizione() != null && !prod.getDescrizione().isEmpty())
                    ? prod.getDescrizione()
                    : prod.getNome());
                // PrezzoUnitario => det.getPrezzoUnitario()
                riga.put("PrezzoUnitario", det.getPrezzoUnitario().toPlainString());
                // AliquotaIVA => es. 22
                riga.put("AliquotaIVA", prod.getAliquotaIVA().intValue()); // se BigDecimal => converti int
                // Quantita => det.getQuantita()
                riga.put("Quantita", det.getQuantita());

                // ScontoMaggiorazione => se non lo gestisci, metti default
                Map<String, Object> scontoMap = new HashMap<>();
                scontoMap.put("Tipo", "SC");
                scontoMap.put("Percentuale", "");
                scontoMap.put("Importo", "");
                riga.put("ScontoMaggiorazione", scontoMap);

                // Natura => se non c'è => vuoto
                riga.put("Natura", (prod.getNatura() != null) ? prod.getNatura() : "");

                // CodiceArticolo => se usi codici (prod.getCodiceTipo, prod.getCodiceValore)
                Map<String, Object> codiceArticolo = new HashMap<>();
                codiceArticolo.put("CodiceTipo", (prod.getCodiceTipo() != null) ? prod.getCodiceTipo() : "");
                codiceArticolo.put("CodiceValore", (prod.getCodiceValore() != null) ? prod.getCodiceValore() : "");
                riga.put("CodiceArticolo", codiceArticolo);

                // UnitaMisura => prod.getUnitaMisura()?
                riga.put("UnitaMisura", (prod.getUnitaMisura() != null) ? prod.getUnitaMisura() : "");

                // EsigibilitaIVA => se gestisci, altrimenti blank
                riga.put("EsigibilitaIVA", (prod.getEsigibilitaIVA() != null) ? prod.getEsigibilitaIVA() : "");

                righeList.add(riga);
            }
        }
        root.put("righe", righeList);

        return root;
    }

    /**
     * Metodo di comodo per creare la mappa dei dati di riferimento (ordine, contratto, ecc.).
     */
    private Map<String, Object> mappaDocumentoRiferimento(DocumentoRiferimento docRef) {
        Map<String, Object> map = new HashMap<>();
        if (docRef != null) {
            map.put("IdDocumento", docRef.getIdDocumento());
            // Data => se non null, in formato "yyyy-mm-dd"
            map.put("Data", (docRef.getData() != null) ? docRef.getData().toString() : "");
            map.put("CodiceCommessaConvenzione", docRef.getCodiceCommessaConvenzione());
            map.put("CodiceCUP", docRef.getCodiceCUP());
            map.put("CodiceCIG", docRef.getCodiceCIG());
        } else {
            // Se non esiste => campi vuoti
            map.put("IdDocumento", "");
            map.put("Data", "");
            map.put("CodiceCommessaConvenzione", "");
            map.put("CodiceCUP", "");
            map.put("CodiceCIG", "");
        }
        return map;
    }
    

    public List<Fattura> getAllFatture() {
        // Recupera solo le fatture dell'utente corrente
        Long currentUserId = getCurrentUser().getId();
        return fatturaRepository.findByUserId(currentUserId);
    }

    public List<Fattura> getFattureNonPagate() {
        Long currentUserId = getCurrentUser().getId();
        return fatturaRepository.findByStatoAndUserId("Non pagata", currentUserId);
    }

    public void updateFattura(Fattura fattura) {
        // Assicurati che la fattura appartenga all'utente corrente prima di aggiornare
        User currentUser = getCurrentUser();
        if (!fattura.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("Accesso negato: la fattura non appartiene all'utente corrente.");
        }
        fatturaRepository.save(fattura);
    }

    public Optional<Fattura> getFatturaById(Long id) {
        Long currentUserId = getCurrentUser().getId();
        return fatturaRepository.findByIdAndUserId(id, currentUserId);
    }

    public void deleteFattura(Long id) {
        Long currentUserId = getCurrentUser().getId();
        Optional<Fattura> fatturaOpt = fatturaRepository.findByIdAndUserId(id, currentUserId);
        if (fatturaOpt.isPresent()) {
            Fattura fattura = fatturaOpt.get();
            fatturaRepository.delete(fattura);
            // Aggiornamento dell'ordine associato
            Ordine ordine = ordineRepository.getReferenceById(fattura.getOrdine().getId());
            ordine.setFatturato(false);
            ordineRepository.save(ordine);
        } else {
            throw new IllegalStateException("Fattura non trovata o accesso negato.");
        }
    }

    public byte[] generaPdfFattura(Fattura fattura) throws IOException {
        // Si assume che la fattura sia già filtrata e appartenga all'utente corrente.
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        PdfFont fontNormale = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA);
        PdfFont fontTitolo = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD);
        DecimalFormat df = new DecimalFormat("0.00");

        // Intestazione Azienda
        document.add(new Paragraph(fattura.getNomeEmittente())
                .setFont(fontTitolo)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.LEFT));

        document.add(new Paragraph(fattura.getIndirizzoEmittente() + ", " +
                fattura.getCapEmittente() + " " +
                fattura.getCittaEmittente() + " (" +
                fattura.getProvinciaEmittente() + ")\n" +
                "P.IVA: " + fattura.getPartitaIVAEmittente() +
                " - CF: " + fattura.getCodiceFiscaleEmittente())
                .setFont(fontNormale)
                .setFontSize(10));

        // Titolo della Fattura
        document.add(new Paragraph("Fattura N° " + fattura.getNumeroFattura())
                .setFont(fontTitolo)
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph("Data Emissione: " + fattura.getDataEmissione() +
                "\nScadenza: " + fattura.getDataScadenza())
                .setFont(fontNormale)
                .setFontSize(10));

        // Dati Cliente
        document.add(new Paragraph("Dati Cliente:")
                .setFont(fontTitolo)
                .setFontSize(12));

        document.add(new Paragraph(fattura.getNomeCliente() + "\n" +
                fattura.getIndirizzoCliente() + ", " +
                fattura.getCapCliente() + " " +
                fattura.getCittaCliente() + " (" +
                fattura.getProvinciaCliente() + ")\n" +
                "P.IVA: " + fattura.getPartitaIVACliente() +
                " - CF: " + fattura.getCodiceFiscaleCliente())
                .setFont(fontNormale)
                .setFontSize(10));

        // Tabella Prodotti
        Table table = new Table(new float[]{40f, 10f, 15f, 15f, 20f});
        table.setWidth(UnitValue.createPercentValue(100));

        // Intestazioni tabella
        aggiungiCellaTabella(table, "Prodotto", fontTitolo);
        aggiungiCellaTabella(table, "Quantità", fontTitolo);
        aggiungiCellaTabella(table, "Prezzo Unitario", fontTitolo);
        aggiungiCellaTabella(table, "IVA (%)", fontTitolo);
        aggiungiCellaTabella(table, "Totale", fontTitolo);

        // Popolamento tabella con i dettagli della fattura
        for (DettaglioOrdine dettaglio : fattura.getOrdine().getDettagliOrdine()) {
            aggiungiCellaTabella(table, dettaglio.getProdotto().getNome(), fontNormale);
            aggiungiCellaTabella(table, String.valueOf(dettaglio.getQuantita()), fontNormale);
            aggiungiCellaTabella(table, String.format("%.2f €", dettaglio.getPrezzoUnitario()), fontNormale);
            aggiungiCellaTabella(table, dettaglio.getProdotto().getAliquotaIVA() + "%", fontNormale);
            BigDecimal prezzoUnitario = dettaglio.getPrezzoUnitario();
            BigDecimal quantita = BigDecimal.valueOf(dettaglio.getQuantita());
            BigDecimal aliquotaIVA = dettaglio.getProdotto().getAliquotaIVA();
            BigDecimal multiplicatoreIVA = BigDecimal.ONE.add(aliquotaIVA.divide(BigDecimal.valueOf(100)));
            BigDecimal totaleBD = prezzoUnitario.multiply(quantita).multiply(multiplicatoreIVA);
            double totale = totaleBD.doubleValue();
            aggiungiCellaTabella(table, String.format("%.2f €", totale), fontNormale);
        }

        document.add(table);

        // Totali e riepilogo finanziario
        document.add(new Paragraph("\nSubtotale: " + df.format(fattura.getTotaleNetto()) + " €")
                .setFont(fontNormale));
        document.add(new Paragraph("IVA Totale: " + df.format(fattura.getTotaleIVA()) + " €")
                .setFont(fontNormale));

        if (fattura.isApplicareRitenuta()) {
            BigDecimal ritenuta = fattura.getTotaleLordo()
                    .multiply(fattura.getRitenutaAcconto())
                    .divide(BigDecimal.valueOf(100), MathContext.DECIMAL128);
            document.add(new Paragraph("Ritenuta d'acconto (" + fattura.getRitenutaAcconto() + "%): " +
                    df.format(ritenuta.doubleValue()) + " €")
                    .setFont(fontNormale));
        }

        document.add(new Paragraph("Totale Dovuto: " + df.format(fattura.getTotaleDovuto()) + " €")
                .setFont(fontTitolo));

        document.close();
        return outputStream.toByteArray();
    }

    private void aggiungiCellaTabella(Table table, String contenuto, PdfFont font) {
        table.addCell(new Cell().add(new Paragraph(contenuto).setFont(font).setFontSize(10)));
    }

    public BigDecimal getFatturatoMese() {
        Long currentUserId = getCurrentUser().getId();
        return fatturaRepository.findByUserId(currentUserId).stream()
                .filter(f -> f.getDataEmissione().getMonth().equals(LocalDate.now().getMonth()))
                .map(Fattura::getTotaleLordo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public String generaXmlFattura(Fattura fattura) throws JAXBException {
        StringWriter writer = new StringWriter();
        DecimalFormat df = new DecimalFormat("0.00");

        writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        writer.append("<FatturaElettronica versione=\"FPR12\" xmlns=\"http://ivaservizi.agenziaentrate.gov.it/docs/xsd/fatture/v1.2\">\n");

        // CedentePrestatore (emittente)
        writer.append("  <CedentePrestatore>\n");
        writer.append("    <Denominazione>").append(fattura.getNomeEmittente()).append("</Denominazione>\n");
        writer.append("    <PartitaIVA>").append(fattura.getPartitaIVAEmittente()).append("</PartitaIVA>\n");
        writer.append("    <CodiceFiscale>").append(fattura.getCodiceFiscaleEmittente()).append("</CodiceFiscale>\n");
        writer.append("    <Indirizzo>").append(fattura.getIndirizzoEmittente()).append("</Indirizzo>\n");
        writer.append("    <CAP>").append(fattura.getCapEmittente()).append("</CAP>\n");
        writer.append("    <Citta>").append(fattura.getCittaEmittente()).append("</Citta>\n");
        writer.append("    <Provincia>").append(fattura.getProvinciaEmittente()).append("</Provincia>\n");
        writer.append("  </CedentePrestatore>\n");

        // CessionarioCommittente (cliente)
        writer.append("  <CessionarioCommittente>\n");
        writer.append("    <Denominazione>").append(fattura.getNomeCliente()).append("</Denominazione>\n");
        writer.append("    <CodiceFiscale>").append(fattura.getCodiceFiscaleCliente()).append("</CodiceFiscale>\n");
        writer.append("    <Indirizzo>").append(fattura.getIndirizzoCliente()).append("</Indirizzo>\n");
        writer.append("    <CAP>").append(fattura.getCapCliente()).append("</CAP>\n");
        writer.append("    <Citta>").append(fattura.getCittaCliente()).append("</Citta>\n");
        writer.append("    <Provincia>").append(fattura.getProvinciaCliente()).append("</Provincia>\n");
        writer.append("  </CessionarioCommittente>\n");

        // Dati Generali
        writer.append("  <DatiGenerali>\n");
        writer.append("    <NumeroFattura>").append(fattura.getNumeroFattura()).append("</NumeroFattura>\n");
        writer.append("    <DataEmissione>").append(fattura.getDataEmissione().toString()).append("</DataEmissione>\n");
        writer.append("    <Totale>").append(df.format(fattura.getTotaleDovuto())).append("</Totale>\n");
        writer.append("  </DatiGenerali>\n");

        // Dettagli della Fattura
        writer.append("  <DatiBeniServizi>\n");
        for (DettaglioOrdine dettaglio : fattura.getOrdine().getDettagliOrdine()) {
            BigDecimal totaleBD = dettaglio.getPrezzoUnitario()
                    .multiply(BigDecimal.valueOf(dettaglio.getQuantita()))
                    .multiply(BigDecimal.ONE.add(dettaglio.getProdotto().getAliquotaIVA().divide(BigDecimal.valueOf(100))));
            double totale = totaleBD.doubleValue();
            writer.append("    <DettaglioLinea>\n");
            writer.append("      <Descrizione>").append(dettaglio.getProdotto().getNome()).append("</Descrizione>\n");
            writer.append("      <Quantita>").append(String.valueOf(dettaglio.getQuantita())).append("</Quantita>\n");
            writer.append("      <PrezzoUnitario>").append(df.format(dettaglio.getPrezzoUnitario())).append("</PrezzoUnitario>\n");
            writer.append("      <AliquotaIVA>").append(df.format(dettaglio.getProdotto().getAliquotaIVA())).append("</AliquotaIVA>\n");
            writer.append("      <Totale>").append(df.format(totale)).append("</Totale>\n");
            writer.append("    </DettaglioLinea>\n");
        }
        writer.append("  </DatiBeniServizi>\n");

        // Chiusura XML
        writer.append("</FatturaElettronica>");

        return writer.toString();
    }

    public Map<String, BigDecimal> getFatturatoMensile() {
        Long currentUserId = getCurrentUser().getId();
        return fatturaRepository.findByUserId(currentUserId).stream()
                .collect(Collectors.groupingBy(
                        f -> f.getDataEmissione().getYear() + "-" + f.getDataEmissione().getMonthValue(),
                        Collectors.reducing(BigDecimal.ZERO, Fattura::getTotaleLordo, BigDecimal::add)
                ));
    }
}
