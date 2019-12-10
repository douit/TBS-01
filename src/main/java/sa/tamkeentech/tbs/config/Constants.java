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

    /*public static final String CLIENT_MUSANED = "MUSANED";
    public static final String CLIENT_AJIR = "AJIR";
    public static final String CLIENT_TAHAQAQ = "TAHAQAQ";*/

    public enum CLIENT_SADAD_CONFIG {
        MUSANED(5000000000l, 1000l),
        AJIR(6000000000l, 1000l),
        TAHAQAQ(7000000000l, 4000l);

        CLIENT_SADAD_CONFIG(Long initialAccountId, Long initialBillId) {
            this.initialAccountId = initialAccountId;
            this.initialBillId = initialBillId;
        }

        Long initialAccountId;
        Long initialBillId;

        public Long getInitialAccountId() {
            return initialAccountId;
        }

        public Long getInitialBillId() {
            return initialBillId;
        }
    }

    public static final String INVOICE_DEFAULT_SEQ = "invoice_%s_id_seq";

    private Constants() {
    }
}
