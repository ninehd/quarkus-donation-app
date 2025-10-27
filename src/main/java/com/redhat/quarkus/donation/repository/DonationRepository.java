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
     * Count completed donations
     */
    public long countCompleted() {
        return count("paypalInfo.status", "COMPLETED");
    }
}