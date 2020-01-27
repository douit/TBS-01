package sa.tamkeentech.tbs.web.rest.errors;

public class PaymentGatewayException extends BadRequestAlertException {

    private static final long serialVersionUID = 1L;

    public PaymentGatewayException(String title) {
        super(ErrorConstants.PAYMENT_PROVIDER_ISSUE_TYPE, title, "", ErrorConstants.ERR_PAYMENT_GATEWAY);
    }
}
