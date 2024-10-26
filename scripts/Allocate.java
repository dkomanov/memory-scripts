package scripts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static scripts.Utils.*;

// javac -d out scripts/*.java && java -cp out -Xmx128M -Xms128M -XX:+AlwaysPreTouch -Xlog:gc -XX:+UseG1GC scripts.Allocate
public class Allocate {
    public static void main(String[] args) {
        var smallRefs = new ArrayList<>(20);
        var bigRefs = new ArrayList<>(20);

        var p = new DiffPrinter();

        warmup(p);

        printAllHeapMemoryPools();

        for (int i = 0; i < 5; ++i) {
            smallRefs.add(executeAndPrintHeap(p, i + ": new Integer[ 10000]", () -> allocate(10000)));
        }

        executeAndPrintHeap(p, "clear small refs", () -> resetArrayList(smallRefs));
        gcAndPrint(p);

        for (int i = 0; i < 5; ++i) {
            smallRefs.add(executeAndPrintHeap(p, i + ": new Integer[ 10000]", () -> allocate(10000)));
            bigRefs.add(executeAndPrintHeap(p, i + ": new Integer[500000]", () -> allocate(500000)));
        }

        executeAndPrintHeap(p, "clear small refs", () -> resetArrayList(smallRefs));
        gcAndPrint(p);

        for (int i = 0; i < 5; ++i) {
            smallRefs.add(executeAndPrintHeap(p, i + ": new Integer[ 10000]", () -> allocate(10000)));
        }

        useUnused(smallRefs);
        useUnused(bigRefs);
    }

    private static Integer[] allocate(int count) {
        var array = new Integer[count];
        for (int i = 0; i < count; ++i) {
            array[i] = Integer.valueOf(1000 + i);
        }
        return array;
    }

    // allocates _at most_ `count` bytes (could be at most 12 bytes less).
    private static Integer[] allocateBytes(long count) {
        return allocate((int) count / 20);
    }

    private static void gcAndPrint(DiffPrinter p) {
        System.gc();
        p.print();
    }

    private static void printAllHeapMemoryPools() {
        Columns.print(
                Arrays.asList("Pool Name", "Used*", "Max*"),
                sortedMemoryPools()
                        .stream()
                        .filter(Utils::isHeapMemoryPool)
                        .map(mp -> Arrays.asList(mp.getName(), mp.getUsage().getUsed(), mp.getUsage().getMax()))
        );
    }

    private static void warmup(DiffPrinter p) {
        println("warmup (execute all actions and then GC)");
        printAllHeapMemoryPools();
        useUnused(allocate(10000));
        p.print();
        gcAndPrint(p);
        println("warmup completed\n\n\n");
        // The expectation here, that eden will become empty (used=0).
    }

    private static <T> Object resetArrayList(List<T> list) {
        list.replaceAll(ignored -> null);
        list.clear();
        return null;
    }

    private static <T> T executeAndPrintHeap(DiffPrinter p, String title, Supplier<T> action) {
        println(title);
        var r = action.get();
        sleep(1000);
        p.print();
        sleep(2000);
        return r;
    }
}
