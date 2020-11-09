package fr.pinguet62.reactorstacklogger;

public class Utils {
    public static String format(CallStack callStack) {
        StringBuilder stringBuilder = new StringBuilder();
        print(stringBuilder, callStack, 0);
        return stringBuilder.toString();
    }

    private static void print(StringBuilder stringBuilder, CallStack callStack, int index) {
        if (index == 0) {
            stringBuilder.append("Total time: ").append(callStack.getTime()).append("ms\n");
        } else {
            String indentation = repeat(" ", 2 * index);
            stringBuilder.append(indentation).append("⮡ ").append(callStack.getTime()).append("ms ").append(callStack.getName()).append("\n");
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
