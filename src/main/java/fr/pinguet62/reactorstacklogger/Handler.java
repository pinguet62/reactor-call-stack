package fr.pinguet62.reactorstacklogger;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static fr.pinguet62.reactorstacklogger.Appender.appendCallStackToFlux;
import static fr.pinguet62.reactorstacklogger.Appender.appendCallStackToMono;
import static fr.pinguet62.reactorstacklogger.CallStack.Status.CANCELED;
import static fr.pinguet62.reactorstacklogger.CallStack.Status.ERROR;
import static fr.pinguet62.reactorstacklogger.CallStack.Status.SUCCESS;
import static fr.pinguet62.reactorstacklogger.StackContext.KEY;
import static fr.pinguet62.reactorstacklogger.StackContext.getStack;

/**
 * Create a <i>root</i> {@link CallStack},
 * then execute handler on total time of target {@link Publisher}.
 * <p>
 * Can be used on global {@code org.springframework.web.server.WebFilter} to log request total execute, and stack calls.
 */
public class Handler {

    public static <T> UnaryOperator<Mono<T>> doWithCallStackMono(Consumer<CallStack> handler) {
        AtomicReference<CallStack> callStack = new AtomicReference<>();
        return mono -> mono
                .doOnCancel(() -> handler.accept(callStack.get()))
                .flatMap(value ->
                        getStack()
                                .doOnNext(callStack::set)
                                .thenReturn(value))
                .switchIfEmpty(Mono.defer(() ->
                        getStack()
                                .doOnNext(callStack::set)
                                .then(Mono.empty())))
                .doOnCancel(() -> callStack.get().setStatus(CANCELED))
                .onErrorResume(error ->
                        getStack()
                                .doOnNext(cs -> cs.setStatus(ERROR))
                                .doOnNext(callStack::set)
                                .then(Mono.error(error)))
                .contextWrite(context -> {
                    CallStack currentCallStack = context.get(KEY);
                    callStack.set(currentCallStack);
                    return context;
                })
                .transform(appendCallStackToMono("<root>"))
                .doOnTerminate(() -> handler.accept(callStack.get()));
    }

    public static <T> UnaryOperator<Flux<T>> doWithCallStackFlux(Consumer<CallStack> handler) {
        AtomicReference<CallStack> callStack = new AtomicReference<>();
        return flux -> flux
                .doOnCancel(() -> handler.accept(callStack.get()))
                .collectList()
                .flatMapMany(result ->
                        getStack()
                                .doOnNext(cs -> cs.setStatus(SUCCESS))
                                .doOnNext(callStack::set)
                                .thenMany(Flux.fromIterable(result)))
                .doOnCancel(() -> callStack.get().setStatus(CANCELED))
                .onErrorResume(error ->
                        getStack()
                                .doOnNext(cs -> cs.setStatus(ERROR))
                                .doOnNext(callStack::set)
                                .then(Mono.error(error)))
                .contextWrite(context -> {
                    CallStack currentCallStack = context.get(KEY);
                    callStack.set(currentCallStack);
                    return context;
                })
                .transform(appendCallStackToFlux("<root>"))
                .doOnTerminate(() -> handler.accept(callStack.get()));
    }
}
