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
import static fr.pinguet62.reactorstacklogger.TestUtils.match;
import static java.util.function.Predicate.isEqual;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

class HandlerTest {
    @Nested
    class doWithCallStackMono {
        @Test
        void emptyContext() {
            AtomicBoolean called = new AtomicBoolean(false);
            Mono<String> mono = Mono.just("first");
            StepVerifier.create(mono.transform(doWithCallStackMono(rootCallStack -> {
                called.set(true);
                assertThat(rootCallStack, match(is("<root>"), is(empty())));
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
                assertThat(rootCallStack, match(
                        is("<root>"),
                        contains(
                                match(is("child"), is(empty())))));
            })))
                    .expectNext("second")
                    .verifyComplete();
            assertThat(called.get(), is(true));
        }

        @Test
        void error() {
            AtomicBoolean called = new AtomicBoolean(false);

            Exception exception = new RuntimeException("Oups!");
            Mono<String> mono = Mono.error(exception);

            StepVerifier.create(mono.transform(doWithCallStackMono(rootCallStack -> {
                called.set(true);
                assertThat(rootCallStack, match(is("<root>"), is(empty())));
            })))
                    .verifyErrorMatches(isEqual(exception));
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
                assertThat(rootCallStack, match(is("<root>"), is(empty())));
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
                assertThat(rootCallStack, match(
                        is("<root>"),
                        contains(
                                match(is("child"), is(empty())))));
            })))
                    .expectNext("second", "third")
                    .verifyComplete();
            assertThat(called.get(), is(1));
        }

        @Test
        void error() {
            AtomicInteger called = new AtomicInteger(0);

            Exception exception = new RuntimeException("Oups!");
            Flux<String> flux = Flux.error(exception);

            StepVerifier.create(flux.transform(doWithCallStackFlux(rootCallStack -> {
                called.incrementAndGet();
                assertThat(rootCallStack, match(is("<root>"), is(empty())));
            })))
                    .verifyErrorMatches(isEqual(exception));
            assertThat(called.get(), is(1));
        }
    }
}
