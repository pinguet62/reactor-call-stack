package fr.pinguet62.reactorstacklogger;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static fr.pinguet62.reactorstacklogger.Appender.appendCallStackToFlux;
import static fr.pinguet62.reactorstacklogger.Appender.appendCallStackToMono;

@EnableAspectJAutoProxy
@Component
@Aspect
public class AnnotationInterceptor {

    @Around("@annotation(appendCallStack)")
    public Object intercept(ProceedingJoinPoint proceedingJoinPoint, AppendCallStack appendCallStack) throws Throwable {
        Class<?> methodSignature = ((MethodSignature) proceedingJoinPoint.getSignature()).getReturnType();
        if (methodSignature == Mono.class) {
            return ((Mono<?>) proceedingJoinPoint.proceed())
                    .transform(appendCallStackToMono(appendCallStack.value()));
        } else if (methodSignature == Flux.class) {
            return ((Flux<?>) proceedingJoinPoint.proceed())
                    .transform(appendCallStackToFlux(appendCallStack.value()));
        } else throw new UnsupportedOperationException("Not supported type: " + methodSignature);
    }

}
