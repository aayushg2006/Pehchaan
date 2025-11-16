package com.pehchaan.backend.dto.gig;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceRequest {
    // This amount is FOR ADD-ONS (parts, extra labor)
    // The visiting charge is separate.
    private BigDecimal additionalAmount;
}