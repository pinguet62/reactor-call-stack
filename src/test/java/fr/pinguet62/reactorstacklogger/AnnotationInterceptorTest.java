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

import static fr.pinguet62.reactorstacklogger.AnnotationInterceptorTest.SampleComponent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
        AnnotationInterceptor.class,
        SampleComponent.class,
})
class AnnotationInterceptorTest {

    @TestComponent
    static class SampleComponent {
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
    }

    @Autowired
    SampleComponent component;

    @Test
    void mono() {
        StepVerifier.create(component.testMono())
                .expectNext("value")
                .expectAccessibleContext()
                .assertThat(context -> {
                    CallStack callStack = context.get(StackContext.KEY);
                    assertThat(callStack.getName(), is("mono"));
                    assertThat(callStack.getTime(), is(notNullValue()));
                    assertThat(callStack.getChildren(), is(empty()));
                })
                .then()
                .verifyComplete();
    }

    @Test
    void flux() {
        StepVerifier.create(component.testFlux())
                .expectNext("first", "second")
                .expectAccessibleContext()
                .assertThat(context -> {
                    CallStack callStack = context.get(StackContext.KEY);
                    assertThat(callStack.getName(), is("flux"));
                    assertThat(callStack.getTime(), is(notNullValue()));
                    assertThat(callStack.getChildren(), is(empty()));
                })
                .then()
                .verifyComplete();
    }

    @Test
    void unsupportedReturnType() {
        assertThrows(RuntimeException.class, () -> component.testUnsupportedReturnType());
    }
}
