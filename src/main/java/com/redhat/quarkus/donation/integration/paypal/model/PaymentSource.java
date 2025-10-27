package com.redhat.quarkus.donation.integration.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentSource {
    @JsonProperty("paypal")
    public PayPalPaymentSource paypal;

    public PayPalPaymentSource getPaypal() { return paypal; }
}
