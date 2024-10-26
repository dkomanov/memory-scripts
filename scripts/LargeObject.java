package scripts;

import java.util.stream.Collectors;

import static scripts.Utils.*;

public class LargeObject {
    private static void gcAndPrint(String name) {
        println(" --- " + name);
        printUsed();
        gcAndSleep();
        printUsed();
        println("");
        println("");
    }

    public static void main(String[] args) {
        Utils.registerGcPrint();
        var len = args.length == 1 ? Integer.parseInt(args[0]) : 100 * 1024 * 1024;

        println("-- Initial memory state");
        gcAndSleep();
        println("-- Warmup");
        for (int i = 0; i < 1_000_000; ++i) {
            var s = " ".repeat(100);
            if (s.length() > 100) {
                println("OMG! " + s);
            }
        }
        gcAndSleep();

        var task = new Task(len);
        new Thread(task, "task").start();
    }

    private static void printUsed() {
        var areas = sortedMemoryPools()
                .stream()
                .filter(mp -> isHeapMemoryPool(mp) && mp.getUsage().getUsed() > 0)
                .map(mp -> "%s=%s".formatted(HEAP_AREAS.get(mp.getName()), mp.getUsage().getUsed()))
                .collect(Collectors.joining(", "));
        println("HEAP: %s".formatted(areas));
    }

    private static class Task implements Runnable {
        public final int size;
        private String payload;

        public Task(int size) {
            this.size = size;
        }

        private void executeAction(String name, Runnable action) {
            println("BEFORE: " + name);
            printUsed();
            action.run();
            println(name);
            printUsed();
            gcAndSleep();
        }

        @Override
        public void run() {
            executeAction("payload = new String[%s]".formatted(size), () -> payload = "*".repeat(size));
            executeAction("payload = null", () -> payload = null);
            executeAction("payload = new String[%s]".formatted(size), () -> payload = " ".repeat(size));
            executeAction("payload = null", () -> payload = null);
        }
    }
}
