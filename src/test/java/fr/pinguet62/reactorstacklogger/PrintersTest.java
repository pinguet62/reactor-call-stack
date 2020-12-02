package fr.pinguet62.reactorstacklogger;

import org.junit.jupiter.api.Test;

import static fr.pinguet62.reactorstacklogger.CallStack.Status.CANCELED;
import static fr.pinguet62.reactorstacklogger.CallStack.Status.ERROR;
import static fr.pinguet62.reactorstacklogger.CallStack.Status.SUCCESS;
import static fr.pinguet62.reactorstacklogger.Printers.simpleFormat;
import static java.time.Duration.ofMillis;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class PrintersTest {

    @Test
    void test_simpleFormat() {
        CallStack callStack = new CallStack("@RestController", SUCCESS, ofMillis(100), asList(
                new CallStack("@Service", SUCCESS, ofMillis(90), asList(
                        new CallStack("getProduct", SUCCESS, ofMillis(30), asList(
                                new CallStack("fromCache", SUCCESS, ofMillis(25), asList()),
                                new CallStack("slowWebservice", CANCELED, ofMillis(26), asList()))),
                        new CallStack("getPrice", ERROR, ofMillis(79), asList())))));

        assertThat(simpleFormat(callStack), is(
                "Total time: 100ms\n" +
                        "    ⮡ 90ms @Service\n" +
                        "        ⮡ 30ms getProduct\n" +
                        "            ⮡ 25ms fromCache\n" +
                        "            ⮡ 26ms slowWebservice (CANCELED)\n" +
                        "        ⮡ 79ms getPrice (ERROR)"));
    }
}
