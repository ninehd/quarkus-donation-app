package com.redhat.quarkus.donation.integration.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ApplicationContext {
    @JsonProperty("return_url")
    public String returnUrl;

    @JsonProperty("cancel_url")
    public String cancelUrl;

    public ApplicationContext() {}
}