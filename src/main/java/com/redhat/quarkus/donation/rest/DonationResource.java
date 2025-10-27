package com.redhat.quarkus.donation.rest;

import com.redhat.quarkus.donation.dto.DonationDTO;
import com.redhat.quarkus.donation.dto.StatsDTO;
import com.redhat.quarkus.donation.entity.Donation;
import com.redhat.quarkus.donation.integration.paypal.model.PayPalOrderResponse;
import com.redhat.quarkus.donation.integration.paypal.PayPalOrderStatus;
import com.redhat.quarkus.donation.mapper.DonationResponseMapper;
import com.redhat.quarkus.donation.service.DonationService;
import com.redhat.quarkus.donation.service.PayPalService;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
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
    DonationResponseMapper donationResponseMapper;

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
    @Blocking
    public StatsDTO getStats() {
        long totalDonations = donationService.countCompletedDonations();
        BigDecimal totalAmount = donationService.getTotalCompletedAmount();
        return new StatsDTO(totalDonations, totalAmount);
    }

    /**
     * POST /api/donations/paypal/initiate
     */
    @POST
    @Path("/api/donations/paypal/initiate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Blocking
    @Transactional
    public Response initiateDonation(DonationDTO donationDTO) {
        log.infof("Initiating donation for %s with amount %s", donationDTO.getDonorEmail(), donationDTO.getAmount());

        Donation donation = donationService.createDonation(donationDTO.toEntity());

        paypalService.initiateDonation(donation);

        // Redirect to PayPal
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("approvalUrl", donation.getPaypalInfo().getApproveLink());

        return Response.ok(responseBody).build();
    }

    /**
     * GET /donations/return
     * Unified return endpoint from PayPal (both success and cancel)
     * Verifies the actual payment status with PayPal API
     */
    @GET
    @Path("/api/donations/paypal/return")
    @Produces(MediaType.TEXT_HTML)
    @Blocking
    @Transactional
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

            // Check if already captured in database
            if (PayPalOrderStatus.isCompleted(donation.getPaypalInfo().getStatus())) {
                log.infof("Donation %s already completed in database, displaying success page", donation.getUuid());
                return Response.ok(donationResult.data(donationResponseMapper.toResult(donation, true)).render()).build();
            }

            // Verify status directly with PayPal API
            PayPalOrderResponse orderDetails = paypalService.getOrderDetails(paypalOrderId);
            String paypalStatusString = orderDetails.getStatus();
            PayPalOrderStatus paypalStatus = PayPalOrderStatus.fromString(paypalStatusString);


            // Check if payment can be captured
            if (paypalStatus != null && paypalStatus.canBeCaptured()) {
                // Payment approved, capture it now
                Donation captured = paypalService.captureDonation(donation, paypalOrderId);
                return Response.ok(donationResult.data(donationResponseMapper.toResult(captured, true)).render()).build();
            } else {
                // Payment was not approved (cancelled or other status)
                donationService.failDonation(donation, "Payment not approved. Status: " + paypalStatusString);
                return Response.ok(donationResult.data(donationResponseMapper.toResult(donation, false)).render()).build();
            }

        } catch (Exception e) {
            log.errorf("Error processing PayPal return: %s", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("<h1>Error</h1><p>An error occurred processing your donation.</p>")
                    .build();
        }
    }
}