package sa.tamkeentech.tbs.domain.enumeration;

/**
 * The PaymentStatus enumeration.
 */
public enum PaymentStatus {
    // Better to have Reconciled status as a separate flag because it can be either paid or refunded
    PENDING("Payment in progress"),
    CHECKOUT_PAGE("Checkout page rendered"),
    PAID("Bill was paid"),
    UNPAID("Bill has not been paid"),
    REFUNDED("Refund Accepted"),
    NONE("None Options search");

    PaymentStatus (String description) {
        this.description = description;
    }

    String description;
}
