package com.redhat.quarkus.donation.repository;

import com.redhat.quarkus.donation.entity.Donation;
import com.redhat.quarkus.donation.entity.PayPalInfo;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class DonationRepositoryTest {

    @Inject
    DonationRepository donationRepository;

    @BeforeEach
    @Transactional
    void setUp() {
        // Clean up database before each test
        donationRepository.deleteAll();
    }

    @Test
    @Transactional
    void testPersistAndFind() {
        Donation donation = new Donation("John Doe", "john@example.com", new BigDecimal("100.00"), "Test donation");

        donationRepository.persist(donation);

        assertThat(donation.getUuid()).isNotNull();

        Donation found = donationRepository.findById(donation.getUuid());
        assertThat(found).isNotNull();
        assertThat(found.getDonorName()).isEqualTo("John Doe");
        assertThat(found.getDonorEmail()).isEqualTo("john@example.com");
        assertThat(found.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @Transactional
    void testFindByPaypalOrderId() {
        Donation donation = new Donation("Jane Doe", "jane@example.com", new BigDecimal("50.00"), "Test");
        PayPalInfo paypalInfo = new PayPalInfo();
        paypalInfo.setOrderId("ORDER-123");
        paypalInfo.setStatus("CREATED");
        donation.setPaypalInfo(paypalInfo);

        donationRepository.persist(donation);

        Donation found = donationRepository.findByPaypalOrderId("ORDER-123");

        assertThat(found).isNotNull();
        assertThat(found.getPaypalInfo().getOrderId()).isEqualTo("ORDER-123");
        assertThat(found.getDonorEmail()).isEqualTo("jane@example.com");
    }

    @Test
    @Transactional
    void testFindByPaypalOrderId_NotFound() {
        Donation found = donationRepository.findByPaypalOrderId("NONEXISTENT");

        assertThat(found).isNull();
    }

    @Test
    @Transactional
    void testFindCompleted() {
        // Create completed donations
        Donation completed1 = new Donation("User1", "user1@example.com", new BigDecimal("100.00"), "Completed 1");
        PayPalInfo paypalInfo1 = new PayPalInfo();
        paypalInfo1.setOrderId("ORDER-1");
        paypalInfo1.setStatus("COMPLETED");
        paypalInfo1.setCaptureId("CAPTURE-1");
        completed1.setPaypalInfo(paypalInfo1);
        donationRepository.persist(completed1);

        Donation completed2 = new Donation("User2", "user2@example.com", new BigDecimal("50.00"), "Completed 2");
        PayPalInfo paypalInfo2 = new PayPalInfo();
        paypalInfo2.setOrderId("ORDER-2");
        paypalInfo2.setStatus("COMPLETED");
        paypalInfo2.setCaptureId("CAPTURE-2");
        completed2.setPaypalInfo(paypalInfo2);
        donationRepository.persist(completed2);

        // Create non-completed donations
        Donation pending = new Donation("User3", "user3@example.com", new BigDecimal("75.00"), "Pending");
        PayPalInfo paypalInfo3 = new PayPalInfo();
        paypalInfo3.setOrderId("ORDER-3");
        paypalInfo3.setStatus("CREATED");
        pending.setPaypalInfo(paypalInfo3);
        donationRepository.persist(pending);

        Donation failed = new Donation("User4", "user4@example.com", new BigDecimal("25.00"), "Failed");
        PayPalInfo paypalInfo4 = new PayPalInfo();
        paypalInfo4.setOrderId("ORDER-4");
        paypalInfo4.setStatus("FAILED");
        failed.setPaypalInfo(paypalInfo4);
        donationRepository.persist(failed);

        // Query completed donations
        List<Donation> completedDonations = donationRepository.findCompleted();

        assertThat(completedDonations).hasSize(2);
        assertThat(completedDonations)
            .extracting(d -> d.getPaypalInfo().getStatus())
            .containsOnly("COMPLETED");
    }

    @Test
    @Transactional
    void testFindCompleted_Empty() {
        // No completed donations
        Donation pending = new Donation("User", "user@example.com", new BigDecimal("100.00"), "Pending");
        PayPalInfo paypalInfo = new PayPalInfo();
        paypalInfo.setOrderId("ORDER-1");
        paypalInfo.setStatus("CREATED");
        pending.setPaypalInfo(paypalInfo);
        donationRepository.persist(pending);

        List<Donation> completedDonations = donationRepository.findCompleted();

        assertThat(completedDonations).isEmpty();
    }

    @Test
    @Transactional
    void testCountCompleted() {
        // Create completed donations
        for (int i = 1; i <= 3; i++) {
            Donation donation = new Donation("User" + i, "user" + i + "@example.com",
                new BigDecimal("50.00"), "Donation " + i);
            PayPalInfo paypalInfo = new PayPalInfo();
            paypalInfo.setOrderId("ORDER-" + i);
            paypalInfo.setStatus("COMPLETED");
            paypalInfo.setCaptureId("CAPTURE-" + i);
            donation.setPaypalInfo(paypalInfo);
            donationRepository.persist(donation);
        }

        // Create non-completed donations
        Donation pending = new Donation("Pending User", "pending@example.com", new BigDecimal("100.00"), "Pending");
        PayPalInfo paypalInfo = new PayPalInfo();
        paypalInfo.setOrderId("ORDER-PENDING");
        paypalInfo.setStatus("CREATED");
        pending.setPaypalInfo(paypalInfo);
        donationRepository.persist(pending);

        long count = donationRepository.countCompleted();

        assertThat(count).isEqualTo(3L);
    }

    @Test
    @Transactional
    void testCountCompleted_Zero() {
        // No donations at all
        long count = donationRepository.countCompleted();

        assertThat(count).isZero();
    }

    @Test
    @Transactional
    void testFindCompleted_OrderedByCreatedAt() {
        // Create completed donations with slight delay to ensure different timestamps
        Donation first = new Donation("First", "first@example.com", new BigDecimal("100.00"), "First donation");
        PayPalInfo paypalInfo1 = new PayPalInfo();
        paypalInfo1.setOrderId("ORDER-FIRST");
        paypalInfo1.setStatus("COMPLETED");
        first.setPaypalInfo(paypalInfo1);
        donationRepository.persist(first);

        try {
            Thread.sleep(10); // Small delay to ensure different timestamps
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Donation second = new Donation("Second", "second@example.com", new BigDecimal("50.00"), "Second donation");
        PayPalInfo paypalInfo2 = new PayPalInfo();
        paypalInfo2.setOrderId("ORDER-SECOND");
        paypalInfo2.setStatus("COMPLETED");
        second.setPaypalInfo(paypalInfo2);
        donationRepository.persist(second);

        List<Donation> completedDonations = donationRepository.findCompleted();

        assertThat(completedDonations).hasSize(2);
        // Should be ordered by createdAt DESC (most recent first)
        assertThat(completedDonations.get(0).getDonorName()).isEqualTo("Second");
        assertThat(completedDonations.get(1).getDonorName()).isEqualTo("First");
    }

    @Test
    @Transactional
    void testDeleteAll() {
        // Create some donations
        for (int i = 1; i <= 5; i++) {
            Donation donation = new Donation("User" + i, "user" + i + "@example.com",
                new BigDecimal("25.00"), "Donation " + i);
            donationRepository.persist(donation);
        }

        assertThat(donationRepository.count()).isEqualTo(5L);

        donationRepository.deleteAll();

        assertThat(donationRepository.count()).isZero();
    }

    @Test
    @Transactional
    void testMultipleDonationsSameEmail() {
        // Create multiple donations from same email
        for (int i = 1; i <= 3; i++) {
            Donation donation = new Donation("Same User", "same@example.com",
                new BigDecimal(i * 10 + ".00"), "Donation " + i);
            PayPalInfo paypalInfo = new PayPalInfo();
            paypalInfo.setOrderId("ORDER-" + i);
            paypalInfo.setStatus(i == 3 ? "COMPLETED" : "CREATED");
            donation.setPaypalInfo(paypalInfo);
            donationRepository.persist(donation);
        }

        long total = donationRepository.count();
        assertThat(total).isEqualTo(3L);

        long completed = donationRepository.countCompleted();
        assertThat(completed).isEqualTo(1L);
    }
}
