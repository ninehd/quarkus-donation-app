package com.redhat.quarkus.donation.service;

import com.redhat.quarkus.donation.entity.Donation;
import com.redhat.quarkus.donation.entity.PayPalInfo;
import com.redhat.quarkus.donation.repository.DonationRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@QuarkusTest
class DonationServiceTest {

    @InjectMock
    DonationRepository donationRepository;

    @Inject
    DonationService donationService;

    @BeforeEach
    void setUp() {
        Mockito.reset(donationRepository);
    }

    @Test
    void testCreateDonation() {
        Donation donation = new Donation("John Doe", "john@example.com", new BigDecimal("100.00"), "Test donation");

        doNothing().when(donationRepository).persist(any(Donation.class));

        Donation result = donationService.createDonation(donation);

        assertThat(result).isNotNull();
        assertThat(result.getDonorName()).isEqualTo("John Doe");
        assertThat(result.getDonorEmail()).isEqualTo("john@example.com");
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));

        verify(donationRepository, times(1)).persist(donation);
    }

    @Test
    void testFindByPaypalOrderId() {
        String orderId = "ORDER-123";
        Donation donation = new Donation("Jane Doe", "jane@example.com", new BigDecimal("50.00"), "Test");
        PayPalInfo paypalInfo = new PayPalInfo();
        paypalInfo.setOrderId(orderId);
        donation.setPaypalInfo(paypalInfo);

        when(donationRepository.findByPaypalOrderId(orderId)).thenReturn(donation);

        Donation result = donationService.findByPaypalOrderId(orderId);

        assertThat(result).isNotNull();
        assertThat(result.getPaypalInfo().getOrderId()).isEqualTo(orderId);

        verify(donationRepository, times(1)).findByPaypalOrderId(orderId);
    }

    @Test
    void testFindByPaypalOrderId_NotFound() {
        when(donationRepository.findByPaypalOrderId("INVALID")).thenReturn(null);

        Donation result = donationService.findByPaypalOrderId("INVALID");

        assertThat(result).isNull();

        verify(donationRepository, times(1)).findByPaypalOrderId("INVALID");
    }

    @Test
    void testFailDonation() {
        Donation donation = new Donation("Bob Smith", "bob@example.com", new BigDecimal("75.00"), "Test");
        LocalDateTime before = LocalDateTime.now();

        donationService.failDonation(donation, "Payment failed");

        assertThat(donation.getPaypalInfo().getStatus()).isEqualTo("FAILED");
        assertThat(donation.getPaypalInfo().getErrorMessage()).isEqualTo("Payment failed");
        assertThat(donation.getUpdatedAt()).isAfterOrEqualTo(before);
    }

    @Test
    void testCountCompletedDonations() {
        when(donationRepository.countCompleted()).thenReturn(5L);

        long count = donationService.countCompletedDonations();

        assertThat(count).isEqualTo(5L);

        verify(donationRepository, times(1)).countCompleted();
    }

    @Test
    void testCountCompletedDonations_Zero() {
        when(donationRepository.countCompleted()).thenReturn(0L);

        long count = donationService.countCompletedDonations();

        assertThat(count).isZero();

        verify(donationRepository, times(1)).countCompleted();
    }

    @Test
    void testGetTotalCompletedAmount() {
        Donation donation1 = new Donation("User1", "user1@example.com", new BigDecimal("100.00"), "Donation 1");
        Donation donation2 = new Donation("User2", "user2@example.com", new BigDecimal("50.00"), "Donation 2");
        Donation donation3 = new Donation("User3", "user3@example.com", new BigDecimal("75.50"), "Donation 3");

        List<Donation> donations = Arrays.asList(donation1, donation2, donation3);

        when(donationRepository.findCompleted()).thenReturn(donations);

        BigDecimal total = donationService.getTotalCompletedAmount();

        assertThat(total).isEqualByComparingTo(new BigDecimal("225.50"));

        verify(donationRepository, times(1)).findCompleted();
    }

    @Test
    void testGetTotalCompletedAmount_NoDonations() {
        when(donationRepository.findCompleted()).thenReturn(Collections.emptyList());

        BigDecimal total = donationService.getTotalCompletedAmount();

        assertThat(total).isEqualByComparingTo(BigDecimal.ZERO);

        verify(donationRepository, times(1)).findCompleted();
    }

    @Test
    void testGetTotalCompletedAmount_SingleDonation() {
        Donation donation = new Donation("Single User", "single@example.com", new BigDecimal("42.99"), "Only one");

        when(donationRepository.findCompleted()).thenReturn(Collections.singletonList(donation));

        BigDecimal total = donationService.getTotalCompletedAmount();

        assertThat(total).isEqualByComparingTo(new BigDecimal("42.99"));

        verify(donationRepository, times(1)).findCompleted();
    }
}
