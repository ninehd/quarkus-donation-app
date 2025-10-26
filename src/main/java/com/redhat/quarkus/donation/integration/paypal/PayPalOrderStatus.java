package com.redhat.quarkus.donation.integration.paypal;

/**
 * PayPal Order Status enum
 * @see <a href="https://developer.paypal.com/docs/api/orders/v2/#orders_get">PayPal Orders API</a>
 */
public enum PayPalOrderStatus {

    /**
     * The order was created with the specified context.
     */
    CREATED,

    /**
     * The order was saved and persisted. The order status continues to be in progress until a capture
     * is made with final_capture = true for all purchase units within the order.
     */
    SAVED,

    /**
     * The customer approved the payment through the PayPal wallet or another form of guest or unbranded payment.
     */
    APPROVED,

    /**
     * All purchase units in the order are voided.
     */
    VOIDED,

    /**
     * The payment was authorized or the authorized payment was captured for the order.
     */
    COMPLETED,

    /**
     * The order requires an action from the payer (e.g. 3DS authentication).
     */
    PAYER_ACTION_REQUIRED;

    /**
     * Parse PayPal status string to enum
     */
    public static PayPalOrderStatus fromString(String status) {
        if (status == null) {
            return null;
        }
        try {
            return PayPalOrderStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Check if the status indicates a successful payment
     */
    public boolean isSuccessful() {
        return this == COMPLETED;
    }

    /**
     * Check if the status indicates the order can be captured
     */
    public boolean canBeCaptured() {
        return this == APPROVED;
    }

    /**
     * Check if the status indicates a failed or cancelled payment
     */
    public boolean isFailed() {
        return this == VOIDED || (this != COMPLETED && this != APPROVED && this != PAYER_ACTION_REQUIRED);
    }

    /**
     * Check if a status string indicates a completed payment
     */
    public static boolean isCompleted(String status) {
        PayPalOrderStatus statusEnum = fromString(status);
        return statusEnum != null && statusEnum.isSuccessful();
    }
}
