package fr.pinguet62.reactorstacklogger;

import static fr.pinguet62.reactorstacklogger.CallStack.Status.SUCCESS;

public class Printers {

    public static String simpleFormat(CallStack callStack) {
        StringBuilder stringBuilder = new StringBuilder();
        print(stringBuilder, callStack, 0);
        return stringBuilder.toString();
    }

    private static void print(StringBuilder stringBuilder, CallStack callStack, int index) {
        if (index == 0) {
            stringBuilder
                    .append("Total time: ")
                    .append(callStack.getTime().toMillis()).append("ms");
            if (callStack.getStatus() != SUCCESS)
                stringBuilder.append(" (").append(callStack.getStatus()).append(")");
        } else {
            String indentation = repeat(" ", 4 * index);
            stringBuilder
                    .append("\n")
                    .append(indentation).append("⮡ ")
                    .append(callStack.getTime().toMillis()).append("ms ")
                    .append(callStack.getName());
            if (callStack.getStatus() != SUCCESS)
                stringBuilder.append(" (").append(callStack.getStatus()).append(")");
        }
        for (CallStack childCallStack : callStack.getChildren())
            print(stringBuilder, childCallStack, index + 1);
    }

    private static String repeat(String pattern, int count) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < count; i++)
            stringBuilder.append(pattern);
        return stringBuilder.toString();
    }
}
