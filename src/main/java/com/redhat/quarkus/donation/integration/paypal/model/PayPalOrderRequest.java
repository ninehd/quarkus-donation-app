package com.redhat.quarkus.donation.integration.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PayPalOrderRequest {

    @JsonProperty("intent")
    private String intent = "CAPTURE";

    @JsonProperty("purchase_units")
    private PurchaseUnit[] purchaseUnits;

    @JsonProperty("payer")
    private Payer payer;

    @JsonProperty("application_context")
    private ApplicationContext applicationContext;

    public PayPalOrderRequest() {}

    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }

    public PurchaseUnit[] getPurchaseUnits() { return purchaseUnits; }
    public void setPurchaseUnits(PurchaseUnit[] purchaseUnits) { this.purchaseUnits = purchaseUnits; }

    public Payer getPayer() { return payer; }
    public void setPayer(Payer payer) { this.payer = payer; }

    public ApplicationContext getApplicationContext() { return applicationContext; }
    public void setApplicationContext(ApplicationContext applicationContext) { this.applicationContext = applicationContext; }
}
