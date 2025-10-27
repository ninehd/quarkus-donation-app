package com.redhat.quarkus.donation.integration.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Payments {
    @JsonProperty("captures")
    public List<Capture> captures;

    public List<Capture> getCaptures() { return captures; }
}
