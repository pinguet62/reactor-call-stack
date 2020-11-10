package fr.pinguet62.reactorstacklogger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static fr.pinguet62.reactorstacklogger.AnnotationInterceptorTest.*;
import static fr.pinguet62.reactorstacklogger.TestUtils.match;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        AnnotationInterceptor.class,
        SampleMethodsComponent.class,
        SampleClassComponent.class,
})
class AnnotationInterceptorTest {

    @TestComponent
    static class SampleMethodsComponent {
        @AppendCallStack("mono")
        Mono<String> testMono() {
            return Mono.just("value");
        }

        @AppendCallStack("flux")
        Flux<String> testFlux() {
            return Flux.just("first", "second");
        }

        @AppendCallStack("unsupported")
        String testUnsupportedReturnType() {
            return "any";
        }

        @AppendCallStack
        Mono<String> testDetermineName() {
            return Mono.just("any");
        }
    }

    @TestComponent
    @AppendCallStack
    static class SampleClassComponent {
        Mono<String> test() {
            return Mono.just("value");
        }
    }

    @Autowired
    SampleMethodsComponent methodsComponent;

    @Autowired
    SampleClassComponent classComponent;

    @Test
    void mono() {
        StepVerifier.create(methodsComponent.testMono())
                .expectNext("value")
                .expectAccessibleContext()
                .assertThat(context -> {
                    CallStack callStack = context.get(StackContext.KEY);
                    assertThat(callStack, match(is("mono"), is(empty())));
                })
                .then()
                .verifyComplete();
    }

    @Test
    void flux() {
        StepVerifier.create(methodsComponent.testFlux())
                .expectNext("first", "second")
                .expectAccessibleContext()
                .assertThat(context -> {
                    CallStack callStack = context.get(StackContext.KEY);
                    assertThat(callStack, match(is("flux"), is(empty())));
                })
                .then()
                .verifyComplete();
    }

    @Test
    void unsupportedReturnType() {
        assertThrows(RuntimeException.class, () -> methodsComponent.testUnsupportedReturnType());
    }

    @Test
    void determineName() {
        StepVerifier.create(methodsComponent.testDetermineName())
                .expectNext("any")
                .expectAccessibleContext()
                .assertThat(context -> {
                    CallStack callStack = context.get(StackContext.KEY);
                    assertThat(callStack, match(is("SampleMethodsComponent.testDetermineName"), is(empty())));
                })
                .then()
                .verifyComplete();
    }

    @Test
    void annotatedClass() {
        StepVerifier.create(classComponent.test())
                .expectNext("value")
                .expectAccessibleContext()
                .assertThat(context -> {
                    CallStack callStack = context.get(StackContext.KEY);
                    assertThat(callStack, match(is("SampleClassComponent.test"), is(empty())));
                })
                .then()
                .verifyComplete();
    }
}
