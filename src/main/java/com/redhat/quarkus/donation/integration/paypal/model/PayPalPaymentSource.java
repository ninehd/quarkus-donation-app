package com.redhat.quarkus.donation.integration.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PayPalPaymentSource {
    @JsonProperty("email_address")
    public String emailAddress;

    @JsonProperty("account_id")
    public String accountId;

    @JsonProperty("name")
    public Name name;

    public String getEmailAddress() { return emailAddress; }
    public String getAccountId() { return accountId; }
    public Name getName() { return name; }
}
