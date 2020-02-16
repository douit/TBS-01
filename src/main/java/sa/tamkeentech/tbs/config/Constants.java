package sa.tamkeentech.tbs.config;

import java.time.ZoneId;

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
    public static final String CREDIT_CARD = "CREDIT_CARD";
    public static final String CASH = "CASH";

    public static final String CC_PAYMENT_SUCCESS_CODE = "1007";
    public static final String CC_REFUND_SUCCESS_CODE = "7004";

    public static Integer INVOICE_EXPIRY_DAYS = 1;


    // Moved to client entity
    /*public enum CLIENT_SADAD_CONFIG {
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
    }*/

    // Default language
    public static final String DEFAULT_HEADER_LANGUAGE = "ar";

    /**
     * Language enumeration
     */
    public enum LANGUAGE {
        ARABIC("ar-ly", "ar"),
        ENGLISH("en", "en");
        LANGUAGE(String languageKey, String headerKey) {
            this.languageKey = languageKey;
            this.headerKey = headerKey;
        }

        public static String getLanguageByHeaderKey(String headerKey) {
            switch (headerKey){
                case "en": return ENGLISH.getLanguageKey();
                default: return ARABIC.getLanguageKey();
            }
        }
        String languageKey;
        String headerKey;

        public String getLanguageKey() {
            return this.languageKey;
        }

        public String getHeaderKey() {
            return headerKey;
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
        CREDIT_CARD_INITIATE("Upload invoice to the online payment provider"), //ok
        CREDIT_CARD_NOTIFICATION("Receive payment notification from the online payment provider"), //ok

        INVOICE_REFUND_REQUEST("Refund request by client app"), // ok

        SADAD_REFUND_REQUEST("Send Refund request to Sadad"), // ok
        SADAD_REFUND_NOTIFICATION("Receive Refund notification from Sadad"), // ..

        CREDIT_CARD_REFUND_REQUEST("Send Refund request to the online payment provider"); // ok

        EventType (String description) {
            this.description = description;
        }

        String description;


    }

    // ZoneDateTime
    public static ZoneId RIYADH_ZONE_ID = ZoneId.of("Asia/Riyadh");
    public static String RIYADH_OFFSET = "+03:00";
    public static ZoneId UTC_ZONE_ID = ZoneId.of("UTC");

    private Constants() {
    }
}
