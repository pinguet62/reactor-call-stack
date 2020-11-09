package fr.pinguet62.reactorstacklogger;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

import static fr.pinguet62.reactorstacklogger.StopWatchUtils.doOnTerminateTimeFlux;
import static fr.pinguet62.reactorstacklogger.StopWatchUtils.doOnTerminateTimeMono;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class StopWatchUtilsTest {

    @Nested
    class doOnTerminateTimeMono {
        @Test
        void just() {
            Consumer<Duration> consumer = mock(Consumer.class);
            doNothing().when(consumer).accept(any());

            Mono<String> mono = Mono.just("value")
                    .transform(doOnTerminateTimeMono(consumer));

            StepVerifier.create(mono)
                    .expectNext("value")
                    .verifyComplete();
            verify(consumer).accept(any());
        }

        @Test
        void empty() {
            Consumer<Duration> consumer = mock(Consumer.class);
            doNothing().when(consumer).accept(any());

            Mono<Object> mono = Mono.empty()
                    .transform(doOnTerminateTimeMono(consumer));

            StepVerifier.create(mono)
                    .verifyComplete();
            verify(consumer).accept(any());
        }

        @Test
        void error() {
            Consumer<Duration> consumer = mock(Consumer.class);
            doNothing().when(consumer).accept(any());

            Mono<Object> mono = Mono.error(new IllegalArgumentException("test"))
                    .transform(doOnTerminateTimeMono(consumer));

            StepVerifier.create(mono)
                    .verifyError(IllegalArgumentException.class);
            verify(consumer).accept(any());
        }

        @Test
        void cancel() {
            Consumer<Duration> consumer = mock(Consumer.class);
            doNothing().when(consumer).accept(any());

            TestPublisher<Object> testPublisher = TestPublisher.create();

            MonoProcessor<Object> mono = testPublisher.mono()
                    .transform(doOnTerminateTimeMono(consumer))
                    .toProcessor();

            mono.subscribe();
            mono.cancel();
            verify(consumer).accept(any());
        }
    }

    @Nested
    class doOnTerminateTimeFlux {
        @Test
        void just() {
            Consumer<Duration> consumer = mock(Consumer.class);
            doNothing().when(consumer).accept(any());

            Flux<String> flux = Flux.just("first", "second", "third")
                    .transform(doOnTerminateTimeFlux(consumer));

            StepVerifier.create(flux)
                    .expectNext("first", "second", "third")
                    .verifyComplete();
            verify(consumer).accept(any());
        }

        @Test
        void empty() {
            Consumer<Duration> consumer = mock(Consumer.class);
            doNothing().when(consumer).accept(any());

            Flux<Object> flux = Flux.empty()
                    .transform(doOnTerminateTimeFlux(consumer));

            StepVerifier.create(flux)
                    .verifyComplete();
            verify(consumer).accept(any());
        }

        @Test
        void error() {
            Consumer<Duration> consumer = mock(Consumer.class);
            doNothing().when(consumer).accept(any());

            Flux<Object> flux = TestPublisher.create()
                    .emit("first")
                    .error(new IllegalArgumentException("test"))
                    .emit("second")
                    .flux()
                    .transform(doOnTerminateTimeFlux(consumer));

            StepVerifier.create(flux)
                    .verifyError(IllegalArgumentException.class);
            verify(consumer).accept(any());
        }

        @Test
        void cancel() {
            Consumer<Duration> consumer = mock(Consumer.class);
            doNothing().when(consumer).accept(any());

            TestPublisher<Object> testPublisher = TestPublisher.create();

            MonoProcessor<List<Object>> mono = testPublisher.flux()
                    .transform(doOnTerminateTimeFlux(consumer))
                    .collectList()
                    .toProcessor();

            mono.subscribe();
            mono.cancel();
            verify(consumer).accept(any());
        }
    }
}
