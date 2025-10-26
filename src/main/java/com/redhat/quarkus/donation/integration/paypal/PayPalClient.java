package com.redhat.quarkus.donation.integration.paypal;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.Map;

@RegisterRestClient(configKey = "paypal")
@Path("/v2/checkout/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface PayPalClient {

    @POST
    PayPalOrderResponse createOrder(PayPalOrderRequest request);

    @POST
    @Path("/{id}/capture")
    PayPalCaptureResponse captureOrder(@PathParam("id") String orderId, Map<String, Object> body);

    @GET
    @Path("/{id}")
    PayPalOrderResponse getOrder(@PathParam("id") String orderId);
}