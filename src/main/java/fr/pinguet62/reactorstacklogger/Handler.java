package fr.pinguet62.reactorstacklogger;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static fr.pinguet62.reactorstacklogger.Appender.appendCallStackToFlux;
import static fr.pinguet62.reactorstacklogger.Appender.appendCallStackToMono;
import static fr.pinguet62.reactorstacklogger.StackContext.KEY;

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
                .contextWrite(context -> {
                    CallStack currentCallStack = context.get(KEY);
                    callStack.set(currentCallStack);
                    return context;
                })
                .transform(appendCallStackToMono("<root>"))
                .doFinally(signalType -> handler.accept(callStack.get()));
    }

    public static <T> UnaryOperator<Flux<T>> doWithCallStackFlux(Consumer<CallStack> handler) {
        AtomicReference<CallStack> callStack = new AtomicReference<>();
        return flux -> flux
                .contextWrite(context -> {
                    CallStack currentCallStack = context.get(KEY);
                    callStack.set(currentCallStack);
                    return context;
                })
                .transform(appendCallStackToFlux("<root>"))
                .doFinally(signalType -> handler.accept(callStack.get()));
    }
}
