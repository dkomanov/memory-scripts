package scripts;

import java.util.ArrayList;

import static scripts.Utils.*;

// javac -d out scripts/*.java && java -cp out -Xmx128M -Xms128M -XX:+AlwaysPreTouch -Xlog:gc -XX:+UseG1GC scripts.AllocateInfinitely
public class AllocateInfinitely {
    public static void main(String[] args) {
        var refs = new ArrayList<>(100);

        var p = new DiffPrinter(200, 1000);

        var allocationCount = 0;
        var oomCounter = 0;
        //noinspection InfiniteLoopStatement
        while (true) {
            ++allocationCount;
            useUnused(refs);
            try {
                refs.add(p.executeAndPrint(allocationCount + ": new Integer[500000]", () -> allocateIntegerArray(500_000)));
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
}
