package com.carrot.Carrot.repository;

import com.carrot.Carrot.enumerator.TipoMovimento;
import com.carrot.Carrot.model.PrimaNota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface PrimaNotaRepository extends JpaRepository<PrimaNota, Long> {
    // ✅ Ottenere tutte le operazioni di un utente
    List<PrimaNota> findByUserId(Long userId);

    // ✅ Filtrare per tipo di movimento (ENTRATA o USCITA)
    List<PrimaNota> findByUserIdAndTipoMovimento(Long userId, TipoMovimento tipoMovimento);

    // ✅ Ottenere operazioni in un determinato periodo
    List<PrimaNota> findByUserIdAndDataOperazioneBetween(Long userId, LocalDate startDate, LocalDate endDate);

    // ✅ Ottenere solo le entrate o le uscite in un periodo specifico
    List<PrimaNota> findByUserIdAndTipoMovimentoAndDataOperazioneBetween(Long userId, TipoMovimento tipoMovimento, LocalDate startDate, LocalDate endDate);

    // ✅ Ottenere il totale delle entrate o uscite in un periodo specifico
    BigDecimal sumImportoByUserIdAndTipoMovimentoAndDataOperazioneBetween(Long userId, TipoMovimento tipoMovimento, LocalDate startDate, LocalDate endDate);
}
