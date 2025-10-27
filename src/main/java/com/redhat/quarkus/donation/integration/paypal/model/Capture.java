package com.redhat.quarkus.donation.integration.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Capture {
    @JsonProperty("id")
    public String id;

    @JsonProperty("status")
    public String status;

    public String getId() { return id; }
    public String getStatus() { return status; }
}
