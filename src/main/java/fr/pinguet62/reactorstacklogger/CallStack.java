package fr.pinguet62.reactorstacklogger;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PACKAGE;

@Getter
@EqualsAndHashCode
@ToString
public class CallStack {

    private final String name;

    private final List<CallStack> children;

    @Setter(PACKAGE)
    private Long time;

    CallStack(String name) {
        this.name = name;
        this.children = new ArrayList<>();
    }
}
