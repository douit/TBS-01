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

    /**
     * The ProcessStatus enumeration.
     */
    public enum EventType {

        INVOICE_CREATE("Create invoice by client app"), //ok

        SADAD_INITIATE("Upload invoice to Sadad"), //ok
        SADAD_NOTIFICATION("Receive payment notification from Sadad"), //ok

        CREDIT_CARD_PAYMENT_REQUEST("Request new credit card payment by client app"), //ok
        CREDIT_CARD_INITIATE("Upload invoice to the payment provider"), //ok
        CREDIT_CARD_NOTIFICATION("Receive payment notification from the payment provider"), //ok

        INVOICE_REFUND_REQUEST("Refund request by client app"), // ok

        SADAD_REFUND_REQUEST("Send Refund request to Sadad"), // ..
        SADAD_REFUND_NOTIFICATION("Receive Refund notification from Sadad"), // ..

        CREDIT_CARD_REFUND_REQUEST("Send Refund request to the payment provider"); // ok

        EventType (String description) {
            this.description = description;
        }

        String description;


    }

    private Constants() {
    }
}
