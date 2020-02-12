package sa.tamkeentech.tbs.web.rest.errors;

import org.apache.commons.lang3.ArrayUtils;

public class ItemAlreadyUsedException extends RuntimeException {
    public ItemAlreadyUsedException() {
    }

    public ItemAlreadyUsedException(String message) {
        super(message);
    }

    public ItemAlreadyUsedException(String message, Throwable cause) {
        super(message, cause);
    }

    public ItemAlreadyUsedException(Throwable cause) {
        super(cause);
    }

    public ItemAlreadyUsedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public static void checkNotNull(Object o, String message) {
        negate(o != null, message);
    }

    public static void checkNotEmpty(Object[] o, String message) {
        negate(ArrayUtils.isEmpty(o), message);
    }

    public static void negate(boolean expression, String message) {
        checkExpression(!expression, message);
    }

    public static void checkExpression(boolean expression, String message) {
        if (expression) {
            throw new ItemAlreadyUsedException(message);
        }
    }
}
