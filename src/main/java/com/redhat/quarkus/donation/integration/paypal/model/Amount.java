package com.redhat.quarkus.donation.integration.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Amount {
    @JsonProperty("currency_code")
    public String currencyCode;

    @JsonProperty("value")
    public String value;

    @JsonProperty("breakdown")
    public Breakdown breakdown;

    public Amount() {}
}