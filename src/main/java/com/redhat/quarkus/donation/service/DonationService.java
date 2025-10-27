package com.redhat.quarkus.donation.service;

import com.redhat.quarkus.donation.entity.Donation;
import com.redhat.quarkus.donation.repository.DonationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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