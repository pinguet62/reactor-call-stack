package fr.pinguet62.reactorstacklogger;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static fr.pinguet62.reactorstacklogger.StopWatchUtils.doOnTerminateTimeFlux;
import static fr.pinguet62.reactorstacklogger.StopWatchUtils.doOnTerminateTimeMono;
import static java.time.Duration.ofMillis;
import static java.time.Instant.now;
import static java.time.ZoneOffset.UTC;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static reactor.test.publisher.TestPublisher.create;

class StopWatchUtilsTest {

    @Nested
    class doOnTerminateTimeMono {
        @Test
        void just() {
            Consumer<Duration> consumer = mock(Consumer.class);
            doNothing().when(consumer).accept(any());

            AtomicReference<Clock> clock = new AtomicReference<>(Clock.fixed(now(), UTC));

            TestPublisher<String> testPublisher = create();
            Mono<String> mono = testPublisher.mono()
                    .transform(doOnTerminateTimeMono(clock::get, consumer));

            Duration duration = ofMillis(123);
            StepVerifier.create(mono)
                    .then(() -> clock.set(Clock.offset(clock.get(), duration)))
                    .then(() -> testPublisher.emit("value").complete())
                    .expectNext("value")
                    .verifyComplete();
            verify(consumer).accept(duration);
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

            TestPublisher<Object> testPublisher = create();

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

            AtomicReference<Clock> clock = new AtomicReference<>(Clock.fixed(now(), UTC));

            TestPublisher<String> testPublisher = create();
            Flux<String> flux = testPublisher.flux()
                    .transform(doOnTerminateTimeFlux(clock::get, consumer));

            Duration duration = ofMillis(123);
            StepVerifier.create(flux)
                    .then(() -> clock.set(Clock.offset(clock.get(), duration)))
                    .then(() -> testPublisher.emit("first", "second", "third").complete())
                    .expectNext("first", "second", "third")
                    .verifyComplete();
            verify(consumer).accept(duration);
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

            Flux<Object> flux = create()
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

            TestPublisher<Object> testPublisher = create();

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
