package com.redhat.quarkus.donation.service;

import com.redhat.quarkus.donation.entity.Donation;
import com.redhat.quarkus.donation.integration.paypal.PayPalClient;
import com.redhat.quarkus.donation.integration.paypal.model.PayPalOrderRequest;
import com.redhat.quarkus.donation.integration.paypal.model.PayPalOrderResponse;
import com.redhat.quarkus.donation.integration.paypal.model.Link;
import com.redhat.quarkus.donation.mapper.PayPalOrderMapper;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class PayPalServiceTest {

    @InjectMock
    @RestClient
    PayPalClient paypalClient;

    @InjectMock
    DonationService donationService;

    @Inject
    PayPalOrderMapper paypalMapper;

    @Inject
    PayPalService paypalService;

    @BeforeEach
    void setUp() {
        Mockito.reset(paypalClient, donationService);
    }

    @Test
    void testInitiateDonation_Success() {
        Donation donation = new Donation("John Doe", "john@example.com", new BigDecimal("100.00"), "Test donation");

        PayPalOrderResponse response = new PayPalOrderResponse();
        response.setId("ORDER-123");
        response.setStatus("CREATED");

        Link approveLink = new Link();
        approveLink.setRel("approve");
        approveLink.setHref("https://paypal.com/approve/ORDER-123");
        response.setLinks(Arrays.asList(approveLink));

        when(paypalClient.createOrder(any(PayPalOrderRequest.class))).thenReturn(response);

        paypalService.initiateDonation(donation);

        assertThat(donation.getPaypalInfo().getOrderId()).isEqualTo("ORDER-123");
        assertThat(donation.getPaypalInfo().getApproveLink()).contains("paypal.com");

        verify(paypalClient, times(1)).createOrder(any(PayPalOrderRequest.class));
    }

    @Test
    void testInitiateDonation_NullResponse() {
        Donation donation = new Donation("Jane Doe", "jane@example.com", new BigDecimal("50.00"), "Test");

        when(paypalClient.createOrder(any(PayPalOrderRequest.class))).thenReturn(null);
        doNothing().when(donationService).failDonation(any(Donation.class), anyString());

        assertThatThrownBy(() -> paypalService.initiateDonation(donation))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to initiate payment");

        verify(paypalClient, times(1)).createOrder(any(PayPalOrderRequest.class));
        verify(donationService, times(1)).failDonation(eq(donation), anyString());
    }

    @Test
    void testInitiateDonation_NoApproveLink() {
        Donation donation = new Donation("Bob Smith", "bob@example.com", new BigDecimal("75.00"), "Test");

        PayPalOrderResponse response = new PayPalOrderResponse();
        response.setId("ORDER-456");
        response.setStatus("CREATED");
        response.setLinks(Collections.emptyList()); // No approve link

        when(paypalClient.createOrder(any(PayPalOrderRequest.class))).thenReturn(response);
        doNothing().when(donationService).failDonation(any(Donation.class), anyString());

        assertThatThrownBy(() -> paypalService.initiateDonation(donation))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to initiate payment");

        verify(paypalClient, times(1)).createOrder(any(PayPalOrderRequest.class));
        verify(donationService, times(1)).failDonation(eq(donation), anyString());
    }

    @Test
    void testCaptureDonation_Success() {
        Donation donation = new Donation("Alice Johnson", "alice@example.com", new BigDecimal("25.00"), "Test");
        donation.getPaypalInfo().setOrderId("ORDER-789");

        PayPalOrderResponse captureResponse = new PayPalOrderResponse();
        captureResponse.setId("ORDER-789");
        captureResponse.setStatus("COMPLETED");

        when(paypalClient.captureOrder(eq("ORDER-789"), anyMap())).thenReturn(captureResponse);

        Donation result = paypalService.captureDonation(donation, "ORDER-789");

        assertThat(result).isNotNull();
        assertThat(result.getPaypalInfo().getStatus()).isEqualTo("COMPLETED");

        verify(paypalClient, times(1)).captureOrder(eq("ORDER-789"), anyMap());
    }

    @Test
    void testCaptureDonation_InvalidResponse() {
        Donation donation = new Donation("Charlie Brown", "charlie@example.com", new BigDecimal("30.00"), "Test");
        donation.getPaypalInfo().setOrderId("ORDER-999");

        when(paypalClient.captureOrder(eq("ORDER-999"), anyMap())).thenReturn(null);
        doNothing().when(donationService).failDonation(any(Donation.class), anyString());

        assertThatThrownBy(() -> paypalService.captureDonation(donation, "ORDER-999"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to capture payment");

        verify(paypalClient, times(1)).captureOrder(eq("ORDER-999"), anyMap());
        verify(donationService, times(1)).failDonation(eq(donation), anyString());
    }

    @Test
    void testCaptureDonation_NotCompleted() {
        Donation donation = new Donation("David Lee", "david@example.com", new BigDecimal("40.00"), "Test");
        donation.getPaypalInfo().setOrderId("ORDER-888");

        PayPalOrderResponse captureResponse = new PayPalOrderResponse();
        captureResponse.setId("ORDER-888");
        captureResponse.setStatus("FAILED");

        when(paypalClient.captureOrder(eq("ORDER-888"), anyMap())).thenReturn(captureResponse);
        doNothing().when(donationService).failDonation(any(Donation.class), anyString());

        assertThatThrownBy(() -> paypalService.captureDonation(donation, "ORDER-888"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to capture payment");

        verify(paypalClient, times(1)).captureOrder(eq("ORDER-888"), anyMap());
        verify(donationService, times(1)).failDonation(eq(donation), anyString());
    }

    @Test
    void testGetOrderDetails_Success() {
        PayPalOrderResponse orderResponse = new PayPalOrderResponse();
        orderResponse.setId("ORDER-555");
        orderResponse.setStatus("APPROVED");

        when(paypalClient.getOrder("ORDER-555")).thenReturn(orderResponse);

        PayPalOrderResponse result = paypalService.getOrderDetails("ORDER-555");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("ORDER-555");
        assertThat(result.getStatus()).isEqualTo("APPROVED");

        verify(paypalClient, times(1)).getOrder("ORDER-555");
    }

    @Test
    void testGetOrderDetails_NullResponse() {
        when(paypalClient.getOrder("INVALID-ORDER")).thenReturn(null);

        assertThatThrownBy(() -> paypalService.getOrderDetails("INVALID-ORDER"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to fetch order details");

        verify(paypalClient, times(1)).getOrder("INVALID-ORDER");
    }

    @Test
    void testGetOrderDetails_Exception() {
        when(paypalClient.getOrder("ERROR-ORDER")).thenThrow(new RuntimeException("Network error"));

        assertThatThrownBy(() -> paypalService.getOrderDetails("ERROR-ORDER"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to fetch order details");

        verify(paypalClient, times(1)).getOrder("ERROR-ORDER");
    }
}
