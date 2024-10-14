package scripts;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Utils {
    private static void gcInternal(boolean print) {
        var start = System.currentTimeMillis();
        System.gc();
        System.gc();
        System.runFinalization();
        System.gc();
        var duration = System.currentTimeMillis() - start;
        if (print) {
            println("Full GC took " + duration + "ms");
        }
    }

    public static void gc() {
        gcInternal(true);
    }

    public static void gcNoPrint() {
        gcInternal(false);
    }

    public static void println(Object o) {
        System.out.println(o);
    }

    public static MemoryPoolMXBean getMemoryPool(String name) {
        return ManagementFactory.getMemoryPoolMXBeans()
                .stream()
                .filter(mp -> name.equals(mp.getName()))
                .findFirst()
                .orElseThrow();
    }

    public static List<MemoryPoolMXBean> sortedMemoryPools() {
        return ManagementFactory.getMemoryPoolMXBeans()
                .stream()
                .sorted(Comparator.comparing(mp -> mp.getName()))
                .collect(Collectors.toList());
    }

    public static void printMemoryPools(
            Predicate<MemoryPoolMXBean> filter,
            java.util.function.ToLongFunction<MemoryPoolMXBean> extractor) {
        Columns.print(
                Arrays.asList("Pool Name", "Used*"),
                sortedMemoryPools().stream().filter(filter)
                        .map(mp -> Arrays.asList(mp.getName(), extractor.applyAsLong(mp))));
    }
}
