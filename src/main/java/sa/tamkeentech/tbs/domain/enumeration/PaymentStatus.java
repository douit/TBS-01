package sa.tamkeentech.tbs.domain.enumeration;

/**
 * The PaymentStatus enumeration.
 */
public enum PaymentStatus {
    // Better to have Reconciled status as a separate flag because it can be either paid or refunded
    PENDING, PAID, UNPAID, REFUNDED
}
