package com.redhat.quarkus.donation.mapper;

import com.redhat.quarkus.donation.entity.Donation;
import com.redhat.quarkus.donation.integration.paypal.model.PayPalOrderRequest;
import com.redhat.quarkus.donation.integration.paypal.model.PayPalOrderResponse;
import com.redhat.quarkus.donation.integration.paypal.model.Amount;
import com.redhat.quarkus.donation.integration.paypal.model.ApplicationContext;
import com.redhat.quarkus.donation.integration.paypal.model.Breakdown;
import com.redhat.quarkus.donation.integration.paypal.model.Item;
import com.redhat.quarkus.donation.integration.paypal.model.Money;
import com.redhat.quarkus.donation.integration.paypal.model.Payer;
import com.redhat.quarkus.donation.integration.paypal.model.PurchaseUnit;
import com.redhat.quarkus.donation.utils.JsonUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Objects;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PayPalOrderMapper {

    /**
     * Create PayPal order request from donation and URLs
     */
    @Mapping(target = "intent", constant = "CAPTURE")
    @Mapping(target = "payer", source = "donation", qualifiedByName = "createPayer")
    @Mapping(target = "purchaseUnits", source = "donation", qualifiedByName = "createPurchaseUnits")
    @Mapping(target = "applicationContext", source = "returnUrl", qualifiedByName = "createApplicationContext")
    PayPalOrderRequest toOrderRequest(Donation donation, String returnUrl);

    /**
     * Create Payer from donation
     */
    @Named("createPayer")
    default Payer createPayer(Donation donation) {
        Payer payer = new Payer();
        payer.emailAddress = donation.getDonorEmail();
        return payer;
    }

    /**
     * Create PurchaseUnits from donation
     */
    @Named("createPurchaseUnits")
    default PurchaseUnit[] createPurchaseUnits(Donation donation) {
        String description = "My Wonderful Donation";

        PurchaseUnit purchaseUnit = new PurchaseUnit();
        purchaseUnit.description = description;

        // Create amount with breakdown
        Amount amount = new Amount();
        amount.value = donation.getAmount().toString();
        amount.currencyCode = donation.getCurrency();

        Breakdown breakdown = new Breakdown();
        Money itemTotal = new Money();
        itemTotal.value = donation.getAmount().toString();
        itemTotal.currencyCode = donation.getCurrency();
        breakdown.itemTotal = itemTotal;
        amount.breakdown = breakdown;

        purchaseUnit.amount = amount;

        // Create items
        Item item = new Item();
        item.name = description;
        item.description = description;
        item.quantity = "1";
        Money unitAmount = new Money();
        unitAmount.value = donation.getAmount().toString();
        unitAmount.currencyCode = donation.getCurrency();
        item.unitAmount = unitAmount;

        purchaseUnit.items = new Item[]{item};

        return new PurchaseUnit[]{purchaseUnit};
    }

    /**
     * Create ApplicationContext from URLs
     */
    @Named("createApplicationContext")
    default ApplicationContext createApplicationContext(String returnUrl) {
        ApplicationContext context = new ApplicationContext();
        context.returnUrl = returnUrl;
        context.cancelUrl = returnUrl;
        return context;
    }

    /**
     * Update donation entity with PayPal order response data after order creation
     */
    @Mapping(target = "paypalInfo.orderId", source = "response.id")
    @Mapping(target = "paypalInfo.status", source = "response.status")
    @Mapping(target = "paypalInfo.approveLink", source = "response", qualifiedByName = "extractApproveLink")
    @Mapping(target = "paypalInfo.createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "paypalInfo.updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    void updateDonationFromOrderResponse(@MappingTarget Donation donation, PayPalOrderResponse response);

    /**
     * Update donation entity with PayPal capture response data after payment capture
     */
    @Mapping(target = "paypalInfo.status", source = "response.status")
    @Mapping(target = "paypalInfo.captureId", source = "response", qualifiedByName = "extractCaptureId")
    @Mapping(target = "paypalInfo.email", source = "response", qualifiedByName = "extractPayPalEmail")
    @Mapping(target = "paypalInfo.payerId", source = "response", qualifiedByName = "extractPayerId")
    @Mapping(target = "paypalInfo.responseData", source = "response", qualifiedByName = "serializeResponse")
    @Mapping(target = "paypalInfo.updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    void updateDonationFromCaptureResponse(@MappingTarget Donation donation, PayPalOrderResponse response);

    /**
     * Extract approve link from PayPal links
     */
    @Named("extractApproveLink")
    default String extractApproveLink(PayPalOrderResponse response) {
        if (response.getLinks() == null) {
            return null;
        }
        return response.getLinks()
                .stream()
                .filter(link -> "approve".equals(link.rel))
                .map(link -> link.href)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    /**
     * Extract capture ID from purchase units
     */
    @Named("extractCaptureId")
    default String extractCaptureId(PayPalOrderResponse response) {
        if (response.getPurchaseUnits() == null || response.getPurchaseUnits().isEmpty()) {
            return null;
        }

        PurchaseUnit purchaseUnit = response.getPurchaseUnits().get(0);
        if (purchaseUnit.getPayments() == null
            || purchaseUnit.getPayments().getCaptures() == null
            || purchaseUnit.getPayments().getCaptures().isEmpty()) {
            return null;
        }

        return purchaseUnit.getPayments().getCaptures().get(0).getId();
    }

    /**
     * Extract PayPal email from payment source
     */
    @Named("extractPayPalEmail")
    default String extractPayPalEmail(PayPalOrderResponse response) {
        if (response.getPaymentSource() == null
            || response.getPaymentSource().getPaypal() == null) {
            return null;
        }
        return response.getPaymentSource().getPaypal().getEmailAddress();
    }

    /**
     * Extract payer ID from payment source
     */
    @Named("extractPayerId")
    default String extractPayerId(PayPalOrderResponse response) {
        if (response.getPaymentSource() == null
            || response.getPaymentSource().getPaypal() == null) {
            return null;
        }
        return response.getPaymentSource().getPaypal().getAccountId();
    }

    /**
     * Serialize PayPal response to JSON string
     */
    @Named("serializeResponse")
    default String serializeResponse(PayPalOrderResponse response) {
        return JsonUtils.toJson(response);
    }
}