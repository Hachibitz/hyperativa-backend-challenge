package br.com.hyperativa.api.aop;

import br.com.hyperativa.api.util.MaskingUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    /**
     * Define um pointcut que corresponde a qualquer método anotado com @Loggable.
     */
    @Pointcut("@annotation(br.com.hyperativa.api.aop.Loggable)")
    public void loggableMethod() {

    }

    /**
     * Advice do tipo "Around" que envolve a execução do método anotado.
     * Ele permite logar antes e depois da execução do método.
     */
    @Around("loggableMethod()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        String arguments = Arrays.stream(joinPoint.getArgs())
                .map(MaskingUtil::maskObject)
                .collect(Collectors.joining(", "));

        log.info("==> ENTER: {} with args=[{}]", methodName, arguments);

        try {
            Object result = joinPoint.proceed();
            log.info("<== EXIT: {} with result=[{}]", methodName, MaskingUtil.maskObject(result));
            return result;
        } catch (Exception e) {
            log.error("<== EXCEPTION in {}: {}", methodName, e.getMessage());
            throw e;
        }
    }
}