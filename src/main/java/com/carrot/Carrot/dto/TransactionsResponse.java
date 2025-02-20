package com.carrot.Carrot.dto;

import lombok.Data;
import java.util.List;

@Data
public class TransactionsResponse {
    private TransactionsObject transactions;

    @Data
    public static class TransactionsObject {
        private List<TransactionDto> booked;
        private List<TransactionDto> pending;
    }
}

