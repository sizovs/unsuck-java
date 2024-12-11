package cleanbank.infra.fp;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public sealed interface Maybe<V, E extends Throwable> permits Maybe.Value, Maybe.Failure {

  static <V, E extends Throwable> Maybe<V, E> of(V value) {
    return new Maybe.Value<>(value);
  }

  static <V, E extends Throwable> Maybe<V, E> of(E exception) {
    return new Maybe.Failure<>(exception);
  }

  <V2> Maybe<V2, E> map(Function<? super V, ? extends V2> fn);

  <V2> Maybe<V2, E> flatMap(Function<? super V, Maybe<V2, E>> fn);

  boolean isPresent();

  boolean isEmpty();

  void ifPresent(Consumer<? super V> consumer);

  V orElse(V other);

  V orElseGet(Supplier<? extends V> supplier);

  V orElseThrow() throws E;

  <E2 extends Throwable> V orElseThrow(Function<? super E, E2> exceptionWrapper) throws E2;

  final class Value<V, E extends Throwable> implements Maybe<V, E> {

    private final V value;

    Value(V value) {
      this.value = requireNonNull(value, "Value cannot be null");
    }

    @Override
    public <V2> Maybe<V2, E> map(Function<? super V, ? extends V2> fn) {
      return of(fn.apply(value));
    }

    @Override
    public <V2> Maybe<V2, E> flatMap(Function<? super V, Maybe<V2, E>> fn) {
      return fn.apply(value);
    }

    @Override
    public boolean isPresent() {
      return true;
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public void ifPresent(Consumer<? super V> consumer) {
      consumer.accept(value);
    }

    @Override
    public V orElse(V other) {
      return value;
    }

    @Override
    public V orElseGet(Supplier<? extends V> supplier) {
      return value;
    }

    @Override
    public V orElseThrow() {
      return value;
    }

    @Override
    public <E2 extends Throwable> V orElseThrow(Function<? super E, E2> exceptionWrapper) {
      return value;
    }

    @Override
    public String toString() {
      return Objects.toString(value);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(value);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof Maybe.Value<?, ?> that) {
        return Objects.equals(this.value, that.value);
      }
      return false;
    }
  }

  final class Failure<V, E extends Throwable> implements Maybe<V, E> {

    private final E exception;

    Failure(E exception) {
      this.exception = requireNonNull(exception, "Exception cannot be null");
    }

    @Override
    public <V2> Maybe<V2, E> map(Function<? super V, ? extends V2> fn) {
      return of(exception);
    }

    @Override
    public <V2> Maybe<V2, E> flatMap(Function<? super V, Maybe<V2, E>> fn) {
      return of(exception);
    }

    @Override
    public boolean isPresent() {
      return false;
    }

    @Override
    public boolean isEmpty() {
      return true;
    }

    @Override
    public void ifPresent(Consumer<? super V> consumer) {
    }

    @Override
    public V orElse(V other) {
      return other;
    }

    @Override
    public V orElseGet(Supplier<? extends V> supplier) {
      return supplier.get();
    }

    @Override
    public V orElseThrow() throws E {
      throw exception;
    }

    @Override
    public <E2 extends Throwable> V orElseThrow(Function<? super E, E2> exceptionWrapper) throws E2 {
      throw exceptionWrapper.apply(exception);
    }

    @Override
    public String toString() {
      return Objects.toString(exception);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(exception);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof Maybe.Failure<?, ?> that) {
        return Objects.equals(exception, that.exception);
      }
      return false;
    }
  }

}
