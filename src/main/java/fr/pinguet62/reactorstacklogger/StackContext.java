package fr.pinguet62.reactorstacklogger;

import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

public class StackContext {

    static final Object KEY = StackContext.class;

    public static Mono<CallStack> getStack() {
        return Mono.deferContextual(c -> Mono.just(c.get(KEY)));
    }

    public static ContextView withStack(CallStack value) {
        return Context.of(KEY, value);
    }
}
