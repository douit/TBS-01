package sa.tamkeentech.tbs.domain.enumeration;

/**
 * The InvoiceStatus enumeration.
 */
public enum InvoiceStatus {
    NEW("New bill creation request"),
    FAILED("bill creation was failed in SADAD"),
    CREATED("Bill was created in SADAD"),
    EXPIRED("Bill expired");

    InvoiceStatus (String description) {
        this.description = description;
    }

    String description;
}
