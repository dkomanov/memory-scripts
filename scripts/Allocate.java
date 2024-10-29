package scripts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static scripts.Utils.*;

// javac -d out scripts/*.java && java -cp out -Xmx128M -Xms128M -XX:+AlwaysPreTouch -Xlog:gc -XX:+UseG1GC scripts.Allocate
public class Allocate {
    public static void main(String[] args) {
        var smallRefs = new ArrayList<>(20);
        var bigRefs = new ArrayList<>(20);

        var p = new DiffPrinter(1000, 2000);

        warmup(p);

        printAllHeapMemoryPools();

        for (int i = 0; i < 5; ++i) {
            smallRefs.add(p.executeAndPrint(i + ": new Integer[ 10000]", () -> allocateIntegerArray(10_000)));
        }

        p.executeAndPrint("clear small refs", () -> resetArrayList(smallRefs));
        gcAndPrint(p);

        for (int i = 0; i < 5; ++i) {
            smallRefs.add(p.executeAndPrint(i + ": new Integer[ 10000]", () -> allocateIntegerArray(10_000)));
            bigRefs.add(p.executeAndPrint(i + ": new Integer[500000]", () -> allocateIntegerArray(500_000)));
        }

        p.executeAndPrint("clear small refs", () -> resetArrayList(smallRefs));
        gcAndPrint(p);

        for (int i = 0; i < 5; ++i) {
            smallRefs.add(p.executeAndPrint(i + ": new Integer[ 10000]", () -> allocateIntegerArray(10_000)));
        }

        useUnused(smallRefs);
        useUnused(bigRefs);
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
        useUnused(allocateIntegerArray(10000));
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
}
