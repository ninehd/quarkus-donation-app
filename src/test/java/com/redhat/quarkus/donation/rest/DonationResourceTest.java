package com.redhat.quarkus.donation.rest;

import com.redhat.quarkus.donation.dto.DonationDTO;
import com.redhat.quarkus.donation.dto.StatsDTO;
import com.redhat.quarkus.donation.entity.Donation;
import com.redhat.quarkus.donation.entity.PayPalInfo;
import com.redhat.quarkus.donation.integration.paypal.PayPalOrderStatus;
import com.redhat.quarkus.donation.integration.paypal.model.PayPalOrderResponse;
import com.redhat.quarkus.donation.service.DonationService;
import com.redhat.quarkus.donation.service.PayPalService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@QuarkusTest
class DonationResourceTest {

    @InjectMock
    PayPalService paypalService;

    @Inject
    DonationService donationService;

    @BeforeEach
    @Transactional
    void setUp() {
        Mockito.reset(paypalService);
    }

    @Test
    void testShowForm() {
        given()
            .when()
            .get("/")
            .then()
            .statusCode(200)
            .contentType(ContentType.HTML);
    }

    @Test
    void testGetStats() {
        // Create test data first in separate transaction
        createTestDonation("ORDER-STATS", "COMPLETED");

        given()
            .when()
            .get("/api/donations/stats")
            .then()
            .statusCode(200)
            .contentType(ContentType.JSON)
            .body("donationCount", greaterThanOrEqualTo(1))
            .body("totalAmount", notNullValue());
    }

    @Test
    void testInitiateDonation_Success() {
        // Mock PayPal service to set approval link
        doAnswer(invocation -> {
            Donation donation = invocation.getArgument(0);
            donation.getPaypalInfo().setOrderId("ORDER-123");
            donation.getPaypalInfo().setApproveLink("https://paypal.com/approve/ORDER-123");
            donation.getPaypalInfo().setStatus("CREATED");
            return null;
        }).when(paypalService).initiateDonation(any(Donation.class));

        DonationDTO donationDTO = new DonationDTO();
        donationDTO.setDonorName("John Doe");
        donationDTO.setDonorEmail("john@example.com");
        donationDTO.setAmount(new BigDecimal("50.00"));
        donationDTO.setMessage("Test donation");

        given()
            .contentType(ContentType.JSON)
            .body(donationDTO)
            .when()
            .post("/api/donations/paypal/initiate")
            .then()
            .statusCode(200)
            .body("approvalUrl", notNullValue())
            .body("approvalUrl", containsString("paypal.com"));

        verify(paypalService, times(1)).initiateDonation(any(Donation.class));
    }

    @Test
    void testInitiateDonation_MissingFields() {
        DonationDTO donationDTO = new DonationDTO();
        // Missing required fields

        given()
            .contentType(ContentType.JSON)
            .body(donationDTO)
            .when()
            .post("/api/donations/paypal/initiate")
            .then()
            .statusCode(500); // Will fail due to null pointer or validation
    }

    @Test
    void testHandlePayPalReturn_NotFound() {
        given()
            .queryParam("token", "INVALID-ORDER-ID")
            .queryParam("PayerID", "PAYER123")
            .when()
            .get("/api/donations/paypal/return")
            .then()
            .statusCode(404);
    }

    @Test
    void testHandlePayPalReturn_Success() {
        // Create a donation in a separate transaction
        createTestDonation("ORDER-456", "APPROVED");

        // Verify donation exists
        Donation check = donationService.findByPaypalOrderId("ORDER-456");
        assertThat(check).isNotNull();

        // Mock PayPal API responses
        PayPalOrderResponse orderResponse = new PayPalOrderResponse();
        orderResponse.setStatus("APPROVED");
        when(paypalService.getOrderDetails("ORDER-456")).thenReturn(orderResponse);

        when(paypalService.captureDonation(any(Donation.class), anyString())).thenAnswer(invocation -> {
            Donation d = invocation.getArgument(0);
            d.getPaypalInfo().setStatus("COMPLETED");
            d.getPaypalInfo().setCaptureId("CAPTURE-789");
            return d;
        });

        given()
            .queryParam("token", "ORDER-456")
            .queryParam("PayerID", "PAYER123")
            .when()
            .get("/api/donations/paypal/return")
            .then()
            .statusCode(200)
            .contentType(ContentType.HTML);

        verify(paypalService, times(1)).getOrderDetails("ORDER-456");
        verify(paypalService, times(1)).captureDonation(any(Donation.class), anyString());
    }

    @Test
    void testHandlePayPalReturn_AlreadyCompleted() {
        // Create a donation that's already completed
        createTestDonation("ORDER-COMPLETED", "COMPLETED");

        given()
            .queryParam("token", "ORDER-COMPLETED")
            .queryParam("PayerID", "PAYER123")
            .when()
            .get("/api/donations/paypal/return")
            .then()
            .statusCode(200)
            .contentType(ContentType.HTML);

        // Should not call PayPal service since already completed
        verify(paypalService, never()).getOrderDetails(anyString());
        verify(paypalService, never()).captureDonation(any(Donation.class), anyString());
    }

    @Test
    void testHandlePayPalReturn_Cancelled() {
        // Create a donation
        createTestDonation("ORDER-CANCELLED", "CREATED");

        // Mock PayPal API to return cancelled status
        PayPalOrderResponse orderResponse = new PayPalOrderResponse();
        orderResponse.setStatus("VOIDED");
        when(paypalService.getOrderDetails("ORDER-CANCELLED")).thenReturn(orderResponse);

        given()
            .queryParam("token", "ORDER-CANCELLED")
            .queryParam("PayerID", "PAYER123")
            .when()
            .get("/api/donations/paypal/return")
            .then()
            .statusCode(200)
            .contentType(ContentType.HTML);

        verify(paypalService, times(1)).getOrderDetails("ORDER-CANCELLED");
        verify(paypalService, never()).captureDonation(any(Donation.class), anyString());
    }

    @Transactional
    void createTestDonation(String orderId, String status) {
        Donation donation = new Donation("Test User", "test@example.com", new BigDecimal("100.00"), "Test");
        PayPalInfo paypalInfo = new PayPalInfo();
        paypalInfo.setOrderId(orderId);
        paypalInfo.setStatus(status);
        if ("COMPLETED".equals(status)) {
            paypalInfo.setCaptureId("CAPTURE-" + orderId);
        }
        donation.setPaypalInfo(paypalInfo);
        donationService.createDonation(donation);
    }
}
