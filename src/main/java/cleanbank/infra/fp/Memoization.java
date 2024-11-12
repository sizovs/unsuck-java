package cleanbank.infra.fp;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class Memoization {

  public static <F, T> Function<F, T> once(Function<F, T> computation) {
    var memoizer = new ConcurrentHashMap<F, T>(1, 1.0f);
    return fn -> memoizer.computeIfAbsent(fn, computation);
  }

}
