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
    public Object interceptMethod(ProceedingJoinPoint proceedingJoinPoint, AppendCallStack appendCallStack) throws Throwable {
        String stackName = !appendCallStack.value().equals("")
                ? appendCallStack.value()
                : proceedingJoinPoint.getSignature().getDeclaringType().getSimpleName() + "." + proceedingJoinPoint.getSignature().getName();

        Class<?> methodSignature = ((MethodSignature) proceedingJoinPoint.getSignature()).getReturnType();
        if (methodSignature == Mono.class) {
            return ((Mono<?>) proceedingJoinPoint.proceed())
                    .transform(appendCallStackToMono(stackName));
        } else if (methodSignature == Flux.class) {
            return ((Flux<?>) proceedingJoinPoint.proceed())
                    .transform(appendCallStackToFlux(stackName));
        } else throw new UnsupportedOperationException("Not supported type: " + methodSignature);
    }

    @Around("within(@fr.pinguet62.reactorstacklogger.AppendCallStack *)")
    public Object interceptAllMethods(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        String stackName = proceedingJoinPoint.getSignature().getDeclaringType().getSimpleName() + "." + proceedingJoinPoint.getSignature().getName();

        Class<?> methodSignature = ((MethodSignature) proceedingJoinPoint.getSignature()).getReturnType();
        if (methodSignature == Mono.class) {
            return ((Mono<?>) proceedingJoinPoint.proceed())
                    .transform(appendCallStackToMono(stackName));
        } else if (methodSignature == Flux.class) {
            return ((Flux<?>) proceedingJoinPoint.proceed())
                    .transform(appendCallStackToFlux(stackName));
        } else throw new UnsupportedOperationException("Not supported type: " + methodSignature);
    }
}
