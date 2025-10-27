package com.redhat.quarkus.donation.integration.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Name {
    @JsonProperty("given_name")
    public String givenName;

    @JsonProperty("surname")
    public String surname;

    public String getGivenName() { return givenName; }
    public String getSurname() { return surname; }
}
