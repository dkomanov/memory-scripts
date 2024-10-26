package scripts;

import java.util.ArrayList;
import java.util.function.Supplier;

import static scripts.Utils.*;

// javac -d out scripts/*.java && java -cp out -Xmx128M -Xms128M -XX:+AlwaysPreTouch -Xlog:gc -XX:+UseG1GC scripts.AllocateInfinitely
public class AllocateInfinitely {
    public static void main(String[] args) {
        var refs = new ArrayList<>(100);

        var p = new DiffPrinter();

        var allocationCount = 0;
        var oomCounter = 0;
        while (true) {
            ++allocationCount;
            useUnused(refs);
            try {
                refs.add(executeAndPrintHeap(p, allocationCount + ": new Integer[500000]", () -> allocateIntegerArray(500_000)));
            }
            catch (OutOfMemoryError oom) {
                var toRemove = oomCounter % refs.size();
                println("Removing " + toRemove + " of " + refs.size() + " because of: " + oom);
                for (int i = 0; i < toRemove; ++i) {
                    refs.set(0, null);
                    refs.remove(0);
                }
                ++oomCounter;
            }
        }
    }

    private static <T> T executeAndPrintHeap(DiffPrinter p, String title, Supplier<T> action) {
        println(title);
        var r = action.get();
        sleep(200);
        p.print();
        sleep(1000);
        return r;
    }
}
