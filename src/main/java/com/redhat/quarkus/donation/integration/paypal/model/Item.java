package com.redhat.quarkus.donation.integration.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Item {
    @JsonProperty("name")
    public String name;

    @JsonProperty("description")
    public String description;

    @JsonProperty("quantity")
    public String quantity;

    @JsonProperty("unit_amount")
    public Money unitAmount;

    public Item() {}
}