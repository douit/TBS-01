package sa.tamkeentech.tbs.config;

/**
 * Application constants.
 */
public final class Constants {

    // Regex for acceptable logins
    public static final String LOGIN_REGEX = "^[_.@A-Za-z0-9-]*$";

    public static final String SYSTEM_ACCOUNT = "system";
    public static final String DEFAULT_LANGUAGE = "en";
    public static final String ANONYMOUS_USER = "anonymoususer";

    // Payment
    public static final String SADAD = "SADAD";
    public static final String VISA = "VISA";
    public static final String CASH = "CASH";

    public static final String CC_PAYMENT_SUCCESS_CODE = "1007";
    public static final String CC_REFUND_SUCCESS_CODE = "7004";

    private Constants() {
    }
}
