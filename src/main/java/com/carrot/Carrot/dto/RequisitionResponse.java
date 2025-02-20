package com.carrot.Carrot.dto;

import lombok.Data;

@Data
public class RequisitionResponse {
    private String id;
    private String redirect;
    private RequisitionStatus status;
    private String link;
    private String agreement;
    // ... e così via

    @Data
    public static class RequisitionStatus {
        private String shortName;
        private String description;
    }
}
