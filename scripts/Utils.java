package scripts;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {
    public static void gc() {
        var startTime = System.currentTimeMillis();
        System.gc();
        System.gc();
        println("GC took " + (System.currentTimeMillis() - startTime) + "ms");
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
}
