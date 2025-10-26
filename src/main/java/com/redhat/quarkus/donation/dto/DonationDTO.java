package com.redhat.quarkus.donation.dto;

import com.redhat.quarkus.donation.entity.Donation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class DonationDTO {

    private UUID uuid;

    private String donorName;

    private String donorEmail;

    private BigDecimal amount;

    private String message;

    // PayPal fields
    private String paypalOrderId;
    private String paypalTransactionId;
    private String paypalEmail;
    private BigDecimal paypalAmount;
    private String paypalCurrency;
    private String paypalStatus;
    private String paypalErrorMessage;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DonationDTO() {
    }

    public DonationDTO(String donorName, String donorEmail, BigDecimal amount, String message) {
        this.donorName = donorName;
        this.donorEmail = donorEmail;
        this.amount = amount;
        this.message = message;
    }

    public static DonationDTO fromEntity(Donation donation) {
        DonationDTO dto = new DonationDTO();
        dto.uuid = donation.getUuid();
        dto.donorName = donation.getDonorName();
        dto.donorEmail = donation.getDonorEmail();
        dto.amount = donation.getAmount();
        dto.message = donation.getMessage();

        dto.paypalOrderId = donation.getPaypalInfo().getOrderId();
        dto.paypalTransactionId = donation.getPaypalInfo().getCaptureId();
        dto.paypalEmail = donation.getPaypalInfo().getEmail();
        dto.paypalStatus = donation.getPaypalInfo().getStatus();
        dto.paypalErrorMessage = donation.getPaypalInfo().getErrorMessage();

        dto.createdAt = donation.getCreatedAt();
        dto.updatedAt = donation.getUpdatedAt();
        return dto;
    }

    // Factory method: convert DTO to Donation entity
    public Donation toEntity() {
        Donation donation = new Donation(donorName, donorEmail, amount, message);
        return donation;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
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

    public String getPaypalOrderId() {
        return paypalOrderId;
    }

    public void setPaypalOrderId(String paypalOrderId) {
        this.paypalOrderId = paypalOrderId;
    }

    public String getPaypalTransactionId() {
        return paypalTransactionId;
    }

    public void setPaypalTransactionId(String paypalTransactionId) {
        this.paypalTransactionId = paypalTransactionId;
    }

    public String getPaypalEmail() {
        return paypalEmail;
    }

    public void setPaypalEmail(String paypalEmail) {
        this.paypalEmail = paypalEmail;
    }

    public BigDecimal getPaypalAmount() {
        return paypalAmount;
    }

    public void setPaypalAmount(BigDecimal paypalAmount) {
        this.paypalAmount = paypalAmount;
    }

    public String getPaypalCurrency() {
        return paypalCurrency;
    }

    public void setPaypalCurrency(String paypalCurrency) {
        this.paypalCurrency = paypalCurrency;
    }

    public String getPaypalStatus() {
        return paypalStatus;
    }

    public void setPaypalStatus(String paypalStatus) {
        this.paypalStatus = paypalStatus;
    }

    public String getPaypalErrorMessage() {
        return paypalErrorMessage;
    }

    public void setPaypalErrorMessage(String paypalErrorMessage) {
        this.paypalErrorMessage = paypalErrorMessage;
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
        return "DonationDTO{" +
                "uuid=" + uuid +
                ", donorName='" + donorName + '\'' +
                ", donorEmail='" + donorEmail + '\'' +
                ", amount=" + amount +
                ", paypalStatus='" + paypalStatus + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}