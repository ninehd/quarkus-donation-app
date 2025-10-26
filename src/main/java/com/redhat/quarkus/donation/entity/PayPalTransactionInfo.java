package com.redhat.quarkus.donation.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import java.time.LocalDateTime;

@Embeddable
public class PayPalTransactionInfo {

    @Column(name = "paypal_order_id")
    private String orderId;

    @Column(name = "paypal_transaction_id")
    private String transactionId;

    @Column(name = "paypal_email")
    private String email;

    @Column(name = "paypal_payer_id")
    private String payerId;

    @Column(name = "paypal_status")
    private String status;

    @Column(name = "paypal_response_data", columnDefinition = "TEXT")
    private String responseData;

    @Column(name = "paypal_error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "paypal_created_at")
    private LocalDateTime createdAt;

    @Column(name = "paypal_updated_at")
    private LocalDateTime updatedAt;

    public PayPalTransactionInfo() {
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPayerId() {
        return payerId;
    }

    public void setPayerId(String payerId) {
        this.payerId = payerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
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
        return "PayPalTransactionInfo{" +
                "orderId='" + orderId + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}