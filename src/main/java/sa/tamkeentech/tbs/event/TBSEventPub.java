package sa.tamkeentech.tbs.event;

import java.lang.annotation.*;

/**
 * Custom Event publisher.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
@Documented
public @interface TBSEventPub {

    String identifier() default "id";

    String eventName() default "";

}
