package com.redhat.quarkus.donation.service;

import com.redhat.quarkus.donation.entity.Donation;
import com.redhat.quarkus.donation.integration.paypal.PayPalClient;
import com.redhat.quarkus.donation.integration.paypal.model.PayPalOrderRequest;
import com.redhat.quarkus.donation.integration.paypal.model.PayPalOrderResponse;
import com.redhat.quarkus.donation.integration.paypal.PayPalOrderStatus;
import com.redhat.quarkus.donation.mapper.PayPalOrderMapper;
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

    @Inject
    PayPalOrderMapper paypalMapper;

    @ConfigProperty(name = "app.base-url")
    String baseUrl;

    /**
     * Initiate a donation by creating a PayPal order
     */
    public void initiateDonation(Donation donation) {
        log.infof("Initiating PayPal payment for donation %s", donation.getUuid());

        try {

            PayPalOrderRequest request = paypalMapper.toOrderRequest(
                    donation,
                    baseUrl + "/api/donations/paypal/return"
            );

            PayPalOrderResponse response = paypalClient.createOrder(request);

            if (response != null && response.getId() != null) {
                // Use mapper to update donation with PayPal order response
                paypalMapper.updateDonationFromOrderResponse(donation, response);

                // Validate approve link was extracted
                if (donation.getPaypalInfo().getApproveLink() == null) {
                    throw new RuntimeException("Could not retrieve PayPal approval URL");
                }

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
    public Donation captureDonation(Donation donation, String paypalOrderId) {
        log.infof("Capturing PayPal payment for donation %s with order %s", donation.getUuid(), paypalOrderId);

        try {
            PayPalOrderResponse captureResponse = paypalClient.captureOrder(paypalOrderId, Collections.emptyMap());

            if (captureResponse != null && PayPalOrderStatus.isCompleted(captureResponse.getStatus())) {
                log.infof("PayPal payment status for donation %s: %s", donation.getUuid(), captureResponse.getStatus());
                paypalMapper.updateDonationFromCaptureResponse(donation, captureResponse);
                return donation;

            } else {
                throw new RuntimeException("Invalid capture response from PayPal for donation " + donation.getUuid());
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
                log.infof("PayPal order status for order %s: %s", paypalOrderId, orderDetails.getStatus());
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