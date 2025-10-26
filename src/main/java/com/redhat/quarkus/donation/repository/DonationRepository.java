package com.redhat.quarkus.donation.repository;

import com.redhat.quarkus.donation.entity.Donation;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class DonationRepository implements PanacheRepositoryBase<Donation, UUID> {

    /**
     * Find donation by PayPal order ID
     */
    public Donation findByPaypalOrderId(String orderId) {
        return find("paypalInfo.orderId", orderId).firstResult();
    }

    /**
     * Find all completed donations (ordered by latest)
     */
    public List<Donation> findCompleted() {
        return list("paypalInfo.status = ?1 order by createdAt desc", "COMPLETED");
    }

    /**
     * Find donations by email
     */
    public List<Donation> findByEmail(String email) {
        return list("donorEmail = ?1 order by createdAt desc", email);
    }

    /**
     * Find pending donations (waiting for PayPal)
     */
    public List<Donation> findPending() {
        return list("paypalInfo.status is null or paypalInfo.status = ?1 order by createdAt desc", "PENDING");
    }

    /**
     * Count completed donations
     */
    public long countCompleted() {
        return count("paypalInfo.status", "COMPLETED");
    }
}