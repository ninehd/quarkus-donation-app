package com.redhat.quarkus.donation.service;

import com.redhat.quarkus.donation.entity.Donation;
import com.redhat.quarkus.donation.integration.paypal.PayPalClient;
import com.redhat.quarkus.donation.integration.paypal.PayPalCaptureResponse;
import com.redhat.quarkus.donation.integration.paypal.PayPalOrderRequest;
import com.redhat.quarkus.donation.integration.paypal.PayPalOrderResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
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
    @Transactional
    public PayPalOrderResponse initiateDonation(Long donationId) {
        log.infof("Initiating PayPal payment for donation %d", donationId);

        Donation donation = donationService.getDonationById(donationId)
                .orElseThrow(() -> new RuntimeException("Donation not found: " + donationId));

        try {
            String returnUrl = baseUrl + "/donations/paypal/return";
            String cancelUrl = baseUrl + "/donations/paypal/return";

            PayPalOrderRequest request = new PayPalOrderRequest(
                    donation.getAmount().toString(),
                    "USD",
                    donation.getDonorEmail(),
                    returnUrl,
                    cancelUrl
            );

            PayPalOrderResponse response = paypalClient.createOrder(request);

            if (response != null && response.getId() != null) {
                donationService.updatePaypalOrderId(donationId, response.getId());
                log.infof("PayPal order created successfully. Order ID: %s", response.getId());
                return response;
            } else {
                throw new RuntimeException("Invalid PayPal response: no order ID");
            }

        } catch (Exception e) {
            log.errorf("Error initiating donation payment: %s", e.getMessage());
            donationService.failDonation(donationId, "Failed to create PayPal order: " + e.getMessage());
            throw new RuntimeException("Failed to initiate payment", e);
        }
    }

    /**
     * Capture a donation after user approves on PayPal
     */
    @Transactional
    public Donation captureDonation(Long donationId, String paypalOrderId) {
        log.infof("Capturing PayPal payment for donation %d with order %s", donationId, paypalOrderId);

        Donation donation = donationService.getDonationById(donationId)
                .orElseThrow(() -> new RuntimeException("Donation not found: " + donationId));

        try {
            PayPalCaptureResponse captureResponse = paypalClient.captureOrder(paypalOrderId, Collections.emptyMap());

            if (captureResponse != null && captureResponse.getId() != null) {
                Donation captured = donationService.captureDonation(
                        donationId,
                        captureResponse.getId(),
                        donation.getDonorEmail(),
                        paypalOrderId
                );

                log.infof("Donation captured successfully. Transaction ID: %s", captureResponse.getId());
                return captured;

            } else {
                throw new RuntimeException("Invalid capture response from PayPal");
            }

        } catch (Exception e) {
            log.errorf("Error capturing donation payment: %s", e.getMessage());
            donationService.failDonation(donationId, "Failed to capture PayPal payment: " + e.getMessage());
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