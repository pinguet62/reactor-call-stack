package fr.pinguet62.reactorstacklogger;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class StopWatchUtils {

    public static <T> UnaryOperator<Mono<T>> doOnTerminateTimeMono(Consumer<Duration> consumer) {
        return doOnTerminateTimeMono(() -> Clock.systemDefaultZone(), consumer);
    }

    public static <T> UnaryOperator<Mono<T>> doOnTerminateTimeMono(Supplier<Clock> clock, Consumer<Duration> consumer) {
        return mono -> {
            AtomicReference<Instant> start = new AtomicReference<>();
            return mono
                    .doOnSubscribe(subscription -> start.set(Instant.now(clock.get())))
                    .doOnCancel(() -> consumer.accept(Duration.between(start.get(), Instant.now(clock.get()))))
                    .doOnTerminate(() -> consumer.accept(Duration.between(start.get(), Instant.now(clock.get()))));
        };
    }

    public static <T> UnaryOperator<Flux<T>> doOnTerminateTimeFlux(Consumer<Duration> consumer) {
        return doOnTerminateTimeFlux(() -> Clock.systemDefaultZone(), consumer);
    }

    public static <T> UnaryOperator<Flux<T>> doOnTerminateTimeFlux(Supplier<Clock> clock, Consumer<Duration> consumer) {
        return flux -> {
            AtomicReference<Instant> start = new AtomicReference<>();
            return flux
                    .doOnSubscribe(subscription -> start.set(Instant.now(clock.get())))
                    .doOnCancel(() -> consumer.accept(Duration.between(start.get(), Instant.now(clock.get()))))
                    .doOnTerminate(() -> consumer.accept(Duration.between(start.get(), Instant.now(clock.get()))));
        };
    }
}
