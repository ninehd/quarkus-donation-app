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

    public String getId() { return id; }
    public String getStatus() { return status; }
    public List<Link> getLinks() { return links; }

    public static class Link {
        @JsonProperty("rel")
        public String rel;

        @JsonProperty("href")
        public String href;

        public String getApproveLink() {
            return "approve".equals(rel) ? href : null;
        }
    }
}