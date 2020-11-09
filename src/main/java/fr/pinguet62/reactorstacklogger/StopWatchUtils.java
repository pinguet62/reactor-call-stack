package fr.pinguet62.reactorstacklogger;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public class StopWatchUtils {

    public static <T> UnaryOperator<Mono<T>> doOnTerminateTimeMono(Consumer<Duration> consumer) {
        return mono -> {
            AtomicReference<Instant> start = new AtomicReference<>();
            return mono
                    .doOnSubscribe(subscription -> start.set(Instant.now()))
                    .doOnCancel(() -> consumer.accept(Duration.between(start.get(), Instant.now())))
                    .doOnTerminate(() -> consumer.accept(Duration.between(start.get(), Instant.now())));
        };
    }

    public static <T> UnaryOperator<Flux<T>> doOnTerminateTimeFlux(Consumer<Duration> consumer) {
        return flux -> {
            AtomicReference<Instant> start = new AtomicReference<>();
            return flux
                    .doOnSubscribe(subscription -> start.set(Instant.now()))
                    .doOnCancel(() -> consumer.accept(Duration.between(start.get(), Instant.now())))
                    .doOnTerminate(() -> consumer.accept(Duration.between(start.get(), Instant.now())));
        };
    }
}
