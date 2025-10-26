package com.redhat.quarkus.donation.service;

import com.redhat.quarkus.donation.entity.Donation;
import com.redhat.quarkus.donation.repository.DonationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    public void updatePaypalOrderId(Donation donation, String orderId) {
        log.infof("PayPal order created successfully for donation %d with PayPal order ID %s", donation.getId(), orderId);
        donation.getPaypalInfo().setOrderId(orderId);
        donation.getPaypalInfo().setCreatedAt(LocalDateTime.now());
        donation.getPaypalInfo().setUpdatedAt(LocalDateTime.now());
    }


    /**
     * Capture donation from PayPal
     */
    public Donation captureDonation(Donation donation, String transactionId, String paypalEmail, String payerId, String responseData) {
        log.infof("Capturing donation %d with PayPal transaction %s", donation.getId(), transactionId);
        donation.getPaypalInfo().setTransactionId(transactionId);
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
        log.infof("Marking donation %d as FAILED: %s", donation.getId(), errorMessage);
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
     * Count total donations
     */
    public long countDonations() {
        return donationRepository.count();
    }

    /**
     * Get total amount donated
     */
    public BigDecimal getTotalAmount() {
        List<Donation> donations = donationRepository.listAll();
        return donations.stream()
                .map(Donation::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}