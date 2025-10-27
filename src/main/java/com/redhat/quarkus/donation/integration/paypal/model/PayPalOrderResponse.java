package com.redhat.quarkus.donation.integration.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class PayPalOrderResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("status")
    private String status;

    @JsonProperty("links")
    private List<Link> links;

    @JsonProperty("payment_source")
    private PaymentSource paymentSource;

    @JsonProperty("purchase_units")
    private List<PurchaseUnit> purchaseUnits;

    public String getId() { return id; }
    public String getStatus() { return status; }
    public List<Link> getLinks() { return links; }
    public PaymentSource getPaymentSource() { return paymentSource; }
    public List<PurchaseUnit> getPurchaseUnits() { return purchaseUnits; }
}
