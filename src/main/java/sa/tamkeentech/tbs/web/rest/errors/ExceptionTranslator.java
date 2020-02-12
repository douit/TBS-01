package sa.tamkeentech.tbs.web.rest.errors;

import io.github.jhipster.web.util.HeaderUtil;
import io.sentry.Sentry;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.DefaultProblem;
import org.zalando.problem.Problem;
import org.zalando.problem.ProblemBuilder;
import org.zalando.problem.Status;
import org.zalando.problem.spring.web.advice.ProblemHandling;
import org.zalando.problem.spring.web.advice.security.SecurityAdviceTrait;
import org.zalando.problem.violations.ConstraintViolationProblem;
import sa.tamkeentech.tbs.service.util.RandomUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Controller advice to translate the server side exceptions to client-friendly json structures.
 * The error response follows RFC7807 - Problem Details for HTTP APIs (https://tools.ietf.org/html/rfc7807).
 */
@ControllerAdvice
public class ExceptionTranslator implements ProblemHandling, SecurityAdviceTrait {

    private static final String FIELD_ERRORS_KEY = "fieldErrors";
    private static final String MESSAGE_KEY = "message";
    private static final String PATH_KEY = "path";
    private static final String VIOLATIONS_KEY = "violations";
    private static final String CODE_KEY = "code";
    private static final String CODE_TIME = "time";

    private final Logger log = LoggerFactory.getLogger(ExceptionTranslator.class);

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    /**
     * Post-process the Problem payload to add the message key for the front-end if needed.
     */
    @Override
    public ResponseEntity<Problem> process(@Nullable ResponseEntity<Problem> entity, NativeWebRequest request) {
        String code = RandomUtil.randomAlphaNumeric(6);
        if (entity == null) {
            return entity;
        }
        Problem problem = entity.getBody();
        if (!(problem instanceof ConstraintViolationProblem || problem instanceof DefaultProblem)) {
            return entity;
        }
        ProblemBuilder builder = Problem.builder()
            .withType(Problem.DEFAULT_TYPE.equals(problem.getType()) ? ErrorConstants.DEFAULT_TYPE : problem.getType())
            .withStatus(problem.getStatus())
            .withTitle(problem.getTitle())
            .with(CODE_KEY, code)
            .with(CODE_TIME, ZonedDateTime.now())
            .with(PATH_KEY, request.getNativeRequest(HttpServletRequest.class).getRequestURI());

        if (problem instanceof ConstraintViolationProblem) {
            builder
                .with(VIOLATIONS_KEY, ((ConstraintViolationProblem) problem).getViolations())
                .with(MESSAGE_KEY, ErrorConstants.ERR_VALIDATION);
        } else {
            /*builder
                .withCause(((DefaultProblem) problem).getCause())
                .withDetail(problem.getDetail())
                .withInstance(problem.getInstance());
            problem.getParameters().forEach(builder::with);*/
           if (!problem.getParameters().containsKey(MESSAGE_KEY) && problem.getStatus() != null) {
                builder.with(MESSAGE_KEY, "error.http." + problem.getStatus().getStatusCode());
            } else {
               builder.with(MESSAGE_KEY, ErrorConstants.ERR_VALIDATION);
           }
        }
        if (Status.UNAUTHORIZED != problem.getStatus() && Status.FORBIDDEN != problem.getStatus()) {
            Sentry.capture("Exception code: " + code + ", title: " + problem.getTitle() + ", please check out the log");
            log.error("---Exception code: {}, title: {}, please check out the log", code, problem.getTitle());
        }
        return new ResponseEntity<>(builder.build(), entity.getHeaders(), entity.getStatusCode());
    }

    public ResponseEntity<Problem> create(final Throwable throwable, final Problem problem, final NativeWebRequest request, String code) {
        log.error("---Exception code: {}, title: {}, message: {}, stack: {}", code, problem.getTitle(), ErrorConstants.ERR_VALIDATION, throwable.getStackTrace());
        Sentry.capture(throwable);
        return create(throwable, problem, request);
    }

    @Override
    public ResponseEntity<Problem> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, @Nonnull NativeWebRequest request) {
        BindingResult result = ex.getBindingResult();
        String code = RandomUtil.randomAlphaNumeric(6);
        List<FieldErrorVM> fieldErrors = result.getFieldErrors().stream()
            .map(f -> new FieldErrorVM(f.getObjectName().replaceFirst("DTO$", ""), f.getField(), f.getCode()))
            .collect(Collectors.toList());

        Problem problem = Problem.builder()
            .withType(ErrorConstants.CONSTRAINT_VIOLATION_TYPE)
            .withTitle("Method argument not valid")
            .withStatus(defaultConstraintViolationStatus())
            .with(MESSAGE_KEY, ErrorConstants.ERR_VALIDATION)
            .with(FIELD_ERRORS_KEY, fieldErrors)
            .with(CODE_KEY, code)
            .with(CODE_TIME, ZonedDateTime.now())
            .build();
        return create(ex, problem, request, code);
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleNoSuchElementException(NoSuchElementException ex, NativeWebRequest request) {
        String code = RandomUtil.randomAlphaNumeric(6);
        Problem problem = Problem.builder()
            .withStatus(Status.NOT_FOUND)
            .with(MESSAGE_KEY, ErrorConstants.ENTITY_NOT_FOUND_TYPE)
            .with(CODE_KEY, code)
            .with(CODE_TIME, ZonedDateTime.now())
            .with(CODE_KEY, code)
            .with(CODE_TIME, ZonedDateTime.now())
            .build();
        return create(ex, problem, request, code);
    }
    @ExceptionHandler
    public ResponseEntity<Problem> handleEmailAreadyUsedException(sa.tamkeentech.tbs.service.EmailAlreadyUsedException ex, NativeWebRequest request) {
        EmailAlreadyUsedException problem = new EmailAlreadyUsedException();
        return create(problem, request, HeaderUtil.createFailureAlert(applicationName,  true, problem.getEntityName(), problem.getErrorKey(), problem.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleUsernameAreadyUsedException(sa.tamkeentech.tbs.service.UsernameAlreadyUsedException ex, NativeWebRequest request) {
        LoginAlreadyUsedException problem = new LoginAlreadyUsedException();
        return create(problem, request, HeaderUtil.createFailureAlert(applicationName,  true, problem.getEntityName(), problem.getErrorKey(), problem.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleInvalidPasswordException(sa.tamkeentech.tbs.service.InvalidPasswordException ex, NativeWebRequest request) {
        return create(new InvalidPasswordException(), request);
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleBadRequestAlertException(BadRequestAlertException ex, NativeWebRequest request) {
        return create(ex, request, HeaderUtil.createFailureAlert(applicationName, true, ex.getEntityName(), ex.getErrorKey(), ex.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Problem> handleConcurrencyFailure(ConcurrencyFailureException ex, NativeWebRequest request) {
        Problem problem = Problem.builder()
            .withStatus(Status.CONFLICT)
            .with(MESSAGE_KEY, ErrorConstants.ERR_CONCURRENCY_FAILURE)
            .build();
        return create(ex, problem, request);
    }

    @ExceptionHandler
    public ResponseEntity<Problem> tbsRuntime(TbsRunTimeException ex, NativeWebRequest request) {
        String code = RandomUtil.randomAlphaNumeric(6);

        Problem problem = Problem.builder()
            .withTitle(ex.getMessage())
            .withStatus(Status.BAD_REQUEST)
            .withType(ErrorConstants.DEFAULT_TYPE)
            // .with(MESSAGE_KEY, ErrorConstants.ERR_VALIDATION)
            .with(MESSAGE_KEY, ErrorConstants.ERR_VALIDATION)
            .with(CODE_KEY, code)
            .with(CODE_TIME, ZonedDateTime.now())
            .with(PATH_KEY, request.getNativeRequest(HttpServletRequest.class).getRequestURI())
            .build();

        log.error("---TbsRunTimeException code: {}, title: {}, message: {}, stack: {}", code, problem.getTitle(), ErrorConstants.ERR_VALIDATION, ex.getStackTrace());
        Sentry.capture(ex);
        return ResponseEntity.badRequest()
            .body(problem);
    }

    @ExceptionHandler
    public ResponseEntity<Problem> paymentGatewayRuntime(PaymentGatewayException ex, NativeWebRequest request) {
        String code = RandomUtil.randomAlphaNumeric(6);

        Problem problem = Problem.builder()
            .withTitle(ex.getMessage())
            .withStatus(Status.INTERNAL_SERVER_ERROR)
            .withType(ErrorConstants.PAYMENT_PROVIDER_ISSUE_TYPE)
            // .with(MESSAGE_KEY, ErrorConstants.ERR_VALIDATION)
            .with(MESSAGE_KEY, ErrorConstants.ERR_PAYMENT_GATEWAY)
            .with(CODE_KEY, code)
            .with(CODE_TIME, ZonedDateTime.now())
            .with(PATH_KEY, request.getNativeRequest(HttpServletRequest.class).getRequestURI())
            .build();

        log.error("---PaymentGatewayException code: {}, title: {}, message: {}, stack: {}", code, problem.getTitle(), ErrorConstants.ERR_PAYMENT_GATEWAY, ex.getStackTrace());
        Sentry.capture(ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

    @ExceptionHandler
    public ResponseEntity<Problem> itemAlreadyUsedRuntime(ItemAlreadyUsedException ex, NativeWebRequest request) {
        String code = RandomUtil.randomAlphaNumeric(6);

        Problem problem = Problem.builder()
            .withTitle(ex.getMessage())
            .withStatus(Status.BAD_REQUEST)
            .withType(ErrorConstants.DEFAULT_TYPE)
            // .with(MESSAGE_KEY, ErrorConstants.ERR_VALIDATION)
            .with(MESSAGE_KEY, ErrorConstants.ERR_ITEM_ALREADY_USED)
            .with(CODE_KEY, code)
            .with(CODE_TIME, ZonedDateTime.now())
            .with(PATH_KEY, request.getNativeRequest(HttpServletRequest.class).getRequestURI())
            .build();

        log.error("---PaymentGatewayException code: {}, title: {}, message: {}, stack: {}", code, problem.getTitle(), ErrorConstants.ERR_PAYMENT_GATEWAY, ex.getStackTrace());
        // Sentry.capture(ex);
        return ResponseEntity.badRequest().body(problem);
    }

/*    @ExceptionHandler
    public ResponseEntity<Problem> exceptionRuntime(Exception ex, NativeWebRequest request) {
        String code = RandomUtil.randomAlphaNumeric(6);

        Problem problem = Problem.builder()
            .withTitle(ex.getMessage())
            .withStatus(Status.INTERNAL_SERVER_ERROR)
            .withType(ErrorConstants.INTERNAL_SERVER_ISSUE_TYPE)
            .with(MESSAGE_KEY, ErrorConstants.ERR_INTERNAL)
            .with(CODE_KEY, code)
            .with(CODE_TIME, ZonedDateTime.now())
            .with(PATH_KEY, request.getNativeRequest(HttpServletRequest.class).getRequestURI())
            .build();

        log.error("---Exception code: {}, title: {}, message: {}, stack: {}", code, problem.getTitle(), ErrorConstants.ERR_PAYMENT_GATEWAY, ex.getStackTrace());
        Sentry.capture(ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }*/

}
