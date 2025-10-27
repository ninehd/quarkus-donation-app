package com.redhat.quarkus.donation.integration.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PurchaseUnit {
    @JsonProperty("amount")
    public Amount amount;

    @JsonProperty("description")
    public String description;

    @JsonProperty("items")
    public Item[] items;

    @JsonProperty("payments")
    public Payments payments;

    public PurchaseUnit() {}

    public Payments getPayments() { return payments; }
}