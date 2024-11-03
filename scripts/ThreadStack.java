package scripts;

import static scripts.Utils.println;

// javac -d out scripts/*.java && java -cp out -Xss1M -XX:NativeMemoryTracking=summary scripts.ThreadStack 80000
public class ThreadStack {
    private static int maxDepth = 0;

    public static void main(String[] args) {
        var n = Integer.parseInt(args[0]);
        waitForJcmd();

        for (int i = 100; i < n; i += 10) {
            try {
                makeDeepCall(i, false);
            }
            catch (StackOverflowError ignore) {
                maxDepth = i;
                println("OVERFLOW at " + maxDepth + "!");
                break;
            }
        }

        makeDeepCall(maxDepth - 1000, true);
    }

    private static int makeDeepCall(int n, boolean wait) {
        if (n != 0) {
            if (n == 1_000_000) {
                println("million");
            }
            return makeDeepCall(n - 1, wait);
        } else {
            if (wait) {
                waitForJcmd();
            }
            return 0;
        }
    }

    private static void waitForJcmd() {
        println("jcmd " + ProcessHandle.current().pid() + " VM.native_memory");
        System.console().readLine();
    }
}
