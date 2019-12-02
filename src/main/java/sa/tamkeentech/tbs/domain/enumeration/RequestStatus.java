package sa.tamkeentech.tbs.domain.enumeration;

/**
 * The PaymentStatus enumeration.
 */
public enum RequestStatus {
    CREATED("Request received"),
    PENDING("Request in progress"),
    SUCCEEDED("Request accepted"),
    FAILED("Request rejected");

    RequestStatus(String description) {
        this.description = description;
    }

    String description;
}
