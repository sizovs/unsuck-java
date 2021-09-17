package effective.bank.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class FunctionMemoizer<F, T> {

  private final Map<F, T> memoizer = new ConcurrentHashMap<>(1, 1.0f);
  private final Function<F, T> nonMemoized;

  private FunctionMemoizer(Function<F, T> nonMemoized) {
    this.nonMemoized = nonMemoized;
  }

  private T memoize(F key) {
    return memoizer.computeIfAbsent(key, nonMemoized);
  }

  public static <F, T> Function<F, T> memoize(Function<F, T> nonMemoized) {
    return new FunctionMemoizer<>(nonMemoized)::memoize;
  }
}