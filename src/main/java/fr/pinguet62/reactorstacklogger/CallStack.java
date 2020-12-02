package fr.pinguet62.reactorstacklogger;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PACKAGE;

@Getter
@EqualsAndHashCode
@ToString
public class CallStack {

    enum Status {
        SUCCESS, CANCELED, ERROR;
    }

    private final String name;

    private final List<CallStack> children;

    @Setter(PACKAGE)
    private Duration time;

    @Setter(PACKAGE)
    private Status status;

    CallStack(String name) {
        this.name = name;
        this.children = new ArrayList<>();
    }

    /**
     * For testing.
     */
    CallStack(String name, Status status, Duration time, List<CallStack> children) {
        this.name = name;
        this.status = status;
        this.time = time;
        this.children = children;
    }
}
