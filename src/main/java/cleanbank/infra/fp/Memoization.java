package cleanbank.infra.fp;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class Memoization {

  public static <F, T> Memoize<F, T> memoize(Function<F, T> computation) {
    var memoizer = new ConcurrentHashMap<F, T>(1, 1.0f);
    return fn -> memoizer.computeIfAbsent(fn, computation);
  }

  // This can be replaced by Function, but using a custom type results in a better naming:
  // memoize(accounts::notExistsByIban)::apply -> memoize(accounts::notExistsByIban)::once
  @FunctionalInterface
  public interface Memoize<T, R> {
    R once(T t);
  }

}
