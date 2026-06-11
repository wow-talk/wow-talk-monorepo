package io.wowtalk.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceExecutionLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ServiceExecutionLoggingAspect.class);
    private static final long SLOW_SERVICE_THRESHOLD_MILLIS = 500L;

    @Around("execution(public * io.wowtalk..service..*(..))")
    public Object logSlowServiceExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long startedAt = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000;
            if (elapsedMillis >= SLOW_SERVICE_THRESHOLD_MILLIS) {
                log.warn(
                        "slow_service method={} elapsedMs={}",
                        joinPoint.getSignature().toShortString(),
                        elapsedMillis
                );
            }
        }
    }
}
