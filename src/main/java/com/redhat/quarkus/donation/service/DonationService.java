package com.redhat.quarkus.donation.service;

import com.redhat.quarkus.donation.entity.Donation;
import com.redhat.quarkus.donation.repository.DonationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class DonationService {

    private static final Logger log = Logger.getLogger(DonationService.class);

    @Inject
    DonationRepository donationRepository;

    /**
     * Create a new donation
     */
    public Donation createDonation(Donation donation) {
        log.infof("Creating donation from %s for amount %s", donation.getDonorEmail(), donation.getAmount());
        donationRepository.persist(donation);
        return donation;
    }

    /**
     * Find donation by PayPal order ID
     */
    public Donation findByPaypalOrderId(String orderId) {
        return donationRepository.findByPaypalOrderId(orderId);
    }

    /**
     * Update donation with PayPal order ID
     */
    public void updatePaypalOrderId(Donation donation, String orderId) {
        log.infof("PayPal order created successfully for donation %s with PayPal order ID %s", donation.getUuid(), orderId);
        donation.getPaypalInfo().setOrderId(orderId);
        donation.getPaypalInfo().setCreatedAt(LocalDateTime.now());
        donation.getPaypalInfo().setUpdatedAt(LocalDateTime.now());
    }


    /**
     * Capture donation from PayPal
     */
    public Donation captureDonation(Donation donation, String captureId, String paypalEmail, String payerId, String responseData) {
        log.infof("Capturing donation %s with PayPal capture ID %s", donation.getUuid(), captureId);
        donation.getPaypalInfo().setCaptureId(captureId);
        donation.getPaypalInfo().setEmail(paypalEmail);
        donation.getPaypalInfo().setPayerId(payerId);
        donation.getPaypalInfo().setStatus("COMPLETED");
        donation.getPaypalInfo().setResponseData(responseData);
        donation.getPaypalInfo().setCreatedAt(java.time.LocalDateTime.now());
        donation.setUpdatedAt(LocalDateTime.now());
        return donation;
    }

    /**
     * Mark donation as failed
     */
    public void failDonation(Donation donation, String errorMessage) {
        log.infof("Marking donation %s as FAILED: %s", donation.getUuid(), errorMessage);
        donation.getPaypalInfo().setStatus("FAILED");
        donation.getPaypalInfo().setErrorMessage(errorMessage);
        donation.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * Get total count of completed donations
     */
    public long countCompletedDonations() {
        return donationRepository.countCompleted();
    }

    /**
     * Get total amount donated from completed donations only
     */
    public BigDecimal getTotalCompletedAmount() {
        List<Donation> completedDonations = donationRepository.findCompleted();
        return completedDonations.stream()
                .map(Donation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}