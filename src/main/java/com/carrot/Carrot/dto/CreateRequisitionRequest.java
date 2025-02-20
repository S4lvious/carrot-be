package com.carrot.Carrot.dto;
import lombok.Data;

@Data
public class CreateRequisitionRequest {
    private String institutionId;
    private String redirectUrl;
    private String reference;
    private String userLanguage;
    // se vuoi passare agreementId, maxHistoricalDays, ecc. puoi aggiungerli qui
}
