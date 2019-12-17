package sa.tamkeentech.tbs.domain.enumeration;

/**
 * The InvoiceStatus enumeration.
 */
public enum InvoiceStatus {
    NEW("New bill creation request"),
    WITTING("Witting response from clint"),
    FAILED("bill creation was failed in SADAD"),
    CREATED("Bill was created in SADAD"),
    EXPIRED("Bill expired"),
    CLIENT_NOTIFIED("The client was notified");

    InvoiceStatus (String description) {
        this.description = description;
    }

    String description;
}
