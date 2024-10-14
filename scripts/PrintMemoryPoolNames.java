package scripts;

import static scripts.Utils.*;

public class PrintMemoryPoolNames {
    public static void main(String[] args) {
        sortedMemoryPools().forEach(mp -> println(mp.getName()));
    }
}
