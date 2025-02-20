package com.carrot.Carrot.dto;

import lombok.Data;
import java.util.List;

@Data
public class RequisitionDetails {
    private String id;
    private String status;
    private String agreement;
    private List<String> accounts;
    private String reference;
    // ...
}
