package com.redhat.quarkus.donation.rest;

import com.redhat.quarkus.donation.dto.DonationDTO;
import com.redhat.quarkus.donation.dto.StatsDTO;
import com.redhat.quarkus.donation.entity.Donation;
import com.redhat.quarkus.donation.integration.paypal.PayPalCaptureRequest;
import com.redhat.quarkus.donation.integration.paypal.PayPalOrderResponse;
import com.redhat.quarkus.donation.service.DonationService;
import com.redhat.quarkus.donation.service.PayPalService;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Path("/")
public class DonationResource {

    private static final Logger log = Logger.getLogger(DonationResource.class);

    @Inject
    DonationService donationService;

    @Inject
    PayPalService paypalService;

    @Inject
    @Location("donation-form.html")
    Template donationForm;

    @Inject
    @Location("donation-result.html")
    Template donationResult;

    /**
     * GET /
     * Show donation form
     */
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String showForm() {
        return donationForm.instance().render();
    }

    /**
     * GET /api/donations/stats
     */
    @GET
    @Path("/api/donations/stats")
    @Produces(MediaType.APPLICATION_JSON)
    public StatsDTO getStats() {
        long totalDonations = donationService.countDonations();
        BigDecimal totalAmount = donationService.getTotalAmount();
        return new StatsDTO(totalDonations, totalAmount);
    }

    /**
     * POST /api/donations/paypal/initiate
     */
    @POST
    @Path("/api/donations/paypal/initiate")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response initiateDonation(DonationDTO donationDTO) {
        log.infof("Initiating donation for %s with amount %s", donationDTO.getDonorEmail(), donationDTO.getAmount());

        Donation donation = donationDTO.toEntity();
        Donation savedDonation = donationService.createDonation(donation);

        PayPalOrderResponse paypalResponse = paypalService.initiateDonation(savedDonation.getId());

        String approvalLink = paypalResponse.getLinks()
                .stream()
                .map(link -> link.getApproveLink())
                .filter(link -> link != null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Could not retrieve PayPal approval URL"));

        // Return Response with JSON
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("approvalUrl", approvalLink);
        responseBody.put("donationId", savedDonation.getId());

        return Response.ok(responseBody).build();
    }

    /**
     * GET /donations/return
     * Unified return endpoint from PayPal (both success and cancel)
     * Verifies the actual payment status with PayPal API
     */
    @GET
    @Path("/donations/paypal/return")
    @Produces(MediaType.TEXT_HTML)
    public Response handlePayPalReturn(
            @QueryParam("token") String paypalOrderId,
            @QueryParam("PayerID") String payerId) {

        log.infof("PayPal return with token=%s, PayerID=%s", paypalOrderId, payerId);

        try {
            // Find donation by PayPal order ID
            Donation donation = donationService.findByPaypalOrderId(paypalOrderId);
            if (donation == null) {
                log.errorf("No donation found for PayPal order ID: %s", paypalOrderId);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("<h1>Donation not found</h1>")
                        .build();
            }

            // Verify status directly with PayPal API
            PayPalOrderResponse orderDetails = paypalService.getOrderDetails(paypalOrderId);
            String paypalStatus = orderDetails.getStatus();

            log.infof("PayPal order status for donation %d: %s", donation.getId(), paypalStatus);

            // If approved, capture the payment
            if ("APPROVED".equals(paypalStatus)) {
                Donation captured = paypalService.captureDonation(donation.getId(), paypalOrderId);

                // Render success page
                return Response.ok(donationResult.data(
                        "success", true,
                        "donationId", captured.getId(),
                        "amount", captured.getAmount(),
                        "email", captured.getDonorEmail(),
                        "name", captured.getDonorName()
                ).render()).build();
            } else {
                // Payment was not approved (cancelled or other status)
                donationService.failDonation(donation.getId(), "Payment not approved. Status: " + paypalStatus);

                // Render cancelled page
                return Response.ok(donationResult.data(
                        "success", false,
                        "reason", "Payment status: " + paypalStatus
                ).render()).build();
            }

        } catch (Exception e) {
            log.errorf("Error processing PayPal return: %s", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("<h1>Error</h1><p>An error occurred processing your donation.</p>")
                    .build();
        }
    }

    /**
     * POST /api/donations/paypal/capture
     */
    @POST
    @Path("/api/donations/paypal/capture")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public DonationDTO captureDonation(PayPalCaptureRequest captureRequest) {
        log.infof("Capturing PayPal payment for donation %d", captureRequest.getDonationId());

        Donation captured = paypalService.captureDonation(
                captureRequest.getDonationId(),
                captureRequest.getPaypalOrderId()
        );

        return DonationDTO.fromEntity(captured);
    }
}