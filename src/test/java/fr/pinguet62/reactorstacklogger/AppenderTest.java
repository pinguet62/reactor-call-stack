package fr.pinguet62.reactorstacklogger;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static fr.pinguet62.reactorstacklogger.Appender.appendCallStackToFlux;
import static fr.pinguet62.reactorstacklogger.Appender.appendCallStackToMono;
import static fr.pinguet62.reactorstacklogger.TestUtils.match;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

class AppenderTest {
    @Nested
    class appendCallStackToMono {
        @Test
        void withSingleContext() {
            Mono<String> mono = Mono.just("result")
                    .transform(appendCallStackToMono("single"));
            StepVerifier.create(mono)
                    .expectNext("result")
                    .expectAccessibleContext()
                    .assertThat(context -> {
                        CallStack rootCallStack = context.get(StackContext.KEY);
                        assertThat(rootCallStack, match(is("single"), is(empty())));
                    }).then()
                    .verifyComplete();
        }

        @Test
        void withMultipleContexts() {
            Mono<String> mono = Mono.just("first")
                    .flatMap(it -> Mono.just("second")
                            .transform(appendCallStackToMono("child")))
                    .transform(appendCallStackToMono("parent"));
            StepVerifier.create(mono)
                    .expectNext("second")
                    .expectAccessibleContext()
                    .assertThat(context -> {
                        CallStack rootCallStack = context.get(StackContext.KEY);
                        assertThat(rootCallStack, match(
                                is("parent"),
                                contains(
                                        match(is("child"), is(empty())))));
                    }).then()
                    .verifyComplete();
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
                        assertThat(rootCallStack, match(is("single"), is(empty())));
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
                        assertThat(rootCallStack, match(
                                is("parent"),
                                contains(
                                        match(is("child-1"), is(empty())),
                                        match(is("child-2"), is(empty())))));
                    }).then()
                    .verifyComplete();
        }
    }
}
