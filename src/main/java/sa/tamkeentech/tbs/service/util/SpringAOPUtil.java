package sa.tamkeentech.tbs.service.util;

import org.aspectj.lang.Signature;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class SpringAOPUtil {


    public String simpleClassAndMethodName(Signature methodSignature)
    {
        return methodSignature.toShortString().substring(0,
                methodSignature.toShortString().indexOf("("));
    }

    public String simpleClassName(Signature methodSignature)
    {
        return methodSignature.toShortString().substring(0,
                methodSignature.toShortString().indexOf("."));
    }

}
