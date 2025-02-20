package com.carrot.Carrot.dto;

import lombok.Data;

@Data
public class InstitutionDTO {
    private String id;
    private String name;
    private String bic;
    private String transaction_total_days;
    private String logo;
    // ... e così via
}
