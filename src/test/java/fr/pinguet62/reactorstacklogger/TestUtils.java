package fr.pinguet62.reactorstacklogger;

import fr.pinguet62.reactorstacklogger.CallStack.Status;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.time.Duration;

import static org.hamcrest.Matchers.any;

public class TestUtils {

    public static Matcher<CallStack> match(Matcher<String> nameMatcher, Matcher<Duration> timeMatcher, Matcher<Status> statusMatcher, Matcher<? extends Iterable<? extends CallStack>> childrenMatcher) {
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
                if (!statusMatcher.matches(callStack.getStatus())) {
                    mismatch.appendDescriptionOf(statusMatcher).appendText(" ");
                    statusMatcher.describeMismatch(callStack.getStatus(), mismatch);
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
                        .appendText(" and status ").appendDescriptionOf(statusMatcher)
                        .appendText(" and children ").appendDescriptionOf(childrenMatcher)
                        .appendText(")");
            }
        };
    }

    public static Matcher<CallStack> match(Matcher<String> nameMatcher, Matcher<? extends Iterable<? extends CallStack>> childrenMatcher) {
        return match(nameMatcher, any(Duration.class), any(Status.class), childrenMatcher);
    }

    public static Matcher<CallStack> match(Matcher<String> nameMatcher, Matcher<Status> statusMatcher, Matcher<? extends Iterable<? extends CallStack>> childrenMatcher) {
        return match(nameMatcher, any(Duration.class), statusMatcher, childrenMatcher);
    }
}
