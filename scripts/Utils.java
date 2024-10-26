package scripts;

import com.sun.management.GarbageCollectionNotificationInfo;

import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.openmbean.CompositeData;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Utils {

    public static Map<String, String> HEAP_AREAS = Map.of(
            "G1 Eden Space", "eden",
            "G1 Survivor Space", "survivor",
            "G1 Old Gen", "old",
            "PS Eden Space", "eden",
            "PS Survivor Space", "survivor",
            "PS Old Gen", "old",
            "ZHeap", "old",
            "ZGC Old Generation", "old",
            "ZGC Young Generation", "eden"
    );

    private static void gcInternal(boolean print) {
        var start = System.currentTimeMillis();
        System.gc();
        var duration = System.currentTimeMillis() - start;
        if (print) {
            println("Full GC took " + duration + "ms");
        }
    }

    public static void gc() {
        gcInternal(true);
    }

    public static void gcAndSleep() {
        System.gc();
        try {
            Thread.sleep(2000);
        }
        catch (InterruptedException ignore) {
        }
    }

    public static void gcNoPrint() {
        gcInternal(false);
    }

    public static void println(Object o) {
        System.out.println(o);
    }

    public static MemoryPoolMXBean getMemoryPool(final String name) {
        final var normalized = HEAP_AREAS.entrySet().stream().filter(e -> e.getValue().equals(name)).map(Map.Entry::getKey).collect(Collectors.toSet());
        normalized.add(name);
        return ManagementFactory.getMemoryPoolMXBeans()
                .stream()
                .filter(mp -> normalized.contains(mp.getName()))
                .findFirst()
                .orElseThrow();
    }

    public static List<MemoryPoolMXBean> sortedMemoryPools() {
        return ManagementFactory.getMemoryPoolMXBeans()
                .stream()
                .sorted(Comparator.comparing(MemoryPoolMXBean::getName))
                .collect(Collectors.toList());
    }

    public static void registerGcPrint() {
        ManagementFactory.getGarbageCollectorMXBeans().forEach(gcbean -> {
            ((NotificationEmitter) gcbean).addNotificationListener(
                    (final Notification n, final Object handback) -> {
                        var sb = new StringBuilder("\n");
                        var info = GarbageCollectionNotificationInfo.from((CompositeData) n.getUserData());
                        sb.append("GC[%s] action=%s, cause=%s, duration=%s".formatted(info.getGcName(), info.getGcAction(), info.getGcCause(), info.getGcInfo().getDuration())).append("\n");
                        HEAP_AREAS.keySet().forEach(name -> {
                            var before = info.getGcInfo().getMemoryUsageBeforeGc().get(name);
                            var after = info.getGcInfo().getMemoryUsageAfterGc().get(name);
                            if (before != null && after != null) {
                                var diff = before.getUsed() - after.getUsed();
                                if (diff != 0) {
                                    sb.append("%s: diff %s, before=%s, after=%s".formatted(name, diff, before.getUsed(), after.getUsed())).append("\n");
                                }
                            }
                        });
                        println(sb.toString());
                    },
                    null,
                    null
            );
        });
    }

    public static boolean isHeapMemoryPool(MemoryPoolMXBean mp) {
        return mp.getType() == MemoryType.HEAP;
    }

    public static void printMemoryPools(
            Predicate<MemoryPoolMXBean> filter,
            java.util.function.ToLongFunction<MemoryPoolMXBean> extractor) {
        Columns.print(
                Arrays.asList("Pool Name", "Used*"),
                sortedMemoryPools().stream().filter(filter)
                        .map(mp -> Arrays.asList(mp.getName(), extractor.applyAsLong(mp))));
    }

    public static void printUsedConcise() {
        var areas = sortedMemoryPools()
                .stream()
                .filter(mp -> isHeapMemoryPool(mp) && mp.getUsage().getUsed() > 0)
                .map(mp -> "%s=%s".formatted(HEAP_AREAS.get(mp.getName()), mp.getUsage().getUsed()))
                .collect(Collectors.joining(", "));
        println("HEAP: %s".formatted(areas));
    }

    public static <T> void useUnused(T x) {
        if (Boolean.getBoolean("non-existing-property")) {
            println(x);
        }
    }

    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
