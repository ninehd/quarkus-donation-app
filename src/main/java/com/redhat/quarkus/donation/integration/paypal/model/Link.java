package com.redhat.quarkus.donation.integration.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Link {
    @JsonProperty("rel")
    public String rel;

    @JsonProperty("href")
    public String href;

    public String getApproveLink() {
        return "approve".equals(rel) ? href : null;
    }
}
