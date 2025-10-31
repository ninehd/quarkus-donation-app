package com.redhat.quarkus.donation.integration.paypal.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Link {
    @JsonProperty("rel")
    public String rel;

    @JsonProperty("href")
    public String href;

    public String getRel() { return rel; }
    public String getHref() { return href; }

    public void setRel(String rel) { this.rel = rel; }
    public void setHref(String href) { this.href = href; }

    public String getApproveLink() {
        return "approve".equals(rel) ? href : null;
    }
}
