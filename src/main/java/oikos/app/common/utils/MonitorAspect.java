package oikos.app.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/** Created by Mohamed Haamdi on 26/06/2021 */
@Component
@Aspect
@Slf4j
public class MonitorAspect {
  @Around("execution(* (@oikos.app.common.utils.Monitor *).*(..))")
  public Object beforeYourAnnotation(ProceedingJoinPoint joinPoint) throws Throwable {
    var start = Instant.now();
    Object proceed = joinPoint.proceed();
    var end = Instant.now();
    final var methodSignature = (MethodSignature) joinPoint.getSignature();
    final var method = methodSignature.getMethod();
    log.info(
        "{}-{}: {} ms",
        method.getDeclaringClass().getName(),
        method.getName(),
        Duration.between(start, end).toMillis());
    return proceed;
  }
}
