# Reactor Stack-logger

## The need of "complete call stack hierarchy"

### Problem of *asynchronous*

It's difficult to timing the *run time* of an asynchronous call.  
This doesn't work, because the execution is triggered at *subscription*:
```java
Mono<Object> getAsyncValue() {
    long start = System.currentTimeMillis();
    Mono<Object> result = callAsync();
    long end = System.currentTimeMillis();
    System.out.println(end - start); // 0ms
    return result;
}
```

A solution is to subscribe to start/end events of `Publisher`:
```java
Mono<Object> getAsyncValue() {
    AtomicReference<Long> start = new AtomicReference<>();
    return callAsync()
        .doOnSubscribe(x -> start.set(System.currentTimeMillis()))
        .doOnTerminate(() -> {
            long end = System.currentTimeMillis();
            System.out.println(end - start.get());
        });
}
```

### Need of *call stack*

Write calls individually in logs is not exploitable:
* insufficient: you don't know what function triggered it (need of *request id*)
* verbose: all calls are listed without order or hierarchy

A *call stack* like following is exploitable:
```
Total time: 620ms /productInfo
    ⮡ 608ms getProduct
        ⮡ 210ms callDatabase
        ⮡ 305ms callStockApi
    ⮡ 343ms getPrice
```

## Toolbox

### 1. Register call

It's necessary to define specific sections of stack trace.

#### Transformer

Use `appendCallStackToMono`/`appendCallStackToFlux` transformers:
```java
Mono<Object> getProduct() {
    return doSomething()
        .transform(Applicator.appendCallStackToMono("getProduct"));
}
```

#### Annotation & AOP

If you whant tag the entire method result,
you can use Spring support with `@LogTerminateTime`:
```java
@LogTerminateTime("getProduct")
Mono<Object> getProduct() {
    return doSomething();
}
```

### 2. Handle `Publisher` time execution

Use the `appendCallStackToMono`/`appendCallStackToFlux` transformers:
```java
asyncCall()
    .transform(Handler.doWithCallStackFlux(callStack -> System.out.println(callStack)))
```

## Full example

```java
@RestController
class SampleController {
    Mono<Product> getProduct() {
        return doSomething1()
            .transform(Applicator.appendCallStackToMono("getProduct"));
    }
    Mono<Price> getPrice() {
        return doSomething2()
            .transform(Applicator.appendCallStackToMono("getPrice"));
    }
    
    @GetMapping
    Mono<Info> getInfo() {
        return Mono.zip(
            getProduct(), getPrice(),
            (price, product) -> new Info(product, price));
    }
}

class CallStackFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(exchange)
                .transform(doWithCallStackMono(callStack -> System.out.println(callStack)));
    }
}
```

```
Total time: 620ms <root>
  ⮡ 608ms getProduct
  ⮡ 343ms getPrice
```
