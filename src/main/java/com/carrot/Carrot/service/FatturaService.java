package com.carrot.Carrot.service;

import com.carrot.Carrot.dto.FatturaCompletaDTO;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

    public Fattura generaFatturaCompleta(FatturaCompletaDTO dto) {
        // Estrai dal DTO i campi
        Ordine incarico = dto.getOrdine();
        boolean inserisciMovimento = dto.isInserisciMovimento();
        boolean applicareRitenuta = dto.isApplicareRitenuta();
        BigDecimal ritenutaAcconto = dto.getRitenutaAcconto();
        LocalDate scadenza = dto.getScadenza();
        String stato = dto.getStato();

        // Controlli preliminari
        User currentUser = getCurrentUser();
        if (!incarico.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("L'ordine non appartiene all'utente autenticato.");
        }
        if (fatturaRepository.existsByOrdineIdAndUserId(incarico.getId(), currentUser.getId())) {
            throw new IllegalStateException("Esiste già una fattura per questo ordine.");
        }

        // Verifica dati emittente
        // ... (come facevi prima)

        // Verifica dati cliente
        // ... (come facevi prima)

        // 2) Creazione della Fattura
        Fattura fattura = new Fattura();
        fattura.setUser(currentUser);
        fattura.setDataEmissione(LocalDate.now());

        int annoCorrente = LocalDate.now().getYear();
        int numeroProgressivo = fatturaRepository.countByUserAndYear(currentUser.getId(), annoCorrente) + 1;
        String numeroFattura = String.format("%d-%03d", annoCorrente, numeroProgressivo);
        fattura.setNumeroFattura(numeroFattura);

        // Collega l'ordine
        fattura.setOrdine(incarico);

        // Calcolo totali
        BigDecimal totaleNetto = incarico.getDettagliOrdine().stream()
            .map(d -> d.getPrezzoUnitario().multiply(BigDecimal.valueOf(d.getQuantita())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        fattura.setTotaleNetto(totaleNetto);

        BigDecimal totaleIVA = incarico.getDettagliOrdine().stream()
            .map(d -> {
                BigDecimal imponibileRiga = d.getPrezzoUnitario().multiply(BigDecimal.valueOf(d.getQuantita()));
                BigDecimal aliquotaProdotto = d.getProdotto().getAliquotaIVA();
                return imponibileRiga.multiply(aliquotaProdotto).divide(BigDecimal.valueOf(100), MathContext.DECIMAL128);
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        fattura.setTotaleIVA(totaleIVA);

        BigDecimal totaleLordo = totaleNetto.add(totaleIVA);
        fattura.setTotaleLordo(totaleLordo);

        // Ritenuta
        fattura.setApplicareRitenuta(applicareRitenuta);
        fattura.setRitenutaAcconto(BigDecimal.ZERO);
        BigDecimal importoRitenuta = BigDecimal.ZERO;

        if (applicareRitenuta && ritenutaAcconto != null) {
            importoRitenuta = totaleLordo.multiply(ritenutaAcconto).divide(BigDecimal.valueOf(100), MathContext.DECIMAL128);
            fattura.setRitenutaAcconto(ritenutaAcconto);
        }
        fattura.setImportoRitenuta(importoRitenuta);

        BigDecimal totaleDovuto = totaleLordo.subtract(importoRitenuta);
        fattura.setTotaleDovuto(totaleDovuto);

        // 5) Campi generali
        fattura.setInviataAdE(false);
        fattura.setDataScadenza(scadenza);
        fattura.setStato(stato);

        // 5.1 Dati Emittente
        // ... come già facevi

        // 5.2 Dati Cliente
        // ... come già facevi

        // CodiceSDI, PEC, Nazione
        // ... come già facevi

        // 6) Assegnazione dei NUOVI CAMPI dal DTO
        fattura.setTipoDocumento(dto.getTipoDocumento());
        fattura.setCausale(dto.getCausale());
        fattura.setCausalePagamento(dto.getCausalePagamento());

        // Embeddable
        fattura.setDatiBollo(dto.getDatiBollo());
        fattura.setDatiCassaPrevidenziale(dto.getDatiCassaPrevidenziale());
        fattura.setDatiPagamento(dto.getDatiPagamento());

        // Documenti di riferimento
        fattura.setDatiOrdineAcquisto(dto.getDatiOrdineAcquisto());
        fattura.setDatiContratto(dto.getDatiContratto());
        fattura.setDatiConvenzione(dto.getDatiConvenzione());
        fattura.setDatiRicezione(dto.getDatiRicezione());
        fattura.setDatiFattureCollegate(dto.getDatiFattureCollegate());

        // 7) Salva la fattura
        fatturaRepository.save(fattura);

        // Segna l'ordine come fatturato
        incarico.setFatturato(true);
        ordineRepository.save(incarico);

        // Traccia operazione
        Operazione operazione = new Operazione();
        operazione.setUser(currentUser);
        operazione.setEntita("Fattura");
        operazione.setTipo("Aggiunta");
        operazione.setDescrizione("Nuova fattura creata: " + fattura.getNumeroFattura() + " per l'ordine " + incarico.getId());
        operazione.setDataOperazione(LocalDateTime.now());
        operazioneRepository.save(operazione);

        // 8) Inserisci movimento in PrimaNota (opzionale)
        if (inserisciMovimento) {
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
