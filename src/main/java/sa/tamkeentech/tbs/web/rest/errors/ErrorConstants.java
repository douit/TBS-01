package sa.tamkeentech.tbs.web.rest.errors;

import java.net.URI;

public final class ErrorConstants {

    public static final String ERR_CONCURRENCY_FAILURE = "error.concurrencyFailure";
    public static final String ERR_VALIDATION = "error.validation";
    public static final String ERR_PAYMENT_GATEWAY = "error.paymentgateway";
    public static final String ERR_INTERNAL = "error.internal";
    public static final String PROBLEM_BASE_URL = "";
    public static final URI DEFAULT_TYPE = URI.create(PROBLEM_BASE_URL + "problem-with-message");
    public static final URI CONSTRAINT_VIOLATION_TYPE = URI.create(PROBLEM_BASE_URL + "constraint-violation");
    public static final URI ENTITY_NOT_FOUND_TYPE = URI.create(PROBLEM_BASE_URL + "entity-not-found");
    public static final URI INVALID_PASSWORD_TYPE = URI.create(PROBLEM_BASE_URL + "invalid-password");
    public static final URI EMAIL_ALREADY_USED_TYPE = URI.create(PROBLEM_BASE_URL + "email-already-used");
    public static final URI LOGIN_ALREADY_USED_TYPE = URI.create(PROBLEM_BASE_URL + "login-already-used");
    public static final URI EMAIL_NOT_FOUND_TYPE = URI.create(PROBLEM_BASE_URL + "email-not-found");
    public static final URI PAYMENT_PROVIDER_ISSUE_TYPE = URI.create(PROBLEM_BASE_URL + "problem-with-payment-provider");
    public static final URI INTERNAL_SERVER_ISSUE_TYPE = URI.create(PROBLEM_BASE_URL + "problem-with-payment-provider");

    private ErrorConstants() {
    }
}
