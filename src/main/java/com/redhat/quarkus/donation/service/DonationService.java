package com.redhat.quarkus.donation.service;

import com.redhat.quarkus.donation.entity.Donation;
import com.redhat.quarkus.donation.repository.DonationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class DonationService {

    private static final Logger log = Logger.getLogger(DonationService.class);

    @Inject
    DonationRepository donationRepository;

    /**
     * Create a new donation
     */
    @Transactional
    public Donation createDonation(Donation donation) {
        log.infof("Creating donation from %s for amount %s", donation.getDonorEmail(), donation.getAmount());
        donationRepository.persist(donation);
        return donation;
    }

    /**
     * Get donation by ID
     */
    public Optional<Donation> getDonationById(Long id) {
        return donationRepository.findByIdOptional(id);
    }

    /**
     * Get all donations
     */
    public List<Donation> getAllDonations() {
        return donationRepository.listAll();
    }

    /**
     * Get all completed donations
     */
    public List<Donation> getCompletedDonations() {
        return donationRepository.findCompleted();
    }

    /**
     * Get donations by email
     */
    public List<Donation> getDonationsByEmail(String email) {
        return donationRepository.findByEmail(email);
    }

    /**
     * Get pending donations (waiting for PayPal)
     */
    public List<Donation> getPendingDonations() {
        return donationRepository.findPending();
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
    @Transactional
    public Donation updatePaypalOrderId(Long id, String orderId) {
        log.infof("Updating donation %d with PayPal order ID %s", id, orderId);
        Donation donation = donationRepository.findById(id);
        if (donation != null) {
            donation.getPaypalInfo().setOrderId(orderId);
            donationRepository.persist(donation);
        }
        return donation;
    }

    /**
     * Capture donation from PayPal
     */
    @Transactional
    public Donation captureDonation(Long id, String transactionId, String paypalEmail, String responseData) {
        log.infof("Capturing donation %d with PayPal transaction %s", id, transactionId);
        Donation donation = donationRepository.findById(id);
        if (donation != null) {
            donation.getPaypalInfo().setTransactionId(transactionId);
            donation.getPaypalInfo().setEmail(paypalEmail);
            donation.getPaypalInfo().setStatus("COMPLETED");
            donation.getPaypalInfo().setResponseData(responseData);
            donation.getPaypalInfo().setCreatedAt(java.time.LocalDateTime.now());
            donation.setUpdatedAt(java.time.LocalDateTime.now());
            donationRepository.persist(donation);
        }
        return donation;
    }

    /**
     * Mark donation as failed
     */
    @Transactional
    public Donation failDonation(Long id, String errorMessage) {
        log.infof("Marking donation %d as FAILED: %s", id, errorMessage);
        Donation donation = donationRepository.findById(id);
        if (donation != null) {
            donation.getPaypalInfo().setStatus("FAILED");
            donation.getPaypalInfo().setErrorMessage(errorMessage);
            donation.setUpdatedAt(java.time.LocalDateTime.now());
            donationRepository.persist(donation);
        }
        return donation;
    }

    /**
     * Get total count of completed donations
     */
    public long countCompletedDonations() {
        return donationRepository.countCompleted();
    }

    /**
     * Delete donation
     */
    @Transactional
    public void deleteDonation(Long id) {
        log.infof("Deleting donation %d", id);
        donationRepository.deleteById(id);
    }
}