package fr.pinguet62.reactorstacklogger;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static fr.pinguet62.reactorstacklogger.Appender.appendCallStackToFlux;
import static fr.pinguet62.reactorstacklogger.Appender.appendCallStackToMono;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

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
                        assertThat(rootCallStack.getName(), is("single"));
                        assertThat(rootCallStack.getTime(), is(notNullValue()));
                        assertThat(rootCallStack.getChildren(), is(empty()));
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
                        assertThat(rootCallStack.getName(), is("parent"));
                        assertThat(rootCallStack.getTime(), is(notNullValue()));
                        assertThat(rootCallStack.getChildren(), hasSize(1));
                        {
                            CallStack firstCallStack = rootCallStack.getChildren().get(0);
                            assertThat(firstCallStack.getName(), is("child"));
                            assertThat(firstCallStack.getTime(), is(notNullValue()));
                            assertThat(firstCallStack.getChildren(), is(empty()));
                        }
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
                        assertThat(rootCallStack.getName(), is("single"));
                        assertThat(rootCallStack.getTime(), is(notNullValue()));
                        assertThat(rootCallStack.getChildren(), is(empty()));
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
                        assertThat(rootCallStack.getName(), is("parent"));
                        assertThat(rootCallStack.getTime(), is(notNullValue()));
                        assertThat(rootCallStack.getChildren(), hasSize(2));
                        {
                            CallStack child1CallStack = rootCallStack.getChildren().get(0);
                            assertThat(child1CallStack.getName(), is("child-1"));
                            assertThat(child1CallStack.getTime(), is(notNullValue()));
                            assertThat(child1CallStack.getChildren(), is(empty()));
                        }
                        {
                            CallStack child2CallStack = rootCallStack.getChildren().get(1);
                            assertThat(child2CallStack.getName(), is("child-2"));
                            assertThat(child2CallStack.getTime(), is(notNullValue()));
                            assertThat(child2CallStack.getChildren(), is(empty()));
                        }
                    }).then()
                    .verifyComplete();
        }
    }
}
