package sa.tamkeentech.tbs.domain.enumeration;

/**
 * The ProcessStatus enumeration.
 */
public enum NotificationStatus {
    PAYMENT_NOTIFICATION_NEW("Payment notification has not been sent to Biller"),
    PAYMENT_NOTIFICATION_IN_PROGRESS("Payment notification is in progress"),
    PAYMENT_NOTIFICATION_SUCCESS("Payment notification successfully sent to Biller"),
    PAYMENT_NOTIFICATION_FAILED("Payment notification Failed to sent to Biller"),
    REFUND_REQUEST_RECEIVED("New refund request"),
    REFUND_CREATED("Refund created"),
    REFUND_ACCEPTED("Refund Accepted"),
    REFUND_COMPLETED("Refund Completed");

    NotificationStatus (String description) {
        this.description = description;
    }

    String description;


}
