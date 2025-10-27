package com.redhat.quarkus.donation.integration.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Breakdown {
    @JsonProperty("item_total")
    public Money itemTotal;

    public Breakdown() {}
}