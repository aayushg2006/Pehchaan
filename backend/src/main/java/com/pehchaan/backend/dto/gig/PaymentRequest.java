package com.pehchaan.backend.dto.gig;

import com.pehchaan.backend.entity.Gig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
    private Gig.PaymentMethod paymentMethod; // "CASH" or "ONLINE"
}