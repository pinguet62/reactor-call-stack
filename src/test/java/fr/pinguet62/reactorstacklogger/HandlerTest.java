package fr.pinguet62.reactorstacklogger;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
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
import static org.hamcrest.Matchers.notNullValue;

class HandlerTest {
    @Nested
    class doWithCallStackMono {
        @Test
        void emptyContext() {
            Mono<String> mono = Mono.just("first");
            AtomicInteger called = new AtomicInteger(0);
            StepVerifier.create(mono.transform(doWithCallStackMono(rootCallStack -> {
                assertThat(rootCallStack, match(is("<root>"), is(SUCCESS), notNullValue(Duration.class), is(empty())));
                called.incrementAndGet();
            })))
                    .expectNext("first")
                    .verifyComplete();
            assertThat(called.get(), is(1));
        }

        @Test
        void parentCallInContext() {
            Mono<String> mono = Mono.just("first")
                    .flatMap(it -> Mono.just("second")
                            .transform(appendCallStackToMono("child")));
            AtomicInteger called = new AtomicInteger(0);
            StepVerifier.create(mono.transform(doWithCallStackMono(rootCallStack -> {
                assertThat(rootCallStack, match(is("<root>"), is(SUCCESS), notNullValue(Duration.class), contains(
                        match(is("child"), is(SUCCESS), notNullValue(Duration.class), is(empty())))));
                called.incrementAndGet();
            })))
                    .expectNext("second")
                    .verifyComplete();
            assertThat(called.get(), is(1));
        }

        @Test
        void canceled() {
            AtomicReference<Subscription> subscription = new AtomicReference<>();
            Mono<String> mono = TestPublisher.<String>create()
                    .mono()
                    .doOnSubscribe(subscription::set);
            AtomicInteger called = new AtomicInteger(0);
            StepVerifier.create(mono.transform(doWithCallStackMono(rootCallStack -> {
                assertThat(rootCallStack, match(is("<root>"), is(CANCELED), notNullValue(Duration.class), is(empty())));
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
            TestPublisher<String> testPublisher = TestPublisher.create();
            Mono<String> mono = testPublisher.mono();
            AtomicInteger called = new AtomicInteger(0);
            StepVerifier.create(mono.transform(doWithCallStackMono(rootCallStack -> {
                assertThat(rootCallStack, match(is("<root>"), is(ERROR), notNullValue(Duration.class), is(empty())));
                called.incrementAndGet();
            })))
                    .then(() -> testPublisher.error(exception))
                    .verifyErrorMatches(isEqual(exception));
            assertThat(called.get(), is(1));
        }
    }

    @Nested
    class doWithCallStackFlux {
        @Test
        void emptyContext() {
            Flux<String> flux = Flux.just("first");
            AtomicInteger called = new AtomicInteger(0);
            StepVerifier.create(flux.transform(doWithCallStackFlux(rootCallStack -> {
                assertThat(rootCallStack, match(is("<root>"), is(SUCCESS), notNullValue(Duration.class), is(empty())));
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
                assertThat(rootCallStack, match(is("<root>"), is(SUCCESS), notNullValue(Duration.class), contains(
                        match(is("child"), is(SUCCESS), notNullValue(Duration.class), is(empty())))));
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
                assertThat(rootCallStack, match(is("<root>"), is(CANCELED), notNullValue(Duration.class), is(empty())));
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
            TestPublisher<String> testPublisher = TestPublisher.create();
            Flux<String> flux = testPublisher.flux();
            AtomicInteger called = new AtomicInteger(0);
            StepVerifier.create(flux.transform(doWithCallStackFlux(rootCallStack -> {
                assertThat(rootCallStack, match(is("<root>"), is(ERROR), notNullValue(Duration.class), is(empty())));
                called.incrementAndGet();
            })))
                    .then(() -> testPublisher.error(exception))
                    .verifyErrorMatches(isEqual(exception));
            assertThat(called.get(), is(1));
        }
    }
}
