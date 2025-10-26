package com.redhat.quarkus.donation.dto;

import java.math.BigDecimal;

public record StatsDTO(long donationCount, BigDecimal totalAmount) {
    public StatsDTO(long donationCount, BigDecimal totalAmount) {
        this.donationCount = donationCount;
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
    }
}