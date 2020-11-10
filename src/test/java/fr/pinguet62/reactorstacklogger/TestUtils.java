package fr.pinguet62.reactorstacklogger;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.time.Duration;

import static org.hamcrest.Matchers.any;

public class TestUtils {

    public static Matcher<CallStack> match(Matcher<String> nameMatcher, Matcher<Duration> timeMatcher, Matcher<? extends Iterable<? extends CallStack>> childrenMatcher) {
        return new TypeSafeDiagnosingMatcher<CallStack>() {
            @Override
            protected boolean matchesSafely(CallStack callStack, Description mismatch) {
                if (!nameMatcher.matches(callStack.getName())) {
                    mismatch.appendDescriptionOf(nameMatcher).appendText(" ");
                    nameMatcher.describeMismatch(callStack.getName(), mismatch);
                    return false;
                }
                if (!timeMatcher.matches(callStack.getTime())) {
                    mismatch.appendDescriptionOf(timeMatcher).appendText(" ");
                    timeMatcher.describeMismatch(callStack.getTime(), mismatch);
                    return false;
                }
                if (!childrenMatcher.matches(callStack.getChildren())) {
                    mismatch.appendDescriptionOf(childrenMatcher).appendText(" ");
                    childrenMatcher.describeMismatch(callStack.getChildren(), mismatch);
                    return false;
                }
                return true;
            }

            @Override
            public void describeTo(Description description) {
                description
                        .appendText("(")
                        .appendText("message ").appendDescriptionOf(nameMatcher)
                        .appendText(" and time ").appendDescriptionOf(timeMatcher)
                        .appendText(" and children ").appendDescriptionOf(childrenMatcher)
                        .appendText(")");
            }
        };
    }

    public static Matcher<CallStack> match(Matcher<String> nameMatcher, Matcher<? extends Iterable<? extends CallStack>> childrenMatcher) {
        return match(nameMatcher, any(Duration.class), childrenMatcher);
    }

    public static String format(CallStack callStack) {
        StringBuilder stringBuilder = new StringBuilder();
        print(stringBuilder, callStack, 0);
        return stringBuilder.toString();
    }

    private static void print(StringBuilder stringBuilder, CallStack callStack, int index) {
        if (index == 0) {
            stringBuilder.append("Total time: ").append(callStack.getTime().toMillis()).append("ms\n");
        } else {
            String indentation = repeat(" ", 4 * index);
            stringBuilder.append(indentation).append("⮡ ").append(callStack.getTime().toMillis()).append("ms ").append(callStack.getName()).append("\n");
        }
        for (CallStack childCallStack : callStack.getChildren()) {
            print(stringBuilder, childCallStack, index + 1);
        }
    }

    private static String repeat(String pattern, int count) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            stringBuilder.append(pattern);
        }
        return stringBuilder.toString();
    }
}
