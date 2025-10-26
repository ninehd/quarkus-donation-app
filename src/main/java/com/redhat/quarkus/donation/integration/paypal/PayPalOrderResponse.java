package com.redhat.quarkus.donation.integration.paypal;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class PayPalOrderResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("status")
    private String status;

    @JsonProperty("links")
    private List<Link> links;

    @JsonProperty("payment_source")
    private PaymentSource paymentSource;

    @JsonProperty("purchase_units")
    private List<PurchaseUnit> purchaseUnits;

    public String getId() { return id; }
    public String getStatus() { return status; }
    public List<Link> getLinks() { return links; }
    public PaymentSource getPaymentSource() { return paymentSource; }
    public List<PurchaseUnit> getPurchaseUnits() { return purchaseUnits; }

    public static class Link {
        @JsonProperty("rel")
        public String rel;

        @JsonProperty("href")
        public String href;

        public String getApproveLink() {
            return "approve".equals(rel) ? href : null;
        }
    }

    public static class PaymentSource {
        @JsonProperty("paypal")
        public PayPalPaymentSource paypal;

        public PayPalPaymentSource getPaypal() { return paypal; }
    }

    public static class PayPalPaymentSource {
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

    public static class Name {
        @JsonProperty("given_name")
        public String givenName;

        @JsonProperty("surname")
        public String surname;

        public String getGivenName() { return givenName; }
        public String getSurname() { return surname; }
    }

    public static class PurchaseUnit {
        @JsonProperty("payments")
        public Payments payments;

        public Payments getPayments() { return payments; }
    }

    public static class Payments {
        @JsonProperty("captures")
        public List<Capture> captures;

        public List<Capture> getCaptures() { return captures; }
    }

    public static class Capture {
        @JsonProperty("id")
        public String id;

        @JsonProperty("status")
        public String status;

        public String getId() { return id; }
        public String getStatus() { return status; }
    }
}