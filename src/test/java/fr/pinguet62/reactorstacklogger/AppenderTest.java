package fr.pinguet62.reactorstacklogger;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;
import reactor.util.context.Context;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static fr.pinguet62.reactorstacklogger.Appender.appendCallStackToFlux;
import static fr.pinguet62.reactorstacklogger.Appender.appendCallStackToMono;
import static fr.pinguet62.reactorstacklogger.CallStack.Status.CANCELED;
import static fr.pinguet62.reactorstacklogger.CallStack.Status.ERROR;
import static fr.pinguet62.reactorstacklogger.CallStack.Status.SUCCESS;
import static fr.pinguet62.reactorstacklogger.TestUtils.match;
import static java.util.function.Predicate.isEqual;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class AppenderTest {
    @Nested
    class appendCallStackToMono {
        @Test
        void withSingleContext() {
            TestPublisher<String> testPublisher = TestPublisher.create();
            Mono<String> mono = testPublisher.mono()
                    .transform(appendCallStackToMono("single"));
            StepVerifier.create(mono)
                    .then(() -> testPublisher.emit("result").complete())
                    .expectNext("result")
                    .expectAccessibleContext()
                    .assertThat(context -> {
                        CallStack rootCallStack = context.get(StackContext.KEY);
                        assertThat(rootCallStack, match(is("single"), is(SUCCESS), notNullValue(Duration.class), is(empty())));
                    }).then()
                    .verifyComplete();
        }

        @Test
        void withMultipleContexts() {
            TestPublisher<String> testPublisher = TestPublisher.create();
            Mono<String> mono = testPublisher.mono()
                    .flatMap(it -> Mono.just("second")
                            .transform(appendCallStackToMono("child")))
                    .transform(appendCallStackToMono("parent"));
            StepVerifier.create(mono)
                    .then(() -> testPublisher.emit("first").complete())
                    .expectNext("second")
                    .expectAccessibleContext()
                    .assertThat(context -> {
                        CallStack rootCallStack = context.get(StackContext.KEY);
                        assertThat(rootCallStack, match(is("parent"), is(SUCCESS), notNullValue(Duration.class), contains(
                                match(is("child"), is(SUCCESS), notNullValue(Duration.class), is(empty())))));
                    }).then()
                    .verifyComplete();
        }

        @Test
        void shouldNotReplaceExistingContext() {
            Mono<String> mono = Mono.just("value")
                    .contextWrite(Context.of("first", "A"))
                    .transform(appendCallStackToMono("<root>"))
                    .contextWrite(Context.of("second", "B"));
            StepVerifier.create(mono)
                    .expectNext("value")
                    .expectAccessibleContext()
                    .assertThat(context -> {
                        assertThat(context.get("first"), is("A"));
                        assertThat(context.get(StackContext.KEY), match(is("<root>"), is(SUCCESS), notNullValue(Duration.class), is(empty())));
                        assertThat(context.get("second"), is("B"));
                    }).then()
                    .verifyComplete();
        }

        @Test
        void emptyPublisher() {
            TestPublisher<String> testPublisher = TestPublisher.create();
            Mono<String> mono = testPublisher.mono()
                    .transform(appendCallStackToMono("single"));
            StepVerifier.create(mono)
                    .then(testPublisher::complete)
                    .expectAccessibleContext()
                    .assertThat(context -> {
                        CallStack rootCallStack = context.get(StackContext.KEY);
                        assertThat(rootCallStack, match(is("single"), is(SUCCESS), notNullValue(Duration.class), is(empty())));
                    }).then()
                    .verifyComplete();
        }

        @Test
        void canceled() {
            AtomicReference<Subscription> subscription = new AtomicReference<>();
            Mono<String> mono = TestPublisher.<String>create().mono()
                    .transform(appendCallStackToMono("single"))
                    .doOnSubscribe(subscription::set);
            StepVerifier.create(mono)
                    .then(() -> subscription.get().cancel())
                    .expectAccessibleContext()
                    .assertThat(context -> {
                        CallStack rootCallStack = context.get(StackContext.KEY);
                        assertThat(rootCallStack, match(is("single"), is(CANCELED), notNullValue(Duration.class), is(empty())));
                    }).then()
                    .thenCancel()
                    .verify();
        }

        @Test
        void error() {
            Exception exception = new RuntimeException("Oups!");
            TestPublisher<String> testPublisher = TestPublisher.create();
            Mono<String> mono = testPublisher.mono()
                    .transform(appendCallStackToMono("single"));
            StepVerifier.create(mono)
                    .then(() -> testPublisher.error(exception).complete())
                    .expectAccessibleContext()
                    .assertThat(context -> {
                        CallStack rootCallStack = context.get(StackContext.KEY);
                        assertThat(rootCallStack, match(is("single"), is(ERROR), notNullValue(Duration.class), is(empty())));
                    }).then()
                    .verifyErrorMatches(isEqual(exception));
        }
    }

    @Nested
    class appendCallStackToFlux {
        @Test
        void withSingleContext() {
            Flux<String> flux = Flux.just("result")
                    .transform(appendCallStackToFlux("single"));
            StepVerifier.create(flux)
                    .expectNext("result")
                    .expectAccessibleContext()
                    .assertThat(context -> {
                        CallStack rootCallStack = context.get(StackContext.KEY);
                        assertThat(rootCallStack, match(is("single"), is(SUCCESS), notNullValue(Duration.class), is(empty())));
                    }).then()
                    .verifyComplete();
        }

        @Test
        void withMultipleContexts() {
            Flux<Integer> flux = Flux.just(1, 2)
                    .flatMap(nb -> Flux.just(nb).repeat(nb - 1)
                            .transform(appendCallStackToFlux("child-" + nb)))
                    .transform(appendCallStackToFlux("parent"));
            StepVerifier.create(flux)
                    .expectNext(1, 2, 2)
                    .expectAccessibleContext()
                    .assertThat(context -> {
                        CallStack rootCallStack = context.get(StackContext.KEY);
                        assertThat(rootCallStack, match(is("parent"), is(SUCCESS), notNullValue(Duration.class), contains(
                                match(is("child-1"), is(SUCCESS), notNullValue(Duration.class), is(empty())),
                                match(is("child-2"), is(SUCCESS), notNullValue(Duration.class), is(empty())))));
                    }).then()
                    .verifyComplete();
        }

        @Test
        void shouldNotReplaceExistingContext() {
            Flux<String> flux = Flux.just("result")
                    .contextWrite(Context.of("first", "A"))
                    .transform(appendCallStackToFlux("<root>"))
                    .contextWrite(Context.of("second", "B"));
            StepVerifier.create(flux)
                    .expectNext("result")
                    .expectAccessibleContext()
                    .assertThat(context -> {
                        assertThat(context.get("first"), is("A"));
                        assertThat(context.get(StackContext.KEY), match(is("<root>"), is(SUCCESS), notNullValue(Duration.class), is(empty())));
                        assertThat(context.get("second"), is("B"));
                    }).then()
                    .verifyComplete();
        }

        @Test
        void emptyPublisher() {
            TestPublisher<String> testPublisher = TestPublisher.create();
            Flux<String> flux = testPublisher.flux()
                    .transform(appendCallStackToFlux("single"));
            StepVerifier.create(flux)
                    .then(testPublisher::complete)
                    .expectAccessibleContext()
                    .assertThat(context -> {
                        CallStack rootCallStack = context.get(StackContext.KEY);
                        assertThat(rootCallStack, match(is("single"), is(SUCCESS), notNullValue(Duration.class), is(empty())));
                    }).then()
                    .verifyComplete();
        }

        @Test
        void canceled() {
            AtomicReference<Subscription> subscription = new AtomicReference<>();
            Flux<String> flux = TestPublisher.<String>create().flux()
                    .transform(appendCallStackToFlux("single"))
                    .doOnSubscribe(subscription::set);
            StepVerifier.create(flux)
                    .then(() -> subscription.get().cancel())
                    .expectAccessibleContext()
                    .assertThat(context -> {
                        CallStack rootCallStack = context.get(StackContext.KEY);
                        assertThat(rootCallStack, match(is("single"), is(CANCELED), notNullValue(Duration.class), is(empty())));
                    }).then()
                    .thenCancel()
                    .verify();
        }

        @Test
        void error() {
            Exception exception = new RuntimeException("Oups!");
            TestPublisher<String> testPublisher = TestPublisher.create();
            Flux<String> flux = testPublisher.flux()
                    .transform(appendCallStackToFlux("single"));
            StepVerifier.create(flux)
                    .then(() -> testPublisher.error(exception).complete())
                    .expectAccessibleContext()
                    .assertThat(context -> {
                        CallStack rootCallStack = context.get(StackContext.KEY);
                        assertThat(rootCallStack, match(is("single"), is(ERROR), notNullValue(Duration.class), is(empty())));
                    }).then()
                    .verifyErrorMatches(isEqual(exception));
        }
    }
}
