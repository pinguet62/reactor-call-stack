package fr.pinguet62.reactorstacklogger;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static fr.pinguet62.reactorstacklogger.Appender.appendCallStackToFlux;
import static fr.pinguet62.reactorstacklogger.Appender.appendCallStackToMono;
import static fr.pinguet62.reactorstacklogger.StackContext.getStack;

/**
 * Create a <i>root</i> {@link CallStack},
 * then execute handler on total time of target {@link Publisher}.
 * <p>
 * Can be used on global {@link org.springframework.web.server.WebFilter} to log request total execute, and stack calls.
 */
public class Handler {

    public static <T> UnaryOperator<Mono<T>> doWithCallStackMono(Consumer<CallStack> handler) {
        AtomicReference<CallStack> callStack = new AtomicReference<>();
        return mono -> getStack()
                .doOnNext(callStack::set)
                .then(mono)
                .transform(appendCallStackToMono("<root>"))
                .doOnTerminate(() -> handler.accept(callStack.get()));
    }

    public static <T> UnaryOperator<Flux<T>> doWithCallStackFlux(Consumer<CallStack> handler) {
        AtomicReference<CallStack> callStack = new AtomicReference<>();
        return flux -> getStack()
                .doOnNext(callStack::set)
                .thenMany(flux)
                .transform(appendCallStackToFlux("<root>"))
                .doOnTerminate(() -> handler.accept(callStack.get()));
    }
}
