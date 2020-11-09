package fr.pinguet62.reactorstacklogger;

import reactor.core.publisher.Mono;
import reactor.util.context.Context;

public class StackContext {

    static final Object KEY = StackContext.class;

    public static Mono<CallStack> getStack() {
        return Mono.subscriberContext()
                .filter(c -> c.hasKey(KEY))
                .map(c -> c.get(KEY));
    }

    public static Context withStack(CallStack value) {
        return Context.of(KEY, value);
    }
}
