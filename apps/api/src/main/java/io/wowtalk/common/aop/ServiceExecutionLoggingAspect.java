package io.wowtalk.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ServiceExecutionLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ServiceExecutionLoggingAspect.class);

    private final long slowServiceThresholdMillis;

    public ServiceExecutionLoggingAspect(
            @Value("${wowtalk.observability.slow-service-threshold-ms:500}") long slowServiceThresholdMillis
    ) {
        this.slowServiceThresholdMillis = slowServiceThresholdMillis;
    }

    @Around("execution(public * io.wowtalk..service..*(..))")
    public Object logSlowServiceExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long startedAt = System.nanoTime();
        try {
            return joinPoint.proceed();
        } finally {
            long elapsedMillis = (System.nanoTime() - startedAt) / 1_000_000;
            if (elapsedMillis >= slowServiceThresholdMillis) {
                log.warn(
                        "slow_service method={} elapsedMs={} thresholdMs={}",
                        joinPoint.getSignature().toShortString(),
                        elapsedMillis,
                        slowServiceThresholdMillis
                );
            }
        }
    }
}
