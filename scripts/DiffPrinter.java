package scripts;

import java.lang.management.MemoryPoolMXBean;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

import static scripts.Utils.*;

final class DiffPrinter {
    private static Map<String, Long> snapshot() {
        return sortedMemoryPools()
                .stream()
                .filter(Utils::isHeapMemoryPool)
                .collect(Collectors.toMap(MemoryPoolMXBean::getName, mp -> mp.getUsage().getUsed()));
    }

    private Map<String, Long> prev = snapshot();

    public void print() {
        record Pool(String name, long value, long diff) {}

        var curr = snapshot();
        var line = curr.entrySet()
                .stream()
                .map(e -> new Pool(e.getKey(), e.getValue(), e.getValue() - prev.get(e.getKey())))
                .filter(p -> p.value > 0 || p.diff != 0)
                .map(p -> "%s=%s [%s]".formatted(HEAP_AREAS.get(p.name), p.value, p.diff))
                .sorted(Comparator.comparing(s -> s))
                .collect(Collectors.joining(", "));
        prev = curr;
        println(">> %s".formatted(line));
    }
}
