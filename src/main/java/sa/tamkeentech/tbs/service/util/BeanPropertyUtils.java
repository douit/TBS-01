package sa.tamkeentech.tbs.service.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import sa.tamkeentech.tbs.web.rest.errors.TbsRunTimeException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 *
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class BeanPropertyUtils {

    private static final String GETTER_METHOD_PREFIX ="get";
    private static final String IS_METHOD_PREFIX ="is";

    private final ObjectMapper objectMapper;

    private static Set<Class<?>> ignored_classes = new HashSet<Class<?>>(){{
        add(Map.class);
        add(Collection.class);
    }};

    /**
     * select desired properties form the given object based on getter methods
     *
     * @param target targeClass
     * @param desiredProperties desiredProps
     * @return map of string and object values
     */
    public static Map<String,Object> select(final Object target,String... desiredProperties){

        TbsRunTimeException.checkNotNull(target,"target object must not be null");

        if(target instanceof Map || target instanceof Collection){
            throw new TbsRunTimeException("target must not be a collection or map");
        }

        Map<String,Object> selectedProperties =  new HashMap<>();
        final boolean isSelectAll = ArrayUtils.isEmpty(desiredProperties) || desiredProperties[0].equals("*");

        final Class<?> targetClass = target.getClass();


        final List<Method> desiredGetterMethods = Arrays.stream(targetClass.getMethods())
                .filter(method->{

                    final String pojoMethodName = method.getName();
                    if(isSelectAll && (pojoMethodName.startsWith(GETTER_METHOD_PREFIX) ||  pojoMethodName.startsWith(IS_METHOD_PREFIX))){
                        return true;
                    }

                    final String methodPrefix = pojoMethodName.startsWith(GETTER_METHOD_PREFIX)?GETTER_METHOD_PREFIX:IS_METHOD_PREFIX;
                    return Arrays.stream(desiredProperties)
                            .map(desiredProperty-> (methodPrefix+desiredProperty))
                            .filter(methodName-> methodName.equalsIgnoreCase(method.getName()))
                            .findFirst().isPresent();
                })
                .collect(Collectors.toList());

        desiredGetterMethods.forEach(method -> {
            if(!Modifier.isPublic(method.getDeclaringClass().getModifiers())){
                method.setAccessible(true);
            }

            final String propertyName = method.getName().startsWith(GETTER_METHOD_PREFIX)?method.getName().substring(GETTER_METHOD_PREFIX.length()):method.getName().substring(IS_METHOD_PREFIX.length());
            final Object val = ReflectionUtils.invokeMethod(method,target);

            selectedProperties.put(lowerCaseFirstLetter(propertyName),val);
        });

        return selectedProperties;
    }

    /**
     *
     * @param init string to be loweredCastFirstLetter
     * @return the string
     */
    public static String lowerCaseFirstLetter(final String init) {
        if (init==null)
            return null;

        final StringBuilder ret = new StringBuilder(init);
        String firstLetter  = ret.substring(0, 1).toLowerCase();
        ret.replace(0,1,firstLetter);
        return ret.toString();
    }

    /**
     * Silently reads a generic object and travese it's path to get properity.
     * It will return null if it's not found.
     * Note: Passing object that is not null
     * @param theObject the object to be traversed
     * @param path the traverse path
     * @param <T> the object result, can't be null
     * @return the object result
     */
    public <T> Object getProp(T theObject,String path)
    {
        Object result = null;
        try {
            if(CompletableFuture.class.isAssignableFrom(theObject.getClass()))
            {
                theObject = (T) ((CompletableFuture)theObject).getNow(new Object());
            }
            result =  PropertyUtils.getProperty(theObject,path);
        }  catch (IllegalAccessException e) {
            //warned to be aware of it, however, suppressing it.
            log.warn(e.getMessage(),e);
        } catch (InvocationTargetException e) {
            log.warn(e.getMessage(),e);
        } catch (NoSuchMethodException e) {
            log.warn(e.getMessage(),e);
        }catch (NestedNullException e) {
            log.warn(e.getMessage(),e);
        }

        return result;
    }

    /**
     * Silently reads a generic object and travese it's path to get properity.
     * It will return the object serilized to json string or null if it's not found.
     * @param theObject the object to be traversed
     * @param path the traverse path
     * @param <T> the object
     * @return a string representation of the target object
     */
    public <T> String getPropStr(T theObject,String path)
    {
        Object resultOb = getProp(theObject, path);
        return writeValueSilently(resultOb);
    }

    /**
     * Write Json String of an Object Silently
     * or return null;
     * @param object the object to be converted to string
     * @return the string json of the provided object
     */
    public String writeValueSilently(Object object)
    {
        String result = null;
        try {
            result = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.warn(e.getMessage(),e);
        }
        return result;
    }

}
