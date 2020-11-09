package fr.pinguet62.reactorstacklogger;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.UnaryOperator;

import static fr.pinguet62.reactorstacklogger.StackContext.KEY;
import static fr.pinguet62.reactorstacklogger.StackContext.getStack;
import static fr.pinguet62.reactorstacklogger.StackContext.withStack;
import static fr.pinguet62.reactorstacklogger.StopWatchUtils.doOnTerminateTimeFlux;
import static fr.pinguet62.reactorstacklogger.StopWatchUtils.doOnTerminateTimeMono;

public class Appender {

    public static <T> UnaryOperator<Mono<T>> appendCallStackToMono(String stackName) {
        return mono -> getStack()
                .flatMap(callStack -> mono
                        .transform(doOnTerminateTimeMono(callStack::setTime)))
                .subscriberContext(context -> {
                    Optional<CallStack> currentCallStack = context.getOrEmpty(KEY);
                    CallStack nextCallStack = new CallStack(stackName);
                    currentCallStack.ifPresent(current -> current.getChildren().add(nextCallStack));
                    return withStack(nextCallStack);
                });
    }

    public static <T> UnaryOperator<Flux<T>> appendCallStackToFlux(String stackName) {
        return flux -> getStack()
                .flatMapMany(callStack -> flux
                        .transform(doOnTerminateTimeFlux(callStack::setTime)))
                .subscriberContext(context -> {
                    Optional<CallStack> currentCallStack = context.getOrEmpty(KEY);
                    CallStack nextCallStack = new CallStack(stackName);
                    currentCallStack.ifPresent(current -> current.getChildren().add(nextCallStack));
                    return withStack(nextCallStack);
                });
    }
}
