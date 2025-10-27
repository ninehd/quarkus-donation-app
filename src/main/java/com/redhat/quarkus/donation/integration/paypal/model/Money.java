package com.redhat.quarkus.donation.integration.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Money {
    @JsonProperty("currency_code")
    public String currencyCode;

    @JsonProperty("value")
    public String value;

    public Money() {}
}