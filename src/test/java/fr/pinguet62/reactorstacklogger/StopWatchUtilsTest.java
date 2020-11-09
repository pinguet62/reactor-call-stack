package fr.pinguet62.reactorstacklogger;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.util.List;
import java.util.function.LongConsumer;

import static fr.pinguet62.reactorstacklogger.StopWatchUtils.doOnTerminateTimeFlux;
import static fr.pinguet62.reactorstacklogger.StopWatchUtils.doOnTerminateTimeMono;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class StopWatchUtilsTest {

    @Nested
    class doOnTerminateTimeMono {
        @Test
        void just() {
            LongConsumer consumer = mock(LongConsumer.class);
            doNothing().when(consumer).accept(anyLong());

            Mono<String> mono = Mono.just("value")
                    .transform(doOnTerminateTimeMono(consumer));

            StepVerifier.create(mono)
                    .expectNext("value")
                    .verifyComplete();
            verify(consumer).accept(anyLong());
        }

        @Test
        void empty() {
            LongConsumer consumer = mock(LongConsumer.class);
            doNothing().when(consumer).accept(anyLong());

            Mono<Object> mono = Mono.empty()
                    .transform(doOnTerminateTimeMono(consumer));

            StepVerifier.create(mono)
                    .verifyComplete();
            verify(consumer).accept(anyLong());
        }

        @Test
        void error() {
            LongConsumer consumer = mock(LongConsumer.class);
            doNothing().when(consumer).accept(anyLong());

            Mono<Object> mono = Mono.error(new IllegalArgumentException("test"))
                    .transform(doOnTerminateTimeMono(consumer));

            StepVerifier.create(mono)
                    .verifyError(IllegalArgumentException.class);
            verify(consumer).accept(anyLong());
        }

        @Test
        void cancel() {
            LongConsumer consumer = mock(LongConsumer.class);
            doNothing().when(consumer).accept(anyLong());

            TestPublisher<Object> testPublisher = TestPublisher.create();

            MonoProcessor<Object> mono = testPublisher.mono()
                    .transform(doOnTerminateTimeMono(consumer))
                    .toProcessor();

            mono.subscribe();
            mono.cancel();
            verify(consumer).accept(anyLong());
        }
    }

    @Nested
    class doOnTerminateTimeFlux {
        @Test
        void just() {
            LongConsumer consumer = mock(LongConsumer.class);
            doNothing().when(consumer).accept(anyLong());

            Flux<String> flux = Flux.just("first", "second", "third")
                    .transform(doOnTerminateTimeFlux(consumer));

            StepVerifier.create(flux)
                    .expectNext("first", "second", "third")
                    .verifyComplete();
            verify(consumer).accept(anyLong());
        }

        @Test
        void empty() {
            LongConsumer consumer = mock(LongConsumer.class);
            doNothing().when(consumer).accept(anyLong());

            Flux<Object> flux = Flux.empty()
                    .transform(doOnTerminateTimeFlux(consumer));

            StepVerifier.create(flux)
                    .verifyComplete();
            verify(consumer).accept(anyLong());
        }

        @Test
        void error() {
            LongConsumer consumer = mock(LongConsumer.class);
            doNothing().when(consumer).accept(anyLong());

            Flux<Object> flux = TestPublisher.create()
                    .emit("first")
                    .error(new IllegalArgumentException("test"))
                    .emit("second")
                    .flux()
                    .transform(doOnTerminateTimeFlux(consumer));

            StepVerifier.create(flux)
                    .verifyError(IllegalArgumentException.class);
            verify(consumer).accept(anyLong());
        }

        @Test
        void cancel() {
            LongConsumer consumer = mock(LongConsumer.class);
            doNothing().when(consumer).accept(anyLong());

            TestPublisher<Object> testPublisher = TestPublisher.create();

            MonoProcessor<List<Object>> mono = testPublisher.flux()
                    .transform(doOnTerminateTimeFlux(consumer))
                    .collectList()
                    .toProcessor();

            mono.subscribe();
            mono.cancel();
            verify(consumer).accept(anyLong());
        }
    }
}
