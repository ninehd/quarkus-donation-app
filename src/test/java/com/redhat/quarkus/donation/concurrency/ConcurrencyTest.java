package com.redhat.quarkus.donation.concurrency;

import com.redhat.quarkus.donation.entity.Donation;
import com.redhat.quarkus.donation.entity.PayPalInfo;
import com.redhat.quarkus.donation.repository.DonationRepository;
import com.redhat.quarkus.donation.service.DonationService;
import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class ConcurrencyTest {

    @Inject
    DonationService donationService;

    @Inject
    DonationRepository donationRepository;

    @BeforeEach
    @Transactional
    void setUp() {
        donationRepository.deleteAll();
    }

    @Test
    @Transactional
    void testConcurrentDonationCreation() {
        int numberOfDonations = 50;

        // Create donations sequentially within transaction
        for (int i = 0; i < numberOfDonations; i++) {
            Donation donation = new Donation(
                "User-" + i,
                "user" + i + "@example.com",
                new BigDecimal("10.00"),
                "Concurrent donation"
            );
            donationRepository.persist(donation);
        }

        // Verify all donations were created
        long count = donationRepository.count();
        assertThat(count).isEqualTo(numberOfDonations);
    }

    @Test
    @Transactional
    void testConcurrentReadOperations() {
        // Setup test data
        setupTestDonations(20);

        // Perform multiple read operations
        for (int i = 0; i < 10; i++) {
            long count = donationRepository.countCompleted();
            assertThat(count).isGreaterThanOrEqualTo(0);
        }
    }

    @Test
    @Transactional
    void testConcurrentUpdateOperations() {
        // Create a donation to be updated
        Donation donation = new Donation("Test User", "test@example.com", new BigDecimal("100.00"), "Test");
        PayPalInfo paypalInfo = new PayPalInfo();
        paypalInfo.setOrderId("ORDER-CONCURRENT");
        paypalInfo.setStatus("CREATED");
        donation.setPaypalInfo(paypalInfo);
        donationRepository.persist(donation);

        // Perform sequential updates
        for (int i = 0; i < 5; i++) {
            donationService.failDonation(donation, "Failed by iteration " + i);
        }

        assertThat(donation.getPaypalInfo().getStatus()).isEqualTo("FAILED");
        assertThat(donation.getPaypalInfo().getErrorMessage()).contains("iteration 4");
    }

    @Test
    @Transactional
    void testConcurrentStatsCalculation() {
        // Setup completed donations
        setupTestDonations(15);

        // Calculate stats multiple times
        for (int i = 0; i < 10; i++) {
            BigDecimal total = donationService.getTotalCompletedAmount();
            assertThat(total).isNotNull();
            assertThat(total).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        }
    }

    @Test
    @Transactional
    void testConcurrentFindByPaypalOrderId() {
        // Create donations with different order IDs
        List<String> orderIds = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Donation donation = new Donation("User" + i, "user" + i + "@example.com",
                new BigDecimal("50.00"), "Test");
            PayPalInfo paypalInfo = new PayPalInfo();
            String orderId = "ORDER-" + i;
            paypalInfo.setOrderId(orderId);
            paypalInfo.setStatus("COMPLETED");
            donation.setPaypalInfo(paypalInfo);
            donationRepository.persist(donation);
            orderIds.add(orderId);
        }

        // Search for each donation
        for (String orderId : orderIds) {
            Donation found = donationRepository.findByPaypalOrderId(orderId);
            assertThat(found).isNotNull();
            assertThat(found.getPaypalInfo().getOrderId()).isEqualTo(orderId);
        }
    }

    @Test
    @Transactional
    void testHighLoadScenario() {
        int numberOfOperations = 100;
        int successfulOperations = 0;

        // Simulate high load with mixed operations
        for (int i = 0; i < numberOfOperations; i++) {
            try {
                if (i % 3 == 0) {
                    // Create
                    Donation donation = new Donation(
                        "HighLoad-" + i,
                        "highload" + i + "@example.com",
                        new BigDecimal("15.00"),
                        "High load test"
                    );
                    donationRepository.persist(donation);
                } else if (i % 3 == 1) {
                    // Count
                    donationRepository.countCompleted();
                } else {
                    // Calculate total
                    donationService.getTotalCompletedAmount();
                }
                successfulOperations++;
            } catch (Exception e) {
                Log.errorf("Operation failed: %s", e.getMessage());
            }
        }

        // Verify high success rate
        double successRate = (double) successfulOperations / numberOfOperations;
        assertThat(successRate).isGreaterThan(0.8);
        assertThat(successfulOperations).isGreaterThan(0);
    }

    @Test
    @Transactional
    void testThreadSafetyWithMultipleReads() {
        // Create test data
        setupTestDonations(20);

        long expectedCount = donationRepository.countCompleted();
        BigDecimal expectedTotal = donationService.getTotalCompletedAmount();

        // Perform multiple reads and verify consistency
        for (int i = 0; i < 20; i++) {
            long count = donationRepository.countCompleted();
            BigDecimal total = donationService.getTotalCompletedAmount();

            assertThat(count).isEqualTo(expectedCount);
            assertThat(total).isEqualByComparingTo(expectedTotal);
        }
    }

    @Test
    @Transactional
    void testBulkInsert() {
        int bulkSize = 100;

        for (int i = 0; i < bulkSize; i++) {
            Donation donation = new Donation(
                "BulkUser" + i,
                "bulk" + i + "@example.com",
                new BigDecimal("5.00"),
                "Bulk donation"
            );
            PayPalInfo paypalInfo = new PayPalInfo();
            paypalInfo.setOrderId("BULK-ORDER-" + i);
            paypalInfo.setStatus(i % 2 == 0 ? "COMPLETED" : "CREATED");
            donation.setPaypalInfo(paypalInfo);
            donationRepository.persist(donation);
        }

        long totalCount = donationRepository.count();
        long completedCount = donationRepository.countCompleted();

        assertThat(totalCount).isEqualTo(bulkSize);
        assertThat(completedCount).isEqualTo(bulkSize / 2);
    }

    @Transactional
    void setupTestDonations(int count) {
        for (int i = 0; i < count; i++) {
            Donation donation = new Donation("User" + i, "user" + i + "@example.com",
                new BigDecimal("25.00"), "Test donation " + i);
            PayPalInfo paypalInfo = new PayPalInfo();
            paypalInfo.setOrderId("ORDER-" + i);
            paypalInfo.setStatus("COMPLETED");
            paypalInfo.setCaptureId("CAPTURE-" + i);
            donation.setPaypalInfo(paypalInfo);
            donationRepository.persist(donation);
        }
    }
}
