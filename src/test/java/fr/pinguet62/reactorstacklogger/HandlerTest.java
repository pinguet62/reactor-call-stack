package fr.pinguet62.reactorstacklogger;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static fr.pinguet62.reactorstacklogger.Appender.appendCallStackToFlux;
import static fr.pinguet62.reactorstacklogger.Appender.appendCallStackToMono;
import static fr.pinguet62.reactorstacklogger.CallStack.Status.CANCELED;
import static fr.pinguet62.reactorstacklogger.CallStack.Status.ERROR;
import static fr.pinguet62.reactorstacklogger.CallStack.Status.SUCCESS;
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
            Mono<String> mono = Mono.just("first");
            AtomicBoolean called = new AtomicBoolean(false);
            StepVerifier.create(mono.transform(doWithCallStackMono(rootCallStack -> {
                assertThat(rootCallStack, match(is("<root>"), is(SUCCESS), is(empty())));
                called.set(true);
            })))
                    .expectNext("first")
                    .verifyComplete();
            assertThat(called.get(), is(true));
        }

        @Test
        void parentCallInContext() {
            Mono<String> mono = Mono.just("first")
                    .flatMap(it -> Mono.just("second")
                            .transform(appendCallStackToMono("child")));
            AtomicBoolean called = new AtomicBoolean(false);
            StepVerifier.create(mono.transform(doWithCallStackMono(rootCallStack -> {
                assertThat(rootCallStack, match(
                        is("<root>"),
                        contains(
                                match(is("child"), is(empty())))));
                called.set(true);
            })))
                    .expectNext("second")
                    .verifyComplete();
            assertThat(called.get(), is(true));
        }

        @Test
        void canceled() {
            AtomicReference<Subscription> subscription = new AtomicReference<>();
            Mono<String> mono = TestPublisher.<String>create()
                    .mono()
                    .doOnSubscribe(subscription::set);
            AtomicBoolean called = new AtomicBoolean(false);
            StepVerifier.create(mono.transform(doWithCallStackMono(rootCallStack -> {
                assertThat(rootCallStack, match(is("<root>"), is(CANCELED), is(empty())));
                called.set(true);
            })))
                    .then(() -> subscription.get().cancel())
                    .thenCancel()
                    .verify();
            assertThat(called.get(), is(true));
        }

        @Test
        void error() {
            Exception exception = new RuntimeException("Oups!");
            Mono<String> mono = Mono.error(exception);
            AtomicBoolean called = new AtomicBoolean(false);
            StepVerifier.create(mono.transform(doWithCallStackMono(rootCallStack -> {
                assertThat(rootCallStack, match(is("<root>"), is(ERROR), is(empty())));
                called.set(true);
            })))
                    .verifyErrorMatches(isEqual(exception));
            assertThat(called.get(), is(true));
        }
    }

    @Nested
    class doWithCallStackFlux {
        @Test
        void emptyContext() {
            Flux<String> flux = Flux.just("first");
            AtomicInteger called = new AtomicInteger(0);
            StepVerifier.create(flux.transform(doWithCallStackFlux(rootCallStack -> {
                assertThat(rootCallStack, match(is("<root>"), is(SUCCESS), is(empty())));
                called.incrementAndGet();
            })))
                    .expectNext("first")
                    .verifyComplete();
            assertThat(called.get(), is(1));
        }

        @Test
        void parentCallInContext() {
            Flux<String> flux = Flux.just("first")
                    .flatMap(it -> Flux.just("second", "third")
                            .transform(appendCallStackToFlux("child")));
            AtomicInteger called = new AtomicInteger(0);
            StepVerifier.create(flux.transform(doWithCallStackFlux(rootCallStack -> {
                assertThat(rootCallStack, match(
                        is("<root>"),
                        contains(
                                match(is("child"), is(empty())))));
                called.incrementAndGet();
            })))
                    .expectNext("second", "third")
                    .verifyComplete();
            assertThat(called.get(), is(1));
        }

        @Test
        void canceled() {
            AtomicReference<Subscription> subscription = new AtomicReference<>();
            Flux<String> flux = TestPublisher.<String>create()
                    .flux()
                    .doOnSubscribe(subscription::set);
            AtomicInteger called = new AtomicInteger(0);
            StepVerifier.create(flux.transform(doWithCallStackFlux(rootCallStack -> {
                assertThat(rootCallStack, match(is("<root>"), is(CANCELED), is(empty())));
                called.incrementAndGet();
            })))
                    .then(() -> subscription.get().cancel())
                    .thenCancel()
                    .verify();
            assertThat(called.get(), is(1));
        }

        @Test
        void error() {
            Exception exception = new RuntimeException("Oups!");
            Flux<String> flux = Flux.error(exception);
            AtomicInteger called = new AtomicInteger(0);
            StepVerifier.create(flux.transform(doWithCallStackFlux(rootCallStack -> {
                assertThat(rootCallStack, match(is("<root>"), is(ERROR), is(empty())));
                called.incrementAndGet();
            })))
                    .verifyErrorMatches(isEqual(exception));
            assertThat(called.get(), is(1));
        }
    }
}
