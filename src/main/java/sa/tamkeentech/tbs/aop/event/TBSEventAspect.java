package sa.tamkeentech.tbs.aop.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.LinkedMap;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import sa.tamkeentech.tbs.config.AuditEventPublisher;
import sa.tamkeentech.tbs.service.util.BeanPropertyUtils;
import sa.tamkeentech.tbs.service.util.SpringAOPUtil;
import sa.tamkeentech.tbs.web.rest.errors.TbsRunTimeException;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 *
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class TBSEventAspect {

    private final SpringAOPUtil springAOPUtil;

    private final BeanPropertyUtils beanPropertyUtils;

    @Inject
    private AuditEventPublisher auditPublisher;

    @Async
    @AfterReturning(value = "@annotation(sa.tamkeentech.tbs.aop.event.TBSEventPub)", returning = "result", argNames = "joinPoint,result")
    public void fireAnEvent(final JoinPoint joinPoint, final Object result) {
        CompletableFuture.supplyAsync(() -> {
            if (joinPoint.getSignature() instanceof MethodSignature) {
                Method targetMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
                TBSEventPub event = AnnotationUtils.findAnnotation(targetMethod, TBSEventPub.class);
                if (event != null) {
                    return fireAnEvent(joinPoint, event, result);
                }
                return null;
            } else {
                //this should never happen.
                throw new TbsRunTimeException("Target is not method! The annotation is might be placed in wrong place!");
            }
        });
    }

    @AfterThrowing(value = "@annotation(sa.tamkeentech.tbs.aop.event.TBSEventPub)", throwing = "error")
    public void fireAnEvent(final JoinPoint joinPoint, Throwable error) {
        CompletableFuture.supplyAsync(() -> {
            if (joinPoint.getSignature() instanceof MethodSignature) {
                Method targetMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
                TBSEventPub event = AnnotationUtils.findAnnotation(targetMethod, TBSEventPub.class);
                if (event != null) {
                    return fireAnException(joinPoint, event, error);
                }
                return null;
            } else {
                //this should never happen.
                throw new TbsRunTimeException("Target is not method! The annotation is might be placed in wrong place!");
            }
        });
    }

    private CompletableFuture<Boolean> fireAnEvent(JoinPoint joinPoint, TBSEventPub event, Object result) {
        final String eventName = (event.eventName() != null ? event.eventName().name() : springAOPUtil.simpleClassAndMethodName(joinPoint.getSignature()));

        // try to extract referenceId from req else try resp
        String referenceId = "";
        Object referenceObject = beanPropertyUtils.getProp(joinPoint.getArgs()[0], event.referenceId());
        if (referenceObject != null) {
            referenceId = referenceObject.toString();
        } else if (result != null) {
            referenceId = beanPropertyUtils.getProp(result, event.referenceId()).toString();
        }

        // extract prop from input
        final String principal = (!event.principal().isEmpty() && joinPoint.getArgs().length > 0 ?
            beanPropertyUtils.getProp(joinPoint.getArgs()[0], event.principal()).toString() : "");

        log.debug("defaultEvent: {}, identifier: {}", eventName, principal);
        // Method principal
        Map<String, Object> paramValues = extractParams(joinPoint, event);
        paramValues.put("result", result);
        paramValues.put("successful", true);
        paramValues.put("referenceId", referenceId);
        log.debug("paramValues: {}", paramValues);
        return persistEvent(principal, eventName, paramValues);
    }

    private CompletableFuture<Boolean> fireAnException(JoinPoint joinPoint, TBSEventPub event, Throwable error) {
        final String eventName = (event.eventName() != null ? event.eventName().name() : springAOPUtil.simpleClassAndMethodName(joinPoint.getSignature()));
        // extract prop from first parameter
        final String principal = (!event.principal().isEmpty() && joinPoint.getArgs().length > 0 ?
            beanPropertyUtils.getProp(joinPoint.getArgs()[0], event.principal()).toString() : "");
        Map<String, Object> paramValues = extractParams(joinPoint, event);

        // try to extract referenceId from req else try resp
        String referenceId = "";
        Object referenceObject = beanPropertyUtils.getProp(joinPoint.getArgs()[0], event.referenceId());
        if (referenceObject != null) {
            referenceId = referenceObject.toString();
            paramValues.put("referenceId", referenceId);
        }
        log.debug("defaultEvent: {}, identifier: {}", eventName, principal);
        // Method params

        paramValues.put("result", error);
        paramValues.put("successful", false);
        log.debug("paramValues: {}", paramValues);
        return persistEvent(principal, eventName, paramValues);
    }

    @Async
    public CompletableFuture<Boolean> persistEvent(String identifier, String eventName,Map<String, Object> paramValues) {
        AuditEvent event = new AuditEvent(identifier, eventName, paramValues);
        auditPublisher.publish(event);
        SecurityContextHolder.clearContext();
        return CompletableFuture.completedFuture(true);
    }

    private Map<String, Object> extractParams(JoinPoint joinPoint, TBSEventPub event) {
        Map<String, Object> paramValue = new LinkedMap();
        String[] params = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        Object[] values = joinPoint.getArgs();

        // Get only first param
        if (params != null && params.length > 0) {
            paramValue.put(params[0], values[0]);
        }
        /*if (params != null) {
            for (int idx = 0; idx < params.length; idx++)
                paramValue.put(params[idx], values[idx]);
        }*/
        return paramValue;
    }



}
