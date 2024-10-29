package scripts;

import static scripts.Utils.*;

// javac -d out scripts/*.java && java -cp out -Xss136K scripts.ThreadStack 170
// javac -d out scripts/*.java && java -cp out -Xss256K scripts.ThreadStack 1400
// javac -d out scripts/*.java && java -cp out -Xss1M scripts.ThreadStack 7350
public class ThreadStack {
  public static void main(String[] args) {
    var n = Integer.parseInt(args[0]);
    new Thread(() -> {
      var a = new byte[10 * 1024];
      makeDeepCall(n);
      var b = new byte[10 * 1024];
      println("Completed: " + a.length + ", " + b.length);
    }).start();
  }

  private static int makeDeepCall(int n) {
    if (n != 0) {
      if (n == 1_000_000) {
        println("million");
      }
      return makeDeepCall(n - 1);
    } else {
      println(Thread.currentThread().getStackTrace().length);
      return 0;
    }
  }
}
