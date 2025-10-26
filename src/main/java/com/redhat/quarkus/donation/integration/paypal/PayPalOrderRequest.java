package com.redhat.quarkus.donation.integration.paypal;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PayPalOrderRequest {

    @JsonProperty("intent")
    private String intent = "CAPTURE";

    @JsonProperty("purchase_units")
    private PurchaseUnit[] purchaseUnits;

    @JsonProperty("payer")
    private Payer payer;

    public PayPalOrderRequest() {}

    public PayPalOrderRequest(String amount, String currency, String email) {
        this.payer = new Payer(email);
        this.purchaseUnits = new PurchaseUnit[]{
            new PurchaseUnit(amount, currency)
        };
    }

    // Getters
    public String getIntent() { return intent; }
    public PurchaseUnit[] getPurchaseUnits() { return purchaseUnits; }
    public Payer getPayer() { return payer; }

    // Inner classes
    public static class PurchaseUnit {
        @JsonProperty("amount")
        public Amount amount;

        public PurchaseUnit() {}
        public PurchaseUnit(String value, String currency) {
            this.amount = new Amount(value, currency);
        }
    }

    public static class Amount {
        @JsonProperty("currency_code")
        public String currencyCode;

        @JsonProperty("value")
        public String value;

        public Amount() {}
        public Amount(String value, String currencyCode) {
            this.value = value;
            this.currencyCode = currencyCode;
        }
    }

    public static class Payer {
        @JsonProperty("email_address")
        public String emailAddress;

        public Payer() {}
        public Payer(String emailAddress) {
            this.emailAddress = emailAddress;
        }
    }
}