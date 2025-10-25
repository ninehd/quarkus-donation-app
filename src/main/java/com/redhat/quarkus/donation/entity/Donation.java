package com.redhat.quarkus.donation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "donations")
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "donor_name", nullable = false)
    private String donorName;

    @Column(name = "donor_email", nullable = false)
    private String donorEmail;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Embedded
    private PayPalTransactionInfo paypalInfo = new PayPalTransactionInfo();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Donation() {
    }

    public Donation(String donorName, String donorEmail, BigDecimal amount, String message) {
        this.donorName = donorName;
        this.donorEmail = donorEmail;
        this.amount = amount;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDonorName() {
        return donorName;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public String getDonorEmail() {
        return donorEmail;
    }

    public void setDonorEmail(String donorEmail) {
        this.donorEmail = donorEmail;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public PayPalTransactionInfo getPaypalInfo() {
        return paypalInfo;
    }

    public void setPaypalInfo(PayPalTransactionInfo paypalInfo) {
        this.paypalInfo = paypalInfo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Donation{" +
                "id=" + id +
                ", donorName='" + donorName + '\'' +
                ", donorEmail='" + donorEmail + '\'' +
                ", amount=" + amount +
                ", paypalInfo=" + paypalInfo +
                ", createdAt=" + createdAt +
                '}';
    }
}