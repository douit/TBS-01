package sa.tamkeentech.tbs.service.util;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import sa.tamkeentech.tbs.config.Constants;

import javax.inject.Inject;
import java.util.Locale;

@Component
public class LanguageUtil {

    @Inject
    private MessageSource messageSource;

    public String getMessageByKey(String titleKey, String headerKey) {
        Locale locale = Locale.forLanguageTag(Constants.LANGUAGE.getLanguageByHeaderKey(headerKey));
        return messageSource.getMessage(titleKey, null, locale);
    }


}
