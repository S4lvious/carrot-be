package com.carrot.Carrot.dto;

import lombok.Data;

@Data
public class AccountDetailsResponse {
    private AccountDTO account;

    @Data
    public static class AccountDTO {
        private String resourceId;
        private String iban;
        private String currency;
        private String ownerName;
        private String product;
    }
}
