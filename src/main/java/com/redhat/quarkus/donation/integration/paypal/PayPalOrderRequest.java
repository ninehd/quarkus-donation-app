package com.redhat.quarkus.donation.integration.paypal;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PayPalOrderRequest {

    @JsonProperty("intent")
    private String intent = "CAPTURE";

    @JsonProperty("purchase_units")
    private PurchaseUnit[] purchaseUnits;

    @JsonProperty("payer")
    private Payer payer;

    @JsonProperty("application_context")
    private ApplicationContext applicationContext;

    public PayPalOrderRequest() {}

    public PayPalOrderRequest(String amount, String currency, String email) {
        this.payer = new Payer(email);
        this.purchaseUnits = new PurchaseUnit[]{
            new PurchaseUnit(amount, currency)
        };
    }

    public PayPalOrderRequest(String amount, String currency, String email, String returnUrl, String cancelUrl) {
        this.payer = new Payer(email);
        this.purchaseUnits = new PurchaseUnit[]{
            new PurchaseUnit(amount, currency)
        };
        this.applicationContext = new ApplicationContext(returnUrl, cancelUrl);
    }

    public PayPalOrderRequest(String amount, String currency, String email, String returnUrl, String cancelUrl, String description) {
        this.payer = new Payer(email);
        this.purchaseUnits = new PurchaseUnit[]{
            new PurchaseUnit(amount, currency, description)
        };
        this.applicationContext = new ApplicationContext(returnUrl, cancelUrl);
    }

    public String getIntent() { return intent; }
    public PurchaseUnit[] getPurchaseUnits() { return purchaseUnits; }
    public Payer getPayer() { return payer; }

    public static class PurchaseUnit {
        @JsonProperty("amount")
        public Amount amount;

        @JsonProperty("description")
        public String description;

        @JsonProperty("items")
        public Item[] items;

        public PurchaseUnit() {}
        public PurchaseUnit(String value, String currency) {
            this.amount = new Amount(value, currency);
        }
        public PurchaseUnit(String value, String currency, String description) {
            this.amount = new Amount(value, currency, value);
            this.description = description;
            this.items = new Item[]{
                new Item(description, value, currency)
            };
        }
    }

    public static class Amount {
        @JsonProperty("currency_code")
        public String currencyCode;

        @JsonProperty("value")
        public String value;

        @JsonProperty("breakdown")
        public Breakdown breakdown;

        public Amount() {}
        public Amount(String value, String currencyCode) {
            this.value = value;
            this.currencyCode = currencyCode;
        }
        public Amount(String value, String currencyCode, String itemTotal) {
            this.value = value;
            this.currencyCode = currencyCode;
            this.breakdown = new Breakdown(itemTotal, currencyCode);
        }
    }

    public static class Breakdown {
        @JsonProperty("item_total")
        public Money itemTotal;

        public Breakdown() {}
        public Breakdown(String value, String currencyCode) {
            this.itemTotal = new Money(value, currencyCode);
        }
    }

    public static class Money {
        @JsonProperty("currency_code")
        public String currencyCode;

        @JsonProperty("value")
        public String value;

        public Money() {}
        public Money(String value, String currencyCode) {
            this.value = value;
            this.currencyCode = currencyCode;
        }
    }

    public static class Item {
        @JsonProperty("name")
        public String name;

        @JsonProperty("description")
        public String description;

        @JsonProperty("quantity")
        public String quantity;

        @JsonProperty("unit_amount")
        public Money unitAmount;

        public Item() {}
        public Item(String name, String value, String currencyCode) {
            this.name = name;
            this.description = name;
            this.quantity = "1";
            this.unitAmount = new Money(value, currencyCode);
        }
    }

    public static class Payer {
        @JsonProperty("email_address")
        public String emailAddress;

        public Payer() {}
        public Payer(String emailAddress) {
            this.emailAddress = emailAddress;
        }
    }

    public static class ApplicationContext {
        @JsonProperty("return_url")
        public String returnUrl;

        @JsonProperty("cancel_url")
        public String cancelUrl;

        public ApplicationContext() {}
        public ApplicationContext(String returnUrl, String cancelUrl) {
            this.returnUrl = returnUrl;
            this.cancelUrl = cancelUrl;
        }
    }
}