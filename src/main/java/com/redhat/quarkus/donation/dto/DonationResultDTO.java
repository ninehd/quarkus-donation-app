package com.redhat.quarkus.donation.dto;

import java.math.BigDecimal;

public record DonationResultDTO(
        boolean success,
        String orderId,
        BigDecimal amount,
        String maskedEmail,
        String donorEmail,
        String reason
) {
}