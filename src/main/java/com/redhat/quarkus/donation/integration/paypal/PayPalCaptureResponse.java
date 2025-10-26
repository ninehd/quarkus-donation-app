package com.redhat.quarkus.donation.integration.paypal;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class PayPalCaptureResponse {

    @JsonProperty("id")
    private String id;

    @JsonProperty("status")
    private String status;

    @JsonProperty("purchase_units")
    private List<PurchaseUnit> purchaseUnits;

    public String getId() { return id; }
    public String getStatus() { return status; }
    public List<PurchaseUnit> getPurchaseUnits() { return purchaseUnits; }

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