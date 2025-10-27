package com.redhat.quarkus.donation.integration.paypal;

import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.util.Base64;

@Provider
public class PayPalClientRequestFilter implements ClientRequestFilter {

    private final String encodedAuth;

    @Inject
    public PayPalClientRequestFilter(
            @ConfigProperty(name = "paypal.client.id") String clientId,
            @ConfigProperty(name = "paypal.client.secret") String clientSecret) {
        String credentials = clientId + ":" + clientSecret;
        this.encodedAuth = Base64.getEncoder()
            .encodeToString(credentials.getBytes());
    }

    @Override
    public void filter(ClientRequestContext requestContext) {
        requestContext.getHeaders()
            .add("Authorization", "Basic " + encodedAuth);
    }
}