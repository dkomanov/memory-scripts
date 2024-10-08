package scripts;

import static scripts.Utils.*;

public class PrintMemoryPools {
    public static void main(String[] args) {
        sortedMemoryPools().forEach(mp -> println(mp.getName()));
    }
}
