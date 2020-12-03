package fr.pinguet62.reactorstacklogger;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.UnaryOperator;

import static fr.pinguet62.reactorstacklogger.CallStack.Status.CANCELED;
import static fr.pinguet62.reactorstacklogger.CallStack.Status.ERROR;
import static fr.pinguet62.reactorstacklogger.CallStack.Status.SUCCESS;
import static fr.pinguet62.reactorstacklogger.StackContext.KEY;
import static fr.pinguet62.reactorstacklogger.StackContext.withStack;
import static fr.pinguet62.reactorstacklogger.StopWatchUtils.doOnTerminateTimeFlux;
import static fr.pinguet62.reactorstacklogger.StopWatchUtils.doOnTerminateTimeMono;

public class Appender {

    public static <T> UnaryOperator<Mono<T>> appendCallStackToMono(String stackName) {
        CallStack callStack = new CallStack(stackName);
        return mono -> mono
                .doOnCancel(() -> callStack.setStatus(CANCELED))
                .doOnSuccess(nextOrNull -> callStack.setStatus(SUCCESS))
                .doOnError(error -> callStack.setStatus(ERROR))
                .transform(doOnTerminateTimeMono(time -> callStack.setTime(time)))
                .contextWrite(context -> {
                    Optional<CallStack> currentCallStack = context.getOrEmpty(KEY);
                    currentCallStack.ifPresent(current -> current.getChildren().add(callStack));
                    return context.putAll(withStack(callStack));
                });
    }

    public static <T> UnaryOperator<Flux<T>> appendCallStackToFlux(String stackName) {
        CallStack callStack = new CallStack(stackName);
        callStack.setStatus(SUCCESS); // TODO partial?
        return flux -> flux
                .doOnCancel(() -> callStack.setStatus(CANCELED))
                .doOnError(error -> callStack.setStatus(ERROR))
                .transform(doOnTerminateTimeFlux(time -> callStack.setTime(time)))
                .contextWrite(context -> {
                    Optional<CallStack> currentCallStack = context.getOrEmpty(KEY);
                    currentCallStack.ifPresent(current -> current.getChildren().add(callStack));
                    return context.putAll(withStack(callStack));
                });
    }
}
