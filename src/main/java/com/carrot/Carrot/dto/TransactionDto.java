package com.carrot.Carrot.dto;

import lombok.Data;

@Data
public class TransactionDto {
    private String transactionId;
    private String bookingDate;  // "2020-11-11" ...
    private String valueDate;
    private TransactionAmount transactionAmount;
    private String remittanceInformationUnstructured;
    // ...
}
