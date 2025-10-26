package com.redhat.quarkus.donation.service;

import com.redhat.quarkus.donation.entity.Donation;
import com.redhat.quarkus.donation.integration.paypal.PayPalClient;
import com.redhat.quarkus.donation.integration.paypal.PayPalOrderRequest;
import com.redhat.quarkus.donation.integration.paypal.PayPalOrderResponse;
import com.redhat.quarkus.donation.utils.JsonUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.util.Collections;

@ApplicationScoped
public class PayPalService {

    private static final Logger log = Logger.getLogger(PayPalService.class);

    @Inject
    @RestClient
    PayPalClient paypalClient;

    @Inject
    DonationService donationService;

    @ConfigProperty(name = "app.base-url")
    String baseUrl;

    /**
     * Initiate a donation by creating a PayPal order
     */
    public PayPalOrderResponse initiateDonation(Donation donation) {
        log.infof("Initiating PayPal payment for donation %d", donation.getId());

        try {
            String returnUrl = baseUrl + "/donations/paypal/return";
            String cancelUrl = baseUrl + "/donations/paypal/return";

            PayPalOrderRequest request = new PayPalOrderRequest(
                    donation.getAmount().toString(),
                    donation.getCurrency(),
                    donation.getDonorEmail(),
                    returnUrl,
                    cancelUrl,
                    "My wonderful contribution"
            );

            PayPalOrderResponse response = paypalClient.createOrder(request);

            if (response != null && response.getId() != null) {
                donationService.updatePaypalOrderId(donation, response.getId());
                return response;
            } else {
                throw new RuntimeException("Invalid PayPal response: no order ID");
            }

        } catch (Exception e) {
            log.errorf("Error initiating donation payment: %s", e.getMessage());
            donationService.failDonation(donation, "Failed to create PayPal order: " + e.getMessage());
            throw new RuntimeException("Failed to initiate payment", e);
        }
    }

    /**
     * Capture a donation after user approves on PayPal
     */
    public Donation  captureDonation(Donation donation, String paypalOrderId) {
        log.infof("Capturing PayPal payment for donation %d with order %s", donation.getId(), paypalOrderId);

        try {
            PayPalOrderResponse captureResponse = paypalClient.captureOrder(paypalOrderId, Collections.emptyMap());

            if (captureResponse != null && captureResponse.getId() != null) {
                String paypalEmail = null;
                String payerId = null;

                if (captureResponse.getPaymentSource() != null
                    && captureResponse.getPaymentSource().getPaypal() != null) {
                    PayPalOrderResponse.PayPalPaymentSource paypalSource = captureResponse.getPaymentSource().getPaypal();
                    paypalEmail = paypalSource.getEmailAddress();
                    payerId = paypalSource.getAccountId();
                }

                if (paypalEmail == null || paypalEmail.isEmpty()) {
                    paypalEmail = donation.getDonorEmail();
                }

                Donation captured = donationService.captureDonation(
                        donation,
                        captureResponse.getId(),
                        paypalEmail,
                        payerId,
                        JsonUtils.toJson(captureResponse)
                );

                log.infof("Donation captured successfully. Transaction ID: %s, PayPal Email: %s, Payer ID: %s",
                         captureResponse.getId(), paypalEmail, payerId);
                return captured;

            } else {
                throw new RuntimeException("Invalid capture response from PayPal");
            }

        } catch (Exception e) {
            log.errorf("Error capturing donation payment: %s", e.getMessage());
            donationService.failDonation(donation, "Failed to capture PayPal payment: " + e.getMessage());
            throw new RuntimeException("Failed to capture payment", e);
        }
    }

    /**
     * Get PayPal order details
     */
    public PayPalOrderResponse getOrderDetails(String paypalOrderId) {
        log.infof("Fetching PayPal order details for order %s", paypalOrderId);

        try {
            PayPalOrderResponse orderDetails = paypalClient.getOrder(paypalOrderId);

            if (orderDetails != null) {
                log.infof("PayPal order status: %s", orderDetails.getStatus());
                return orderDetails;
            } else {
                throw new RuntimeException("Could not fetch order details from PayPal");
            }

        } catch (Exception e) {
            log.errorf("Error fetching order details: %s", e.getMessage());
            throw new RuntimeException("Failed to fetch order details", e);
        }
    }
}