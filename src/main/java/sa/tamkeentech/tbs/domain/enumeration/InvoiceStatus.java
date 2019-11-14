package sa.tamkeentech.tbs.domain.enumeration;

/**
 * The InvoiceStatus enumeration.
 */
public enum InvoiceStatus {
    NEW("New bill creation request"),
    FAILED("bill creation was failed"),
    CREATED("Bill was created"),
    EXPIRED("Bill expired");

    InvoiceStatus (String description) {
        this.description = description;
    }

    String description;
}
