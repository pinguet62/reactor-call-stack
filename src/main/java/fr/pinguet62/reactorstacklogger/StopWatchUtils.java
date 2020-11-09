package fr.pinguet62.reactorstacklogger;

import org.springframework.util.StopWatch;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.function.LongConsumer;
import java.util.function.UnaryOperator;

public class StopWatchUtils {

    public static <T> UnaryOperator<Mono<T>> doOnTerminateTimeMono(LongConsumer msConsumer) {
        return mono -> {
            StopWatch stopWatch = new StopWatch(UUID.randomUUID().toString());
            return mono
                    .doOnSubscribe(subscription -> stopWatch.start())
                    .doOnCancel(() -> {
                        stopWatch.stop();
                        msConsumer.accept(stopWatch.getTotalTimeMillis());
                    })
                    .doOnTerminate(() -> {
                        stopWatch.stop();
                        msConsumer.accept(stopWatch.getTotalTimeMillis());
                    });
        };
    }

    public static <T> UnaryOperator<Flux<T>> doOnTerminateTimeFlux(LongConsumer msConsumer) {
        return flux -> {
            StopWatch stopWatch = new StopWatch();
            return flux
                    .doOnSubscribe(subscription -> stopWatch.start())
                    .doOnCancel(() -> {
                        stopWatch.stop();
                        msConsumer.accept(stopWatch.getTotalTimeMillis());
                    })
                    .doOnTerminate(() -> {
                        stopWatch.stop();
                        msConsumer.accept(stopWatch.getTotalTimeMillis());
                    });
        };
    }
}
