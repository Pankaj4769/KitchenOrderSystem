package com.kos.aspect;

import java.util.Set;
import java.util.stream.Collectors;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Centralised entry / exit / error logging for every method in
 * {@code com.kos.service.*}. Adds uniform tracing without polluting each
 * service with boilerplate {@code logger.info} calls.
 *
 * <p>Argument logging is deliberately conservative: each argument's class name
 * is emitted, and any argument whose parameter name matches a sensitive token
 * ({@code password}, {@code pwd}, {@code otp}, {@code token}, {@code secret})
 * is replaced with {@code [REDACTED]}. Return values are <em>not</em> logged at
 * INFO to avoid leaking sensitive fields nested inside DTOs; only the boolean
 * "completed successfully" outcome is reported.
 */
@Aspect
@Component
@Profile({"local", "dev"})
public class ServiceLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(ServiceLoggingAspect.class);

    private static final Set<String> SENSITIVE_PARAM_NAMES = Set.of(
            "password", "pwd", "newpassword", "oldpassword", "temppassword",
            "otp", "code", "token", "secret", "apikey", "authtoken"
    );

    @Pointcut("execution(public * com.kos.service..*(..))")
    public void anyServiceMethod() {}

    @Around("anyServiceMethod()")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        String klass = signature.getDeclaringType().getSimpleName();
        String method = signature.getName();

        // Per-class logger so log lines are attributed to the service, not the aspect.
        Logger targetLogger = LoggerFactory.getLogger(signature.getDeclaringType());

        if (targetLogger.isInfoEnabled()) {
            targetLogger.info("[ENTER] {}.{}({})", klass, method, summariseArgs(signature, pjp.getArgs()));
        }

        long started = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            if (targetLogger.isDebugEnabled()) {
                targetLogger.debug("[EXIT ] {}.{} completed in {}ms", klass, method, System.currentTimeMillis() - started);
            }
            return result;
        } catch (Throwable ex) {
            targetLogger.error("[ERROR] {}.{} failed after {}ms: {}",
                    klass, method, System.currentTimeMillis() - started, ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * Build a compact, safe rendering of the method's argument list.
     * Sensitive-named parameters are replaced with [REDACTED].
     */
    private String summariseArgs(MethodSignature signature, Object[] args) {
        if (args == null || args.length == 0) return "";
        String[] names = signature.getParameterNames();
        return java.util.stream.IntStream.range(0, args.length)
                .mapToObj(i -> {
                    String name = (names != null && i < names.length) ? names[i] : ("arg" + i);
                    if (isSensitive(name)) return name + "=[REDACTED]";
                    return name + "=" + describe(args[i]);
                })
                .collect(Collectors.joining(", "));
    }

    private boolean isSensitive(String paramName) {
        if (paramName == null) return false;
        return SENSITIVE_PARAM_NAMES.contains(paramName.toLowerCase());
    }

    /**
     * Argument renderer that prints scalars verbatim but DTO-like objects by
     * class name only. Prevents accidentally serialising a whole entity that
     * contains a password field.
     */
    private String describe(Object arg) {
        if (arg == null) return "null";
        if (arg instanceof CharSequence || arg instanceof Number || arg instanceof Boolean
                || arg instanceof Enum<?> || arg instanceof java.time.temporal.Temporal
                || arg instanceof java.util.Date) {
            return String.valueOf(arg);
        }
        if (arg.getClass().isArray()) {
            return "[" + arg.getClass().getComponentType().getSimpleName() + "[" + java.lang.reflect.Array.getLength(arg) + "]]";
        }
        if (arg instanceof java.util.Collection<?> c) {
            return "[" + arg.getClass().getSimpleName() + "(size=" + c.size() + ")]";
        }
        if (arg instanceof java.util.Map<?, ?> m) {
            return "{" + arg.getClass().getSimpleName() + "(size=" + m.size() + ")}";
        }
        return "<" + arg.getClass().getSimpleName() + ">";
    }
}
