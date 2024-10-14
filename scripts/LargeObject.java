package scripts;

import static scripts.Utils.*;

public class LargeObject {
    public static void main(String[] args) {
        gc();
        println("--- Initial memory state");
        printUsed();
        var array = new byte[100 * 1024 * 1024];
        println("--- array of " + array.length + " bytes is allocated");
        printUsed();
        gc();
        println("--- array is alive, GC performed");
        printUsed();
        array = null;

        gc();

        println("--- array is collected");
        printUsed();
        gc();
        println("--- Initial memory state");
        printUsed();
        array = new byte[100 * 1024 * 1024];
        println("--- array of " + array.length + " bytes is allocated");
        printUsed();
        gc();
        println("--- array is alive, GC performed");
        printUsed();
        array = null;

        gc();

        println("--- array is collected");
        printUsed();
    }

    private static void printUsed() {
        printMemoryPools(
            mp -> mp.getName().startsWith("G1 ") || mp.getName().startsWith("PS "),
            mp -> mp.getUsage().getUsed());
    }
}
