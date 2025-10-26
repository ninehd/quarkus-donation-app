package com.redhat.quarkus.donation.dto;

import java.math.BigDecimal;

public class StatsDTO {
    private long donationCount;
    private BigDecimal totalAmount;

    public StatsDTO(long donationCount, BigDecimal totalAmount) {
        this.donationCount = donationCount;
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
    }

    public long getDonationCount() { return donationCount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
}