package fr.pinguet62.reactorstacklogger;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

import static fr.pinguet62.reactorstacklogger.StackContext.KEY;
import static fr.pinguet62.reactorstacklogger.StackContext.getStack;
import static fr.pinguet62.reactorstacklogger.StackContext.withStack;
import static fr.pinguet62.reactorstacklogger.StopWatchUtils.doOnTerminateTimeFlux;
import static fr.pinguet62.reactorstacklogger.StopWatchUtils.doOnTerminateTimeMono;

public class Appender {

    public static <T> UnaryOperator<Mono<T>> appendCallStackToMono(String stackName) {
        AtomicReference<CallStack> callStack = new AtomicReference<>();
        return mono -> mono
                .flatMap(value ->
                        getStack()
                                .doOnNext(callStack::set)
                                .thenReturn(value))
                .switchIfEmpty(Mono.defer(() ->
                        getStack()
                                .doOnNext(callStack::set)
                                .then(Mono.empty())))
                .transform(doOnTerminateTimeMono(time -> callStack.get().setTime(time)))
                .subscriberContext(context -> {
                    Optional<CallStack> currentCallStack = context.getOrEmpty(KEY);
                    CallStack nextCallStack = new CallStack(stackName);
                    currentCallStack.ifPresent(current -> current.getChildren().add(nextCallStack));
                    return context.putAll(withStack(nextCallStack));
                });
    }

    public static <T> UnaryOperator<Flux<T>> appendCallStackToFlux(String stackName) {
        AtomicReference<CallStack> callStack = new AtomicReference<>();
        return flux -> flux
                .collectList()
                .flatMapMany(result ->
                        getStack()
                                .doOnNext(callStack::set)
                                .thenMany(Flux.fromIterable(result)))
                .transform(doOnTerminateTimeFlux(time -> callStack.get().setTime(time)))
                .subscriberContext(context -> {
                    Optional<CallStack> currentCallStack = context.getOrEmpty(KEY);
                    CallStack nextCallStack = new CallStack(stackName);
                    currentCallStack.ifPresent(current -> current.getChildren().add(nextCallStack));
                    return context.putAll(withStack(nextCallStack));
                });
    }
}
