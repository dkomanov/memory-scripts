package scripts;

import static scripts.Utils.printMemoryPools;

public class PrintMemoryPools {
    public static void main(String[] args) {
        printMemoryPools(mp -> true, mp -> mp.getUsage().getUsed());
    }
}
