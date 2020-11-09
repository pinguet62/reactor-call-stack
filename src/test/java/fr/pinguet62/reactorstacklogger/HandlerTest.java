package fr.pinguet62.reactorstacklogger;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static fr.pinguet62.reactorstacklogger.Appender.appendCallStackToFlux;
import static fr.pinguet62.reactorstacklogger.Appender.appendCallStackToMono;
import static fr.pinguet62.reactorstacklogger.Handler.doWithCallStackFlux;
import static fr.pinguet62.reactorstacklogger.Handler.doWithCallStackMono;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class HandlerTest {
    @Nested
    class doWithCallStackMono {
        @Test
        void emptyContext() {
            AtomicBoolean called = new AtomicBoolean(false);
            Mono<String> mono = Mono.just("first");
            StepVerifier.create(mono.transform(doWithCallStackMono(rootCallStack -> {
                called.set(true);
                assertThat(rootCallStack.getName(), is("<root>"));
                assertThat(rootCallStack.getTime(), is(notNullValue()));
                assertThat(rootCallStack.getChildren(), is(empty()));
            })))
                    .expectNext("first")
                    .verifyComplete();
            assertThat(called.get(), is(true));
        }

        @Test
        void parentCallInContext() {
            AtomicBoolean called = new AtomicBoolean(false);
            Mono<String> mono = Mono.just("first")
                    .flatMap(it -> Mono.just("second")
                            .transform(appendCallStackToMono("child")));
            StepVerifier.create(mono.transform(doWithCallStackMono(rootCallStack -> {
                called.set(true);
                assertThat(rootCallStack.getName(), is("<root>"));
                assertThat(rootCallStack.getTime(), is(notNullValue()));
                assertThat(rootCallStack.getChildren(), hasSize(1));
                {
                    CallStack childCallStack = rootCallStack.getChildren().get(0);
                    assertThat(childCallStack.getName(), is("child"));
                    assertThat(childCallStack.getTime(), is(notNullValue()));
                    assertThat(childCallStack.getChildren(), is(empty()));
                }
            })))
                    .expectNext("second")
                    .verifyComplete();
            assertThat(called.get(), is(true));
        }
    }

    @Nested
    class doWithCallStackFlux {
        @Test
        void emptyContext() {
            AtomicInteger called = new AtomicInteger(0);
            Flux<String> flux = Flux.just("first");
            StepVerifier.create(flux.transform(doWithCallStackFlux(rootCallStack -> {
                called.incrementAndGet();
                assertThat(rootCallStack.getName(), is("<root>"));
                assertThat(rootCallStack.getTime(), is(notNullValue()));
                assertThat(rootCallStack.getChildren(), is(empty()));
            })))
                    .expectNext("first")
                    .verifyComplete();
            assertThat(called.get(), is(1));
        }

        @Test
        void parentCallInContext() {
            AtomicInteger called = new AtomicInteger(0);
            Flux<String> flux = Flux.just("first")
                    .flatMap(it -> Flux.just("second", "third")
                            .transform(appendCallStackToFlux("child")));
            StepVerifier.create(flux.transform(doWithCallStackFlux(rootCallStack -> {
                called.incrementAndGet();
                assertThat(rootCallStack.getName(), is("<root>"));
                assertThat(rootCallStack.getTime(), is(notNullValue()));
                assertThat(rootCallStack.getChildren(), hasSize(1));
                {
                    CallStack childCallStack = rootCallStack.getChildren().get(0);
                    assertThat(childCallStack.getName(), is("child"));
                    assertThat(childCallStack.getTime(), is(notNullValue()));
                    assertThat(childCallStack.getChildren(), is(empty()));
                }
            })))
                    .expectNext("second", "third")
                    .verifyComplete();
            assertThat(called.get(), is(1));
        }
    }
}
