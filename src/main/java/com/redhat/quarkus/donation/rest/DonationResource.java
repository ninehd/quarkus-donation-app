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
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

import java.math.BigDecimal;

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
    @Produces(MediaType.APPLICATION_JSON)
    public PayPalOrderResponse initiateDonation(DonationDTO donationDTO) {
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

        return paypalResponse;
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