package dev.viniciusjmr.servicerequest.infrastructure.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Aspect
@Component
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditAspect.class);


    @Around("@annotation(audit)")
    public Object audit(ProceedingJoinPoint joinPoint, Audit audit) throws Throwable {
        long start = System.currentTimeMillis();

        String userId = getUserId();
        String role = getRole();
        String entityId = getEntityId(joinPoint, audit.entityIdParam());

        try {
            Object result = joinPoint.proceed();

            log.info("audit userId={} role={} action={} entityId={} durationMs={} status=success",
                    userId,
                    role,
                    audit.action(),
                    entityId,
                    System.currentTimeMillis() - start
            );

            return result;
        } catch (Throwable ex) {
            log.warn("audit userId={} role={} action={} entityId={} durationMs={} status=error error={}",
                    userId,
                    role,
                    audit.action(),
                    entityId,
                    System.currentTimeMillis() - start,
                    ex.getClass().getSimpleName()
            );

            throw ex;
        }
    }

    private String getUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return null;
        }

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }

        return authentication.getName();
    }

    private String getRole() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return null;
        }

        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }

    private String getEntityId(ProceedingJoinPoint joinPoint, String paramName) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameterNames.length; i++) {
            if (paramName.equals(parameterNames[i])) {
                return args[i] != null ? args[i].toString() : null;
            }
        }

        return null;
    }
}
