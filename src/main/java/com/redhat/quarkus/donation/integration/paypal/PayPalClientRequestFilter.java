package com.redhat.quarkus.donation.integration.paypal;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.ConfigProvider;
import java.util.Base64;

@Provider
public class PayPalClientRequestFilter implements ClientRequestFilter {

    @Override
    public void filter(ClientRequestContext requestContext) {
        String clientId = ConfigProvider.getConfig()
            .getValue("paypal.client.id", String.class);
        String clientSecret = ConfigProvider.getConfig()
            .getValue("paypal.client.secret", String.class);

        String credentials = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder()
            .encodeToString(credentials.getBytes());

        requestContext.getHeaders()
            .add("Authorization", "Basic " + encodedAuth);
    }
}