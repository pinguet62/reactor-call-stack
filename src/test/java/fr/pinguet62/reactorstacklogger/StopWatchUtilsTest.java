package fr.pinguet62.reactorstacklogger;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Clock;
import java.time.Duration;
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
            Mono<String> mono = testPublisher.mono();
            Duration duration = ofMillis(123);
            StepVerifier.create(mono
                    .transform(doOnTerminateTimeMono(clock::get, consumer)))
                    .then(() -> clock.set(Clock.offset(clock.get(), duration)))
                    .then(() -> testPublisher.emit("value").complete())
                    .expectNext("value")
                    .verifyComplete();
            verify(consumer).accept(duration);
        }

        @Test
        void emptyPublisher() {
            Consumer<Duration> consumer = mock(Consumer.class);
            doNothing().when(consumer).accept(any());

            Mono<Object> mono = Mono.empty();
            StepVerifier.create(mono
                    .transform(doOnTerminateTimeMono(consumer)))
                    .verifyComplete();
            verify(consumer).accept(any());
        }

        @Test
        void error() {
            Consumer<Duration> consumer = mock(Consumer.class);
            doNothing().when(consumer).accept(any());

            Mono<Object> mono = Mono.error(new IllegalArgumentException("test"));
            StepVerifier.create(mono
                    .transform(doOnTerminateTimeMono(consumer)))
                    .verifyError(IllegalArgumentException.class);
            verify(consumer).accept(any());
        }

        @Test
        void canceled() {
            Consumer<Duration> consumer = mock(Consumer.class);
            doNothing().when(consumer).accept(any());

            AtomicReference<Subscription> subscription = new AtomicReference<>();
            Mono<Object> mono = TestPublisher.create()
                    .mono()
                    .doOnSubscribe(subscription::set);
            StepVerifier.create(mono
                    .transform(doOnTerminateTimeMono(consumer)))
                    .thenCancel()
                    .verify();
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
            Flux<String> flux = testPublisher.flux();
            Duration duration = ofMillis(123);
            StepVerifier.create(flux
                    .transform(doOnTerminateTimeFlux(clock::get, consumer)))
                    .then(() -> clock.set(Clock.offset(clock.get(), duration)))
                    .then(() -> testPublisher.emit("first", "second", "third").complete())
                    .expectNext("first", "second", "third")
                    .verifyComplete();
            verify(consumer).accept(duration);
        }

        @Test
        void emptyPublisher() {
            Consumer<Duration> consumer = mock(Consumer.class);
            doNothing().when(consumer).accept(any());

            Flux<Object> flux = Flux.empty();
            StepVerifier.create(flux
                    .transform(doOnTerminateTimeFlux(consumer)))
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
                    .flux();
            StepVerifier.create(flux
                    .transform(doOnTerminateTimeFlux(consumer)))
                    .verifyError(IllegalArgumentException.class);
            verify(consumer).accept(any());
        }

        @Test
        void canceled() {
            Consumer<Duration> consumer = mock(Consumer.class);
            doNothing().when(consumer).accept(any());

            AtomicReference<Subscription> subscription = new AtomicReference<>();
            Flux<Object> flux = TestPublisher.create()
                    .flux()
                    .doOnSubscribe(subscription::set);
            StepVerifier.create(flux
                    .transform(doOnTerminateTimeFlux(consumer)))
                    .thenCancel()
                    .verify();
            verify(consumer).accept(any());
        }
    }
}
