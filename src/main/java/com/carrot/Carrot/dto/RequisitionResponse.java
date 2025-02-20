package com.carrot.Carrot.dto;

import lombok.Data;

@Data
public class RequisitionResponse {
    private String id;
    private String redirect;
    private String status;
    private String link;
    private String agreement;

    @Data
    public static class RequisitionStatus {
        private String shortName;
        private String description;
    }
}
