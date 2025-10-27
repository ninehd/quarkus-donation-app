package com.redhat.quarkus.donation.integration.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Payer {
    @JsonProperty("email_address")
    public String emailAddress;

    public Payer() {}
}