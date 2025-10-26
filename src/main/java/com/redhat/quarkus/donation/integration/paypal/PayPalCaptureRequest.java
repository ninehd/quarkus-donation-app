package com.redhat.quarkus.donation.integration.paypal;

public class PayPalCaptureRequest {
    private Long donationId;
    private String paypalOrderId;

    public PayPalCaptureRequest() {}

    public Long getDonationId() { return donationId; }
    public void setDonationId(Long donationId) { this.donationId = donationId; }

    public String getPaypalOrderId() { return paypalOrderId; }
    public void setPaypalOrderId(String paypalOrderId) { this.paypalOrderId = paypalOrderId; }
}